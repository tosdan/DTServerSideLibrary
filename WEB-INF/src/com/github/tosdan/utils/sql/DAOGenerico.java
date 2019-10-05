package com.github.tosdan.utils.sql;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author Daniele
 * @version 0.0.1-b2013-09-13
 */
public interface DAOGenerico {

	void setCloseConn( boolean closeConn );

	int update( String sql ) throws SQLException;

	List<Map<String, Object>> getMapList( String sql ) throws SQLException;

	Map<String, Object> getMap( String sql ) throws SQLException;

	List<Object[]> getArrayList( String sql ) throws SQLException;

	Object[] getArray( String sql ) throws SQLException;

	Map<String, Map<String, Object>> getKeyedMap( String sql, String columnToBeKey ) throws SQLException;

	Map<String, List<Map<String, Object>>> getKeyedMapList( String sql, String columnToBeKey ) throws SQLException;

	void closeQuietly();

	void close() throws SQLException;

}
