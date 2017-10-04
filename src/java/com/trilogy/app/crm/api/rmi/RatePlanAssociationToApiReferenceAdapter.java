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
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.BundleCategoryRatePlanAssociationReference;


/**
 * Adapts BundleProfile object to API objects.
 * 
 * @author bhupendra.pandey@redknee.com
 */
public class RatePlanAssociationToApiReferenceAdapter implements Adapter
{

    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
        return adaptRatePlanAssociationToReference(ctx, (RatePlanAssociation) obj);
    }


    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }


    public static BundleCategoryRatePlanAssociationReference adaptRatePlanAssociationToReference(Context ctx,
            final RatePlanAssociation crmRtp)
    {
        BundleCategoryRatePlanAssociationReference apiRtpRef = new BundleCategoryRatePlanAssociationReference();
        adaptRatePlanAssociationToReference(ctx, crmRtp, apiRtpRef);
        return apiRtpRef;
    }


    public static BundleCategoryRatePlanAssociationReference adaptRatePlanAssociationToReference(Context ctx,
            final RatePlanAssociation crmRtp, BundleCategoryRatePlanAssociationReference apiRtpRef)
    {
        apiRtpRef.setBundleID(crmRtp.getBundleId());
        apiRtpRef.setCategoryID(Long.valueOf(crmRtp.getCategoryId()));
        apiRtpRef.setIdentifier(crmRtp.getId());
        apiRtpRef.setSpid(crmRtp.getSpid());
        return apiRtpRef;
    }
}
