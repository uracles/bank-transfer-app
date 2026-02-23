package com.bank.transfer.app.util;


public class CodeGeneratorUtil {

    private static final String PRODUCT_PREFIX = "CMBPR";

    public static String generateProductCode(String lastCode) {
    int nextNumber = 1;

    if (lastCode != null && lastCode.startsWith(PRODUCT_PREFIX)) {
        String numPart = lastCode.substring(PRODUCT_PREFIX.length()); // after prefix
        nextNumber = Integer.parseInt(numPart) + 1;
    }

    return String.format(PRODUCT_PREFIX + "%04d", nextNumber);
    }
}