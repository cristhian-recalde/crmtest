/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee, no
 * unauthorised use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the licence agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright &copy; Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.contract;

import com.trilogy.app.crm.contract.SubscriptionContract;
import com.trilogy.app.crm.contract.SubscriptionContractTerm;
import com.trilogy.app.crm.contract.SubscriptionContractXInfo;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeOperationEnum;


/**
 * @since 2011-02-22
 */
public class ContractTermValidator implements Validator
{

    private static ContractTermValidator instance = new ContractTermValidator();


    public static ContractTermValidator instance()
    {
        return instance;
    }


    /**
     * @param ctx
     * @param obj
     * @throws IllegalStateException
     * @see com.redknee.framework.xhome.beans.Validator#validate(com.redknee.framework.xhome.context.Context,
     *      java.lang.Object)
     */
    @Override
    public void validate(Context ctx, Object obj) throws IllegalStateException
    {
        SubscriptionContractTerm contract = (SubscriptionContractTerm) obj;
        CompoundIllegalStateException el = new CompoundIllegalStateException();
        HomeOperationEnum op = (HomeOperationEnum) ctx.get(HomeOperationEnum.class);
        if (contract.getPrepaymentAmount() > 0)
        {
            if (contract.getPrePaymentLength() <= 0)
            {
                el.thrown(new IllegalArgumentException(
                        "If prepayment amount > 0, then PrePayment Length has to be > 0, "));
            }
        }
        if (contract.getBonusPeriodLength() > 0)
        {
            if (contract.getMaximumBound() <= 0)
            {
                el.thrown(new IllegalArgumentException("If bonus period length > 0, then bonus bound has to be > 0"));
            }
        }
        if (contract.getBonusPeriodLength() == 0 && contract.getMaximumBound() > 0)
        {
            el.thrown(new IllegalArgumentException("If bonus period length  = 0, then bonus bound has to be 0"));
        }
        if (contract.getPrePaymentLength() > 0 && (contract.getPrepaymentAmount() < contract.getMaximumBound()))
        {
            el.thrown(new IllegalArgumentException(
                    "Maximum bonus credit amount cannot exceed the Contract Payment Amount."));
        }
        
        if (contract.getContractLength() != SubscriptionContractSupport.DUMMY_SUBSCRIPITON_CONTRACT_LENGTH && contract.getContractLength() < contract.getPrePaymentLength())
        {
            el.thrown(new IllegalArgumentException(
                    "Length of Contract payment duration cannot exceed length of contract term"));
        }
        if (contract.getContractLength() < contract.getBonusPeriodLength())
        {
            el.thrown(new IllegalArgumentException("Bonus period length length cannot exceed contract length"));
        }
        if (op != null && op == HomeOperationEnum.REMOVE || op == HomeOperationEnum.REMOVE_ALL)
        {
            el.thrown(new IllegalArgumentException("Can not remove contract"));
        }
        validationForDummyContract(ctx, contract, el);
        canUpdate(ctx, contract, el);
        el.throwAll();
    }

