package com.github.tosdan.utils.stringhe;

import java.util.Map;
/**
 * 
 * @author Daniele
 * @version 0.1.1-b2013-07-29
 */
public interface MapFormatMatcher
{
	String getPlaceHolderDaRimpiazzare();
	String getStrDentroAlPlaceHolder();
	String getType();
	Map<String, Object> getCustomAttributes();
	
	String getValidatorRegExPart();
	MapFormatMatcher setValidatorRegExPart( String tipi );
	
	MapFormatMatcher reset(String sequenza);
	boolean find();
	
	StringBuffer appendTail(StringBuffer sb);
	MapFormatMatcher appendReplacement(StringBuffer sb, String replacement);
	
	String quoteReplacement(String s);
}
