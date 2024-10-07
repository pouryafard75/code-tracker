package org.codetracker.blame.impl.differ.sd;

/* Created by pourya on 2024-10-27*/
public class GenericStatementModifier implements ContentModifier {
    @Override
    public String apply(String content) {
        // Regular expression to match generics (e.g., <T>, <K, V>, or <>)
        String genericPattern = "<[^>]*>";  // Matches anything inside <>, including empty brackets

        // Split the content into lines
        String[] lines = content.split("\n");
        StringBuilder result = new StringBuilder();

        for (String line : lines) {
            // Remove generics from the line
            String modifiedLine = line.replaceAll(genericPattern, "");

            // Append the modified line to the result without trimming
            result.append(modifiedLine).append("\n");
        }

        // Remove last newline character if necessary
        if (result.length() > 0) {
            result.deleteCharAt(result.length() - 1);
        }

        // Return the modified content
        return result.toString();
    }
}
