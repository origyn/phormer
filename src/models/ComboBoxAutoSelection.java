package models;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.ComboBoxModel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;

import visuals.OComboBox;

public class ComboBoxAutoSelection<E> extends PlainDocument {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7551415780206792450L;
	OComboBox<E> comboBox;
	ComboBoxModel<E> model;
	JTextComponent editor;
	boolean selecting = false;

	public ComboBoxAutoSelection(final OComboBox<E> cb) {
		this.comboBox = cb;
		this.model = cb.getModel();
		this.editor = ((JTextField) this.comboBox.getEditor().getEditorComponent());
		
		this.editor.setDocument(this);
		
		this.comboBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!selecting && model.getSelectedItem() != null) {
					editor.setSelectionStart(0);
					editor.setSelectionEnd(model.getSelectedItem().toString().length());
				}
			}
		});
		
		this.editor.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) { }
			
			@Override
			public void keyReleased(KeyEvent e) { }
			
			@Override
			public void keyPressed(KeyEvent e) {
				if(comboBox.isDisplayable()) {
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
		
		super.insertString(offs, str, a);
		
		Object item = findItem(this.getText(0, this.getLength()));
		
		if(item != null) {
			setSelectedItem(item);
		}
		else {
			item = model.getSelectedItem();
			offs = offs - str.length();
			UIManager.getLookAndFeel().provideErrorFeedback(comboBox);
		}
		
		setText(item.toString());
		highlightText(offs + str.length());
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
	
	public void highlightText(int start) {
		editor.setSelectionStart(start);
		editor.setSelectionEnd(this.getLength());
	}
	
	public Object findItem(String content) {
		Object selectedItem = model.getSelectedItem();
		
		if(selectedItem != null && ignoreCaseMatch(selectedItem.toString(), content)) {
			return selectedItem;
		}
		else {
			for (int i = 0; i < model.getSize(); i++) {
				if(ignoreCaseMatch(model.getElementAt(i).toString(), content)) {
					return model.getElementAt(i);
				}
			}
		}
		
		return null;
	}
	
	public boolean ignoreCaseMatch(String itemText, String content) {
		return itemText.toLowerCase().startsWith(content.toLowerCase());
	}
}
