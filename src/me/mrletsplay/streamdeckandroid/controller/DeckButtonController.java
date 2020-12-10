package me.mrletsplay.streamdeckandroid.controller;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import me.mrletsplay.mrcore.misc.FriendlyException;
import me.mrletsplay.streamdeck.action.Action;
import me.mrletsplay.streamdeck.action.StreamDeckAction;
import me.mrletsplay.streamdeck.action.StreamDeckActionParameter;
import me.mrletsplay.streamdeck.deck.ButtonState;
import me.mrletsplay.streamdeckandroid.StreamDeckDesktop;
import me.mrletsplay.streamdeckandroid.util.ActionDescriptor;
import me.mrletsplay.streamdeckandroid.util.IncompleteAction;
import me.mrletsplay.streamdeckandroid.util.IncompleteButtonState;

public class DeckButtonController {

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private Button button;

	@SuppressWarnings("unchecked")
	@FXML
	void onAction(ActionEvent event) {
		try {
			if(StreamDeckDesktop.getDeck().getCurrentProfile() == null) return;
			
			URL url = StreamDeckDesktop.class.getResource("/include/button-options.fxml");
			if (url == null)
				url = new File("./include/button-options.fxml").toURI().toURL();
			
			int buttonID = (int) button.getProperties().get("button-id");
			
			Dialog<ButtonState[]> dialog = new Dialog<>();
			dialog.setTitle("Edit Button");
			dialog.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
			dialog.getDialogPane().setMaxHeight(Region.USE_PREF_SIZE);

			FXMLLoader l = new FXMLLoader(url);
			Parent pr = l.load(url.openStream());
			
			List<IncompleteButtonState> states = Arrays.stream(StreamDeckDesktop.getButton(buttonID).getStates())
					.map(IncompleteButtonState::new)
					.collect(Collectors.toList());
			
			if(states.isEmpty()) states.add(new IncompleteButtonState());
			
			ChoiceBox<Integer> statesBox = (ChoiceBox<Integer>) pr.lookup("#buttonStatesBox");
			
			for(int i = 0; i < states.size(); i++) {
				statesBox.getItems().add(i);
			}

			ChoiceBox<ActionDescriptor> actionDropdown = (ChoiceBox<ActionDescriptor>) pr.lookup("#actionDropdown");
			
			ActionDescriptor noneAction = new ActionDescriptor(null);
			actionDropdown.getItems().add(noneAction);
			
			for(Method m : Action.class.getDeclaredMethods()) {
				if(!m.isAnnotationPresent(StreamDeckAction.class)) continue;
				ActionDescriptor desc = new ActionDescriptor(m);
				actionDropdown.getItems().add(desc);
			}
			
			VBox contentBox = (VBox) pr.lookup("#contentBox");
			contentBox.setSpacing(5);
			
			ImageView buttonIcon = (ImageView) pr.lookup("#buttonIcon");
			
			actionDropdown.valueProperty().addListener((it, o, n) -> {
				contentBox.getChildren().removeIf(node -> node.getStyleClass().contains("action-parameter"));
				Method m = n == null ? null : n.getMethod();
				IncompleteButtonState state = states.get(statesBox.getValue());
				
				if(!Objects.equals(n, state.getAction() == null ? null : state.getAction().getDescriptor())) {
					state.setAction(m == null ? null : new IncompleteAction(n));
				}
				
				if(m == null) {
					dialog.getDialogPane().getScene().getWindow().sizeToScene();
					return;
				}
				
				for(Parameter p : m.getParameters()) {
					if(p.getType().equals(String.class)) {
						TextField f = new TextField();
						StreamDeckActionParameter pAn = p.getAnnotation(StreamDeckActionParameter.class);
						f.setPromptText(pAn.friendlyName());
						f.getStyleClass().add("action-parameter");
						f.getProperties().put("property-name", pAn.name());
						f.textProperty().addListener((it2, o2, n2) -> {
							IncompleteButtonState state2 = states.get(statesBox.getValue());
							if(n2 == null || n2.isBlank()) {
								state2.getAction().unsetParameter(pAn.name());
							}else {
								state2.getAction().setParameter(pAn.name(), n2);
							}
						});
						contentBox.getChildren().add(contentBox.getChildren().indexOf(actionDropdown) + 1, f);
					}else if(p.getType().equals(int.class) || p.getType().equals(Integer.class)) {
						TextField f = new TextField();
						StreamDeckActionParameter pAn = p.getAnnotation(StreamDeckActionParameter.class);
						f.setPromptText(pAn.friendlyName());
						f.getStyleClass().add("action-parameter");
						f.getProperties().put("property-name", pAn.name());
						
						f.textProperty().addListener((it2, o2, n2) -> {
							IncompleteButtonState state2 = states.get(statesBox.getValue());
							if(n2 == null || n2.isBlank()) {
								state2.getAction().unsetParameter(pAn.name());
							}else {
								if (!n2.matches("\\d*")) {
						            f.setText(n2.replaceAll("[^\\d]", ""));
						        }
								
								state2.getAction().setParameter(pAn.name(), Integer.parseInt(n2));
							}
						});
						contentBox.getChildren().add(contentBox.getChildren().indexOf(actionDropdown) + 1, f);
					}else if(Enum.class.isAssignableFrom(p.getType())) {
						Enum<?>[] enums = (Enum<?>[]) p.getType().getEnumConstants();
						
						ChoiceBox<String> f = new ChoiceBox<>();
						StreamDeckActionParameter pAn = p.getAnnotation(StreamDeckActionParameter.class);
//						f.setPromptText(pAn.friendlyName());
						f.getStyleClass().add("action-parameter");
						f.getProperties().put("property-name", pAn.name());
						f.getItems().addAll(Arrays.stream(enums).map(Enum::name).collect(Collectors.toList()));
						
						f.valueProperty().addListener((it2, o2, n2) -> {
							IncompleteButtonState state2 = states.get(statesBox.getValue());
							state2.getAction().setParameter(pAn.name(), n2);
						});
						contentBox.getChildren().add(contentBox.getChildren().indexOf(actionDropdown) + 1, f);
					}
				}
				
				dialog.getDialogPane().getScene().getWindow().sizeToScene();	
			});
			
			Button changeIconButton = (Button) pr.lookup("#changeIconButton");
			changeIconButton.setOnAction(acEvent -> {
				byte[] bitmap = changeButtonIcon(dialog);
				if(bitmap == null) return;
				IncompleteButtonState state = states.get(statesBox.getValue());
				buttonIcon.setImage(new Image(new ByteArrayInputStream(bitmap)));
				state.setBitmap(bitmap);
			});
			
			Runnable updateState = () -> {
				IncompleteButtonState state = states.get(statesBox.getValue());
				IncompleteAction ac = state.getAction();
				actionDropdown.setValue(ac == null ? noneAction : ac.getDescriptor());
				
				byte[] bitmap = state.getBitmap();
				buttonIcon.setImage(bitmap == null ? null : new Image(new ByteArrayInputStream(bitmap)));
				
				contentBox.getChildren().stream()
					.filter(nd -> nd.getStyleClass().contains("action-parameter"))
					.forEach(nd -> {
						String prop = (String) nd.getProperties().get("property-name");
						if(nd instanceof TextField) ((TextField) nd).setText(String.valueOf(ac.getParameters().getOrDefault(prop, null)));
						if(nd instanceof ChoiceBox<?>) ((ChoiceBox<?>) nd).setValue(null);
					});
			};
			
			statesBox.valueProperty().addListener((it, o, n) -> updateState.run());
			
			statesBox.setValue(0);
			
			Button newStateButton = (Button) pr.lookup("#newStateButton");
			
			newStateButton.setOnAction(a -> {
				states.add(new IncompleteButtonState());
				int index = statesBox.getItems().size();
				statesBox.getItems().add(index);
				statesBox.setValue(index);
			});
			
			Button deleteStateButton = (Button) pr.lookup("#deleteStateButton");
			
			deleteStateButton.setOnAction(a -> {
				states.remove((int) statesBox.getValue());
				
				if(states.isEmpty()) {
					states.add(new IncompleteButtonState());
				}
				
				statesBox.setValue(Math.min(states.size() - 1, statesBox.getValue()));
				if(statesBox.getItems().size() > 1) statesBox.getItems().remove(statesBox.getItems().size() - 1);
				updateState.run();
			});
			
			dialog.getDialogPane().setContent(pr);
			
			dialog.setResultConverter(btn -> {
				if(btn != ButtonType.OK) return null;
				
				return states.stream()
						.map(s -> new ButtonState(s.getAction() == null ? null : s.getAction().toAction(), s.getBitmap()))
						.toArray(ButtonState[]::new);
			});
			
			dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
			
			dialog.getDialogPane().lookupButton(ButtonType.OK).addEventFilter(ActionEvent.ACTION, a -> {
				List<String> missing = new ArrayList<>();
				for(int i = 0; i < states.size(); i++) {
					IncompleteButtonState state = states.get(i);
					if(state.getAction() == null) continue;
					
					List<String> m = state.getAction().getMissingFieldNames();
					if(!m.isEmpty()) {
						final int fI = i;
						m.forEach(f -> missing.add(String.format("State #%s (%s): %s", fI, state.getAction().getDescriptor().getName(), f)));
					}
				}
				
				if(!missing.isEmpty()) {
					Alert al = new Alert(AlertType.ERROR);
					al.setContentText("You're missing the following fields:\n\n" + missing.stream().collect(Collectors.joining("\n")));
					al.show();
					a.consume();
				}
			});
			
			ButtonState[] newStates = dialog.showAndWait().orElse(null);
			if(newStates == null) return;
			StreamDeckDesktop.setButtonStates(buttonID, newStates, true);
		} catch (IOException e) {
			Alert a = new Alert(AlertType.ERROR);
			a.setHeaderText("An unexptected exception occured");
			a.setContentText(e.toString());
			a.show();
		}
	}
	
