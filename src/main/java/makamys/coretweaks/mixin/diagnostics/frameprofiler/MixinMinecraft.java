package makamys.coretweaks.mixin.diagnostics.frameprofiler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import makamys.coretweaks.diagnostics.FrameProfiler;
import net.minecraft.client.Minecraft;

@Mixin(Minecraft.class)
public class MixinMinecraft {
	
	@Inject(method = "runGameLoop", at = @At(value = "INVOKE", target= "Lorg/lwjgl/opengl/Display;sync(I)V", remap = false))
	public void preSync(CallbackInfo ci) {
		FrameProfiler.instance.preSync();
	}
	
	@Inject(method = "runGameLoop", at = @At(value = "INVOKE", target= "Lorg/lwjgl/opengl/Display;sync(I)V", shift = At.Shift.AFTER, remap = false))
	public void postSync(CallbackInfo ci) {
		FrameProfiler.instance.postSync();
	}
	
	@Inject(method = "runGameLoop", at = @At(value = "HEAD"))
	public void preRunGameLoop(CallbackInfo ci) {
		FrameProfiler.instance.preRunGameLoop();
	}
	
	@Inject(method = "runGameLoop", at = @At(value = "RETURN"))
	public void postRunGameLoop(CallbackInfo ci) {
		FrameProfiler.instance.postRunGameLoop();
	}
}
