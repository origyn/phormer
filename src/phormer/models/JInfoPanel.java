package phormer.models;

import java.util.EventObject;

import javax.swing.JPanel;

public class JInfoPanel extends JPanel {
	private static final long serialVersionUID = -256817526257668485L;
	int entityId;
	String entityName;
	FormListener listener;
	
	public JInfoPanel() {
		super();
	}
	
	public void triggerInfoPanelRefresh() {
		if(listener != null) {
			listener.onSubmit(new EventObject(this));
		}
	}
	
	public void addFormListener(FormListener listener) {
		this.listener = listener;
	}
	
	public int getEntityId() {
		return entityId;
	}
	
	public void setEntityId(int entityId) {
		this.entityId = entityId;
	}

	public String getEntityName() {
		return entityName;
	}

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}
}
