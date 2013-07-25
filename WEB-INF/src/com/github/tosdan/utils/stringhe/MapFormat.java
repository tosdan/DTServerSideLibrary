package com.github.tosdan.utils.stringhe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author Daniele
 * @version 0.4.0-b2013-07-25
 */
public class MapFormat
{
    public static void main( String[] args )
	{
		String example = 
				"Nunc ${param2, string} vulputate turpis ${param3, string} feugiat. \n"+
				"Pellentesque ${param1, string} fringilla eleifend arcu id rutrum. \n"+
				"Proin blandit scelerisque tempus. Pellentesque nec tincidunt elit.\n"+
				"Nullam rutrum odio ac ante ${param1, string} interdum.\n"+
				"Fusce ${param2, string} mauris sit amet metus vulputate fermentum sit ${param1, string} at nulla.\n";
		String example2 = 
				"Nunc ${param2} vulputate turpis ${param3} feugiat. \n"+
				"Pellentesque ${param1} fringilla eleifend arcu id rutrum. \n"+
				"Proin blandit scelerisque tempus. Pellentesque nec tincidunt elit.\n"+
				"Nullam rutrum odio ac ante ${param1} interdum.\n"+
				"Fusce ${param2} mauris sit amet metus vulputate fermentum sit '${param1}' at nulla.\n";
		MapFormatTypeValidator validator = new MapFormatTypeValidatorSQL();
		
		Map<String, Object> mappa = new HashMap<String, Object>();
			mappa.put( "param1", "PARAM1" );
			mappa.put( "param2", "PARAM2" );
			mappa.put( "param3", "PARAM3" );
		MapFormat mf = new MapFormat( mappa, validator );
		String a = mf.format( example );
		System.out.println( a + "\n\n\n" + MapFormat.format( example2, mappa ));
		
	}

	// TODO prendere spunto da qui per una NamedPreparedStatement... ma anche no.
    
	private boolean blankSeNull = false;
	private boolean keepUnMatched = false;
	private Map<String, ? extends Object> parametri;
	private MapFormatTypeValidator typeValidator;
	private String tipi;
	private MapFormatMatcher matcher;

    /**
     * 
     * @param template
     * @param params
     * @return
     */
    public static String format(String template, Map<String, ? extends Object> params)
    {
    	MapFormat mf = new MapFormat( params );
		return mf.format( template );
    	
    }
	
    /**
     * 
     * @param template
     * @param params
     * @return
     */
    public static String format(String template, Map<String, ? extends Object> params, MapFormatTypeValidator typeValidator)
    {
    	MapFormat mf = new MapFormat( params, typeValidator );
		return mf.format( template );
    	
    }

    /**
     * 
     * @param parametri mappa contenente le stringhe di sostituizione per i vari segnaposto usati nel template 
     */
	public MapFormat(Map<String, ? extends Object> parametri) {
		this( parametri, null, null );
		
	}
	
	/**
	 * 
	 * @param parametri mappa contenente le stringhe di sostituizione per i vari segnaposto usati nel template 
	 * @param typeValidator oggetto che implementa metodi di controllo sui tipi specificati nel testo da sostituire
	 */
	public MapFormat(Map<String, ? extends Object> parametri, MapFormatTypeValidator typeValidator) {
		this( parametri, typeValidator, null );
	}
	
    /**
     * 
     * @param parametri mappa contenente le stringhe di sostituizione per i vari segnaposto usati nel template 
     * @param typeValidator oggetto che implementa metodi di controllo sui tipi specificati nel testo da sostituire
     * @param matcher
     */
	public MapFormat(Map<String, ? extends Object> parametri, MapFormatTypeValidator typeValidator, MapFormatMatcher matcher) {
		
		this.parametri = parametri;
		this.typeValidator = typeValidator;
		
		if (typeValidator != null)
			this.tipi = typeValidator.getTypes();
		
		if (matcher == null) 
			setMatcher( new MapFormatMatcherImpl("") );
		else 
			setMatcher( matcher );
		
	}
	
	public MapFormat setMatcher(MapFormatMatcher matcher) {
		this.matcher = matcher;
		this.matcher.setTipi(this.tipi);
		return this;
	}
	
