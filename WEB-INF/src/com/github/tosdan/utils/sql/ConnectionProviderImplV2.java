package com.github.tosdan.utils.sql;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 
 * @author Daniele
 * @version 0.1.1-b2013-07-19
 */
public class ConnectionProviderImplV2 implements ConnectionProvider
{
	private Connection conn;
	private String driver;
	private String url;
	private String username;
	private String password;
//	private String dbType;
	private String dbConfFile;
	private boolean isDBConfLoaded;
	// TODO campo per indicare se la connsessione deve esser chiusa dal chiamato o se lasciata aperta perche' e' il chiamante che si impegnera' a chiuderla
	@Deprecated
	public ConnectionProviderImplV2()
	{
	}
	
	/**
	 * 
	 * @param dbConfFile Percorso assoluto del file di configurazione con i parametri di accesso al database.
	 */
	public ConnectionProviderImplV2(String dbConfFile) {
		this.dbConfFile = dbConfFile;
		this.isDBConfLoaded = false;
	}
	
	/**
	 * 
	 * @param driver
	 * @param url
	 * @param username
	 * @param password
	 */
	public ConnectionProviderImplV2( String driver, String url, String username, String password ) {
		this.driver = driver;
		this.url = url;
		this.username = username;
		this.password = password;
		this.isDBConfLoaded = false;
	}

	/**
	 * Restituisce una connessione al database.
	 * @return Un oggetto {@link Connection}
	 * @throws ConnectionProviderException
	 */
	public Connection getConnection() throws ConnectionProviderException {
		return stabilisciConnessione();
		
	}
	
	/**
	 * Restituisce una connessione al database. <i>Equivalente a getConnection(), anzi questo metodo viene chiamato da getConnection()</i>
	 * @return Un oggetto {@link Connection}
	 * @throws ConnectionProviderException
	 */
	@Override
	public Connection stabilisciConnessione() throws ConnectionProviderException {
		if ( ! this.isDBConfLoaded )
			caricaDBConf( this.dbConfFile );
		return connect();
		
	}
	
	/**
	 * Restituisce una connessione al database.
	 * @param dbConfFile Percorso assoluto del file di configurazione con i parametri di accesso al database.
	 * @return Un oggetto {@link Connection}
	 * @throws ConnectionProviderException
	 */
	public Connection getConnection(String dbConfFile) throws ConnectionProviderException {
		return stabilisciConnessione(dbConfFile);
		
	}
	
	/**
	 * Restituisce una connessione al database. <i>Equivalente a getConnection(String dbConfFile), anzi questo metodo viene chiamato da getConnection()</i>
	 * @param dbConfFile Percorso assoluto del file di configurazione con i parametri di accesso al database.
	 * @return Un oggetto {@link Connection}
	 * @throws ConnectionProviderException
	 */
	@Override
	public Connection stabilisciConnessione(String dbConfFile) throws ConnectionProviderException {
		caricaDBConf( dbConfFile );
		return connect();
		
	}
	
	/**
	 * 
	 * @return
	 * @throws ConnectionProviderException
	 */
	private Connection connect()  throws ConnectionProviderException {
		try {
			
			if ( conn == null || conn.isClosed() ) {
				
				try {
					
					Class.forName( driver ).newInstance();
					
				} catch ( InstantiationException e ) {
					throw new ConnectionProviderException( "Costruttore classe Driver non accessibile.", e );
				} catch ( IllegalAccessException e ) {
					throw new ConnectionProviderException( "Creazione istanza classe Driver fallita.", e );
				} catch ( ClassNotFoundException e ) {
					throw new ConnectionProviderException( "Impossibile trovare la classe del Driver", e );
				}
				
				this.conn = DriverManager.getConnection( url, username, password);
				
			}
			
		} catch ( SQLException e ) {
			throw new ConnectionProviderException( "Errore in fase di connessione.", e );
		}
		
		return this.conn;
		
	}
	

	/**
	 * 
	 * @param dbConfFile
	 * @throws ConnectionProviderException
	 */
	@Override
	public void caricaDBConf( String dbConfFile ) throws ConnectionProviderException
	{
		this.dbConfFile = dbConfFile;
		this.isDBConfLoaded = true;
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
