package com.campustruth;

import com.campustruth.evidence.EvidenceFactory;
import com.campustruth.exception.InvalidRumorException;
import com.campustruth.model.*;
import com.campustruth.service.CredibilityCalculator;
import com.campustruth.storage.FileStorageService;
import com.campustruth.util.IdGenerator;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.util.List;

public class App extends Application {
    private final List<Rumor> rumors = FXCollections.observableArrayList();
    private final FileStorageService storage = new FileStorageService();
    private final CredibilityCalculator calculator = new CredibilityCalculator();

    private TableView<Rumor> table;
    private Label scoreLabel;
    private ComboBox<Rumor> rumorBox;
    private TextArea evidenceDescription;
    private ComboBox<String> evidenceType;
    private ComboBox<String> supportBox;
    private ComboBox<RumorStatus> statusBox;

    @Override
    public void start(Stage stage) {
        try {
            rumors.addAll(storage.load());
        } catch (IOException ex) {
            showError("Could not load CSV files: " + ex.getMessage());
        }
        if (rumors.isEmpty()) {
            seed();
        }

        BorderPane root = new BorderPane();
        root.setTop(header());

        TabPane tabs = new TabPane();
        tabs.getTabs().addAll(
                new Tab("Submit Rumor", submitTab()),
                new Tab("Rumor Feed", feedTab()),
                new Tab("Evidence & Review", reviewTab())
        );
        tabs.getTabs().forEach(t -> t.setClosable(false));
        root.setCenter(tabs);

        Scene scene = new Scene(root, 1080, 720);
        var cssUrl = getClass().getResource("/style.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        stage.setTitle("CampusTruth | Java OOP Project");
        stage.setScene(scene);
        stage.setMinWidth(960);
        stage.setMinHeight(640);
        stage.show();
        refresh();
    }

    private HBox header() {
        Label title = new Label("CampusTruth");
        title.getStyleClass().add("app-title");

        Label subtitle = new Label("Student Rumor Verification & Credibility Network");
        subtitle.getStyleClass().add("app-subtitle");

        VBox titleBox = new VBox(2, title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label badge = new Label("Java OOP • CSV Connected");
        badge.getStyleClass().add("header-badge");

        HBox header = new HBox(12, titleBox, spacer, badge);
        header.getStyleClass().add("app-header");
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    private ScrollPane submitTab() {
        VBox card = new VBox(16);
        card.getStyleClass().add("card");
        card.setMaxWidth(720);

        Label sectionTitle = new Label("Submit a Campus Rumor");
        sectionTitle.getStyleClass().add("section-title");

        Label sectionSubtitle = new Label("Report an unverified announcement, notice, or event claim circulating on campus.");
        sectionSubtitle.getStyleClass().add("section-subtitle");

        VBox headerGroup = new VBox(4, sectionTitle, sectionSubtitle);

        // Title field
        Label titleLabel = new Label("Rumor Title");
        titleLabel.getStyleClass().add("field-label");
        TextField title = new TextField();
        title.setPromptText("e.g., Cultural fest postponed to late October");

        // Claim field
        Label claimLabel = new Label("Exact Claim & Details");
        claimLabel.getStyleClass().add("field-label");
        TextArea claim = new TextArea();
        claim.setPromptText("Quote or summarize the exact statement, message, or claim being circulated...");
        claim.setPrefRowCount(4);

        // Category and Source fields in two columns
        Label catLabel = new Label("Category");
        catLabel.getStyleClass().add("field-label");
        ComboBox<String> cat = new ComboBox<>(FXCollections.observableArrayList(
                "Examination", "Event", "Class schedule", "Library", "Facility", "Scholarship", "Other"
        ));
        cat.setValue("Other");
        cat.setMaxWidth(Double.MAX_VALUE);
        VBox catCol = new VBox(6, catLabel, cat);
        HBox.setHgrow(catCol, Priority.ALWAYS);

        Label sourceLabel = new Label("Reported Source");
        sourceLabel.getStyleClass().add("field-label");
        ComboBox<String> source = new ComboBox<>(FXCollections.observableArrayList(
                "Student group", "Notice board", "Official department", "Word of mouth", "Social media", "Unknown"
        ));
        source.setValue("Unknown");
        source.setMaxWidth(Double.MAX_VALUE);
        VBox sourceCol = new VBox(6, sourceLabel, source);
        HBox.setHgrow(sourceCol, Priority.ALWAYS);

        HBox dropdownRow = new HBox(16, catCol, sourceCol);

        // Action button
        Button save = new Button("Submit Rumor");
        save.getStyleClass().add("button-primary");

        Label helperNote = new Label("Submissions are initialized with 'Under Review' status. Provide evidence in Evidence & Review to adjust credibility.");
        helperNote.getStyleClass().add("helper-text");

        HBox actionRow = new HBox(16, save, helperNote);
        actionRow.setAlignment(Pos.CENTER_LEFT);

        save.setOnAction(e -> {
            try {
                if (title.getText().isBlank() || claim.getText().isBlank()) {
                    throw new InvalidRumorException("Title and claim are required");
                }
                rumors.add(new Rumor(
                        IdGenerator.nextRumorId(),
                        title.getText().trim(),
                        claim.getText().trim(),
                        cat.getValue(),
                        source.getValue(),
                        "Demo Student"
                ));
                storage.save(rumors);
                title.clear();
                claim.clear();
                refresh();
                showInfo("Rumor submitted successfully and saved to CSV.");
            } catch (InvalidRumorException | IOException ex) {
                showError(ex.getMessage());
            }
        });

        card.getChildren().addAll(
                headerGroup,
                new VBox(6, titleLabel, title),
                new VBox(6, claimLabel, claim),
                dropdownRow,
                actionRow
        );

        VBox container = new VBox(card);
        container.getStyleClass().add("tab-content");
        container.setAlignment(Pos.TOP_LEFT);

        ScrollPane scroll = new ScrollPane(container);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: #f8fafc;");
        return scroll;
    }

    @SuppressWarnings("unchecked")
    private VBox feedTab() {
        Label sectionTitle = new Label("Campus Rumor Feed");
        sectionTitle.getStyleClass().add("section-title");

        Label sectionSubtitle = new Label("Browse tracked campus claims, monitor review states, and inspect credibility scores.");
        sectionSubtitle.getStyleClass().add("section-subtitle");

        VBox titleBox = new VBox(3, sectionTitle, sectionSubtitle);

        Button save = new Button("Save Data to CSV");
        save.getStyleClass().add("button-secondary");
        save.setOnAction(e -> {
            try {
                storage.save(rumors);
                showInfo("Saved to campustruth-data/rumors.csv and evidence.csv");
            } catch (IOException ex) {
                showError(ex.getMessage());
            }
        });

        Button refreshBtn = new Button("Refresh");
        refreshBtn.getStyleClass().add("button-secondary");
        refreshBtn.setOnAction(e -> refresh());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topBar = new HBox(12, titleBox, spacer, refreshBtn, save);
        topBar.setAlignment(Pos.CENTER_LEFT);

        table = new TableView<>();
        table.setItems(FXCollections.observableArrayList(rumors));
        table.getColumns().addAll(
                col("ID", "id", 90),
                col("Title", "title", 320),
                col("Category", "category", 140),
                statusCol("Status", 150),
                col("Author", "author", 130)
        );
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Inspection panel at bottom
        Label inspectHeader = new Label("Selected Rumor Credibility Inspection");
        inspectHeader.getStyleClass().add("field-label");

        scoreLabel = new Label("Select a row in the table above to view its credibility score and evidence details.");
        scoreLabel.setStyle("-fx-text-fill: #475569;");

        table.getSelectionModel().selectedItemProperty().addListener((o, a, r) -> {
            if (r != null) {
                scoreLabel.setText("[" + r.getId() + "] " + r.getTitle()
                        + "   |   Status: " + formatStatus(r.getStatus())
                        + "   |   Evidence Items: " + r.getEvidence().size()
                        + "   |   Credibility Score: " + calculator.calculate(r) + "/100");
                scoreLabel.setStyle("-fx-text-fill: #0f172a; -fx-font-weight: bold;");
            } else {
                scoreLabel.setText("Select a row in the table above to view its credibility score and evidence details.");
                scoreLabel.setStyle("-fx-text-fill: #475569;");
            }
        });

        VBox inspectCard = new VBox(6, inspectHeader, scoreLabel);
        inspectCard.getStyleClass().add("card");
        inspectCard.setStyle("-fx-padding: 12 16;");

        VBox feedLayout = new VBox(14, topBar, table, inspectCard);
        feedLayout.getStyleClass().add("tab-content");
        VBox.setVgrow(table, Priority.ALWAYS);
        return feedLayout;
    }

    private ScrollPane reviewTab() {
        // Card 1: Add Evidence
        VBox evidenceCard = new VBox(14);
        evidenceCard.getStyleClass().add("card");
        HBox.setHgrow(evidenceCard, Priority.ALWAYS);

        Label evTitle = new Label("Add Supporting or Opposing Evidence");
        evTitle.getStyleClass().add("section-title");
        Label evSubtitle = new Label("Attach verifiable artifacts or observations to adjust the claim's credibility.");
        evSubtitle.getStyleClass().add("section-subtitle");
        VBox evHeader = new VBox(3, evTitle, evSubtitle);

        Label rumorLabel = new Label("Select Rumor to Review");
        rumorLabel.getStyleClass().add("field-label");
        rumorBox = new ComboBox<>();
        rumorBox.setMaxWidth(Double.MAX_VALUE);
        configureRumorDropdown(rumorBox);

        Label typeLabel = new Label("Evidence Type");
        typeLabel.getStyleClass().add("field-label");
        evidenceType = new ComboBox<>(FXCollections.observableArrayList(
                "Official document", "Screenshot", "Direct observation", "Anonymous report"
        ));
        evidenceType.setValue("Official document");
        evidenceType.setMaxWidth(Double.MAX_VALUE);
        VBox typeCol = new VBox(6, typeLabel, evidenceType);
        HBox.setHgrow(typeCol, Priority.ALWAYS);

        Label stanceLabel = new Label("Evidence Stance");
        stanceLabel.getStyleClass().add("field-label");
        supportBox = new ComboBox<>(FXCollections.observableArrayList("Supports claim", "Opposes claim"));
        supportBox.setValue("Supports claim");
        supportBox.setMaxWidth(Double.MAX_VALUE);
        VBox stanceCol = new VBox(6, stanceLabel, supportBox);
        HBox.setHgrow(stanceCol, Priority.ALWAYS);

        HBox evTypesRow = new HBox(12, typeCol, stanceCol);

        Label descLabel = new Label("Evidence Description");
        descLabel.getStyleClass().add("field-label");
        evidenceDescription = new TextArea();
        evidenceDescription.setPromptText("Detail what this evidence confirms or disputes (e.g., notice signed by Dean, registrar email)...");
        evidenceDescription.setPrefRowCount(3);

        Button addBtn = new Button("Attach Evidence");
        addBtn.getStyleClass().add("button-primary");
        addBtn.setOnAction(e -> {
            Rumor r = rumorBox.getValue();
            if (r == null || evidenceDescription.getText().isBlank()) {
                showError("Choose a rumor and describe the evidence.");
                return;
            }
            boolean supports = "Supports claim".equals(supportBox.getValue());
            r.addEvidence(EvidenceFactory.create(
                    evidenceType.getValue(),
                    IdGenerator.nextEvidenceId(),
                    evidenceDescription.getText().trim(),
                    supports
            ));
            try {
                storage.save(rumors);
                refresh();
                evidenceDescription.clear();
                showInfo("Evidence added successfully. Credibility score updated via polymorphism.");
            } catch (IOException ex) {
                showError(ex.getMessage());
            }
        });

        evidenceCard.getChildren().addAll(
                evHeader,
                new VBox(6, rumorLabel, rumorBox),
                evTypesRow,
                new VBox(6, descLabel, evidenceDescription),
                addBtn
        );

        // Card 2: Status Review & Scoring Info
        VBox statusCard = new VBox(14);
        statusCard.getStyleClass().add("card");
        HBox.setHgrow(statusCard, Priority.ALWAYS);

        Label stTitle = new Label("Update Verifier Status");
        stTitle.getStyleClass().add("section-title");
        Label stSubtitle = new Label("Set the official verification verdict for the active claim.");
        stSubtitle.getStyleClass().add("section-subtitle");
        VBox stHeader = new VBox(3, stTitle, stSubtitle);

        Label verifierLabel = new Label("Official Verdict");
        verifierLabel.getStyleClass().add("field-label");
        statusBox = new ComboBox<>(FXCollections.observableArrayList(RumorStatus.values()));
        statusBox.setValue(RumorStatus.UNDER_REVIEW);
        statusBox.setMaxWidth(Double.MAX_VALUE);
        configureStatusDropdown(statusBox);

        Button updateBtn = new Button("Update Status");
        updateBtn.getStyleClass().add("button-primary");
        updateBtn.setOnAction(e -> {
            Rumor r = rumorBox.getValue();
            if (r == null) {
                showError("Select a rumor to update status.");
                return;
            }
            r.setStatus(statusBox.getValue());
            try {
                storage.save(rumors);
                refresh();
                showInfo("Status updated to '" + formatStatus(statusBox.getValue()) + "' and saved.");
            } catch (IOException ex) {
                showError(ex.getMessage());
            }
        });

        // Sync rumorBox selection to statusBox
        rumorBox.getSelectionModel().selectedItemProperty().addListener((o, prev, curr) -> {
            if (curr != null && statusBox != null) {
                statusBox.setValue(curr.getStatus());
            }
        });

        // Scoring rules info card
        VBox scoringGuide = new VBox(6);
        scoringGuide.getStyleClass().add("info-box");

        Label guideTitle = new Label("Polymorphic Credibility Weights");
        guideTitle.getStyleClass().add("field-label");

        Label rule1 = new Label("• Official Document: \u00b140 points (highest authority)");
        rule1.getStyleClass().add("helper-text");
        Label rule2 = new Label("• Screenshot Evidence: \u00b125 points");
        rule2.getStyleClass().add("helper-text");
        Label rule3 = new Label("• Direct Observation: \u00b120 points");
        rule3.getStyleClass().add("helper-text");
        Label rule4 = new Label("• Anonymous Report: \u00b110 points");
        rule4.getStyleClass().add("helper-text");
        Label ruleNote = new Label("Supporting evidence adds points; opposing evidence subtracts points. Score is normalized from 0 to 100.");
        ruleNote.getStyleClass().add("helper-text");

        scoringGuide.getChildren().addAll(guideTitle, rule1, rule2, rule3, rule4, new Separator(), ruleNote);

        statusCard.getChildren().addAll(
                stHeader,
                new VBox(6, verifierLabel, statusBox),
                updateBtn,
                scoringGuide
        );

        HBox columns = new HBox(20, evidenceCard, statusCard);
        columns.setAlignment(Pos.TOP_LEFT);

        VBox container = new VBox(columns);
        container.getStyleClass().add("tab-content");

        ScrollPane scroll = new ScrollPane(container);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: #f8fafc;");
        return scroll;
    }

    private void configureRumorDropdown(ComboBox<Rumor> box) {
        StringConverter<Rumor> converter = new StringConverter<>() {
            @Override
            public String toString(Rumor r) {
                return (r == null) ? "Select a rumor..." : "[" + r.getId() + "] " + r.getTitle();
            }

            @Override
            public Rumor fromString(String string) {
                return null;
            }
        };
        box.setConverter(converter);
        box.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Rumor r, boolean empty) {
                super.updateItem(r, empty);
                setText(empty || r == null ? "" : "[" + r.getId() + "] " + r.getTitle());
            }
        });
        box.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Rumor r, boolean empty) {
                super.updateItem(r, empty);
                setText(empty || r == null ? "Select a rumor..." : "[" + r.getId() + "] " + r.getTitle());
            }
        });
    }

    private void configureStatusDropdown(ComboBox<RumorStatus> box) {
        StringConverter<RumorStatus> converter = new StringConverter<>() {
            @Override
            public String toString(RumorStatus s) {
                return formatStatus(s);
            }

            @Override
            public RumorStatus fromString(String string) {
                return null;
            }
        };
        box.setConverter(converter);
        box.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(RumorStatus item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : formatStatus(item));
            }
        });
        box.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(RumorStatus item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : formatStatus(item));
            }
        });
    }

    private static String formatStatus(RumorStatus s) {
        if (s == null) return "";
        return switch (s) {
            case UNDER_REVIEW -> "Under Review";
            case VERIFIED_TRUE -> "Verified True";
            case VERIFIED_FALSE -> "Verified False";
            case MISLEADING -> "Misleading";
            case OUTDATED -> "Outdated";
        };
    }

    private <T> TableColumn<Rumor, T> col(String title, String property, int width) {
        TableColumn<Rumor, T> c = new TableColumn<>(title);
        c.setCellValueFactory(new PropertyValueFactory<>(property));
        c.setPrefWidth(width);
        return c;
    }

    private TableColumn<Rumor, RumorStatus> statusCol(String title, int width) {
        TableColumn<Rumor, RumorStatus> c = new TableColumn<>(title);
        c.setCellValueFactory(new PropertyValueFactory<>("status"));
        c.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(RumorStatus item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(formatStatus(item));
                }
            }
        });
        c.setPrefWidth(width);
        return c;
    }

    private void refresh() {
        if (table != null) {
            Rumor selected = table.getSelectionModel().getSelectedItem();
            table.setItems(FXCollections.observableArrayList(rumors));
            table.refresh();
            if (selected != null) {
                table.getSelectionModel().select(selected);
            }
        }
        if (rumorBox != null) {
            Rumor current = rumorBox.getValue();
            rumorBox.setItems(FXCollections.observableArrayList(rumors));
            if (current != null && rumors.contains(current)) {
                rumorBox.setValue(current);
            } else if (!rumors.isEmpty()) {
                rumorBox.getSelectionModel().select(0);
            }
        }
    }

    private void seed() {
        Rumor r = new Rumor(
                "R-100",
                "Cultural fest is postponed",
                "The cultural fest will happen on 20 October instead of 15 October.",
                "Event",
                "Official department",
                "Demo Student"
        );
        r.addEvidence(EvidenceFactory.create(
                "Official document",
                "E-200",
                "Student Affairs notice confirms the new date.",
                true
        ));
        rumors.add(r);
    }

    private void showInfo(String s) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, s);
        alert.setHeaderText(null);
        alert.setTitle("CampusTruth Information");
        alert.showAndWait();
    }

    private void showError(String s) {
        Alert alert = new Alert(Alert.AlertType.ERROR, s);
        alert.setHeaderText(null);
        alert.setTitle("CampusTruth Error");
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
