package br.com.nimblebaas.payment_gateway.helpers;

public class StringHelper {

    private StringHelper() {}

    public static String onlyNumbers(String string) {
        return string.replaceAll("\\D", "");
    }
}
