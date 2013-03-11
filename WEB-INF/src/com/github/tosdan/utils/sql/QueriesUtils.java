package com.github.tosdan.utils.sql;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.github.tosdan.utils.io.IOfrw;
import com.github.tosdan.utils.stringhe.MapFormat;
import com.github.tosdan.utils.stringhe.MapFormatTypeValidator;
import com.github.tosdan.utils.stringhe.StrUtils;

public class QueriesUtils
{
	/**
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main( String[] args ) throws IOException {
		testCompilaQueryDaFile();
	}

	
	/**
	 * 
	 * @param is InputStream che contiene la query
	 * @param nomeQuery nome della sezione che contiene la query 
	 * @param parametri mappa con i parametri da sostituire nella query
	 * @return
	 * @throws IOException
	 */
	public static String compilaQueryDaInputStream(InputStream is, String nomeQuery, Map<String, Object> parametri)
			throws IOException
	{
		return compilaQueryDaInputStream( is, nomeQuery, parametri, null );
	}
	
	/**
	 * 
	 * @param is InputStream che contiene la query
	 * @param nomeQuery nome della sezione che contiene la query 
	 * @param parametri mappa con i parametri da sostituire nella query
	 * @return
	 * @throws IOException
	 */
	public static String compilaQueryDaInputStream(InputStream is, String nomeQuery, Map<String, Object> parametri, MapFormatTypeValidator validator)
			throws IOException
	{
		String contenutoFile = IOfrw.leggiInputStream( is );
		String query = StrUtils.findSection( contenutoFile, nomeQuery );
		
		return MapFormat.format( query, parametri, validator );
	}
	
	/**
	 * 
	 * @param nomeFile File che contiene la query
	 * @param nomeQuery nome della sezione che contiene la query 
	 * @param parametri mappa con i parametri da sostituire nella query
	 * @return
	 * @throws IOException
	 */
	public static String compilaQueryDaFile(String nomeFile, String nomeQuery, Map<String, Object> parametri)
			throws IOException
	{
		return compilaQueryDaFile( nomeFile, nomeQuery, parametri, null );
	}
	
	/**
	 * @deprecated
	 * 
	 * @param fileAbsPath File che contiene la query
	 * @param nomeQuery nome della sezione che contiene la query 
	 * @param parametri mappa con i parametri da sostituire nella query
	 * @return
	 * @throws IOException
	 */
	public static String compilaQueryDaFile(String fileAbsPath, String nomeQuery, Map<String, Object> parametri, MapFormatTypeValidator validator)
			throws IOException
	{
		String contenutoFile = IOfrw.leggiFile( fileAbsPath );
		String query = StrUtils.findSection( contenutoFile, nomeQuery );
		
		return MapFormat.format( query, parametri, validator );
	}
	

	/**
	 * 
	 * @param prop
	 * @param pathConfigFiles
	 * @param nomeQuery
	 * @param parametri
	 * @return
	 * @throws IOException
	 */
	public static String compilaQueryDaFile(Properties prop, String pathConfigFiles, String nomeQuery, Map<String, Object> parametri)
			throws IOException
	{
		return compilaQueryDaFile( prop, pathConfigFiles, nomeQuery, parametri, null );
	}

	/**
	 * 
	 * @param prop
	 * @param queriesRepoFolderFullPath
	 * @param nomeQuery
	 * @param parametri
	 * @param validator
	 * @return
	 * @throws IOException
	 */
	public static String compilaQueryDaFile(Properties prop, String queriesRepoFolderFullPath, String nomeQuery, Map<String, Object> parametri, MapFormatTypeValidator validator)
			throws IOException
	{
		String nomeFile = prop.getProperty( nomeQuery );
		String contenutoFile = IOfrw.leggiFile( queriesRepoFolderFullPath + nomeFile );
		String query = StrUtils.findSection( contenutoFile, nomeQuery );
		
		return MapFormat.format( query, parametri, validator );
	}
	
	/* * * * * * * * * * * * * * * * * * * * * TEST * * * * * * * * * * * * * * * * * * * * * * */
	
	/**
	 * 
	 * @throws IOException
	 */
	public static void testCompilaQueryDaFile() throws IOException
	{  	// Esempio di un file contenente piu' stringhe in diverse sezioni
		String esempio = 
			 	"%[sezione1]%\n" +
				"Lorem ipsum dolor sit amet, consectetur adipiscing elit. \n" +
				"Curabitur vel purus non ante scelerisque lacinia a ac lorem.\n" +
				"Nunc ante ante, tempus ornare cursus eget, adipiscing at odio.\n" +
				"Nam a tellus id sapien commodo sodales a non velit.\n" +
				
				"%[sezione2]%\n" +
				"Pellentesque rutrum mauris eget sapien porta dapibus.\n" +
				"Phasellus consectetur dui eget augue imperdiet consectetur.\n" +
				"Donec tellus massa, dapibus a faucibus et, tempor eget eros.\n" +
				"Donec iaculis condimentum porta. \n" +
				
				"%[sezione3]%\n" +
				"Nunc ${param2} vulputate turpis ${param3} feugiat.\n" +
				"Pellentesque ${param1} fringilla eleifend arcu id rutrum.\n" +
				"Proin blandit scelerisque tempus. Pellentesque nec tincidunt elit.\n" +
				"Nullam rutrum odio ac ante ${param1} interdum.\n" +
				"Fusce '${param2}' mauris sit amet metus vulputate fermentum sit ${param1} at nulla.";
		
		InputStream is = new ByteArrayInputStream( esempio.getBytes("ISO-8859-1") );
		
		Map<String, Object> mappa = new HashMap<String, Object>();
		mappa.put( "param1", "PARAM1" );
		mappa.put( "param2", "PARAM2" );
		mappa.put( "param3", "PARAM3" );
		String test = compilaQueryDaInputStream( is, "sezione3", mappa );
//		String test = compilaQueryDaFile( "d:/tmp/configfile.txt", "sezione3", mappa );
		System.out.println( "testCompilaQueryDaFile:\n" + test + "\n******* Linea di Debug *******\nCaratteri: " + test.length());
		
	}
	
}
