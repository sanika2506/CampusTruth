package com.campustruth;

import com.campustruth.evidence.Evidence;
import com.campustruth.evidence.EvidenceFactory;
import com.campustruth.exception.InvalidRumorException;
import com.campustruth.model.*;
import com.campustruth.service.CredibilityCalculator;
import com.campustruth.storage.FileStorageService;
import com.campustruth.util.IdGenerator;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.*;

public class App extends Application {
    private final List<Rumor> rumors=FXCollections.observableArrayList();
    private final FileStorageService storage=new FileStorageService();
    private final CredibilityCalculator calculator=new CredibilityCalculator();
    private TableView<Rumor> table; private Label scoreLabel; private ComboBox<Rumor> rumorBox;
    private TextArea evidenceDescription; private ComboBox<String> evidenceType, supportBox;

    @Override public void start(Stage stage) {
        try { rumors.addAll(storage.load()); } catch(IOException ex) { showError("Could not load CSV files: "+ex.getMessage()); }
        if(rumors.isEmpty()) seed();
        BorderPane root=new BorderPane(); root.setPadding(new Insets(14)); root.setTop(header());
        TabPane tabs=new TabPane(); tabs.getTabs().addAll(new Tab("Submit rumor",submitTab()),new Tab("Rumor feed",feedTab()),new Tab("Evidence & review",reviewTab()));
        tabs.getTabs().forEach(t->t.setClosable(false)); root.setCenter(tabs);
        Scene scene=new Scene(root,1050,680); stage.setTitle("CampusTruth | Java OOP Project"); stage.setScene(scene); stage.show(); refresh();
    }
    private HBox header(){Label l=new Label("CampusTruth  |  Java OOP Rumor Verification Network");l.setStyle("-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:#17324d;");HBox h=new HBox(l);h.setPadding(new Insets(0,0,14,0));return h;}
    private VBox submitTab(){
        TextField title=new TextField(); title.setPromptText("Rumor title"); TextArea claim=new TextArea(); claim.setPromptText("Write the exact claim"); claim.setPrefRowCount(5);
        ComboBox<String> cat=new ComboBox<>(FXCollections.observableArrayList("Examination","Event","Class schedule","Library","Facility","Scholarship","Other"));cat.setValue("Other");
        ComboBox<String> source=new ComboBox<>(FXCollections.observableArrayList("Student group","Notice board","Official department","Word of mouth","Social media","Unknown"));source.setValue("Unknown");
        Button save=new Button("Submit rumor"); save.setOnAction(e->{try{if(title.getText().isBlank()||claim.getText().isBlank())throw new InvalidRumorException("Title and claim are required");rumors.add(new Rumor(IdGenerator.nextRumorId(),title.getText().trim(),claim.getText().trim(),cat.getValue(),source.getValue(),"Demo Student"));storage.save(rumors);title.clear();claim.clear();refresh();showInfo("Rumor saved to CSV files.");}catch(InvalidRumorException|IOException ex){showError(ex.getMessage());}});
        VBox v=new VBox(9,new Label("Title"),title,new Label("Exact claim"),claim,new Label("Category"),cat,new Label("Source"),source,save);v.setPadding(new Insets(20));return v;
    }
    private VBox feedTab(){
        table=new TableView<>(); table.setItems(FXCollections.observableArrayList(rumors));
        table.getColumns().addAll(col("ID","id",80),col("Title","title",260),col("Category","category",150),col("Status","status",150),col("Author","author",120));
        scoreLabel=new Label("Select a row to see its polymorphic score."); table.getSelectionModel().selectedItemProperty().addListener((o,a,r)->{if(r!=null)scoreLabel.setText("Evidence score: "+calculator.calculate(r)+"/100 | Evidence items: "+r.getEvidence().size());});
        Button save=new Button("Save CSV now");save.setOnAction(e->{try{storage.save(rumors);showInfo("Saved to campustruth-data/rumors.csv and evidence.csv");}catch(IOException ex){showError(ex.getMessage());}});
        VBox v=new VBox(10,table,scoreLabel,save);v.setPadding(new Insets(12));VBox.setVgrow(table,Priority.ALWAYS);return v;
    }
    private VBox reviewTab(){
        rumorBox=new ComboBox<>();evidenceType=new ComboBox<>(FXCollections.observableArrayList("Official document","Screenshot","Direct observation","Anonymous report"));evidenceType.setValue("Official document");supportBox=new ComboBox<>(FXCollections.observableArrayList("Supports claim","Opposes claim"));supportBox.setValue("Supports claim");evidenceDescription=new TextArea();evidenceDescription.setPromptText("Describe what this evidence proves");evidenceDescription.setPrefRowCount(4);
        Button add=new Button("Add evidence");add.setOnAction(e->{Rumor r=rumorBox.getValue();if(r==null||evidenceDescription.getText().isBlank()){showError("Choose a rumor and describe evidence");return;}boolean supports=supportBox.getValue().equals("Supports claim");r.addEvidence(EvidenceFactory.create(evidenceType.getValue(),IdGenerator.nextEvidenceId(),evidenceDescription.getText().trim(),supports));try{storage.save(rumors);refresh();evidenceDescription.clear();showInfo("Evidence added. Score recalculated using polymorphism.");}catch(IOException ex){showError(ex.getMessage());}});
        ComboBox<RumorStatus> status=new ComboBox<>(FXCollections.observableArrayList(RumorStatus.values()));status.setValue(RumorStatus.UNDER_REVIEW);Button update=new Button("Update selected status");update.setOnAction(e->{Rumor r=rumorBox.getValue();if(r==null)return;r.setStatus(status.getValue());try{storage.save(rumors);refresh();showInfo("Status updated and saved.");}catch(IOException ex){showError(ex.getMessage());}});
        VBox v=new VBox(9,new Label("Rumor"),rumorBox,new Label("Evidence type"),evidenceType,new Label("Evidence meaning"),supportBox,new Label("Description"),evidenceDescription,add,new Separator(),new Label("Verifier status"),status,update);v.setPadding(new Insets(20));return v;
    }
    private <T> TableColumn<Rumor,T> col(String title,String property,int width){TableColumn<Rumor,T> c=new TableColumn<>(title);c.setCellValueFactory(new PropertyValueFactory<>(property));c.setPrefWidth(width);return c;}
    private void refresh(){if(table!=null){table.setItems(FXCollections.observableArrayList(rumors));table.refresh();}if(rumorBox!=null){rumorBox.setItems(FXCollections.observableArrayList(rumors));if(!rumors.isEmpty())rumorBox.getSelectionModel().select(0);}}
    private void seed(){Rumor r=new Rumor("R-100","Cultural fest is postponed","The cultural fest will happen on 20 October instead of 15 October.","Event","Official department","Demo Student");r.addEvidence(EvidenceFactory.create("Official document","E-200","Student Affairs notice confirms the new date.",true));rumors.add(r);}
    private void showInfo(String s){new Alert(Alert.AlertType.INFORMATION,s).showAndWait();} private void showError(String s){new Alert(Alert.AlertType.ERROR,s).showAndWait();}
    public static void main(String[] args){launch(args);}
}
