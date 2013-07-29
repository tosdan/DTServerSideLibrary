package com.github.tosdan.utils.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ArrayHandler;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.apache.commons.dbutils.handlers.KeyedHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;


/**
 * 
 * @author Daniele
 * @version 0.3.1-b2013-07-19
 */
public class BasicDAO
{
	private DataSource dataSource;
	private ConnectionProvider provider;
	private Connection conn;
	private boolean closeConn;
	
	/**
	 * 
	 * @param dataSource
	 */
	public BasicDAO( DataSource dataSource ) {
		this.dataSource = dataSource;
		this.setCloseConn( true );
	}

	/**
	 * 
	 * @param provider
	 */
	public BasicDAO( ConnectionProvider provider ) {
		this.provider = provider;
		this.setCloseConn( true );
	}
	
	/**
	 * 
	 * @param conn
	 */
	public BasicDAO( Connection conn ) {
		this.conn = conn;
		this.setCloseConn( true );
	}
	
	/**
	 * 
	 * @param closeConn
	 * @return Restituisce l'oggetto stesso su cui si esegue il metodo.
	 */
	public BasicDAO setCloseConn( boolean closeConn ) {
		this.closeConn = closeConn;
		return this;
	}

	/**
	 * Imposta lo stato di autoCommit della connessione utilizzata.
	 * @param autoCommit
	 * @return Restituisce l'oggetto stesso su cui si esegue il metodo.
	 * @throws BasicDAOException
	 */
	public BasicDAO setAutoCommit(boolean autoCommit) throws BasicDAOException {
		try {
			if (this.conn != null)
				this.conn.setAutoCommit( autoCommit );
		} catch ( SQLException e ) {
			throw new BasicDAOException( "Errore durante il setAutoCommit().", e );
		}
		
		return this;
	}
	
	/**
	 * Eseguo il commit delle modifiche rendenole permanenti e rilascia eventuali locks mantenuti dall'oggetto {@link Connection }.
	 * @throws BasicDAOException
	 */
	public void commit() throws BasicDAOException {
		try {
			this.conn.commit();
		} catch ( SQLException e ) {
			throw new BasicDAOException( "Errore durante il commit().", e );
		}
	}
	
	/**
	 * Undoes all changes made in the current transaction and releases any database locks currently held by this Connection object.
	 * @throws BasicDAOException
	 */
	public void rollBack() throws BasicDAOException {
		try {
			this.conn.rollback();
		} catch ( SQLException e ) {
			throw new BasicDAOException( "Errore durante il rollBack().", e );
		}
	}
	
	/**
	 * Undoes all changes made after the given Savepoint object was set.
	 * @param savepoint 
	 * @throws BasicDAOException
	 */
	public void rollBack(Savepoint savepoint) throws BasicDAOException {
		try {
			this.conn.rollback( savepoint );
		} catch ( SQLException e ) {
			throw new BasicDAOException( "Errore durante il rollBack().", e );
		}
	}
	
	/**
	 * Creates an unnamed savepoint in the current transaction and returns the new Savepoint object that represents it. If setSavepoint is invoked outside of an active transaction, a transaction will be started at this newly created savepoint.
	 * @return the new {@link Savepoint } object
	 * @throws BasicDAOException
	 */
	public Savepoint setSavepoint() throws BasicDAOException {
		try {
			return this.conn.setSavepoint();
		} catch ( SQLException e ) {
			throw new BasicDAOException( "Errore durante il setSavepoint().", e );
		}
	}
	
	/**
	 * Creates a savepoint with the given name in the current transaction and returns the new {@link Savepoint } object that represents it. If setSavepoint is invoked outside of an active transaction, a transaction will be started at this newly created savepoint.
	 * @param name - a String containing the name of the savepoint
	 * @return the new {@link Savepoint } object
	 * @throws BasicDAOException
	 */
	public Savepoint setSavepoint(String name) throws BasicDAOException {
		try {
			return this.conn.setSavepoint( name );
		} catch ( SQLException e ) {
			throw new BasicDAOException( "Errore durante il setSavepoint(String name).", e );
		}
	}
	
	/**
	 * 
	 * @param sql Stringa contenente la query sql da eseguire.
	 * @return
	 * @throws BasicDAOException
	 */
	public int update( String sql ) throws BasicDAOException {
		Connection conn = this.getConnection();
		MyQueryRunner run = new MyQueryRunner();
		
		try {
			return run.update( conn, sql );
		} catch ( SQLException e ) {
			throw new BasicDAOException( "Errore durante l'update.", e );
		} finally {
			if (this.closeConn)
				DbUtils.closeQuietly( conn );
		}
	}
	

	/**
	 * Estrae una lista di records.
	 * @param sql Stringa contenente la query sql da eseguire.
	 * @return Restituisce i record estratti sotto forma di lista di mappe <code>String</code> -> <code>Object</code>.
	 * @throws BasicDAOException
	 */
	public List<Map<String, Object>> runAndGetMapList(String sql)
			throws BasicDAOException 
	{
		ResultSetHandler<List<Map<String, Object>>> rsh = new MapListHandler( new BasicRowProcessorMod() );
		return runAndGetSomething( sql, rsh );
	}
	
	/**
	 * Estrae un solo record.
	 * @param sql Stringa contenente la query sql da eseguire.
	 * @return Restituisce il record estratto sotto forma di mappa <code>String</code> -> <code>Object</code>.
	 * @throws BasicDAOException
	 */
	public Map<String, Object> runAndGetMap(String sql) 
			throws BasicDAOException 
	{
		ResultSetHandler<Map<String, Object>> rsh = new MapHandler( new BasicRowProcessorMod() );
		return runAndGetSomething( sql, rsh );
	}
	

