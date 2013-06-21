package com.github.tosdan.utils.filters.sqlLoader;

import java.io.IOException;
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

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;

import com.github.tosdan.utils.filters.BasicFilter;
import com.github.tosdan.utils.sql.QueriesUtilsException;
/**
 * 
 * @author Daniele
 * @version 0.1.1-b2013.06.19
 */
public class SqlLoaderFilter extends BasicFilter
{
	private String[]  nomiQueriesDaCompilare;
	private List<Map<String, Object>> updateParamsMapsList;
	private Properties queriesRepository;
	private String queriesRepoFolderFullPath;
	private boolean isLogEnabled;
	private boolean lasciaParametrica;
	private boolean printStackTrace;
	private Properties thisProperties;
	private String thisConfigFile;
	private String queriesRepositoryFileName;

	@Override
	public void doFilter( ServletRequest request, ServletResponse response, FilterChain chain )	throws IOException, ServletException
	{
		HttpServletRequest req = ( HttpServletRequest ) request;
		HttpServletResponse resp = ( HttpServletResponse ) response;

		/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

		this.nomiQueriesDaCompilare = this.getNomiQueriesDaCompilare( req );
		
		// Nome dell'attributo inserito nella request, inviato alla servlet cliente, che conterra' le queries richieste
		// sqlLoaderMappaQueries e' la chiave di default per recuperare (nella servlet cliente) la mappa con le queries compilate
		String contenitoreMultiQueries = StringUtils.defaultString( req.getParameter("MultiQueryContainer"), "sqlLoaderMappaQueries" );
		
		// Lista eventuali parametri per query di update (anche piu' di un update per volta essendo appunto una lista di parametri)
		this.updateParamsMapsList = (List<Map<String, Object>>) req.getAttribute("sqlLoaderParametriUpdate");

		this.retrieveOptions(req);
		this.loadConfiguration();

		String reqLog = this.get_requestParamsProcessLog( req );
		if ( this.isLogEnabled && this.thisProperties.get("logFileName") != null ) 
			// crea un file di log con il nome passato come parametro nella sottocartella della webapp
			this._logOnFile( this._appRealPath + thisProperties.get("logFileName"), reqLog );

		/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

		if ( ! this.lasciaParametrica ) {
			Map<String, Object> allPossibleParams = this.getAllParamsMap( req );
			SqlLoaderQueriesProvider sqlQP = new SqlLoaderQueriesProvider( this.nomiQueriesDaCompilare, this.queriesRepository, this.queriesRepoFolderFullPath, this.updateParamsMapsList );

			try {
				Map<String, List<String>> queries = sqlQP.getQueriesListMap( allPossibleParams );
				
				// Se c'e' un solo valore per il parametro 'sqlName', allora la query e' anche recuperabile,  
				// direttamente come stringa, dall'attributo con chiave uguale ad 'sqlName'.
				req.setAttribute( this.nomiQueriesDaCompilare[0], (queries.get(this.nomiQueriesDaCompilare[0])).get(0) ); // giusto per comodita' in caso di un unica query da gestire
				req.setAttribute( "queryRecuperata", (queries.get(this.nomiQueriesDaCompilare[0])).get(0) ); // retro compatibilita' con sqlManagerFilter
				
				// Essendo possibile specificare piu' di un valore per il parametro sqlName
				// viene creata anche una mappa con chiave sqlName[i] e valore Query[i]
				// nel caso sia fornito il parametro 'sqlLoaderParametriUpdate' la mappa avra' chiave sqlName[i] e valore List<Query[i]>
				req.setAttribute( contenitoreMultiQueries, queries );
				req.setAttribute( "sqlManagerMappaQueries", queries ); // retro compatibilita' con sqlManagerFilter
				
			} catch ( QueriesUtilsException e ) {
				if ( this.printStackTrace )
					e.printStackTrace();
				throw new SqlLoaderFilterException(  "Filtro " + this._filterConfig.getFilterName()
						+ ": errore caricamento query da file. Classe: "+this.getClass().getName(), e );
			}
			
		} else {
			// TODO recupero query senza compilarla
			
		}
		chain.doFilter( req, resp );
//		System.out.println( this._filterConfig.getFilterName() +"\n" + req.getSession().getAttribute( "JsonDataTableString" ) + " - " + req.getSession().getAttribute( "DataTableQuery" ));
	}
	
	/**
	 * Dalla request recupera 'sqlName' (parametro) oppure 'sqlOverride' (attributo)
	 * @param req HttpServletRequest
	 * @return
	 * @throws SqlLoaderFilterException
	 */
	private String[] getNomiQueriesDaCompilare(HttpServletRequest req) throws SqlLoaderFilterException {
		
		String [] nomiQueriesDaCompilare = req.getParameterValues( "sqlName" );
		
		// sqlOverride puo' arrivare solo da una servlet e ha precedenza su sqlName
		Object sqlOverride = req.getAttribute("sqlOverride");
		
		if (sqlOverride instanceof String[]) {
			nomiQueriesDaCompilare = (String[]) sqlOverride;
		} else if (sqlOverride instanceof String) {
			nomiQueriesDaCompilare = new String[] { (String) sqlOverride };
		}		
		
		if (nomiQueriesDaCompilare == null)
			throw new SqlLoaderFilterException( "Filtro " + this._filterConfig.getFilterName() + ": errore, parametro sqlName mancante nella request." );
		
		return nomiQueriesDaCompilare;

	}
	
