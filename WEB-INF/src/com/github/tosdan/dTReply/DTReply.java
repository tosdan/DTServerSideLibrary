package com.github.tosdan.dTReply;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.github.tosdan.utils.varie.YamlUtils;
/**
 * 
 * @author Daniele
 * @version 0.0.2-b2013-09-14
 */
public class DTReply {

	private Map<String, Object> reply;
	private List<Map<String, Object>> aaData;


	public DTReply() {
		this(null, null);
	}
	
	public DTReply(String iTotalRecords, String iTotalDisplayRecords) {
		aaData = new ArrayList<Map<String, Object>>();
		reply = new LinkedHashMap<String, Object>();
		reply.put("aaData", aaData);
		
		if (iTotalRecords != null)
			setITotalRecords(iTotalRecords);
		if (iTotalDisplayRecords != null)
			setITotalDisplayRecords(iTotalDisplayRecords);
	}
	
	public Map<String, Object> getReplyMap() {
		return reply;
	}

	/**
	 * 
	 * @param record
	 */
	public void addRecord( Map<String, Object> record ) {
		aaData.add(record);
	}

	/**
	 * 
	 * @param nomeParametro
	 * @param valoreParametro
	 */
	public void addCustomParam( String nomeParametro, String valoreParametro ) {
		reply.put(nomeParametro, valoreParametro);
	}

	/***	SETTER	***/
	
	/**
	 * 
	 * @param iTotalRecords
	 */
	public void setITotalRecords( String iTotalRecords ) {
		setITotalRecords(Integer.valueOf(iTotalRecords));
	}
	
	/**
	 * 
	 * @param iTotalRecords
	 */
	public void setITotalRecords( Integer iTotalRecords ) {
		reply.put("iTotalRecords", iTotalRecords);
	}
	
	/**
	 * 
	 * @param iTotalDisplayRecords
	 */
	public void setITotalDisplayRecords( String iTotalDisplayRecords ) {
		setITotalDisplayRecords(Integer.valueOf(iTotalDisplayRecords));
	}
	
	/**
	 * 
	 * @param iTotalDisplayRecords
	 */
	public void setITotalDisplayRecords( Integer iTotalDisplayRecords ) {
		reply.put("iTotalDisplayRecords", iTotalDisplayRecords);// Parametro che indica la conta dei record rimasti dopo la scrematura della ricerca. 
	}

	/**
	 * 
	 * @param sEcho
	 */
	public void setSEcho( String sEcho ) {
		reply.put("sEcho", Integer.parseInt(sEcho));

	}

	@Override
	public String toString() {
		return this.getClass().getName() +"\n"+ YamlUtils.toYamlBlock(getReplyMap());
	}

	
	/*********		TEST		*********/
	
	public static void main( String[] args ) {
		// TODO Auto-generated method stub
		DTReply d = new DTReply();
		d.addCustomParam("sEcho", "1");
		d.addCustomParam("iDisplayLength", "10");
		d.addCustomParam("iDisplayStart", "0");
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("ciao", "ciao");
		d.addRecord(m);
		d.addRecord(m);
		d.addRecord(m);
		System.out.println(d);
	}
}
