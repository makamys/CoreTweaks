package makamys.coretweaks.util;

/** Very important */
public class PluralUtil {
    public static String pluralSuffix(int number) {
        return pluralSuffix(number, "s");
    }
    
    public static String pluralSuffix(int number, String suffix) {
        return number == 1 ? "" : suffix;
    }
}
