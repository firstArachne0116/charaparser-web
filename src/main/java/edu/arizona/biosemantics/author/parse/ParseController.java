package edu.arizona.biosemantics.author.parse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

import edu.arizona.biosemantics.author.ontology.search.OntologySearchController;
import edu.arizona.biosemantics.author.parse.model.BiologicalEntity;
import edu.arizona.biosemantics.author.parse.model.Description;
import edu.arizona.biosemantics.author.parse.model.Relation;
import edu.arizona.biosemantics.author.parse.model.Statement;
import edu.arizona.biosemantics.oto2.oto.server.Configuration;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.ChunkCollector;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.IDescriptionExtractor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.transform.SentenceChunkerRun;


@RestController
public class ParseController {
	private static final Logger LOGGER = LoggerFactory.getLogger(ParseController.class);
	private MarkupCreator markupCreator;
	private DescriptionResponseCreator descriptionResponseCreator;
	private DocumentCreator documentCreator;
	private EnhanceRun enhanceRun;
	private SentenceSplitter sentenceSplitter;
	//private HashMap<String, Hashtable<String, String>> termDefinitionMap = new HashMap<String, Hashtable<String, String>>();
	//private OntologySearchController OSC;


	@Autowired
	public ParseController(MarkupCreator markupCreator, DocumentCreator documentCreator,
			EnhanceRun enhanceRun, DescriptionResponseCreator descriptionResponseCreator, 
			SentenceSplitter sentenceSplitter) throws Exception {
		this.markupCreator = markupCreator;
		this.documentCreator = documentCreator;
		//this.enhanceRun = new EnhanceRun(OSC);
		this.enhanceRun = enhanceRun;
		this.descriptionResponseCreator = descriptionResponseCreator;
		this.sentenceSplitter = sentenceSplitter;
		
	}

	/*test description: perigynium beak weak, 4-5 mm; apex awnlike; stamen branching, full.*/
	@GetMapping(value = "/parse", produces = { MediaType.APPLICATION_JSON_VALUE })
	public Description parse(@RequestParam Optional<String> sentence, @RequestParam Optional<String> description) throws Exception {
		
		
		List<ChunkCollector> chunkCollectors = new ArrayList<ChunkCollector>();
		edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Description descriptionObject = 
				new edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Description();

		String descriptionText = "";
		if(description.isPresent()) {
			descriptionText = description.get();
			descriptionObject.setText(descriptionText);
			List<String> sentences = sentenceSplitter.split(descriptionText);	
			for(int source = 0; source < sentences.size(); source++) {
				edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Statement statement = new edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Statement();
				statement.setText(sentences.get(source));
				String statementId = "d" + "0" + "_s" + source; //"0" refers to the current description in the text editor
				statement.setId(statementId);
				descriptionObject.addStatement(statement);
				SentenceChunkerRun chunkerRun = markupCreator.createChunkerRun(sentences.get(source), String.valueOf(source));
				ChunkCollector chunkCollector = chunkerRun.call();
				System.out.println(chunkCollector.toString());
				chunkCollectors.add(chunkCollector);
			}
		} else if(sentence.isPresent()) {
			descriptionText = sentence.get();
			descriptionObject.setText(descriptionText);
			edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Statement statement = new edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Statement();
			statement.setText(descriptionText);
			String statementId = "d" + "0" + "_s" + "0"; //"0" refers to the current description and the current sentence in the text editor
			statement.setId(statementId);
			descriptionObject.addStatement(statement);
			SentenceChunkerRun chunkerRun = markupCreator.createChunkerRun(descriptionText, String.valueOf(1));
			ChunkCollector chunkCollector = chunkerRun.call();
			System.out.println(chunkCollector.toString());
			chunkCollectors.add(chunkCollector);
		}
		if(chunkCollectors.isEmpty())
			throw new IllegalArgumentException();
		
		return createDescription(chunkCollectors, descriptionObject);
	}
	
