package phormer.performers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

public class DatabaseUtility {
	private Connection conn = null;
	private boolean connectivity = true;
	private String host = "", database = "", encoding = "", user = "", password;
	
	public DatabaseUtility() {
		testConnectivity();
	}
	
	public boolean connect() {
		try {
			conn = DriverManager.getConnection("jdbc:mysql://" + host + "/" + database + "?characterEncoding=" + encoding + "&user=" + user + (password != null ? "&password=" + password : ""));
			
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public ResultSet execute(String query) {
		try {
			if(connect()) {
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(query);
				
				return rs;
			}
			else {
				return null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Object execute(String query, String fieldToReturn) {
		try {
			if(connect()) {
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(query);
				Object returnValue = null;
				
				if(rs.next()) {
					returnValue = rs.getInt(fieldToReturn);
				}
				
				disconnect();
				
				return returnValue;
			}
			else {
				return null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public int applyToDatabase(String query, HashMap<String, String> arguments, int id) throws SQLException {
		int dbRowId = 0;
		
		if(connect()) {
			PreparedStatement prep = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			
			String[] keys = arguments.keySet().stream().filter(k->!k.equals("id")).collect(Collectors.toList()).toArray(new String[]{});
			Arrays.sort(keys);
			
			for(int i = 0; i < keys.length; i++) {
				try {
					int argValue = Integer.parseInt(arguments.get(keys[i]));

					prep.setInt(i+1, argValue);
				}
				catch (NumberFormatException ex) {
					prep.setString(i+1, arguments.get(keys[i]));
				}
			}
			
			prep.setInt(keys.length+1, id);
			
			prep.executeUpdate();
			
			ResultSet rs = prep.getGeneratedKeys();
			
			if(rs.next()) {
				dbRowId = rs.getInt(1);
			}
			
			disconnect();
		}
		
		return dbRowId;
	}
	
	public int applyToDatabase(String query, ArrayList<HashMap<String, String>> batches) throws SQLException {
		int dbRowId = 0;
		
		if(connect()) {
			PreparedStatement prep = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			
			for (HashMap<String, String> arguments : batches) {
				String[] keys = arguments.keySet().toArray(new String[]{});
				Arrays.sort(keys);
				
				for(int i = 0; i < keys.length; i++) {
					try {
						int argValue = Integer.parseInt(arguments.get(keys[i]));
	
						prep.setInt(i+1, argValue);
					}
					catch (NumberFormatException ex) {
						if(arguments.get(keys[i]).length() > 0) {
							prep.setString(i+1, arguments.get(keys[i]));
						}
						else {
							prep.setNull(i+1, Types.INTEGER);
						}
					}
					
				}
				
				prep.addBatch();
			}
			
			prep.executeBatch();
			
			ResultSet rs = prep.getGeneratedKeys();
			
			if(rs.next()) {
				dbRowId = rs.getInt(1);
			}
			
			System.out.println("Row id: " + dbRowId);
			
			disconnect();
		}
		
		return dbRowId;
	}
	
	public boolean hasResult(String query) {
		try {
			connect();
			
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			
			boolean result = rs.next();
			
			disconnect();
			
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public void disconnect() {
		if(conn != null) {
            try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean hasConnectivity() {
		return connectivity;
	}

	public void setConnectivity(boolean connectivity) {
		this.connectivity = connectivity;
	}
	
	public void testConnectivity() {
		if(connect()) {
			connectivity = true;
		}
		else {
			connectivity = false;
		}
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}