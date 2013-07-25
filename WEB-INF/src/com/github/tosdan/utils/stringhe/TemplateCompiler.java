package com.github.tosdan.utils.stringhe;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

/**
 * 
 * @author Daniele
 * @version 1.1.2-b2013-07-25
 */
public class TemplateCompiler
{
	/*************************************************************************/
	/*******************              Campi            ***********************/
	/*************************************************************************/
	
	/**
	 * Oggetto per effettuare verifica di coerenza dei tipi sui valori che dovranno esser sostituiti ai parametri del template.
	 */
	private MapFormatTypeValidator validator;
	/**
	 * Mappa contenente i valori sostitutivi con cui rimpiazzare i parametri nel template
	 */
	private Map<String, Object> substitutesValuesMap;
	/**
	 * Nome del template da compilare
	 */
	private String templateName;
	/**
	 * Percorso completo della cartella contenente il/i file repository dei template/s. Indispensabile se si vuole mantenere percorsi relativi nel file indice dei repositories
	 */
	private String repositoryFolderPath;
	/**
	 * Mappa con le associazioni tra i nomi dei templates e i relativi file-repository da cui recuperarli effettivamente
	 */
	private Map<String, String> repositoryIndex;
	/**
	 * InputStream conenente la stringa in cui e'/sono memorizzato/i il/i template/s
	 */
	private InputStream is;
	/**
	 * Oggetto che sia in grado di interpretare i dati forniti al fine di trovare il template contenutovi.
	 */
	private TemplatePicker picker;
	private String templateAsString;
	
	/*************************************************************************/
	/*******************          Costruttori          ***********************/
	/*************************************************************************/
	
	/**
	 * 
	 * @param repositoryIndex Mappa con le associazioni tra i nomi dei templates e i relativi file-repository da cui recuperarli effettivamente.
	 * @param repositoryFolderPath Percorso completo della cartella contenente il/i file repository dei template/s. Indispensabile se si vuole mantenere percorsi relativi nel file indice dei repositories.
	 * @param templateName Nome del template da compilare.
	 * @param substitutesValuesMap Mappa contenente i valori sostitutivi con cui rimpiazzare i parametri nel template.
	 */
	public TemplateCompiler( Map<String, String> repositoryIndex, String repositoryFolderPath, String templateName, Map<String, Object> substitutesValuesMap ) {
		this( null, null, repositoryIndex, repositoryFolderPath, templateName, substitutesValuesMap );
	}
	
	/**
	 * 
	 * @param is InputStream conenente la stringa in cui e'/sono memorizzato/i i/il template/s.
	 * @param templateName Nome del template da compilare.
	 * @param substitutesValuesMap Mappa contenente i valori sostitutivi con cui rimpiazzare i parametri nel template.
	 */
	public TemplateCompiler( InputStream is, String templateName, Map<String, Object> substitutesValuesMap ) {
		this( null, is, null, null, templateName, substitutesValuesMap );
	}
	
	/**
	 * 
	 * @param templateAsString Stringa conenente uno o più template/s.
	 * @param templateName Nome del template da compilare.
	 * @param substitutesValuesMap Mappa contenente i valori sostitutivi con cui rimpiazzare i parametri nel template.
	 */
	public TemplateCompiler( String templateAsString, String templateName, Map<String, Object> substitutesValuesMap ) {
		this( templateAsString, null, null, null, templateName, substitutesValuesMap );
	}
	
	/**
	 * 
	 * @param templateAsString Stringa conenente uno o più template/s.
	 * @param is InputStream conenente la stringa in cui e'/sono memorizzato/i il/i template/s.
	 * @param repositoryIndex Mappa con le associazioni tra i nomi dei templates e i relativi file-repository da cui recuperarli effettivamente.
	 * @param repositoryFolderPath Percorso completo della cartella contenente i/il file repository dei template/s. Indispensabile se si vuole mantenere percorsi relativi nel file indice dei repositories.
	 * @param templateName Nome del template da compilare.
	 * @param substitutesValuesMap Mappa contenente i valori sostitutivi con cui rimpiazzare i parametri nel template.
	 */
	private TemplateCompiler( String templateAsString
							, InputStream is
							, Map<String, String> repositoryIndex
							, String repositoryFolderPath
							, String templateName
							, Map<String, Object> substitutesValuesMap ) 
	{
		this.templateAsString = templateAsString;
		this.is = is;
		this.repositoryIndex = repositoryIndex;
		this.repositoryFolderPath = repositoryFolderPath;
		this.templateName = templateName;
		this.substitutesValuesMap = substitutesValuesMap;
		this.picker = new TemplatePickerYaml();
		
	}
	
