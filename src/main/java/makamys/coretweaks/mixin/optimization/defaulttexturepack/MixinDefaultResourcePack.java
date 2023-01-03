package makamys.coretweaks.mixin.optimization.defaulttexturepack;

import java.io.InputStream;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import makamys.coretweaks.optimization.DefaultResourcePackHelper;
import net.minecraft.client.resources.DefaultResourcePack;
import net.minecraft.util.ResourceLocation;

@Mixin(DefaultResourcePack.class)
public class MixinDefaultResourcePack {
    
    @Overwrite
    private InputStream getResourceStream(ResourceLocation p_110605_1_) {
        return DefaultResourcePackHelper.getResourceStream(p_110605_1_);
    }
    
}
