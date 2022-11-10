package makamys.coretweaks.asm.itaros.backport5160;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.*;

import makamys.coretweaks.asm.itaros.asmutils.ASMUtils;
import makamys.coretweaks.asm.itaros.asmutils.NameResolutionException;
import makamys.coretweaks.asm.itaros.asmutils.SimpleNameResolver;

/**
 * TODO LIST:
 * 1) Wut? https://github.com/MinecraftForge/MinecraftForge/pull/5160/files#diff-66531a6c0289a9654ce522f047a8827eR78
 * 2) Entity LeashKnot
 * 3) Entity Shulker//DO NOT EXISTS APPARENTLY LOL
 * 4) NetHandlerPlayServer - resync player
 * 5) Wut? https://github.com/MinecraftForge/MinecraftForge/pull/5160/files#diff-3d0fbbd4fa45e8003e514fcac5f2f148R55
 * 6) Wut? MC-117412? https://github.com/MinecraftForge/MinecraftForge/pull/5160/files#diff-496aa22ea49bec02cab69d859142959dR327
 * 7) There is some magic with perWorldStorage = new MapStorage((ISaveHandler)null);. Wut?? https://github.com/MinecraftForge/MinecraftForge/pull/5160/files#diff-0facadd57a1c485eed926033d315a5f0R45
 * 8) Wut? https://github.com/MinecraftForge/MinecraftForge/pull/5160/files#diff-0facadd57a1c485eed926033d315a5f0R268
 */


@SuppressWarnings("unused")
public class Forge5160Transformer implements IClassTransformer {

    static {
        logger = LogManager.getLogger(Forge5160Transformer.class.getSimpleName());
    }

    private static final boolean flagInstrumentAggressiveLogging = false;

    private static final Logger logger;

    private static SimpleNameResolver resolverMethodNames = new SimpleNameResolver(
            new ImmutablePair[]{
                    new ImmutablePair("addEntity", "func_76612_a"),
                    new ImmutablePair("removeEntityAtIndex", "func_76608_a"),
                    new ImmutablePair("setPositionAndRotation", "func_70080_a"),
                    new ImmutablePair("onRemovedFromWorld", "onRemovedFromWorld"),//WE ADD IT
                    new ImmutablePair("onAddedToWorld", "onAddedToWorld"),//WE ADD IT
                    new ImmutablePair("isAddedToWorld", "isAddedToWorld"),//WE ADD IT
                    new ImmutablePair("setPosition", "func_70107_b"),
                    new ImmutablePair("moveEntity", "func_70091_d"),
                    new ImmutablePair("setChunkModified", "func_76630_e"),
                    new ImmutablePair("notifyCall", "notifyCall"),//OUR INTERNAL
                    new ImmutablePair("updateEntityWithOptionalForce", "func_72866_a"),
                    new ImmutablePair("getChunkFromChunkCoords", "func_72964_e"),
                    new ImmutablePair("onEntityAdded", "func_72923_a"),
                    new ImmutablePair("onEntityRemoved", "func_72847_b")
            }
    );

    private static SimpleNameResolver resolverFieldNames = new SimpleNameResolver(
            new ImmutablePair[]{
                    new ImmutablePair("posX", "field_70165_t"),
                    new ImmutablePair("posY", "field_70163_u"),
                    new ImmutablePair("posZ", "field_70161_v"),
                    new ImmutablePair("worldObj", "field_70170_p"),
                    new ImmutablePair("isAddedToWorld", "isAddedToWorld"),//WE ADD IT
                    new ImmutablePair("isRemote", "field_72995_K")
            }
    );

    private static SimpleNameResolver resolverClassNames = new SimpleNameResolver(
        new ImmutablePair[]{
                new ImmutablePair("net.minecraft.entity.Entity","sa"),
                new ImmutablePair("net.minecraft.world.World","ahb"),
                new ImmutablePair("net.minecraft.world.chunk.Chunk","apx")
        }
    );

    private final static SimpleNameResolver[] resolverGroup = new SimpleNameResolver[]{
            resolverMethodNames,
            resolverFieldNames,
            resolverClassNames
    };

    private static Object NamePolicyResolutionActivationToken = null;

