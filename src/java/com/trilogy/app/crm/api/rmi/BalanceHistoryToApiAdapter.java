/*
 * Copyright (c) 2012, Redknee Inc. and its subsidiaries. All Rights Reserved.
 *
 * This code is a protected work and subject to domestic and international copyright law(s). 
 * A complete listing of authors of this work is readily available. Additionally, source
 * code is, by its very nature, confidential information and inextricably contains trade
 * secrets and other information proprietary, valuable and sensitive to Redknee. No unauthorized
 * use, disclosure, manipulation or otherwise is permitted, and may only be used in accordance
 * with the terms of the license agreement entered into with Redknee Inc. and/or its subsidiaries.
 */
package com.trilogy.app.crm.api.rmi;

import java.util.Calendar;
import java.util.Collection;

import com.trilogy.app.crm.api.rmi.support.APIGenericParameterSupport;
import com.trilogy.app.crm.bean.BalanceHistory;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.bean.UsageType;
import com.trilogy.app.crm.bean.UsageTypeXInfo;
import com.trilogy.app.crm.bean.calldetail.BillingCategory;
import com.trilogy.app.crm.bean.calldetail.BillingCategoryXInfo;
import com.trilogy.app.crm.bean.calldetail.CallDetail;
import com.trilogy.app.crm.bean.calldetail.RateUnitEnum;
import com.trilogy.app.crm.bundle.BundleCategory;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.util.crmapi.wsdl.v2_1.types.calldetail.VariableRateUnitTypeEnum;
import com.trilogy.util.crmapi.wsdl.v3_0.types.calldetail.CallDetailReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.calldetail.ChargedBundleInfo;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.BundleTypeEnum;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.MergedBalanceHistory;
import com.trilogy.util.crmapi.wsdl.v3_0.types.transaction.TransactionReference;
/**
 * 
 * @author atul.mundra@redknee.com
 * @since 9.8
 */
