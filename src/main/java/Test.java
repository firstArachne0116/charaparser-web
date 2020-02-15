import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.obolibrary.macro.ManchesterSyntaxTool;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.ChangeApplied;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import edu.arizona.biosemantics.common.ontology.search.FileSearcher;
import edu.arizona.biosemantics.author.ontology.search.model.OntologyIRI;
import edu.arizona.biosemantics.author.ontology.search.model.Synonym;
import edu.arizona.biosemantics.common.ontology.search.OntologyAccess;
import edu.arizona.biosemantics.common.ontology.search.model.Ontology;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.AnnotationProperty;

public class Test {

	private static String ontoDir = "C:/Users/hongcui/Documents/research/AuthorOntology/Experiment/ontologies";
	private static OntologyIRI CAREX = new OntologyIRI(ontoDir+"/"+"CAREX.owl", 
			"http://biosemantics.arizona.edu/ontologies/carex",
			"CAREX");
	private static String wordNetDir = "C:/Users/hongcui/Documents/research/AuthorOntology/Experiment/wordnet/wn31/dict";
	private static String HAS_PART = "http://purl.obolibrary.org/obo/BFO_0000051"; 
	private static String ELUCIDATION = "http://purl.oblibrary.org/obo/IAO_0000600";
	private static String createdBy = "http://www.geneontology.org/formats/oboInOwl#created_by";
	private static String creationDate = "http://www.geneontology.org/formats/oboInOwl#creation_date";
	private static String definitionSrc = "http://purl.obolibrary.org/obo/IAO_0000119";
	private static String exampleOfUsage = "http://purl.obolibrary.org/obo/IAO_0000112";
	
	static Hashtable<String, String> ontologyIRIs = new Hashtable<String, String>();
	static{
		ontologyIRIs.put("CAREX", "http://biosemantics.arizona.edu/ontologies/carex");
		ontologyIRIs.put("EXP", "http://biosemantics.arizona.edu/ontologies/exp");
		ontologyIRIs.put("PO", "http://purl.obolibrary.org/obo/po");
		ontologyIRIs.put("PATO", "http://purl.obolibrary.org/obo/pato");
	}

