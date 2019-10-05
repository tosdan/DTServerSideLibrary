package com.github.tosdan.utils.sql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.tosdan.utils.stringhe.MapFormatTypeValidator;
import com.github.tosdan.utils.stringhe.MapFormatTypeValidatorSQL;
import com.github.tosdan.utils.stringhe.TemplateCompiler;
import com.github.tosdan.utils.stringhe.TemplateCompilerException;
import com.github.tosdan.utils.stringhe.TemplatePicker;

/**
 * 
 * @author Daniele
 * @version 0.1.4-b2013-09-02
 */
public class MassiveQueryCompiler
{
	/*************************************************************************/
	/*******************              Campi            ***********************/
	/*************************************************************************/
	/**
	 *  (null-tolerant)
	 */
	private List<Map<String, Object>> shiftingParamsMapsList;
	private Map<String, String> repositoryIndexMap;
	private String queriesRepoFolderPath;
	private MapFormatTypeValidator validator;
	private String templateContainerAsString;
	/**
	 * Oggetto che sappia interpretare il template al fine di trovare la query scelta.
	 */
	private TemplatePicker picker;
	
	/*************************************************************************/
	/*******************          Costruttori          ***********************/
	/*************************************************************************/
	
	/**
	 * 
	 * @param templateContainerAsString Contenuto testale del file template. Puo' contenere piu' di una query. Ogni query deve avere un identificativo distinto
	 */
	public MassiveQueryCompiler( String templateContainerAsString ) {
		this( templateContainerAsString, null );
	}
	

	/**
	 * 
	 * @param templateContainerAsString Contenuto testale del file template. Puo' contenere piu' di una query. Ogni query deve avere un identificativo distinto
	 * @param shiftingParamsMapsList Lista con con parametri per compilazione di piu' query dallo stesso template. (null-tolerant)
	 */
	public MassiveQueryCompiler( String templateContainerAsString, List<Map<String, Object>> shiftingParamsMapsList ) {
		this( null, null, shiftingParamsMapsList );
		this.templateContainerAsString = templateContainerAsString;
	}
	

	/**
	 * 
	 * @param repositoryFilesIndex Contiene le associazioni nome-query -> file-query-template. L'associazione da un id/nome di una query al file contenente il modello/template della query associata. 
	 * @param queriesRepoFolderFullPath Percorso assoluto completo della cartella (radice) contenente i template delle queries da compilare
	 */
	public MassiveQueryCompiler(Map<String, String> repositoryFilesIndex, String queriesRepoFolderFullPath ) {
		this( repositoryFilesIndex, queriesRepoFolderFullPath, null );
	}
	
	/**
	 * 
	 * @param repositoryIndexMap Contiene le associazioni nome-query -> file-query-template. L'associazione da un id/nome di una query al file contenente il modello/template della query associata. 
	 * @param queriesRepoFolderPath Percorso assoluto completo della cartella (radice) contenente i template delle queries da compilare
	 * @param shiftingParamsMapsList Lista con con parametri per compilazione di piu' query dallo stesso template. (null-tolerant)
	 */
	public MassiveQueryCompiler(Map<String, String> repositoryIndexMap, String queriesRepoFolderPath, List<Map<String, Object>> shiftingParamsMapsList) {
		this.shiftingParamsMapsList = shiftingParamsMapsList;
		this.repositoryIndexMap = repositoryIndexMap;
		this.queriesRepoFolderPath = queriesRepoFolderPath;
		// Istanzia l'oggetto di default per la validazione dei parametri rispetto ai valori effettivamente passati per evitare problemi sui Tipi
		this.validator = new MapFormatTypeValidatorSQL();
	}
	
	/*************************************************************************/
	/*******************            Metodi             ***********************/
	/*************************************************************************/
	
	/**
	 *
	 * @param nomeQueriesDaCompilare  Nomi della query da recuperare dal template e compilare
	 * @param paramsMap Mappa per la sostituzione dei parameti nelle queries da compilare
	 * @return
	 * @throws TemplateCompilerException 
	 */
	public Map<String, List<String>> getQueriesListMap(String nomeQueriesDaCompilare, Map<String, Object> paramsMap) throws TemplateCompilerException {
		return this.getQueriesListMap( new String[] {nomeQueriesDaCompilare}, paramsMap );
		
	}
	
