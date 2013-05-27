package com.github.tosdan.utils.filters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.tosdan.utils.sql.QueriesUtils;
import com.github.tosdan.utils.sql.QueriesUtilsException;
import com.github.tosdan.utils.stringhe.MapFormatTypeValidator;
import com.github.tosdan.utils.stringhe.MapFormatTypeValidatorSQL;

public class SqlManagerFilter extends BasicFilter
{

	private Map<String, String> requestParamsMap;
	private Map<String, List<String>> requestMultipleValuesParamsMap;
	private boolean printStackTrace;
	private Map<String, Object> requestAttributes;

	@Override
	public void doFilter( ServletRequest request, ServletResponse response, FilterChain chain )	throws IOException, ServletException
	{
		HttpServletRequest req = ( HttpServletRequest ) request;
		HttpServletResponse resp = ( HttpServletResponse ) response;
		 
		// inizializza le mappe contenenti i parametri/attributi della request
		this.requestParamsMap = this.get_requestParamsMap( req );
		this.requestMultipleValuesParamsMap = this.get_requestMultipleValuesParamsMap( req );
		this.requestAttributes = this.get_requestAttributes( req );
		
		String reqLog = this.get_requestParamsProcessLog( req );
		if ( this._booleanSafeParse(req.getParameter("logSqlManager")) && this._initConfigParamsMap.get("logFileName") != null ) 
			// crea un file di log con il nome passato come parametro nella sottocartella della webapp
			this._logOnFile( this._ctx.getRealPath(this._initConfigParamsMap.get("logFileName")), reqLog );
		
		// flag per verbose stacktrace delle eccezioni catturate da questa servlet
		this.printStackTrace = this._booleanSafeParse( req.getParameter("printStackTrace") );
		// percorso file settings
		String queriesRepoFolderFullPath = this._appRealPath + this._initConfigParamsMap.get( "SqlManagerServletConf_Path" );
		// nome file settings
		String propertiesFile = this._initConfigParamsMap.get( "SqlManagerServletConf_File" );
		// carica la configurazione dal file properties
		Properties dtrProperties = this.loadProperties( propertiesFile );

		/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

		// query da eseguire
		String querySql = "";
		Map<String, List<String>> queries = new HashMap<String, List<String>>();
		// identificativo query nel file repository delle queries
		String nomeSQL = req.getParameter( "sqlName" );
		String[] nomiSQL = req.getParameterValues( "sqlName" );
		if (nomiSQL == null) 
			throw new SqlManagerFilterException( "Filtro " + this._filterConfig.getFilterName()  
					+ ": errore, parametro sqlName mancante nella request." );
		
		// raccoglie i parametri della request, degli initConf della servlet e degli attributes eventualmente aggiunti da servlet o filtri precedenti
		Map<String, Object> allParams = new HashMap<String, Object>();
		allParams.putAll( this.requestParamsMap );
		allParams.putAll( this.requestMultipleValuesParamsMap );
		allParams.putAll( this._initConfigParamsMap );
		allParams.putAll( this.requestAttributes );
		allParams.putAll( this.getParametriAggiuntivi(req) );
		
		if ( !_booleanSafeParse(req.getParameter("lasciaQueryParametrica")) )
		{
			try {
				// istanza l'oggetto per la validazione dei parametri rispetto ai valori effettivamente passati per evitare problemi sui Tipi
				MapFormatTypeValidator validator = new MapFormatTypeValidatorSQL();
				// compila la query parametrica sostituendo ai parametri i valori contenuti nella request e nell'initConf della servlet 
				querySql = QueriesUtils.compilaQueryDaFile( dtrProperties, queriesRepoFolderFullPath, nomeSQL, allParams, validator );
				
				for( int i = 0 ; i < nomiSQL.length ; i++ ) {
					List<String> listaQueries = new ArrayList<String>();
					if (req.getAttribute( "sqlManagerParametriUpdate" ) != null) {
						@SuppressWarnings( "unchecked" )
						List<Map<String, Object>> listaUpdatesMaps = ( List<Map<String, Object>> ) req.getAttribute( "sqlManagerParametriUpdate" );
						for( Map<String, Object> updateMap : listaUpdatesMaps ) {
							listaQueries.add( QueriesUtils.compilaQueryDaFile( dtrProperties, queriesRepoFolderFullPath, nomiSQL[i], updateMap, validator ) );							
						}
					} else {
						listaQueries.add( QueriesUtils.compilaQueryDaFile( dtrProperties, queriesRepoFolderFullPath, nomiSQL[i], allParams, validator ) );
					}
					queries.put( nomiSQL[i], listaQueries );
				}
				
			} catch ( QueriesUtilsException e ) {
				if ( this.printStackTrace )
					e.printStackTrace();
				throw new SqlManagerFilterException(  "Filtro " + this._filterConfig.getFilterName()
						+ ": errore caricamento query da file. Classe: "+this.getClass().getName(), e );
			}
		} else {
			// TODO recupero query senza compilarla
		}
		/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		req.setAttribute( "queryRecuperata", querySql ); // per retro compatibilita'
		req.setAttribute( nomeSQL, querySql );
		req.setAttribute( "sqlManagerMappaQueries", queries );
		
		chain.doFilter( req, resp );
//		System.out.println( this._filterConfig.getFilterName() +"\n" + req.getSession().getAttribute( "JsonDataTableString" ) + " - " + req.getSession().getAttribute( "DataTableQuery" ));
	}
	
	@SuppressWarnings( "unchecked" )
	protected Map<String, Object> getParametriAggiuntivi(HttpServletRequest req) {
		String idParametriAggiuntiviDaSessione = req.getParameter( "SqlMngrSessionCustomParamsMap" );
		String idParametriAggiuntiviOnCallingChain = req.getParameter( "SqlMngrCustomParamsMap" ); // aggiunti da servlet nella catena di chiamate 

		Map<String, Object> mappaParametriAggiuntivi = null;
		if (idParametriAggiuntiviDaSessione != null )		
			mappaParametriAggiuntivi = (Map<String, Object>) req.getSession().getAttribute(idParametriAggiuntiviDaSessione);

		Map<String, Object> mappaParametriAggiuntiviOnCallingChain = null;
		if (idParametriAggiuntiviOnCallingChain != null)
		mappaParametriAggiuntiviOnCallingChain = (Map<String, Object>) req.getAttribute(idParametriAggiuntiviOnCallingChain);

		if (mappaParametriAggiuntivi == null) 
			mappaParametriAggiuntivi = new HashMap<String, Object>();
		if (mappaParametriAggiuntiviOnCallingChain != null)
			mappaParametriAggiuntivi.putAll( mappaParametriAggiuntiviOnCallingChain );
		
		
		return mappaParametriAggiuntivi;
	}
	
	
	/**
	 * Carica e restituisce un oggetto {@link Properties}
	 * @param propertiesFile
	 * @throws SqlManagerServletException
	 */
	protected Properties loadProperties(String propertiesFile) throws SqlManagerFilterException
	{
		Properties dtrSettings = new Properties();
		
		try {
			// carica, dal file passato, l'oggetto Properties salvandolo in un campo della servlet 
			dtrSettings.load( this._ctx.getResourceAsStream( propertiesFile ) );
			
		} catch ( IOException e ) {
			if ( printStackTrace )
				e.printStackTrace();
			throw new SqlManagerFilterException( "Filtro " + this._filterConfig.getFilterName()
					+ ": errore caricamento file configurazione. Classe: "+this.getClass().getName(), e );
		}
		
		return dtrSettings;
	}
}
