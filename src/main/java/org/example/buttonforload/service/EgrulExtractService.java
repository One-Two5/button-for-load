package org.example.buttonforload.service;

import lombok.extern.slf4j.Slf4j;
import org.example.buttonforload.dto.InnProcessingResult;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class EgrulExtractService extends BaseEgrulService{

    public EgrulExtractService(RestTemplate unsafeRestTemplate) {
        super(unsafeRestTemplate);
    }
    public List<InnProcessingResult> downloadPdfExtracts(List<String> innList, String outputFolder) {
        List<InnProcessingResult> results = new ArrayList<>();
        if (!prepareFolder(outputFolder, innList, results)) return results;

        for (String inn : innList) {
            try {
                log.info("[ВЫПИСКА] Старт обработки ИНН: {}", inn);
                String taskToken = initiateSearchTask(inn);
                if (taskToken == null) {
                    results.add(new InnProcessingResult(inn, "ERROR", "ИНН некорректен."));
                    continue;
                }

                JsonNode responseNode = waitForResponse(taskToken);
                if (responseNode != null && responseNode.has("rows") && !responseNode.get("rows").isEmpty()) {
                    String documentId = responseNode.get("rows").get(0).get("t").asText();
                    Path finalPath = Paths.get(outputFolder, "Выписка_" + inn + ".pdf");

                    try {
                        downloadAndSavePdf(documentId, finalPath);
                        results.add(new InnProcessingResult(inn, "SUCCESS",
                                "Выписка успешно сохранена по пути: %s".formatted(finalPath.toAbsolutePath())));
                    } catch (Exception e) {
                        log.error("ФНС сгенерировала ID выписки, но не смогла отдать файл для ИНН {}: {}", inn, e.getMessage());
                        results.add(new InnProcessingResult(inn, "ERROR", "Ошибка ФНС при генерации PDF: %s"
                                .formatted(e.getMessage())));
                    }
                } else {
                    results.add(new InnProcessingResult(inn, "ERROR", "Организация или ИП не найдены по ИНН."));
                }

                Thread.sleep(4000);
            } catch (InterruptedException e) {
                log.error("Процесс прерван", e);
                results.add(new InnProcessingResult(inn, "ERROR", "Процесс прерван сервером."));
                Thread.currentThread().interrupt();
                break;
            }
        }
        return results;
    }
}