	/*************************************************************************/
	/*******************            Metodi             ***********************/
	/*************************************************************************/
	
	/**
	 * Compila il template associato al templateName fornito
	 * @return Stringa corrispondente al contenuto del template con parametri rimpiazzati dai valori passati nella mappa con le associazioni parametro->valore
	 * @throws TemplateCompilerException Lanciata qualora il contenuto della sorgente dati/repository-file sia vuoto o qualora in esso non sia presente il template specificato
	 */
	public String compile() throws TemplateCompilerException {
		return compile( new MapFormat( substitutesValuesMap, validator ) );
	}
	
	/**
	 * Compila il template associato al templateName fornito
	 * @param mapFormat Oggetto {@link MapFormat} custom. 
	 * @return Stringa corrispondente al contenuto del template con parametri rimpiazzati dai valori passati nella mappa con le associazioni parametro->valore
	 * @throws TemplateCompilerException Lanciata qualora il contenuto della sorgente dati/repository-file sia vuoto o qualora in esso non sia presente il template specificato
	 */
	public String compile(MapFormat mapFormat) throws TemplateCompilerException  {
		
		// Percorso dell'eventuale file che contiene i/il template/s
		String templatesRepositoryFilename = this.templateFileLookup();
		// Contenuto del file template
		String sourceContent = this.templateAsString == null 
							? this.readSourceContent(templatesRepositoryFilename)
							: this.templateAsString;
		
		if (sourceContent == null || sourceContent.isEmpty()) {
			String msg = (this.repositoryIndex == null) // se e' stato passato direttamente un InputStream 'repositoryIndex' e' nullo
					? "Nessun contenuto valido per il template '" + this.templateName + "' nella sorgente passata passata." 
					: "Nessun contenuto valido per il template '" + this.templateName + "' nel file repository dei templates: '" + templatesRepositoryFilename + "'.";			
			throw new TemplateCompilerException( msg );
			
		}
		
		String template = this.templateLookupByName( sourceContent );
		
		if ( template == null || template.equals("") ) {
			String msg = (this.repositoryIndex == null)  // se e' stato passato direttamente un InputStream 'repositoryIndex' e' nullo
					? "Associazione vuota o mancante per il template '" + this.templateName + "' nella sorgente passata."
					: "Associazione vuota o mancante per il template '" + this.templateName + "' nel file repository dei templates: '" + templatesRepositoryFilename + "'.";
			throw new TemplateCompilerException( msg );
			
		}
		
		return mapFormat.format( template );
	}
	
	/**
	 * 
	 * @param validator Oggetto per effettuare verifica di coerenza dei tipi sui valori che dovranno esser sostituiti ai parametri del template.
	 */
	public void setValidator( MapFormatTypeValidator validator ) {
		this.validator = validator;
	}

	/**
	 * 
	 * @param picker Oggetto che sia in grado di interpretare i dati forniti al fine di trovare il template contenutovi.
	 */
	public void setPicker( TemplatePicker picker ) {
		this.picker = picker;
	}

	/**
	 * Recupera il contenuto associato al nome di template fornito.
	 * @param sourceContent Dati contenuti nel file templates-repository
	 * @return Stringa rappresentante il template
	 */
	private String templateLookupByName (String sourceContent) {
		return this.picker.pick( this.picker.normalize(sourceContent), this.templateName );
		// String templateType = "yaml.Template";
		// TemplatePicker tp = TemplatePickerFactory(templateType).NewInstance(); 
		// return otf.pick(sourceContent, this.templateName);
	}
	
	/**
	 * Recupera il contenuto del file repository.
	 * @param templatesRepositoryFilename Percorso assoluto del file repository contenente il template. Null se viene passato un InputStream invece di un file indice dei repositories. 
	 * @return Una stringa contenente uno o piu' templates.
	 * @throws TemplateCompilerException Lanciata qualora il file-repository non esista o qualora leggere la sorgente dati/file-repository
	 */
	private String readSourceContent(String templatesRepositoryFilename) throws TemplateCompilerException {
		
		String sourceContent = null;
		
		if (templatesRepositoryFilename != null) {
			try {
				this.is = new FileInputStream( templatesRepositoryFilename );
				
			} catch ( FileNotFoundException e1 ) {				
				throw new TemplateCompilerException( "Errore I/O: file '"+ templatesRepositoryFilename+"',  non trovato.", e1 );
				
			}
		}
	
		try {
			sourceContent = IOUtils.toString( this.is );
			
		} catch ( IOException e ) {
			String msg = (templatesRepositoryFilename == null) 
					? "Impossibile leggere la sorgente dati." 
					: "Errore nel tentativo di lettura del file '"+ templatesRepositoryFilename+"'";
			throw new TemplateCompilerException( msg, e );
			
		}
		
		return sourceContent;
		
	}
	
