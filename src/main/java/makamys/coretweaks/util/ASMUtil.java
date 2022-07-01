package makamys.coretweaks.util;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

public class ASMUtil {
    
    public static int findLocalVariable(MethodNode m, Class<?> type, int ordinal) throws LocalVariableNotFoundException {
        return m.localVariables.stream().filter(v -> v.desc.equals("L" + Type.getInternalName(type) + ";")).skip(ordinal).findFirst().orElseThrow(() -> new LocalVariableNotFoundException()).index;
    }
    
    public static class LocalVariableNotFoundException extends Exception {

        private static final long serialVersionUID = -8487195122667750376L;
        
    }
    
}
