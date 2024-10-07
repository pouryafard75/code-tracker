package org.codetracker.blame.util;

public class FileNotFoundInThePrevCommitException extends Exception{
    public FileNotFoundInThePrevCommitException() {
    }

    public FileNotFoundInThePrevCommitException(String message) {
        super(message);
    }
}
