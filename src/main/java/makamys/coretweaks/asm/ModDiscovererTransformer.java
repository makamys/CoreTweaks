package makamys.coretweaks.asm;

import static org.objectweb.asm.Opcodes.*;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import net.minecraft.launchwrapper.IClassTransformer;

public class ModDiscovererTransformer implements IClassTransformer {
    
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if(name.equals("cpw.mods.fml.common.discovery.ModDiscoverer")) {
            basicClass = doTransform(basicClass);
        }
        return basicClass;
    }

    private static byte[] doTransform(byte[] bytes) {
        System.out.println("Transforming ModDiscoverer to skip known libraries");
        boolean found = false;
        
        try {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(bytes);
            classReader.accept(classNode, 0);
            for(MethodNode m : classNode.methods) {
                if(m.name.equals("findClasspathMods")) {
                    Iterator<AbstractInsnNode> it = m.instructions.iterator();
                    AbstractInsnNode lastInsn = null;
                    while(it.hasNext()) {
                        AbstractInsnNode i = it.next();
                        if(i.getOpcode() == INVOKEINTERFACE) {
                            MethodInsnNode im = (MethodInsnNode)i;
                            if(im.owner.equals("java/util/List") && im.name.equals("contains") && im.desc.equals("(Ljava/lang/Object;)Z")) {
                                if(lastInsn.getOpcode() == INVOKEVIRTUAL) {
                                    MethodInsnNode lim = (MethodInsnNode)lastInsn;
                                    if(lim.owner.equals("java/io/File") && lim.name.equals("getName") && lim.desc.equals("()Ljava/lang/String;")) {
                                        m.instructions.insertBefore(lim, new InsnNode(DUP));
                                        
                                        InsnList callRedirectContains = new InsnList();
                                        callRedirectContains.add(new InsnNode(SWAP));
                                        callRedirectContains.add(new MethodInsnNode(INVOKESTATIC, "makamys/coretweaks/asm/ModDiscovererTransformer", "redirectKnownLibrariesContains", "(Ljava/util/List;Ljava/lang/String;Ljava/io/File;)Z", false));
                                        m.instructions.insertBefore(im, callRedirectContains);
                                        it.remove();
                                        found = true;
                                        break;
                                    }
                                }
                            }
                        }
                        lastInsn = i;
                    }
                }
            }
            
            if(found) {
                ClassWriter writer = new ClassWriter(0);
                classNode.accept(writer);
                return writer.toByteArray();
            } else {
                System.out.println("Couldn't find target instructions");
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        return bytes;
    }
    
    public static boolean redirectKnownLibrariesContains(List<String> list, String obj, File file) {
        assert file.getName().equals(obj);
        return list.contains(obj) || isDefaultLibrary(file);
    }
    
    /* From Forge 1.12.2-14.23.5.2847 */
    private static boolean isDefaultLibrary(File file)
    {
        String home = System.getProperty("java.home"); // Nullcheck just in case some JVM decides to be stupid
        if (home != null && file.getAbsolutePath().startsWith(home)) return true;
        // Should really pull this from the json somehow, but we dont have that at runtime.
        String name = file.getName();
        if (!name.endsWith(".jar")) return false;
        String[] prefixes =
        {
            "launchwrapper-",
            "asm-all-",
            "akka-actor_2.11-",
            "config-",
            "scala-",
            "jopt-simple-",
            "lzma-",
            "realms-",
            "httpclient-",
            "httpcore-",
            "vecmath-",
            "trove4j-",
            "icu4j-core-mojang-",
            "codecjorbis-",
            "codecwav-",
            "libraryjavawound-",
            "librarylwjglopenal-",
            "soundsystem-",
            "netty-all-",
            "guava-",
            "commons-lang3-",
            "commons-compress-",
            "commons-logging-",
            "commons-io-",
            "commons-codec-",
            "jinput-",
            "jutils-",
            "gson-",
            "authlib-",
            "log4j-api-",
            "log4j-core-",
            "lwjgl-",
            "lwjgl_util-",
            "twitch-",
            "jline-",
            "jna-",
            "platform-",
            "oshi-core-",
            "netty-",
            "libraryjavasound-",
            "fastutil-"
        };
        for (String s : prefixes)
        {
            if (name.startsWith(s)) return true;
        }
        return false;
    }
}
