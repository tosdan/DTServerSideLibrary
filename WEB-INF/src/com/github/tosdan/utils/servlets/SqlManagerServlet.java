package com.github.tosdan.utils.servlets;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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
		this.printStackTrace = req.getParameter( "printStackTrace" );
		String dtrConfFile = this.envConfigParams.get( "SqlManagerServletConf_File" );
		String dtrConfPath = this.realPath + this.envConfigParams.get( "SqlManagerServletConf_Path" );
		Properties dtrSettings = new Properties();
		try {
			dtrSettings.load( this.app.getResourceAsStream( dtrConfFile ) );
		} catch ( IOException e2 ) {
			if ( printStackTrace != null && printStackTrace.equalsIgnoreCase("true") )
				e2.printStackTrace();
			throw new SqlManagerServletException( "Errore caricamento file configurazione SqlManagerServlet.", e2 );
		}

		/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

		String nomeSql = req.getParameter( "sqlName" );
		String querySql = "";
		if (nomeSql == null ) {
			throw new SqlManagerServletException( "Errore: parametro sqlName mancante nella request." );
		} else {
			String queryFile = dtrSettings.getProperty( nomeSql );
			Map<String, String> tempAllParams = new HashMap<String, String>();
			tempAllParams.putAll( this.requestParams );
			tempAllParams.putAll( this.envConfigParams );
			
			try {
				querySql = QueriesUtils.compilaQueryDaFile( (dtrConfPath + queryFile), nomeSql, tempAllParams, (new MapFormatTypeValidatorSQL()) );
			} catch ( IOException e1 ) {
				if ( printStackTrace != null && printStackTrace.equalsIgnoreCase("true") )
					e1.printStackTrace();
				throw new SqlManagerServletException( "Errore caricamente query da file.", e1 );
			}
			
//		System.out.println( this.getClass().getName() + " - querySql: " + querySql );
		}
		
		int esito = -1;	
		String file_dbConf = this.realPath + this.envConfigParams.get( "file_dbConf" );
		String azioneSql = req.getParameter( "sqlAction" );
		if (azioneSql != null && (azioneSql.equalsIgnoreCase("insert") || azioneSql.equalsIgnoreCase("update") || azioneSql.equalsIgnoreCase("delete")) ) {
			esito = this.executeUpdate( querySql, file_dbConf );
		} else if (azioneSql != null && azioneSql.equalsIgnoreCase( "select" )) {
			// Per ora esegue solo operazioni di insert, update e delete. Per le select e' previsto l'uso della servlet DTReplyServlet, in associazione con DataTables
		}

		/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

		HttpSession session = req.getSession();
		if (esito >= 1) {
			session.setAttribute( "SqlManagerServlet_OK", "Operazione eseguita con successo." );
		} else {
			session.setAttribute( "SqlManagerServlet_Error", "Operazione non riuscita." );
			// TODO sarebbe utile qualche info aggiuntiva: prevedere casi di fallimento.
		}
		
		String paginaPrecedente = req.getHeader("referer");	
		resp.sendRedirect( paginaPrecedente );	
	}

	/**
	 * 
	 * @param sql
	 * @param file_dbConf
	 * @return
	 * @throws SqlManagerServletException
	 */
	private int executeUpdate( String sql, String file_dbConf )
			throws SqlManagerServletException
	{
		SqlManagerDAO dao = new SqlManagerDAO( this.getConnectionProvider(file_dbConf) );
		try {
			return dao.update( sql );
		} catch ( SqlManagerDAOException e1 ) {
			if ( printStackTrace != null && printStackTrace.equalsIgnoreCase("true") )
				e1.printStackTrace();
			throw new SqlManagerServletException( "Errore di accesso al database.", e1 );
		}
	}
	

	/**
	 * 
	 * @param file_dbConf
	 * @return
	 * @throws DTReplayServletException
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
			throw new SqlManagerServletException( "Errore creazione ConnectionProvider.", e );
		}
	}
	
}
