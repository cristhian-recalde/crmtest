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
package com.trilogy.app.crm.api.rmi;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.api.rmi.support.APIGenericParameterSupport;
import com.trilogy.app.crm.api.rmi.support.RmiApiSupport;
import com.trilogy.app.crm.bean.TaxAuthority;
import com.trilogy.app.crm.bean.TaxAuthorityXInfo;
import com.trilogy.app.crm.bean.UsageType;
import com.trilogy.app.crm.bean.UsageTypeXInfo;
import com.trilogy.app.crm.bean.calldetail.BillingCategory;
import com.trilogy.app.crm.bean.calldetail.BillingCategoryXInfo;
import com.trilogy.app.crm.bean.calldetail.CallDetail;
import com.trilogy.app.crm.bean.calldetail.RateUnitEnum;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.util.crmapi.wsdl.v2_1.types.calldetail.VariableRateUnitTypeEnum;
import com.trilogy.util.crmapi.wsdl.v3_0.types.calldetail.CallDetailReference;

/**
 * Adapts CallDetail object to API objects.
 *
 * @author victor.stratan@redknee.com
 */
public class CallDetailToApiAdapter implements Adapter
{
	@Override
    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
		return adaptCallDetailToReference(ctx, (CallDetail) obj);
    }

    @Override
    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }

	public static void adaptCallDetailToApi(final Context ctx, 
	        final com.redknee.app.crm.bean.calldetail.CallDetail callDetail, 
	        final com.redknee.util.crmapi.wsdl.v3_0.types.calldetail.CallDetail detail)
    {
		adaptCallDetailToReference(ctx, callDetail, detail);
        detail.setPostedDate(CalendarSupportHelper.get(ctx).dateToCalendar(callDetail.getPostedDate()));
        detail.setFlatRate(Long.valueOf(callDetail.getFlatRate()));
        detail.setVariableRate(Long.valueOf(callDetail.getVariableRate()));

        //TT#11120139004: Workaround for call detail variable rate unit.
        if (callDetail.getVariableRateUnit().getIndex() != RateUnitEnum.EVENT_INDEX)
        {
            detail.setVariableRateUnits(VariableRateUnitTypeEnum.valueOf(callDetail.getVariableRateUnit().getIndex()));
        }
        
        detail.setBucketRateID(Long.valueOf(callDetail.getBucketRateID()));
        detail.setUsedBucketMinutes(Long.valueOf(callDetail.getUsedMinutes()));
        detail.setRatePlan(callDetail.getRatePlan());
		if (callDetail.getRatingRule() != null
		    && !callDetail.getRatingRule().isEmpty())
		{
		    try
		    {
	            detail.setRatingRule(Long.valueOf(callDetail.getRatingRule()));
		    }
		    catch (NumberFormatException e)
		    {
		        new InfoLogMsg(CallDetailToApiAdapter.class, "Call detail " + callDetail.ID()
		                + " contains non-numeric rating rule '" + callDetail.getRatingRule() 
		                + "'.  API currently only supports numeric rate rules.  Returning blank rating rule...", null).log(ctx);
		    }
		}
        detail.setGlCode(callDetail.getGLCode());
        detail.setBillingCategoryID(Long.valueOf(callDetail.getBillingCategory()));
        detail.setPrimaryTaxAuthorityID(Long.valueOf(callDetail.getTaxAuthority1()));
        detail.setSecondaryTaxAuthorityID(Long.valueOf(callDetail.getTaxAuthority2()));
		detail.setSubscriberType(RmiApiSupport
		    .convertCrmSubscriberPaidType2Api(callDetail.getSubscriberType()));
		
		fillInCallDetailGenericParameters(ctx, callDetail, detail);
	}
    
    
	protected static void fillInCallDetailGenericParameters(final Context ctx, 
	        final com.redknee.app.crm.bean.calldetail.CallDetail callDetail,
	        final com.redknee.util.crmapi.wsdl.v3_0.types.calldetail.CallDetail detail)
	{
		try
		{
			BillingCategory billingCategory =
			    HomeSupportHelper.get(ctx).findBean(
			        ctx,
			        BillingCategory.class,
			        new EQ(BillingCategoryXInfo.ID, Short.valueOf(callDetail
			            .getBillingCategory())));
			if (billingCategory == null)
			{
				LogSupport.minor(
				    ctx,
				    CallDetailToApiAdapter.class,
				    "Cannot find Billing Category "
				        + callDetail.getBillingCategory());
			}
			else
			{
				detail.addParameters(APIGenericParameterSupport
				    .getCallDetailRNBillingCategoryID(ctx, billingCategory));
			}
		}
		catch (HomeException e)
		{
			LogSupport.minor(ctx, CallDetailToApiAdapter.class,
			    "Lookup of Billing Category falied.", e);
		}

		try
		{
			TaxAuthority primaryTaxAuthority =
			    HomeSupportHelper.get(ctx).findBean(
			        ctx,
			        TaxAuthority.class,
			        new EQ(TaxAuthorityXInfo.TAX_ID, Integer.valueOf(callDetail
			            .getTaxAuthority1())));
			if (primaryTaxAuthority == null)
			{
				LogSupport.minor(
				    ctx,
				    CallDetailToApiAdapter.class,
				    "Cannot find Primary Tax Authority "
				        + callDetail.getTaxAuthority1());
			}
			else
			{
				detail.addParameters(APIGenericParameterSupport
				    .getCallDetailRNPrimaryTaxAuthorityID(ctx,
				        primaryTaxAuthority));
			}
		}
		catch (HomeException e)
		{
			LogSupport.minor(ctx, CallDetailToApiAdapter.class,
			    "Lookup of primary Tax Authority falied.", e);
		}

		try
		{
			TaxAuthority secondaryTaxAuthority =
			    HomeSupportHelper.get(ctx).findBean(
			        ctx,
			        TaxAuthority.class,
			        new EQ(TaxAuthorityXInfo.TAX_ID, Integer.valueOf(callDetail
			            .getTaxAuthority2())));
			if (secondaryTaxAuthority == null)
			{
				LogSupport.minor(
				    ctx,
				    CallDetailToApiAdapter.class,
				    "Cannot find Secondary Tax Authority "
				        + callDetail.getTaxAuthority2());
			}
			else
			{
				detail.addParameters(APIGenericParameterSupport
				    .getCallDetailRNSecondaryTaxAuthorityID(ctx,
				        secondaryTaxAuthority));
			}
		}
		catch (HomeException e)
		{
			LogSupport.minor(ctx, CallDetailToApiAdapter.class,
			    "Lookup of secondary Tax Authority falied.", e);
		}
		
	    detail.addParameters(APIGenericParameterSupport.getCallDetailRatingRule(ctx, callDetail));
    }

	public static CallDetailReference adaptCallDetailToReference(Context ctx,
	    final CallDetail callDetail)
    {
        final CallDetailReference reference = new CallDetailReference();
		adaptCallDetailToReference(ctx, callDetail, reference);
		
        return reference;
    }

	public static CallDetailReference adaptCallDetailToReference(Context ctx,
	    final CallDetail callDetail, final CallDetailReference reference)
    {
        reference.setIdentifier(callDetail.getId());
        reference.setTransactionDate(CalendarSupportHelper.get().dateToCalendar(callDetail.getTranDate()));
        reference.setCallType(Long.valueOf(callDetail.getCallType().getIndex()));
        reference.setAccountID(callDetail.getBAN());
        reference.setMobileNumber(callDetail.getChargedMSISDN());
        reference.setOriginatingNumber(callDetail.getOrigMSISDN());
        reference.setDestinationNumber(callDetail.getDestMSISDN());
        reference.setOriginatingLocation(callDetail.getCallingPartyLocation());
        reference.setDestinationLocation(callDetail.getDestinationPartyLocation());
        reference.setDuration(callDetail.getDuration().getTime());
        reference.setDataUsage(callDetail.getDataUsage());
        reference.setUsageType(callDetail.getUsageType());
        reference.setCharge(Long.valueOf(callDetail.getCharge()));
        reference.setBalance(Long.valueOf(callDetail.getBalance()));

		fillInCallDetailReferenceGenericParameters(ctx, callDetail, reference);
        return reference;
    }

	protected static void fillInCallDetailReferenceGenericParameters(
	    Context ctx,
        final CallDetail callDetail, final CallDetailReference reference)
    {
	    try
		{
			UsageType usageType =
			    HomeSupportHelper.get(ctx).findBean(
			        ctx,
			        UsageType.class,
			        new EQ(UsageTypeXInfo.ID, Long.valueOf(callDetail
			            .getUsageType())));
			if (usageType == null)
			{
				LogSupport.minor(ctx, CallDetailToApiAdapter.class,
				    "Cannot find Usage Type " + callDetail.getUsageType());
			}
			else
			{
				reference.addParameters(APIGenericParameterSupport
				    .getCallDetailRNUsageType(ctx, usageType));
			}
		}
		catch (HomeException e)
		{
			LogSupport.minor(ctx, CallDetailToApiAdapter.class,
			    "Lookup of usageType falied.", e);
		}
	   
	    /**
	     * Added for PTUB Feature
	     */
	    reference.addParameters(APIGenericParameterSupport.getCallDetailSecondaryBalanceIndicator(ctx, callDetail.getSecondaryBalanceIndicator().getIndex()));
		reference.addParameters(APIGenericParameterSupport.getCallDetailSecondaryBalanceAmount(ctx, callDetail.getSecondaryBalanceChargedAmount()));
		
		reference.addParameters(APIGenericParameterSupport.getCallChargedParty(ctx, callDetail.getChargedParty()));
    }
}
