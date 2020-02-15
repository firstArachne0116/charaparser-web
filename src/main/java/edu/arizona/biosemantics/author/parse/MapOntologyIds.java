package edu.arizona.biosemantics.author.parse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import edu.arizona.biosemantics.author.ontology.search.OntologySearchController;
import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.common.ontology.search.FileSearcher;
import edu.arizona.biosemantics.common.ontology.search.Searcher;
import edu.arizona.biosemantics.common.ontology.search.model.Ontology;
import edu.arizona.biosemantics.common.ontology.search.model.OntologyEntry;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.AbstractTransformer;

/*test description: perigynium beak weak, 4-5 mm; apex awnlike; stamen branching, full.*/
//http://shark.sbs.arizona.edu:8080/parse?description=perigynium beak weak, 4-5 mm long; apex awnlike; stamen branching, full.
@Component
public class MapOntologyIds extends AbstractTransformer {

	//private static Ontology[] ontologies = { Ontology.po, Ontology.pato, Ontology.carex };
	//private static Ontology ontology = Ontology.carex;
	private static String ontoName = "carex";
	private FileSearcher searcher;
	//private static HashSet<String> entity = new HashSet<String>();
	//static{entity.add(ontoName);}
	//private static HashSet<String> quality = new HashSet<String>();
	//static{quality.add(ontoName);}
	//private HashMap<Ontology, Searcher> searchersMap;
	//private FileSearcher searcher;
	static HashMap<String, Hashtable<String, String>> termDefinitionCache = new HashMap<String, Hashtable<String, String>>();
	
	//* run when api starts, this make all /parse requests use the same static ontology
	/*@Autowired
	public MapOntologyIds(@Value("${ontologySearch.ontologyDir}") String ontologyDir,
			@Value("${ontologySearch.wordNetDir}") String wordNetDir) throws OWLOntologyCreationException {
		this.searchersMap = new HashMap<Ontology, Searcher>();
		this.searchersMap.put(ontology, new FileSearcher(entity, quality, ontologyDir, wordNetDir, false));
		//deprecated FileSearcher
		//for(Ontology o : ontologies) 
			//this.searchersMap.put(o, new FileSearcher(o, ontologyDir, wordNetDir, false));
	}*/
	
	/**
	 * run when a /parse request is made to use the updated ontology
	 * @param ontologyDir
	 * @param wordNetDir
	 * @throws OWLOntologyCreationException
	 */
	/*public MapOntologyIds(String ontologyDir, String wordNetDir, HashMap<String, Hashtable<String, String>> termDefinitionCache) throws OWLOntologyCreationException {
		this.termDefinitionCache = termDefinitionCache;
		this.searchersMap = new HashMap<Ontology, Searcher>();
		this.searchersMap.put(ontology, new FileSearcher(entity, quality, ontologyDir, wordNetDir, false));
		//deprecated FileSearcher
		//for(Ontology o : ontologies) 
			//this.searchersMap.put(o, new FileSearcher(o, ontologyDir, wordNetDir, false));
	}*/
	
	/**
	 * share the ontology and search function of OSC
	 * @param OSC
	 * @param termDefinitionCache
	 * @throws OWLOntologyCreationException
	 */
	@Autowired
	public MapOntologyIds(OntologySearchController OSC) throws OWLOntologyCreationException {
		ArrayList<String> ontos = new ArrayList<String>();
		ontos.add("carex");
		OSC.createSharedOntology(ontos); //setup 'carex' ontology for use for all users
		this.searcher = OSC.getSearcher(MapOntologyIds.ontoName);
		MapOntologyIds.termDefinitionCache.put("carex", new Hashtable<String,String>());
	}

	@Override
	public void transform(Document document) {
		mapEntities(document);
		mapCharacters(document);
	}

	private void mapCharacters(Document document) {
		for (Element character : this.characterPath.evaluate(document)) {
			String value = character.getAttributeValue("value");
			String charType = character.getAttributeValue("char_type");
			if(value != null)
				value = value.trim();
			if(charType != null)
				charType = charType.trim();
			
			if(charType == null || !charType.equals("range_value")) {
					String ontologyIds = "";
					//value may contain modifiers separated from value by ; or alternative values separated by "|"
					for(String searchTerm: value.split("\\s*[;|]\\s*")){
						//call OSC to search for the searchTerm
						List<OntologyEntry> entries = getCharacterEntries(searchTerm);
						if(!entries.isEmpty())
							ontologyIds += formulateCacheOntologyMatchingInfo(searchTerm, entries) + " ; ";
					}
					character.setAttribute("ontologyid", ontologyIds.replaceFirst(" ; $", "").trim());
				
			}
		}
	}


	private void mapEntities(Document document) {
		for (Element biologicalEntity : this.biologicalEntityPath.evaluate(document)) {
			
			String name = biologicalEntity.getAttributeValue("name");
			if(name != null)
				name = name.trim();
			String constraint = biologicalEntity.getAttributeValue("constraint");
			if(constraint != null)
				constraint = constraint.trim();
			
			String searchTerm = name;
			if(constraint != null)
				searchTerm = constraint + " " + name;
			
			if(searchTerm != null) {
				List<OntologyEntry> entries = getEntityEntries(searchTerm);
				if(!entries.isEmpty()){
					String ontologyId = formulateCacheOntologyMatchingInfo(searchTerm, entries);
					biologicalEntity.setAttribute("ontologyid", ontologyId);
				}
			}
		}
	}
	
	/**
	 * use " ; " to separate matches
	 * scores for all notrecommended type = -1 * score
	 * scores for broader synonyms score = score +"b". 
	 * @param searchTerm
	 * @param entries
	 * @return
	 */
	private String formulateCacheOntologyMatchingInfo(String searchTerm, List<OntologyEntry> entries) {
		String ontologyId = "";
		for(OntologyEntry entry : entries) {
			if(!ontologyId.isEmpty())
				ontologyId += " ; ";
			
			String score = "";
			
			if(entry.getMatchType()==null){ 
				score = Double.toString(entry.getScore());
			}else if(entry.getMatchType().compareTo("notrecommended")==0){
				score = Double.toString(-1.0*entry.getScore());
			}else if(entry.getMatchType().compareTo("broad")==0){
					score = Double.toString(entry.getScore())+"b";
			}else{
				score = Double.toString(entry.getScore());
			}
			ontologyId += entry.getClassIRI() + "[" + searchTerm + ":" + entry.getParentLabel() + 
					"/" + entry.getLabel() + ":" + score + "]";
			termDefinitionCache.get("carex").put(entry.getClassIRI(), entry.getDefinition());
		}
		return ontologyId;
	}

	private List<OntologyEntry> getEntityEntries(String searchTerm) {
		return searcher.getEntityEntries(searchTerm, "", "");
		
		/*
		List<OntologyEntry> ontologyEntries = new ArrayList<OntologyEntry>();
		Searcher searcher = this.searchersMap.get(Ontology.carex);
		ontologyEntries.addAll(searcher.getEntityEntries(searchTerm, "", ""));
		
		return ontologyEntries;
		*/
	}
	
	private List<OntologyEntry> getCharacterEntries(String searchTerm) {
		return searcher.getQualityEntries(searchTerm);
		//Searcher searcher = this.searchersMap.get(Ontology.pato);
		//Searcher searcher = this.searchersMap.get(Ontology.carex);
		//return searcher.getEntityEntries(searchTerm, "", "");
	}
}
