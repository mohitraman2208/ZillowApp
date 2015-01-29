package com.example.zillowapplication;

public class PropertyDetailRow {

	private String propertyName;
	private String propertyValue;
	private String key;
	
	public String getPropertyName() 
	{
		return propertyName;
	}
	
	public PropertyDetailRow(String propertyName, String propertyValue) 
	{
		super();
		this.propertyName = propertyName;
		this.propertyValue = propertyValue;
		this.setKey("");
	}
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}
	public String getPropertyValue() {
		return propertyValue;
	}
	public void setPropertyValue(String propertyValue) {
		this.propertyValue = propertyValue;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
}
