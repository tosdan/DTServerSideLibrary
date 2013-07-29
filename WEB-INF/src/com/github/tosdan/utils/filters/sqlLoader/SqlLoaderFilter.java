package com.github.tosdan.utils.filters.sqlLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;

import com.github.tosdan.utils.filters.BasicFilterV2;
import com.github.tosdan.utils.io.FileUtils;
import com.github.tosdan.utils.sql.MassiveQueryCompiler;
import com.github.tosdan.utils.stringhe.TemplateCompilerException;
import com.github.tosdan.utils.stringhe.TemplatePickerSections;
/**
 * 
 * @author Daniele
 * @version 0.2.2-b2013-07-23
 */
public class SqlLoaderFilter extends BasicFilterV2
{
	private String[] nomiQueriesDaCompilare;
	private List<Map<String, Object>> updateParamsMapsList;
	private Map<String, String> queriesRepositoryIndexMap;
	private String queriesRepoFolderPath;
	private boolean isLogEnabled;
	private boolean lasciaParametrica;
	private boolean printStackTrace;
	private Map<String, String> sqlLoaderSettings;
	private String queriesRepositoryIndexFilename;

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

		this.getSqlLoaderOptions(req);
		this.loadConfiguration();

		String reqLog = this.getRequestParamsProcessLog( req );
		if ( this.isLogEnabled && this.sqlLoaderSettings.get("logFileName") != null ) 
			// crea un file di log con il nome passato come parametro nella sottocartella della webapp
			FileUtils.toFile( this.realPath + sqlLoaderSettings.get("logFileName"), reqLog );

		/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		boolean retroCompatibilitaTemplate = BooleanUtils.toBoolean(req.getParameter("noYamlPicker"));
		
		if ( ! this.lasciaParametrica ) {
			Map<String, Object> allPossibleParams = this.getAllParamsMap( req );
			MassiveQueryCompiler mqc = new MassiveQueryCompiler( this.queriesRepositoryIndexMap, this.queriesRepoFolderPath, this.updateParamsMapsList );
			
			if ( retroCompatibilitaTemplate  )
				mqc.setPicker( new TemplatePickerSections() ); // TODO convertire i template delle query vecchie in yaml

			try {
				Map<String, List<String>> queries = mqc.getQueriesListMap( this.nomiQueriesDaCompilare, allPossibleParams );
				
				// Se c'e' un solo valore per il parametro 'sqlName', allora la query e' anche recuperabile,  
				// direttamente come stringa, dall'attributo con chiave uguale ad 'sqlName'.
				req.setAttribute( this.nomiQueriesDaCompilare[0], (queries.get(this.nomiQueriesDaCompilare[0])).get(0) ); // giusto per comodita' in caso di un unica query da gestire
				req.setAttribute( "queryRecuperata", (queries.get(this.nomiQueriesDaCompilare[0])).get(0) ); // retro compatibilita' con sqlManagerFilter
				
				// Essendo possibile specificare piu' di un valore per il parametro sqlName
				// viene creata anche una mappa con chiave sqlName[i] e valore Query[i]
				// nel caso sia fornito il parametro 'sqlLoaderParametriUpdate' la mappa avra' chiave sqlName[i] e valore List<Query[i]>
				req.setAttribute( contenitoreMultiQueries, queries );
				req.setAttribute( "sqlManagerMappaQueries", queries ); // retro compatibilita' con sqlManagerFilter
				
			} catch ( TemplateCompilerException e ) {
				if ( this.printStackTrace )
					e.printStackTrace();
				throw new SqlLoaderFilterException(  "Filtro " + this.filterConfig.getFilterName()
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
			throw new SqlLoaderFilterException( "Filtro " + this.filterConfig.getFilterName() + ": errore, parametro sqlName mancante nella request." );
		
		return nomiQueriesDaCompilare;

	}
	
	/**
	 * Raccoglie tutti i <code>parameters</code> della <code>request</code> compresi <code>attributes</code> eventualmente aggiunti da servlet o filtri precedenti e parametri custom contenuti in mappe inserite in sessione o in un attribute della request
	 * @param req
	 * @return
	 */
	private Map<String, Object> getAllParamsMap(HttpServletRequest req) {	
		Map<String, Object> allParams = new HashMap<String, Object>();
		allParams.putAll( this.getRequestParamsMap( req ) );
		allParams.putAll( this.getRequestMultipleValuesParamsMap( req ) );
//		allParams.putAll( this._initConfigParamsMap ); // TODO prevedere caricamento parametri costanti da file, magari su base di sqlName
		allParams.putAll( this.getRequestAttributes( req ) );
		allParams.putAll( this.getParametriAggiuntivi(req) );
		return allParams;
	}
	
	/**
	 * 
	 * @throws SqlLoaderFilterException 
	 */
	private void loadConfiguration() throws SqlLoaderFilterException {
		// Cerca nel web.xml il nome del file contenente la configurazione di questo Filter (completo di percorso relativo) 
		String sqlLoaderConfigFile = this.initConfigParamsMap.get("SqlLoaderConf_File");
		// Carica la configurazione per SqlLoaderFilter da file.
		this.sqlLoaderSettings = this.loadMapFromConfFile( sqlLoaderConfigFile );
		
		// Percorso completo del file contenente l'index delle queries
		this.queriesRepositoryIndexFilename = this.sqlLoaderSettings.get("SqlLoaderQueriesReposIndex_File").toString(); //TODO sotto try/catch
		// Crea una mappa con l'index delle queries
		this.queriesRepositoryIndexMap = this.loadMapFromConfFile( this.queriesRepositoryIndexFilename );
		// Percorso assoluto dei file contenenti i templates delle queries
		this.queriesRepoFolderPath = this.realPath + this.sqlLoaderSettings.get("SqlLoaderConf_Path");
		
	}
	
	/**
	 * Legge dalla request eventuali opzioni inserite nella chiamata e imposta le relative variabili di istanza con i valori recuperati
	 * @param req
	 */
	private void getSqlLoaderOptions(HttpServletRequest req) {
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
	 * Carica e restituisce una mappa con i dati di configurazione contenuti nel file passato.
	 * @param configFile
	 * @throws SqlLoaderFilterException
	 */
	@SuppressWarnings( "unchecked" )
	private Map<String, String> loadMapFromConfFile(String configFile) throws SqlLoaderFilterException
	{
		Map<String, String> contentMap;
		Yaml yaml = new Yaml();
		
		try {
			InputStream is =  this.ctx.getResourceAsStream( configFile );
			contentMap = (Map<String, String>) yaml.load( is );
			is.close();
			
		} catch ( IOException e ) {
			if ( this.printStackTrace )
				e.printStackTrace();
			throw new SqlLoaderFilterException( "Filtro " + this.filterConfig.getFilterName()
					+ ": errore caricamento file configurazione. Classe: "+this.getClass().getName(), e );
		}
		
		return contentMap;
	}
}
