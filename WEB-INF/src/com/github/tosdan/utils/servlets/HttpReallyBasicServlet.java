package com.github.tosdan.utils.servlets;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings( "serial" )
public abstract class HttpReallyBasicServlet extends HttpServlet {
	
	protected ServletContext ctx;
	@Override protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException { this.doService(req, resp); }
	@Override protected void doPost( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException { this.doService(req, resp); }
	@Override
	public void init() throws ServletException {
		super.init();
		ctx = getServletContext();
		inizializza();
	}

	/**
	 * Metodo da sovrascrivere per effettuare operazioni di inizilizzazione. Se viene sovrascritto init() si perde il campo <code>ServletContext</code> ctx
	 */
	protected void inizializza() {
	}
	
	/**
	 * Metodo astratto chiamato sia in caso di request GET che POST.
	 * @param req
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 */
	public abstract void doService( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException;
}
