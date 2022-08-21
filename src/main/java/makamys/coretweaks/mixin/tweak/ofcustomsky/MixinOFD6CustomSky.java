package makamys.coretweaks.mixin.tweak.ofcustomsky;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Pseudo
@Mixin(targets = "CustomSky", remap = false)
public class MixinOFD6CustomSky {
    
    @ModifyConstant(method = "renderSky", constant = @Constant(intValue = 8, ordinal = 0))
    private static int minRenderDistance(int o) {
        return 0;
    }
    
}
