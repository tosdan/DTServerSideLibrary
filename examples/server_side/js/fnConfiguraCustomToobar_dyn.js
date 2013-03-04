/**
 * Genera i pulsanti da includere nella toolbar personalizzata
 */
function configuraCustomToobar(classeDivCustomToolbar, idTable, oTable, arrayTitoliColonne)
{
	// Elenco pulsanti da aggiungere alla toolbar personalizzata 
	var classPulsante = 'btn' ,

		idSelezionaTutto = 'bSeleziona_tutto',
		bSeleziona_tutto = "<a href='javascript:void(0)' class='"+ classPulsante +"' id='"+ idSelezionaTutto +"' ><i class='icon-asterisk'></i> Seleziona tutto</input>" ,
		
		idAnnulla_selezione = 'bAnnulla_selezione',
		bAnnulla_selezione = "<a href='javascript:void(0)' class='"+ classPulsante +"' id='"+ idAnnulla_selezione +"' ><i class='icon-minus'></i> Annulla selezione</input>" ,
		
		idAzione = 'bAzione' ,
		bAzione = "<a href='javascript:void(0)' class='"+ classPulsante +"' id='"+ idAzione +"' ><i class='icon-repeat'></i> Carica</input>" ,
		
		idElimina = 'bElimina' ,
		bElimina = "<a href='javascript:void(0)' class='"+ classPulsante +"' id='"+ idElimina +"' ><i class='icon-trash'></i> Elimina</input>";
	
	// Aggiunge i pulsanti al div custom_toolbar 
	$(classeDivCustomToolbar).html(	
//			bSeleziona_tutto + " " +
			bAnnulla_selezione + " " +
			bAzione + " " +
			bElimina
			); 

	
	// Configura l'azione per il pulsante Seleziona_tutto 
	$(classeDivCustomToolbar + " #"+ idSelezionaTutto ).on('click', function (e) {
		dtFn.fnSetSelection(true, oTable, 'expand' );
	} );
	
	
	// Configura l'azione per il pulsante Annulla_selezione 
	$(classeDivCustomToolbar + " #"+ idAnnulla_selezione ).on('click', function (e) {
		dtFn.fnSetSelection(false, oTable, 'expand');
	} );

	setActionPulsanteAzione(oTable, idAzione, classeDivCustomToolbar, arrayTitoliColonne, 'nascosta');
	setActionPulsanteElimina(oTable, idElimina, classeDivCustomToolbar, 'nascosta');
}

