package makamys.coretweaks.mixin.tweak.launchondesktop;

import static makamys.coretweaks.CoreTweaks.LOGGER;

import java.io.IOException;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import makamys.coretweaks.JVMArgs;

/**
 * Sets _NET_WM_DESKTOP property of window so it appears on a certain desktop.
 */
@Mixin(targets = "org.lwjgl.opengl.LinuxDisplay", remap = false)
public class MixinLinuxDisplay {

	@Shadow
	private static long current_window;
	
	@Inject(method = "createWindow", at = @At(value = "FIELD", target = "Lorg/lwjgl/opengl/LinuxDisplay;current_window:J", shift = At.Shift.AFTER))
	public void afterCreateWindow(CallbackInfo ci) {
		try {
			LOGGER.info("Attempting to set _NET_WM_DESKTOP property of window (" + current_window + ") to " + JVMArgs.LAUNCH_ON_DESKTOP);
			Runtime.getRuntime().exec("xprop -id " + current_window + " -f _NET_WM_DESKTOP 32c -set _NET_WM_DESKTOP 0x" + JVMArgs.LAUNCH_ON_DESKTOP);
			
			LOGGER.info("Success!");
		} catch (IOException/* | InterruptedException */e) {
			LOGGER.error("Failed to set _NET_WM_DESKTOP property of window. Is xprop installed?");
			e.printStackTrace();
		}
	}
	
}
