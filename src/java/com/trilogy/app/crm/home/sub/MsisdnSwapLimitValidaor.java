/*
 * Copyright (c) 2007, REDKNEE.com. All Rights Reserved.
 * 
 * This software is the confidential and proprietary information of REDKNEE.com.
 * ("Confidential Information"). You shall not disclose such Confidential Information and
 * shall use it only in accordance with the terms of the license agreement you entered
 * into with REDKNEE.com.
 * 
 * REDKNEE.COM MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT.
 * REDKNEE.COM SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */


package com.trilogy.app.crm.home.sub;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.trilogy.app.crm.bean.PortingTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.Msisdn;
import com.trilogy.app.crm.extension.spid.MsisdnSwapLimitSpidExtension;
import com.trilogy.app.crm.extension.spid.MsisdnSwapLimitSpidExtensionXInfo;
import com.trilogy.app.crm.numbermgn.HistoryEventSupport;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistory;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistoryHome;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistoryXInfo;
import com.trilogy.app.crm.support.CalendarSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.MsisdnSupport;
import com.trilogy.framework.auth.AuthMgr;
import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.auth.bean.UserHome;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GTE;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.elang.LTE;
import com.trilogy.framework.xhome.elang.OrderBy;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.OrderByHome;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * Validates Msisdn Swap Limit.
 * 
 * @author piyush.shirke@redknee.com
 * 
 */

public class MsisdnSwapLimitValidaor  implements Validator {
	
	private static MsisdnSwapLimitValidaor instance;
	private static final String permission = "ByPass.MsisdnSwapLimit.Restriction";
	
	private static int IN_CLAUSE_LIMIT  = 1000;
	
	private static String APPLICATION_CONTEXT = "AppCrm";
	
	private MsisdnSwapLimitValidaor()
	{
	}

    public static Validator instance()
	{
	        if (instance == null)
	        {
	            instance = new MsisdnSwapLimitValidaor();
	        }

	        return instance;
	}
	
    /* Returns Msisdn Swap Limit Extension Configured at SPID Level.
	 * 
	 */
	public static MsisdnSwapLimitSpidExtension getMsisdnSwapLimitSpidExtension(final Context ctx, final int spid)
	{
		MsisdnSwapLimitSpidExtension ext = null;

		try
		{
			final EQ filter = new EQ(MsisdnSwapLimitSpidExtensionXInfo.SPID, spid);
			ext = HomeSupportHelper.get(ctx).findBean(ctx, MsisdnSwapLimitSpidExtension.class, filter);
		}
		catch (HomeException exception)
		{
			new MinorLogMsg(MsisdnSwapLimitValidaor.class,"No MsisdnSwapLimit configuration found for SPID"+spid+","+exception.toString(),null).log(ctx);
		}

		return ext;

	}


	public  void  validate(final Context ctx, Object obj) throws IllegalStateException
	{

		
		Subscriber sub = (Subscriber)obj;

		final Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER); 	
		boolean msisdnChanged = oldSub != null	&& !SafetyUtil.safeEquals(oldSub.getMSISDN(), sub.getMSISDN());

		String msisdn = sub.getMsisdn();
		Msisdn msisdnBean = null;

		if(!msisdnChanged){ 

			return;

		}else{

			try {
				msisdnBean = MsisdnSupport.getMsisdn(ctx, msisdn);
			} catch (HomeException e) {

			}

			if(msisdnBean!=null && PortingTypeEnum.IN == msisdnBean.getPortingType()){
				return;
			}
		}
		
		if(LogSupport.isDebugEnabled(ctx))
		{
			LogSupport.debug(ctx, this,"MsisdnSwapLimitValidaor::validate():Validating Subscriber for Msisdn Swap limit.");
		}

		MsisdnSwapLimitSpidExtension ext = getMsisdnSwapLimitSpidExtension(ctx, sub.getSpid());
		
		AuthMgr authMgr = new AuthMgr(ctx);

