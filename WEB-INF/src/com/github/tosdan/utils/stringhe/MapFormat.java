package com.github.tosdan.utils.stringhe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MapFormat
{
	// TODO metodi per personalizzazione sulla forma del placeholder: parentesi da usare e/o presenza del $ in testa
	// TODO prendere spunto da qui per una NamedPreparedStatement
	private Pattern schema;
	private boolean blanknull = false;
	private boolean keepUnMatched = false;
	private Map<String, ? extends Object> parametri;
	private MapFormatTypeValidator typeValidator;

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
	
    /**
     * @deprecated
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
     * @deprecated
     * 
     * @param parametri mappa contenente le stringhe di sostituizione per i vari segnaposto usati nel template 
     */
	public MapFormat(Map<String, ? extends Object> parametri) {
		this( parametri, null );
		
	}
	
    /**
     * 
     * @param parametri mappa contenente le stringhe di sostituizione per i vari segnaposto usati nel template 
     * @param typeValidator oggetto che implementa metodi di controllo sui tipi specificati nel testo da sostituire
     */
	public MapFormat(Map<String, ? extends Object> parametri, MapFormatTypeValidator typeValidator) {
		
		this.parametri = parametri;
		this.typeValidator = typeValidator;
		String tipi = "";
		if (typeValidator != null)
			tipi = typeValidator.getTypes();
		
		this.schema = Pattern.compile("\\$\\{([a-zA-Z][a-zA-Z0-9_]*(\\.[a-zA-Z][a-zA-Z0-9_]*)*)(,[ ]?(?i)(" + tipi + "))?\\}");
		
	}
	
	/**
	 * 
	 * @param stringTemplate
	 * @return
	 */
	@SuppressWarnings( "unchecked" )
	public String format(String stringTemplate)
	{
		Matcher matcher = this.schema.matcher("");
		String result = "";
		StringReader sr = new StringReader( stringTemplate );
		BufferedReader bf = new BufferedReader( sr );
		
		String riga = "";
		try {
			while ( (riga = bf.readLine()) != null ) {
				
		        StringBuffer sb = new StringBuffer();
		        
				matcher.reset(riga);
				// finche' nella riga trova un match
		        while ( matcher.find() ) {
		            String sDaSostituire = matcher.group(); 	// puo' esser 	-> ${stringaDaSostituire} oppure ${stringaDaSostituire, integer}
		            String chiaveSostituto = matcher.group(1); 	// sempre 		-> "stringaDaSostituire"
		            String type = matcher.group(4);		 		// se presente	-> "integer"
		            Object objSostituto = this.parametri.get(chiaveSostituto);//-> Null oppure Integer, Boolean o String
		            
		            // se nessuna delle condizioni che seguono viene soddisfatta, non viene nemmeno effettuata
		            // la sostituzione, perche' la sostituta rimarra' uguale al valore da sostituire
		            String sSostituta = sDaSostituire;
		            
		            // se e' una lista, concatena i valori separandoli con la virgola.
		            if ( objSostituto instanceof List && !((List<Object>) objSostituto).isEmpty() ) {
		            	sSostituta = "";
		        		for( Object listElem : (List<Object>) objSostituto ) {
		        			if (! this.isTipoAmmesso(listElem) )
		        				continue;
		        			if ( !sSostituta.isEmpty() )
		        				sSostituta += ", ";
		        			
		        			if ( this.typeValidator != null )
		    		           	sSostituta += this.typeValidator.validate( listElem.toString(), type );
		        			else
		        				sSostituta += "'"+ listElem + "'"; // e' una precauzione, ma non sarebbe del tutto corretto. E' consigliato aggiugngere sempre il tipo di dato nel parametro da sostituire
		        		}
		        	// se non e' nullo il valore con cui sostituire il parametro 
		            } else if ( this.isTipoAmmesso(objSostituto) ) {
			            if ( this.typeValidator != null )
			            	sSostituta = this.typeValidator.validate( objSostituto.toString(), type );
			            else
			            	sSostituta = objSostituto.toString();
			        // se e' nullo il valore con cui sostituire il parametro ma e' true il flag per usare spazi bianchi al suo posto
		            } else if ( objSostituto == null && this.blanknull) {
		            	sSostituta = ""; 
		            } 
		            
		            matcher.appendReplacement( sb, Matcher.quoteReplacement(sSostituta) );
		        }
		        matcher.appendTail(sb);
		        
		        // recupera la riga con le sostituzioni dallo string buffer
		        riga = sb.toString();
		        
		        // ri-testa la riga con il pattern per trovare parametri non sostituiti
		        matcher.reset(riga);
		        // se non ne trova o se e' true il flag per tenerli aggiunge la riga alla variabile di output
		        if ( this.keepUnMatched || ! matcher.find() ) 
		        	result += riga + "\n";
			}
		} catch ( IOException e ) { e.printStackTrace(); } // riguarda il buffered reader ma qui di I/O c'e' ben poco
		
        return result;
        
	}
	
	private boolean isTipoAmmesso(Object o) {
		return ( o instanceof String || o instanceof Integer || o instanceof Boolean 
				|| o instanceof Double || o instanceof Float  || o instanceof BigDecimal 
				|| o instanceof BigInteger );
	}
	
	/**
	 * 
	 * @return
	 */
	public Map<String, ? extends Object> getParametri()
	{
		return parametri;
	}

	/**
	 * 
	 * @param parametri
	 */
	public void setParametri( Map<String, ? extends Object> parametri )
	{
		this.parametri = parametri;
	}

	/**
	 * 
	 * @return
	 */
    public MapFormat setBlankNull() {
        this.blanknull = true;
        return this;
        
    }

    /**
     * 
     * @return
     */
	public MapFormat setKeepUnMatched() {
		this.keepUnMatched = true;
		return this;
		
	}

    
}
