package com.vijay.tool;

public class ToolUtils {

    public static String cleanToolName(String fullName) {
        int lastUnderscore = fullName.lastIndexOf("_");
        return (lastUnderscore >= 0) ? fullName.substring(lastUnderscore + 1) : fullName;
    }

    public static String generateExample(String name, String inputSchema) {
        if (inputSchema.contains("\"path\"")) {
            return "Run " + name + " on E:/demo";
        } else if (inputSchema.contains("\"code\"")) {
            return "Run " + name + " on this code: print('Hello')";
        } else if (inputSchema.contains("\"title\"") && inputSchema.contains("\"body\"")) {
            return "Create a note titled Hello with body This is my first note.";
        } else {
            return "Try using tool: " + name;
        }
    }
}
