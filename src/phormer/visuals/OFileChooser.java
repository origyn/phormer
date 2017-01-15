package phormer.visuals;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.UIManager;

import phormer.models.FormComponent;
import phormer.models.FormField;

public class OFileChooser extends JFileChooser implements FormComponent {

	private static final long serialVersionUID = -3848882597284792094L;
	private boolean mandatory = false;
	private JTextField tfPath = new JTextField();
	private JButton bChoose = new JButton("Избери");
	
	public OFileChooser(FormField field) {
		super(field.getDefaultFilePath());
		
		setMandatory(field.isMandatory());

		bChoose.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				int result = showOpenDialog(OFileChooser.this);
				
				if(result == OFileChooser.APPROVE_OPTION) {
					tfPath.setText(getSelectedFile().getAbsolutePath());
				}
			}
		});
	}

	@Override
	public boolean validateFormComponent() {
		String errorMessage = "<html>";
		boolean error = false;
		
		if(mandatory && this.tfPath.getText().length() == 0) {
			errorMessage += "You must choose a file.";
			error = true;
		}
		
		errorMessage += "</html>";
		
		if(error) {
			this.tfPath.setBorder(BorderFactory.createLineBorder(Color.RED));
			this.tfPath.setToolTipText(errorMessage);
		}
		else {
			this.tfPath.setBorder(UIManager.getBorder("TextField.border"));
			this.tfPath.setToolTipText(null);
		}
		
		return error;
	}
	
	public boolean isMandatory() {
		return mandatory;
	}

	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}

	public JTextField getTextField() {
		return tfPath;
	}

	public JButton getButton() {
		return bChoose;
	}
}