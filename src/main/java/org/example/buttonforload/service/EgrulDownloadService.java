package org.example.buttonforload.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.buttonforload.dto.InnProcessingResult;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EgrulDownloadService {

    private final RestTemplate unsafeRestTemplate;
    private static final String BASE_URL = "https://egrul.nalog.ru";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    private static final String REFERER = "https://egrul.nalog.ru/";

    public List<InnProcessingResult> downloadPdfExtracts(List<String> innList, String outputFolder) {
        List<InnProcessingResult> results = new ArrayList<>();
        try {
            Files.createDirectories(Paths.get(outputFolder));
        } catch (IOException e) {
            log.error("Не удалось создать папку для выгрузки: {}", e.getMessage());
            for (String inn : innList) {
                results.add(new InnProcessingResult(inn, "ERROR", "Не удалось создать целевую папку: %s"
                        .formatted(e.getMessage())));
            }
            return results;
        }

        for (String inn : innList) {
            try {
                log.info("Старт обработки ИНН: {}", inn);

                String taskToken = initiateSearchTask(inn);
                if (taskToken == null) {
                    results.add(new InnProcessingResult(
                            inn, "ERROR", "Не удалось запустить задачу поиска ФНС. Возможно ИНН некорректен."
                    ));
                    continue;
                }

                String documentId = waitForDocumentId(taskToken);
                if (documentId == null) {
                    results.add(new InnProcessingResult(
                            inn, "ERROR", "ФНС не сформировала документ в отведенное время."
                    ));
                    continue;
                }

                Path finalPath = Paths.get(outputFolder, "Выписка_" + inn +".pdf");
                downloadAndSavePdf(documentId, finalPath, inn);

                results.add(new InnProcessingResult(
                        inn, "SUCCESS", "Выписка успешно сохранена по пути: %s"
                        .formatted(finalPath.toAbsolutePath())
                ));

                Thread.sleep(3000);
            } catch (InterruptedException e) {
                log.error("Процесс обработки ИНН был прерван", e);
                results.add(new InnProcessingResult(inn, "ERROR", "Процесс обработки прерван сервером."));
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Ошибка при обработке ИНН {}: {}", inn, e.getMessage());
                results.add(new InnProcessingResult(inn, "ERROR", e.getMessage()));
            }
        }
        return results;
    }

    private String initiateSearchTask(String inn) {
        String url = BASE_URL + "/";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("User-Agent", USER_AGENT);
        headers.set("Accept", "application/json, text/javascript, */*; q=0.01");
        headers.set("Referer", REFERER);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("query", inn);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        ResponseEntity<JsonNode> responseEntity = unsafeRestTemplate.exchange(
                url, HttpMethod.POST, request, JsonNode.class
        );

        JsonNode response = responseEntity.getBody();
        if (response != null && response.has("t")) {
            return response.get("t").asText();
        }
        return null;
    }

    private String waitForDocumentId(String taskToken) throws InterruptedException {
        String url = BASE_URL + "/search-result/" + taskToken;
        int maxAttempts = 10;

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            Thread.sleep(3000);

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", USER_AGENT);
            headers.set("Referer", REFERER);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<JsonNode> responseEntity = unsafeRestTemplate.exchange(
                    url, HttpMethod.GET, request, JsonNode.class
            );

            JsonNode response = responseEntity.getBody();
            if (response != null && response.has("rows") && response.get("rows").isArray()) {
                JsonNode rows = response.get("rows");
                if (!rows.isEmpty()) {
                    JsonNode firstAgent = rows.get(0);
                    if (firstAgent.has("t")) {
                        return firstAgent.get("t").asText();
                    }
                }
            }
        }
        return null;
    }

    private void downloadAndSavePdf(String documentId,
                                    Path targetFilePath,
                                    String inn) throws IOException, InterruptedException {
        Thread.sleep(2000);

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", USER_AGENT);
        headers.set("Referer", REFERER);
        headers.set("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8");

        HttpEntity<Void> request = new HttpEntity<>(headers);
        
        ResponseEntity<byte[]> response;
        if (inn.length() == 12) {
            try {
                String url = BASE_URL + "/vyp-status/" + documentId;
                response = unsafeRestTemplate.exchange(url, HttpMethod.GET, request, byte[].class);

                if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                    throw new RemoteException("Справка об отсутствии ИП не найдена, пробуем выписку ЕГРИП");
                }
            } catch (Exception e) {
                String url = BASE_URL + "/vyp-download/" + documentId;
                response = unsafeRestTemplate.exchange(url, HttpMethod.GET, request, byte[].class);
            }
        } else {
            String url = BASE_URL + "/vyp-download/" + documentId;
            response = unsafeRestTemplate.exchange(url, HttpMethod.GET, request, byte[].class);
        }

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Files.write(targetFilePath, response.getBody());
            log.info("Файл успешно скачан и сохранен: {}", targetFilePath);
        } else {
            throw new IOException("Сервер ФНС вернул статус %s".formatted(response.getStatusCode()));
        }
    }
}
