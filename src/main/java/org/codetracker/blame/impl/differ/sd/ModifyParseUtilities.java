package org.codetracker.blame.impl.differ.sd;

/* Created by pourya on 2024-10-28 */

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;


import java.io.*;

import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import net.sourceforge.pmd.lang.LanguageProcessor;
import net.sourceforge.pmd.lang.LanguageProcessorRegistry;
import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.lang.PmdCapableLanguage;
import net.sourceforge.pmd.lang.ast.*;
import net.sourceforge.pmd.lang.document.TextDocument;
import org.apache.commons.io.IOUtils;
import smallChanges.statements.ParseUtilities;

import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;


public class ModifyParseUtilities {
    public static void main(String[] args) {
        System.out.println(parseJavaContent("public class Test {}"));
    }
    public static void main2(String[] args) throws Exception {
//         Load the original JAR file
        File jarFile = new File("/Users/pourya/.m2/repository/ca/concordia/export/sdiff/1.2.0/sdiff-1.2.0.jar");
        File tempJarFile = new File("sdiff-1.2.0-modified.jar");

        // Copy the original JAR to a new file to modify
        Files.copy(jarFile.toPath(), tempJarFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        // Modify the method using ByteBuddy
        DynamicType.Unloaded<?> dynamicType = new ByteBuddy()
                .redefine(ParseUtilities.class)
                .method(ElementMatchers.named("parseToASTCompilationUnit"))
                .intercept(MethodDelegation.to(CustomParseUtilities.class))
                .make();

        // Replace the class in the JAR file with the modified class
        writeModifiedJar(tempJarFile, dynamicType);

        System.out.println("Method modified and JAR file updated.");

        // Load the modified class
//        ParseUtilities.parseToASTCompilationUnit(null, null);
    }

    private static void writeModifiedJar(File jarFile, DynamicType.Unloaded<?> dynamicType) throws IOException {
        File outputJarFile = new File("sdiff-1.2.0-modified-output.jar");

        try (JarFile jar = new JarFile(jarFile);
             JarOutputStream jos = new JarOutputStream(new FileOutputStream(outputJarFile))) {
            for (JarEntry entry : java.util.Collections.list(jar.entries())) {
                try (InputStream is = jar.getInputStream(entry)) {
                    jos.putNextEntry(new JarEntry(entry.getName()));
                    if (!entry.getName().equals(dynamicType.getTypeDescription().getInternalName() + ".class")) {
                        jos.write(is.readAllBytes());
                    } else {
                        jos.write(dynamicType.getBytes());
                    }
                    jos.closeEntry();
                }
            }
        }

        System.out.println("Modified JAR file written to: " + outputJarFile.getAbsolutePath());
    }

    public static class CustomParseUtilities {
        public static net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit parseToASTCompilationUnit(Reader rdr, String sourceVersion){
            try {
                return parseJavaContent(IOUtils.toString(rdr));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private static net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit parseJavaContent(String content) {
        PmdCapableLanguage java = (PmdCapableLanguage) LanguageRegistry.PMD.getLanguageById("java");
        LanguageProcessor processor = java.createProcessor(java.newPropertyBundle());
        Parser parser = processor.services().getParser();
        try (TextDocument textDocument = TextDocument.readOnlyString(content, java.getDefaultVersion());
             LanguageProcessorRegistry lpr = LanguageProcessorRegistry.singleton(processor)) {

            Parser.ParserTask task = new Parser.ParserTask(textDocument, SemanticErrorReporter.noop(), lpr);
            RootNode root = parser.parse(task);
            net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit root1 = (net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit) root;
            return root1;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}

