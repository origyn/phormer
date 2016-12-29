package models;

public class Relator {
	private int orderNumber = 0;
	private String prefix = "", suffix = "", relationField, joinRelation, joinRelationField;
	private boolean voluntary = false;
	
	public int getOrderNumber() {
		return orderNumber;
	}
	public void setOrderNumber(int orderNumber) {
		this.orderNumber = orderNumber;
	}
	public String getPrefix() {
		return prefix;
	}
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	public String getSuffix() {
		return suffix;
	}
	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}
	public String getRelationField() {
		return relationField;
	}
	public void setRelationField(String relationField) {
		this.relationField = relationField;
	}
	public String getJoinRelation() {
		return joinRelation;
	}
	public void setJoinRelation(String joinRelation) {
		this.joinRelation = joinRelation;
	}
	public String getJoinRelationField() {
		return joinRelationField;
	}
	public void setJoinRelationField(String joinRelationField) {
		this.joinRelationField = joinRelationField;
	}
	public boolean isVoluntary() {
		return voluntary;
	}
	public void setVoluntary(boolean voluntary) {
		this.voluntary = voluntary;
	}
}
