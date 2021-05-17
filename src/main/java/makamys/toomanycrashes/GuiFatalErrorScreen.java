package makamys.toomanycrashes;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMemoryErrorScreen;
import net.minecraft.client.gui.GuiOptionButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

public class GuiFatalErrorScreen extends GuiScreen {
    /**
     * Draws the screen and all the components in it.
     */
    Throwable theError;
    public GuiFatalErrorScreen(Throwable theError) {
        this.theError = theError;
    }
    
    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    public void initGui()
    {
        this.buttonList.clear();
        this.buttonList.add(new GuiOptionButton(0, this.width / 2 - 75, this.height / 4 + 120 + 12, I18n.format("gui.toMenu", new Object[0])));
    }

    protected void actionPerformed(GuiButton p_146284_1_)
    {
        if (p_146284_1_.id == 0)
        {
            this.mc.displayGuiScreen(new GuiMainMenu());
        }
    }

    /**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
     */
    protected void keyTyped(char p_73869_1_, int p_73869_2_) {}

    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int p_73863_1_, int p_73863_2_, float p_73863_3_)
    {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, "Minecraft crashed!", this.width / 2, this.height / 4 - 60 + 20, 16777215);
        
        this.drawString(this.fontRendererObj, "Minecraft ran into a problem and crashed.", this.width / 2 - 140, this.height / 4 - 70 + 60 + 0, 10526880);
        
        this.drawString(this.fontRendererObj, "TooManyCrashes caught the exception, so you should be", this.width / 2 - 140, this.height / 4 - 70 + 60 + 9 * 2, 10526880);
        this.drawString(this.fontRendererObj, "able to continue playing.", this.width / 2 - 140, this.height / 4 - 70 + 60 + 9 * 3, 10526880);
        
        this.drawString(this.fontRendererObj, "Please restart the game if you see this message again.", this.width / 2 - 140, this.height / 4 - 70 + 60 + 9 * 5, 10526880);
        
        if(theError != null) {
            this.drawString(this.fontRendererObj, "Caught exception:", this.width / 2 - 140, this.height / 4 - 70 + 60 + 9 * 7 + 4, 10526880);
            this.drawString(this.fontRendererObj, theError.toString(), this.width / 2 - 140, this.height / 4 - 70 + 60 + 9 * 8 + 8, 0xFFFF00);
            this.drawString(this.fontRendererObj, "in class", this.width / 2 - 140, this.height / 4 - 70 + 60 + 9 * 9 + 8, 10526880);
            this.drawString(this.fontRendererObj, theError.getStackTrace()[0].getClassName(), this.width / 2 - 140 + 44, this.height / 4 - 70 + 60 + 9 * 9 + 8, 0xFFFFFF);
        } else {
            this.drawString(this.fontRendererObj, "Caught exception on server thread.", this.width / 2 - 140, this.height / 4 - 70 + 60 + 9 * 7 + 4, 10526880);
        }
        this.drawString(this.fontRendererObj, "See the log for more details.", this.width / 2 - 140, this.height / 4 - 70 + 60 + 9 * 12, 10526880);
        
        super.drawScreen(p_73863_1_, p_73863_2_, p_73863_3_);
    }
}
