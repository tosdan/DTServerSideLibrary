package com.github.tosdan.utils.varie;

public class BoolUtils
{
	/**
	 * Effettua il <code>parse</code> su un oggetto per restituire un <code>boolean</code>. In caso di oggetto nullo o in caso di fallimento del parse restituisce <code>false</code>.
	 * @param obj oggetto da valutare
	 * @return
	 */
	public static <T> boolean toBoolean(T obj) {
		return toBoolean( obj, false );
	}
	
	/**
	 * Effettua il <code>parse</code> su un oggetto per restituire un <code>boolean</code>. In caso di oggetto nullo o in caso di fallimento del parse restituisce il valore booleano passato come parametro.
	 * @param obj oggetto da valutare
	 * @param defaultValue valore di ritorno di default: <code>true/false</code>
	 * @return
	 */
	public static <T> boolean toBoolean(T obj, boolean defaultValue) {
		boolean result = false;
		try {
			if ( obj != null && (obj instanceof String) )
				result = Boolean.valueOf( (String) obj );
			else if ( obj != null && obj instanceof Boolean )
				result = Boolean.valueOf( (Boolean) obj );
			else
				throw new IllegalArgumentException( "L'oggetto (non nullo) passato non e' istanza di String." );
		} catch ( Exception e ) {
			return defaultValue;
		}
		return result;
	}
	
}
