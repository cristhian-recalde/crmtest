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
package com.trilogy.app.crm.transaction;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.trilogy.framework.core.locale.Currency;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.calculation.service.CalculationService;
import com.trilogy.app.crm.support.PaymentPlanSupportHelper;
import com.trilogy.util.snippet.log.Logger;

/**
 * Like AccountPaymentDistribution class this Payment Distribution implementation determines
 * the amount to pay to Payment Plan Installment charges and what to pay to the Payment Plan remaining 
 * balance (as an overpayment).
 * 
 * The distribution of the Payment Plan payments are done as separate steps because the priority states
 * that the subscriptions new charges be paid off after the Payment Plan Installment charges:
 * 
 * See DDD for details at
 * onenote:http://redportal/sites/Redknee%20R_D%20Portal/convergedbilling/CRM%20Detail%20Design%20Documents/Payment%20Plan.one#Detail%20Design&section-id={86D80C9F-4AE1-4B0A-B011-B3B73C651C87}&page-id={199D8570-34B7-4F89-8300-E5CA51D7F1B4}&object-id={27DFD941-8A8C-04DE-133F-E43DFE7ACE3A}&3D
 * 
 * @author angie.li@redknee.com
 *
 */
public class PaymentPlanLoanPaymentDistribution extends AbstractPaymentDistribution
{

    public PaymentPlanLoanPaymentDistribution(final Context ctx, 
            final Account account,
            final Subscriber assignee)
    {
        transactionProcessingDate_ = new Date(); // or could this be trans.getReceiveDate() ?
        account_ = account;
        paymentPlanStartDate_ = account.getPaymentPlanStartDate(ctx, transactionProcessingDate_);
        accountHasPaymentPlan_ = PaymentPlanSupportHelper.get(ctx).isEnabled(ctx) && PaymentPlanSupportHelper.get(ctx).isValidPaymentPlan(ctx, account.getPaymentPlan());
        remaingSubscriberAssigneeID_ = assignee.getId();
        subscriberDistributions_ = new HashMap();
    }
    
