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
package de.tudarmstadt.ukp.dkpro.core.snowball;

import static org.apache.commons.lang.StringUtils.isBlank;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeaturePath;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.tartarus.snowball.SnowballProgram;
import org.uimafit.descriptor.ConfigurationParameter;

import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathAnnotatorBase;
import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathException;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Stem;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * UIMA wrapper for the Snowball stemmer. Annotation types to be stemmed can beconfigured by a
 * {@link FeaturePath}.
 *
 * @see <a href="http://snowball.tartarus.org/">Snowball stemmer homepage</a>
 * @see FeaturePathAnnotatorBase
 * @author Benjamin Herbert
 * @author Richard Eckart de Castilho
 * @since 1.1.0
 */
public class SnowballStemmer
	extends FeaturePathAnnotatorBase
{
	public static final String MESSAGE_DIGEST = "de.tudarmstadt.ukp.dkpro.core.snowball.SnowballStemmer_Messages";
	private final static String SNOWBALL_PACKAGE = "org.tartarus.snowball.ext.";

	public static final String PARAM_LANGUAGE = "Language";
	@ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false)
	protected String language;

	public static final Map<String, String> languages = new HashMap<String, String>();
	static {
		languages.put("da", "Danish");
		languages.put("nl", "Dutch");
		languages.put("en", "English");
		languages.put("fi", "Finnish");
		languages.put("fr", "French");
		languages.put("de", "German");
		languages.put("hu", "Hungarian");
		languages.put("it", "Italian");
		languages.put("no", "Norwegian");
		languages.put("pt", "Portuguese");
		languages.put("ro", "Romanian");
		languages.put("ru", "Russian");
		languages.put("es", "Spanish");
		languages.put("sv", "Swedish");
		languages.put("tr", "Turkish");
	}

	private SnowballProgram snowballProgram;
	private String snowballProgramLanguage;

	@Override
	protected Set<String> getDefaultPaths()
	{
		return Collections.singleton(Token.class.getName());
	}

	@Override
	public void initialize(org.apache.uima.UimaContext aContext)
		throws org.apache.uima.resource.ResourceInitializationException
	{
		super.initialize(aContext);
	}

	@Override
	protected void generateAnnotations(JCas jcas)
		throws AnalysisEngineProcessException, FeaturePathException
	{
		// CAS is necessary to retrieve values
		CAS currCAS = jcas.getCas();

		for (String path : paths) {

			// Separate Typename and featurepath
			String[] segments = path.split("/", 2);
			String typeName = segments[0];

			// Try to get the type from the typesystem of the CAS
			Type t = currCAS.getTypeSystem().getType(typeName);
			if (t == null) {
				throw new IllegalStateException("Type [" + typeName + "] not found in type system");
			}

			// get an fpi object and initialize it
			// initialize the FeaturePathInfo with the corresponding part
			initializeFeaturePathInfoFrom(fp, segments);

			// get the annotations
			AnnotationIndex<?> idx = currCAS.getAnnotationIndex(t);
			FSIterator<?> iterator = idx.iterator();

			while (iterator.hasNext()) {
				AnnotationFS fs = (AnnotationFS) iterator.next();

				try {
					if (this.filterFeaturePath != null) {
						// check annotation filter condition
						if (this.filterFeaturePathInfo.match(fs, this.filterCondition)) {
							createStemAnnotation(jcas, fs);
						}
					}
					else { // no annotation filter specified
						createStemAnnotation(jcas, fs);
					}
				}
				catch (AnalysisEngineProcessException e) {
					// TODO Auto-generated catch block
					throw new IllegalStateException(
							"error occured while creating a stem annotation", e);
				}

			}

		}
	}

	private SnowballProgram getSnowballProgram(JCas aCas)
		throws AnalysisEngineProcessException
	{
		// Try language set on analysis engine
		String lang = language;
		if (isBlank(lang)) {
			lang = aCas.getDocumentLanguage();
		}

		// Try language set in CAS.
		if (isBlank(lang)) {
			throw new AnalysisEngineProcessException(MESSAGE_DIGEST, "no_language_error", null);
		}

		lang = lang.toLowerCase(Locale.US);

		if (!lang.equals(snowballProgramLanguage)) {
			try {
				String langPart = languages.get(lang);
				if (langPart == null) {
					throw new AnalysisEngineProcessException(MESSAGE_DIGEST,
							"unsupported_language_error", new Object[] { lang });
				}
				String snowballStemmerClass = SNOWBALL_PACKAGE + languages.get(lang) + "Stemmer";
				@SuppressWarnings("unchecked")
				Class<SnowballProgram> stemClass = (Class<SnowballProgram>) Class
						.forName(snowballStemmerClass);
				snowballProgram = stemClass.newInstance();
				snowballProgramLanguage = lang;
			}
			catch (Exception e) {
				throw new AnalysisEngineProcessException(e);
			}
		}

		return snowballProgram;
	}

	/**
	 * Creates a Stem annotation with same begin and end as the AnnotationFS fs, the value is the
	 * stemmed value derived by applying the featurepath.
	 *
	 * @param jcas
	 * @param fs
	 *            the AnnotationFS where the Stem annotation is created
	 * @throws AnalysisEngineProcessException
	 */
	private void createStemAnnotation(JCas jcas, AnnotationFS fs)
		throws AnalysisEngineProcessException
	{
		// Check for blank text, it makes no sense to add a stem then (and raised an exception)
		String value = fp.getValue(fs);
		if (!StringUtils.isBlank(value)) {
			Stem stemAnnot = new Stem(jcas, fs.getBegin(), fs.getEnd());
			SnowballProgram programm = getSnowballProgram(jcas);
			programm.setCurrent(value);
			programm.stem();
			stemAnnot.setValue(programm.getCurrent());
			stemAnnot.addToIndexes(jcas);

			// Try setting the "stem" feature on Tokens.
			Feature feat = fs.getType().getFeatureByBaseName("stem");
			if (feat != null && feat.getRange() != null
					&& jcas.getTypeSystem().subsumes(feat.getRange(), stemAnnot.getType())) {
				fs.setFeatureValue(feat, stemAnnot);
			}
		}
	}
}
