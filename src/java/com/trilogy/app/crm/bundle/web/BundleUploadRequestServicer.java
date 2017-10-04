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

package com.trilogy.app.crm.bundle.web;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.csv.CSVIterator;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.web.util.FileUploadRequestServicer;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.CommonTime;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bundle.AddBundleRequest;
import com.trilogy.app.crm.bundle.AddBundleRequestCSVSupport;
import com.trilogy.app.crm.bundle.BundleSegmentEnum;
import com.trilogy.app.crm.bundle.BundleTypeEnum;
import com.trilogy.app.crm.bundle.SubscriberBundleSupport;
import com.trilogy.app.crm.support.BundleSupportHelper;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;

/**
 * File Upload RequestServicer for handling bundle bulk loading.
 *
 * @author kevin.greer@redknee.com
 */
public class BundleUploadRequestServicer extends FileUploadRequestServicer
{
    public static final String STATUS_OK = "201 OK";
    public static final String ERROR_FILE_IO = "401 FILE IO ERROR";
    public static final String ERROR_LINE_IO = "411 LINE READ ERROR";
    public static final String ERROR_UNKNOWN_BUNDLE = "421 UNKNOWN BUNDLE";
    public static final String ERROR_UNKNOWN_SUBSCRIBER = "422 UNKNOWN SUBSCRIBER";
    public static final String ERROR_NON_AUX_BUNDLE = "431 NON AUXILIARY OR POINTS BUNDLE";
    public static final String ERROR_INCOMPATIBLE_SPIDS = "432 INCOMPATIBLE SERVICE PROVIDERS";
    public static final String ERROR_INCOMPATIBLE_SEGMENTS = "433 INCOMPATIBLE SEGMENTS";
    public static final String ERROR_LINE_OTHER = "439 OTHER CONSTRAINT ERROR";
    public static final String ERROR_REDUNDANT = "441 BUNDLE ALREADY PROVISIONED";
    public static final String ERROR_INTERNAL = "490 INTERNAL ERROR";

    public BundleUploadRequestServicer()
    {
        // 8 Meg limit
        super("Bundle Bulk Loader", 8 * 1024 * 1024);
    }

    @Override
    public void processFile(final Context ctx, final PrintWriter out, final File file)
    {
        out.print("</center>");
        long line = -1;

        try
        {
            final CSVIterator i = new CSVIterator(AddBundleRequestCSVSupport.instance(), file.getCanonicalPath());
            AddBundleRequest req = null;

            for (line = 1; i.hasNext(); line++)
            {
                try
                {
                    req = (AddBundleRequest) i.next();
                    process(ctx, req);

                    status(ctx, line, out, STATUS_OK);
                }
                catch (IllegalArgumentException t)
                {
                    status(ctx, line, out, buildMessage(t.getMessage(), req));
                }
                catch (HomeException t)
                {
                    status(ctx, line, out, buildMessage(ERROR_LINE_OTHER, req), t);
                }
                catch (Throwable t)
                {
                    status(ctx, line, out, buildMessage(ERROR_LINE_IO, req), t);
                }
            }
        }
        catch (IOException t)
        {
            status(ctx, line, out, ERROR_FILE_IO, t);
        }
        catch (Throwable t)
        {
            status(ctx, line, out, ERROR_INTERNAL, t);
        }

        out.print("<center>");
    }

    private String buildMessage(final String message, final AddBundleRequest req)
    {
        if (req == null)
        {
            return message;
        }
        else
        {
            return message + ", msisdn=" + req.getMSISDN() + ", bundle=" + req.getId();
        }
    }

    public void status(final Context ctx, final long line, final PrintWriter out, final String msg)
    {
        status(ctx, line, out, msg, null);
    }

    public void status(final Context ctx, final long line, final PrintWriter out, final String msg, final Throwable t)
    {
        if (t == null)
        {
            out.print(msg + ", line: " + line);
        }
        else
        {
            final String s = msg + ", line: " + line + ", " + t;
            out.print(s);
            LogSupport.minor(ctx, this, s, t);
        }

        out.println("<br/>");
    }

    public void process(final Context ctx, final AddBundleRequest req)
        throws IllegalArgumentException, HomeException
    {
      Home subHome    = (Home) ctx.get(SubscriberHome.class);

      BundleProfile bundle = null;

      try
      {
          bundle = BundleSupportHelper.get(ctx).getBundleProfile(ctx, req.getId());
      }
      catch (Exception e)
      {
          LogSupport.major(ctx, this, "Error while trying to get the bundle id " + req.getId(), e);
          throw new IllegalArgumentException(ERROR_UNKNOWN_BUNDLE);
      }

        if (bundle == null)
        {
            throw new IllegalArgumentException(ERROR_UNKNOWN_BUNDLE);
        }

        boolean isPointsBundle = BundleTypeEnum.POINTS_INDEX == bundle.getType();
        if (!bundle.isAuxiliary() && !isPointsBundle)
        {
            throw new IllegalArgumentException(ERROR_NON_AUX_BUNDLE);
        }

        final Subscriber sub = SubscriberSupport.lookupSubscriberForMSISDN(ctx, req.getMSISDN());

        if (sub == null)
        {
            throw new IllegalArgumentException(ERROR_UNKNOWN_SUBSCRIBER);
        }

        if (sub.getSpid() != bundle.getSpid())
        {
            throw new IllegalArgumentException(ERROR_INCOMPATIBLE_SPIDS);
        }

        if (!matchesSegments(sub, bundle))
        {
            throw new IllegalArgumentException(ERROR_INCOMPATIBLE_SEGMENTS);
        }

        if (SubscriberBundleSupport.getSubscribedBundles(ctx, sub).containsKey(Long.valueOf(req.getId())))
        {
            throw new IllegalArgumentException(ERROR_REDUNDANT);
        }

        // add bundle and store
        Date endDate = req.getEndDate();
        Date startDate = req.getStartDate();

        if (startDate == null)
        {
            startDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(new Date());
        }
        else
        {
            startDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(startDate);
        }

        final BundleFee fee = new BundleFee();
        fee.setId(req.getId());
        fee.setStartDate(startDate);

        final int paymentNum = req.getPaymentNum();
        if (paymentNum > 0)
        {
            endDate = CalendarSupportHelper.get(ctx).findDateMonthsAfter(paymentNum, startDate);
        }
        else if (endDate == null)
        {
            endDate = CalendarSupportHelper.get(ctx).findDateYearsAfter(CommonTime.YEARS_IN_FUTURE, startDate);
        }
        else
        {
            endDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(endDate);
        }

        fee.setEndDate(endDate);
        fee.setPaymentNum(paymentNum);

        if (!isPointsBundle)
        {
            sub.getBundles().put(Long.valueOf(req.getId()), fee);
        }
        else
        {
            sub.getPointsBundles().put(Long.valueOf(req.getId()), fee);
        }
        subHome.store(ctx,sub);
    }

    private boolean matchesSegments(Subscriber sub, BundleProfile bundle)
    {
        if (bundle.getSegment() == BundleSegmentEnum.HYBRID)
        {
            //If the bundle is Hybrid, we don't have to worry about segments
            return true;
        }
        boolean result = sub.isPrepaid() == (bundle.getSegment() == BundleSegmentEnum.PREPAID);
        return result;
    }
}


