package me.mrletsplay.streamdeckandroid.controller;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Dialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import me.mrletsplay.streamdeckandroid.StreamDeckDesktop;
import me.mrletsplay.streamdeckandroid.util.BitmapUtils;

public class ButtonIconController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private AnchorPane pane;

    @FXML
    private ImageView buttonIcon;

    @FXML
    private ColorPicker backgroundColor;
    
    private BufferedImage icon;
    
    private Color iconBackground;
    
    private BufferedImage combined;

    @FXML
    void addText(ActionEvent event) {
    	try {
			Dialog<BufferedImage> dialog = new Dialog<>();
			dialog.setTitle("Add Text");
			dialog.initOwner(pane.getScene().getWindow());
			
			URL url = StreamDeckDesktop.class.getResource("/include/button-icon-text.fxml");
			if (url == null)
				url = new File("./include/button-icon-text.fxml").toURI().toURL();

			FXMLLoader l = new FXMLLoader(url);
			Parent pr = l.load(url.openStream());
			ButtonIconTextController controller = l.getController();
			controller.setBaseIcon(iconBackground, icon);
			
			dialog.getDialogPane().setContent(pr);
			dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
			
			dialog.setResultConverter(button -> {
				if(button != ButtonType.OK) return null;
				
				return controller.compileIcon();
			});
			
			BufferedImage img = dialog.showAndWait().orElse(null);
			if(img != null) {
				icon = img;
				updateIcon();
			}
		}catch(IOException e) {
			Alert a = new Alert(AlertType.ERROR);
			a.setHeaderText("An unexptected exception occured");
			a.setContentText(e.toString());
			a.show();
		}
    }

    @FXML
    void selectFile(ActionEvent event) {
    	FileChooser chooser = new FileChooser();
		chooser.setInitialDirectory(new File("."));
		chooser.getExtensionFilters().add(new ExtensionFilter("Image Icon", "*.png", "*.jpeg", "*.jpg"));
		File f = chooser.showOpenDialog(pane.getScene().getWindow());
		if(f == null) return;
		try {
			BufferedImage img = ImageIO.read(f);
			BufferedImage copy = new BufferedImage(128, 128, BufferedImage.TYPE_4BYTE_ABGR);
			Graphics2D g2d = copy.createGraphics();
			
			if(img.getWidth() > img.getHeight()) {
				int newH = (int) ((double) img.getHeight() / img.getWidth() * 128);
				g2d.drawImage(img.getScaledInstance(128, newH, BufferedImage.SCALE_SMOOTH), 0, (128 - newH) / 2, 128, newH, null);
			}else {
				int newW = (int) ((double) img.getWidth() / img.getHeight() * 128);
				g2d.drawImage(img.getScaledInstance(newW, 128, BufferedImage.SCALE_SMOOTH), (128 - newW) / 2, 0, newW, 128, null);
			}
			
			icon = copy;
			updateIcon();
		} catch (IOException e) {
			Alert a = new Alert(AlertType.ERROR);
			a.setHeaderText("An unexptected exception occured");
			a.setContentText(e.toString());
			a.show();
		}
    }
    
    private void updateIcon() {
    	try {
    		Graphics2D g2d = combined.createGraphics();
    		g2d.setBackground(iconBackground);
    		g2d.clearRect(0, 0, combined.getWidth(), combined.getHeight());
    		
    		g2d.drawImage(icon, 0, 0, null);
    		
			ByteArrayOutputStream bOut = new ByteArrayOutputStream();
			ImageIO.write(combined, "PNG", bOut);
	
			buttonIcon.setImage(new Image(new ByteArrayInputStream(bOut.toByteArray())));
    	}catch(IOException e) {}
    }
    
    public BufferedImage compileIcon() {
		return combined;
	}

    @FXML
    void initialize() {
    	icon = BitmapUtils.newBufferedImage();
    	iconBackground = new Color(255, 255, 255);
    	combined = BitmapUtils.newBufferedImage();
    	
    	backgroundColor.setValue(new javafx.scene.paint.Color(iconBackground.getRed() / 255d, iconBackground.getGreen() / 255d, iconBackground.getBlue() / 255d, iconBackground.getAlpha() / 255d));
    	backgroundColor.valueProperty().addListener((it, o, n) -> {
    		iconBackground = new Color((float) n.getRed(), (float) n.getGreen(), (float) n.getBlue(), (float) n.getOpacity());
    		updateIcon();
    	});
    }
}
