package me.mrletsplay.streamdeckandroid.util;

import me.mrletsplay.streamdeck.deck.ButtonState;

public class IncompleteButtonState {
	
	private IncompleteAction action;
	private byte[] bitmap;
	
	public IncompleteButtonState() {}
	
	public IncompleteButtonState(ButtonState initialState) {
		this.action = initialState.getAction() == null ? null : new IncompleteAction(initialState.getAction());
		this.bitmap = initialState.getBitmap();
	}
	
	public void setAction(IncompleteAction action) {
		this.action = action;
	}

	public IncompleteAction getAction() {
		return action;
	}
	
	public void setBitmap(byte[] bitmap) {
		this.bitmap = bitmap;
	}

	public byte[] getBitmap() {
		return bitmap;
	}

}
