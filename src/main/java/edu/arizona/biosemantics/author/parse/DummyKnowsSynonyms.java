package edu.arizona.biosemantics.author.parse;

import java.util.HashSet;
import java.util.Set;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsSynonyms;

public class DummyKnowsSynonyms implements KnowsSynonyms {

	@Override
	public Set<SynonymSet> getSynonyms(String term, String category) {
		return new HashSet<SynonymSet>();
	}

	@Override
	public void log(LogLevel arg0, String arg1, Throwable arg2) { }

	@Override
	public void log(LogLevel arg0, String arg1) {	}

}
