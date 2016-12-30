package phormer.models;

import java.io.File;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

import com.thoughtworks.xstream.XStream;

import phormer.performers.DatabaseUtility;

public class Entity {
	public DatabaseUtility dbUtility;
	public String relation = "";
	public HashMap<String, String> properties = new HashMap<>();
	
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
			properties.put("id", dbUtility.applyToDatabase(query, properties) + "");
			
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
	
	public void addProperty(String propertyName, String propertyValue) {
		properties.put(propertyName, propertyValue);
	}
	
	public String get(String propertyName) {
		return properties.get(propertyName);
	}
}