	/**
	 * Se presente un indice dei file template-repository tenta il recupero dall'indice del nome del file contenente i/il template/s.
	 * @return Percorso assoluto del file repository contenente i/il template/s
	 * @throws TemplateCompilerException Lanciata qualora l'indice dei repository non contenga un'associazione valida per il nome del template passato.
	 */
	private String templateFileLookup()  throws TemplateCompilerException {
		
		String filenameAbsolutePath = null;
		
		if (this.repositoryIndex != null) {
			filenameAbsolutePath = this.repositoryIndex.get( this.templateName ); 
		
			// Se e' stato passato un file indice, deve esistere l'associazione per il file del templateName passato
			if (filenameAbsolutePath == null || filenameAbsolutePath.equals("") ) {
				throw new TemplateCompilerException( "Nessun file repository associato al template '" + this.templateName + "' nell'indice repository passato.");
			}
		}
		
		// Se l'indice contiene i percorsi relativi dei file template e' necessario aggiungere anche il percorso completo alla cartella che li contiene.
		// Se non viene passato il percorso completo a tale cartella si assume che nell'indice ci siano percorsi assoluti.
		if (this.repositoryFolderPath != null)
			filenameAbsolutePath = this.repositoryFolderPath + filenameAbsolutePath;
		
		return filenameAbsolutePath;
		
	}
	
	
	
	/*************************************************************************/
	/*******************   Convenience Static Method   ***********************/
	/*************************************************************************/
	
	/**
	 * Compila il template associato al templateName fornito. Il template e' cercato nell'InputStream fornito.
	 * @param is InputStream conenente la stringa in cui e'/sono memorizzato/i i/il template/s
	 * @param templateName Nome del template da compilare
	 * @param substitutesValuesMap Mappa contenente i valori sostitutivi con cui rimpiazzare i parametri nel template
	 * @return Stringa corrispondente al contenuto del template con parametri rimpiazzati dai valori passati nella mappa con le associazioni parametro->valore
	 * @throws TemplateCompilerException 
	 */
	public static String compile(InputStream is, String templateName, Map<String, Object> substitutesValuesMap) throws TemplateCompilerException {
		return compile( is, templateName, substitutesValuesMap, null, null );
		
	}
	
	/**
	 * Compila il template associato al templateName fornito. Il template e' cercato nell'InputStream fornito.
	 * @param is InputStream conenente la stringa in cui e'/sono memorizzato/i i/il template/s
	 * @param templateName Nome del template da compilare
	 * @param substitutesValuesMap Mappa contenente i valori sostitutivi con cui rimpiazzare i parametri nel template
	 * @param picker Oggetto che sia in grado di interpretare i dati forniti al fine di trovare il template contenutovi. 
	 * @return Stringa corrispondente al contenuto del template con parametri rimpiazzati dai valori passati nella mappa con le associazioni parametro->valore
	 * @throws TemplateCompilerException 
	 */
	public static String compile(InputStream is, String templateName, Map<String, Object> substitutesValuesMap, TemplatePicker picker) throws TemplateCompilerException {
		return compile( is, templateName, substitutesValuesMap, picker, null );
		
	}
	
