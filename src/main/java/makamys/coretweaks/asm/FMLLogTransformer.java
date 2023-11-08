package makamys.coretweaks.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import cpw.mods.fml.relauncher.FMLRelaunchLog;
import makamys.coretweaks.bugfix.SafeFMLLog;

import static org.objectweb.asm.Opcodes.*;

import org.apache.logging.log4j.Level;

import static makamys.coretweaks.CoreTweaks.LOGGER;

/**
 * <p>Redirects FMLRelaunchLog#log to a safer version that shouldn't crash.
 * <p>Log4j (version 2.0-beta9) is known to crash when Logger#log is called with a throwable whose stack trace contains
 *    classes from a mod which contains a tweaker. Log4j tries to load all classes in the stack trace
 *    so it can display which jar they originate from. But in this case, classes from the mod are on the
 *    parent class loader's class path (since the mod contains a tweaker), while the Minecraft classes
 *    it references are not (only LaunchClassLoader remaps the names of these). Thus a ClassNotFoundError
 *    happens when trying to load these classes, which log4j does not catch.
 * <p>We use a transformer instead of a mixin because FML is a minefield for mixin errors.
 */
public class FMLLogTransformer implements IClassTransformer {
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if(basicClass == null) return null;

        if(name.equals("cpw.mods.fml.common.FMLLog")) {
            return transformFMLLog(basicClass);
        }
        return basicClass;
    }

    /**
     * <pre>
     * public static void log(String targetLog, Level level, Throwable ex, String format, Object... data)
     * {
     * -   coreLog.log(targetLog, level, ex, format, data);
     * +   FMLLogTransformer.Hooks.redirectLog(targetLog, level, ex, format, data, coreLog);
     * }
     * 
     * public static void log(Level level, Throwable ex, String format, Object... data)
     * {
     * -   coreLog.log(level, ex, format, data);
     * +   FMLLogTransformer.Hooks.redirectLog(level, ex, format, data, coreLog);
     * }
     * </pre>
     */
    private static byte[] transformFMLLog(byte[] bytes) {
        LOGGER.info("Transforming FMLLog#log to fix ClassNotFoundError when logging throwables");

        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);
        for(MethodNode m : classNode.methods) {
            if (m.name.equals("log") && (m.desc.equals("(Ljava/lang/String;Lorg/apache/logging/log4j/Level;Ljava/lang/Throwable;Ljava/lang/String;[Ljava/lang/Object;)V") || m.desc.equals("(Lorg/apache/logging/log4j/Level;Ljava/lang/Throwable;Ljava/lang/String;[Ljava/lang/Object;)V"))) {
                AbstractInsnNode injectionTarget = null;
                for(int i = 0; i < m.instructions.size(); i++) {
                    AbstractInsnNode ain = m.instructions.get(i);
                    if(ain instanceof MethodInsnNode) {
                        MethodInsnNode min = (MethodInsnNode)ain;
                        if(min.getOpcode() == INVOKESTATIC && min.owner.equals("cpw/mods/fml/relauncher/FMLRelaunchLog") && min.name.equals("log") && min.desc.equals(m.desc)) {
                            injectionTarget = ain;
                            break;
                        }
                    }
                }

                if(injectionTarget != null) {
                    m.instructions.insertBefore(injectionTarget, new FieldInsnNode(GETSTATIC, "cpw/mods/fml/common/FMLLog", "coreLog", "Lcpw/mods/fml/relauncher/FMLRelaunchLog;"));
                    m.instructions.set(injectionTarget, new MethodInsnNode(INVOKESTATIC, "makamys/coretweaks/asm/FMLLogTransformer$Hooks", "redirectLog", m.desc.replace(")", "Lcpw/mods/fml/relauncher/FMLRelaunchLog;)"), false));
                }
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(writer);
        return writer.toByteArray();
    }

    public static class Hooks {

        public static void redirectLog(String targetLog, Level level, Throwable ex, String format, Object[] data, FMLRelaunchLog coreLog) {
            SafeFMLLog.log(coreLog, targetLog, level, ex, format, data);
        }
        
        public static void redirectLog(Level level, Throwable ex, String format, Object[] data, FMLRelaunchLog coreLog) {
            SafeFMLLog.log(coreLog, level, ex, format, data);
        }
    }
}
