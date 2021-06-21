package makamys.toomanycrashes;

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

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.IClassTransformer;

public class ProfilerTransformer implements IClassTransformer {
	
	private static Map<String, List<String>> targets = new HashMap<>();
	private static List<MethodInstrumentationData> methodInstrumentationDatas = new ArrayList<>();
	
	static void init() {
		if(Config.methodsToProfile.isEmpty()) return;
		
		for(String methodStr : Config.methodsToProfile.split(",")) {
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
					FileUtils.write(new File("profiler-" + System.currentTimeMillis() + ".json"), String.join("\n", methodInstrumentationDatas.stream().map(d -> d.getDump()).collect(Collectors.toList())));
				} catch (IOException e) {
					System.err.println("Failed to write profiler data");
					e.printStackTrace();
				}
			}}, "TooManyCrashes profiler save thread"));
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
		
		ClassWriter writer = null;
		try {
			ClassNode classNode = new ClassNode();
			ClassReader classReader = new ClassReader(bytes);
			classReader.accept(classNode, 0);
			for(MethodNode m : classNode.methods) {
				// map from obf to searge if we're in a production environment
				String className = FMLDeobfuscatingRemapper.INSTANCE.map(classNode.name);
				String methodName = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(classNode.name, m.name, m.desc);
				String methodDesc = FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(m.desc);
				if(classTargets.contains(methodName)) {
					System.out.println("Instrumenting method " + methodName + " in class " + className);
					
					m.instructions.insert(new MethodInsnNode(INVOKESTATIC, "makamys/toomanycrashes/ProfilerTransformer", "preTargetCalled", "(I)V", false));
					m.instructions.insert(new IntInsnNode(SIPUSH, methodInstrumentationDatas.size()));
					methodInstrumentationDatas.add(new MethodInstrumentationData(className, methodName, methodDesc));
				}
			}
			
			writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
			classNode.accept(writer);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return writer.toByteArray();
	}
	
	public static void preTargetCalled(int id) {
		MethodInstrumentationData data = methodInstrumentationDatas.get(id);
		data.calls++;
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
