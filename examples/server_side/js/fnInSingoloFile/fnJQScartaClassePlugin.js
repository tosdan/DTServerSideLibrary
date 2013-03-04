
// Aggiunge una funzione custom alle funzioni di jQuery per poter filtrare il contenuto di 
// un oggetto jQuery escludendo gli elementi che contengano le classi css passate come parametro 
jQuery.fn.scartaClasse = function (cssClass)
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
	