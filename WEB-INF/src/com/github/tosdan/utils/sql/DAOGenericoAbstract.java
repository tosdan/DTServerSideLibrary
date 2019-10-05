package com.github.tosdan.utils.sql;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
/**
 * 
 * @author Daniele
 * @version 0.0.1-b2013-09-13
 */
public abstract class DAOGenericoAbstract implements DAOGenerico {

	
	
	protected Connection conn;
	private boolean closeConn;

	
	
	public boolean isCloseConn() {
		return closeConn;
	}


	/**
	 * 
	 * @param closeConn
	 * @return Restituisce l'oggetto stesso su cui si esegue il metodo.
	 */
	@Override
	public void setCloseConn( boolean closeConn ) {
		this.closeConn = closeConn;
	}

	
	/**
	 * 
	 * @param sql Stringa contenente la query sql da eseguire.
	 * @return
	 * @throws SQLException 
	 */
	@Override
	public int update( String sql ) throws SQLException {
		MyQueryRunner run = new MyQueryRunner();
		
		int retVal = run.update( conn, sql );
		
		if (this.closeConn)
			DbUtils.closeQuietly( conn );
		
		return retVal;
	}

	
	/**
	 * Esegue la query sql passata e processa i records estratti attraverso il <code>ResultSetHandler</code> passato.
	 * @param <T> tipo del parametro restituito
	 * @param sql Stringa contenente la query sql da eseguire.
	 * @param rsh {@link ResultSetHandler} con cui processare i records estratti
	 * @return Restituisce un oggetto di tipo <code>&lt;T&gt;</code> analogo a quello usato per costruire il <code>ResultSetHandler&lt;T&gt;</code> passato come parametro.
	 * @throws SQLException 
	 */
	public <T> T query(String sql, ResultSetHandler<T> rsh ) throws SQLException  {
		MyQueryRunner run = new MyQueryRunner();

		T retVal = run.query( conn, sql, rsh );

		if (closeConn)
			DbUtils.closeQuietly( conn );

		return retVal;
	}
	
	
	
	/**
	 * Chiude la connessione (se non nulla) nascondendo evenutali eccezioni.
	 */
	@Override
	public void closeQuietly() {
		DbUtils.closeQuietly( conn );
	}
	
	
	
	/**
	 * Chiude la connessione senza alcun controllo preventivo. Eventuali controlli di valore null sono lasciati al chiamante.
	 * @throws SQLException
	 */
	@Override
	public void close() throws SQLException {
		conn.close();
	}
}
