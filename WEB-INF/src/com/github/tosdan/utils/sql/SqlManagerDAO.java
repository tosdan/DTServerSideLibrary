package com.github.tosdan.utils.sql;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;



public class SqlManagerDAO
{
	private DataSource dataSource;
	private ConnectionProvider provider;
	private Connection conn;
	
	public SqlManagerDAO( DataSource dataSource ) {
		this.dataSource = dataSource;
	}

	public SqlManagerDAO( ConnectionProvider provider ) {
		this.provider = provider;
	}

	public int update( String sql ) throws SqlManagerDAOException 
	{
		Connection conn = this.getConnection();
		QueryRunner run = new QueryRunner();
		
		try {
			return run.update( conn, sql );
		} catch ( SQLException e1 ) {
			throw new SqlManagerDAOException( "Errore di accesso al database.", e1 );
		}
		finally {
			DbUtils.closeQuietly( conn );
		}
	}
	

	/**
	 * 
	 * @return
	 * @throws SqlManagerDAOException 
	 */
	private Connection getConnection() throws SqlManagerDAOException
	{
		if (this.conn == null) {
			
			if (this.dataSource != null) {
				
				try {
					return this.dataSource.getConnection();
				} catch ( SQLException e ) {
					throw new SqlManagerDAOException( "Errore durante la connessione.", e );
				}
				
			} else if (this.provider != null) {
				
				try {
					return provider.stabilisciConnessione();
				} catch ( ConnectionProviderException e ) {
					throw new SqlManagerDAOException( "Errore durante la connessione.", e );
				}
				
			} else {
				throw new SqlManagerDAOException("Datasource e ConnectionProvider sono stati entrambi forniti nulli.");
			}
		}

		return conn;
	}
}
