package com.github.tosdan.utils.servlets;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;


import lime49.lockcrypt.WildcardURL;

/**
 * 
 * @author Daniele
 * @version 0.1.3-b2013-08-25
 */
public class ServletUtils
{
	private HttpServletRequest config;
	private WildcardURL wurl;
	private boolean validReferer;
	
	public ServletUtils(HttpServletRequest request) {
		setRequest(request);
	}
	
	private void setRequest(HttpServletRequest req) {
		this.config = req;
		validReferer = getReferer() != null;
		if (validReferer)
			wurl = new WildcardURL(getReferer());
	}
	
	public ServletUtils resetRequest(HttpServletRequest req) {
		setRequest(req);
		return this;
	}
	
	public String getCtxPath() {
		if (validReferer)
			return config.getContextPath();
		return null;
	}

	public String getReferer() {
		return config.getHeader("referer");
	}
	
	public String getPath() {
		if (validReferer)
			return wurl.getPath();
		return null;
	}
	
	public String getRequestedPage() {
		if (validReferer)
			return wurl.getFile();
		return null;
	}
	
	public Integer getPort() {
		if (validReferer)
			return wurl.getPort();
		return null;
	}
	
	public String getProtocol() {
		if (validReferer)
			return wurl.getProtocol();
		return null;
	}
	
	public String getAuthority() {
		if (validReferer)
			return wurl.getAuthority();
		return null;				
	}

	public String getDomain() {
		if (validReferer)
			return wurl.getHost();
		return null;
	}
	
	public String getDirectory() {
		if (validReferer)
			return wurl.getDirectory();
		return null;
	}
	
	public String getRef() {
		if (validReferer)
			return wurl.getRef();
		return null;
	}
	
	public String getQuery() {
		if (validReferer)
			return wurl.getQuery();
		return null;
	}
	
	public String getUserInfo() {
		if (validReferer)
			return wurl.getUserInfo();
		return null;
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
								.append("getAuthority", getAuthority())
								.append("getCtxPath", getCtxPath())
								.append("getDirectory", getDirectory())
								.append("getDomain", getDomain())
								.append("getPath", getPath())
								.append("getPort", getPort())
								.append("getProtocol", getProtocol())
								.append("getQuery", getQuery())
								.append("getRef", getRef())
								.append("getReferer", getReferer())
								.append("getRequestedPage", getRequestedPage())
								.append("getUserInfo", getUserInfo())
								.toString();
	}
	
	/*******************************************************/
	/*************		Metodi statici		****************/
	/*******************************************************/

	private final static int VALORE_SINGOLO = 0; 
	private final static int VALORE_MULTIPLO = 1; 
	private final static int VALORE_SINGOLO_E_MULTIPLO = 2; 

	/**
	 * Interfaccia per filtri su parametri e attributi
	 */
	public interface NamesFilter {
		public boolean accept(String name, Object value);
	}
	
	/**
	 * 
	 * @param req
	 * @return
	 */
	public static Map<String, Object> getReqParameters(HttpServletRequest req) {
		return getReqParameters( req, null);
	}
	
	/**
	 * 
	 * @param req
	 * @param filter
	 * @return
	 */
	public static Map<String, Object> getReqParameters(HttpServletRequest req, NamesFilter filter) {
		return reqParamsParser( req, filter, VALORE_SINGOLO_E_MULTIPLO );
	}
	
	/**
	 * 
	 * @param req oggetto request da processare
	 * @return 
	 */
	public static Map<String, String> getReqSingleValueParams(HttpServletRequest req) {
		return getReqSingleValueParams( req, null );
	}
	/**
	 * 
	 * @param req
	 * @param filter
	 * @return
	 */
	public static Map<String, String> getReqSingleValueParams(HttpServletRequest req, NamesFilter filter) {
		return reqParamsParser( req, filter, VALORE_SINGOLO );
	}
	
	/**
	 * 
	 * @param req oggetto request da processare
	 * @return 
	 */
	public static Map<String, List<String>> getReqMultipleValuesParams(HttpServletRequest req) { 
		return getReqMultipleValuesParams( req, null );
	}
	
	/**
	 * 
	 * @param req
	 * @param filter
	 * @return
	 */
	public static Map<String, List<String>> getReqMultipleValuesParams(HttpServletRequest req, NamesFilter filter) {
		return reqParamsParser( req, filter, VALORE_MULTIPLO );
	}
	
