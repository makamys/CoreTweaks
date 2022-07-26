package makamys.coretweaks.mixin.tweak.launchminimized;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Hacks WindowsDisplay to keep the window minimized during launch.
 */
@Mixin(targets = "org.lwjgl.opengl.WindowsDisplay", remap = false)
public abstract class MixinWindowsDisplay {
	
	private static final int
		SWP_NOMOVE       = 0x0002,
		SWP_NOZORDER     = 0x0004,
		SWP_NOACTIVATE   = 0x0010,
		SWP_FRAMECHANGED = 0x0020,
		
		SW_SHOWNOACTIVATE = 4,
		SW_SHOWMINNOACTIVE = 7;
	
	private static boolean firstRun = true;
	
	@Redirect(method = "createWindow", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/WindowsDisplay;showWindow(JI)V"))
	public void redirectShowWindow(long hwnd, int mode) {
		showWindow(hwnd, firstRun ? SW_SHOWNOACTIVATE : mode);
		// Minimizing it here would result in wrong window dimensions:
		// The correct window size is set in setResizable, but setting it fails if the window is minimized at the time. 
	}
	
	@ModifyConstant(method = "setResizable", constant = @Constant(longValue = SWP_NOMOVE | SWP_NOZORDER | SWP_FRAMECHANGED))
	private long modifySetResizableUflags(long uflags) {
		return firstRun ? uflags | SWP_NOACTIVATE : uflags;
	}
	
	@Redirect(method = "createWindow", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/WindowsDisplay;setForegroundWindow(J)V"))
	public void redirectSetForegroundWindow(long hwnd) {
		if(firstRun) {
			// Sometimes the (inactive) window stays in front, we make sure it's minimized here.
			// (This has to happen after the call to setResizable.)
			showWindow(hwnd, SW_SHOWMINNOACTIVE);
		} else {
			setForegroundWindow(hwnd);
		}
	}
	
	@Redirect(method = "createWindow", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/WindowsDisplay;grabFocus()V"))
	public void redirectGrabFocus() {
		if(!firstRun) {
			grabFocus();
		}
	}
	
	@Inject(method = "createWindow", at = @At("RETURN"))
	public void postCreateWindow(CallbackInfo ci) {
		firstRun = false;
	}
	
	@Shadow
	private void grabFocus() {};
	
	@Shadow
	private static native void showWindow(long hwnd, int mode);
	
	@Shadow
	private static native void setForegroundWindow(long hwnd);
}
