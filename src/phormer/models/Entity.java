package phormer.models;

import java.io.File;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

import javax.swing.JPanel;

import com.thoughtworks.xstream.XStream;

import phormer.performers.DatabaseUtility;

public class Entity {
	public DatabaseUtility dbUtility;
	public String relation = "";
	public HashMap<String, String> properties = new HashMap<>();
	private JPanel representativePanel = new JPanel();
	
	public Entity() {
		
	}
	
	public Entity(String relation, String dbSettingsXmlPath) {
		this.relation = relation;
		XStream xstream = new XStream();
		xstream.setMode(XStream.NO_REFERENCES);
		
		dbUtility = (DatabaseUtility) xstream.fromXML(new File(dbSettingsXmlPath));
	}
	
	public Entity(String dbSettingsXmlPath) {
		XStream xstream = new XStream();
		xstream.setMode(XStream.NO_REFERENCES);
		
		dbUtility = (DatabaseUtility) xstream.fromXML(new File(dbSettingsXmlPath));
	}
	
	public boolean save() {
		if(properties.get("id") != null) {
			if(Integer.parseInt(properties.get("id")) > 0) {
				return update();
			}
			else {
				return insert();
			}
		}
		else {
			return insert();
		}
	}
	
	public boolean insert() {
		String[] keys = properties.keySet().toArray(new String[]{});

		Arrays.sort(keys);

		String query = "INSERT INTO " + relation + "(" + keys[0];
		String queryValues = "VALUES (?";
		
		for (int i = 1; i < keys.length; i++) {
			query += ", " + keys[i];
			queryValues += ", ?";
		}
		
		query = query + ") " + queryValues + ")";
		
		try {
			ArrayList<HashMap<String, String>> singleBatch = new ArrayList<>();
			
			singleBatch.add(properties);
			
			properties.put("id", dbUtility.applyToDatabase(query, singleBatch) + "");
			
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			
			return false;
		}
	}
	
	public boolean update() {
		String[] keys = properties.keySet().stream().filter(k->!k.equals("id")).collect(Collectors.toList()).toArray(new String[]{});

		Arrays.sort(keys);
		
		String query = "UPDATE " + relation + " SET " + keys[0] + " = ?";
		
		for (int i = 1; i < keys.length; i++) {
			query += ", " + keys[i] + " = ?";
		}
		
		query += " WHERE id = ?";
		
		try {
			dbUtility.applyToDatabase(query, properties, Integer.parseInt(properties.get("id")));
			
			return true;
		} catch (NumberFormatException | SQLException e) {
			e.printStackTrace();
			
			return false;
		}
	}
	
	public boolean delete() {
		try {
			dbUtility.applyToDatabase("DELETE FROM " + relation + " WHERE id = ?", new HashMap<String, String>(), Integer.parseInt(properties.get("id")));
			
			return true;
		} catch (NumberFormatException | SQLException e) {
			e.printStackTrace();
			
			return false;
		}
	}
	
	public boolean populateFromDB() {
		if(this.get("id") == null || this.relation.length() == 0) {
			return false;
		}
		
		try {
			ResultSet rs = dbUtility.execute("SELECT * FROM " + this.relation + " WHERE id = " + this.get("id") + " LIMIT 1;");
			
			rs.next();
			
			ResultSetMetaData rsmd = rs.getMetaData();
			
			for (int i = 0; i < rsmd.getColumnCount(); i++) {
				this.addProperty(rsmd.getColumnName(i+1), rs.getObject(i+1).toString());
			}
			
			rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return true;
	}
	
	public void addProperty(String propertyName, String propertyValue) {
		if(properties.containsKey(propertyName)) {
			properties.remove(propertyName);
		}
		
		properties.put(propertyName, propertyValue);
	}
	
	public void addProperty(String propertyName, Integer propertyValue) {
		this.addProperty(propertyName, propertyValue + "");
	}
	
	public void addProperty(String propertyName, Long propertyValue) {
		this.addProperty(propertyName, propertyValue + "");
	}
	
	public String get(String propertyName) {
		return properties.get(propertyName);
	}

	public JPanel getRepresentativePanel() {
		return representativePanel;
	}

	public void setRepresentativePanel(JPanel representativePanel) {
		this.representativePanel = representativePanel;
	}
	
	public void createDbUtility(String dbSettingsXmlPath) {
		XStream xstream = new XStream();
		xstream.setMode(XStream.NO_REFERENCES);
		
		dbUtility = (DatabaseUtility) xstream.fromXML(new File(dbSettingsXmlPath));
	}
}
