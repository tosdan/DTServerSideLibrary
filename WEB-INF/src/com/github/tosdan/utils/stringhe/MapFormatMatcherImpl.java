package com.github.tosdan.utils.stringhe;

/**
 * 
 * @author Daniele
 * @version 0.1.3-b2013-07-29
 */
public class MapFormatMatcherImpl extends MapFormatAbstractMatcher
{
	public MapFormatMatcherImpl(String riga) {
		super( riga );
	}
	
	public String getRegExSchema() {
		return "([a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)*)(,[ ]*(?i)("+ getValidatorRegExPart()+"))?";
	}
	
	public MapFormatAbstractMatcher resetPlaceHolder() {
		setPlaceHolderHeadAndTail( "${", "}" );
		return this;
	}
	
}
