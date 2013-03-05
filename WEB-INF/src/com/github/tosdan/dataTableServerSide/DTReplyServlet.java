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
		ServletOutputStream out = resp.getOutputStream();
		this.printStackTrace = req.getParameter( "printStackTrace" );
		String reqLog = this.processRequestForParams( req );
		
		String debugReqParams = req.getParameter("debugRequestParams");
		if (debugReqParams != null && debugReqParams.equalsIgnoreCase( "true" )) {
			PrintWriter logWriter = new PrintWriter( this.app.getRealPath(this.envConfigParams.get("logFileName")) );
			logWriter.println(reqLog);
			logWriter.close(); 
		}
		
		/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

		String dtrConfFile = this.envConfigParams.get( "DTReplyConf_File" );
		String dtrConfPath = this.realPath + this.envConfigParams.get( "DTReplyConf_Path" );
		Properties dtrSettings = new Properties();
		try {
			dtrSettings.load( this.app.getResourceAsStream( dtrConfFile ) );
		} catch ( IOException e2 ) {
			if ( printStackTrace != null && printStackTrace.equalsIgnoreCase("true") )
				e2.printStackTrace();
			throw new DTReplayServletException( "Errore caricamento file configurazione DTReplyConf.", e2 );
		}

		/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

		String nomeSql = req.getParameter( "sqlName" );
		String tipoSql = req.getParameter( "sqlType" );
		if (nomeSql == null || tipoSql == null) 
			throw new DTReplayServletException( "Errore: parametro/i sqlName e/o sqlSource mancante/i nella request." );
		
		Map<String, String> allParams = new HashMap<String, String>();
		allParams.putAll( this.requestParams );
		allParams.putAll( this.envConfigParams );
		
		String querySql = "";
		String queryFile = dtrSettings.getProperty( nomeSql );
		try {
			querySql = QueriesUtils.compilaQueryDaFile( (dtrConfPath + queryFile), nomeSql, allParams, (new MapFormatTypeValidatorSQL()) );
		} catch ( IOException e1 ) {
			if ( printStackTrace != null && printStackTrace.equalsIgnoreCase("true") )
				e1.printStackTrace();
			throw new DTReplayServletException( "Errore caricamente query da file.", e1 );
		}
		
		if ( !querySql.isEmpty() ) {
			
			if ( tipoSql.equalsIgnoreCase("query") ) 
				querySql = "( "+ querySql +" ) AS tabella";
			else if ( tipoSql.equalsIgnoreCase("table") ) 
				querySql = "( SELECT * FROM "+ querySql +" ) AS tabella";
			
		}

		/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

		resp.setContentType("text/html; charset=ISO-8859-1");
		String json = this.getDati(this.requestParams, querySql);
		out.print( json );
		
		String debugJson = req.getParameter("debugJson");
		if ( debugJson != null && debugJson.equalsIgnoreCase("true") )
			System.out.println( json );
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
		DTReplySqlProvider dtReplySqlProvider = new DTReplySqlProvider(parametriDataTable, querySql);
		
		String stampaQuery = parametriDataTable.get("stampaQuery");
		if ( stampaQuery != null && stampaQuery.equalsIgnoreCase("true") ) { 		
			this.addToLog( dtReplySqlProvider.getSqlDatiQuery() );
			this.addToLog( dtReplySqlProvider.getSqlDatiAggiuntivi() );
		}
		
		DTReplyProvider dbc = new DTReplyProvider( parametriDataTable, dtReplySqlProvider );
		String noMData = parametriDataTable.get("mDataBinded");
		if ( noMData != null && noMData.equalsIgnoreCase("false") ) 
			dbc.setIndexedOutout();
		
		String file_dbConf = this.realPath + this.envConfigParams.get( "file_dbConf" );
		
		try {
			return dbc.buildJsonPerDataTables( this.getConnectionProvider(file_dbConf) );
		
		} catch ( DTReplyDAOExcetion e ) {
			if ( printStackTrace != null && printStackTrace.equalsIgnoreCase("true") )
				e.printStackTrace();
			throw new DTReplayServletException( "Errore di connessione al database.", e );
		}
	}
	
	/**
	 * TODO da utilizzare con un qualche oggetto logger 
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
			throw new DTReplayServletException( "Errore creazione ConnectionProvider.", e );
		}
	}
	
} // chiusura classe
