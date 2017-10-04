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
 * Copyright &copy; Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.web.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.core.Msisdn;
import com.trilogy.app.crm.bean.MsisdnStateEnum;
import com.trilogy.app.crm.bean.PackageStateEnum;
import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.ReactivateSubscriberForm;
import com.trilogy.app.crm.bean.ReactivateSubscriberFormWebControl;
import com.trilogy.app.crm.bean.ReactivateSubscriberFormXInfo;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberUsage;
import com.trilogy.app.crm.home.sub.Claim;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.app.crm.numbermgn.GenericPackage;
import com.trilogy.app.crm.numbermgn.MsisdnManagement;
import com.trilogy.app.crm.subscriber.filter.SubscriberUseExpiryPredicate;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.PackageSupportHelper;
import com.trilogy.app.crm.support.PricePlanSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.web.renderer.ButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultFormRenderer;
import com.trilogy.framework.xhome.web.renderer.FormRenderer;
import com.trilogy.framework.xhome.web.util.Link;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xhome.webcontrol.OutputWebControl;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.OMLogMsg;
import com.trilogy.framework.xhome.web.util.ImageLink;

/**
 * Handles the processing of the ReactivateSubscriberForm entity defined in
 * ReactivateSubscriber.xml model file.
 *
 * @author jimmy.ng@redknee.com
 */
public class ReactivateSubscriberRequestServicer implements RequestServicer
{
    /**
     * Creates a new ReactivateSubscriberRequestServicer.
     */
    public ReactivateSubscriberRequestServicer()
    {
        webControl_ = new ReactivateSubscriberFormWebControl();
    }

    /**
     * {@inheritDoc}
     */
    public void service(final Context ctx, final HttpServletRequest req, final HttpServletResponse res)
        throws ServletException, IOException
    {
        final HttpSession session = req.getSession();
        final PrintWriter out = res.getWriter();
        final Context subContext = ctx.createSubContext();
        subContext.put("MODE", OutputWebControl.EDIT_MODE);
        final ReactivateSubscriberForm form = new ReactivateSubscriberForm();
        String msg = null;
        boolean msgIsProblemReport = false;
        final ButtonRenderer buttonRenderer = (ButtonRenderer) subContext.get(ButtonRenderer.class,
                DefaultButtonRenderer.instance());
        final MessageMgr manager = new MessageMgr(subContext, this);
        final HTMLExceptionListener exceptions = new HTMLExceptionListener(manager);
        subContext.put(ExceptionListener.class, exceptions);
        webControl_.fromWeb(subContext, form, req, "");
        if (!exceptions.hasErrors())
        {
            try
            {
                synchronized (SESSION_SET)
                {
                    if (!SESSION_SET.add(session.getId()))
                    {
                        throw new HomeException("The previous reactivate subscriber action isn't "
                                + "complete yet, please try again later.");
                    }
                }
                if (buttonRenderer.isButton(subContext, "Preview"))
                {
                    final Subscriber subscriber = getSubscriber(subContext, form.getExistingSubscriberIdentifier());
                    validate(subContext, form, subscriber);
                }
                else if (buttonRenderer.isButton(subContext, "Reactivate"))
                {
                    final Subscriber subscriber = getSubscriber(subContext, form.getExistingSubscriberIdentifier());
                    validate(subContext, form, subscriber);
                    if (reactivate(subContext, form, subscriber) != null)
                    {
                        msg = manager.get("ReactvateSubscriber.successMessage",
                                "Subscriber {0} successfully reactivated.",
                                new String[]{form.getExistingSubscriberIdentifier()});
                    }
                    else
                    {
                        msg = manager.get("ReactvateSubscriber.successMessage",
                                "Make the Mobile no and package available for the subscriber to go active.",
                                new String[]{form.getExistingSubscriberIdentifier()});
                    }
                }
            }
            catch (final IllegalPropertyArgumentException exception)
            {
                exceptions.thrown(exception);
                msgIsProblemReport = true;
                msg = "";
            }
            catch (final HomeException exception)
            {
                exceptions.thrown(exception);
                msgIsProblemReport = true;
                msg = exception.getMessage();
                new MajorLogMsg(this, "Problem occured during subscriber reactivation.", exception).log(subContext);
            }
            catch (final Throwable throwable)
            {
                exceptions.thrown(throwable);
                msgIsProblemReport = true;
                msg = throwable.getMessage();
                new MajorLogMsg(this, "Unexpected problem occured during subscriber reactivation.",
                        throwable).log(subContext);
            }
        }
        if (exceptions.hasErrors())
        {
            exceptions.toWeb(subContext, out, "", form);
        }
        final FormRenderer formRenderer = (FormRenderer) subContext.get(FormRenderer.class,
                DefaultFormRenderer.instance());
        formRenderer.Form(out, subContext);
        out.print("<table>");
        if (msg != null)
        {
            if (!msgIsProblemReport)
            {
                out.println("<tr><td align=\"center\"><b style=\"color:green;\">");
            }
            else
            {
                out.println("<tr><td align=\"center\"><b style=\"color:red;\">");
            }
            out.print(msg);
            out.println("</b></td></tr>");
        }
        out.print("<tr><td>");
        webControl_.toWeb(subContext, out, "", form);

        // TT7090600044 - Put each button in its own cell to prevent IE from overlapping the buttons when the form is too narrow
        out.print("<table><tr><td>");
        buttonRenderer.inputButton(out, subContext, "Reactivate");
        out.print("</td><td>");
        buttonRenderer.inputButton(out, subContext, "Preview");
        out.print("</td><td>");
        outputHelpLink(subContext, out);
        out.print("</td></tr></table>");
        out.print("</td></tr></table>");
        formRenderer.FormEnd(out);
        synchronized (SESSION_SET)
        {
            SESSION_SET.remove(session.getId());
        }
    }


