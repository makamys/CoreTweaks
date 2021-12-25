package makamys.coretweaks.asm;

import static org.objectweb.asm.Opcodes.*;

import java.util.Iterator;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import net.minecraft.launchwrapper.IClassTransformer;

public class FMLFastSplashTransformer implements IClassTransformer {
    
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if(name.equals("cpw.mods.fml.common.ProgressManager$ProgressBar")) {
            basicClass = doTransformProgressBar(basicClass);
        }
        return basicClass;
    }
 
    private static byte[] doTransformProgressBar(byte[] bytes) {
        System.out.println("FMLFastSplashTransformer: Transforming ProgressManager$ProgressBar");
        
        try {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(bytes);
            classReader.accept(classNode, 0);
            for(MethodNode m : classNode.methods) {
                if(m.name.equals("step") && m.desc.equals("(Ljava/lang/String;)V")) {
                    Iterator<AbstractInsnNode> it = m.instructions.iterator();
                    while(it.hasNext()) {
                        AbstractInsnNode i = it.next();
                        if(i.getOpcode() == INVOKEVIRTUAL) {
                            MethodInsnNode mi = (MethodInsnNode)i;
                            if(mi.owner.equals("cpw/mods/fml/common/FMLCommonHandler") && mi.name.equals("processWindowMessages") && mi.desc.equals("()V")) {
                                m.instructions.insertBefore(mi, new InsnNode(POP));
                                it.remove();
                                break;
                            }
                        }
                    }
                }
            }
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            classNode.accept(writer);
            return writer.toByteArray();
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        return bytes;
    }
}
