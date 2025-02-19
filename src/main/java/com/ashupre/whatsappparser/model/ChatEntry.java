package com.ashupre.whatsappparser.model;

import java.time.LocalDateTime;

/**
 * records are immutable, have setters automatically as function .<fieldname>()
 */
public record ChatEntry(LocalDateTime timestamp, String name, String message) {}