	/**
	 * Compila il template associato al templateName fornito. Il template e' cercato nell'InputStream fornito.
	 * @param is InputStream conenente la stringa in cui e'/sono memorizzato/i i/il template/s
	 * @param templateName Nome del template da compilare
	 * @param substitutesValuesMap Mappa contenente i valori sostitutivi con cui rimpiazzare i parametri nel template
	 * @param validator Oggetto per effettuare verifica di coerenza dei tipi sui valori che dovranno esser sostituiti ai parametri del template.
	 * @return Stringa corrispondente al contenuto del template con parametri rimpiazzati dai valori passati nella mappa con le associazioni parametro->valore
	 * @throws TemplateCompilerException 
	 */
	public static String compile(InputStream is, String templateName, Map<String, Object> substitutesValuesMap, MapFormatTypeValidator validator) throws TemplateCompilerException {
		return compile( is, templateName, substitutesValuesMap, null, validator );
		
	}

	
	/**
	 * Compila il template associato al templateName fornito. Il template e' cercato nell'InputStream fornito.
	 * @param is InputStream conenente la stringa in cui e'/sono memorizzato/i i/il template/s.
	 * @param templateName Nome del template da compilare.
	 * @param substitutesValuesMap Mappa contenente i valori sostitutivi con cui rimpiazzare i parametri nel template.
	 * @param picker Oggetto che sia in grado di interpretare i dati forniti al fine di trovare il template contenutovi. 
	 * @param validator Oggetto per effettuare verifica di coerenza dei tipi sui valori che dovranno esser sostituiti ai parametri del template.
	 * @return Stringa corrispondente al contenuto del template con parametri rimpiazzati dai valori passati nella mappa con le associazioni parametro->valore.
	 * @throws TemplateCompilerException
	 */
	public static String compile(InputStream is, String templateName, Map<String, Object> substitutesValuesMap, TemplatePicker picker, MapFormatTypeValidator validator) throws TemplateCompilerException {
		TemplateCompiler tc = new TemplateCompiler( is, templateName, substitutesValuesMap );
		
		if (validator != null)
			tc.setValidator( validator );
		if (picker != null)
			tc.setPicker( picker );


		return tc.compile();
		
	}
	

	/**
	 * Compila il template associato al templateName fornito. Il template e' cercato nell'InputStream fornito.
	 * @param templateAsString Stringa contenente uno o più template/s
	 * @param templateName Nome del template da compilare
	 * @param substitutesValuesMap Mappa contenente i valori sostitutivi con cui rimpiazzare i parametri nel template
	 * @return Stringa corrispondente al contenuto del template con parametri rimpiazzati dai valori passati nella mappa con le associazioni parametro->valore
	 * @throws TemplateCompilerException 
	 */
	public static String compile(String templateAsString, String templateName, Map<String, Object> substitutesValuesMap) throws TemplateCompilerException {
		return compile( templateAsString, templateName, substitutesValuesMap, null, null );
		
	}
	
	/**
	 * Compila il template associato al templateName fornito. Il template e' cercato nell'InputStream fornito.
	 * @param templateAsString Stringa contenente uno o più template/s
	 * @param templateName Nome del template da compilare
	 * @param substitutesValuesMap Mappa contenente i valori sostitutivi con cui rimpiazzare i parametri nel template
	 * @param picker Oggetto che sia in grado di interpretare i dati forniti al fine di trovare il template contenutovi. 
	 * @return Stringa corrispondente al contenuto del template con parametri rimpiazzati dai valori passati nella mappa con le associazioni parametro->valore
	 * @throws TemplateCompilerException 
	 */
	public static String compile(String templateAsString, String templateName, Map<String, Object> substitutesValuesMap, TemplatePicker picker) throws TemplateCompilerException {
		return compile( templateAsString, templateName, substitutesValuesMap, picker, null );
		
	}
	
