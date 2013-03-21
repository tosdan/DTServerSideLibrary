package com.github.tosdan.utils.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.util.Properties;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.tosdan.utils.sql.ConnectionProvider;
import com.github.tosdan.utils.sql.ConnectionProviderException;
import com.github.tosdan.utils.sql.ConnectionProviderImpl;
import com.github.tosdan.utils.sql.BasicDAO;
import com.github.tosdan.utils.sql.BasicDAOException;

/**
 * 
 * @author Daniele
 *
 */
@SuppressWarnings( "serial" )
public class QueryRunnerServlet extends BasicHttpServlet
{
	private boolean printStackTrace;

	@Override
	protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws QueryRunnerServletException, IOException { this.doPost( req, resp ); }
	
	@Override
	protected void doPost( HttpServletRequest req, HttpServletResponse resp )
			throws QueryRunnerServletException, IOException
	{
		// inizializza la mappa contenente i parametri della request
		String reqLog = this._processRequestForParams( req );
		if ( this._booleanSafeParse(req.getParameter("logQueryRunner")) && this._initConfigParamsMap.get("logFileName") != null ) 
			// crea un file di log con il nome passato come parametro nella sottocartella della webapp
			this._logOnFile( this._ctx.getRealPath(this._initConfigParamsMap.get("logFileName")), reqLog );

		// flag per verbose stacktrace delle eccezioni catturate da questa servlet
		this.printStackTrace = this._booleanSafeParse( req.getParameter("printStackTrace") );
		
		// query da eseguire		
		String querySql = "";
		try {
			querySql = req.getAttribute("queryRecuperata").toString();
		} catch ( NullPointerException e ) {
			throw new QueryRunnerServletException( "Servlet " + this.getServletName() + 
					": errore, attributo queryRecuperata mancante/nullo nella request." );
		}
		
		int esito = this.eseguiQuery( querySql );

		if ( this._blankIfNull(req.getParameter("reqMethod")).equalsIgnoreCase("ajax") )
		{
			ServletOutputStream out = resp.getOutputStream();
			out.print(esito);
			
		} else {
			if ( esito > 0 ) {
				req.getSession().setAttribute( "QueryRunnerServlet_OK", "Operazione eseguita con successo." );
			} else {
				req.getSession().setAttribute( "QueryRunnerServlet_Error", "Rchiesta fallita." );
				// TODO
			}
			
			String jspChiamante = req.getHeader("referer");	
			resp.sendRedirect( jspChiamante );
		}
		
	}
	

	/**
	 * 
	 * @param sql stringa contenente la query sql
	 * @return il numero di record aggiornati/inseriti/eliminati
	 * @throws QueryRunnerServletException 
	 */
	private int eseguiQuery( String sql )
			throws QueryRunnerServletException
	{
		String fileDBConf = this._appRealPath + this._initConfigParamsMap.get( "file_dbConf" );
		BasicDAO dao = new BasicDAO( this.getConnectionProvider(fileDBConf) );
		try {
			return dao.update( sql );
			
		} catch ( BasicDAOException e1 ) {
			if ( printStackTrace )
				e1.printStackTrace();
			throw new QueryRunnerServletException(  "Servlet " + this.getServletName() 
					+ ": errore di accesso al database. Classe: "+this.getClass().getName(), e1 );
		}
	}
	

	/**
	 * Configura e restituisce un {@link ConnectionProvider}
	 * @param fileDBConf file {@link Properties} contenente la confiurazione per l'accesso al database.
	 * @return un oggetto {@link ConnectionProvider} dal quale poter ottenere, senza ulteriori configurazioni, un oggetto {@link Connection} 
	 * @throws QueryRunnerServletException 
	 */
	private ConnectionProvider getConnectionProvider(String fileDBConf)
			throws QueryRunnerServletException
	{
		try {
			return new ConnectionProviderImpl( fileDBConf );
			
		} catch ( ConnectionProviderException e ) {
			if ( printStackTrace )
				e.printStackTrace();
			throw new QueryRunnerServletException(  "Servlet " + this.getServletName()
					+": errore creazione ConnectionProvider. Classe: "+this.getClass().getName(), e );
		}
	}
} //***Chiusura classe
