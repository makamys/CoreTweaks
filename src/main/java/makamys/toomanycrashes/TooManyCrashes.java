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
import net.minecraft.client.gui.GuiMemoryErrorScreen;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;

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
