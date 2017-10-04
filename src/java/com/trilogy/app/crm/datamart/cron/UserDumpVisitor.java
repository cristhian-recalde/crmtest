/*
 * Created on Feb 9, 2006
 *
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.datamart.cron;

import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Execute a User Dump everytime a user is visited
 * 
 * @author amedina
 *
 */
public class UserDumpVisitor implements Visitor 
{

	public UserDumpVisitor(UserDumpAgent agent)
	{
		executor_ = agent;
	}
	
	/* (non-Javadoc)
	 * @see com.redknee.framework.xhome.visitor.Visitor#visit(com.redknee.framework.xhome.context.Context, java.lang.Object)
	 */
    public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException
    {
        try
        {
            User user = (User) obj;
            String[] fields = new String [] {
            		(user.getLastModified() != null ) ?  user.getLastModified().toString() : "",
            		(user.getActivated()) ? "1" : "0",
            		user.getId(),
            		user.getFirstName(),
            		user.getMiddleName(),
            		user.getLastName(),
            		user.getGroup(),
            		//user.getEmail(),
            		String.valueOf(user.getSpid()),
            		user.getLanguage(),
            		(user.getStartDate() != null ) ?  user.getStartDate().toString() : "",
            		(user.getEndDate() != null ) ?  user.getEndDate().toString() : "",
            		};
            
            String u = executor_.format(fields);
            
            //Write User to Dump File
            executor_.initUserWriter(ctx);
            executor_.writeToFile(ctx, u);
            executor_.closeUserWriter();
        }
        catch (Exception e)
        {
            LogSupport.debug(ctx,this,"Error writing user to file",e);
        }
    }

	protected UserDumpAgent executor_;

}
