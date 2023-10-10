package makamys.coretweaks.optimization.transformercache.lite;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import net.minecraft.launchwrapper.Launch;

public class CachedTransformerProxyGenerator implements Opcodes {
    private static Set<String> registeredNames = new HashSet<>();
    private static final boolean isMixingasmPresent;
    static {
        isMixingasmPresent = CachedTransformerProxyGenerator.class.getResource("/makamys/mixingasm/api/IMixinSafeTransformer.class") != null;
    }
    
    public static Class<?> generate(Class<?> parent, String transName) throws Exception {
        String newName = parent.getName() + "$$" + transName;
        int discriminator = 0;
        while(registeredNames.contains(newName + (discriminator == 0 ? "": "$" + discriminator))) {
            discriminator++;
        }
        newName = newName + (discriminator == 0 ? "": "$" + discriminator);
        
        byte[] result = generateBytecode(parent.getName(), transName);
        
        FileUtils.writeByteArrayToFile(new File("output.class"), result);
        
        Method defineClass = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
        defineClass.setAccessible(true);
        defineClass.invoke(Launch.classLoader, newName, result, 0, result.length);
        
        registeredNames.add(newName);        
        return Class.forName(newName);
    }
    
    private static byte[] generateBytecode(String parentName, String transName) {
        ClassWriter cw = new ClassWriter(0);
        FieldVisitor fv;
        MethodVisitor mv;
        AnnotationVisitor av0;
        
        // TODO decide this in a better way...
        boolean mixinSafe = transName.equals("DeobfuscationTransformer");

        cw.visit(52, ACC_PUBLIC + ACC_SUPER,
                parentName.replace('.', '/') + "$$" + transName, null,
                parentName.replace('.', '/'),
                (isMixingasmPresent && mixinSafe) ? new String[] {"makamys/mixingasm/api/IMixinSafeTransformer"} : null);

        {
            mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(Lnet/minecraft/launchwrapper/IClassTransformer;)V", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKESPECIAL,
                    parentName.replace('.', '/'), "<init>",
                    "(Lnet/minecraft/launchwrapper/IClassTransformer;)V", false);
            mv.visitInsn(RETURN);
            mv.visitMaxs(2, 2);
            mv.visitEnd();
        }
        cw.visitEnd();

        return cw.toByteArray();
    }
}
