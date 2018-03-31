package edu.fiit.schneider_plugin.entity;

public class Change {
    private int lineAt;
    private int lineCount;

    public Change(int lineAt, int lineDif) {
        this.lineAt = lineAt;
        this.lineCount = lineDif;
    }

    public int getLineCount() {
        return lineCount;
    }

    public void setLineCount(int lineCount) {
        this.lineCount = lineCount;
    }

    public int getLineAt() {
        return lineAt;
    }

    public void setLineAt(int lineAt) {
        this.lineAt = lineAt;
    }
}
