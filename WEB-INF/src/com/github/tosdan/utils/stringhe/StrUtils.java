package com.github.tosdan.utils.stringhe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

/**
 * 
 * @author Daniele
 * @version 0.0.3-b2013-07-22
 */
public class StrUtils
{
	/**
	 * 
	 * @param obj
	 * @return
	 */
	public static <T> String safeToStr(T obj) {
		return safeToStr( obj, "" );
	}
	
	/**
	 * 
	 * @param obj
	 * @param defaultString
	 * @return
	 */
	public static <T> String safeToStr(T obj, String defaultString) {
		try {
			return obj.toString();
		} catch ( Exception e ) {
			return defaultString;
		}
	}
	
	/**
	 * 
	 * @param args
	 * @return 
	 */
	public static <T> String coalesce(T... args) {
		for( int i = 0 ; i < args.length ; i++ ) {
			if (args[i] != null)
				return args[i].toString();
		}
		return null;
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
