package com.github.tosdan.utils.sql.tests;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.github.tosdan.utils.sql.ConnectionProvider;
import com.github.tosdan.utils.sql.ConnectionProviderImpl;
import com.github.tosdan.utils.sql.BasicDAO;
import com.github.tosdan.utils.sql.BasicDAOException;

public class SqlManagerDAOTest
{

	/**
	 * @param args
	 * @throws BasicDAOException 
	 */
	public static void main( String[] args ) throws BasicDAOException
	{
		// TODO Auto-generated method stub
		ConnectionProvider provider = new ConnectionProviderImpl( "net.sourceforge.jtds.jdbc.Driver", "jdbc:jtds:sqlserver://192.168.42.132:1331/test;", "sa", "daniele" );
		BasicDAO dao = new BasicDAO( provider );
		String sql = "SELECT * FROM foglio";

//		testRunAndGetArray( dao, sql );
//		testRunAndGetArrayList( dao, sql );
//		testRunAndGetMap( dao, sql );
//		testRunAndGetMapList( dao, sql );
		testRunAndGetKeyedMap( dao, sql );
	}

	public static void testRunAndGetArray( BasicDAO dao, String sql ) throws BasicDAOException
	{
		System.out.println("------------------");
		Object[] result = dao.runAndGetArray( sql );
		for( int i = 0 ; i < result.length ; i++ ) {
			System.out.print( result[i] + " \t ");
		}
		System.out.println("\n------------------");
	}
	
	public static void testRunAndGetArrayList( BasicDAO dao, String sql ) throws BasicDAOException
	{
		System.out.println("------------------");
		List<Object[]> result = dao.runAndGetArrayList( sql );
		for( Object[] objects : result ) {
			for( int i = 0 ; i < objects.length ; i++ ) {
				System.out.print( objects[i] + "\t ");
			}
			System.out.println();
		}
		System.out.println("------------------");
	}
	
	public static void testRunAndGetMap( BasicDAO dao, String sql ) throws BasicDAOException
	{
		System.out.println("------------------");
		Map<String, Object> result = dao.runAndGetMap( sql );
		Set<Entry<String, Object>> entrySet = result.entrySet();
		for( Entry<String, Object> entry : entrySet ) {
			System.out.print( entry.getKey() + ": " + entry.getValue() +"\t");
		}
		System.out.println("\n------------------");
	}
	
	public static void testRunAndGetMapList( BasicDAO dao, String sql ) throws BasicDAOException
	{
		System.out.println("------------------");
		List<Map<String, Object>> result = dao.runAndGetMapList( sql );
		for( Map<String, Object> map : result ) {
			Set<Entry<String, Object>> entrySet = map.entrySet();
			for( Entry<String, Object> entry : entrySet ) {
				System.out.print( entry.getKey() + ": "+entry.getValue() +"\t");
			}
			System.out.println();
		}
		System.out.println("------------------");
	}
	
	public static void testRunAndGetKeyedMap( BasicDAO dao, String sql ) throws BasicDAOException
	{
		System.out.println("------------------");
		Map<String, Map<String, Object>> result = dao.runAndGetKeyedMap( sql, "browser" );
		Set<Entry<String, Map<String, Object>>> outerEntrySet = result.entrySet();
		for( Entry<String, Map<String, Object>> entry : outerEntrySet ) {
			String chiave = entry.getKey();
			Map<String, Object> record = entry.getValue();
			System.out.print( String.format( "%-46s", "record con chiave: " + chiave+"->"));
			Set<Entry<String, Object>> innerEntrySet = record.entrySet();
			for( Entry<String, Object> entry2 : innerEntrySet ) {
				String nomeColonna = entry2.getKey();
				String valore = ( String ) entry2.getValue();
				System.out.print( String.format( "%-34s", nomeColonna +": "+valore) );
				
			}
			System.out.println();
		}
		System.out.println("------------------");
	}
	
}
