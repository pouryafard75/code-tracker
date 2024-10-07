package org.codetracker.blame.impl.differ.sd;

/* Created by pourya on 2024-10-27*/
public class LambdaBodyModifier implements ContentModifier {

    @Override
    public String apply(String content) {
        return replaceLambdasWithReturnBody(content);
    }
    // Method to replace lambda expressions (->) with their return body
    private String replaceLambdasWithReturnBody(String line) {
        // Regular expression to match lambda expressions (e.g., filter -> ...)
        return line.replaceAll("(\\w+) -> (.*)", "$2"); // Replace lambda with its body
    }
}
