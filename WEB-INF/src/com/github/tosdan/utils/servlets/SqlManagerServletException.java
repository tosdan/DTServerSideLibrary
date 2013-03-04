package com.github.tosdan.utils.servlets;

import javax.servlet.ServletException;

@SuppressWarnings( "serial" )
public class SqlManagerServletException extends ServletException
{

	public SqlManagerServletException()
	{
		// TODO Auto-generated constructor stub
	}

	public SqlManagerServletException( String message )
	{
		super( message );
		// TODO Auto-generated constructor stub
	}

	public SqlManagerServletException( Throwable rootCause )
	{
		super( rootCause );
		// TODO Auto-generated constructor stub
	}

	public SqlManagerServletException( String message, Throwable rootCause )
	{
		super( message, rootCause );
		// TODO Auto-generated constructor stub
	}

}
