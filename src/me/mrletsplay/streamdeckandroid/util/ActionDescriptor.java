package me.mrletsplay.streamdeckandroid.util;

import java.lang.reflect.Method;
import java.util.Objects;

import me.mrletsplay.streamdeck.action.StreamDeckAction;

public class ActionDescriptor {
	
	private Method method;
	
	public ActionDescriptor(Method method) {
		this.method = method;
	}

	public String getID() {
		return method.getAnnotation(StreamDeckAction.class).id();
	}

	public String getName() {
		return method.getAnnotation(StreamDeckAction.class).name();
	}

	public Method getMethod() {
		return method;
	}
	
	@Override
	public String toString() {
		return method == null ? "None" : getName();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof ActionDescriptor)) return false;
		return Objects.equals(((ActionDescriptor) obj).method, method);
	}

}
