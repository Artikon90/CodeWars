public class isogram {
    public static boolean isIsogram(String str) {
        boolean result = true;
        str = str.toLowerCase();
            for (int i = 0; i < str.length(); i++) {
                if (i + 1 < str.length()) {
                    for (int j = i + 1; j < str.length(); j++) {
                        if (str.charAt(i) == str.charAt(j)) {
                            result = false;
                            break;
                        }
                    }
                }
            }
            return result;
    }
}
