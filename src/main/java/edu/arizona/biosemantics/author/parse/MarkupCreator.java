package edu.arizona.biosemantics.author.parse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

import edu.arizona.biosemantics.common.biology.TaxonGroup;
import edu.arizona.biosemantics.common.ling.know.IGlossary;
import edu.arizona.biosemantics.common.ling.know.Term;
import edu.arizona.biosemantics.common.ling.know.lib.InMemoryGlossary;
import edu.arizona.biosemantics.common.ling.pos.IPOSTagger;
import edu.arizona.biosemantics.common.ling.transform.IInflector;
import edu.arizona.biosemantics.common.ling.transform.lib.WhitespaceTokenizer;
import edu.arizona.biosemantics.oto.client.oto.OTOClient;
import edu.arizona.biosemantics.oto.model.GlossaryDownload;
import edu.arizona.biosemantics.oto.model.TermCategory;
import edu.arizona.biosemantics.oto.model.TermSynonym;
import edu.arizona.biosemantics.oto2.oto.shared.model.Collection;
import edu.arizona.biosemantics.oto2.oto.shared.model.Label;
import edu.arizona.biosemantics.semanticmarkup.config.Configuration;
import edu.arizona.biosemantics.semanticmarkup.config.taxongroup.PlantConfig;
import edu.arizona.biosemantics.semanticmarkup.ling.chunk.lib.CharaparserChunkerChain;
import edu.arizona.biosemantics.semanticmarkup.ling.know.lib.ElementRelationGroup;
import edu.arizona.biosemantics.semanticmarkup.ling.normalize.INormalizer;
import edu.arizona.biosemantics.semanticmarkup.ling.parse.IParser;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.io.ParentTagProvider;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.extract.IDescriptionExtractor;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.ITerminologyLearner;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.ling.learn.lib.NoTerminologyLearner;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.AbstractDescriptionsFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Description;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.DescriptionsFile;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.transform.SentenceChunkerRun;
import edu.arizona.biosemantics.semanticmarkup.markupelement.description.transform.TransformationException;

@Component
public class MarkupCreator {

	private Logger logger = LoggerFactory.getLogger(MarkupCreator.class);
	private WhitespaceTokenizer tokenizer;
	private IPOSTagger posTagger;
	private IParser parser;
	private CharaparserChunkerChain chunkerChain;
	private INormalizer normalizer;
	private Injector injector;
	private ParentTagProvider parentTagProvider;

	public MarkupCreator() throws Exception {
		PlantConfig config = new PlantConfig();
		config.setGlossary(InMemoryGlossary.class);
		config.setTerminologyLearner(GlossaryBasedTerminologyLearner.class);
		this.injector = Guice.createInjector(config);
		IInflector inflector = injector.getInstance(IInflector.class);
		this.normalizer = injector.getInstance(INormalizer.class);
		this.parentTagProvider = injector.getInstance(Key.get(ParentTagProvider.class, Names.named("ParentTagProvider")));
		IGlossary glossary = injector.getInstance(IGlossary.class);
		//OTOClient otoClient = injector.getInstance(OTOClient.class);

		GlossaryDownload glossaryDownload = this.getGlossaryDownload(glossary, /*otoClient,*/ TaxonGroup.PLANT);
		this.initGlossary(glossaryDownload, new Collection(), glossary, inflector);
		ITerminologyLearner terminologyLearner= injector.getInstance(ITerminologyLearner.class);
		terminologyLearner.readResults(new ArrayList<AbstractDescriptionsFile>());

		this.tokenizer = new WhitespaceTokenizer();
		this.posTagger = injector.getInstance(IPOSTagger.class);
		this.parser = injector.getInstance(IParser.class);
		this.chunkerChain = injector.getInstance(CharaparserChunkerChain.class);

	}

	public IDescriptionExtractor createDescriptionExtractor() throws IOException {
		return injector.getInstance(IDescriptionExtractor.class);
	}

