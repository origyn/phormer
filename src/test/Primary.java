package test;

import javax.swing.JFrame;

import models.FormListener;
import visuals.JForm;

public class Primary {
	public Primary() {
		JForm form = new JForm("files/xml/forms/test.xml", "files/xml/settings/db.xml", "foods.product", 200, 20);
		form.addFormListener(new FormListener() {
			
			@Override
			public void onSubmit() {
				System.out.println("Button \"Save\" was clicked");
			}
			
			@Override
			public void onCancel() {
				System.exit(0);
			}
		});
		
		form.addComboOption("test", 35, "third");
		form.addComboOption("custom options", 23, "hm");
		
		JFrame fr = new JFrame();
		fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		fr.setSize(640, 480);
		fr.setLocationRelativeTo(null);
		fr.setVisible(true);
		fr.add(form);
	}
	
	public static void main(String[] args) {
		new Primary();
	}
}