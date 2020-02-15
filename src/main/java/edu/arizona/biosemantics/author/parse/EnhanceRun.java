package edu.arizona.biosemantics.author.parse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.jdom2.Document;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.springframework.stereotype.Component;

import au.com.bytecode.opencsv.CSVReader;
import edu.arizona.biosemantics.author.ontology.search.OntologySearchController;
import edu.arizona.biosemantics.common.biology.TaxonGroup;
import edu.arizona.biosemantics.common.ling.know.IGlossary;
import edu.arizona.biosemantics.common.ling.know.SingularPluralProvider;
import edu.arizona.biosemantics.common.ling.know.Term;
import edu.arizona.biosemantics.common.ling.know.lib.GlossaryBasedCharacterKnowledgeBase;
import edu.arizona.biosemantics.common.ling.know.lib.InMemoryGlossary;
import edu.arizona.biosemantics.common.ling.know.lib.WordNetPOSKnowledgeBase;
import edu.arizona.biosemantics.common.ling.transform.IInflector;
import edu.arizona.biosemantics.common.ling.transform.ITokenizer;
import edu.arizona.biosemantics.common.ling.transform.lib.SomeInflector;
import edu.arizona.biosemantics.common.ling.transform.lib.WhitespaceTokenizer;
import edu.arizona.biosemantics.oto.client.oto.OTOClient;
import edu.arizona.biosemantics.oto.model.GlossaryDownload;
import edu.arizona.biosemantics.oto.model.TermCategory;
import edu.arizona.biosemantics.oto.model.TermSynonym;
import edu.arizona.biosemantics.oto.model.lite.Decision;
import edu.arizona.biosemantics.oto.model.lite.Download;
import edu.arizona.biosemantics.oto.model.lite.Synonym;
import edu.arizona.biosemantics.semanticmarkup.enhance.config.Configuration;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsPartOf;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsSynonyms;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.lib.CSVKnowsSynonyms;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.lib.JustKnowsPartOf;
import edu.arizona.biosemantics.semanticmarkup.enhance.run.Run;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.AbstractTransformer;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.CollapseBiologicalEntities;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.CollapseBiologicalEntityToName;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.CollapseCharacterToValue;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.CollapseCharacters;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.CreateOrPopulateWholeOrganism;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.MoveNegationOrAdverbBiologicalEntityConstraint;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.OrderBiologicalEntityConstraint;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.RemoveDuplicateValues;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.RemoveNonSpecificBiologicalEntitiesByBackwardConnectors;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.RemoveNonSpecificBiologicalEntitiesByForwardConnectors;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.RemoveNonSpecificBiologicalEntitiesByPassedParents;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.RemoveNonSpecificBiologicalEntitiesByRelations;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.RemoveOrphanRelations;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.RemoveUselessCharacterConstraint;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.RemoveUselessWholeOrganism;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.RenameCharacter;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.ReplaceTaxonNameByWholeOrganism;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.SimpleRemoveSynonyms;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.SortBiologicalEntityNameWithDistanceCharacter;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.SplitCompoundBiologicalEntitiesCharacters;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.SplitCompoundBiologicalEntity;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.StandardizeCount;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.StandardizeQuantityPresence;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.old.MoveCharacterToStructureConstraint;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.old.StandardizeStructureNameBySyntax;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.old.StandardizeTerminology;

@Component
public class EnhanceRun {

