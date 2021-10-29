package makamys.coretweaks.mixin.tweak.ofcustomsky;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/* According to the javadoc of `targets`, specifying this (public) class via a string is supposed to cause an exception during runtime.
 * It only causes a warning though, and compilation is way easier this way, so *shrug* */
@Mixin(targets = "CustomSky", remap = false)
public class MixinOFD6CustomSky {
    
    @ModifyConstant(method = "renderSky", constant = @Constant(intValue = 8, ordinal = 0))
    private static int minRenderDistance(int o) {
        return 0;
    }
    
}
