/*
 *  BundleUsageWebAgent.java
 *
 *  Author : kgreer
 *  Date   : Oct 21, 2005
 *
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee, no unauthorised use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the licence agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */

package com.trilogy.app.crm.bundle.web;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import java.text.SimpleDateFormat;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bundle.Balance;
import com.trilogy.app.crm.bundle.BundleTypeEnum;
import com.trilogy.app.crm.bundle.SubscriberBucket;
import com.trilogy.app.crm.bundle.SubscriberBucketHome;
import com.trilogy.app.crm.bundle.SubscriberBucketXInfo;
import com.trilogy.app.crm.bundle.UnitTypeEnum;
import com.trilogy.app.crm.bundle.service.CRMBundleProfile;
import com.trilogy.app.crm.bundle.service.CRMSubscriberBucketProfile;
import com.trilogy.app.crm.support.FrameworkSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.web.control.CurrencyContextSetupWebControl;
import com.trilogy.framework.core.web.XCurrencyWebControl;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.True;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.session.Session;
import com.trilogy.framework.xhome.web.agent.WebAgent;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xhome.web.renderer.TableRenderer;
import com.trilogy.framework.xhome.webcontrol.DateLongWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * Bundle Usage WebAgent Report.
 *
 * @author kgreer
 */
public class BundleUsageWebAgent extends WebAgents implements WebAgent
{
    private WebControl currencyWebControl_ = new CurrencyContextSetupWebControl(new XCurrencyWebControl(false));

    public BundleUsageWebAgent()
    {
    }


    protected void rightTD(final Context ctx, final PrintWriter out, final TableRenderer r, final UnitTypeEnum unit, final long value)
    {
        r.TD(ctx,out);
        out.print("<div align=right>");
        if (unit.equals(UnitTypeEnum.CURRENCY))
        {
            currencyWebControl_.toWeb(ctx, out, unit.toString(), value);
        }
        else
        {
            out.print(String.valueOf(unit.adjust(ctx, value)));
        }
        
        out.print("</div>");
        r.TDEnd(ctx,out);
    }

    protected void outputRows(
            Context ctx,
            Collection<SubscriberBucket> buckets,
            String bucketName,
            int i,
            Subscriber subscriber,
            WebControl dateWebControl) //throws HomeException
    {
        boolean first = true;
        for (SubscriberBucket bucket : buckets)
        {
            if (first)
            {
                outputFirstRow(ctx, bucket, bucketName, i, subscriber, dateWebControl);
                first = false;
            }
            else
            {
                outputRow(ctx, bucket, "", i, subscriber, dateWebControl);
            }
        }
    }
    
    protected void outputFirstRow(
            Context ctx,
            SubscriberBucket bucket,
            String bucketName,
            int i,
            Subscriber subscriber,
            WebControl dateWebControl) //throws HomeException
    {
        PrintWriter        out  = (PrintWriter) ctx.get(PrintWriter.class);
        TableRenderer      r    = FrameworkSupportHelper.get(ctx).getTableRenderer(ctx);
        Balance balance = bucket.getRegularBal();
        
        r.TR(ctx, out, balance, i);
        
        outputRow(ctx, bucket, bucketName, i, subscriber, dateWebControl);
    }

