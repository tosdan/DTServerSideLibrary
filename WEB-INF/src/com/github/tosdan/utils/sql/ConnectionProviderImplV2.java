package com.github.tosdan.utils.sql;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;


public class ConnectionProviderImplV2 implements ConnectionProvider
{
	private Connection conn;
	private String driver;
	private String url;
	private String username;
	private String password;
//	private String dbType;
	private String dbConfFile;
	
	public ConnectionProviderImplV2()
	{
	}
	
	public ConnectionProviderImplV2(String dbConfFile) {
		this.dbConfFile = dbConfFile;
	}
	
	public ConnectionProviderImplV2( String driver, String url, String username, String password ) {
		this.driver = driver;
		this.url = url;
		this.username = username;
		this.password = password;
	}

	@Override
	public Connection stabilisciConnessione() throws ConnectionProviderException {
		return stabilisciConnessione(this.dbConfFile);
		
	}
	
	@Override
	public Connection stabilisciConnessione(String dbConfFile) throws ConnectionProviderException {
		this.caricaDBConf( dbConfFile );
		try {
			
			if ( conn == null || conn.isClosed() ) {
				this.connect();
			}
			
		} catch ( SQLException e ) {
			throw new ConnectionProviderException( "Errore nella connessione.", e );
		}
		
		return conn;
		
	}
	
	private void connect() throws ConnectionProviderException {
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
		this.dbConfFile = dbConfFile;
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
        this.url = (prop.getProperty("url") != null) ? prop.getProperty("url") : prop.getProperty("connection");
        this.username = prop.getProperty("username");
        this.password = prop.getProperty("password");       
//        this.dbType   = prop.getProperty("dbtype");   
	}

}
