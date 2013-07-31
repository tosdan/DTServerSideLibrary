package com.github.tosdan.utils.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.ResultSetHandler;

/**
 * Abstract class that simplify development of <code>ResultSetHandler</code>
 * classes that convert <code>ResultSet</code> into <code>List</code>.
 *
 * @author Daniele
 * @param <T> the target List generic type
 * @see org.apache.commons.dbutils.ResultSetHandler
 */
public abstract class AbstractKeyedListHandler<K, V> implements ResultSetHandler<Map<K, List<V>>> {


    /**
     * Convert each row's columns into a Map and store then
     * in a <code>Map</code> under <code>ResultSet.getObject(key)</code> key.
     * @param rs <code>ResultSet</code> to process.
     * @return A <code>Map</code>, never <code>null</code>.
     * @throws SQLException if a database access error occurs
     * @see org.apache.commons.dbutils.ResultSetHandler#handle(java.sql.ResultSet)
     */
    @Override
    public Map<K, List<V>> handle(ResultSet rs) throws SQLException {
        List<V> tempRowsList;
        Map<K, List<V>> result = createMap();
        while (rs.next()) {
        	K key = createKey(rs);
        	tempRowsList = getRowsList(result, key);
        	tempRowsList.add(createRow(rs));
            result.put(key, tempRowsList);
        }
        return result;
    }
    
    /**
     * 
     * @param result
     * @param key
     * @return
     */
    private List<V> getRowsList(Map<K, List<V>> result, K key) {
    	List<V> rowsList = result.get( key );
    	if (rowsList == null)
    		rowsList = new ArrayList<V>();    	
    	return rowsList;
    }

    /**
     * This factory method is called by <code>handle()</code> to create the Map
     * to store records in.  This implementation returns a <code>HashMap</code>
     * instance.
     *
     * @return Map to store records in
     */
    protected Map<K, List<V>> createMap() {
        return new LinkedHashMap<K, List<V>>();
    }

    /**
     * This factory method is called by <code>handle()</code> to retrieve the
     * key value from the current <code>ResultSet</code> row.
     * @param rs ResultSet to create a key from
     * @return K from the configured key column name/index
     * @throws SQLException if a database access error occurs
     */
    protected abstract K createKey(ResultSet rs) throws SQLException;

    /**
     * This factory method is called by <code>handle()</code> to store the
     * current <code>ResultSet</code> row in some object.
     * @param rs ResultSet to create a row from
     * @return V object created from the current row
     * @throws SQLException if a database access error occurs
     */
    protected abstract V createRow(ResultSet rs) throws SQLException;

}
