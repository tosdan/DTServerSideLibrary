package com.github.tosdan.utils.stringhe;

import java.util.Map;
/**
 * 
 * @author Daniele
 * @version 0.2.0-b2013-07-29
 */
public interface MapFormatTypeValidator
{
	String getValidatorRegExPart();
	
	String validate(String source, String type) throws MapFormatTypeValidatorException;
	
	String validate(String source, String type, Map<String, Object> customAttributes) throws MapFormatTypeValidatorException;
}
