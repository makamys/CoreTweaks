package makamys.coretweaks.mixin.optimization.foldertexturepack;

import static makamys.coretweaks.CoreTweaks.LOGGER;

import java.io.File;
import java.util.HashSet;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import makamys.coretweaks.optimization.FastFolderResourcePack;
import net.minecraft.client.resources.FolderResourcePack;

/**
 * The method Minecraft uses for loading resources out of folder resource packs is horribly
 * inefficient with large modpacks: it checks every single resource if it's inside the folder,
 * and does this for all folder resource packs present.<br><br>
 * This class uses a more effective method that drastically reduces the cost.
 */

@Mixin(FolderResourcePack.class)
public abstract class MixinFolderResourcePack {
    HashSet<String> filePaths = new HashSet<String>();
    
    @Inject(method = "<init>*", at = @At("RETURN"))
    private void afterConstructor(File folder, CallbackInfo ci) {
        FastFolderResourcePack.afterConstructor(folder, filePaths);
    }
    
    private void explore(File folder, String path) {
        FastFolderResourcePack.explore(folder, path, filePaths);
    }
    
    @Redirect(method = "hasResourceName(Ljava/lang/String;)Z", 
            at = @At(value = "INVOKE", target = "Ljava/io/File;isFile()Z", remap = false))
    public boolean redirectIsFile(File file) {
        return FastFolderResourcePack.redirectIsFile(file, filePaths);
    }
    
}
