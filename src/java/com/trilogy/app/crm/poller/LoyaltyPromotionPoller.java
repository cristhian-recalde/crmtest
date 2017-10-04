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
package com.trilogy.app.crm.poller;

import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;

import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.core.BundleCategoryAssociation;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bundle.BundleSegmentEnum;
import com.trilogy.app.crm.bundle.BundleTypeEnum;
import com.trilogy.app.crm.bundle.InvalidBundleApiException;
import com.trilogy.app.crm.bundle.rateplan.RatePlanAssociation;
import com.trilogy.app.crm.bundle.rateplan.RatePlanAssociationHome;
import com.trilogy.app.crm.bundle.rateplan.RatePlanAssociationXInfo;
import com.trilogy.app.crm.bundle.service.CRMBundleCategory;
import com.trilogy.app.crm.poller.event.CRMProcessorSupport;
import com.trilogy.app.crm.poller.event.LoyaltyProcessor;
import com.trilogy.app.crm.support.BundleSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;
/**
 * @author dzajac
 */
public class LoyaltyPromotionPoller implements ContextAgent
{

    public LoyaltyPromotionPoller()
    {
        super();
    }

    public void execute(Context ctx) throws AgentException
    {
        List params = new ArrayList();
        ProcessorInfo info = (ProcessorInfo) ctx.get(ProcessorInfo.class);

        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "execute()");
        try
        {
            try {
                CRMProcessorSupport.makeArray(ctx, params, info.getRecord(), info.getStartIndex(), ',', info.getErid(),this);
            } catch ( FilterOutException e){
                return; 
            }
            
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, LoyaltyProcessor.getDebugParams(params), null).log(ctx);
            }

            if (params.size() < REQUIRED_LENGTH)
            {
                throw new HomeException("The number of ER fields, " + params.size()
                        + ", is less than the minimum required, " + REQUIRED_LENGTH);
            }
            final String msisdnStr = CRMProcessorSupport.getField(params, INDEX_MSISDN);
            String msisdn = "";

            try
            {
                msisdn = CRMProcessorSupport.getMsisdn(msisdnStr);
            }
            catch (ParseException e)
            {
                final String formattedMsg = MessageFormat.format(
                        "Could not parse Msisdn \"{0}\".",
                        new Object[] { msisdnStr });

                throw new HomeException(formattedMsg);
            }

            final String bundleId = CRMProcessorSupport.getField(params, INDEX_BUNDLE_ID);

            Subscriber sub = null;
            BundleFee bundle = new BundleFee();

            try
            {
                // ensure bundle is auxiliary
                Long longBundleId = Long.valueOf(bundleId);
                BundleProfile bundleDefinition = BundleSupportHelper.get(ctx).getBundleProfile(ctx, longBundleId.longValue());

                if (bundleDefinition == null)
                {
                    new MinorLogMsg(this,
                            "Cannot find bundle " + bundleId + " for provisioning to subscriber " + msisdn,
                            null).log(ctx);
                    return;
                }
                else if (!bundleDefinition.isAuxiliary())
                {
                    // ignore non-auxiliary bundles because they should not be
                    // provisioned this way
                    new MinorLogMsg(this,
                            "Skipping non-auxiliary bundle " + bundleId + " for subscriber " + msisdn,
                            null).log(ctx);
                    return;
                }

                Home subHome = (Home) ctx.get(SubscriberHome.class);
                sub = SubscriberSupport.lookupSubscriberForMSISDN(ctx, msisdn, new Date(info.getDate()));
                
                final long marketingCampaignId = Long.parseLong(CRMProcessorSupport.getField(params, INDEX_MARKETING_CAMPAIGN_ID));
                sub.getMarketingCampaignBean().setMarketingId(marketingCampaignId);
                
                final Date endDate = (CRMProcessorSupport.getDate(CRMProcessorSupport.getField(params, INDEX_MARKETING_CAMPAIGN_START_DATE))); 
                sub.getMarketingCampaignBean().setEndDate(endDate);
                bundle.setId(longBundleId.longValue());
                
                // Make sure the bundle is the same  voice/sms/data rate plan as the subscriber's PPV
                // otherwise, we should not provision the bundle to the sub
                //As of CRM 8.2, the Rate Plan information is stored in Price Plan
                PricePlan pp = sub.getRawPricePlanVersion(ctx).getPricePlan(ctx);

                Object ratePlanFilter = null;
                
                if ((!bundleDefinition.isOfType(BundleTypeEnum.CROSS_SERVICE)) && !bundleDefinition.isOfType(BundleTypeEnum.MONETARY))
                {
                    ratePlanFilter = getRatePlanFilter(ctx, bundleDefinition.getType(), pp);
                }
                else if (bundleDefinition.getBundleCategoryIds().size()>0)
                {
                    CRMBundleCategory service = (CRMBundleCategory) ctx.get(CRMBundleCategory.class);
                    Iterator<Map.Entry<?, BundleCategoryAssociation>> iter = bundleDefinition.getBundleCategoryIds().entrySet().iterator();
                    ratePlanFilter = new Or();
                    while (iter.hasNext())
                    {
                        Object filter = null;
                        BundleCategoryAssociation association = iter.next().getValue();
                        filter = getRatePlanFilter(ctx, association.getType(), pp);
                        
                        if (filter!=null)
                        {
                            ((Or) ratePlanFilter).add(filter);
                        }
                    }            
                }                 
                
                Home assocHome = (Home)ctx.get(RatePlanAssociationHome.class);
                RatePlanAssociation assoc = null;
                
                if (ratePlanFilter != null)
                {
                    assoc = (RatePlanAssociation)assocHome.find(ctx,
                        new And()
                        .add(ratePlanFilter)
                           .add(new EQ(RatePlanAssociationXInfo.BUNDLE_ID, Long.valueOf(bundle.getId()))));
                }
                if (assoc != null)
                {
                    if ((bundleDefinition.getSegment() == BundleSegmentEnum.POSTPAID) && sub.isPrepaid())
                    {
                        new InfoLogMsg(this, "Unable to provision postpaid bundle " + bundle.getId() + " to" +
                                "prepaid subscriber " + sub.getId(),null).log(ctx);
                    }
                    else if ((bundleDefinition.getSegment() == BundleSegmentEnum.PREPAID) && sub.isPostpaid())
                    {
                        new InfoLogMsg(this, "Unable to provision prepaid bundle " + bundle.getId() + " to" +
                                "postpaid subscriber " + sub.getId(),null).log(ctx);
                    }
                    else 
                    {
                        if (sub.getBundles().put(longBundleId, bundle) != null)
                        {
                            new InfoLogMsg(this, "Error adding bundle " + bundleId + " for subscriber " + msisdn, null).log(ctx);
                        }
                        else
                        {
                            subHome.store(ctx, sub);
                        }
                    }
                }
                else
                {
                    new InfoLogMsg(this, "Bundle " + bundle.getId() + "is not assosicated with subscriber " +
                            sub.getId() + " rateplan", null).log(ctx);
                }
            }
            catch (HomeException e)
            {
                final String formattedMsg = MessageFormat.format(
                        "Failed to provision bundle \"{0}\" for subscriber for MSISDN \"{1}\".",
                        new Object[] { bundleId, msisdnStr });

                throw new HomeException(formattedMsg, e);
            }
            catch (InvalidBundleApiException e)
            {
                final String formattedMsg = MessageFormat.format(
                        "Failed to provision bundle \"{0}\" for subscriber for MSISDN \"{1}\".",
                        new Object[] { bundleId, msisdnStr });

                throw new HomeException(formattedMsg, e);
            }
        }
        catch (Exception e)
        {
            new MajorLogMsg(ctx, "Error parsing loyalty ER", e).log(ctx);
        }
    }

    private Object getRatePlanFilter(Context ctx, int type, PricePlan pp)
    {
        Object ratePlanFilter = null; 
        if (type == BundleTypeEnum.VOICE_INDEX)
        {
            ratePlanFilter = new EQ(RatePlanAssociationXInfo.VOICE_RATE_PLAN, pp.getVoiceRatePlan());
        }
        else if (type == BundleTypeEnum.SMS_INDEX)
        {
            ratePlanFilter = new EQ(RatePlanAssociationXInfo.SMS_RATE_PLAN, pp.getSMSRatePlan());
        }
        else if (type == BundleTypeEnum.DATA_INDEX)
        {
            ratePlanFilter = new EQ(RatePlanAssociationXInfo.DATA_RATE_PLAN, Integer.valueOf(pp.getDataRatePlan()));
        }
        else
        {
            new MajorLogMsg(this, "Invalid Bundle Type " + type, null).log(ctx);
        }
        
        return ratePlanFilter;
    }

    protected static final String PM_MODULE = LoyaltyPromotionPoller.class.getName();

    protected static final int INDEX_BUNDLE_ID = 4;

    protected static final int INDEX_MSISDN = 3;

    protected static final int INDEX_ACTIVATION_DATE = 5;
    
    protected static final int INDEX_MARKETING_CAMPAIGN_ID = 7;
    
    protected static final int INDEX_MARKETING_CAMPAIGN_START_DATE = 8;

    protected static final int REQUIRED_LENGTH = 9;
}
