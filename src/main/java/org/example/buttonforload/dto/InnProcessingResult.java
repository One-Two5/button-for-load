package org.example.buttonforload.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InnProcessingResult {

    private String inn;
    private String status;
    private String message;
}
