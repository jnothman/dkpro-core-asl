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
package de.tudarmstadt.ukp.dkpro.core.io.text;

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasFileWriter_ImplBase;

/**
 * UIMA CAS consumer writing the CAS document text as plain text file.
 *
 * @author Richard Eckart de Castilho
 */
public class TextWriter
extends JCasFileWriter_ImplBase
{
	@Override
	public void process(JCas aJCas)
		throws AnalysisEngineProcessException
	{
		OutputStream docOS = null;
		try {
			docOS = getOutputStream(aJCas, ".txt");
			
			IOUtils.write(aJCas.getDocumentText(), docOS);
		}
		catch (Exception e) {
			throw new AnalysisEngineProcessException(e);
		}
		finally {
			closeQuietly(docOS);
		}
	}
}
