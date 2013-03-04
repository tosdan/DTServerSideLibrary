package com.github.tosdan.utils.stringhe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
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
				"Nunc ${param2} vulputate turpis ${param3} feugiat. \n"+
				"Pellentesque ${param1} fringilla eleifend arcu id rutrum. \n"+
				"Proin blandit scelerisque tempus. Pellentesque nec tincidunt elit.\n"+
				"Nullam rutrum odio ac ante ${param1} interdum.\n"+
				"Fusce ${param2, string} mauris sit amet metus vulputate fermentum sit ${param1} at nulla.\n";
	
		MapFormatTypeValidator validator = new MapFormatTypeValidatorSQL();
		
		Map<String, String> mappa = new HashMap<String, String>();
			mappa.put( "param1", "PARAM1" );
			mappa.put( "param2", "PARAM2" );
			mappa.put( "param3", "PARAM3" );
		MapFormat mf = new MapFormat( mappa, validator );
		String a = mf.format( example );
		System.out.println( a + "\n\n\n" + MapFormat.format( example, mappa ));
		
	}
	
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
		
		this.schema = Pattern.compile("\\$\\{([a-zA-Z][a-zA-Z0-9_]*(\\.[a-zA-Z][a-zA-Z0-9_]*)*)(,[ ]?(" + tipi + "))?\\}");
		
	}
	
	/**
	 * 
	 * @param stringTemplate
	 * @return
	 */
	public String format(String stringTemplate)
	{
		Matcher matcher = schema.matcher("");
		String result = "";
		StringReader sr = new StringReader( stringTemplate );
		BufferedReader bf = new BufferedReader( sr );
		
		String riga = "";
		try {
			while ( (riga = bf.readLine()) != null ) {
				
		        StringBuffer sb = new StringBuffer();
		        
				matcher.reset(riga);
		        while ( matcher.find() ) {
		            String sDaSostituire = matcher.group(); // -> ${stringaDaSostituire}
		            String chiaveSostituto = matcher.group(1); // -> stringaDaSostituire
		            String type = matcher.group(4);		            
		            Object objSostituto = parametri.get(chiaveSostituto);
		            
		            String sostituto = (objSostituto == null) 
		            	? (this.blanknull ? "" : sDaSostituire)
		                : objSostituto.toString();
		            	
		            if ( !sostituto.equals(sDaSostituire) 
	            		&& !sostituto.equals("") 
	            		&& typeValidator != null 
	            		&& type != null 
	            		&& !type.equalsIgnoreCase("") )
		            {
		            	sostituto = this.typeValidator.validate( sostituto, type );
		            }
		            
		            matcher.appendReplacement( sb, Matcher.quoteReplacement(sostituto) );
		        }
		        matcher.appendTail(sb);
		        
		        riga = sb.toString();
		        
		        
		        matcher.reset(riga);
		        if ( keepUnMatched || ! matcher.find() ) // verifica che non sia rimaste qualche stringa non sostituita
		        	result += riga + "\n";
			}
		} catch ( IOException e ) { e.printStackTrace(); } // catturata qui perche' impossibile che venga lanciata
		
        return result;
        
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