	private String negWords = "no|not|never";
	private String advModifiers = "at least|at first|at times";
	private String stopWords = "a|about|above|across|after|along|also|although|amp|an|and|are|as|at|be|because|become|becomes|becoming|been|before|being|"
			+ "beneath|between|beyond|but|by|ca|can|could|did|do|does|doing|done|for|from|had|has|have|hence|here|how|however|if|in|into|inside|inward|is|it|its|"
			+ "may|might|more|most|near|of|off|on|onto|or|out|outside|outward|over|should|so|than|that|the|then|there|these|this|those|throughout|"
			+ "to|toward|towards|up|upward|was|were|what|when|where|which|why|with|within|without|would";
	private String units = "(?:(?:pm|cm|mm|dm|ft|m|meters|meter|micro_m|micro-m|microns|micron|unes|µm|μm|um|centimeters|centimeter|millimeters|millimeter|transdiameters|transdiameter)[23]?)"; //squared or cubed
	private TaxonGroup taxonGroup;
	private IGlossary glossary = new InMemoryGlossary();
	private HashMap<String, String> renames;
	private SingularPluralProvider singularPluralProvider = new SingularPluralProvider();
	private ITokenizer tokenizer = new WhitespaceTokenizer();
	private Set<String> lifeStyles;
	private GlossaryBasedCharacterKnowledgeBase characterKnowledgeBase;
	private Set<String> possessionTerms = getWordSet("with|has|have|having|possess|possessing|consist_of");
	private Set<String> durations;
	//private List<String> filePath2KnowsPartOf; //.owl or .csv
	//private String termReviewTermCategorization;
	//private String termReviewSynonyms;
	private WordNetPOSKnowledgeBase wordNetPOSKnowledgeBase;
	private SomeInflector inflector;
	private MapOntologyIds mapOntologyIds;
	
	//public EnhanceRun(MapOntologyIds mapOntologyIds) throws IOException, InterruptedException, ExecutionException {
	public EnhanceRun(MapOntologyIds mapOntologyIds) throws IOException, ClassNotFoundException, InterruptedException, ExecutionException,  OWLOntologyCreationException {
		//this.mapOntologyIds = new MapOntologyIds(Configuration.ontologyDirectory, Configuration.wordNetDirectory, termDefinitionCache);
		//this.mapOntologyIds = new MapOntologyIds(OSC);
		this.mapOntologyIds = mapOntologyIds;
		/*this.filePath2KnowsPartOf = filePath2KnowsPartOf;
		this.termReviewTermCategorization = termReviewTermCategorization;
		this.termReviewSynonyms = termReviewSynonyms;*/
		this.wordNetPOSKnowledgeBase = new WordNetPOSKnowledgeBase(Configuration.wordNetDirectory, false);
		this.inflector = new SomeInflector(wordNetPOSKnowledgeBase, singularPluralProvider.getSingulars(), singularPluralProvider.getPlurals());
		this.taxonGroup = TaxonGroup.PLANT;
		
		initGlossary(glossary, inflector, taxonGroup);//, termReviewTermCategorization, termReviewSynonyms);
		
		renames = new HashMap<String, String>();
		renames.put("count", "quantity");
		renames.put("atypical_count", "atypical_quantity");
		renames.put("color", "coloration");
		
		lifeStyles = glossary.getWordsInCategory("life_style");
		lifeStyles.addAll(glossary.getWordsInCategory("growth_form"));
		durations = glossary.getWordsInCategory("duration");
		

		
		
		characterKnowledgeBase = new GlossaryBasedCharacterKnowledgeBase(glossary, negWords, advModifiers, stopWords, units, inflector);
	}
	
