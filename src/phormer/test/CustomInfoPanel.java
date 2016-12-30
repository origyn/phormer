package phormer.test;

import java.awt.Cursor;
import java.awt.Font;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import phormer.models.JInfoPanel;

public class CustomInfoPanel extends JInfoPanel {
	private static final long serialVersionUID = 7732634722274903705L;
	HashMap<String, JButton> buttons = new HashMap<>();

	public CustomInfoPanel() {
		super();
		
		this.setBorder(BorderFactory.createTitledBorder(""));
		((TitledBorder) this.getBorder()).setTitleFont(new Font("Cambria", Font.BOLD, 14));
		((TitledBorder) this.getBorder()).setTitleJustification(TitledBorder.LEADING);
		((TitledBorder) this.getBorder()).setTitlePosition(TitledBorder.ABOVE_TOP);
		this.setCursor(new Cursor(Cursor.HAND_CURSOR));
		
		createButtons();
		createLayouts();
	}
	
	public void createButtons() {
		buttons.put("bEdit", new JButton(new ImageIcon("files/pix/edit_icon.png")));
		buttons.get("bEdit").setToolTipText("Редактиране");
		buttons.put("bView", new JButton(new ImageIcon("files/pix/details_expand.png")));
		buttons.get("bView").setToolTipText("Преглед");
		
		for (JButton b : buttons.values()) {
			b.setBorder(null);
			b.setOpaque(false);
			b.setContentAreaFilled(false);
			b.setBorderPainted(false);
			b.setCursor(new Cursor(Cursor.HAND_CURSOR));
			b.setHorizontalTextPosition(SwingConstants.CENTER);
			b.setVerticalTextPosition(SwingConstants.BOTTOM);
		}
	}
	
	public void createLayouts() {
		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setAutoCreateGaps(true);
//		layout.setAutoCreateContainerGaps(true);
		
		layout.setHorizontalGroup(
			layout.createSequentialGroup()
				.addComponent(buttons.get("bEdit"))
				.addComponent(buttons.get("bView")));
			
		layout.setVerticalGroup(
			layout.createParallelGroup()
				.addComponent(buttons.get("bEdit"))
				.addComponent(buttons.get("bView")));
	}

	public void setProductName(String name) {
		((TitledBorder) this.getBorder()).setTitle(name);
	}
}
