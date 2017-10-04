/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee, no unauthorised use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 * author: simar.singh@redknee.com
 */
package com.trilogy.app.crm.home;

import com.trilogy.app.crm.bean.MsisdnStateEnum;
import com.trilogy.app.crm.bean.MsisdnXInfo;
import com.trilogy.app.crm.bean.PortingTypeEnum;
import com.trilogy.app.crm.bean.core.Msisdn;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.MinorLogMsg;


public class MsisdnPortHandlingHome extends HomeProxy
{
    
    /**
     * Handles the porting of MSISDNs
     */
    private static final long serialVersionUID = 1L;

    public MsisdnPortHandlingHome(Home msisdnHome)
    {
        super(msisdnHome);
    }

    @Override
    public Object create(Context ctx, Object obj) throws HomeException
    {
       Msisdn requestMsisdn = prepare(ctx, (Msisdn) obj);
       Msisdn createdMsisdn = (Msisdn) super.create(ctx, requestMsisdn);
       Msisdn finalMsisdn = ensure(ctx, createdMsisdn);
       return finalMsisdn;
       
    }

    @Override
    public Object store(Context ctx, Object obj) throws HomeException
    {
        Msisdn requestMsisdn = prepare(ctx, (Msisdn) obj);
        Msisdn updatedMsisdn = (Msisdn)super.store(ctx, requestMsisdn);
        return ensure(ctx, updatedMsisdn);
        
    }
    
   
    private Msisdn prepare(Context ctx, Msisdn msisdnBean) throws HomeException
    {
        return protectPortedMsisdn(ctx, msisdnBean);
        
    }
    
    
    private Msisdn ensure(Context ctx, Msisdn msisdnBean) throws  HomeException
    {
        msisdnBean = protectPortedMsisdn(ctx, msisdnBean);
        super.store(ctx, msisdnBean);
        return msisdnBean;
        
    }
    
    /*
     * Fail if Ported MSISDN is trying to becoem available.
     * Except In case of Move and Conversion, 
     * In Move/Conversion, the MSISDN may need to become available as old sub get's deactivated, releases and new moved/converted one's takes in
     */
    private Msisdn protectPortedMsisdn(Context ctx, Msisdn msisdnBean) throws HomeException
    {
        if (PortingTypeEnum.NONE != msisdnBean.getPortingType() || ctx.has(MSISDN_PORT_KEY))
        {
            final String msisdn = msisdnBean.getMsisdn();
            if(!msisdn.equals(ctx.get(MSISDN_PORT_KEY)))
            {
                //  not a port in request
                if (MsisdnStateEnum.AVAILABLE == msisdnBean.getState())
                {
                    msisdnBean.setState(MsisdnStateEnum.HELD);
                    // resetting the BAN so it cannot be acquired by the holding BAN once a making it available is attempted (HELD to AVAIL task)
                    msisdnBean.setBAN("");
                    String message = "Ported MSISDN [" + msisdn  + "] can not be made available. It will stay in HELD state. They can be acquired only witha valid port-in request";
                    handleWarning(ctx, message);
                }
                if (MsisdnStateEnum.IN_USE == msisdnBean.getState())
                {
                    Msisdn origMsisdnBean = (Msisdn) super.find(ctx, msisdn);
                    if(null != origMsisdnBean && origMsisdnBean.getState() != msisdnBean.getState() )
                    {
                        if (origMsisdnBean.getState() == MsisdnStateEnum.AVAILABLE )
                        {
                            origMsisdnBean.setState(MsisdnStateEnum.HELD);
                            super.store(ctx, origMsisdnBean);
                            throw new HomeException("MSISDN was ported in for account [" + origMsisdnBean.getBAN() + "], and was temporarily avaiable for the porting process. It is not avaialble any more");
                        }
                        else if(origMsisdnBean.getState() == MsisdnStateEnum.HELD && !origMsisdnBean.getBAN().equals(msisdnBean.getBAN()) )
                        {
                            throw new HomeException("MSISDN was ported in for account [" + origMsisdnBean.getBAN() + "] and is being held for the account");
                        }
                    }
                }
            } else
            {
                // porting in a porting out MSIDN
                if(PortingTypeEnum.OUT == msisdnBean.getPortingType())
                {
                    msisdnBean.setPortingType(PortingTypeEnum.NONE);
                }
            }
            // ported out MSIDNs can never be in use
            if(MsisdnStateEnum.IN_USE == msisdnBean.getState() && PortingTypeEnum.OUT == msisdnBean.getPortingType())
            {
                throw new HomeException("Ported-out MSISDN [" + msisdn  + "] can not be brought-in, it must be brought in with a valid port-in request");
            }
            
            return msisdnBean;
        }
        return msisdnBean;
        
    }
    
    private void handleWarning(Context ctx, String message)
    {
        new MinorLogMsg(this, message, null).log(ctx);
        final ExceptionListener excl = (ExceptionListener)ctx.get(ExceptionListener.class);
        if(null != excl)
        {
            excl.thrown(new IllegalPropertyArgumentException(MsisdnXInfo.STATE, message));
        }
    }


    public static String MSISDN_PORT_KEY = MsisdnPortHandlingHome.class.getName() + ".msisdn";
    
}
