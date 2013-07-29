package com.github.tosdan.utils.sql;

import java.util.Map;

import com.github.tosdan.utils.stringhe.MapFormatTypeValidatorSQL;
import com.github.tosdan.utils.stringhe.TemplateCompiler;
import com.github.tosdan.utils.stringhe.TemplateCompilerException;

public class QuerySupplierImpl implements QuerySupplier
{
	private Map<String, Object> parametriTempl;
	private String templName;
	private String queryTemplate;
	
	public QuerySupplierImpl( String queryTemplate, Map<String, Object> parametriTempl) {
		this( queryTemplate, parametriTempl, null);
	}

	public QuerySupplierImpl( String queryTemplate, Map<String, Object> parametriTempl, String templName) {
		this.queryTemplate = queryTemplate;
		this.templName = templName;
		this.parametriTempl = parametriTempl;
	}
	
	@Override
	public String getQuery() throws TemplateCompilerException {
		return getQuery(templName);
	}
	
	@Override
	public String getQuery(String templName) throws TemplateCompilerException {
		return TemplateCompiler.compile( queryTemplate, templName, parametriTempl, new MapFormatTypeValidatorSQL() );
	}

}
