package edu.arizona.biosemantics.author.ontology.search.model;

import java.util.List;

public class OntologySearchResultEntry {

	private double score;
	private String term;
	private String parentTerm;
	private List<Annotation> resultAnnotations;

	public OntologySearchResultEntry(String term, double score, String parentTerm, List<Annotation> resultAnnotations) {
		this.term = term;
		this.score = score;
		this.parentTerm = parentTerm;
		this.resultAnnotations = resultAnnotations;
	}
	
	public double getScore() {
		return score;
	}

	public String getTerm() {
		return term;
	}

	public String getParentTerm() {
		return parentTerm;
	}

	public List<Annotation> getResultAnnotations() {
		return resultAnnotations;
	}
	
	
	
}
