package com.github.tosdan.dataTableServerSide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class DTReplySqlProvider
{
	private String stringaSQL;
	private Map<Integer, String> nomiColonne;
	private Map<String, String> parametriRequest;
	private List<String> listaDegliMDataProp;
	/**
	 * Contiene l'indice del primo elemento mostrato nella tabella.
	 * La numerazione parte da 0.
	 */
	private String iDisplayStart;
	/**
	 * Contiene il numero di record da mostrare per ogni pagina della tabella
	 * e quindi il numero di record restituiti dalla query.
	 */
	private String iDisplayLength;
	/**
	 * Numero di colonne per le quali e' richiesto un ordinamento
	 */
	private String iSortingCols;
	/**
	 * Stringa del filtro di ricerca
	 */
	private String sSearch;
	/**
	 * Numero totali di colonne visibili + invisibili della tabella
	 */
	private String iColumns;
	private String row_number;
	
	public DTReplySqlProvider( Map<String, String> parametriRequest, String stringaSQL )
	{
		this.iColumns = parametriRequest.get("iColumns");
		this.sSearch = parametriRequest.get( "sSearch" );
		this.iSortingCols = parametriRequest.get( "iSortingCols" );
		this.iDisplayLength = parametriRequest.get( "iDisplayLength" );
		this.iDisplayStart = parametriRequest.get( "iDisplayStart" );
		this.row_number = parametriRequest.get( "row_number" );
		
		this.parametriRequest = parametriRequest;
		this.nomiColonne = new HashMap<Integer, String>();
		this.stringaSQL = stringaSQL;
		this.listaDegliMDataProp = new ArrayList<String>();
		
		this.parseParametri();
	}
	/**
	 * 
	 * @return Query SQL per recuperare i record con cui compilare la tabella
	 */
	public String getSqlDatiQuery()
	{
		String querySql = "";
		String sOrderBy = this.getFiltroOrderBy();
		
		String aliasRowNumber = "DT_RowId"; // DT_RowId stringa riservata in DataTables: il valore ricavato dal DB viene inserito come id del <tr> associato al record
		
		if ( sOrderBy.isEmpty() || (row_number != null && row_number.equalsIgnoreCase("false")) )
		{	// Senza alcun ordinamento e' impossibile usare la row_number quindi viene a meno anche la paginazione
			String orderByClause = sOrderBy.isEmpty() ? "" : " ORDER BY " + sOrderBy;
			querySql =
					" SELECT * \n " +
					" FROM " + stringaSQL + "\n " +
					" WHERE 1=1 " + this.getFiltroRicerca() + "\n " +
					orderByClause + "\n";
		} else {
			querySql = 
				" SELECT * FROM ( \n"+
				" 	SELECT ROW_NUMBER() OVER( ORDER BY "+sOrderBy+" ) AS "+aliasRowNumber+ ", * \n" +
				" 	FROM " + stringaSQL + " \n" +
				" WHERE 1=1 " + this.getFiltroRicerca() + "\n" +
				" ) AS tabella_con_righe \n" + 
				" WHERE 1=1 " + this.getFiltroPaginazione(aliasRowNumber);
		}

		return querySql;
	}

	/**
	 * 
	 * @return
	 */
	public String getSqlDatiAggiuntivi()
	{
		String sql = 
			"SELECT ( SELECT COUNT(*) FROM " + stringaSQL +" )  AS iTotalRecords \n " +
			"		,  ( SELECT COUNT(*) FROM " + stringaSQL + " WHERE 1=1" + this.getFiltroRicerca() +" ) AS iTotalDisplayRecords \n ";

		return sql;
	}
		
	/**
	 * Restituisce una stringa pronta da usare dopo la clausola order by nella query sql  
	 * @return String nella forma "nomeColonna ASC/DESC , nomeColonna ASC/DESC , ... "
	 */
	public String getFiltroOrderBy()
	{
		String retVal = "";

		if ( this.listaDegliMDataProp.get(0).matches("(^[a-z\\[A-Z_][a-zA-Z_0-9-\\]]*$)|(^[\\[][a-z A-Z_0-9-]*[\\]]$)") ) // match con un identificativo valido sql
		{
			Map<String, String> ordinamentoColonne = this.getOrdinamentoColonneNamed();
			Set<Entry<String, String>> setColonne = ordinamentoColonne.entrySet();
			for( Entry<String, String> colonna : setColonne ) {
				if ( ! retVal.equals("") )
					retVal += " , ";

				String nomeCol = colonna.getKey();
				if (nomeCol.equals("")) return ""; 
				String verso = colonna.getValue();

				retVal += nomeCol + " " + verso;
			}
		} else  {
			Map<Integer, String> ordinamentoColonne = this.getOrdinamentoColonneNum();
			Set<Entry<Integer, String>> entrySet = ordinamentoColonne.entrySet();
			for( Entry<Integer, String> entry : entrySet ) {
				if ( ! retVal.equals("") )
					retVal += " , ";

				int numColonna = entry.getKey();
				String verso = entry.getValue();
				
				retVal += (1+numColonna) + " " + verso;
			}
		}
		
		return retVal;
	}
	
	/**
	 * Recupera il verso d'ordinamento delle colonne della tabella
	 * @return Mappa con le coppie [ nomeColonna -> versoOrdinamento ]
	 */
	public Map<String, String> getOrdinamentoColonneNamed()
	{
		LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
		if ( iSortingCols != null )
		{
			for ( int i = 0 ; i < Integer.valueOf( iSortingCols ) ; i++ )
			{
				String versoOrdinamentoIEsima 		=	parametriRequest.get("sSortDir_" + i).toUpperCase(); // puo' essere ASC o DESC
				int indiceIEsimaColonnaDaOrdinare 	=	Integer.parseInt( parametriRequest.get("iSortCol_" + i) );
				// NB: iSortCol_[0, 1, 2...] => [I, II, III...] numera i criteri di ordinamento: il 1o criterio per cui ordinare e' la colonna I, il 2o e' la colonna II...   
				// Recupera il nome associato alle colonne attraverso mDataProp_xx piu' l'indice di colonna recuperato da iSortCol 
				String nomeIEsimaColonnaDaOrdinare 	= nomiColonne.get( indiceIEsimaColonnaDaOrdinare );	//parametriRequest.get("mDataProp_" + indiceIEsimaColonnaDaOrdinare); 
				
				result.put( nomeIEsimaColonnaDaOrdinare, versoOrdinamentoIEsima );
			}
		}
		
		return result;
	}

	/**
	 * Recupera il verso d'ordinamento delle colonne della tabella
	 * @return Mappa con le coppie [ nomeColonna -> versoOrdinamento ]
	 */
	public Map<Integer, String> getOrdinamentoColonneNum()
	{
		LinkedHashMap<Integer, String> result = new LinkedHashMap<Integer, String>();
		if ( iSortingCols != null )
		{
			for ( int i = 0 ; i < Integer.valueOf( iSortingCols ) ; i++ )
			{
				String versoOrdinamentoIEsima 		=	parametriRequest.get("sSortDir_" + i).toUpperCase(); // puo' essere ASC o DESC
				int indiceIEsimaColonnaDaOrdinare 	=	Integer.parseInt( parametriRequest.get("iSortCol_" + i) );
				
				result.put( indiceIEsimaColonnaDaOrdinare, versoOrdinamentoIEsima );
			}
		}
		
		return result;
	}
	
	/**
	 * Assembla il filtro di ricerca solo per le colonne per cui è richiesta
	 * @return Una stringa nella forma " AND ( xxx like '%abc%' OR yyy  like '%abc%' OR ... ) " da appendere ad una clausola WHERE esistente 
	 */
	public String getFiltroRicerca()
	{
		String filtro = "";
		
		if ( sSearch !=null && !sSearch.equals("") )
		{
			for ( int i = 0; i < Integer.valueOf(iColumns) ; i++ ) 
			{
				if ( parametriRequest.get("bSearchable_"+i).equalsIgnoreCase("true") && nomiColonne.get(i) != null && !nomiColonne.get(i).equals("")) // verifica che per tale colonna sia richiesto di attuare il filtro
				{
					if ( filtro.isEmpty() ) 
						filtro += " AND ( ";
					else 
						filtro += " OR ";
					
					filtro += nomiColonne.get( i ) + " LIKE " + " '%"+ sSearch +"%' " ;
				}
			}
			
			if ( ! filtro.isEmpty() )
				filtro += " ) ";
			
		}
		
		return filtro;
	}
	

	/**
	 * Assembla la stringa per il filtro dei paginazione 
	 * @param alisRowNumber Il nome dell'alias assegnato nella query alla funzione sql ROW_NUMBER()
	 * @return Stringa nella forma " AND 'aliasRowNumber' BETWEEN 'indicePrimoElemPagina' AND 'indiceUltimoElemPagina' "
	 */
	public String getFiltroPaginazione(String alisRowNumber)
	{
		String retVal = "";
		
		if ( iDisplayStart != null  &&  iDisplayLength != null && ! iDisplayLength.equals( "-1" ))
		{
			int iIndicePrimoElem 	=	Integer.valueOf(iDisplayStart);
			int iDimPagina 			=	Integer.valueOf(iDisplayLength);
			
			retVal = " AND "+alisRowNumber+" BETWEEN "+(iIndicePrimoElem + 1)+" AND "+ (iDimPagina + iIndicePrimoElem);
		}

		return retVal;
	}
	
	

	private void parseParametri()
	{
		Set<Entry<String, String>> setParametriReq = this.parametriRequest.entrySet();
		for( Entry<String, String> entry : setParametriReq ) {	
			String key = entry.getKey();
			String nomeCol = entry.getValue();
			
			if (key.indexOf( "mDataProp_" ) > -1) {
				this.listaDegliMDataProp.add(key);
				int index = ( "mDataProp_" ).length();
				int numCol = Integer.valueOf( key.substring(index) );
				this.nomiColonne.put( numCol, nomeCol );
				
			}
		}
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	

	/**
	 * Verifica le impostazioni di ricerca per le varie colonne.
	 * Restituisce un elenco di nomi di colonna accoppiati ad un
	 * valore booleano che indica se per tale colonna i il filtro
	 * di ricerca deve essere applicato o meno.
	 * @return Mappa con coppie [ nomeColonna -> Boolean ]
	 */
	/**/
	@Deprecated
	public Map<String, Boolean> getColumnSearchSettings()
	{
		LinkedHashMap<String, Boolean> result = new LinkedHashMap<String, Boolean>();
		
		if ( sSearch !=null && ! sSearch.equals("") )
		{
			for (int i = 0; i < Integer.valueOf(iColumns) ; i++) 
			{
				if ( parametriRequest.get("bSearchable_"+i).equalsIgnoreCase("true") )
					result.put( nomiColonne.get( i ), Boolean.valueOf(parametriRequest.get("bSearchable_"+i)) );
			}
		}
		
		return result;
		
	}
	/**/
	
}
