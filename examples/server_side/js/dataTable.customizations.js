var dtCustom = {
		/**
		 * 
		 * @param errorCallBack
		 * @param completeOverride
		 */
		"setCustomErrorCallBack": function( errorCallBack, completeOverride ) {
			
			jQuery.fn.dataTable.defaults.fnServerData = function ( sUrl, aoData, fnCallback, oSettings ) {
				oSettings.jqXHR = $.ajax( {
					"url":  sUrl,
					"data": aoData,
					"success": function (json) {
						if ( json.sError ) {
							oSettings.oApi._fnLog( oSettings, 0, json.sError );
						}
						
						$(oSettings.oInstance).trigger('xhr', [oSettings, json]);
						fnCallback( json );
					},
					"dataType": "json",
					"cache": false,
					"type": oSettings.sServerMethod,
					"error": function (xhr, error, thrown) {
						// In caso completeOverride sia true viene eseguita la funzione errorCallBack anche in caso di un errore di parsing del json.
						// Altrimenti per questo tipo di errore verrebbe usato il sistema interno di datatable invece della funzione passata.
						if ( !completeOverride && error == "parsererror" ) {
							oSettings.oApi._fnLog( oSettings, 0, "DataTables warning: JSON data from "+
								"server could not be parsed. This is caused by a JSON formatting error." );
						}
						else
							errorCallBack(xhr, error, thrown);
					}
				} );
			}
		} ,

		/**
		 * 
		 * @param numPages
		 */
		"showPagesNumber": function( numPages ) {
			
			jQuery.fn.dataTableExt.oPagination.iFullNumbersShowPages = numPages;	
		}
}