    private void switchNameRoslutionPolicyCached() {
        if(NamePolicyResolutionActivationToken != null)
            return;
        NamePolicyResolutionActivationToken = new Object();
        //Check for developer env.
        boolean developerEnvironment = (Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
        ;
        SimpleNameResolver.NamingPolicy namingPolicy;
        if (!developerEnvironment)
            namingPolicy = SimpleNameResolver.NamingPolicy.SRG;
        else
            namingPolicy = SimpleNameResolver.NamingPolicy.PKG;
        logger.info("Switching name resolution mode to " + namingPolicy);
        for (SimpleNameResolver resolver :
                resolverGroup) {
            resolver.setNamingPolicy(namingPolicy);
        }
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        switchNameRoslutionPolicyCached();

        try {
            return mutate(name, basicClass);
        } catch (NameResolutionException e) {
            logger.error(e);
            throw e;//Fail-fast
        }
    }

    private byte[] mutate(String name, byte[] bytecode) {
        ClassReader reader = new ClassReader(bytecode);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        ClassVisitor visitor = null;
        if (name.equals(resolverClassNames.getName("net.minecraft.world.chunk.Chunk"))) {
            visitor = new ClassVisitor(Opcodes.ASM5, writer) {
                //TODO: Event bus calls are ignored. Should probably fix. Or not?

                @Override
                public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                    MethodVisitor finalVisitor = super.visitMethod(access, name, desc, signature, exceptions);
                    //Here we mark chunks dirty when entity is entering or leaving one
                    if (name.equals(resolverMethodNames.getName("addEntity"))) {
                        logger.info("Instrumenting addEntity...");
                        finalVisitor = new MethodVisitor(Opcodes.ASM5, finalVisitor) {
                            @Override
                            public void visitInsn(int opcode) {
                                //Append just before returning
                                if (Opcodes.RETURN == opcode) {
                                    visitVarInsn(Opcodes.ALOAD, 0);//Chunk = this (call from this)
                                    visitMethodInsn(Opcodes.INVOKEVIRTUAL, ASMUtils.internalize(resolverClassNames.getName("net.minecraft.world.chunk.Chunk", SimpleNameResolver.NamingPolicy.PKG)), resolverMethodNames.getName("setChunkModified"), "()V", false);
                                    if(flagInstrumentAggressiveLogging) {
                                        visitLdcInsn("Chunk has been tainted by entity addition!");
                                        visitMethodInsn(Opcodes.INVOKESTATIC, "makamys/coretweaks/asm/itaros/backport5160/DebugProbe", resolverMethodNames.getName("notifyCall"), "(Ljava/lang/String;)V", false);
                                    }
                                }
                                super.visitInsn(opcode);
                            }
                        };
                    } else
                        //Yes, there is asymmetry in call convention. removeEntity is actually a delegator to naked implementation.
                        if (name.equals(resolverMethodNames.getName("removeEntityAtIndex"))) {
                            logger.info("Instrumenting removeEntityAtIndex...");
                            finalVisitor = new MethodVisitor(Opcodes.ASM5, finalVisitor) {
                                @Override
                                public void visitInsn(int opcode) {
                                    //Append just before returning
                                    if (Opcodes.RETURN == opcode) {
                                        visitVarInsn(Opcodes.ALOAD, 0);//Chunk = this (call from this)
                                        visitMethodInsn(Opcodes.INVOKEVIRTUAL, ASMUtils.internalize(resolverClassNames.getName("net.minecraft.world.chunk.Chunk", SimpleNameResolver.NamingPolicy.PKG)), resolverMethodNames.getName("setChunkModified"), "()V", false);
                                        if(flagInstrumentAggressiveLogging) {
                                            visitLdcInsn("Chunk has been tainted by entity removal!");
                                            visitMethodInsn(Opcodes.INVOKESTATIC, "makamys/coretweaks/asm/itaros/backport5160/DebugProbe", resolverMethodNames.getName("notifyCall"), "(Ljava/lang/String;)V", false);
                                        }
                                    }
                                    super.visitInsn(opcode);
                                }
                            };
                        }
                    return finalVisitor;
                }
            };
        } else if (name.equals(resolverClassNames.getName("net.minecraft.world.World"))) {
            visitor = new ClassVisitor(Opcodes.ASM5, writer) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                    MethodVisitor finalVisitor = super.visitMethod(access, name, desc, signature, exceptions);
                    //Here we mark Entity as tracked so when it moves across chunk border it could let it be notified
                    if (name.equals(resolverMethodNames.getName("onEntityAdded"))) {
                        logger.info("Instrumenting onEntityAdded...");
                        finalVisitor = new MethodVisitor(Opcodes.ASM5, finalVisitor) {
                            @Override
                            public void visitInsn(int opcode) {
                                if (opcode == Opcodes.RETURN) {
                                    visitVarInsn(Opcodes.ALOAD, 1);//First argument
                                    visitMethodInsn(Opcodes.INVOKEVIRTUAL, ASMUtils.internalize(resolverClassNames.getName("net.minecraft.entity.Entity", SimpleNameResolver.NamingPolicy.PKG)), resolverMethodNames.getName("onAddedToWorld"), "()V", false);
                                }
                                super.visitInsn(opcode);
                            }
                        };
                        //Here we unmark Entity as tracked so when it moves across chunk border it would be ignored
                    } else if (name.equals(resolverMethodNames.getName("onEntityRemoved"))) {
                        logger.info("Instrumenting onEntityRemoved...");
                        finalVisitor = new MethodVisitor(Opcodes.ASM5, finalVisitor) {
                            @Override
                            public void visitInsn(int opcode) {
                                if (opcode == Opcodes.RETURN) {
                                    visitVarInsn(Opcodes.ALOAD, 1);//First argument
                                    visitMethodInsn(Opcodes.INVOKEVIRTUAL, ASMUtils.internalize(resolverClassNames.getName("net.minecraft.entity.Entity", SimpleNameResolver.NamingPolicy.PKG)), resolverMethodNames.getName("onRemovedFromWorld"), "()V", false);
                                }
                                super.visitInsn(opcode);
                            }
                        };
                    }
                    return finalVisitor;
                }
            };
        } else if (name.equals(resolverClassNames.getName("net.minecraft.entity.Entity"))) {
            visitor = new ClassVisitor(Opcodes.ASM5, writer) {
                @Override
                public void visitEnd() {
                    //Adding tracking monitoring facilities
                    visitField(Opcodes.ACC_PRIVATE, resolverFieldNames.getName("isAddedToWorld"), "Z", null, false);
                    MethodVisitor mv = visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, resolverMethodNames.getName("isAddedToWorld"), "()Z", null, new String[0]);
                    mv.visitCode();
                    mv.visitEnd();
                    mv = visitMethod(Opcodes.ACC_PUBLIC, resolverMethodNames.getName("onAddedToWorld"), "()V", null, new String[0]);//TODO: Append @javax.annotations.OverridingMethodsMustInvokeSuper
                    mv.visitCode();
                    mv.visitEnd();
                    mv = visitMethod(Opcodes.ACC_PUBLIC, resolverMethodNames.getName("onRemovedFromWorld"), "()V", null, new String[0]);//TODO: Append @javax.annotations.OverridingMethodsMustInvokeSuper
                    mv.visitCode();
                    mv.visitEnd();
                    super.visitEnd();
                }

                @Override
                public MethodVisitor visitMethod(int access, String name, String description, String signature, String[] exceptions) {
                    MethodVisitor finalVisitor = super.visitMethod(access, name, description, signature, exceptions);
                    if (name.equals(resolverMethodNames.getName("setPositionAndRotation"))) {
                        logger.info("Instrumenting setPositionAndRotation...");
                        finalVisitor = new MethodVisitor(Opcodes.ASM5, finalVisitor) {
                            @Override
                            public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
                                //We need to insert critical accessor call to trick game to load a chunk and then discard
                                if (name.equals(resolverMethodNames.getName("setPosition"))) {
                                    Label excludeLabel = new Label();
                                    
                                    visitVarInsn(Opcodes.ALOAD, 0);//Entity = this (load from this)
                                    visitFieldInsn(Opcodes.GETFIELD, owner, resolverFieldNames.getName("worldObj"), "L"+ASMUtils.internalize(resolverClassNames.getName("net.minecraft.world.World", SimpleNameResolver.NamingPolicy.PKG))+";");//World
                                    visitFieldInsn(Opcodes.GETFIELD, ASMUtils.internalize(resolverClassNames.getName("net.minecraft.world.World", SimpleNameResolver.NamingPolicy.PKG)), resolverFieldNames.getName("isRemote"), "Z");//isRemote
                                    visitJumpInsn(Opcodes.IFNE, excludeLabel);//Exclude update if world.isRemote evaluates to true
                                    
                                    //Argument zero(callsite locality)
                                    visitVarInsn(Opcodes.ALOAD, 0);//Entity = this (load from this)
                                    visitFieldInsn(Opcodes.GETFIELD, owner, resolverFieldNames.getName("worldObj"), "L"+ASMUtils.internalize(resolverClassNames.getName("net.minecraft.world.World", SimpleNameResolver.NamingPolicy.PKG))+";");//World
                                    //visitInsn(Opcodes.POP);
                                    //Argument one(chunk coordinate X)
                                    visitVarInsn(Opcodes.ALOAD, 0);//Entity = this (load from this)
                                    visitFieldInsn(Opcodes.GETFIELD, owner, resolverFieldNames.getName("posX"), "D");//posX
                                    visitMethodInsn(Opcodes.INVOKESTATIC, "makamys/coretweaks/asm/itaros/backport5160/ComplexOperations", "shiftFlooredIntegral4Right", "(D)I", false);
                                    //visitInsn(Opcodes.POP);
                                    //Argument two(chunk coordinate Z)
                                    visitVarInsn(Opcodes.ALOAD, 0);//Entity = this (load from this)
                                    visitFieldInsn(Opcodes.GETFIELD, owner, resolverFieldNames.getName("posZ"), "D");//posZ
                                    visitMethodInsn(Opcodes.INVOKESTATIC, "makamys/coretweaks/asm/itaros/backport5160/ComplexOperations", "shiftFlooredIntegral4Right", "(D)I", false);
                                    //visitInsn(Opcodes.POP);
                                    //CALL FFS!
                                    //TODO: I am not sure this is correct method, but judging by 4 shift it should be
                                    visitMethodInsn(Opcodes.INVOKEVIRTUAL, ASMUtils.internalize(resolverClassNames.getName("net.minecraft.world.World", SimpleNameResolver.NamingPolicy.PKG)), resolverMethodNames.getName("getChunkFromChunkCoords"), "(II)L"+ASMUtils.internalize(resolverClassNames.getName("net.minecraft.world.chunk.Chunk", SimpleNameResolver.NamingPolicy.PKG))+";", false);
                                    visitInsn(Opcodes.POP);//Ignore result. Here we are just teasing the game to force chunkload
                                    if(flagInstrumentAggressiveLogging) {
                                        visitLdcInsn("Chunkload has been forced!");
                                        visitMethodInsn(Opcodes.INVOKESTATIC, "makamys/coretweaks/asm/itaros/backport5160/DebugProbe", resolverMethodNames.getName("notifyCall"), "(Ljava/lang/String;)V", false);
                                    }
                                    
                                    visitLabel(excludeLabel);//End of exclusion zone for client side
                                }
                                super.visitMethodInsn(opcode, owner, name, desc, itf);
                            }
                        };
                    } else
                        //Here we implement method used to unset tracking flag
                        if (name.equals(resolverMethodNames.getName("onRemovedFromWorld"))) {
                            logger.info("Instrumenting onRemovedFromWorld...");
                            finalVisitor = new MethodVisitor(Opcodes.ASM5, finalVisitor) {
                                @Override
                                public void visitCode() {
                                    visitVarInsn(Opcodes.ALOAD, 0);//Entity = this (store to this)
                                    visitInsn(Opcodes.ICONST_0);//Boolean const = false
                                    visitFieldInsn(Opcodes.PUTFIELD, ASMUtils.internalize(resolverClassNames.getName("net.minecraft.entity.Entity", SimpleNameResolver.NamingPolicy.PKG)), resolverFieldNames.getName("isAddedToWorld"), "Z");//World
                                    visitInsn(Opcodes.RETURN);
                                }
                            };
                        } else
                            //Here we implement method used to set tracking flag
                            if (name.equals(resolverMethodNames.getName("onAddedToWorld"))) {
                                logger.info("Instrumenting onAddedToWorld...");
                                finalVisitor = new MethodVisitor(Opcodes.ASM5, finalVisitor) {
                                    @Override
                                    public void visitCode() {
                                        visitVarInsn(Opcodes.ALOAD, 0);//Entity = this (store to this)
                                        visitInsn(Opcodes.ICONST_1);//Boolean const = true
                                        visitFieldInsn(Opcodes.PUTFIELD, ASMUtils.internalize(resolverClassNames.getName("net.minecraft.entity.Entity", SimpleNameResolver.NamingPolicy.PKG)), resolverFieldNames.getName("isAddedToWorld"), "Z");//World
                                        visitInsn(Opcodes.RETURN);
                                    }
                                };
                            } else
                                //Here we implement isAddedToWorld accessor to get current status of world presence tracking
                                if (name.equals(resolverMethodNames.getName("isAddedToWorld"))) {
                                    logger.info("Instrumenting isAddedToWorld...");
                                    finalVisitor = new MethodVisitor(Opcodes.ASM5, finalVisitor) {

                                        @Override
                                        public void visitCode() {
                                            visitVarInsn(Opcodes.ALOAD, 0);//Entity = this (load from this)
                                            visitFieldInsn(Opcodes.GETFIELD, ASMUtils.internalize(resolverClassNames.getName("net.minecraft.entity.Entity", SimpleNameResolver.NamingPolicy.PKG)), resolverFieldNames.getName("isAddedToWorld"), "Z");//World
                                            visitInsn(Opcodes.IRETURN);
                                        }
                                    };
                                } else
                                    //Here we FORCE game to reregister entity on EVERY move. This ensures it will not get lost(wrong chunk assignment) when moving through chunk boundary
                                    //Origin: https://github.com/PaperMC/Paper/blob/fd1bd5223a461b6d98280bb8f2d67280a30dd24a/Spigot-Server-Patches/0315-Always-process-chunk-registration-after-moving.patch
                                    if (name.equals(resolverMethodNames.getName("setPosition"))
                                            || name.equals(resolverMethodNames.getName("moveEntity"))) {
                                        logger.info("Instrumenting setPosition and moveEntity...");
                                        finalVisitor = new MethodVisitor(Opcodes.ASM5, finalVisitor) {
                                            @Override
                                            public void visitFieldInsn(int opcode, String owner, String name, String desc) {
                                                super.visitFieldInsn(opcode, owner, name, desc);//Keep original
                                                //TODO CONVENIENCE: Actually find coordinate setter triplet and insert after to avoid inconsistancies
                                                if (name.equals(resolverFieldNames.getName("posZ"))) {
                                                    //Inserting after posZ assignment(as we called super before)
                                                    Label excludeLabel = new Label();
                                                    
                                                    visitVarInsn(Opcodes.ALOAD, 0);//Entity = this (load from this)
                                                    visitMethodInsn(Opcodes.INVOKEVIRTUAL, owner, resolverMethodNames.getName("isAddedToWorld"), "()Z", false);
                                                    visitJumpInsn(Opcodes.IFEQ, excludeLabel);//Exclude update if isAddedToWorld evaluates to false
                                                    
                                                    visitVarInsn(Opcodes.ALOAD, 0);//Entity = this (load from this)
                                                    visitFieldInsn(Opcodes.GETFIELD, owner, resolverFieldNames.getName("worldObj"), "L"+ASMUtils.internalize(resolverClassNames.getName("net.minecraft.world.World", SimpleNameResolver.NamingPolicy.PKG))+";");//World
                                                    visitFieldInsn(Opcodes.GETFIELD, ASMUtils.internalize(resolverClassNames.getName("net.minecraft.world.World", SimpleNameResolver.NamingPolicy.PKG)), resolverFieldNames.getName("isRemote"), "Z");//isRemote
                                                    visitJumpInsn(Opcodes.IFNE, excludeLabel);//Exclude update if world.isRemote evaluates to true
                                                    
                                                    //Forces world to recognize the entity on each movement. EWH!!!
                                                    visitVarInsn(Opcodes.ALOAD, 0);//Entity = this (load from this)
                                                    visitFieldInsn(Opcodes.GETFIELD, owner, resolverFieldNames.getName("worldObj"), "L"+ASMUtils.internalize(resolverClassNames.getName("net.minecraft.world.World", SimpleNameResolver.NamingPolicy.PKG))+";");//World
                                                    visitVarInsn(Opcodes.ALOAD, 0);//Entity = this (call on this)
                                                    visitInsn(Opcodes.ICONST_0);//Boolean const = false
                                                    visitMethodInsn(Opcodes.INVOKEVIRTUAL, ASMUtils.internalize(resolverClassNames.getName("net.minecraft.world.World", SimpleNameResolver.NamingPolicy.PKG)), resolverMethodNames.getName("updateEntityWithOptionalForce"), "(L"+ASMUtils.internalize(resolverClassNames.getName("net.minecraft.entity.Entity", SimpleNameResolver.NamingPolicy.PKG))+";Z)V", false);
                                                    //Thank gods it returns nothing
                                                    if(flagInstrumentAggressiveLogging) {
                                                        visitLdcInsn("PATCHED!!!");
                                                        visitMethodInsn(Opcodes.INVOKESTATIC, "makamys/coretweaks/asm/itaros/backport5160/DebugProbe", "notifyCall", "(Ljava/lang/String;)V", false);
                                                    }
                                                    visitLabel(excludeLabel);//End of exclusion zone for non-tracked entities or client side
                                                }
                                            }

                                        };
                                    }
                    return finalVisitor;
                }
            };
        }
        if (visitor != null) {
            reader.accept(visitor, 0);
            return writer.toByteArray();
        } else
            return bytecode;//Nothing to modify
    }

}
