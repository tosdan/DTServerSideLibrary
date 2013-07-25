package com.github.tosdan.utils.stringhe;

import java.util.Map;

public interface MapFormatTypeValidator
{
	String[] getSupportedTypes();
	
	String getTypes();
	
	String validate(String source, String type) throws MapFormatTypeValidatorException;
	
	String validate(String source, String type, Map<String, Object> customAttributes) throws MapFormatTypeValidatorException;
}
