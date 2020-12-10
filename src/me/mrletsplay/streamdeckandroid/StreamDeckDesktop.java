package me.mrletsplay.streamdeckandroid;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.robot.Robot;
import javafx.stage.Stage;
import me.mrletsplay.mrcore.io.IOUtils;
import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.json.JSONType;
import me.mrletsplay.mrcore.json.converter.JSONConverter;
import me.mrletsplay.streamdeck.action.Action;
import me.mrletsplay.streamdeck.deck.ButtonState;
import me.mrletsplay.streamdeck.deck.StreamDeck;
import me.mrletsplay.streamdeck.deck.StreamDeckButton;
import me.mrletsplay.streamdeck.deck.StreamDeckProfile;
import me.mrletsplay.streamdeckandroid.controller.DeckController;

public class StreamDeckDesktop extends Application {
	
	private static final String KEY = "C34$2bivrqcAM@otTVgoSA9mz8qLNeGRhMwesGogF7uBakTWb$7YCnZCz!9ToY6obsdiGS^fMx%x2ySuv@Lr8!2EhGwPMxRYD5gAtNYq^q5bhJjdVWaTwUyFNwF^dDUA";
	
	public static Stage stage;
	
	private static DeckController deckController;
	
	private static ServerSocket serverSocket;
	
	private static StreamDeck deck;
	
	private static DataOutputStream dOut;
	
	private static Map<Integer, Button> uiButtons;
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		deck = new StreamDeck(15);
//		StreamDeckProfile p = deck.createNewProfile("default");
//		Random r = new Random();
//		p.getButton(0).setBitmap(BitmapUtils.text("ID: " + 0, 32, Color.WHITE, new Color(r.nextFloat(), r.nextFloat(), r.nextFloat())));
//		deck.selectProfile(p);
		
		stage = primaryStage;
		
		URL url = StreamDeckDesktop.class.getResource("/include/deck.fxml");
		if(url == null) url = new File("./include/deck.fxml").toURI().toURL();
		
		FXMLLoader l = new FXMLLoader(url);
		Parent pr = l.load(url.openStream());
		deckController = l.getController();
		
		uiButtons = new HashMap<>();
		
		AtomicInteger i = new AtomicInteger();
		pr.lookupAll(".deck-button").forEach(n -> {
			Button btn = (Button) n;
			int id = i.getAndIncrement();
			btn.getProperties().put("button-id", id);
			uiButtons.put(id, btn);
		});
		
		Scene sc = new Scene(pr);
		primaryStage.setOnCloseRequest(event -> exit());
		primaryStage.setTitle("StreamDeckDesktop");
		primaryStage.setResizable(false);
		primaryStage.setScene(sc);
		primaryStage.show();
		
		serverSocket = new ServerSocket(10238);
		
		new Thread(() -> {
			while(true) {
				try {
					Socket s = serverSocket.accept();
					DataInputStream dIn = new DataInputStream(s.getInputStream());
					dOut = new DataOutputStream(s.getOutputStream());
					new Thread(() -> {
						try {
							String key = dIn.readUTF();
							if(!key.equals(KEY)) s.close();
							
							while(!s.isClosed()) {
								JSONObject obj = new JSONObject(dIn.readUTF());
								
								if(obj.isOfType("requestData", JSONType.BOOLEAN) && obj.getBoolean("requestData")) {
									sendAllProfiles();
									continue;
								}
								
								Action a = JSONConverter.decodeObject(obj.getJSONObject("action"), Action.class);
								JSONObject data = a.getData();
								switch(a.getType()) {
									case RUN_TERMINAL:
										Runtime.getRuntime().exec(data.getString("command"));
										break;
									case TYPE_TEXT:
										new ProcessBuilder("bash", "-c", "setxkbmap de && xdotool type \"" + data.getString("text").replace("\\", "\\\\").replace("\"", "\\\"") + "\"")
											.inheritIO()
											.start();
										break;
									case PRESS_FUNCTION_KEY:
										System.out.println(data.getString("key"));
										Platform.runLater(() -> new Robot().keyType(KeyCode.valueOf(data.getString("key"))));
										break;
									default:
										break;
								}
							}
						}catch(IOException e) {}
					}).start();
				}catch(Exception e) {
					continue;
				}
			}
		}).start();
		
