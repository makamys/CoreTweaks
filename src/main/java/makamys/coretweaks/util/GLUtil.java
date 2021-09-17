/*
 * Inspired by from VanillaFix's GlUtil (MIT License, Copyright (c) 2018 Dimensional Development)
 */

package makamys.coretweaks.util;

import static org.lwjgl.opengl.GL11.*;

import net.minecraft.client.renderer.Tessellator;

public class GLUtil {
    
    public static void resetState() {
        System.out.println("Attempting to reset GL state.");
        OpenGLDebugging.dumpOpenGLState();
        glMatrixMode(GL_MODELVIEW);
        for(int i = 0; i < glGetInteger(GL_MODELVIEW_STACK_DEPTH) - 1; i++) {
            glPopMatrix();
        }
        if(glGetInteger(GL_LIST_INDEX) != 0) {
            glEndList();
        }
        Tessellator.instance.setTranslation(0.0D, 0.0D, 0.0D);
    }
    
}
