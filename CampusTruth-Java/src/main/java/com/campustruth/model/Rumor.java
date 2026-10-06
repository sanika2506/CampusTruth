package com.campustruth.model;

import com.campustruth.evidence.Evidence;
import java.time.LocalDateTime;
import java.util.*;

public class Rumor {
    private final String id, title, claim, category, source, author, createdAt;
    private RumorStatus status;
    private final List<Evidence> evidence = new ArrayList<>();

    public Rumor(String id, String title, String claim, String category, String source, String author) {
        if (title == null || title.isBlank() || claim == null || claim.isBlank()) throw new IllegalArgumentException("Title and claim are required");
        this.id=id; this.title=title; this.claim=claim; this.category=category; this.source=source; this.author=author;
        this.status=RumorStatus.UNDER_REVIEW; this.createdAt=LocalDateTime.now().toString();
    }
    public void addEvidence(Evidence item) { evidence.add(Objects.requireNonNull(item)); }
    public List<Evidence> getEvidence() { return Collections.unmodifiableList(evidence); }
    public void setStatus(RumorStatus status) { this.status=Objects.requireNonNull(status); }
    public String getId(){return id;} public String getTitle(){return title;} public String getClaim(){return claim;}
    public String getCategory(){return category;} public String getSource(){return source;} public String getAuthor(){return author;}
    public String getCreatedAt(){return createdAt;} public RumorStatus getStatus(){return status;}
}
