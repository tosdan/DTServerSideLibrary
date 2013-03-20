package com.github.tosdan.utils.filters;

import java.io.IOException;

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
		String sUserID = req.getParameter( userIDIdentifier );
		
		if ( session.getAttribute(sUserID) != null ) {
			chain.doFilter( req, response );
		} else {
			request.getRequestDispatcher(warnPage).include(request, response);
		}
		
	}


	@Override
	public void destroy() {
		this.filterConfig = null;		
	}
}
