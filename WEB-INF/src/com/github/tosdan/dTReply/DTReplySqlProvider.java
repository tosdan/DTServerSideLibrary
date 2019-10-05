package com.github.tosdan.dTReply;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
/**
 * 
 * @author Daniele
 * @version 0.0.1-b2013-09-13
 */
public class DTReplySqlProvider
{
	private String sqlBase;
	private Map<String, String> parametriRequest;
	private List<String> listaColonne;
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
	private Integer iColumns;
	private Boolean rowNumber;
	
	public DTReplySqlProvider( Map<String, String> parametriRequest, String sqlBase ) {
		init(parametriRequest, sqlBase);
	}
	/**
	 * 
	 * @return Query SQL per recuperare i record con cui compilare la tabella
	 */
	public String getSqlDatiQuery() {
		String querySql = "";
		String sOrderBy = getFiltroOrderBy();
		
		String aliasRowNumber = "DT_RowId"; // DT_RowId stringa riservata in DataTables: il valore ricavato dal DB viene inserito come id del <tr> associato al record + progressivo (Es. DT_RowId_01)
		
		if ( sOrderBy.isEmpty() || !isRowNumber() )
		{	// Senza alcun ordinamento e' impossibile usare la rowNumber quindi viene a meno anche la paginazione
			String orderByClause = sOrderBy.isEmpty() ? "" : " ORDER BY " + sOrderBy;
			querySql =
					" SELECT * \n " +
					" FROM (" + getSqlBase() + ") as tabella \n " +
					" WHERE 1=1 \n" + getFiltroRicerca() + "\n " +
					orderByClause + "\n";
		} else {
			querySql = 
				" SELECT * FROM ( \n"+
				" 	SELECT ROW_NUMBER() OVER( ORDER BY "+sOrderBy+" ) AS "+aliasRowNumber+ ", * \n" +
				" 	FROM (" + getSqlBase() + ") as tabella \n" +
				" WHERE 1=1 " + getFiltroRicerca() + "\n" +
				" ) AS tabella_con_righe \n" + 
				" WHERE 1=1 \n" + getFiltroPaginazione(aliasRowNumber);
		}

		return querySql;
	}

	/**
	 * 
	 * @return
	 */
	public String getSqlDatiExtra()
	{
		String sql = "SELECT \n" +
			"  (SELECT COUNT(*) FROM ( " + getSqlBase() +" ) as tabella ) AS iTotalRecords \n" +
			", (SELECT COUNT(*) FROM ( " + getSqlBase() +" ) as tabella WHERE 1=1 " + getFiltroRicerca() +" ) AS iTotalDisplayRecords \n ";
		return sql;
	}
		
	/**
	 * Restituisce una stringa pronta da usare dopo la clausola order by nella query sql  
	 * @return String nella forma "nomeColonna ASC/DESC , nomeColonna ASC/DESC , ... "
	 */
	public String getFiltroOrderBy() {
		String retVal = "";
		String sqlValidColName = "(^[a-z\\[A-Z_][a-zA-Z_$@#0-9-\\]]*$)|(^[\\[][a-z A-Z_$@#0-9-]*[\\]]$)";
		if (listaColonne.get(0).matches(sqlValidColName)) // match con un identificativo valido sql
		{
			Map<String, String> mapColonnaVerso = getVersoColonneNamed();
			for( String colonna : mapColonnaVerso.keySet() ) {
				if (colonna.isEmpty())
					continue; 
				
				if ( !retVal.isEmpty() )
					retVal += ", ";

				retVal += colonna + " " + mapColonnaVerso.get(colonna); // nomeColonna versoOrdinamento
			}
		} else { // Quando l'ordinamento e' per indice invece che per nome, il match sopra fallisce sempre perche' 
				//  il nome di una colonna sql non puo' iniziare per un numero. Quindi esegue questo ramo.
			
			Map<Integer, String> mapIdxColonnaVerso = getVersoColonneIndexed();
			for( Integer idxCol : mapIdxColonnaVerso.keySet() ) {
				if ( !retVal.isEmpty() )
					retVal += ", ";
				
				retVal += (1+idxCol) + " " + mapIdxColonnaVerso.get(idxCol); // indiceColonna versoOrdinamento NB. In sql la numerazione delle colonne parte da 1
			}
		}
		
		return retVal;
	}
	
