# CampusTruth Java OOP

JavaFX college rumor verification system with mandatory Java OOP and file handling.

## Run
`mvn javafx:run`

## Mandatory file handling
The app creates `campustruth-data/rumors.csv` and `campustruth-data/evidence.csv`. `FileStorageService` uses `Path`, `Files.createDirectories`, `Files.newBufferedWriter`, `BufferedReader`, `Files.readAllLines`, and `IOException` to save and load data.

## OOP concepts
Encapsulation, abstraction, inheritance, polymorphism, interface, static members, composition, factory pattern, collections, custom exceptions, JavaFX UI, and Java NIO file handling.

## Demo
Submit rumor -> add evidence -> see polymorphic score -> change status -> close/reopen -> prove CSV persistence.

## Run the connected HTML/CSS/JS website

The original CampusTruth web UI is included under `src/main/resources/web` and is connected to the Java backend.

```bash
mvn clean compile exec:java -Dexec.mainClass=com.campustruth.web.WebAppLauncher
```

Open `http://localhost:8080`. Do not double-click the HTML file for the connected version. The Java server must serve it so browser requests reach `/api/rumors` and `/api/evidence`.

Read `CONNECTED_WEB_GUIDE.md` for the complete architecture and Antigravity steps.