	/**
	 * 
	 * @param req
	 * @param filter
	 * @return
	 */
	@SuppressWarnings( "unchecked" )
	private static <T> Map<String, T> reqParamsParser(HttpServletRequest req, NamesFilter filter, int tipo)
	{
		Map<String, T> requestParamsMap = new HashMap<String, T>();
		
		Enumeration<String> paramsNames = req.getParameterNames();
		while ( paramsNames.hasMoreElements() ) {
			String name = paramsNames.nextElement();
			String[] values = req.getParameterValues(name);
			
			if ( values.length == 1 ) {
				
				if ( (tipo == VALORE_SINGOLO || tipo == VALORE_SINGOLO_E_MULTIPLO) ) {
					
					if ( filter == null )
						requestParamsMap.put( name, ( T ) values[ 0 ] );
					else if ( filter.accept( name, values ) )
						requestParamsMap.put( name, ( T ) values[ 0 ] );
				}
				
				
			} else if ( values.length > 1 ) {
				
				if	(tipo == VALORE_MULTIPLO || tipo == VALORE_SINGOLO_E_MULTIPLO) {
					
					if (filter == null)
						requestParamsMap.put(name, ( T ) Arrays.asList(values));
					else if ( filter.accept(name, values) )
						requestParamsMap.put(name, ( T ) Arrays.asList(values));
				}
			}
			
			
		}
		return requestParamsMap;
	}
	
	
	/**
	 * 
	 * @param req
	 * @return
	 */
	public static Map<String, Object> getReqAttributes(HttpServletRequest req) {
		return getReqAttributes(req, null);
	}
	/**
	 * 
	 * @param req
	 * @param filter
	 * @return
	 */
	public static Map<String, Object> getReqAttributes(ServletRequest req, NamesFilter filter) {
		return getGenericObjAttributes(new ServletUtils.GenericAttribGetterWrapper<ServletRequest>(req, ServletRequest.class), filter);
	}
	
	
	/**
	 * 
	 * @param session
	 * @return
	 */
	public static Map<String, Object> getSessionAttributes(HttpSession session) {
		return getSessionAttributes(session, null);
	}
	/**
	 * 
	 * @param session
	 * @param filter
	 * @return
	 */
	public static Map<String, Object> getSessionAttributes(HttpSession session, NamesFilter filter) {
		return getGenericObjAttributes(new ServletUtils.GenericAttribGetterWrapper<HttpSession>(session, HttpSession.class), filter);
	}
	
	
	/**
	 * 
	 * @param attribsContainer oggetto request da processare
	 * @return 
	 */
	private static Map<String, Object> getGenericObjAttributes(AttribsGetter attribsContainer, NamesFilter filter)
	{
		Map<String, Object> attribsMap = new HashMap<String, Object>();
		@SuppressWarnings( "unchecked" )
		Enumeration<String> attributes = attribsContainer.getAttributeNames();
		while ( attributes.hasMoreElements() ) {
			String attribName = (String) attributes.nextElement();
			Object attribValue = attribsContainer.getAttribute(attribName);
			
			if (filter == null)
				attribsMap.put( attribName, attribValue );
			 else if ( filter.accept(attribName, attribValue) )
				attribsMap.put( attribName, attribValue );
		}
		return attribsMap;
	}
	
	/**
	 * 
	 * @author Daniele
	 *
	 */
	private interface AttribsGetter {
		@SuppressWarnings( "rawtypes" )
		Enumeration getAttributeNames();
		Object getAttribute(String arg0);
	}
	/**
	 * 
	 * @author Daniele
	 *
	 * @param <T>
	 */
	private static class GenericAttribGetterWrapper<T> implements AttribsGetter {
		private T obj;
		Method getAttributeNames;
		Method getAttribute;
		
		public GenericAttribGetterWrapper(T obj, Class<T> clazz) { 
			this.obj = obj;
			try {
				getAttributeNames = clazz.getDeclaredMethod("getAttributeNames", new Class[] {} );
				getAttribute = clazz.getDeclaredMethod("getAttribute", new Class[] {String.class} );
			}
			catch ( SecurityException e ) { reThrow(e.getMessage(), e); } 
			catch ( NoSuchMethodException e ) { reThrow(e.getMessage(), e); }
		}
		
		@SuppressWarnings( "rawtypes" )
		@Override public Enumeration getAttributeNames() {
			Enumeration retVal = null;
			try {
				retVal = (Enumeration) getAttributeNames.invoke(obj);				
			} 
			catch ( IllegalAccessException e ) { reThrow(e.getMessage(), e); }
			catch ( InvocationTargetException e ) { reThrow(e.getMessage(), e); }
			return retVal; 
		}
		