    private void validationForDummyContract(final Context ctx, SubscriptionContractTerm contract, CompoundIllegalStateException el)
    {
      if (contract.getContractLength() == SubscriptionContractSupport.DUMMY_SUBSCRIPITON_CONTRACT_LENGTH)
      {
          
          if(contract.getRenewalCreditAmount() > AbstractSubscriptionContractTerm.DEFAULT_RENEWALCREDITAMOUNT)
          {
            el.thrown(new IllegalArgumentException("Renewal Credit Amount should be zero for dummy contract"));
          }
          if(contract.getPrepaymentAmount() > AbstractSubscriptionContractTerm.DEFAULT_PREPAYMENTAMOUNT)
          {
            el.thrown(new IllegalArgumentException("Contract Payment Amount should be zero for dummy contract"));
          }
          if(contract.getPrePaymentLength() > AbstractSubscriptionContractTerm.DEFAULT_PREPAYMENTLENGTH)
          {
            el.thrown(new IllegalArgumentException("Contract Payment Length should be zero for dummy contract"));
          }
          if(contract.getFlatPenaltyFee() > AbstractSubscriptionContractTerm.DEFAULT_FLATPENALTYFEE)
          {
            el.thrown(new IllegalArgumentException("Penalty fee should be zero for dummy contract"));
          }
          if(contract.getPenaltyFeePerMonth() > AbstractSubscriptionContractTerm.DEFAULT_PENALTYFEEPERMONTH)
          {
            el.thrown(new IllegalArgumentException("Penalty Fee Per Month should be zero for dummy contract"));
          }
          if(contract.getSubsidyAmount() > AbstractSubscriptionContractTerm.DEFAULT_SUBSIDYAMOUNT)
          {
            el.thrown(new IllegalArgumentException("Subsidy Amount should be zero for dummy contract"));
          }
          if(contract.getCancelPricePlan() != AbstractSubscriptionContractTerm.DEFAULT_CANCELPRICEPLAN)
          {
            el.thrown(new IllegalArgumentException("Not allow to select Default PricePlan after Contract Ends for dummy contract"));
          }
          if(contract.getPrepaymentRefund())
          {
            el.thrown(new IllegalArgumentException("Not allow Contract Payment refunded upon cancellation for dummy contract"));
          }
          if(contract.getProrateCancellationFees())
          {
            el.thrown(new IllegalArgumentException("Not allow Proration Cancellation fee for dummy contract"));
          }
          if(contract.getBonusPeriodLength() > AbstractSubscriptionContractTerm.DEFAULT_BONUSPERIODLENGTH)
          {
            el.thrown(new IllegalArgumentException("Bonus Period Length should be zero for dummy contract"));
          }
          if(contract.getMaximumBound() > AbstractSubscriptionContractTerm.DEFAULT_MAXIMUMBOUND)
          {
            el.thrown(new IllegalArgumentException("Maximum Bonus credit amount should be zero for dummy contract"));
          }
      }
    }
    private void canUpdate(final Context ctx, SubscriptionContractTerm contract, CompoundIllegalStateException el)
    {
        HomeOperationEnum op = (HomeOperationEnum) ctx.get(HomeOperationEnum.class);
        if (op != null && op == HomeOperationEnum.STORE)
        {
            try
            {
                long count = HomeSupportHelper.get(ctx).getBeanCount(ctx, SubscriptionContract.class,
                        new EQ(SubscriptionContractXInfo.CONTRACT_ID, contract.getId()));
                if (count > 0)
                {
                    if (checkDiff(ctx, contract, el) != ONLY_DISABLE_FLAG_CHANGED)
                    {
                        el.thrown(new IllegalStateException(
                                "  Contract can not be modified when subscription(s) in any states is/are assigned to the contract"));
                    }
                }
            }
            catch (HomeException homeEx)
            {
                el.thrown(new HomeException("Unable load the subscription contract "));
            }
        }
    }


    private int checkDiff(final Context ctx, SubscriptionContractTerm term, CompoundIllegalStateException el)
            throws HomeException
    {
        try
        {
            SubscriptionContractTerm origTerm = HomeSupportHelper.get(ctx).findBean(ctx,
                    SubscriptionContractTerm.class, new EQ(SubscriptionContractTermXInfo.ID, term.getId()));
            if (origTerm.isDisable() != term.isDisable())
            {
                boolean changed = false;
                if(!origTerm.getName().equals(term.getName()))
                {
                    changed = true;
                }
                if (origTerm.getPrepaymentAmount() != term.getPrepaymentAmount())
                {
                    changed = true;
                }
                if (origTerm.getPrepaymentRefund() != term.getPrepaymentRefund())
                {
                    changed = true;
                }
                if (origTerm.getBonusPeriodLength() != term.getBonusPeriodLength())
                {
                    changed = true;
                }
                if (origTerm.getCancelPricePlan() != term.getCancelPricePlan())
                {
                    changed = true;
                }
                if (origTerm.getContractLength() != term.getContractLength())
                {
                    changed = true;
                }
                if (origTerm.getContractPricePlan() != term.getContractPricePlan())
                {
                    changed = true;
                }
                if (origTerm.getFlatPenaltyFee() != term.getFlatPenaltyFee())
                {
                    changed = true;
                }
                if (origTerm.getMaximumBound() != term.getMaximumBound())
                {
                    changed = true;
                }
                if (origTerm.getPenaltyFeePerMonth() != term.getPenaltyFeePerMonth())
                {
                    changed = true;
                }
                if (origTerm.getPrePaymentLength() != term.getPrePaymentLength())
                {
                    changed = true;
                }
                if (origTerm.getSpid() != term.getSpid())
                {
                    el.thrown(new IllegalArgumentException("Can not change spid after creation "));
                    changed = true;
                }
                if (origTerm.getSubscriberType() != term.getSubscriberType())
                {
                    changed = true;
                }
                if (origTerm.getSubscriptionType() != term.getSubscriptionType())
                {
                    changed = true;
                }
                if (origTerm.getTaxAuthority() != term.getTaxAuthority())
                {
                    changed = true;
                }
                if (changed)
                {
                    return MORE_THAN_ONE_PROPERTY_CHANGED;
                }
                return ONLY_DISABLE_FLAG_CHANGED;
            }
        }
        catch (HomeException homeEx)
        {
            throw homeEx;
        }
        return NOTHING_CHANGED;
    }

    private final static int ONLY_DISABLE_FLAG_CHANGED = 1;
    private final static int MORE_THAN_ONE_PROPERTY_CHANGED = 2;
    private final static int NOTHING_CHANGED = 3;
}
