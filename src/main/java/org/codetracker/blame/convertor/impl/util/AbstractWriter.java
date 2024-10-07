package org.codetracker.blame.convertor.impl.util;

import org.eclipse.jgit.lib.Repository;

import java.io.File;

/* Created by pourya on 2024-10-17*/
public class AbstractWriter {

    protected final String filePath;
    protected final String owner;
    protected final String project;
    protected final String commitId;
    protected String output_folder = "out/";

    public AbstractWriter(Repository repository, String commitId, String filePath) {
        this.filePath = filePath;
        this.owner = new File(repository.getDirectory().getPath()).getParentFile().getParentFile().getName();
        this.project = new File(repository.getDirectory().getPath()).getParentFile().getName();
        this.commitId = commitId;
    }


    public void setOutput_folder(String output_folder) {
        this.output_folder = output_folder;
    }

    public AbstractWriter(String owner, String project, String commitId, String filePath) {
        this.filePath = filePath;
        this.owner = owner;
        this.project = project;
        this.commitId = commitId;
    }

    protected File makeFile(String fileName) {
        String finalPath = output_folder + owner + File.separator + project + File.separator + commitId + File.separator + filePath.replace(File.separator,".") + File.separator + fileName;
        File file = new File(finalPath);
        file.getParentFile().mkdirs(); // Create the directories if they don't exist
        return file;
    }
}
