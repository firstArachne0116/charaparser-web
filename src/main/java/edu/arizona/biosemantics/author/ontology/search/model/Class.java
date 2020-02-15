package edu.arizona.biosemantics.author.ontology.search.model;

import org.semanticweb.owlapi.model.IRI;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Class {
	private String user;
	private String ontology;
	private String term;
	private String superclassIRI;
	private String definition;
	private String elucidation;
	private String createdBy;
	private String exampleOfUsage;
	private String creationDate;
	private String definitionSrc;
	private String logicDefinition;
	
	@JsonCreator
	public Class(@JsonProperty(value="user", required=false) String user, 
			@JsonProperty("ontology") String ontology, 
			@JsonProperty("term") String term, 
			@JsonProperty("superclassIRI")String superclassIRI, 
			@JsonProperty("definition")String definition,
			@JsonProperty("elucidation") String elucidation, 
			@JsonProperty("createdBy") String creator,
			@JsonProperty("examples") String exampleOfUsage, 
			@JsonProperty("creationDate") String creationDate, 
			@JsonProperty("definitionSrc") String definitionSrc, 
			@JsonProperty("logicDefinition") String logicDefinition) {
		super();
		this.user = user;
		this.ontology = ontology;
		this.term = term;
		this.superclassIRI = superclassIRI;
		this.definition = definition;
		this.elucidation = elucidation;
		this.createdBy = creator;
		this.exampleOfUsage = exampleOfUsage;
		this.creationDate = creationDate;
		this.definitionSrc = definitionSrc;
		this.logicDefinition = logicDefinition; // leaf blade = blade and part_of some leaf
	}

	public String getUser() {
		return user==null? "" : user;
	}

	public String getOntology() {
		return ontology == null? "":ontology;
	}

	public String getTerm() {
		return term;
	}

	public String getCreatedBy() {
		return createdBy;
	}


	public String getExampleOfUsage() {
		return exampleOfUsage;
	}


	public String getSuperclassIRI() {
		return superclassIRI;
	}

	public String getDefinition() {
		return definition;
	}

	public String getElucidation() {
		return elucidation;
	}

	public String getCreationDate() {
		return creationDate;
	}


	public String getDefinitionSrc() {
		return definitionSrc;
	}


	public String getLogicDefinition() {
		return logicDefinition;
	}


}
