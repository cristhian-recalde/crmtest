/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.listener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.ui.Service;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.util.snippet.log.Logger;


public class ServiceSpidSwitchFromWebListener implements PropertyChangeListener
{

    public void propertyChange(final PropertyChangeEvent evt)
    {
        final Context ctx = (Context) evt.getSource();
        final Service oldService = (Service) evt.getOldValue();
        final Service newService = (Service) evt.getNewValue();
        long oldSpid = -1;
        if (oldService != null)
        {
            oldSpid = oldService.getSpid();
        }
        if (Service.isFromWebNewOrPreviewOnSpid(ctx))
        {
            if (newService.isEnableCLTC())
            {
                try
                {
                    CRMSpid crmSpid = SpidSupport.getCRMSpid(ctx, newService.getSpid());
                    long newSpid = newService.getSpid();
                    if (newSpid != oldSpid)
                    {
                        // Setting default values based on spid configuration
                        if (crmSpid != null)
                        {
                            newService.setClctThreshold(crmSpid.getDefaultClctThreshold());
                        }
                    }
                    else
                    {
                        if (oldService.getClctThreshold() >= 0)
                        {
                            newService.setClctThreshold(oldService.getClctThreshold());
                        }
                        else
                        {
                            if (crmSpid != null)
                            {
                                newService.setClctThreshold(crmSpid.getDefaultClctThreshold());
                            }
                        }
                    }
                }
                catch (Throwable t)
                {
                    if (Logger.isDebugEnabled())
                    {
                        Logger.debug(ctx, this, "Error setting default SPID values in Service initialization", t);
                    }
                }
            }
        }
    }
}
