package org.example.buttonforload.controller;

import lombok.AllArgsConstructor;
import org.example.buttonforload.dto.ImportResultDto;
import org.example.buttonforload.service.FileImportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@AllArgsConstructor
public class WebController {

    private final FileImportService fileImportService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("result", null);
        return "index";
    }

    @PostMapping("/import")
    public String importFile(Model model) {
        ImportResultDto resultDto = fileImportService.importFile();
        model.addAttribute("result", resultDto);
        return "index";
    }
}
