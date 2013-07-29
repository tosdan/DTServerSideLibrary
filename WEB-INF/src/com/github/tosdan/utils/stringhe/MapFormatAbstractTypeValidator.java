package com.github.tosdan.utils.stringhe;

import java.text.ParseException;
import java.text.DateFormat;

/**
 * 
 * @author Daniele
 * @version 0.2.0-b2013-07-29
 */
public abstract class MapFormatAbstractTypeValidator implements MapFormatTypeValidator
{
	private DateFormat dateFormat;
	
	
	protected String checkMatching( String regex, String source, String type ) throws MapFormatTypeValidatorException {
		if (source.matches(regex) ) {
			
			return source;
			
		} else {
			throw new MapFormatTypeValidatorException("Errore di validazione per il tipo '" + type +"' sul valore: " + source);
		}
	}
	
	
	protected String parseDate(String source) throws MapFormatTypeValidatorException {
		String result = "";
		try {
			
			dateFormat.setLenient( false ); // -> con false equivale a Strict Mode
			dateFormat.parse( source );
			
		} catch ( ParseException e ) {
			throw new MapFormatTypeValidatorException("Errore validazine data: " + source, e);
		}
		result = quote(source);
		
		return result;
	}

	
	protected <T extends DateFormat> void setDateFormat(T dateFormat) {
		this.dateFormat = dateFormat;
	}
	
	
	protected abstract String quote(String input);
}
