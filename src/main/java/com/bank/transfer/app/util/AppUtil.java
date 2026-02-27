package com.bank.transfer.app.util;

import com.bank.transfer.app.exception.CustomException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

@Component
@Slf4j
public class AppUtil {
    public static final String EMAIL_REGEX = "(?:[a-zA-Z0-9-!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+))|^$";
    public static final String PHONE_REGEX = "^[0|+234]\\d{10}|^$";
    private static final String ALPHABET_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String NUMERIC_CHARACTERS = "0123456789";
    private static final String ALPHANUMERIC_CHARACTERS = ALPHABET_CHARACTERS + NUMERIC_CHARACTERS;
    private static final String LOCALHOST_IPV4 = "127.0.0.1";
    private static final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";
    private static final String UNKNOWN = "unknown";


    public static String formatLocalDateTime(LocalDateTime dateTime, String pattern, boolean maskYear) {
        if (dateTime == null) return "N/A";
        String date = dateTime.format(DateTimeFormatter.ofPattern(pattern));
        return maskYear ? "YYYY" + date.substring(4) : date;
    }

    public static LocalDate strToLocalDate(String dateStr, String pattern) {
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
        } catch (Exception e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, String.format("Invalid Date Format, expected '%s'", pattern));
        }
    }

    public static String localDateToString(LocalDate localDate, String pattern) {
        return localDate.format(DateTimeFormatter.ofPattern(pattern));
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isBlank();
    }

    public static String[] getIgnorePropertyNames(Object source) {
        final BeanWrapper beanWrapper = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = beanWrapper.getPropertyDescriptors();

        Set<String> nullValues = new HashSet<>();
        for (java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = beanWrapper.getPropertyValue(pd.getName());
            if (srcValue == null || ((srcValue instanceof String string) && string.isBlank())) {
                nullValues.add(pd.getName());
            }
        }

        String[] result = new String[nullValues.size()];
        return nullValues.toArray(result);
    }

    public static LocalDateTime parseStringToLocalDateTime(String str, boolean isStart) {
        try {
            return LocalDateTime.parse(str + (isStart ? "T00:00:00" : "T23:59:59"));
        } catch (Exception e) {
            throw new CustomException(String.format("Invalid Date Format '%s', valid format - '%s'", str, "yyyy-MM-dd"));
        }
    }

    public static <T> PagedResponse<T> buildPagedResponse(int page, int size, Page<?> result, T content) {

        return PagedResponse.<T>builder()
                .success(true)
                .message(String.format("%s Record(s) Found", result.getTotalElements()))
                .data(content)
                .currentPage(page)
                .size(size)
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .isFirst(result.isFirst())
                .isLast(result.isLast())
                .build();
    }

    public static void validateDateRange(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            throw new CustomException("Invalid Date Range");
        }
    }

    public static String dateToString(LocalDateTime dateTime, String pattern) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
        return dateTime.format(dateTimeFormatter);
    }

    public static String generateOtp(int length) {
        SecureRandom secureRandom = new SecureRandom();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < length; i++) {
            otp.append(secureRandom.nextInt(0, 10));
        }
        return otp.toString();
    }

    public static String generateTransactionRef() {
        String ref = "066001" + dateToString(LocalDateTime.now(), "yyyyMMddHHmmssSSSS") + generateRandomNumeric(10);
        return ref.substring(0, 30);
    }

    public static void writeToFile(String text) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("output.html"))) {
            writer.write(text);
        } catch (IOException e) {
            log.error("Could not write to file...");
        }
    }

    public static String readFile(String filename) {
        try (InputStream inputStream = AppUtil.class.getResourceAsStream("/" + filename)) {
            // Use resource
            return readFromInputStream(inputStream);
        } catch (Exception e) {
            log.error("Could not read file...");
            return "";
        }
    }

    public static String readFromInputStream(InputStream inputStream) throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }
        return resultStringBuilder.toString().trim();
    }

    public static String formatPhoneNumber(String phoneNumber) {
        phoneNumber = phoneNumber.replace("+", "");
        int n = phoneNumber.length();
        return "234" + (n < 10 ? phoneNumber : phoneNumber.substring(n - 10, n));
    }

    public static void validateEqualValues(@NonNull String val1,
                                           String val2,
                                           @NonNull String valName) {
        if (!val1.equals(val2)) {
            throw new CustomException(String.format("%s and Confirm%s mismatch", valName, valName));
        }
    }

    public static String maskPhoneNo(String phoneNo) {
        String toMask = phoneNo.substring(3, 7);
        return phoneNo.replace(toMask, "*".repeat(toMask.length()));
    }

    public static String generateRandomNumeric(int length) {
        if (length < 1) {
            throw new IllegalArgumentException("Length must be greater than 0");
        }

        SecureRandom secureRandom = new SecureRandom();
        StringBuilder result = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int randomIndex = secureRandom.nextInt(NUMERIC_CHARACTERS.length());
            result.append(NUMERIC_CHARACTERS.charAt(randomIndex));
        }

        return result.toString();
    }

    public static String generateRandomAlphaNumeric(int length) {
        if (length < 1) {
            throw new IllegalArgumentException("Length must be greater than 0");
        }

        SecureRandom secureRandom = new SecureRandom();
        StringBuilder result = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int randomIndex = secureRandom.nextInt(ALPHANUMERIC_CHARACTERS.length());
            result.append(ALPHANUMERIC_CHARACTERS.charAt(randomIndex));
        }

        return result.toString().toUpperCase();
    }

    public static String maskPhoneNumber(String phoneNumber) {
        String toMask = phoneNumber.substring(3, 8);
        return phoneNumber.replace(toMask, "*".repeat(toMask.length()));
    }

    public static void handleEmptyOrHtmlResponseBody(String responseBody) {
        if (!StringUtils.hasText(responseBody) || responseBody.toLowerCase().contains("html")) {
            throw new CustomException(HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable");
        }
    }

    public static BigDecimal amountTo2dp(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_EVEN);
    }

    public static String formatAmount(BigDecimal amount) {
        if (amount == null) {
            amount = BigDecimal.ZERO;
        }
        String[] formatted = amount.setScale(2, RoundingMode.HALF_EVEN).toString().split("\\.");
        StringBuilder result = new StringBuilder();
        int count = 1;
        for (int i = formatted[0].length() - 1; i >= 0; i--) {
            result.insert(0, formatted[0].charAt(i));
            if (count % 3 == 0 && i > 0) {
                result.insert(0, ",");
            }
            count++;
        }
        return result + "." + formatted[1];
    }

    public static String formatAmount(BigDecimal amount, boolean addCurr) {
        if (amount == null) {
            amount = BigDecimal.ZERO;
        }
        String[] formatted = amount.setScale(2, RoundingMode.HALF_EVEN).toString().split("\\.");
        StringBuilder result = new StringBuilder();
        int count = 1;
        for (int i = formatted[0].length() - 1; i >= 0; i--) {
            result.insert(0, formatted[0].charAt(i));
            if (count % 3 == 0 && i > 0) {
                result.insert(0, ",");
            }
            count++;
        }
        return (addCurr ? "₦" : "") + result + "." + formatted[1];
    }

}
