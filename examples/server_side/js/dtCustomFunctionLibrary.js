var dtFn = new Object();


//Aggiunge una funzione custom alle funzioni di jQuery per poter filtrare il contenuto di 
//un oggetto jQuery escludendo gli elementi che contengano le classi css passate come parametro 
/**
* @param cssClass
*/
jQuery.fn.scartaClasse = function(cssClass)
{
	var i = cssClass.indexOf(';');
	var $this = $(this);

	if (i >= 0) {
		return $this.filter( function () {
			var classes = cssClass.split(';');
			
			for ( i = 0 ; i < classes.length ; i++) {
				if ( $(this).hasClass(classes[i]) ) { // *questi this interni puntano al td corrente passato dalla filter
					return false; // basta che contenga anche una sola classe tra quelle da scartare
				}
			}
			return true;
		});
		
	} else {
		return $this.filter( function () {
			return ! $(this).hasClass(cssClass);// *questi this interni puntano al td corrente passato dalla filter
		});
		
	}
}




/**
 * fnGetSelectedRow
 * @param oTable
 * @param arrayTitoliColonne
 * @param classiDaScartare non utilizzato
 * @returns {Object}
 */
dtFn.fnGetSelectedRow = function(oTable)
{
	var righe = new Object();
	
	$('tr.row_selected', oTable).each( function(ind, el) { // ciclo sui TR.row_selected
		righe = oTable._(el)[0];
	});
	
	return righe;
}

/** vecchia versione 
dtFn.fnGetSelectedRow = function fnGetSelectedRow(oTable, arrayTitoliColonne, classiDaScartare)
{
	var righe = new Object(),
		classiCSSDaSaltare = '';
	
	if (classiDaScartare)
		classiCSSDaSaltare =  classiDaScartare.split(";");
	
	if (!classiDaScartare) {
		classiDaScartare = ''; //'expand;nascosta'
	}
	
	$('tr.row_selected', oTable).each( function(ind, el) { // ciclo sui TR.row_selected

		$(this).find('td') // nascosta
			.each(function (index, elem) { // ciclo sui TD che non abbiano .nascosta
				var nomeColonna = arrayTitoliColonne[index];
				var contenuto = $(elem).text();
				
//				console.log(nomeColonna +' -> '+ contenuto);
				if ( ! dtFn.containsAtLeastOneElem(classiCSSDaSaltare, $(elem).attr('class')) ) {
					righe[nomeColonna] = contenuto; 
				}
		});
	});
	
	return righe;
}
 */





/**
 * fnGetSelectedRows
 * @param oTable
 * @param arrayTitoliColonne
 * @param classiDaScartare : non utilizzato
 * @returns {Object}
 */
dtFn.fnGetSelectedRows = function(oTable)
{
	var righe = new Array(),
		rigaTemp;
		
	$('tr.row_selected', oTable).each( function(ind, el) { // ciclo sui TR.row_selected
			
		rigaTemp = oTable._(el)[0];
//		console.log(rigaTemp);
		righe.push(rigaTemp);
		
	});
	
	return righe;
}

/** vecchia versione
dtFn.fnGetSelectedRows = function fnGetSelectedRows(oTable, arrayTitoliColonne, classiDaScartare)
{
	var righe = new Array(),
		rigaTemp,
		classiCSSDaSaltare = '';
		
		if (classiDaScartare)
			classiCSSDaSaltare =  classiDaScartare.split(";");
	
	if (!classiDaScartare) {
		classiDaScartare = ''; //'expand;nascosta'
	}
	$('tr.row_selected', oTable).each( function(ind, el) { // ciclo sui TR.row_selected
			
		rigaTemp = new Object();
		$(this).find('td') // nascosta
			.each(function (index, elem) { // ciclo sui TD che non abbiano .nascosta
				var nomeColonna = arrayTitoliColonne[index];
				var contenuto = $(elem).text();
				
//				console.log(nomeColonna +' -> '+ contenuto);
				if ( ! dtFn.containsAtLeastOneElem(classiCSSDaSaltare, $(elem).attr('class')) ) {
					rigaTemp[nomeColonna] = contenuto; 
				}
		});
		
		righe.push(rigaTemp);
	});
	
	return righe;
}
*/



/**
 * fnSelectedRowToString
 * @param oTable
 * @param classiDaScartare
 * @returns {String}
 */
