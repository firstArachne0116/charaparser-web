package edu.arizona.biosemantics.author.parse.model;

public class Relation {

	private String alterName;
	private String from;
	private String geographicalConstraint;
	private String id;
	private String inBrackets;
	private String modifier;
	private String name;
	private String negation;
	private String notes;
	private String ontologyId;
	private String organConstraint;
	private Object parallelismConstraint;
	private String provenance;
	private String src;
	private String taxonConstraint;
	private String to;

	public Relation(String alterName, String from, String geographicalConstraint, String id, String inBrackets,
			String modifier, String name, String negation, String notes, String ontologyId, String organConstraint,
			String parallelismConstraint, String provenance, String src, String taxonConstraint, String to) {
		this.alterName = alterName;
		this.from = from;
		this.geographicalConstraint = geographicalConstraint;
		this.id = id;
		this.inBrackets = inBrackets;
		this.modifier = modifier;
		this.name= name;
		this.negation = negation;
		this.notes = notes;
		this.ontologyId = ontologyId;
		this.organConstraint = organConstraint;
		this.parallelismConstraint = parallelismConstraint;
		this.provenance = provenance;
		this.src = src;
		this.taxonConstraint = taxonConstraint;
		this.to = to;
	}

	public String getAlterName() {
		return alterName;
	}

	public String getFrom() {
		return from;
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

	public String getModifier() {
		return modifier;
	}

	public String getName() {
		return name;
	}

	public String getNegation() {
		return negation;
	}

	public String getNotes() {
		return notes;
	}

	public String getOntologyId() {
		return ontologyId;
	}

	public String getOrganConstraint() {
		return organConstraint;
	}

	public Object getParallelismConstraint() {
		return parallelismConstraint;
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

	public String getTo() {
		return to;
	}

	
}