    protected void outputRow(
           Context ctx,
           SubscriberBucket bucket,
           String bucketName,
           int i,
           Subscriber subscriber,
           WebControl dateWebControl) //throws HomeException
   {
       PrintWriter        out  = (PrintWriter) ctx.get(PrintWriter.class);
       TableRenderer      r    = FrameworkSupportHelper.get(ctx).getTableRenderer(ctx);
       Balance balance = bucket.getRegularBal();

       r.TR(ctx, out, balance, i);

       r.TD(ctx,out, "align=\"left\"");
       out.print("<b>" + bucketName + "</b>");
       r.TDEnd(ctx,out);

       // KGR: This is because "Member" bundles set their values using the Group
       // fields.
       // I don't know why they don't just reuse the "Personal" fields or else only
       // have
       // one set of fields but the BM team insists on this odd design. Dec 15, 2005.
       long awarded = balance.getPersonalLimit()   + balance.getGroupLimit();
       long used = balance.getPersonalUsed()    + balance.getGroupUsed();
       long available = balance.getPersonalBalance() + balance.getGroupBalance();
       long rollOverAwarded = balance.getRolloverLimit(); 
       long rollOverUsed = balance.getRolloverUsed();
       long rollOverAvailable = balance.getRolloverBalance();
       UnitTypeEnum       unit = bucket.getUnitType();

       rightTD(ctx, out, r, unit, awarded);
       rightTD(ctx, out, r, unit, used);
       rightTD(ctx, out, r, unit, available);
       rightTD(ctx, out, r, unit, rollOverAwarded);
       rightTD(ctx, out, r, unit, rollOverUsed);
       rightTD(ctx, out, r, unit, rollOverAvailable);

       r.TD(ctx,out);
       out.print(unit.getUnits());
       r.TDEnd(ctx,out);

       r.TD(ctx,out);
       out.print(bucket.getStatus());
       r.TDEnd(ctx,out);

       r.TD(ctx,out, "align=\"right\"");
       if (bucket.getActivationTime() != -1)
       {
           dateWebControl.toWeb(ctx, out, "", bucket.getActivationTime());
       }
       r.TDEnd(ctx,out);

       r.TD(ctx,out, "align=\"right\"");
       if (bucket.getExpiryTime() != -1)
       {
           dateWebControl.toWeb(ctx, out, "", bucket.getExpiryTime());
       }
       r.TDEnd(ctx,out);

       r.TREnd(ctx,out);
    }


    protected void outputHeading(final PrintWriter out, final String heading)
    {
        out.print("<tr><td align=center bgcolor=afafaf colspan=11 align=left><font color=white size=+1>");
        out.print(heading);
        out.println("</font></td></tr>");
    }

    private BundleProfile getBundleProfile(final Context ctx, final SubscriberBucket bucket, final Map<Long, BundleProfile> crossUnitBundles)
    {
        BundleProfile profile = crossUnitBundles.get(bucket.getBundleId());
        try
        {
            if (profile==null)
            {
                final CRMBundleProfile bundleService = (CRMBundleProfile) ctx.get(CRMBundleProfile.class);
                profile = bundleService.getBundleProfile(ctx, bucket.getSpid(), bucket.getBundleId());

                if (profile!=null && profile.isCrossService())
                {
                    crossUnitBundles.put(bucket.getBundleId(), profile);
                }
            }
        }
        catch (Exception e)
        {
            StringBuilder msg = new StringBuilder();
            msg.append("Could not retrieve bundle profile ");
            msg.append(bucket.getBundleId());
            msg.append(" while showing bundle usage for subscriber with MSISDN='");
            msg.append(bucket.getMsisdn());
            msg.append("': ");
            msg.append(e.getMessage());
            new MinorLogMsg(this, msg.toString(), e);
        }
        
        return profile;
    }
    

