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
 *******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.decompounding.uima.resource;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.uimafit.component.Resource_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;

import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.core.decompounding.dictionary.LinkingMorphemes;

public class SharedLinkingMorphemes
    extends Resource_ImplBase
{

    public static final String PARAM_MORPHEMES_PATH = "morphemesPath";
    public static final String DEFAULT_MORPHEMES_PATH = "/de/tudarmstadt/ukp/dkpro/core/decompounding/lib/de_DE.linking";
    @ConfigurationParameter(name = PARAM_MORPHEMES_PATH, mandatory = false, defaultValue = DEFAULT_MORPHEMES_PATH)
    private String morphemesPath;

    private LinkingMorphemes morphemes;

    @Override
    public void afterResourcesInitialized()
    {
        try {
            URL url = morphemesPath.equals(DEFAULT_MORPHEMES_PATH) ? getClass().getResource(
                    morphemesPath) : ResourceUtils.resolveLocation(new File(morphemesPath).toURI()
                    .toString(), this, null);

            morphemes = new LinkingMorphemes(url.openStream());
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public LinkingMorphemes getLinkingMorphemes()
    {
        return morphemes;
    }

}
