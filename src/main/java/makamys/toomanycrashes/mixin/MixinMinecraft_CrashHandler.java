package makamys.toomanycrashes.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At;

import makamys.toomanycrashes.GuiFatalErrorScreen;
import makamys.toomanycrashes.MixinLogger;
import makamys.toomanycrashes.TooManyCrashes;
import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.MinecraftError;
import net.minecraft.util.ReportedException;

@Mixin(Minecraft.class)
abstract class MixinMinecraft_CrashHandler {
    
    @Shadow
    public abstract void runGameLoop();
    
    @Shadow
    private boolean hasCrashed;
    
    @Shadow
    private CrashReport crashReporter;
    
    private Throwable theError;
    
    @Redirect(method = "Lnet/minecraft/client/Minecraft;run()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;runGameLoop()V"))
    public void run(Minecraft minecraft) {
        MixinLogger.printActive(this);
        
        try {
            runGameLoop();
        } catch(Throwable e) {
            theError = e;
            TooManyCrashes.handleCrash(e, crashReporter);
        }
        if(hasCrashed) {
            hasCrashed = false;
            TooManyCrashes.handleCrash(null, crashReporter);
        }
    }
    
    @Redirect(method = "Lnet/minecraft/client/Minecraft;run()V", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/Minecraft;displayGuiScreen(Lnet/minecraft/client/gui/GuiScreen;)V"))
    public void redirectDisplayGuiScreen(Minecraft minecraft) {
        minecraft.displayGuiScreen(new GuiFatalErrorScreen(theError));
    }

}
