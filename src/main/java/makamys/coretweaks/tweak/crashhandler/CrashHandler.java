package makamys.coretweaks.tweak.crashhandler;

import static makamys.coretweaks.CoreTweaks.LOGGER;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.common.collect.Multimap;

import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.ReflectionHelper;
import makamys.coretweaks.CoreTweaksMod;
import makamys.coretweaks.util.GLUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.MinecraftError;
import net.minecraft.util.ReportedException;

public class CrashHandler {
    
    public static void resetState() {
        List<Exception> exceptions = new ArrayList<>();
        
        try {
            // When an exception happens in a mod event handler, FML adds it to the error map.
            // It will refuse to restart the server if the errors map is not empty, and it never gets cleared.
            // So we need to clear it ourselves.
            LoadController modController = ReflectionHelper.getPrivateValue(Loader.class, Loader.instance(), "modController");
            Multimap<String, Throwable> errors = ReflectionHelper.getPrivateValue(LoadController.class, modController, "errors");
            errors.clear();
        } catch(Exception e) {
            exceptions.add(e);
        }
        
        try {
            GLUtil.resetState();
        } catch(Exception e) {
            exceptions.add(e);
        }
        
        try {
            boolean isDrawing = ReflectionHelper.getPrivateValue(Tessellator.class, Tessellator.instance, "isDrawing", "field_78415_z");
            if(isDrawing) {
                Tessellator.instance.draw();
            }
        } catch(Exception e) {
            exceptions.add(e);
        }
        
        try {
            ReflectionHelper.setPrivateValue(Minecraft.class, Minecraft.getMinecraft(), -1L, "field_83002_am");
        } catch(Exception e) {
            exceptions.add(e);
        }
        
        try {
            if(Minecraft.getMinecraft().renderGlobal != null) {
                List renderersToUpdate = ReflectionHelper.getPrivateValue(RenderGlobal.class, Minecraft.getMinecraft().renderGlobal, "worldRenderersToUpdate", "field_72767_j");
                renderersToUpdate.clear();
            }
        } catch(Exception e) {
            exceptions.add(e);
        }
        
        try {
            Tessellator.instance.setTranslation(0.0D, 0.0D, 0.0D);
        } catch(Exception e) {
            exceptions.add(e);
        }
        
        if(!exceptions.isEmpty()) {
            for(Exception e : exceptions) {
                LOGGER.warn("Something went wrong while attempting to restore state:");
                e.printStackTrace();
            }
        }
    }
    
    public static void createCrashReport(CrashReport crashReporter) {
        File file1 = new File(Minecraft.getMinecraft().mcDataDir, "crash-reports");
        File file2 = new File(file1, "crash-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + "-client.txt");
        LOGGER.info(crashReporter.getCompleteReport());

        if (crashReporter.getFile() != null) {
            LOGGER.info("#@!@# Game crashed! Crash report saved to: #@!@# " + crashReporter.getFile());
        }
        else if (crashReporter.saveToFile(file2)) {
            LOGGER.info("#@!@# Game crashed! Crash report saved to: #@!@# " + file2.getAbsolutePath());
        } else {
            LOGGER.info("#@?@# Game crashed! Crash report could not be saved. #@?@#");
        }
    }
}