		if (ext == null || authMgr.check(permission))
		{
			return;
		} 
		boolean isStartDtSameOrBeforeSubCreatDt = true; 
		try {

			Date subCreatedDate = sub.getDateCreated(); //Susbcriber Creation Date

			Home msisdnMgmtHistoryHome = (Home) ctx.get(MsisdnMgmtHistoryHome.class);


			//Get the list of All Users
			Context ctx1 = (Context)ctx.get(APPLICATION_CONTEXT); //Get Application Context to fetch all available users.
			Home userHome = (Home)ctx1.get(UserHome.class);
			HashSet<String> userSet = new HashSet<String>();
			Collection<User> userCol = userHome.selectAll(); 

			int maxNumberOfSwaps = ext.getMsisdnSwapLimit();
			int period =  ext.getSwapPeriod(); //Number of days in which Msisdn Swap count has to be checked.

			Calendar cal = Calendar.getInstance();
			Date endDate = cal.getTime();//End date is always current date. It is the end date of period for Msisdn Swap Limit Validation
			CalendarSupport calSupp = CalendarSupportHelper.get(ctx);
			calSupp.clearTimeOfDay(cal);

			cal.add(Calendar.DAY_OF_MONTH, -period+1); //Add 1 to include the correct start date.
			Date startDate = cal.getTime(); //Star Date is the starting date of a period for Msisdn Swap Limit Validation.

			isStartDtSameOrBeforeSubCreatDt = startDate.before(subCreatedDate); //Check if Start Date is same/before the subscription Creation Date.

			if(LogSupport.isDebugEnabled(ctx))
			{
				LogSupport.debug(ctx, this,"MsisdnSwapLimitValidaor::validate(): validating from Start Date :"+startDate+",to End Date:"+endDate);
			}

			//Constructing And Predicate
			final And condition1 = new And();
			condition1.add(new EQ(MsisdnMgmtHistoryXInfo.SUBSCRIBER_ID, sub.getId()));
			condition1.add(new EQ(MsisdnMgmtHistoryXInfo.EVENT, HistoryEventSupport.SUBID_MOD));

			condition1.add(new GTE(MsisdnMgmtHistoryXInfo.TIMESTAMP,startDate));

			condition1.add(new LTE(MsisdnMgmtHistoryXInfo.TIMESTAMP,endDate));

			Collection<com.redknee.app.crm.numbermgn.MsisdnMgmtHistory> col = null;
			
				int counter = 0;
				List<HashSet<String>> userList = new ArrayList<HashSet<String>>();
				HashSet<String> tempUserSet = new HashSet<String>();
				for (User user : userCol) {
					counter++;
					boolean skip = authMgr.check(user ,AuthMgr.objectToPermission(permission));

					if(!skip){
						userSet.add(user.getId());
						tempUserSet.add(user.getId());
					}

					if(userSet.size()== IN_CLAUSE_LIMIT){
						userList.add(new HashSet<String>(userSet));
						userSet.clear();
				
					}else if(counter == userCol.size() - 1){
						userList.add(new HashSet<String>(userSet));
					}

				}
				if(!isStartDtSameOrBeforeSubCreatDt){ //If start date is after subscriber creation date. Fetch records only for restricted Users which requires Msisdn Swap Limit validation.
					for (Iterator<HashSet<String>> iterator = userList.iterator(); iterator.hasNext();) 
					{
						Home mhome = null;
			
						Set<String>  users = (HashSet<String>) iterator.next();

						And condition = new And();
						condition.add(condition1);
						condition.add(new In(MsisdnMgmtHistoryXInfo.USER_ID,users));

						mhome = (Home) msisdnMgmtHistoryHome.where(ctx, condition); //Where Clause

						if(col == null)
							col = mhome.selectAll(ctx);
						else
							col.addAll(mhome.selectAll(ctx));

					}

				}else { //If Start Date is <= Subscriber Creation Date , skip the first entry from MsidnMgmt history records. The first entry is for subscription creation and not considered as swap.

					msisdnMgmtHistoryHome = (Home) msisdnMgmtHistoryHome.where(ctx, condition1); //Where Clause

					//Add OrderBy Clause if required.
					final OrderBy order = new OrderBy(MsisdnMgmtHistoryXInfo.TIMESTAMP, true);
					Home orderedHome = new OrderByHome(ctx, order, msisdnMgmtHistoryHome);
					msisdnMgmtHistoryHome = orderedHome ; 
					col = msisdnMgmtHistoryHome.selectAll(ctx);
				}
			
			int currSwap = 0;
			
			if(col !=null){
			
				if(col.size() > 1){
		
					if(isStartDtSameOrBeforeSubCreatDt){//Since it is of same date complete set of records are fetched from msisdnmgmt history

						int count = 0;
						for (MsisdnMgmtHistory  msisdnMgmtHistory: col) {
							count++;
							if(count == 1) //Skip first row
								continue;
							
							String userId = msisdnMgmtHistory.getUserId();

							if(tempUserSet.contains(userId)){
								currSwap = currSwap+1;
							}
						}
						
					}else
						currSwap = col.size();
		
				}else if(col.size() == 1){
			
					if(!isStartDtSameOrBeforeSubCreatDt){
						currSwap = 1;
					}
				}
			}

			if(LogSupport.isDebugEnabled(ctx))
			{
				LogSupport.debug(ctx, this,"MsisdnSwapLimitValidaor::validate():Current Number of Msisdn Swap is:"+currSwap);
			}

			if(currSwap >= maxNumberOfSwaps){

				new MinorLogMsg(this,"MsisdnSwapLimitValidaor::validate():Subscriber has reached Maxmimum Swap limit ",null).log(ctx);
							
				final CompoundIllegalStateException el = new CompoundIllegalStateException();
				el.thrown(new IllegalPropertyArgumentException(
						"",	"Subscriber has reached Maxmimum Msisdn Swap limit of "+maxNumberOfSwaps+". Subscriber can not change Msisdn."));
				el.throwAll();
			}
		}		
		catch (HomeException e) {
		
			 new MinorLogMsg(this,e.toString(),null).log(ctx);
		}
	}
	
}
