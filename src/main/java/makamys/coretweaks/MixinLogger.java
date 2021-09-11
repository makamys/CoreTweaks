package makamys.coretweaks;

import java.util.HashSet;
import java.util.Set;

public class MixinLogger {
    
    static Set<Class> printed = new HashSet<>(); 
    
    public static void printActive(Object obj) {
        if(Config.printActive && !printed.contains(obj.getClass())) {
            System.out.println("Mixin for " + obj.getClass() + " is active");
            printed.add(obj.getClass());
        }
    }
    
}
