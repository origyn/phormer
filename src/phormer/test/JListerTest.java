package phormer.test;

import java.awt.Dimension;
import java.util.HashMap;

import javax.swing.JFrame;

import phormer.models.Entity;
import phormer.visuals.JLister;

public class JListerTest {
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public JListerTest() {
		HashMap<String, String> hmFieldMapper = new HashMap<>();
//		hmFieldMapper.put("id", "entityId");
		hmFieldMapper.put("energy", "showEnergyValue");
		
		JLister<Entity> list = null;
		
		try {
			list = new JLister("files/xml/settings/db.xml", "SELECT id, name FROM commodities.product;", "commodities.product", "phormer.test.CustomInfoPanel", Class.forName("phormer.models.Entity"), hmFieldMapper, "files/xml/forms/test.xml", new Dimension(100, 50));
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		list.populate("SELECT id, name, energy FROM foods.product limit 5;");
		JFrame fr = new JFrame();
		
		fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		fr.setSize(640, 480);
		fr.setLocationRelativeTo(null);
		fr.setVisible(true);
		fr.add(list);
	}
	
	public static void main(String[] args) {
		new JListerTest();
	}
}