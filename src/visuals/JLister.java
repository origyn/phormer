package visuals;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import models.Entity;
import models.FormListener;

public class JLister extends JPanel {
	private static final long serialVersionUID = -8710333638794205968L;
	Entity databaseCollection;
	private String panelClass, mainExpanderXML, dbSettingsXmlPath, expanderRelation, selectQuery;
	private HashMap<String, String> fieldMapper, methodMapper;
	private Dimension infoPanelDimension = new Dimension(100, 20);
	private JPanel pnTools = new JPanel(), pnInfoPanelsContainer = new JPanel();
	private JScrollPane scrlPane = new JScrollPane(pnInfoPanelsContainer);
	public JButton bNew = new JButton(new ImageIcon("files/pix/new_icon2.png")), bSearch = new JButton(new ImageIcon("files/pix/search.png"));;
	public JTextField tfSearch = new JTextField();
	
	/**
	 * @param dbSettingsXmlPath		Path to xml file with database connection data
	 * @param selectQuery			MySQL query to retrieve data
	 * @param panelClass			Class to use for information panels
	 * @param methodMapper			Key - database field name; Value - method in "panelClass" to invoke on every info panel creation with db value from field
	 * @param mainExpanderXML		Path to xml file for JForm to popup on button bNew action listener
	 * @param infoPanelDimension	Maximum size for every info panel
	 */
	public JLister(String dbSettingsXmlPath, String selectQuery, String expanderRelation, String panelClass, HashMap<String, String> methodMapper, String mainExpanderXML, Dimension infoPanelDimension) {
		this.selectQuery = selectQuery;
		this.dbSettingsXmlPath = dbSettingsXmlPath;
		this.mainExpanderXML = mainExpanderXML;
		this.expanderRelation = expanderRelation;
		this.databaseCollection = new Entity(dbSettingsXmlPath);
		this.setInfoPanelDimension(infoPanelDimension);
		pnInfoPanelsContainer.setLayout(new BoxLayout(pnInfoPanelsContainer, BoxLayout.Y_AXIS));
		scrlPane.getVerticalScrollBar().setUnitIncrement(16);
		scrlPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrlPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		if(panelClass == null) {
			this.setPanelClass("visuals.JInfoPanel");
		}
		else if(panelClass.length() == 0) {
			this.setPanelClass("visuals.JInfoPanel");
		}
		else {
			this.setPanelClass(panelClass);
		}
		
		this.createLayouts();
		this.createListeners();
		this.populate(selectQuery, fieldMapper, methodMapper);
	}
	
	public void createLayouts() {
		bNew.setPressedIcon(new ImageIcon("files/pix/new_icon.png"));
		bNew.setBorder(null);
		bNew.setOpaque(false);
		bNew.setContentAreaFilled(false);
		bNew.setBorderPainted(false);
		bNew.setCursor(new Cursor(Cursor.HAND_CURSOR));
		bNew.setVerticalTextPosition(SwingConstants.BOTTOM);
		bNew.setHorizontalTextPosition(SwingConstants.CENTER);
		
		bSearch.setPressedIcon(new ImageIcon("files/pix/search2.png"));
		bSearch.setBorder(null);
		bSearch.setOpaque(false);
		bSearch.setContentAreaFilled(false);
		bSearch.setBorderPainted(false);
		bSearch.setCursor(new Cursor(Cursor.HAND_CURSOR));
		bSearch.setVerticalTextPosition(SwingConstants.BOTTOM);
		bSearch.setHorizontalTextPosition(SwingConstants.CENTER);
		
		tfSearch.setMaximumSize(new Dimension(150, 25));
		
		GroupLayout layout = new GroupLayout(pnTools);
		pnTools.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
			.addComponent(bNew)
			.addGap(40)
			.addComponent(tfSearch)
			.addComponent(bSearch));
		
		layout.setVerticalGroup(layout.createParallelGroup(Alignment.CENTER)
				.addComponent(bNew)
				.addComponent(tfSearch)
				.addComponent(bSearch));
		
		GroupLayout layoutMain = new GroupLayout(this);
		this.setLayout(layoutMain);
		layoutMain.setAutoCreateGaps(true);
		layoutMain.setAutoCreateContainerGaps(true);
		
		layoutMain.setHorizontalGroup(layoutMain.createParallelGroup(Alignment.TRAILING)
			.addComponent(pnTools)
			.addComponent(scrlPane));
		
		layoutMain.setVerticalGroup(layoutMain.createSequentialGroup()
				.addComponent(pnTools)
				.addComponent(scrlPane));
	}
	
	public void createListeners() {
		bNew.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFrame fr = new JFrame(expanderRelation);
				JForm expander = new JForm(mainExpanderXML, dbSettingsXmlPath, expanderRelation, 150, 25);
				
				expander.addFormListener(new FormListener() {
					
					@Override
					public void onSubmit() {
						populate(selectQuery);
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
			}
		});
		
		tfSearch.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				bSearch.doClick();
			}
		});
		
		bSearch.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	public void populate(String selectQuery) {
		ResultSet rs = this.databaseCollection.dbUtility.execute(selectQuery);
		
		try {
			ResultSetMetaData rsmd = rs.getMetaData();
			
			pnInfoPanelsContainer.removeAll();
			
			Class<?> c = Class.forName(getPanelClass());

			while(rs.next()) {
				Object p = c.newInstance();
				
				((Component) p).setMaximumSize(this.infoPanelDimension);
				
				if(methodMapper != null) {
					String[] keys = methodMapper.keySet().toArray(new String[]{});
					
					for (String key : keys) {
						int columnIndex = 0;
						
						for(int i = 1; i < rsmd.getColumnCount() + 1; i++) {
							if(rsmd.getColumnName(i).equals(key)) {
								columnIndex = i;
								break;
							}
						}
						
						if(columnIndex > 0) {
							Method m = c.getDeclaredMethod(methodMapper.get(key), Class.forName(rsmd.getColumnClassName(columnIndex)));
							
							m.invoke(p, rs.getObject(key));
						}
					}
				}
				
				addEntityPanel(p);
			}
			
			scrlPane.setViewportView(pnInfoPanelsContainer);
			
			rs.close();
		} catch (SQLException | ClassNotFoundException | InstantiationException | IllegalAccessException | SecurityException | NoSuchMethodException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	public void populate(String selectQuery, HashMap<String, String> fieldMapper, HashMap<String, String> methodMapper) {
		this.fieldMapper = fieldMapper;
		this.methodMapper = methodMapper;
		
		populate(selectQuery);
	}
	
	public void addEntityPanel(Object p) {
		((JPanel) p).setAlignmentX(Component.LEFT_ALIGNMENT);
		pnInfoPanelsContainer.add((Component) p);
	}

	public String getPanelClass() {
		return panelClass;
	}

	public void setPanelClass(String panelClass) {
		this.panelClass = panelClass;
	}

	public Dimension getInfoPanelDimension() {
		return infoPanelDimension;
	}

	public void setInfoPanelDimension(Dimension infoPanelDimension) {
		this.infoPanelDimension = infoPanelDimension;
	}

	public JScrollPane getScrlPane() {
		return scrlPane;
	}
}
