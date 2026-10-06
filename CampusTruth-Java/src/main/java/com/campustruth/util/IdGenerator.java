package com.campustruth.util;
public final class IdGenerator {
    private static int rumor=100, evidence=200;
    private IdGenerator() {}
    public static synchronized String nextRumorId(){return "R-"+(++rumor);}
    public static synchronized String nextEvidenceId(){return "E-"+(++evidence);}
}
