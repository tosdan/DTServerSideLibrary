/**
 * Prototipo
 * Chiamata ajax per eseguire una certa azione lato server su uno o più records
 */	
function eseguiAzione( azione, listaRecords, onSuccess, onComplete ) {
	$.ajax( {
		url: "./pagina.jsp",
        type: "POST",
       	//processData: false,
        dataType: "html",
        data:	{	
		        	eseguire:		azione,
		        	records:		listaRecords
	        	} ,  
		//contentType: false,
        success : function ( returnValue,textStatus, jqXHR ) {
        			onSuccess();
        			
		        },
		complete : function ( jqXHR, textStatus ) {
					onComplete();
					
				}
	} );
}