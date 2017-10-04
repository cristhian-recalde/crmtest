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
package com.trilogy.app.crm.urcs;

import java.io.PrintWriter;
import java.util.Formatter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.trilogy.framework.core.locale.Currency;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.util.time.Duration;
import com.trilogy.framework.xhome.web.agent.WebAgent;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xhome.web.renderer.DefaultTableRenderer;
import com.trilogy.framework.xhome.web.renderer.TableRenderer;
import com.trilogy.framework.xhome.webcontrol.ColourSettings;
import com.trilogy.framework.xlog.log.PMLogMsg;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.custom.AuxiliaryService;
import com.trilogy.app.crm.client.RemoteServiceException;
import com.trilogy.app.crm.client.urcs.PromotionProvisionClient;
import com.trilogy.app.crm.client.urcs.UrcsClientInstall;
import com.trilogy.app.crm.support.AuxiliaryServiceSupport;
import com.trilogy.app.crm.support.FrameworkSupportHelper;
import com.trilogy.app.crm.support.LicensingSupportHelper;
import com.trilogy.app.urcs.promotion.Counter;
import com.trilogy.app.urcs.promotion.CounterInterval;
import com.trilogy.app.urcs.promotion.CounterProfile;
import com.trilogy.app.urcs.promotion.Promotion;
import com.trilogy.app.urcs.promotion.PromotionStatus;
import com.trilogy.app.urcs.promotion.PromotionUnit;
import com.trilogy.util.snippet.log.Logger;

/**
 * @author victor.stratan@redknee.com
 */
public class SubscriptionPromotionStatusWebAgent implements WebAgent
{
    /**
     * PM log module name.
     */
    public static String PM_MODULE = SubscriptionPromotionStatusWebAgent.class.getSimpleName();

