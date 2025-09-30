package com.ashupre.whatsappparser.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvocationType;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;

@Service
public class EmailService {

    public void sendWelcomeEmail(String name, String email) {
        String jsonInput = String.format("{\"name\":\"%s\", \"email\":\"%s\"}", name, email);
        InvokeRequest request = InvokeRequest.builder()
                // this is the online AWS lambda function name, not handler name of that function
                // (handler - I wrote in java - jdk 21 [present in Lambda module])
                .functionName("sendWelcomeEmailWithInstructions")
                // this is async invocation - fire and forget, dont care about result
                .invocationType(InvocationType.EVENT)
                .payload(SdkBytes.fromUtf8String(jsonInput))
                .build();

        try(LambdaClient client = LambdaClient.builder().region(Region.AP_SOUTH_1).build()) {
            client.invoke(request);
        }
    }

}
