function fnServerParams( aoData ) {
	aoData.push({ "name": "stampaQuery" ,			"value": "false" } , 
				{ "name": "custom02" ,				"value": "<img src='../examples_support/details_open.png'>" } ,
				{ "name": "row_number" ,			"value": "true" } ,
				{ "name": "printStackTrace" ,		"value": "false" } , 
				{ "name": "debugJson" ,				"value": "false" } ,
				{ "name": "mDataBinded" ,			"value": "true" } , // se false rende l'associazione colonne/contenuto dinamica basata su indici invece che cablata su nomi di parametro-mData/campi-record
//				{ "name": "custom01" , 				"value": "<input type='checkbox' />" } ,
				{ "name": "echoParams" , 			"value": "DT_RowClass;custom" } , // Specifica (separati dai ; ) quali siano altri suffissi di parametri che devono tornare indietro 
//				{ "name": "DT_RowClass" ,			"value": "customCssClass" } , // impone la stessa classe custom per tutti i teg "tr"
				{ "name": "logDTReplyReqParams" ,	"value": "true" } // flag 
	);
}