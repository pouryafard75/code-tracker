package org.codetracker.blame.benchmark;

import org.codetracker.blame.IBlame;
import org.codetracker.blame.impl.CodeTrackerBlame;
import org.codetracker.blame.impl.GitBlame;

public enum BlamerFactory {

    GitBlame(new GitBlame()),
    CodeTrackerBlame(new CodeTrackerBlame());
    private final IBlame blamer;

    BlamerFactory(IBlame iBlame) {
        this.blamer = iBlame;
    }

    public IBlame getBlamer() {
        return blamer;
    }

    public String getName() {
        return name();
    }
}