    /**
     * Validates the form input for the reactivation of subscriber.
     *
     * @param context
     *            The operating context.
     * @param form
     *            The form containing the reactivation paramters.
     *
     * @throws IllegalPropertyArgumentException Thrown if the input is invalid.
     * @throws HomeException Thrown if there are problems accessing data in the operating context.
     */
    public static void validate(final Context context, final ReactivateSubscriberForm form, final Subscriber subscriber)
        throws IllegalPropertyArgumentException, HomeException
    {
        final MessageMgr manager = new MessageMgr(context, ReactivateSubscriberRequestServicer.class);
        final String extensionProperty = manager.get("ReactivateSubscriberForm.extensionPeriod.Label",
                ReactivateSubscriberFormXInfo.EXTENSION_PERIOD.getLabel(context));
        final String subscriberProperty = manager.get("ReactivateSubscriberForm.existingSubscriberIdentifier.Label",
                ReactivateSubscriberFormXInfo.EXISTING_SUBSCRIBER_IDENTIFIER.getLabel(context));
        // Disallow reactivation when subscriber is not inactive.
        if (subscriber.getState() != SubscriberStateEnum.INACTIVE)
        {
            throw new IllegalPropertyArgumentException(subscriberProperty, "The subscriber is not inactive.");
        }

        // Disallow reactivation when subscriber expiry is not being ignored and old extension period is smaller than 1.
        final int extensionPeriod = form.getExtensionPeriod();
        if (SubscriberUseExpiryPredicate.instance().f(context, subscriber) && extensionPeriod < 1)
        {
            throw new IllegalPropertyArgumentException(extensionProperty, "The extension period must be at least 1.");
        }

        // check that the MSISDN was not assigned to another Subscriber while this one was disabled
        try
        {
            final Msisdn msisdn = SubscriberSupport.lookupMsisdnObjectForMSISDN(context, subscriber.getMSISDN());
            if (msisdn.getState() != MsisdnStateEnum.AVAILABLE)
            {
                final String subID = msisdn.getSubscriberID(context, subscriber.getSubscriptionType());
                if (!SafetyUtil.safeEquals(subID, subscriber.getId()))
                {
                    throw new IllegalPropertyArgumentException(subscriberProperty,
                            "The subscriber MSISDN was reassigned to another subscriber.");
                }
            }
        }
        catch (final HomeException e)
        {
            throw new IllegalPropertyArgumentException(subscriberProperty, e);
        }

        final TechnologyEnum technology = subscriber.getTechnology();
        if (technology.isPackageAware())
        {
            // this validation applies only to package-aware subscriptions
            final GenericPackage pack = PackageSupportHelper.get(context).getPackage(context, subscriber.getTechnology(), subscriber.getPackageId(),
                    subscriber.getSpid());
            if (pack.getState() == PackageStateEnum.IN_USE)
            {
                throw new IllegalPropertyArgumentException(subscriberProperty,
                        "The subscriber Package was reassigned to another subscriber.");
            }
        }
    }