    /**
     * Distribute Payment For Payment Plan Installments Charged.
     * This method will distribute as much of the payment that can be made to each Subscription based on its percentage
     * of the total Payment Plan Installments (at the Account level). 
     * This method is ignorant of the Subscription which will receive the Remaining assignment.  So any remainder 
     * of the original payment left due to rounding issues is allocated by calling the "additionalInstallmentChargesPaymentToSubscriber"
     * method 
     * 
     * @param ctx
     * @param paymentAmount should be the originalPaymentAmount, since Payments to Payment Plan charges are prioritized
     * @throws HomeException
     */
    public void distributePaymentForCharges(Context ctx, long paymentAmount)
        throws HomeException 
    {
        if (accountHasPaymentPlan_)
        {
            if (Logger.isDebugEnabled())
            {
                Logger.debug(ctx, this, "Account " + account_.getBAN() + " is enrolled in payment plan. Begin Calculation of payment allocation to Payment Plan Adjustments charges.");
            }
            
            try
            {                
                //AccountPaymentPlanCalculation accountCalculation = null;//getPaymentPlanCalculation(ctx);
                
                /* Make payments for only the Amount of Payment Plan Charges already applied to the Account.
                 * Cap the payment amount to the Total of Payment Plan Charges for the Account. 
                 * Since paymentAmount is a negative value, we have to change the sign on the Total Value of Payment Plan Charges.*/
                CalculationService service = (CalculationService) ctx.get(CalculationService.class);
                long unpaidPaymentLoanAdjustments = service.getAccountDistrUnpaidPaymentPlanLoanAdjustments(ctx,
                        account_.getBAN(), paymentPlanStartDate_,
                        transactionProcessingDate_);
                long loanPaymentAmount = Math.max(paymentAmount, - unpaidPaymentLoanAdjustments);
                
                Collection<String> ppSubscriptions = service.getSubscribersInAccountForPaymentDistribution(ctx,
                        account_.getBAN(), paymentPlanStartDate_,
                        transactionProcessingDate_);
                
                for (String subscriberId : ppSubscriptions)
                {
                    long subsUnpaidPaymentLoanAdjustments = service.getSubscriberDistrUnpaidPaymentPlanLoanAdjustments(ctx,
                            account_.getBAN(), subscriberId, paymentPlanStartDate_,
                            transactionProcessingDate_);
                    long subUnpaidPaymentUnchargedLoanRemainder = service.getSubscriberDistrPaymentPlanUnchargedLoanRemainder(ctx,
                            account_.getBAN(), subscriberId, paymentPlanStartDate_,
                            transactionProcessingDate_);
                    if (Logger.isDebugEnabled())
                    {
                        Logger.debug(ctx, this, "subsUnpaidPaymentLoanAdjustments = " + subsUnpaidPaymentLoanAdjustments
                        		+ " and subUnpaidPaymentUnchargedLoanRemainder = " + subUnpaidPaymentUnchargedLoanRemainder + " for subscriberId = " + subscriberId);
                    }
                    SubscriberPaymentPlanLoanPaymentDistribution distribution = getSubscriptionDistribution(ctx,
                            subscriberId, subsUnpaidPaymentLoanAdjustments, subUnpaidPaymentUnchargedLoanRemainder);
                    //Allocate Payment To Payment Plan Loan Charges
                    
                    
                    
                    //Calculate this subscriber's percentage (ratio) of the larger account loan, apply it to the original transaction amount.
                    long subsPaymentPlanChargesDue = subsUnpaidPaymentLoanAdjustments;
                    long accountPaymentPlanChargesDue = unpaidPaymentLoanAdjustments;
                    double ratio = 0;
                    if (accountPaymentPlanChargesDue != 0)
                    {
                            ratio = (((float)subsPaymentPlanChargesDue)/accountPaymentPlanChargesDue);
                    }
                    if (Logger.isDebugEnabled())
                    {
                        Logger.debug(ctx, this, "Ratio of distribution = " + ratio + " for subscriberId = " + subscriberId);
                    }
                    // For comparison with subsPaymentPlanChargesDue it must be the absolute value
                    long subscriberAllocatedPayment = Double.valueOf(loanPaymentAmount * ratio).longValue();
                    
                    distribution.setPaymentForOutStandOwing(subscriberAllocatedPayment);
                    
                    this.paymentForOutStandOwing += distribution.getPaymentForOutStandOwing();
                                        
                    if (Logger.isDebugEnabled())
                    {
                        Logger.debug(ctx, this, "Distribution of Payment to balance Payment Plan charges has determined Subscriber="
                                + subscriberId + " receives a payment allocation as follows: "
                                + distribution.appendDistributionDetails());
                    }
                }
                
                /* If the original payment could not be evenly split, then take care of the remainder now.
                 * Strictly speaking overPayment will be 0 at this point, so we reduce the following long form of 
                 * the formula to what is not commented out.
                 * 
                 * long remainder = paymentAmount - this.paymentForOutStandOwing - this.overPayment;
                 */
                long remainder = paymentAmount - this.paymentForOutStandOwing;
                applyPaymentRemainderToInstallmentCharges(ctx, remainder);
                
                if (Logger.isDebugEnabled())
                {
                    Logger.debug(ctx, this, "Finished calculating the payment proportions due to Payment Plan for Account " 
                            + account_.getBAN() + ".  Next, the Transactions will be created against these amounts.");
                }
            }
            catch (Exception e)
            {
                if (e.getCause() instanceof HomeException)
                {
                    throw (HomeException) e.getCause();
                }
                else
                {
                    throw new HomeException(e);
                }
            }
        }
        else
        {
            if (Logger.isDebugEnabled())
            {
                Logger.debug(ctx, this, "Skipped PaymentPlanLoanPaymentDistribution since the account " 
                    + account_.getBAN() + " is not enrolled in Payment Plan.", null);
            }
        }
    }
    
