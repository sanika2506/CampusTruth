package com.campustruth.service;
import com.campustruth.model.Rumor;
public class CredibilityCalculator implements CredibilityStrategy {
    @Override public int calculate(Rumor rumor) {
        int total=0;
        for(var item: rumor.getEvidence()) total += item.supportsClaim()?item.calculatePoints():-item.calculatePoints();
        return Math.max(0, Math.min(100,total));
    }
}
