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
package com.trilogy.app.crm.web.control;

import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.HelpWebControl;

import com.trilogy.app.crm.move.MoveRequest;
import com.trilogy.app.crm.move.support.MoveRequestSupport;


/**
 * This class returns a HelpWebControl for the given MoveRequest.  It can also
 * return a HelpWebControl for a MoveRequest generated from a given bean.
 *
 * @author Aaron Gourley
 * @since 8.1
 */
public abstract class MoveRequestHelpWebControlFactory
{

    public static HelpWebControl getInstance(Context ctx, Object bean)
    {
        return getInstance(ctx, MoveRequestSupport.getMoveRequest(ctx, bean));
    }
    
    public static HelpWebControl getInstance(Context ctx, MoveRequest request)
    {
        HelpWebControl wc = null;

        if (request != null)
        {
            wc = (HelpWebControl) XBeans.getInstanceOf(ctx, request, HelpWebControl.class);
        }

        return wc;
    }

}