	/**
	 * Raccoglie tutti i <code>parameters</code> della <code>request</code> compresi <code>attributes</code> eventualmente aggiunti da servlet o filtri precedenti e parametri custom contenuti in mappe inserite in sessione o in un attribute della request
	 * @param req
	 * @return
	 */
	private Map<String, Object> getAllParamsMap(HttpServletRequest req) {	
		Map<String, Object> allParams = new HashMap<String, Object>();
		allParams.putAll( this.get_requestParamsMap( req ) );
		allParams.putAll( this.get_requestMultipleValuesParamsMap( req ) );
//		allParams.putAll( this._initConfigParamsMap ); // TODO prevedere caricamento parametri da file, magari su base di sqlName
		allParams.putAll( this.get_requestAttributes( req ) );
		allParams.putAll( this.getParametriAggiuntivi(req) );
		return allParams;
	}
	
	/**
	 * 
	 * @throws SqlLoaderFilterException 
	 */
	private void loadConfiguration() throws SqlLoaderFilterException {
		// Cerca nel web.xml il nome del file contenente la configurazione di questo Filter (completo di percorso relativo) 
		this.thisConfigFile = this._initConfigParamsMap.get("SqlLoaderConf_File");
		// Carica la configurazione per SqlLoaderFilter da file.
		this.thisProperties = this.loadConfigFile( thisConfigFile );		
		// Percorso completo del file contenente il repository delle queries
		this.queriesRepositoryFileName = thisProperties.get("SqlLoaderQueriesRepos_File").toString(); //TODO sotto try/catch
		// Crea l'oggetto Properties con il repository delle queries // TODO visto l'utilizzo forse è meglio trasformarlo in una mappa -> cambiare anche queruUtils
		this.queriesRepository = this.loadConfigFile( queriesRepositoryFileName );
		// Percorso dei file contenenti le queries
		this.queriesRepoFolderFullPath = this._appRealPath + thisProperties.get("SqlLoaderConf_Path");
	}
	
	/**
	 * Legge dalla request eventuali opzioni inserite nella chiamata e imposta le relative variabili di istanza con i valori recuperati
	 * @param req
	 */
	private void retrieveOptions(HttpServletRequest req) {
		// Opzione per verbose stacktrace delle eccezioni catturate
		this.printStackTrace = BooleanUtils.toBoolean(req.getParameter("printStackTrace"));
		// Opzione per abilitare il log
		this.isLogEnabled =  BooleanUtils.toBoolean(req.getParameter("logSqlLoader"));
		// Opzione per evitare la compilazione automatica della query. Fondamentalmente recupera la query e la serve immutata TODO
		this.lasciaParametrica = BooleanUtils.toBoolean(req.getParameter("lasciaQueryParametrica"));
	}
	
	/**
	 * 
	 * @param req
	 * @return
	 */
	@SuppressWarnings( "unchecked" )
	private Map<String, Object> getParametriAggiuntivi(HttpServletRequest req) {
		// Se nella chiamata del jsp sono presenti uno o entrambi i seguenti parametri viene cercata nella session e/o nella request
		// un attributo con chiave corrispondente a quella/e recuperata/e attraverso questi due parametri.
		String idParametriAggiuntiviDaSessione = req.getParameter( "LoaderSessionCustomParamsMap" );
		String idParametriAggiuntiviOnCallingChain = (String) req.getParameter("LoaderCustomParamsMap"); // aggiunti da servlet nella catena di chiamate 

		// tenta il recupero dalla sessione di una mappa con eventuali parametri custom che non e' stato possibile (o consigliabile) inserire nel jsp
		// Es. chiamata JSP: /servlet/blabla?do=something&LoaderSessionCustomParamsMap=miaMappaParametriCustom
		Map<String, Object> mappaParametriAggiuntivi = null;
		if (idParametriAggiuntiviDaSessione != null ) // cerchera' in sessione un attribute con chiave 'miaMappaParametriCustom'
			mappaParametriAggiuntivi = (Map<String, Object>) req.getSession().getAttribute(idParametriAggiuntiviDaSessione);

		// tenta il recupero dalla request di una mappa con eventuali parametri custom che non e' stato possibile (o consigliabile) inserire nel jsp
		Map<String, Object> mappaParametriAggiuntiviOnCallingChain = null;
		if (idParametriAggiuntiviOnCallingChain != null)
		mappaParametriAggiuntiviOnCallingChain = (Map<String, Object>) req.getAttribute(idParametriAggiuntiviOnCallingChain);

		// unisce le due mappe o alla peggio crea una mappa vuota (no parametri custom)
		if (mappaParametriAggiuntivi == null) 
			mappaParametriAggiuntivi = new HashMap<String, Object>();
		if (mappaParametriAggiuntiviOnCallingChain != null)
			mappaParametriAggiuntivi.putAll( mappaParametriAggiuntiviOnCallingChain );
		
		
		return mappaParametriAggiuntivi;
	}
	
	/**
	 * Carica e restituisce un oggetto {@link Properties}
	 * @param propertiesFile
	 * @throws SqlLoaderFilterException
	 */
	private Properties loadConfigFile(String propertiesFile) throws SqlLoaderFilterException
	{
		Properties dtrSettings = new Properties();
//		Yaml yaml = new Yaml(); // TODO
		try {
			// carica, dal file passato, l'oggetto Properties salvandolo in un campo della servlet 
			dtrSettings.load( this._ctx.getResourceAsStream( propertiesFile ) );
			
		} catch ( IOException e ) {
			if ( printStackTrace )
				e.printStackTrace();
			throw new SqlLoaderFilterException( "Filtro " + this._filterConfig.getFilterName()
					+ ": errore caricamento file configurazione. Classe: "+this.getClass().getName(), e );
		}
		
		return dtrSettings;
	}
}
