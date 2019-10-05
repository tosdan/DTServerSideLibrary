package com.github.tosdan.utils.sql;

public interface RowProcessorFormatter {
	
	<T extends Object> T format(String nomeColonna, Object valoreColonna);
	boolean isToFormat(String nomeColonna);
	
}
