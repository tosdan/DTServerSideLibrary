package com.github.tosdan.utils.stringhe;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * 
 * @author Daniele
 * @version 0.1.1-b2013-07-29
 */
public abstract class MapFormatAbstractMatcher implements MapFormatMatcher
{
	private Matcher matcher;
	private String placeHolderHead;
	private String placeHolderTail;
	
	private String validatorRegExPart;
	
	private String placeHolderDaRimpiazzare;
	private String strDentroAlPlaceHolder;
	private String type;
	
	
	public MapFormatAbstractMatcher(String riga) {
		resetPlaceHolder();
		reset( riga );
		
	}

	@Override
	public MapFormatMatcher reset(String sequenza) {
		matcher = getPattern().matcher(sequenza);
		return this;
	}

	@Override
	public boolean find() {
		boolean esito = matcher.find();
		
		if (esito) {
	        placeHolderDaRimpiazzare = matcher.group(); 	// puo' esser 	-> ${stringaDaSostituire} oppure ${stringaDaSostituire, integer}
	        strDentroAlPlaceHolder = matcher.group(1);		// sempre 		-> "stringaDaSostituire"
	        type = matcher.group(4);		 				// se presente	-> "integer"
		}
		
        return esito;
	}

	@Override
	public StringBuffer appendTail(StringBuffer sb) {
		return matcher.appendTail( sb );
	}
	@Override
	public MapFormatMatcher appendReplacement(StringBuffer sb, String replacement) {
		matcher.appendReplacement( sb, replacement );
		return this;
	}
	@Override
	public String quoteReplacement(String s) {
		return Matcher.quoteReplacement( s );
	}
	
	@Override
	public String getPlaceHolderDaRimpiazzare() {
		return placeHolderDaRimpiazzare;
	}
	@Override
	public String getStrDentroAlPlaceHolder() {
		return strDentroAlPlaceHolder;
	}
	@Override
	public String getType() {
		return type;
	}

	@Override
	public String getValidatorRegExPart() {
		if (validatorRegExPart == null)
			return "";
		else 
			return validatorRegExPart;
	}

	@Override
	public MapFormatMatcher setValidatorRegExPart( String validatorRegExPart ) {
		this.validatorRegExPart = validatorRegExPart;
		return this;
	}
	
	@Override
	public Map<String, Object> getCustomAttributes() {
		return null;
	}
	
	
	
	public abstract String getRegExSchema();	//	return "([a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)*)(,[ ]*(?i)(" + getTipi() + "))?";
	public abstract MapFormatAbstractMatcher resetPlaceHolder(); //	setPlaceHolderHeadAndTail("${","}");

	
	/**
	 * 
	 * @return
	 */
	public String getRegEx() {
		return placeHolderHead + getRegExSchema() + placeHolderTail;
	}
	
	
	/**
	 * Reimposta il Pattern compilando l'espressione regolare.
	 * @return 
	 */
	private Pattern getPattern() {
		return Pattern.compile( getRegEx() );
	}

	
	/**
	 * Permette di cambiare Head e Tail del PlaceHolder lasciando invariata l'espressione regolare.
	 * @param head
	 * @param tail
	 * @return
	 */
	public MapFormatAbstractMatcher setPlaceHolderHeadAndTail(String head, String tail) {
		this.placeHolderHead = Pattern.quote(head);
		this.placeHolderTail = Pattern.quote(tail);
		return this;
	}

}