	public SentenceChunkerRun createChunkerRun(String sentence, String source) {
		HashMap<String, String> grandParentTags = new HashMap<String, String>();
		HashMap<String, String> parentTags = new HashMap<String, String>();
		parentTags.put(source, "");
		grandParentTags.put(source, "");
		parentTagProvider.init(parentTags, grandParentTags);
		normalizer.init();
		Hashtable<String, String> prevMissingOrgan = new Hashtable<String, String>();
		prevMissingOrgan.put("source", source);
		CountDownLatch sentencesLatch = new CountDownLatch(1);

		DescriptionsFile descriptionsFile = new DescriptionsFile();
		descriptionsFile.setFile(new File(""));

		Description description = new Description();

		String sentenceArgument = "a" + "##" + "b" + "##" + sentence + "##" + sentence;
		/*
		 * String[] sentenceArray = sentenceString.split("##"); String originalSent =
		 * sentenceArray[3]; sentenceString = sentenceArray[2]; String subjectTag =
		 * sentenceArray[1]; //TODO: Hong stop using subjectTag and modifier. Pause.
		 * Still used in fixInner. String modifier = sentenceArray[0];
		 */
		return new SentenceChunkerRun(source, sentenceArgument, description, descriptionsFile, normalizer, tokenizer,
				posTagger, parser, chunkerChain, prevMissingOrgan, sentencesLatch);
	}

	private GlossaryDownload getGlossaryDownload(IGlossary glossary, /*OTOClient otoClient,*/ TaxonGroup taxonGroup)
			throws TransformationException {

		GlossaryDownload glossaryDownload = new GlossaryDownload();
		//remove dependency on OTO, use local version
		String glossaryVersion = "latest";
		/*boolean downloadSuccessful = false;
		otoClient.open();
		Future<GlossaryDownload> futureGlossaryDownload = otoClient.getGlossaryDownload(taxonGroup.getDisplayName(),
				glossaryVersion);*/

		try {
			/*glossaryDownload = futureGlossaryDownload.get();

			downloadSuccessful = glossaryDownload != null
					&& !glossaryDownload.getVersion().equals("Requested version not available")
					&& !glossaryDownload.getVersion().equals("No Glossary Available")
					&& !glossaryDownload.getVersion().contains("available")
					&& !glossaryDownload.getVersion().contains("Available");
			if (!downloadSuccessful)*/
				glossaryDownload = getLocalGlossaryDownload(taxonGroup);
		} catch (Exception e) {
			//otoClient.close();
			logger.error("Couldn't download glossary " + taxonGroup.getDisplayName() + " version: " + glossaryVersion,
					e);
			throw new TransformationException();
		}
		//otoClient.close();
		return glossaryDownload;

	}

	private GlossaryDownload getLocalGlossaryDownload(TaxonGroup taxonGroup)
			throws FileNotFoundException, IOException, ClassNotFoundException {
		ObjectInputStream objectIn = new ObjectInputStream(new FileInputStream(Configuration.glossariesDownloadDirectory
				+ File.separator + "GlossaryDownload." + taxonGroup.getDisplayName() + ".ser"));
		GlossaryDownload glossaryDownload = (GlossaryDownload) objectIn.readObject();
		objectIn.close();
		return glossaryDownload;
	}

