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
 * Prende una query (o una tabella) ed elabora una query in base alle impostazioni richieste dalla DataTable
 * @param queryRecuperata <code>Request Attribute</code> in cui deve essere passata la query da eseguire
 * @param logDTReplyReqParams <code>Request Parameter</code> Flag (<code>true</code>/<code>false</code>) per determinare se debbano essere stampati nel log i parametri letti nella request
 * @param sqlType <code>Request Parameter</code> Identificativo (<code>query</code>/<code>table</code>) che specifica se viene passata una query vera e propria o il nome di una tabella (su cui verrà eseguita una SELECT *) 
 * @param debugJson <code>Request Parameter</code> Flag (<code>true</code>/<code>false</code>) specifica se stampare a console l'oggetto JSON generato
 * @param printStackTrace <code>Request Parameter</code> Flag (<code>true</code>/<code>false</code>) per determinare se lo <code>stacktrace</code> delle eccezioni catturate debba esser stampato in console (<code>System.err</code>) o meno
 * @return <code>text/plain</code> contenente un oggetto JSON da dare in ingresso alla DataTable
 * @author Daniele
 * @version 0.9
 */
@SuppressWarnings( "serial" )
public class DTReplyServlet extends BasicHttpServlet
{
	private boolean printStackTrace;
	private String queryEseguita;
	
	@Override 
	protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException { this.doPost( req, resp ); }

	@Override
	protected void doPost( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException
	{
		// flag per verbose stacktrace delle eccezioni catturate da questa servlet
		this.printStackTrace = this._booleanSafeParse( req.getParameter("printStackTrace") );
		// query da eseguire		
		String querySql = "";
		try {
			querySql = req.getAttribute("queryRecuperata").toString();
		} catch ( NullPointerException e ) {
			throw new DTReplayServletException( "Servlet " + this.getServletName() + 
					": errore, attributo queryRecuperata mancante/nullo nella request." );
		}
		String reqLog = this._processRequestForParams( req );
		if (this._booleanSafeParse( req.getParameter("logDTReplyReqParams") ) && this._initConfigParamsMap.get("logFileName") != null ) 
			// crea un file di log con il nome passato come parametro nella sottocartella della webapp
			this._logOnFile( this._ctx.getRealPath(this._initConfigParamsMap.get("logFileName")), reqLog );
		
		/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

		// identificativo per distinguere una query da una vera e propria tabella
		String tipoSql = req.getParameter( "sqlType" );
		if (tipoSql == null) 
			throw new DTReplayServletException( "Servlet " + this.getServletName() + 
					": errore, parametro sqlType mancante nella request." );
		
		// racchiude la query/tabella da passare come "seme" ad sqlProvider 
		if ( !querySql.isEmpty() ) {			
			if ( tipoSql.equalsIgnoreCase("query") ) 
				querySql = "( "+ querySql +" ) AS tabella";
			else if ( tipoSql.equalsIgnoreCase("table") ) 
				querySql = "( SELECT * FROM "+ querySql +" ) AS tabella";	
		}

		/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

		// recupera l'oggetto JSON da usare come valore di ritorno
		String json = this.getDati(this._requestParamsMap, querySql);
		
		ServletOutputStream out = resp.getOutputStream();
		resp.setContentType("text/html; charset=ISO-8859-1");
		// restituisce il JSON
		out.print( json );
		this._toSession( "JsonDataTableString", json , req );
		this._cryptToSession( "DataTableQuery", this.queryEseguita, req ); // TODO criptare
//		req.getSession().setAttribute( "JsonDataTableString", json );
//		req.getSession().setAttribute( "DataTableQuery", this.queryEseguita );
		
		if ( this._booleanSafeParse( req.getParameter("debugJson") ) )
			System.out.println( json );
		
	} //*** close doPost

	
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
		// oggetto che rielabora la query in base alle impostazioni passate dalla DataTable
		DTReplySqlProvider dtrSqlProvider = new DTReplySqlProvider(parametriDataTable, querySql);
		this.queryEseguita = dtrSqlProvider.toString();
		if ( this._booleanSafeParse(parametriDataTable.get("stampaQuery")) ) {
			System.out.println( "-- Query per " + this.getClass().getName() + "\n"+ this.queryEseguita );
		}
		// oggetto che fornisce la risposta da inoltrare alla DataTable
		DTReplyProvider dtRProvider = new DTReplyProvider( parametriDataTable, dtrSqlProvider );
		
		// specifica se l'output e' legato alla configurazione del javascript (parametri mData) o se indicizzato e quindi (pressapoco) "dinamico"
		if ( ! this._booleanSafeParse(parametriDataTable.get("mDataBinded")) ) 
			dtRProvider.setIndexedOutout();
		
		// file con la configurazione per l'accesso al database
		String fileDBConf = this._appRealPath + this._initConfigParamsMap.get( "file_dbConf" );
		
		try {
			// ottiene l'oggetto JSON da restituire
			return dtRProvider.buildJsonPerDataTables( this.getConnectionProvider(fileDBConf) );
		
		} catch ( DTReplyDAOExcetion e ) {
			if ( printStackTrace )
				e.printStackTrace();
			throw new DTReplayServletException( "Servlet " + this.getServletName() 
					+ ": errore di accesso al database. Classe: "+this.getClass().getName(), e );
		}
	}
	
	/**
	 * Configura e restituisce un {@link ConnectionProvider}
	 * @param fileDBConf file {@link Properties} contenente la confiurazione per l'accesso al database.
	 * @return un oggetto {@link ConnectionProvider} dal quale poter ottenere, senza ulteriori configurazioni, un oggetto {@link Connection} 
	 * @throws SqlManagerServletException 
	 */
	private ConnectionProvider getConnectionProvider(String fileDBConf)
			throws DTReplayServletException
	{
		try {
			return new ConnectionProviderImpl( fileDBConf );
			
		} catch ( ConnectionProviderException e ) {
			if ( printStackTrace )
				e.printStackTrace();
			throw new DTReplayServletException( "Servlet " + this.getServletName()
					+": errore creazione ConnectionProvider. Classe: "+this.getClass().getName(), e );
		}
	}
	
} // chiusura classe
