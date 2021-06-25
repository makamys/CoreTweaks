package makamys.toomanycrashes.mixin;

import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At;

import makamys.toomanycrashes.FrameProfiler;
import net.minecraft.client.Minecraft;

@Mixin(Minecraft.class)
public class MixinMinecraft_FrameProfiler {
	
	@Redirect(method = "runGameLoop", at = @At(value = "INVOKE", target= "Lorg/lwjgl/opengl/Display;sync(I)V", args= {"log=true"}))
	public void redirectSync(int fps) {
		FrameProfiler.instance.preSync();
		Display.sync(fps);
		FrameProfiler.instance.postSync();
	}
}
