package com.github.tosdan.dataTableServerSide;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.sql.DataSource;

import com.github.tosdan.dataTableServerSide.exceptions.DTReplyDAOExcetion;
import com.github.tosdan.utils.sql.ConnectionProvider;
import com.github.tosdan.utils.sql.ConnectionProviderException;
import com.github.tosdan.utils.sql.ConnectionProviderImpl;

/**
 * Classe helper per la gestione della parte server della generazione dinamica di DataTables.
 * Scambia messaggi via ajax con la DataTable in formato JSON: legge ed esegue il parse del
 * messaggio di request inviato dalla DataTable. Interpreta i parametri della richiesta e 
 * prepara un messaggio di risposta con i dati richiesti.
 * 
 * @author tosdan
 *
 */
public class DTReplyProvider
{

	public static void main(String[] args) throws DTReplyDAOExcetion 
	{
		Map<String, String> dummyParams = new HashMap<String, String>();
		dummyParams.put("iDisplayLength", "10");
		dummyParams.put("iDisplayStart", "0");
		dummyParams.put("sEcho", "1");
		
		ConnectionProvider connProv = new ConnectionProviderImpl( "net.sourceforge.jtds.jdbc.Driver", "jdbc:jtds:sqlserver://192.168.42.132:1331/test;", "sa", "daniele" );
		DTReplySqlProvider providerSql = new DTReplySqlProvider( dummyParams, "( SELECT * FROM foglio) AS tabella" );
		String providedSQL = providerSql.getSqlDatiQuery();
		providedSQL += "\n" + providerSql.getSqlDatiAggiuntivi();
		System.out.println( providedSQL );
		DTReplyProvider d = new DTReplyProvider(dummyParams , providerSql );
		
		String result;
		result = d.buildJsonPerDataTables(connProv);
		System.out.println(result);
	}

	/**
	 * Parametri personalizzati che devono essere inseriti cablati nel JSON di output per ogni record estratto dal DB
	 */
	private Map<String, String> parametriCustom;
	
	/**
	 * Parametri della request inviati dalla DataTable in fase di (ri)generazione della tabella.
	 */
	private Map<String, String> parametriRequest;
	
	/**
	 * Oggetto per memorizzare la risposta da fornire alla DataTable. Il toString di questo oggetto restituisce un output in formato JSON
	 */
	private DTReplyJsonProvider jsonOutput;

	/**
	 * Se true rende l'output indicizzato invece che associato ai nomi delle colonne. Serve per datatable senza associazione cablata (quindi senza mData) delle colonne, ovvero quando ogni colonna html e' associata alla omonima colonna del record estrtto dal DB.
	 * L'associazione quindi sara' dinamica, la prima colonna si prende il valore 0, la seconda il valore 1...
	 */
	private boolean indexedOutout = false;

	/**
	 * Lista ordinata con i nomi delle colonne estratte dalla query
	 */
	private ArrayList<String> listaColonne;
	
	private DTReplySqlProvider sqlProvider;

	
	/**
	 * 
	 * @param parametriRequest Mappa contenente le coppie [parametro -> valore] contenuti nella request ricevuta dalla pagina jsp che ha intercettato la richiesta dalla  DataTable.
	 * @param sqlProvider Oggetto che dopo il parse dei parametri della request assembla la query definitiva che dovra' esser eseguita
	 */
	public DTReplyProvider(Map<String, String> parametriRequest, DTReplySqlProvider sqlProvider )
	{
		this.jsonOutput = new DTReplyJsonProvider();
		this.parametriRequest = parametriRequest;
		this.sqlProvider = sqlProvider; // Chiave privilegiata DT_RowId : assegna come id dei tr della tabella il valore del record associato a questa colonna 
		this.parametriCustom = new HashMap<String, String>();
		this.parseParametriCustom();
	}
	
	/**
	 * @deprecated il dao sottostante non offre ancora il supporto al datasource
	 * @param datasource
	 * @return
	 * @throws SQLException
	 * @throws ConnectionProviderException
	 * @throws DTReplyDAOExcetion
	 */
	public String buildJsonPerDataTables(DataSource datasource) 
			throws DTReplyDAOExcetion  {
		
		return buildJsonPerDataTables( new DTReplyDAO( datasource ));	
	}
	
	/**
	 * 
	 * @param provider
	 * @return
	 * @throws SQLException
	 * @throws ConnectionProviderException
	 * @throws DTReplyDAOExcetion
	 */
	public String buildJsonPerDataTables(ConnectionProvider provider) 
			throws DTReplyDAOExcetion  {
		
		return buildJsonPerDataTables( new DTReplyDAO( provider ));	
	}
	
