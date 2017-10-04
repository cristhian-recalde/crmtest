/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee, no unauthorised use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the licence agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.transaction;

import java.util.Collection;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GT;
import com.trilogy.framework.xhome.elang.NEQ;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.state.InOneOfStatesPredicate;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.CollectionSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;

/**
 * Abstract Proportioning Calculator 
 * 
 * @author Angie Li
 */
public abstract class AbstractProportioningCalculator implements ProportioningCalculator 
{
	/**
    * Returns all postpaid subscribers in the following states:
    *   Active, Non-payment Suspended, Suspended,
    *   Promise-to-Pay, In Collection, Non-payment Warned,
    *   In Arrears.
    * @param ctx
    * @param acct
    * @return Collection
    * @throws HomeException
    */
   protected Collection getPostpaidSubscribers(Context ctx,Account acct, CRMSpid spid)
   throws HomeException
   {
       if (spid.isPaymentAcctLevelToInactive())
       {
           // Return all postpaid subscribers
           return getPostpaidSubscribers(ctx, acct);
       }
       else
       {
           // Don't return deactivated subscribers
           return getPostpaidSubscribers(ctx, acct, ACTIVE_SUBSCRIPTION_STATES);
       }
   }
   
   /**
    * Gets a postpaid subscribers in the account that are in one of the given states
    * 
    * @param ctx
    * @param acct
    * @param states
    * @return Collection of 0 or more subscribers
    * @throws HomeException
    */
   protected Collection<Subscriber> getPostpaidSubscribers(Context ctx, Account acct, SubscriberStateEnum... states) 
   throws HomeException
   {
       Collection<Subscriber> cl = null;
       
       if (acct.isResponsible())
       {
            cl = AccountSupport.getNonResponsibleSubscribers(ctx, acct);
       }
       else
       {
           cl = AccountSupport.getImmediateChildrenSubscribers(ctx,acct);
       }
                  
          cl = CollectionSupportHelper.get(ctx).findAll(ctx, cl, new EQ(SubscriberXInfo.SUBSCRIBER_TYPE, SubscriberTypeEnum.POSTPAID));
          if (states != null && states.length > 0)
          {
              cl = CollectionSupportHelper.get(ctx).findAll(ctx, cl,new InOneOfStatesPredicate(states));
          }
               
          return cl;
   }
   
   /**
    * Return the total owing excluding payment plan of all the subscribers in a given subscriber list.
    *
    * @param subs The subscriber collection.
    * @return long The total owing of the subscribers.
    */
   protected long getTotalOwingOfSubscribersWithoutPaymentPlan(Context ctx, final Collection<Subscriber> subs)
   {
       long totalOwing = 0;
       for (Subscriber sub : subs)
       {
           // Reset the amount owing for the subscriber so that the getter will calculate it.
           sub.setAmountOwingWithoutPaymentPlan(SubscriberSupport.INVALID_VALUE);

           final long subOwing = sub.getAmountOwingWithoutPaymentPlan(ctx);
           if (subOwing > 0)
           {
               // Only subscribers with an outstanding balance are taken into account.
               totalOwing += subOwing;
           }
       }
       return totalOwing;
   }


   /**
    * Return the first postpaid subscriber in a given subscriber list.
    *
    * @param subs The subscriber collection.
    * @param letDeactivated true if the configuratio to let deactive subscribers is on
    * @return Subscriber The first subscriber.
    */
   protected Subscriber getFirstPostpaidSubscriber(final Context ctx, final CRMSpid spid, final Collection<Subscriber> subs)
   {
       return getFirstSubscriber(ctx, spid, subs, new EQ(SubscriberXInfo.SUBSCRIBER_TYPE, SubscriberTypeEnum.POSTPAID));
   }


   /**
    * Return the first subscriber with an outstanding balance in a given subscriber list.
    *
    * @param subs The subscriber collection.
    * @param letDeactivated true if the configuration to let deactive subscribers is on
    * @return Subscriber The first subscriber with an outstanding balance.
    */
   protected Subscriber getFirstOwingSubscriber(final Context ctx, final CRMSpid spid, final Collection<Subscriber> subs)
   {
       return getFirstSubscriber(ctx, spid, subs, 
               new And().add(new GT(SubscriberXInfo.AMOUNT_OWING, 0L))
                        .add(new EQ(SubscriberXInfo.SUBSCRIBER_TYPE, SubscriberTypeEnum.POSTPAID)));
   }

   private Subscriber getFirstSubscriber(final Context ctx, final CRMSpid spid, final Collection<Subscriber> subs, Predicate p)
   {
       Subscriber result = null;
       
       if( spid.isInactiveSubscriberPriority() )
       {
           // SPID flag says to give priority to INACTIVE accounts
           Collection<Subscriber> inactiveSubs = CollectionSupportHelper.get(ctx).findAll(ctx, subs, new EQ(SubscriberXInfo.STATE, SubscriberStateEnum.INACTIVE));

           // Return an inactive subscriber with an outstanding balance if one exists.
           // This is done in 2 steps to avoid calculating the amount owing of all subscribers.
           // This way, we only calculate the amount owing of at most all inactive subscribers.
           And predicate = new And();
           predicate.add(new GT(SubscriberXInfo.AMOUNT_OWING, 0L));
           predicate.add(p);
           result = CollectionSupportHelper.get(ctx).findFirst(ctx, inactiveSubs, predicate);
       }
       
       if( result == null )
       {
           // Find first non-deactivated subscriber matching selection criteria
           And predicate = new And();
           predicate.add(new NEQ(SubscriberXInfo.STATE, SubscriberStateEnum.INACTIVE));
           predicate.add(p);
           
           result = CollectionSupportHelper.get(ctx).findFirst(ctx, subs, predicate);
       }
       
       return result;
   }
   
   protected SubscriberStateEnum[] ACTIVE_SUBSCRIPTION_STATES = 
   {
		   SubscriberStateEnum.IN_ARREARS,
		   SubscriberStateEnum.NON_PAYMENT_WARN,
		   SubscriberStateEnum.IN_COLLECTION,
		   SubscriberStateEnum.PROMISE_TO_PAY,
		   SubscriberStateEnum.SUSPENDED,
		   SubscriberStateEnum.NON_PAYMENT_SUSPENDED,
		   SubscriberStateEnum.ACTIVE
   };
}