	public static void main(String[] args) throws Exception{
		/** Setting up ontology access (search, modify) facilitators; same as in controller initialization **/
		//individual
		String userId = "1";
		String[] userOntologies ={"EXP"};
		
		//copy base ontologies to user ontologies
		HashSet<String> entityOntologyNames = new HashSet<String>();
		OntologyIRI o;
		int i = 0;
		String onto = userOntologies[0];
		File ontoS = new File(ontoDir, onto.toLowerCase()+".owl");
		File ontoD = new File(ontoDir, onto.toLowerCase()+"_"+userId+".owl"); //ontology indexed as EXP_1.owl, EXP_2.owl, 1 and 2 are user ids.
		if(!ontoD.exists())
			Files.copy(ontoS.toPath(), ontoD.toPath(), StandardCopyOption.REPLACE_EXISTING);

		o = new OntologyIRI(ontoD.getAbsolutePath(), 
				ontologyIRIs.get(onto.toUpperCase()), onto+"_"+userId.toUpperCase()); //for experiments
		entityOntologyNames.add(onto+"_"+userId.toUpperCase()); //EXP_1

		//shared
		/*
		OntologyIRI o = CAREX;
		HashSet<String> entityOntologyNames = new HashSet<String>();
		entityOntologyNames.add(o.getName());
		 */

		//set up ontology search environment
		FileSearcher searcher = new FileSearcher(entityOntologyNames, new HashSet<String>(), 
				ontoDir, wordNetDir, false);
		OWLOntologyManager owlOntologyManager = searcher.getOwlOntologyManager();
		OWLOntology owlOntology = owlOntologyManager.getOntology(IRI.create(o.getIri()));
		Set<OWLOntology> ontologies = new HashSet<OWLOntology>();
		ontologies.add(owlOntology);
		OntologyAccess ontologyAccess  = new OntologyAccess(ontologies);

		
		OWLDataFactory owlDataFactory = owlOntologyManager.getOWLDataFactory();
		long start = System.currentTimeMillis();
		for(int t = 0; t< 1000; t++){
			/** Try sample addition of class; same as in controller upon incoming request **/
			/*String subclassIRI = "http://test.org/terms#test"+t;
			OWLClass subclass = owlDataFactory.getOWLClass(subclassIRI);
			
			OWLAnnotationProperty labelProperty =
					owlDataFactory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());
			OWLLiteral labelLiteral = owlDataFactory.getOWLLiteral("test"+t, "en");
			OWLAnnotation labelAnnotation = owlDataFactory.getOWLAnnotation(labelProperty, labelLiteral);
			OWLAxiom labelAxiom = owlDataFactory.getOWLAnnotationAssertionAxiom(subclass.getIRI(), labelAnnotation);
			ChangeApplied change = owlOntologyManager.addAxiom(owlOntology, labelAxiom);
			//if(change != ChangeApplied.SUCCESSFULLY && change !=ChangeApplied.NO_OPERATION )
				//return change.name();
			
			OWLClass superclass = owlDataFactory.getOWLClass("http://biosemantics.arizona.edu/ontologies/exp#apex");
			OWLAxiom subclassAxiom = owlDataFactory.getOWLSubClassOfAxiom(subclass, superclass);
			change = owlOntologyManager.addAxiom(owlOntology, subclassAxiom);
			//if(change != ChangeApplied.SUCCESSFULLY && change !=ChangeApplied.NO_OPERATION)
				//return change.name();
			
			OWLAnnotationProperty definitionProperty = 
					owlDataFactory.getOWLAnnotationProperty(IRI.create(AnnotationProperty.DEFINITION.getIRI()));
			OWLAnnotation definitionAnnotation = owlDataFactory.getOWLAnnotation
					(definitionProperty, owlDataFactory.getOWLLiteral("d", "en")); 
			OWLAxiom definitionAxiom = owlDataFactory.getOWLAnnotationAssertionAxiom(
					subclass.getIRI(), definitionAnnotation); 
			change = owlOntologyManager.addAxiom(owlOntology, definitionAxiom);
			
			//if(change != ChangeApplied.SUCCESSFULLY && change !=ChangeApplied.NO_OPERATION)
				//return change.name();
			
			//if(c.getElucidation() != null && !c.getElucidation().isEmpty()) {
				OWLAnnotationProperty elucidationProperty = 
						owlDataFactory.getOWLAnnotationProperty(IRI.create(ELUCIDATION));
				OWLAnnotation elucidationAnnotation = owlDataFactory.getOWLAnnotation
						(elucidationProperty, owlDataFactory.getOWLLiteral("http://test.jpg")); 
				OWLAxiom elucidationAxiom = owlDataFactory.getOWLAnnotationAssertionAxiom(
						subclass.getIRI(), elucidationAnnotation); 
				change = owlOntologyManager.addAxiom(owlOntology, elucidationAxiom);
				//if(change != ChangeApplied.SUCCESSFULLY && change !=ChangeApplied.NO_OPERATION)
					//return change.name();
			//}
			
			
			//if(c.getCreatedBy() != null && !c.getCreatedBy().isEmpty()) {
				OWLAnnotationProperty createdByProperty = 
						owlDataFactory.getOWLAnnotationProperty(IRI.create(createdBy));
				OWLAnnotation createdByAnnotation = owlDataFactory.getOWLAnnotation
						(createdByProperty, owlDataFactory.getOWLLiteral("me")); 
				OWLAxiom createdByAxiom = owlDataFactory.getOWLAnnotationAssertionAxiom(
						subclass.getIRI(), createdByAnnotation); 
				change = owlOntologyManager.addAxiom(owlOntology, createdByAxiom);
				//if(change != ChangeApplied.SUCCESSFULLY && change !=ChangeApplied.NO_OPERATION)
					//return change.name();
			//}
			
			//if(c.getCreationDate() != null && !c.getCreationDate().isEmpty()) {
				OWLAnnotationProperty CreationDateProperty = 
						owlDataFactory.getOWLAnnotationProperty(IRI.create(creationDate));
				OWLAnnotation CreationDateAnnotation = owlDataFactory.getOWLAnnotation
						(CreationDateProperty, owlDataFactory.getOWLLiteral("today")); 
				OWLAxiom CreationDateAxiom = owlDataFactory.getOWLAnnotationAssertionAxiom(
						subclass.getIRI(), CreationDateAnnotation); 
				change = owlOntologyManager.addAxiom(owlOntology, CreationDateAxiom);
				//if(change != ChangeApplied.SUCCESSFULLY&& change !=ChangeApplied.NO_OPERATION)
					//return change.name();
			//}
			
			//if(c.getDefinitionSrc() != null && !c.getDefinitionSrc().isEmpty()) {
				OWLAnnotationProperty DefinitionSrcProperty = 
						owlDataFactory.getOWLAnnotationProperty(IRI.create(definitionSrc));
				OWLAnnotation DefinitionSrcAnnotation = owlDataFactory.getOWLAnnotation
						(DefinitionSrcProperty, owlDataFactory.getOWLLiteral("me")); 
				OWLAxiom DefinitionSrcAxiom = owlDataFactory.getOWLAnnotationAssertionAxiom(
						subclass.getIRI(), DefinitionSrcAnnotation); 
				change = owlOntologyManager.addAxiom(owlOntology, DefinitionSrcAxiom);
				//if(change != ChangeApplied.SUCCESSFULLY&& change !=ChangeApplied.NO_OPERATION)
					//return change.name();
			//}

			

			//if(c.getLogicDefinition() != null && !c.getLogicDefinition().isEmpty()) {
				OWLClassExpression clsB = null;
				try{
					ManchesterSyntaxTool parser = new ManchesterSyntaxTool(owlOntology, null);
					clsB = parser.parseManchesterExpression("'part of' some 'physical entity'"); //"'part of' some 'physical entity'"
				}catch(Exception e){
					String msg = e.getMessage();
					//System.out.println(msg);
					//return msg;
				}
				OWLAxiom def = owlDataFactory.getOWLEquivalentClassesAxiom(subclass, clsB);
				change = owlOntologyManager.addAxiom(owlOntology, def);
			//}
			*/
			
			/** Try sample addition of synonym; same as in controller upon incoming request **/
			Synonym synonym = new Synonym("", "test"+t, "", "http://biosemantics.arizona.edu/ontologies/exp#apex");
			String synonymTerm = synonym.getTerm();
			OWLClass clazz = owlDataFactory.getOWLClass(synonym.getClassIRI());
			OWLAnnotationProperty exactSynonymProperty = 
					owlDataFactory.getOWLAnnotationProperty(IRI.create(AnnotationProperty.EXACT_SYNONYM.getIRI()));
			OWLAnnotation synonymAnnotation = owlDataFactory.getOWLAnnotation(
					exactSynonymProperty, owlDataFactory.getOWLLiteral(synonymTerm, "en"));
			OWLAxiom synonymAxiom = owlDataFactory.getOWLAnnotationAssertionAxiom(clazz.getIRI(), synonymAnnotation);
			try {
				ChangeApplied done = owlOntologyManager.addAxiom(owlOntology, synonymAxiom);
				System.out.println(done.name());
				//try(FileOutputStream fos = new FileOutputStream(ontoDir + File.separator + o.getName().toLowerCase() + ".owl")) {
				//	owlOntologyManager.saveOntology(owlOntologyManager.getOntology(IRI.create(o.getIri())), fos);
				//}
			} catch(Throwable th) {
				th.printStackTrace();
			}
		}
		long end = System.currentTimeMillis();
		System.out.println("Total time:"+ (end-start));
	}

}
