/*******************************************************************************
 * Copyright 2012
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.opennlp;

import static org.uimafit.factory.AnalysisEngineFactory.createAggregateDescription;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitive;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.uimafit.util.JCasUtil.select;
import static org.uimafit.util.JCasUtil.selectSingle;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;

public class OpenNlpParserTest
{
	static final String documentEnglish = "We need a very complicated example sentence, which " +
			"contains as many constituents and dependencies as possible.";
	
	@Test
	public void testEnglishChunking()
		throws Exception
	{
		JCas jcas = runTest("en", "chunking", documentEnglish);
		
		String[] constituentMapped = new String[] { "ADJP 10,26", "ADJP 101,109", "NP 0,2", 
				"NP 63,109", "NP 63,97", "NP 8,109", "NP 8,43", "PP 60,109", "PP 98,109", 
				"ROOT 0,110", "S 0,110", "S 51,109", "SBAR 45,109", "VP 3,109", "VP 51,109", 
				"WHNP 45,50" };

		String[] constituentOriginal = new String[] { "ADJP 10,26", "ADJP 101,109", "NP 0,2", 
				"NP 63,109", "NP 63,97", "NP 8,109", "NP 8,43", "PP 60,109", "PP 98,109", 
				"ROOT 0,110", "S 0,110", "S 51,109", "SBAR 45,109", "VP 3,109", "VP 51,109", 
				"WHNP 45,50" };

		String[] posMapped = new String[] { "PR", "V", "ART", "ADV", "V", "NN", "NN", "PUNC",
				"ART", "V", "PP", "ADJ", "NN", "CONJ", "NN", "PP", "ADJ", "PUNC" };

		String[] posOriginal = new String[] { "PRP", "VBP", "DT", "RB", "VBN", "NN", 
				"NN", ",", "WDT", "VBZ", "IN", "JJ", "NNS", "CC",
				"NNS", "IN", "JJ", "." };

		String pennTree = "(ROOT (S (NP (PRP We)) (VP (VBP need) (NP (NP (DT a) (ADJP (RB very) " +
				"(VBN complicated)) (NN example) (NN sentence))(, ,) (SBAR (WHNP (WDT which)) " +
				"(S (VP (VBZ contains) (PP (IN as) (NP (NP (JJ many) (NNS constituents) (CC and) " +
				"(NNS dependencies)) (PP (IN as) (ADJP (JJ possible))))))))))(. .)))";

		AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
		AssertAnnotations.assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
		AssertAnnotations.assertConstituents(constituentMapped, constituentOriginal, select(jcas, Constituent.class));
	}

	/**
	 * Setup CAS to test parser for the English language (is only called once if
	 * an English test is run)
	 */
	private JCas runTest(String aLanguage, String aVariant, String aText)
		throws Exception
	{
		AnalysisEngineDescription segmenter = createPrimitiveDescription(OpenNlpSegmenter.class);

		// setup English
		AnalysisEngineDescription parser = createPrimitiveDescription(OpenNlpParser.class,
				OpenNlpParser.PARAM_VARIANT, aVariant,
				OpenNlpParser.PARAM_PRINT_TAGSET, true,
				OpenNlpParser.PARAM_CREATE_PENN_TREE_STRING, true);

		AnalysisEngineDescription aggregate = createAggregateDescription(segmenter, parser);
		
		AnalysisEngine engine = createPrimitive(aggregate);
		JCas jcas = engine.newJCas();
		jcas.setDocumentLanguage(aLanguage);
		jcas.setDocumentText(aText);
		engine.process(jcas);
		
		return jcas;
	}
	
	@Rule
	public TestName name = new TestName();

	@Before
	public void printSeparator()
	{
		System.out.println("\n=== " + name.getMethodName() + " =====================");
	}
}
