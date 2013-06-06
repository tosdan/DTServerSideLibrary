package com.github.tosdan.utils.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 
 * @author Daniele
 * @version 0.1.0-r13.06.05
 */
public class ReadToOutputStream
{
	/**
	 * 
	 * @param nomeFile
	 * @param outStream
	 * @throws IOException
	 */
	public static void readFile(String nomeFile, OutputStream outStream) throws IOException {
		ReadToOutputStream.readInputStream( new FileInputStream(new File(nomeFile)), outStream );
			
	}
	
	/**
	 * 
	 * @param is
	 * @param outStream
	 * @throws IOException
	 */
	public static void readInputStream(InputStream is, OutputStream outStream) throws IOException {		
		BufferedInputStream bis = new BufferedInputStream( is );
		BufferedOutputStream bufferOut = new BufferedOutputStream( outStream );
		int b = 0;
		byte[] bufferData = new byte[ 8192 ];
		
		while ( ( b = bis.read( bufferData ) ) != -1 ) {
			bufferOut.write( bufferData, 0, b );
		}
		bis.close();
		bufferOut.flush();
		bufferOut.close();
			
	}
}
