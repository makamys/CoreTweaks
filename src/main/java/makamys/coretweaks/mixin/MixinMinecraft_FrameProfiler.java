package makamys.coretweaks.mixin;

import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import makamys.coretweaks.diagnostics.FrameProfiler;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import net.minecraft.client.Minecraft;

@Mixin(Minecraft.class)
public class MixinMinecraft_FrameProfiler {
	
	@Inject(method = "runGameLoop", at = @At(value = "INVOKE", target= "Lorg/lwjgl/opengl/Display;sync(I)V"))
	public void preSync(CallbackInfo ci) {
		FrameProfiler.instance.preSync();
	}
	
	@Inject(method = "runGameLoop", at = @At(value = "INVOKE", target= "Lorg/lwjgl/opengl/Display;sync(I)V", shift = At.Shift.AFTER))
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
