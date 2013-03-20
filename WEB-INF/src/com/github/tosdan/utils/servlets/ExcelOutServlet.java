package com.github.tosdan.utils.servlets;

import java.io.IOException;
import java.util.Map;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@SuppressWarnings( "serial" )
public class ExcelOutServlet extends BasicHttpServlet
{
	@Override
	protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException { this.doService( req, resp ); }
	@Override
	protected void doPost( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException { this.doService( req, resp ); }
	
	protected void doService( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException
	{
		int riga = 0;
		String nomeFoglio = this._defaultString( req.getParameter("nomeFoglioXls").replaceAll( "__", " " ), "Foglio 1" );
		String titolo = this._blankIfNull( req.getParameter("titoloXls").replaceAll( "__", " " ) );
		
		Workbook wb = new HSSFWorkbook();
		CreationHelper helper = wb.getCreationHelper();
		Sheet foglio = wb.createSheet(nomeFoglio);
		
		Row row = foglio.createRow( riga++ ); // Prima riga vuota
		
		if ( ! titolo.equals("") ) {
			row = foglio.createRow( riga++ ); // riga per titolo, viene messo dopo l'autoresize delle colonne
			row = foglio.createRow( riga++ ); // riga vuota
		}
		row = foglio.createRow( riga++ );
		row.createCell( 1 ).setCellValue( helper.createRichTextString( "Engine" ) );
		row.createCell( 2 ).setCellValue( helper.createRichTextString( "Browser" ) );
		row.createCell( 3 ).setCellValue( helper.createRichTextString( "Version" ) );
		
		String jsonString = ( String ) req.getSession().getAttribute( "JsonDataTableString" );
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		Map<String, Object> json = gson.fromJson( jsonString, Map.class );
		List<Map<String, Object>> records = ( List<Map<String, Object>> ) json.get( "aaData" );
		
		for( Map<String, Object> record : records ) {
			String e = ( String ) record.get( "engine" );
			String b = ( String ) record.get( "browser" );
			String v = ( String ) record.get( "version" );
			row = foglio.createRow( riga++ );
			row.createCell( 1 ).setCellValue( helper.createRichTextString( e ) );
			row.createCell( 2 ).setCellValue( helper.createRichTextString( b ) );
			row.createCell( 3 ).setCellValue( helper.createRichTextString( v ) );
//			CellStyle cellStyle = wb.createCellStyle();
//			cellStyle.setDataFormat(helper.createDataFormat().getFormat("dd/mm/yy hh:mm"));
			
			
		}
		
		foglio.autoSizeColumn( 1 );
		foglio.autoSizeColumn( 2 );
		foglio.autoSizeColumn( 3 );
		
		if ( ! titolo.equals("") ) {
			row = foglio.getRow( 1 ); // titolo nella seconda riga, la prima e' lasciata vuota
			Font font = wb.createFont();
	        font.setFontName(HSSFFont.FONT_ARIAL);
	        font.setFontHeightInPoints((short) 18);
	        Cell cell = row.createCell( 1 );
	        cell.setCellValue( titolo );
	        CellStyle style = wb.createCellStyle();
	        style.setFont( font );
			cell.setCellStyle( style );
		}
		
		resp.setContentType( "application/octet-stream" );
		resp.setHeader( "Content-Disposition", "attachment; filename="+"prova.xls" );
		ServletOutputStream o = resp.getOutputStream();
		wb.write( o );
		o.close();
	}
	
}
