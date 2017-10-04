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
package com.trilogy.app.crm.home;

import java.util.Date;
import java.util.Map;

import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.OMLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;
import com.trilogy.product.s2100.oasis.param.Parameter;
import com.trilogy.product.s2100.oasis.param.ParameterID;
import com.trilogy.product.s2100.oasis.param.ParameterValue;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.OcgGenericParameterHolder;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.core.AdjustmentType;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.AdjustmentTypeSupport;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;
import com.trilogy.app.crm.support.DefaultCoreTransactionSupport;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.xhome.home.OcgTransactionException;


/**
 * Forwards the create operation to the OCG application as a balance decrement or
 * increment.
 *
 * @author paul.sperneac@redknee.com
 */
public class OcgForwardTransactionHome extends HomeProxy
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 2L;


    /**
     * Create a new instance of <code>OcgForwardTransactionHome</code>.
     *
     * @param delegate
     *            Delegate of this home.
     */
    public OcgForwardTransactionHome(final Home delegate)
    {
        super(delegate);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(final Context ctx, final Object obj) throws HomeException
    {
        boolean ocgTransDone = false;
        boolean createTransDone = false;
        Account acct = null;
        final Transaction trans = (Transaction) obj;
        int result = 1;

        try
        {
            acct = getAcct(ctx, trans);

            // send transaction to ocg
            if (isForwardToOcg(ctx, trans))
            {
                final PMLogMsg pmLogMsg = new PMLogMsg("Transaction", "Forwarding to OCG", "");
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "Forwarding Transaction to OCG.", null).log(ctx);
                }
                Subscriber subscriber = (Subscriber) ctx.get(Subscriber.class);
                if (subscriber==null || subscriber.getId()==null || !subscriber.getId().equals(trans.getSubscriberID()))
                {
                    subscriber = SubscriberSupport.lookupSubscriberForMSISDN(ctx,trans.getMSISDN(), trans.getTransDate());                  
                }
                
                
                Context subContext = ctx.createSubContext();
                CRMSpid crmSpid = getCRMSpid(ctx,acct.getSpid());
                
                if(subscriber.getState() == SubscriberStateEnum.EXPIRED || subscriber.getStateWithExpired() == SubscriberStateEnum.EXPIRED)
                {
                	
                	/*
                	 * Subscriber is expired 
                	 */
                	if(crmSpid != null && crmSpid.isAllowDebitForExpiredSubscriber())
                	{
                		OcgGenericParameterHolder paramHolder =  (OcgGenericParameterHolder)ctx.get(OcgGenericParameterHolder.class);
                        
                        if(paramHolder == null)
                        {
                        	paramHolder =  new OcgGenericParameterHolder();
                        	ctx.put(OcgGenericParameterHolder.class,paramHolder);
                        }
                        
                		Parameter infoParam = new Parameter();
                		infoParam.parameterID = ParameterID.DEBIT_EXPIRED_BALANCE;
                		ParameterValue paramValue = new ParameterValue();
                		paramValue.booleanValue(true);
                		infoParam.value = paramValue;
                		
                		paramHolder.addInputParameter(infoParam);
                		
                	}
                }
                
                boolean useOCGLadder = false;
                if(crmSpid != null)
                {
                	 useOCGLadder = crmSpid.getUseOcgLaddersForExtension(); 
                	 if(useOCGLadder)
                	 {
                		 OcgGenericParameterHolder paramHolder =  (OcgGenericParameterHolder)ctx.get(OcgGenericParameterHolder.class);
                         
                         if(paramHolder == null)
                         {
                         	paramHolder =  new OcgGenericParameterHolder();
                         	ctx.put(OcgGenericParameterHolder.class,paramHolder);
                         }
                         
                 		Parameter infoParam = new Parameter();
                 		infoParam.parameterID = ParameterID.APPLICATION_ID;
                 		ParameterValue paramValue = new ParameterValue();
                 		paramValue.intValue(crmSpid.getAppIdNumeric());
                 		infoParam.value = paramValue;
                 		
                 		paramHolder.addInputParameter(infoParam);
                 		if(LogSupport.isDebugEnabled(ctx))
                 		{
                 			LogSupport.debug(ctx, this, "Use OCG Ladders for Expiry Extension flag is " + useOCGLadder + "hence adding application id " +
                 					crmSpid.getAppIdNumeric() + "as a input to requestCredit() API");
                 		}
                	 }
                }
                
                CoreTransactionSupportHelper.get(ctx).forwardTransactionToOcg(subContext, trans, trans.getMSISDN(), subscriber.getSubscriberType(), acct
                             .getCurrency(), false, trans.getAmount(), this);
                ocgTransDone = true;
                pmLogMsg.log(ctx);
            }
            // add transaction to datastore
            final Object savedTrans = super.create(ctx, trans);
            createTransDone = true;
            result = 0;
            return savedTrans;
        }
        catch (final OcgTransactionException ocge)
        {
            result = ocge.getErrorCode();
            throw ocge;

        }
        catch (final HomeException e)
        {

            if (acct != null && ocgTransDone && !createTransDone)
            {
                LogSupport.minor(ctx, this, "Error creating transaction, attempting to roll back OCG call", e);
                /*
                 * roll back ocg trans if fail to create transaction in the table
                 */
                Subscriber sub = SubscriberSupport.lookupSubscriberForMSISDN(ctx,trans.getMSISDN(), trans.getTransDate());
                CoreTransactionSupportHelper.get(ctx).forwardTransactionToOcg(ctx, trans, trans.getMSISDN(), sub.getSubscriberType(), acct
                    .getCurrency(), true, trans.getAmount(), this);
            }
            throw e;
        }
        finally
        {
            if (!CoreTransactionSupportHelper.get(ctx).isPayment(ctx, trans))
            {
                logAdjustmentErOm(ctx, trans, acct, result);
            }
        }
    }


    private CRMSpid getCRMSpid(Context ctx, int spid) throws HomeInternalException, HomeException 
    {
    	Home spidHome = (Home) ctx.get(CRMSpidHome.class);
    	return (CRMSpid) spidHome.find(ctx,spid);
    		
    	
	}


	/**
     * Determines whether a transaction should be forwarded to OCG.
     *
     * @param ctx
     *            The operating context.
     * @param trans
     *            The transaction.
     * @return Returns <code>true</code> if the transaction should be forwarded,
     *         <code>false</code> otherwise.
     */
    private boolean isForwardToOcg(final Context ctx, final Transaction trans)
    {
        boolean forward = true;
        /*
         * Do not forward to OCG if the context says so.
         */
        if(ctx.getBoolean(IS_IGNORE_OCG_FW,false))
        {
            forward = false;
        }
        /*
         * Transactions generated as a result of VRA ER polled do not need to be forwarded --
         * VRA should already forwarded them to OCG.
         */
        else if (trans.getFromVRAPoller())
        {
            forward = false;
        }
        /*
         * Transactions generated from polled TFA ER do not need to be forwarded
         */
        else if (trans.getFromTFAPoller())
        {
            forward = false;
        }
        /*
         * Payments are not forwarded.
         */
        else if (CoreTransactionSupportHelper.get(ctx).isPayment(ctx, trans))
        {
            forward = false;
        }
        /*
         * Reactivation Fee transactions only need the creation of a record in the DB and
         * don't need an ABM adjustment (Angie Li)
         */
        else if (CoreTransactionSupportHelper.get(ctx).isReactivationFeeDebit(ctx, trans))
        {
            forward = false;
        }
        /*
         * Initial Balance transactions only need the creation of a record in the DB and
         * don't need ABM balance adjustment. (Angie Li)
         */
        else if (CoreTransactionSupportHelper.get(ctx).isInitialBalanceCredit(ctx, trans))
        {
            forward = false;
        }
        /*
         * [Cindy] 2007-11-08: Deposit transactions should not be forwarded.
         */
        else if (CoreTransactionSupportHelper.get(ctx).isDeposit(ctx, trans))
        {
            forward = false;
        }
        else if(
               
                AdjustmentTypeSupportHelper.get(ctx).isInCategory(ctx, trans.getAdjustmentType(), AdjustmentTypeEnum.WriteOffTax)
                )
        {
            forward = false;
        }
        
        else
        {
            Subscriber subscriber = (Subscriber) ctx.get(Subscriber.class);
            
            if (subscriber==null || subscriber.getId()==null || !subscriber.getId().equals(trans.getSubscriberID()))
            {
            
                try
                {
                    subscriber = SubscriberSupport.lookupSubscriberForMSISDN(ctx,trans.getMSISDN(), trans.getTransDate());
                }
                catch (final HomeException exception)
                {
                    forward = false;
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        LogSupport.debug(ctx, this, "Cannot forward transction to OCG - cannot look up subscriber in CRM",
                            exception);
                    }
                }
            }
            
            if (subscriber == null)
            {
                forward = false;
                if (LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, this, "Cannot forward transction to OCG - subscriber not found in CRM");
                }
            }
            else
            {
                /*
                 * [Cindy] Do not forward any transaction to ABM/OCG if the subscriber is
                 * inactive.
                 */
                if (SafetyUtil.safeEquals(subscriber.getState(), SubscriberStateEnum.INACTIVE))
                {
                    forward = false;
                }
                else if(subscriber.isInitialBalanceByCreditCard())
                {
                    CRMSpid spid = null;
                    try
                    {
                        spid = SpidSupport.getCRMSpid(ctx, subscriber.getSpid());
                    }
                    catch (HomeException e)
                    {
                        LogSupport.minor(ctx, this, "Exception occurred while fetching spid for subscriber with id : "+subscriber.getId(), e);
                    }
                    if(spid != null 
                            && spid.getCreditCardPaymentAdjustmentID() == trans.getAdjustmentType())
                    {
                        forward = false;
                    }
                }
            }
        }
        return forward;
    }


    /**
     * Logs the appropriate ERs and OMs.
     *
     * @param ctx
     *            The operating context.
     * @param trans
     *            The transaction being forwarded.
     * @param acct
     *            The account associated with the transaction.
     * @param result
     *            OCG result code.
     */
    private void logAdjustmentErOm(final Context ctx, final Transaction trans, final Account acct, final int result)
    {

        logAdjustmentOm(ctx, result);

        Subscriber sub = (Subscriber) ctx.get(Subscriber.class);

        if (sub == null || !sub.getMSISDN().equals(trans.getMSISDN()))
        {
            try
            {
                sub = SubscriberSupport.lookupSubscriberForMSISDN(ctx, trans.getMSISDN());
            }
            catch (final HomeException e)
            {
                LogSupport.major(ctx, this, "Exception when trying to get the right Subscriber on OCGForward: ", e);
            }

        }

        int spid;
        if (acct == null)
        {
            spid = -1;
        }
        else
        {
            spid = acct.getSpid();
        }

        final String adjustmentType = AdjustmentTypeSupportHelper.get(ctx).getAdjustmentTypeName(ctx, trans);

        String subscriberId;
        if (sub == null)
        {
            subscriberId = "";
        }
        else
        {
            subscriberId = sub.getId();
        }

        String locationCode;
        if (trans.getLocationCode() != null && trans.getLocationCode().length() > 0)
        {
            locationCode = trans.getLocationCode();
        }
        else
        {
            locationCode = "none";
        }

        /*
         * Larry: added for TT 403303537.
         */
        final String agent = trans.getAgent();

        ERLogger.createSubAdjustmentEr(ctx, spid, trans.getAcctNum(), subscriberId, trans.getAmount(), trans
            .getCSRInput(), adjustmentType, trans.getGLCode(), result, agent, locationCode);

    }


    /**
     * Logs the OM.
     *
     * @param ctx
     *            The operating context.
     * @param result
     *            Result code.
     */
    private void logAdjustmentOm(final Context ctx, final int result)
    {
        if (result != 0)
        {
            new OMLogMsg(Common.OM_MODULE, Common.OM_ACCT_ADJUST_FAIL).log(ctx);
        }
        else
        {
            new OMLogMsg(Common.OM_MODULE, Common.OM_ACCT_ADJUST_SUCCESS).log(ctx);
        }
    }


    /**
     * Retrieves the account associated with the transaction.
     *
     * @param ctx
     *            The operating context.
     * @param trans
     *            The transaction being looked up.
     * @return The account associated with the transaction.
     * @throws HomeException
     *             Thrown if there are problems retrieving the account associated with the
     *             transaction.
     * @throws HomeInternalException
     *             Thrown if there are irrecoverable problems retrieving the account
     *             associated with the transaction.
     */
    private Account getAcct(final Context ctx, final Transaction trans) throws HomeException, HomeInternalException
    {
        Account account = null;
        if (ctx.has(Account.class))
        {
            account = (Account) ctx.get(Account.class);
        }
        
        if (account == null
                || !SafetyUtil.safeEquals(account.getBAN(), trans.getAcctNum()))
        {
            account = AccountSupport.getAccount(ctx, trans.getAcctNum());
        }
        
        return account;
    }
    
    public static final String IS_IGNORE_OCG_FW = "IS_IGNORE_OCG_FW";
    
}
