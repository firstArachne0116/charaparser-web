/*package edu.arizona.biosemantics.author.ontology.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.arizona.biosemantics.author.ontology.search.model.OntologyIRI;
import edu.arizona.biosemantics.common.ontology.search.model.OntologyEntry;
import edu.arizona.biosemantics.common.ontology.search.model.OntologyEntry.Type;
import edu.arizona.biosemantics.oto.common.ontologylookup.search.OntologyLookupClient;
import edu.arizona.biosemantics.oto.common.ontologylookup.search.data.CompositeEntity;
import edu.arizona.biosemantics.oto.common.ontologylookup.search.data.Entity;
import edu.arizona.biosemantics.oto.common.ontologylookup.search.data.EntityProposals;
import edu.arizona.biosemantics.oto.common.ontologylookup.search.data.FormalConcept;
import edu.arizona.biosemantics.oto.common.ontologylookup.search.owlaccessor.OWLAccessorImpl;
import edu.arizona.biosemantics.oto.common.ontologylookup.search.search.TermSearcher;

public class FileSearcher {

	private static final Logger LOGGER = LoggerFactory.getLogger(FileSearcher.class);
	
	private OntologyLookupClient ontologyLookupClient;
	private HashSet<String> entityOntologyNames;
	private HashSet<String> qualityOntologyNames;

	public FileSearcher(HashSet<String> entityOntologyNames, HashSet<String> qualityOntologyNames,
			String ontologyDir, String dictDir) {
		LOGGER.info("Init filesearcher: " + entityOntologyNames + "; " + qualityOntologyNames + "; " + ontologyDir + "; " + dictDir);
		this.entityOntologyNames = entityOntologyNames;
		this.qualityOntologyNames = qualityOntologyNames;
		try {
			this.ontologyLookupClient = new OntologyLookupClient(
					entityOntologyNames, 
					qualityOntologyNames, 
					ontologyDir,
					dictDir);
		} catch(Throwable t) {
			LOGGER.error("Could not instantiate OLC", t);
			
		}
	}
	
	public List<OntologyEntry> getEntityEntries(String term, String locator, String rel) {
		List<OntologyEntry> result = new ArrayList<OntologyEntry>();
		
		//Only search structures for now leveraging ontologylookup client
		//This is all construction zone to find out use cases of a Searcher of ontologies we have
		try {
			List<EntityProposals> entityProposals = this.ontologyLookupClient.searchStructure(term, locator, rel, false);
			if(entityProposals != null && !entityProposals.isEmpty()) {
				for(EntityProposals eps: entityProposals){
					for(Entity entity: eps.getProposals()){
						if(entity instanceof CompositeEntity){
							CompositeEntity ce = (CompositeEntity) entity;
							for(Entity indiv: ce.getIndividualEntities()){ //use entity score for all components of composite entity
								result.add(new OntologyEntry(null, indiv.getClassIRI(), Type.ENTITY, entity.getConfidenceScore(),indiv.getLabel(), indiv.getDef(), indiv.getPLabel(), indiv.getMatchType()));
							}
					}
						result.add(new OntologyEntry(null, entity.getClassIRI(), Type.ENTITY, entity.getConfidenceScore(),entity.getLabel(), entity.getDef(), entity.getPLabel(), entity.getMatchType()));
					}
				}
				
			}
		} catch(Throwable t) {
			t.printStackTrace();
		}
		
		Collections.sort(result);
		return result;
	}

	public List<OntologyEntry> getQualityEntries(String term) {
		List<OntologyEntry> result = new ArrayList<OntologyEntry>();
		
		TermSearcher termSearcher = new TermSearcher(ontologyLookupClient, false);
		ArrayList<FormalConcept> concepts = termSearcher.searchTerm(term, Type.QUALITY.toString().toLowerCase(), 1.0f);
		if(concepts != null)
			for(FormalConcept concept : concepts) 
				result.add(new OntologyEntry(null, concept.getClassIRI(), Type.QUALITY, concept.getConfidenceScore(), concept.getLabel(), concept.getDef(), concept.getPLabel(), concept.getMatchType()));
		
		Collections.sort(result);
		return result;
	}

	public OWLOntologyManager getOwlOntologyManager() {
		if(this.entityOntologyNames.size() > 0)
			return this.ontologyLookupClient.ontoutil.OWLentityOntoAPIs.get(0).getManager();
		return this.ontologyLookupClient.ontoutil.OWLqualityOntoAPIs.get(0).getManager();
	}
	
	public void updateSearcher(OntologyIRI oIRI){
		OWLOntologyManager owlOntologyManager = getOwlOntologyManager();
		//get rootOnto, assuming there is not imported ontologies
		OWLOntology owlOntology = owlOntologyManager.getOntology(IRI.create(oIRI.getIri()));
		//System.out.println("updateSearcher "+oIRI.getName() +"####################owlOntology="+owlOntology);
		//System.out.println("OWLentityOntoAPIs size (expected 1) = "+this.ontologyLookupClient.ontoutil.OWLentityOntoAPIs.size());
		OWLAccessorImpl api = this.ontologyLookupClient.ontoutil.OWLentityOntoAPIs.get(0);
		api.constructorHelper(owlOntology, new ArrayList<String>());
		api.retrieveAllConcept();
		api.resetSearchCache();
		//System.out.println("updateSearcher ####################api = "+api);
		//System.out.println("updateSearcher ####################owlOntology axiom count (updated to)="+owlOntology.getAxiomCount());

		api = this.ontologyLookupClient.ontoutil.OWLqualityOntoAPIs.get(0);
		api.constructorHelper(owlOntology, new ArrayList<String>());
		api.retrieveAllConcept();
		api.resetSearchCache();
	}
	
	OntologyLookupClient getOntoLookupClient(){
		return this.ontologyLookupClient;
	}
}*/
