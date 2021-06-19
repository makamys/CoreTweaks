package makamys.toomanycrashes;

/*
 * Forge Mod Loader
 * Copyright (c) 2012-2013 cpw.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * Contributors:
 *     cpw - implementation
 */

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.LoaderException;
import cpw.mods.fml.common.discovery.ASMDataTable;
import cpw.mods.fml.common.discovery.ModCandidate;
import cpw.mods.fml.common.discovery.asm.ModAnnotation;
import cpw.mods.fml.common.discovery.asm.ModClassVisitor;

public class ASMModParserBoob
{

    private Type asmType;
    private int classVersion;
    private Type asmSuperType;
    private LinkedList<ModAnnotation> annotations = Lists.newLinkedList();
    private String baseModProperties;

    static enum AnnotationType
    {
        CLASS, FIELD, METHOD, SUBTYPE;
    }

    public ASMModParserBoob(InputStream stream) throws IOException
    {
        try
        {
            ClassReader reader = new ClassReader(stream);
            //reader.accept(new ModClassVisitor(this), 0);
        }
        catch (Exception ex)
        {
            FMLLog.log(Level.ERROR, ex, "Unable to read a class file correctly");
            throw new LoaderException(ex);
        }
    }
    
    public ASMModParserBoob() {
    	
    }
    
    public void memeRun() {
    	
    }
}