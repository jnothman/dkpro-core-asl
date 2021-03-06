/*******************************************************************************
 * Copyright 2011
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
 ******************************************************************************/package de.tudarmstadt.ukp.dkpro.core.io.imscwb;

import org.junit.Test;
import org.uimafit.component.xwriter.CASDumpWriter;

import static org.uimafit.factory.CollectionReaderFactory.*;
import static org.uimafit.factory.AnalysisEngineFactory.*;
import static org.uimafit.pipeline.SimplePipeline.*;

import org.apache.uima.collection.CollectionReader;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;

import de.tudarmstadt.ukp.dkpro.core.io.negra.NegraExportReader;
import de.tudarmstadt.ukp.dkpro.core.treetagger.TreeTaggerPosLemmaTT4J;

/**
 *
 * @author Erik-Lân Do Dinh
 *
 */
public class ImsCwbWriterTest
{
	private static final String outputFile = "target/corpus-sample.ims.xml";

	@Test
	public void test1()
		throws Exception
	{
		CollectionReader ner = createCollectionReader(
				NegraExportReader.class,
				NegraExportReader.PARAM_INPUT_FILE, "src/test/resources/corpus-sample.export",
				NegraExportReader.PARAM_LANGUAGE, "de",
				NegraExportReader.PARAM_ENCODING, "UTF-8");

		AnalysisEngineDescription tag = createPrimitiveDescription(
				TreeTaggerPosLemmaTT4J.class,
				TreeTaggerPosLemmaTT4J.PARAM_LANGUAGE_CODE, "de");

		AnalysisEngineDescription tw = createPrimitiveDescription(
				ImsCwbWriter.class,
				ImsCwbWriter.PARAM_OUTPUT_FILE, outputFile,
				ImsCwbWriter.PARAM_ENCODING, "UTF-8");

		AnalysisEngineDescription cdw = createPrimitiveDescription(
				CASDumpWriter.class,
				CASDumpWriter.PARAM_OUTPUT_FILE, "target/dump.txt");

		runPipeline(ner, tag, tw);
	}
}
