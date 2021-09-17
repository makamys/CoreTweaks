/*
 * Inspired by VanillaFix's GlUtil (MIT License, Copyright (c) 2018 Dimensional Development)
 */

package makamys.coretweaks.util;

import static org.lwjgl.opengl.GL11.*;

import static makamys.coretweaks.CoreTweaks.LOGGER;

public class GLUtil {
    
    public static void resetState() {
        LOGGER.debug("Attempting to reset GL state.");
        LOGGER.debug("GL state before restore:");
        OpenGLDebugging.dumpOpenGLState();
        
        glMatrixMode(GL_MODELVIEW);
        for(int i = 0; i < glGetInteger(GL_MODELVIEW_STACK_DEPTH) - 1; i++) {
            glPopMatrix();
        }
        if(glGetInteger(GL_LIST_INDEX) != 0) {
            glEndList();
        }
        // TODO restore other stuff too, like attrib stack
        
        LOGGER.debug("GL state after restore:");
        OpenGLDebugging.dumpOpenGLState();
    }
    
}