    /**
     * Distribute the given amount as a Payment Plan Over Payment.
     * For the Payment Plan case, the regular charges payment is paid before the over payment case.
     * @param ctx
     * @param transHome
     * @param originalTransaction
     */
    public void distributeOverPayment(Context ctx, final long accountOverPaymentAmount)
    throws HomeException
    {
        if (accountHasPaymentPlan_)
        {
            if (Logger.isDebugEnabled())
            {
                Logger.debug(ctx, this, "Begin distributing Payment Plan Over Payment");
            }
            
            try
            {
                
                //AccountPaymentPlanCalculation accountCalculation = null;//getPaymentPlanCalculation(ctx);
                
                CalculationService service = (CalculationService) ctx.get(CalculationService.class);
                long unpaidPaymentUnchargedLoanRemainder = service.getAccountDistrPaymentPlanUnchargedLoanRemainder(ctx,
                        account_.getBAN(), paymentPlanStartDate_,
                        transactionProcessingDate_);    
                if (Logger.isDebugEnabled())
                {
                    Logger.debug(ctx, this, "Account unpaid  payment uncharged loan remainder = "
                            + unpaidPaymentUnchargedLoanRemainder);
                }
                if(unpaidPaymentUnchargedLoanRemainder == 0)
                {
                	return;
                }
                Collection<String> ppSubscriptions = service.getSubscribersInAccountForPaymentDistribution(ctx,
                        account_.getBAN(), paymentPlanStartDate_,
                        transactionProcessingDate_);
                
                for (String subscriberId : ppSubscriptions)
                {
                    long subsUnpaidPaymentLoanAdjustments = service.getSubscriberDistrUnpaidPaymentPlanLoanAdjustments(ctx,
                            account_.getBAN(), subscriberId, paymentPlanStartDate_,
                            transactionProcessingDate_);
                    long subUnpaidPaymentUnchargedLoanRemainder = service.getSubscriberDistrPaymentPlanUnchargedLoanRemainder(ctx,
                            account_.getBAN(), subscriberId, paymentPlanStartDate_,
                            transactionProcessingDate_);
                    
                    if (Logger.isDebugEnabled())
                    {
                        Logger.debug(ctx, this, "subsUnpaidPaymentLoanAdjustments = " + subsUnpaidPaymentLoanAdjustments
                                + ", subUnpaidPaymentUnchargedLoanRemainder = " + subUnpaidPaymentUnchargedLoanRemainder);
                    }
                    
                    SubscriberPaymentPlanLoanPaymentDistribution distribution = getSubscriptionDistribution(ctx,
                            subscriberId, subsUnpaidPaymentLoanAdjustments, subUnpaidPaymentUnchargedLoanRemainder);
                    //allocate Payment To Payment Plan Loan Charges
                    
                    //Calculate this subscriber's ratio of the larger account loan (not yet charged), apply it to the original transaction amount.
                    long payment = (accountOverPaymentAmount
                            * subUnpaidPaymentUnchargedLoanRemainder / unpaidPaymentUnchargedLoanRemainder);
                    
                    if(payment < subUnpaidPaymentUnchargedLoanRemainder)
                    {
                    	/*
                    	 * payment and subUnpaidPaymentUnchargedLoanRemainder are -ive values.
                    	 * Therefore, less than check is done. subUnpaidPaymentUnchargedLoanRemainder cannot be greater than 0.
                    	 * payment is credit/payment being done, therefore, it has to be negative (system assumption).
                    	 */
                    	payment = subUnpaidPaymentUnchargedLoanRemainder;
                    }
                    distribution.loanBucketRemaining = distribution.loanBucketRemaining - payment;
                    distribution.setOverPayment(payment);
                    
                    this.overPayment += payment;
                    
                    if (Logger.isDebugEnabled())
                    {
                        Logger.debug(ctx, this, "Distribution of Over Payment to Payment Plan Loan has determined Subscriber="
                                + subscriberId + " receives an over payment allocation as follows: "
                                + distribution.appendDistributionDetails());
                    }
                }
                if (Logger.isDebugEnabled())
                {
                    Logger.debug(ctx, this, "Initial Amount applicable for over payment = "
                            + accountOverPaymentAmount + " , Amount actually over paid in payment plan = "
                            + this.overPayment);
                }
                /* If the original payment could not be evenly split, then take care of the remainder now.
                 */
                long remainder = accountOverPaymentAmount - this.overPayment;
                applyPaymentRemainderToOverPayment(ctx, remainder);
                
                //Any left over remainder after this point has to be applied as a Regular Over Payment. 
            }
            catch (Exception e)
            {
                if (e.getCause() instanceof HomeException)
                {
                    throw (HomeException) e.getCause();
                }
                else
                {
                    throw new HomeException(e);
                }
            }
        }
        else
        {
            if (Logger.isDebugEnabled())
            {
                Logger.debug(ctx, this, "Skipped PaymentPlan Loan Payment Over Payment Distribution since the account " 
                    + account_.getBAN() + " is not enrolled in Payment Plan.", null);
            }
        }
    }
    
