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

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.provision.xgen.AmountOwing;
import com.trilogy.app.crm.provision.xgen.SubscriberBalance;
import com.trilogy.app.crm.provision.xgen.SubscriberServiceException;
import com.trilogy.app.crm.provision.xgen.SubscriberServiceInternalException;
import com.trilogy.app.crm.provision.xgen.SubscriberServiceProxy;
import com.trilogy.app.crm.support.SubscriberSupport;

/**
 * Server Implementation
 */
public class SubscriberServiceServer extends SubscriberServiceProxy
	implements ContextAware
{
	/**
	 * container context 
	 */
	protected Context context_ = null;

	/**
	 * Default constructor. 
	 */
	private SubscriberServiceServer()
	{
	}

	/**
	 * Constructor taking the context directly.
	 * @param context Context 
	 */
	public SubscriberServiceServer(Context context)
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
	 * getSub returns all supplied subscriber profile details for a 
            specified subscriber MSISDN. The return value is the subscriber
            profile set.
	 * 
	 * @param  msisdn
	 * @return Subscriber
	 * @exception SubscriberServiceException
	 * @exception SubscriberServiceInternalException
	 */
	@Override
    public Subscriber getSub(Context ctx,String msisdn)
		throws SubscriberServiceException,  SubscriberServiceInternalException
	{
	   try
	   {
	      return SubscriberSupport.lookupSubscriberForMSISDN(ctx,msisdn);
	   }
	   catch (HomeInternalException e)
	   {
	      throw new SubscriberServiceInternalException(e);
	   }
	   catch (HomeException e)
	   {
	      throw new SubscriberServiceException(e);
	   }
	}

   protected Account getAcct(Context ctx,String acctNum)
   {
      Home acctHome=(Home) getContext().get(AccountHome.class);
      
      if(acctHome==null)
      {
         if(LogSupport.isDebugEnabled(getContext()))
         {
            new DebugLogMsg(this,"Cannot find AccountHome in context",null).log(getContext());
         }
         return null;
      }
      
      try
      {
         return (Account) acctHome.find(ctx,acctNum);
      }
      catch (HomeException e)
      {
         if(LogSupport.isDebugEnabled(getContext()))
         {   
            new DebugLogMsg(this,e.getMessage(),e).log(getContext());
         }
      }
      
      return null;
   }
   
	/**
	 * the editSub method is used to change any number of 
            subscriber profile parameters.
	 * 
	 * @param  subProfileSet
	 * @exception SubscriberServiceException
	 * @exception SubscriberServiceInternalException
	 */
	@Override
    public void editSub(Context ctx,Subscriber subProfileSet)
		throws SubscriberServiceException,  SubscriberServiceInternalException
	{
	   Home subHome=(Home) getContext().get(SubscriberHome.class);
	   
	   if(subHome==null)
	   {
	      if(LogSupport.isDebugEnabled(getContext()))
         {
	         new DebugLogMsg(this,"Cannot find SubscriberHome in context",null).log(getContext());
	      }
	      throw new SubscriberServiceInternalException("Cannot find SubscriberHome in context");
	   }
	   
	   try
	   {
	      subHome.store(ctx,subProfileSet);
	   }
	   catch (HomeInternalException e)
	   {
	      throw new SubscriberServiceInternalException(e);
	   }
	   catch (HomeException e)
	   {
	      throw new SubscriberServiceException(e);
	   }
	}

	/**
	 * The activateSub method is used to create a new subscriber
            within the defined account profile. 
            The return value is the subscriber.
	 * 
	 * @param  subProfileSet
	 * @return Subscriber
	 * @exception SubscriberServiceException
	 * @exception SubscriberServiceInternalException
	 */
	@Override
    public Subscriber activateSub(Context ctx,Subscriber subProfileSet)
		throws SubscriberServiceException,  SubscriberServiceInternalException
	{
	   Home subHome=(Home) getContext().get(SubscriberHome.class);
	   
	   if(subHome==null)
      {
	      if(LogSupport.isDebugEnabled(getContext()))
         {
	         new DebugLogMsg(this,"Cannot find SubscriberHome in context",null).log(getContext());
	      }
	      throw new SubscriberServiceInternalException("Cannot find SubscriberHome in context");
	   }
	   
	   try
	   {
	      return (Subscriber) subHome.create(ctx,subProfileSet);
	   }
	   catch (HomeInternalException e)
	   {
	      throw new SubscriberServiceInternalException(e);
	   }
	   catch (HomeException e)
	   {
	      throw new SubscriberServiceException(e);
	   }
	}

	/**
	 * the changePlan method changes the subscriber's price plan within 
         their profile in E-Care
	 * 
	 * @param  msisdn
	 * @param  pricePlan
	 * @exception SubscriberServiceException
	 * @exception SubscriberServiceInternalException
	 */
	@Override
    public void changePlan(Context ctx,String msisdn, int pricePlan)
		throws SubscriberServiceException,  SubscriberServiceInternalException
	{
	   Subscriber sub=getSub(msisdn);
	   if(sub==null)
	   {
	      throw new SubscriberServiceException("Cannot find subscriber");
	   }
	   
	   Account acct=getAcct(ctx,sub.getBAN());
      if(acct==null)
      {
         throw new SubscriberServiceException("Cannot find account");
      }
      
      sub.setPricePlan(pricePlan);
	   editSub(sub);
	}

   /**
    * @see com.redknee.app.crm.provision.xgen.SubscriberService#getPlan(java.lang.String)
    */
   @Override
   public int getPlan(Context ctx,String msisdn) throws SubscriberServiceException, SubscriberServiceInternalException
   {
      Subscriber sub=getSub(msisdn);
      if(sub==null)
      {
         throw new SubscriberServiceException("Cannot find subscriber");
      }
      
      Account acct=getAcct(ctx,sub.getBAN());
      if(acct==null)
      {
         throw new SubscriberServiceException("Cannot find account");
      }
      
      return (int)sub.getPricePlan();
   }
   
	/**
	 * the changeState method is used to change the subscriber's state within their
            subscriber profile.
	 * 
	 * @param  msisdn
	 * @param  state
	 * @exception SubscriberServiceException
	 * @exception SubscriberServiceInternalException
	 */
	@Override
    public void changeState(Context ctx,String msisdn, com.redknee.app.crm.bean.SubscriberStateEnum state)
		throws SubscriberServiceException,  SubscriberServiceInternalException
	{
	   Subscriber sub=getSub(ctx, msisdn);
	   if(sub==null)
	   {
	      throw new SubscriberServiceException("Cannot find account");
	   }
       
	   sub.setState(state);
	   editSub(sub);
	}

	/**
	 * the changePackage method is used to change the subscriber's package within their
            subscriber profile.
	 * 
	 * @param  msisdn
	 * @param  packageID
	 * @exception SubscriberServiceException
	 * @exception SubscriberServiceInternalException
	 */
	@Override
    public void changePackage(Context ctx,String msisdn, String packageID)
		throws SubscriberServiceException,  SubscriberServiceInternalException
	{
	   Subscriber sub=getSub(msisdn);
	   if(sub==null)
	     {
	      throw new SubscriberServiceException("Cannot find account");
	   }
	   
	   sub.setPackageId(packageID);
	   editSub(sub);
	}

	/**
	 * the changeCredit method is used to change the subscriber's credit limit
            within their subscriber profile.
	 * 
	 * @param  msisdn
	 * @param  creditLimit
	 * @exception SubscriberServiceException
	 * @exception SubscriberServiceInternalException
	 */
	@Override
    public void changeCredit(Context ctx,String msisdn, long creditLimit)
		throws SubscriberServiceException,  SubscriberServiceInternalException
	{
	   Subscriber sub=getSub(msisdn);
	   if(sub==null)
	     {
	      throw new SubscriberServiceException("Cannot find account");
	   }
	   
	   sub.setCreditLimit(creditLimit);
	   editSub(sub);
	}

	/**
	 * the changeServices method is used to change the subscriber's services
            within their subscriber profile. The services include Voice, SMS, WAP or FAX.
	 * 
	 * @param  msisdn
	 * @param  services
	 * @exception SubscriberServiceException
	 * @exception SubscriberServiceInternalException
	 */
	@Override
    public void changeServices(Context ctx,String msisdn, java.util.Set services)
		throws SubscriberServiceException,  SubscriberServiceInternalException
	{
	   Subscriber sub=getSub(msisdn);
	   if(sub==null)
	   {
	      throw new SubscriberServiceException("Cannot find account");
	   }
	   
	   sub.setServices(services);
	   editSub(sub);
	}

	/**
    * The balanceSub is used to find the specified subscribers current balance
    * and their used/available voice and text bundles.
    * 
    * @param msisdn
    * @return 
    * @exception SubscriberServiceException
    * @exception SubscriberServiceInternalException
    */
	@Override
    public SubscriberBalance balanceSub(Context ctx,String msisdn)
		throws SubscriberServiceException,  SubscriberServiceInternalException
	{
	   Subscriber sub=getSub(msisdn);
	   if(sub==null)
	   {
	      throw new SubscriberServiceException("Cannot find account");
	   }
	   
	   SubscriberBalance bal=new SubscriberBalance(); 

	   bal.setAmountOwing(sub.getAmountOwing());
       // TODO - 2004-11-18 - This code does not appear to be used.  It was
	   // getting the free minutes information from the subscriber but that is
	   // not how we manage such data any longer.
	   //bal.setFreeMINAvail(sub.getFreeMinAvail());
	   //bal.setFreeMINUsed(sub.getFreeMinUsed());
	   bal.setFreeSMSAvail(0);
	   bal.setFreeSMSSent(0);
	   
	   return bal;
	}
	
	/**
	 * Returns the amount Owing of the subscriber requested.
	 * 
	 * @param  id
	 * @return AmountOwing
	 * @exception SubscriberServiceException
	 * @exception SubscriberServiceInternalException
	 */
	@Override
    public AmountOwing getAmountOwing(Context ctx, String id)
		throws SubscriberServiceException,  SubscriberServiceInternalException
	{
		AmountOwing owing = new AmountOwing();
		Context subCtx = ctx.createSubContext();
		try
		{
			Subscriber subs = SubscriberSupport.lookupSubscriberForSubId(subCtx, id);
			
			if (!subs.isPrepaid())
			{
				subs.setContext(subCtx);
				owing.setId(id);
				owing.setAmountOwing(subs.getAmountOwing(subCtx));
				owing.setMonthToDate(subs.getMonthToDateBalance(subCtx));
			}
		} 
		catch (HomeException e) 
		{
			LogSupport.info(ctx, this, e.getMessage(), e);
			throw new SubscriberServiceException(e);
		}
		
	    return owing;
	}

}