dtFn.fnSelectedRowToString = function( oTable, classiDaScartare ) {
	var dati = "",
		classiCSSDaSaltare = '';
	
	if (classiDaScartare)
		classiCSSDaSaltare =  classiDaScartare.split(";");
	
	$('tr.row_selected', oTable)
		.each( function(ind, el) { // ciclo sui TR.row_selected
			var riga = "";
			
			if (dati.length > 0) {
				dati += "\n";
			}
			
			$(this).find('td')
				.each(function (index, elem) {
					if (riga.length > 0) {
						riga += " - ";
					}	
					
					if ( ! dtFn.containsAtLeastOneElem(classiCSSDaSaltare, $(elem).attr('class')) )
						riga += $(elem).text();
			});
			
			dati += riga;
	});
	
	return dati;
}

/**
 * fnSelectedRowToString
 * @param oTable
 * @param arrayTitoliColonne
 * @param classiDaScartare
 * @returns {String}
 */
dtFn.fnSelectedRowToStringDebug = function( oTable, arrayTitoliColonne, classiDaScartare ) {
	var dati = "",
		classiCSSDaSaltare = '',
		selectedRows = dtFn.fnGetSelectedRows(oTable);
	
	console.log(selectedRows);
	
	if (classiDaScartare)
		classiCSSDaSaltare =  classiDaScartare.split(";");
	
	$('tr.row_selected', oTable)
		.each( function(ind, el) { // ciclo sui TR.row_selected
			var riga = "";
			
			if (dati.length > 0) {
				dati += "\n";
			}
			
			$(this).find('td')
				.each(function (index, elem) {
					if (riga.length > 0) {
						riga += "\n";
					}
					
					var nomeColonna = arrayTitoliColonne[index];
					var contenuto = $(elem).text();
					
					if ( ! dtFn.containsAtLeastOneElem(classiCSSDaSaltare, $(elem).attr('class')) )
						riga += nomeColonna + " => " + contenuto;
			});
			
			dati += riga;
	});
	
	return dati;
}




/**
 * fnGetMDataColumns
 * @param oTable
 * @returns {Array}
 */
dtFn.fnGetMDataColumns = function(oTable)
{
	var aoColumns = oTable.fnSettings().aoColumns,
		mDataColums = new Array();
	
	for (var i = 0 ; i < aoColumns.length ; i++)
	{
		mDataColums[i] = aoColumns[i].mData;
	}
	return mDataColums;
}




/**
 * Disabilita la ricerca istantanea e impone "invio" per confermare la ricerca
 * @param oTable
 * @param idTable 
 */
dtFn.fnDTDisableInstantSearch = function(oTable, idTable)
{
	$('#'+idTable+'_filter.dataTables_filter input').unbind('keypress keyup')
		.bind('keypress keyup', function( e ) {
			if ( e.keyCode == 13 ) {
				oTable.fnFilter( $(this).val(), null );
			}
	});
}



/**
 * Funzione ausiliaria per selezionare/deselezionare tutti gli elementi visibili della tabella
 * @param shouldBeSet boolean per indicare se le righe devono essere selezionate o deselezionate
 * @param table tabella su cui cercare le righe
 * @param classiDeiTDinattivi classe dei TD all'interno di una riga che se cliccati non hanno effetto sulla selezione/deselezione della stessa riga richiede che sia importato fnJQScartaClassePlugin.js 
 */ 
dtFn.fnSetSelection = function( shouldBeSet, oTable, classiDeiTDinattivi )
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




/**
 * Popola un arary con i nomi delle colonne della tabella nell'ordine in cui sono mostrate
 * @param oTable
 */
