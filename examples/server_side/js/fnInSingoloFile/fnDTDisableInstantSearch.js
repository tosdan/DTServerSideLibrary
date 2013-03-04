/**
 * Disabilita la ricerca istantanea e impone "invio" per confermare la ricerca 
 */
function fnDTDisableInstantSearch(oTable, idTable)
{
	$('#'+idTable+'_filter.dataTables_filter input').unbind('keypress keyup')
		.bind('keypress keyup', function( e ) {
			if ( e.keyCode == 13 ) {
				oTable.fnFilter( $(this).val(), null );
			}
	});
}