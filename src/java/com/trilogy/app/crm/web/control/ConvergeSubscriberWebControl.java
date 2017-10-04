/*
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
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.filter.SubscriberConversionAllowedPredicate;
import com.trilogy.app.crm.filter.SubscriberConversionInProgressPredicate;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.ViewModeEnum;
import com.trilogy.framework.xhome.webcontrol.WebControl;

/**
 * If the subscriber account is not eligible to change subscriber type;
 * make subscriber type read-only 
 * 
 * @author kwong
 *
 */
// TODO Should be renamed to SubscriberConversionWebControl, since it doesn't involve "converging" subscribers
public class ConvergeSubscriberWebControl extends ProxyWebControl
{
    
	/**
	 * @param delegate
	 */
	public ConvergeSubscriberWebControl(WebControl delegate) {
		super(delegate);
	}

    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
    {
        Subscriber subscriber = (Subscriber)obj;
        
        int  mode = ctx.getInt("MODE", DISPLAY_MODE);
        if ( mode == EDIT_MODE)
        {
            // TODO 2008-09-19 correct by using the PropertyInfo without name concatenation
            if (!SubscriberConversionAllowedPredicate.instance().f(ctx, subscriber))
            {
                AbstractWebControl.setMode(
                        ctx, 
                        SubscriberXInfo.instance().getName() + "." + SubscriberXInfo.SUBSCRIBER_TYPE.getName(), 
                        ViewModeEnum.READ_ONLY);
            }
            if (!SubscriberConversionInProgressPredicate.instance().f(ctx, subscriber))
            {
            	AbstractWebControl.setMode(
                        ctx, 
                        SubscriberXInfo.instance().getName() + "." + SubscriberXInfo.DEPOSIT.getName(), 
                        ViewModeEnum.READ_ONLY);
            }
            else 
            {
                // Can this be moved into the Field Level Permissions on the Subscriber menu?         
            	AbstractWebControl.setMode(
                        ctx, 
                        SubscriberXInfo.instance().getName() + "." + SubscriberXInfo.STATE.getName(), 
                        ViewModeEnum.READ_ONLY);
            }
        }
        super.toWeb(ctx, out, name, obj);
    }
}
