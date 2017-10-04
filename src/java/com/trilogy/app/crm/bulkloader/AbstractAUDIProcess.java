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
 * Copyright &copy; Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.bulkloader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.InfoLogMsg;

import com.trilogy.app.crm.bean.BillCycle;
import com.trilogy.app.crm.bean.CampaignConfig;
import com.trilogy.app.crm.bean.CampaignConfigHome;
import com.trilogy.app.crm.bean.CampaignConfigXInfo;
import com.trilogy.app.crm.bean.CreditCategory;
import com.trilogy.app.crm.bean.CreditCategoryHome;
import com.trilogy.app.crm.bean.CreditCategoryXInfo;
import com.trilogy.app.crm.bean.DealerCode;
import com.trilogy.app.crm.bean.DealerCodeHome;
import com.trilogy.app.crm.bean.DealerCodeXInfo;
import com.trilogy.app.crm.bean.DiscountClass;
import com.trilogy.app.crm.bean.DiscountClassHome;
import com.trilogy.app.crm.bean.DiscountClassXInfo;
import com.trilogy.app.crm.bean.GeneralConfig;
import com.trilogy.app.crm.contract.SubscriptionContract;
import com.trilogy.app.crm.contract.SubscriptionContractHome;
import com.trilogy.app.crm.contract.SubscriptionContractXInfo;
import com.trilogy.app.crm.support.BillCycleSupport;

/**
 * @author amedina
 *
 * Shares common functionality between the Delta changes proceses
 */
public abstract class AbstractAUDIProcess 
{
	/**
	 * @param msisdn
	 * @param reason
	 */
	protected synchronized void printMessage(Context ctx, String msisdn, String reason) 
	{
		String msg="";
		
		//An error occured trying to add this subscriber
		try
		{
			msg = msisdn +"-1" + "-Failure updating Subscriber-" + reason + "\n";
			getSubscriberWriter().print(msg);
		}
		catch (Throwable tt)
		{
			//Can't write to log file
			new InfoLogMsg(this, "Unable to write to Subscriber log file: " + msg,
					tt).log(ctx);
		}
	}
	
	/**
	 * @param msg
	 */
	protected synchronized void printMessage(Context ctx, String msg) 
	{
		try
		{
			getSubscriberWriter().print(msg);
		}
		catch (Throwable tt)
		{
			//Can't write to log file
			new InfoLogMsg(this, "Unable to write to Subscriber log file: " + msg,
					tt).log(ctx);
		}
	}

	/**
	 * @return Returns the subscriberWriter.
	 */
	public PrintWriter getSubscriberWriter()
	{
		return subscriberWriter;
	}

	/**
	 * @param subscriberWriter The subscriberWriter to set.
	 */
	public void setSubscriberWriter(PrintWriter subscriberWriter)
	{
		this.subscriberWriter = subscriberWriter;
	}

	/**
	 * @param ctx
	 * @return
	 * @throws IOException
	 */
	protected PrintWriter getSubscriberWriter(Context ctx) throws IOException 
	{
		PrintWriter writer = null;
		GeneralConfig config = (GeneralConfig) ctx.get(GeneralConfig.class);
		FileWriter subFileWr = null;
		PrintWriter subPrintWr = null;

		
		String path = config.getAudiLogDir();
		
		File dir = new File(path);
		
		if (!dir.exists())
		{
			dir.mkdir();
		}
		
		File subFile = new File(path + File.separator + "subUpload.log");

		if (!subFile.exists())
		{
			subFile.createNewFile();
		}
		
		subFileWr = new FileWriter(subFile, true);
		subPrintWr = new PrintWriter(new BufferedWriter(subFileWr));

		return subPrintWr;
	}

	/**
	 * @param ctx
	 * @param discountClass
	 * @return
	 * @throws HomeException
	 */
	protected DiscountClass getDiscountClass(Context ctx, int spid,
	    int discountClass) throws HomeException
	{
		Home dcHome = (Home) ctx.get(DiscountClassHome.class);
		DiscountClass dcClass = null;
		if (dcHome != null)
		{
			And and = new And();
			and.add(new EQ(DiscountClassXInfo.ID, Integer
			    .valueOf(discountClass)));
			and.add(new EQ(DiscountClassXInfo.SPID, Integer.valueOf(spid)));
			dcClass = (DiscountClass) dcHome.find(ctx, and);
		}

		return dcClass;
	}

	protected DealerCode
	    getDealerCode(Context ctx, int spid, String dealerCode)
	        throws HomeException
	{
		Home home = (Home) ctx.get(DealerCodeHome.class);
		DealerCode dc = null;
		if (home != null && dealerCode != null)
		{
			And and = new And();
			and.add(new EQ(DealerCodeXInfo.CODE, dealerCode.trim()));
			and.add(new EQ(DealerCodeXInfo.SPID, Integer.valueOf(spid)));
			dc = (DealerCode) home.find(ctx, and);
		}
		return dc;
	}

	protected CreditCategory getCreditCategory(Context ctx, int spid, int ccId)
	    throws HomeException
	{
		Home home = (Home) ctx.get(CreditCategoryHome.class);
		CreditCategory cc = null;
		if (home != null)
		{
			And and = new And();
			and.add(new EQ(CreditCategoryXInfo.CODE, Long.valueOf(ccId)));
			and.add(new EQ(CreditCategoryXInfo.SPID, Integer.valueOf(spid)));
			cc = (CreditCategory) home.find(ctx, and);
		}
		return cc;
	}

	protected CampaignConfig getMarketingCampaign(Context ctx, int spid,
	    long configId) throws HomeException
	{
		Home home = (Home) ctx.get(CampaignConfigHome.class);
		CampaignConfig config = null;
		if (home != null)
		{
			And and = new And();
			and.add(new EQ(CampaignConfigXInfo.CAMPAIGN_ID, Long
			    .valueOf(configId)));
			and.add(new EQ(CampaignConfigXInfo.SPID, Integer.valueOf(spid)));
			config = (CampaignConfig) home.find(ctx, and);
		}
		return config;
	}

	protected BillCycle getBillCycle(Context ctx, int spid, int billCycleId)
	    throws HomeException
	{
		BillCycle bc = BillCycleSupport.getBillCycle(ctx, billCycleId);
		if (bc != null && bc.getSpid() != spid)
		{
			bc = null;
		}
		return bc;
	}

	protected SubscriptionContract getSubscriptionContract(Context ctx,
	    long contractId) throws HomeException
	{
		Home home = (Home) ctx.get(SubscriptionContractHome.class);
		SubscriptionContract contract = null;
		if (home != null)
		{
			contract =
			    (SubscriptionContract) home.find(ctx, new EQ(
			        SubscriptionContractXInfo.CONTRACT_ID, contractId));
		}
		return contract;
	}

	protected PrintWriter subscriberWriter;

}
