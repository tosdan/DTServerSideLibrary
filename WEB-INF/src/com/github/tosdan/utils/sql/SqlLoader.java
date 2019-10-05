package com.github.tosdan.utils.sql;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.yaml.snakeyaml.Yaml;

import com.github.tosdan.utils.stringhe.TemplateCompilerException;
import com.github.tosdan.utils.stringhe.TemplatePickerSections;
import com.github.tosdan.utils.varie.BoolUtils;
/**
 * 
 * @author Daniele
 * @version 0.4.3-b2013-10-02
 */
public class SqlLoader
{
	// Contenitore configurazione filtro
	private Map<String, String> settings;
	// Configurazione specifica per recupero/compilazione queries
	private Map<String, String> queriesRepositoryIndexMap;
	private String queriesRepositoryIndexFilename;
	private String queriesRepoFolderPath;
	// Opzioni
	private boolean isLogEnabled;
	private boolean lasciaParametrica;
	private boolean printStackTrace;
	private boolean retroCompatibilitaTemplate;
	private String SqlLoaderConfFile;
	private String webAppPath;

	public SqlLoader(String SqlLoaderConfFile, String webAppPath) {
		this.SqlLoaderConfFile = SqlLoaderConfFile;
		this.webAppPath = webAppPath;
	}
	
