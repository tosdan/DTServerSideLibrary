package com.github.tosdan.utils.servlets;

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
 * @version 0.0.2-b2013-07-22
 */
@SuppressWarnings( "serial" )
public abstract class BasicHttpServletV2 extends HttpServlet
{
	/** 
	 * ServletContext di questa servlet
	 */
	protected ServletContext ctx;
	
	/**
	 * Contiene i parametri di inizializzazione specifici della servlet (per intendersi: presi dall'oggetto ServletConfig)
	 */
	protected HashMap<String, String> initConfigParamsMap;
	
	/**
	 * Path assoluto su disco della cartella di questa webapp.
	 * Puo' esser utile da passare ad oggetti che non siano istanze di Servlet 
	 */
	protected String realPath;
	
	/**
	 * Mappa dei <code>Parameters</code> della request: solo quelli a valore singolo
	 */
	private Map<String, String> requestParamsMap;
	
	/**
	 * Mappa dei <code>Parameters</code> della request: solo quelli a valore multiplo
	 */
	private Map<String, List<String>> requestMultipleValuesParamsMap;
	
	/**
	 * Mappa degli <code>Attributes</code> della request
	 */
	private Map<String, Object> requestAttributes;

	private String reqLog;
	
	@Override
	public void init( ServletConfig config ) throws ServletException {
		super.init( config );
		this.ctx = config.getServletContext();
		
		this.initConfigParamsMap = new HashMap<String, String>();
		this.realPath = this.ctx.getRealPath( "/" );
		this.initConfigParamsMap.put( "realPath", this.realPath );
		
		@SuppressWarnings( "rawtypes" )
		Enumeration parametriServlet = config.getInitParameterNames();
		while (parametriServlet.hasMoreElements()) {
			String nomeParam = ( String ) parametriServlet.nextElement();
			String valore = config.getInitParameter( nomeParam );
			this.initConfigParamsMap.put( nomeParam, valore );
			
//			System.out.println( nomeParam + " - " + valore );
//			System.out.println( System.getProperty("catalina.base") );
		}
		
	}

	/**
	 * Estrae i parametri dalla request e li inseriesce in una Mappa da stringa a stringa per i parametri a valore singolo; in una mappa da stringa a lista per i parametri che abbiano valori multipli.
	 * La mappe sono mantenute in un campo della classe (Map&lt;String, String&gt; <code>requestParams</code> e Map&lt;String, List&lt;String&gt;&gt; <code>requestMultipleValuesParamsMap</code>).
	 * @param req oggetto request da processare
	 * @return Log testuale delle associazioni parametro=>valoreParametro dei parametri estratti
	 */
	private void processRequestForParams(HttpServletRequest req)
	{
		this.reqLog = "";	
		this.requestParamsMap = new HashMap<String, String>();
		this.requestMultipleValuesParamsMap = new HashMap<String, List<String>>();
		this.requestAttributes = new HashMap<String, Object>();
		
		@SuppressWarnings( "unchecked" )
		Enumeration<String> paramsNames = req.getParameterNames();
		while ( paramsNames.hasMoreElements() ) {
			String name = paramsNames.nextElement();
			String[] values = req.getParameterValues(name);
			
			if ( values.length == 1 ) {
				this.requestParamsMap.put(name, values[0]);
				this.reqLog += name+"=>"+values[0]+"\n";
				
			} else if ( values.length > 1 ) {
				List<String> valuesList = new ArrayList<String>();
				for( int i = 0 ; i < values.length ; i++ ) {
					valuesList.add( values[i] );
				}
				this.requestMultipleValuesParamsMap.put(name, valuesList);
				this.reqLog += name+"=>"+valuesList+"\n";
			}
		}

		@SuppressWarnings( "unchecked" )
		Enumeration<String> attributes = req.getAttributeNames();
		while ( attributes.hasMoreElements() ) {
			String attribName = (String) attributes.nextElement();
			Object attribValue = req.getAttribute(attribName);
			if (attribValue instanceof String || attribValue instanceof List)
				this.requestAttributes.put( attribName, attribValue );
			this.reqLog += attribName+"=>"+attribValue+"\n";
		}

		
		this.reqLog += "---- Fine parametri ----";
	}
	
	

	/**
	 * 
	 * @param req
	 * @return
	 */
	protected Map<String, String> getRequestParamsMap(HttpServletRequest req)
	{
		this.processRequestForParams( req );
		
		return this.requestParamsMap;
	}

	/**
	 * 
	 * @param req
	 * @return
	 */
	protected Map<String, List<String>> getRequestMultipleValuesParamsMap(HttpServletRequest req)
	{
		if (this.requestMultipleValuesParamsMap == null)
			this.processRequestForParams( req );
		
		return requestMultipleValuesParamsMap;
	}

	/**
	 * 
	 * @param req
	 * @return
	 */
	protected Map<String, Object> getRequestAttributes(HttpServletRequest req)
	{
		if (this.requestAttributes == null)
			this.processRequestForParams( req );
		
		return requestAttributes;
	}

	/**
	 * 
	 * @param req
	 * @return
	 */
	protected String getRequestParamsProcessLog(HttpServletRequest req)
	{
		if (this.reqLog == null)
			this.processRequestForParams( req );
		
		return reqLog;
	}

	
}
