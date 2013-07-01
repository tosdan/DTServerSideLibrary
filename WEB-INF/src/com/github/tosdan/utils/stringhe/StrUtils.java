package com.github.tosdan.utils.stringhe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

public class StrUtils
{
	
	public static <T extends Object> String safeToStr(T obj) {
		return safeToStr( obj, "" );
	}
	
	public static <T extends Object> String safeToStr(T obj, String defaultString) {
		try {
			return obj.toString();
		} catch ( Exception e ) {
			return defaultString;
		}
	}
	
	/**
	 * estrae il testo associato ad una particolare sezione in un file di configurazione
	 * @param source
	 * @param section
	 * @return
	 */
	public static String findSection(String source, String section)
	{
		String result = "";
		
		StringReader reader = new StringReader( source );
		BufferedReader bf = new BufferedReader( reader );
		
		String temp = "";
		try {
			while( (temp = bf.readLine()) != null) {
				
				if ( temp.indexOf("%[") > -1 && temp.matches( "(\\s)*%\\[(" + section + ")\\]%(\\s)*" ) ) {
					
					while( (temp = bf.readLine()) != null) {
						
						if (temp.indexOf( "%[" ) > -1) {
							break;
						} else {
							
							if (result.length() > 0)
								result += "\n";
							
							result += temp;
						}
					}
				}
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
		
		return result;
	}

	/* * * * * * * * * * * * * * * * * * * * * TEST * * * * * * * * * * * * * * * * * * * * * * */
	
	
	/**
	 * 
	 */
	public static void testFindSection()
	{
		String iniTemplate =
				"%[sezione1]%\n" +
				"testo sezione 1\n" +
				"altro testo sezione 1\n" +
				"\n" +
				
				"%[sezione2]%\n" +
				"testo sezione 2\n" +
				"altro testo sezione 2\n"+
				"\n" +
				
				"%[sezione3]%\n" +
				"testo sezione 3\n" +
				"altro testo sezione 3\n";
		String findResult = findSection( iniTemplate, "sezione2" );
		System.out.println( "**********\n"+findResult+"\n*************" );
	}
	
}
