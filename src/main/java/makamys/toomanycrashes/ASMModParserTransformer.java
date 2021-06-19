package makamys.toomanycrashes;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import codechicken.lib.asm.ASMReader;
import net.minecraft.launchwrapper.IClassTransformer;

public class ASMModParserTransformer implements IClassTransformer {

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if(name.equals("cpw.mods.fml.common.discovery.asm.ASMModParser")) {
			basicClass = doTransform(basicClass);
		}
		return basicClass;
	}

	private static byte[] doTransform(byte[] bytes) {
		System.out.println("Transforming ASMModParser");
		
		boolean fail = true;
		ClassWriter writer = null;
		while(fail) {
			try {
				ClassNode classNode = new ClassNode();
				ClassReader classReader = new ClassReader(bytes);
				classReader.accept(classNode, 0);
				MethodNode emptyConstructor = new MethodNode(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
				
				emptyConstructor.instructions.add(ASMReader.loadResource("/assets/toomanycrashes/asm/tweaks.asm").get("ASMModParserEmptyConstructor").rawListCopy());
				classNode.methods.add(emptyConstructor);
				
				writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
				classNode.accept(writer);
				fail = false;
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return writer.toByteArray();
	}
}
