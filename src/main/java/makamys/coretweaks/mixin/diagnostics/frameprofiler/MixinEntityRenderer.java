package makamys.coretweaks.mixin.diagnostics.frameprofiler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import makamys.coretweaks.diagnostics.FrameProfiler;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.profiler.Profiler;

@Mixin(EntityRenderer.class)
abstract class MixinEntityRenderer {
	
	@Inject(method = "renderWorld", at = @At("RETURN"))
	public void postRenderWorld(float alpha, long deadline, CallbackInfo ci) {
		FrameProfiler.instance.postRenderWorld(alpha, deadline);
	}
	
	@Redirect(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V"))
	public void redirectEndStartSection(Profiler profiler, String str) {
		if(str.equals("prepareterrain")) {
			FrameProfiler.instance.postUpdateRenderers();
		}
		profiler.endStartSection(str);
	}
}