	/**
	 * Compila il template associato al templateName fornito. Il template e' cercato nell'InputStream fornito.
	 * @param templateAsString Stringa contenente uno o più template/s
	 * @param templateName Nome del template da compilare
	 * @param substitutesValuesMap Mappa contenente i valori sostitutivi con cui rimpiazzare i parametri nel template
	 * @param validator Oggetto per effettuare verifica di coerenza dei tipi sui valori che dovranno esser sostituiti ai parametri del template.
	 * @return Stringa corrispondente al contenuto del template con parametri rimpiazzati dai valori passati nella mappa con le associazioni parametro->valore
	 * @throws TemplateCompilerException 
	 */
	public static String compile(String templateAsString, String templateName, Map<String, Object> substitutesValuesMap, MapFormatTypeValidator validator) throws TemplateCompilerException {
		return compile( templateAsString, templateName, substitutesValuesMap, null, validator );
		
	}

	
	/**
	 * Compila il template associato al templateName fornito. Il template e' cercato nell'InputStream fornito.
	 * @param templateAsString Stringa contenente uno o più template/s
	 * @param templateName Nome del template da compilare.
	 * @param substitutesValuesMap Mappa contenente i valori sostitutivi con cui rimpiazzare i parametri nel template.
	 * @param picker Oggetto che sia in grado di interpretare i dati forniti al fine di trovare il template contenutovi. 
	 * @param validator Oggetto per effettuare verifica di coerenza dei tipi sui valori che dovranno esser sostituiti ai parametri del template.
	 * @return Stringa corrispondente al contenuto del template con parametri rimpiazzati dai valori passati nella mappa con le associazioni parametro->valore.
	 * @throws TemplateCompilerException
	 */
	public static String compile(String templateAsString, String templateName, Map<String, Object> substitutesValuesMap, TemplatePicker picker, MapFormatTypeValidator validator) throws TemplateCompilerException {
		TemplateCompiler tc = new TemplateCompiler( templateAsString, templateName, substitutesValuesMap );
		
		if (validator != null)
			tc.setValidator( validator );
		if (picker != null)
			tc.setPicker( picker );
		
		
		return tc.compile();
		
	}

	
	/**
	 * Compila il template associato al templateName fornito. Il template e' cercato attraverso un indice dei file template-repository.
	 * @param repositoryIndex Mappa con le associazioni tra i nomi dei templates e i relativi file-repository da cui recuperarli effettivamente.
	 * @param repositoryFolderPath Percorso completo della cartella contenente i/il file repository dei template/s. Indispensabile se si vuole mantenere percorsi relativi nel file indice dei repositories.
	 * @param templateName Nome del template da compilare.
	 * @param substitutesValuesMap Mappa contenente i valori sostitutivi con cui rimpiazzare i parametri nel template.
	 * @return Stringa corrispondente al contenuto del template con parametri rimpiazzati dai valori passati nella mappa con le associazioni parametro->valore.
	 * @throws TemplateCompilerException
	 */
	public static String compile(Map<String, String> repositoryIndex, String repositoryFolderPath, String templateName, Map<String, Object> substitutesValuesMap) throws TemplateCompilerException {
		return compile( repositoryIndex, repositoryFolderPath, templateName, substitutesValuesMap, null, null );
		
	}
	
	/**
	 * Compila il template associato al templateName fornito. Il template e' cercato attraverso un indice dei file template-repository.
	 * @param repositoryIndex Mappa con le associazioni tra i nomi dei templates e i relativi file-repository da cui recuperarli effettivamente.
	 * @param repositoryFolderPath Percorso completo della cartella contenente i/il file repository dei template/s. Indispensabile se si vuole mantenere percorsi relativi nel file indice dei repositories.
	 * @param templateName Nome del template da compilare.
	 * @param substitutesValuesMap Mappa contenente i valori sostitutivi con cui rimpiazzare i parametri nel template.
	 * @param validator Oggetto che sia in grado di interpretare i dati forniti al fine di trovare il template contenutovi.
	 * @return Stringa corrispondente al contenuto del template con parametri rimpiazzati dai valori passati nella mappa con le associazioni parametro->valore.
	 * @throws TemplateCompilerException
	 */
	public static String compile(Map<String, String> repositoryIndex, String repositoryFolderPath, String templateName, Map<String, Object> substitutesValuesMap, MapFormatTypeValidator validator) throws TemplateCompilerException {
		return compile( repositoryIndex, repositoryFolderPath, templateName, substitutesValuesMap, null, validator );
		
	}
	
	/**
	 * Compila il template associato al templateName fornito. Il template e' cercato attraverso un indice dei file template-repository.
	 * @param repositoryIndex Mappa con le associazioni tra i nomi dei templates e i relativi file-repository da cui recuperarli effettivamente.
	 * @param repositoryFolderPath Percorso completo della cartella contenente i/il file repository dei template/s. Indispensabile se si vuole mantenere percorsi relativi nel file indice dei repositories.
	 * @param templateName Nome del template da compilare.
	 * @param substitutesValuesMap Mappa contenente i valori sostitutivi con cui rimpiazzare i parametri nel template.
	 * @param picker Oggetto che sia in grado di interpretare i dati forniti al fine di trovare il template contenutovi. 
	 * @return Stringa corrispondente al contenuto del template con parametri rimpiazzati dai valori passati nella mappa con le associazioni parametro->valore.
	 * @throws TemplateCompilerException
	 */
	public static String compile(Map<String, String> repositoryIndex, String repositoryFolderPath, String templateName, Map<String, Object> substitutesValuesMap, TemplatePicker picker) throws TemplateCompilerException {
		return compile( repositoryIndex, repositoryFolderPath, templateName, substitutesValuesMap, picker, null );
		
	}

