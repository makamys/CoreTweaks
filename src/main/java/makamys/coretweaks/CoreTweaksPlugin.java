package makamys.coretweaks;

import static makamys.coretweaks.CoreTweaks.LOGGER;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import makamys.coretweaks.optimization.JarDiscovererCache;

@MCVersion("1.7.10")
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
        if(Config.forgeModDiscovererSkipKnownLibraries.isActive()) {
            transformerClasses.add("makamys.coretweaks.asm.ModDiscovererTransformer");
        }
        if(!MixinConfigPlugin.isForgeSplashEnabled()) {
            if(Config.forgeFastProgressBar.isActive()) {
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
