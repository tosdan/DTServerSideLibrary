package com.github.tosdan.dTReply;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;


import com.github.tosdan.utils.sql.DAOGenerico;
import com.github.tosdan.utils.sql.DAOGenericoImpl;
/**
 * 
 * @author Daniele
 * @version 0.0.1-b2013-09-14
 */
public class DTReplyDAO {
	
	private Connection conn;
	
	/**
	 * Parametri custom uguali per tutti i record. In ogni record verranno innestati i dati contenuti in questa mappa.
	 */
	private Map<String, String> customParamsMap;
	/**
	 * Mappa con i nomi delle colonne dei record estratti da DB. Valorizzata solo dopo aver usato il metodo getDTReply()
	 */
	private List<String> nomiColonne;
	
	private boolean indexedResults;

	/*********************/
	/**   Costruttori   **/
	/*********************/
	
	/**
	 * 
	 * @param provider
	 */
	public DTReplyDAO( Connection conn ) {
		this(conn, false);
	}
	
	/**
	 * 
	 * @param provider
	 * @param indexedResults
	 */
	public DTReplyDAO( Connection conn, boolean indexedResults ) {
		this(conn, indexedResults, null);
	}
	
	/**
	 * 
	 * @param provider
	 * @param indexedResults
	 * @param customParamsMap
	 */
	public DTReplyDAO( Connection conn, boolean indexedResults, Map<String, String> customParamsMap ) {
		this.conn = conn;
		this.indexedResults = indexedResults;
		this.customParamsMap = customParamsMap;
	}

	/***********************/
	/**   Metodi Getters  **/
	/***********************/
	
	/**
	 * 
	 * @param sqlDati
	 * @param sqlDatiExtra
	 * @return
	 * @throws SQLException 
	 * @throws DTReplyDAOExcetion
	 */
	public DTReply getDTReply(String sqlDati, String sqlDatiExtra) throws SQLException {
		DAOGenerico dao = new DAOGenericoImpl(conn, false);
		String 	iTotalRecords = null,
				iTotalDisplayRecords = null;
		List<Map<String, Object>> listaRecord = dao.getMapList(sqlDati);

		if ( sqlDatiExtra != null ) {
			Map<String, Object> results = dao.getMap(sqlDatiExtra);
			iTotalRecords = results.get("iTotalRecords").toString();
			iTotalDisplayRecords = results.get("iTotalDisplayRecords").toString();
		} else
			iTotalDisplayRecords = iTotalRecords = "" + listaRecord.size();
		
		DbUtils.closeQuietly(conn);
		
		DTReply reply = new DTReply(iTotalRecords, iTotalDisplayRecords);
		
		for (Map<String, Object> record : listaRecord) {
			if (nomiColonne == null)
				popolaNomiColonne(record);
			
			if (customParamsMap != null)
				record.putAll(customParamsMap);
			
			if (indexedResults)
				record = index(record);
			
			reply.addRecord(record);
		}
		
		return reply;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public List<String> getNomiColonne() {
		return nomiColonne;
	}
	

	/**********************/
	/**  Metodi Setters  **/
	/**********************/

	
	public void setIndexedResults( boolean indexedResults ) {
		this.indexedResults = indexedResults;
	}

	
	/**
	 * 
	 * @param customParamsMap
	 */
	public void setCustomParamsMap(Map<String, String> customParamsMap) {
		this.customParamsMap = customParamsMap;
	}
	
	
	/********************/
	/** Metodi Privati **/
	/********************/
	
	
	/**
	 * 
	 * @param record
	 */
	private void popolaNomiColonne( Map<String, Object> record ) {
		nomiColonne = new ArrayList<String>();
		Set<String> colonne = record.keySet();
		for( String col : colonne ) {
			if (!isToIgnore(col)) // parametro riservato di DataTable che non ha senso inserire tra i nomi delle colonne estratte
				nomiColonne.add(col);
		}
	}

	
	/**
	 * 
	 * @param s
	 * @return
	 */
	private boolean isToIgnore(String s) {
		boolean retVal = false;
		String[] toIgnore = new String[] {"DT_RowClass"};
		for( String sti : toIgnore ) {
			if (sti.equalsIgnoreCase(s)) {
				retVal = true;
				break;
			}
		}
		return retVal;
	}
	
	
	/**
	 * 
	 * @param record
	 * @return
	 */
	private Map<String, Object> index(Map<String, Object> record) {
		Map<String, Object> retVal = new LinkedHashMap<String, Object>();

		int counter = 0;
		for (String key : record.keySet()) {
			if ( !isToIgnore(key) )
				retVal.put(""+counter++, record.get(key));
		}
		
		return retVal;
	}
}
