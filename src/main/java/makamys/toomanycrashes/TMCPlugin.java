package makamys.toomanycrashes;

import java.util.Map;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import net.minecraft.launchwrapper.IClassTransformer;

public class TMCPlugin implements IFMLLoadingPlugin {

	public TMCPlugin() {
		System.out.println("Instantiating TMCPlugin");
	}
	
	@Override
	public String[] getASMTransformerClass() {
		// TODO Auto-generated method stub
		return new String[] {"makamys.toomanycrashes.ASMModParserTransformer"};
	}

	@Override
	public String getModContainerClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSetupClass() {
		JarDiscovererCache.load();
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getAccessTransformerClass() {
		// TODO Auto-generated method stub
		return null;
	}
	

	
}
