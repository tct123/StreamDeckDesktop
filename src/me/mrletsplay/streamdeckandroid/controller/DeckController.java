package me.mrletsplay.streamdeckandroid.controller;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextInputDialog;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import me.mrletsplay.streamdeckandroid.StreamDeckDesktop;

public class DeckController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private ChoiceBox<String> profileBox;
    
    @FXML
    public void saveConfig(ActionEvent event) {
    	FileChooser chooser = new FileChooser();
		chooser.setInitialDirectory(new File("."));
		File f = chooser.showSaveDialog(null);
		if(f == null) return;
		
		if(!f.getName().endsWith(".json")) {
			Alert alert = new Alert(AlertType.INFORMATION, "The file's name doesn't end with a .json extension.\nDo you want to save it with that extension instead? (might override existing files)", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
			alert.setTitle("Save");
			alert.setHeaderText("StreamDeckDesktop");
			Optional<ButtonType> b = alert.showAndWait();
			if(!b.isPresent() || b.get() == ButtonType.CANCEL) return;
			
			if(b.get() == ButtonType.YES) f = new File(f.getAbsolutePath() + ".json");
		}
		
		StreamDeckDesktop.saveConfig(f);
    }
    
    @FXML
    public void loadConfig(ActionEvent event) {
    	FileChooser chooser = new FileChooser();
		chooser.setInitialDirectory(new File("."));
		chooser.getExtensionFilters().add(new ExtensionFilter("JSON Profile data", "*.json"));
		File f = chooser.showOpenDialog(null);
		if(f == null) return;
		StreamDeckDesktop.loadConfig(f);
    }

    @FXML
    void newProfile(ActionEvent event) {
    	TextInputDialog tIn = new TextInputDialog();
    	tIn.setHeaderText("Input profile name");
    	Optional<String> str = tIn.showAndWait();
    	if(str.isPresent()) {
    		String id = str.get();
    		
    		if(StreamDeckDesktop.getDeck().getProfile(id) != null) {
    			Alert a = new Alert(AlertType.ERROR);
    			a.setHeaderText("Can't create profile");
    			a.setContentText("A profile with that name already exists");
    			a.show();
    			return;
    		}
    		
    		StreamDeckDesktop.createNewProfile(str.get(), true);
    		StreamDeckDesktop.selectProfile(str.get());
    	}
    }

    @FXML
    void renameProfile(ActionEvent event) {
    	TextInputDialog tIn = new TextInputDialog();
    	tIn.setTitle("Create Profile");
    	tIn.setHeaderText("Input profile name");
    	Optional<String> str = tIn.showAndWait();
    	if(str.isPresent()) {
    		StreamDeckDesktop.renameProfile(str.get(), true);
    	}
    }

    @FXML
    void deleteProfile(ActionEvent event) {
    	if(StreamDeckDesktop.getDeck().getCurrentProfile() == null) return;
    	StreamDeckDesktop.deleteProfile(true);
    }
    
    public void addProfile(String profile) {
    	profileBox.getItems().add(profile);
    }
    
    public void setProfiles(List<String> profiles) {
    	profileBox.getItems().clear();
    	profileBox.getItems().addAll(profiles);
    }
    
    public void renameProfile(String profile, String newID) {
    	int idx = profileBox.getItems().indexOf(profile);
    	if(idx == -1) return;
    	boolean selected = profileBox.getValue() != null && profileBox.getValue().equals(profile);
    	profileBox.getItems().remove(profile);
    	profileBox.getItems().add(idx, newID);
    	if(selected) profileBox.setValue(newID);
    }

    public void removeProfile(String profile) {
    	profileBox.getItems().remove(profile);
    }
    
    public void selectProfile(String profile) {
    	profileBox.setValue(profile);
    }

    @FXML
    void initialize() {
    	profileBox.setOnAction(event -> {
    		StreamDeckDesktop.selectProfile(profileBox.getValue());
    	});
    }
}
