package com.campustruth.web;

import com.campustruth.evidence.Evidence;
import com.campustruth.evidence.EvidenceFactory;
import com.campustruth.model.Rumor;
import com.campustruth.model.RumorStatus;
import com.campustruth.storage.FileStorageService;
import com.campustruth.util.IdGenerator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.Executors;

public class WebAppLauncher {
    private static final Gson GSON = new GsonBuilder().create();
    private static final FileStorageService STORAGE = new FileStorageService();
    private static final List<Rumor> RUMORS = Collections.synchronizedList(new ArrayList<>());
    private static final Path WEB_ROOT = Paths.get("target/classes/web");

    public static void main(String[] args) throws Exception {
        RUMORS.addAll(STORAGE.load());
        if (RUMORS.isEmpty()) seed();
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", 8080), 0);
        server.createContext("/api/rumors", WebAppLauncher::rumorsApi);
        server.createContext("/api/evidence", WebAppLauncher::evidenceApi);
        server.createContext("/api/health", e -> sendJson(e, 200, "{\"status\":\"ok\"}"));
        server.createContext("/", WebAppLauncher::staticFile);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.println("CampusTruth web app: http://localhost:8080");
        System.out.println("CSV data folder: " + FileStorageService.DATA_FOLDER.toAbsolutePath());
    }

    private static void rumorsApi(HttpExchange e) throws IOException {
        cors(e);
        String method=e.getRequestMethod();
        if ("GET".equals(method)) { sendJson(e,200,GSON.toJson(RUMORS.stream().map(WebAppLauncher::rumorDto).toList())); return; }
        if ("POST".equals(method)) { JsonObject j=body(e); Rumor r=new Rumor(IdGenerator.nextRumorId(),required(j,"title"),required(j,"claim"),get(j,"category","Other"),get(j,"source","Unknown"),get(j,"author","Demo Student")); RUMORS.add(r); STORAGE.save(RUMORS); sendJson(e,201,GSON.toJson(rumorDto(r))); return; }
        String[] parts=e.getRequestURI().getPath().split("/");
        if (parts.length>=4 && "PATCH".equals(method)) { Rumor r=find(parts[3]); if(r==null){sendJson(e,404,"{\"error\":\"Not found\"}");return;} JsonObject j=body(e); r.setStatus(RumorStatus.valueOf(required(j,"status"))); STORAGE.save(RUMORS); sendJson(e,200,GSON.toJson(rumorDto(r))); return; }
        sendJson(e,405,"{\"error\":\"Method not allowed\"}");
    }

    private static void evidenceApi(HttpExchange e) throws IOException {
        cors(e); if(!"POST".equals(e.getRequestMethod())){sendJson(e,405,"{\"error\":\"POST required\"}");return;}
        JsonObject j=body(e); Rumor r=find(required(j,"rumorId")); if(r==null){sendJson(e,404,"{\"error\":\"Rumor not found\"}");return;}
        String type=get(j,"type","Anonymous report"); boolean supports=Boolean.parseBoolean(get(j,"supports","true"));
        Evidence item=EvidenceFactory.create(type,IdGenerator.nextEvidenceId(),required(j,"description"),supports); r.addEvidence(item); STORAGE.save(RUMORS); sendJson(e,201,GSON.toJson(rumorDto(r)));
    }

    private static Map<String,Object> rumorDto(Rumor r){
        Map<String,Object> m=new LinkedHashMap<>(); m.put("id",r.getId());m.put("title",r.getTitle());m.put("claim",r.getClaim());m.put("category",r.getCategory());m.put("source",r.getSource());m.put("status",pretty(r.getStatus()));m.put("author",r.getAuthor());m.put("created",r.getCreatedAt());m.put("evidence",r.getEvidence().stream().map(WebAppLauncher::evidenceDto).toList());
        int score=r.getEvidence().stream().mapToInt(x->x.supportsClaim()?x.calculatePoints():-x.calculatePoints()).sum();m.put("score",Math.max(0,Math.min(100,score)));return m;
    }
    private static Map<String,Object> evidenceDto(Evidence x){Map<String,Object> m=new LinkedHashMap<>();m.put("id",x.getId());m.put("type",x.getType());m.put("description",x.getDescription());m.put("side",x.supportsClaim()?"Supports claim":"Opposes claim");return m;}
    private static String pretty(RumorStatus s){return switch(s){case UNDER_REVIEW->"Under Review";case VERIFIED_TRUE->"Verified True";case VERIFIED_FALSE->"Verified False";case MISLEADING->"Misleading";case OUTDATED->"Outdated";};}
    private static Rumor find(String id){return RUMORS.stream().filter(r->r.getId().equals(id)).findFirst().orElse(null);}
    private static JsonObject body(HttpExchange e)throws IOException{return JsonParser.parseString(new String(e.getRequestBody().readAllBytes(),StandardCharsets.UTF_8)).getAsJsonObject();}
    private static String required(JsonObject j,String key){String v=get(j,key,null);if(v==null||v.isBlank())throw new IllegalArgumentException(key+" is required");return v;}
    private static String get(JsonObject j,String key,String fallback){return j.has(key)&&!j.get(key).isJsonNull()?j.get(key).getAsString():fallback;}
    private static void sendJson(HttpExchange e,int code,String data)throws IOException{byte[] b=data.getBytes(StandardCharsets.UTF_8);e.getResponseHeaders().set("Content-Type","application/json; charset=utf-8");e.getResponseHeaders().set("Access-Control-Allow-Origin","*");e.sendResponseHeaders(code,b.length);try(OutputStream o=e.getResponseBody()){o.write(b);}}
    private static void cors(HttpExchange e){e.getResponseHeaders().set("Access-Control-Allow-Origin","*");e.getResponseHeaders().set("Access-Control-Allow-Headers","Content-Type");}
    private static void staticFile(HttpExchange e)throws IOException{String path=e.getRequestURI().getPath();if(path.equals("/"))path="/index.html";Path file=WEB_ROOT.resolve(path.substring(1)).normalize();if(!file.startsWith(WEB_ROOT)||!Files.exists(file)){sendText(e,404,"Not found");return;}String type=path.endsWith(".css")?"text/css":path.endsWith(".js")?"application/javascript":"text/html";byte[] b=Files.readAllBytes(file);e.getResponseHeaders().set("Content-Type",type+"; charset=utf-8");e.sendResponseHeaders(200,b.length);try(OutputStream o=e.getResponseBody()){o.write(b);}}
    private static void sendText(HttpExchange e,int code,String text)throws IOException{byte[]b=text.getBytes(StandardCharsets.UTF_8);e.sendResponseHeaders(code,b.length);try(OutputStream o=e.getResponseBody()){o.write(b);}}
    private static void seed(){Rumor r=new Rumor("R-100","Cultural fest is postponed","The cultural fest will happen on 20 October instead of 15 October.","Event","Official department","Demo Student");RUMORS.add(r);}
}
