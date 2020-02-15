package edu.arizona.biosemantics.author.parse;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.inject.Inject;

import edu.arizona.biosemantics.common.ling.know.IGlossary;
import edu.arizona.biosemantics.common.ling.transform.IInflector;
import edu.arizona.biosemantics.semanticmarkup.ling.know.lib.ElementRelationGroup;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.lib.NoTerminologyLearner;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.AbstractDescriptionsFile;

public class GlossaryBasedTerminologyLearner extends NoTerminologyLearner {
	
	private IGlossary glossary;
	private HashMap<String, Set<String>> roleToWords;
	private HashMap<String, Set<String>> wordsToRoles;
	private IInflector inflector;

	@Inject
	public GlossaryBasedTerminologyLearner(IGlossary glossary, IInflector inflector) {
		this.glossary = glossary;
		this.inflector = inflector;
	}
	
	@Override
	public void readResults(List<AbstractDescriptionsFile> descriptionsFiles) {
		Set<String> structures = glossary.getWordsInCategory("structure");
		Set<String> nonCharacterCategories = new HashSet<String>();
		nonCharacterCategories.add("structure");
		nonCharacterCategories.add("substance");
		nonCharacterCategories.add("taxon_name");
		Set<String> characters = glossary.getWordsNotInCategories(nonCharacterCategories);
		this.roleToWords = new HashMap<String, Set<String>>();
		this.wordsToRoles = new HashMap<String, Set<String>>();
		for(String structure : structures) {
			String semanticRole = "os";
			
			if(inflector.isPlural(structure)) {
				semanticRole = "op";
			}
			if(!roleToWords.containsKey(semanticRole))
				roleToWords.put(semanticRole, new HashSet<String>());
			roleToWords.get(semanticRole).add(structure);
			
			if(!wordsToRoles.containsKey(structure))
				wordsToRoles.put(structure, new HashSet<String>());
			wordsToRoles.get(structure).add(semanticRole);
		}
		
		for(String character : characters) {
			String semanticRole = "c";
			if(!roleToWords.containsKey(semanticRole))
				roleToWords.put(semanticRole, new HashSet<String>());
			roleToWords.get(semanticRole).add(character);
			
			if(!wordsToRoles.containsKey(character))
				wordsToRoles.put(character, new HashSet<String>());
			wordsToRoles.get(character).add(semanticRole);
		}
	}
	
	@Override
	public Map<String, Set<String>> getRoleToWords() {
		return roleToWords;
	}

	@Override
	public Map<String, Set<String>> getWordsToRoles() {
		return wordsToRoles;
	}
}
