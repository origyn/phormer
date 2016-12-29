package models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import visuals.JForm;
import visuals.OMultipleOption;

public class FormField {
	private String name = "", defaultValue = "", relationField, sourceRelation, sourceRelationField, sourceRelatorAggregate, expanderXmlPath;
	private JForm.fieldTypes type = null;
	private int minLength = 0, maxLength = 0, defaultSelectedEntityId = 0, orderNumber = 0;
	private ArrayList<OMultipleOption> multipleOptions = new ArrayList<OMultipleOption>();
	private ArrayList<Relator> sourceRelators = new ArrayList<Relator>();
	private boolean mandatory = false, optionsSorted = false, selectionLocked = false, onlyNumbers = false, expandable = false, searchable = false;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public JForm.fieldTypes getType() {
		return type;
	}

	public void setType(JForm.fieldTypes type) {
		this.type = type;
	}

	public int getMinLength() {
		return minLength;
	}

	public void setMinLength(int minLength) {
		this.minLength = minLength;
	}

	public int getMaxLength() {
		return maxLength;
	}

	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}

	public boolean isMandatory() {
		return mandatory;
	}

	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}
	
	public ArrayList<OMultipleOption> getMultipleOptionsPure() {
		return multipleOptions;
	}

	public ArrayList<OMultipleOption> getMultipleOptions() {
		if(!optionsSorted) {
			return multipleOptions;
		}
		else {
			return (ArrayList<OMultipleOption>) multipleOptions.stream()
					.sorted((OMultipleOption o1, OMultipleOption o2) -> {return o1.getName().compareTo(o2.getName());} )
					.collect(Collectors.toList());
		}
	}
	
	public String[] getMultipleOptionsAsArray(boolean optionsSorted) {
		String[] items = multipleOptions.stream().map(OMultipleOption::getName).collect(Collectors.toList()).toArray(new String[]{});
		
		if(optionsSorted) {
			Arrays.sort(items);
		}
		
		return items;
	}

	public void setMultipleOptions(ArrayList<OMultipleOption> multipleOptions) {
		this.multipleOptions = multipleOptions;
	}
	
	public void emptyMultipleOptions() {
		if(this.multipleOptions != null) {
			this.multipleOptions.clear();
		}
	}
	
	public ArrayList<Relator> getSourceRelators() {
		return sourceRelators;
	}

	public void setSourceRelators(ArrayList<Relator> sourceRelators) {
		this.sourceRelators = sourceRelators;
	}

	public int getDefaultSelectedEntityId() {
		return defaultSelectedEntityId;
	}

	public void setDefaultSelectedEntityId(int defaultSelectedOptionIndex) {
		this.defaultSelectedEntityId = defaultSelectedOptionIndex;
	}

	public boolean areOptionsSorted() {
		return optionsSorted;
	}

	public void setOptionsSorted(boolean optionsSorted) {
		this.optionsSorted = optionsSorted;
	}

	public boolean isSelectionLocked() {
		return selectionLocked;
	}

	public void setSelectionLocked(boolean selectionLocked) {
		this.selectionLocked = selectionLocked;
	}

	public int getOrderNumber() {
		return orderNumber;
	}

	public void setOrderNumber(int orderNumber) {
		this.orderNumber = orderNumber;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getRelationField() {
		return relationField;
	}

	public void setRelationField(String relationField) {
		this.relationField = relationField;
	}

	public boolean isOptionsSorted() {
		return optionsSorted;
	}

	public boolean isOnlyNumbers() {
		return onlyNumbers;
	}

	public void setOnlyNumbers(boolean onlyNumbers) {
		this.onlyNumbers = onlyNumbers;
	}

	public boolean isExpandable() {
		return expandable;
	}

	public void setExpandable(boolean expandable) {
		this.expandable = expandable;
	}

	public boolean isSearchable() {
		return searchable;
	}

	public void setSearchable(boolean searchable) {
		this.searchable = searchable;
	}

	public String getSourceRelation() {
		return sourceRelation;
	}

	public void setSourceRelation(String sourceRelation) {
		this.sourceRelation = sourceRelation;
	}

	public String getSourceRelationField() {
		return sourceRelationField;
	}

	public void setSourceRelationField(String sourceRelationField) {
		this.sourceRelationField = sourceRelationField;
	}

	public String getExpanderXmlPath() {
		return expanderXmlPath;
	}

	public void setExpanderXmlPath(String expanderXmlPath) {
		this.expanderXmlPath = expanderXmlPath;
	}

	public String getSourceRelatorAggregate() {
		return sourceRelatorAggregate;
	}

	public void setSourceRelatorAggregate(String sourceRelatorAggregate) {
		this.sourceRelatorAggregate = sourceRelatorAggregate;
	}
}