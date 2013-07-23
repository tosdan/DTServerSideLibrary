package com.github.tosdan.utils.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

public class FileUtils
{	

	/**
	 * Scrive semplicemente una stringa in un file
	 * @param fileName nome file con percorso completo
	 * @param content contenuto da scrivere all'interno del file
	 */
	public static void toFile(String fileName, String content) {
		toFile( fileName, content, false );
	}
	/**
	 * Scrive semplicemente una stringa in un file
	 * @param fileName nome file con percorso completo
	 * @param content contenuto da scrivere all'interno del file
	 * @param appendFlag 
	 */
	public static void toFile(String fileName, String content, boolean appendFlag)
	{
		// crea un file di log con il nome passato come parametro nella sottocartella della webapp
			File logFile = new File( fileName );
			File logPath = new File( logFile.getParent() );
		try {
			if ( !logPath.exists() )
				logPath.mkdirs();
			
			FileOutputStream fos = new FileOutputStream( fileName, appendFlag );
			PrintWriter logWriter = new PrintWriter( fos );
			logWriter.println(content);
			logWriter.close();
			
		} catch ( Exception e ) {
			// qualsiasi eccezione nella stampa del log non deve essere bloccante
			String astks = "*************************************";
			String ecc = astks+"\n* Eccezione catturata, ma non gestita\n"+astks+"\n"; 
			System.err.println( ecc+ "FileUtils.toFile() - Errore nel tentativo di scrivere il logfile\n"+logFile.getName()+"\nnella cartella\n"+logPath.getAbsolutePath() );
			e.printStackTrace();
			System.err.println( "\n"+astks );
		} 
	}
}
