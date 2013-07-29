package com.github.tosdan.utils.sql;

import com.github.tosdan.utils.stringhe.TemplateCompilerException;

public interface QuerySupplier
{
	String getQuery() throws TemplateCompilerException;
	String getQuery(String templName) throws TemplateCompilerException;

}
