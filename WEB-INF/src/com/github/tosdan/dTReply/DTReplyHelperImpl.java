package com.github.tosdan.dTReply;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.github.tosdan.utils.sql.ConnectionProvider;
import com.github.tosdan.utils.sql.ConnectionProviderException;
import com.github.tosdan.utils.sql.ConnectionProviderImplV2;
import com.github.tosdan.utils.varie.BoolUtils;
import com.github.tosdan.utils.varie.JsonUtils;

/**
 * Classe helper per la gestione della parte server della generazione dinamica di DataTables.
 * Scambia messaggi via ajax con la DataTable in formato JSON: legge ed esegue il parse del
 * messaggio di request inviato dalla DataTable. Interpreta i parametri della richiesta e 
 * prepara un messaggio di risposta con i dati richiesti.
 * 
 * @author Daniele
 * @version 0.0.3-b2013-09-14
 */
public class DTReplyHelperImpl implements DTReplyHelper {
	/**
	 * Parametri della request inviati dalla DataTable in fase di (ri)generazione della tabella.
	 */
	private Map<String, String> reqParams;
	
	/**
	 * Se true rende l'output indicizzato invece che associato ai nomi delle colonne. Serve per datatable senza associazione cablata (quindi senza mData) delle colonne, ovvero quando ogni colonna html e' associata alla omonima colonna del record estrtto dal DB.
	 * L'associazione quindi sara' dinamica, la prima colonna si prende il valore 0, la seconda il valore 1...
	 */
	private boolean indexedResults;
	
	/**
	 * Se true delega  al DB le operazioni di filtro ricerca, ordinamento e paginazione.
	 */
	private boolean serverSideProcessing;
	
	private String 	sqlDati,
					sqlDatiExtra;

	/**
	 * 
	 * @param parametriRequest Mappa contenente le coppie [parametro -> valore] contenuti nella request ricevuta dalla pagina jsp che ha intercettato la richiesta dalla  DataTable.
	 * @param sqlProvider Oggetto che dopo il parse dei parametri della request assembla la query definitiva che dovra' esser eseguita
	 * @param serverSideProcessing Flag per stabilire se ricerca, ordinamento e paginazione debbano esser gestite via DB o meno.
	 */
	public DTReplyHelperImpl(Map<String, String> parametriRequest, String querySql, boolean serverSideProcessing ) {
		this(parametriRequest, querySql, (serverSideProcessing) ? (new DTReplySqlProvider(parametriRequest, querySql)) : (null));
	}
	
	/**
	 * 
	 * @param reqParams Mappa contenente le coppie [parametro -> valore] contenuti nella request ricevuta dalla pagina jsp che ha intercettato la richiesta dalla  DataTable.
	 * @param sqlProvider Oggetto che dopo il parse dei parametri della request assembla la query definitiva che dovra' esser eseguita
	 */
	public DTReplyHelperImpl(Map<String, String> reqParams, String querySql, DTReplySqlProvider dtReplySqlProvider ) {
		this.reqParams = reqParams;
		
		if (dtReplySqlProvider != null) {
			sqlDati = dtReplySqlProvider.getSqlDatiQuery();
			sqlDatiExtra = dtReplySqlProvider.getSqlDatiExtra();
		} else 
			sqlDati = querySql;
		
		serverSideProcessing = dtReplySqlProvider != null;
		indexedResults = !BoolUtils.toBoolean(reqParams.get("mDataBinded")) || "0".equals(StringUtils.defaultString(reqParams.get("mDataProp_0")));
	}
	

	@Override
	public String getSqlDati() {
		return sqlDati;
	}

	@Override
	public String getSqlDatiExtra() {
		return sqlDatiExtra;
	}
	
	/**
	 * 
	 * @param conn 
	 * @return
	 * @throws SQLException
	 * @throws ConnectionProviderException
	 * @throws DTReplyDAOExcetion
	 */
	@Override
	public Map<String, Object> getReplyMap(Connection conn) throws SQLException {
		DTReplyDAO dao = new DTReplyDAO(conn, indexedResults, getParametriCustom());
		DTReply reply = dao.getDTReply(getSqlDati(), getSqlDatiExtra());
		
		if (serverSideProcessing)
			reply.setSEcho(reqParams.get("sEcho"));
		
		reply.addCustomParam( "nomiColonne", StringUtils.join(dao.getNomiColonne(), ";"));
		
		return reply.getReplyMap();
	}

	
	/**
	 * 
	 * @return
	 */
	private Map<String, String> getParametriCustom() {
		Map<String, String> parametriCustom = new HashMap<String, String>();
		for (String nomeParam : reqParams.keySet()) {	
			if ( isParametroCustom(nomeParam) ) 	
				parametriCustom.put( nomeParam, reqParams.get(nomeParam) );
		}
		return parametriCustom;
	}
	
	/**
	 * 
	 * @param nomeParametro
	 * @return
	 */
	private boolean isParametroCustom(String nomeParametro) {
		String echoParams = StringUtils.defaultString(reqParams.get("echoParams"));
		return echoParams.contains(nomeParametro);
	}

	/** Main di test **/

	public static void main(String[] args) throws ConnectionProviderException, SQLException 
	{
		Map<String, String> dummyParams = new HashMap<String, String>();
		dummyParams.put("iDisplayLength", "10");
		dummyParams.put("iDisplayStart", "0");
		dummyParams.put("sEcho", "1");
		
		ConnectionProvider connProv = new ConnectionProviderImplV2( "net.sourceforge.jtds.jdbc.Driver", "jdbc:jtds:sqlserver://192.168.42.132:1331/test;", "sa", "daniele" );
		DTReplySqlProvider providerSql = new DTReplySqlProvider( dummyParams, "( SELECT * FROM foglio) AS tabella" );
		String providedSQL = providerSql.getSqlDatiQuery();
		providedSQL += "\n" + providerSql.getSqlDatiExtra();
		System.out.println( providedSQL );
		DTReplyHelperImpl d = new DTReplyHelperImpl(dummyParams , "( SELECT * FROM foglio) AS tabella", true );
		
		String result;
		result = JsonUtils.toJSON( d.getReplyMap(connProv.stabilisciConnessione()));
		System.out.println(result);
	}

}