	/**
	 * Compila il template associato al templateName fornito. Il template e' cercato attraverso un indice dei file template-repository.
	 * @param repositoryIndex Mappa con le associazioni tra i nomi dei templates e i relativi file-repository da cui recuperarli effettivamente.
	 * @param repositoryFolderPath Percorso completo della cartella contenente i/il file repository dei template/s. Indispensabile se si vuole mantenere percorsi relativi nel file indice dei repositories.
	 * @param templateName Nome del template da compilare.
	 * @param substitutesValuesMap Mappa contenente i valori sostitutivi con cui rimpiazzare i parametri nel template.
	 * @param picker Oggetto che sia in grado di interpretare i dati forniti al fine di trovare il template contenutovi. 
	 * @param validator Oggetto per effettuare verifica di coerenza dei tipi sui valori che dovranno esser sostituiti ai parametri del template.
	 * @return Stringa corrispondente al contenuto del template con parametri rimpiazzati dai valori passati nella mappa con le associazioni parametro->valore.
	 * @throws TemplateCompilerException
	 */
	public static String compile(Map<String, String> repositoryIndex, String repositoryFolderPath, String templateName, Map<String, Object> substitutesValuesMap, TemplatePicker picker, MapFormatTypeValidator validator) throws TemplateCompilerException {
		TemplateCompiler tc = new TemplateCompiler( repositoryIndex, repositoryFolderPath, templateName, substitutesValuesMap );
		
		if (validator != null)
			tc.setValidator( validator );
		if (picker != null)
			tc.setPicker( picker );


		return tc.compile();
	}
	
	
	
	
	/*************************************************************************/
	/****************************    Demo Main    ****************************/
	/*************************************************************************/
	
	/**
	 * 
	 * @param args
	 * @throws TemplateCompilerException 
	 * @throws UnsupportedEncodingException 
	 */
	public static void main( String[] args ) throws TemplateCompilerException, UnsupportedEncodingException {
		test();
	}
	
	/**
	 * 
	 * @throws TemplateCompilerException 
	 * @throws UnsupportedEncodingException 
	 */
	public static void test() throws TemplateCompilerException, UnsupportedEncodingException
	{  	// Esempio di un file contenente piu' stringhe in diverse sezioni
		String esempio = 
			 	"%[sezione1]%\n" +
				"Lorem ipsum dolor sit amet, consectetur adipiscing elit. \n" +
				"Curabitur vel purus non ante scelerisque lacinia a ac lorem.\n" +
				"Nunc ante ante, tempus ornare cursus eget, adipiscing at odio.\n" +
				"Nam a tellus id sapien commodo sodales a non velit.\n" +
				
				"%[sezione2]%\n" +
				"Pellentesque rutrum mauris eget sapien porta dapibus.\n" +
				"Phasellus consectetur dui eget augue imperdiet consectetur.\n" +
				"Donec tellus massa, dapibus a faucibus et, tempor eget eros.\n" +
				"Donec iaculis condimentum porta. \n" +
				
				"%[sezione3]%\n" +
				"Nunc ${param2} vulputate turpis ${param3} feugiat.\n" +
				"Pellentesque ${param1} fringilla eleifend arcu id rutrum.\n" +
				"Proin blandit scelerisque tempus. Pellentesque nec tincidunt elit.\n" +
				"Nullam rutrum odio ac ante ${param1} interdum.\n" +
				"Fusce '${param2}' mauris sit amet metus vulputate fermentum sit ${param1} at nulla.";
		
		InputStream is = new ByteArrayInputStream( esempio.getBytes("UTF-8") );
		
		Map<String, Object> mappa = new HashMap<String, Object>();
		mappa.put( "param1", "Value1" );
		mappa.put( "param2", "Value2" );
		mappa.put( "param3", "Value3" );
		String sorgente = "";
		String test = "";
		test = compile( is, "sezione3", mappa, new TemplatePickerSections() ); sorgente = "InputStream";
		
		/*
		try {
			FileInputStream fis = new FileInputStream( "d:/tmp/configfile.txt" );
			test = compile( fis, "sezione3", mappa ); sorgente = "File";
		} catch ( FileNotFoundException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		
		System.out.println( "Test compilazione template da '"+sorgente+"':\n\n" + test + "\n******* Linea di Debug *******\nCaratteri: " + test.length());
		
	}
}
