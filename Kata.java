public class Kata {
    public static int findShort(String s) {
        s.chars();
        int res = 0;
        String[] str = s.split(" ");
        res = str[0].length();
        for (int i = 1; i < str.length; i++) {
            if (str[i].length() < res)
                res = str[i].length();
        }
        return res;
    }
}
