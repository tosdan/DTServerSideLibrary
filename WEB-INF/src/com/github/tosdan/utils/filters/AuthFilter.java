package com.github.tosdan.utils.filters;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class AuthFilter implements Filter
{
	private FilterConfig filterConfig;

	@Override
	public void init( FilterConfig filterConfig ) throws ServletException {
		this.filterConfig = filterConfig; 		
	}

	@Override
	public void doFilter( ServletRequest request, ServletResponse response, FilterChain chain ) throws IOException, ServletException
	{
		HttpServletRequest req = ( HttpServletRequest ) request;
		HttpSession session = req.getSession();
		String userIDIdentifier = this.filterConfig.getInitParameter( "userIDIdentifier" );
		String warnPage = this.filterConfig.getInitParameter( "warnPage" );
		String sUserID = (String) session.getAttribute(userIDIdentifier);
		String usefulSessionParamsIdentifier = this.filterConfig.getInitParameter( "UsefullSessionParamsIdentifier" );
		
		if ( sUserID != null ) {
			@SuppressWarnings( "unchecked" )
			Map<String, Object> mapUsefulParams = (Map<String, Object>) session.getAttribute( usefulSessionParamsIdentifier );
			if (mapUsefulParams == null) {
				mapUsefulParams = new HashMap<String, Object>();
				session.setAttribute( usefulSessionParamsIdentifier, mapUsefulParams );
			}
			mapUsefulParams.put( userIDIdentifier, sUserID );
			chain.doFilter( req, response );
		} else {
			response.setContentType( "text/html; charset=ISO-8859-1" );
			request.getRequestDispatcher(warnPage).include(request, response);
		}
	}

	@Override
	public void destroy() {
		this.filterConfig = null;		
	}
}
