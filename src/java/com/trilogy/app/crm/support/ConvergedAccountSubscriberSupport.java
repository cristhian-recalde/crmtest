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
package com.trilogy.app.crm.support;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import com.trilogy.app.crm.bean.*;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.SortingHome;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * @author lxia
 */
public class ConvergedAccountSubscriberSupport
{

	public static Home mergeAccountHome(Context ctx,Account acct,short searchType)
	{
	    return mergeAcctSubHome(ctx, acct, Collections.EMPTY_LIST, null, searchType);
	}
	/**
	 * Adds a SpidAware home to the home pipeline to search for the right subscribers on the right SPID user
	 * @param ctx
	 * @param home
	 * @return
	 */
	private static Home getHome(Context ctx, Home home) 
	{
		Home homeResult = home;
		
		if (home == null)
		{
			homeResult = new SortingHome(ctx,new ConvergedAccountSubscriberTransientHome(ctx),new ConvergedAcctSubComparator());
		}
		else
		{
			homeResult = home;
		}
		
		return homeResult;
	}
	public static Home mergeAcctSubHome(Context ctx,Account acct,Collection subs,short searchType)
	{
	    return mergeAcctSubHome(ctx, acct, subs, null, searchType);
	}

	public static ConvergedStateEnum mapConvergedState(Context ctx, SubscriberStateEnum state)
	{
		Map map = (Map) ctx.get(ConvergedStateEnum.class);
		ConvergedStateEnum result = (ConvergedStateEnum) map.get(state);
		return result;
	}

	public static ConvergedStateEnum mapConvergedState(Context ctx, AccountStateEnum state)
	{
		Map map = (Map) ctx.get(ConvergedStateEnum.class);
		ConvergedStateEnum result = (ConvergedStateEnum) map.get(state);
		return result;
	}

    	
    public static Home mergeAcctSubHome(Context ctx,Account acct,Subscriber subs,Home conAcctSub,short searchType)
    {
        return mergeAcctSubHome(ctx, acct, Arrays.asList(subs), conAcctSub, searchType);
    }
    public static Home mergeAcctSubHome(Context ctx,Account acct,Collection subs,Home conAcctSub,short searchType)
    {
    	conAcctSub = getHome(ctx, conAcctSub);
    
        if (acct!=null)
        {
        	ConvergedAccountSubscriber conAcct = new ConvergedAccountSubscriber();
        	
        	conAcct.setBAN(acct.getBAN());
        	conAcct.setAccountName(acct.getAccountName());
    	    conAcct.setBillingAddress1(acct.getBillingAddress1());
    	    conAcct.setCompanyName(acct.getCompanyName());
    	    conAcct.setFirstName(acct.getFirstName());
    	    conAcct.setLastName(acct.getLastName());
    	    conAcct.setType(SearchTypeEnum.get(searchType));
    	    conAcct.setState(mapConvergedState(ctx, acct.getState()));
    	    conAcct.setSpid(acct.getSpid());
    	    conAcct.setBillingType(acct.getSubscriberType());
    	   
    	    
    	    try
    		{
    			conAcctSub.create(ctx,conAcct);
    		}
    		catch (Exception e)
    		{
    			if(LogSupport.isDebugEnabled(ctx))
    			{
    				new DebugLogMsg(ConvergedAccountSubscriberSupport.class.getName(),e.getMessage(),e).log(ctx);
    			}
    		}
        
            if(subs!=null && !subs.isEmpty() && acct.isIndividual(ctx))
            {
            	for (Iterator it = subs.iterator(); it.hasNext();)
        		{
            	    Subscriber sub =(Subscriber) it.next();
            	    if (sub == null)
            	    {
            	        continue;
            	    }
        			ConvergedAccountSubscriber conSub= new ConvergedAccountSubscriber();
        			
        			
        			conSub.setBAN(sub.getBAN());
        			conSub.setMSISDN(sub.getMSISDN());
                    // TODO 2008-08-22 name no longer part of subscriber
        		    //conSub.setFirstName(sub.getFirstName());
        		    //conSub.setLastName(sub.getLastName());
        		    conSub.setIMSI(sub.getIMSI());
        		    conSub.setPackageId(sub.getPackageId());
        		    conSub.setSubId(sub.getId());
        		    conSub.setType(SearchTypeEnum.get(searchType));
        		    conSub.setState(mapConvergedState(ctx, sub.getStateWithExpired()));
        		    conSub.setSpid(sub.getSpid());
                    conSub.setBillingType(sub.getSubscriberType());
    
            		try
        			{
        				conAcctSub.create(ctx,conSub);
        			}
        			catch (Exception e)
        			{
        				if(LogSupport.isDebugEnabled(ctx))
        				{
        					new DebugLogMsg(ConvergedAccountSubscriberSupport.class.getName(),e.getMessage(),e).log(ctx);
        				}
        			}
        		}
            }
        }

        return conAcctSub;
    }

}