	/**
	 *
	 * Merge glossaryDownload and collection to one glossary which holds both terms
	 * and synonyms note: decisions from collection (results from term review by the
	 * user) takes priority over those from glossary note: synonyms and terms are
	 * disjoint -- add only term as entry to the glossary (addEntry), synonyms added
	 * as synonyms (addSynonym) For structure terms, both singular and plural forms
	 * are included in the synonyms
	 * 
	 * @param glossaryDownload
	 * @param collection
	 */
	protected void initGlossary(GlossaryDownload glossaryDownload, Collection collection, IGlossary glossary,
			IInflector inflector) {
		logger.debug("initiate in-memory glossary using glossaryDownload and collection...");
		logger.debug("obtaining synonyms from glossaryDownload...");
		// 1. obtain synonyms from glossaryDownload
		HashSet<Term> gsyns = new HashSet<Term>();
		obtainSynonymsFromGlossaryDownload(glossaryDownload, gsyns, inflector);

		logger.debug("obtaining synonyms from collection...");
		// 2. obtain synonyms from collection
		HashSet<Term> dsyns = new HashSet<Term>();
		obtainSynonymsFromCollection(collection, dsyns, inflector);

		logger.debug("merging synonyms...");
		// 3. merge synonyms into one set
		gsyns = mergeSynonyms(gsyns, dsyns);

		logger.debug("adding synonyms to in-mem glossary...");
		// 4. addSynonyms to glossary
		HashSet<Term> simpleSyns = addSynonyms2Glossary(glossary, gsyns);

		logger.debug("adding preferred terms to in-mem glossary...");
		// 5. addEntry
		// the glossaryDownload, excluding syns
		for (TermCategory termCategory : glossaryDownload.getTermCategories()) {
			if (!simpleSyns.contains(new Term(termCategory.getTerm().replaceAll("_", "-"), termCategory.getCategory())))
				glossary.addEntry(termCategory.getTerm().replaceAll("_", "-"), termCategory.getCategory()); // primocane_foliage
																											// =>primocane-foliage
																											// Hong
																											// 3/2014
			else
				logger.debug("synonym not add to in-mem glossary: " + termCategory.getTerm().replaceAll("_", "-") + "<"
						+ termCategory.getCategory() + ">");
		}

		// the collection, excluding syns
		if (collection != null) {
			for (Label label : collection.getLabels()) {
				for (edu.arizona.biosemantics.oto2.oto.shared.model.Term mainTerm : label.getMainTerms()) {
					if (!simpleSyns.contains(new Term(mainTerm.getTerm().replaceAll("_", "-"), label.getName()))) {// calyx_tube
																													// =>
																													// calyx-tube
						glossary.addEntry(mainTerm.getTerm().replaceAll("_", "-"), label.getName());
						logger.debug("adding collection term to in-mem glossary: "
								+ mainTerm.getTerm().replaceAll("_", "-") + "<" + label.getName() + ">");
					} else
						logger.debug("synonym not add to in-mem glossary: " + mainTerm.getTerm().replaceAll("_", "-")
								+ "<" + label.getName() + ">");
				}
			}
		}
	}

	/**
	 *
	 * @param gsyns
	 * @return set of synonyms added (minus the preferred terms)
	 */
	private HashSet<Term> addSynonyms2Glossary(IGlossary glossary, HashSet<Term> gsyns) {
		HashSet<Term> simpleSyns = new HashSet<Term>();
		Iterator<Term> sit = gsyns.iterator();
		while (sit.hasNext()) {
			Term syn = sit.next();
			String[] tokens = syn.getLabel().split(":");
			String category = syn.getCategory();
			glossary.addSynonym(tokens[0], category, tokens[1]);
			logger.debug("adding synonym to in-mem glossary: " + tokens[0] + " U " + tokens[1] + "<" + category + ">");
			simpleSyns.add(new Term(tokens[0], category));
		}
		return simpleSyns;
	}

