package makamys.toomanycrashes;

import net.minecraft.launchwrapper.IClassTransformer;

public class TestTransformer implements IClassTransformer {

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if(name.equals("cpw.mods.fml.common.discovery.asm.ASMModParser")) {
			System.out.println("pep");
		}
		return basicClass;
	}
	
}