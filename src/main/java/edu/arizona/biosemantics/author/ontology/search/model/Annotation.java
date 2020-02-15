package edu.arizona.biosemantics.author.ontology.search.model;

public class Annotation {

	public String property;
	public String value;
	
	public Annotation(String property, String value) {
		this.property = property;
		this.value = value;
	}
	
	public String getProperty() {
		return property;
	}
	public String getValue() {
		return value;
	}
	
	
}
