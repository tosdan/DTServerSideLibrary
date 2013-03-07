package com.github.tosdan.utils.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.github.tosdan.utils.sql.ConnectionProvider;
import com.github.tosdan.utils.sql.ConnectionProviderException;
import com.github.tosdan.utils.sql.ConnectionProviderImpl;
import com.github.tosdan.utils.sql.QueriesUtils;
import com.github.tosdan.utils.sql.SqlManagerDAO;
import com.github.tosdan.utils.sql.SqlManagerDAOException;
import com.github.tosdan.utils.stringhe.MapFormatTypeValidator;
import com.github.tosdan.utils.stringhe.MapFormatTypeValidatorSQL;

@SuppressWarnings( "serial" )
public class SqlManagerServlet extends BasicHttpServlet
{
	private String printStackTrace;

	@Override
	protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException { this.doPost( req, resp ); }
	
	@Override
	protected void doPost( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException
	{
		// inizializza la mappa contenente i parametri della request
		String reqLog = this.processRequestForParams( req );
		if (req.getParameter("debugRequestParams") != null && req.getParameter("debugRequestParams").equalsIgnoreCase( "true" ) && this.envConfigParams.get("logFileName") != null ) 
			// crea un file di log con il nome passato come parametro nella sottocartella della webapp
			this.logOnFile( this.app.getRealPath(this.envConfigParams.get("logFileName")), reqLog );
		// flag per verbose stacktrace
		this.printStackTrace = req.getParameter( "printStackTrace" );
		// percorso file settings
		String queriesRepoFolderFullPath = this.realPath + this.envConfigParams.get( "SqlManagerServletConf_Path" );
		// nome file settings
		String propertiesFile = this.envConfigParams.get( "SqlManagerServletConf_File" );
		// carica la configurazione dal file properties
		Properties dtrProperties = this.loadProperties( propertiesFile );

		/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

		// query da eseguire
		String querySql = "";
		// identificativo query nel file repository delle queries
		String nomeSQL = req.getParameter( "sqlName" );
		if (nomeSQL == null) 
			throw new SqlManagerServletException( "Servlet " + this.getServletName() + 
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
			throw new SqlManagerServletException(  "Servlet " + this.getServletName() 
					+ ": errore caricamente query da file. Classe: "+this.getClass().getName(), e1 );
		}

		/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		
		String azioneSql = req.getParameter( "sqlAction" );
		if (azioneSql != null && (azioneSql.equalsIgnoreCase("insert") || azioneSql.equalsIgnoreCase("update") || azioneSql.equalsIgnoreCase("delete")) ) {
			boolean esitoOK = this.eseguiUpdate( querySql );
			HttpSession session = req.getSession();
			if (esitoOK) {
				session.setAttribute( "SqlManagerServlet_OK", "Operazione eseguita con successo." );
			} else {
				session.setAttribute( "SqlManagerServlet_Error", "Rchiesta fallita." );
				// TODO
			}
			String paginaPrecedente = req.getHeader("referer");	
			resp.sendRedirect( paginaPrecedente );
			
		} else if ( azioneSql != null && azioneSql.equalsIgnoreCase("select") ) {
			String nextHandlerServlet = req.getParameter( "NextHandlerServlet" );
			req.setAttribute( "queryRecuperata", querySql );
			RequestDispatcher dispatcher = req.getRequestDispatcher(  "/" + nextHandlerServlet + "/go" );
//			System.out.println(  "/" + nextHandlerServlet + "/go" );
			dispatcher.forward( req, resp );
			//esitoOK = false; // Per ora esegue solo operazioni di insert, update e delete. Per le select e' previsto l'uso della servlet DTReplyServlet, in associazione con DataTables
		
		}

	}

	
	/**
	 * 
	 * @param sql
	 * @param req 
	 * @param resp 
	 * @param file_dbConf
	 * @return
	 * @throws SqlManagerServletException
	 */
	private boolean eseguiUpdate( String sql )
			throws SqlManagerServletException
	{
		String file_dbConf = this.realPath + this.envConfigParams.get( "file_dbConf" );
		SqlManagerDAO dao = new SqlManagerDAO( this.getConnectionProvider(file_dbConf) );
		try {
			return (dao.update( sql ) > 0);
		} catch ( SqlManagerDAOException e1 ) {
			if ( printStackTrace != null && printStackTrace.equalsIgnoreCase("true") )
				e1.printStackTrace();
			throw new SqlManagerServletException(  "Servlet " + this.getServletName() 
					+ ": errore di accesso al database. Classe: "+this.getClass().getName(), e1 );
		}
	}
	

	/**
	 * 
	 * @param file_dbConf
	 * @return
	 * @throws SqlManagerServletException 
	 */
	private ConnectionProvider getConnectionProvider(String file_dbConf)
			throws SqlManagerServletException
	{
		try {
			return new ConnectionProviderImpl( file_dbConf );
		} catch ( ConnectionProviderException e ) {
			if ( printStackTrace != null && printStackTrace.equalsIgnoreCase("true") )
				e.printStackTrace();
			throw new SqlManagerServletException(  "Servlet " + this.getServletName()
					+": errore creazione ConnectionProvider. Classe: "+this.getClass().getName(), e );
		}
	}


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
	 * @throws SqlManagerServletException
	 */
	protected Properties loadProperties(String propertiesFile) throws SqlManagerServletException
	{
		Properties dtrSettings = new Properties();
		
		try {
			// carica, dal file passato, l'oggetto Properties salvandolo in un campo della servlet 
			dtrSettings.load( this.app.getResourceAsStream( propertiesFile ) );
			
		} catch ( IOException e2 ) {
			if ( printStackTrace != null && printStackTrace.equalsIgnoreCase("true") )
				e2.printStackTrace();
			throw new SqlManagerServletException( "Servlet " + this.getServletName()
					+ ": errore caricamento file configurazione. Classe: "+this.getClass().getName(), e2 );
		}
		
		return dtrSettings;
	}
}
