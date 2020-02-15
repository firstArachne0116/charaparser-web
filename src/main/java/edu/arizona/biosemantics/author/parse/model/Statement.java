package edu.arizona.biosemantics.author.parse.model;

import java.util.List;

public class Statement {

	private String id;
	private String notes;
	private String provenance;
	private String text;
	private List<BiologicalEntity> biologicalEntities;
	private List<Relation> relations;

	public Statement(String id, String notes, String provenance, String text,
			List<BiologicalEntity> biologicalEntities, List<Relation> relations) {
		this.id = id;
		this.notes = notes;
		this.provenance = provenance;
		this.text = text;
		this.biologicalEntities = biologicalEntities;
		this.relations = relations;
	}

	public String getId() {
		return id;
	}

	public String getNotes() {
		return notes;
	}

	public String getProvenance() {
		return provenance;
	}

	public String getText() {
		return text;
	}

	public List<BiologicalEntity> getBiologicalEntities() {
		return biologicalEntities;
	}

	public List<Relation> getRelations() {
		return relations;
	}

}
