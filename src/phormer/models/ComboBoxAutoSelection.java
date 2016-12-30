package phormer.models;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.lang.Character.UnicodeBlock;

import javax.swing.ComboBoxModel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;

import phormer.visuals.OComboBox;

public class ComboBoxAutoSelection<E> extends PlainDocument {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7551415780206792450L;
	OComboBox<E> comboBox;
	ComboBoxModel<E> model;
	JTextComponent editor;
	boolean selecting = false;
	String accumulatedText = "";

	public ComboBoxAutoSelection(final OComboBox<E> cb) {
		this.comboBox = cb;
		this.model = cb.getModel();
		this.editor = ((JTextField) this.comboBox.getEditor().getEditorComponent());
		
		this.editor.setDocument(this);
		
		this.comboBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!selecting && model.getSelectedItem() != null) {
					accumulatedText = "";
					editor.setSelectionStart(0);
					editor.setSelectionEnd(model.getSelectedItem().toString().length());
				}
			}
		});
		
		this.editor.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
				
			}
			
			@Override
			public void keyReleased(KeyEvent e) { }
			
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
					e.consume();
					
					if(accumulatedText.length() > 0) {
						String searchText = accumulatedText.substring(0, accumulatedText.length()-1);
						
						accumulatedText = "";
						
						try {
							insertString(0, searchText, null);
						} catch (BadLocationException e1) {
							e1.printStackTrace();
						}
					}
				}
				
				if(comboBox.isDisplayable() && isPrintableCharacter(e.getKeyChar())) {
					comboBox.setPopupVisible(true);
				}
			}
		});
	}
	
	public void remove(int offs, int len) throws BadLocationException {
		if(selecting) {
			return;
		}
		
		super.remove(offs, len);
	}
	
	public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
		if(selecting) {
			return;
		}
		
		str = accumulatedText.concat(str);
		
		super.insertString(offs, str, a);
		
		Object item = findItem(str);
		
		if(item != null) {
			accumulatedText = str;
			
			setSelectedItem(item);
			
			offs = item.toString().toLowerCase().indexOf(str.toLowerCase());
			
			setText(item.toString());
			highlightText(offs, offs + str.length());
		}
		else {
			item = model.getSelectedItem();
			
			UIManager.getLookAndFeel().provideErrorFeedback(comboBox);
			
			setText(item.toString());
			highlightText(offs, offs + str.length() - 1);
		}
	}
	
	public void setSelectedItem(Object item) {
		selecting = true;
		model.setSelectedItem(item);
		selecting = false;
	}
	
	public void setText(String str) throws BadLocationException {
		super.remove(0, this.getLength());
		super.insertString(0, str, null);
	}
	
	public void highlightText(int start, int end) {
		editor.setSelectionStart(start);
		editor.setSelectionEnd(end);
	}
	
	public Object findItem(String content) {
		for (int i = 0; i < model.getSize(); i++) {
			if(ignoreCaseMatch(model.getElementAt(i).toString(), content)) {
				return model.getElementAt(i);
			}
		}
		
		return null;
	}
	
	public boolean ignoreCaseMatch(String itemText, String content) {
		return itemText.toLowerCase().contains(content.toLowerCase());
	}
	
	public boolean isPrintableCharacter(char c) {
		UnicodeBlock ub = UnicodeBlock.of(c);
		
		return !Character.isISOControl(c) && c != KeyEvent.CHAR_UNDEFINED && ub != null && ub != UnicodeBlock.SPECIALS;
	}
}
