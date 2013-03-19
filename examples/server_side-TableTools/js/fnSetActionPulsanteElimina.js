/**
 * Configura l'azione per il pulsnte Elimina
 */
function setActionPulsanteElimina( oTable, idPulsante, classeDivCustomToolbar, classiDaScartare )
{
	$( classeDivCustomToolbar + " #"+idPulsante).on('click', function (e) {
		
		var dati = dtFn.fnSelectedRowToString( oTable, classiDaScartare );
		
		if (dati.length > 0 ) {
			confirm('Cancellare le seguenti righe: \n' + dati);
		}
		//oTable.fnDraw(); // refresh tabella
	} );
}
	