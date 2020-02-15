package edu.arizona.biosemantics.author.parse.model;

import java.util.List;

public class Description {
	
	private List<Statement> statements;
	
	public Description(List<Statement> statements) {
		this.statements = statements;
	}

	public List<Statement> getStatements() {
		return statements;
	}
}
