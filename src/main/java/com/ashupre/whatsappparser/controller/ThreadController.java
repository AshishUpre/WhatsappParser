package com.ashupre.whatsappparser.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ThreadController {

    @GetMapping("/curr/thread")
    public ResponseEntity<String> getCurrThreadInfo() {
        Thread currThread = Thread.currentThread();
        return ResponseEntity.ok((currThread.isVirtual() ? " Virtual thread" : "Not virtual"));
    }
}
