package com.github.tosdan.utils.stringhe;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;
/**
 * Validator per T-SQL
 * @author Daniele
 * @version 0.2.0-b2013-07-25
 */
public class MapFormatTypeValidatorSQL extends MapFormatAbstractTypeValidator
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

	@Override
	public String[] getSupportedTypes() {
		return new String [] {"string", "stringa", "integer", "numeric", "boolean", "compactDate", "revCompDate",
								"itaDate", "revItaDate", "date", "reverseDate", "table", "tablePart", "free"};
	}

	/**
	 * Effettua un opportuno controllo sul valore passato in base al tipo di dato e restituisce il valore tale com'era oppure opportunamente formattato 
	 * @param source sorgente da validare
	 * @param type Tipo del parametro da validare
	 * @return La stringa sorgente normalizzata
	 * @throws MapFormatTypeValidatorException Se la stringa sorgente non e' conforme al tipo specificato o se non e' stato specificato il parametro type.
	 */
	@Override
	public String validate(String source, String type) throws MapFormatTypeValidatorException {
		return validate( source, type, null );
	}
	
	/**
	 * Effettua un opportuno controllo sul valore passato in base al tipo di dato e restituisce il valore tale com'era oppure opportunamente formattato 
	 * @param source sorgente da validare
	 * @param type Tipo del parametro da validare
	 * @param customAttributes <i>Parametro non utilizzato</i>
	 * @return La stringa sorgente normalizzata
	 * @throws MapFormatTypeValidatorException Se la stringa sorgente non e' conforme al tipo specificato o se non e' stato specificato il parametro type.
	 */
	@Override
	public String validate( String source, String type, Map<String, Object> customAttributes ) throws MapFormatTypeValidatorException {
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
        		throw new MapFormatTypeValidatorException("Errore di validazione: il tipo di validazione '"+ type +"' e' sconosciuto. Parametro source="+source);
        	
        } else {
        	throw new MapFormatTypeValidatorException( "Parametro type mancante." );
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
