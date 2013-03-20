package com.github.tosdan.experiments;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class ExcelOutTest
{

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main( String[] args ) throws IOException
	{

		Workbook wb = new HSSFWorkbook();
		CreationHelper helper = wb.getCreationHelper();
		Sheet foglio = wb.createSheet("Foglio 1");
		Row row = foglio.createRow( 0 );
		row.createCell( 0 ).setCellValue( helper.createRichTextString( "prova" ) );
		
		FileOutputStream fileOut = new FileOutputStream( "prova.xls" );
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		wb.write( baos );
		baos.writeTo( fileOut );
		fileOut.close();
		baos.close();
	}

}
