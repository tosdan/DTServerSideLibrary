/**
 * Aggancia al body della tabella un gestore eventi che ad ogni clik selezioni/deselezioni la riga applicando la classe row_selected 
 * @param mantieniSelezione flag per stabilire se le righe selezionate devono esser mantenute anche dopo i cambi di pagina
 * @param aSelected array in cui vengono tenute in memoria le righe che devono essere mantenute selezionate al cambio di pagina
 * @param multipleMode flag per indicare se si desidera la selezione multipla o singola delle righe
 */
function addClickSelectionEvent( oTable, multipleMode, mantieniSelezione, aSelected )
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