package com.campustruth.storage;

import com.campustruth.evidence.*;
import com.campustruth.model.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class FileStorageService {
    public static final Path DATA_FOLDER=Paths.get("campustruth-data");
    private static final Path RUMORS_FILE=DATA_FOLDER.resolve("rumors.csv");
    private static final Path EVIDENCE_FILE=DATA_FOLDER.resolve("evidence.csv");

    public void save(List<Rumor> rumors) throws IOException {
        Files.createDirectories(DATA_FOLDER);
        try(BufferedWriter out=Files.newBufferedWriter(RUMORS_FILE, StandardCharsets.UTF_8)) {
            out.write("id|title|claim|category|source|author|status|createdAt\n");
            for(Rumor r:rumors) out.write(String.join("|",clean(r.getId()),clean(r.getTitle()),clean(r.getClaim()),clean(r.getCategory()),clean(r.getSource()),clean(r.getAuthor()),r.getStatus().name(),clean(r.getCreatedAt()))+"\n");
        }
        try(BufferedWriter out=Files.newBufferedWriter(EVIDENCE_FILE, StandardCharsets.UTF_8)) {
            out.write("rumorId|id|type|description|supports\n");
            for(Rumor r:rumors) for(Evidence e:r.getEvidence()) out.write(String.join("|",clean(r.getId()),clean(e.getId()),clean(e.getType()),clean(e.getDescription()),String.valueOf(e.supportsClaim()))+"\n");
        }
    }
    public List<Rumor> load() throws IOException {
        List<Rumor> result=new ArrayList<>(); if(!Files.exists(RUMORS_FILE)) return result;
        Map<String,Rumor> byId=new HashMap<>();
        try(BufferedReader in=Files.newBufferedReader(RUMORS_FILE,StandardCharsets.UTF_8)) {
            String line; in.readLine(); while((line=in.readLine())!=null){String[] p=line.split("\\|",-1);if(p.length>=8){Rumor r=new Rumor(p[0],p[1],p[2],p[3],p[4],p[5]);r.setStatus(RumorStatus.valueOf(p[6]));result.add(r);byId.put(r.getId(),r);}}
        }
        if(Files.exists(EVIDENCE_FILE)) try(BufferedReader in=Files.newBufferedReader(EVIDENCE_FILE,StandardCharsets.UTF_8)) {
            String line; in.readLine(); while((line=in.readLine())!=null){String[] p=line.split("\\|",-1);if(p.length>=5 && byId.containsKey(p[0])) byId.get(p[0]).addEvidence(EvidenceFactory.create(p[2],p[1],p[3],Boolean.parseBoolean(p[4])));}
        }
        return result;
    }
    private String clean(String s){return s.replace("|","/").replace("\n"," ").replace("\r"," ");}
}
