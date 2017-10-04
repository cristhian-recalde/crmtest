/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee, no
 * unauthorised use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the licence agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.web.border;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.support.IdentitySupport;
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.CreditCategory;
import com.trilogy.app.crm.bean.SystemNoteSubTypeEnum;
import com.trilogy.app.crm.bean.SystemNoteTypeEnum;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.NoteSupport;
import com.trilogy.app.crm.support.NoteSupportHelper;


public class PTPCurrentNumTransitionBorder implements Border
{

    public void service(Context ctx, HttpServletRequest req, HttpServletResponse res, RequestServicer delegate)
            throws ServletException, IOException
    {
        PrintWriter out = res.getWriter();
        String ban = parseBan(ctx, req.getParameter("key"));
        Account act = null;
        try
        {
            if ((ban != null) && (ban.trim().length() != 0))
            {
                act = AccountSupport.getAccount(ctx, ban);
                if (act != null)
                {
                    CreditCategory cc = HomeSupportHelper.get(ctx).findBean(ctx, CreditCategory.class, act.getCreditCategory());
                    
                    String resetPTP = req.getParameter("resetPTP");
                    if ((resetPTP != null) && (resetPTP.trim().length() != 0))
                    {
                        
                        act.setCurrentNumPTPTransitions(0);
                        act.setPromiseToPayStartDate(null);
                        if (act.getAccumulatedBalance() <= 0)
                        {
                            act.setState(AccountStateEnum.ACTIVE);
                        }
                        else
                        {
                            act.setState(AccountStateEnum.IN_ARREARS);
                        }
                        Home home = (Home) ctx.get(AccountHome.class);
                        home.store(ctx, act);
                        NoteSupportHelper.get(ctx).addAccountNote(ctx, act.getBAN(), "Number of transitions has been reset",
                                SystemNoteTypeEnum.EVENTS, SystemNoteSubTypeEnum.PTPReset);

                        try
                        {
                            //generate ER 1107
                            ERLogger.genPTPResetER(ctx, act, cc, act.getState());
                        }
                        catch (Exception e)
                        {
                            new MinorLogMsg(this, "Error generating ER 1107 - BAN: " + act.getBAN(), e).log(ctx);
                        }
                        
                        out.print("<font color='red'>Current Number of PTP transitions set to 0</font>");
                    }
                    else if ((cc != null) && (act.getCurrentNumPTPTransitions() == cc.getMaxNumberPTP()))
                    {
                        if (act.getState().equals(AccountStateEnum.PROMISE_TO_PAY))
                        {
                            if (act.getPromiseToPayDate() != null)
                            {
                                Date finalDate;
                                Calendar cal = Calendar.getInstance();
                                cal.setTime(act.getPromiseToPayDate());
                                cal.add(Calendar.DAY_OF_YEAR, cc.getMaxNumberPTP());
                                finalDate = cal.getTime();
                                Date today = new Date();
                                long daysInBetween = today.getTime() - finalDate.getTime();
                                daysInBetween = daysInBetween / (1000 * 60 * 60 * 24);
                                if (finalDate.before(today))
                                {
                                    out.print("PTP: " + act.getCurrentNumPTPTransitions() + "(PTP uses) of "
                                            + cc.getMaxNumberPTP() + "  (max. permitted) within last " + daysInBetween
                                            + " days");
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            out.print("Error in resetCounter " + e);
            new MinorLogMsg(this, "Error in resetPTPcounter", e).log(ctx);
        }
        delegate.service(ctx, req, res);
    }

	private String parseBan(Context ctx, String key)
	{
		String ban = null;
		String separator = "" + IdentitySupport.SEPERATOR;
		
		if (key != null)
		{
			String [] keys = key.split(separator);
			
			/*
			 * Converged search has a pair as a key, the msisdn and the 
			 * ban the ban is always second 
			 *
			 * */
			
			if (keys.length <= 1)
			{
				ban = keys[0];
			}
			else
			{
				ban = keys[1];
			}
			
	
			LogSupport.debug(ctx, this, "Ban to search is " + ban);
		}
		return ban;
	}
	
}