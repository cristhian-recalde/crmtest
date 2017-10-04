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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.trilogy.framework.core.locale.Currency;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bas.tps.TPSSupport;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.CreditCategory;
import com.trilogy.app.crm.bean.CreditCategoryHome;
import com.trilogy.app.crm.bean.GroupTypeEnum;
import com.trilogy.app.crm.bean.Invoice;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.calculation.service.CalculationService;
import com.trilogy.app.crm.calculation.service.CalculationServiceException;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.AccountOverPaymentHistorySupport;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;
import com.trilogy.app.crm.support.FrameworkSupportHelper;
import com.trilogy.app.crm.support.PaymentExceptionSupport;
import com.trilogy.app.crm.report.ReportUtilities;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.tps.pipe.TPSPipeConstant;
import com.trilogy.app.crm.writeoff.WriteOffSupport;
import com.trilogy.util.snippet.log.Logger;
/**
 * This Class was added when porting the new Account Level Payment Splitting Logic from 
 * CRM 7.3
 * @since 7.3, ported to 8.2, Sept 21, 2009.
 * 
 * 
 * @author Larry Xia
 * @author Angie Li
 *
 */
public class AccountPaymentDistribution 
extends AbstractPaymentDistribution
{
    

	public AccountPaymentDistribution(final Context ctx, 
            final Account acct, 
            final Transaction trans)
    throws HomeException
    {
        this.account = acct; 
        this.orignalTransaction = trans; 
        this.OBOPayableSubscribers = TPSSupport.getPayableSubscribers(ctx, account); 
        this.postpaidSubscribers = this.OBOPayableSubscribers; 
        if (this.OBOPayableSubscribers.size()< 1)
        {
            throw new HomeException("no postpaid subscriber found under the account"); 
        }

        this.overPaymentPayableSubscribers = TPSSupport.removeDeactiveSubscriber(OBOPayableSubscribers);

        if(this.overPaymentPayableSubscribers.size() < 1 )
        {
            Logger.minor(ctx, this, "No active under the account " + 
                    this.account.getBAN() + 
                    " over payment will go to deactived subscriber");
            this.overPaymentPayableSubscribers = this.OBOPayableSubscribers; 
        }

        if (!SpidSupport.getCRMSpid(ctx, acct.getSpid()).isPaymentAcctLevelToInactive())
        {
            this.OBOPayableSubscribers = this.overPaymentPayableSubscribers; 
        }
        subscriberDistributions = new HashMap();
        
        //Initialize PaymentPlanLoanPaymentDistribution calculator
        getPaymentPlanDistribution(ctx);
    }

    protected AccountPaymentDistribution(final Context ctx, 
            final Account acct)
    {
        this.account = acct; 
        subscriberDistributions = new HashMap(); 

    }

    public void Distribute(Context ctx, Home transHome)
    throws HomeException, CalculationServiceException
    {
        if (account == null || orignalTransaction == null)
        {
            throw new HomeException("account and transaction must not be null"); 
        }
        
        if(isOverpayable(ctx) && isPaymentExceptionEntryPresentForAccount(ctx))
        {
        	throw new HomeException(new PaymentDistributionException("Payment Exception Record found", TPSPipeConstant.PAYMENT_EXCEPTION_RECORD_FOUND));
        }

        if (this.orignalTransaction.getAmount() > 0)
        {
            distributePaymentReverse(ctx); 
        } 
        else 
        {    
            // Distribute Payment to the Payment Plan Installment Charges 
            distributePaymentPlanChargesPayment(ctx, this.orignalTransaction.getAmount());

            CalculationService service = (CalculationService) ctx.get(CalculationService.class);
            Invoice invoice = null;
            try
            {
                invoice = service.getMostRecentInvoice(ctx, account.getBAN());
            }
            catch (CalculationServiceException e)
            {
                new MinorLogMsg(this, "Error retrieving most recent invoice for account " + account.getBAN() + ": " + e.getMessage(), e).log(ctx);
            }
            if ( invoice == null)
            {
                //Prior to distributing Over Payment, Distribute Payment Plan Loan Over Payment
                distributePaymentPlanOverPayment(ctx, calculatePaymentRemainder(ctx));
                //Distribute Regular Over Payment
                distributeOverpayment(ctx, calculatePaymentRemainder(ctx)); 
            } 
            else 
            {
                distributePayment(ctx, calculatePaymentRemainder(ctx)); 
            }
        }
        this.handleRemainings(ctx);
        createDistributions(ctx, transHome); 
       
	}
    private void distributePaymentReverse(Context ctx)
    throws HomeException, CalculationServiceException
    {
        if (Logger.isDebugEnabled())
        {
            Logger.debug(ctx, this, "distributePaymentReverse:: distributing as a Payment Reversal because the Transaction amount is > 0. Transaction Amount=" + this.orignalTransaction.getAmount() );
        }
        // payment reverse use same strategy of over payment. which
        // is divided evenly amount subscribers legal to get payment.
        distributeOverpayment(ctx, this.orignalTransaction.getAmount()); 
    }


    private void distributeOverpayment(Context ctx, long amount)
    throws HomeException, CalculationServiceException
    {             
        //For Overpayment handling
        
        if(isOverpayable(ctx))
        {
        	// undistributedOverPayment = amount which is kept aside when creditCategory.isOverPaymentDistributed()==false.
        	this.undistributedOverPayment=amount;
        	if (Logger.isDebugEnabled())
        	{
        		Logger.debug(ctx, this, "distributeOverpayment:: Overpayments to be stored in Overpayments Entity "+ this.undistributedOverPayment);
        	}  		
        }
        else
        {   
        	if (TPSSupport.isAmountSmallerThanLowestCurrencyUnit(ctx, Math.abs(amount), this.overPaymentPayableSubscribers.size()))
            {
                return; 
            }
                 
	        if (Logger.isDebugEnabled())
	        {
	            Logger.debug(ctx, this, "distributeOverpayment:: beginning distribution of over payment amounts.");
	        }
	        
	        long overPayment = (amount / this.overPaymentPayableSubscribers.size());
	        
	        overPayment = applyDisplayPrecision(ctx,overPayment);
	        
	        for (Iterator i = this.overPaymentPayableSubscribers.iterator(); i.hasNext();)
	        {
	            SubscriberPaymentDistribution subDistribution = getSubscriberPaymentDistribution(ctx, (Subscriber) i.next()); 
	
	            subDistribution.setOverPayment(overPayment);
	
	            this.overPayment += overPayment;            
	            
	            if (Logger.isDebugEnabled())
	            {
	                Logger.debug(ctx, this, "distributeOverpayment:: Updated SubscriberPaymentDistribution " + subDistribution.appendDistributionDetails());
	                logDistributionStatus(ctx);
	            }
	        }
        }
    }

	private boolean isOverpayable(Context ctx)
			throws HomeInternalException, HomeException {

	    /* Condition 'this.orignalTransaction.getAmount()<0' -- To skip payment reversals */
		if(this.orignalTransaction.getAmount()<0 && GroupTypeEnum.GROUP.equals(this.account.getGroupType()) && (this.account.isResponsible()))
		{
			CreditCategory creditCategory = HomeSupportHelper.get(ctx).findBean(ctx, CreditCategory.class, this.account.getCreditCategory());
			if(!creditCategory.isOverPaymentDistributed())
			{
			    if(AccountSupport.getNonDeActiveChildrenSubscribers(ctx,this.account).size()>0)
				{
				     return true;   
				}
			}
		}

		return false;
	}

    private void handleRemainings(Context ctx)
    throws HomeException, CalculationServiceException
    {
        long remaining = calculatePaymentRemainder(ctx);

        if ( remaining == 0)
        {
            return;     
        }

        Subscriber sub = this.getRemainingAssignee(ctx);            

        if (sub == null )
        {
            throw new HomeException("cannot find subscriber to assing the remaining"); 
        }

        if (Logger.isDebugEnabled())
        {
            Logger.debug(ctx, this, "The subscription that is assigned the payment remainder is " + sub.getId());
        }
        
        SubscriberPaymentDistribution subDistribution = getSubscriberPaymentDistribution(ctx, sub); 

        long subOutstandingOwingRemains = subDistribution.outStandingOwing  + subDistribution.paymentForOutStandOwing ; 

        if (subOutstandingOwingRemains > 0)
        {
            if ( (remaining *-1) <= subOutstandingOwingRemains )
            {
                additionalOBOPaymentToSubscriber( ctx, subDistribution, remaining); 
            } else 
            {    
                additionalOBOPaymentToSubscriber(ctx, subDistribution, subOutstandingOwingRemains * -1 ); 
                additionalOverPaymentToSubscriber(ctx, subDistribution, remaining- (subOutstandingOwingRemains *-1)); 
            }

        } else {
            additionalOverPaymentToSubscriber(ctx, subDistribution, remaining);             
        }

    }


    /**
     * Accumulate the given Payment remainder to the given Subscriber's Over Payment amount.
     * Keep track of this allocation by updating the Account's Distribution totals.
     * 
     * 7.3@Larry: Skip updating Tax Totals for now.
     * 
     * @param ctx
     * @param subDistribution
     * @param remaining
     * @throws HomeException
     */
    private void additionalOverPaymentToSubscriber(final Context ctx, 
            final SubscriberPaymentDistribution subDistribution, final long remaining)
    throws HomeException
    {
        long overPayment = subDistribution.overPayment + remaining;            

        subDistribution.setOverPayment(overPayment);

        this.overPayment += remaining; 

        if (Logger.isDebugEnabled())
        {
            Logger.debug(ctx, this, "additionalOverPaymentToSubscriber:: Updated SubscriberPaymentDistribution " + subDistribution.appendDistributionDetails());
            logDistributionStatus(ctx);
        }
    }


    /**
     * Accumulate the given Payment remainder into the Subscriber's OBO Payment. 
     * Keep track of this allocation by updating the Account's Distribution totals.
     * 
     * 7.3@Larry: Skip updating Tax Totals for now.
     * 
     * @param ctx
     * @param subDistribution
     * @param remaining
     * @throws HomeException
     */
    private void additionalOBOPaymentToSubscriber(final Context ctx, 
            final SubscriberPaymentDistribution subDistribution, final long remaining)
    throws HomeException
    {
        long payment = subDistribution.paymentForOutStandOwing + remaining;            

        subDistribution.setPaymentForOutStandOwing(payment);
        
        this.paymentForOutStandOwing += remaining;
        
        if (Logger.isDebugEnabled())
        {
            Logger.debug(ctx, this, "additionalOBOPaymentToSubscriber:: Updated SubscriberPaymentDistribution " + subDistribution.appendDistributionDetails());
            logDistributionStatus(ctx);
        }
    }


    private void distributePayment(Context ctx, final long paymentAmount)
    throws HomeException, CalculationServiceException
    {
        this.calculateOBO(ctx); 
        this.adjustAmountPaidAgainstPaymentPlanLoanInOBO(ctx);

        if (Logger.isDebugEnabled())
        {
            Logger.debug(ctx, this, "distributePayment:: beginning distribution of regular payment amounts.");
        }
        
        if ( paymentAmount * -1 >= this.outStandingOwing )
        {
            distributeOBO(ctx, this.outStandingOwing * -1); 
            
            //Prior to distributing Over Payment, Distribute Payment Plan Loan Over Payment
            distributePaymentPlanOverPayment(ctx, calculatePaymentRemainder(ctx));
            
            //Distribute Regular Over Payment
            distributeOverpayment(ctx, calculatePaymentRemainder(ctx)); 
        }
        else 
        {
            distributeOBO(ctx, paymentAmount); 
        }

    }
    
    protected void distributeOBO(Context ctx, long amount)
    throws CalculationServiceException
    {
        if (TPSSupport.isAmountSmallerThanLowestCurrencyUnit(ctx, Math.abs(amount), this.OBOPayableSubscribers.size()))
        {
            return; 
        }

        for (Iterator i = OBOPayableSubscribers.iterator(); i.hasNext();)
        {
            Subscriber sub = (Subscriber) i.next();
            SubscriberPaymentDistribution subDistribution = this.getSubscriberPaymentDistribution(ctx, sub);

            long payment = (amount * subDistribution.outStandingOwing / this.outStandingOwing); 
            payment = applyDisplayPrecision(ctx, payment);

            subDistribution.setPaymentForOutStandOwing(payment); 

            this.paymentForOutStandOwing += payment;
            
            if (Logger.isDebugEnabled())
            {
                Logger.debug(ctx, this, "distributeOBO:: Updated SubscriberPaymentDistribution " + subDistribution.appendDistributionDetails());
                logDistributionStatus(ctx);
            }
        }
    }

    /**
     * Distribute the Payment amount to the Payment Plan Loan Monthly Installment Changes.
     * Payment Plan Loan Installments don't need to be invoiced to be included in the charges for payment.  
     * Paying these charges is the top priority. 
     * @param ctx
     * @param amount
     * @throws HomeException
     */
    private void distributePaymentPlanChargesPayment(Context ctx, long amount) 
    throws HomeException
    {
        getPaymentPlanDistribution(ctx).distributePaymentForCharges(ctx, amount);
    }
    
    /**
     * Distribute the Payment amount to the Payment Plan Loan Balance.
     * We do this step separate from distribute Over Payment because that method is reused by 
     * the distributePaymentReverse() method.
     */
    private void distributePaymentPlanOverPayment(Context ctx, final long paymentAmount)
        throws HomeException
    {
        getPaymentPlanDistribution(ctx).distributeOverPayment(ctx, paymentAmount);
    }
    
    protected void createDistributions(Context ctx, Home transHome)
    {
        //Create Payment Plan Distributions (Transactions)
        try
        {
            /*
             * TT8041500041: Don't let dunning run unless it's the last transaction.
             * If there is any allocation to the regular/Standard payment, then skip
             * dunning while creating the Payment plan payment transactions.
             */
            final Context subContext = ctx.createSubContext();
            if (this.paymentForOutStandOwing != 0)
            {
                subContext.put(Common.DUNNING_EXEMPTION, Boolean.TRUE);
            }
            
            getPaymentPlanDistribution(subContext).createDistributions(subContext, this.orignalTransaction, transHome, getCurrency(subContext));    
        }
        catch(HomeException e)
        {
            Logger.minor(ctx, this, "Failed to create Payment Plan Payment Transactions", e);
        }
        
        for (Iterator i = subscriberDistributions.values().iterator(); i.hasNext();)
        {
            final Context subContext2 = ctx.createSubContext();
            
            SubscriberPaymentDistribution subDistribution = (SubscriberPaymentDistribution)i.next();

            if (i.hasNext())
            {
                /*
                 * TT8041500041: Don't let dunning run unless it's the last transaction.
                 * If we are paying a mix of Payment Plan and Regular charges this parameter
                 * might already have been set to TRUE previously.
                 */
                subContext2.put(Common.DUNNING_EXEMPTION, Boolean.TRUE);
            }

            if (subDistribution.createTransaction(subContext2, this.orignalTransaction, transHome, getCurrency(subContext2)) )
            {
                this.successAmount += subDistribution.getPaymentForOutStandOwing() + subDistribution.getOverPayment(); 
                ++this.successCount; 
				postWriteOffPayment_ += subDistribution.getPostWriteOffPayment(); 
            } else 
            {
                this.failedAmount += subDistribution.getPaymentForOutStandOwing() + subDistribution.getOverPayment(); 
                ++this.failedCount; 
            }                        
        }
        /*
         * Below code try catch block is added for Over Payment Handling 
         */
        try
        {
        	if(this.orignalTransaction.getAdjustmentType()!=AdjustmentTypeEnum.OverPaymentCredit.getIndex() && this.undistributedOverPayment!=0)
        	{	
        		AccountOverPaymentHistorySupport.createAccountOverPaymentHistoryTransaction(ctx, resolveResposibleGroupBAN(ctx,this.account),this.account.getSpid(),this.account.getBillCycleID(),this.orignalTransaction.getAmount(),this.orignalTransaction.getAdjustmentType(),this.orignalTransaction.getReceiptNum(),this.undistributedOverPayment);
        	}
        	else if (this.orignalTransaction.getAdjustmentType()==AdjustmentTypeEnum.OverPaymentCredit.getIndex()/* && this.orignalTransaction.getAmount()==this.undistributedOverPayment*/)
        	{
        	
        		AccountOverPaymentHistorySupport.createAccountOverPaymentHistoryTransaction(ctx, resolveResposibleGroupBAN(ctx,this.account),this.account.getSpid(),this.account.getBillCycleID(),this.orignalTransaction.getAdjustmentType(),this.orignalTransaction.getReceiptNum(),this.undistributedOverPayment);
        	}
        	handleOverpaymentCreationSuccess();
        }
        catch(Exception e)
        {
           	handleOverpaymentCreationFailure(ctx,this.undistributedOverPayment,currency,e);
        }
    }


    protected void handleOverpaymentCreationFailure(Context ctx, long undistributedOverPayment,Currency currency, Throwable e ) {      
     
        
        final String fileName = CoreTransactionSupportHelper.get(ctx).getTPSFileName(ctx);

        String amount = String.valueOf(undistributedOverPayment);

        if (currency != null)
        {
            amount = currency.formatValue(undistributedOverPayment);
        }

        final StringBuilder msg = new StringBuilder("Unable to create overpayment transaction with amount ");
        msg.append(amount);
        if (!fileName.equals(""))
        {
            msg.append(" and external file ");
            msg.append(fileName);
        }
        
        AbstractTransactionProcessor.writeAccountNote(ctx, this.account, msg.toString());
        
        msg.append(" for account '");
        msg.append(this.account.getBAN());
        msg.append("': ");
        msg.append(e.getMessage());

        FrameworkSupportHelper.get(ctx).notifyExceptionListener(ctx, new HomeException(msg.toString()));  
 
	}

	protected void handleOverpaymentCreationSuccess() {
		// TODO Auto-generated method stub
		
	}
	protected String resolveResposibleGroupBAN(Context context,Account account) throws HomeException
	{
	
		if (GroupTypeEnum.GROUP.equals(account.getGroupType()) && (account.isResponsible()==true))
		{
			return account.getBAN();
		}
		else
		{
			String responsibleBAN =  account.getResponsibleBAN();
			
			if(responsibleBAN == account.getBAN())
			{
				throw new HomeException("Responsible BAN " + responsibleBAN+" is same as account BAN "+account.getBAN());
			}
			
			Account responsibleAccount= AccountSupport.getAccount(context, responsibleBAN);
			
			return resolveResposibleGroupBAN(context,responsibleAccount);
		}
	
	}

	protected void calculateOBO(Context ctx)
    throws HomeException, CalculationServiceException
    {
        if (Logger.isDebugEnabled())
        {
            Logger.debug(ctx, this, "calculateOBO:: beginning calculations of Subscriber and Account OBO.");
        }
        
        long outstandingOwingOfSubscribers =0; 
        for (Iterator i = OBOPayableSubscribers.iterator(); i.hasNext();)
        {
            SubscriberPaymentDistribution subDistribution = getSubscriberPaymentDistribution(ctx, (Subscriber) i.next());  
            // Sum(subscriber OBO) = Account OBO if inactive subscriber are included. 
            outstandingOwingOfSubscribers +=  subDistribution.getOutStandingOwing();             
        }
        long accountOutStandingOwing = TPSSupport.getAccountOutstanddingBalanceOwing(ctx, this.account); 
        if (outstandingOwingOfSubscribers != accountOutStandingOwing)
        {
            Logger.major(ctx, this, "The account out standing owing is " + accountOutStandingOwing + 
                    " but subscriber total owing is " + outstandingOwingOfSubscribers); 
        }
        this.setOutStandingOwing(outstandingOwingOfSubscribers); 
        if (Logger.isDebugEnabled())
        {
            Logger.debug(ctx, this, "The calculated Account " + account.getBAN() + " OBO (in cents) is " + outstandingOwingOfSubscribers);
        }
    }
	
	private void adjustAmountPaidAgainstPaymentPlanLoanInOBO(Context ctx) throws HomeException, CalculationServiceException
    {
		if (Logger.isDebugEnabled())
        {
            Logger.debug(ctx, this, "adjustPaymentPlanLoanAmountInOBO:: deduct OBO by payment done against paymentPlan loan...");
        }
		for (Iterator i = OBOPayableSubscribers.iterator(); i.hasNext();)
        {
			/*
			 * subscriberLoanPaymentAmount is the amount that is paid by a subscriber against the loan. It is negative in the system.
			 */
			Subscriber subscriber = ((Subscriber) i.next());
			long subscriberLoanPaymentAmount = getPaymentPlanDistribution(ctx).getSubscriptionLoanPayment(ctx, subscriber.getId());
			SubscriberPaymentDistribution subDistribution = getSubscriberPaymentDistribution(ctx, subscriber); 
			subDistribution.setOutStandingOwing(subDistribution.getOutStandingOwing() + subscriberLoanPaymentAmount);
			this.outStandingOwing = this.outStandingOwing + subscriberLoanPaymentAmount;
        }
		if (Logger.isDebugEnabled())
        {
            Logger.debug(ctx, this, "The calculated Account " + account.getBAN() + " OBO (in cents) after deducting payment against loan is " + this.outStandingOwing);
        }
    }
	
	private boolean isPaymentExceptionEntryPresentForAccount(Context context)
	{
		for (Iterator i = OBOPayableSubscribers.iterator(); i.hasNext();)
        {
			Subscriber subscriber = ((Subscriber) i.next());
			if(PaymentExceptionSupport.isPaymentExceptionRecordPresent(context, subscriber.getBAN(), subscriber.getMsisdn()))
			{
				return true;
			}
        }
		return false;
	}

    /**
     * Return the Original Payment Transaction amount less the current distributions:
     * to the Payment Plan charges, OBO charges, Payment Plan Over Payments, 
     * and regular Over Payments
     * At various points in the payment distribution we will have to find out how much 
     * of the original Payment is left to be distributed.
     * @param ctx
     * @return
     */
    protected long calculatePaymentRemainder(Context ctx)
    {
        final long amt = this.orignalTransaction.getAmount() 
                        - paymentPlanDistribution.getPaymentForOutStandOwing()
                        - this.paymentForOutStandOwing 
                        - paymentPlanDistribution.getOverPayment() 
                        - this.overPayment - this.undistributedOverPayment;
        if (Logger.isDebugEnabled())
        {
            Logger.debug(ctx, this, "At this point of the process, the Amount from the original Payment Transaction still to be distributed (in cents)=" + amt);
        }
        return amt;
    }

    protected SubscriberPaymentDistribution getSubscriberPaymentDistribution(Context ctx, Subscriber sub)
    throws CalculationServiceException
    {
        SubscriberPaymentDistribution subDistribution = (SubscriberPaymentDistribution)subscriberDistributions.get(sub.getId());
        if ( subDistribution == null)
        {
            subDistribution = new SubscriberPaymentDistribution(ctx, sub);
            subscriberDistributions.put(sub.getId(), subDistribution);
        }
        return subDistribution; 
    }


    /**
     * Choose the Subscription that receives any remainder when there is a chance of uneven payment 
     * splitting (usually the round off amount).
     * 
     * @since 7.3 This logic not very sophisticated.  It simply chooses the first postpaid subscriber 
     * from the list of Subscribers eligible for over payment.
     *  
     * @param ctx
     * @return
     * @throws HomeException
     */
    public Subscriber getRemainingAssignee(Context ctx)
    throws HomeException
    {
        if (remaingAssignee == null)
        {    
            this.remaingAssignee = TPSSupport.getFirstSubscriber(ctx, 
                    this.account.getSpid(), this.overPaymentPayableSubscribers);
        }    

        return this.remaingAssignee;  

    }
    
    private PaymentPlanLoanPaymentDistribution getPaymentPlanDistribution(Context ctx)
    throws HomeException
    {
        if (paymentPlanDistribution == null)
        {
            paymentPlanDistribution = new PaymentPlanLoanPaymentDistribution(ctx, account, 
                    getRemainingAssignee(ctx));
        }
        return paymentPlanDistribution;
    }
    
    private void logDistributionStatus(Context ctx) 
    {
        if (Logger.isDebugEnabled())
        {
            Logger.debug(ctx, this, "At this moment in the process, the " + appendDistributionDetails());
        }
    }
    
    /**
     * Returns the details of this SubscriberPaymentPlanLoanPaymentDistribution
     * @param subDistribution
     * @return
     */
    public String appendDistributionDetails() 
    {
        StringBuilder str = new StringBuilder();
        str.append(" Account ");
        str.append(this.account.getBAN());
        str.append(" has Outstanding Balance Owing="); 
        str.append(this.getOutStandingOwing());
        str.append(", Payment towards OBO Charges=");
        str.append(this.getPaymentForOutStandOwing());
        str.append(", Payment towards Overpayment=");
        str.append(this.getOverPayment());
        
        if (paymentPlanDistribution != null)
        {
            str.append(paymentPlanDistribution.appendDistributionDetails());
        }
        return str.toString();
    }

    protected Currency getCurrency(Context ctx)
    {
        if (currency == null)
        {
            currency = ReportUtilities.getCurrency(ctx, this.account.getCurrency());
        }
        return currency; 
    }
    /**
     * Returns returns non deactivated postpaid subscribers
     * @param 
     * @return
     */
	public Collection getNonDeactivatedPostpaidSubscribers()
	{
	    return overPaymentPayableSubscribers;
	}
    /**
     * Returns returns postpaid subscribers
     * @param 
     * @return
     */
    public Collection getPostpaidSubscribers()
    {
        return postpaidSubscribers;
    }
    public void setOrignalTransaction(Transaction orignalTransaction) {
        this.orignalTransaction = orignalTransaction;
    } 


    final protected Account account; 
    protected Collection postpaidSubscribers; 
    protected Collection OBOPayableSubscribers; 
    protected Collection overPaymentPayableSubscribers; 
    protected Transaction orignalTransaction; 
    protected double taxRate=Double.MAX_VALUE; 
    protected Map subscriberDistributions; 
    protected Subscriber remaingAssignee; 
    protected Currency currency; 

    protected int successCount=0;
    protected int failedCount=0;
    protected long successAmount =0;
    protected long failedAmount =0;
    
    /**
     * Details about the Payment Plan Payment distribution.
     */
    protected PaymentPlanLoanPaymentDistribution paymentPlanDistribution;

	protected long postWriteOffPayment_ = 0L;
	protected long undistributedOverPayment;
}
