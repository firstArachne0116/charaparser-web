package edu.arizona.biosemantics.author.ontology.search.model;

//import edu.arizona.biosemantics.common.ontology.search.model.Ontology;

public class OntologyIRI {

	/*private Ontology ontology;
	private String iri;

	public OntologyIRI(Ontology ontology, String iri) {
		this.ontology = ontology;
		this.iri = iri;
	}

	public Ontology getOntology() {
		return ontology;
	}

	public String getIri() {
		return iri;
	}
	*/
	
	private String ontologyFP; //file path
	private String iri;
	private String name; //PO, PATO, CAREX, EXP_1, EXP_2 (1 and 2 are users) EXP_1 and EXP_2 share the same iri

	public OntologyIRI(String ontologyFP, String iri, String name) {
		this.ontologyFP = ontologyFP;
		this.iri = iri;
		this.name = name;
	}

	public String getOntologyFP() {
		return ontologyFP;
	}

	public String getIri() {
		return iri;
	}
	
	public String getName(){
		return name;
	}
}
