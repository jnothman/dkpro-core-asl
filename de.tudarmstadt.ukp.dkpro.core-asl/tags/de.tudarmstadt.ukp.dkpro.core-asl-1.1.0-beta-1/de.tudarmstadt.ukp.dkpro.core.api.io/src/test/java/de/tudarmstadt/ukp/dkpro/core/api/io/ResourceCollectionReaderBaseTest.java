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
package de.tudarmstadt.ukp.dkpro.core.api.io;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.uimafit.factory.CollectionReaderFactory.createCollectionReader;
import static org.uimafit.factory.TypeSystemDescriptionFactory.createTypeSystemDescription;

import java.io.IOException;

import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.util.CasCreationUtils;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;


public class ResourceCollectionReaderBaseTest
{
	@Test
	public void testClasspath() throws Exception
	{
		CollectionReader reader = createCollectionReader(DummyReader.class, createTypeSystemDescription(),
				ResourceCollectionReaderBase.PARAM_PATH, "classpath*:/de/tudarmstadt/ukp/",
				ResourceCollectionReaderBase.PARAM_PATTERNS, new String[] {
					"[+]**/FileSetCollectionReaderBase.class",
					"[-]**/ResourceCollectionReaderBase.class"});

		searchForResourceCollectionReaderBase(reader);
	}

	@Test
	public void testFile() throws Exception
	{
		CollectionReader reader = createCollectionReader(DummyReader.class, createTypeSystemDescription(),
				ResourceCollectionReaderBase.PARAM_PATH, "file:src/main/java/de/tudarmstadt/ukp/",
				ResourceCollectionReaderBase.PARAM_PATTERNS, new String[] {
					"[+]**/FileSetCollectionReaderBase.java",
					"[-]**/ResourceCollectionReaderBase.java"});

		searchForResourceCollectionReaderBase(reader);
	}

	public void searchForResourceCollectionReaderBase(CollectionReader aReader)
		throws Exception
	{
		String goodNeedle = "FileSetCollectionReaderBase";
		String badNeedle = "ResourceCollectionReaderBase";

		boolean found = false;
		CAS cas = CasCreationUtils.createCas(aReader.getProcessingResourceMetaData());
		while (aReader.hasNext()) {
			aReader.getNext(cas);
			DocumentMetaData meta = DocumentMetaData.get(cas);
			if (meta.getDocumentUri().contains(goodNeedle)) {
				found = true;
				break;
			}
			if (meta.getDocumentUri().contains(badNeedle)) {
				fail("Bad needle ["+badNeedle+"] found even though it is excluded...");
			}
			cas.reset();
		}
		cas.release();

		assertTrue("Good needle ["+goodNeedle+"] not found...", found);
	}


	public static final class DummyReader extends ResourceCollectionReaderBase
	{
		@Override
		public void getNext(CAS aCAS)
			throws IOException, CollectionException
		{
			Resource res = nextFile();
			initCas(aCAS, res);
		}

		@Override
		public void close()
			throws IOException
		{
			// Ignore
		}
	}
}
