package makamys.coretweaks.mixin.optimization.defaulttexturepack;

import java.io.InputStream;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import makamys.coretweaks.optimization.PrefixedClasspathResourceAccelerator;
import net.minecraft.client.resources.DefaultResourcePack;
import net.minecraft.util.ResourceLocation;

@Mixin(DefaultResourcePack.class)
public class MixinDefaultResourcePack {
    
    private PrefixedClasspathResourceAccelerator crtw$resourceFetchAccelerator;
    
    @Overwrite
    private InputStream getResourceStream(ResourceLocation p_110605_1_) {
        if(crtw$resourceFetchAccelerator == null) {
            crtw$resourceFetchAccelerator = new PrefixedClasspathResourceAccelerator();
        }
        return crtw$resourceFetchAccelerator.getResourceAsStream("/assets/" + p_110605_1_.getResourceDomain() + "/" + p_110605_1_.getResourcePath());
    }
    
}
