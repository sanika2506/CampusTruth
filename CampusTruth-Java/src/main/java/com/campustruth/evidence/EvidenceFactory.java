package com.campustruth.evidence;
public final class EvidenceFactory {
    private EvidenceFactory() {}
    public static Evidence create(String type,String id,String description,boolean supports) {
        return switch(type) {
            case "Official document" -> new OfficialDocument(id,description,supports);
            case "Screenshot" -> new ScreenshotEvidence(id,description,supports);
            case "Direct observation" -> new DirectObservation(id,description,supports);
            default -> new AnonymousReport(id,description,supports);
        };
    }
}
