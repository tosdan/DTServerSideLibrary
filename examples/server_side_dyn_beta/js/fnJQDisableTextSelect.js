$.extend($.fn.disableTextSelect = function() {
	return this.each(function() {
			var $this = $(this);
			if (typeof this.style.MozUserSelect!="undefined") { // Firefox
					this.style.MozUserSelect = 'none';
			} else if (typeof this.onselectstart != 'undefined') {  // IE
		       	$this.bind('selectstart', function(){return false;});
			}
			else { // Altri
		       	$this.bind('selectstart', function(){return false;});
		       	$this.bind('mousedown', function(){return false;});
			}
		});
	});
   