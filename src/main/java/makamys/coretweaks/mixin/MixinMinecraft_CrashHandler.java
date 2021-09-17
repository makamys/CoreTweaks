package makamys.coretweaks.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Redirect;

import makamys.coretweaks.CoreTweaksMod;
import makamys.coretweaks.tweak.crashhandler.CrashHandler;
import makamys.coretweaks.tweak.crashhandler.GuiFatalErrorScreen;

import org.spongepowered.asm.mixin.injection.At;

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
        try {
            runGameLoop();
        } catch(Throwable e) {
            theError = e;
            CrashHandler.handleCrash(e, crashReporter);
        }
        if(hasCrashed) {
            theError = null;
            hasCrashed = false;
            CrashHandler.handleCrash(null, crashReporter);
        }
    }
    
    @Redirect(method = "Lnet/minecraft/client/Minecraft;run()V", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/Minecraft;displayGuiScreen(Lnet/minecraft/client/gui/GuiScreen;)V"))
    public void redirectDisplayGuiScreen(Minecraft minecraft) {
        minecraft.displayGuiScreen(new GuiFatalErrorScreen(theError));
    }

}
