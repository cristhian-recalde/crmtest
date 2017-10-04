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

import com.trilogy.app.crm.bean.BillCycle;
import com.trilogy.app.crm.support.CollectionSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.util.crmapi.wsdl.v2_0.types.BillingMessageReference;
import com.trilogy.util.crmapi.wsdl.v2_2.types.invoice.BillCycleReference;

/**
 * Adapts CreditCategory object to API objects.
 *
 * @author victor.stratan@redknee.com
 */
public class BillCycleToApiAdapter implements Adapter
{
    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
        return adaptBillCycleToReference((BillCycle) obj);
    }

    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }

    public static  com.redknee.util.crmapi.wsdl.v2_2.types.invoice.BillCycle adaptBillCycleToApi(final Context ctx, final BillCycle billCycle) throws HomeException
    {
        final  com.redknee.util.crmapi.wsdl.v2_2.types.invoice.BillCycle cycle;
        cycle = new  com.redknee.util.crmapi.wsdl.v2_2.types.invoice.BillCycle();
        adaptBillCycleToReference(billCycle, cycle);
        
        BillingMessageReference[] billingMessages = new BillingMessageReference[] {};
        
        cycle.setBillingMessage(
                CollectionSupportHelper.get(ctx).adaptCollection(
                        ctx, 
                        billCycle.getBillingMessages(), 
                        new BillingMessageToApiAdapter(), 
                        billingMessages));

        return cycle;
    }

    public static BillCycleReference adaptBillCycleToReference(final BillCycle billCycle)
    {
        final BillCycleReference reference = new BillCycleReference();
        adaptBillCycleToReference(billCycle, reference);

        return reference;
    }

    public static BillCycleReference adaptBillCycleToReference(final BillCycle billCycle,
            final BillCycleReference reference)
    {
        reference.setIdentifier(billCycle.getIdentifier());
        reference.setSpid(billCycle.getSpid());
        reference.setDescription(billCycle.getDescription());
        reference.setDayOfMonth(billCycle.getDayOfMonth());

        return reference;
    }
}
