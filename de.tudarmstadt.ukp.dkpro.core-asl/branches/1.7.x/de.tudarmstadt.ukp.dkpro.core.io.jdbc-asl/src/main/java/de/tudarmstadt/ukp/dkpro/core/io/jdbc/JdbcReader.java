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
package de.tudarmstadt.ukp.dkpro.core.io.jdbc;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.component.CasCollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

/**
 * Collection reader for JDBC database.The obtained data will be written into CAS DocumentText as
 * well as fields of the {@link DocumentMetaData} annotation.
 * <p>
 * The field names are available as constants and begin with <code>CAS_</code>. Please specify the
 * mapping of the columns and the field names in the query. For example,
 * <p>
 * <code>SELECT text AS cas_text, title AS cas_metadata_title FROM test_table</code>
 * <p>
 * will create a CAS for each record, write the content of "text" column into CAS documen text and
 * that of "title" column into the document title field of the {@link DocumentMetaData} annotation.
 *
 * @author Shuo Yang
 *
 */

@TypeCapability(
        outputs={
                "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData"})

public class JdbcReader
    extends CasCollectionReader_ImplBase
{
    public static final String CAS_TEXT = "cas_text";
    public static final String CAS_METADATA_TITLE = "cas_metadata_title";
    public static final String CAS_METADATA_LANGUAGE = "cas_metadata_language";

    public static final String CAS_METADATA_DOCUMENT_ID = "cas_metadata_document_id";
    public static final String CAS_METADATA_COLLECTION_ID = "cas_metadata_collection_id";
    public static final String CAS_METADATA_DOCUMENT_URI = "cas_metadata_document_uri";
    public static final String CAS_METADATA_DOCUMENT_BASE_URI = "cas_metadata_document_base_uri";

    private static final Set<String> CAS_COLUMNS = new HashSet<String>(asList(
    		CAS_TEXT, CAS_METADATA_TITLE, CAS_METADATA_LANGUAGE, CAS_METADATA_DOCUMENT_ID,
    		CAS_METADATA_COLLECTION_ID, CAS_METADATA_DOCUMENT_URI, CAS_METADATA_DOCUMENT_BASE_URI));

    /**
     * Specify the class name of the JDBC driver.
     * <p>
     * If used with uimaFIT and the value is not given, <code>com.mysql.jdbc.Driver</code> will be
     * taken.
     */
    public static final String PARAM_DRIVER = "driver";
    @ConfigurationParameter(name = PARAM_DRIVER, mandatory = true, defaultValue = "com.mysql.jdbc.Driver")
    private String driver;

    /**
     * Specifies the URL to the database.
     * <p>
     * If used with uimaFIT and the value is not given, <code>jdbc:mysql://127.0.0.1/</code> will be
     * taken.
     */
    public static final String PARAM_CONNECTION = "connection";
    @ConfigurationParameter(name = PARAM_CONNECTION, mandatory = true, defaultValue = "jdbc:mysql://127.0.0.1/")
    private String connection;

    /**
     * Specifies name of the database to be accessed.
     */
    public static final String PARAM_DATABASE = "database";
    @ConfigurationParameter(name = PARAM_DATABASE, mandatory = true)
    private String database;

    /**
     * Specifies the user name for database access.
     */
    public static final String PARAM_USER = "user";
    @ConfigurationParameter(name = PARAM_USER, mandatory = true)
    private String user;

    /**
     * Specifies the password for database access.
     */
    public static final String PARAM_PASSWORD = "password";
    @ConfigurationParameter(name = PARAM_PASSWORD, mandatory = true)
    private String password;

    /**
     * Specifies the query.
     */
    public static final String PARAM_QUERY = "query";
    @ConfigurationParameter(name = PARAM_QUERY, mandatory = true)
    private String query;

    private Connection sqlConnection;
    private ResultSet resultSet;
    private int resultSetSize;
    private int completed;
    private Set<String> columnNames;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        openDatabaseConnection();
        query();
    }

    private void openDatabaseConnection()
    	throws ResourceInitializationException
    {
        // Open connection
        if (!connection.endsWith("/")) {
            connection = connection + "/";
        }
        String url = connection + database + "?user=" + user + "&password=" + password;

        try {
            Class.forName(driver);
            sqlConnection = DriverManager.getConnection(url);
        }
        catch (ClassNotFoundException e) {
            throw new ResourceInitializationException(e);
        }
        catch (SQLException e) {
            throw new ResourceInitializationException(e);
        }
    }

    private void query() throws ResourceInitializationException
    {
        try {
            Statement statement = sqlConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            resultSet = statement.executeQuery(query);
            resultSet.last();
            resultSetSize = resultSet.getRow();
            resultSet.beforeFirst();
            completed = 0;

            // Store available column names
            columnNames = new HashSet<String>();
            ResultSetMetaData meta = resultSet.getMetaData();
            for (int i = 1; i < meta.getColumnCount() + 1; i++) {
            	String columnName = meta.getColumnLabel(i);
                columnNames.add(columnName);
                if (!CAS_COLUMNS.contains(columnName)) {
                    getLogger().warn("Unknown column [" + columnName + "].");
                }
            }
        }
        catch (SQLException e) {
            throw new ResourceInitializationException(e);
        }
    }

    @Override
    public void getNext(CAS cas)
        throws IOException, CollectionException
    {
    	try {
    		resultSet.next(); // advance to next item (used to be done in hasNext())
    	} catch (SQLException e) {
    		throw new CollectionException(e);
    	}
        // Store data into CAS
        DocumentMetaData metadata = null;
        try {
            metadata = DocumentMetaData.create(cas);
        }
        catch (CASException e) {
            throw new CollectionException(e);
        }

        cas.setDocumentText(getStringQuietly(CAS_TEXT));
        metadata.setDocumentTitle(getStringQuietly(CAS_METADATA_TITLE));
        metadata.setLanguage(getStringQuietly(CAS_METADATA_LANGUAGE));

        metadata.setDocumentId(getStringQuietly(CAS_METADATA_DOCUMENT_ID));
        metadata.setCollectionId(getStringQuietly(CAS_METADATA_COLLECTION_ID));
        metadata.setDocumentUri(getStringQuietly(CAS_METADATA_DOCUMENT_URI));
        metadata.setDocumentBaseUri(getStringQuietly(CAS_METADATA_DOCUMENT_BASE_URI));

        completed++;
    }

    private String getStringQuietly(String columnName)
    {
        if (columnNames.contains(columnName)) {
            try {
                return resultSet.getString(columnName);
            }
            catch (SQLException e) {
                getLogger().warn("Error getting value for column [" + columnName + "].", e);
            }
        }
        return null;
    }

    @Override
    public Progress[] getProgress()
    {
        // REC: It should be possible to determine the current row and the total size of the result
        // set and use this here to return progress information.
        return new Progress[] { new ProgressImpl( completed, resultSetSize, "row" ) };
    }

    @Override
    public boolean hasNext()
        throws IOException, CollectionException
    {
       	return (completed < resultSetSize);
    }

    @Override
    public void close()
        throws IOException
    {
    	DbUtils.closeQuietly(resultSet);
    	DbUtils.closeQuietly(sqlConnection);
    }
}
