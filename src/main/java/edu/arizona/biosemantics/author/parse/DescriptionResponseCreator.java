package edu.arizona.biosemantics.author.parse;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.springframework.stereotype.Component;

import edu.arizona.biosemantics.author.parse.model.BiologicalEntity;
import edu.arizona.biosemantics.author.parse.model.Description;
import edu.arizona.biosemantics.author.parse.model.Relation;
import edu.arizona.biosemantics.author.parse.model.Statement;

@Component
public class DescriptionResponseCreator {
	
	private XPathExpression<Element> statementExpression;

	public DescriptionResponseCreator() {
		XPathFactory xpfac = XPathFactory.instance();
		this.statementExpression = xpfac.compile("//description/statement", Filters.element());
	}
	
	public Description create(Document document) {		
		return new Description(createResponseStatements(statementExpression.evaluate(document)));
	}

	private List<Statement> createResponseStatements(List<Element> statements) {
		List<Statement> result = new ArrayList<Statement>();
		for(Element s : statements) {
			result.add(createResponseStatement(s));
		}
		return result;
	}

	private Statement createResponseStatement(Element s) {
		String text = s.getChildText("text").intern();
		return new Statement(s.getAttributeValue("id"), s.getAttributeValue("notes"), 
				s.getAttributeValue("provenance"), text, 
				createResponseBiologicalEntities(s.getChildren("biological_entity")), 
				createResponseRelations(s.getChildren("relation")));
	}

	private List<BiologicalEntity> createResponseBiologicalEntities(List<Element> biologicalEntities) {
		List<BiologicalEntity> result = new ArrayList<BiologicalEntity>();
		for(Element e : biologicalEntities) {
			result.add(createResponseEntity(e));
		}
		return result;
	}

	private BiologicalEntity createResponseEntity(Element e) {
		return new BiologicalEntity(
				e.getAttributeValue("alter_name"), 
				createResponseCharacters(e.getChildren("character")),
				e.getAttributeValue("constraint"),
				e.getAttributeValue("constraintid"), 
				e.getAttributeValue("constraint_original"), 
				e.getAttributeValue("geographical_constraint"), 
				e.getAttributeValue("id"), 
				e.getAttributeValue("in_brackets"), 
				e.getAttributeValue("name"), 
				e.getAttributeValue("name_original"), 
				e.getAttributeValue("notes"), 
				e.getAttributeValue("ontologyid"), 
				e.getAttributeValue("parallelism_constraint"), 
				e.getAttributeValue("provenance"), 
				e.getAttributeValue("src"), 
				e.getAttributeValue("taxon_constraint"), 
				e.getAttributeValue("type"));
	}

	private List<edu.arizona.biosemantics.author.parse.model.Character> createResponseCharacters(
			List<Element> characters) {
		List<edu.arizona.biosemantics.author.parse.model.Character> result = 
				new ArrayList<edu.arizona.biosemantics.author.parse.model.Character>();
		for(Element c : characters) {
			result.add(createResponseCharacter(c));
		}
		return result;
	}

	private edu.arizona.biosemantics.author.parse.model.Character createResponseCharacter(Element c) {
		return new edu.arizona.biosemantics.author.parse.model.Character(
				c.getAttributeValue("char_type"),
				c.getAttributeValue("constraint"),
				c.getAttributeValue("constraintid"),
				c.getAttributeValue("establishment_means"),
				c.getAttributeValue("from"),
				c.getAttributeValue("from_inclusive"),
				c.getAttributeValue("from_modifier"),
				c.getAttributeValue("from_unit"),
				c.getAttributeValue("gegraphical_constraint"), 
				c.getAttributeValue("in_brackets"), 
				c.getAttributeValue("is_modifier"), 
				c.getAttributeValue("modifier"), 
				c.getAttributeValue("name"), 
				c.getAttributeValue("notes"), 
				c.getAttributeValue("ontologyid"), 
				c.getAttributeValue("organ_constraint"), 
				c.getAttributeValue("other_constraint"), 
				c.getAttributeValue("parallelism_constraint"), 
				c.getAttributeValue("provenance"), 
				c.getAttributeValue("src"), 
				c.getAttributeValue("taxon_constraint"), 
				c.getAttributeValue("to"), 
				c.getAttributeValue("to_inclusive"), 
				c.getAttributeValue("to_modifier"),
				c.getAttributeValue("to_unit"), 
				c.getAttributeValue("type"), 
				c.getAttributeValue("unit"), 
				c.getAttributeValue("upper_restricted"), 
				c.getAttributeValue("value"));
	}

	private List<Relation> createResponseRelations(List<Element> relations) {
		List<Relation> result = new ArrayList<Relation>();
		for(Element r : relations) {
			result.add(createResponseRelation(r));
		}
		return result;
	}

	private Relation createResponseRelation(Element r) {
		return new Relation(r.getAttributeValue("alter_name"), r.getAttributeValue("from"), 
				r.getAttributeValue("geographical_constraint"),
				r.getAttributeValue("id"), 
				r.getAttributeValue("in_brackets"), 
				r.getAttributeValue("modifier"), 
				r.getAttributeValue("name"), 
				r.getAttributeValue("negation"), 
				r.getAttributeValue("notes"), 
				r.getAttributeValue("ontologyid"), 
				r.getAttributeValue("organ_constraint"), 
				r.getAttributeValue("parallelism_constraint"),
				r.getAttributeValue("provenance"), 
				r.getAttributeValue("src"),
				r.getAttributeValue("taxon_constraint"),
				r.getAttributeValue("to"));
	}
	
}
