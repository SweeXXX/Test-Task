package Classes;

public class NumericProcess {
    public static boolean isNumber(String s) {
        try {
            Long.parseLong(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    public static boolean isNumericString(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }

        int start = 0;
        int length = s.length();

        // Проверка знака
        if (s.charAt(0) == '-') {
            if (length == 1) return false; // Только минус
            start = 1;
        }

        // Проверка всех символов после знака
        for (int i = start; i < length; i++) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }
}
