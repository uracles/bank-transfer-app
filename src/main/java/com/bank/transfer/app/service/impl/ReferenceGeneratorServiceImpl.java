package com.bank.transfer.app.service.impl;

import com.bank.transfer.app.service.ReferenceGeneratorService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class ReferenceGeneratorServiceImpl implements ReferenceGeneratorService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Override
    public String generate() {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String uniquePart = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "TXN" + timestamp + uniquePart;
    }
}
