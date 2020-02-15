package edu.arizona.biosemantics.author.parse;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.springframework.stereotype.Component;

import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.BiologicalEntity;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Description;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Statement;

@Component
public class DocumentCreator {
	
	private Namespace bioNamespace = Namespace.getNamespace("bio", "http://www.github.com/biosemantics");
	
	public Document create(Description description) {
		Document document = new Document();
		
		Element root = new Element("treatment");
		root.setNamespace(bioNamespace);
		document.setRootElement(root);
		
		Element descr = new Element("description");
		descr.setAttribute("type", "morphology");
		root.addContent(descr);
		
		for(Statement s : description.getStatements()) {
			descr.addContent(createStatementElement(s));
		}
		
		return document;
	}

	private Element createStatementElement(Statement s) {
		Element statement = new Element("statement");
		statement.setAttribute("id", s.getId());
		Element text = new Element("text");
		text.setText(s.getText());
		statement.addContent(text);
		for(BiologicalEntity b : s.getBiologicalEntities()) {
			Element entity = new Element("biological_entity");
			
			if(b.getAlterName() != null)
				entity.setAttribute("alter_name", b.getAlterName());
			if(b.getConstraint() != null)
				entity.setAttribute("constraint", b.getConstraint());
			if(b.getConstraintId() != null)
				entity.setAttribute("constraintid", b.getConstraintId());
			if(b.getConstraintOriginal() != null)
				entity.setAttribute("constraint_original", b.getConstraintOriginal());
			if(b.getGeographicalConstraint() != null)
				entity.setAttribute("geographical_constraint", b.getGeographicalConstraint());
			if(b.getId() != null)
				entity.setAttribute("id", b.getId());
			if(b.getInBrackets() != null)
				entity.setAttribute("in_brackets", b.getInBrackets());
			if(b.getName() != null)
				entity.setAttribute("name", b.getName());
			if(b.getNameOriginal() != null)
				entity.setAttribute("name_original", b.getNameOriginal());
			if(b.getNotes() != null)
				entity.setAttribute("notes", b.getNotes());
			if(b.getOntologyId() != null)
				entity.setAttribute("ontologyid", b.getOntologyId());
			if(b.getParallelismConstraint() != null)
				entity.setAttribute("parallelism_constraint", b.getParallelismConstraint());
			if(b.getProvenance() != null)
				entity.setAttribute("provenance", b.getProvenance());
			if(b.getSrc() != null)
				entity.setAttribute("src", b.getSrc());
			if(b.getTaxonConstraint() != null)
				entity.setAttribute("taxon_constraint", b.getTaxonConstraint());
			if(b.getType() != null)
				entity.setAttribute("type", b.getType());
			if(b.getTaxonConstraint() != null)
				entity.setAttribute("taxon_constraint", b.getTaxonConstraint());
			for(edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Character c : 
				b.getCharacters()) {
				Element character = new Element("character");
				if(c.getCharType() != null)
					character.setAttribute("char_type", c.getCharType());
				if(c.getConstraint() != null)
					character.setAttribute("constraint", c.getConstraint());
				if(c.getConstraintId() != null)
					character.setAttribute("constraintid", c.getConstraintId());
				if(c.getEstablishedMeans() != null)
					character.setAttribute("establishment_means", c.getEstablishedMeans());
				if(c.getFrom() != null)
					character.setAttribute("from", c.getFrom());
				if(c.getFromInclusive() != null)
					character.setAttribute("from_inclusive", c.getFromInclusive());
				if(c.getFromModifier() != null)
					character.setAttribute("from_modifier", c.getFromModifier());
				if(c.getFromUnit() != null)
					character.setAttribute("from_unit", c.getFromUnit());
				if(c.getGeographicalConstraint() != null)
					character.setAttribute("geographical_constraint", c.getGeographicalConstraint());
				if(c.getInBrackets() != null)
					character.setAttribute("in_brackets", c.getInBrackets());
				if(c.getIsModifier() != null)
					character.setAttribute("is_modifier", c.getIsModifier());
				if(c.getModifier() != null)
					character.setAttribute("modifier", c.getModifier());
				if(c.getName() != null)
					character.setAttribute("name", c.getName());
				if(c.getNotes() != null)
					character.setAttribute("notes", c.getNotes());
				if(c.getOntologyId() != null)
					character.setAttribute("ontologyid", c.getOntologyId());
				if(c.getOrganConstraint() != null)
					character.setAttribute("organ_constraint", c.getOrganConstraint());
				if(c.getOtherConstraint() != null)
					character.setAttribute("other_constraint", c.getOtherConstraint());
				if(c.getParallelismConstraint() != null)
					character.setAttribute("parallelism_constraint", c.getParallelismConstraint());
				if(c.getProvenance() != null)
					character.setAttribute("provenance", c.getProvenance());
				if(c.getSrc() != null)
					character.setAttribute("src", c.getSrc());
				if(c.getTaxonConstraint() != null)
					character.setAttribute("taxon_constraint", c.getTaxonConstraint());
				if(c.getTo() != null)
					character.setAttribute("to", c.getTo());
				if(c.getToInclusive() != null)
					character.setAttribute("to_inclusive", c.getToInclusive());
				if(c.getToModifier() != null)
					character.setAttribute("to_modifier", c.getToModifier());
				if(c.getToUnit() != null)
					character.setAttribute("to_unit", c.getToUnit());
				if(c.getType() != null)
					character.setAttribute("type", c.getType());
				if(c.getUnit() != null)
					character.setAttribute("unit", c.getUnit());
				if(c.getUpperRestricted() != null)
					character.setAttribute("upper_restricted", c.getUpperRestricted());
				if(c.getValue() != null)
					character.setAttribute("value", c.getValue());
				entity.addContent(character);
			}
			statement.addContent(entity);
		}
		
		return statement;
	}

}