	public void run(Document document) {
		ArrayList<String> ontologies = new ArrayList<String>();
		ArrayList<String> csvs = new ArrayList<String>();
		List<AbstractTransformer> transformers = new LinkedList<AbstractTransformer>();
		try{
			//when knowledge entities can be constructed, use them for certain enhancement transformations
			/*for(String filePath: filePath2KnowsPartOf){
				if(filePath.endsWith(".owl")){
					ontologies.add(filePath);
				}else if(filePath.endsWith(".csv")){
					csvs.add(filePath);
				}
			}*/
			
			KnowsSynonyms knowsSynonyms = new DummyKnowsSynonyms();
			//new CSVKnowsSynonyms(termReviewSynonyms, inflector);
			KnowsPartOf knowsPartOf = new DummyKnowsPartOf();
			//new JustKnowsPartOf(csvs, ontologies, knowsSynonyms, inflector); 
			
			RemoveNonSpecificBiologicalEntitiesByRelations transformer1 = new RemoveNonSpecificBiologicalEntitiesByRelations(
					knowsPartOf, knowsSynonyms, tokenizer, new CollapseBiologicalEntityToName());
			RemoveNonSpecificBiologicalEntitiesByBackwardConnectors transformer2 = new RemoveNonSpecificBiologicalEntitiesByBackwardConnectors(
					knowsPartOf, knowsSynonyms, tokenizer, new CollapseBiologicalEntityToName());
			RemoveNonSpecificBiologicalEntitiesByForwardConnectors transformer3 = new RemoveNonSpecificBiologicalEntitiesByForwardConnectors(
					knowsPartOf, knowsSynonyms, tokenizer, new CollapseBiologicalEntityToName());
			RemoveNonSpecificBiologicalEntitiesByPassedParents transformer4 = new RemoveNonSpecificBiologicalEntitiesByPassedParents(
					knowsPartOf, knowsSynonyms, tokenizer, new CollapseBiologicalEntityToName(), inflector);
			
			transformers.add(new SimpleRemoveSynonyms(knowsSynonyms));
			transformers.add(transformer1);
			transformers.add(transformer2);
			transformers.add(transformer3);
			transformers.add(transformer4);
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Reduced enhancement due to unavailability of knowledge entities for advanced enhancements");
		}

		transformers.add(new SplitCompoundBiologicalEntity(inflector));
		transformers.add(new SplitCompoundBiologicalEntitiesCharacters(inflector));
		transformers.add(new RemoveUselessWholeOrganism());
		transformers.add(new RemoveUselessCharacterConstraint());
		transformers.add(new RenameCharacter(renames));
		transformers.add(new MoveCharacterToStructureConstraint());
		transformers.add(new MoveNegationOrAdverbBiologicalEntityConstraint(wordNetPOSKnowledgeBase));
		transformers.add(new ReplaceTaxonNameByWholeOrganism());
		transformers.add(new CreateOrPopulateWholeOrganism(lifeStyles, "growth_form"));
		transformers.add(new CreateOrPopulateWholeOrganism(durations, "duration"));
		transformers.add(new StandardizeQuantityPresence());
		transformers.add(new StandardizeCount());
		transformers.add(new SortBiologicalEntityNameWithDistanceCharacter());
		transformers.add(new OrderBiologicalEntityConstraint());
		transformers.add(new StandardizeStructureNameBySyntax(characterKnowledgeBase, possessionTerms));
		//transformers.add(new StandardizeStructureNameTest(characterKnowledgeBase, possessionTerms));
		transformers.add(new StandardizeTerminology(characterKnowledgeBase));
		transformers.add(new RemoveOrphanRelations());
		transformers.add(new RemoveDuplicateValues());
		transformers.add(new CollapseBiologicalEntityToName());
		transformers.add(new CollapseCharacterToValue());
		transformers.add(new CollapseBiologicalEntities());
		transformers.add(new CollapseCharacters());
		transformers.add(mapOntologyIds);
		
		for(AbstractTransformer transformer : transformers) 
			try {
				transformer.transform(document);
			} catch(Throwable t) {
				t.printStackTrace();
			}
	}
	
	private static Set<String> getWordSet(String regexString) {
		Set<String> set = new HashSet<String>();
		String[] wordsArray = regexString.split("\\|");
		for (String word : wordsArray)
			set.add(word.toLowerCase().trim());
		return set;
	}

	private void initGlossary(IGlossary glossary, IInflector inflector, TaxonGroup taxonGroup/*, String termReviewTermCategorization, String termReviewSynonyms*/) throws IOException, ClassNotFoundException, InterruptedException, ExecutionException {
		addPermanentGlossary(glossary, inflector, taxonGroup);
		//addTermReviewGlossary(glossary, inflector, termReviewTermCategorization, termReviewSynonyms);
	}

