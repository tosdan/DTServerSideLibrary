function fnServerParams( aoData, oSettings ) {
	aoData.push(
			{ "name": "serverSideProcessing" ,	"value": oSettings.oFeatures.bServerSide  } ,  
			{ "name": "rowNumber" ,				"value": oSettings.oFeatures.bPaginate   } , // se false o mancante non viene calcolata la paginazione (tramite query)
			{ "name": "printStackTrace" ,		"value": "false" } , // se true stampa eventuali stacktrace di eccezioni della servlet
			{ "name": "mDataBinded" ,			"value": "true"  } , // se false rende l'associazione colonne/contenuto dinamica basata su indici invece che cablata su nomi di parametro-mData/campi-record
			/** @param echoParams Elenco (separato da ';') dei parametri che devono essere spediti indietro cosi' come sono stati inviati: in ogni record 
				estratto da db verra' aggiunta (cablata) ogni coppia chiave/valore corrispondente al nome di parametro specificato in questo elenco.*/
			{ "name": "echoParams" , 			"value": "DT_RowClass;custom02" } , 
//			{ "name": "custom02" ,				"value": "<img src='../examples_support/details_open.png'>" } ,
//			{ "name": "custom01" , 				"value": "<input type='checkbox' />" } ,
//			{ "name": "DT_RowClass" ,			"value": "customCssClass" } , // aggiunge una classe custom a tutti i teg "tr"
			{ "name": "DebugDTReply" ,			"value": "true"  } // flag per il debug della servlet
	);
}