/*
    InvalidTPSRecordException

    @Author : Larry Xia
    Date    : Oct, 21 2003
*/

package com.trilogy.app.crm.bas.tps;

/**  Exception thrown by TPSInputstream if the incoming TPS does 
 *   not follow predefined format. 
 **/
public class InvalidTPSRecordException
   extends Exception
{
   
	public InvalidTPSRecordException(String s)
	{
		super(s);
	}

   
	public InvalidTPSRecordException(String s, Throwable t)
	{
		super(s, t);
	}

   
	public InvalidTPSRecordException(Throwable t)
	{
		super(t);
	}
   
}

