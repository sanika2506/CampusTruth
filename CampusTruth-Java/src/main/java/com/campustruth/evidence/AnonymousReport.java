package com.campustruth.evidence;
public class AnonymousReport extends Evidence {
    public AnonymousReport(String id,String description,boolean supports){super(id,description,supports);}
    @Override public int calculatePoints(){return 2;}
    @Override public String getType(){return "Anonymous report";}
}
