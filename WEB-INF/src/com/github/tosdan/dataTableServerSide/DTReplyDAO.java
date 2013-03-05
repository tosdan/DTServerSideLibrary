package com.github.tosdan.dataTableServerSide;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;

import com.github.tosdan.dataTableServerSide.exceptions.DTReplyDAOExcetion;
import com.github.tosdan.utils.sql.BasicRowProcessorMod;
import com.github.tosdan.utils.sql.ConnectionProvider;
import com.github.tosdan.utils.sql.ConnectionProviderException;

public class DTReplyDAO
{
	private DataSource datasource;
	private ConnectionProvider provider;
	private Connection conn;
	
	private List<Map<String, Object>> listaRecord;
	private Map<String, Object> mappaConteggioRecords;

	/**
	 * 
	 * @param provider
	 */
	public DTReplyDAO(ConnectionProvider provider) {
		this( null, provider );
	}
	
	/**
	 * 
	 * @param dataSource
	 */
	public DTReplyDAO( DataSource dataSource ) {
		this( dataSource, null );
	}
	
	/**
	 * 
	 * @param dataSource
	 * @param provider
	 */
	private DTReplyDAO( DataSource dataSource, ConnectionProvider provider ) {
		this.provider = provider;
		this.datasource = dataSource;
	}
	
	/* * * * * * * * * * * * * GETTERS * * * * * * * * * * * * * * * * */
	
	/**
	 * Recupera i dati dal DB e li memorizza in una lista ordinata di record. Ogni record viene insreito in una sua propria mappa.
	 * @param sqlDati
	 * @param sqlDatiAggiuntivi
	 * @return Lista ordinata di mappe. Ogni mappa è una coppia [ colonna -> valore ] con tutti i dati del record estratto.
	 * @throws DTReplyDAOExcetion
	 */
	public List<Map<String, Object>> getRecordsList( String sqlDati, String sqlDatiAggiuntivi )
			throws DTReplyDAOExcetion 
	{
		if ( listaRecord == null )
			retrieveData( sqlDati, sqlDatiAggiuntivi );
		
		return listaRecord;
	}
	
	/**
	 * 
	 * @param sqlDati
	 * @param sqlDatiAggiuntivi
	 * @return
	 * @throws DTReplyDAOExcetion
	 */
	public String getITotalRecords( String sqlDati, String sqlDatiAggiuntivi )	
			throws DTReplyDAOExcetion
	{
		if (this.mappaConteggioRecords == null || this.mappaConteggioRecords.isEmpty() )
			this.retrieveData( sqlDati, sqlDatiAggiuntivi );
		
		return this.mappaConteggioRecords.get( "iTotalRecords" ).toString();
	}
	
	/**
	 * 
	 * @param sqlDati
	 * @param sqlDatiAggiuntivi
	 * @return
	 * @throws DTReplyDAOExcetion
	 */
	public String getITotalDisplayRecords( String sqlDati, String sqlDatiAggiuntivi ) 
			throws DTReplyDAOExcetion
	{
		if (this.mappaConteggioRecords == null || this.mappaConteggioRecords.isEmpty() )
			this.retrieveData( sqlDati, sqlDatiAggiuntivi );
		
		return this.mappaConteggioRecords.get( "iTotalDisplayRecords" ).toString();
	}

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	/**
	 * 
	 * @return
	 * @throws DTReplyDAOExcetion
	 */
	private Connection getConnection() throws DTReplyDAOExcetion
	{
		if (this.conn == null) {
			
			if (this.datasource != null) {
				
				try {
					return this.datasource.getConnection();
				} catch ( SQLException e ) {
					throw new DTReplyDAOExcetion( "Errore durante la connessione al database.", e );
				}
				
			} else if (this.provider != null) {
				
				try {
					return provider.stabilisciConnessione();
				} catch ( ConnectionProviderException e ) {
					throw new DTReplyDAOExcetion( "Errore durante la connessione al database.", e );
				}
				
			} else {
				throw new DTReplyDAOExcetion("Datasource e ConnectionProvider sono stati entrambi forniti nulli.");
			}
		}

		return conn;
	}
	
	/**
	 * 
	 * @param sqlDati
	 * @param sqlDatiAggiuntivi
	 * @throws DTReplyDAOExcetion
	 */
	private void retrieveData( String sqlDati, String sqlDatiAggiuntivi ) 
			throws DTReplyDAOExcetion 
	{
		Connection conn = this.getConnection();
		QueryRunner run = new QueryRunner();		
		ResultSetHandler<List<Map<String, Object>>> rshDati = new MapListHandler( new BasicRowProcessorMod() );
		ResultSetHandler<Map<String, Object>> rshDatiAggiuntivi = new MapHandler();
		
		try {
			this.listaRecord = run.query( conn, sqlDati, rshDati );
			
			if ( sqlDatiAggiuntivi != null )
				this.mappaConteggioRecords = run.query( conn, sqlDatiAggiuntivi, rshDatiAggiuntivi );
			
		} catch ( SQLException e1 ) {
			throw new DTReplyDAOExcetion( "Errore di accesso al database.", e1 );
		} finally {
			DbUtils.closeQuietly( conn );
		}
		
	}

}
