package com.github.tosdan.utils.stringhe;

public interface TemplatePicker
{
	String pick( String sourceContent, String templateName );
	String normalize(String source);
}