	/**
	 * 
	 * @param stringTemplate
	 * @return
	 */
	@SuppressWarnings( "unchecked" )
	public String format(String stringTemplate)
	{
		String result = "";
		StringReader sr = new StringReader( stringTemplate );
		BufferedReader bf = new BufferedReader( sr );
		
		String riga = "";
		try {
			while ( (riga = bf.readLine()) != null ) {
				
		        StringBuffer sb = new StringBuffer();
		        matcher.reset( riga );
		        
		        while ( matcher.find() ) { // <- Finche' nella riga trova un match
		            
		        	String placeHolderDaRimpiazzare = matcher.getPlaceHolderDaRimpiazzare();
		            String strDentroAlPlaceHolder = matcher.getStrDentroAlPlaceHolder();
		            String type = matcher.getType();
		            Map<String, Object> customAttributes = matcher.getCustomAttributes();
		            
		            // La stringa del placeholder viene usata come chiave per trovare il valore sostitutivo nella mappa delle sostituzioni.
		            Object objSostitutivo = parametri.get(strDentroAlPlaceHolder);//-> Null oppure Integer, Boolean o String...
		            
		            // Se nessuna delle condizioni che seguono viene soddisfatta, non viene nemmeno effettuata
		            // la sostituzione, perche' la sostituita rimarra' uguale al valore da sostituire
		            String strSostituita = placeHolderDaRimpiazzare;
		            
		            // Se e' una lista: forma una sequenza composta dai valori sostitutivi separati da virgola.
		            if ( objSostitutivo instanceof List && !((List<Object>) objSostitutivo).isEmpty() ) {
		            	strSostituita = "";
		        		for( Object listElem : (List<Object>) objSostitutivo ) {
		        			
		        			if (! isTipoAmmesso(listElem) )
		        				continue;
		        			
		        			strSostituita += strSostituita.isEmpty() ? "" : ", ";
		        			
		        			if ( typeValidator != null ) {
		    		           	
		        				try {
		    		           		
									strSostituita += typeValidator.validate( listElem.toString(), type, customAttributes );
									
								} catch ( MapFormatTypeValidatorException e ) {
									throw new MapFormatException( "Errore in validazione per '" +listElem.toString() + "'. " + e.getMessage(), e );
								}
		    		           	
		        			} else 
		        				strSostituita += listElem.toString();
		        		}
		        	// se il valore con cui sostituire il parametro non e' nullo (ed e' un oggetto ammesso)
		            } else if ( isTipoAmmesso(objSostitutivo) ) {
		            	
			            if ( typeValidator != null ) {
			            	
			            	try {
			            		
								strSostituita = typeValidator.validate( objSostitutivo.toString(), type, customAttributes );
								
							} catch ( MapFormatTypeValidatorException e ) {
								throw new MapFormatException( "Errore in validazione per '" +objSostitutivo.toString() + "'. " + e.getMessage(), e );
							}
			            	
			            } else 
			            	strSostituita = objSostitutivo.toString();
			            
			        // se e' nullo il valore con cui sostituire il parametro, ma e' true il flag per usare spazi bianchi al suo posto
		            } else if ( objSostitutivo == null && blankSeNull) {
		            	strSostituita = ""; 
		            }
		            matcher.appendReplacement( sb, matcher.quoteReplacement(strSostituita) );
		        }
		        matcher.appendTail( sb );
		        
		        // recupera la riga con le sostituzioni dallo string buffer
		        riga = sb.toString();
		        
		        // ri-testa la riga con il pattern per trovare parametri non sostituiti
		        matcher.reset( riga );
		        // se non ne trova o se e' true il flag per tenerli aggiunge la riga alla variabile di output
		        if ( keepUnMatched || ! matcher.find() ) 
		        	result += riga + "\n";
			}
		} catch ( IOException e ) { e.printStackTrace(); } // e' obbligatorio intercettarla per via del BufferedReader, ma qui di I/O c'e' ben poco
		
        return result;
        
	}
	
	private boolean isTipoAmmesso(Object o) {
		return (   o instanceof String 
				|| o instanceof Integer 
				|| o instanceof Boolean 
				|| o instanceof Double 
				|| o instanceof Float  
				|| o instanceof BigDecimal 
				|| o instanceof BigInteger 
				|| o instanceof Long 
				|| o instanceof Short 
				|| o instanceof Character);
	}
	
	/**
	 * 
	 * @return
	 */
	public Map<String, ? extends Object> getParametri() {
		return parametri;
	}

	/**
	 * 
	 * @param parametri
	 * @return 
	 */
	public MapFormat setParametri( Map<String, ? extends Object> parametri ) {
		this.parametri = parametri;
		return this;
	}

	/**
	 * 
	 * @return
	 */
    public MapFormat setBlankSeNull() {
        blankSeNull = true;
        return this;
        
    }

    /**
     * 
     * @return
     */
	public MapFormat setKeepUnMatched() {
		keepUnMatched = true;
		blankSeNull = false;
		return this;
		
	}

    
}