	/**
	 *
	 * @param nomiQueriesDaCompilare Lista dei nomi delle queries da recuperare dal template e compilare
	 * @param paramsMap Mappa per la sostituzione dei parameti nelle queries da compilare
	 * @return
	 * @throws TemplateCompilerException 
	 */
	public Map<String, List<String>> getQueriesListMap(List<String> nomiQueriesDaCompilare, Map<String, Object> paramsMap) throws TemplateCompilerException {
		String[] temp = new String[1];
		return this.getQueriesListMap( nomiQueriesDaCompilare.toArray(temp), paramsMap );
		
	}
	
	/**
	 * 
	 * @param nomiQueriesDaCompilare Array dei nomi delle queries da recuperare dal template e compilare
	 * @param paramsMap Mappa per la sostituzione dei parameti nelle queries da compilare
	 * @return Mappa con chiave nome/id Query e per valore una lista di queries: se 'shiftingParamsMapsList' e' nullo la lista conteiene sempre una sola query 
	 * @throws TemplateCompilerException
	 */
	public Map<String, List<String>> getQueriesListMap(String[] nomiQueriesDaCompilare, Map<String, Object> paramsMap) throws TemplateCompilerException {

		Map<String, List<String>> queriesListMappedByName = new HashMap<String, List<String>>();
		paramsMap = (paramsMap == null) ? (new HashMap<String, Object>()) : paramsMap;

		for( int i = 0 ; i < nomiQueriesDaCompilare.length ; i++ ) {
			
			List<String> compiledQueriesList = new ArrayList<String>();
			if (this.shiftingParamsMapsList != null && !shiftingParamsMapsList.isEmpty() ) {
				Map<String, Object> tmpSingleShiftAndConstantParamsMap = null;
				for( Map<String, Object> singleShiftParamsMap : this.shiftingParamsMapsList ) {
					
					tmpSingleShiftAndConstantParamsMap = new HashMap<String, Object>();
					tmpSingleShiftAndConstantParamsMap.putAll( paramsMap );
					tmpSingleShiftAndConstantParamsMap.putAll( singleShiftParamsMap ); // sovrascrive eventuali parametri con stessa chiave presenti nella mappa dei parametri costanti
					
					compiledQueriesList.add( this.getCompiledQuery(nomiQueriesDaCompilare[i], tmpSingleShiftAndConstantParamsMap) );	
				}
				
			} else {
				compiledQueriesList.add( this.getCompiledQuery(nomiQueriesDaCompilare[i], paramsMap) );
				
			}
			
			queriesListMappedByName.put( nomiQueriesDaCompilare[i], compiledQueriesList );
		}
			
		return queriesListMappedByName;
	}

	/**
	 * 
	 * @param templateName Nome che identifica il template all'interno dei file di configurazione e all'interno del file template
	 * @param substitutesValuesMap Mappa dei valori sostitutivi per i parametri (a valore costante) 
	 * @return
	 * @throws TemplateCompilerException
	 */
	private String getCompiledQuery( String templateName, Map<String, Object> substitutesValuesMap) 
			throws TemplateCompilerException {
		
		if (this.repositoryIndexMap != null && this.queriesRepoFolderPath != null)
			return TemplateCompiler.compile( repositoryIndexMap, queriesRepoFolderPath, templateName, substitutesValuesMap, picker, validator );
		
		else if (this.templateContainerAsString != null) {
			String s = TemplateCompiler.compile( templateContainerAsString, templateName, substitutesValuesMap, picker, validator );
//			System.out.println( s );
			return s;
		}
		
		else 
			throw new IllegalArgumentException( "Errore " + this.getClass().getName() + ": repositoryIndexMap + queriesRepoFolderPath nulli o templateContainerAsString nullo. Deve esser fornita una sorgente valida per recuperare il template da compilare." );
	}

	/**
	 * 
	 * @param validator
	 */
	public void setValidator( MapFormatTypeValidator validator ) {
		this.validator = validator;
	}

	/**
	 * 
	 * @param picker Oggetto che sappia interpretare il template al fine di trovare la query scelta.
	 */
	public void setPicker( TemplatePicker picker ) {
		this.picker = picker;
	}
}
