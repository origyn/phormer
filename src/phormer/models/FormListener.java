package phormer.models;

import java.util.EventObject;

public interface FormListener {
	public void onSubmit(EventObject e);
	public void onCancel(EventObject e);
}
