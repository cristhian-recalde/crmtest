/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.poller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bundle.BundleProfile;
import com.trilogy.app.crm.bundle.SubscriberBundleSupport;
import com.trilogy.app.crm.poller.BucketProvEventAgent.ERConstants.Feilds;
import com.trilogy.app.crm.poller.BucketProvEventAgent.ERConstants.Values;
import com.trilogy.app.crm.poller.event.CRMProcessor;
import com.trilogy.app.crm.poller.event.CRMProcessorSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;


/**
 * @author simar.singh@redknee.com
 * @category This ER agent processes manages association of Bundles to Subscriber
 *           triggered [Subscriber Bucket Provisioning Event Record - ER 1314]
 *
 */
public class BucketProvEventAgent extends BucketEventFunctions implements ContextAgent
{

    /**
     * @param processor
     */
    public BucketProvEventAgent(CRMProcessor processor)
    {
        super();
        processor_ = processor;
    }


    /*
     * (non-Javadoc)
     *
     * @see com.redknee.framework.xhome.context.ContextAgent#execute(com.redknee.
     * framework.xhome.context.Context)
     */
    /**
     * @param ctx
     * @throws AgentException
     */
    public void execute(Context ctx) throws AgentException
    {
        List<String> params = new ArrayList<String>();
        ProcessorInfo info = (ProcessorInfo) ctx.get(ProcessorInfo.class);
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "execute()");
        try
        {
            try
            {
                CRMProcessorSupport.makeArray(ctx, params, info.getRecord(), info.getStartIndex(), ',', info.getErid(), this);
            }
            catch (FilterOutException e)
            {
                new InfoLogMsg(this, "ER 1314 [" + String.valueOf(info.getRecord()) + "] got filtered out due to reason ["
                        + e.getMessage() + "]", null).log(ctx);
                new DebugLogMsg(this, "ER 1314 got filtered out", null).log(ctx);
                return;
            }
            int resultCode = CRMProcessorSupport.getInt(ctx, CRMProcessorSupport.getField(params, Feilds.RESULT_CODE),
                    Values.ResultCodes.UNKNOWN_ERROR);
            if (Values.ResultCodes.UNKNOWN_ERROR == resultCode || Values.ResultCodes.SQL_ERROR == resultCode)
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "Result code contained an error indicator, ER was not processed.", null)
                            .log(ctx);
                }
                return;
            }
            //final int spid = CRMProcessor.getInt(ctx, CRMProcessor.getField(params, Feilds.SPID), Values.UNKNOWN_SPID);
            final String msisdn = CRMProcessorSupport.getField(params, Feilds.MSISDN);
            final int operationType = CRMProcessorSupport.getInt(ctx, CRMProcessorSupport.getField(params, Feilds.OPERATION_TYPE),
                    Values.OperationType.UNKNOWN);
            final long bundleId = CRMProcessorSupport.getLong(ctx, CRMProcessorSupport.getField(params, Feilds.BUNDLE_ID),
                    Values.UNKNOWN_BUNDLE_ID);
            final Date endDate = ERConstants.getBucketProvDate(ctx,CRMProcessorSupport.getField(params, Feilds.BUCKET_END_DATE));
            
            final Subscriber subscriber = SubscriberSupport.lookupSubscriberForMSISDN(ctx, msisdn, new Date(info
                    .getDate()));
            if (null == subscriber)
            {
                throw new Exception("Subscriber not found for MSISDN [" + msisdn + "]");
            }
            
            final BundleProfile bundle = (Values.UNKNOWN_BUNDLE_ID == bundleId) ? (null) : (getBundle(ctx, bundleId, subscriber.getAccount(ctx).getSpid()));

            if (null == bundle || (!bundle.isAuxiliary() && !bundle.isFlex())) /**TT# 13092651014 **/
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "Ignore provisioning event for Bundle-ID [" + bundleId
                            + "] as it could not found in CRM. It could be for a promtional bundle-bucket", null)
                            .log(ctx);
                }
            }
            else
            {
                BundleFee fee = getBundleFee(ctx, bundle, new Date(), endDate);
                
                switch (operationType)
                {
                case Values.OperationType.CREATE_BUCKET: {
                    addOrUpdateSubscriberBundle(ctx, subscriber, fee);
                }
                    break;
                case Values.OperationType.DELETE_BUCKET: {
                	
                	 boolean isPPBundleMandatory = Boolean.FALSE;
                     /** For PP Bundle, applicable only for DELETE bucket case **/
                     if (!bundle.isAuxiliary()){
                     	Map<Long, BundleFee> map = SubscriberBundleSupport.getPricePlanBundles(ctx, subscriber);
                     	fee = map.get(bundle.getBundleId());
                     	
                     	if (fee == null){
                     		if (LogSupport.isDebugEnabled(ctx)){
                    			LogSupport.debug(ctx, this, "Bundle FEE is NULL for Subscriber-ID["
                                        + subscriber.getId() + "], bundle-ID[" + bundleId + "] for Operation-Type ["
                                        + operationType + "]");
                    		}
                     		break;
                     	}
                     	isPPBundleMandatory = fee.isMandatory();
                     } 
                     
                     /**
                 	 * Ignore PP mandatory bundle, default and optional should be processed
                 	 */
                	if (!isPPBundleMandatory){
                		removeSubscriberBundle(ctx, subscriber, fee);
                	} else {
                		if (LogSupport.isDebugEnabled(ctx)){
                			LogSupport.debug(ctx, this, "Ignoring PP Bundle-Bucket Provisioning Event for Subscriber-ID["
                                    + subscriber.getId() + "], bundle-ID[" + bundleId + "] for Operation-Type ["
                                    + operationType + "] because Bundle is mandatory.");
                		}
                	}
                }
                    break;
                default: {
                }
                    new DebugLogMsg(this, "Ignoring Bundle-Bucket Porvisioniong Event for Subscriber-ID["
                            + subscriber.getId() + "], bundle-ID[" + bundleId + "] because Operation-Type ["
                            + operationType + "] is unknown.", null).log(ctx);
                    break;
                }
            }
        }
        catch (Throwable t)
        {
            new MinorLogMsg(this, "Failed to process ER 1380 because of Exception " + t.getMessage(), t).log(ctx);
            processor_.saveErrorRecord(ctx, info.getRecord());
        }
        finally
        {
            pmLogMsg.log(ctx);
        }
    }

    final private CRMProcessor processor_;
    private static final String PM_MODULE = BucketProvEventProcessor.class.getName();

    public static class ERConstants
    {

        // /////////////////////////
        // BMEvent 1314
        // index 0 is the ER Date
        public static final class Feilds
        {

            /** ER field index */
            public static final int DATE = 0;
            /** ER field index */
            public static final int SPID = 2;
            /** ER field index */
            public static final int OPERATION_TYPE = 4;
            /** ER field index */
            public static final int MSISDN = 5;
            /** ER field index */
            public static final int BUNDLE_ID = 6;
            /** ER field index */
            public static final int BUCKET_ID = 7;
            /** ER field index */
            public static final int CATEGORY_ID = 8;
            /** ER field index */
            public static final int RESULT_CODE = 18;
            /** ER field index */
            public static final int BUCKET_END_DATE = 20;
        }
        public static final class Values
        {

            public static final class ResultCodes
            {

                /** ER result code error code */
                public static final int SQL_ERROR = 202;
                /** ER result code error code */
                public static final int UNKNOWN_ERROR = 104;
            }
            public static final class OperationType
            {

                public static final int UNKNOWN = Integer.MIN_VALUE;
                public static final int CREATE_BUCKET = 1;
                public static final int DELETE_BUCKET = 3;
                public static final int UPDATE_BUCKET_STATUS = 7;
            }

            private static final int UNKNOWN_BUNDLE_ID = Integer.MIN_VALUE;
            private static final int UNKNOWN_SPID = Integer.MIN_VALUE;
        }

        private static final SimpleDateFormat BUCKET_PROV_DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd");


        public static Date getBucketProvDate(Context ctx,String dateString) throws ParseException
        {
            if ("".equals(dateString) || dateString == null )
            {
            	return CalendarSupportHelper.get(ctx).findDateYearsAfter(20, new Date());
            }
            return BUCKET_PROV_DATE_FORMAT.parse(dateString);
        }
    }
}