	private byte[] changeButtonIcon(Dialog<?> owner) {
		try {
			Dialog<byte[]> dialog = new Dialog<>();
			dialog.setTitle("Change Button Icon");
			dialog.initOwner(owner.getDialogPane().getScene().getWindow());
			
			URL url = StreamDeckDesktop.class.getResource("/include/button-icon.fxml");
			if (url == null)
				url = new File("./include/button-icon.fxml").toURI().toURL();

			FXMLLoader l = new FXMLLoader(url);
			Parent pr = l.load(url.openStream());
			ButtonIconController controller = l.getController();
			
			dialog.getDialogPane().setContent(pr);
			dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
			
			dialog.setResultConverter(button -> {
				if(button != ButtonType.OK) return null;
				
				BufferedImage icon = controller.compileIcon();
				ByteArrayOutputStream bOut = new ByteArrayOutputStream();
				try {
					ImageIO.write(icon, "PNG", bOut);
				}catch(IOException e) {}
				
				return bOut.toByteArray();
			});
			
			return dialog.showAndWait().orElse(null);
		}catch(IOException e) {
			Alert a = new Alert(AlertType.ERROR);
			a.setHeaderText("An unexptected exception occured");
			a.setContentText(e.toString());
			a.show();
			throw new FriendlyException(e);
		}
	}

	@FXML
	void initialize() {

	}
}
