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
package com.trilogy.app.crm.api.rmi;

import java.util.ArrayList;

import com.trilogy.app.crm.bundle.BundleAdjustment;
import com.trilogy.app.crm.bundle.BundleAdjustmentItem;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.BundleAdjustmentOperationTypeEnum;

/**
 * 
 *
 * @author Marcio Marques
 * @since 3.0
 */
public class BundleAdjustmentToApiAdapter
{
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;


    /**
     * {@inheritDoc}
     */
    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
        return adaptBundleAdjustmentToApi(ctx, (BundleAdjustment) obj, new com.redknee.util.crmapi.wsdl.v3_0.types.serviceandbundle.BundleAdjustment());
    }


    /**
     * {@inheritDoc}
     */
    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        return adaptApiToBundleAdjustment(ctx, (com.redknee.util.crmapi.wsdl.v3_0.types.serviceandbundle.BundleAdjustment) obj, new BundleAdjustment());
    }


    public static com.redknee.util.crmapi.wsdl.v3_0.types.serviceandbundle.BundleAdjustment adaptBundleAdjustmentToApi(
            final Context ctx, final BundleAdjustment crmBundleAdjustment,
            final com.redknee.util.crmapi.wsdl.v3_0.types.serviceandbundle.BundleAdjustment apiBundleAdjustment)
    {
        if (crmBundleAdjustment.getItems().size() == 0)
        {
            return null;
        }
        else if (crmBundleAdjustment.getItems().size() == 1)
        {
            BundleAdjustmentItem item = (BundleAdjustmentItem) crmBundleAdjustment.getItems().iterator().next();
            apiBundleAdjustment.setAmount(item.getAmount());
            apiBundleAdjustment.setBundleIdentifier(item.getBundleProfile());
            apiBundleAdjustment.setOperation(BundleAdjustmentOperationTypeEnum.valueOf(item.getType().getIndex()));
        }
        else
        {
            throw new UnsupportedOperationException();
        }

        return apiBundleAdjustment;
    }


    public static BundleAdjustment adaptApiToBundleAdjustment(final Context ctx,
            final com.redknee.util.crmapi.wsdl.v3_0.types.serviceandbundle.BundleAdjustment apiBundleAdjustment,
            final BundleAdjustment crmBundleAdjustment)
    {
        BundleAdjustmentItem crmItem = new BundleAdjustmentItem();
        crmItem.setBundleProfile(apiBundleAdjustment.getBundleIdentifier());
        crmItem.setAmount(apiBundleAdjustment.getAmount());
        crmItem.setType(com.redknee.app.crm.bundle.BundleAdjustmentTypeEnum.get((short) apiBundleAdjustment.getOperation().getValue())); 
        crmBundleAdjustment.setItems(new ArrayList<BundleAdjustmentItem>());
        crmBundleAdjustment.getItems().add(crmItem);
        return crmBundleAdjustment;
    }

}
