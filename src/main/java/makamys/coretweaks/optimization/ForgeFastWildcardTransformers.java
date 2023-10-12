package makamys.coretweaks.optimization;

import static makamys.coretweaks.CoreTweaks.LOGGER;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.google.common.primitives.Bytes;

import cpw.mods.fml.common.asm.transformers.SideTransformer;
import cpw.mods.fml.common.asm.transformers.TerminalTransformer;
import makamys.coretweaks.util.WrappedAddListenableList;
import makamys.coretweaks.util.WrappedAddListenableList.AdditionEvent;
import makamys.coretweaks.util.WrappedAddListenableList.AdditionEventListener;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.classloading.FluidIdTransformer;

public class ForgeFastWildcardTransformers implements AdditionEventListener<IClassTransformer> {
    public static ForgeFastWildcardTransformers instance;
    
    public static final String FLUID_ID_TRANSFORMER_PATTERN = "fluidID";
    public static final String SIDE_TRANSFORMER_PATTERN = "cpw/mods/fml/relauncher/SideOnly";
    public static final String[] TERMINAL_TRANSFORMER_PATTERNS = new String[]{"java/lang/System", "java/lang/Runtime"};
    
    public ForgeFastWildcardTransformers(){
        try {
            LaunchClassLoader lcl = (LaunchClassLoader)Launch.classLoader;
            
            Field transformersField = LaunchClassLoader.class.getDeclaredField("transformers");
            transformersField.setAccessible(true);
            List<IClassTransformer> transformers = (List<IClassTransformer>)transformersField.get(lcl);
            
            WrappedAddListenableList<IClassTransformer> wrappedTransformers = 
                    new WrappedAddListenableList<IClassTransformer>(transformers);
            
            transformersField.set(lcl, wrappedTransformers);
            
            wrappedTransformers.addListener(this);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAdd(AdditionEvent<IClassTransformer> event) {
        if(event.element instanceof FluidIdTransformer) {
            LOGGER.info("Replacing " + event.element.getClass().getName() + " with conditional proxy");
            event.element = new PatternConditionalTransformer(event.element, FLUID_ID_TRANSFORMER_PATTERN);
        } else if(event.element instanceof SideTransformer) {
            LOGGER.info("Replacing " + event.element.getClass().getName() + " with conditional proxy");
            event.element = new PatternConditionalTransformer(event.element, SIDE_TRANSFORMER_PATTERN);
        } else if(event.element instanceof TerminalTransformer) {
            LOGGER.info("Replacing " + event.element.getClass().getName() + " with conditional proxy");
            event.element = new PatternConditionalTransformer(event.element, TERMINAL_TRANSFORMER_PATTERNS);
        }
    }
    
    public static class PatternConditionalTransformer implements IClassTransformer, NonFunctionAlteringWrapper<IClassTransformer> {
        private final IClassTransformer original;
        private final List<byte[]> patterns = new ArrayList<>();
        
        public PatternConditionalTransformer(IClassTransformer original, String... patterns) {
            this.original = original;
            for(String pattern : patterns) {
                this.patterns.add(pattern.getBytes());
            }
        }
        
        @Override
        public byte[] transform(String name, String transformedName, byte[] basicClass) {
            if(containsAnyPattern(basicClass)) {
                return original.transform(name, transformedName, basicClass);
            } else {
                return basicClass;
            }
        }

        private boolean containsAnyPattern(byte[] array) {
            if(array == null) {
                return false;
            }
            for(byte[] pattern : patterns) {
                if(Bytes.indexOf(array, pattern) != -1) {
                    return true;
                }
            }
            return false;
        }
        
        public IClassTransformer getOriginal() {
            return original;
        }
        
        @Override
        public String toString() {
            return "PatternConditionalTransformer{" + original.getClass().getName() + "}";
        }
    }

}
