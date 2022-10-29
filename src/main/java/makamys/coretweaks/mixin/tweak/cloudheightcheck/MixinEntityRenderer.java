package makamys.coretweaks.mixin.tweak.cloudheightcheck;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import makamys.coretweaks.Config;
import makamys.coretweaks.util.OFUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;

@Mixin(EntityRenderer.class)
abstract class MixinEntityRenderer {
    
    @ModifyConstant(method = "renderWorld", constant = @Constant(doubleValue = 128.0D, ordinal = 0))
    double cloudHeight0(double o) {
        return getCloudHeight(o, true);
    }
    
    @ModifyConstant(method = "renderWorld", constant = @Constant(doubleValue = 128.0D, ordinal = 1))
    double cloudHeight1(double o) {
        return getCloudHeight(o, false);
    }
    
    /** Returns the height above which clouds should be rendered as transparent. */
    private double getCloudHeight(double original, boolean addOF) {
        switch(Config.cloudHeightCheckMode) {
        case VARIABLE_CORRECTED:
            // OptiFine has a bug where it only adds ofCloudsHeight*128 to the second height check.
            // To fix this, we add it to the first one here.
            return Minecraft.getMinecraft().theWorld.provider.getCloudHeight() + (addOF ? OFUtil.getOfCloudsHeight() * 128f : 0);
        case ALWAYS_TRANSPARENT:
            return Double.NEGATIVE_INFINITY;
        case ALWAYS_OPAQUE:
            return Double.POSITIVE_INFINITY;
        default:
            return original;
        }
        
    }
}
