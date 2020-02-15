package edu.arizona.biosemantics.author.ontology.search.model;

import java.util.ArrayList;
import java.util.List;

public class OntologySearchResult {

	private List<OntologySearchResultEntry> entries;
	
	public OntologySearchResult() {
		this.entries = new ArrayList<OntologySearchResultEntry>();
	}

	public List<OntologySearchResultEntry> getEntries() {
		return entries;
	}
	
}
