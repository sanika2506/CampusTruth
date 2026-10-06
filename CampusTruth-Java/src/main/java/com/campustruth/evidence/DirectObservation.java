package com.campustruth.evidence;
public class DirectObservation extends Evidence {
    public DirectObservation(String id,String description,boolean supports){super(id,description,supports);}
    @Override public int calculatePoints(){return 20;}
    @Override public String getType(){return "Direct observation";}
}
