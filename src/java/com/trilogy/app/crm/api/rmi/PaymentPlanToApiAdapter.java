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

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.util.crmapi.wsdl.v2_2.types.invoice.PaymentPlanReference;
import com.trilogy.util.crmapi.wsdl.v2_2.types.invoice.PaymentPlanState;

/**
 * Adapts Contract object to API objects.
 *
 * @author victor.stratan@redknee.com
 */
public class PaymentPlanToApiAdapter implements Adapter
{
    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
        return adaptPaymentPlanToReference((com.redknee.app.crm.bean.payment.PaymentPlan) obj);
    }

    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }

    public static com.redknee.util.crmapi.wsdl.v2_2.types.invoice.PaymentPlan adaptPaymentPlanToApi(final com.redknee.app.crm.bean.payment.PaymentPlan paymentPlan)
    {
        final com.redknee.util.crmapi.wsdl.v2_2.types.invoice.PaymentPlan apiPaymentPlan
        = new com.redknee.util.crmapi.wsdl.v2_2.types.invoice.PaymentPlan();
        apiPaymentPlan.setCreditLimitDecrease(paymentPlan.getCreditLimitDecrease());
        apiPaymentPlan.setDescription(paymentPlan.getDesc());
        apiPaymentPlan.setIdentifier(paymentPlan.getIdentifier());
        apiPaymentPlan.setName(paymentPlan.getName());
        apiPaymentPlan.setNumberOfPayments(paymentPlan.getNumOfPayments());
        apiPaymentPlan.setSpid(paymentPlan.getSpid());
        apiPaymentPlan.setState(com.redknee.util.crmapi.wsdl.v2_2.types.invoice.PaymentPlanStateEnum.valueOf(paymentPlan.getState().getIndex()));
        return apiPaymentPlan;
    }

    public static PaymentPlanReference adaptPaymentPlanToReference(final com.redknee.app.crm.bean.payment.PaymentPlan paymentPlan)
    {
        final PaymentPlanReference reference = new PaymentPlanReference();
        adaptPaymentPlanToReference(paymentPlan, reference);

        return reference;
    }

    public static PaymentPlanReference adaptPaymentPlanToReference(final com.redknee.app.crm.bean.payment.PaymentPlan paymentPlan, final PaymentPlanReference reference)
    {
        reference.setIdentifier(paymentPlan.getIdentifier());
        reference.setDescription(paymentPlan.getDesc());
        reference.setSpid(paymentPlan.getSpid());
        reference.setState(com.redknee.util.crmapi.wsdl.v2_2.types.invoice.PaymentPlanStateEnum.valueOf(paymentPlan.getState().getIndex()));
        reference.setName(paymentPlan.getName());
        
        
        
        return reference;
    }
}
