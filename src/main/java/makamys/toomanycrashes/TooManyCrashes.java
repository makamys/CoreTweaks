package makamys.toomanycrashes;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
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

@Mod(modid = TooManyCrashes.MODID, version = TooManyCrashes.VERSION)
public class TooManyCrashes
{
    public static final String MODID = "toomanycrashes";
    public static final String VERSION = "0.1";
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        FMLCommonHandler.instance().bus().register(this);
    }
    
    @SubscribeEvent
    public void onClientTick(ClientTickEvent event) {
        /*if(Keyboard.isKeyDown(Keyboard.KEY_O)) {
            System.out.println(((EntityPlayer)null).posX);
            //Tessellator.instance.startDrawing(GL11.GL_QUADS);
            //Tessellator.instance.startDrawing(GL11.GL_QUADS);
        }*/
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
