package com.github.tosdan.utils.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ArrayHandler;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.apache.commons.dbutils.handlers.KeyedHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;



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

	public int update( String sql ) throws SqlManagerDAOException {
		Connection conn = this.getConnection();
		QueryRunner run = new QueryRunner();
		
		try {
			return run.update( conn, sql );
		} catch ( SQLException e ) {
			throw new SqlManagerDAOException( "Errore di accesso al database.", e );
		} finally {
			DbUtils.closeQuietly( conn );
		}
	}
	

	public List<Map<String, Object>> runAndGetMapList(String sql)
			throws SqlManagerDAOException 
	{
		ResultSetHandler<List<Map<String, Object>>> rsh = new MapListHandler( new BasicRowProcessorMod() );
		return ( List<Map<String, Object>> ) runAndGetSomething( sql, rsh );
	}
	
	public Map<String, Object> runAndGetMap(String sql) 
			throws SqlManagerDAOException 
	{
		ResultSetHandler<Map<String, Object>> rsh = new MapHandler( new BasicRowProcessorMod() );
		return ( Map<String, Object> ) runAndGetSomething( sql, rsh );
	}
	

	public List<Object[]> runAndGetArrayList(String sql) 
			throws SqlManagerDAOException 
	{
		ResultSetHandler<List<Object[]>> rsh = new ArrayListHandler( new BasicRowProcessorMod() );
		return ( List<Object[]> ) runAndGetSomething( sql, rsh );
	}
	

	public Object[] runAndGetArray(String sql) 
			throws SqlManagerDAOException 
	{
		ResultSetHandler<Object[]> rsh = new ArrayHandler( new BasicRowProcessorMod() );
		return ( Object[] ) runAndGetSomething( sql, rsh );
	}
	
	public Map<String, Map<String, Object>> runAndGetKeyedMap(String sql, String columnToBeKey) throws SqlManagerDAOException 
	{
		ResultSetHandler<Map<String, Map<String, Object>>> rsh = new KeyedHandler<String>( columnToBeKey );
		return ( Map<String, Map<String, Object>> ) runAndGetSomething( sql, rsh );
	}
	
	
	public Object runAndGetSomething(String sql, ResultSetHandler<? extends Object> rsh ) 
			throws SqlManagerDAOException 
	{
		Connection conn = this.getConnection();
		QueryRunner run = new QueryRunner();
		
		try {
			return run.query( conn, sql, rsh );
		} catch ( SQLException e ) {
			throw new SqlManagerDAOException( "Errore di accesso al database.", e );
		} finally {
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
