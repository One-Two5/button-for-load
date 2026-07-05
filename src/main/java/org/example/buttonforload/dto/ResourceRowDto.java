package org.example.buttonforload.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ResourceRowDto {

    private Integer number;
    private String fullName;
    private String inclusionGrounds;
    private LocalDate inclusionDecisionDate;
    private LocalDate exclusionDecisionDate;

}
