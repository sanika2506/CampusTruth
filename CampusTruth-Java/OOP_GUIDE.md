# Java OOP concepts in CampusTruth

## Encapsulation
`Rumor`, `Evidence`, and `User` have private fields. Access happens through methods such as `getStatus()`, `setStatus()`, and `addEvidence()`.

## Abstraction
`Evidence` is an abstract class. It declares `calculatePoints()` without deciding the exact points.

## Inheritance
`OfficialDocument`, `ScreenshotEvidence`, `DirectObservation`, and `AnonymousReport` extend `Evidence`.

## Polymorphism
`CredibilityCalculator` loops over `List<Evidence>` and calls `item.calculatePoints()`. Java chooses the overridden method belonging to the real child object.

## Interface
`CredibilityStrategy` defines `calculate(Rumor)`. `CredibilityCalculator` implements it.

## Static
`IdGenerator` has static counters and static methods. `FileStorageService.DATA_FOLDER` is a static constant. `EvidenceFactory.create()` is a static factory method.

## File handling (compulsory)
`FileStorageService` uses Java NIO:
- `Path` and `Paths.get()` locate files.
- `Files.createDirectories()` creates the data folder.
- `Files.newBufferedWriter()` writes CSV.
- `BufferedReader` reads CSV.
- `IOException` is caught by the JavaFX application.

Files created: `campustruth-data/rumors.csv` and `campustruth-data/evidence.csv`.

## Viva demonstration
1. Submit a rumor.
2. Add an official document and screenshot.
3. Point to different `calculatePoints()` methods to explain polymorphism.
4. Change the status.
5. Close and reopen the app.
6. Open both CSV files and show that data persisted.
