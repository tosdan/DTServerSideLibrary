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
	protected ServletContext _ctx;
	
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
	 * Mappa dei <code>Parameters</code> della request: solo quelli a valore singolo
	 */
	protected Map<String, String> _requestParamsMap;
	
	/**
	 * Mappa dei <code>Parameters</code> della request: solo quelli a valore multiplo
	 */
	protected Map<String, List<String>> _requestMultipleValuesParamsMap;
	
	/**
	 * Mappa degli <code>Attributes</code> della request
	 */
	protected Map<String, Object> _requestAttributes;
	
	@Override
	public void init( ServletConfig config ) throws ServletException {
		super.init( config );
		this._ctx = config.getServletContext();
		
		this._initConfigParamsMap = new HashMap<String, String>();
		this._appRealPath = this._ctx.getRealPath( "/" );
		this._initConfigParamsMap.put( "realPath", this._appRealPath );
		
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
	 * Memorizza un oggetto in sessione
	 * @param key nome da associare all'oggetto da memorizzare in sessione
	 * @param obj oggetto da memorizzare in sessione
	 * @param req request della servlet corrente
	 */
	protected void _toSession(String key, Object obj, HttpServletRequest req) {
		req.getSession().setAttribute( key, obj );
	}
	
	/**
	 * Memorizza una stringa in sessione criptandola con una chiave standard
	 * @since Per ora memorizza la stringa in chiaro: e' pensato per una implementazione futura
	 * @param key nome da associare alla stringa da memorizzare in sessione
	 * @param str stringa da criptare e memorizzare in sessione
	 * @param req request della servlet corrente
	 */
	protected void _cryptToSession(String key, String str, HttpServletRequest req) {
		req.getSession().setAttribute( key, str );
	}
	
	/**
	 * Estrae i parametri dalla request e li inseriesce in una Mappa da stringa a stringa per i parametri a valore singolo; in una mappa da stringa a lista per i parametri che abbiano valori multipli.
	 * La mappe sono mantenute in un campo della classe (Map&lt;String, String&gt; <code>_requestParams</code> e Map&lt;String, List&lt;String&gt;&gt; <code>_requestMultipleValuesParamsMap</code>).
	 * @param req oggetto request da processare
	 * @return Log testuale delle associazioni parametro=>valoreParametro dei parametri estratti
	 * @deprecated
	 */
	protected String _processRequestForParams(HttpServletRequest req)
	{
		String reqLog = "";		
		this._requestParamsMap = new HashMap<String, String>();
		this._requestMultipleValuesParamsMap = new HashMap<String, List<String>>();
		this._requestAttributes = new HashMap<String, Object>();
		
		@SuppressWarnings( "unchecked" )
		Enumeration<String> paramsNames = req.getParameterNames();
		while ( paramsNames.hasMoreElements() ) {
			String name = paramsNames.nextElement();
			String[] paramsValues = req.getParameterValues(name);
			
			if ( paramsValues.length == 1 ) {
				this._requestParamsMap.put(name, paramsValues[0]);
				reqLog += name+"=>"+paramsValues[0]+"\n";
				
			} else if ( paramsValues.length > 1 ) {
				List<String> values = new ArrayList<String>();
				for( int i = 0 ; i < paramsValues.length ; i++ ) {
					values.add( paramsValues[i] );
				}
				this._requestMultipleValuesParamsMap.put(name, values);
				reqLog += name+"=>"+values+"\n";
			}
		}

		@SuppressWarnings( "unchecked" )
		Enumeration<String> attributes = req.getAttributeNames();
		while ( attributes.hasMoreElements() ) {
			String attribName = (String) attributes.nextElement();
			Object attriValue = req.getAttribute(attribName);
			this._requestAttributes.put( attribName, attriValue  );
			reqLog += attribName+"=>"+attriValue+"\n";
		}

		
		reqLog += "---- Fine parametri ----";
		
		return reqLog;
	}
	
	

	/**
	 * 
	 * @param req
	 * @return
	 */
	protected Map<String, String> get_requestParamsMap(HttpServletRequest req)
	{
		if (this._requestParamsMap == null)
			this._processRequestForParams( req );
		
		return this._requestParamsMap;
	}

	/**
	 * 
	 * @param req
	 * @return
	 */
	protected Map<String, List<String>> get_requestMultipleValuesParamsMap(HttpServletRequest req)
	{
		if (this._requestMultipleValuesParamsMap == null)
			this._processRequestForParams( req );
		
		return this._requestMultipleValuesParamsMap;
	}

	/**
	 * 
	 * @param req
	 * @return
	 */
	protected Map<String, Object> get_requestAttributes(HttpServletRequest req)
	{
		if (this._requestAttributes == null)
			this._processRequestForParams( req );
		
		return this._requestAttributes;
	}

	/**
	 * Effettua il <code>parse</code> su una stringa per restituire un <code>boolean</code>, in caso di null o o in caso di fallimento del parse restituisce <code>false</code>.
	 * @param s stringa da valutare
	 * @return
	 */
	protected boolean _booleanSafeParse(String s) {
		return _defaultBoolean( (Object) s, false );
	}
	
	/**
	 * Effettua il <code>parse</code> su una stringa per restituire un <code>boolean</code>, in caso di null o in caso di fallimento del parse restituisce il valore booleano passato come parametro.
	 * @param s stringa da valutare
	 * @param defaultValue valore di ritorno di default: <code>true/false</code>
	 * @return
	 */
	protected boolean _defaultBoolean(String s, boolean defaultValue) {
		return this._defaultBoolean( (Object) s, defaultValue );
	}

	/**
	 * Effettua il <code>parse</code> su un oggetto per restituire un <code>boolean</code>. In caso di oggetto nullo o in caso di fallimento del parse restituisce <code>false</code>.
	 * @param obj oggetto da valutare
	 * @return
	 */
	protected boolean _booleanSafeParse(Object obj) {
		return _defaultBoolean( obj, false );
	}
	
	/**
	 * Effettua il <code>parse</code> su un oggetto per restituire un <code>boolean</code>. In caso di oggetto nullo o in caso di fallimento del parse restituisce il valore booleano passato come parametro.
	 * @param obj oggetto da valutare
	 * @param defaultValue valore di ritorno di default: <code>true/false</code>
	 * @return
	 */
	protected boolean _defaultBoolean(Object obj, boolean defaultValue) {
		boolean result = false;
		try {
			result = Boolean.valueOf(obj.toString());
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
	 * Restituisce una stringa vuota nel caso il valore passato sia null
	 * @param o oggetto da valutare
	 * @return 
	 */
	protected String _blankIfNull(Object o) {
		return this._defaultString( o, "" );
	}
	
	/**
	 * Se la stringa passata e' nulla restituisce il valore di default
	 * @param s stringa in ingresso
	 * @param defaultValue
	 * @return
	 */
	protected String _defaultString(String s, String defaultValue) {
		return this._defaultString( (Object) s, defaultValue );
	}

	/**
	 * Se l'oggetto passato e' nullo restituisce il valore di default
	 * @param o oggetto da valutare
	 * @param defaultValue
	 * @return
	 */
	protected String _defaultString(Object o, String defaultValue) {
		return (o == null) ? defaultValue : o.toString();
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
