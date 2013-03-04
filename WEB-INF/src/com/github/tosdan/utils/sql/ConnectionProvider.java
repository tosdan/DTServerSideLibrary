package com.github.tosdan.utils.sql;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;


public interface ConnectionProvider
{ 
	/**
	 *  
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ConnectionProviderException 
	 */
	public Connection stabilisciConnessione() throws ConnectionProviderException;
	
	/**
	 * 
	 * @param dbConfFile File di configurazione connessione al DB.
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IOException
	 */
	public Connection stabilisciConnessione(String dbConfFile) throws ConnectionProviderException;
		
	/**
	 * 
	 * @param dbConfFile File di configurazione connessione al DB.
	 * @throws IOException
	 */
	public void caricaDBConf( String dbConfFile ) throws ConnectionProviderException;
}
