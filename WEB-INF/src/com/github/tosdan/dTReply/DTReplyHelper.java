package com.github.tosdan.dTReply;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
/**
 * 
 * @author Daniele
 * @version 0.0.1-b2013-09-13
 */
public interface DTReplyHelper {

	Map<String, Object> getReplyMap( Connection conn ) throws SQLException;

	String getSqlDati();

	String getSqlDatiExtra();

}
