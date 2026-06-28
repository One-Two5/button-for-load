package org.example.buttonforload.dto;

public class ImportResultDto {

    private String message;

    public ImportResultDto(String message) {
        this.message = message;
    }

    public ImportResultDto() {}

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
