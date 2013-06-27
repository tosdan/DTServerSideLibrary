package com.github.tosdan.utils.stringhe;

import java.util.Map;

import org.yaml.snakeyaml.Yaml;

public class TemplatePickerYaml implements TemplatePicker
{

	@Override
	public String pick(String sourceContent, String templateName) {
		Yaml yaml = new Yaml();
		
		@SuppressWarnings( "unchecked" )
		Map<String, String> map = ( Map<String, String> ) yaml.load( sourceContent );
		
		return map.get( templateName );
	}

	@Override
	public String normalize( String source )
	{
		// yaml non consente di usare tab per l'indentazione
		source = source.replaceAll( "\t", String.format("%4s", " ") ); 
		
		// EDIT: non sembra esser un problema. Il segno "-" seguito da uno spazio in yaml è un elemento di una lista.
//		source = source.replaceAll( "--(\\s)*", String.format("%s", "--") ); 
		
		return source;
	}

}
