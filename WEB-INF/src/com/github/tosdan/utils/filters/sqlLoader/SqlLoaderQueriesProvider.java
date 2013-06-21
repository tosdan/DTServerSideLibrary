package com.github.tosdan.utils.filters.sqlLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.github.tosdan.utils.sql.QueriesUtils;
import com.github.tosdan.utils.sql.QueriesUtilsException;
import com.github.tosdan.utils.stringhe.MapFormatTypeValidator;
import com.github.tosdan.utils.stringhe.MapFormatTypeValidatorSQL;

public class SqlLoaderQueriesProvider
{
	private String[]  nomiQueriesDaCompilare;
	private List<Map<String, Object>> updateParamsMapsList;
	private Properties queriesRepository;
	private String queriesRepoFolderFullPath;
	
	/**
	 * 
	 * @param nomiQueriesDaCompilare
	 * @param queriesRepository
	 * @param queriesRepoFolderFullPath
	 */
	public SqlLoaderQueriesProvider(String[] nomiQueriesDaCompilare, Properties queriesRepository, String queriesRepoFolderFullPath ) {
		this( nomiQueriesDaCompilare, queriesRepository, queriesRepoFolderFullPath, null );
	}
	
	/**
	 * 
	 * @param nomiQueriesDaCompilare Array di nomi/id delle queries da compilare.
	 * @param queriesRepository Contiene le associazioni nome-query -> file-query-template. L'associazione da un id/nome di una query al file contenente il modello/template della query associata. 
	 * @param queriesRepoFolderFullPath Percorso assoluto completo della cartella (radice) contenente i template delle queries da compilare
	 * @param updateParamsMapsList
	 */
	public SqlLoaderQueriesProvider(String[] nomiQueriesDaCompilare, Properties queriesRepository, String queriesRepoFolderFullPath, List<Map<String, Object>> updateParamsMapsList) {
		this.nomiQueriesDaCompilare = nomiQueriesDaCompilare;
		this.updateParamsMapsList = updateParamsMapsList;
		this.queriesRepository = queriesRepository;
		this.queriesRepoFolderFullPath = queriesRepoFolderFullPath;
	}
	
	/**
	 * 
	 * @param allParams Mappa per la sostituzione dei parameti nelle queries (parametriche) da compilare
	 * @return
	 * @throws QueriesUtilsException 
	 */
	public Map<String, List<String>> getQueriesListMap(Map<String, Object> allParams) throws QueriesUtilsException {

		Map<String, List<String>> queries = new HashMap<String, List<String>>();

		// Istanzia l'oggetto per la validazione dei parametri rispetto ai valori effettivamente passati per evitare problemi sui Tipi
		MapFormatTypeValidator validator = new MapFormatTypeValidatorSQL(); // TODO possibilita' di impostarne uno a piacere
		// Compila la query parametrica sostituendo ai parametri i valori contenuti nella mappa 'allParams'
		for( int i = 0 ; i < this.nomiQueriesDaCompilare.length ; i++ ) {
			List<String> listaQueries = new ArrayList<String>();
			if (this.updateParamsMapsList != null) {
				for( Map<String, Object> updateParamsMap : this.updateParamsMapsList ) {
					listaQueries.add( QueriesUtils.compilaQueryDaFile( this.queriesRepository, this.queriesRepoFolderFullPath, this.nomiQueriesDaCompilare[i], updateParamsMap, validator ) );							
				}
			} else {
				listaQueries.add( QueriesUtils.compilaQueryDaFile( this.queriesRepository, this.queriesRepoFolderFullPath, this.nomiQueriesDaCompilare[i], allParams, validator ) );
			}
			queries.put( this.nomiQueriesDaCompilare[i], listaQueries );
		}
			
		return queries;
	}
}
