package com.github.tosdan.utils.filters;

import java.io.IOException;
import java.util.HashMap;
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

	private boolean printStackTrace;

	@Override
	public void doFilter( ServletRequest request, ServletResponse response, FilterChain chain )	throws IOException, ServletException
	{
		HttpServletRequest req = ( HttpServletRequest ) request;
		HttpServletResponse resp = ( HttpServletResponse ) response;
		 
		// inizializza la mappa contenente i parametri della request
		String reqLog = this._processRequestForParams( req );
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
		// identificativo query nel file repository delle queries
		String nomeSQL = req.getParameter( "sqlName" );
		if (nomeSQL == null) 
			throw new SqlManagerFilterException( "Filtro " + this._filterConfig.getFilterName()  
					+ ": errore, parametro sqlName mancante nella request." );
		
		// raccoglie i parametri della request, degli initConf della servlet e degli attributes eventualmente aggiunti da servlet o filtri precedenti
		Map<String, Object> allParams = new HashMap<String, Object>();
		allParams.putAll( this._requestParamsMap );
		allParams.putAll( this._requestMultipleValuesParamsMap );
		allParams.putAll( this._initConfigParamsMap );
		allParams.putAll( this._requestAttributes );
		allParams.putAll( this.getParametriAggiuntivi(req) );
		
		if ( !_booleanSafeParse(req.getParameter("lasciaQueryParametrica")) )
		{
			try {
				// istanza l'oggetto per la validazione dei parametri rispetto ai valori effettivamente passati per evitare problemi sui Tipi
				MapFormatTypeValidator validator = new MapFormatTypeValidatorSQL();
				// compila la query parametrica sostituendo ai parametri i valori contenuti nella request e nell'initConf della servlet 
				querySql = QueriesUtils.compilaQueryDaFile( dtrProperties, queriesRepoFolderFullPath , nomeSQL, allParams, validator );
				
			} catch ( QueriesUtilsException e ) {
				if ( this.printStackTrace )
					e.printStackTrace();
				throw new SqlManagerFilterException(  "Filtro " + this._filterConfig.getFilterName()
						+ ": errore caricamento query da file. Classe: "+this.getClass().getName(), e );
			}
		}
		/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		
		req.setAttribute( "queryRecuperata", querySql ); // per retro compatibilita'
		req.setAttribute( nomeSQL, querySql );
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
