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
package com.trilogy.app.crm.client.ipcg;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;

import com.trilogy.app.crm.support.CorbaSupportHelper;
import com.trilogy.product.s5600.iprc.rating.provisioning.IprcRatePlan;
import com.trilogy.product.s5600.iprc.rating.provisioning.IprcRatePlanSetHolder;
import com.trilogy.product.s5600.iprc.rating.provisioning.IprcRatingProv;
import com.trilogy.product.s5600.iprc.rating.provisioning.IprcResponseCode;
import com.trilogy.product.s5600.iprc.rating.provisioning.IprcUserAuthHelper;
import com.trilogy.util.corba.CorbaClientException;

public class ProductS5600IprcRatingProvClient extends UrcsDataRatingProvClient
{
    public ProductS5600IprcRatingProvClient(final Context ctx, final String propertiesKey)
    {
        propertiesKey_ = propertiesKey;
        setContext(ctx);

        try
        {
            super.init();
        }
        catch (final IllegalArgumentException exception)
        {
            new MajorLogMsg(
                    this,
                    "Initialization during construction failed.",
                    exception).log(ctx);
        }
        service_ = null;
    }

    private synchronized IprcRatingProv getService(final Context context) throws IpcgRatingProvException
    {
        org.omg.CORBA.Object objServant = null;

        if (service_ != null)
        {
            try
            {
                 // this will resolve URSC Data restart caused line-broken
                if (service_._non_existent())
                {
                     // give it another try
                    //invalidate();
                }
                else
                {
                    return service_;
                }
            }
            catch (Exception e)
            {
                //invalidate();
            }
        }

        if (ratingProvCorbaProperty_ == null)
        {
            final String msg = "Corba Client " + propertiesKey_ + " configuration missing!";

            throw new IpcgRatingProvException(msg);
        }

        if (ratingProvCorbaProxy_ == null)
        {
            try
            {
                ratingProvCorbaProxy_ = CorbaSupportHelper.get(context).createProxy(context, ratingProvCorbaProperty_, this);
            }
            catch (CorbaClientException ccEx)
            {
                invalidate();
                new MinorLogMsg(this, "unable to createProxy for IPCG Rating Prov: ", ccEx).log(context);
                return null;
            }
        }
        service_ = null;
        objServant = ratingProvCorbaProxy_.instance();
        if (objServant == null)
        {
            return null;
        }
        try
        {
            service_ = IprcUserAuthHelper.narrow(objServant).login(ratingProvusername_, ratingProvpassword_);
            connectionUp();
            return service_;
        }
        catch (Exception e)
        {
            invalidate();
            new MinorLogMsg(this, "unable to get IPRC Rating Prov: ", e).log(context);
            return null;
        }
    }

    @Override
    public IprcRatePlan[] queryRatePlans(final Context context, final int spid)
        throws IpcgRatingProvException
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "queryRatePlans()");

        service_ = getService(context);
        if (service_ == null)
        {
            throw new IpcgRatingProvException("Corba Comm Error", 301);
        }
        try
        {
            final IprcRatePlanSetHolder ratePlanSet = new IprcRatePlanSetHolder();
            final int result = service_.queryRatePlans(spid, ratePlanSet);
            if (result == IprcResponseCode.SUCCESS)
            {
                pmLogMsg.log(context);
                return ratePlanSet.value;
            }
            throw new IpcgRatingProvException("IPRC Server Response Code = " + result, -1);
        }
        catch (org.omg.CORBA.COMM_FAILURE commFail)
        {
            invalidate();
            final String msg = "Fail to Query Rate Plan for Service Provider " 
                + spid + " due to Corba Communication Error:";
            new MinorLogMsg(this, msg, commFail).log(context);
            throw new IpcgRatingProvException("Corba Comm Error", 301, commFail);
        }
        catch (Exception e)
        {
            final String msg = "Fail to Query Rate Plans for Service Provider " 
                + spid + " due to unknown Exception:";
            new MinorLogMsg(this, msg, e).log(context);
            throw new IpcgRatingProvException(msg, e);
        }
    }
    
    private IprcRatingProv service_;
    
    private static final String PM_MODULE = ProductS5600IprcRatingProvClient.class.getName();


}
