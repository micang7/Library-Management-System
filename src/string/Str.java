package string;

import java.util.HashMap;

public abstract class Str {
    private static final HashMap<String, Integer> letters_order = new HashMap<>() {{
        String order = "aąbcćdeęfghijklłmnńoópqrsśtuvwxyzźż";
        for (int i = 0; i<order.length(); i++) {
            put(order.charAt(i)+"", i);
        }
    }};

    public static String adjustToWidth(String str, int width, char filler, boolean atEnd) {
        while (str.length() < width)
            str = atEnd ? str + filler : filler + str;
        return str;
    }

    public static int compare(String str1, String str2) {
        for (int i = 0; i < Math.min(str1.length(), str2.length()); i++) {
            try {
                int compare = letters_order.get((str1.charAt(i) + "").toLowerCase()) - letters_order.get((str2.charAt(i) + "").toLowerCase());
                if (compare != 0) {
                    return compare;
                }
            } catch (NullPointerException _) {}
        }
        return str1.length() - str2.length();
    }
}
