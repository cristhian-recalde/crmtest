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
package com.trilogy.app.crm.home.sub;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.account.filter.ResponsibleAccountPredicate;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.state.InOneOfStatesPredicate;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.CollectionSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.bean.GroupTypeEnum;

/**
 * This class insert / update the owner MSISDN (to the group MSISDN) for group-pooled & Group 
 * accounts if the subscriber of the owner MSISDN is no longer in valid state.
 * 
 * CASE I : When first non responsible postpaid account under group or group pool account is created then set is as owner of the group. 
 * 
 * CASE II :  If subscriber state change from Active to Deactivate then next non responsible subscriber will be owner of that group.
 * This home check the next owner & reset against PArent account.
 * 
 * CASE III : When account's first subscriber state is updated from  "PENDING"  to "ACTIVE" state then afer activation set it as owner of the group.
 *
 * @author unmesh.sonawane@redknee.com
 */
public class PooledGroupOwnerAccountOwnerMsisdnResettingHome extends HomeProxy
{
    /**
     * Creates a new PooledGroupOwnerAccountOwnerMsisdnResettingHome for the
     * given home.
     *
     * @param delegate The Home to which this object delegates.
     */
    public PooledGroupOwnerAccountOwnerMsisdnResettingHome(final Home delegate)
    {
        super(delegate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
	public Object store(Context ctx, final Object obj) throws HomeException 
	{
		final Subscriber newSubscriber = (Subscriber) obj;
		final Subscriber oldSubscriber = (Subscriber) ctx
				.get(Lookup.OLDSUBSCRIBER);

		Object ret = super.store(ctx, newSubscriber);

		// get the curent account
		final Account account = (Account) ctx.get(Account.class);

		// Owner msisdn is always non-responsible postpaid account & Account
		// must be a part of group or group-pool account
		if (!newSubscriber.isPostpaid() && !account.isIndividual(ctx)
				&& account.getParentAccount(ctx) == null
				&& account.isResponsible()) 
		{
			return ret;
		}

		// get the parent account detail
		Account acct = account.getParentAccount(ctx);

		// Parent account must be a group or group pooled account.
		if (acct.isPooled(ctx)	|| acct.getGroupType().equals(GroupTypeEnum.GROUP)) 
		{
			if (LogSupport.isDebugEnabled(ctx)) 
			{
				LogSupport.debug(ctx, this, "Parent Account found " + acct);
			}

		} else 
		{
			return ret;
		}

		// set the next non responsible postpaid subscriber as owner if previous
		// owner is deactivated.
		if (oldSubscriber.getState() == SubscriberStateEnum.ACTIVE
				&& newSubscriber.getState() == SubscriberStateEnum.INACTIVE) 
		{

			// check subscriber msisdn is equal to parent owner msisdn that
			// means owner account is deactivating
			if (acct.getOwnerMSISDN() != null
					&& acct.getOwnerMSISDN().equals(newSubscriber.getMSISDN())) 
			{

				// Find the next non responsible postpaid subscriber
				List<Subscriber> subs = (List<Subscriber>) AccountSupport.getNonResponsibleSubscribers(getContext(), acct);

				// if subscriber list is not Empty.
				if (null != subs && !subs.isEmpty()) 
				{

					And predicate = new And();
					predicate.add(new EQ(SubscriberXInfo.SUBSCRIBER_TYPE,SubscriberTypeEnum.POSTPAID));
					predicate.add(new InOneOfStatesPredicate(SubscriberStateEnum.ACTIVE));

					// get all non responsible subscriber.
					subs = (List<Subscriber>) CollectionSupportHelper.get(ctx).findAll(ctx, subs, predicate);

					// Sort the subscriber on date creation basis.
					Collections.sort(subs,new SubscriberCreationDateComparator());

					// get the first subscriber as owner.
					Subscriber ownerSubscriber = subs.get(0);

					// if new owner subscriber is postpaid & parent account's owner msisdn is not null.
					// update the owner msisdn of parent account
					if ((ownerSubscriber.isPostpaid())
							&& acct.getOwnerMSISDN() != null) 
					{
						final Home accountHome 	= (Home) ctx.get(AccountHome.class);
						String msisdn 			= ownerSubscriber.getMsisdn();
						
						acct.setOwnerMSISDN(msisdn);
						accountHome.store(ctx, acct);
					}
				}

			}// end of if
			// Account creation failure then it goes to Pending state
			// & when subscriber is updated from pending state to Active state
			// then we have to set the owner MSISDN of new subscriber.
		} else if (oldSubscriber.getState() == SubscriberStateEnum.PENDING
				&& (newSubscriber.getState() == SubscriberStateEnum.ACTIVE)) 
		{
			if ((!account.isResponsible() && newSubscriber.isPostpaid()) && (acct.getOwnerMSISDN() == null || acct.getOwnerMSISDN().trim().length() < 1)) 
			{
				final Home accountHome 	= (Home) ctx.get(AccountHome.class);
				String msisdn 			= newSubscriber.getMsisdn();
				
				acct.setOwnerMSISDN(msisdn);
				accountHome.store(ctx, acct);
			}
		}

		return ret;
	}
    
 /**
  * Update the owner msisdn of group or group pooled account when first non-responsible postpaid subscriber is created in that group.   
  */
    @Override
	public Object create(Context ctx, final Object obj) throws HomeException 
	{
		final Subscriber newSubscriber = (Subscriber) obj;
		final Subscriber oldSubscriber = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);

		Object ret = super.create(ctx, newSubscriber);

		final Account account = (Account) ctx.get(Account.class);

		if (!newSubscriber.isPostpaid() && !account.isIndividual(ctx) && account.getParentAccount(ctx) == null) 
		{
			return ret;
		}

		if ((oldSubscriber == null || oldSubscriber.getState() != SubscriberStateEnum.ACTIVE)
				&& (newSubscriber.getState() == SubscriberStateEnum.ACTIVE)) {

			Account acct = account.getParentAccount(ctx);

			if (acct.isPooled(ctx) || acct.getGroupType().equals(GroupTypeEnum.GROUP)) 
			{
				if (LogSupport.isDebugEnabled(ctx)) 
				{
					LogSupport.debug(ctx, this,"Parent Account found :" + acct.getBAN());
				}

			} else 
			{
				return ret;
			}

			if ((!account.isResponsible() && newSubscriber.isPostpaid()) && (acct.getOwnerMSISDN() == null || acct.getOwnerMSISDN().trim().length() < 1)) 
			{
				final Home accountHome 	= (Home) ctx.get(AccountHome.class);
				String msisdn 			= newSubscriber.getMsisdn();
				
				acct.setOwnerMSISDN(msisdn);
				accountHome.store(ctx, acct);
			}
		}

		return ret;
	}
}

/**
 * 
 * @author unmesh.sonawane
 * 
 * Sort the list of subscriber on basis of creation date.
 *
 */
class SubscriberCreationDateComparator implements Comparator<Subscriber> {

	@Override
	public int compare(Subscriber o1, Subscriber o2) {
		Date d1 = o1.getDateCreated();
		Date d2 = o2.getDateCreated();

		if (d1.after(d2))
			return 1;
		else if (d1.before(d2))
			return -1;
		else
			return 0;
	}
}
