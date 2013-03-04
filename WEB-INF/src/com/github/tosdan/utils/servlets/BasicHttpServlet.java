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

@SuppressWarnings( "serial" )
public abstract class BasicHttpServlet extends HttpServlet
{
	/** 
	 * ServletContext di questa servlet
	 */
	protected ServletContext app;
	
	/**
	 * Contiene i parametri di inizializzazione specifici della servlet (per intendersi: presi dall'oggetto ServletConfig)
	 */
	protected HashMap<String, String> envConfigParams;
	
	/**
	 * Path assoluto su disco della cartella di questa webapp.
	 * Puo' esser utile da passare ad oggetti che non siano istanze di Servlet 
	 */
	protected String realPath;
	
	/**
	 * Mappa dei parametri della request: solo quelli a valore singolo
	 */
	protected Map<String, String> requestParams;
	
	/**
	 * Mappa dei parametri della request: solo quelli a valore multiplo
	 */
	protected Map<String, List<String>> requestMultipleValuesParams;
	
	
	@Override
	public void init( ServletConfig config ) throws ServletException {
		super.init( config );
		this.app = config.getServletContext();
		
		this.envConfigParams = new HashMap<String, String>();
		this.realPath = app.getRealPath( "/" );
		this.envConfigParams.put( "realPath", realPath );
		
		@SuppressWarnings( "rawtypes" )
		Enumeration parametriServlet = config.getInitParameterNames();
		while (parametriServlet.hasMoreElements()) {
			String nomeParam = ( String ) parametriServlet.nextElement();
			String valore = config.getInitParameter( nomeParam );
			this.envConfigParams.put( nomeParam, valore );
			
//			System.out.println( nomeParam + " - " + valore );
//			System.out.println( System.getProperty("catalina.base") );
		}
		
	}

	/**
	 * Estrae i parametri dalla request e li inseriesce in una Mappa da stringa a stringa ignorando i parametri che abbiano valori multipli.
	 * La mappa è mantenuta in un campo della classe.
	 * @param req
	 * @return Log testuale delle associazioni parametro=>valoreParametro dei parametri estratti
	 */
	protected String processRequestForParams(HttpServletRequest req)
	{
		String reqLog = "";
		@SuppressWarnings( "unchecked" )
		Enumeration<String> e = req.getParameterNames();
		this.requestParams = new HashMap<String, String>();
		while ( e.hasMoreElements() ) {
			String elem = (String) e.nextElement();
			String val = req.getParameter(elem);
			if ( req.getParameterValues(val) == null )
				this.requestParams.put(elem, val);
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
	 * @return Log testuale delle associazioni parametro=>valoreParametro dei parametri estratti
	 */
	protected String processRequestForMultipleValuesParams(HttpServletRequest req)
	{
		String reqLog = "";
		@SuppressWarnings( "unchecked" )
		Enumeration<String> e = req.getParameterNames();
		this.requestMultipleValuesParams = new HashMap<String, List<String>>();
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
				this.requestMultipleValuesParams.put(elem, values);
				reqLog += elem+"=>"+values+"\n";
			}
		}
		reqLog += "---- Fine parametri checkbox ----";
		
		return reqLog;
	}
	
}
