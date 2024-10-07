package org.codetracker.blame.impl.differ.sd;

/* Created by pourya on 2024-10-27*/
public class MethodRefModifier implements ContentModifier {

    @Override
    public String apply(String content) {
        return replaceMethodReferences(content);
    }

    // Method to replace method references (::) with dot notation (.)
    private String replaceMethodReferences(String line) {
        // Regular expression to match method references (e.g., ClassName::methodName)
        return line.replaceAll("(\\w+)::(\\w+)", "$1.$2()"); // Converts to ClassName.methodName()
    }
}
