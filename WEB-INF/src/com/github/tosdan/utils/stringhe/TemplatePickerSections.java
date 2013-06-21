package com.github.tosdan.utils.stringhe;

public class TemplatePickerSections implements TemplatePicker
{

	@Override
	public String pick( String sourceContent, String templateName )
	{
		return StrUtils.findSection( sourceContent, templateName );
	}

}
