import java.util.HashSet;

import edu.arizona.biosemantics.oto.common.ontologylookup.search.OntologyLookupClient;
import edu.arizona.biosemantics.oto.common.ontologylookup.search.search.TermSearcher;

public class TermSearcherTest {

	public static void main(String[] args) {
		HashSet<String> qualityOntologies = new HashSet<String>();
		qualityOntologies.add("exp_2");
		OntologyLookupClient olc = new OntologyLookupClient(
				new HashSet<String>(), 
				qualityOntologies, 
				"ontologies",
				"wordNet/wn31/dict");
		TermSearcher ts = new TermSearcher(olc, false);
		ts.searchTerm("length of leaf blade", "quality", 1.0f);
	}
	
}
