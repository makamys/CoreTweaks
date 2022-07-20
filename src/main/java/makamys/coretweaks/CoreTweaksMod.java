package makamys.coretweaks;

import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.common.FMLCommonHandler;
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
import makamys.coretweaks.bugfix.DoubleEatFixer;
import makamys.coretweaks.command.CoreTweaksCommand;
import makamys.coretweaks.diagnostics.FrameProfiler;
import makamys.coretweaks.diagnostics.MethodProfiler;
import makamys.coretweaks.diagnostics.ServerRunTimePrinter;
import makamys.coretweaks.ducks.IChunkProviderClient;
import makamys.coretweaks.optimization.ClientChunkMap;
import makamys.coretweaks.optimization.JarDiscovererCache;
import makamys.coretweaks.optimization.transformercache.lite.TransformerCache;
import makamys.coretweaks.tweak.crashhandler.Crasher;
import makamys.coretweaks.util.KeyboardUtil;
import makamys.mclib.core.MCLib;
import makamys.mclib.core.MCLibModules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.LongHashMap;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.client.ClientCommandHandler;

@Mod(modid = CoreTweaks.MODID, version = CoreTweaks.VERSION)
public class CoreTweaksMod
{
    private static List<IModEventListener> listeners = new ArrayList<>();
    
    @EventHandler
    public void onConstruction(FMLConstructionEvent event) {
        MCLib.init();
        
        Config.reload();
        
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                listeners.forEach(l -> l.onShutdown());
            }}, "CoreTweaks shutdown thread"));
        
        if(Config.crasher) {
            registerListener(Crasher.instance = new Crasher());
        }
        if(Config.serverRunTimePrinter) {
            registerListener(ServerRunTimePrinter.instance = new ServerRunTimePrinter());
        }
        if(Config.transformerCache == Config.TransformerCache.LITE) {
            registerListener(TransformerCache.instance);
        }
        registerListener(FrameProfiler.instance = new FrameProfiler());
    }
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MCLibModules.updateCheckAPI.submitModTask(CoreTweaks.MODID, "@UPDATE_URL@");
        
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
        if(Config.fixDoubleEat) {
            FMLCommonHandler.instance().bus().register(new DoubleEatFixer());
        }
        if(MethodProfiler.isActive()) {
            FMLCommonHandler.instance().bus().register(MethodProfiler.instance);
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
        Config.reload();
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
    
    public void registerListener(IModEventListener listener) {
        listeners.add(listener);
    }
}