    /**
     * Gets an account from the given context.
     *
     * @param context
     *            The operating context.
     * @param ban
     *            The account identifier.
     *
     * @return The account.
     *
     * @exception HomeException
     *                Thrown if no AccountHome found in context.
     */
    private static Account getAccount(final Context context, final String ban) throws HomeException
    {
        final Home accountHome = (Home) context.get(AccountHome.class);
        if (accountHome == null)
        {
            throw new HomeException("System error: no AccountHome found in context.");
        }
        final Account account = (Account) accountHome.find(context, ban);
        if (account == null)
        {
            throw new HomeException("Could not find account for number \"" + ban + "\"");
        }
        return account;
    }


    /**
     * Gets a subscriber from the given context.
     *
     * @param context
     *            The operating context.
     * @param subscriberID
     *            The subscriber identifier.
     *
     * @return The subscriber.
     *
     * @throws IllegalPropertyArgumentException Thrown if the subscriber could not be found.
     * @throws HomeException Thrown if no SubscriberHome found in context.
     */
    private static Subscriber getSubscriber(final Context context, final String subscriberID)
        throws IllegalPropertyArgumentException, HomeException
    {
        final Home subscriberHome = getSubscriberHome(context);
        final Subscriber subscriber = (Subscriber) subscriberHome.find(context, subscriberID);
        if (subscriber == null)
        {
            final MessageMgr manager = new MessageMgr(context, ReactivateSubscriberRequestServicer.class);
            final String subscriberProperty = manager.get(
                    "ReactivateSubscriberForm.existingSubscriberIdentifier.Label",
                    ReactivateSubscriberFormXInfo.EXISTING_SUBSCRIBER_IDENTIFIER.getLabel(context));
            throw new IllegalPropertyArgumentException(subscriberProperty, "Could not find subscriber for number \""
                    + subscriberID + "\"");
        }
        return subscriber;
    }


    /**
     * Gets the subscriber home from the given context.
     *
     * @param context
     *            The operating context.
     *
     * @return The subscriber home.
     *
     * @exception HomeException
     *                Thrown if no SubscriberHome found in context.
     */
    private static Home getSubscriberHome(final Context context) throws HomeException
    {
        final Home subscriberHome = (Home) context.get(SubscriberHome.class);
        if (subscriberHome == null)
        {
            throw new HomeException("System error: no SubscriberHome found in context.");
        }
        return subscriberHome;
    }