    public void createDistributions(Context ctx, 
            final Transaction orignalTransaction, 
            Home transHome, 
            final Currency currency)
    {
        for (Iterator i = subscriberDistributions_.values().iterator(); i.hasNext();)
        {
            final Context subContext = ctx.createSubContext();
            if (i.hasNext())
            {
                /*
                 * TT8041500041: Don't let dunning run unless it's the last transaction.
                 * If we are paying a mix of Payment Plan and Regular charges this parameter
                 * might already have been set to TRUE previously.
                 */
                subContext.put(Common.DUNNING_EXEMPTION, Boolean.TRUE);
            }
            
            SubscriberPaymentPlanLoanPaymentDistribution subDistribution = (SubscriberPaymentPlanLoanPaymentDistribution)i.next();
            if (subDistribution.createTransactions(subContext, orignalTransaction, transHome, currency) )
            {
                this.successAmount += subDistribution.getPaymentForOutStandOwing() + subDistribution.getOverPayment(); 
                ++this.successCount; 
            } else 
            {
                this.failedAmount += subDistribution.getPaymentForOutStandOwing() + subDistribution.getOverPayment(); 
                ++this.failedCount; 

            }
        }
    }

    /**
     * Retrieve the SubscriberPaymentPlanLoanPaymentDistribution for the given subscriber
     * from the distributions map, if none exists then create one and place it in the map.
     * @param ctx
     * @param calculation
     * @return
     */
    private SubscriberPaymentPlanLoanPaymentDistribution getSubscriptionDistribution(
            Context ctx, String subscriberId,
             long outStandingOwing,  long loanBucketRemaining) 
    {        
        SubscriberPaymentPlanLoanPaymentDistribution subDistribution = getSubscriptionDistribution(ctx, subscriberId);
        if ( subDistribution == null)
        {
            subDistribution = new SubscriberPaymentPlanLoanPaymentDistribution(ctx, subscriberId, outStandingOwing,
                    loanBucketRemaining);
            subscriberDistributions_.put(subscriberId, subDistribution);
        }
        else
        {
            if (Logger.isDebugEnabled())
            {
                Logger.debug(ctx, this, "SubscriberPaymentPlanLoanPaymentDistribution record existed in the context for Subscriber="
                        + subscriberId + ". Subscription's"
                        + subDistribution.appendDistributionDetails());
            }    
        }
        return subDistribution; 
    }
    
    /**
     * Retrieve the SubscriberPaymentPlanLoanPaymentDistribution for the given subscriber
     * from the distributions map.
     * @param ctx
     * @param sub 
     * @return could be NULL if no SubscriberPaymentPlanLoanPaymentDistribution exists in the map
     */
    private SubscriberPaymentPlanLoanPaymentDistribution getSubscriptionDistribution(
            Context ctx, final String subscriberId) 
    {
        SubscriberPaymentPlanLoanPaymentDistribution subDistribution = 
            (SubscriberPaymentPlanLoanPaymentDistribution)subscriberDistributions_.get(subscriberId);
        if ( subDistribution == null)
        {
            if (Logger.isDebugEnabled())
            {
                Logger.debug(ctx, this, 
                        "At this point the SubscriberPaymentPlanLoanPaymentDistribution should not be missing.  Subscriber=" + subscriberId);    
            }
        }
        return subDistribution; 
    }
    
    public long getSubscriptionLoanPayment(
            Context ctx, final String subscriberId) 
    {
        SubscriberPaymentPlanLoanPaymentDistribution subDistribution = 
            (SubscriberPaymentPlanLoanPaymentDistribution)subscriberDistributions_.get(subscriberId);
        if ( subDistribution == null)
        {
            if (Logger.isDebugEnabled())
            {
                Logger.debug(ctx, this, 
                        "The SubscriberPaymentPlanLoanPaymentDistribution is not present for Subscriber=" + subscriberId);    
            }
            return 0;
        }
        else
        {
        	return subDistribution.getPaymentForOutStandOwing();
        }
    }

