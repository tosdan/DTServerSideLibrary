package com.github.tosdan.dataTableServerSide;

import java.io.IOException;
import java.sql.Connection;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.tosdan.dataTableServerSide.exceptions.DTReplayServletException;
import com.github.tosdan.dataTableServerSide.exceptions.DTReplyDAOExcetion;
import com.github.tosdan.utils.servlets.BasicHttpServlet;
import com.github.tosdan.utils.servlets.SqlManagerServletException;
import com.github.tosdan.utils.sql.ConnectionProvider;
import com.github.tosdan.utils.sql.ConnectionProviderException;
import com.github.tosdan.utils.sql.ConnectionProviderImpl;

/**
 * 
 * @author Daniele
 * @version 0.9
 */
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
		try {
			querySql = req.getAttribute("queryRecuperata").toString();
		} catch ( NullPointerException e ) {
			throw new DTReplayServletException( "Servlet " + this.getServletName() + 
					": errore, attributo queryRecuperata mancante/nullo nella request." );
		}
		this.processRequestForParams( req );
		
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

	
	/**
	 * Recupera i dati da inviare in risposta alla <code>DataTable</code>
	 * @param parametriDataTable i parametri inviati dalla <code>DataTable</code> reperibili dalla request
	 * @param querySql query opportunamente elaborata in base ai parametri inviati dalla <code>DataTable</code>
	 * @return un oggetto <code>JSON</code> memorizzato in una stringa
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
	 * Stampa semplicemente in console le query eseguite
	 * TODO da utilizzare con un qualche oggetto logger, nel frattempo stampa a console
	 * @param s
	 */
	private void addToLog(String s) {
		System.out.println( "-- " + this.getClass().getName() + ": sqlDati\n" + s );
		
	}
	
	/**
	 * Configura e restituisce un {@link ConnectionProvider}
	 * @param file_dbConf file {@link Properties} contenente la confiurazione per l'accesso al database.
	 * @return un oggetto {@link ConnectionProvider} dal quale poter ottenere, senza ulteriori configurazioni, un oggetto {@link Connection} 
	 * @throws SqlManagerServletException 
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
