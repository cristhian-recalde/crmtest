/*
 * Created on May 14, 2005
 * 
 * This code is a protected work and subject to domestic and international copyright law(s). A complete listing of
 * authors of this work is readily available. Additionally, source code is, by its very nature, confidential information
 * and inextricably contains trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be used in accordance with the
 * terms of the license agreement entered into with Redknee Inc. and/or its subsidiaries.
 * 
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home.sub;

import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.FacetMgr;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.webcontrol.WebControl;

import com.trilogy.app.crm.numbermgn.*;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.bean.Subscriber;

/**
 * Tracks subscriber profile modification to the corresponding package and msisdn history
 */
public class SubModAppendNumberMgmtHistoryHome extends AppendNumberMgmtHistoryHome
{
	public SubModAppendNumberMgmtHistoryHome(Home delegate)
	{
		super(delegate);
	}

	public Object store(Context ctx, Object obj) throws HomeException, HomeInternalException
	{
		Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
		Subscriber newSub = (Subscriber) obj;

		Object result = super.store(ctx, obj);

		createMessageHistoryDetail(ctx, oldSub, newSub);

		return result;
	}

	/**
	 * @param ctx
	 * @param oldSub
	 * @param newSub
	 * @throws HomeException
	 * @throws HomeInternalException
	 */
	protected void createMessageHistoryDetail(Context ctx, Subscriber oldSub, Subscriber newSub) throws HomeException,
		HomeInternalException
	{
		StringBuilder detail = new StringBuilder();
		FacetMgr fmgr = (FacetMgr) ctx.get(FacetMgr.class);
		MessageMgr mmgr = new MessageMgr(ctx, fmgr.getClass(ctx, Subscriber.class, WebControl.class));
		NumberMgmtHistory history = null;
		// trace changes in subscriber name
//		compareField(oldSub.getFirstName(), newSub.getFirstName(), mmgr.get("Subscriber.firstName.Label",
//			SubscriberLanguageSupport.FirstNameLabel), detail);
//		compareField(oldSub.getLastName(), newSub.getLastName(), mmgr.get("Subscriber.lastName.Label",
//			SubscriberLanguageSupport.LastNameLabel), detail);
		if (detail.length() > 0)
		{
			// TODO need to modify this to log Account History when the above logic is uncommented (and put back in the pipeline)
			history = (NumberMgmtHistory) appendAccountMsisdnHistory(ctx, newSub.getMSISDN(), newSub.getBAN(), newSub.getLastModified(), getHistoryEventSupport(ctx)
				.getSubNameModificationEvent(ctx), detail.toString());
			history = (NumberMgmtHistory) appendPackageHistory(ctx, newSub.getPackageId(), getHistoryEventSupport(ctx)
				.getSubNameModificationEvent(ctx), detail.toString());
		}
		// trace changes in subscriber address
		detail = new StringBuilder();
//		compareField(oldSub.getAddress1(), newSub.getAddress1(), mmgr.get("Subscriber.address1.Label",
//			SubscriberLanguageSupport.Address1Label), detail);
//		compareField(oldSub.getAddress2(), newSub.getAddress2(), mmgr.get("Subscriber.address2.Label",
//			SubscriberLanguageSupport.Address2Label), detail);
//		compareField(oldSub.getAddress3(), newSub.getAddress3(), mmgr.get("Subscriber.address3.Label",
//			SubscriberLanguageSupport.Address3Label), detail);
//		compareField(oldSub.getCity(), newSub.getCity(), mmgr.get("Subscriber.city.Label",
//			SubscriberLanguageSupport.CityLabel), detail);
//		compareField(oldSub.getProvince(), newSub.getProvince(), mmgr.get("Subscriber.province.Label",
//			SubscriberLanguageSupport.ProvinceLabel), detail);
//		compareField(oldSub.getCountry(), newSub.getCountry(), mmgr.get("Subscriber.country.Label",
//			SubscriberLanguageSupport.CountryLabel), detail);
		if (detail.length() > 0)
		{
			// TODO need to modify this to log Account History when the above logic is uncommented (and put back in the pipeline)
			history = (NumberMgmtHistory) appendAccountMsisdnHistory(ctx, newSub.getMSISDN(), newSub.getBAN(), newSub.getLastModified(), getHistoryEventSupport(ctx)
				.getSubAddrModificationEvent(ctx), detail.toString());
			history = (NumberMgmtHistory) appendPackageHistory(ctx, newSub.getPackageId(), getHistoryEventSupport(ctx)
				.getSubAddrModificationEvent(ctx), detail.toString());
		}
	}

	/**
	 * compares 2 string values and if they are different it appends them to a message
	 * @param oldValue
	 * @param newValue
	 * @param propertyName
	 * @param buffer
	 */
	protected void compareField(Object oldValue, Object newValue, String propertyName, StringBuilder buffer)
	{
		if (!SafetyUtil.safeEquals(oldValue, newValue))
		{
			buffer.append(propertyName);
			buffer.append(" updated from [");
			buffer.append(oldValue);
			buffer.append("] to [");
			buffer.append(newValue);
			buffer.append("]\n");
		}
	}
}