dtFn.populateColumnsTitleArray = function(oTable) {
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





/**
 * Aggancia al body della tabella un gestore eventi che ad ogni clik selezioni/deselezioni la riga applicando la classe row_selected 
 * @param mantieniSelezione flag per stabilire se le righe selezionate devono esser mantenute anche dopo i cambi di pagina
 * @param aSelected array in cui vengono tenute in memoria le righe che devono essere mantenute selezionate al cambio di pagina
 * @param multipleMode flag per indicare se si desidera la selezione multipla o singola delle righe
 */
dtFn.addClickSelectionEvent = function( oTable, multipleMode, mantieniSelezione, aSelected )
{
	$('tbody', oTable).on('click', 'td', function () {
		var $td = $(this);
		
		if ( ! $td.hasClass('expand') ) {
			var $tr = $td.closest('tr');
			
			if ( mantieniSelezione ) {
				var id = $tr.attr('id') , 
				index = $.inArray(id, aSelected);
				
				if ( index === -1 )  {
		            aSelected.push( id );
		        } else  {
		            aSelected.splice( index, 1 );
		        }
				
			}
			if ( multipleMode ) {
				
				$tr.toggleClass('row_selected'); /* per selezione multipla */
			} else {
				
				/* Per selezione singola */
				if ( $tr.hasClass('row_selected') ) {
					$tr.removeClass('row_selected');
				}  else  {
					$('.dataTable tr.row_selected').removeClass('row_selected');
					$tr.addClass('row_selected');
				}
			}
		}
	} );
}



/**
 * Espande o contrae TUTTE le rige con le informazioni extra 
 */
dtFn.setExpandAllExtraEvent = function(oTable , tabelId, fnFormatDetails) {
	$('#'+tabelId ).on( 'click', 'thead th img', function () {
		
		var $img = $(this);
		if ( $img.attr('aperti') == 'true' )
		{
			$img.attr('aperti', 'false');
			$img.attr('src', '../examples_support/details_open.png');
			$('#'+tabelId+' tbody').find('tr').each(function(index,tr) {
				if ( oTable.fnIsOpen(tr) )
				{
					$(tr).find('img').attr('src', "../examples_support/details_open.png");
		            $('td.details div#dettagli_'+tr.id).slideToggle(150, function () {
		                /* Comprime la riga gia' aperta */
		                oTable.fnClose( tr );
		            });
				}
			});
		}
		else
		{
			$img.attr('aperti', 'true');
			$img.attr('src', '../examples_support/details_close.png');
			$('#'+tabelId+' tbody').find('tr').each(function(index,tr) {
				if ( tr.id == undefined || tr.id == '' )
				{
					/*continue*/
				}
				else if ( ! oTable.fnIsOpen(tr) )
				{
					$(tr).find('img').attr('src', "../examples_support/details_close.png");
		            /* Espande la riga */
		            oTable.fnOpen( tr, fnFormatDetails(oTable, tr), 'details' );
		            $('td.details div#dettagli_'+tr.id).slideToggle(150);
				}
			});
		}
        
    });
}




/**
 * Espande o contrae le informazioni extra della riga cliccata 
 */
dtFn.setExpandExtraEvent = function(oTable , tabelId, fnFormatDetails) {
	$( '#'+tabelId ).on( 'click', 'tbody td img', function () {
        var nTr = $(this).parents('tr')[0];
        if ( oTable.fnIsOpen(nTr) )
        {
            this.src = "../examples_support/details_open.png";
            $('td.details div#dettagli_'+nTr.id).slideToggle(150, function () {
                /* Comprime la riga gia' aperta */
                oTable.fnClose( nTr );
            });
        }
        else
        {
            this.src = "../examples_support/details_close.png";
            /* Espande la riga */
            oTable.fnOpen( nTr, fnFormatDetails(oTable, nTr), 'details' );
            $('td.details div#dettagli_'+nTr.id).slideToggle(150);
        }
    } );
}




/**
 * Giusto per esempio
 * Restituisce il contenuto che deve essere mostrato nella riga di informazioni extra  
 */
dtFn.fnFormatDetails = function( oTable, nTr ) {
    var aData = oTable.fnGetData( nTr ),
    	sOut =	'';
    sOut +=		'<div id="dettagli_'+nTr.id+'" class="nascosta">';
    sOut +=		'<table cellpadding="5" cellspacing="0" border="0" style="padding-left:50px;">';
    sOut += 	'	<tr> ';
    sOut += 	'		<td>Rendering engine:</td>';
    sOut += 	'		<td>'+ aData['browser'] +' '+ aData['engine'] +'</td>';
    sOut +=		'	</tr>';
    sOut += 	'	<tr> ';
    sOut += 	'		<td>Informazioni extra:</td>';
    sOut += 	'		<td>Informazioni extra</td>';
    sOut +=		'	</tr>';
//	    sOut += 	'	<tr> ';
//	    sOut += 	'		<td>Altre informazioni:</td>';
//	    sOut += 	'		<td>Anche qui ci vanno le informazioni extra.</td>';
//	    sOut +=		'	</tr>';
    sOut +=		'</table>';
    sOut +=		'</div>';
     
    return sOut;
}





/**
 * 
 */
dtFn.containsAtLeastOneElem = function(classiDaSaltare, stringaCSS) {
	
	var stringaClassCss = stringaCSS,
		classiDelTD = '';
	
	if (stringaCSS)
		classiDelTD = stringaClassCss.split(' ');
		
	for( var i = 0 ; i < classiDaSaltare.length ; i++) {

		if ( $.inArray(classiDaSaltare[i], classiDelTD) > 0) 
			return true;
	}
	
	return false;
} 


/**
 * 
 * @param idTable
 * @param oSettings
 */
dtFn.dynamicTHsTitles = function(idTable, oSettings, indexToSkip) {
	var aoColumns = oSettings.aoColumns,
		scroll = $('.dataTables_scrollHeadInner').length > 0,
		headerScrollClass = ' .dataTables_scrollHeadInner',
		footerScrollClass = ' .dataTables_scrollFootInner';
		
	if (!scroll)
	{
		footerScrollClass = '';
		headerScrollClass = '';
	}

	var header = $('#'+idTable + '_wrapper' + headerScrollClass + ' thead'),
		footer = $('#'+idTable + '_wrapper' + footerScrollClass + ' tfoot'), 
		$headerTHs = $('th', header),
		$footerTHs = $('th', footer);

	$(aoColumns).each( function (index, elem) {
		if ( indexToSkip === undefined || $.inArray(index, indexToSkip) == -1 ) {
			
			if ($headerTHs.length > 0)
				$headerTHs[index].innerText = elem.mData;
			
			if ($footerTHs.length > 0)
				$footerTHs[index].innerText = elem.mData;
		}
	});
}


/**
 * 
 * @param idTable
 * @param oSettings
 */
dtFn.semiDynamicTHsTitles = function(nomiColonne, idTable, oSettings) {
		var aoColumns = oSettings.aoColumns ,
			arrayNomiCol = nomiColonne.split(";") ,
			scroll = $('.dataTables_scrollHeadInner').length > 0 ,
			headerScrollClass = ' .dataTables_scrollHeadInner' ,
			footerScrollClass = ' .dataTables_scrollFootInner';
		
		if (!scroll)
		{
			footerScrollClass = '';
			headerScrollClass = '';
		}

		var header = $('#'+idTable + '_wrapper' + headerScrollClass + ' thead'),
			footer = $('#'+idTable + '_wrapper' + footerScrollClass + ' tfoot'), 
			$headerTHs = $('th', header),
			$footerTHs = $('th', footer);

		$(aoColumns).each( function (index, elem) {
				
			if ($headerTHs.length > 0)
				$headerTHs[index].innerText = arrayNomiCol[index]  === undefined ? "" : arrayNomiCol[index] ;
			
			if ($footerTHs.length > 0)
				$footerTHs[index].innerText = arrayNomiCol[index]  === undefined ? "" : arrayNomiCol[index];
			
		});
}



/**
 * Sperimentale
 * @param nomiColonne
 * @param idTable
 * @param oSettings
 * @param startIdxNullCols
 */
dtFn.hideNullColumns = function(nomiColonne, idTable, oSettings, startIdxNullCols) {
	var aoColumns = oSettings.aoColumns ,
		arrayNomiCol = nomiColonne.split(";") ,
		scroll = $('.dataTables_scrollHeadInner').length > 0 ,
		headerScrollClass = ' .dataTables_scrollHeadInner' ,
		footerScrollClass = ' .dataTables_scrollFootInner' ,
		bodyScrollClass = ' .dataTables_scrollBody';
	
	if (!scroll)
	{
		footerScrollClass = '';
		headerScrollClass = '';
		bodyScrollClass = '';
	}

	var header = $('#'+idTable + '_wrapper' + headerScrollClass + ' thead'),
		footer = $('#'+idTable + '_wrapper' + footerScrollClass + ' tfoot'), 
		body = $('#'+idTable + '_wrapper' + bodyScrollClass + ' table'),
		$headerTHs = $('th', header),
		$footerTHs = $('th', footer),
		$bodyTRs = $('tr', body);
	
	if ($bodyTRs.length > 0) {
		$($bodyTRs).each( function(index, element) {
			
			$('td', element).each(function(idx, elem) {
				console.log(elem);
				if (idx >= startIdxNullCols)
					$(elem).addClass('nascosta');
			});
			
			$('th', element).each(function(idx, elem) {
				console.log(elem);
				if (idx >= startIdxNullCols)
					$(elem).addClass('nascosta');
			});
		});				
	}
	
	$(aoColumns).each( function (index, elem) {
		if (index >= startIdxNullCols) {	

			if ($headerTHs.length > 0)
				$($headerTHs[index]).addClass('nascosta');

			if ($footerTHs.length > 0)
				$($footerTHs[index]).addClass('nascosta');
		}
		
	});
}






