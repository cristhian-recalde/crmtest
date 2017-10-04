/* 
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries. 
 * 
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.home.sub;

import java.util.ArrayList;
import java.util.Collection;

import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.True;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.NoSelectAllHome;
import com.trilogy.framework.xlog.log.LogSupport;


public final class DebuggingNotSelectAllHome extends NoSelectAllHome
{
    public DebuggingNotSelectAllHome(){}


    public DebuggingNotSelectAllHome(Home delegate)
    {
        setDelegate(delegate);
    }

    
    /* (non-Javadoc)
     * @see com.redknee.framework.xhome.home.NoSelectAllHome#select(com.redknee.framework.xhome.context.Context, java.lang.Object)
     */
 
    public Collection select(Context ctx, Object where) 
        throws HomeException, UnsupportedOperationException
    {
        if (where == True.instance() || where == True.instance()
        || (where instanceof And && ((And) where).getList().size() == 0))
        {
            String msg = "Error: Selecting all is not supported and will be blocked.";
            Exception e = new UnsupportedOperationException(msg);
            
            // do not throw the exception here to avoid displaying the large stack trace on GUI.
            ExceptionListener listener = (ExceptionListener) ctx.get(ExceptionListener.class);
            listener.thrown(e);
            
            LogSupport.major(ctx, this, msg, e);
            
            // output the stack trace to ctl log for later investigation.
            // ctl log wont' be rolled over. The bug is more likely comes from framework, 
            // however, we don't have hard evidence so far. 
            e.printStackTrace();
            
            return new ArrayList();
        }
        else
        {
            return getDelegate().select(ctx, where);
        }
    }   

}
