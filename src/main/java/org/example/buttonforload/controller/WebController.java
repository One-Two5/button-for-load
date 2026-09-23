package org.example.buttonforload.controller;

import lombok.RequiredArgsConstructor;
import org.example.buttonforload.dto.ImportResultDto;
import org.example.buttonforload.dto.InnProcessingResult;
import org.example.buttonforload.service.EgrulDownloadService;
import org.example.buttonforload.service.FileImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final FileImportService fileImportService;
    private final EgrulDownloadService egrulDownloadService;

    @PostMapping("/import")
    public String importFile(Model model) {
        ImportResultDto resultDto = fileImportService.importFile();
        model.addAttribute("result", resultDto);
        return "index";
    }

    @PostMapping("/download")
    public ResponseEntity<List<InnProcessingResult>> downloadExtracts(@RequestBody List<String> innList,
                                                                      @RequestParam(defaultValue = "./downloads")
                                                                      String outputFolder) {
        List<InnProcessingResult> results = egrulDownloadService.downloadPdfExtracts(innList, outputFolder);
        return ResponseEntity.ok(results);
    }
}