	/**
	 * Term string takes the form of "syn:preferred"
	 * 
	 * @param gsyns
	 * @param dsyns
	 * @return
	 */
	private HashSet<Term> mergeSynonyms(HashSet<Term> gsyns, HashSet<Term> dsyns) {
		HashSet<Term> merged = new HashSet<Term>();
		Iterator<Term> git = gsyns.iterator();
		while (git.hasNext()) {
			Iterator<Term> dit = dsyns.iterator();
			Term gsyn = git.next();
			String gcat = gsyn.getCategory();
			List<String> gtokens = Arrays.asList(gsyn.getLabel().split(":"));
			while (dit.hasNext()) { // nested loop, very inefficient
				Term dsyn = dit.next();
				String dcat = dsyn.getCategory();
				List<String> dtokens = Arrays.asList(dsyn.getLabel().split(":"));
				if (!gcat.equals(dcat)) {
					// add both to merged
					merged.add(gsyn);
					logger.debug("add to merged synonyms: " + gsyn.toString());
					merged.add(dsyn);
					logger.debug("add to merged synonyms: " + dsyn.toString());
				} else {
					boolean isSame = false; // all four terms are synonyms
					for (String t : gtokens) {
						if (dtokens.contains(t))
							isSame = true;
					}
					if (isSame) {
						// use preferred term of dsyns as the preferred term
						if (dtokens.get(1).equals(gtokens.get(1))) {// share the same preferred term,
							// add both to merged SET
							merged.add(gsyn);
							logger.debug("add to merged synonyms: " + gsyn.toString());
							merged.add(dsyn);
							logger.debug("add to merged synonyms: " + dsyn.toString());
						} else {
							merged.add(dsyn);
							if (!gtokens.get(0).equals(dtokens.get(1))) { // don't add B:B
								merged.add(new Term(gtokens.get(0) + ":" + dtokens.get(1), dcat));
								logger.debug("add to merged synonyms: "
										+ new Term(gtokens.get(0) + ":" + dtokens.get(1), dcat).toString());
							}
							if (!gtokens.get(1).equals(dtokens.get(1))) {
								merged.add(new Term(gtokens.get(1) + ":" + dtokens.get(1), dcat));
								logger.debug("add to merged synonyms: "
										+ new Term(gtokens.get(1) + ":" + dtokens.get(1), dcat).toString());
							}

						}
					} else {
						// add both to merged
						merged.add(gsyn);
						logger.debug("add to merged synonyms: " + gsyn.toString());
						merged.add(dsyn);
						logger.debug("add to merged synonyms: " + dsyn.toString());
					}
				}
			}
		}
		return merged;
	}

	private void obtainSynonymsFromCollection(Collection collection, HashSet<Term> dsyns, IInflector inflector) {
		if (collection != null) {
			for (Label label : collection.getLabels()) {
				for (edu.arizona.biosemantics.oto2.oto.shared.model.Term mainTerm : label.getMainTerms()) {
					// if(!dsyns.contains(new Term(mainTerm.getTerm().replaceAll("_", "-"),
					// label.getName())))//calyx_tube => calyx-tube
					// glossary.addEntry(mainTerm.getTerm().replaceAll("_", "-"), label.getName());

					// Hong TODO need to add category info to synonym entry in OTOLite
					// if(termSyn.getCategory().compareTo("structure")==0){
					if (label.getName().matches(ElementRelationGroup.entityElements)) {
						for (edu.arizona.biosemantics.oto2.oto.shared.model.Term synonym : label
								.getSynonyms(mainTerm)) {
							// take care of singular and plural forms
							String syns = "";
							String synp = "";
							String terms = "";
							String termp = "";
							if (inflector.isPlural(synonym.getTerm().replaceAll("_", "-"))) {
								synp = synonym.getTerm().replaceAll("_", "-");
								syns = inflector.getSingular(synp);
							} else {
								syns = synonym.getTerm().replaceAll("_", "-");
								synp = inflector.getPlural(syns);
							}

							if (inflector.isPlural(mainTerm.getTerm().replaceAll("_", "-"))) {
								termp = mainTerm.getTerm().replaceAll("_", "-");
								terms = inflector.getSingular(termp);
							} else {
								terms = mainTerm.getTerm().replaceAll("_", "-");
								termp = inflector.getPlural(terms);
							}
							// plural forms are synonyms to the singular
							if (!syns.equals(terms)) {
								dsyns.add(new Term(syns + ":" + terms, label.getName()));
								logger.debug("synonym from collection: "
										+ new Term(syns + ":" + terms, label.getName()).toString());
							}
							if (!synp.equals(terms)) {
								dsyns.add(new Term(synp + ":" + terms, label.getName()));
								logger.debug("synonym from collection: "
										+ new Term(synp + ":" + terms, label.getName()).toString());
							}
							if (!termp.equals(terms)) {
								dsyns.add(new Term(termp + ":" + terms, label.getName()));
								logger.debug("synonym from collection: "
										+ new Term(termp + ":" + terms, label.getName()).toString());
							}
						}
					} else {// forking_1 and forking are syns 5/5/14 hong test, shouldn't _1 have already
							// been removed?
						for (edu.arizona.biosemantics.oto2.oto.shared.model.Term synonym : label
								.getSynonyms(mainTerm)) {
							// glossary.addSynonym(synonym.getTerm().replaceAll("_", "-"), label.getName(),
							// mainTerm.getTerm());
							dsyns.add(new Term(synonym.getTerm().replaceAll("_", "-") + ":"
									+ mainTerm.getTerm().replaceAll("_", "-"), label.getName()));
							logger.debug("synonym from collection: "
									+ new Term(synonym.getTerm().replaceAll("_", "-") + ":" + mainTerm.getTerm(),
											label.getName()).toString());
						}
					}
				}
			}
		}
	}

