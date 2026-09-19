package org.example.buttonforload.service;

import lombok.extern.slf4j.Slf4j;
import org.example.buttonforload.dto.InnProcessingResult;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class EgrulCertificateService extends BaseEgrulService {

    public EgrulCertificateService(RestTemplate unsafeRestTemplate) {
        super(unsafeRestTemplate);
    }

    public List<InnProcessingResult> downloadPdfCertificates(List<String> innList, String outputFolder) {
        List<InnProcessingResult> results = new ArrayList<>();
        if (!prepareFolder(outputFolder, innList, results)) return results;

        for (String inn : innList) {
            try {
                log.info("Старт обработки ИНН: {}", inn);
                String taskToken = initiateSearchTask(inn);
                if (taskToken == null) {
                    results.add(new InnProcessingResult(inn, "ERROR", "ИНН некорректен."));
                    continue;
                }

                JsonNode responseNode = waitForResponse(taskToken);
                if (responseNode != null && responseNode.has("bkn")) {
                    String bknToken = responseNode.get("bkn").asText();
                    String documentId = initiateCertificateGeneration(bknToken);
                    if (documentId == null) {
                        results.add(new InnProcessingResult(inn, "ERROR", "Не удалось сгенерировать справку на стороне ФНС."));
                        continue;
                    }

                    Path finalPath = Paths.get(outputFolder, "Справка_" + inn + ".pdf");
                    downloadAndSavePdf(documentId, finalPath);

                    results.add(new InnProcessingResult(inn, "SUCCESS",
                            "Справка успешно сохранена по пути: %s".formatted(finalPath.toAbsolutePath())));
                } else {
                    results.add(new InnProcessingResult(inn, "ERROR", "По данному ИНН найдена активная организация/ИП. Справка об отсутствии невозможна."));
                }

                Thread.sleep(4000);
            } catch (InterruptedException e) {
                log.error("Процесс прерван", e);
                results.add(new InnProcessingResult(inn, "ERROR", "Процесс прерван сервером."));
                Thread.currentThread().interrupt();
                break;
            } catch (IOException e) {
                log.error("Ошибка ввода-вывода для ИНН {}: {}", inn, e.getMessage());
                results.add(new InnProcessingResult(inn, "ERROR", e.getMessage()));
            }
        }
        return results;
    }

    private String initiateCertificateGeneration(String bknToken) {
        String url = BASE_URL + "/bkn-processing";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("User-Agent", USER_AGENT);
        headers.set("Referer", REFERER);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("token", bknToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        try {
            ResponseEntity<JsonNode> responseEntity = userRestTemplate.exchange(url, HttpMethod.POST, request, JsonNode.class);
            JsonNode response = responseEntity.getBody();
            if (response != null && response.has("t")) {
                return response.get("t").asText();
            }
        } catch (Exception e) {
            log.error("Ошибка при запросе к /bkn-processing: {}", e.getMessage());
        }
        return null;
    }
}