	/**
	 * Recupera il verso d'ordinamento delle colonne della tabella
	 * @return Mappa con le coppie [ nomeColonna -> versoOrdinamento ]
	 */
	public Map<String, String> getVersoColonneNamed() {
		return getVersoColonne(new LinkedHashMap<String, String>(), true);
	}
	/**
	 * Recupera il verso d'ordinamento delle colonne della tabella
	 * @return Mappa con le coppie [ nomeColonna -> versoOrdinamento ]
	 */
	public Map<Integer, String> getVersoColonneIndexed() {
		return getVersoColonne(new LinkedHashMap<Integer, String>(), false);
	}
	/**
	 * 
	 * @param mapToPopulate
	 * @param named
	 * @return
	 */
	@SuppressWarnings( "unchecked" )
	private <T> Map<T, String> getVersoColonne(Map<T, String> mapToPopulate, boolean named) {
		if ( iSortingCols != null ) {
			for ( int i = 0 ; i < Integer.valueOf( iSortingCols ) ; i++ ) {
				String versoOrdinamentoIEsima 		=	parametriRequest.get("sSortDir_" + i).toUpperCase(); // puo' essere ASC o DESC
				Integer indiceIEsimaColonnaDaOrdinare 	=	Integer.parseInt( parametriRequest.get("iSortCol_" + i) );
				
				if (named) {
					// NB: iSortCol_[0, 1, 2...] => [I, II, III...] numera i criteri di ordinamento: il 1o criterio per cui ordinare e' la colonna I, il 2o e' la colonna II...   
					// Recupera il nome associato alle colonne attraverso mDataProp_xx piu' l'indice di colonna recuperato da iSortCol 
					String nomeIEsimaColonnaDaOrdinare 	= listaColonne.get( indiceIEsimaColonnaDaOrdinare );	//parametriRequest.get("mDataProp_" + indiceIEsimaColonnaDaOrdinare); 
					mapToPopulate.put((T)nomeIEsimaColonnaDaOrdinare, versoOrdinamentoIEsima);
					
				} else {
					mapToPopulate.put((T)indiceIEsimaColonnaDaOrdinare, versoOrdinamentoIEsima);
					
				}
			}
		}
		return mapToPopulate;
	}
	
	
	/**
	 * Assembla il filtro di ricerca solo per le colonne per cui è richiesta
	 * @return Una stringa nella forma " AND ( xxx like '%abc%' OR yyy  like '%abc%' OR ... ) " da appendere ad una clausola WHERE esistente 
	 */
	public String getFiltroRicerca() {
		String filtro = "";
		
		if ( !sSearch.isEmpty() ) {
			for ( int i = 0 ; i < listaColonne.size() ; i++ ) {
				String colonnaI = listaColonne.get(i);
				// verifica che per tale colonna il filtro sia consentito
				if (BooleanUtils.toBoolean(parametriRequest.get("bSearchable_" + i)) && !colonnaI.isEmpty()) {
					filtro += (filtro.isEmpty()) ? (" AND ( ") : (" OR ");
					filtro += colonnaI + " LIKE " + " '%"+ sSearch.replaceAll( "'", "''" ) +"%' " ;
				}
			}
			
			if ( !filtro.isEmpty() )
				filtro += " ) ";
		}
		
		return filtro;
	}
	

	/**
	 * Assembla la stringa per il filtro dei paginazione 
	 * @param alisRowNumber Il nome dell'alias assegnato nella query alla funzione sql rowNumber()
	 * @return Stringa nella forma " AND 'aliasRowNumber' BETWEEN 'indicePrimoElemPagina' AND 'indiceUltimoElemPagina' "
	 */
	public String getFiltroPaginazione(String alisRowNumber) {
		String retVal = "";
		
		if ( iDisplayStart != null  &&  iDisplayLength != null && !iDisplayLength.equals( "-1" )) {
			int iIndicePrimoElem 	=	Integer.valueOf(iDisplayStart);
			int iDimPagina 			=	Integer.valueOf(iDisplayLength);
			
			retVal = " AND "+alisRowNumber+" BETWEEN "+(iIndicePrimoElem + 1)+" AND "+ (iDimPagina + iIndicePrimoElem);
		}

		return retVal;
	}
	