	private void obtainSynonymsFromGlossaryDownload(GlossaryDownload glossaryDownload, HashSet<Term> gsyns,
			IInflector inflector) {
		for (TermSynonym termSyn : glossaryDownload.getTermSynonyms()) {

			// if(termSyn.getCategory().compareTo("structure")==0){
			if (termSyn.getCategory().matches(ElementRelationGroup.entityElements)) {
				// take care of singular and plural forms
				String syns = "";
				String synp = "";
				String terms = "";
				String termp = "";
				if (inflector.isPlural(termSyn.getSynonym().replaceAll("_", "-"))) { // must convert _ to -, as matching
																						// entity phrases will be
																						// converted from leg iii to
																						// leg-iii in the sentence.
					synp = termSyn.getSynonym().replaceAll("_", "-");
					syns = inflector.getSingular(synp);
				} else {
					syns = termSyn.getSynonym().replaceAll("_", "-");
					synp = inflector.getPlural(syns);
				}

				if (inflector.isPlural(termSyn.getTerm().replaceAll("_", "-"))) {
					termp = termSyn.getTerm().replaceAll("_", "-");
					terms = inflector.getSingular(termp);
				} else {
					terms = termSyn.getTerm().replaceAll("_", "-");
					termp = inflector.getPlural(terms);
				}
				// plural forms are synonyms to the singular
				if (!syns.equals(terms)) {
					gsyns.add(new Term(syns + ":" + terms, termSyn.getCategory()));
					logger.debug("synonym from glossaryDownload: "
							+ new Term(syns + ":" + terms, termSyn.getCategory()).toString());
				}
				if (!synp.equals(terms)) {
					gsyns.add(new Term(synp + ":" + terms, termSyn.getCategory()));
					logger.debug("synonym from glossaryDownload: "
							+ new Term(synp + ":" + terms, termSyn.getCategory()).toString());
				}
				if (!termp.equals(terms)) {
					gsyns.add(new Term(termp + ":" + terms, termSyn.getCategory()));
					logger.debug("synonym from glossaryDownload: "
							+ new Term(termp + ":" + terms, termSyn.getCategory()).toString());
				}
			} else {
				// glossary.addSynonym(termSyn.getSynonym().replaceAll("_", "-"),
				// termSyn.getCategory(), termSyn.getTerm());
				gsyns.add(new Term(
						termSyn.getSynonym().replaceAll("_", "-") + ":" + termSyn.getTerm().replaceAll("_", "-"),
						termSyn.getCategory()));
				logger.debug("synonym from glossaryDownload: "
						+ new Term(termSyn.getSynonym().replaceAll("_", "-") + ":" + termSyn.getTerm(),
								termSyn.getCategory()).toString());
			}
		}
	}

}
