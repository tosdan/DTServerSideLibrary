package com.github.tosdan.utils.varie;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.github.tosdan.utils.stringhe.StrUtils;

/**
 * 
 * @author Daniele
 * @version 0.0.4-b2013-08-30
 */
public class MapUtils
{
	/**
	 * Trasforma una mappa di liste di mappe in una mappa di oggetti. Ad ogni chiave della mappa deve esser associata una lista di mappe che costituiranno i dati da inserire nell'oggetto creato che dovra' sostituire la lista
	 * @param sourceMap
	 * @param type
	 * @return
	 */
	public static <T extends MapFeadable> Map<String, T> turnKeyedMapListIntoMapOfT(Map<String, List<Map<String, Object>>> sourceMap, Class<T> type) {
		
		Map<String, T> destMap = new LinkedHashMap<String, T>();
		
		String key;
		T tempInstanceOfT = null;
		List<Map<String, Object>> tempMapList;
		
		Set<Entry<String, List<Map<String, Object>>>> sourceMapEntrySet = sourceMap.entrySet();
		for( Entry<String, List<Map<String, Object>>> entry : sourceMapEntrySet ) {
			
			key = entry.getKey();
			
			tempMapList = entry.getValue();			
			try {
				
				tempInstanceOfT = type.newInstance();
				
			} catch ( InstantiationException e ) {
//				e.printStackTrace();
				throw new UnsupportedOperationException( e.getMessage(), e );
			} catch ( IllegalAccessException e ) {
//				e.printStackTrace();
				throw new TypeNotPresentException( e.getMessage(), e );
			}
			
			
			for( Map<String, Object> map : tempMapList ) {
				tempInstanceOfT.addRecordAsMap( map );
			}
			
			destMap.put( key, tempInstanceOfT );
		}
		
		return destMap;
	}
	

	/**
	 * Fonde due o piu' mappe.
	 * @param maps Elenco di mappe separate da virgole
	 * @return
	 */
	public static <T extends Object> Map<String, T> mergeMaps(Map<String, T>... maps) {
		Map<String, T> retVal = new HashMap<String, T>();
		for( int i = 0 ; i < maps.length ; i++ ) {
			if (maps[i] != null)
				retVal.putAll( maps[i] );
		}
		return retVal;
	}

	/**
	 * Trasforma una Mappa di Mappe in una Lista di Mappe ordinata.
	 * @param keyedMap mappa da trasformare
	 * @return
	 */
	public static <T extends Object> List<Map<String, T>> keyedMapToMapList(Map<String, Map<String, T>> keyedMap) {
		return keyedMapToMapList(keyedMap, null, false);
	}

	/**
	 * Trasforma una Mappa di Mappe in una Lista di Mappe ordinata.
	 * @param keyedMap mappa da trasformare
	 * @param orderKey chiave secondo la quale ordinare la lista
	 * @return
	 */
	public static <T extends Object> List<Map<String, T>> keyedMapToMapList(Map<String, Map<String, T>> keyedMap, final String orderKey) {
		return keyedMapToMapList(keyedMap, orderKey, false);
	}
	
	/**
	 * Trasforma una Mappa di Mappe in una Lista di Mappe ordinata.
	 * @param keyedMap mappa da trasformare
	 * @param orderKey chiave secondo la quale ordinare la lista
	 * @param reverse flag per invertire l'ordine da crescente a decrescente
	 * @return
	 */
	public static <T extends Object> List<Map<String, T>> keyedMapToMapList(Map<String, Map<String, T>> keyedMap, final String orderKey, final boolean reverse) {
		List<Map<String, T>> retVal = new ArrayList<Map<String,T>>();
		
		for( Entry<String, Map<String, T>> aziEntry : keyedMap.entrySet() )
			retVal.add(aziEntry.getValue());
		
		if (orderKey != null && !orderKey.isEmpty())
			sortMapList(retVal, orderKey, reverse);
		
		return retVal;
	}

	/**
	 * Ordina una lista di mappe.
	 * @param mapList mappa da riordinare
	 * @param orderKey chiave secondo la quale ordinare la lista
	 * @return
	 */
	public static <T extends Object> void sortMapList(List<Map<String, T>> mapList, final String orderKey) {
		sortMapList(mapList, orderKey, false);
	}
	
	/**
	 * Ordina una lista di mappe.
	 * @param mapList mappa da riordinare
	 * @param orderKey chiave secondo la quale ordinare la lista
	 * @param reverse flag per invertire l'ordine da crescente a decrescente
	 * @return
	 */
	public static <T extends Object> void sortMapList(List<Map<String, T>> mapList, final String orderKey, final Boolean reverse) {
			Collections.sort(mapList, new Comparator<Map<String, T>>() {
				@Override public int compare( Map<String, T> o1, Map<String, T> o2 ) {
					String s1 = StrUtils.safeToStr(o1.get(orderKey));
					String s2 = StrUtils.safeToStr(o2.get(orderKey));
					if (reverse != null && reverse)
						return s2.compareTo(s1);
					else 
						return s1.compareTo(s2);				
				}
			});
	}
	
	/**
	 * Cerca all'interno di una lista di mappe una mappa che contenga la coppia chiave/valore passati.
	 * @param mapList lista di mappe in cui effettuare la ricerca
	 * @param searchKey chiave del valore da cercare
	 * @param searchValue valore cercato
	 * @return
	 */
	public static <T extends Object> Map<String, T> findMap(List<Map<String, T>> mapList, String searchKey, String searchValue) {
		for(Map<String, T> map : mapList) {
			if (map.get(searchKey).equals(searchValue))
				return map;
		}
		return null;
	}
	
	/**
	 * Cerca all'interno di una lista di mappe una mappa che contenga il valore passato, indipendentemente da quale chiave lo contenga.
	 * @param mapList lista di mappe in cui effettuare la ricerca
	 * @param searchValue valore cercato
	 * @return
	 */
	public static <T extends Object> Map<String, T> findMap(List<Map<String, T>> mapList, String searchValue) {
		for(Map<String, T> map : mapList) {
			for( Entry<String, T> mapEntrySet : map.entrySet()) {
				if (mapEntrySet.getValue().equals(searchValue))
					return map;
			}
		}
		return null;
	}
}
