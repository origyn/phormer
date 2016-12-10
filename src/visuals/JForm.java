package visuals;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;

import com.thoughtworks.xstream.XStream;

import models.Entity;
import models.FormField;
import models.FormListener;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.GroupLayout.Alignment;

public class JForm extends JPanel {
	private static final long serialVersionUID = -432718193365912996L;
	private int componentsWidth = 0, componentsHeight = 0;
	private FormListener listener;
	HashMap<Integer, FormField> formFields = new HashMap<>();
	HashMap<Integer, String> hmFieldOrder = new HashMap<>();
	HashMap<String, OTextField> hmTextFields = new HashMap<>();
	HashMap<String, OComboBox<String>> hmComboBoxes = new HashMap<>();
	HashMap<String, OTextArea> hmTextAreas = new HashMap<>();
	HashMap<String, JLabel> hmLabels = new HashMap<>();
	HashMap<String, JLabel> hmInfoLabels = new HashMap<>();
	HashMap<String, OButtonGroup> hmRadioGroups = new HashMap<>();
	Entity entity;
	public JButton btSave = new JButton("Save");
	public JButton btCancel = new JButton("Cancel");
	public enum fieldTypes {
		LABEL,
		TEXT_FIELD,
		COMBO_BOX,
		TEXT_AREA,
		RADIO_GROUP
	}
	
	public JForm(String formSpecsXmlPath, String dbSettingsXmlPath, String relation, int componentsWidth, int componentsHeight) {
		this.componentsWidth = componentsWidth;
		this.componentsHeight = componentsHeight;
		this.entity = new Entity(relation, dbSettingsXmlPath);
		
		buildFormFromXML(formSpecsXmlPath);
		createListeners();
	}
	
	private void buildFormFromXML(String formSpecsXmlPath) {
		XStream xstream = new XStream();
		xstream.setMode(XStream.NO_REFERENCES);
		
		@SuppressWarnings("unchecked")
		ArrayList<FormField> formFieldsList = (ArrayList<FormField>) xstream.fromXML(new File(formSpecsXmlPath));
		
		formFieldsList.stream().sorted((a, b)->Integer.compare(a.getOrderNumber(), b.getOrderNumber())).forEach(f -> {
			formFields.put(formFields.size(), f);
		});
		
		createForm();
		putFocusOnFirstField();
	}
	
	public void putFocusOnFirstField() {
		Integer orderKeys[] = hmFieldOrder.keySet().toArray(new Integer[]{});
		Arrays.sort(orderKeys);
		
		String fieldName = hmFieldOrder.get(orderKeys[0]);
		Component component = hmTextFields.get(fieldName);
		
		if(component == null) {
			component = hmTextAreas.get(fieldName);
		}
				
		component.requestFocusInWindow();
	}
	
	public void createListeners() {
		btSave.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(validateForm())
				{
					for (FormField formField : formFields.values()) {
						if(formField.getRelationField() != null) {
							if(hmTextFields.get(formField.getName()) != null) {
								entity.addProperty(formField.getRelationField(), hmTextFields.get(formField.getName()).getText());
							}
							else if(hmComboBoxes.get(formField.getName()) != null) {
								entity.addProperty(formField.getRelationField(), hmComboBoxes.get(formField.getName()).getSelectedEntityId() + "");
							}
							else if(hmTextAreas.get(formField.getName()) != null) {
								entity.addProperty(formField.getRelationField(), hmTextAreas.get(formField.getName()).getText());
							}
							else if(hmRadioGroups.get(formField.getName()) != null) {
								entity.addProperty(formField.getRelationField(), hmRadioGroups.get(formField.getName()).getSelectedEntityId() + "");
							}
						}
					}
					
					if(entity.save()) {
						clearValues();
						
						if(listener != null) listener.onSubmit();
					}
				}
			}
		});
		
		btCancel.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				clearValues();
				if(listener != null) listener.onCancel();
			}
		});
	}

