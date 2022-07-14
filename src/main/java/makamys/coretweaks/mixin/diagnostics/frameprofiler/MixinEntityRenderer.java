package makamys.coretweaks.mixin.diagnostics.frameprofiler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import makamys.coretweaks.diagnostics.FrameProfiler;
import net.minecraft.client.renderer.EntityRenderer;

@Mixin(EntityRenderer.class)
abstract class MixinEntityRenderer {
	
	@Inject(method = "renderWorld", at = @At("RETURN"))
	public void postRenderWorld(float alpha, long deadline, CallbackInfo ci) {
		FrameProfiler.instance.postRenderWorld(alpha, deadline);
	}
	
	@ModifyArg(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V"), index = 0)
	private String adjustEndStartSection(String str) {
	    if(str.equals("updatechunks")) {
            FrameProfiler.instance.preUpdateRenderers();
        } else if(str.equals("prepareterrain")) {
            FrameProfiler.instance.postUpdateRenderers();
        }
	    return str;
	}
	
}