	protected void init(Map<String, String> parametriRequest, String sqlBase) {
		this.sqlBase = sqlBase;
		this.parametriRequest = parametriRequest;
		
		iColumns = Integer.valueOf(parametriRequest.get("iColumns"));
		sSearch = StringUtils.defaultString(parametriRequest.get("sSearch"));
		iSortingCols = parametriRequest.get( "iSortingCols" );
		iDisplayLength = parametriRequest.get( "iDisplayLength" );
		iDisplayStart = parametriRequest.get( "iDisplayStart" );
		rowNumber = BooleanUtils.toBoolean(parametriRequest.get("rowNumber"));		
		listaColonne = new ArrayList<String>();
		
		parseParametri();
	}

	protected void parseParametri() {
		Set<String> parametri = parametriRequest.keySet();
		String[] arrayColonneTemp = new String[50];
		
		for( String param : parametri ) {
			String nomeCol = parametriRequest.get(param);
			
			if (param.startsWith("mDataProp_")) {
				int indexCol = Integer.valueOf( param.substring("mDataProp_".length()) );
				arrayColonneTemp[indexCol] = nomeCol;

				if (arrayColonneTemp[arrayColonneTemp.length-1] != null)
					arrayColonneTemp = Arrays.copyOf(arrayColonneTemp, arrayColonneTemp.length*2);
			}
		}
		listaColonne = Arrays.asList(arrayColonneTemp);
	}
	

	public String getSqlBase() {
		return sqlBase;
	}
	public void setSqlBase( String sqlBase ) {
		this.sqlBase = sqlBase;
	}
	public Map<String, String> getParametriRequest() {
		return parametriRequest;
	}
	public void setParametriRequest( Map<String, String> parametriRequest ) {
		this.parametriRequest = parametriRequest;
	}
	public boolean isRowNumber() {
		return rowNumber;
	}
	public void setRowNumber( boolean rowNumber ) {
		this.rowNumber = rowNumber;
	}
	public List<String> getListaColonne() {
		return listaColonne;
	}
	public void setListaColonne( List<String> listaColonne ) {
		this.listaColonne = listaColonne;
	}
	public String getIDisplayStart() {
		return iDisplayStart;
	}
	public void setIDisplayStart( String iDisplayStart ) {
		this.iDisplayStart = iDisplayStart;
	}
	public String getIDisplayLength() {
		return iDisplayLength;
	}
	public void setIDisplayLength( String iDisplayLength ) {
		this.iDisplayLength = iDisplayLength;
	}
	public String getISortingCols() {
		return iSortingCols;
	}
	public void setISortingCols( String iSortingCols ) {
		this.iSortingCols = iSortingCols;
	}
	public String getSSearch() {
		return sSearch;
	}
	public void setSSearch( String sSearch ) {
		this.sSearch = sSearch;
	}
	public Integer getIColumns() {
		return iColumns;
	}
	public void setIColumns( Integer iColumns ) {
		this.iColumns = iColumns;
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
			for (int i = 0; i < listaColonne.size() ; i++) 
			{
				if ( parametriRequest.get("bSearchable_"+i).equalsIgnoreCase("true") )
					result.put( listaColonne.get(i), Boolean.valueOf(parametriRequest.get("bSearchable_"+i)) );
			}
		}
		
		return result;
		
	}
	
	
	/**/
	
	@Override
	public String toString()
	{
		String s = "--********************************************\n";
		s += "-- *** SqlDatiQuery:\n" + getSqlDatiQuery();
		s += "\n\n-- *** SqlDatiAggiuntivi:\n" + getSqlDatiExtra();
		s += "\n********************************************\n";
		return s;
	}
	
}
