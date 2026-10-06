package com.campustruth.evidence;
public class OfficialDocument extends Evidence {
    public OfficialDocument(String id,String description,boolean supports){super(id,description,supports);}
    @Override public int calculatePoints(){return 40;}
    @Override public String getType(){return "Official document";}
}
