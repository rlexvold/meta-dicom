/*
 * Created by IntelliJ IDEA.
 * User: mb
 * Date: Nov 20, 2003
 * Time: 1:50:08 PM
 */
package acme.db;

import java.sql.ResultSet;

public interface RowProcessor
{
	void processRow(ResultSet rs) throws Exception;
}