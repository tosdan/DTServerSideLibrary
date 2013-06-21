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

}