    /**
     * Change the state of the Subscriber from Inactive to Active. Both the extension
     * period and the subscriber identifier are provided in the form. This method assumes
     * that the reactivation has already been validated.
     *
     * @param context
     *            The operating context.
     * @param form
     *            The form containing the identifiers.
     *
     * @return reactivated Subscriber object. Null if reactivation failed
     *
     * @throws HomeException
     *                Thrown if there are problems getting the required homes or related
     *                information from the given context.
     */
    public static Subscriber reactivate(final Context context, final ReactivateSubscriberForm form,
            final Subscriber subscriber) throws HomeException
    {
        // Clone the subscriber (before reactivation) for ER logging purpose.
        Subscriber subscriberBeforeReactivation = null;
        try
        {
            subscriberBeforeReactivation = (Subscriber) subscriber.clone();
        }
        catch (final CloneNotSupportedException exception)
        {
            throw new HomeException("System error: unable to clone subscriber.", exception);
        }
        
        boolean partiallyReactivate = false;
        try
        {
            new OMLogMsg(Common.OM_MODULE, Common.OM_SUB_REACTIVATE_ATTEMPT).log(context);
            subscriber.setState(SubscriberStateEnum.AVAILABLE);
            subscriber.setExpDateExt(form.getExtensionPeriod());
            final PricePlan plan = PricePlanSupport.getPlan(context, subscriber.getPricePlan());
            subscriber.setPricePlanVersion(plan.getCurrentVersion());
            
            context.put(REACTIVATION, true);
            final Home subHome = getSubscriberHome(context);
            final Subscriber resultSub = (Subscriber) subHome.store(context, subscriber);
            partiallyReactivate = true;
            
            if (resultSub.getState() == SubscriberStateEnum.AVAILABLE)
            {
                subscriber.setState(SubscriberStateEnum.ACTIVE);
                subHome.store(context,subscriber);
            }

            if (resultSub.getState() == SubscriberStateEnum.ACTIVE)
            {
                final Account account = AccountSupport.getAccount(context, resultSub.getBAN());
                // TODO 2008-09-05 isInvididual() figure out the propper condition
                if (false && account.isIndividual(context) && account.getState().equals(AccountStateEnum.INACTIVE))
                {
                    account.setState(AccountStateEnum.ACTIVE);
                    final Home home = (Home) context.get(AccountHome.class);
                    home.store(context, account);
                }

                new OMLogMsg(Common.OM_MODULE, Common.OM_SUB_REACTIVATE_SUCCESS).log(context);
                return resultSub;
            }
            else
            {
                new OMLogMsg(Common.OM_MODULE, Common.OM_SUB_REACTIVATE_FAIL).log(context);
            }
        }
        catch (final HomeException e)
        {
            new OMLogMsg(Common.OM_MODULE, Common.OM_SUB_REACTIVATE_FAIL).log(context);
            final ExceptionListener exceptions = (ExceptionListener) context.get(ExceptionListener.class);
            if (exceptions != null)
            {
                exceptions.thrown(e);
            }
            final String formattedMsg = MessageFormat.format("Failed to reactivate subscriber \"{0}\".",
                    subscriber.getId());

            if (!partiallyReactivate)
            {
                releaseMSISDNPackage(context, subscriber, exceptions);
            }

            throw new HomeException(formattedMsg, e);
        }
        finally
        {
            logSubscriberReactivationEventER(context, subscriberBeforeReactivation, subscriber);
        }
        return null;
    }

    /**
     * Copied from com.redknee.framework.xhome.webcontrol.BeanWebController. This
     * functionality is expected to be provided in a more reusable format in an upcoming
     * implementation of the Framework.
     *
     * @param context The operating context
     * @param out The output writer
     */
    private void outputHelpLink(final Context context, final PrintWriter out)
    {
        // TODO 2007-10-02 change to ButtonRenderer
        final ImageLink image = new ImageLink(context);
        final MessageMgr mmgr = new MessageMgr(context, this);
        final Link link = new Link(context);
        link.add("mode", "help");
        link.add("border", "hide");
        link.add("menu", "hide");
        final String redirectURL = link.write();
        out.print("<a href=\"" + redirectURL + "\" onclick=\"showHelpMenu('" + redirectURL
                + "'); return false\"><img border=\"0\" " + image.getSource(mmgr.get("Button.Help", "Help"))
                + " align=\"right\" alt=\"Help\" /></a>");
    }


