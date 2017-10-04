/*
 * Created on Dec 08, 2003
 *
 * Copyright (c) 1999-2003 REDKNEE.com. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * REDKNEE.com. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with REDKNEE.com.
 *
 * REDKNEE.COM MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE
 * SUITABILITY OF THE SOFTWARE, EITHCDR EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE IMPLIED WARRANTIES OF MCDRCHANTABILITY, FITNESS FOR
 * A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. REDKNEE.COM SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFCDRED BY LICENSEE AS A RESULT OF USING,
 * MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */ 
package com.trilogy.app.crm.provision;

import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.calculation.support.CalculationServiceSupport;
import com.trilogy.app.crm.provision.xgen.AccountBalance;
import com.trilogy.app.crm.provision.xgen.AccountServiceException;
import com.trilogy.app.crm.provision.xgen.AccountServiceInternalException;
import com.trilogy.app.crm.provision.xgen.AccountServiceProxy;
import com.trilogy.app.crm.provision.xgen.AmountOwing;
import com.trilogy.app.crm.support.AccountSupport;

/**
 * Server Implementation
 */
public class AccountServiceServer extends AccountServiceProxy
	implements ContextAware
{
	/**
	 * container context 
	 */
	protected Context context_ = null;

	/**
	 * Default constructor. 
	 */
	private AccountServiceServer()
	{
		// intentionally empty
	}

	/**
	 * Constructor taking the context directly.
	 * @param context Context 
	 */
	public AccountServiceServer(Context context)
	{
		setContext(context);
	}

	/**
	 * Get the container context.
	 * 
	 * @return Context
	 */
	@Override
    public Context getContext()
	{
		return context_;
	}

	/**
	 * Set the container context.
	 * @param context
	 * @throws IllegalArgumentException
	 */
	@Override
    public void setContext(Context context)
		throws IllegalArgumentException
	{
		context_ = context;
	}

	
	/**
    * getAcct is used to get information about an account. The return value is
    * the account profile set.
    * 
    * @param acctNum
    * @return Account
    * @exception AccountServiceException
    * @exception AccountServiceInternalException
    */
	@Override
    public Account getAcct(	Context ctx,String acctNum)
		throws AccountServiceException,  AccountServiceInternalException
	{
	   Home acctHome=(Home) getContext().get(AccountHome.class);
	   
	   if(acctHome==null)
	   {
	      if(LogSupport.isDebugEnabled(getContext()))
	      {
	         new DebugLogMsg(this,"Cannot find AccountHome in context",null).log(getContext());
	      }
	      throw new AccountServiceInternalException("Cannot find AccountHome in context");
	   }
	   
	   try
      {
         return (Account) acctHome.find(ctx,acctNum);
      }
      catch (HomeInternalException e)
      {
         throw new AccountServiceInternalException(e);
      }
      catch (HomeException e)
      {
         throw new AccountServiceException(e);
      }
	}

	/**
	 * the editAcct method is used to change any number of account profile parameters.
	 * 
	 * @param  acctProfileSet
	 * @exception AccountServiceException
	 * @exception AccountServiceInternalException
	 */
	@Override
    public void editAcct(Context ctx,Account acctProfileSet)
		throws AccountServiceException,  AccountServiceInternalException
	{
	   Home acctHome=(Home) getContext().get(AccountHome.class);
	   
	   if(acctHome==null)
	   {
	      if(LogSupport.isDebugEnabled(getContext()))
	      {
	         new DebugLogMsg(this,"Cannot find AccountHome in context",null).log(getContext());
	      }
	      throw new AccountServiceInternalException("Cannot find AccountHome in context");
	   }
	   
	   try
	   {
	      acctHome.store(ctx,acctProfileSet);
	   }
	   catch (HomeInternalException e)
	   {
	      throw new AccountServiceInternalException(e);
	   }
	   catch (HomeException e)
	   {
	      throw new AccountServiceException(e);
	   }
	}

	/**
	 * The activateAcct method is used to create a new account profile. 
            The return value is the account number.
	 * 
	 * @param  acct
	 * @return Account
	 * @exception AccountServiceException
	 * @exception AccountServiceInternalException
	 */
	@Override
    public Account activateAcct(Context ctx,Account acct)
		throws AccountServiceException,AccountServiceInternalException
	{
	   Home acctHome=(Home) getContext().get(AccountHome.class);
	   
	   if(acctHome==null)
	   {
	      if(LogSupport.isDebugEnabled(getContext()))
	      {
	         new DebugLogMsg(this,"Cannot find AccountHome in context",null).log(getContext());
	      }
	      throw new AccountServiceInternalException("Cannot find AccountHome in context");
	   }
	   
	   try
	   {
	      return (Account) acctHome.create(ctx,acct);
	   }
	   catch (HomeInternalException e)
	   {
	      throw new AccountServiceInternalException(e);
	   }
	   catch (HomeException e)
	   {
	      throw new AccountServiceException(e);
	   }
	}

	/**
	 * Changes the state of the account
	 * 
	 * @param  acctNum
	 * @param  state
	 * @exception AccountServiceException
	 * @exception AccountServiceInternalException
	 */
	@Override
    public void changeState(Context ctx,String acctNum, com.redknee.app.crm.bean.AccountStateEnum state)
		throws AccountServiceException,  AccountServiceInternalException
	{
	   Account acct=getAcct(acctNum);
	   if(acct==null)
	   {
	      throw new AccountServiceException("Cannot find account");
	   }
	   
	   acct.setState(state);
	   editAcct(acct);
	}

	/**
	 * The balanceAcct is used to find the accumulated usage for the account
	 * 
	 * @param  acctNum
	 * @return
	 * @exception AccountServiceException
	 * @exception AccountServiceInternalException
	 */
	@Override
    public AccountBalance balanceAcct(Context ctx,String acctNum)
		throws AccountServiceException,  AccountServiceInternalException
	{
	    Context sCtx = ctx.createSubContext();

        String sessionKey = CalculationServiceSupport.createNewSession(sCtx);
        try
        {
            Account acct=getAcct(acctNum);
            if(acct==null)
            {
               throw new AccountServiceException("Cannot find account");
            }
            
            AccountBalance bal = null;
            try
            {
                bal = (AccountBalance) XBeans.instantiate(AccountBalance.class, sCtx);
            }
            catch (Exception e)
            {
                bal = new AccountBalance();
            } 
            
			bal.setAccumulatedBalanceOwing(acct.getAccumulatedBalance(sCtx,
			    sessionKey));
            bal.setAccumulatedBundleMessages(acct.getAccumulatedBundleMessages());
            bal.setAccumulatedBundleMinutes(acct.getAccumulatedBundleMinutes());
			bal.setAccumulatedMonthToDateUsage(acct.getAccumulatedMDUsage(sCtx,
			    sessionKey));
            
            return bal;
        }
        finally
        {
            CalculationServiceSupport.endSession(sCtx, sessionKey);
        }
	}
	
	/**
	 * Returns the amount Owing of the account requested.
	 * 
	 * @param  id
	 * @return AmountOwing
	 * @exception AccountServiceException
	 * @exception AccountServiceInternalException
	 */
	@Override
    public AmountOwing getAmountOwing(Context ctx, String id)
		throws AccountServiceException,  AccountServiceInternalException
	{
		AmountOwing owing = new AmountOwing();
		Context subCtx = ctx.createSubContext();

        String sessionKey = CalculationServiceSupport.createNewSession(subCtx);
        try
        {
            try
            {
                Account acct = AccountSupport.getAccount(subCtx, id);
                if (!(acct.isIndividual(subCtx) && acct.getSystemType() == SubscriberTypeEnum.PREPAID))
                {
                    acct.setContext(subCtx);
                    
                    owing.setId(id);
					owing.setAmountOwing(acct.getAccumulatedBalance(subCtx,
					    sessionKey));
					owing.setMonthToDate(acct.getAccumulatedMDUsage(subCtx,
					    sessionKey));
                }
            } 
            catch (HomeException e) 
            {
                LogSupport.info(ctx, this, e.getMessage(), e);
                throw new AccountServiceException(e);
            }
            
            return owing;
        }
        finally
        {
            CalculationServiceSupport.endSession(subCtx, sessionKey);
        }
	}

	

}


