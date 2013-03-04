/**
 * Funzione ausiliaria per selezionare/deselezionare tutti gli elementi visibili della tabella
 * @param shouldBeSet boolean per indicare se le righe devono essere selezionate o deselezionate
 * @param table tabella su cui cercare le righe
 * @param classiDeiTDinattivi classe dei TD all'interno di una riga che se cliccati non hanno effetto sulla selezione/deselezione della stessa riga richiede che sia importato fnJQScartaClassePlugin.js 
 */ 
function fnSetSelection( shouldBeSet, oTable, classiDeiTDinattivi )
//function fnSetSelection( shouldBeSet, oTable, fnFiltro )
{
	$('tr', oTable).each( function(i,e) {
		var $tr = $(e);
		
		if ( ( ! $tr.hasClass('row_selected') && shouldBeSet)
				|| ($tr.hasClass('row_selected') && ! shouldBeSet)  ) {	

			if ( classiDeiTDinattivi && (typeof jQuery.fn.scartaClasse != 'undefined') )
//			if ( typeof fnFiltro != 'undefined' )
			{
				$tr.children('td').scartaClasse(classiDeiTDinattivi).first().trigger('click'); // innesca un click sul primo td che non abbia la classe da ignorare
//				$tr.children('td').filter( fnFiltro ).first().trigger('click'); // innesca un click sul primo td che non abbia la classe da ignorare
			}
			else
			{
				$tr.children('td').first().trigger('click');
			}
		}
	} );
}