package org.codetracker.blame.convertor.model;

/* Created by pourya on 2024-11-25*/
public class PartialBlameCaseInfo extends BlameCaseInfo {
    final int fromLine;
    final int toLine;
    public PartialBlameCaseInfo(String url, String filePath, int fromLine, int toLine) {
        super(url, filePath);
        this.fromLine = fromLine;
        this.toLine = toLine;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        PartialBlameCaseInfo that = (PartialBlameCaseInfo) o;
        return fromLine == that.fromLine && toLine == that.toLine;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + fromLine;
        result = 31 * result + toLine;
        return result;
    }
}
