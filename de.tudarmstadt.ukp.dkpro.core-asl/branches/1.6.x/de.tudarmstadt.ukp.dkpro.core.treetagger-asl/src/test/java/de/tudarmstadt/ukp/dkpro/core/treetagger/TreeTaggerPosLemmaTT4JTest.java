/*******************************************************************************
 * Copyright 2010
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
package de.tudarmstadt.ukp.dkpro.core.treetagger;

import static org.apache.commons.lang.StringUtils.repeat;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.annolab.tt4j.TreeTaggerWrapper;
import org.apache.commons.lang.StringUtils;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.JCasBuilder;
import org.apache.uima.fit.testing.util.HideOutput;
import org.apache.uima.jcas.JCas;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;

public
class TreeTaggerPosLemmaTT4JTest
{
	@Before
	public void initTrace()
	{
		// TreeTaggerWrapper.TRACE = true;
	}

	@Test
	public void treeTaggerAnnotatorEnglishTest()
		throws Exception
	{
        String[] tagset = { "#", "$", "''", "(", ")", ",", ":", "CC", "CD", "DT", "EX", "FW", "IN",
                "IN/that", "JJ", "JJR", "JJS", "LS", "MD", "NN", "NNS", "NP", "NPS", "NS", "PDT",
                "POS", "PP", "PP$", "RB", "RBR", "RBS", "RP", "SENT", "SYM", "TO", "UH", "VB",
                "VBD", "VBG", "VBN", "VBP", "VBZ", "VH", "VHD", "VHG", "VHN", "VHP", "VHZ", "VV",
                "VVD", "VVG", "VVN", "VVP", "VVZ", "WDT", "WP", "WP$", "WRB", "``" };
	    
        runTest("en", "ptb-tt", tagset, "This is a test .",
				new String[] { "this", "be",  "a",   "test", "."    },
				new String[] { "DT",   "VBZ", "DT",  "NN",   "SENT" },
				new String[] { "ART",  "V",   "ART", "NN",   "PUNC" });

        runTest("en", "ptb-tt", tagset, "A neural net .",
        		new String[] { "a",   "neural", "net", "."    },
        		new String[] { "DT",  "JJ",     "NN",  "SENT" },
        		new String[] { "ART", "ADJ",    "NN",  "PUNC" });

        runTest("en", "ptb-tt", tagset, "John is purchasing oranges .",
        		new String[] { "John", "be",  "purchase", "orange", "."    },
        		new String[] { "NP",   "VBZ", "VVG",      "NNS",    "SENT" },
        		new String[] { "NP",   "V",   "V",        "NN",     "PUNC" });

        // TT4J per default runs TreeTagger with the -sgml option, so XML tags are not tagged
        runTest("en", "ptb-tt", tagset, "My homepage is <url> http://null.dummy </url> .",
        		new String[] { "my", "homepage", "be", "http://null.dummy", "." },
        		new String[] { "PP$", "NN", "VBZ", "JJ",  "SENT" },
        		new String[] { "PR",  "NN", "V",   "ADJ", "PUNC" });
	}

	@Test
	public void treeTaggerAnnotatorGermanTest()
		throws Exception
    {
        String[] tagset = { "$(", "$,", "$.", "ADJ", "ADJA", "ADJD", "ADV", "APPO", "APPR",
                "APPRART", "APZR", "ART", "CARD", "FM", "ITJ", "KOKOM", "KON", "KOUI", "KOUS",
                "NE", "NN", "PAV", "PDAT", "PDS", "PIAT", "PIS", "PPER", "PPOSAT", "PPOSS",
                "PRELAT", "PRELS", "PRF", "PTKA", "PTKANT", "PTKNEG", "PTKVZ", "PTKZU", "PWAT",
                "PWAV", "PWS", "TRUNC", "VAFIN", "VAIMP", "VAINF", "VAPP", "VMFIN", "VMINF",
                "VMPP", "VVFIN", "VVIMP", "VVINF", "VVIZU", "VVPP", "XY" };
	    
        runTest("de", "stts", tagset, "10 Minuten sind das Mikro an und die Bühne frei .",
        		new String[] { "10", "Minute", "sein", "die", "Mikro", "an", "und", "die", "Bühne", "frei", "."  },
        		new String[] { "CARD", "NN", "VAFIN", "ART", "NN", "PTKVZ", "KON",  "ART", "NN", "PTKVZ", "$."   },
        		new String[] { "CARD", "NN", "V",     "ART", "NN", "V",     "CONJ", "ART", "NN", "V",     "PUNC" });

        runTest("de", "stts", tagset, "Das ist ein Test .",
        		new String[] { "die", "sein",  "eine", "Test", "."   },
        		new String[] { "PDS", "VAFIN", "ART", "NN",   "$."   },
        		new String[] { "PR",  "V",     "ART", "NN",   "PUNC" });
    }

	@Test
	public void treeTaggerAnnotatorDutchTest()
		throws Exception
    {
        String[] tagset = { "$.", "adj", "adj*kop", "adjabbr", "adv", "advabbr", "conjcoord",
                "conjsubo", "det__art", "det__demo", "det__excl", "det__indef", "det__poss",
                "det__quest", "det__rel", "int", "noun*kop", "nounabbr", "nounpl", "nounprop",
                "nounsg", "num__card", "num__ord", "partte", "prep", "prepabbr", "pronadv",
                "prondemo", "pronindef", "pronpers", "pronposs", "pronquest", "pronrefl",
                "pronrel", "punc", "verbinf", "verbpapa", "verbpastpl", "verbpastsg", "verbpresp",
                "verbprespl", "verbpressg" };
	    
        runTest("nl", "tt", tagset, "Dit is een test .",
        		new String[] { "dit",      "zijn",       "een",      "test",   "."    },
        		new String[] { "prondemo", "verbpressg", "det__art", "nounsg", "$."   },
        		new String[] { "POS",      "POS",        "POS",      "POS",    "POS" });

        runTest("nl", "tt", tagset, "10 minuten op de microfoon en vrij podium .",
        		new String[] { "@card@",   "minuut", "op",   "de",       "microfoon", "en",        "vrij", "podium", "."   },
        		new String[] { "num__ord", "nounpl", "prep", "det__art", "nounsg",    "conjcoord", "adj",  "nounsg", "$."  },
        		new String[] { "POS",      "POS",    "POS",  "POS",      "POS",       "POS",       "POS",  "POS",    "POS" });
    }

	@Test
	@Ignore("Slovene model currently not in Artifactory because we do not know tagset yet")
	public void treeTaggerAnnotatorSloveneTest()
		throws Exception
    {
        String[] tagset = { };
        
        runTest("sl",  null, tagset, "To je test .",
        		new String[] { "ta",          "biti",      "test",  "." },
        		new String[] { "zk-sei----s", "gvpste--n", "somei", "SENT" },
        		new String[] { "POS",         "POS",       "POS",   "POS" });

        runTest("sl",  null, tagset, "Gremo na Češko za kosilo .",
        		new String[] { "iti",             "na",   "Češko", "za",   "kosilo", "." },
        		new String[] { "gppspm--n-----d", "dpet", "slmei", "dpet", "soset",  "SENT" },
        		new String[] { "POS",             "POS",  "POS",   "POS",  "POS",    "POS" });
    }

    @Test
    public
    void treeTaggerAnnotatorChineseTest()
    	throws Exception
    {
        String[] tagset = { "a", "ad", "ag", "an", "b", "bg", "c", "d", "dg", "e", "ew", "f", "g",
                "h", "i", "j", "k", "l", "m", "mg", "n", "nd", "ng", "nh", "ni", "nl", "nr", "ns",
                "nt", "nx", "nz", "o", "p", "q", "r", "rg", "s", "t", "tg", "u", "v", "vd", "vg",
                "vn", "w", "wp", "ws", "x", "y", "z" };
        
    	// The rudder often in the wake of the wind round the back of the area.
        runTest("zh", "lcmc", tagset, "尾 舵 常 处于 风轮 后面 的 尾流 区里 。",
        		new String[] { "_",  "_",  "_",   "_", "风轮", "_", "_", "_",  "_",  "_"    },
        		new String[] { "ng", "n",  "d",   "v", "n",   "f", "u", "n",  "nl", "ew"   },
        		new String[] { "O",  "NN", "ADV", "V", "NN",  "O", "O", "NN", "O",  "PUNC" } );

        // The service sector has become an important engine of Guangdong's economic transformation
        // and upgrading.
        runTest("zh", "lcmc", tagset, "服务业 成为 广东 经济 转型 升级 的 重要 引擎 。",
        		new String[] { "_",  "_", "_",  "_",  "_", "_", "_", "_", "_",  "_"     },
        		new String[] { "n",  "v", "ns", "n",  "v", "v", "u", "a", "n",  "ew"    },
        		new String[] { "NN", "V", "O",  "NN", "V", "V", "O", "O", "NN", "PUNC"  } );

        // How far is China from the world brand?
        runTest("zh", "lcmc", tagset, "中国 离 世界 技术 品牌 有 多远 ？",
        		new String[] { "_",  "_", "_",  "_",  "_",  "_", "多远", "_"  },
        		new String[] { "ns", "v", "n",  "n",  "n",  "v", "n",   "ew" },
        		new String[] { "O",  "V", "NN", "NN", "NN", "V", "NN",  "PUNC" } );
    }

	@Test
//	@Ignore("Platform specific")
	public void testOddCharacters()
		throws Exception
    {
        runTest("en", null, null, "² § ¶ § °",
        		new String[] { "²",  "§",    "¶",  "§",    "°"   },
        		new String[] { "NN", "SYM",  "NN", "SYM",  "SYM" },
        		new String[] { "NN", "PUNC", "NN", "PUNC", "PUNC"   });
    }

	/**
	 * Generate a very large document and test it.
	 */
	@Test
	public void hugeDocumentTest()
		throws Exception
	{
		// Start Java with -Xmx512m
		boolean run = Runtime.getRuntime().maxMemory() > (500000000);
		if (!run) {
			System.out.println("Test requires more heap than available, skipping");
		}
		Assume.assumeTrue(run);

		// Disable trace as this significantly slows down the test
		TreeTaggerWrapper.TRACE = false;

		String text = "This is a test .";
		int reps = 4000000 / text.length();
        String testString = repeat(text, " ", reps);

        JCas jcas = runTest("en", null, null, testString, null, null, null);
    	List<POS> actualTags = new ArrayList<POS>(select(jcas, POS.class));
        assertEquals(reps * 5, actualTags.size());

        // test POS annotations
		String[] expectedTags = new String[] { "DT",   "VBZ", "DT",  "NN",   "SENT" };
		String[] expectedTagClasses = new String[] { "ART",  "V",   "ART", "NN",   "PUNC" };

		for (int i = 0; i < actualTags.size(); i++) {
            POS posAnnotation = actualTags.get(i);
            assertEquals("In position "+i, expectedTagClasses[i%5], posAnnotation.getType().getShortName());
            assertEquals("In position "+i, expectedTags[i%5], posAnnotation.getPosValue());
		}

        System.out.println("Successfully tagged document with " + testString.length() +
        		" characters");
    }

	/**
	 * Test using the same AnalysisEngine multiple times.
	 */
	@Test
	public void multiDocumentTest()
		throws Exception
	{
    	checkModelsAndBinary("en");

		String testDocument = "This is a test .";
		String[] lemmas     = new String[] { "this", "be",  "a",   "test", "."    };
		String[] tags       = new String[] { "DT",   "VBZ", "DT",  "NN",   "SENT" };
		String[] tagClasses = new String[] { "ART",  "V",   "ART", "NN",   "PUNC" };

        AnalysisEngine engine = createEngine(TreeTaggerPosLemmaTT4J.class);

        HideOutput hideOut = new HideOutput();
		try {

			for (int n = 0; n < 100; n++) {
		        JCas aJCas = TestRunner.runTest(engine, "en", testDocument);

		        AssertAnnotations.assertPOS(tagClasses, tags, select(aJCas, POS.class));
		        AssertAnnotations.assertLemma(lemmas, select(aJCas, Lemma.class));
			}
		}
		finally {
			engine.destroy();
			hideOut.restoreOutput();
		}
    }

    /**
     * Run the {@link #hugeDocumentTest()} 100 times.
     */
    @Test
    @Ignore("This test takes a very long time. Only include it if you need to "+
    		"test the stability of the annotator")
	public void loadTest()
		throws Exception
	{
		for (int i = 0; i < 100; i++) {
			System.out.println("Load test iteration " + i);
			hugeDocumentTest();
		}
	}

	private void checkModelsAndBinary(String lang)
	{
		Assume.assumeTrue(getClass().getResource(
				"/de/tudarmstadt/ukp/dkpro/core/treetagger/lib/tagger-" + lang
						+ "-little-endian.par") != null);

		Assume.assumeTrue(getClass().getResource(
				"/de/tudarmstadt/ukp/dkpro/core/treetagger/bin/LICENSE.txt") != null);
	}

    private JCas runTest(String language, String tagsetName, String[] tagset, String testDocument,
            String[] lemmas, String[] tags, String[] tagClasses)
		throws Exception
	{
		checkModelsAndBinary(language);

        AnalysisEngine engine = createEngine(TreeTaggerPosLemmaTT4J.class,
        		TreeTaggerPosLemmaTT4J.PARAM_PRINT_TAGSET, true);

        JCas aJCas = TestRunner.runTest(engine, language, testDocument);

        AssertAnnotations.assertLemma(lemmas, select(aJCas, Lemma.class));
        AssertAnnotations.assertPOS(tagClasses, tags, select(aJCas, POS.class));
        if (tagset != null) {
            AssertAnnotations.assertTagset(POS.class, tagsetName, tagset, aJCas);        
        }

        return aJCas;
    }

	/**
	 * Test using the same AnalysisEngine multiple times.
	 */
	@Test
	public void longTokenTest()
		throws Exception
	{
    	checkModelsAndBinary("en");

        AnalysisEngine engine = createEngine(TreeTaggerPosLemmaTT4J.class);
        JCas jcas = engine.newJCas();

		try {
			for (int n = 99990; n < 100000; n ++) {
				System.out.println(n);
		        jcas.setDocumentLanguage("en");
		        JCasBuilder builder = new JCasBuilder(jcas);
		        builder.add("Start", Token.class);
		        builder.add("with", Token.class);
		        builder.add("good", Token.class);
		        builder.add("tokens", Token.class);
		        builder.add(".", Token.class);
		        builder.add(StringUtils.repeat("b", n), Token.class);
		        builder.add("End", Token.class);
		        builder.add("with", Token.class);
		        builder.add("some", Token.class);
		        builder.add("good", Token.class);
		        builder.add("tokens", Token.class);
		        builder.add(".", Token.class);
		        builder.close();
		        engine.process(jcas);
		        jcas.reset();
			}
		}
		finally {
			engine.destroy();
		}
    }

    /**
     * Runs a small pipeline on a text containing quite odd characters such as
     * Unicode LEFT-TO-RIGHT-MARKs. The BreakIteratorSegmenter creates tokens from these
     * which are send to TreeTagger as tokens containing line breaks or only
     * whitespace. TreeTaggerPosLemmaTT4J has to filter these tokens before
     * they reach the TreeTaggerWrapper.
     */