    /**
     * Returns the Payment Plan Calculations for this Account hierarchy
     * @param context
     * @return
     * @throws CalculationException
     */
    /*protected AccountPaymentPlanCalculation getPaymentPlanCalculation(Context context)
        throws CalculationException
    {
        if (paymentPlanCalculation_ == null)
        {
            paymentPlanCalculation_ = new AccountPaymentPlanCalculation(context, 
                    account_, paymentPlanStartDate_, transactionProcessingDate_);
        }
        return paymentPlanCalculation_;
    }*/
    
    /**
     * Distribute any payment amount that could not be evenly split among the payable subscribers
     * to the one subscriber assigned for the remainder.
     * It is important to separate payment at this point to the Payment Plan Over Payment because
     * CRM should address the Account's OBO (non-Payment Plan charges) before addressing the Payment Plan
     * Over Payments.
     *  
     * @param ctx
     * @param sub
     * @param remaining
     * @throws HomeException
     */
    private void applyPaymentRemainderToInstallmentCharges(Context ctx, long remaining)
    throws HomeException
    {
        if ( remaining == 0)
        {
            return;     
        }
        //the subscriber to be assigned any remainder in the payment
        final String subscriberID = this.remaingSubscriberAssigneeID_;
        SubscriberPaymentPlanLoanPaymentDistribution subDistribution = getSubscriptionDistribution(ctx, subscriberID); 
        if (subDistribution == null)
        {
            throw new HomeException("Failed to find the Subscriber to assign the Payment Plan remaining payment. Subscriber inquiry=" + subscriberID);
        }

        long subOutstandingOwingRemains = subDistribution.outStandingOwing + subDistribution.paymentForOutStandOwing; 

        if (subOutstandingOwingRemains > 0)
        {
            if ( (remaining * -1) <= subOutstandingOwingRemains )
            {
                additionalInstallmentChargesPaymentToSubscriber( ctx, subDistribution, remaining); 
            } 
            else 
            {   
                //Pay only outstanding Payment Plan Installment charges.
                long amountForInstallmentCharges = subOutstandingOwingRemains * -1;
                additionalInstallmentChargesPaymentToSubscriber(ctx, subDistribution, amountForInstallmentCharges);
            }
        }
        //else this remainder should be applied next to the Account's OBO (non-Payment Plan charges).
        
        if (Logger.isDebugEnabled())
        {
            Logger.debug(ctx, this, "Assigning Payment remainder for Payment Plan has determined Subscriber="
                    + subscriberID + " receives a payment allocation as follows: "
                    + subDistribution.appendDistributionDetails());
        }
    }
    
    /**
     * Distribute any Over Payment amount that could not be evenly split among the payable subscribers
     * to the one subscriber assigned for the remainder.
     * 
     * After this method is run there may still be remainder left to be distributed as a 
     * Regular Subscriber Over Payment.
     * @param ctx
     * @param sub
     * @param remaining
     * @throws HomeException
     */
    private void applyPaymentRemainderToOverPayment(Context ctx, long remaining)
    throws HomeException
    {
        if ( remaining == 0)
        {
            return;     
        }
        final String subscriberID = this.remaingSubscriberAssigneeID_;
        SubscriberPaymentPlanLoanPaymentDistribution subDistribution = getSubscriptionDistribution(ctx, subscriberID); 
        if (subDistribution == null)
        {
            throw new HomeException("Failed to find the Subscriber to assign the Payment Plan remaining payment.");
        }

        if(subDistribution.loanBucketRemaining > remaining)
        {
            additionalOverPaymentToSubscriber(ctx, subDistribution, subDistribution.loanBucketRemaining); 
        }
        else
        {
            additionalOverPaymentToSubscriber(ctx, subDistribution, remaining);
        }
        
        if (Logger.isDebugEnabled())
        {
            Logger.debug(ctx, this, "Assigning Over Payment remainder for Payment Plan has determined Subscriber="
                    + subscriberID + " receives an over payment allocation as follows: "
                    + subDistribution.appendDistributionDetails());
        }
    }
    
