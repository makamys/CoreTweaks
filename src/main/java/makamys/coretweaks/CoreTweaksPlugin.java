package makamys.coretweaks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import makamys.coretweaks.asm.ProfilerTransformer;
import makamys.coretweaks.optimization.JarDiscovererCache;

@IFMLLoadingPlugin.SortingIndex(1001) // Run after deobf (FMLDeobfTweaker has an index of 1000)
public class CoreTweaksPlugin implements IFMLLoadingPlugin {

	public CoreTweaksPlugin() {
		System.out.println("Instantiating CoreTweaksPlugin");
		Config.reload();
		CoreTweaks.init();
	}
	
	@Override
	public String[] getASMTransformerClass() {
		List<String> transformerClasses = new ArrayList<>();
		if(JarDiscovererCache.isActive()) {
			transformerClasses.add("makamys.coretweaks.asm.ASMModParserTransformer");
		}
		if(ProfilerTransformer.isActive()) {
			transformerClasses.add("makamys.coretweaks.asm.ProfilerTransformer");
		}
		return transformerClasses.toArray(new String[] {});
	}

	@Override
	public String getModContainerClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSetupClass() {
		if(JarDiscovererCache.isActive()) {
			JarDiscovererCache.load();
		}
		if(ProfilerTransformer.isActive()) {
			ProfilerTransformer.init();
		}
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
