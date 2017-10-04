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
package com.trilogy.app.crm.poller.agent;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberCycleBundleUsageHome;
import com.trilogy.app.crm.bean.SubscriberXDBHome;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bean.core.custom.SubscriberCycleBundleUsage;
import com.trilogy.app.crm.bundle.BundleAuxiliaryServiceHome;
import com.trilogy.app.crm.bundle.BundleAuxiliaryServiceXInfo;
import com.trilogy.app.crm.bundle.SubscriberBundleSupport;
import com.trilogy.app.crm.bundle.UnitTypeEnum;
import com.trilogy.app.crm.bundle.service.CRMBundleProfile;
import com.trilogy.app.crm.home.sub.SubscriberNoteSupport;
import com.trilogy.app.crm.poller.Constants;
import com.trilogy.app.crm.poller.FilterOutException;
import com.trilogy.app.crm.poller.ProcessorInfo;
import com.trilogy.app.crm.poller.event.BMEventProcessor;
import com.trilogy.app.crm.poller.event.CRMProcessor;
import com.trilogy.app.crm.poller.event.CRMProcessorSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.bean.SystemNoteSubTypeEnum;
import com.trilogy.app.crm.bean.SystemNoteTypeEnum;

/**
 * @author lko
 */
public class BMEventAgent implements ContextAgent, Constants {

	/**
	 *
	 */
	public BMEventAgent(CRMProcessor processor)
	{
		super();
        processor_ = processor;
	}

    /* (non-Javadoc)
     * @see com.redknee.framework.xhome.context.ContextAgent#execute(com.redknee.framework.xhome.context.Context)
     */
    public void execute(Context ctx) throws AgentException
    {

        List params = new ArrayList();
        ProcessorInfo info = (ProcessorInfo) ctx.get(ProcessorInfo.class);

        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "execute()");