	private void addTermReviewGlossary(IGlossary glossary,	IInflector inflector, String termReviewTermCategorization,	String termReviewSynonyms) throws IOException {
		List<Synonym> synonyms = new LinkedList<Synonym>();
		Set<String> hasSynonym = new HashSet<String>();
		
		if(termReviewSynonyms != null && new File(termReviewSynonyms).exists()) {
			try(CSVReader reader = new CSVReader(new FileReader(termReviewSynonyms))) {
				List<String[]> lines = reader.readAll();
				int i=0;
				for(String[] line : lines) {
					synonyms.add(new Synonym(String.valueOf(i), line[1], line[0], line[2]));
					hasSynonym.add(line[1]);
				}	
			}
		}
		
		if(termReviewTermCategorization != null && new File(termReviewTermCategorization).exists()) {
			try(CSVReader reader = new CSVReader(new FileReader(termReviewTermCategorization))) {
				List<String[]> lines = reader.readAll();
				List<Decision> decisions = new LinkedList<Decision>();
				int i=0;
				for(String[] line : lines) {
					decisions.add(new Decision(String.valueOf(i), line[1], line[0], hasSynonym.contains(line[1]), ""));
				}
				Download download = new Download(true, decisions, synonyms);
				
				//add syn set of term_category
				HashSet<Term> dsyns = new HashSet<Term>();
				if(download != null) {
					for(Synonym termSyn: download.getSynonyms()){
						//Hong TODO need to add category info to synonym entry in OTOLite
						//if(termSyn.getCategory().compareTo("structure")==0){
						if(termSyn.getCategory().matches("structure|taxon_name|substance")){
							//take care of singular and plural forms
							String syns = ""; 
							String synp = "";
							String terms = "";
							String termp = "";
							if(inflector.isPlural(termSyn.getSynonym().replaceAll("_",  "-"))){
								synp = termSyn.getSynonym().replaceAll("_",  "-");
								syns = inflector.getSingular(synp);					
							}else{
								syns = termSyn.getSynonym().replaceAll("_",  "-");
								synp = inflector.getPlural(syns);
							}
				
							if(inflector.isPlural(termSyn.getTerm().replaceAll("_",  "-"))){
								termp = termSyn.getTerm().replaceAll("_",  "-");
								terms = inflector.getSingular(termp);					
							}else{
								terms = termSyn.getTerm().replaceAll("_",  "-");
								termp = inflector.getPlural(terms);
							}
							//glossary.addSynonym(syns, termSyn.getCategory(), terms);
							//glossary.addSynonym(synp, termSyn.getCategory(), termp);
							//dsyns.add(new Term(syns, termSyn.getCategory());
							//dsyns.add(new Term(synp, termSyn.getCategory());
							glossary.addSynonym(syns, termSyn.getCategory(), terms);
							glossary.addSynonym(synp,termSyn.getCategory(), termp);
							dsyns.add(new Term(syns, termSyn.getCategory()));
							dsyns.add(new Term(synp, termSyn.getCategory()));
						}else{//forking_1 and forking are syns 5/5/14 hong test, shouldn't _1 have already been removed?
							glossary.addSynonym(termSyn.getSynonym().replaceAll("_",  "-"), termSyn.getCategory(), termSyn.getTerm());
							dsyns.add(new Term(termSyn.getSynonym().replaceAll("_",  "-"), termSyn.getCategory()));
						}					
					}
				
					//term_category from OTO, excluding dsyns
					for(Decision decision : download.getDecisions()) {
						if(!dsyns.contains(new Term(decision.getTerm().replaceAll("_",  "-"), decision.getCategory())))//calyx_tube => calyx-tube
							glossary.addEntry(decision.getTerm().replaceAll("_",  "-"), decision.getCategory());  
					}
				}
			}
		}
	}

