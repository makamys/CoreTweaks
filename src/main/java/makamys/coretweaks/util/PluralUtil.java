package makamys.coretweaks.util;

/** Very important */
public class PluralUtil {
    public static String pluralSuffix(int number) {
        return pluralSuffix(number, "s");
    }
    
    public static String pluralSuffix(int number, String suffix) {
        return number == 1 ? "" : suffix;
    }

    /**
     * Examples:
     * <blockquote><pre>
     * PluralUtil.pluralizeCount(3, "cat") returns "3 cats"
     * PluralUtil.pluralizeCount(1, "dog") returns "1 dog"
     * </pre></blockquote>
     */
    public static String pluralizeCount(int number, String noun) {
        return "" + number + " " + noun + pluralSuffix(number); 
    }
}
