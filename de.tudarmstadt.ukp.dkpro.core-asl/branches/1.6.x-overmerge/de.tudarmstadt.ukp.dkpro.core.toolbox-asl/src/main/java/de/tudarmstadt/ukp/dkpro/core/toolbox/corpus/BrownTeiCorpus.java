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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.toolbox.corpus;

import static de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase.INCLUDE_PREFIX;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;

import de.tudarmstadt.ukp.dkpro.core.api.resources.DkproContext;
import de.tudarmstadt.ukp.dkpro.core.io.tei.TeiReader;


/**
 * Tiger Corpus
 *
 * @author zesch
 *
 */
public class BrownTeiCorpus
    extends CorpusBase
{
    static final String LANGUAGE = "en";
    static final String NAME = "Brown";

    CollectionReaderDescription reader;

    public BrownTeiCorpus() throws Exception
    {
        String brownPath = DkproContext.getContext().getWorkspace("toolbox_corpora").getAbsolutePath() +
        "/brown_tei/";

        initialize(brownPath);
    }

    public BrownTeiCorpus(String brownPath) throws Exception
    {
        initialize(brownPath);
    }

    private void initialize(String brownPath) throws Exception {
        reader = CollectionReaderFactory.createReaderDescription(
                TeiReader.class,
                TeiReader.PARAM_LANGUAGE, LANGUAGE,
                TeiReader.PARAM_SOURCE_LOCATION, brownPath,
                TeiReader.PARAM_PATTERNS, new String[] {INCLUDE_PREFIX + "*.xml", INCLUDE_PREFIX + "*.xml.gz"}
        );
    }

    @Override
    protected CollectionReaderDescription getReader()
    {
        return reader;
    }

    @Override
    public String getLanguage()
    {
        return LANGUAGE;
    }

    @Override
    public String getName()
    {
        return NAME;
    }
}