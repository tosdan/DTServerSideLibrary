/**
 * Configura l'azione per un generico pulsnte Azione 
 */
function setActionPulsanteAzione( oTable, idPulsante, classeDivCustomToolbar, arrayTitoliColonne, classiDaScartare) {
	
	$(classeDivCustomToolbar+" #"+idPulsante).on('click', function (e) {
		
		var classiCSSDaSaltare = '',
			dati = dtFn.fnSelectedRowToStringDebug( oTable, arrayTitoliColonne, classiDaScartare );
		
		if (dati.length > 0) {
			alert(dati);
		}
	} );
//	console.log(oTable.oSettings);
//	oTable._fnAjaxParameters(oTable.oSettings);
//	console.log(oTable.aoData);
}