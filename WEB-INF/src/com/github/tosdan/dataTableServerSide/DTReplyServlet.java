package com.github.tosdan.dataTableServerSide;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.tosdan.dataTableServerSide.exceptions.DTReplayServletException;
import com.github.tosdan.dataTableServerSide.exceptions.DTReplyDAOExcetion;
import com.github.tosdan.utils.servlets.BasicHttpServlet;
import com.github.tosdan.utils.sql.ConnectionProvider;
import com.github.tosdan.utils.sql.ConnectionProviderException;
import com.github.tosdan.utils.sql.ConnectionProviderImpl;
import com.github.tosdan.utils.sql.QueriesUtils;
import com.github.tosdan.utils.stringhe.MapFormatTypeValidator;
import com.github.tosdan.utils.stringhe.MapFormatTypeValidatorSQL;

@SuppressWarnings( "serial" )
public class DTReplyServlet extends BasicHttpServlet
{
	private String printStackTrace;
	
	@Override 
	protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException { this.doPost( req, resp ); }

	@Override
	protected void doPost( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException
	{
		// query da eseguire
		String querySql = "";
		Object queryRecuperata = req.getAttribute("queryRecuperata");
		
		if (queryRecuperata == null)
		{
			// inizializza la mappa contenente i parametri della request
			String reqLog = this.processRequestForParams( req );
			if (req.getParameter("debugRequestParams") != null && req.getParameter("debugRequestParams").equalsIgnoreCase( "true" ) && this.envConfigParams.get("logFileName") != null ) 
				// crea un file di log con il nome passato come parametro nella sottocartella della webapp
				this.logOnFile( this.app.getRealPath(this.envConfigParams.get("logFileName")), reqLog );
			// flag per verbose stacktrace
			this.printStackTrace = req.getParameter( "printStackTrace" ); 
			// percorso file settings
			String queriesRepoFolderFullPath = this.realPath + this.envConfigParams.get( "DTReplyConf_Path" );
			// nome file settings
			String dtrPropertiesFile = this.envConfigParams.get( "DTReplyConf_File" );
			// carica la configurazione dal file properties
			Properties dtrProperties = this.loadProperties( dtrPropertiesFile );
			
			/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
			// identificativo query nel file repository delle queries
			String nomeSQL = req.getParameter( "sqlName" );
			if (nomeSQL == null) 
				throw new DTReplayServletException( "Servlet " + this.getServletName() + 
						": errore, parametro sqlName mancante nella request." );
			// raccoglie i parametri della request e di initConf della servlet
			Map<String, String> allParams = new HashMap<String, String>();
			allParams.putAll( this.requestParams );
			allParams.putAll( this.envConfigParams );
			
			try {
				// istanza l'oggetto per la validazione dei parametri rispetto ai valori effettivamente passati per evitare problemi sui Tipi
				MapFormatTypeValidator validator = new MapFormatTypeValidatorSQL();
				// compila la query parametrica sostituendo ai parametri i valori contenuti nella request e nell'initConf della servlet 
				querySql = QueriesUtils.compilaQueryDaFile( dtrProperties, queriesRepoFolderFullPath , nomeSQL, allParams, validator );
			} catch ( IOException e1 ) {
				if ( printStackTrace != null && printStackTrace.equalsIgnoreCase("true") )
					e1.printStackTrace();
				throw new DTReplayServletException( "Servlet " + this.getServletName() 
						+ ": errore caricamente query da file. Classe: "+this.getClass().getName(), e1 );
			}
		} else {
			querySql = queryRecuperata.toString();
			this.processRequestForParams( req );
		}
		/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

		// identificativo per distinguere una query da una vera e propria tabella
		String tipoSql = req.getParameter( "sqlType" );
		if (tipoSql == null) 
			throw new DTReplayServletException( "Servlet " + this.getServletName() + 
					": errore, parametro sqlType mancante nella request." );
		
		// racchiude la query/tabella da passare come "seme" all' sqlProvider 
		if ( !querySql.isEmpty() ) {			
			if ( tipoSql.equalsIgnoreCase("query") ) 
				querySql = "( "+ querySql +" ) AS tabella";
			else if ( tipoSql.equalsIgnoreCase("table") ) 
				querySql = "( SELECT * FROM "+ querySql +" ) AS tabella";	
		}

		/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

		// recupera l'oggetto JSON da usare come valore di ritorno
		String json = this.getDati(this.requestParams, querySql);
		ServletOutputStream out = resp.getOutputStream();
		resp.setContentType("text/html; charset=ISO-8859-1");
		// restituisce il JSON
		out.print( json );
		
		String debugJson = req.getParameter("debugJson");
		if ( debugJson != null && debugJson.equalsIgnoreCase("true") )
			System.out.println( json );
		
	} //close doPost

	protected void logOnFile(String fileName, String content) throws IOException
	{
		// crea un file di log con il nome passato come parametro nella sottocartella della webapp
		PrintWriter logWriter = new PrintWriter( fileName );
		logWriter.println(content);
		logWriter.close(); 
	}
	
	/**
	 * Se nullo recupera da file l'oggetto properties con la configurazione
	 * @param propertiesFile
	 * @throws DTReplayServletException
	 */
	protected Properties loadProperties(String propertiesFile) throws DTReplayServletException
	{
		Properties dtrSettings = new Properties();
		
		try {
			// carica, dal file passato, l'oggetto Properties salvandolo in un campo della servlet 
			dtrSettings.load( this.app.getResourceAsStream( propertiesFile ) );
			
		} catch ( IOException e2 ) {
			if ( printStackTrace != null && printStackTrace.equalsIgnoreCase("true") )
				e2.printStackTrace();
			throw new DTReplayServletException( "Servlet " + this.getServletName()
					+ ": errore caricamento file configurazione. Classe: "+this.getClass().getName(), e2 );
		}
		
		return dtrSettings;
	}
	
	/**
	 * 
	 * @param parametriDataTable
	 * @param querySql
	 * @return
	 * @throws DTReplayServletException
	 */
	private String getDati(Map<String, String> parametriDataTable, String querySql) 
			throws DTReplayServletException
	{
		DTReplySqlProvider dtrSqlProvider = new DTReplySqlProvider(parametriDataTable, querySql);
		
		String stampaQuery = parametriDataTable.get("stampaQuery");
		if ( stampaQuery != null && stampaQuery.equalsIgnoreCase("true") ) { 		
			this.addToLog( dtrSqlProvider.getSqlDatiQuery() );
			this.addToLog( dtrSqlProvider.getSqlDatiAggiuntivi() );
		}
		
		DTReplyProvider dbc = new DTReplyProvider( parametriDataTable, dtrSqlProvider );
		
		// specifica se l'output e' legato alla configurazione del javascript (parametri mData) o se indicizzato e quindi (pressapoco) "dinamico"
		String noMData = parametriDataTable.get("mDataBinded");
		if ( noMData != null && noMData.equalsIgnoreCase("false") ) 
			dbc.setIndexedOutout();
		
		String file_dbConf = this.realPath + this.envConfigParams.get( "file_dbConf" );
		
		try {
			return dbc.buildJsonPerDataTables( this.getConnectionProvider(file_dbConf) );
		
		} catch ( DTReplyDAOExcetion e ) {
			if ( printStackTrace != null && printStackTrace.equalsIgnoreCase("true") )
				e.printStackTrace();
			throw new DTReplayServletException( "Servlet " + this.getServletName() 
					+ ": errore di accesso al database. Classe: "+this.getClass().getName(), e );
		}
	}
	
	/**
	 * TODO da utilizzare con un qualche oggetto logger, nel frattempo stampa a console
	 * @param s
	 */
	private void addToLog(String s) {
		System.out.println( "-- " + this.getClass().getName() + ": sqlDati\n" + s );
	}
	
	/**
	 * 
	 * @param file_dbConf
	 * @return
	 * @throws DTReplayServletException
	 */
	private ConnectionProvider getConnectionProvider(String file_dbConf)
			throws DTReplayServletException
	{
		try {
			return new ConnectionProviderImpl( file_dbConf );
		} catch ( ConnectionProviderException e ) {
			if ( printStackTrace != null && printStackTrace.equalsIgnoreCase("true") )
				e.printStackTrace();
			throw new DTReplayServletException( "Servlet " + this.getServletName()
					+": errore creazione ConnectionProvider. Classe: "+this.getClass().getName(), e );
		}
	}
	
} // chiusura classe
