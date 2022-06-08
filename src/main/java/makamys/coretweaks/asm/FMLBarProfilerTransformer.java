package makamys.coretweaks.asm;

import static org.objectweb.asm.Opcodes.*;
import static makamys.coretweaks.CoreTweaks.LOGGER;

import java.util.Iterator;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraft.launchwrapper.IClassTransformer;

public class FMLBarProfilerTransformer implements IClassTransformer {
    
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if(name.equals("cpw.mods.fml.common.ProgressManager")) {
            basicClass = doTransformProgressManager(basicClass);
        } else if(name.equals("cpw.mods.fml.common.ProgressManager$ProgressBar")) {
            basicClass = doTransformProgressBar(basicClass);
        }
        return basicClass;
    }

    private static byte[] doTransformProgressManager(byte[] bytes) {
        LOGGER.info("FMLBarProfiler: Transforming ProgressManager");
        
        try {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(bytes);
            classReader.accept(classNode, 0);
            for(MethodNode m : classNode.methods) {
                if(m.name.equals("push") && m.desc.equals("(Ljava/lang/String;IZ)Lcpw/mods/fml/common/ProgressManager$ProgressBar;")) {
                    InsnList postList = new InsnList();
                    postList.add(new InsnNode(DUP));
                    postList.add(new MethodInsnNode(INVOKESTATIC, "makamys/coretweaks/diagnostics/FMLBarProfiler", "instance", "()Lmakamys/coretweaks/diagnostics/FMLBarProfiler;", false));
                    postList.add(new InsnNode(SWAP));
                    postList.add(new MethodInsnNode(INVOKEVIRTUAL, "makamys/coretweaks/diagnostics/FMLBarProfiler", "onPush", "(Lcpw/mods/fml/common/ProgressManager$ProgressBar;)V", false));    
                    
                    Iterator<AbstractInsnNode> it = m.instructions.iterator();
                    while(it.hasNext()) {
                        AbstractInsnNode i = it.next();
                        if(i.getOpcode() == ARETURN) {
                            m.instructions.insertBefore(i, postList);
                            break;
                        }
                    }
                } else if(m.name.equals("pop")) {
                    InsnList preList = new InsnList();
                    preList.add(new MethodInsnNode(INVOKESTATIC, "makamys/coretweaks/diagnostics/FMLBarProfiler", "instance", "()Lmakamys/coretweaks/diagnostics/FMLBarProfiler;", false));
                    preList.add(new VarInsnNode(ALOAD, 0));
                    preList.add(new MethodInsnNode(INVOKEVIRTUAL, "makamys/coretweaks/diagnostics/FMLBarProfiler", "onPop", "(Lcpw/mods/fml/common/ProgressManager$ProgressBar;)V", false));    
                    
                    m.instructions.insert(preList);
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
    
    private static byte[] doTransformProgressBar(byte[] bytes) {
        LOGGER.info("FMLBarProfiler: Transforming ProgressManager$ProgressBar");
        
        try {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(bytes);
            classReader.accept(classNode, 0);
            for(MethodNode m : classNode.methods) {
                if(m.name.equals("step") && m.desc.equals("(Ljava/lang/String;)V")) {
                    InsnList postList = new InsnList();
                    postList.add(new MethodInsnNode(INVOKESTATIC, "makamys/coretweaks/diagnostics/FMLBarProfiler", "instance", "()Lmakamys/coretweaks/diagnostics/FMLBarProfiler;", false));
                    postList.add(new VarInsnNode(ALOAD, 0));
                    postList.add(new InsnNode(DUP));
                    postList.add(new FieldInsnNode(GETFIELD, "cpw/mods/fml/common/ProgressManager$ProgressBar", "timeEachStep", "Z"));
                    postList.add(new MethodInsnNode(INVOKEVIRTUAL, "makamys/coretweaks/diagnostics/FMLBarProfiler", "onStep", "(Lcpw/mods/fml/common/ProgressManager$ProgressBar;Z)V", false));    
                    
                    Iterator<AbstractInsnNode> it = m.instructions.iterator();
                    while(it.hasNext()) {
                        AbstractInsnNode i = it.next();
                        if(i.getOpcode() == RETURN) {
                            m.instructions.insertBefore(i, postList);
                            break;
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
