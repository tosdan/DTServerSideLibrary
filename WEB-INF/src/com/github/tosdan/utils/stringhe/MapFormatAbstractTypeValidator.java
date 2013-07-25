package com.github.tosdan.utils.stringhe;
/**
 * 
 * @author Daniele
 * @version 0.1.0-b2013-07-25
 */
public abstract class MapFormatAbstractTypeValidator implements MapFormatTypeValidator
{

	/**
	 * @return Elenco di tipi separati dala carattere pipe "|"
	 */
	@Override
	public String getTypes() {
		String[] types = getSupportedTypes();
		String result = "";
		for( int i = 0 ; i < types.length ; i++ ) {
			result += result.isEmpty() ? "" : "|";
			result += types[i];
		}
		return result;
	}
	
}
