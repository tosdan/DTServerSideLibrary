package com.github.tosdan.utils.sql;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
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
	 * @throws QueriesUtilsException 
	 * @throws UnsupportedEncodingException 
	 */
	public static void main( String[] args ) throws QueriesUtilsException, UnsupportedEncodingException {
		testCompilaQueryDaFile();
	}

	
	/**
	 * 
	 * @param is InputStream che contiene la query
	 * @param nomeQuery nome della sezione che contiene la query 
	 * @param parametri mappa con i parametri da sostituire nella query
	 * @return
	 * @throws QueriesUtilsException 
	 */
	public static String compilaQueryDaInputStream(InputStream is, String nomeQuery, Map<String, Object> parametri)
			throws QueriesUtilsException {
		return compilaQueryDaInputStream( is, nomeQuery, parametri, null, null );
	}


	/**
	 * 
	 * @param is (opzionale se si passa direttamente un <code>File</code>) InputStream che contiene la query
	 * @param nomeQuery nome della sezione che contiene la query 
	 * @param parametri mappa con i parametri da sostituire nella query
	 * @param validator (opzionale) oggetto per validare, tramite verifiche dei tipi, i parametri passati per compilare la query
	 * @param nomeFile (opzionale se si passa direttamente un <code>InputStream</code>) nome file per lo storage delle queies
	 * @return
	 * @throws QueriesUtilsException
	 */
	public static String compilaQueryDaInputStream(InputStream is, String nomeQuery, Map<String, Object> parametri, MapFormatTypeValidator validator, String nomeFile) 
			throws QueriesUtilsException 
	{
		String contenutoFile = null;
		try {
			if (nomeFile != null)
				is = new FileInputStream( nomeFile );
			contenutoFile = IOfrw.leggiInputStream( is );
		} catch ( IOException e ) {
			String msg = (nomeFile == null) 
					? "Errore in lettura dalla sorgente." 
					: "Errore nel tentativo di lettura del file '"+ nomeFile+"'";
			throw new QueriesUtilsException( msg, e );
		}		
		if (contenutoFile == null || contenutoFile.isEmpty()) {
			String msg = (nomeFile == null) 
					? "Nessun contenuto valido per la query '" + nomeQuery + "' nella sorgente passata."
					: "Nessun contenuto valido per la query '" + nomeQuery + "' nel file queries repository: '" + nomeFile + "'.";			
			throw new QueriesUtilsException( msg );
		}
		
		String query = StrUtils.findSection( contenutoFile, nomeQuery );
		if ( query.equals("") ) {
			String msg = (nomeFile == null) 
					? "Associazione vuota o mancante per la query '" + nomeQuery + "' nella sorgente passata."
					: "Associazione vuota o mancante per la query '" + nomeQuery + "' nel file queries repository: '" + nomeFile + "'.";
			throw new QueriesUtilsException( msg );
		}
		
		return MapFormat.format( query, parametri, validator );
	}

	/**
	 * @deprecated
	 * @param prop oggetto property con le associazione tra i nomi delle queries e il file repository in cui reperirle
	 * @param queriesRepoFolderFullPath percorso completo in cui trovare il file repository delle queries
	 * @param nomeQuery nome della sezione che contiene la query 
	 * @param parametri mappa con i parametri da sostituire nella query
	 * @param validator (opzionale) oggetto per validare, tramite verifiche dei tipi, i parametri passati per compilare la query
	 * @return
	 * @throws QueriesUtilsException 
	 */
	public static String compilaQueryDaFileOld(Properties prop, String queriesRepoFolderFullPath, String nomeQuery, Map<String, Object> parametri, MapFormatTypeValidator validator) throws QueriesUtilsException
	{
		String nomeFile = prop.getProperty( nomeQuery );
		if (nomeFile == null || nomeFile.equals("") )
			throw new QueriesUtilsException( "Nessun file per lo store delle queries associato alla query '" + nomeQuery + "' nel file di configurazione della webapp." );
		String contenutoFile = null;
		try {
			contenutoFile = IOfrw.leggiFile( queriesRepoFolderFullPath + nomeFile );
		} catch ( IOException e ) {
			throw new QueriesUtilsException( "Errore di nel tentativo di lettura del file '"+queriesRepoFolderFullPath + nomeFile+"'", e );
		}
		if (contenutoFile == null || contenutoFile.isEmpty())
			throw new QueriesUtilsException( "Nessun contenuto valido per la query '" + nomeQuery + "' nel file '"+ queriesRepoFolderFullPath + nomeFile +"' per lo store delle queries.");
		String query = StrUtils.findSection( contenutoFile, nomeQuery );
		if ( query.equals("") )
			throw new QueriesUtilsException( "Associazione vuota o mancante per la query '" + nomeQuery + "' nel file per lo store delle queries: '" +queriesRepoFolderFullPath + nomeFile + "'." );
		
		return MapFormat.format( query, parametri, validator );
	}

	/**
	 * 
	 * @param prop oggetto property con le associazione tra i nomi delle queries e il file repository in cui reperirle
	 * @param queriesRepoFolderFullPath percorso completo in cui trovare il file repository delle queries
	 * @param nomeQuery nome della sezione che contiene la query 
	 * @param parametri mappa con i parametri da sostituire nella query
	 * @return
	 * @throws QueriesUtilsException 
	 */
	public static String compilaQueryDaFile(Properties prop, String queriesRepoFolderFullPath, String nomeQuery, Map<String, Object> parametri)
			throws QueriesUtilsException {
		return compilaQueryDaFile( prop, queriesRepoFolderFullPath, nomeQuery, parametri, null );
	}

	/**
	 * 
	 * @param prop oggetto property con le associazione tra i nomi delle queries e il file repository in cui reperirle
	 * @param queriesRepoFolderFullPath percorso completo in cui trovare il file repository delle queries
	 * @param nomeQuery nome della sezione che contiene la query 
	 * @param parametri mappa con i parametri da sostituire nella query
	 * @param validator (opzionale) oggetto per validare, tramite verifiche dei tipi, i parametri passati per compilare la query
	 * @return
	 * @throws QueriesUtilsException
	 */
	public static String compilaQueryDaFile(Properties prop, String queriesRepoFolderFullPath, String nomeQuery, Map<String, Object> parametri, MapFormatTypeValidator validator) 
			throws QueriesUtilsException
	{
		String nomeFile = prop.getProperty( nomeQuery );
		if (nomeFile == null || nomeFile.equals("") ) {
			throw new QueriesUtilsException( "Nessun file queries repository associato alla query '" + nomeQuery + "' nel file di configurazione della webapp." );
		}
		return compilaQueryDaInputStream( null, nomeQuery, parametri, validator, queriesRepoFolderFullPath + nomeFile ); 
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
	

	
	
	/* * * * * * * * * * * * * * * * * * * * * TEST * * * * * * * * * * * * * * * * * * * * * * */
	
	/**
	 * 
	 * @throws QueriesUtilsException 
	 * @throws UnsupportedEncodingException 
	 */
	public static void testCompilaQueryDaFile() throws QueriesUtilsException, UnsupportedEncodingException
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