	@GetMapping(value = "/{ontology}/getDefinition", produces = { MediaType.APPLICATION_JSON_VALUE })
	public String getClassHierarchyInJSON(@PathVariable String ontology, @RequestParam Optional<String> user, 
			@RequestParam String baseIri, @RequestParam String term){
		String usrid = "";
		String ontoName = ontology; //this ontology=carex
		if(user.isPresent()){
			usrid = user.get();
			ontoName = ontology+"_"+usrid;
		}
		return MapOntologyIds.termDefinitionCache.get(ontoName).get(baseIri+"#"+term);
	}
	
	private Description createDescription(List<ChunkCollector> chunkCollectors, edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Description description) throws IOException, InterruptedException, ExecutionException,  OWLOntologyCreationException {
		IDescriptionExtractor descriptionExtractor = markupCreator.createDescriptionExtractor();
		descriptionExtractor.extract(description, 1, chunkCollectors);
		

		Document document = documentCreator.create(description);
		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        System.out.println(outputter.outputString(document));
        //EnhanceRun enhanceRun = new EnhanceRun(OSC);//create a fresh instance of enhanceRun to use updated ontology
		enhanceRun.run(document);
        System.out.println(outputter.outputString(document));
		
		return descriptionResponseCreator.create(document);
	}
	
	/* original from Thomas 
	@GetMapping(value = "/parse", produces = { MediaType.APPLICATION_JSON_VALUE })
	public Description parse(@RequestParam Optional<String> sentence, @RequestParam Optional<String> description) throws Exception {
		List<ChunkCollector> chunkCollectors = new ArrayList<ChunkCollector>();
		String descriptionText = "";
		if(description.isPresent()) {
			descriptionText = description.get();
			List<String> sentences = sentenceSplitter.split(descriptionText);	
			for(int source = 0; source < sentences.size(); source++) {
				SentenceChunkerRun chunkerRun = markupCreator.createChunkerRun(sentences.get(source), String.valueOf(source));
				ChunkCollector chunkCollector = chunkerRun.call();
				System.out.println(chunkCollector.toString());
				chunkCollectors.add(chunkCollector);
			}
		} else if(sentence.isPresent()) {
			descriptionText = sentence.get();
			SentenceChunkerRun chunkerRun = markupCreator.createChunkerRun(descriptionText, String.valueOf(1));
			ChunkCollector chunkCollector = chunkerRun.call();
			System.out.println(chunkCollector.toString());
			chunkCollectors.add(chunkCollector);
		}
		if(chunkCollectors.isEmpty())
			throw new IllegalArgumentException();
		
		return createDescription(chunkCollectors, descriptionText);
	}
	
	private Description createDescription(List<ChunkCollector> chunkCollectors, String descriptionText) throws IOException {
		IDescriptionExtractor descriptionExtractor = markupCreator.createDescriptionExtractor();
		edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Description description = 
				new edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Description();
		description.setText(descriptionText);
		descriptionExtractor.extract(description, 1, chunkCollectors);
		

		Document document = documentCreator.create(description);
		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        System.out.println(outputter.outputString(document));
		enhanceRun.run(document);
        System.out.println(outputter.outputString(document));
		
		return descriptionResponseCreator.create(document);
	}*/
}



/* test description 
 * perigynium beak weak, 4-5 mm long; apex awnlike; stamen branching, full.
Petals as many as sepals, sometimes absent
 

Herbs, perennial, cespitose or not, rhizomatous, rarely stoloniferous. Culms usually trigonous, sometimes round. Leaves basal and cauline, sometimes all basal; ligules present; blades flat, V-shaped, or M-shaped in cross section, rarely filiform, involute, or rounded, commonly less than 20 mm wide, if flat then with distinct midvein. Inflorescences terminal, consisting of spikelets borne in spikes arranged in spikes, racemes, or panicles; bracts subtending spikes leaflike or scalelike; bracts subtending spikelets scalelike, very rarely leaflike. Spikelets 1-flowered; scales 0–1. Flowers unisexual; staminate flowers without scales; pistillate flowers with 1 scale with fused margins (perigynium) enclosing flower, open only at apex; perianth absent; stamens 1–3; styles deciduous or variously persistent, linear, 2–3(–4)-fid. Achenes biconvex, plano-convex, or trigonous, rarely 4-angled. x = 10.
*/