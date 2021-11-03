package makamys.coretweaks.mixin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.LWJGLException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import makamys.coretweaks.tweak.crashhandler.CrashHandler;
import makamys.coretweaks.tweak.crashhandler.GuiFatalErrorScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.MinecraftError;
import net.minecraft.util.ReportedException;

@Mixin(Minecraft.class)
abstract class MixinMinecraft_CrashHandler {
    
    private Throwable theError;
    
    @Shadow
    volatile boolean running = true;
    
    @Shadow
    private boolean hasCrashed;
    
    @Shadow
    private CrashReport crashReporter;
    
    @Shadow
    private static final Logger logger = LogManager.getLogger();
    
    @Shadow
    abstract void startGame() throws LWJGLException;
    
    @Shadow
    public abstract CrashReport addGraphicsAndWorldToCrashReport(CrashReport p_71396_1_);
    
    @Shadow
    public abstract void displayCrashReport(CrashReport p_71377_1_);
    
    @Shadow
    abstract void runGameLoop();
    
    @Shadow
    public abstract void freeMemory();
    
    @Shadow
    public abstract void displayGuiScreen(GuiScreen p_147108_1_);
    
    @Shadow
    public abstract void shutdownMinecraftApplet();
    
    @Overwrite
    public void run() {
        this.running = true;
        CrashReport crashreport;

        try {
            this.startGame();
        } catch (Throwable throwable) {
            crashreport = CrashReport.makeCrashReport(throwable, "Initializing game");
            crashreport.makeCategory("Initialization");
            this.displayCrashReport(this.addGraphicsAndWorldToCrashReport(crashreport));
            return;
        }

        while (true) {
            try {
                while (this.running) {
                    if (!this.hasCrashed || this.crashReporter == null) {
                        try {
                            try {
                                runGameLoop();
                            } catch (Throwable e) {
                                theError = e;
                                CrashHandler.handleCrash(e, crashReporter);
                            }
                            if (hasCrashed) {
                                theError = null;
                                hasCrashed = false;
                                CrashHandler.handleCrash(null, crashReporter);
                            }
                        } catch (OutOfMemoryError outofmemoryerror) {
                            this.freeMemory();
                            this.displayGuiScreen(new GuiFatalErrorScreen(theError));
                            System.gc();
                        }

                        continue;
                    }

                    this.displayCrashReport(this.crashReporter);
                    return;
                }
            } catch (MinecraftError minecrafterror) {
                ;
            } catch (ReportedException reportedexception) {
                this.addGraphicsAndWorldToCrashReport(reportedexception.getCrashReport());
                this.freeMemory();
                logger.fatal("Reported exception thrown!", reportedexception);
                this.displayCrashReport(reportedexception.getCrashReport());
            } catch (Throwable throwable1) {
                crashreport = this.addGraphicsAndWorldToCrashReport(new CrashReport("Unexpected error", throwable1));
                this.freeMemory();
                logger.fatal("Unreported exception thrown!", throwable1);
                this.displayCrashReport(crashreport);
            } finally {
                this.shutdownMinecraftApplet();
            }

            return;
        }
    }

}
