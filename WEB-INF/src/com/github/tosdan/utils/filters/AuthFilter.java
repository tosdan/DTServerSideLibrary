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
	private boolean debugUrls = true;

	@Override
	public void init( FilterConfig filterConfig ) throws ServletException {
		this.filterConfig = filterConfig; 		
	}

	@Override
	public void doFilter( ServletRequest request, ServletResponse response, FilterChain chain ) throws IOException, ServletException
	{
		HttpServletRequest req = ( HttpServletRequest ) request;
		HttpSession session = req.getSession();
		
		if (debugUrls) {
			printUrls( req );
		}
		
		String userIDIdentifier = this.filterConfig.getInitParameter( "userIDIdentifier" ); // da web.xml
		String reqUserIDIdentifier = req.getParameter( "userIDIdentifier" ); // da request http <- Se presente ha la precedenza.
		String warnPage = this.filterConfig.getInitParameter( "warnPage" ); // template incluso nella risposta

		String sUserID = (reqUserIDIdentifier != null) ? reqUserIDIdentifier : (String) session.getAttribute(userIDIdentifier);
		
		if ( sUserID != null ) {
			chain.doFilter( req, response );
			
		} else {
			response.setContentType( "text/html; charset=ISO-8859-15" );
			request.getRequestDispatcher(warnPage).include(request, response);
			
		}
	}
	
	private void printUrls(HttpServletRequest req) {
		String pathInfo = req.getPathInfo();
		String referer = req.getHeader("referer");
		String contextPath = this.filterConfig.getServletContext().getContextPath();
		StringBuffer url = req.getRequestURL();
		int k = url.indexOf( contextPath );
		String refererDomain = url.substring( 0, k );
		String uri = req.getRequestURI();		
		String pagina = uri.substring( contextPath.length() );
		int i = ("ContextPath:").length();
		int j = ( k + contextPath.length() );
		System.out.printf( "%-"+i+"s %s\n", "Domain: ", refererDomain);
		System.out.printf( "%-"+i+"s %s\n", "Referer:", referer);
		System.out.printf( "%-"+i+"s %s\n", "Url:", url);
		System.out.printf( "%-"+j+"s %s\n", "ContextPath:", contextPath);
		System.out.printf( "%-"+j+"s %s\n", "URI:", uri);
		System.out.printf("%-"+(j+i)+"s %s\n","Pagina:", pagina);
		System.out.printf("%-"+(j+i)+"s %s","pathInfo:", pathInfo);
	}

	@Override
	public void destroy() {
		this.filterConfig = null;		
	}
}
