package makamys.toomanycrashes;

import java.util.HashMap;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import makamys.toomanycrashes.command.TMCCommand;
import makamys.toomanycrashes.ducks.IChunkProviderClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMemoryErrorScreen;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.LongHashMap;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.client.ClientCommandHandler;

@Mod(modid = TooManyCrashes.MODID, version = TooManyCrashes.VERSION)
public class TooManyCrashes
{
    public static final String MODID = "toomanycrashes";
    public static final String VERSION = "0.1";
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        Config.reload();
        
        FMLCommonHandler.instance().bus().register(this);
        
        if(Config.TMCCommand) {
            ClientCommandHandler.instance.registerCommand(new TMCCommand());
        }
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        JarDiscovererCache.finish();
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
    }
    
    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if(event.phase == TickEvent.Phase.START) {
            FrameTimer.instance.onFrameStart();
        }
    }
    
    public static void handleCrash(Throwable t) {
        if(t instanceof IllegalStateException && t.getMessage().equals("Already tesselating!")) {
            Tessellator.instance.draw();
        }
        if(t != null) {
            System.out.println("Caught exception:");
            t.printStackTrace();
        }
        
        throw new OutOfMemoryError();
    }
}
