/*
 * Created on May 23, 2005
 *
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
package com.trilogy.app.crm.external.smsb;

import java.util.Collection;

import com.trilogy.app.crm.client.exception.SMSBReturnCodeMsgMapping;
import com.trilogy.app.crm.client.smsb.AppSmsbClient;

import com.trilogy.app.smsb.dataserver.smsbcorba.subsProfile7;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.AbstractHome;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.Visitor;

public class SmsbServiceHome extends AbstractHome implements Adapter
{
	public SmsbServiceHome(Context ctx)
	{
		super(ctx);
	}

	public Object create(Context ctx, Object obj) throws HomeException
	{
		if(obj==null)
		{
			return null;
		}
		
		if(getService(ctx)==null)
		{
			return null;
		}
		
		subsProfile7 profile=(subsProfile7) adapt(ctx,obj);
		
		int result=getService(ctx).addSubscriber(profile);
		
		if(result!=0)
		{
			throw new HomeException("SMS profile creation failed for MSISDN " + profile.msisdn + SMSBReturnCodeMsgMapping.getMessage(result));
		}
		
		return obj;
	}

	public Object store(Context ctx, Object obj) throws HomeException
	{
		if(obj==null)
		{
			return null;
		}
		
		if(getService(ctx)==null)
		{
			return null;
		}
		
		subsProfile7 profile=(subsProfile7) adapt(ctx,obj);
		
		int result=getService(ctx).updateSubscriber(profile);

		if(result!=0)
		{
            throw new HomeException("SMS profile update failed for MSISDN " + profile.msisdn + SMSBReturnCodeMsgMapping.getMessage(result));
		}
		
		return obj;
	}

	public Object find(Context ctx, Object obj) throws HomeException
	{
		if(obj==null)
		{
			return null;
		}
		
		if(getService(ctx)==null)
		{
			return null;
		}
		
		String msisdn="";
		if(obj instanceof String)
		{
			msisdn=(String) obj;
		}
		else if(obj instanceof SmsbSubscriber)
		{
			msisdn=((SmsbSubscriber)obj).getMsisdn();
		}
		
		subsProfile7 profile=getService(ctx).getSubsProfile(msisdn);
		
		return unAdapt(ctx,profile);
	}

	public void remove(Context ctx, Object obj) throws HomeException
	{
		if(obj==null)
		{
			return;
		}
		
		if(getService(ctx)==null)
		{
			return;
		}
		
		getService(ctx).deleteSubscriber(((SmsbSubscriber)obj).getMsisdn());
	}

	private AppSmsbClient getService(Context ctx)
	{
		return (AppSmsbClient) ctx.get(AppSmsbClient.class);
	}
	
	public Collection select(Context ctx, Object obj) throws HomeException
	{
		throw new UnsupportedOperationException();
	}

	public void removeAll(Context ctx, Object where) throws HomeException
	{
		throw new UnsupportedOperationException();
	}

	public Visitor forEach(Context ctx, Visitor visitor, Object where) throws HomeException
	{
		throw new UnsupportedOperationException();
	}

	public Object adapt(Context ctx, Object obj) throws HomeException
	{
		if(obj==null)
		{
			return null;
		}
		
		SmsbSubscriber sub=(SmsbSubscriber)obj;
		
		subsProfile7 profile = new subsProfile7();

		profile.msisdn = sub.getMsisdn();
		profile.imsi = sub.getImsi();
		profile.spid = (short) sub.getSpid();
		profile.svcGrade = sub.getSvcGrade();
		profile.ban = sub.getBan();
		profile.language = sub.getLanguage();
		profile.location = sub.getLocation();
		profile.TzOffset = sub.getTzOffset();
		profile.ratePlan = sub.getPricePlan();
		profile.scpid = sub.getScpId();
		profile.hlrid = sub.getHlrId();
		profile.enable = sub.getEnable();
		profile.barringplan = sub.getBarringPlan();

		return profile;
	}

	public Object unAdapt(Context ctx, Object obj) throws HomeException
	{
		if(obj==null)
		{
			return null;
		}
		
		subsProfile7 profile=(subsProfile7) obj;
		SmsbSubscriber sub=new SmsbSubscriber();

		sub.setMsisdn(profile.msisdn);
		sub.setImsi(profile.imsi);
		sub.setSpid(profile.spid);
		sub.setSvcGrade(profile.svcGrade);
		sub.setBan(profile.ban);
		sub.setLanguage(profile.language);
		sub.setLocation(profile.location);
		sub.setTzOffset(profile.TzOffset);
		sub.setPricePlan(profile.ratePlan);
		sub.setScpId(profile.scpid);
		sub.setHlrId(profile.hlrid);
		sub.setEnable(profile.enable);
		sub.setBarringPlan(profile.barringplan);
		
		return sub;
	}

}
