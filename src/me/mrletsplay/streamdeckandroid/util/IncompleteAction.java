package me.mrletsplay.streamdeckandroid.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.misc.FriendlyException;
import me.mrletsplay.streamdeck.action.Action;
import me.mrletsplay.streamdeck.action.StreamDeckAction;
import me.mrletsplay.streamdeck.action.StreamDeckActionParameter;

public class IncompleteAction {
	
	private ActionDescriptor descriptor;
	
	private JSONObject parameters;
	
	public IncompleteAction(ActionDescriptor descriptor) {
		this.descriptor = descriptor;
		this.parameters = new JSONObject();
	}
	
	public IncompleteAction(Action initialValue) {
		String methodID = initialValue.getData().getString("method_id");
		
		for(Method m : Action.class.getDeclaredMethods()) {
			StreamDeckAction d = m.getAnnotation(StreamDeckAction.class);
			if(d == null) continue;
			if(d.id().equals(methodID)) this.descriptor = new ActionDescriptor(m);
		}
		
		if(this.descriptor == null) throw new FriendlyException("Unknown action method id");
		
		this.parameters = new JSONObject(initialValue.getData());
	}
	
	public void setParameter(String name, Object value) {
		parameters.put(name, value);
	}
	
	public void unsetParameter(String name) {
		parameters.remove(name);
	}
	
	public ActionDescriptor getDescriptor() {
		return descriptor;
	}
	
	public JSONObject getParameters() {
		return parameters;
	}
	
	public boolean isComplete() {
		return getMissingFieldNames().isEmpty();
	}
	
	public List<String> getMissingFieldNames() {
		return Arrays.stream(descriptor.getMethod().getParameters())
				.filter(p -> !parameters.has(p.getAnnotation(StreamDeckActionParameter.class).name()))
				.map(p -> p.getAnnotation(StreamDeckActionParameter.class).friendlyName())
				.collect(Collectors.toList());
	}
	
	public Action toAction() {
		try {
			return (Action) descriptor.getMethod().invoke(null, Arrays.stream(descriptor.getMethod().getParameters())
					.map(p -> {
						Object val = parameters.get(p.getAnnotation(StreamDeckActionParameter.class).name());
						if(p.getType().isEnum()) {
							try {
								return p.getType().getMethod("valueOf", String.class).invoke(null, (String) val);
							} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
									| NoSuchMethodException | SecurityException e) {
								throw new FriendlyException(e);
							}
						}
						
						return val;
					})
					.toArray(Object[]::new));
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new FriendlyException("Failed to create action", e);
		}
	}
	
	@Override
	public String toString() {
		return "{INCOMPLETE ACTION: " + descriptor.getName() + "}";
	}

}