		@Override public Object getAttribute( String arg0 ) {
			Object retVal = null;
			try {
				retVal = getAttribute.invoke(obj, arg0);				
			} 
			catch ( IllegalAccessException e ) { reThrow(e.getMessage(), e); }
			catch ( InvocationTargetException e ) { reThrow(e.getMessage(), e); }
			return retVal; 
		}	
	}

	/**
	 * 
	 * @param ctx
	 * @return
	 */
	public static Map<String, String> getServletContextInitParameters(ServletContext ctx) {
		return getServletContextInitParameters(ctx, null);
	}
	/**
	 * 
	 * @param ctx
	 * @param filter
	 * @return
	 */
	public static Map<String, String> getServletContextInitParameters(ServletContext ctx, NamesFilter filter) {
		return getInitParameters(new ServletUtils.GenericInitParamGetterWrapper<ServletContext>(ctx, ServletContext.class), filter);
	}
	/**
	 * 
	 * @param servlet
	 * @return
	 */
	public static Map<String, String> getServletInitParameters(ServletConfig servlet) {
		return getServletInitParameters(servlet, null);
	}
	/**
	 * 
	 * @param servlet
	 * @param filter
	 * @return
	 */
	public static Map<String, String> getServletInitParameters(ServletConfig servlet, NamesFilter filter) {
		return getInitParameters(new ServletUtils.GenericInitParamGetterWrapper<ServletConfig>(servlet, ServletConfig.class), filter);
	}
	
	/**
	 * 
	 * @param config
	 * @return
	 */
	public static Map<String, String> getFilterInitParameters(FilterConfig config) {
		return getFilterInitParameters(config, null);
	}
	/**
	 * 
	 * @param config
	 * @param filter
	 * @return
	 */
	public static Map<String, String> getFilterInitParameters(FilterConfig config, NamesFilter filter) {
		return getInitParameters(new ServletUtils.GenericInitParamGetterWrapper<FilterConfig>(config, FilterConfig.class), filter);
	}
	
	
	/**
	 * 
	 * @param paramsContainer
	 * @param filter
	 * @return
	 */
	private static Map<String, String> getInitParameters(InitParamsGetter paramsContainer, NamesFilter filter) {
		Map<String, String> paramsMap = new HashMap<String, String>();
		@SuppressWarnings( "unchecked" )
		Enumeration<String> params = paramsContainer.getInitParameterNames();
		while ( params.hasMoreElements() ) {
			String name = (String) params.nextElement();
			String value = paramsContainer.getInitParameter(name);
			
			if (filter == null)
				paramsMap.put( name, value );
			 else if ( filter.accept(name, value) )
				paramsMap.put( name, value );
		}
		return paramsMap;
	}
	
	/**
	 * 
	 * @author Daniele
	 *
	 */
	private interface InitParamsGetter {
		@SuppressWarnings( "rawtypes" )
		Enumeration getInitParameterNames();
		String getInitParameter(String arg0);
	}
	/**
	 * 
	 * @author Daniele
	 *
	 * @param <T>
	 */
	private static class GenericInitParamGetterWrapper<T> implements InitParamsGetter {
		private T obj;
		Method getInitParameterNames;
		Method getInitParameter;
		
		public GenericInitParamGetterWrapper(T obj, Class<T> clazz) { 
			this.obj = obj;
			try {
				getInitParameterNames = clazz.getDeclaredMethod("getInitParameterNames", new Class[] {} );
				getInitParameter = clazz.getDeclaredMethod("getInitParameter", new Class[] {String.class} );
			}
			catch ( SecurityException e ) { reThrow(e.getMessage(), e); } 
			catch ( NoSuchMethodException e ) { reThrow(e.getMessage(), e); }
		}
		@SuppressWarnings( "rawtypes" )
		@Override public Enumeration getInitParameterNames() {
			Enumeration retVal = null;
			try {
				retVal = (Enumeration) getInitParameterNames.invoke(obj);
			} 
			catch ( IllegalAccessException e ) { reThrow(e.getMessage(), e); }
			catch ( InvocationTargetException e ) { reThrow(e.getMessage(), e); }
			return retVal; 
		}
		@Override public String getInitParameter( String arg0 ) {
			String retVal = null;
			try {
				retVal = (String) getInitParameter.invoke(obj, arg0);
			}
			catch ( IllegalAccessException e ) { reThrow(e.getMessage(), e); }
			catch ( InvocationTargetException e ) { reThrow(e.getMessage(), e); }
			return retVal; 
		}	
	}

	
	/**
	 * 
	 * @param msg
	 * @param t
	 */
	private static void reThrow(String msg, Throwable t) {
		throw new IllegalArgumentException(msg, t);
	}

}
