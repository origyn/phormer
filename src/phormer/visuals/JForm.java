package phormer.visuals;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;

import com.thoughtworks.xstream.XStream;

import phormer.models.Entity;
import phormer.models.FormField;
import phormer.models.FormListener;
import phormer.models.Relator;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.GroupLayout.Alignment;

public class JForm extends JPanel {
	private static final long serialVersionUID = -432718193365912996L;
	private int componentsWidth = 0, componentsHeight = 0;
	private FormListener listener;
	private String dbSettingsXmlPath;
	HashMap<Integer, FormField> formFields = new HashMap<>();
	HashMap<Integer, String> hmFieldOrder = new HashMap<>();
	HashMap<String, OTextField> hmTextFields = new HashMap<>();
	HashMap<String, OComboBox<String>> hmComboBoxes = new HashMap<>();
	HashMap<String, OTextArea> hmTextAreas = new HashMap<>();
	HashMap<String, JLabel> hmLabels = new HashMap<>();
	HashMap<String, JLabel> hmInfoLabels = new HashMap<>();
	HashMap<String, OButtonGroup> hmRadioGroups = new HashMap<>();
	HashMap<String, HashMap<String, String>> hmComboBoxSourceRelationMapper = new HashMap<>();
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
		this.dbSettingsXmlPath = dbSettingsXmlPath;
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
		
		if(component != null) {
			component.requestFocusInWindow();
		}
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
				if(formFields.get(key).getSubordinateOf() != null && formFields.get(key).getSubordinateRelationField() != null) {
					formFields.get(key).setSelectionLocked(true);
				}

				populateComboBoxFromDB(key, false);
				hmComboBoxes.put(formFields.get(key).getName(), new OComboBox<String>(formFields.get(key)));
				
				if(formFields.get(key).isExpandable() && formFields.get(key).getExpanderXmlPath() != null) {
					hmComboBoxes.get(formFields.get(key).getName()).btNew.addActionListener(new ActionListener() {
						
						@Override
						public void actionPerformed(ActionEvent e) {
							JFrame fr = new JFrame("New " + formFields.get(key).getName());
							JForm expander = new JForm(formFields.get(key).getExpanderXmlPath(), dbSettingsXmlPath, formFields.get(key).getSourceRelation(), 300, 25);
							
							expander.addFormListener(new FormListener() {
								
								@Override
								public void onSubmit() {
									formFields.get(key).emptyMultipleOptions();
									
									if(formFields.get(key).getSubordinateOf() != null && formFields.get(key).getSubordinateRelationField() != null) {
										if(hmComboBoxes.get(formFields.get(key).getSubordinateOf()).getSelectedEntityId() > 0) {
											populateComboBoxFromDB(key, true);
										}
										else {
											formFields.get(key).setSelectionLocked(true);
										}
									}
									else {
										populateComboBoxFromDB(key, true);
									}
									
									hmComboBoxes.get(formFields.get(key).getName()).rebuildOptions(formFields.get(key));
									((JTextField) hmComboBoxes.get(formFields.get(key).getName()).getEditor().getEditorComponent()).requestFocusInWindow();
									
									fr.dispatchEvent(new WindowEvent(fr, WindowEvent.WINDOW_CLOSING));
								}
								
								@Override
								public void onCancel() {
									fr.dispatchEvent(new WindowEvent(fr, WindowEvent.WINDOW_CLOSING));
								}
							});
							
							fr.setSize(new Dimension(480, 320));
							fr.add(expander);
							fr.setLocationRelativeTo(null);
							fr.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
							fr.setVisible(true);
							fr.pack();
						}
					});
				}
				
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
		
		formFields.keySet().stream()
			.forEach(key -> {
				if(formFields.get(key).getType() == fieldTypes.COMBO_BOX && formFields.get(key).getSubordinateOf() != null && formFields.get(key).getSubordinateRelationField() != null) {
					hmComboBoxes.get(formFields.get(key).getSubordinateOf()).addItemListener(new ItemListener() {
						
						@Override
						public void itemStateChanged(ItemEvent e) {
							if(((OComboBox<?>) e.getSource()).getSelectedItem() != null) {
								int selectedOptionEntityId = ((OComboBox<?>) e.getSource()).getSelectedEntityId();
								
								if(selectedOptionEntityId == 0) {
									formFields.get(key).setSelectionLocked(true);
									hmComboBoxes.get(formFields.get(key).getName()).rebuildOptions(formFields.get(key));
								}
								else {
									formFields.get(key).setSubordinateRelationFieldValue(selectedOptionEntityId);
									formFields.get(key).setSelectionLocked(false);
									populateComboBoxFromDB(key, true);
									hmComboBoxes.get(formFields.get(key).getName()).rebuildOptions(formFields.get(key));
								}
							}
						}
					});
				}
			});
		
