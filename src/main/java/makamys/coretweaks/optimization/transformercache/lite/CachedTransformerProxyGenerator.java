package makamys.coretweaks.optimization.transformercache.lite;

import java.lang.reflect.Method;

import org.apache.commons.io.FileUtils;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import makamys.coretweaks.CoreTweaks;
import makamys.coretweaks.util.Util;
import net.minecraft.launchwrapper.Launch;

public class CachedTransformerProxyGenerator implements Opcodes {
    private static final boolean DEBUG = Boolean.parseBoolean(System.getProperty("coretweaks.debugCachedTransformerProxyGenerator", "false"));
    
    public static Class<?> generate(Class<?> parent, Class<?> transClass) throws Exception {
        String newName = transClass.getName() + "$$CoreTweaksProxy";
        
        byte[] result = generateBytecode(parent.getName(), transClass.getName());
        
        if(DEBUG) {
            FileUtils.writeByteArrayToFile(Util.childFile(CoreTweaks.OUT_DIR, "DUMP__" + newName + ".class"), result);
        }
        
        Method defineClass = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
        defineClass.setAccessible(true);
        defineClass.invoke(Launch.classLoader, newName, result, 0, result.length);
                
        return Class.forName(newName);
    }
    
    private static byte[] generateBytecode(String parentName, String transName) {
        ClassWriter cw = new ClassWriter(0);
        FieldVisitor fv;
        MethodVisitor mv;
        AnnotationVisitor av0;
        
        // TODO copy IMixinSafe interface
        cw.visit(52, ACC_PUBLIC + ACC_SUPER,
                transName.replace('.', '/') + "$$CoreTweaksProxy", null,
                parentName.replace('.', '/'), null);

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
