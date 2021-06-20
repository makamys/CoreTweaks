package makamys.toomanycrashes.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import makamys.toomanycrashes.Util;

import org.spongepowered.asm.mixin.injection.Constant;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;

@Mixin(EntityRenderer.class)
abstract class MixinEntityRenderer {
	
	@ModifyConstant(method = "renderWorld", constant = @Constant(doubleValue = 128.0D, ordinal = 0))
	double cloudHeight0(double o) {
		// OptiFine has a bug where it only adds ofCloudsHeight*128 to the second height check.
		// To fix this, we add it to the first one here.
		return Minecraft.getMinecraft().theWorld.provider.getCloudHeight() + Util.getOfCloudsHeight() * 128f;
	}
	
	@ModifyConstant(method = "renderWorld", constant = @Constant(doubleValue = 128.0D, ordinal = 1))
	double cloudHeight1(double o) {
		return Minecraft.getMinecraft().theWorld.provider.getCloudHeight();
	}
}
