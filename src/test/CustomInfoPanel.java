package test;

import java.awt.FlowLayout;
import java.math.BigDecimal;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

public class CustomInfoPanel extends JPanel {
	private static final long serialVersionUID = 6353061305655008967L;
	public JButton bEnergy = new JButton();

	public CustomInfoPanel() {
		this.setLayout(new FlowLayout());
		
		this.add(bEnergy);
	}
	
	public void showEnergyValue(BigDecimal energy) {
		bEnergy.setText(energy.toString());
		
		this.setBorder(BorderFactory.createEtchedBorder());
	}
}
