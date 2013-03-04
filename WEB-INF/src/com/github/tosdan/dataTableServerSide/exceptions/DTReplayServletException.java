package com.github.tosdan.dataTableServerSide.exceptions;

import javax.servlet.ServletException;

@SuppressWarnings( "serial" )
public class DTReplayServletException extends ServletException
{

	public DTReplayServletException()
	{
		// TODO Auto-generated constructor stub
	}

	public DTReplayServletException( String message )
	{
		super( message );
		// TODO Auto-generated constructor stub
	}

	public DTReplayServletException( Throwable rootCause )
	{
		super( rootCause );
		// TODO Auto-generated constructor stub
	}

	public DTReplayServletException( String message, Throwable rootCause )
	{
		super( message, rootCause );
		// TODO Auto-generated constructor stub
	}

}
