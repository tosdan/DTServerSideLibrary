package com.github.tosdan.dataTableServerSide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class DTReplyJsonProvider
{

	private Map<String, Object> reply;
	private List<Map<String, Object>> aaData;
	
	/**
	 * @param args
	 */
	public static void main( String[] args )
	{
		// TODO Auto-generated method stub
		DTReplyJsonProvider d = new DTReplyJsonProvider();
		d.addCustomParam("sEcho", "1");
		d.addCustomParam("iDisplayLength", "10");
		d.addCustomParam("iDisplayStart", "0");
		Map<String, Object> m = new HashMap<String, Object>();
		m.put( "ciao", "ciao" );
		d.addRecord( m );
		d.addRecord( m );
		d.addRecord( m );
		System.out.println( d );
	}
	
	public DTReplyJsonProvider()
	{
		aaData = new ArrayList<Map<String,Object>>();
		reply = new LinkedHashMap<String, Object>();
	}
	
	public String getJsonReply()
	{
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		reply.put( "aaData", aaData );
		String json = gson.toJson( reply );
		
		return json;
	}
	
	@Override
	public String toString()
	{
		return getJsonReply();
	}
	
	/**
	 * 
	 * @param e
	 */
	public void addRecord(Map<String, Object> e)
	{
		aaData.add( e );
	}
	
	/**
	 * 
	 * @param nomeParametro
	 * @param valoreParametro
	 */
	public void addCustomParam(String nomeParametro, String valoreParametro)
	{
		reply.put( nomeParametro, valoreParametro );
	}

	/**
	 * 
	 * @param iTotalRecords
	 */
	public void setiTotalRecords(String iTotalRecords) {
		reply.put( "iTotalRecords", iTotalRecords );
	}

	/**
	 * 
	 * @param iTotalDisplayRecords
	 */
	public void setiTotalDisplayRecords(String iTotalDisplayRecords) {
		reply.put( "iTotalDisplayRecords", iTotalDisplayRecords );
		
	}

	/**
	 * 
	 * @param sEcho
	 */
	public void setsEcho( String sEcho ) {
		reply.put( "sEcho", Integer.parseInt( sEcho ) );
		
	}
}
