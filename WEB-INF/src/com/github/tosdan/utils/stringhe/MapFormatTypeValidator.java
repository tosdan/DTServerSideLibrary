package com.github.tosdan.utils.stringhe;

public interface MapFormatTypeValidator
{
	public String getTypes();
	
	public String validate(String source, String type) throws MapFormatTypeValidatorException;
}
