package phormer.models;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

public class EntityCollection<E extends Entity> extends ArrayList<E> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8391695068942262741L;
	
	public EntityCollection() {
		super();
	}
	
	public void saveCollection() {
		Iterator<E> infoIterator = this.iterator();
		E helpEntity = infoIterator.next();
		
		String[] keys = helpEntity.properties.keySet().toArray(new String[]{});

		Arrays.sort(keys);
		
		String query = "INSERT INTO " + helpEntity.relation + "(" + keys[0];
		String queryValues = "VALUES (?";
		
		for (int i = 1; i < keys.length; i++) {
			query += ", " + keys[i];
			queryValues += ", ?";
		}
		
		query = query + ") " + queryValues + ")";
		
		ArrayList<HashMap<String, String>> dataBatch = new ArrayList<>();
		
		for (Iterator<E> iterator = this.iterator(); iterator.hasNext();) {
			E entity = (E) iterator.next();
			
			dataBatch.add(entity.properties);
		}
		
		try {
			helpEntity.dbUtility.applyToDatabase(query, dataBatch);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