	private void addPermanentGlossary(IGlossary glossary, IInflector inflector, TaxonGroup taxonGroup) throws InterruptedException, IOException, ClassNotFoundException, ExecutionException {
		/*remove the dependency on OTO, use local glossary*/
		/*
		OTOClient otoClient = new OTOClient("http://biosemantics.arizona.edu:8080/OTO");
		GlossaryDownload glossaryDownload = new GlossaryDownload();		
		String glossaryVersion = "latest";
		otoClient.open();
		Future<GlossaryDownload> futureGlossaryDownload = otoClient.getGlossaryDownload(taxonGroup.getDisplayName(), glossaryVersion);
		glossaryDownload = futureGlossaryDownload.get();
		otoClient.close();
		*/
		
		//TODO: remove the dependencies on OTO, replace otoClient with package edu.arizona.biosemantics.author.parse.MarkupCreator.java private GlossaryDownload getLocalGlossaryDownload(TaxonGroup taxonGroup)
		ObjectInputStream objectIn = new ObjectInputStream(new FileInputStream(Configuration.glossariesDownloadDirectory
				+ File.separator + "GlossaryDownload." + taxonGroup.getDisplayName() + ".ser"));
		GlossaryDownload glossaryDownload = (GlossaryDownload) objectIn.readObject();
		objectIn.close();
				
		//add the syn set of the glossary
		HashSet<Term> gsyns = new HashSet<Term>();
		for(TermSynonym termSyn : glossaryDownload.getTermSynonyms()) {
		
			//if(termSyn.getCategory().compareTo("structure")==0){
			if(termSyn.getCategory().matches("structure|taxon_name|substance")) {
				//take care of singular and plural forms
				String syns = ""; 
				String synp = "";
				String terms = "";
				String termp = "";
				if(inflector.isPlural(termSyn.getSynonym().replaceAll("_",  "-"))){ //must convert _ to -, as matching entity phrases will be converted from leg iii to leg-iii in the sentence.
					synp = termSyn.getSynonym().replaceAll("_",  "-");
					syns = inflector.getSingular(synp);					
				} else {
					syns = termSyn.getSynonym().replaceAll("_",  "-");
					synp = inflector.getPlural(syns);
				}
		
				if(inflector.isPlural(termSyn.getTerm().replaceAll("_",  "-"))){
					termp = termSyn.getTerm().replaceAll("_",  "-");
					terms = inflector.getSingular(termp);					
				}else{
					terms = termSyn.getTerm().replaceAll("_",  "-");
					termp = inflector.getPlural(terms);
				}
				glossary.addSynonym(syns, termSyn.getCategory(), terms);
				glossary.addSynonym(synp, termSyn.getCategory(), termp);
				gsyns.add(new Term(syns, termSyn.getCategory()));
				gsyns.add(new Term(synp, termSyn.getCategory()));
			} else {
				//glossary.addSynonym(termSyn.getSynonym().replaceAll("_",  "-"), "arrangement", termSyn.getTerm());
				glossary.addSynonym(termSyn.getSynonym().replaceAll("_",  "-"), termSyn.getCategory(), termSyn.getTerm());
				gsyns.add(new Term(termSyn.getSynonym().replaceAll("_",  "-"), termSyn.getCategory()));
				//gsyns.add(new Term(termSyn.getSynonym().replaceAll("_",  "-"), "arrangement"));
			}
		}
	
		//the glossary, excluding gsyns
		for(TermCategory termCategory : glossaryDownload.getTermCategories()) {
			if(!gsyns.contains(new Term(termCategory.getTerm().replaceAll("_", "-"), termCategory.getCategory())))
				glossary.addEntry(termCategory.getTerm().replaceAll("_", "-"), termCategory.getCategory()); //primocane_foliage =>primocane-foliage Hong 3/2014
		}
	}

}
