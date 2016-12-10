package visuals;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.stream.Collectors;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

import models.FormComponent;
import models.FormField;

public class OButtonGroup extends ButtonGroup implements FormComponent {
	private static final long serialVersionUID = -4237967458406808827L;
	private ArrayList<OMultipleOption> options = new ArrayList<>();
	
	public OButtonGroup(FormField field) {
		setOptions(field.getMultipleOptions());
		
		String[] radioButtonNames = field.getMultipleOptionsAsArray(field.areOptionsSorted());
		
		for(int i = 0; i < radioButtonNames.length; i++) {
			JRadioButton currentRadioButton = new JRadioButton(radioButtonNames[i]);
			this.add(currentRadioButton);
			
			if(i == field.getDefaultSelectedEntityId()) {
				this.setSelected(currentRadioButton.getModel(), true);
			}
		}
	}
	
	public int getSelectedEntityId() {
		int entityId = 0;
		
		for (Enumeration<AbstractButton> buttons = getElements(); buttons.hasMoreElements();) {
			AbstractButton radioButton = buttons.nextElement();
			
			if(radioButton.isSelected()) {
				entityId = options.stream().filter(option -> option.getName().equals(radioButton.getText())).map(OMultipleOption::getId).collect(Collectors.toList()).toArray(new Integer[]{})[0];
				break;
			}
		}
		
		return entityId;
	}

	public ArrayList<OMultipleOption> getOptions() {
		return options;
	}

	public void setOptions(ArrayList<OMultipleOption> options) {
		this.options = options;
	}

	@Override
	public boolean validateFormComponent() {
		// TODO Auto-generated method stub
		return false;
	}
}