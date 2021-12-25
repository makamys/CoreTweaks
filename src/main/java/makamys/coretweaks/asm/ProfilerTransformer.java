package makamys.coretweaks.asm;

import static org.objectweb.asm.Opcodes.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import makamys.coretweaks.Config;
import makamys.coretweaks.CoreTweaks;
import makamys.coretweaks.util.Util;
import net.minecraft.launchwrapper.IClassTransformer;

/** Instruments classes so the profiler can profile them. */

public class ProfilerTransformer implements IClassTransformer {
	
	private static Map<String, List<String>> targets = new HashMap<>();
	private static List<MethodInstrumentationData> methodInstrumentationDatas = new ArrayList<>();
	
	public static void init() {
		if(Config.profilerMethods.isEmpty()) return;
		
		for(String methodStr : Config.profilerMethods.split(",")) {
			int lastDot = methodStr.lastIndexOf('.');
			String clazz = methodStr.substring(0, lastDot);
			String method = methodStr.substring(lastDot + 1);
			List<String> classTargets = targets.get(clazz);
			if(classTargets == null) {
				classTargets = new ArrayList<>();
				targets.put(clazz, classTargets);
			}
			
			classTargets.add(method);
		}
		
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					FileUtils.write(Util.childFile(CoreTweaks.OUT_DIR, "profiler-" + System.currentTimeMillis() + ".json"), String.join("\n", methodInstrumentationDatas.stream().map(d -> d.getDump()).collect(Collectors.toList())));
				} catch (IOException e) {
					System.err.println("Failed to write profiler data");
					e.printStackTrace();
				}
			}}, "CoreTweaks profiler save thread"));
	}
	
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		List<String> classTargets = targets.get(transformedName);
		if(classTargets != null) {
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
					System.out.println("Instrumenting method " + methodName + " in class " + className);
					
					m.instructions.insert(new MethodInsnNode(INVOKESTATIC, "makamys/coretweaks/asm/ProfilerTransformer", "preTargetCalled", "(I)V", false));
					m.instructions.insert(new IntInsnNode(SIPUSH, methodInstrumentationDatas.size()));
					methodInstrumentationDatas.add(new MethodInstrumentationData(className, methodName, methodDesc));
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
	
	public static void preTargetCalled(int id) {
		MethodInstrumentationData data = methodInstrumentationDatas.get(id);
		data.calls++;
	}
	
	public static boolean isActive() {
		return !Config.profilerMethods.isEmpty();
	}
	
	private static class MethodInstrumentationData {
		String owner;
		String name;
		String desc;
		int calls;
		
		public MethodInstrumentationData(String owner, String name, String desc) {
			this.owner = owner;
			this.name = name;
			this.desc = desc;
		}
		
		public String getDump() {
			return "\"" + owner + ";" + name + desc + "\": {\n" +
					"  calls: " + calls + "\n"
					+ "}\n";
		}
	}
}