		createLayouts();
		setSizes();
	}
	
	public void populateComboBoxFromDB(int key, boolean isClean) {
		if(formFields.get(key).getSourceRelation() != null && formFields.get(key).getSourceRelationField() != null) {
			if(isClean) {
				formFields.get(key).emptyMultipleOptions();
			}
			
			ResultSet rs = entity.dbUtility.execute(buildPopulatorQuery(formFields.get(key)));

			if(formFields.get(key).getMultipleOptions() == null) {
				formFields.get(key).setMultipleOptions(new ArrayList<OMultipleOption>());
			}

			try {
				while(rs.next()) {
					formFields.get(key).getMultipleOptions().add(
							new OMultipleOption(rs.getInt("id"), rs.getObject(formFields.get(key).getSourceRelationField()).toString()));
				}
				
				rs.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			
			if(formFields.get(key).getMultipleOptions().size() == 0) {
				formFields.get(key).setSelectionLocked(true);
			}
		}
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
	
	public String buildPopulatorQuery(FormField field) {
		String query = "SELECT ".concat(field.getSourceRelation()).concat(".id, ");
		
		if(field.getSourceRelatorAggregate() == null) {
			query = query.concat(field.getSourceRelationField()).concat(" FROM ").concat(field.getSourceRelation());;
		}
		else {
			query = query.concat(field.getSourceRelatorAggregate()).concat("(");
			
			HashMap<Integer, Relator> hm = new HashMap<>();
			
			for (Relator rel : field.getSourceRelators()) {
				hm.put(rel.getOrderNumber(), rel);
			}
			
			Integer[] keys = hm.keySet().toArray(new Integer[]{});
			
			Arrays.sort(keys);
			
			for(int i = 0; i < keys.length; i++) {
				if(!hm.get(keys[i]).isVoluntary()) {
					if(hm.get(keys[i]).getPrefix() != null || hm.get(keys[i]).getSuffix() != null) {
						query = query.concat("CONCAT(");
					}
					
					if(hm.get(keys[i]).getPrefix() != null) {
						query = query.concat("'").concat(hm.get(keys[i]).getPrefix()).concat("', ");
					}
					
					query = query.concat(hm.get(keys[i]).getJoinRelation() == null?field.getSourceRelation():hm.get(keys[i]).getJoinRelation()).concat(".")
							.concat(hm.get(keys[i]).getJoinRelationField() == null?hm.get(keys[i]).getRelationField():hm.get(keys[i]).getJoinRelationField());
					
					if(hm.get(keys[i]).getSuffix() != null) {
						query = query.concat(", '").concat(hm.get(keys[i]).getSuffix()).concat("'");
					}
					
					if(hm.get(keys[i]).getPrefix() != null || hm.get(keys[i]).getSuffix() != null) {
						query = query.concat(")");
					}
					
					query = query.concat(", ");
				}
				else if(hm.get(keys[i]).getJoinRelation() != null && hm.get(keys[i]).getJoinRelationField() != null) {
					query = query.concat("IF(").concat(hm.get(keys[i]).getJoinRelation().concat(".").concat(hm.get(keys[i]).getJoinRelationField())).concat(" IS NULL, '', ");
					
					if(hm.get(keys[i]).getPrefix() != null || hm.get(keys[i]).getSuffix() != null) {
						query = query.concat("CONCAT(");
					}
					
					if(hm.get(keys[i]).getPrefix() != null) {
						query = query.concat("'").concat(hm.get(keys[i]).getPrefix()).concat("', ");
					}
					
					query = query.concat(hm.get(keys[i]).getJoinRelation()).concat(".").concat(hm.get(keys[i]).getJoinRelationField());
					
					if(hm.get(keys[i]).getSuffix() != null) {
						query = query.concat(", '").concat(hm.get(keys[i]).getSuffix()).concat("'");
					}
					
					if(hm.get(keys[i]).getPrefix() != null || hm.get(keys[i]).getSuffix() != null) {
						query = query.concat(")");
					}
					
					query = query.concat("), ");
				}
				else {
					query = query.concat("IF(").concat(field.getSourceRelation().concat(".").concat(hm.get(keys[i]).getRelationField())).concat(" IS NULL, '', ");
					
					if(hm.get(keys[i]).getPrefix() != null || hm.get(keys[i]).getSuffix() != null) {
						query = query.concat("CONCAT(");
					}
					
					if(hm.get(keys[i]).getPrefix() != null) {
						query = query.concat("'").concat(hm.get(keys[i]).getPrefix()).concat("', ");
					}
					
					query = query.concat(field.getSourceRelation()).concat(".").concat(hm.get(keys[i]).getRelationField());
					
					if(hm.get(keys[i]).getSuffix() != null) {
						query = query.concat(", '").concat(hm.get(keys[i]).getSuffix()).concat("'");
					}
					
					if(hm.get(keys[i]).getPrefix() != null || hm.get(keys[i]).getSuffix() != null) {
						query = query.concat(")");
					}
					
					query = query.concat("), ");
				}
			}
			
			query = query.substring(0, query.length()-2).concat(") AS ").concat(field.getSourceRelationField()).concat(" FROM ").concat(field.getSourceRelation());
			
			for (Integer key : keys) {
				if(hm.get(key).getJoinRelation() != null && hm.get(key).getJoinRelationField() != null) {
					query= query.concat(" JOIN ").concat(hm.get(key).getJoinRelation()).concat(" ON ")
							.concat(hm.get(key).getJoinRelation()).concat(".id = ").concat(field.getSourceRelation()).concat(".").concat(hm.get(key).getRelationField());
				}
			}
		}
		
		if(field.getSubordinateRelationField() != null && field.getSubordinateRelationFieldValue() != null) {
			query = query.concat(" WHERE ").concat(field.getSourceRelation()).concat(".").concat(field.getSubordinateRelationField()).concat(" = ")
					.concat(field.getSubordinateRelationFieldValue());
		}
		
//		System.out.println(query);
		return query;
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