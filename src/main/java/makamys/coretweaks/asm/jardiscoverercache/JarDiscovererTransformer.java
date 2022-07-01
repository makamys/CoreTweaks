package makamys.coretweaks.asm.jardiscoverercache;

import static makamys.coretweaks.CoreTweaks.LOGGER;
import static org.objectweb.asm.Opcodes.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.ModContainerFactory;
import cpw.mods.fml.common.discovery.ModCandidate;
import cpw.mods.fml.common.discovery.asm.ASMModParser;
import makamys.coretweaks.optimization.JarDiscovererCache;
import makamys.coretweaks.optimization.JarDiscovererCache.CachedModInfo;
import makamys.coretweaks.util.ASMUtil;
import net.minecraft.launchwrapper.IClassTransformer;

public class JarDiscovererTransformer implements IClassTransformer {
    
    private static ZipEntry lastZipEntry;
    
    private static String lastHash;
    private static CachedModInfo lastCMI;
    
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if(name.equals("cpw.mods.fml.common.discovery.JarDiscoverer")) {
            basicClass = doTransform(basicClass);
        }
        return basicClass;
    }

    private static byte[] doTransform(byte[] bytes) {
        System.out.println("Transforming JarDiscoverer to use cached values");
        
        try {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(bytes);
            classReader.accept(classNode, 0);
            
            for(MethodNode m : classNode.methods) {
                if(m.name.equals("discover")) {
                    final int VAR_IDX_MOD_CANDIDATE = 1;
                    final int VAR_IDX_JAR = ASMUtil.findLocalVariable(m, JarFile.class, 0);
                    final int VAR_IDX_ZE = ASMUtil.findLocalVariable(m, ZipEntry.class, 0);
                    final int VAR_IDX_MOD_PARSER = ASMUtil.findLocalVariable(m, ASMModParser.class, 0);
                    
                    // add hook to preDiscover at the beginning of the method
                    
                    InsnList preList = new InsnList();
                    preList.add(new VarInsnNode(ALOAD, VAR_IDX_MOD_CANDIDATE));
                    preList.add(new MethodInsnNode(INVOKESTATIC, "makamys/coretweaks/asm/jardiscoverercache/JarDiscovererTransformer", "preDiscover", "(Lcpw/mods/fml/common/discovery/ModCandidate;)V", false));
                    m.instructions.insert(preList);
                    
                    // redirect ASMModParser construction to constructModParser
                    
                    AbstractInsnNode newASMModParser = null;
                    AbstractInsnNode storeModParser = null;
                    {
                        Iterator<AbstractInsnNode> it = m.instructions.iterator();
                        while(it.hasNext()) {
                            AbstractInsnNode i = it.next();
                            if(i.getOpcode() == NEW && ((TypeInsnNode)i).desc.equals("cpw/mods/fml/common/discovery/asm/ASMModParser")) {
                                if(newASMModParser != null) {
                                    throw new IllegalStateException("Multiple NEW ASMModParser instructions found");
                                }
                                newASMModParser = i;
                            } else if(i.getOpcode() == ASTORE && ((VarInsnNode)i).var == VAR_IDX_MOD_PARSER) {
                                if(storeModParser != null) {
                                    throw new IllegalStateException("Multiple ASTORE modParser instructions found");
                                }
                                storeModParser = i;
                            }
                        }
                    }
                    if(newASMModParser == null) throw new IllegalStateException("Couldn't find NEW ASMModParser instruction");
                    if(storeModParser == null) throw new IllegalStateException("Couldn't find ASTORE modParser instruction");
                    
                    {
                        Iterator<AbstractInsnNode> it = m.instructions.iterator();
                        boolean deleting = false;
                        while(it.hasNext()) {
                            AbstractInsnNode i = it.next();
                            if(i.equals(storeModParser)) {
                                break;
                            }
                            if(i.equals(newASMModParser)) {
                                deleting = true;
                            }
                            if(deleting) {
                                it.remove();
                            }
                        }
                    }
                    InsnList callConstructNewASMModParser = new InsnList();
                    callConstructNewASMModParser.add(new VarInsnNode(ALOAD, VAR_IDX_JAR));
                    callConstructNewASMModParser.add(new VarInsnNode(ALOAD, VAR_IDX_ZE));
                    callConstructNewASMModParser.add(new MethodInsnNode(INVOKESTATIC, "makamys/coretweaks/asm/jardiscoverercache/JarDiscovererTransformer", "constructASMModParser", "(Ljava/util/jar/JarFile;Ljava/util/zip/ZipEntry;)Lcpw/mods/fml/common/discovery/asm/ASMModParser;", false));
                    m.instructions.insertBefore(storeModParser, callConstructNewASMModParser);
                    
                    // redirect ModContainer construction to constructASMModParser
                    
                    {
                        Iterator<AbstractInsnNode> it = m.instructions.iterator();
                        while(it.hasNext()) {
                            AbstractInsnNode i = it.next();
                            if(i.getOpcode() == INVOKEVIRTUAL) {
                                MethodInsnNode im = (MethodInsnNode)i;
                                if(im.owner.equals("cpw/mods/fml/common/ModContainerFactory") && im.name.equals("build") && im.desc.equals("(Lcpw/mods/fml/common/discovery/asm/ASMModParser;Ljava/io/File;Lcpw/mods/fml/common/discovery/ModCandidate;)Lcpw/mods/fml/common/ModContainer;")) {
                                    it.remove();
                                    m.instructions.insertBefore(it.next(), new MethodInsnNode(INVOKESTATIC, "makamys/coretweaks/asm/jardiscoverercache/JarDiscovererTransformer", "redirectBuild", "(Lcpw/mods/fml/common/ModContainerFactory;Lcpw/mods/fml/common/discovery/asm/ASMModParser;Ljava/io/File;Lcpw/mods/fml/common/discovery/ModCandidate;)Lcpw/mods/fml/common/ModContainer;", false));
                                    break;
                                }
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
    
    /** Load the saved result if the jar's path and modification date haven't changed. */
    public static void preDiscover(ModCandidate candidate) {
        String hash = null;
        File file = candidate.getModContainer();
        hash = file.getPath() + "@" + file.lastModified();
        
        lastHash = hash;
        lastCMI = JarDiscovererCache.getCachedModInfo(lastHash);
        
        LOGGER.debug("preDiscover " + candidate.getModContainer() + "(hash " + lastHash + ")");
    }
    
    /** Try to load cached ASMModParser instead of creating a new one. */
    public static ASMModParser constructASMModParser(JarFile jar, ZipEntry ze) throws IOException {
        lastZipEntry = ze;
        ASMModParser parser = lastCMI.getCachedParser(lastZipEntry);
        if(parser == null) {
            try(InputStream is = jar.getInputStream(lastZipEntry)) {
                parser = new ASMModParser(is);
            }
            lastCMI.putParser(lastZipEntry, parser);
        }
        return parser;
    }
    
    /** Remember if the ModContainer was null last time; if it was, return null instead of trying to create one. */ 
    public static ModContainer redirectBuild(ModContainerFactory factory, ASMModParser modParser, File modSource, ModCandidate container) {
        int isModClass = lastCMI.getCachedIsModClass(lastZipEntry);
        ModContainer mc = null;
        if(isModClass != 0) {
            mc = factory.build(modParser, modSource, container);
            if(isModClass == -1) {
                lastCMI.putIsModClass(lastZipEntry, mc != null);
            }
        }
        return mc;
    }
}
