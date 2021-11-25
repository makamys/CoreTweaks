package makamys.coretweaks.bugfix;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import net.minecraft.item.ItemStack;

public class DoubleEatFixer {
    
    /*
     * Using the second suggested fix in this comment (don't reset itemInUse if the items are the "same"): https://bugs.mojang.com/browse/MC-86252?focusedCommentId=298278&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#comment-298278
     * We don't ignore item damage though. Not that it matters for food.
     */
    @SubscribeEvent
    public void onClientTick(TickEvent.PlayerTickEvent event) {
        if(event.phase == Phase.START) {
            if(event.player.itemInUse != null) {
                ItemStack currentItem = event.player.inventory.getCurrentItem();
                if(event.player.itemInUse != currentItem && ItemStack.areItemStacksEqual(currentItem, event.player.itemInUse)) {
                    event.player.itemInUse = currentItem;
                }
            }
        }
    }
    
}
