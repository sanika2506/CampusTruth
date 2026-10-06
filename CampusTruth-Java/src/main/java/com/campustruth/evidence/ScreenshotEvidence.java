package com.campustruth.evidence;
public class ScreenshotEvidence extends Evidence {
    public ScreenshotEvidence(String id,String description,boolean supports){super(id,description,supports);}
    @Override public int calculatePoints(){return 10;}
    @Override public String getType(){return "Screenshot";}
}