//    @Test
//    public
//    void testStrangeDocument()
//    throws Exception
//    {
//		CollectionReader reader = createReader(
//				FileSystemReader.class,
//				createTypeSystemDescription(),
//				FileSystemReader.PARAM_INPUTDIR, getTestResource(
//						"test_files/annotator/TreeTaggerPosLemmaTT4J/strange"));
//
//		AnalysisEngine sentenceSplitter = createEngine(
//				BreakIteratorSegmenter.class,
//				tsd);
//
//		AnalysisEngine tt = createEngine(TreeTaggerPosLemmaTT4J.class, tsd,
//				TreeTaggerTT4JBase.PARAM_LANGUAGE_CODE, "en");
//
//		runPipeline(reader, sentenceSplitter, tt);
//    }

//    @Test
//    @Ignore("This test should fail, however - due to fixes in the Tokenizer, " +
//    		"we can currently not provokate a failure with the given 'strange' " +
//    		"document.")
//    public
//    void testStrangeDocumentFail()
//    throws Exception
//    {
//		CollectionReader reader = createReader(
//				FileSystemReader.class,
//				createTypeSystemDescription(),
//				FileSystemReader.PARAM_INPUTDIR, getTestResource(
//						"test_files/annotator/TreeTaggerPosLemmaTT4J/strange"));
//
//		AnalysisEngine sentenceSplitter = createEngine(
//				BreakIteratorSegmenter.class,
//				tsd);
//
//		AnalysisEngine tt = createEngine(TreeTaggerPosLemmaTT4J.class, tsd,
//				TreeTaggerTT4JBase.PARAM_LANGUAGE_CODE, "en",
//				TreeTaggerTT4JBase.PARAM_PERFORMANCE_MODE, true);
//
//		runPipeline(
//				reader,
//				sentenceSplitter,
//				tt);
//    }

    /**
     * When running this test, check manually if TreeTagger is restarted
     * between the documents. If you jank up the log levels, that should be
     * visible on the console. Unfortunately we cannot easily access the
     * restartCount of the TreeTaggerWrapper.
     */