	/**
	 * Estrae una lista di records.
	 * @param sql Stringa contenente la query sql da eseguire.
	 * @return Restituisce i records estratti sotto forma di lista di <code>Object[]</code> 
	 * @throws BasicDAOException
	 */
	public List<Object[]> runAndGetArrayList(String sql) 
			throws BasicDAOException 
	{
		ResultSetHandler<List<Object[]>> rsh = new ArrayListHandler( new BasicRowProcessorMod() );
		return runAndGetSomething( sql, rsh );
	}
	

	/**
	 * Estrae un solo record.
	 * @param sql Stringa contenente la query sql da eseguire.
	 * @return Restituisce il record estratto sotto forma di <code>Object[]</code> 
	 * @throws BasicDAOException
	 */
	public Object[] runAndGetArray(String sql) 
			throws BasicDAOException 
	{
		ResultSetHandler<Object[]> rsh = new ArrayHandler( new BasicRowProcessorMod() );
		return runAndGetSomething( sql, rsh );
	}
	
	/**
	 * Produce una mappa dei records estratti. <i>NB. Com'e' logico pensare, non ha senso per chiavi che non siano univoche.</i>
	 * @param sql Stringa contenente la query sql da eseguire.
	 * @param columnToBeKey Nome della colonna da usare come chiave per la mappa. Deve essere una chiave univoca nella tabella estratta.
	 * @return Restitiusce una mappa con chiave <code>columnToBeKey</code> e per valore il record associato a tale chiave, memorizzato sotto forma di mappa <code>String</code> -> <code>Object</code>.
	 * @throws BasicDAOException
	 */
	public Map<String, Map<String, Object>> runAndGetKeyedMap(String sql, String columnToBeKey) throws BasicDAOException 
	{
		ResultSetHandler<Map<String, Map<String, Object>>> rsh = new KeyedHandler<String>( columnToBeKey );
		return runAndGetSomething( sql, rsh );
	}
	
	

	/**
	 * Raggruppa i records estratti in liste. <i>Ovviamente non e' necessario che la chiave scelta per raggruppare i records sia univoca.</i>
	 * @param sql Stringa contenente la query sql da eseguire.
	 * @param columnToBeKey Nome della colonna da usare come chiave per la mappa.
	 * @return Restituisce una mappa raggruppata secondo la chiave <code>columnToBeKey</code>. Ad ogni chiave e'associata una lista, filtrata secondo la chiave scelta, contenente i records estratti, memorizzati come mappe <code>String</code> -> <code>Object</code>.
	 * @throws BasicDAOException
	 */
	public Map<String, List<Map<String, Object>>> runAndGetKeyedMapList(String sql, String columnToBeKey) throws BasicDAOException 
	{
		ResultSetHandler<Map<String, List<Map<String, Object>>>> rsh = new KeyedMapListHandler<String>( columnToBeKey );
		return runAndGetSomething( sql, rsh );
	}
	
	
	/**
	 * Esegue la query sql passata e processa i records estratti attraverso il <code>ResultSetHandler</code> passato.
	 * @param <T> tipo del parametro restituito
	 * @param sql Stringa contenente la query sql da eseguire.
	 * @param rsh {@link ResultSetHandler} con cui processare i records estratti
	 * @return Restituisce un oggetto di tipo <code>&lt;T&gt;</code> analogo a quello usato per costruire il <code>ResultSetHandler&lt;T&gt;</code> passato come parametro.
	 * @throws BasicDAOException
	 */
	public <T> T runAndGetSomething(String sql, ResultSetHandler<T> rsh ) 
			throws BasicDAOException 
	{
		Connection conn = this.getConnection();
		MyQueryRunner run = new MyQueryRunner();
		
		try {
			return run.query( conn, sql, rsh );
		} catch ( SQLException e ) {
			throw new BasicDAOException( "Errore durante l'estrazione.", e );
		} finally {
			if (closeConn)
				DbUtils.closeQuietly( conn );
		}
		
	}
	
	/**
	 * Restituisce la connessione corrente o dopo averne aperta una da un {@link ConnectionProvider } o da un {@link DataSource }
	 * @return
	 * @throws BasicDAOException 
	 */
	private Connection getConnection() throws BasicDAOException
	{
		if (this.conn == null) {
			
			if (this.dataSource != null) {
				
				try {
					return this.dataSource.getConnection();
				} catch ( SQLException e ) {
					throw new BasicDAOException( "Errore di accesso al database durante l'ottenimento della connessione dal DataSource.", e );
				}
				
			} else if (this.provider != null) {
				
				try {
					return provider.stabilisciConnessione();
				} catch ( ConnectionProviderException e ) {
					throw new BasicDAOException( "Errore di accesso al database durante l'ottenimento della connessione dal ConnectionProvider.", e );
				}
				
			} else {
				throw new BasicDAOException("Non è stata fornita una sorgente valida per stabilire una connessione. Datasource, ConnectinProvider o Connection sono null.");
			}
		}

		return conn;
	}
	
	/**
	 * Chiude la connessione (se non nulla) nascondendo evenutali eccezioni.
	 */
	public void closeQuietly() {
		DbUtils.closeQuietly( this.conn );
		
	}
	
	/**
	 * Chiude la connessione senza alcun controllo preventivo. Eventuali controlli di valore null sono lasciato al chiamante.
	 * @throws SQLException
	 */
	public void close() throws SQLException {
		this.conn.close();
		
	}
	
}
