package makamys.coretweaks.mixin.optimization.foldertexturepack;

import static makamys.coretweaks.CoreTweaks.LOGGER;

import java.io.File;
import java.util.HashSet;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.resources.FolderResourcePack;

/**
 * The method Minecraft uses for loading resources out of folder resource packs is horribly
 * inefficient with large modpacks: it checks every single resource if it's inside the folder,
 * and does this for all folder resource packs present.<br><br>
 * This class uses a more effective method that drastically reduces the cost.
 */

@Mixin(FolderResourcePack.class)
public abstract class MixinFolderResourcePack {
	
    private static final boolean DEBUG = Boolean.parseBoolean(System.getProperty("coretweaks.debugFolderResourcePackMixin", "false"));
    
	HashSet<String> filePaths = new HashSet<String>();
	
	@Inject(method = "<init>*", at = @At("RETURN"))
    private void afterConstructor(File folder, CallbackInfo ci) {
	    if(DEBUG) LOGGER.info("running after constructor, folder=" + folder);
	    
		explore(folder, folder.getPath());
    }
	
	private void explore(File folder, String path) {
	    if(DEBUG) LOGGER.info("exploring folder=" + folder + " path=" + path);
		
	    for(File f: folder.listFiles()) {
			String myPath = (path.isEmpty() ? "" : path + File.separator) + f.getName();
			filePaths.add(myPath);
			if(f.isDirectory()) {
				explore(f, myPath);
			}
		}
	}
    
    @Redirect(method = "hasResourceName(Ljava/lang/String;)Z", 
            at = @At(value = "INVOKE", target = "Ljava/io/File;isFile()Z"))
    public boolean redirectIsFile(File file) {
        boolean result = filePaths.contains(file.getPath());
        
        if(DEBUG) LOGGER.info("isFile " + file + " ? " + result);
        
		return result;
    }
	
}