//    @Test
//    public
//    void testRealMultiDocument()
//    throws Exception
//    {
//		CollectionReader reader = createReader(
//				FileSystemReader.class,
//				createTypeSystemDescription(),
//				FileSystemReader.PARAM_INPUTDIR, getTestResource(
//						"test_files/annotator/TreeTaggerPosLemmaTT4J/multiDoc"));
//
//		AnalysisEngine sentenceSplitter = createEngine(
//				BreakIteratorSegmenter.class,
//				tsd);
//
//		AnalysisEngine tt = createEngine(TreeTaggerPosLemmaTT4J.class, tsd,
//				TreeTaggerTT4JBase.PARAM_LANGUAGE_CODE, "en");
//
//		runPipeline(
//				reader,
//				sentenceSplitter,
//				tt);
//    }

	/*
	 * Uncomment to test explicitly setting model/binary locations
	 */
//	@Test
//	public void testExplicitBinaryModel() throws Exception
//	{
//          AnalysisEngine tt = createEngine(TreeTaggerPosLemmaTT4J.class,
//                  TreeTaggerPosLemmaTT4J.PARAM_EXECUTABLE_PATH, 
//                  "/Applications/tree-tagger-MacOSX-3.2-intel/bin/tree-tagger",
//                  TreeTaggerPosLemmaTT4J.PARAM_MODEL_PATH,
//                  "/Applications/tree-tagger-MacOSX-3.2-intel/models/german-par-linux-3.2-utf8.bin",
//                  TreeTaggerPosLemmaTT4J.PARAM_MODEL_ENCODING, "UTF-8");
//          
//          JCas jcas = JCasFactory.createJCas();
//          jcas.setDocumentLanguage("de");
//
//          TokenBuilder<Token, Sentence> tb = new TokenBuilder<Token, Sentence>(Token.class,
//                  Sentence.class);
//          tb.buildTokens(jcas, "Dies ist ein test .");
//          
//          tt.process(jcas);
//	}

	@Rule
	public TestName name = new TestName();

	@Before
	public void printSeparator()
	{
		System.out.println("\n=== " + name.getMethodName() + " =====================");
	}
}
