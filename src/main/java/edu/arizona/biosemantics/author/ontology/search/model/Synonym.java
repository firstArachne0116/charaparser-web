package edu.arizona.biosemantics.author.ontology.search.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Synonym {

	private String synonym;
	private String classIRI;
	private String user;
	private String ontology;
	
	@JsonCreator
	public Synonym(@JsonProperty(value="user", required=false) String user, @JsonProperty("term")String term, @JsonProperty("ontology") String ontology, 
			@JsonProperty("classIRI") String classIRI) {
		this.synonym = term;
		this.classIRI = classIRI;
		this.user = user;
		this.ontology = ontology;
	}
	
	public String getUser(){
		return user==null? "" : user;
	}
	
	public String getOntology(){
		return ontology==null? "":ontology;
	}
	public String getTerm() {
		return synonym;
	}

	public String getClassIRI() {
		return classIRI;
	}

}
