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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.types.resources.FileResourceIterator;
import org.apache.uima.UimaContext;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.component.CasCollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;

/**
 * Base class for file system collection readers. Uses an Ant FileSet to conveniently walk the
 * file system.
 * <p>
 * Example of a hypothetic <code>FooReader</code> that should read only files ending in
 * <code>.foo</code> from in the directory <code>foodata</code> or any subdirectory thereof:
 * <pre>
 * CollectionReader reader = createReader(FooReader.class,
 *     FileSetCollectionReaderBase.PARAM_LANGUAGE, "en",
 *     FileSetCollectionReaderBase.PARAM_SOURCE_LOCATION, "some/path",
 *     FileSetCollectionReaderBase.PARAM_PATTERNS, "[+]foodata&#47;**&#47;*.foo" );
 * </pre>
 * @author Richard Eckart de Castilho
 * @since 1.0.6
 * @deprecated use {@link ResourceCollectionReaderBase} instead.
 */
@Deprecated
public abstract class FileSetCollectionReaderBase
	extends CasCollectionReader_ImplBase
{
	public static final String INCLUDE_PREFIX = "[+]";
	public static final String EXCLUDE_PREFIX = "[-]";

    /**
     * Location from which the input is read.
     * 
     * @deprecated use {@link #PARAM_SOURCE_LOCATION}
     */
    @Deprecated
    public static final String PARAM_PATH = ComponentParameters.PARAM_SOURCE_LOCATION;
    
    /**
     * Location from which the input is read.
     */
    public static final String PARAM_SOURCE_LOCATION = ComponentParameters.PARAM_SOURCE_LOCATION;
	@ConfigurationParameter(name=PARAM_SOURCE_LOCATION, mandatory=false)
	private File sourceLocation;

	/**
	 * A set of Ant-like include/exclude patterns. A pattern starts with {@link #INCLUDE_PREFIX [+]}
	 * if it is an include pattern and with {@link #EXCLUDE_PREFIX [-]} if it is an exclude pattern.
	 * The wildcard <code>&#47;**&#47;</code> can be used to address any number of sub-directories.
	 * The wildcard {@code *} can be used to a address a part of a name.
	 */
	public static final String PARAM_PATTERNS = "patterns";
	@ConfigurationParameter(name=PARAM_PATTERNS, mandatory=true)
	private String[] patterns;

    /**
     * Use the default excludes.
     */
    public static final String PARAM_USE_DEFAULT_EXCLUDES = "useDefaultExcludes";
    @ConfigurationParameter(name=PARAM_USE_DEFAULT_EXCLUDES, mandatory=true, defaultValue="true")
    private boolean useDefaultExcludes;


	/**
	 * The language.
	 */
	public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
	@ConfigurationParameter(name=PARAM_LANGUAGE, mandatory=false)
	private String language;

	/**
	 * States whether the matching is done case sensitive. (default: true)
	 */
	public static final String PARAM_CASE_SENSITIVE= "caseSensitive";
	@ConfigurationParameter(name=PARAM_CASE_SENSITIVE, mandatory=false, defaultValue="true")
	private boolean caseSensitive;

	private DirectoryScanner directoryScanner;
	private int completed;
	private Iterator<FileResource> fileSetIterator;

	@SuppressWarnings("unchecked")
	@Override
	public void initialize(UimaContext aContext)
		throws ResourceInitializationException
	{
		super.initialize(aContext);

		// Configure the FileSet.
		directoryScanner = new DirectoryScanner();
		if (sourceLocation != null) {
			directoryScanner.setBasedir(sourceLocation);
		}

		// Configure case sensitivity
		directoryScanner.setCaseSensitive(caseSensitive);

		// Parse the patterns and inject them into the FileSet
		List<String> includes = new ArrayList<String>();
		List<String> excludes = new ArrayList<String>();
		for (String pattern : patterns) {
			if (pattern.startsWith(INCLUDE_PREFIX)) {
				includes.add(pattern.substring(INCLUDE_PREFIX.length()));
			}
			else if (pattern.startsWith(EXCLUDE_PREFIX)) {
				excludes.add(pattern.substring(EXCLUDE_PREFIX.length()));
			}
            else if (pattern.matches("^\\[.\\].*")) {
                throw new ResourceInitializationException(new IllegalArgumentException(
                        "Patterns have to start with " + INCLUDE_PREFIX + " or " + EXCLUDE_PREFIX
                                + "."));
            }
            else {
                includes.add(pattern);
            }
		}
		
        // These should be the same as documented here: http://ant.apache.org/manual/dirtasks.html
        if (useDefaultExcludes) {
            excludes.add("**/*~");
            excludes.add("**/#*#");
            excludes.add("**/.#*");
            excludes.add("**/%*%");
            excludes.add("**/._*");
            excludes.add("**/CVS");
            excludes.add("**/CVS/**");
            excludes.add("**/.cvsignore");
            excludes.add("**/SCCS");
            excludes.add("**/SCCS/**");
            excludes.add("**/vssver.scc");
            excludes.add("**/.svn");
            excludes.add("**/.svn/**");
            excludes.add("**/.DS_Store");
            excludes.add("**/.git");
            excludes.add("**/.git/**");
            excludes.add("**/.gitattributes");
            excludes.add("**/.gitignore");
            excludes.add("**/.gitmodules");
            excludes.add("**/.hg");
            excludes.add("**/.hg/**");
            excludes.add("**/.hgignore");
            excludes.add("**/.hgsub");
            excludes.add("**/.hgsubstate");
            excludes.add("**/.hgtags");
            excludes.add("**/.bzr");
            excludes.add("**/.bzr/**");
            excludes.add("**/.bzrignore");
        }		
		
		directoryScanner.setIncludes(includes.toArray(new String[includes.size()]));
		directoryScanner.setExcludes(excludes.toArray(new String[excludes.size()]));
		directoryScanner.scan();

		// Get the iterator that will be used to actually traverse the FileSet.
		fileSetIterator = new FileResourceIterator(null, sourceLocation, directoryScanner.getIncludedFiles());

		getLogger().info("Found [" + getIncludedFilesCount() + "] files to be read");
	}

	protected int getIncludedFilesCount() {

		return directoryScanner.getIncludedFilesCount();

	}
	protected Iterator<FileResource> getFileSetIterator()
	{
		return fileSetIterator;
	}

	protected FileResource nextFile()
	{
		try {
			return fileSetIterator.next();
		}
		finally {
			completed++;
		}
	}

	@Override
	public Progress[] getProgress()
	{
		return new Progress[] { new ProgressImpl(completed, getIncludedFilesCount(), "file") };
	}

	@Override
	public boolean hasNext()
		throws IOException, CollectionException
	{
		return fileSetIterator.hasNext();
	}

    /**
     * Initialize the {@link DocumentMetaData}. This must be called before setting the document
     * text, otherwise the end feature of this annotation will not be set correctly.
     * 
     * @param aCas
     *            the CAS.
     * @param aFile
     *            the file from which the CAS is initialized.
     * @param aQualifier
     *            a qualifier if multiple CASes are generated from the same file.
     */
	protected void initCas(CAS aCas, FileResource aFile, String aQualifier)
	{
		String qualifier = aQualifier != null ? "#"+aQualifier : "";
		try {
			// Set the document metadata
			DocumentMetaData docMetaData = DocumentMetaData.create(aCas);
			File file = aFile.getFile();
			docMetaData.setDocumentTitle(file.getName());
			docMetaData.setDocumentUri(file.toURI().toString()+qualifier);
			docMetaData.setDocumentId(aFile.getName()+qualifier);
			if (aFile.getBaseDir() != null) {
			    docMetaData.setDocumentBaseUri(sourceLocation.toURI().toString());
				docMetaData.setCollectionId(aFile.getBaseDir().getPath());
			}

			// Set the document language
			aCas.setDocumentLanguage(language);
		}
		catch (CASException e) {
			// This should not happen.
			throw new RuntimeException(e);
		}
	}

	public String getLanguage() {
		return language;
	}
}
