package makamys.coretweaks.util;

import java.io.File;

public class Util {

    public static File childFile(File parent, String childName) {
        parent.mkdirs();
        return new File(parent, childName);
    }
    
    public static boolean isClassPresent(String className) {
        return Util.class.getResource("/" + className.replace('.', '/') + ".class") != null;
    }
    
}