		String config = getParameters().getNamed().get("config");
		if(config != null) {
			Platform.runLater(() -> loadConfig(new File(config)));
		}else {
			createNewProfile("default", false);
			selectProfile("default");
//			for(StreamDeckButton b : deck.getCurrentProfile().getButtons()) {
//				Random r = new Random();
//				setButtonStates(b.getID(), new ButtonState[] {new ButtonState(null, BitmapUtils.text("ID: " + b.getID(), 32, Color.WHITE, new Color(r.nextFloat(), r.nextFloat(), r.nextFloat())))}, false);
//			}
		}
	}
	
	private static void sendAllProfiles() {
		try {
			if(dOut == null) return;
			JSONArray arr = new JSONArray(deck.getProfiles().stream()
					.map(StreamDeckProfile::toJSON)
					.collect(Collectors.toList()));
			JSONObject data = new JSONObject();
			data.put("command", "setProfiles");
			data.put("profiles", arr);
			String dat = data.toString();
			dOut.writeInt(dat.length());
			dOut.write(dat.getBytes(StandardCharsets.UTF_8));
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void sendData(JSONObject obj) {
		try {
			if(dOut == null) return;
			String dat = obj.toString();
			dOut.writeInt(dat.length());
			dOut.write(dat.getBytes(StandardCharsets.UTF_8));
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void exit() {
		try {
			serverSocket.close();
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void updateUIBitmap(int buttonID) {
		Button uiButton = uiButtons.get(buttonID);
		
		if(deck.getCurrentProfile() == null) {
			uiButton.setGraphic(null);
			return;
		}
		
		ButtonState state = deck.getCurrentProfile().getButton(buttonID).getState(0);
		byte[] bitmap = state == null ? null : state.getBitmap();
		
		if(bitmap != null) {
			ImageView v = new ImageView(new Image(new ByteArrayInputStream(bitmap)));
			v.setFitWidth(uiButton.getWidth());
			v.setFitHeight(uiButton.getHeight());
			uiButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
			uiButton.setGraphic(v);
		}else {
			uiButton.setGraphic(null);
		}
	}

	public static void setButtonStates(int buttonID, ButtonState[] states, boolean send) {
		if(deck.getCurrentProfile() == null) return;
		
		deck.getCurrentProfile().getButton(buttonID).setStates(states);
		updateUIBitmap(buttonID);
		
		if(send) {
			JSONObject data = new JSONObject();
			data.put("command", "setButtonStates");
			data.put("profile", deck.getCurrentProfile().getIdentifier());
			data.put("button", buttonID);
			data.put("states", new JSONArray(Arrays.stream(states)
					.map(s -> s.toJSON())
					.collect(Collectors.toList())));
			sendData(data);
		}
	}

	public static StreamDeckButton getButton(int buttonID) {
		if(deck.getCurrentProfile() == null) return null;
		return deck.getCurrentProfile().getButton(buttonID);
	}
	
	public static StreamDeckProfile createNewProfile(String id, boolean send) {
		StreamDeckProfile p = deck.createNewProfile(id);
		deckController.addProfile(id);
		
		if(send) {
			JSONObject data = new JSONObject();
			data.put("command", "createProfile");
			data.put("profile", id);
			sendData(data);
		}
		
		return p;
	}
	
	public static void renameProfile(String id, boolean send) {
		if(deck.getCurrentProfile() == null) return;
		String oldID = deck.getCurrentProfile().getIdentifier();
		deck.getCurrentProfile().setIdentifier(id);
		deckController.renameProfile(oldID, id);
		
		if(send) {
			JSONObject data = new JSONObject();
			data.put("command", "renameProfile");
			data.put("profile", oldID);
			data.put("newID", id);
			sendData(data);
		}
	}
	
	public static void deleteProfile(boolean send) {
		if(deck.getCurrentProfile() == null) return;
		String ident = deck.getCurrentProfile().getIdentifier();
		deck.deleteProfile(ident);
		deckController.removeProfile(ident);
		
		if(!deck.getProfiles().isEmpty()) {
			selectProfile(deck.getProfiles().get(0).getIdentifier());
		}else {
			selectProfile(null);
		}
		
		if(send) {
			JSONObject data = new JSONObject();
			data.put("command", "deleteProfile");
			data.put("profile", ident);
			sendData(data);
		}
	}
	
	public static void selectProfile(String profile) {
		deck.selectProfile(profile);
		deckController.selectProfile(profile);
		
		for(int i = 0; i < deck.getButtonCount(); i++)
			updateUIBitmap(i);
	}
	
	public static StreamDeck getDeck() {
		return deck;
	}
	
	public static void saveConfig(File file) {
		IOUtils.createFile(file);
		try(FileOutputStream fOut = new FileOutputStream(file)) {
			fOut.write(new JSONArray(deck.getProfiles().stream()
					.map(p -> p.toJSON(false))
					.collect(Collectors.toList())).toString().getBytes(StandardCharsets.UTF_8));
		}catch(IOException e) {
			Alert a = new Alert(AlertType.ERROR);
			a.setHeaderText("An unexptected exception occured");
			a.setContentText(e.toString());
			a.show();
		}
	}
	
	public static void loadConfig(File file) {
		if(!file.exists()) return;
		try(FileInputStream fOut = new FileInputStream(file)) {
			String str = new String(fOut.readAllBytes(), StandardCharsets.UTF_8);
			deck.setProfiles(new JSONArray(str).stream()
					.map(o -> JSONConverter.decodeObject((JSONObject) o, StreamDeckProfile.class))
					.collect(Collectors.toList()));
			deckController.setProfiles(deck.getProfiles().stream()
					.map(p -> p.getIdentifier())
					.collect(Collectors.toList()));
			selectProfile(deck.getProfiles().get(0).getIdentifier());
			sendAllProfiles();
		}catch(IOException e) {
			Alert a = new Alert(AlertType.ERROR);
			a.setHeaderText("An unexptected exception occured");
			a.setContentText(e.toString());
			a.show();
		}
	}
	
}
