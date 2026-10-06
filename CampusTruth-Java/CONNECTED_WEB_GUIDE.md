# CampusTruth connected website + Java project

This folder combines the original `campustruth-website` UI with the Java OOP backend.

## What is connected

```text
Browser HTML/CSS/JavaScript
        |
        | HTTP JSON requests
        v
Java WebAppLauncher (port 8080)
        |
        v
Java model + OOP services + FileStorageService
        |
        v
campustruth-data/rumors.csv
campustruth-data/evidence.csv
```

The browser no longer saves rumors in `localStorage`. The Java backend is the source of truth and writes the CSV files.

## Run in Antigravity

1. Extract `CampusTruth-Java.zip`.
2. Open the extracted `CampusTruth-Java` folder in Antigravity. Open the folder containing `pom.xml`.
3. Open Antigravity's terminal.
4. Check Java and Maven:

```bash
java -version
mvn -version
```

Java 17+ and Maven are required.

5. Start the Java server:

```bash
mvn clean compile exec:java -Dexec.mainClass=com.campustruth.web.WebAppLauncher
```

6. Open this URL in a browser:

```text
http://localhost:8080
```

Keep the terminal running while using the website. Stop it with `Ctrl+C`.

## What to demonstrate

1. Open the Overview page.
2. Open Rumor feed and click a claim.
3. Submit a new claim through the HTML form.
4. Java receives `POST /api/rumors`.
5. Add evidence through the Review queue.
6. Java creates an `Evidence` subclass using `EvidenceFactory`.
7. Java calculates the score through polymorphic `calculatePoints()` methods.
8. Change the status.
9. Java writes the new data to `campustruth-data/rumors.csv` and `evidence.csv`.
10. Stop and restart the server; the data loads again.

## API routes

- `GET /api/health` — server health check
- `GET /api/rumors` — returns rumors and their evidence
- `POST /api/rumors` — creates a rumor
- `POST /api/evidence` — adds evidence to a rumor
- `PATCH /api/rumors/{id}` — changes status
- `GET /` — serves the copied HTML UI

## Important files

- `src/main/resources/web/index.html` — original UI structure
- `src/main/resources/web/styles.css` — original UI styling
- `src/main/resources/web/app.js` — UI connected to Java `fetch()` API calls
- `src/main/java/com/campustruth/web/WebAppLauncher.java` — Java HTTP server and API
- `src/main/java/com/campustruth/storage/FileStorageService.java` — mandatory Java file handling
- `src/main/java/com/campustruth/evidence/` — inheritance and polymorphism
- `src/main/java/com/campustruth/service/` — interface and strategy

## Why this satisfies the Java OOP course

The UI is HTML/CSS/JS, but the actual data and business logic run in Java. The Java backend contains encapsulation, abstraction, inheritance, runtime polymorphism, interfaces, static members, composition, collections, a factory, custom exceptions, and Java NIO file handling.

## If the browser says Java server is not running

Run this command from the folder containing `pom.xml`:

```bash
mvn clean compile exec:java -Dexec.mainClass=com.campustruth.web.WebAppLauncher
```

Then open `http://localhost:8080` again. Do not open `index.html` directly for the connected version; it must be served by Java so `/api/...` requests reach the backend.
