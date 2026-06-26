package org.example.buttonforload.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class FileDownloadService {

    private final RestTemplate restTemplate;

    public FileDownloadService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public byte[] download(String url) {
        return  restTemplate.getForObject(url, byte[].class);
    }
}
