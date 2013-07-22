package com.github.tosdan.utils.sql;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Utilizzare ConnectionProviderImplV2 
 * @author Daniele
 *
 */
@Deprecated
public class ConnectionProviderImpl implements ConnectionProvider
{
	private Connection conn;
	private String driver;
	private String url;
	private String username;
	private String password;
//	private String dbType;
	
	public ConnectionProviderImpl()
	{
	}
	
	public ConnectionProviderImpl(String dbConfFile) throws ConnectionProviderException
	{
		this.caricaDBConf( dbConfFile );
	}
	
	public ConnectionProviderImpl( String driver, String url, String username, String password )
	{
		this.driver = driver;
		this.url = url;
		this.username = username;
		this.password = password;
	}

	@Override
	public Connection stabilisciConnessione(String dbConfFile) throws ConnectionProviderException
	{
		this.caricaDBConf( dbConfFile );
		return stabilisciConnessione();
		
	}
	
	@Override
	public Connection stabilisciConnessione() throws ConnectionProviderException
	{
		try {
			
			if ( conn == null || conn.isClosed() )
			{
				this.connect();
			}
			
		} catch ( SQLException e ) {
			throw new ConnectionProviderException( "Errore nella connessione.", e );
		}
		
		return conn;
		
	}
	
	private void connect() throws ConnectionProviderException
	{
		try {
			
			Class.forName( driver ).newInstance();
			
		} catch ( InstantiationException e ) {
			throw new ConnectionProviderException( e );
		} catch ( IllegalAccessException e ) {
			throw new ConnectionProviderException( e );
		} catch ( ClassNotFoundException e ) {
			throw new ConnectionProviderException( e );
		}
		
		try {
			
			this.conn = DriverManager.getConnection( url, username, password);
			
		} catch ( SQLException e ) {
			throw new ConnectionProviderException( "Errore creazione connessione.", e );
		}
	}
	

	@Override
	public void caricaDBConf( String dbConfFile ) throws ConnectionProviderException
	{
		Properties prop = new Properties();
		FileInputStream fis = null;
		
        try {
        	fis = new FileInputStream( dbConfFile );
			prop.load(fis);
			
		} catch ( FileNotFoundException e ) {
			throw new ConnectionProviderException( "File di configurazione DB non trovato.", e );
		} catch ( IOException e ) {
			throw new ConnectionProviderException( "Errore in accesso al file di configurazione DB.", e );
		} finally {
        	try {
				fis.close();
				
			}
			catch ( Throwable t ) {
				t.printStackTrace();
			}
        }
        
        this.driver = prop.getProperty("driver");
        this.url = prop.getProperty("url");
        this.username = prop.getProperty("username");
        this.password = prop.getProperty("password");       
//        this.dbType   = prop.getProperty("dbtype");   
	}

}
