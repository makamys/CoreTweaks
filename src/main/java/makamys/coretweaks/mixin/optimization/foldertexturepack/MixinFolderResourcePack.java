package makamys.coretweaks.mixin.optimization.foldertexturepack;

import java.io.File;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import makamys.coretweaks.optimization.PrefixedFolderResourceAccelerator;
import net.minecraft.client.resources.FolderResourcePack;

/**
 * The method Minecraft uses for loading resources out of folder resource packs is horribly
 * inefficient with large modpacks: it checks every single resource if it's inside the folder,
 * and does this for all folder resource packs present.<br><br>
 * This class uses a more effective method that drastically reduces the cost.
 */

@Mixin(FolderResourcePack.class)
public abstract class MixinFolderResourcePack {
    private PrefixedFolderResourceAccelerator crtw$resourceFetchAccelerator;
    
    @Inject(method = "<init>*", at = @At("RETURN"))
    private void afterConstructor(File folder, CallbackInfo ci) {
        crtw$resourceFetchAccelerator = new PrefixedFolderResourceAccelerator(folder);
    }
    
    @Redirect(method = "hasResourceName(Ljava/lang/String;)Z", 
            at = @At(value = "INVOKE", target = "Ljava/io/File;isFile()Z", remap = false))
    public boolean redirectIsFile(File file) {
        return crtw$resourceFetchAccelerator.isFile(file);
    }
    
}
