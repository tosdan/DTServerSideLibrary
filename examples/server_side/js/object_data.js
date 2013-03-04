$(document).ready(function() {
	var oTable ,
		idTable = 'example' , // se si cambia occhio ai css poi
		customToolbarClass = 'custom_toolbar' ,
	
		aSelected = [] ,					// mantiene in memoria gli id delle righe selezionionate nelle varie pagine 
		mantieniSelezione = false , 		// abilita il mantenimento in memoria delle righe selezionate in ogni pagina 
		arrayTitoliColonne = new Array(); 		// array che conterra' le intestazioni della tabella 
		
		
	jQuery.fn.dataTableExt.oPagination.iFullNumbersShowPages = 3; // Numero di pagine direttamente clickabili (default 5)

	oTable = $('#'+idTable).dataTable( {
		"sDom": '<"'+ customToolbarClass +'"><"clear">p<"spacer">lf<"clear">rtip', // Definisce la struttura della DT e viene aggiunto ad essa anche un div con class="custom_toolbar" e uno con class="clear"
		
		
		"bSort": true, // !! Sempre true se e' attiva la paginazione (e se si sfrutta ROW_NUMBER() ). Agire invece sui vari ["bSortable": false] per ogni colonna.   

		"bProcessing": true ,
		"bPaginate": true ,
//		"sScrollY": "500px" ,
//       "bScrollCollapse": true ,
		"sPaginationType": "full_numbers" ,
		"bStateSave": true , // Sfrutta i cookies per mantenere lo stato della pagina (ordinamento scelto, chiave di ricerca inserita... ) 
//	 	"bLengthChange": false , 
//	 	"bInfo": false , 
		"aLengthMenu": [ [10, 15, 25, -1], [10, 15, 25, "Tutti"] ] ,
		
		"bServerSide": true ,
		"sAjaxSource": "../../dtreply/loadData.do?sqlName=dtexample&sqlType=query" ,
		"sServerMethod": "POST" ,
		
		"aaSorting": [ [1,'asc'] ] , // Ordinamento secondo la colonna "n" verso "asc / desc". Si puo' aggiungere un 
		 // ulteriore criterio di ordinamento aggiungendo un altro array simile al primo 
		 // In caso di row_number nella query e' tassativo configurare almeno un criterio
		 // di ordinamento per una colonna anche nel caso in cui si impostera' che nessuna
		 // colonna sia in seguito  riordinabile. 
		
		"fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			
            if ( mantieniSelezione == true && $.inArray(aData.DT_RowId, aSelected) !== -1 ) {
            	$(nRow).addClass('row_selected');
            }
            
            //console.log( 'nRow:['+ nRow+'], iDisplayIndex:['+ iDisplayIndex+'], iDisplayIndexFull:['+iDisplayIndexFull+'], aData:['+aData+']' );
            if ( aData['version'] == 'null'  || aData['version'] == '' || aData['version'] == '-' ) {
            	$('td:eq(4)', nRow).html( 'non pervenuto' ); // non e' chiara la differenza tra questo e 'sDefaultContent' che tra l'altro pare essere indispensabile in caso di null
        	}
        } ,
/*aoColumns*/
		// configurazione colonne 
		"aoColumns": [
		// colonna #1 nella tabella { mData: "nome_colonna_nel_DB" , ... } 
		// colonna #2 nella tabella { mData: "nome_colonna_nel_DB" , "bSortable": false , ... } // ordinamento disabilitato 
		// colonna #... nella tabella { mData: "nome_colonna_nel_DB" , ... } 
		// La Classe expand e' riservata alla colonna che contiene il pulsantino di espansione riga
		// La Classe chiave_record e' riservata per quelle colonno che costituiscono la chiave nel databse e non devono essere visibili 
			{ "mData": "custom02"	
			, "bSortable": false
			, "bSearchable": false
//			, "sWidth": "5%"
		    , "sDefaultContent": ""
			, "sClass": "center expand"
				} ,
				
			{ "mData": "engine"
			, "sWidth": "25%"
		    , "sDefaultContent": ""
//	 		, "bSortable": false 
				} ,
				
			{ "mData": "browser"
			, "sWidth": "15%"
		    , "sDefaultContent": ""
//	 		, "bSortable": false 
				} ,
				
			{ "mData": "platform"
			, "sWidth": "15%"
		    , "sDefaultContent": ""
//	 		, "bSortable": false 
				} ,
				
			{ "mData": "version"
			, "sWidth": "10%"
//	 		, "bSortable": false 
		    , "sDefaultContent": "-"
//			, "mRender": function ( data, type, full ) { return parseFloat(data) }
				} ,
				
			{ "mData": "grade"
			, "sWidth": "5%"
		    , "sDefaultContent": ""
//	 		, "bSortable": false 
			, "bSearchable": false
				}  
		] ,
/***aoColumns***/		
		// Traduzione voci 
		"oLanguage":  { "sUrl": "./js/object_data_localization-it.txt" } , // percorso file per localizzazione lingua
		
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
        	configuraCustomToobar('div.'+customToolbarClass, idTable, oTable, arrayTitoliColonne);
        	dtFn.fnDTDisableInstantSearch(oTable, idTable);
        	$('#'+idTable+'_paginate').disableTextSelect(); // Disabilita la selezione per i pulsanti di cambio pagina (per il problema del doppio click che seleziona tutto)
        	dtFn.addClickSelectionEvent( oTable, true, mantieniSelezione, aSelected );
        	oTable.fnAdjustColumnSizing();

//        	dtFn.dynamicTHsTitles(idTable, oSettings, [0] );
        	
//    		console.log(oTable.fnSettings() );
        } // -- Chiude fnInitComplete()
		
		
	} ); // -- Chiusura datatable() 

} ); // chiusura $(document).ready()  





