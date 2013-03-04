/**
 * Popola un arary con i nomi delle colonne della tabella nell'ordine in cui sono mostrate
 */
function populateColumnsTitleArray(oTable) {
	var arrayTitoliColonne = new Array(),
		arrayNomiColonna = new Array();
	
	arrayNomiColonna = oTable._fnGetUniqueThs();
	for (var i = 0 ; i < arrayNomiColonna.length ; i++ )
	{
		var th = arrayNomiColonna[i];
//		console.log( $(th).text() );
		arrayTitoliColonne[i] = $(th).text();
	}
	
/* Questa versione non funzionerebbe se si utilizza sScrollY in datatable ******
		$THs = $('#'+idTable+'.dataTable thead').children('tr').first().find('th');
//	console.log('populateColumnsTitleArray: chiamata');
	$THs.each( function(index,elem) {
		arrayTitoliColonne[index] = $(elem).text();
//		console.log(elem);
	});
	
/* vecchia versione fine */
	
	return arrayTitoliColonne;
}