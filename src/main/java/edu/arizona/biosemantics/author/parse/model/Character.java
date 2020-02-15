package edu.arizona.biosemantics.author.parse.model;

public class Character {

	private String charType;
	private String constraint;
	private String constraintId;
	private String establishedMeans;
	private String from;
	private String fromInclusive;
	private String fromModifier;
	private String geographicalConstraint;
	private String inBrackets;
	private String isModifier;
	private String modifier;
	private String name;
	private String notes;
	private Object ontologyId;
	private String organConstraint;
	private String otherConstraint;
	private String parallelismConstraint;
	private String provenance;
	private String taxonConstraint;
	private String to;
	private String toInclusive;
	private String toModifier;
	private String toUnit;
	private String type;
	private String src;
	private String unit;
	private String upperRestricted;
	private String value;

	public Character(String charType, String constraint, String constraintId, String establishedMeans, String from,
			String fromInclusive, String fromModifier, String fromUnit, String geographicalConstraint,
			String inBrackets, String isModifier, String modifier, String name, String notes, String ontologyId,
			String organConstraint, String otherConstraint, String parallelismConstraint, String provenance, String src,
			String taxonConstraint, String to, String toInclusive, String toModifier, String toUnit, String type,
			String unit, String upperRestricted, String value) {
		this.charType = charType;
		this.constraint = constraint;
		this.constraintId = constraintId;
		this.establishedMeans = establishedMeans;
		this.from = from;
		this.fromInclusive = fromInclusive;
		this.fromModifier = fromModifier;
		this.geographicalConstraint = geographicalConstraint;
		this.inBrackets = inBrackets;
		this.isModifier = isModifier;
		this.modifier = modifier;
		this.name = name;
		this.notes = notes;
		this.ontologyId = ontologyId;
		this.organConstraint = organConstraint;
		this.otherConstraint = otherConstraint;
		this.parallelismConstraint = parallelismConstraint;
		this.provenance = provenance;
		this.taxonConstraint = taxonConstraint;
		this.to = to;
		this.toInclusive= toInclusive;
		this.toModifier = toModifier;
		this.toUnit = toUnit;
		this.type = type;
		this.src = src;
		this.unit = unit;
		this.upperRestricted = upperRestricted;
		this.value = value;
	}

	public String getCharType() {
		return charType;
	}

	public String getConstraint() {
		return constraint;
	}

	public String getConstraintId() {
		return constraintId;
	}

	public String getEstablishedMeans() {
		return establishedMeans;
	}

	public String getFrom() {
		return from;
	}

	public String getFromInclusive() {
		return fromInclusive;
	}

	public String getFromModifier() {
		return fromModifier;
	}

	public String getGeographicalConstraint() {
		return geographicalConstraint;
	}

	public String getInBrackets() {
		return inBrackets;
	}

	public String getIsModifier() {
		return isModifier;
	}

	public String getModifier() {
		return modifier;
	}

	public String getName() {
		return name;
	}

	public String getNotes() {
		return notes;
	}

	public Object getOntologyId() {
		return ontologyId;
	}

	public String getOrganConstraint() {
		return organConstraint;
	}

	public String getOtherConstraint() {
		return otherConstraint;
	}

	public String getParallelismConstraint() {
		return parallelismConstraint;
	}

	public String getProvenance() {
		return provenance;
	}

	public String getTaxonConstraint() {
		return taxonConstraint;
	}

	public String getTo() {
		return to;
	}

	public String getToInclusive() {
		return toInclusive;
	}

	public String getToModifier() {
		return toModifier;
	}

	public String getToUnit() {
		return toUnit;
	}

	public String getType() {
		return type;
	}

	public String getSrc() {
		return src;
	}

	public String getUnit() {
		return unit;
	}

	public String getUpperRestricted() {
		return upperRestricted;
	}

	public String getValue() {
		return value;
	}
	
	

}
