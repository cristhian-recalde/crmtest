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
package com.trilogy.app.crm.client;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.trilogy.framework.core.platform.Ports;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.rmi.RMIProperty;
import com.trilogy.framework.xhome.rmi.RMIPropertyHome;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.invoice.config.InvoiceServerRemoteServicerConfigXInfo;
import com.trilogy.app.crm.invoice.service.InvoiceServiceSupport;


/**
 * This property change listener dynamically updates the RMI connection parameters for
 * the IS RMI services.
 *
 * @author aaron.gourley@redknee.com
 * @since 8.5
 */
public class InvoiceServerRMIServicePropertyChangeListener extends ContextAwareSupport implements PropertyChangeListener
{
    private static final String[] RMI_SERVICE_NAMES = new String[]
                                              {
            InvoiceServiceSupport.INVOICE_SERVER_SERVICE_NAME,
            InvoiceServiceSupport.INVOICE_RUN_SERVICE_NAME,
            InvoiceServiceSupport.CONFIGURE_IS_SERVICE_NAME
                                              };
    
    public InvoiceServerRMIServicePropertyChangeListener(Context ctx)
    {
        super();
        setContext(ctx);
    }

    /**
     * {@inheritDoc}
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        Context ctx = getContext();
        Home home = (Home) ctx.get(RMIPropertyHome.class);
        if (evt.getNewValue() != null)
        {
            String newStringValue = String.valueOf(evt.getNewValue());
            if (InvoiceServerRemoteServicerConfigXInfo.HOST_NAME.getName().equals(newStringValue))
            {
                for (String service : RMI_SERVICE_NAMES)
                {
                    try
                    {
                        RMIProperty property = (RMIProperty) home.find(ctx, service);
                        if (property != null)
                        {
                            property.setHost(newStringValue);
                            home.store(ctx, property);
                        }
                    }
                    catch (HomeException e)
                    {
                        new MinorLogMsg(this, "Error updating RMI Property for RMI IS service '" + service + "'", e).log(ctx);
                    }
                }
            }
            else if (InvoiceServerRemoteServicerConfigXInfo.BASE_PORT.getName().equals(newStringValue))
            {
                int newPortNumber = Integer.valueOf(newStringValue) + Ports.RMI_OFFSET;

                for (String service : RMI_SERVICE_NAMES)
                {
                    try
                    {
                        RMIProperty property = (RMIProperty) home.find(ctx, service);
                        if (property != null)
                        {
                            property.setPort(newPortNumber);
                            home.store(ctx, property);
                        }
                    }
                    catch (HomeException e)
                    {
                        new MinorLogMsg(this, "Error updating RMI Property for RMI IS service '" + service + "'", e).log(ctx);
                    }
                }
            }
        }
    }

}
