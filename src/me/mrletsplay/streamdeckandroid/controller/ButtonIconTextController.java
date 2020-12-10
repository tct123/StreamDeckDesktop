package me.mrletsplay.streamdeckandroid.controller;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

import javafx.fxml.FXML;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import me.mrletsplay.streamdeckandroid.util.BitmapUtils;

public class ButtonIconTextController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private AnchorPane pane;

    @FXML
    private ImageView buttonIcon;

    @FXML
    private TextArea textArea;

    @FXML
    private Slider fontSizeSlider;
    
    @FXML
    private ColorPicker textColor;

    private Color baseIconBackground;

    private BufferedImage baseIcon;
    
    private BufferedImage overlay;
    
    private BufferedImage combined;
    
    @FXML
    void initialize() {
    	this.overlay = BitmapUtils.newBufferedImage();
    	this.combined = BitmapUtils.newBufferedImage();
    	
    	textArea.textProperty().addListener((it, o, n) -> updateText());
    	fontSizeSlider.valueProperty().addListener((it, o, n) -> updateText());
    	textColor.setValue(javafx.scene.paint.Color.BLACK);
    	textColor.valueProperty().addListener((it, o, n) -> updateText());
    }
    
    void updateText() {
    	String[] lines = textArea.getText().split("\n");
    	float fontSize = (float) fontSizeSlider.getValue();
    	
    	Graphics2D cGr = overlay.createGraphics();
    	cGr.setBackground(new Color(255, 255, 255, 0));
    	cGr.clearRect(0, 0, overlay.getWidth(), overlay.getHeight());
    	cGr.setFont(cGr.getFont().deriveFont(fontSize));
    	
    	javafx.scene.paint.Color fxColor = textColor.getValue();
    	cGr.setColor(new Color(
    			(float) fxColor.getRed(),
    			(float) fxColor.getGreen(),
    			(float) fxColor.getBlue(),
    			(float) fxColor.getOpacity()));
    	BitmapUtils.drawLinesCentered(cGr, 64, 64, lines);
    	
    	updateButtonIcon();
    }
    
    void updateButtonIcon() {
    	Graphics2D cGr = combined.createGraphics();
    	cGr.setBackground(baseIconBackground);
    	cGr.clearRect(0, 0, combined.getWidth(), combined.getHeight());
    	cGr.drawImage(baseIcon, 0, 0, null);
    	cGr.drawImage(overlay, 0, 0, null);
    	
    	ByteArrayOutputStream bOut = new ByteArrayOutputStream();
    	try {
    		ImageIO.write(combined, "PNG", bOut);
    	}catch(IOException e) {}
    	buttonIcon.setImage(new Image(new ByteArrayInputStream(bOut.toByteArray())));
    }
    
    public void setBaseIcon(Color background, BufferedImage icon) {
    	this.baseIconBackground = background;
    	this.baseIcon = icon;
    	updateButtonIcon();
    }
    
    public BufferedImage compileIcon() {
    	Graphics2D cGr = combined.createGraphics();
    	cGr.setBackground(new Color(255, 255, 255, 0));
    	cGr.clearRect(0, 0, combined.getWidth(), combined.getHeight());
    	cGr.drawImage(baseIcon, 0, 0, null);
    	cGr.drawImage(overlay, 0, 0, null);
    	
    	return combined;
    }
    
}
