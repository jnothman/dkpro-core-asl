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

import static de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils.getUrlAsFile;
import static java.io.File.separator;
import static org.annolab.tt4j.Util.getSearchPaths;
import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.annolab.tt4j.DefaultExecutableResolver;
import org.annolab.tt4j.DefaultModel;
import org.annolab.tt4j.DefaultModelResolver;
import org.annolab.tt4j.Model;
import org.annolab.tt4j.ModelResolver;
import org.annolab.tt4j.TokenAdapter;
import org.annolab.tt4j.TreeTaggerWrapper;
import org.apache.commons.io.FilenameUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.uimafit.component.CasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;


/**
 * @author Richard Eckart de Castilho
 *
 * @param <T>
 */
public abstract class TreeTaggerTT4JBase<T>
	extends CasAnnotator_ImplBase
{
    public static final String RESOURCE_TREETAGGER = "TreeTagger";
	protected TreeTaggerWrapper<T> treetagger;

    public static final String PARAM_LANGUAGE_CODE = "LanguageCode";
	@ConfigurationParameter(name=PARAM_LANGUAGE_CODE, mandatory=false)
	protected String languageCode;

    public static final String PARAM_PERFORMANCE_MODE = "PerformanceMode";
	@ConfigurationParameter(name=PARAM_PERFORMANCE_MODE, mandatory=true, defaultValue="false")
	private boolean performanceMode;

    public static final String PARAM_EXECUTABLE_PATH = "ExecutablePath";
	@ConfigurationParameter(name=PARAM_EXECUTABLE_PATH, mandatory=false)
	private File executablePath;

    public static final String PARAM_MODEL_PATH = "ModelPath";
	@ConfigurationParameter(name=PARAM_MODEL_PATH, mandatory=false)
	protected File modelPath;

    public static final String PARAM_MODEL_ENCODING = "ModelEncoding";
	@ConfigurationParameter(name=PARAM_MODEL_ENCODING, mandatory=false)
	protected String modelEncoding;

    public static final String PARAM_TAG_MAPPING_PATH = "TagMappingPath";
	@ConfigurationParameter(name=PARAM_TAG_MAPPING_PATH, mandatory=false)
	protected File tagMappingPath;

	private Set<String> missingTags;

	@Override
	public void initialize(UimaContext aContext)
		throws ResourceInitializationException
	{
		super.initialize(aContext);

		try {
			if (modelPath != null && modelEncoding == null) {
				throw new IllegalArgumentException("When specifying a model file, the model " +
						"encoding has to be specified as well.");
			}
			if (modelPath != null && tagMappingPath == null) {
				throw new IllegalArgumentException("When specifying a model, the tag mapping " +
						"properties file has to be specified as well.");
			}

			missingTags = new HashSet<String>();

			treetagger = new TreeTaggerWrapper<T>();

			// Set the adapter extracting the text from the UIMA token
			treetagger.setAdapter(getAdapter());

			treetagger.setExecutableProvider(new DKProExecutableResolver());
			treetagger.setModelProvider(getModelResolver());
			treetagger.setPerformanceMode(performanceMode);
		}
		catch (ResourceInitializationException e) {
			throw e;
		}
		catch (Exception e) {
			throw new ResourceInitializationException(e);
		}
	}

	protected boolean isPerformanceMode()
	{
		return performanceMode;
	}

	protected abstract TokenAdapter<T> getAdapter()
		throws ResourceInitializationException;

	protected abstract ModelResolver getModelResolver()
		throws ResourceInitializationException;

	/**
	 * Get the UIMA type name for the given tag in the given language.
	 *
	 * @param aModel a model
	 * @param aTag a tag.
	 * @return UIMA type name
	 */
	protected Type getTagType(DKProModel aModel, String aTag, TypeSystem aTS)
	{
        Map<String, String> mapping = aModel.getMapping();

        String type = mapping.get(aTag);
        if (type == null) {
        	missingTags.add(aTag);
        	//getContext().getLogger().log(Level.WARNING, "Mapping does not contain tag: " + aTag);
        	type = mapping.get("*");
        }
        if (type == null) {
        	throw new IllegalStateException("No fallback (*) mapping defined!");
        }

        Type uimaType = aTS.getType(type);

        if (uimaType == null) {
			throw new IllegalStateException("Type [" + type + "] mapped to tag [" + aTag
					+ "] is not defined in type system");
        }

        return uimaType;
    }

	@Override
	public void destroy()
	{
		if (treetagger != null) {
			getContext().getLogger().log(Level.INFO, "Destroying TreeTagger process");
			treetagger.destroy();
		}
	}

	@Override
	protected void finalize()
		throws Throwable
	{
		destroy();
		super.finalize();
	}

	/**
	 * @author Richard Eckart de Castilho
	 */
	protected class DKProExecutableResolver
		extends DefaultExecutableResolver
	{
		public File searchInFilesystem(final Set<String> aSearchedIn)
		{
			String platformId = treetagger.getPlatformDetector().getPlatformId();
			String exeSuffix  = treetagger.getPlatformDetector().getExecutableSuffix();

			for (final String p : getSearchPaths(_additionalPaths, "bin")) {
				if (p == null) {
					continue;
				}

				final File exe1 = new File(p + separator + "tree-tagger" + exeSuffix);
				final File exe2 = new File(p + separator + platformId + separator
						+ "tree-tagger" + exeSuffix);

				aSearchedIn.add(exe1.getAbsolutePath());
				if (exe1.exists()) {
					return exe1;
				}

				aSearchedIn.add(exe2.getAbsolutePath());
				if (exe2.exists()) {
					return exe2;
				}
			}

			return null;
		}

		public File searchInResources(final Set<String> aSearchedIn)
		{
			try {
				if (getContext().getResourceURL(RESOURCE_TREETAGGER) != null) {
					// If we cannot find it in the classpath, try using the specified
					// resource
					String platformId = treetagger.getPlatformDetector().getPlatformId();
					String exeSuffix  = treetagger.getPlatformDetector().getExecutableSuffix();
					String ttRelLoc = "/bin/"+platformId+"/tree-tagger"+exeSuffix;
					String ttPath = getContext().getResourceURL(RESOURCE_TREETAGGER).toURI().getPath();
					ttPath += ttRelLoc;
					File ttFile = new File(ttPath);
					aSearchedIn.add(ttFile.toURI()+" (UIMA external resource)");
					if (ttFile.exists()) {
						return ttFile;
					}
				}
				return null;
			}
			catch (Exception e) {
				return null;
			}
		}

		public File searchInClasspath(final Set<String> aSearchedIn)
		{
			try {
				String platformId = treetagger.getPlatformDetector().getPlatformId();
				String exeSuffix  = treetagger.getPlatformDetector().getExecutableSuffix();
				String ttRelLoc = "/bin/"+platformId+"/tree-tagger"+exeSuffix;
				String loc = "/de/tudarmstadt/ukp/dkpro/core/treetagger"+ttRelLoc;
				aSearchedIn.add("classpath:"+loc);
				URL ttExecUrl = getClass().getResource(loc);

				if (ttExecUrl != null) {
					return getUrlAsFile(ttExecUrl, true);
				}
				return null;
			}
			catch (Exception e) {
				return null;
			}
		}

		@Override
		public String getExecutable()
			throws IOException
		{
			Set<String> searchedIn = new HashSet<String>();

			File exeFile;
			if (executablePath != null) {
				exeFile = executablePath;
				searchedIn.add(executablePath.getAbsolutePath());
			}
			else {
				exeFile = searchInFilesystem(searchedIn);
				if (exeFile == null) {
					exeFile = searchInResources(searchedIn);
				}
				if (exeFile == null) {
					exeFile = searchInClasspath(searchedIn);
				}
			}
			if (exeFile == null) {
				throw new IOException("Unable to locate tree-tagger binary in the following locations " +
						searchedIn + ". Make sure the environment variable 'TREETAGGER_HOME' or " +
						"'TAGDIR' or the system property 'treetagger.home' point to the TreeTagger " +
						"installation directory.");
			}

			exeFile.setExecutable(true);

			if (!exeFile.isFile()) {
				throw new IOException("TreeTagger executable at ["+exeFile+"] is not a file.");
			}

			if (!exeFile.canRead()) {
				throw new IOException("TreeTagger executable at ["+exeFile+"] is not readable.");
			}

			if (!exeFile.canExecute()) {
				throw new IOException("TreeTagger executable at ["+exeFile+"] not executable.");
			}

			getContext().getLogger().log(Level.INFO,
					"TreeTagger executable location: " + exeFile.getAbsoluteFile());
			return exeFile.getAbsolutePath();
		}
	}

	/**
	 * A model resolver that knows how models map to language codes in DKPro.
	 *
	 * @author Richard Eckart de Castilho
	 */
	protected static abstract class DKProModelResolver
		extends DefaultModelResolver
	{
		private File overrideModelPath;
		private String overrideModelEncoding;
		private File overrideMappingPath;

		protected abstract
		String getType();

		public DKProModelResolver(File aModelPath, String aModelEncoding, File aMappingPath)
		{
			overrideModelPath = aModelPath;
			overrideModelEncoding = aModelEncoding;
			overrideMappingPath = aMappingPath;
		}

		public File searchInFilesystem(final String aLocation, final Set<String> aSearchedIn)
		{
			File _file = new File(aLocation);

			if (!_file.exists()) {
				for (final String p : getSearchPaths(_additionalPaths, "lib")) {
					if (p == null) {
						continue;
					}

					_file = new File(p+separator+aLocation);
					aSearchedIn.add(_file.getAbsolutePath());
					if (_file.exists()) {
						return _file;
					}
				}
			}

			return null;
		}

		@Override
		public DKProModel getModel(String aModelName)
			throws IOException
		{
			if (overrideModelPath != null) {
				Map<String, String> mapping = loadProperties(overrideMappingPath.toURI().toURL());
				return new DKProModel(aModelName, overrideModelPath, overrideModelEncoding, mapping);
			}

			File modelFile;
			String modelEnc;
			Map<String, String> mapping = null;
			Map<String, String> properties = null;
			Set<String> searchedIn = new HashSet<String>();
			String byteOrder = getPlatformDetector().getByteOrder();
			String baseFile = aModelName+"-"+getType()+"-"+byteOrder;
			String baseMapFile = aModelName+"-"+getType()+".map";

			// Try file system
			modelFile = searchInFilesystem(baseFile + ".par", searchedIn);
			if (modelFile != null) {
				String baseName = FilenameUtils.removeExtension(modelFile.getPath());
				File mappingFile = new File(modelFile.getAbsoluteFile().getParentFile(), baseMapFile);
				File propertiesFile = new File(baseName + ".properties");

				if (!mappingFile.exists()) {
					throw new IOException("There is no tag mapping for " + "model [" + aModelName
							+ "] at [" + mappingFile + "]");
				}

				if (!propertiesFile.exists()) {
					throw new IOException("There is no properties file for " + "model ["
							+ aModelName + "] at [" + propertiesFile + "]");
				}

				mapping = loadProperties(mappingFile.toURI().toURL());
				properties = loadProperties(propertiesFile.toURI().toURL());
			}

			// Try classpath
			if (modelFile == null) {
				String base = "/de/tudarmstadt/ukp/dkpro/core/treetagger/lib/";
				String mappingLoc = "/de/tudarmstadt/ukp/dkpro/core/treetagger/map/"+baseMapFile;
				String propertiesLoc = base+baseFile+".properties";
				String modelLoc = base+baseFile+".par";
				searchedIn.add("classpath:"+modelLoc);

				URL modelUrl = getClass().getResource(modelLoc);
				if (modelUrl != null) {
					URL propertiesUrl = getClass().getResource(propertiesLoc);
					if (propertiesUrl == null) {
						throw new IOException("There is no tag mapping for " + "model [" + aModelName
								+ "] at [" + propertiesLoc + "]");
					}
					URL mappingUrl = getClass().getResource(mappingLoc);
					if (mappingUrl == null) {
						throw new IOException("There is no properties file for " + "model ["
								+ aModelName + "] at [" + mappingLoc + "]");
					}

					mapping = loadProperties(mappingUrl);
					properties = loadProperties(propertiesUrl);
					modelFile = getUrlAsFile(modelUrl, true);
				}
			}

			if (modelFile == null || properties == null || mapping == null) {
				throw new IOException("Unable to locate model ["+aModelName+"] in the following " +
						"locations "+searchedIn+".  Make sure the environment variable " +
						"'TREETAGGER_HOME' or 'TAGDIR' or the system property 'treetagger.home' " +
						"point to the TreeTagger installation directory.");
			}

			modelEnc = properties.get("encoding");
			return new DKProModel(aModelName, modelFile, modelEnc, mapping);
		}

		private Map<String, String> loadProperties(URL aUrl)
		{
			InputStream is = null;
			try {
				is = aUrl.openStream();
				Properties mappingProperties = new Properties();
				mappingProperties.load(is);
				Map<String, String> mapping = new HashMap<String, String>();
				for (String key : mappingProperties.stringPropertyNames()) {
					mapping.put(key.trim(), mappingProperties.getProperty(key).trim());
				}
				return mapping;
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
			finally {
				closeQuietly(is);
			}
		}
	}

	/**
	 * @author Richard Eckart de Castilho
	 */
	public static class DKProModel
		extends DefaultModel
	{
		private Map<String, String> mapping;

		public DKProModel(String aName, File aFile, String aEncoding, Map<String, String> aMapping)
		{
			super(aName, aFile, aEncoding);
			mapping = aMapping;
		}

		public DKProModel(Model aModel, Map<String, String> aMapping)
		{
			super(aModel.getName(), aModel.getFile(), aModel.getEncoding());
			mapping = aMapping;
		}

		public Map<String, String> getMapping()
		{
			return mapping;
		}
	}
}