	/**
	 * 
	 * @param primaryParams
	 * @param overridingParams
	 * @return
	 * @throws SqlLoaderFilterException 
	 */
	@SuppressWarnings( "unchecked" )
	private List<String> getNomiQueriesDaCompilare(Map<String, Object> primaryParams, Map<String, Object> overridingParams) {
		List<String> nomiQueriesDaCompilare = null;
		Object sqlParam = (overridingParams != null && overridingParams.get("sqlOverride") != null) 
				? overridingParams.get("sqlOverride") 
				: primaryParams.get("sqlName");
		
		if ( sqlParam instanceof String ) {
			nomiQueriesDaCompilare = new ArrayList<String>();
			nomiQueriesDaCompilare.add( (String) sqlParam );
		} else if (sqlParam instanceof List) {
			nomiQueriesDaCompilare = (List<String>) sqlParam;
		}

		if (nomiQueriesDaCompilare == null || nomiQueriesDaCompilare.isEmpty())
			throw new IllegalArgumentException( "Errore parametro 'sqlName' mancante nella request.");
		
		return nomiQueriesDaCompilare;
	}
	
	
	/**
	 * 
	 * @param primaryParams
	 * @return
	 * @throws IOException
	 * @throws ServletException
	 */
	public Map<String, Object> loadQueries(Map<String, Object> primaryParams) throws IOException {
		return loadQueries(primaryParams, null, null);
	}
	/**
	 * 
	 * @param primaryParams
	 * @param updateParamsMapsList
	 * @return
	 * @throws IOException
	 * @throws ServletException
	 */
	public Map<String, Object> loadQueries(Map<String, Object> primaryParams, List<Map<String, Object>> updateParamsMapsList) throws IOException {
		return loadQueries(primaryParams, updateParamsMapsList, null);
		
	}
	
	
	/**
	 * 
	 * @param primaryParams
	 * @param overridingParams
	 * @return
	 * @throws IOException
	 * @throws ServletException
	 */
	public Map<String, Object> loadQueries(Map<String, Object> primaryParams, Map<String, Object> overridingParams) throws IOException {
		return loadQueries(primaryParams, null, overridingParams);
	}
	/**
	 * 
	 * @param primaryParams
	 * @param updateParamsMapsList Lista eventuali parametri per query di update (anche piu' di un update per volta essendo appunto una lista di parametri)
	 * @param overridingParams
	 * @return
	 * @throws IOException
	 * @throws ServletException
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> loadQueries(Map<String, Object> primaryParams, List<Map<String, Object>> updateParamsMapsList, Map<String, Object> overridingParams) throws IOException {
		Map<String, Object> retVal = new HashMap<String, Object>();
		
		List<String> nomiQueriesDaCompilare = getNomiQueriesDaCompilare(primaryParams, overridingParams);
		
		getSqlLoaderOptions(primaryParams);
		loadConfiguration();
		logParametersToFile( primaryParams );
		
		if ( ! this.lasciaParametrica ) {
			if (updateParamsMapsList == null) {
				updateParamsMapsList = (List<Map<String, Object>>) primaryParams.get("sqlLoaderParametriUpdate");
				if (overridingParams != null)
					updateParamsMapsList = (List<Map<String, Object>>) overridingParams.get("sqlLoaderParametriUpdate");
			}
			MassiveQueryCompiler mqc = new MassiveQueryCompiler( this.queriesRepositoryIndexMap, this.queriesRepoFolderPath, updateParamsMapsList );
			
			if ( retroCompatibilitaTemplate )
				mqc.setPicker( new TemplatePickerSections() );

			try {
				Map<String, Object> allParams = mergeParams(primaryParams, overridingParams);
				Map<String, List<String>> queries = mqc.getQueriesListMap( nomiQueriesDaCompilare, allParams );
				
				// Ogni query e' recuperabile usando il nome specificato in sqlName nella request ajax
				for( String nomeSql : nomiQueriesDaCompilare ) {
					
					List<String> listaQueryTemp = queries.get(nomeSql);
					if (listaQueryTemp.size() == 1) {
						retVal.put( nomeSql, listaQueryTemp.get(0) );
					} else {
						retVal.put( nomeSql, listaQueryTemp );
					}
					
				}
				
			} catch ( TemplateCompilerException e ) {
				if ( printStackTrace )
					e.printStackTrace();
				throw new IllegalArgumentException( "Errore compilazione query.", e );
			}
			
		} else {
			// TODO recupero query senza compilarla
			
		}
				
		return retVal;
	}
	
	
	/**
	 * 
	 * @param primaryParams
	 * @throws IOException 
	 */
	private void logParametersToFile(Map<String, Object> primaryParams) throws IOException {
		if ( isLogEnabled && settings.get("logFileName") != null )
			FileUtils.writeStringToFile(new File(settings.get("logFileName")), primaryParams.toString());// crea un file di log con il nome passato come parametro nella sottocartella della webapp
	}
	
	
	/**
	 * Raccoglie tutti i <code>parameters</code> della <code>request</code> compresi <code>attributes</code> eventualmente aggiunti da servlet o filtri precedenti e parametri custom contenuti in mappe inserite in sessione o in un attribute della request
	 * @param primaryParams
	 * @param overridingParams 
	 * @return
	 */
	private Map<String, Object> mergeParams(Map<String, Object> primaryParams, Map<String, Object> overridingParams) {	
		Map<String, Object> allParams = new HashMap<String, Object>();
		allParams.putAll( primaryParams );
		if (overridingParams != null)
			allParams.putAll(overridingParams);
		return allParams;
	}
	
	
	/**
	 * 
	 * @throws IOException 
	 */
	private void loadConfiguration() throws IOException {
		if (SqlLoaderConfFile == null)
			throw new IllegalArgumentException( "Errore, file di configurazione non trovato.");
		// Carica la configurazione per SqlLoaderFilter da file.
		settings = getMapFromYamlFile( webAppPath + "/" + SqlLoaderConfFile );
		
		// Percorso relativo del file contenente l'index delle queries
		queriesRepositoryIndexFilename = settings.get("SqlLoaderQueriesReposIndex_File");
		
		if (queriesRepositoryIndexFilename == null)
			throw new IllegalArgumentException( "Errore, file indice queries non trovato.");
		// Crea una mappa con l'index delle queries
		queriesRepositoryIndexMap = getMapFromYamlFile( webAppPath + "/" + queriesRepositoryIndexFilename );
		
		// Percorso assoluto dei file contenenti i templates delle queries
		queriesRepoFolderPath = webAppPath + "/" + settings.get("SqlLoaderConf_Path");
		
	}
	
	
	/**
	 * Legge dalla request eventuali opzioni inserite nella chiamata e imposta le relative variabili di istanza con i valori recuperati
	 * @param primaryParams
	 */
	private void getSqlLoaderOptions(Map<String, Object> primaryParams) {
		// Opzione per verbose stacktrace delle eccezioni catturate
		printStackTrace = BoolUtils.toBoolean(primaryParams.get("printStackTrace"));
		// Opzione per abilitare il log
		isLogEnabled =  BoolUtils.toBoolean(primaryParams.get("logSqlLoader"));
		// Opzione per evitare la compilazione automatica della query. Fondamentalmente recupera la query e la serve immutata TODO
		lasciaParametrica = BoolUtils.toBoolean(primaryParams.get("lasciaQueryParametrica"));
		// 
		retroCompatibilitaTemplate = BoolUtils.toBoolean(primaryParams.get("noYamlPicker"));
	}
	
	
	/**
	 * Carica e restituisce una mappa con i dati di configurazione contenuti nel file passato.
	 * @param filename
	 * @throws IOException 
	 */
	@SuppressWarnings( "unchecked" )
	private Map<String, String> getMapFromYamlFile(String filename) throws IOException {
		
		Map<String, String> contentMap;
		Yaml yaml = new Yaml();
		String configFileContent = FileUtils.readFileToString(new File(filename));
		contentMap = (Map<String, String>) yaml.load( configFileContent );
		
		return contentMap;
	}
}
