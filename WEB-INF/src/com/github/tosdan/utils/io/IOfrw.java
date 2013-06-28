package com.github.tosdan.utils.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class IOfrw
{

	/**
	 * Legge l'intero contenuto dei un file in un UNICO ACCESSO
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public static String leggiFile(String nomeFile) throws IOException
	{
		File file = new File( nomeFile );
		InputStream is = new FileInputStream( file );
		return leggiInputStream( is );
	}
	
	/**
	 * Legge l'intero contenuto di un InputStream in un UNICO ACCESSO
	 * @param is
	 * @return
	 * @throws IOException
	 */
	public static String leggiInputStream(InputStream is) throws IOException
	{
		InputStreamReader isr = new InputStreamReader( is, "ISO-8859-1" );
		int available = is.available();
		BufferedReader bf = new BufferedReader( isr );
        char[] buffer = new char[ available ]; // normalmente ingoia tutto il file in una volta, file di parecchie MB producono crash
        
		StringBuilder builder = new StringBuilder();
     	while ( bf.read(buffer, 0 , buffer.length) != -1 ) {
     		builder.append( buffer );
     		available = is.available();
     	}
     	
     	return builder.toString();
	}
	
	/* * * * * * * * TEST * * * * * * * * * */
	

	/**
	 * 
	 * @throws IOException
	 */
	public static void testLeggiFile() throws IOException
	{
		String a = leggiFile("d:/tmp/a.txt");
		System.out.println( a );
	}
}
