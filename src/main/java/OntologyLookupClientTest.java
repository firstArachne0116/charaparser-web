import java.util.HashSet;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import edu.arizona.biosemantics.oto.common.ontologylookup.search.OntologyLookupClient;

public class OntologyLookupClientTest {

	public static void main(String[] args) {
		HashSet<String> entityOntologies = new HashSet<String>();
		entityOntologies.add("exp_1");
		HashSet<String> qualityOntologies = new HashSet<String>();
		OntologyLookupClient client = new OntologyLookupClient(entityOntologies, qualityOntologies, "ontologies", "wordnet/wn31/dict");
		OWLOntologyManager man = client.ontoutil.OWLentityOntoAPIs.get(0).getManager();
		OWLOntology onto = man.getOntology(IRI.create("http://biosemantics.arizona.edu/ontologies/exp"));
		client.searchStructure("leaf", "", "", false);
	}
}