	/**
	 * 
	 * @param dao
	 * @return
	 * @throws SQLException
	 * @throws ConnectionProviderException
	 * @throws DTReplyDAOExcetion
	 */
	private String buildJsonPerDataTables(DTReplyDAO dao) 
			throws DTReplyDAOExcetion 
	{
		String sqlDati = sqlProvider.getSqlDatiQuery();
		String sqlDatiAggiuntivi = sqlProvider.getSqlDatiAggiuntivi();
		List<Map<String, Object>> records = dao.getRecordsList( sqlDati, sqlDatiAggiuntivi );
		String iTotalRecords = dao.getITotalRecords( sqlDati, sqlDatiAggiuntivi );
		String iTotalDisplayRecords = dao.getITotalDisplayRecords( sqlDati, sqlDatiAggiuntivi );
		
		/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		
		jsonOutput.setsEcho( this.parametriRequest.get("sEcho") ); // sembra obbligatorio: conteggia i messaggi scambiati tra client e server
		jsonOutput.setiTotalRecords( iTotalRecords ); // count di tutti i record esistenti 
		jsonOutput.setiTotalDisplayRecords( iTotalDisplayRecords ); // count dei filtrati con la ricerca 
		this.generaListaColonne( records.get(0) );
		for( Map<String, Object> record : records ) {
			
			if (indexedOutout)
				record = this.namedRecordToIndexedRecord( record );
			
			Set<Entry<String, String>> setParametriCustom = this.parametriCustom.entrySet(); // Ad ogni record estratto dal DB allega i parametri custom passati
			for( Entry<String, String> entry : setParametriCustom ) {
				record.put( entry.getKey(), entry.getValue() );
			}
			
			jsonOutput.addRecord(record);
		}
		jsonOutput.addCustomParam( "nomiColonne", this.getNomiColonne() );
		return jsonOutput.toString();
	}

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	private void generaListaColonne(Map<String, Object> map)
	{
		listaColonne = new ArrayList<String>();
		if (map == null)
			return;
		Set<Entry<String, Object>> mapEntrySet = map.entrySet();
		for( Entry<String, Object> entry : mapEntrySet ) {
			if ( !entry.getKey().equalsIgnoreCase( "DT_RowClass" ) )
				listaColonne.add( entry.getKey() );
		}
		
	}
	
	private String getNomiColonne()
	{
		String result = "";
		for ( String s : listaColonne ) {
			if ( !result.isEmpty() )
				result += ";";
			result += s;
			
		}
		return result;
	}
	
	public ArrayList<String> getListaColonne() {
		return listaColonne;
	}
	
	/**
	 * Output indicizzato invece che associato ai nomi delle colonne. Serve per datatable senza associazione cablata (quindi senza mData) delle colonne.
	 * L'associazione quindi sara' dinamica, la prima colonna si prende il valore 0, la seconda il valore 1...
	 * @param record
	 * @return
	 */
	private Map<String, Object> namedRecordToIndexedRecord(Map<String, Object> record)
	{
		int counter = 0; 
		Set<Entry<String, Object>> setRecord = record.entrySet();
		Map<String, Object> retVal = new HashMap<String, Object>();
		for( Entry<String, Object> entry : setRecord ) {
			Object val = entry.getValue();
			String numericKey = String.valueOf(counter);
			retVal.put( numericKey, val );
			counter++;
		}
		
		return retVal;
	}
	
	/**
	 * Rende l'output indicizzato invece che associato ai nomi delle colonne. Serve per datatable senza associazione cablata (quindi senza mData) delle colonne, ovvero quando ogni colonna html e' associata alla omonima colonna del record estrtto dal DB.
	 * L'associazione quindi sara' dinamica, la prima colonna si prende il valore 0, la seconda il valore 1...
	 */
	public void setIndexedOutout() {
		this.indexedOutout = true;
	}

	private void parseParametriCustom()
	{
		Set<Entry<String, String>> setParametriReq = this.parametriRequest.entrySet();
		for( Entry<String, String> entry : setParametriReq ) {	
			String nomeParam = entry.getKey();
			String valore = entry.getValue();
			
			if ( isParametroCustom(nomeParam) ) 	
				this.parametriCustom.put( nomeParam, valore );
		}
	}
	
	private boolean isParametroCustom(String nomeParametro)
	{
		if (parametriRequest.get("echoParams") == null)
			return false;
		
		String[] arrayEchoParams = this.parametriRequest.get( "echoParams" ).split( ";" );
		List<String> listaEchoParams = Arrays.asList( arrayEchoParams );
		
		for( String param : listaEchoParams ) {
			if ( nomeParametro.indexOf( param ) > -1 )
				return true;
		}

		return false;
	}

}
