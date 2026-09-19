package org.example.buttonforload.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.buttonforload.dto.InnProcessingResult;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public abstract class BaseEgrulService {

    protected final RestTemplate userRestTemplate;
    protected static final String BASE_URL = "https://egrul.nalog.ru";
    protected static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    protected static final String REFERER = "https://egrul.nalog.ru/";

    protected boolean prepareFolder(String folder, List<String> innList, List<InnProcessingResult> results) {
        try {
            Path path = java.nio.file.Paths.get(folder);
            Files.createDirectories(path);
            return true;
        } catch (Exception e) {
            for (String inn : innList) {
                results.add(new InnProcessingResult(inn, "ERROR", "Не удалось подготовить папку выгрузки: %s"
                        .formatted(e.getMessage())));
            }
            return false;
        }
    }

    protected String initiateSearchTask(String inn) {
        String url = BASE_URL + "/";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("User-Agent", USER_AGENT);
        headers.set("Accept", "application/json, text/javascript, */*; q=0.01");
        headers.set("Referer", REFERER);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("query", inn);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        ResponseEntity<JsonNode> responseEntity = userRestTemplate.exchange(url, HttpMethod.POST, request, JsonNode.class);

        JsonNode response = responseEntity.getBody();
        if (response != null && response.has("t")) {
            return response.get("t").asText();
        }
        return null;
    }

    protected JsonNode waitForResponse(String taskToken) throws InterruptedException {
        String url = BASE_URL + "/search-result/" + taskToken;
        int maxAttempts = 10;

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            Thread.sleep(3500);

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", USER_AGENT);
            headers.set("Referer", REFERER);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            try {
                ResponseEntity<JsonNode> responseEntity = userRestTemplate.exchange(
                        url, HttpMethod.GET, request, JsonNode.class
                );
                JsonNode response = responseEntity.getBody();

                if (response != null) {
                    if ((response.has("rows") && response.get("rows").isArray()) || response.has("bkn")) {
                        Thread.sleep(3000);
                        return response;
                    }
                }
            } catch (Exception e) {
                log.warn("Попытка опроса ФНС {} не удалась: {}", attempt + 1, e.getMessage());
            }
        }
        return null;
    }

    protected void downloadAndSavePdf(String documentId, Path targetFilePath) throws IOException {
        String url = BASE_URL + "/vyp-download/" + documentId;
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", USER_AGENT);
        headers.set("Referer", REFERER);
        headers.set("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8");

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<byte[]> response = userRestTemplate.exchange(url, HttpMethod.GET, request, byte[].class);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Files.write(targetFilePath, response.getBody());
            log.info("Файл успешно скачан и сохранен: {}", targetFilePath.toAbsolutePath());
        } else {
            throw new IOException("Сервер ФНС вернул статус " + response.getStatusCode());
        }
    }
}
