package makamys.coretweaks.mixin.optimization.defaultresourcepack;

import java.io.InputStream;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import makamys.coretweaks.optimization.PrefixedClasspathResourceAccelerator;
import net.minecraft.client.resources.DefaultResourcePack;

@Mixin(DefaultResourcePack.class)
public class MixinDefaultResourcePack {
    
    private PrefixedClasspathResourceAccelerator crtw$resourceFetchAccelerator;
    
    @Redirect(method = "getResourceStream", at = @At(value = "INVOKE", target = "Ljava/lang/Class;getResourceAsStream(Ljava/lang/String;)Ljava/io/InputStream;"))
    private InputStream getResourceAsStreamFast(Class<?> ignored, String path) {
        if(crtw$resourceFetchAccelerator == null) {
            crtw$resourceFetchAccelerator = new PrefixedClasspathResourceAccelerator();
        }
        return crtw$resourceFetchAccelerator.getResourceAsStream(path);
    }
    
}
