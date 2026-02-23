package com.bank.transfer.app.util;

public class AmountToWords {
    private AmountToWords() {}

    private static final String[] ONES = {
            "", "one", "two", "three", "four",
            "five", "six", "seven", "eight",
            "nine", "ten", "eleven", "twelve",
            "thirteen", "fourteen", "fifteen",
            "sixteen", "seventeen", "eighteen",
            "nineteen"
    };

    private static final String[] TENS = {
            "",          // 0
            "",          // 1
            "twenty",    // 2
            "thirty",    // 3
            "forty",     // 4
            "fifty",     // 5
            "sixty",     // 6
            "seventy",   // 7
            "eighty",    // 8
            "ninety"     // 9
    };

    public static String getMoneyIntoWords(final double money) {
        long dollar = (long) money;
        long cents = Math.round((money - dollar) * 100);
        if (money == 0D) {
            return "";
        }
        if (money < 0) {
            return "";
        }
        String dollarPart = "";
        if (dollar > 0) {
            dollarPart = convert(dollar) + " Naira";
        }
        String centsPart = "";
        if (cents > 0) {
            if (!dollarPart.isEmpty()) {
                centsPart = " and ";
            }
            centsPart += convert(cents) + " Kobo";
        }
        return dollarPart + centsPart;
    }

    private static String convert(final long n) {
        if (n < 0) {
            return "";
        }
        if (n < 20) {
            return ONES[(int) n];
        }
        if (n < 100) {
            return TENS[(int) n / 10] + ((n % 10 != 0) ? " " : "") + ONES[(int) n % 10];
        }
        if (n < 1000) {
            return ONES[(int) n / 100] + " hundred" + ((n % 100 != 0) ? " " : "") + convert(n % 100);
        }
        if (n < 1_000_000) {
            return convert(n / 1000) + " thousand" + ((n % 1000 != 0) ? " " : "") + convert(n % 1000);
        }
        if (n < 1_000_000_000) {
            return convert(n / 1_000_000) + " million" + ((n % 1_000_000 != 0) ? " " : "") + convert(n % 1_000_000);
        }
        return convert(n / 1_000_000_000) + " billion" + ((n % 1_000_000_000 != 0) ? " " : "") + convert(n % 1_000_000_000);
    }
}
