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
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletRequest;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.util.Link;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;




/**
 * Provides a custom WebControl for the calling group identifier property of the
 * AuxiliaryServices model.
 *
 * @author gary.anderson@redknee.com
 */
public
class ReactivateLinkWebControl
    extends AbstractWebControl
{
    /**
     * Creates a new CallingGroupLinkedWebControl.
     */
    public ReactivateLinkWebControl()
    { 
     
    }


    // INHERIT
    public void toWeb(
        final Context context,
        final PrintWriter out,
        final String name,
        final Object object)
    {
    	final Link subLink = new Link (context);    	
    	final Object bean = context.get(BEAN);
    	final Subscriber sub = (Subscriber) bean;    	
    	
    	subLink.remove("key");
    	subLink.remove("query");
    	subLink.addRaw(".existingSubscriberIdentifier", sub.getId());
    	subLink.addRaw("cmd", "appCRMReactivateSubscriber");
    	subLink.writeLink(out, "Reactivate");    	
    }
    public Object fromWeb(
            final Context context,
            final ServletRequest req,
            final String name)
        {
            return null;
        }


        /**
         * {@inheritDoc}
         */
        public void fromWeb(
            final Context context,
            final Object obj,
            final ServletRequest req,
            final String name)
        {
            // Empty.
        } 
     

} // class
