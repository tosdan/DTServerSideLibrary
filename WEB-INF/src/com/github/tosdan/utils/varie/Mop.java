package com.github.tosdan.utils.varie;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
/**
 * Mop assiste nella creazione di una mappa inizializzata con i valori passati e facilita l'aggiunta di elementi attraverso la possibilita' di concatenare i metodi di inserimento.
 * @author Daniele
 * @version 0.0.2-b2013-08-10
 * @param <T>
 */
public class Mop<T>
{
	public final static int LINKED = 0;
	public final static int HASH = 1;
	public final static int TREE = 2;
	
	private Map<String, T> map;
	
	/**
	 * Costruisce un Mop
	 */
	public Mop() {
		this( LINKED );
	}
	
	/**
	 * Costruisce un Mop
	 * @param tipo Implementazione della mappa.
	 */
	public Mop(int tipo) {
		initMap(tipo);
	}
	
	/**
	 * Costruisce un Mop: i valori passati nel costruttore sono direttamente inseriti nella mappa da assemblare.
	 * @param key chiave del primo elemento della mappa da assemblare
	 * @param val valore del primo elemento della mappa da assemblare
	 */
	public Mop(String key, T val) {
		this( key, val, LINKED );
	}
	
	/**
	 * Costruisce un Mop: i valori passati nel costruttore sono direttamente inseriti nella mappa da assemblare.
	 * @param key chiave del primo elemento della mappa da assemblare
	 * @param val valore del primo elemento della mappa da assemblare
	 * @param tipo Implementazione della mappa.
	 */
	public Mop(String key, T val, int tipo) {
		initMap(tipo);
		map.put( key, val );
	}
	
	/**
	 * Inizializza la mappa da assemblare.
	 * @param tipo Implementazione della mappa.
	 */
	private void initMap(int tipo) {
		switch ( tipo ) {
			case HASH:
				this.map = new HashMap<String, T>();
				break;
			
			case TREE:
				this.map = new TreeMap<String, T>();
				break;
			
			case LINKED:
			default:
				this.map = new LinkedHashMap<String, T>();
				break;
		}
	}
	
	/**
	 * Aggiunge una coppia chiave valore alla mappa da assemblare
	 * @param key chiave dell'elemento da aggiungere
	 * @param val valore dell'elemento da aggiungere
	 * @return Restituisce l'oggetto Mop su cui si e' eseguito il metodo put
	 */
	public Mop<T> put(String key, T val) {
		map.put( key, val );
		return this;
	}

	/**
	 * Terminatore degli inserimenti.
	 * @return Restituisce la mappa assemblata
	 */
	public Map<String, T> end() {
		return map;
	}
	
	/**
	 * Terminatore degli inserimenti.
	 * @param key chiave dell'ultimo elemento da aggiungere
	 * @param val valore dell'ultimo elemento da aggiungere
	 * @return Restituisce la mappa assemblata
	 */
	public Map<String, T> putEnd(String key, T val) {
		map.put( key, val );
		return map;
	}
	
}
