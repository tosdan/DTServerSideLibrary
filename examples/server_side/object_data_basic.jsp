<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
	<head>
		<meta http-equiv="content-type" content="text/html; charset=utf-8" />
		<link rel="shortcut icon" type="image/ico" href="./images/favicon.ico" /> 
		
		<title>DataTables basic example</title>
		
		<style type="text/css" title="currentStyle">
			@import "./css/object_data.css";
		</style>
		
		<script type="text/javascript" src="./js-3rd-party/jquery-1.9.1.js"></script>
		<script type="text/javascript" src="./js-3rd-party/jquery.dataTables.js"></script>
		<script type="text/javascript" src="./js-3rd-party/dataTables.bootstrap-partial.js"></script>
		
		<script type="text/javascript" src="./js/fnJQDisableTextSelect.js"></script>
		<script type="text/javascript" src="./js/dtCustomFunctionLibrary.js"></script>
		
		<script type="text/javascript" src="./js/fnSetActionPulsanteElimina.js"></script>
		<script type="text/javascript" src="./js/setActionGenericoPulsanteAzione.js"></script> 
		<script type="text/javascript" src="./js/fnConfiguraCustomToobar_basic.js"></script> <!-- Configura pulsanti e azioni collegate  --> 
		
		<script type="text/javascript" src="./js/fnServerParams_basic.js"></script>
		<script type="text/javascript" src="./js/object_data_basic.js"></script>

	</head>
	
	<body id="dt_example">
		<div id="container">
			<div class="full_width big">
				DataTables Esempio server-side Basic - sorgente dati JSON
			</div>
			
			<h1>Esempio Live</h1>
			<div id="dynamic">
			
<!-- ***			Struttura statica della tabella		**** START -->
			<table cellpadding="0" width="100%" cellspacing="0" border="0" class="display" id="example">
			
				<thead>
					<tr>
						<th width="25%">Rendering engine</th>
						<th width="20%">Browser</th>
						<th width="25%">Platform(s)</th>
						<th width="16%">Engine version</th>
						<th width="10%">CSS grade</th>
					</tr>
				</thead>
				
				
				<tbody>
					<tr>
						<td colspan="5" class="dataTables_empty">Loading data from server</td>
					</tr>
				</tbody>
				
			</table>
<!-- ***			Struttura statica della tabella 	*** END -->	
	
			</div>
			<div class="spacer"></div>
		</div>
	</body>
</html>