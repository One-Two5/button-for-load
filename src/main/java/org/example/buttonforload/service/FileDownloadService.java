package org.example.buttonforload.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class FileDownloadService {

    private final RestTemplate unsafeRestTemplate;

    public FileDownloadService(RestTemplate unsafeRestTemplate) {
        this.unsafeRestTemplate = unsafeRestTemplate;
    }

    public byte[] download(String url) {
        return unsafeRestTemplate.getForObject(url, byte[].class);
    }
}