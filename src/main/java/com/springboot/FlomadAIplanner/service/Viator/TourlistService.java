package com.springboot.FlomadAIplanner.service.Viator;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;


@Service
public class TourlistService {
    private static final Logger logger = Logger.getLogger(TourlistService.class.getName());
    @Value("${viator.api.key}")
    private String apiKey;

    @Value("${viator.api.url}")
    private String apiUrl;



    // @Value("${viator.reseller.id}")
    private int resellerId=1000;

    // @Value("${viator.supplier.id}")
    private int supplierId=1004;



    private final RestTemplate restTemplate;

    public TourlistService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getTourList(String destinationId, int topX) {

        TourListRequest.Data requestData = new TourListRequest.Data();
        requestData.setApiKey(apiKey);
        requestData.setResellerId(resellerId);
        requestData.setSupplierId(supplierId);
        requestData.setExternalReference("10051374722994001");
        // requestData.setTimestamp(LocalDateTime.now());
        // 2013-12-10T13:30:54.616+10:00
        requestData.setTimestamp("2013-12-10T13:30:54.616+10:00");

        TourListRequest request = new TourListRequest();
        request.setRequestType("TourListRequest");
        request.setData(requestData);


        

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.set("Accept-Version", "v1"); 

        HttpEntity<TourListRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, String.class);
        logger.info(response.getBody());
        return response.getBody();


        
        // String url = String.format("%s?destId=%s&topX=%d", apiUrl, destinationId, topX);
        // HttpHeaders headers = new HttpHeaders();
        // headers.set("exp-api-key", apiKey);
        // headers.set("Content-Type", "application/json");
        // HttpEntity<String> entity = new HttpEntity<>(headers);

        // ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        

    }
}
