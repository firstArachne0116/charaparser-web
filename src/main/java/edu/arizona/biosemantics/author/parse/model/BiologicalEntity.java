package edu.arizona.biosemantics.author.parse.model;

import java.util.List;

public class BiologicalEntity {

	private String alterName;
	private List<Character> characters;
	private String constraint;
	private String constraintId;
	private String constraintOriginal;
	private String geographicalConstraint;
	private String id;
	private String inBrackets;
	private String name;
	private String nameOriginal;
	private String notes;
	private String ontologyId;
	private String paralellismConstraint;
	private String provenance;
	private String src;
	private String taxonConstraint;
	private String type;

	public BiologicalEntity(String alterName, List<edu.arizona.biosemantics.author.parse.model.Character> characters, 
			String constraint,
			String constraintId, String constraintOriginal, String geographicalConstraint,
			String id, String inBrackets, String name, String nameOriginal,
			String notes, String ontologyId, String parallelismConstraint, String provenance, String src,
			String taxonConstraint, String type) {
		this.alterName = alterName;
		this.characters = characters;
		this.constraint = constraint;
		this.constraintId = constraintId;
		this.constraintOriginal = constraintOriginal;
		this.geographicalConstraint = geographicalConstraint;
		this.id = id;
		this.inBrackets = inBrackets;
		this.name = name;
		this.nameOriginal = nameOriginal;
		this.notes = notes;
		this.ontologyId = ontologyId;
		this.paralellismConstraint = parallelismConstraint;
		this.provenance = provenance;
		this.src = src;
		this.taxonConstraint = taxonConstraint;
		this.type = type;
	}

	public String getAlterName() {
		return alterName;
	}

	public List<Character> getCharacters() {
		return characters;
	}

	public String getConstraint() {
		return constraint;
	}

	public String getConstraintId() {
		return constraintId;
	}

	public String getConstraintOriginal() {
		return constraintOriginal;
	}

	public String getGeographicalConstraint() {
		return geographicalConstraint;
	}

	public String getId() {
		return id;
	}

	public String getInBrackets() {
		return inBrackets;
	}

	public String getName() {
		return name;
	}

	public String getNameOriginal() {
		return nameOriginal;
	}

	public String getNotes() {
		return notes;
	}

	public String getOntologyId() {
		return ontologyId;
	}

	public String getParalellismConstraint() {
		return paralellismConstraint;
	}

	public String getProvenance() {
		return provenance;
	}

	public String getSrc() {
		return src;
	}

	public String getTaxonConstraint() {
		return taxonConstraint;
	}

	public String getType() {
		return type;
	}
	
	

}
