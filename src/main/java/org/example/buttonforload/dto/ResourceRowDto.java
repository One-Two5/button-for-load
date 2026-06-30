package org.example.buttonforload.dto;

import java.time.LocalDate;

public class ResourceRowDto {

    private Integer number;
    private String fullName;
    private String inclusionGrounds;
    private LocalDate inclusionDecisionDate;
    private LocalDate exclusionDecisionDate;

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getInclusionGrounds() {
        return inclusionGrounds;
    }

    public void setInclusionGrounds(String inclusionGrounds) {
        this.inclusionGrounds = inclusionGrounds;
    }

    public LocalDate getInclusionDecisionDate() {
        return inclusionDecisionDate;
    }

    public void setInclusionDecisionDate(LocalDate inclusionDecisionDate) {
        this.inclusionDecisionDate = inclusionDecisionDate;
    }

    public LocalDate getExclusionDecisionDate() {
        return exclusionDecisionDate;
    }

    public void setExclusionDecisionDate(LocalDate exclusionDecisionDate) {
        this.exclusionDecisionDate = exclusionDecisionDate;
    }
}