		try
		{

			try
			{
				CRMProcessorSupport.makeArray(ctx, params, info.getRecord(), info.getStartIndex(), ',', info.getErid(), this);
			}
			catch ( FilterOutException e)
			{
				return;
			}

			int resultCode = CRMProcessorSupport.getInt(ctx, CRMProcessorSupport.getField(params, BM_RESULT_CODE), BM_UNKNOWN_ERROR);
			if ( BM_UNKNOWN_ERROR == resultCode || BM_SQL_ERROR == resultCode )
			{
				if (LogSupport.isDebugEnabled(ctx))
				{
					new DebugLogMsg(this, "Result code contained an error indicator, ER was not processed.", null).log(ctx);
				}
				return;
			}

			String msisdn = CRMProcessorSupport.getField(params, BM_MSISDN);
			int bundleId = CRMProcessorSupport.getInt(ctx, CRMProcessorSupport.getField(params, BM_BUNDLE_ID), -1);
			long bucketId = CRMProcessorSupport.getLong(ctx, CRMProcessorSupport.getField(params, BM_BUCKET_ID), -1);
			String categoryId = CRMProcessorSupport.getField(params, BM_CATEGORY_ID);
			long pastFCTAwarded = CRMProcessorSupport.getLong(ctx, CRMProcessorSupport.getField(params, BM_PAST_FCT_AWARDED), -1);
			long pastFCTUsed = CRMProcessorSupport.getLong(ctx, CRMProcessorSupport.getField(params, BM_PAST_FCT_USED), -1);
			long pastRolloverFCTAwarded = CRMProcessorSupport.getLong(ctx, CRMProcessorSupport.getField(params, BM_PAST_ROLLOVER_FCT_AWARDED), -1);
			long pastRolloverFCTUsed = CRMProcessorSupport.getLong(ctx, CRMProcessorSupport.getField(params, BM_PAST_ROLLOVER_FCT_USED), -1);
			long pastGroupFCTUsed = CRMProcessorSupport.getLong(ctx, CRMProcessorSupport.getField(params, BM_PAST_GROUP_FCT_USED), -1);
			long rolloverFCT = CRMProcessorSupport.getLong(ctx, CRMProcessorSupport.getField(params, BM_ROLLOVER_FCT), -1);
			long pastGroupRolloverFCTUsed = CRMProcessorSupport.getLong(ctx, CRMProcessorSupport.getField(params, BM_PAST_GROUP_ROLLOVER_FCT_USED), -1);
			long FCTAwarded = CRMProcessorSupport.getLong(ctx, CRMProcessorSupport.getField(params, BM_FCT_AWARDED), -1);
			long pastRolloverExpired = CRMProcessorSupport.getLong(ctx, CRMProcessorSupport.getField(params, BM_PAST_ROLLOVER_EXPIRED), -1);
			int eventReason = CRMProcessorSupport.getInt(ctx, CRMProcessorSupport.getField(params, BM_EVENT_REASON), -1);

			// remove purged bundle
			if ( eventReason == BM_PURGE_EVENT)
			{
				removeExpiredOneTimeBundle(ctx, msisdn, bundleId);
			}

			String dateString = CRMProcessorSupport.getField(params, BM_BUCKET_START_DATE);
			Date bucketStartDate = null;
			if( dateString.equals("") )
			{
				bucketStartDate = new Date(0);
			}
			else
			{
				bucketStartDate = CRMProcessorSupport.getDateOnly( dateString );
			}
			Date bucketEndDate = new Date(info.getDate());

		    if(eventReason!=BM_PURGE_EVENT)
		    {
		    	SubscriberCycleBundleUsage usage = (SubscriberCycleBundleUsage) XBeans.instantiate(SubscriberCycleBundleUsage.class, ctx);

		         // The usage object does the work of looking up and calculating all of
		         // the identifiecation information of the MSISDN.
		         usage.initializeIdentification(ctx, msisdn, new Date(), eventReason);
		         usage.setBundleID(bundleId);
		         usage.setBucketId(bucketId);
		         usage.setCategoryId(categoryId);
		         usage.setPastMonthlyFCT(pastFCTAwarded);
		         usage.setPastFCTUsed(pastFCTUsed);
		         usage.setPastRollOverFCTAwarded(pastRolloverFCTAwarded);
		         usage.setPastRollOverFCTUsed(pastRolloverFCTUsed);
		         usage.setPastUsedGroupFCT(pastGroupFCTUsed);
		         usage.setPastUsedGroupFCTRollOver(pastGroupRolloverFCTUsed);
		         usage.setRollOverFCT(rolloverFCT);
		         usage.setExpiredFCT(pastRolloverExpired);
		         usage.setAwardedFCT(FCTAwarded);
		         usage.setEventReason(eventReason);
		         usage.setBucketStartDate(bucketStartDate);
		         usage.setBucketEndDate(bucketEndDate);

		         getAdditionalInformation(ctx, usage, bundleId, msisdn);

		         Home home = (Home)ctx.get(SubscriberCycleBundleUsageHome.class);
		         LogSupport.debug(ctx, this, "usage is: "+usage);
		         home.create(ctx,usage);
		}

		}
		catch( Throwable t )
		{
			new MinorLogMsg(this, "Failed to process ER 1380 because of Exception " + t.getMessage(), t).log(ctx);
			processor_.saveErrorRecord(ctx, info.getRecord());
		}
		finally
		{
			pmLogMsg.log(ctx);
		}
    }


 /**
  * Gets the Bundle Name and the Unit type for this particular bundle based on the balance application
  * @param ctx
  * @param usage
  * @param msisdn
  * @param categoryId
  *				the Category id to get the unit type and the bundle name
  * @throws HomeException
  * @throws HomeInternalException
  */
 private void getAdditionalInformation(Context ctx, SubscriberCycleBundleUsage usage, int bundleId, String msisdn)
 throws HomeInternalException, HomeException
 {
        BundleProfile bundle = getBundle(ctx, usage.getSpid(), bundleId);
        if (bundle != null)
        {
            UnitTypeEnum unitType = UnitTypeEnum.get((short) bundle.getType());
            
            String name = bundle.getName();

            if(LogSupport.isDebugEnabled(ctx)) 
            {
                LogSupport.debug(ctx, this, "Setting Name and UnitType to " + name + " and " + unitType);
            }
            usage.setUnits(unitType);
            usage.setBundleName(name);
        }
 }

 private BundleProfile getBundle(Context ctx, int spId, int bundleId)
 {
        CRMBundleProfile service = (CRMBundleProfile) ctx.get(CRMBundleProfile.class);
        BundleProfile bundle = null;

        if (service != null)
        {
            try
            {
                bundle = service.getBundleProfile(ctx, spId, bundleId);
            }
            catch (Exception e)
            {
                LogSupport.major(ctx, this, "Error while getBundle : " + e.getMessage(), e);
            }
        }

        return bundle;
}



 public void removeExpiredOneTimeBundle(Context ctx, String msisdn, long bundleId){
	 try {
		 Subscriber sub = SubscriberSupport.lookupActiveSubscriberForMSISDN(ctx, msisdn);
		 Map bundles = SubscriberBundleSupport.getPricePlanBundles(ctx, sub);
		 if (bundles.containsKey(Long.valueOf(bundleId))) {
			 removeExpiredOneTimeBundleInPricePlan(ctx, sub, bundleId);
		 } else {
             removeExpiredOneTimeBundleInPricePlan(ctx, sub, bundleId);
			 removeExpiredOneTimeAuxiliaryBundle(ctx, sub, bundleId);
		 }


	 } catch ( HomeException e){
			LogSupport.major(ctx, this, "remvoeExpiredOneTimeBundle: HomeException : " + e.getMessage(), e);
	 }
 }

 public void removeExpiredOneTimeBundleInPricePlan(Context ctx, Subscriber sub, long bundleId)
 throws HomeException{
     final Home home = (Home) ctx.get(SubscriberXDBHome.class);
   
     	 int currBundleId = (int) bundleId;
     	 BundleProfile bundleProfile = getBundle(ctx,sub.getSpid(),currBundleId);
		 ArrayList bundleIds = new ArrayList();
		 bundleIds.add(Long.valueOf(bundleId));		 
		 sub.bundleUnProvisioned( bundleIds);
		 sub.getBundles().keySet().remove(Long.valueOf(bundleId));
		 home.store(ctx,sub);
		
		 String msg = MessageFormat.format("Subscriber Bundle: {0} : {1} remove successfully.",
		                                  new Object[]{Long.valueOf(bundleId), bundleProfile.getName()}); 
		 StringBuilder noteBuff = new StringBuilder();
         noteBuff.append(msg);
         
         SubscriberNoteSupport.createSubscriberNote(ctx, BMEventAgent.class.getName(), 
                 SubscriberNoteSupport.getCsrAgent(ctx, sub), 
                     sub.getId(), SystemNoteTypeEnum.EVENTS, 
                       SystemNoteSubTypeEnum.SUBUPDATE, noteBuff, false); 
 }

 public void removeExpiredOneTimeAuxiliaryBundle(Context ctx, Subscriber sub, long bundleId) throws HomeException{
	 final Home home = (Home) ctx.get(BundleAuxiliaryServiceHome.class);
     home.where(ctx, new And()
     .add(new EQ(BundleAuxiliaryServiceXInfo.ID, Long.valueOf(bundleId)))
     .add(new EQ(BundleAuxiliaryServiceXInfo.SUBSCRIBER_ID, sub.getId()))).removeAll();
 }

    private CRMProcessor processor_= null;
    private static final String PM_MODULE = BMEventProcessor.class.getName();
}
