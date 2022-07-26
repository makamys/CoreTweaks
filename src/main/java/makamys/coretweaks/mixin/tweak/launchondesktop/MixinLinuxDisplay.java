package makamys.coretweaks.mixin.tweak.launchondesktop;

import static makamys.coretweaks.CoreTweaks.LOGGER;

import java.io.File;
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
			Process proc = new ProcessBuilder("xprop", "-id", String.valueOf(current_window), "-f", "_NET_WM_DESKTOP", "32c", "-set", "_NET_WM_DESKTOP", "0x" + JVMArgs.LAUNCH_ON_DESKTOP)
			.redirectOutput(new File("/dev/null")).redirectError(new File("/dev/null")).start();
			proc.waitFor();
			
			if(proc.exitValue() == 0) {
				LOGGER.info("Success!");	
			} else {
				throw new IllegalStateException("Exit code: " + proc.exitValue());
			}
		} catch (IOException | InterruptedException | IllegalStateException e) {
			LOGGER.error("Failed to set _NET_WM_DESKTOP property of window. Is xprop installed?");
			e.printStackTrace();
		}
	}
	
}
