package com.github.tosdan.dTReply;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;

import com.github.tosdan.utils.sql.SqlLoader;
import com.github.tosdan.utils.servlets.HttpReallyBasicServlet;
import com.github.tosdan.utils.servlets.ServletUtils;
import com.github.tosdan.utils.sql.ConnectionProviderException;
import com.github.tosdan.utils.sql.ConnectionProviderImplV2;
import com.github.tosdan.utils.varie.JsonUtils;
import com.github.tosdan.utils.varie.YamlUtils;

/**
 * @param DebugDTReply <code>Request Parameter</code> Flag (<code>true</code>/<code>false</code>) abilita la modalita' debug
 * @return <code>text/plain</code> contenente un oggetto JSON da dare in ingresso alla DataTable
 * 
 * @author Daniele
 * @version 0.0.1-b2013-09-14
 */
@SuppressWarnings( "serial" )
public class DTReplyServlet extends HttpReallyBasicServlet
{
	private boolean debug;
	private boolean printStackTrace;
	
	protected void setDebug(boolean debug) {
		this.debug = debug;
	}
	protected boolean isDebug() {
		return debug;
	}
	private boolean isPrintStackTrace() {
		return printStackTrace;
	}
	private void setPrintStackTrace( boolean printStackTrace ) {
		this.printStackTrace = printStackTrace;
	}
	
	@Override
	public void doService( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException {
		parseDebugOption(req);
		parseStackTraceOption(req);
		parseOptions(req);
		
		String jsonRetVal = null;
		
		String sql = getSql(req);
		
		if (sql != null) {
			Map<String, String> dtParams = getDataTableParams(req);
			Map<String, Object> reply = getReply(dtParams, sql, req.getSession());
			
			if (reply != null)
				jsonRetVal = getJson(reply, req.getSession());
			
			else 
				jsonRetVal = getJson(getMap("error", "Errore recupero dati."));
		}
		else 
			jsonRetVal = getJson(getMap("error", "Errore recupero query."));
		
		
		
		outputJson(resp, jsonRetVal);
	}
	
	
	/**
	 * Restituisce una mappa contenente l'associazione key -> msg
	 * @param key chiave
	 * @param msg messaggio (presumibilmente d'errore)
	 * @return
	 */
	protected Map<String, Object> getMap(String key, String msg) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(key, msg);
		return map;
	}
	
	
	/**
	 * Restituisce una stringa in formato JSON.
	 * @param reply Mappa dei dati recuperati da db che rispetti lo schema del modello di datatable
	 * @return stringa json
	 */
	protected String getJson( Map<String, Object> reply ) {
		return getJson(reply, null);
	}
	/**
	 * Restituisce una stringa in formato JSON.
	 * @param reply Mappa dei dati recuperati da db che rispetti lo schema del modello di datatable
	 * @param session Sessione corrente. Se viene passato un oggetto <code>HttpSession</code> non nullo la stringa json viene memorizata in session nell'attributi <code>JsonDataTableString</code> 
	 * @return stringa json
	 */
	protected String getJson( Map<String, Object> reply, HttpSession session ) {
		String json = JsonUtils.toJSON(reply);
		if (isDebug()) 
			logProxy("**** JSON ****\n" + json);
		if (session != null)
			session.setAttribute( "JsonDataTableString", json );
		return json;
	}

	
	/**
	 * Recupera dalla <code>request</code> i parametri inviati dalla dataTable. 
	 * @param req Oggetto <code>request</code>
	 * @return Mappa con i parametri
	 */
	protected Map<String, String> getDataTableParams(HttpServletRequest req) {
		return ServletUtils.getReqSingleValueParams(req); // Ovvio che cosi' ce ne finisce qualcuno in piu' ma chissene, l'importante e' che sia una Map<String, String>
	}

	
	/**
	 * Invia una response contenente il JSON appena elaborato. 
	 * @param resp Oggetto <code>response</code>
	 * @param json Stringa JSON
	 * @throws IOException
	 */
	protected void outputJson(HttpServletResponse resp, String json) throws IOException {
		PrintWriter out = resp.getWriter();
		resp.setHeader("Content-Type", "application/json");
		resp.setContentType("application/json; charset=UTF-8");
		out.print( json );
	}
	
	
	/**
	 * Recupera la query sql di base. L'unico requesito di questa query è di saper estrarre tutti i dati, senza alcun filtro ne' elaborazione.
	 * Elaborazioni come filtro, paginazione e ordinamento vengono effettuate altrove. 
	 * @param req Oggetto <code>request</code>
	 * @return query sql
	 * @throws IOException
	 * @throws ServletException
	 */
	protected String getSql(HttpServletRequest req) throws IOException {
		Map<String, Object> reqParams = ServletUtils.getReqParameters(req);

		SqlLoader loader = new SqlLoader(ctx.getInitParameter("SqlLoaderConf_File"), ctx.getRealPath("/"));
		Map<String, Object> queries = loader.loadQueries(reqParams);
		
		String sql = queries.get(req.getParameter("sqlName")).toString();
		
		if (isDebug()) {
			logProxy(YamlUtils.toYamlBlock(ServletUtils.getReqParameters(req)));
			logProxy("**** Query ****\n" + sql);
		}
		return sql;
	}


