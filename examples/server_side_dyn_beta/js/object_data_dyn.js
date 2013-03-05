$(document).ready(function() {
	var oTable ,
		idTable = 'example' , // se si cambia occhio ai css poi
		customToolbarClass = 'custom_toolbar' ,
	
		aSelected = [] ,					// mantiene in memoria gli id delle righe selezionionate nelle varie pagine 
		mantieniSelezione = false , 		// abilita il mantenimento in memoria delle righe selezionate in ogni pagina 
		arrayTitoliColonne = new Array(); 		// array che conterra' le intestazioni della tabella 
		

	
	oTable = $('#'+idTable).dataTable( {
		"sDom": '<"'+ customToolbarClass +'"><"clear">rt', // Definisce la struttura della DT e viene aggiunto ad essa anche un div con class="custom_toolbar" e uno con class="clear"
		
		"bSort": true, // !! Sempre true se e' attiva la paginazione (e se si sfrutta ROW_NUMBER() ). Agire invece sui vari ["bSortable": false] per ogni colonna.
		
		"bProcessing": true ,
		"bPaginate": false , // cambiare in accordo anche il parametro row_number nella fnServerParams_basic 
		"sScrollY": "340px" , // manomette l'html della table: sparisce il contenuto dei 'TH'
//       "bScrollCollapse": true ,
		"bStateSave": false , // Sfrutta i cookies per mantenere lo stato della pagina (ordinamento scelto, chiave di ricerca inserita... ) 
//	 	"bLengthChange": true , 
		
		"bServerSide": true ,
		"sAjaxSource": sAjaxSource ,
		"sServerMethod": "POST" ,
		
		"aaSorting": [ [0,'asc'] ] , // Ordinamento secondo la colonna "n" verso "asc / desc". Si puo' aggiungere un 
		 // ulteriore criterio di ordinamento aggiungendo un altro array simile al primo 
		 // In caso di row_number nella query e' tassativo configurare almeno un criterio
		 // di ordinamento per una colonna anche nel caso in cui si impostera' che nessuna
		 // colonna sia in seguito  riordinabile.
		
		"fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			
            if ( mantieniSelezione == true && $.inArray(aData.DT_RowId, aSelected) !== -1 ) {
            	$(nRow).addClass('row_selected');
            }
        } ,


        // Traduzione voci 
		"oLanguage":  { "sUrl": "./js/object_data_localization-it.txt" } ,  // percorso file per localizzazione lingua
		
		// Dati custom da inviare al jsp nella chiamata ajax come parametri aggiuntivi della request 
		"fnServerParams": function ( aoData ) {
			
			fnServerParams(aoData); // sull'omoniomo (o derivato) file esterno 
		} ,
        		
        // funzione eseguita non appena la datatable abbia completato l'inizializzazione
        "fnInitComplete": function (oSettings, json) {
        	
        	// bug (o funzionalita' che sia): importando la localizzazione da file questa tutte le personalizzazioni
        	// sulla struttura della datatable devono essere eseguite da qui , fuori di qui non ha efficacia

//        	arrayTitoliColonne = populateColumnsTitleArray(oTable);
        	arrayTitoliColonne = dtFn.fnGetMDataColumns(oTable);
//        	console.log(arrayTitoliColonne);
        	configuraCustomToobar('div.'+customToolbarClass, idTable, oTable, arrayTitoliColonne);
        	dtFn.fnDTDisableInstantSearch(oTable, idTable);
        	$('#'+idTable+'_paginate').disableTextSelect(); // Disabilita la selezione per i pulsanti di cambio pagina (per il problema del doppio click che seleziona tutto)
        	dtFn.addClickSelectionEvent( oTable, false, mantieniSelezione, aSelected );
        	oTable.fnAdjustColumnSizing();
        	
        	var json = oSettings.jqXHR.responseText;
        	var $json = $.parseJSON(json);
//        	console.log($json.nomiColonne);
        	
        	dtFn.semiDynamicTHsTitles($json.nomiColonne, idTable, oSettings);
        	
//    		console.log( dtFn.fnGetMDataColumns(oTable) );
        } // -- Chiude fnInitComplete()
		
		
	} ); // -- Chiusura datatable() 

	
} ); // chiusura $(document).ready()  





