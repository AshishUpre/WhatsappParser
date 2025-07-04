package com.ashupre;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.util.Map;

public class SendEmailLambda implements RequestHandler<Map<String, String>, String> {

    @Override
    public String handleRequest(Map<String, String> event, Context context) {
        String name = event.get("name");
        String email = event.get("email");

        System.out.println("Lamdba, sending email to " + email + " with name " + name);

        return "Sent email to " + email;
    }
}
