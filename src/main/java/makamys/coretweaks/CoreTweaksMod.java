package makamys.coretweaks;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.Multimap;
import com.google.common.eventbus.EventBus;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLConstructionEvent;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;
import makamys.coretweaks.command.CoreTweaksCommand;
import makamys.coretweaks.diagnostics.FrameProfiler;
import makamys.coretweaks.ducks.IChunkProviderClient;
import makamys.coretweaks.optimization.ClientChunkMap;
import makamys.coretweaks.optimization.JarDiscovererCache;
import makamys.coretweaks.optimization.ThreadedTextureLoader;
import makamys.coretweaks.optimization.transformercache.full.CachingTransformer;
import makamys.coretweaks.tweaks.crashhandler.Crasher;
import makamys.coretweaks.util.KeyboardUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMemoryErrorScreen;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.LongHashMap;
import net.minecraft.util.MinecraftError;
import net.minecraft.util.ReportedException;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.world.ChunkEvent;

@Mod(modid = CoreTweaks.MODID, version = CoreTweaks.VERSION)
public class CoreTweaksMod
{
    private static List<IModEventListener> listeners = new ArrayList<>();
    
    @EventHandler
    public void onConstruction(FMLConstructionEvent event) {
        Config.reload();
        
        if(Config.crasher) {
            registerListener(Crasher.instance = new Crasher());
        }
    }
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        listeners.forEach(l -> l.onPreInit(event));
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event) {
        FMLCommonHandler.instance().bus().register(this);
        
        if(Config.coreTweaksCommand) {
            ClientCommandHandler.instance.registerCommand(new CoreTweaksCommand());
        }
        if(CoreTweaks.textureLoader != null) {
            FMLCommonHandler.instance().bus().register(CoreTweaks.textureLoader);
        }
        
        listeners.forEach(l -> l.onInit(event));
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        JarDiscovererCache.finish();
        
        listeners.forEach(l -> l.onPostInit(event));
    }
    
    @EventHandler
    public void onServerAboutToStart(FMLServerAboutToStartEvent event) {
        listeners.forEach(l -> l.onServerAboutToStart(event));
    }
    
    @EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
        listeners.forEach(l -> l.onServerStarting(event));
    }
    
    @EventHandler
    public void onServerStarted(FMLServerStartedEvent event) {
        listeners.forEach(l -> l.onServerStarted(event));
    }
    
    @EventHandler
    public void onServerStopping(FMLServerStoppingEvent event) {
        listeners.forEach(l -> l.onServerStopping(event));
    }
    
    @EventHandler
    public void onServerStopped(FMLServerStoppedEvent event) {
        listeners.forEach(l -> l.onServerStopped(event));
    }
    
    @SubscribeEvent
    public void onClientTick(ClientTickEvent event) {
        if(Config.clientChunkMap) {
            WorldClient world = Minecraft.getMinecraft().theWorld;
            if(world != null) {
                IChunkProvider provider = world.getChunkProvider();
                if(provider != null && provider instanceof ChunkProviderClient) {
                    ChunkProviderClient cp = (ChunkProviderClient)provider;
                    LongHashMap cm = ((IChunkProviderClient)cp).getChunkMapping();
                    if(cm instanceof ClientChunkMap) {
                        Entity player = Minecraft.getMinecraft().renderViewEntity;
                        ((ClientChunkMap) cm).setCenter(((int)player.posX / 16), ((int)player.posZ / 16));
                    }
                }
            }
        }
        
        KeyboardUtil.tick();
    }
    
    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if(event.phase == TickEvent.Phase.START) {
            FrameProfiler.instance.onFrameStart();
        } else if(event.phase == TickEvent.Phase.END) {
        	FrameProfiler.instance.onFrameEnd();
        }
    }
    
    public void registerListener(IModEventListener listener) {
        listeners.add(listener);
    }
    
    public static void handleCrash(Throwable t, CrashReport crashReporter) {
        if(t instanceof IllegalStateException && t.getMessage().equals("Already tesselating!")) {
            Tessellator.instance.draw();
        }
        if(t != null) {
            System.out.println("Caught exception:");
            t.printStackTrace();
        } else {
            t = new RuntimeException("Exception on server thread");
        }
        if(!(t instanceof OutOfMemoryError)) {
        	if(crashReporter != null) {
        		CoreTweaksMod.createCrashReport(crashReporter);
        	} else if(t instanceof MinecraftError) {
        		// do nothing
        	} else {
        		if(t instanceof ReportedException) {
	        		ReportedException re = (ReportedException)t;
	        		Minecraft.getMinecraft().addGraphicsAndWorldToCrashReport(re.getCrashReport());
	        		CoreTweaksMod.createCrashReport(re.getCrashReport());
        		} else {
        			CrashReport cr = Minecraft.getMinecraft().addGraphicsAndWorldToCrashReport(new CrashReport("Unexpected error", t));
        			CoreTweaksMod.createCrashReport(cr);
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
