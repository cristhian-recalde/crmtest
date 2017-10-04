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
package com.trilogy.app.crm.api.rmi;

import com.trilogy.app.crm.bundle.rateplan.RatePlanAssociation;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.BundleCategoryRatePlanAssociation;


/**
 * Adapts BundleProfile object to API objects.
 * 
 * @author bhupendra.pandey@redknee.com
 */
public class RatePlanAssociationToApiAdapter implements Adapter
{

    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
        return adaptRatePlanAssociationToApi(ctx, (RatePlanAssociation) obj);
    }


    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }


    public static BundleCategoryRatePlanAssociation adaptRatePlanAssociationToApi(Context ctx,
            final RatePlanAssociation crmRtp)
    {
        BundleCategoryRatePlanAssociation apiRtp = new BundleCategoryRatePlanAssociation();
        RatePlanAssociationToApiReferenceAdapter.adaptRatePlanAssociationToReference(ctx, crmRtp, apiRtp);
        apiRtp.setCategoryDescription(crmRtp.getCategoryDesc());
        apiRtp.setDataRatePlan(String.valueOf(crmRtp.getDataRatePlan()));
        apiRtp.setSmsRatePlan(crmRtp.getSmsRatePlan());
        apiRtp.setVoiceRatePlan(crmRtp.getVoiceRatePlan());
        return apiRtp;
    }


    public static RatePlanAssociation adaptApiToRatePlanAssociation(Context ctx,
            final BundleCategoryRatePlanAssociation apiRtp)
    {
        RatePlanAssociation crmRtp = null;
        try
        {
            crmRtp = (RatePlanAssociation) XBeans.instantiate(RatePlanAssociation.class, ctx);
        }
        catch (Exception e)
        {
            new MinorLogMsg(RatePlanAssociationToApiAdapter.class,
                    "Error instantiating new BundleCategory.  Using default constructor.", e).log(ctx);
            crmRtp = new RatePlanAssociation();
        }
        adaptApiToRatePlanAssociation(ctx, apiRtp, crmRtp);
        return crmRtp;
    }


    public static RatePlanAssociation adaptApiToRatePlanAssociation(Context ctx,
            final BundleCategoryRatePlanAssociation apiRtp, RatePlanAssociation crmRtp)
    {
        if (apiRtp.getBundleID() != null)
        {
            crmRtp.setBundleId(apiRtp.getBundleID());
        }
        if (apiRtp.getCategoryDescription() != null)
        {
            crmRtp.setCategoryDesc(apiRtp.getCategoryDescription());
        }
        if (apiRtp.getCategoryID() != null)
        {
            crmRtp.setCategoryId(apiRtp.getCategoryID().intValue());
        }
        if (apiRtp.getDataRatePlan() != null)
        {
            crmRtp.setDataRatePlan(apiRtp.getDataRatePlan());
        }
        if (apiRtp.getSmsRatePlan() != null)
        {
            crmRtp.setSmsRatePlan(apiRtp.getSmsRatePlan());
        }
        if (apiRtp.getSpid() != null)
        {
            crmRtp.setSpid(apiRtp.getSpid());
        }
        if (apiRtp.getVoiceRatePlan() != null)
        {
            crmRtp.setVoiceRatePlan(apiRtp.getVoiceRatePlan());
        }
        return crmRtp;
    }
}
