package com.github.tosdan.utils.stringhe;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class MapFormatTypeValidatorSQL implements MapFormatTypeValidator
{
	public static void main( String[] args )
	{
		MapFormatTypeValidatorSQL v = new MapFormatTypeValidatorSQL();
		System.out.println( v.validate( "test del 'piffero'", "string" ) );
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
	public String getTypes()
	{
		return "string|integer|numeric|boolean|compactDate|revCompDate|itaDate|revItaDate|date|reverseDate";
	}

	/**
	 * Effettua un opportuno controllo sul valore passato in base al tipo di dato e restituisce il valore tale com'era oppure opportunamente formattato 
	 * @param source
	 * @param type
	 * @return
	 */
	@Override
	public String validate(String source, String type)
	{
		String result = "";
		
        if ( type != null ) {
        	if 		( type.equalsIgnoreCase("string" ) ) {
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
        		dateFormat = new SimpleDateFormat( "ddMMyyyy" );        	
        		result = parseDate(source);
        	}
        	else if ( type.equalsIgnoreCase("revCompDate") ) {
        		dateFormat = new SimpleDateFormat( "yyyyMMdd" );        	
        		result = parseDate(source);
        	}
        	else if ( type.equalsIgnoreCase("itaDate") ) {
        		dateFormat = new SimpleDateFormat( "dd/MM/yyyy" );        	
        		result = parseDate(source);        		
        	}
        	else if ( type.equalsIgnoreCase("revItaDate") ) {
        		dateFormat = new SimpleDateFormat( "yyyy/MM/dd" );        	
        		result = parseDate(source);        		
        	}
        	else if ( type.equalsIgnoreCase("date") ) {
        		dateFormat = new SimpleDateFormat( "dd-MM-yyyy" );        	
        		result = parseDate(source);        		
        	}
        	else if ( type.equalsIgnoreCase("reverseDate") ) {
        		dateFormat = new SimpleDateFormat( "yyyy-MM-dd" );        	
        		result = parseDate(source);        		
        	}
        	else
        		throw new MapFormatTypeValidatorException("Errore di validazione: il tipo di validazione "+ type +" e' sconosciuto.");
        }
		
		return result;
	}
	
	private String parseDate(String source)
	{
		String result = "";
		try {
			dateFormat.setLenient( false );
			dateFormat.parse( source );
		} catch ( ParseException e ) {
//			e.printStackTrace();
			throw new MapFormatTypeValidatorException("Errore validazine data: " + source, e);
		}
		result = quote(source);
		
		return result;
	}
	
	private String checkMatching( String regex, String source, String type )
	{
		if (source.matches(regex) ) {
			return source;
		} else {
			throw new MapFormatTypeValidatorException("Errore di validazione per il tipo '" + type +"' sul valore: " + source);
		}
	}
	
	public static String quote(String input)
	{
		return "'" + input.replaceAll( "'", "''" ) + "'";
	}
	
	/*
	 * leggere tutta la query e sostituire ogni segnaposto letterale, della forma ${nome, integer}, con un segnaposto numerico, della forma {i} dove i e' una cifra
	 * inserendo il nome sostituito in un array di stringhe, proprio alla posizione i
	 * 
	 * memorizzare il tipo di dato in un altro array alla medesima posizione i
	 */
	
	
	
}
