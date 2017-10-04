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
package com.trilogy.app.crm.web.control;

import java.util.Iterator;
import java.util.List;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.ConvergedAccountSubscriber;
import com.trilogy.app.crm.bean.account.AccountIdentification;
import com.trilogy.app.crm.blacklist.BlackListSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * To handled issue in TT#5062820359 (Display Balcklisted account's ban in RED)
 * @author Sheetal Thakur
 */
public class ConvergedBanTextFieldWebControl extends ProxyWebControl
{
	public ConvergedBanTextFieldWebControl( WebControl delegate)
	{
		super(delegate);
	}
	
	public void toWeb(Context ctx, java.io.PrintWriter out, String name, Object obj)
	{
		try
		{
			boolean isBlackListed = false;
			
			ConvergedAccountSubscriber bean = (ConvergedAccountSubscriber) ctx.get(AbstractWebControl.BEAN);
			
			Home acctHome = (Home) ctx.get(AccountHome.class);
			Account acct=null;
			
			try {
				acct=(Account)acctHome.find(ctx,bean.getBAN());
				
			} catch (HomeException e) {
				new MinorLogMsg(this, "Unable to find account [BAN=" + bean.getBAN()+ "]", e).log(ctx);
			}
			
			if(acct!=null)
			{
	            List accountIdList = acct.getIdentificationList();
	            if(null != accountIdList)
	            {
	                Iterator i = accountIdList.iterator();
	                while(i.hasNext() && !isBlackListed)
	                {
	                    AccountIdentification ai = (AccountIdentification)i.next();
	                    isBlackListed = isBlackListed || validateGrayIdField(ctx, ai.getIdType(), ai.getIdNumber());
	                }
	            }
			}

			if(isBlackListed)
			{
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, " Account [BAN" + bean.getBAN()+ "] identified as Blacklisted.", null).log(ctx);
                }
                out.println("<font color=\"red\">");
            }   

            super.toWeb(ctx, out, name, obj);
			
			if(isBlackListed)
			{
				out.println("</font>");
			}
		}
		catch(Exception e)
		{
			//eat it
		}
	}

	//Highlight warning message if id is in black/grey list
	public boolean validateGrayIdField(Context ctx, int idType, String idNumber) 
	{
		boolean flag=false;
		try
		{				
			// nothing to validate
			if ((idNumber == null) || (idNumber.length() == 0))
			{
				return flag;
			}
			if ( BlackListSupport.isIdInList(ctx, idType, idNumber) )
			{
				flag=true;
				return flag;
			}
			return flag;	
		}
		catch(Exception exp)
		{
			return flag;
		}
	}	

}