//	Validate the form IssuePanel
	public boolean validateForm()
	{
		long errors = hmTextFields.values().stream().filter(field -> field.validateFormComponent() == true).count();
		errors += hmComboBoxes.values().stream().filter(field -> field.validateFormComponent() == true).count();
		errors += hmTextAreas.values().stream().filter(field -> field.validateFormComponent() == true).count();
		
		return errors>0?false:true;
	}
	
	private void createForm() {
		formFields.keySet().forEach(key-> {
			switch (formFields.get(key).getType()) {
			case LABEL:
				hmInfoLabels.put(formFields.get(key).getName(), new JLabel(formFields.get(key).getDefaultValue()));
				break;
			case TEXT_FIELD:
				hmTextFields.put(formFields.get(key).getName(), new OTextField(formFields.get(key)));
				hmTextFields.get(formFields.get(key).getName()).addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						btSave.doClick();
					}
				});
				break;
			case COMBO_BOX:
				hmComboBoxes.put(formFields.get(key).getName(), new OComboBox<String>(formFields.get(key)));
				break;
			case TEXT_AREA:
				hmTextAreas.put(formFields.get(key).getName(), new OTextArea(formFields.get(key)));
				break;
			case RADIO_GROUP:
				hmRadioGroups.put(formFields.get(key).getName(), new OButtonGroup(formFields.get(key)));
				break;
			default:
				break;
			}
			
			hmLabels.put(formFields.get(key).getName(), new JLabel(formatLabelText(formFields.get(key).getName())));
			hmFieldOrder.put(key, formFields.get(key).getName());
		});
		
		createLayouts();
		setSizes();
	}
	
	public void setSizes() {
		hmTextFields.values().forEach(field-> {
			field.setMinimumSize(new Dimension(componentsWidth, componentsHeight));
			field.setMaximumSize(new Dimension(componentsWidth, componentsHeight));
		});
		
		hmComboBoxes.values().forEach(field-> {
			field.setMinimumSize(new Dimension(componentsWidth, componentsHeight));
			field.setMaximumSize(new Dimension(componentsWidth, componentsHeight));
		});
		
		hmTextAreas.values().forEach(field-> {
			field.setMinimumSize(new Dimension(componentsWidth, componentsHeight * 5));
			field.setMaximumSize(new Dimension(componentsWidth, componentsHeight * 5));
		});
	}
	
	@SuppressWarnings("unchecked")
	public void createLayouts()
	{
		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		
		ParallelGroup labelsHorizontalGroup = layout.createParallelGroup();
		ParallelGroup fieldsHorizontalGroup = layout.createParallelGroup();
		SequentialGroup verticalGroup = layout.createSequentialGroup();
		
		hmFieldOrder.keySet().stream().sorted().forEach(key-> {
			boolean listExpandable = false;
			String fieldName = hmFieldOrder.get(key);
			Component component = hmTextFields.get(fieldName);
			
			if(component == null) {
				component = hmComboBoxes.get(fieldName);
				try {
					Class<?> cl = Class.forName(component.getClass().getName());
					Field fl = cl.getDeclaredField("btNew");
					
					if(fl.get(component) != null) {
						listExpandable = true;
					}
				} catch (ClassNotFoundException | SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchFieldException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if(component == null) {
				component = hmTextAreas.get(fieldName);
			}
			
			if(component == null) {
				component = hmInfoLabels.get(fieldName);
			}
			
			labelsHorizontalGroup.addComponent(hmLabels.get(fieldName));
			
			SequentialGroup radioHorizontalGroup = layout.createSequentialGroup();
			ParallelGroup radioVerticalGroup = layout.createParallelGroup();
			
			if(component == null) {
				radioVerticalGroup.addComponent(hmLabels.get(fieldName));
				
				for(Enumeration<AbstractButton> buttons = hmRadioGroups.get(fieldName).getElements(); buttons.hasMoreElements();) {
					AbstractButton button = buttons.nextElement();
					
					radioHorizontalGroup.addComponent(button);
					radioVerticalGroup.addComponent(button);
				}
			}
			
			if(component == null) {
				fieldsHorizontalGroup.addGroup(radioHorizontalGroup);
				verticalGroup.addGroup(radioVerticalGroup);
			}
			else {
				if(!listExpandable) {
					fieldsHorizontalGroup.addComponent(component);
					verticalGroup.addGroup(layout.createParallelGroup()
						.addComponent(hmLabels.get(fieldName))
						.addComponent(component));
				}
				else {
					fieldsHorizontalGroup.addGroup(layout.createParallelGroup()
						.addComponent(component)
						.addComponent(((OComboBox<String>) component).btNew));
					verticalGroup.addGroup(layout.createParallelGroup()
							.addComponent(hmLabels.get(fieldName))
							.addGroup(layout.createSequentialGroup()
								.addComponent(component)
								.addComponent(((OComboBox<String>) component).btNew)));
				}
			}
		});
		
		verticalGroup.addGap(30);
		verticalGroup.addGroup(layout.createParallelGroup()
			.addComponent(btSave)
			.addComponent(btCancel));
		
		layout.setHorizontalGroup(
			layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.CENTER)
					.addGroup(layout.createSequentialGroup()
						.addGroup(labelsHorizontalGroup)
						.addGroup(fieldsHorizontalGroup))
					.addGroup(layout.createSequentialGroup()
						.addComponent(btSave)
						.addComponent(btCancel))));
		
		layout.setVerticalGroup(verticalGroup);
	}
	
	public String formatLabelText(String text) {
		String formattedString = text.replaceAll(String.format("%s|%s|%s",
																"(?<=[A-Z])(?=[A-Z][a-z])",
																"(?<=[^A-Z])(?=[A-Z])",
																"(?<=[A-Za-z])(?=[^A-Za-z])"
															),
												" ").toLowerCase();
		
		formattedString = formattedString.substring(0, 1).toUpperCase() + formattedString.substring(1);
//		String[] r = s.split("(?=\\p{Lu})");
		
		return formattedString;
	}
	
	public void clearValues()
	{
		hmComboBoxes.values().stream().forEach(cb -> {
			cb.resetSelection();
		});
		
		hmTextFields.values().stream().forEach(tf -> {
			tf.setText("");
		});
		
		hmTextAreas.values().stream().forEach(ta -> {
			ta.setText("");
		});
		
		hmInfoLabels.values().stream().forEach(lb -> {
			lb.setText("");
		});
		
		hmRadioGroups.values().stream().forEach(bg -> {
			for (Enumeration<AbstractButton> buttons = bg.getElements(); buttons.hasMoreElements();) {
				AbstractButton radioButton = buttons.nextElement();
				
				radioButton.setSelected(true);
				break;
			}
		});
	}
	
	public void addComboOption(String comboBoxName, int optionId, String optionText) {
		
		for (FormField ff : formFields.values()) {
			if(ff.getName().equals(comboBoxName)) {
				if(ff.getMultipleOptionsPure() == null) {
					ff.setMultipleOptions(new ArrayList<OMultipleOption>());
				}
				
				ff.getMultipleOptionsPure().add(new OMultipleOption(optionId, optionText));
				hmComboBoxes.get(comboBoxName).rebuildOptions(ff);
				break;
			}
		}
	}
	
	public void addFormListener(FormListener listener) {
		this.listener = listener;
	}
	
	public OComboBox<String> getComboBox(String name) {
		return hmComboBoxes.get(name);
	}
	
	public OTextField getTextField(String name) {
		return hmTextFields.get(name);
	}
	
	public OTextArea getTextArea(String name) {
		return hmTextAreas.get(name);
	}

	public JLabel getInfoLabel(String name) {
		return hmInfoLabels.get(name);
	}
	
	public OButtonGroup getButtonGroup(String name) {
		return hmRadioGroups.get(name);
	}

	public int getComponentsHeight() {
		return componentsHeight;
	}

	public void setComponentsHeight(int componentsHeight) {
		this.componentsHeight = componentsHeight;
	}

	public int getComponentsWidth() {
		return componentsWidth;
	}

	public void setComponentsWidth(int componentsWidth) {
		this.componentsWidth = componentsWidth;
	}
}