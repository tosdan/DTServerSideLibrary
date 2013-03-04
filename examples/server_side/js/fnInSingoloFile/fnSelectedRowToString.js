function fnSelectedRowToString( oTable, classiDaScartare )
{
	var dati = "";
	
	if (!classiDaScartare)
	{
		classiDaScartare = ''; //'expand;nascosta'
	}
	
	$('tr.row_selected', oTable).each( function() {
		var riga = "";
		
		if (dati.length > 0)
		{
			dati += "\n";
		}
		$(this).find('td').scartaClasse(classiDaScartare).each(function (index, elem) {
			if (riga.length > 0)
			{
				riga += " - ";
			}
			riga += $(elem).text();
		});
		
		dati += riga;
	});
	
	return dati;
}