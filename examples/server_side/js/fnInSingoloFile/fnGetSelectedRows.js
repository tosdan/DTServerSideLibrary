function fnGetSelectedRows(oTable, arrayTitoliColonne, classiDaScartare)
{
	var righe = new Object();
	
	if (!classiDaScartare) {
		classiDaScartare = ''; //'expand;nascosta'
	}
	
	$('tr.row_selected', oTable)
	.each( function(ind, el) { // ciclo sui TR.row_selected
		
		$(this).find('td').scartaClasse(classiDaScartare) // nascosta
			.each(function (index, elem) { // ciclo sui TD che non abbiano .nascosta
				
				var nomeColonna = arrayTitoliColonne[index];
				var contenuto = $(elem).text();
				
//				console.log(nomeColonna +' -> '+ contenuto);
				if (nomeColonna != "") {
					righe[nomeColonna] = contenuto; 
				}
		});
	});
	
	return righe;
}