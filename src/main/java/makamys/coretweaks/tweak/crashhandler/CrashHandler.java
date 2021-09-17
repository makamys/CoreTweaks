package makamys.coretweaks.tweak.crashhandler;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.common.collect.Multimap;

import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.ReflectionHelper;
import makamys.coretweaks.CoreTweaksMod;
import makamys.coretweaks.util.GLUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.MinecraftError;
import net.minecraft.util.ReportedException;

public class CrashHandler {
    
    public static void handleCrash(Throwable t, CrashReport crashReporter) {
        resetState();
        
        if(t != null) {
            System.out.println("Caught exception:");
            t.printStackTrace();
        } else {
            t = new RuntimeException("Exception on server thread");
        }
        if(!(t instanceof OutOfMemoryError)) {
            if(crashReporter != null) {
                createCrashReport(crashReporter);
            } else if(t instanceof MinecraftError) {
                // do nothing
            } else {
                if(t instanceof ReportedException) {
                    ReportedException re = (ReportedException)t;
                    Minecraft.getMinecraft().addGraphicsAndWorldToCrashReport(re.getCrashReport());
                    createCrashReport(re.getCrashReport());
                } else {
                    CrashReport cr = Minecraft.getMinecraft().addGraphicsAndWorldToCrashReport(new CrashReport("Unexpected error", t));
                    createCrashReport(cr);
                }
            }
        }
        
        // When an exception happens in a mod event handler, FML adds it to the error map.
        // It will refuse to restart the server if the errors map is not empty, and it never gets cleared.
        // So we need to clear it ourselves.
        LoadController modController = ReflectionHelper.getPrivateValue(Loader.class, Loader.instance(), "modController");
        Multimap<String, Throwable> errors = ReflectionHelper.getPrivateValue(LoadController.class, modController, "errors");
        errors.clear();
        
        // Throw OOME to trigger the crash handler screen
        throw new OutOfMemoryError();
    }
    
    private static void resetState() {
        boolean isDrawing = ReflectionHelper.getPrivateValue(Tessellator.class, Tessellator.instance, "isDrawing");
        if(isDrawing) {
            Tessellator.instance.draw();
        }
        
        GLUtil.resetState();
        Tessellator.instance.setTranslation(0.0D, 0.0D, 0.0D);
    }
    
    public static void createCrashReport(CrashReport crashReporter) {
        File file1 = new File(Minecraft.getMinecraft().mcDataDir, "crash-reports");
        File file2 = new File(file1, "crash-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + "-client.txt");
        System.out.println(crashReporter.getCompleteReport());

        if (crashReporter.getFile() != null) {
            System.out.println("#@!@# Game crashed! Crash report saved to: #@!@# " + crashReporter.getFile());
        }
        else if (crashReporter.saveToFile(file2)) {
            System.out.println("#@!@# Game crashed! Crash report saved to: #@!@# " + file2.getAbsolutePath());
        } else {
            System.out.println("#@?@# Game crashed! Crash report could not be saved. #@?@#");
        }
    }
}
