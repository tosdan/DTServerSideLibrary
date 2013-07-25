package com.github.tosdan.utils.stringhe;

import java.util.Map;
/**
 * 
 * @author Daniele
 * @version 0.1.0-b2013-07-25
 */
public interface MapFormatMatcher
{
	String getPlaceHolderDaRimpiazzare();
	String getStrDentroAlPlaceHolder();
	String getType();
	Map<String, Object> getCustomAttributes();
	
	String getTipi();
	MapFormatMatcher setTipi( String tipi );
	
	MapFormatMatcher reset(String sequenza);
	boolean find();
	
	StringBuffer appendTail(StringBuffer sb);
	MapFormatMatcher appendReplacement(StringBuffer sb, String replacement);
	
	String quoteReplacement(String s);
}
