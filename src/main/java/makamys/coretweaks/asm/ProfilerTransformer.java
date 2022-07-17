package makamys.coretweaks.asm;

import static org.objectweb.asm.Opcodes.*;
import static makamys.coretweaks.CoreTweaks.LOGGER;

import java.util.List;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import makamys.coretweaks.diagnostics.MethodProfiler;
import makamys.coretweaks.diagnostics.MethodProfiler.MethodInstrumentationData;
import net.minecraft.launchwrapper.IClassTransformer;

/** Instruments classes so the profiler can profile them. */

public class ProfilerTransformer implements IClassTransformer {
	
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		List<String> classTargets = MethodProfiler.instance.targets.get(transformedName);
		if(classTargets != null) {
		    LOGGER.trace("Visiting class " + transformedName);
		    MethodProfiler.instance.clearDatasForClass(transformedName);
			basicClass = doTransform(basicClass, classTargets);
		}
		return basicClass;
	}

	private static byte[] doTransform(byte[] bytes, List<String> classTargets) {
		
		try {
			ClassNode classNode = new ClassNode();
			ClassReader classReader = new ClassReader(bytes);
			classReader.accept(classNode, 0);
			for(MethodNode m : classNode.methods) {
				String className = classNode.name;
				String methodName = m.name;
				String methodDesc = m.desc;
				if(classTargets.contains(methodName)) {
					LOGGER.info("Instrumenting method " + methodName + " in class " + className);
					
					m.instructions.insert(new MethodInsnNode(INVOKESTATIC, "makamys/coretweaks/diagnostics/MethodProfiler", "preTargetCalled", "(I)V", false));
					m.instructions.insert(new IntInsnNode(SIPUSH, MethodProfiler.instance.methodInstrumentationDatas.size()));
					MethodProfiler.instance.methodInstrumentationDatas.add(new MethodInstrumentationData(className, methodName, methodDesc));
				}
			}
			
			ClassWriter writer = new ClassWriter(0);
			classNode.accept(writer);
			return writer.toByteArray();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return bytes;
	}
	
}
