package makamys.coretweaks.diagnostics;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import cpw.mods.fml.common.ProgressManager.ProgressBar;
import makamys.coretweaks.Config;
import makamys.coretweaks.CoreTweaks;
import makamys.coretweaks.util.Util;

public class FMLBarProfiler {
    
    private static FMLBarProfiler instance;
    private static final File FML_BAR_PROFILER_CSV = Util.childFile(CoreTweaks.OUT_DIR, "fml_bar_profiler.csv");
    
    private Node root;
    private Node head;
    
    public void init() {
        root = new Node("_root", null);
        head = root;
        push("_early_init");
    }
    
    public void onPush(ProgressBar bar) {
        if(head.text.equals("_early_init")) {
            pop();
        }
        push(bar.getTitle());
    }
    
    public void onPop(ProgressBar bar) {
        if(head.text != bar.getTitle()) {
            pop();
        }
        pop();
        if(bar.getTitle().equals("Loading")) {
            onFinished();
        }
    }
    
    public void onStep(ProgressBar bar, boolean timeEachStep) {
        if(timeEachStep) {
            if(head.text != bar.getTitle()) {
                pop();
            }
            push(bar.getMessage());
        }
    }
    
    private void push(String text) {
        Node node = new Node(text, head);
        head.children.add(node);
        head = node;
    }
    
    private void pop() {
        head.finishTime = System.nanoTime();
        head = head.parent;
    }
    
    private void onFinished() {
        head.finishTime = System.nanoTime();
        writeToFile(FML_BAR_PROFILER_CSV);
    }
    
    private void writeToFile(File file) {
        try(FileWriter fw = new FileWriter(file)){
            fw.write("name,time (s)\n");
            writeToFileRecursive(root, fw);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void writeToFileRecursive(Node node, FileWriter fw) throws IOException {
        List<Node> stack = new ArrayList<>(Arrays.asList(node));
        while(stack.get(stack.size() - 1) != root) {
            stack.add(stack.get(stack.size() - 1).parent);
        }
        Collections.reverse(stack);
        String name = String.join(" > ", stack.stream().map(n -> n.text).collect(Collectors.toList()));
        double timeS = (node.finishTime - node.startTime) / 1_000_000_000.0;
        fw.write(String.format(Locale.ROOT, "%s,%f\n", name, timeS));
        for(Node child : node.children) {
            writeToFileRecursive(child, fw);
        }
    }
    
    private static String bar2str(ProgressBar bar) {
        return bar.getTitle() + " - " + bar.getMessage() + " (" + bar.getStep() + "/" + bar.getSteps() + ")";
    }
    
    public static FMLBarProfiler instance() {
        return instance == null ? (instance = new FMLBarProfiler()) : instance;
    }
    
    public static boolean isActive() {
        return Config.forgeBarProfiler;
    }
    
    private static class Node {
        public List<Node> children = new ArrayList<>();
        public String text;
        public Node parent;
        long startTime;
        long finishTime;
        
        public Node(String text, Node parent) {
            this.text = text;
            this.parent = parent;
            this.startTime = System.nanoTime();
        }
    }
    
}
