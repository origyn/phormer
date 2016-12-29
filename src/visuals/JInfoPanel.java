package visuals;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class JInfoPanel extends JPanel {
	private static final long serialVersionUID = -256817526257668485L;
	int entityId;
	String entityName;
	
	public JInfoPanel() {
		
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
	public void setEnergyValue(String entityName) {
		this.entityName = entityName;
		
		this.setBorder(BorderFactory.createTitledBorder(entityName));
	}
}
