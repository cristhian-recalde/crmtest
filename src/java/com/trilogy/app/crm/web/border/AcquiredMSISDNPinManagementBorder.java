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
package com.trilogy.app.crm.web.border;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.omg.CORBA.BooleanHolder;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.session.Session;
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AcquiredMsisdnPINManagement;
import com.trilogy.app.crm.bean.AcquiredMsisdnPINManagementHome;
import com.trilogy.app.crm.bean.AcquiredMsisdnPINManagementTransientHome;
import com.trilogy.app.crm.bean.account.SubscriptionTypeEnum;
import com.trilogy.app.crm.bean.core.Msisdn;
import com.trilogy.app.crm.bean.core.SubscriptionType;
import com.trilogy.app.crm.support.MsisdnSupport;
import com.trilogy.app.crm.support.PinManagerSupport;


/**
 * @author dmishra
 * 
 */
public class AcquiredMSISDNPinManagementBorder extends Session implements Border
{

    @SuppressWarnings("unchecked")
    public void service(Context ctx, HttpServletRequest req, HttpServletResponse res, RequestServicer delegate)
            throws ServletException, IOException
    {
        req.getParameterMap();
        PrintWriter out = res.getWriter();
        out.println("<table width=\"70%\"><tr><td><center><H3><center>PIN Management</center></H3></center></td></tr></table>");
        Context subCtx = ctx.createSubContext();
        if (getSession(ctx).has(Account.class))
        {
            Account account = (Account) ctx.get(Account.class);
            try
            {
                //BAN would be acts as Subsriber account Identifier.
                Collection<Msisdn> msisdns = MsisdnSupport.getAcquiredMsisdn(ctx, account.getBAN());
                if (msisdns != null)
                {
                    Home transientHome = new AcquiredMsisdnPINManagementTransientHome(ctx);

                    for (Msisdn msisdn : msisdns)
                    {
                        AcquiredMsisdnPINManagement acqMsisdn = new AcquiredMsisdnPINManagement();
                        acqMsisdn.setIdIdentifier(account.getBAN());
                        acqMsisdn.setMsisdn(msisdn.getMsisdn());
                        acqMsisdn.setState(PinManagerSupport.getStateOnPinManager(ctx, msisdn.getMsisdn()));  
                        
                        SubscriptionType subscriptionType = SubscriptionType.getSubscriptionType(ctx, SubscriptionTypeEnum.WIRE_LINE);
                        if (subscriptionType != null)
                        {
                            long wireLineId = subscriptionType.getId();
                            String subId = msisdn.getSubscriberID(ctx, wireLineId, new Date());
                            if ( subId != null )
                            {
                                acqMsisdn.setWireLineSubscription(true);                        
                            }
                            
                            if (acqMsisdn.isWireLineSubscription())
                            {
                                BooleanHolder authenicated = new BooleanHolder(false);
                                short resultCode = PinManagerSupport.queryAuthenticatedFlag(subCtx, acqMsisdn.getMsisdn(),
                                        Long.valueOf(wireLineId).intValue(), "", authenicated);
                                if (resultCode == 0)
                                {
                                    acqMsisdn.setAuthenticated(authenicated.value);
                                }
                            }   
                        }
                        
                        transientHome.create(subCtx,acqMsisdn);
                    }
                    
                    subCtx.put(AcquiredMsisdnPINManagementHome.class, transientHome);
                }
            }
            catch (HomeException e)
            {
                LogSupport.minor(ctx, this, "Problem occured in AcquiredMSISDNPinManagementBorder ", e);
            }
        }
        delegate.service(subCtx, req, res);
    }
}
