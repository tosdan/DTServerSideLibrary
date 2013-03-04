/**
 * Espande o contrae TUTTE le rige con le informazioni extra 
 */
function setExpandAllExtraEvent(oTable , tabelId, fnFormatDetails)
{
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
function setExpandExtraEvent(oTable , tabelId, fnFormatDetails)
{
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
function fnFormatDetails ( oTable, nTr )
{
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
