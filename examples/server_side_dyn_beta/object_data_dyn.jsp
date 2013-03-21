<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
	<head>
		<meta http-equiv="content-type" content="text/html; charset=utf-8" />
		<link rel="shortcut icon" type="image/ico" href="./images/favicon.ico" /> 
		
		<title>Dyn</title>
		
		<style type="text/css" title="currentStyle">
			@import "./css/object_data.css";
		</style>
		
		<script type="text/javascript" src="./js-3rd-party/jquery-1.9.1.js"></script>
		<script type="text/javascript" src="./js-3rd-party/jquery.dataTables.js"></script>
		<script type="text/javascript" src="./js/fnReloadAjax.js"></script>
		<script type="text/javascript" src="./js-3rd-party/dataTables.bootstrap-partial.js"></script>
		
		<script type="text/javascript" src="./js/fnJQDisableTextSelect.js"></script>
		<script type="text/javascript" src="./js/dtCustomFunctionLibrary.js"></script>
		
		<script type="text/javascript" src="./js/fnSetActionPulsanteElimina.js"></script>
		<script type="text/javascript" src="./js/setActionGenericoPulsanteAzione_dyn.js"></script> 
		<script type="text/javascript" src="./js/fnConfiguraCustomToobar_dyn.js"></script> <!-- Configura pulsanti e azioni collegate  --> 
		
		<script type="text/javascript" src="./js/fnServerParams_dyn.js"></script>
		<script type="text/javascript" src="./js/object_data_dyn.js"></script>

		<script type="text/javascript">
			var sqlName = 'dtexample' <% // TODO parametro sqlName passato nella request %>;
			var sAjaxSource = "../../filter/sqlmanager/dtreply/loadData.do?sqlName="+ sqlName +"&sqlType=query";
// 			var sAjaxSource = "../../servlet/sqlmanager/loadData.do?sqlName="+ sqlName +"&sqlType=query&NextHandlerServlet=DTReplyServlet";
		
		</script>
		
		<style type="text/css">
		table.dataTable {
 			 max-width: none;
		}
		</style>
		
	</head>
	
	<body id="dt_example">
		<div id="container">
			<div class="full_width big">
				DataTables Esempio server-side Dyn - sorgente dati JSON
			</div>
			
			<h1>Esempio Live</h1>
			<div id="dynamic">
			
<!-- ***			Struttura statica della tabella		**** START -->
			<table cellpadding="0" width="100%" cellspacing="0" border="0" class="display" id="example">
			<% int width = 270; %>
				<thead>
					<tr>
						<th id="th_01" width="<%= width %>px"></th>
						<th id="th_02" width="<%= width %>px"></th>
						<th id="th_03" width="<%= width %>px"></th>
						<th id="th_04" width="<%= width %>px"></th>
						<th id="th_05" width="<%= width %>px"></th>
						<th id="th_06" width="<%= width %>px"></th>
						<th id="th_07" width="<%= width %>px"></th>
						<th id="th_08" width="<%= width %>px"></th>
						<th id="th_09" width="<%= width %>px"></th>
						<th id="th_10" width="<%= width %>px"></th>
						<th id="th_11" width="<%= width %>px"></th>
						<th id="th_12" width="<%= width %>px"></th>
						<th id="th_13" width="<%= width %>px"></th>
						<th id="th_14" width="<%= width %>px"></th>
						<th id="th_15" width="<%= width %>px"></th>
						<th id="th_16" width="<%= width %>px"></th>
						<th id="th_17" width="<%= width %>px"></th>
						<th id="th_18" width="<%= width %>px"></th>
						<th id="th_19" width="<%= width %>px"></th>
						<th id="th_20" width="<%= width %>px"></th>
					</tr>
				</thead>
				
				
				<tbody>
					<tr>
						<td colspan="20" class="dataTables_empty">Loading data from server</td>
					</tr>
				</tbody>
				
			</table>
<!-- ***			Struttura statica della tabella 	*** END -->	
	
			</div>
			<div class="spacer"></div>
		</div>
	</body>
</html>