    /**
     * {@inheritDoc}
     */
    public void execute(final Context ctx) throws AgentException
    {
        final PrintWriter out = WebAgents.getWriter(ctx);
        final TableRenderer renderer =  FrameworkSupportHelper.get(ctx).getTableRenderer(ctx);
        final MessageMgr messageMgr =  FrameworkSupportHelper.get(ctx).getMessageMgr(ctx, this);
        final Subscriber sub = (Subscriber) ctx.get(Subscriber.class);

        if (sub == null)
        {
            displayErrorMessage(out, "No Subscription selected!");
            return;
        }
        sub.setContext(ctx);

        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "execute", sub.getId());
        try
        {
            final PromotionProvisionClient client = UrcsClientInstall.getClient(ctx, UrcsClientInstall.PROMOTION_PROVISION_CLIENT_KEY);
            final PromotionStatus[] promotionsStatus = client.listSubscriptionPromotionStatus(ctx, sub);
            
            out.println("<table halgin=\"left\"><tr><td>");
            if (promotionsStatus.length == 0)
            {
                displayPromotionMissing(ctx, out, renderer, messageMgr);
            }
            else
            {
                final Map<Long, AuxiliaryService> promoAux =
                        AuxiliaryServiceSupport.getPromotionAuxiliaryServicesMap(ctx, sub.getSpid(), promotionsStatus);

                if (LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.RK_DEV_LICENSE))
                {
                    displayPromotionStatus(ctx, promotionsStatus, out, renderer);
                    out.println("<p>");
                }

                displayPromotionsBoxes(ctx, out, renderer, messageMgr, promotionsStatus, promoAux);
            }
            out.println("</td></tr></table>");
        }
        catch (RemoteServiceException e)
        {
            final String msg = "Unable to retrieve URCS Promotion Status for Subscription [" + sub.getId()
                    + "] due to: " + e.getMessage();
            displayErrorMessage(out, msg);
            Logger.minor(ctx, this, msg, e);
        }
        finally
        {
            pmLogMsg.log(ctx);
        }
    }



    private void displayPromotionStatus(final Context ctx, final PromotionStatus[] promotionsStatus,
            final PrintWriter out, final TableRenderer renderer)
    {
        int lineCount = 0;
        displayHeader(ctx, out, renderer, "Promo");
        for (int i = 0; i < promotionsStatus.length; i++)
        {
            final Promotion promotion = promotionsStatus[i].promotion;
            final Counter[] counters = promotionsStatus[i].counters;
            final CounterProfile[] counterProfiles = promotion.counters;

            displayPromotionRow(ctx, out, renderer, promotion, lineCount++);
            for (int j = 0; j < counters.length; j++)
            {
                final Counter counter = counters[j];
                final CounterProfile profile = findProfile(counter.counterId, counterProfiles);
                displayCounterRow(ctx, out, renderer, counter, profile, lineCount++);
            }
        }
        renderer.TableEnd(ctx, out);
    }

    private CounterProfile findProfile(final long id, final CounterProfile[] profiles)
    {
        for (int i = 0; i < profiles.length; i++)
        {
            if (profiles[i].profileId == id)
            {
                return profiles[i];
            }
        }
        return null;
    }

    private void displayHeader(final Context ctx, final PrintWriter out, final TableRenderer renderer, final String header)
    {
        renderer.Table(ctx, out, header);
        renderer.TR(ctx, out, null, 1);
        out.print("<th>Promotion ID</th><th>Promotion Name</th><th>Promotion Description</th><th>Option</th><th>&nbsp;</th><th>&nbsp;</th>");
        renderer.TREnd(ctx,out);
        renderer.TR(ctx, out, null, 1);
        out.print("<th colspan=\"2\">Counter ID</th><th>Counter Name</th><th>Value</th><th>Units</th><th>Reset Interval</th>");
        renderer.TREnd(ctx,out);
    }

    private void displayPromotionRow(final Context ctx, final PrintWriter out, final TableRenderer renderer, final Promotion promotion,
            final int lineCount)
    {
        renderer.TR(ctx, out, null, lineCount);
        renderer.TD(ctx,out);
        out.print(promotion.promotionId);
        renderer.TDEnd(ctx,out);
        renderer.TD(ctx,out);
        out.print(promotion.name);
        renderer.TDEnd(ctx,out);
        renderer.TD(ctx,out);
        out.print(promotion.description);
        renderer.TDEnd(ctx,out);
        renderer.TD(ctx,out);
        out.print(promotion.optionTag);
        renderer.TDEnd(ctx,out);
        renderer.TD(ctx,out);
        out.print("&nbsp;");
        renderer.TDEnd(ctx,out);
        renderer.TD(ctx,out);
        out.print("&nbsp;");
        renderer.TDEnd(ctx,out);
        renderer.TREnd(ctx,out);
    }

    private void displayCounterRow(final Context ctx, final PrintWriter out, final TableRenderer renderer,
            final Counter counter, final CounterProfile profile, final int lineCount)
    {
        renderer.TR(ctx, out, null, lineCount);
        renderer.TD(ctx,out, "colspan=\"2\"");
        out.print("<div align=\"right\">");
        out.print(profile.profileId);
        out.print("</div>");
        renderer.TDEnd(ctx,out);
        renderer.TD(ctx,out);
        out.print(profile.name);
        renderer.TDEnd(ctx,out);
        renderer.TD(ctx,out);
        out.print(getUnitString(profile));
        renderer.TDEnd(ctx,out);
        renderer.TD(ctx,out);
        out.print(counter.value);
        renderer.TDEnd(ctx,out);
        renderer.TD(ctx,out);
        out.print(getCounterIntervalString(profile.resetType));
        renderer.TDEnd(ctx,out);
        renderer.TREnd(ctx,out);
    }

    private void displayPromotionsBoxes(final Context ctx, final PrintWriter out, final TableRenderer renderer,
            final MessageMgr messageMgr, final PromotionStatus[] promotionsStatus,
            final Map<Long, AuxiliaryService> auxServices)
    {
        int lineCount = 0;
        TableRenderer activeTableRenderer;
        for (int i = 0; i < promotionsStatus.length; i++)
        {
            out.println("<tr><td>");
            final Promotion promotion = promotionsStatus[i].promotion;
            final Counter[] counters = promotionsStatus[i].counters;
            final CounterProfile[] counterProfiles = promotion.counters;
            final AuxiliaryService auxSrv = auxServices.get(Long.valueOf(promotion.optionTag));

            Object[] values = new Object[] {"", "", promotion.name, promotion.description, "", "", "", "", "", "", ""};
            if (auxSrv != null)
            {
                values[0] = String.valueOf(auxSrv.getIdentifier());
                values[1] = auxSrv.getName();
                activeTableRenderer = renderer;
            }
            else
            {
                activeTableRenderer = getAlternativeTableRenderer(ctx);
            }

            final String header = messageMgr.get(PROMOTION_HEADER_KEY, PROMOTION_HEADER_DEFAULT, values);
            displayBoxHeader(ctx, out, activeTableRenderer, header);

            final String promoMsg = messageMgr.get(PROMOTION_LINE_KEY, PROMOTION_LINE_DEFAULT, values);
            out.print(promoMsg);

            for (int j = 0; j < counters.length; j++)
            {
                final Counter counter = counters[j];
                final CounterProfile profile = findProfile(counter.counterId, counterProfiles);

                long[] thresholdHolder = new long[]{ -1L };
                String description = "";
                //String description = extractThreshold(profile.name, thresholdHolder);

                values[4] = profile.name;
                values[5] = description;
                values[6] = getUnitString(profile);
                values[7] = String.valueOf(counter.value);
                values[10] = getUnifiedUnitValueString(ctx, profile, counter.value);

                if (thresholdHolder[0] != -1)
                {
                    values[8] = String.valueOf(thresholdHolder[0]);
                    values[9] = String.valueOf(thresholdHolder[0] - counter.value);
                }
                else
                {
                    values[8] = "";
                    values[9] = "";
                }

                final String counterMsg = messageMgr.get(PROMOTION_COUNTER_LINE_KEY, PROMOTION_COUNTER_LINE_DEFAULT,
                        values);
                out.print(counterMsg);
            }

            if (auxSrv != null)
            {
                final String footerMsg = messageMgr.get(PROMOTION_FOOTER_KEY, PROMOTION_FOOTER_DEFAULT, values);
                out.print(footerMsg);
            }
            displayBoxEnd(ctx, out, activeTableRenderer);
            out.println("<p>");
        }
    }

    private void displayBoxHeader(final Context ctx, final PrintWriter out, final TableRenderer renderer, final String header)
    {
        renderer.Table(ctx, out, header);
        renderer.TR(ctx, out, null, 1);
        renderer.TD(ctx,out);
    }

    private void displayBoxEnd(final Context ctx, final PrintWriter out, final TableRenderer renderer)
    {
        renderer.TDEnd(ctx,out);
        renderer.TREnd(ctx,out);
        renderer.TableEnd(ctx, out);
    }

    private void displayPromotionMissing(final Context ctx, final PrintWriter out, final TableRenderer renderer,
            final MessageMgr messageMgr)
    {
        final String header = messageMgr.get(PROMOTION_MISSING_HEADER_KEY, PROMOTION_MISSING_HEADER_DEFAULT);
        final String message = messageMgr.get(PROMOTION_MISSING_MESSAGE_KEY, PROMOTION_MISSING_MESSAGE_DEFAULT);
        renderer.Table(ctx, out, header);
        renderer.TR(ctx, out, null, 1);
        renderer.TD(ctx,out);
        out.print(message);
        renderer.TDEnd(ctx,out);
        renderer.TREnd(ctx,out);
        renderer.TableEnd(ctx, out);
    }

    private String getUnitString(final CounterProfile profile)
    {
        switch (profile.unit.value())
        {
            case PromotionUnit._SECONDS: return "Seconds";
            case PromotionUnit._CURRENCY: return "Dollar";
            case PromotionUnit._KB: return "KB";
            case PromotionUnit._EVENTS: return "Events";
            case PromotionUnit._NUMBER: return "Number";
        }
        return "";
    }

    private String getUnifiedUnitValueString(final Context ctx, final CounterProfile profile, final long value)
    {
        switch (profile.unit.value())
        {
            case PromotionUnit._SECONDS:
            {
                final Duration duration = new Duration(value * 1000L);
                final Formatter formatter = new Formatter();
                formatter.format("%02d:%02d MIN", duration.getRawMinutes(), duration.getSeconds());
                return formatter.toString();
            }
            case PromotionUnit._CURRENCY:
            {
                final Currency currency = (Currency) ctx.get(Currency.class, Currency.DEFAULT);
                return currency.formatValue(value);
            }
            case PromotionUnit._KB: return String.valueOf(value) + " " + getUnitString(profile);
            case PromotionUnit._EVENTS: return String.valueOf(value);
            case PromotionUnit._NUMBER: return String.valueOf(value);
        }
        return "";
    }

    private String getCounterIntervalString(final CounterInterval interval)
    {
        switch (interval.value())
        {
            case CounterInterval._HOURLY: return "Hourly";
            case CounterInterval._DAILY: return "Daily";
            case CounterInterval._WEEKLY: return "Weekly";
            case CounterInterval._MONTHLY: return "Monthly";
            case CounterInterval._YEARLY: return "Yearly";
            case CounterInterval._ABSOLUTE: return "Absolute";
        }
        return "";
    }

    private String extractThreshold(final String description, final long[] holder)
    {
        final Matcher m = THRESHOLD_REGEXP.matcher(description);
        if (m.matches())
        {
            final String match = m.group(1);
            if (match != null && match.length() > 0)
            {
                holder[0] = Long.parseLong(match);
            }
        }
        return m.replaceFirst("");
    }

    private void displayErrorMessage(final PrintWriter out, final String msg)
    {
        out.print("<font color=red>");
        out.print(msg);
        out.println("</font>");
    }

    private TableRenderer getAlternativeTableRenderer(final Context ctx)
    {
        synchronized (SubscriptionPromotionStatusWebAgent.class)
        {
            if (altTableRenderer == null)
            {
                ColourSettings cs = (ColourSettings) ctx.get(ColourSettings.class);
                final Context subCtx = ctx.createSubContext();
                try
                {
                    cs = (ColourSettings)cs.clone();
                }
                catch(CloneNotSupportedException e)
                {
                }
                cs.setTableTitleBG("#b3b3b3");
                cs.setTableTitleText("black");
                subCtx.put(ColourSettings.class, cs);

                altTableRenderer = new DefaultTableRenderer(subCtx);
            }
        }

        return altTableRenderer;
    }

    /**
     * Default Message for Promotion header.
     * {0} Auxiliary service ID
     * {1} Auxiliary service name
     * {2} Promotion name
     * {3} Promotion description
     * {4} Counter name
     * {5} Counter description (with the threshold removed)
     * {6} Counter units
     * {7} Counter value
     * {8} Counter threshold
     * {9} Counter delta
     * {10} Unified Unit-value i.e. $10 or 23:12 minutes
     */
    public static final String PROMOTION_HEADER_DEFAULT = "Promotion {2}";
    public static final String PROMOTION_HEADER_KEY = "PROMOTION_HEADER_KEY";
    public static final String PROMOTION_LINE_DEFAULT = "Promotion {3}<br>";
    public static final String PROMOTION_LINE_KEY = "PROMOTION_LINE_KEY";
    public static final String PROMOTION_COUNTER_LINE_DEFAULT = "<p>{4} {10}</p>";
    public static final String PROMOTION_COUNTER_LINE_KEY = "PROMOTION_COUNTER_LINE_KEY";
    public static final String PROMOTION_FOOTER_DEFAULT = "Service Name: <a href=\"/AppCrm/home?cmd=appCRMConfigAuxiliaryService&amp;action=edit&amp;key={0}\">{1}</a>";
    public static final String PROMOTION_FOOTER_KEY = "PROMOTION_FOOTER_KEY";

    public static final String PROMOTION_MISSING_HEADER_DEFAULT = "Promotion Status";
    public static final String PROMOTION_MISSING_HEADER_KEY = "PROMOTION_MISSING_HEADER_KEY";
    public static final String PROMOTION_MISSING_MESSAGE_DEFAULT = "Status not available for any promotion OR valid promotion not found for the subscriber.";
    public static final String PROMOTION_MISSING_MESSAGE_KEY = "PROMOTION_MISSING_MESSAGE_KEY";

    public static final String THRESHOLD_REGEXP_STR = ".*%(\\d+)%.*";
    public static final Pattern THRESHOLD_REGEXP = Pattern.compile(THRESHOLD_REGEXP_STR);

    private static TableRenderer altTableRenderer = null;

}
