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

import com.trilogy.app.crm.contract.SubscriptionContract;
import com.trilogy.app.crm.contract.SubscriptionContractTerm;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.util.crmapi.wsdl.v2_1.types.subscription.SubscriptionReference;
import com.trilogy.util.crmapi.wsdl.v2_2.types.subscription.extensions.PPSMSupporterSubscriptionExtensionReference;
import com.trilogy.util.crmapi.wsdl.v2_2.types.subscription.extensions.ReadOnlyPPSMSupporterSubscriptionExtension;


/**
 * Adapts SubscriptionContract object to API objects.
 * 
 * @author kumaran.sivasubramaniam@redknee.com
 * @since 9.0
 */
public class SubscriptionContractTermToApiAdapter implements Adapter
{

    public static com.redknee.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionContractReference toAPIReference(
            final Context ctx, final SubscriptionContractTerm contract) throws HomeException
    {
        if (contract != null)
        {
            final com.redknee.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionContractReference apiContract = new com.redknee.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionContractReference();
            apiContract.setSpid(contract.getSpid());
            apiContract.setAdjustmentTypeID(contract.getContractAdjustmentTypeId());
            apiContract.setIdentifier(contract.getId());
            apiContract.setName(contract.getName());
            apiContract.setPaidType(contract.getSubscriberType().getIndex());
            apiContract.setPeriod(contract.getContractLength());
            apiContract.setPrepaymentAmount(contract.getPrepaymentAmount());
            apiContract.setPrepaymentPeriod(contract.getPrePaymentLength());
            apiContract.setSubscriptionType((int) contract.getSubscriptionType());
            // apiContract.
            return apiContract;
        }
        return null;
    }


    public static com.redknee.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionContract adaptToApi(
            final Context ctx, final Object obj)
    {
        SubscriptionContractTerm subContract = (SubscriptionContractTerm) obj;
        if (subContract != null)
        {
            final com.redknee.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionContract apiContract = new com.redknee.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionContract();
            apiContract.setBonusPeriod(subContract.getBonusPeriodLength());
            apiContract.setDefaultPostContractPricePlanID(subContract.getCancelPricePlan());
            apiContract.setDefaultPricePlanID(subContract.getContractPricePlan());
            apiContract.setMaximumBonusAmount(subContract.getMaximumBound());
            apiContract.setMinimumPenaltyAmount(subContract.getFlatPenaltyFee());
            apiContract.setPerMonthPenaltyAmount(subContract.getPenaltyFeePerMonth());
            apiContract.setPolicySummary(subContract.getContractPolicySummary());
            apiContract.setPrepaymentRefunded(subContract.isPrepaymentRefund());
            apiContract.setSpid(subContract.getSpid());
            apiContract.setAdjustmentTypeID(subContract.getContractAdjustmentTypeId());
            apiContract.setIdentifier(subContract.getId());
            apiContract.setName(subContract.getName());
            apiContract.setPaidType(subContract.getSubscriberType().getIndex());
            apiContract.setPeriod(subContract.getContractLength());
            apiContract.setPrepaymentAmount(subContract.getPrepaymentAmount());
            apiContract.setPrepaymentPeriod(subContract.getPrePaymentLength());
            apiContract.setSubscriptionType((int) subContract.getSubscriptionType());
            // apiContract.
            return apiContract;
        }
        return null;
    }


    /**
     * {@inheritDoc}
     */
    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
        return adaptToApi(ctx, (SubscriptionContractTerm) obj);
    }


    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }
}
