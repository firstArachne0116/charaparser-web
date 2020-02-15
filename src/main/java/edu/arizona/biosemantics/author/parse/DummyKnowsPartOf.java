package edu.arizona.biosemantics.author.parse;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsPartOf;

public class DummyKnowsPartOf implements KnowsPartOf {

	@Override
	public boolean isPartOf(String part, String parent) {
		return false;
	}
	
	@Override
	public void log(LogLevel arg0, String arg1, Throwable arg2) { }

	@Override
	public void log(LogLevel arg0, String arg1) {	}

}
