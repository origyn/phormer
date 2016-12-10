package visuals;

import java.awt.Color;
import java.awt.Cursor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import models.FormComponent;
import models.FormField;

public class OComboBox<E> extends JComboBox<E> implements FormComponent {
	private static final long serialVersionUID = 880424372897780521L;
	private boolean toBeSelected = false, selectionLocked = false;
	private ArrayList<OMultipleOption> options = new ArrayList<>();
	private int defaultSelectedIndex = 0;
	JButton btNew;
	
	public OComboBox(FormField field) {
		build(field);
	}
	
	public void build(FormField field) {
		setToBeSelected(field.isMandatory());
		setOptions(field.getMultipleOptions());
		setSelectionLocked(field.isSelectionLocked());
		
		if(field.isExpandable()) {
			btNew = new JButton(new ImageIcon("files/pix/new.png"));
			btNew.setBorder(null);
			btNew.setOpaque(false);
			btNew.setContentAreaFilled(false);
			btNew.setBorderPainted(false);
			btNew.setCursor(new Cursor(Cursor.HAND_CURSOR));
			btNew.setVerticalTextPosition(SwingConstants.BOTTOM);
			btNew.setHorizontalTextPosition(SwingConstants.CENTER);
			btNew.setPressedIcon(new ImageIcon("files/pix/new_issue_pressed.png"));
		}
		
		updateComboBoxState();
		
		fillOptions(field);
	}
	
	public void rebuildOptions(FormField field) {
		build(field);
	}
	
	public void updateComboBoxState() {
		if(this.selectionLocked || this.options == null) {
			this.setEditable(true);
			((JTextField) this.getEditor().getEditorComponent()).setDisabledTextColor(Color.BLACK);
			((JTextField) this.getEditor().getEditorComponent()).setBackground(Color.LIGHT_GRAY);
			this.setEnabled(false);
		}
		else {
			((JTextField) this.getEditor().getEditorComponent()).setBackground(null);
			this.setEditable(false);
			this.setEnabled(true);
		}
	}
	
	public boolean isToBeSelected() {
		return toBeSelected;
	}

	public void setToBeSelected(boolean toBeSelected) {
		this.toBeSelected = toBeSelected;
	}

	public boolean validateFormComponent() {
		String errorMessage = "<html>";
		boolean error = false;
		
		if(toBeSelected && this.getSelectedIndex() == 0) {
			errorMessage += "You must choose an item.";
			error = true;
		}
		
		errorMessage += "</html>";
		
		if(error) {
			this.setBorder(BorderFactory.createLineBorder(Color.RED));
			this.setToolTipText(errorMessage);
		}
		else {
			this.setBorder(UIManager.getBorder("ComboBox.border"));
			this.setToolTipText(null);
		}
		
		return error;
	}
	
	public int getSelectedEntityId() {
		if(this.getOptions() == null) {
			return 0;
		}
		else {
			Integer[] ids = this.getOptions().stream()
					.filter(option -> option.getName().equals(this.getSelectedItem().toString()))
					.map(OMultipleOption::getId)
					.collect(Collectors.toList())
					.toArray(new Integer[]{});
			
			return ids.length>0?ids[0]:0;
		}
	}
	
	@SuppressWarnings("unchecked")
	private void fillOptions(FormField field) {
		removeAllOptions();
		
		if(options != null)
		{
			for (Iterator<OMultipleOption> iterator = options.iterator(); iterator.hasNext();) {
				OMultipleOption option = (OMultipleOption) iterator.next();
				
				this.addItem((E) option.getName());
				
				if(field.getDefaultSelectedEntityId() == option.getId()) {
					defaultSelectedIndex = this.getItemCount()-1;
					
					resetSelection();
				}
			}
		}
	}
	
	public void resetSelection() {
		if(selectionLocked){
			this.setEnabled(true);
		}
		
		this.setSelectedIndex(defaultSelectedIndex);
		
		if(selectionLocked){
			this.setEnabled(false);
		}
	}
	
	public void rebuild(FormField field) {
		
	}
	
	@SuppressWarnings("unchecked")
	public void removeAllOptions() {
		this.removeAllItems();
		this.addItem((E) "");
	}

	public ArrayList<OMultipleOption> getOptions() {
		return options;
	}

	public void setOptions(ArrayList<OMultipleOption> options) {
		this.options = options;
	}

	public boolean isSelectionLocked() {
		return selectionLocked;
	}

	public void setSelectionLocked(boolean selectionLocked) {
		this.selectionLocked = selectionLocked;
	}
}