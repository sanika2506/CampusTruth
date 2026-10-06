package com.campustruth.evidence;

public abstract class Evidence {
    private final String id;
    private final String description;
    private final boolean supportsClaim;

    protected Evidence(String id, String description, boolean supportsClaim) {
        this.id=id; this.description=description; this.supportsClaim=supportsClaim;
    }
    public abstract int calculatePoints();
    public String getId(){return id;} public String getDescription(){return description;}
    public boolean supportsClaim(){return supportsClaim;}
    public abstract String getType();
}