	/**
	 * Recupera i dati da inviare in risposta alla <code>DataTable</code>
	 * @param dataTableParams Parametri ricevuti via <code>request</code> dalla <code>DataTable</code> in fase di setup o in seguito ad una modifica di ordinamento, filtro o paginazione
	 * @param sql Query opportunamente elaborata per estrarre i dati secondo la richiesta dell'utente
	 * @return Mappa con una struttura che rispetti il modello che datatable accetta in input 
	 * @throws DTReplayServletException
	 */
	protected Map<String, Object> getReply(Map<String, String> dataTableParams, String sql) {
		return getReply(dataTableParams, sql, null);
	}
	/**
	 * Recupera i dati da inviare in risposta alla <code>DataTable</code>
	 * @param dataTableParams Parametri ricevuti via <code>request</code> dalla <code>DataTable</code> in fase di setup o in seguito ad una modifica di ordinamento, filtro o paginazione
	 * @param sql Query opportunamente elaborata per estrarre i dati secondo la richiesta dell'utente
	 * @param session Sesione corrente. Le queries eseguite verranno inserite in sessione ad ogni richiesta e memorizzate negli attributi <code>DataTableSqlDati</code> e <code>DataTableSqlDatiExtra</code>, in modo da consentire altri tipi di estrazione (excel, pdf....
	 * @return Mappa con una struttura che rispetti il modello che datatable accetta in input 
	 * @throws DTReplayServletException
	 */
	protected Map<String, Object> getReply(Map<String, String> dataTableParams, String sql, HttpSession session) {
		Map<String, Object> retVal = null;
		boolean serverSideProcessing = BooleanUtils.toBoolean(dataTableParams.get("serverSideProcessing"));
		
		DTReplyHelper dtrHelper = new DTReplyHelperImpl( dataTableParams, sql, serverSideProcessing );
		String sqlDati = dtrHelper.getSqlDati();
		String sqlDatiExtra = dtrHelper.getSqlDatiExtra();
		
		if (session != null) {
			session.setAttribute( "DataTableSqlDati", sqlDati );
			session.setAttribute( "DataTableSqlDatiExtra", sqlDatiExtra );
		}
		
		if (isDebug())
			logProxy("**** Queries Eseguite **** \n--sqlDati:\n" + sqlDati + "\n\n--sqlDatiExtra:\n" + sqlDatiExtra);
		
		try {
			retVal = dtrHelper.getReplyMap(getConnection());
		
		} catch ( SQLException e ) {
			logProxy("*** Eerrore di accesso al database *** \n", e);
			if (isPrintStackTrace())
				e.printStackTrace();
		}
		
		return retVal;
	}
	
	
	protected Connection getConnection() {
		try {
			return  new ConnectionProviderImplV2(ctx.getRealPath(getInitParameter("file_dbConf"))).getConnection();
		} catch ( ConnectionProviderException e ) {
			logProxy("**** Errore ottenimento connessione ****\n", e);
			if (isPrintStackTrace())
				e.printStackTrace();
		}
		return null;
	}
	
	
	protected void logProxy(String msg) {
		logProxy(msg, null);
	}
	protected void logProxy(String msg, Throwable t) {
		Date timestamp = Calendar.getInstance().getTime();
		File logFile = new File(ctx.getRealPath(getInitParameter("logFileName")));
		String templ = timestamp + " - " + getServletName() + " - " + getClass().getName() + ":\n";
		try {
			FileUtils.writeStringToFile(logFile, templ + msg + "\n", true);
			if (t != null) {
				FileUtils.writeStringToFile(logFile, "***** Eccezione ******\n", true);
				FileUtils.writeStringToFile(logFile, t.getMessage()+ "\n", true);
				PrintWriter pw = new PrintWriter(new FileOutputStream(logFile, true));
				t.printStackTrace(pw);
				pw.close();
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Controlla se nella <code>request</code> sia presente il parametro 'DebugDTReply' e imposta il campo debug con il valore trovato. Default = false.
	 * @param req Oggetto <code>request</code>
	 * @return esito della ricerca del flag di debug nella request
	 */
	protected boolean parseDebugOption(HttpServletRequest req) {
		setDebug(BooleanUtils.toBoolean(req.getParameter("DebugDTReply")));
		return isDebug();
	}
	
	
	/**
	 * Controlla se nella <code>request</code> sia presente il parametro 'printStackTrace' e imposta il campo printStackTrace con il valore trovato. Default = false.
	 * @param req Oggetto <code>request</code>
	 * @return esito della ricerca del flag di printStackTrace nella request
	 */
	protected boolean parseStackTraceOption(HttpServletRequest req) {
		setPrintStackTrace(BooleanUtils.toBoolean(req.getParameter("printStackTrace")));
		return isPrintStackTrace();
	}
	
	
	/**
	 * Default: nessuna azione. Sovrascrivere questo metodo per effettuare il parse di eventuali altri parametri della request.  
	 * @param req Oggetto <code>request</code>
	 */
	protected void parseOptions(HttpServletRequest req) {
	}
	/**
	 * Metodo da sovrascrivere per effettuare operazioni di inizilizzazione. Se viene sovrascritto init(), senza chiamare super.init(), si perde il campo <code>ServletContext</code> ctx usato da altri metodi.
	 */
	@Override
	protected void inizializza() {
	}
	
} // chiusura classe
