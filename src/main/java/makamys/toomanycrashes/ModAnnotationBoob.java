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

package makamys.toomanycrashes;

import java.util.ArrayList;
import java.util.Map;

import org.objectweb.asm.Type;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ModAnnotationBoob
{
    public class EnumHolder
    {
        @SuppressWarnings("unused")
        private String desc;
        @SuppressWarnings("unused")
        private String value;

        public EnumHolder(String desc, String value)
        {
            this.desc = desc;
            this.value = value;
        }

    }
    
    public static class EnumHolder2
    {
        @SuppressWarnings("unused")
        private String desc;
        @SuppressWarnings("unused")
        private String value;

        public EnumHolder2(String desc, String value)
        {
            this.desc = desc;
            this.value = value;
        }

    }
    
    public void test() {
    	EnumHolder test = new EnumHolder("a", "b");
    	EnumHolder2 test2 = new EnumHolder2("c", "d");
    }
    
    Type asmType;
    String member;
    Map<String,Object> values = Maps.newHashMap();
    private ArrayList<Object> arrayList;
    private String arrayName;
    
}