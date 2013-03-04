function fnGetMDataColumns(oTable)
{
	var aoColumns = oTable.fnSettings().aoColumns,
		mDataColums = new Array();
	
	for (var i = 0 ; i < aoColumns.length ; i++)
	{
		mDataColums[i] = aoColumns[i].mData;
	}
	return mDataColums;
}