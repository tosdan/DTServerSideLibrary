package com.github.tosdan.utils.filters;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
/**
 * 
 * @author Daniele
 * @version 0.0.2-b2013-07-23
 */
public abstract class BasicFilterV2 implements Filter
{
	/** 
	 * ServletContext del filtro
	 */
	protected ServletContext ctx;
	
	/**
	 * filterConfig del filtro
	 */
	protected FilterConfig filterConfig;
	
	/**
	 * Contiene i parametri di inizializzazione specifici del filter (per intendersi: presi dall'oggetto ServletConfig)
	 */
	protected HashMap<String, String> initConfigParamsMap;
	
	/**
	 * Path assoluto su disco della cartella di questa webapp.
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
	public void destroy() {
		this.filterConfig = null;
		this.requestAttributes = null;
		this.requestParamsMap = null;
		this.requestMultipleValuesParamsMap = null;
		this.reqLog = null;
	}

	public void init( FilterConfig filterConfig ) throws ServletException {
		this.filterConfig = filterConfig;
		this.ctx = filterConfig.getServletContext();
		this.initConfigParamsMap = new HashMap<String, String>();
		this.realPath = this.ctx.getRealPath( "/" );
		this.initConfigParamsMap.put( "realPath", this.realPath );
		
		@SuppressWarnings( "rawtypes" )
		Enumeration parametriServlet = filterConfig.getInitParameterNames();
		while (parametriServlet.hasMoreElements()) {
			String nomeParam = ( String ) parametriServlet.nextElement();
			String valore = filterConfig.getInitParameter( nomeParam );
			this.initConfigParamsMap.put( nomeParam, valore );
			
//			System.out.println( nomeParam + " - " + valore );
//			System.out.println( System.getProperty("catalina.base") );
		}
	}

	/**
	 * Estrae i parametri dalla request e li inseriesce in una Mappa da stringa a stringa per i parametri a valore singolo; in una mappa da stringa a lista per i parametri che abbiano valori multipli.
	 * La mappe sono mantenute in un campo della classe (Map&lt;String, String&gt; <code>requestParams</code> e Map&lt;String, List&lt;String&gt;&gt; <code>requestMultipleValuesParamsMap</code>).
	 * @param req oggetto request da processare
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
		if (this.requestParamsMap == null)
			this.processRequestForParams( req );
		
		return requestParamsMap;
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
}