    /**
     * Accumulate the given Payment remainder to the given Subscriber's Over Payment amount.
     * Keep track of this allocation by updating the Account's Distribution totals.
     * 
     * @since 8.2 [Angie Li] this method very similar to the "additionalOverPaymentToSubscriber" method
     * in AccountPaymentDistribution.  I tried to refactor this into the AbstractPaymentDistribution, 
     * but in the end it did not make sense as a SubscriberPaymentDistribution wouldn't need it in 
     * this form.  I could not make the method generic enough for multiple reuse -- it would loose
     * its usefulness.
     * 
     * @param ctx
     * @param subDistribution
     * @param remaining
     * @throws HomeException
     */
    private void additionalOverPaymentToSubscriber(final Context ctx, 
            final SubscriberPaymentPlanLoanPaymentDistribution subDistribution, 
            final long remaining)
    throws HomeException
    {
        long overPayment = subDistribution.overPayment + remaining;     
        subDistribution.setOverPayment(overPayment);
        this.overPayment += remaining; 
    }


    /**
     * Accumulate the given Payment remainder to pay the Subscriber's Payment Plan Installment Charges. 
     * Keep track of this allocation by updating the Account's Distribution totals.
     * 
     * @since 8.2 [Angie Li] this method very similar to the "additionalOBOPaymentToSubscriber" method
     * in AccountPaymentDistribution.  I tried to refactor this into the AbstractPaymentDistribution, 
     * but in the end it did not make sense as a SubscriberPaymentDistribution wouldn't need it in 
     * this form.  I could not make the method generic enough for multiple reuse -- it would loose
     * its usefulness.
     * 
     * @param ctx
     * @param subDistribution
     * @param remaining
     * @throws HomeException
     */
    private void additionalInstallmentChargesPaymentToSubscriber(final Context ctx, 
            final SubscriberPaymentPlanLoanPaymentDistribution subDistribution, 
            final long remaining)
    throws HomeException
    {
        long payment = subDistribution.paymentForOutStandOwing + remaining;  
        subDistribution.setPaymentForOutStandOwing(payment);
        this.paymentForOutStandOwing += remaining;
    }
    

    /**
     * Returns the details of the given this PaymentPlanLoanPaymentDistribution
     * @param subDistribution
     * @return
     */
    public String appendDistributionDetails() 
    {
        StringBuilder str = new StringBuilder();
        str.append(" Account's Payment Plan Outstanding Owing="); 
        str.append(this.getOutStandingOwing());
        str.append(", Payment towards Payment Plan Charges=");
        str.append(this.getPaymentForOutStandOwing());
        str.append(", Payment towards Payment Plan Overpayment=");
        str.append(this.getOverPayment());
        return str.toString();
    }

    
    final protected Account account_; 
    
    /**
     * The Outstanding Balance Owing of the Payment Plan Loan are the 
     * loan fees that have been invoiced. 
     */
    protected Collection OBOPayableSubscribers_;
    /**
     * The Over Payment for an Payment Plan Loan are payments made 
     * against the loan amount not yet invoiced.
     */
    protected Collection overPaymentPayableSubscribers_; 
    
    /**
     * Collection of Subscriber Payment Plan Loan Payment distributions
     * Includes details on the OBO Loan payment and Over Payment.
     */
    protected Map subscriberDistributions_; 
    
    /**
     * Date of the start of the Payment Plan Loan Program
     */
    protected Date paymentPlanStartDate_; 
    
    /**
     * Validation of whether this account is in the Payment Plan Program
     */
    protected boolean accountHasPaymentPlan_;
    
    /**
     * The time stamp when the payment is being processed.  Also, we can think of this
     * as the transaction received time stamp.
     */
    protected Date transactionProcessingDate_;
    
    // protected AccountPaymentPlanCalculation paymentPlanCalculation_;
    
    /**
     * The Subscriber that will receive any remainder Payment Plan Payment due to uneven payment splitting.
     */
    protected String remaingSubscriberAssigneeID_;
    
    /**
     * Keeps track of Transaction amounts
     */
    protected int successCount=0;
    protected int failedCount=0;
    protected long successAmount =0;
    protected long failedAmount =0;
    
}
