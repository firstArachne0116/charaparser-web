package edu.arizona.biosemantics.author.ontology.search;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.springframework.stereotype.Component;

import edu.arizona.biosemantics.author.ontology.search.model.Annotation;
import edu.arizona.biosemantics.author.ontology.search.model.OntologySearchResult;
import edu.arizona.biosemantics.author.ontology.search.model.OntologySearchResultEntry;
import edu.arizona.biosemantics.common.ontology.search.OntologyAccess;
import edu.arizona.biosemantics.common.ontology.search.model.OntologyEntry;

@Component
public class OntologySearchResultCreator {

	//private static final Logger LOGGER = LoggerFactory.getLogger(OntologySearchResultCreator.class);
	//private static Ontology[] entityOntologies = { Ontology.po, Ontology.carex };
	//private static String ELUCIDATION = "http://purl.oblibrary.org/obo/IAO_0000600";
	private static String oboInOwlId = "http://www.geneontology.org/formats/oboInOwl#id";
	
	public OntologySearchResult create(String o, List<OntologyEntry> entries, 
			OntologyAccess ontologyAccess, OWLOntology owlOntology, OWLOntologyManager owlOntologyManager) {
		OntologySearchResult result = new OntologySearchResult();
		ArrayList<OntologyEntry> processed = new ArrayList<OntologyEntry>();
		
		for(OntologyEntry e : entries) {
			String iri = e.getClassIRI();
			if(inProcessed(processed, e)) continue;
			if(iri==null) continue; //spatial terms such as left side will matched to a fixed spatial term pattern in ontology search, but without an iri because BSPO is not used.
			OWLClass owlClass = owlOntologyManager.getOWLDataFactory().getOWLClass(IRI.create(iri));
			
			List<Annotation> resultAnnotations = new ArrayList<Annotation>();
			//add oboInOwl:id property
			resultAnnotations.add(new Annotation(oboInOwlId, iri));
			//data properties
			Set<OWLClassExpression> superclasses = EntitySearcher.getSuperClasses(owlClass, owlOntology).collect(Collectors.toSet());
			for(OWLClassExpression superclass: superclasses){
				if(superclass instanceof OWLObjectSomeValuesFrom){
					OWLObjectSomeValuesFrom property = (OWLObjectSomeValuesFrom)superclass;
					if(property.isObjectRestriction()){
						resultAnnotations.add(new Annotation(property.getProperty().toString().replaceAll("^<|>$", ""), 
								property.getFiller().toString().replaceAll("^<|>$", "")));
					}
				}
				if(superclass instanceof OWLObjectAllValuesFrom){
					OWLObjectAllValuesFrom property = (OWLObjectAllValuesFrom)superclass;
					if(property.isObjectRestriction()){
						resultAnnotations.add(new Annotation(property.getProperty().toString().replaceAll("^<|>$", ""), 
								property.getFiller().toString().replaceAll("^<|>$", "")));
					}
				}
			}
			//annotation properties
			Set<OWLAnnotation> annotations = EntitySearcher.getAnnotations(owlClass, owlOntology).collect(Collectors.toSet());
			for(OWLAnnotation a : annotations) {
				Optional<OWLLiteral> literal = a.getValue().annotationValue().asLiteral();
				if(literal.isPresent()) {
					resultAnnotations.add(new Annotation(a.getProperty().getIRI().getIRIString(), literal.get().getLiteral()));
				} /*else {
					resultAnnotations.add(new Annotation(a.getProperty().getIRI().getIRIString(), ""));
				}*/
			}
			

			
			/*String elucidation = this.getElucidation(owlClass, owlOntology);
			if(elucidation != null)
				resultAnnotations.add(new Annotation("elucidation", elucidation));*/
			Set<OWLClass> bearers = ontologyAccess.getBearers(owlClass);
			Set<OWLClass> parts = ontologyAccess.getParts(owlClass);
			for(OWLClass bearer : bearers){
				if(!bearer.equals(owlClass))
					resultAnnotations.add(new Annotation("part of", bearer.getIRI().getIRIString()));
			}
			for(OWLClass part : parts){
				if(!part.equals(owlClass))
				resultAnnotations.add(new Annotation("has part", part.getIRI().getIRIString()));
			}
			processed.add(e);
			result.getEntries().add(new OntologySearchResultEntry(e.getLabel(), e.getScore(), e.getParentLabel(), resultAnnotations));
		}
		return result;
	}

	private boolean inProcessed(ArrayList<OntologyEntry> processed, OntologyEntry e) {
		Iterator<OntologyEntry> it = processed.iterator();
		while(it.hasNext()){
			OntologyEntry t = (OntologyEntry)it.next();
			if((e.getLabel()+e.getClassIRI()).compareTo(t.getLabel()+t.getClassIRI())==0) return true;
		}
		return false;
	}
	
	/*public String getElucidation(OWLClass owlClass, OWLOntology owlOntology) {
		String elucidation = null;
		Optional<OWLAnnotationAssertionAxiom> firstElucidation = EntitySearcher.getAnnotationAssertionAxioms(owlClass, owlOntology).filter(a -> {
			return a.getProperty().getIRI().equals(IRI.create(ELUCIDATION));
		}).findFirst();
		
		if(firstElucidation.isPresent()) {
			Optional<OWLLiteral> literal = firstElucidation.get().literalValue();
			if(literal.isPresent())
				elucidation = literal.get().getLiteral();
		}
		return elucidation;
	}*/
	
}
