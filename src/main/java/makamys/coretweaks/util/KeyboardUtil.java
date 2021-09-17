package makamys.coretweaks.util;

import org.lwjgl.input.Keyboard;

public class KeyboardUtil {
    
    private static boolean[] wasPressed = new boolean[Keyboard.KEYBOARD_SIZE];
    
    public static void tick(){
        for(int i = 0; i < wasPressed.length; i++) {
            wasPressed[i] = Keyboard.isKeyDown(i);
        }
    }
    
    public static boolean wasKeyDown(int key) {
        return wasPressed[key];
    }
    
}
