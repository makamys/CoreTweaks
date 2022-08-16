package makamys.coretweaks.diagnostics;

import static makamys.coretweaks.diagnostics.FrameProfiler.Entry.*;
import static makamys.coretweaks.command.CoreTweaksCommand.*;

import java.io.IOException;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import makamys.coretweaks.Config;
import makamys.coretweaks.CoreTweaks;
import makamys.coretweaks.IModEventListener;
import makamys.coretweaks.command.CoreTweaksCommand;
import makamys.coretweaks.command.ISubCommand;
import makamys.coretweaks.util.TableBuilder;
import makamys.coretweaks.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.ClickEvent.Action;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;

public class FrameProfiler implements IModEventListener {
    
    public static FrameProfiler instance;
    TableBuilder<Entry, Object> tb;
    
    private boolean started = false;
    private boolean renderDebugText;
    
    private int chunksUpdatedAtFrameStart = 0;
    
    enum Entry {
        t_gameLoopStart,
        t_frameStart,
        t_updateRenderersStart,
        t_updateRenderersDeadline,
        t_updateRenderersEnd,
        t_renderWorldEnd,
        t_frameEnd,
        t_syncStart,
        t_syncEnd,
        t_gameLoopEnd,
        chunkUpdates,
        gui
    }
    
    public FrameProfiler() {
        CoreTweaksCommand.registerSubCommand("frameprofiler", new FrameProfilerSubCommand());
    }
    
    private void addEntry(Entry type, Object value) {
    	tb.set(type, value);
    }
    
    private void addEntry(Entry type) {
    	addEntry(type, System.nanoTime());
    }
    
    public void onFrameStart() {
        if(started) {
            addEntry(t_frameStart);
            chunksUpdatedAtFrameStart = WorldRenderer.chunksUpdated;
            GuiScreen screen = Minecraft.getMinecraft().currentScreen;
            addEntry(gui, screen == null ? null : screen.getClass().getName());
        }
        
        if(Config.frameProfilerPrint) {
            RenderTickTimePrinter.preRenderWorld();
        }
    }
    
    public void onFrameEnd() {
        if(started) {
            addEntry(t_frameEnd);
            addEntry(chunkUpdates, WorldRenderer.chunksUpdated - chunksUpdatedAtFrameStart);
        }
        
        if(Config.frameProfilerPrint) {
            RenderTickTimePrinter.postRenderWorld();
        }
    }
    
    public void postRenderWorld(float alpha, long deadline) {
    	if(started) {
	    	addEntry(t_renderWorldEnd);
	        addEntry(t_updateRenderersDeadline, deadline);
    	}
    }
    
    public void preUpdateRenderers() {
        if(started) {
            addEntry(t_updateRenderersStart);
        }
    }
    
    public void postUpdateRenderers() {
    	if(started) {
	    	addEntry(t_updateRenderersEnd);
    	}
    }
    
    public void preSync() {
    	if(started) {
	    	addEntry(t_syncStart);
    	}
    }
    
    public void postSync() {
    	if(started) {
	    	addEntry(t_syncEnd);
    	}
    }
    
    public void preRunGameLoop() {
    	if(started) {
        	tb.endRow();
	    	addEntry(t_gameLoopStart);
    	}
    }
    
    public void postRunGameLoop() {
    	if(started) {
	    	addEntry(t_gameLoopEnd);
    	}
    }
    
    public void start() {
    	tb = new TableBuilder<>();
        started = true;
    }
    
    private boolean dumpProfilingResults() {
    	try {
    		tb.writeToCSV(Util.childFile(CoreTweaks.OUT_DIR, "frameprofiler.csv"));
    		return true;
    	} catch(IOException e) {
    		return false;
    	} finally {
    		tb = null;
    	}
    }
    
    public boolean stop() {
        started = false;
        if(tb != null) {
            return dumpProfilingResults();
        }
        return true;
    }
    
    public boolean isStarted() {
        return started;
    }
    
    @Override
    public void onInit(FMLInitializationEvent event) {
        FMLCommonHandler.instance().bus().register(instance);
        MinecraftForge.EVENT_BUS.register(instance);
        
        if(Config.frameProfilerStartEnabled) {
            start();
        }
    }
    
    @Override
    public void onShutdown() {
        if(started) {
            stop();
        }
    }
    
    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if(event.phase == TickEvent.Phase.START) {
            FrameProfiler.instance.onFrameStart();
        } else if(event.phase == TickEvent.Phase.END) {
            FrameProfiler.instance.onFrameEnd();
        }
    }
    
    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Pre event) {
        if (event.type.equals(RenderGameOverlayEvent.ElementType.DEBUG)) {
            renderDebugText = true;
        } else if (renderDebugText && (event instanceof RenderGameOverlayEvent.Text) && event.type.equals(RenderGameOverlayEvent.ElementType.TEXT)) {
            renderDebugText = false;
            RenderGameOverlayEvent.Text text = (RenderGameOverlayEvent.Text) event;
            if(isStarted()) {
                text.left.add(null);
                text.left.add(EnumChatFormatting.YELLOW + "Having this overlay open may affect profiling results.");
            }
        }
    }
    
    private static class FrameProfilerSubCommand implements ISubCommand {
        
        @Override
        public void processCommand(ICommandSender sender, String[] args) {
            String usage = "coretweaks frameprofiler <start|stop|help>";
            if(args.length == 2) {
                switch(args[1]) {
                    case "start": {
                        if(FrameProfiler.instance.isStarted()) {
                            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Already started frame profiler"));
                        } else {
                            FrameProfiler.instance.start();
                            sender.addChatMessage(new ChatComponentText("Started frame profiler"));
                        }
                        return;
                    }
                    case "stop": {
                        if(!FrameProfiler.instance.isStarted()) {
                            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Frame profiler is not running"));
                        } else {
                            if(FrameProfiler.instance.stop()) {
                                sender.addChatMessage(new ChatComponentText("Stopped frame profiler"));
                            } else {
                                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Failed to save frame profiler results, see log for details"));
                            }
                        }
                        
                        return;
                    }
                    case "help": {
                        addColoredChatMessage(sender, "Usage: " + usage, HELP_USAGE_COLOR);
                        addColoredChatMessage(sender, "Creates a report about the timing of various parts of the rendering process.", HELP_COLOR);
                        addColoredChatMessage(sender, "The report will be written to " + HELP_EMPHASIS_COLOR + "./coretweaks/out/frameprofiler.csv" + HELP_COLOR + ".", HELP_COLOR);
                        addColoredChatMessage(sender, "A useful script for parsing the results of the report can be found at " + HELP_EMPHASIS_COLOR + "https://github.com/makamys/CoreTweaks/tree/master/scripts", HELP_COLOR, (msg) -> msg.getChatStyle().setChatClickEvent(new ClickEvent(Action.OPEN_URL, "https://github.com/makamys/CoreTweaks/tree/master/scripts")));
                        return;
                    }
                }
            }
            throw new WrongUsageException(usage, new Object[0]);
        }
        
    }
}