    /**
     * Logs the "Subscriber Reactivation Event" Event Record.
     *
     * @param context The operating context.
     * @param oldSubscriber The subscriber before reactivation.
     * @param newSubscriber The subscriber after reactivation.
     */
    private static void logSubscriberReactivationEventER(final Context context, final Subscriber oldSubscriber,
            final Subscriber newSubscriber)
    {
        try
        {
            final Account oldAccount = getAccount(context, oldSubscriber.getBAN());
            final Account newAccount = getAccount(context, newSubscriber.getBAN());
            // TODO 2007-06-08 remove price plan use. try using Subscriber.getServices()
            final PricePlanVersion plan = newSubscriber.getPricePlan(context);
            String oldSupportMsisdn = "";
            String newSupportMsisdn = "";
            oldSupportMsisdn = oldSubscriber.getSupportMSISDN(context);
            newSupportMsisdn = newSubscriber.getSupportMSISDN(context);
            final SubscriberUsage usage;

            // FCT no longer supported
            usage = new SubscriberUsage();

            ERLogger.logModificationER(context, newSubscriber.getSpid(), oldSubscriber, newSubscriber,
                    usage.getFreeCallTimeAvailable(), usage.getFreeCallTimeAvailable(), usage.getFreeCallTimeUsed(),
                    0, // adjustmentMinutes
                    0, // adjustmentAmount
                    0, // pricePlanChangeResultCode
                    oldSubscriber.getState(), newSubscriber.getState(),
                    0, // stateChangeResultCode
                    oldSubscriber.getDeposit(context), newSubscriber.getDeposit(context),
                    oldSubscriber.getCreditLimit(context), newSubscriber.getCreditLimit(context),
                    0, // creditLimitResultCode
                    oldAccount.getCurrency(), newAccount.getCurrency(), oldSubscriber.getServices(),
                    newSubscriber.getServices(),
                    0, // serviceChangeResultCode
                    oldSupportMsisdn, newSupportMsisdn, 0);
        }
        catch (final Throwable throwable)
        {
            new MajorLogMsg(ReactivateSubscriberRequestServicer.class,
                    "Failed to create ER log message for Subscriber Reactivation.",
                    throwable).log(context);
        }
    }


    /**
     * Release the MSISDN and Package TO held state if the activation failed somehow
     *
     * @param ctx The operating Context
     * @param sub The subscriber who's MSISDN and Package will be released
     * @param el The ExceptionListener
     */
    private static void releaseMSISDNPackage(final Context ctx, final Subscriber sub, final ExceptionListener el)
    {
        try
        {
            MsisdnManagement.deassociateMsisdnWithSubscription(ctx, sub.getMSISDN(), sub, "voiceMsisdn");
        }
        catch (final Exception e)
        {
            final String msg = "failed to release MSISDN for sub with MSISDN [" + sub.getMSISDN() + "]";
            new MinorLogMsg(ReactivateSubscriberRequestServicer.class, msg, e).log(ctx);
            if (el != null)
            {
                el.thrown(new HomeException(msg, e));
            }
        }

        try
        {
            final TechnologyEnum technology = sub.getTechnology();
            if (technology == null)
            {
                throw new HomeException("Technology " + sub.getTechnology() + " not found for subscription " + sub.getId());
            }
            if (technology.isPackageAware())
            {
                Claim.releasePackage(ctx, sub.getPackageId(), sub.getTechnology(), sub.getSpid());
            }
        }
        catch (final Exception e)
        {
            final String msg = "failed to release package for sub with package id [" + sub.getPackageId() + "]";
            new MinorLogMsg(ReactivateSubscriberRequestServicer.class, msg, e).log(ctx);
            if (el != null)
            {
                el.thrown(new HomeException(msg, e));
            }
        }
    }

    /**
     * The webcontrol used to represent the form.
     */
    private final ReactivateSubscriberFormWebControl webControl_;

    private static final Set SESSION_SET = new HashSet();

    public static final String REACTIVATION = "ReactivatingSub";

} // class