public class BalanceHistoryToApiAdapter
implements Adapter{

	
	public Object adapt(Context ctx, Object obj) throws HomeException {
		return adaptBalanceHistoryToAPI(ctx,
			    (com.redknee.app.crm.bean.BalanceHistory) obj);
	}
	
	public static MergedBalanceHistory adaptBalanceHistoryToAPI(Context ctx, BalanceHistory balanceHistory){
		final MergedBalanceHistory mergedHistory = new MergedBalanceHistory();
		adaptBalanceHistoryToAPI(ctx,balanceHistory,mergedHistory);
		return mergedHistory;
	}
	
	public static void adaptBalanceHistoryToAPI(Context ctx,
            final com.redknee.app.crm.bean.BalanceHistory balanceHistory, final MergedBalanceHistory mergedHistory){

		mergedHistory.setBalance(balanceHistory.getBalance());
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(balanceHistory.getKeyDate());
		
		mergedHistory.setTransactionDate(cal);
		mergedHistory.setUsage(balanceHistory.getDuration().getTime());
		mergedHistory.setMsisdn(balanceHistory.getMsisdn());
		mergedHistory.setOrigMSISDN(balanceHistory.getOrigMSISDN());
		mergedHistory.setDebit(balanceHistory.getCharge());
		mergedHistory.setCallAdjustmentType(balanceHistory.getCallAdjustmentTypeCode());
		mergedHistory.setDestMSISDN(balanceHistory.getDestMSISDN());
		mergedHistory.setUsageType(String.valueOf(balanceHistory.getUsageType()));
		mergedHistory.setOrigPartyLocation(balanceHistory.getOrigPartyLocation());
		mergedHistory.setDestPartyLocation(balanceHistory.getDestPartyLocation());
		mergedHistory.setDataUsage(balanceHistory.getDataUsage());
		mergedHistory.setBundleUsage(balanceHistory.getBundleUsage());
		
		Calendar receivedDate = Calendar.getInstance();
		receivedDate.setTime(balanceHistory.getReceivedDate());
		mergedHistory.setReceivedDate(receivedDate);
		mergedHistory.setBalance(balanceHistory.getBalance());
		mergedHistory.setBillingCatagory(balanceHistory.getBillingCatagory());
		mergedHistory.setIdentifier(Long.parseLong(balanceHistory.getId()));
		mergedHistory.setRecordType(balanceHistory.getRecordType());
		mergedHistory.setCallAdjustmentTypeDescription(balanceHistory.getCallAdjustmentType());
		if (balanceHistory.getVariableRateUnit()!=null && balanceHistory.getVariableRateUnit().getIndex() != RateUnitEnum.EVENT_INDEX)
        {
			mergedHistory.setVariableRateUnits(VariableRateUnitTypeEnum.valueOf(balanceHistory.getVariableRateUnit().getIndex()));
        }
		if(balanceHistory.getRecordType()==0)
		{
			try
			{
				 ChargedBundleInfo[] apiChargedBundles = new ChargedBundleInfo[balanceHistory.getChargedBundles().size()];
	             Collection<com.redknee.app.crm.bean.ChargedBundleInfo> chargedBundles	=	balanceHistory.getChargedBundles();
	             int bundleCounter = 0;
	             for (com.redknee.app.crm.bean.ChargedBundleInfo chargedBundle : chargedBundles)
	             {
	                 apiChargedBundles[bundleCounter++] =ChargedBundleInfoToApiAdapter.adaptChargedBundleInfoToApi(ctx, chargedBundle);
	            	 
	             }
	             mergedHistory.setBundleInfo(apiChargedBundles);
			}
			catch(Exception e)
			{
	       		 LogSupport.minor(ctx, BalanceHistoryToApiAdapter.class, "COuldn't get ChargedBundleInfo",e);
	       	}
		}
		fillInCallDetailReferenceGenericParameters(ctx,balanceHistory,mergedHistory);
		
	}
	
	
	protected static void fillInCallDetailReferenceGenericParameters(
		    Context ctx,
	        final BalanceHistory balanceHistory, final MergedBalanceHistory mergedHistory)
	{
		if(balanceHistory.getRecordType()==0) 
		{
			try
			{
				UsageType usageType =
				    HomeSupportHelper.get(ctx).findBean(
				        ctx,
				        UsageType.class,
				        new EQ(UsageTypeXInfo.ID, Long.valueOf(balanceHistory
				            .getUsageType())));
				if (usageType == null)
				{
					LogSupport.minor(ctx, CallDetailToApiAdapter.class,
					    "Cannot find Usage Type " + balanceHistory.getUsageType());
				}
				else
				{
					mergedHistory.addParameters(APIGenericParameterSupport
					    .getCallDetailRNUsageType(ctx, usageType));
				}
			}
			catch (HomeException e)
			{
				LogSupport.minor(ctx, CallDetailToApiAdapter.class,
				    "Lookup of usageType falied.", e);
			}
			try
			{
				BillingCategory billingCategory =
				    HomeSupportHelper.get(ctx).findBean(
				        ctx,
				        BillingCategory.class,
				        new EQ(BillingCategoryXInfo.ID, Short.valueOf(balanceHistory
				        		.getBillingCatagory())));
				if (billingCategory == null)
				{
					LogSupport.minor(
					    ctx,
					    CallDetailToApiAdapter.class,
					    "Cannot find Billing Category "
					        + balanceHistory.getBillingCatagory());
				}
				else
				{
					mergedHistory.addParameters(APIGenericParameterSupport
					    .getCallDetailRNBillingCategoryID(ctx, billingCategory));
				}
			}
			catch (HomeException e)
			{
				LogSupport.minor(ctx, CallDetailToApiAdapter.class,
				    "Lookup of Billing Category falied.", e);
			}
		}
	}
	@Override
	public Object unAdapt(Context ctx, Object obj) throws HomeException {
		throw new UnsupportedOperationException();
	}
	
}