    public void execute(final Context ctx)
    {
        final PrintWriter out = (PrintWriter) ctx.get(PrintWriter.class);
        final TableRenderer r = FrameworkSupportHelper.get(ctx).getTableRenderer(ctx);
        final Context ses = Session.getSession(ctx);
        final Subscriber sub = (Subscriber) ses.get(Subscriber.class);

      final CRMSubscriberBucketProfile service = (CRMSubscriberBucketProfile) ctx.get(CRMSubscriberBucketProfile.class);

        if (sub == null)
        {
            out.println("Please selected subscriber first.");
            return;
        }

        /*
         * [Cindy] 2008-02-29 TT8020600010: Don't display usage if subscriber is inactive.
         */
        else if (SafetyUtil.safeEquals(sub.getState(), SubscriberStateEnum.INACTIVE))
        {
            out.println("Can not show bundle usage as subscriber has been deactivated.");
            return;
        }

        r.Table(ctx, out, "Bundle Usage and Status");
        out.print("<tr>");
        out.print("<th rowspan=2 bgcolor=e0e0e0>Bundle</th>");
        out.print("<th colspan=3 bgcolor=e0e0e0>Usage</th>");
        out.print("<th colspan=3 bgcolor=e0e0e0>Rollover</th>");
        out.print("<th rowspan=2 bgcolor=e0e0e0>Units</th>");
        out.print("<th rowspan=2 bgcolor=e0e0e0>Status</th>");
        out.print("<th rowspan=2 bgcolor=e0e0e0>Start Date</th>");
        out.print("<th rowspan=2 bgcolor=e0e0e0>End Date</th>");
        out.print("</tr>");
        out.print("<tr>");
        out.print("<th bgcolor=e0e0e0>Awarded</th>");
        out.print("<th bgcolor=e0e0e0>Used</th>");
        out.print("<th bgcolor=e0e0e0>Available</th>");

        out.print("<th bgcolor=e0e0e0>Awarded</th>");
        out.print("<th bgcolor=e0e0e0>Used</th>");
        out.print("<th bgcolor=e0e0e0>Available</th>");
        out.print("</tr>");

        try
        {
            Context sCtx = ctx.createSubContext();
            sCtx.put(SubscriberBucketHome.class, service.getBuckets(ctx, sub.getMSISDN(), (int) sub.getSubscriptionType()));
            
            Collection<SubscriberBucket> buckets = HomeSupportHelper.get(sCtx).getBeans(
                    sCtx, 
                    SubscriberBucket.class, 
                    True.instance(), 
                    true, 
                    SubscriberBucketXInfo.UNIT_TYPE);
            
            Map<Long, BundleProfile> crossUnitBundles = new HashMap<Long, BundleProfile>();
            Map<Long, Collection<SubscriberBucket>> crossUnitBundleBuckets = new HashMap<Long, Collection<SubscriberBucket>>();
            BundleTypeEnum currentBundleType = null; 

            Iterator<SubscriberBucket> iter = buckets.iterator();
            
            while (iter.hasNext())
            {
                SubscriberBucket bucket = iter.next();
                Long bundleId = bucket.getBundleId();
                BundleProfile profile = getBundleProfile(ctx, bucket, crossUnitBundles);

                String bucketName = profile!=null?profile.getName():bucket.getUnitType().getBundleType().getDescription();

                if (profile != null && profile.isCrossService())
                {
                    Collection<SubscriberBucket> bundleBucketsCollection = crossUnitBundleBuckets.get(bundleId);
                    if (bundleBucketsCollection==null)
                    {
                        bundleBucketsCollection = new ArrayList<SubscriberBucket>();
                        crossUnitBundleBuckets.put(bundleId, bundleBucketsCollection);
                    }
                    bundleBucketsCollection.add(bucket);
                }
                else
                {
                    if (currentBundleType == null
                            || !currentBundleType.equals(bucket.getUnitType().getBundleType()))
                    {
                        currentBundleType = bucket.getUnitType().getBundleType();
                        outputHeading(
                                out,
                                currentBundleType.getDescription());
                        outputFirstRow(sCtx, bucket, bucketName, 1, sub, getDateWebControl(ctx));
                    }
                    else
                    {
                        outputRow(sCtx, bucket, bucketName, 1, sub, getDateWebControl(ctx));
                    }
                }
            }
            
            if (crossUnitBundleBuckets.keySet().size()>0)
            {
                outputHeading(
                        out,
                        BundleTypeEnum.CROSS_SERVICE.getDescription());
            }
            
            for (Long bundleId : crossUnitBundleBuckets.keySet())
            {
                String bucketName = crossUnitBundles.get(bundleId).getName();
                outputRows(sCtx, crossUnitBundleBuckets.get(bundleId), bucketName, 1, sub, getDateWebControl(ctx));
            }
            
            
        }
        catch (final Throwable t)
        {
            new MinorLogMsg(this, "Internal Error in Bundle Usage", t).log(ctx);
        }

        r.TableEnd(ctx,out);
    }

    private WebControl getDateWebControl(Context ctx)
    {
        MessageMgr mmgr = new MessageMgr(ctx, this);

        // The old date format that was previously used by default
        String defaultDateFormat = "EEE MMM dd HH:mm:ss z yyyy";
        String dateFormat = mmgr.get("BundleUsageDateFormat", defaultDateFormat, new Object[]{});
/*        try
        {
            // Check to make sure the date format is valid
            new SimpleDateFormat(dateFormat);
        }
        catch (IllegalArgumentException e)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Date format configured in message manager under key 'BundleUsageDateFormat' is invalid: " + dateFormat, e).log(ctx);
            }
            dateFormat = defaultDateFormat;
        }
        catch (Throwable t)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Unexpected error parsing date format: " + dateFormat, t).log(ctx);
            }
            dateFormat = defaultDateFormat;
        }
  */      
        return new DateLongWebControl(dateFormat);
    }
}
