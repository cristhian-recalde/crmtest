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

import org.omg.CORBA.IntHolder;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;

import com.trilogy.app.crm.support.CorbaSupportHelper;
import com.trilogy.product.s5600.ipcg.rating.provisioning.RatePlan;
import com.trilogy.product.s5600.ipcg.rating.provisioning.RatePlanSetHolder;
import com.trilogy.product.s5600.ipcg.rating.provisioning.RatingProv;
import com.trilogy.product.s5600.ipcg.rating.provisioning.ResponseCode;
import com.trilogy.product.s5600.ipcg.rating.provisioning.UserAuthHelper;
import com.trilogy.util.corba.CorbaClientException;

/**
 * @author gary.anderson@redknee.com
 */
class ProductS5600IpcgRatingProvClient
        extends UrcsDataRatingProvClient
{
    public ProductS5600IpcgRatingProvClient(final Context ctx, final String propertiesKey)
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

    private synchronized RatingProv getService(final Context context) throws IpcgRatingProvException
    {
        org.omg.CORBA.Object objServant = null;

        if (service_ != null)
        {
            try
            {
                 // this will resolve IPCG restart caused line-broken
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
            service_ = UserAuthHelper.narrow(objServant).login(ratingProvusername_, ratingProvpassword_);
            connectionUp();
            return service_;
        }
        catch (Exception e)
        {
            invalidate();
            new MinorLogMsg(this, "unable to get IPCG Rating Prov: ", e).log(context);
            return null;
        }
    }

    @Override
    public RatePlan[] retrieveAllRatingPlans(final Context context)
        throws IpcgRatingProvException
    {
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "retrieveAllRatingPlans()");

        service_ = getService(context);
        if (service_ == null)
        {
            throw new IpcgRatingProvException("Corba Comm Error", 301);
        }
        try
        {
            final IntHolder numOfPlans = new IntHolder();
            final RatePlanSetHolder ratePlanSet = new RatePlanSetHolder();
            final int result = service_.retrieveAllRatingPlans(numOfPlans, ratePlanSet);
            if (result == ResponseCode.SUCCESS)
            {
                pmLogMsg.log(context);
                return ratePlanSet.value;
            }
            throw new IpcgRatingProvException("IPCG Server Response Code = " + result, -1);
        }
        catch (org.omg.CORBA.COMM_FAILURE commFail)
        {
            invalidate();
            new MinorLogMsg(this, "Fail to Retrieve All Ipcg Rating plans due to Corba Error:", commFail).log(context);
            throw new IpcgRatingProvException("Corba Comm Error", 301, commFail);
        }
        catch (Exception e)
        {
            new MinorLogMsg(this, "Fail to Retrieve All Ipcg Rating plans due to unknown Exception:", e).log(context);
            throw new IpcgRatingProvException("Fail to get Ipcg Rating plans due to unknown Exception.", e);
        }
    }
    private RatingProv service_;

    private static final String PM_MODULE = ProductS5600IpcgRatingProvClient.class.getName();

}
