package com.github.tosdan.utils.servlets;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

/**
 * 
 * @author Daniele
 *
 */
@SuppressWarnings( "serial" )
public abstract class BasicHttpServlet extends HttpServlet
{
	/** 
	 * ServletContext di questa servlet
	 */
	protected ServletContext _app;
	
	/**
	 * Contiene i parametri di inizializzazione specifici della servlet (per intendersi: presi dall'oggetto ServletConfig)
	 */
	protected HashMap<String, String> _initConfigParamsMap;
	
	/**
	 * Path assoluto su disco della cartella di questa webapp.
	 * Puo' esser utile da passare ad oggetti che non siano istanze di Servlet 
	 */
	protected String _appRealPath;
	
	/**
	 * Mappa dei parametri della request: solo quelli a valore singolo
	 */
	protected Map<String, String> _requestParamsMap;
	
	/**
	 * Mappa dei parametri della request: solo quelli a valore multiplo
	 */
	protected Map<String, List<String>> _requestMultipleValuesParamsMap;
	
	
	@Override
	public void init( ServletConfig config ) throws ServletException {
		super.init( config );
		this._app = config.getServletContext();
		
		this._initConfigParamsMap = new HashMap<String, String>();
		this._appRealPath = this._app.getRealPath( "/" );
		this._initConfigParamsMap.put( "realPath", _appRealPath );
		
		@SuppressWarnings( "rawtypes" )
		Enumeration parametriServlet = config.getInitParameterNames();
		while (parametriServlet.hasMoreElements()) {
			String nomeParam = ( String ) parametriServlet.nextElement();
			String valore = config.getInitParameter( nomeParam );
			this._initConfigParamsMap.put( nomeParam, valore );
			
//			System.out.println( nomeParam + " - " + valore );
//			System.out.println( System.getProperty("catalina.base") );
		}
		
	}

	/**
	 * Estrae i parametri dalla request e li inseriesce in una Mappa da stringa a stringa ignorando i parametri che abbiano valori multipli.
	 * La mappa è mantenuta in un campo della classe (Map<String, String> requestParams).
	 * @param req
	 * @return Log testuale delle associazioni parametro=>valoreParametro dei parametri estratti
	 */
	protected String _processRequestForParams(HttpServletRequest req)
	{
		String reqLog = "";
		@SuppressWarnings( "unchecked" )
		Enumeration<String> e = req.getParameterNames();
		this._requestParamsMap = new HashMap<String, String>();
		while ( e.hasMoreElements() ) {
			String elem = (String) e.nextElement();
			String val = req.getParameter(elem);
			if ( req.getParameterValues(val) == null )
				this._requestParamsMap.put(elem, val);
			reqLog += elem+"=>"+val+"\n";
		}
		reqLog += "---- Fine parametri ----";
		
		return reqLog;
	}

	/**
	 * Estrae dalla request solo i parametri con valori multiplie e li inseriesce in una Mappa da stringa a lista di stringhe.
	 * E' pensato per oggetti HTML come ad esempio le checkbox che anche se contengono un unico valore vanno estratte per forza con questo sistema.
	 * La mappa è mantenuta in un campo della classe.
	 * @param req
	 * @return Log testuale delle associazioni parametro => valoreParametro dei parametri estratti
	 */
	protected String _processRequestForMultipleValuesParams(HttpServletRequest req)
	{
		String reqLog = "";
		@SuppressWarnings( "unchecked" )
		Enumeration<String> e = req.getParameterNames();
		this._requestMultipleValuesParamsMap = new HashMap<String, List<String>>();
		while ( e.hasMoreElements() ) {
			String elem = (String) e.nextElement();
			String val = req.getParameter(elem);
			String[] paramValues;
			if ( (paramValues = req.getParameterValues(val)) != null )
			{
				List<String> values = new ArrayList<String>();
				for( int i = 0 ; i < paramValues.length ; i++ ) {
					values.add( paramValues[i] );
				}
				this._requestMultipleValuesParamsMap.put(elem, values);
				reqLog += elem+"=>"+values+"\n";
			}
		}
		reqLog += "---- Fine parametri checkbox ----";
		
		return reqLog;
	}
	

	/**
	 * Esegue il parse di una stringa per restituire un valore booleano, in caso di null o di valore passato mal formato restituisce false
	 * @param s stringa da valutare
	 * @return
	 */
	protected boolean _booleanSafeParse(String s) {
		return _defaultBoolean( s, false );
	}
	
	/**
	 * Esegue il parse di una stringa per restituire un valore booleano, in caso di null o di valore passato mal formato restituisce il defaultValue passato
	 * @param s stringa da valutare
	 * @param defaultValue valore di ritorno di default: <code>true/false</code>
	 * @return
	 */
	protected boolean _defaultBoolean(String s, boolean defaultValue) {
		boolean result = false;
		try {
			result = Boolean.valueOf(s);
		} catch ( Exception e ) {
			return defaultValue;
		}
		return result;
	}
	
	/**
	 * Restituisce una stringa vuota nel caso il valore passato sia null
	 * @param s Stringa da valutare
	 * @return 
	 */
	protected String _blankIfNull(String s) {
		return (s == null) ? "" : s;
	}

	/**
	 * Scrive semplicemente una stringa in un file
	 * @param fileName nome file con percorso completo
	 * @param content contenuto da scrivere all'interno del file
	 */
	protected void _logOnFile(String fileName, String content) {
		_logOnFile( fileName, content, false );
	}
	/**
	 * Scrive semplicemente una stringa in un file
	 * @param fileName nome file con percorso completo
	 * @param content contenuto da scrivere all'interno del file
	 * @param appendFlag 
	 */
	protected void _logOnFile(String fileName, String content, boolean appendFlag)
	{
		// crea un file di log con il nome passato come parametro nella sottocartella della webapp
			File logFile = new File( fileName );
			File logPath = new File( logFile.getParent() );
		try {
			if ( !logPath.exists() )
				logPath.mkdirs();
			
			FileOutputStream fos = new FileOutputStream( fileName, appendFlag );
			PrintWriter logWriter = new PrintWriter( fos );
			logWriter.println(content);
			logWriter.close();
			
		} catch ( Exception e ) {
			// qualsiasi eccezione nella stampa del log non deve essere bloccante
			String astks = "*************************************";
			String ecc = astks+"\n* Eccezione catturata, ma non gestita\n"+astks+"\n"; 
			System.err.println( ecc+ "Servlet " + this.getServletName()+"\nErrore nel tentativo di scrivere il logfile\n"+logFile.getName()+"\nnella cartella\n"+logPath.getAbsolutePath()+"\nClasse: "+this.getClass().getName() +"\n");
			e.printStackTrace();
			System.err.println( "\n"+astks );
		} 
	}
	
}
