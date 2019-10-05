package com.github.tosdan.utils.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ArrayHandler;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;


/**
 * 
 * @author Daniele
 * @version 0.0.1-b2013-09-13
 */
public class DAOGenericoImpl extends DAOGenericoAbstract {
	

	private RowProcessorFormatter formatter;



	/**
	 * 
	 * @param conn
	 */
	public DAOGenericoImpl(Connection conn) {
		this(conn, true);
	}
	
	/**
	 * 
	 * @param conn
	 * @param closeConn
	 */
	public DAOGenericoImpl(Connection conn,  boolean closeConn) {
		this(conn, closeConn, null);
	}
	
	/**
	 * 
	 * @param conn
	 * @param closeConn
	 * @param formatter
	 */
	public DAOGenericoImpl(Connection conn,  boolean closeConn, RowProcessorFormatter formatter) {
		this.conn = conn;
		this.formatter = formatter;
		setCloseConn(closeConn);
	}
	
	

	/**
	 * Estrae una lista di records.
	 * @param sql Stringa contenente la query sql da eseguire.
	 * @return Restituisce i record estratti sotto forma di lista di mappe <code>String</code> -> <code>Object</code>.
	 * @throws SQLException 
	 */
	@Override
	public List<Map<String, Object>> getMapList(String sql) throws SQLException {
		ResultSetHandler<List<Map<String, Object>>> rsh = new MapListHandler( new BasicRowProcessorMod(formatter) );
		return query( sql, rsh );
	}
	
	/**
	 * Estrae un solo record.
	 * @param sql Stringa contenente la query sql da eseguire.
	 * @return Restituisce il record estratto sotto forma di mappa <code>String</code> -> <code>Object</code>.
	 * @throws SQLException 
	 */
	@Override
	public Map<String, Object> getMap(String sql) throws SQLException {
		ResultSetHandler<Map<String, Object>> rsh = new MapHandler( new BasicRowProcessorMod(formatter) );
		return query( sql, rsh );
	}
	

	/**
	 * Estrae una lista di records.
	 * @param sql Stringa contenente la query sql da eseguire.
	 * @return Restituisce i records estratti sotto forma di lista di <code>Object[]</code> 
	 * @throws SQLException 
	 */
	@Override
	public List<Object[]> getArrayList(String sql) throws SQLException {
		ResultSetHandler<List<Object[]>> rsh = new ArrayListHandler( new BasicRowProcessorMod(formatter) );
		return query( sql, rsh );
	}
	

	/**
	 * Estrae un solo record.
	 * @param sql Stringa contenente la query sql da eseguire.
	 * @return Restituisce il record estratto sotto forma di <code>Object[]</code> 
	 * @throws SQLException 
	 */
	@Override
	public Object[] getArray(String sql) throws SQLException {
		ResultSetHandler<Object[]> rsh = new ArrayHandler( new BasicRowProcessorMod(formatter) );
		return query( sql, rsh );
	}
	
	/**
	 * Produce una mappa dei records estratti. <i>NB. Com'e' logico pensare, non ha senso per chiavi che non siano univoche.</i>
	 * @param sql Stringa contenente la query sql da eseguire.
	 * @param columnToBeKey Nome della colonna da usare come chiave per la mappa. Deve essere una chiave univoca nella tabella estratta.
	 * @return Restitiusce una mappa con chiave <code>columnToBeKey</code> e per valore il record associato a tale chiave, memorizzato sotto forma di mappa <code>String</code> -> <code>Object</code>.
	 * @throws SQLException 
	 */
	@Override
	public Map<String, Map<String, Object>> getKeyedMap(String sql, String columnToBeKey) throws SQLException {
		ResultSetHandler<Map<String, Map<String, Object>>> rsh = new MyKeyedHandler<String>( columnToBeKey );
		return query( sql, rsh );
	}
	
	

	/**
	 * Raggruppa i records estratti in liste. <i>Ovviamente non e' necessario che la chiave scelta per raggruppare i records sia univoca.</i>
	 * @param sql Stringa contenente la query sql da eseguire.
	 * @param columnToBeKey Nome della colonna da usare come chiave per la mappa.
	 * @return Restituisce una mappa raggruppata secondo la chiave <code>columnToBeKey</code>. Ad ogni chiave e'associata una lista, filtrata secondo la chiave scelta, contenente i records estratti, memorizzati come mappe <code>String</code> -> <code>Object</code>.
	 * @throws SQLException 
	 */
	@Override
	public Map<String, List<Map<String, Object>>> getKeyedMapList(String sql, String columnToBeKey) throws SQLException {
		ResultSetHandler<Map<String, List<Map<String, Object>>>> rsh = new KeyedMapListHandler<String>( columnToBeKey, formatter );
		return query( sql, rsh );
	}
	
}
