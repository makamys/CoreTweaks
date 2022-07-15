package makamys.coretweaks;

import static makamys.coretweaks.CoreTweaks.LOGGER;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import makamys.coretweaks.diagnostics.FMLBarProfiler;
import makamys.coretweaks.diagnostics.MethodProfiler;
import makamys.coretweaks.optimization.JarDiscovererCache;
import net.minecraft.launchwrapper.Launch;

@IFMLLoadingPlugin.SortingIndex(1001) // Run after deobf (FMLDeobfTweaker has an index of 1000)
public class CoreTweaksPlugin implements IFMLLoadingPlugin {

	public CoreTweaksPlugin() {
		LOGGER.info("Instantiating CoreTweaksPlugin");
		Config.reload();
		CoreTweaks.init();
	}
	
	@Override
	public String[] getASMTransformerClass() {
		List<String> transformerClasses = new ArrayList<>();
		if(JarDiscovererCache.isActive()) {
			transformerClasses.add("makamys.coretweaks.asm.ASMModParserTransformer");
		}
		if(MethodProfiler.isActive()) {
			transformerClasses.add("makamys.coretweaks.asm.ProfilerTransformer");
		}
		if(Config.forgeModDiscovererSkipKnownLibraries) {
		    transformerClasses.add("makamys.coretweaks.asm.ModDiscovererTransformer");
        }
		if(FMLBarProfiler.isActive()) {
		    transformerClasses.add("makamys.coretweaks.asm.FMLBarProfilerTransformer");
        }
		if(!MixinConfigPlugin.isForgeSplashEnabled()) {
		    if(Config.forgeFastProgressBar) {
		        transformerClasses.add("makamys.coretweaks.asm.FMLFastSplashTransformer");
		    }
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
		if(MethodProfiler.isActive()) {
			MethodProfiler.instance.init();
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
