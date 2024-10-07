package org.codetracker.blame.convertor.model;

public class Stats {
    int truePositive = 0;
    int falsePositive = 0;
    int trueNegative = 0;
    int falseNegative = 0;

    public Stats(int truePositive, int falsePositive, int trueNegative, int falseNegative) {
        this.truePositive = truePositive;
        this.falsePositive = falsePositive;
        this.trueNegative = trueNegative;
        this.falseNegative = falseNegative;
    }
    public void fp(){
        falsePositive++;
    }
    public void tp(){
        truePositive++;
    }
    public void fn(){
        falseNegative++;
    }
    public void tn(){
        trueNegative++;
    }
    public int total() {
        return truePositive + falsePositive + trueNegative + falseNegative;
    }

    public Stats() {}
}
