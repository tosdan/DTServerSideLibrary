/**
 * Configura l'azione per un generico pulsnte Azione 
 */
function setPulsanteReloadAjax( oTable, idPulsante, classeDivCustomToolbar ) {
	
	$(classeDivCustomToolbar+" #"+idPulsante).on('click', function (e) {
//		oTable.fnFilter('fox');
		oTable.fnReloadAjax(sAjaxSource);
		
		oTable.fnDraw(); // refresh tabella
	});
}