package makamys.coretweaks.mixin.bugfix.chatlinkcrash;

import java.net.URI;
import java.util.regex.Matcher;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import makamys.coretweaks.CoreTweaks;
import net.minecraftforge.common.ForgeHooks;

@Mixin(value = ForgeHooks.class, remap = false)
public class MixinForgeHooks {
    
    @Redirect(method = "newChatWithLinks", 
            at = @At(value = "INVOKE", target = "Ljava/util/regex/Matcher;find()Z"))
    private static boolean redirectFind(Matcher matcher, String string) {
        while(true) {
            boolean result = matcher.find();
            if(result) {
                String match = string.substring(matcher.start(), matcher.end());
                boolean valid = true;
                try {
                    URI.create(match);
                } catch(Exception e) {
                    valid = false;
                }
                if(valid) {
                    return true;
                } else {
                    // if the matched string is not a valid URI, we skip it to avoid a crash
                    CoreTweaks.LOGGER.debug("Skipping creating link for invalid URL: " + match);
                }
            }
            return false;
        }
    }
}
