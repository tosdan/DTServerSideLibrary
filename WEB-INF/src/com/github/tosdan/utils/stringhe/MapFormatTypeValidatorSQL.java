package com.github.tosdan.utils.stringhe;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
/**
 * Validator per T-SQL
 * @author Daniele
 *
 */
public class MapFormatTypeValidatorSQL implements MapFormatTypeValidator
{
	public static void main( String[] args ) throws MapFormatTypeValidatorException
	{
		MapFormatTypeValidatorSQL v = new MapFormatTypeValidatorSQL();
		System.out.println( v.validate( "test del 'piffero'", "stringa" ) );
		System.out.println( v.validate( "01012012", "compactDate" ) );
		System.out.println( v.validate( "tRUe", "boolean" ) );
		System.out.println( v.validate( "123", "integer" ) );
		System.out.println( v.validate( "123.4", "numeric" ) );
		System.out.println( v.validate( "1234", "numeric" ) );
	}
	
	private DateFormat dateFormat;

	/**
	 * @return Elenco di tipi separati da pipe "|"
	 */
	@Override
	public String getTypes() {
		return "string|stringa|integer|numeric|boolean|compactDate|revCompDate|itaDate|revItaDate|date|reverseDate|table|tablePart|free";
	}

	/**
	 * Effettua un opportuno controllo sul valore passato in base al tipo di dato e restituisce il valore tale com'era oppure opportunamente formattato 
	 * @param source
	 * @param type
	 * @return
	 * @throws MapFormatTypeValidatorException 
	 */
	@Override
	public String validate(String source, String type) throws MapFormatTypeValidatorException {
		String result = "";

        if ( type != null ) {
        	if 		( type.equalsIgnoreCase("stringa" ) ) {
        		result = quote(source);
        	}
        	else if ( type.equalsIgnoreCase("string" ) ) {
        		result = quote(source);
        	}
        	else if ( type.equalsIgnoreCase("boolean") ) {
        		result = this.checkMatching( "(?i)true|false", source, type );
        	}
        	else if ( type.equalsIgnoreCase("integer") ) {
        		result = this.checkMatching( "[0-9]+", source, type );
        	}
        	else if ( type.equalsIgnoreCase("numeric") ) {
        		result = this.checkMatching( "([0-9])+(.([0-9])+)?", source, type );
        	}
        	else if ( type.equalsIgnoreCase("compactDate") ) {
        		this.dateFormat = new SimpleDateFormat( "ddMMyyyy" );        	
        		result = parseDate(source);
        	}
        	else if ( type.equalsIgnoreCase("revCompDate") ) {
        		this.dateFormat = new SimpleDateFormat( "yyyyMMdd" );        	
        		result = parseDate(source);
        	}
        	else if ( type.equalsIgnoreCase("itaDate") ) {
        		this.dateFormat = new SimpleDateFormat( "dd/MM/yyyy" );        	
        		result = parseDate(source);        		
        	}
        	else if ( type.equalsIgnoreCase("revItaDate") ) {
        		this.dateFormat = new SimpleDateFormat( "yyyy/MM/dd" );        	
        		result = parseDate(source);        		
        	}
        	else if ( type.equalsIgnoreCase("date") ) {
        		this.dateFormat = new SimpleDateFormat( "dd-MM-yyyy" );        	
        		result = parseDate(source);        		
        	}
        	else if ( type.equalsIgnoreCase("reverseDate") ) {
        		this.dateFormat = new SimpleDateFormat( "yyyy-MM-dd" );        	
        		result = parseDate(source);        		
        	}
        	else if ( type.equalsIgnoreCase("table") ) {
        		result = this.checkMatching( "[a-zA-Z_@#][a-zA-Z0-9_@#$]*", source, type );   
        	}
        	else if ( type.equalsIgnoreCase("tablePart") ) {
        		result = this.checkMatching( "[a-zA-Z0-9_@#]+", source, type );   
        	}
        	else if ( type.equalsIgnoreCase("free") ) {
        		result = source;   
        	} else
        		throw new MapFormatTypeValidatorException("Errore di validazione: il tipo di validazione "+ type +" e' sconosciuto.");
        	
        } else {
        	throw new MapFormatTypeValidatorException( "Nella stringa passata c'e' almeuno un parametro type mancante." );
        }
		
		return result;
	}
	
	private String parseDate(String source) throws MapFormatTypeValidatorException {
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
	
	private String checkMatching( String regex, String source, String type ) throws MapFormatTypeValidatorException {
		if (source.matches(regex) ) {
			
			return source;
			
		} else {
			throw new MapFormatTypeValidatorException("Errore di validazione per il tipo '" + type +"' sul valore: " + source);
		}
	}
	
	public static String quote(String input) {
		return "'" + input.replaceAll( "'", "''" ) + "'";
	}
	
}
