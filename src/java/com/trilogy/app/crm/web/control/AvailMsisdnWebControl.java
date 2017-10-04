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
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.elang.Limit;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.Homes;
import com.trilogy.framework.xhome.home.SortingHome;
import com.trilogy.framework.xhome.visitor.CountingVisitor;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.PrimitiveWebControl;
import com.trilogy.framework.xhome.webcontrol.TextFieldWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;

import com.trilogy.app.crm.bean.AbstractSubscriber;
import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.MsisdnGroup;
import com.trilogy.app.crm.bean.MsisdnGroupHome;
import com.trilogy.app.crm.bean.MsisdnGroupKeyWebControl;
import com.trilogy.app.crm.bean.MsisdnGroupTransientHome;
import com.trilogy.app.crm.bean.MsisdnHome;
import com.trilogy.app.crm.bean.MsisdnKeyWebControl;
import com.trilogy.app.crm.bean.MsisdnStateEnum;
import com.trilogy.app.crm.bean.MsisdnTransientHome;
import com.trilogy.app.crm.bean.MsisdnXInfo;
import com.trilogy.app.crm.bean.PortingTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.SysFeatureCfg;
import com.trilogy.app.crm.factory.SubscriberFactory;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.MsisdnSupport;
import com.trilogy.app.crm.technology.TechnologyEnum;


/**
 * WebControl for selecting MSISDN's and MSISDN Groups.
 *
 * @author paul.sperneac@redknee.com
 * @author cindy.wong@redknee.com
 */
public abstract class AvailMsisdnWebControl extends PrimitiveWebControl
{

    /**
     * Field width for free-form MSISDN web control.
     */
    private static final int FREEFORM_MSISDN_FIELD_WIDTH = 15;

    /**
     * Field name suffix of the MSISDN group field.
     */
    protected static final String MSISDN_GROUP_FIELD_NAME = ".MsisdnGroup";

    /**
     * Field name suffix of MSISDN field.
     */
    protected static final String MSISDN_FIELD_NAME = ".Msisdn";

    /**
     * Field name suffix of hidden start index field.
     */
    protected static final String START_INDEX_FIELD_NAME = ".start";

    /**
     * Name of the custom group.
     */
    protected static final String CUSTOM_GROUP_NAME = "custom";

    /**
     * Context key for MSISDN group value.
     */
    protected static final String MSISDN_GROUP_KEY = "AvailMsisdnWebControl.MSISDN_GROUP";

    /**
     * Context key for MSISDN value.
     */
    protected static final String MSISDN_KEY = "AvailMsisdnWebControl.MSISDN";

    /**
     * Context key for original MSISDN value.
     */
    protected static final String ORIGINAL_MSISDN_KEY = "AvailMsisdnWebControl.ORIGINAL_MSISDN";

    /**
     * Number of MSISDN's to display at once. it only display 5 at once, now up the size
     * for bulk load display.
     */
    public static final int WINDOW_SIZE = 20;

    /**
     * Fake ID to use for custom group.
     */
    protected static final int CUSTOM_GROUP = 999;


    /**
     * Create a new instance of <code>AvailMsisdnWebControl</code>.
     */
    public AvailMsisdnWebControl()
    {
        this.msisdnWebControl_ = new MsisdnKeyWebControl(1, false, false);
        this.optional_ = false;
    }


    /**
     * Create a new instance of <code>AvailMsisdnWebControl</code>.
     *
     * @param isOptional
     *            Whether the MSISDN is optional.
     */
    public AvailMsisdnWebControl(final boolean isOptional)
    {
        this.optional_ = isOptional;

        if (isOptional)
        {
            this.msisdnWebControl_ = new MsisdnKeyWebControl(1, false, "");
        }
        else
        {
            this.msisdnWebControl_ = new MsisdnKeyWebControl(1, false, isOptional);
        }
    }


    /**
     * Looks up the subscriber in context.
     *
     * @param context
     *            The operating context.
     * @return Subscriber in context.
     */
    protected Subscriber lookupSubscriber(final Context context)
    {
        final Object bean = context.get(AbstractWebControl.BEAN);
        Subscriber subscriber = null;
        if (bean != null && bean instanceof Subscriber)
        {
            subscriber = (Subscriber) bean;
        }
        else
        {
            subscriber = (Subscriber) context.get(Subscriber.class);
        }
        if (subscriber == null)
        {
            subscriber = (Subscriber) context.get(Lookup.OLDSUBSCRIBER);
        }
        if (subscriber == null)
        {
            subscriber = new Subscriber();
            SubscriberFactory.initSubscriber(context, subscriber);
        }
        return subscriber;
    }


    /**
     * Looks up the technology to use.
     *
     * @param context
     *            The operating context.
     * @return The technology of the current subscriber, or return
     *         {@link TechnologyEnum#ANY} if none exists.
     */
    protected TechnologyEnum lookupTechnology(final Context context)
    {
        final Subscriber subscriber = lookupSubscriber(context);
        TechnologyEnum result = null;
        if (subscriber != null)
        {
            result = subscriber.getTechnology();
        }
        if (result == null)
        {
            result = TechnologyEnum.ANY;
        }
        return result;
    }


    /**
     * Returns a subcontext with the {@link MsisdnGroupHome} filtered based on technology.
     *
     * @param context
     *            The operating context.
     * @return A subcontext of the operating context with the {@link MsisdnGroupHome}
     *         filtered based on the subscriber's technology.
     */
    protected Context wrapMsisdnGroupContext(final Context context)
    {
        final TechnologyEnum techEnum = lookupTechnology(context);
        final Context subContext = context.createSubContext();
        subContext.setName("Technology-filtered MSISDN group home");

        Home msisdnGroupHome = (Home) context.get(MsisdnGroupHome.class);
        final MsisdnGroupTransientHome home = new MsisdnGroupTransientHome(context);
        try
        {
            if (TechnologyEnum.ANY != techEnum && TechnologyEnum.NO_TECH != techEnum)
            {
                final Set<TechnologyEnum> selectableTechnologies = new HashSet<TechnologyEnum>();
                selectableTechnologies.add(techEnum);
                selectableTechnologies.add(TechnologyEnum.ANY);
                msisdnGroupHome = msisdnGroupHome
                    .where(context, new In(MsisdnXInfo.TECHNOLOGY, selectableTechnologies));
            }
            Homes.copy(msisdnGroupHome, home);

            // add custom group
            final MsisdnGroup customGroup = new MsisdnGroup();
            customGroup.setId(CUSTOM_GROUP);
            customGroup.setName(CUSTOM_GROUP_NAME);
            home.create(subContext, customGroup);

            subContext.put(MsisdnGroupHome.class, home);
            subContext.put(AbstractWebControl.PROPERTY, SubscriberXInfo.MSISDNGROUP);
        }
        catch (final UnsupportedOperationException e)
        {
            new DebugLogMsg(this, "Exception while getting the Msisdn Group collection for the Technology ="
                + techEnum.getDescription(), e).log(context);
        }
        catch (final HomeException e)
        {
            new DebugLogMsg(this, "Exception while getting the Msisdn Group collection for the Technology ="
                + techEnum.getDescription(), e).log(context);
        }
        return subContext;
    }


    /**
     * Determines whether only the free-form custom MSISDN field should be displayed.
     *
     * @param context
     *            The operating context.
     * @return Returns <code>true</code> if only the free-form custom MSISDN field
     *         should be displayed.
     */
    protected boolean isDisplayFreeformOnly(final Context context)
    {
        final SysFeatureCfg systemConfiguration = (SysFeatureCfg) context.get(SysFeatureCfg.class);
        final Subscriber subscriber = lookupSubscriber(context);
        boolean result = false;
        if (subscriber != null && systemConfiguration != null)
        {
            /*
             * Only a text field is required because only "custom" is available for
             * Prepaid Subscribers.
             */
            result = subscriber.getSubscriberType() == SubscriberTypeEnum.PREPAID
                && !systemConfiguration.isPrepaidMsisdnSelectionEnabled();
        }
        return result;
    }


    /**
     * Returns a subcontext with the {@link MsisdnHome} filtered.
     *
     * @param context
     *            The operating context.
     * @param msisdnGroupId
     *            The current MSISDN group to use.
     * @param msisdn
     *            The currently selected MSISDN.
     * @param startIndex
     *            The starting index of the window.
     * @return A subcontext of the operating context, with {@link MsisdnHome} filtered
     *         based on the availability, subscriber type, SPID, and MSISDN group.
     * @throws HomeException
     *             Thrown if there are problems creating the home.
     */
    protected Context wrapMsisdnContext(final Context context, final int msisdnGroupId, final String msisdn,
        final int startIndex) throws HomeException
    {
        /*
         * TODO [Cindy] 2008-02-08: Move this logic to MsisdnSupport?
         */
        final Home transientHome = new MsisdnTransientHome(context);
        final Subscriber subscriber = lookupSubscriber(context);
        final And and = new And();
        and.add(new EQ(MsisdnXInfo.GROUP, Integer.valueOf(msisdnGroupId)));
        and.add(new EQ(MsisdnXInfo.SPID, Integer.valueOf(subscriber.getSpid())));
        and.add(new EQ(MsisdnXInfo.SUBSCRIBER_TYPE, subscriber.getSubscriberType()));

        final Or or = new Or();
        or.add(new EQ(MsisdnXInfo.STATE, MsisdnStateEnum.AVAILABLE));

        /*
         * [Cindy] 2007-10-05: Allow MSISDNs in HELD state if it is held by this
         * subscriber.
         */
        if (!SafetyUtil.safeEquals(subscriber.getId(), AbstractSubscriber.DEFAULT_ID))
        {
            final And and2 = new And();
            and2.add(new EQ(MsisdnXInfo.STATE, MsisdnStateEnum.HELD));
            and2.add(new EQ(MsisdnXInfo.BAN, subscriber.getBAN()));
            and2.add(new EQ(MsisdnXInfo.PORTING_TYPE, PortingTypeEnum.NONE));
            or.add(and2);
        }
        and.add(or);

        /*
         * [Cindy] 2007-09-10: Work around framework silently dropping offset in Limit
         * when composing the WHERE clause. When if finally supports Limit properly, the
         * below chunk can be replaced by Homes.copy().
         */
        and.add(new Limit(startIndex - 1 + WINDOW_SIZE));
        final Home msisdnHome = new SortingHome(((Home) context.get(MsisdnHome.class)).where(context, and));
        int count = 0;
        for (final Object element : msisdnHome.selectAll(context))
        {
            final Msisdn m = (Msisdn) element;
            if (count >= startIndex - 1)
            {
                transientHome.create(context, m);
            }
            count++;
        }

        final String originalMsisdn = (String) context.get(ORIGINAL_MSISDN_KEY);
        if (originalMsisdn != null && originalMsisdn.trim().length() > 0)
        {
            final Msisdn msisdnObj = MsisdnSupport.getMsisdn(context, originalMsisdn);

            addOriginalMsisdn(context, msisdnGroupId, transientHome, subscriber, msisdnObj);
        }

        final Context subContext = context.createSubContext();
        subContext.setName("Filtered MSISDN Home");
        subContext.put(MsisdnHome.class, new SortingHome(transientHome));
        return subContext;
    }


    /**
     * Determines whether the original MSISDN should be added back to the transient MSISDN
     * home.
     *
     * @param context
     *            The operating context.
     * @param msisdnGroupId
     *            MSISDN group currently selected.
     * @param transientHome
     *            Transient MSISDN home.
     * @param subscriber
     *            Subscriber to own the MSISDN.
     * @param originalMsisdn
     *            Original MSISDN.
     * @throws HomeException
     *             Thrown if there are home-related problems.
     */
    private void addOriginalMsisdn(final Context context, final int msisdnGroupId, final Home transientHome,
        final Subscriber subscriber, final Msisdn originalMsisdn) throws HomeException
    {
        boolean addMsisdn = true;
        final int mode = context.getInt("MODE", DISPLAY_MODE);
        if (originalMsisdn == null || originalMsisdn.getGroup() != msisdnGroupId)
        {
            addMsisdn = false;
        }

        // don't create unless it's not in the transient home
        else if (transientHome.find(context, originalMsisdn) != null)
        {
            addMsisdn = false;
        }
        else if (mode == CREATE_MODE)
        {
            /*
             * [Cindy] 2007-11-27: Do not add MSISDN if in create mode and
             * technology/subscriber type don't match.
             */
            addMsisdn = SafetyUtil.safeEquals(originalMsisdn.getTechnology(), subscriber.getTechnology())
                && SafetyUtil.safeEquals(originalMsisdn.getSubscriberType(), subscriber.getSubscriberType())
                && SafetyUtil.safeEquals(originalMsisdn.getState(), MsisdnStateEnum.AVAILABLE);
        }

        if (addMsisdn)
        {
            transientHome.create(context, originalMsisdn);
        }

    }


    /**
     * Display the web control for drop-down MSISDN selection.
     *
     * @param context
     *            The operating context.
     * @param msisdnGroupId
     *            Current MSISDN group.
     * @param msisdn
     *            Currently selected MSISDN.
     * @param out
     *            The output print writer.
     * @param name
     *            Name of the field.
     * @param object
     *            Object to be displayed.
     */
    protected void displayMsisdnSelectionWebControl(final Context context, final int msisdnGroupId,
        final String msisdn, final PrintWriter out, final String name, final Object object)
    {
        Object displayedObject = object;

        /*
         * Get the current starting position and incr by WINDOW_SIZE for next time.
         */
        final HttpServletRequest req = (HttpServletRequest) context.get(HttpServletRequest.class);
        final String startName = name + START_INDEX_FIELD_NAME;
        final int start;
        if (req.getParameter(startName) == null || displayedObject == null)
        {
            start = 1;
        }
        else
        {
            start = Integer.parseInt(req.getParameter(startName));
        }
        out.println("<input type=\"hidden\" name=\"" + WebAgents.rewriteName(context, startName) + "\" value=\""
            + start + "\"/>");

        Context subContext = null;
        try
        {
            subContext = wrapMsisdnContext(context, msisdnGroupId, msisdn, start);
        }
        catch (final HomeException exception)
        {
            if (LogSupport.isDebugEnabled(context))
            {
                final StringBuilder sb = new StringBuilder();
                sb.append(exception.getClass().getSimpleName());
                sb.append(" caught in ");
                sb.append("AvailMsisdnWebControl.displayMsisdnSelectionWebControl(): ");
                if (exception.getMessage() != null)
                {
                    sb.append(exception.getMessage());
                }
                LogSupport.debug(context, this, sb.toString(), exception);
            }
            throw new IllegalStateException("Cannot create temporary MSISDN home", exception);
        }
        if (displayedObject == null && !this.optional_)
        {
            displayedObject = getFirstAvailableMsisdn(subContext);
        }

        // only display a limited number of MSISDN
        this.msisdnWebControl_.toWeb(subContext, out, name + MSISDN_FIELD_NAME, displayedObject);

        // add refresh button to get the next batch of MSISDNs
        out.println("<input type=\"button\" onclick=\"setFieldValue('" + WebAgents.rewriteName(context, startName)
            + "','" + (start + WINDOW_SIZE) + "');autoPreview('" + WebAgents.getDomain(context) + "', event)\" name=\""
            + WebAgents.rewriteName(context, (name + ".more")) + "\" value=\"more\"/>");
    }


    /**
     * Display the web control for editable MSISDN.
     *
     * @param context
     *            The operating context.
     * @param currentMsisdnGroup
     *            Current MSISDN group.
     * @param currentMsisdn
     *            Currently selected MSISDN.
     * @param out
     *            The output print writer.
     * @param name
     *            Name of the field.
     * @param object
     *            Object to be displayed.
     */
    protected void displayEditableWebControl(final Context context, final int currentMsisdnGroup,
        final String currentMsisdn, final PrintWriter out, final String name, final Object object)
    {
        Object displayedObject = object;
        final Context subContext = wrapMsisdnGroupContext(context);
        final Home msisdnGroupHome = (Home) subContext.get(MsisdnGroupHome.class);

        int msisdnGroup = currentMsisdnGroup;
        if (msisdnGroup == AbstractSubscriber.DEFAULT_MSISDNGROUP)
        {
            msisdnGroup = getMsisdnGroupId(subContext, currentMsisdn);
        }

        CountingVisitor visitor = null;
        try
        {
            visitor = (CountingVisitor) msisdnGroupHome.forEach(subContext, new CountingVisitor());
        }
        catch (final HomeException exception)
        {
            new DebugLogMsg(this, HomeException.class.getSimpleName()
                + " occurred in displayEditableMsisdn() while obtaining a count of available MSISDN groups: "
                + exception.getMessage(), exception).log(context);
        }
        if (visitor != null && visitor.getCount() == 0)
        {
            /*
             * Manda - Reset the values when technology changes and that technology has no
             * Msisdn groups existing. Fix for TT # 6101240128.
             */
            msisdnGroup = CUSTOM_GROUP;
            displayedObject = "";
        }

        if (isDisplayFreeformOnly(subContext))
        {
            // don't display group selection if only free-form is allowed
            this.freeformMsisdnWebControl_.toWeb(context, out, name + MSISDN_FIELD_NAME, displayedObject);
        }
        else
        {
            this.groupWebControl_.toWeb(subContext, out, name + MSISDN_GROUP_FIELD_NAME, Integer.valueOf(msisdnGroup));

            // custom option selected
            if (msisdnGroup == CUSTOM_GROUP)
            {
                this.freeformMsisdnWebControl_.toWeb(context, out, name + MSISDN_FIELD_NAME, displayedObject);
            }
            else
            {
                displayMsisdnSelectionWebControl(subContext, msisdnGroup, currentMsisdn, out, name, displayedObject);
            }
        }
    }


    /**
     * Returns the MSISDN group ID to be used.
     *
     * @param context
     *            The operating context.
     * @param currentMsisdnId
     *            The current MSISDN group ID.
     * @return The MSISDN group ID to be used.
     */
    protected int getMsisdnGroupId(final Context context, final String currentMsisdnId)
    {
        final Home msisdnHome = (Home) context.get(MsisdnHome.class);
        int msisdnGroup;
        Msisdn currentMsisdn = null;

        if (currentMsisdnId != null && currentMsisdnId.trim().length() > 0)
        {
            try
            {
                currentMsisdn = (Msisdn) msisdnHome.find(context, currentMsisdnId);
            }
            catch (final HomeException exception)
            {
                new MajorLogMsg(this, "Failed to look-up MSISDN " + currentMsisdnId + " information.", exception)
                    .log(context);
            }
        }

        if (currentMsisdn != null)
        {
            msisdnGroup = currentMsisdn.getGroup();
        }
        else
        {
            /*
             * WARNING - 2004-02-02 - We want the "regular" type to be selected by
             * default. It has a value of zero by default. We need a safer way of
             * specifying this.
             */
            msisdnGroup = 0;
        }

        final Home msisdnGroupHome = (Home) context.get(MsisdnGroupHome.class);
        try
        {
            MsisdnGroup group = (MsisdnGroup) msisdnGroupHome.find(context, Integer.valueOf(msisdnGroup));
            if (group == null)
            {
                final Collection collection = new SortingHome(msisdnGroupHome).selectAll(context);
                if (collection != null)
                {
                    group = (MsisdnGroup) collection.iterator().next();
                }
            }
            if (group != null)
            {
                msisdnGroup = group.getId();
            }
        }
        catch (final HomeException exception)
        {
            if (LogSupport.isDebugEnabled(context))
            {
                final StringBuilder sb = new StringBuilder();
                sb.append(exception.getClass().getSimpleName());
                sb.append(" caught in ");
                sb.append("AvailMsisdnWebControl.getMsisdnGroupId(): ");
                if (exception.getMessage() != null)
                {
                    sb.append(exception.getMessage());
                }
                LogSupport.debug(context, this, sb.toString(), exception);
            }

        }

        return msisdnGroup;
    }


    /**
     * {@inheritDoc}
     */
    public synchronized void toWeb(final Context ctx, final PrintWriter out, final String name, final Object obj)
    {
        String currentMsisdn = (String) ctx.get(AvailMsisdnWebControl.MSISDN_KEY, "");
        final int msidnGroup = ((Number) ctx.get(AvailMsisdnWebControl.MSISDN_GROUP_KEY, CUSTOM_GROUP)).intValue();
        final int mode = ctx.getInt("MODE", DISPLAY_MODE);

        if (mode == CREATE_MODE)
        {
            // Don't use the existing MSISDN if in create mode.
            currentMsisdn = "";
            displayEditableWebControl(ctx, msidnGroup, currentMsisdn, out, name, obj);
        }
        else if (mode == EDIT_MODE)
        {
            if (currentMsisdn == null || SafetyUtil.safeEquals(currentMsisdn, ""))
            {
                currentMsisdn = (String) ctx.get(AvailMsisdnWebControl.ORIGINAL_MSISDN_KEY, "");
            }
            displayEditableWebControl(ctx, msidnGroup, currentMsisdn, out, name, obj);
        }
        else
        {
            this.freeformMsisdnWebControl_.toWeb(ctx, out, name + MSISDN_FIELD_NAME, obj);
        }
    }


    /**
     * Returns the first available msisdn.
     *
     * @param context
     *            The operating context.
     * @return The first available MSISDN.
     */
    protected Object getFirstAvailableMsisdn(final Context context)
    {
        Msisdn msisdn = null;
        String result = null;
        try
        {
            final Home msisdnHome = (Home) context.get(MsisdnHome.class);
            // TODO [Cindy]: find a better way to get a MSISDN.
            final Collection msisdns = msisdnHome.selectAll(context);
            if (msisdns != null && msisdns.size() > 0)
            {
                msisdn = (Msisdn) msisdns.iterator().next();
            }
        }
        catch (final UnsupportedOperationException exception)
        {
            new DebugLogMsg(this, UnsupportedOperationException.class.getSimpleName()
                + " caught in getFirstAvailableMsisdn(): " + exception.getMessage(), exception).log(context);
        }
        catch (final HomeException exception)
        {
            new DebugLogMsg(this, HomeException.class.getSimpleName() + " caught in getFirstAvailableMsisdn(): "
                + exception.getMessage(), exception).log(context);
        }

        if (msisdn != null)
        {
            result = msisdn.getMsisdn();
        }
        return result;
    }


    /**
     * {@inheritDoc}
     */
    public synchronized Object fromWeb(final Context ctx, final ServletRequest req, final String name)
    {
        int selectedMsisdnGroup = CUSTOM_GROUP;
        if (!isDisplayFreeformOnly(ctx))
        {
            final Number selectedMsisdnGroupObj = (Number) this.groupWebControl_.fromWeb(ctx, req, name
                + MSISDN_GROUP_FIELD_NAME);
            if (selectedMsisdnGroupObj != null)
            {
                selectedMsisdnGroup = selectedMsisdnGroupObj.intValue();
            }
        }

        String selectedMsisdn = "";
        try
        {
            selectedMsisdn = (String) this.msisdnWebControl_.fromWeb(ctx, req, name + MSISDN_FIELD_NAME);
        }
        catch (final NullPointerException exception)
        {
            // AbstractKeyWebControl throws a NPE when msisdn field is null (i.e. you
            // don't have any regular msisdns)
            new DebugLogMsg(this, "NullPointerException caught in fromWeb()", exception).log(ctx);
        }

        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "selected group: " + selectedMsisdnGroup + " selected msisdn: " + selectedMsisdn,
                null).log(ctx);
        }
        ctx.put(AvailMsisdnWebControl.MSISDN_GROUP_KEY, Integer.valueOf(selectedMsisdnGroup));
        ctx.put(AvailMsisdnWebControl.MSISDN_KEY, selectedMsisdn);
        return selectedMsisdn;
    }


    /**
     * Retrieves the value of <code>groupWebControl</code>.
     *
     * @return The value of <code>groupWebControl</code>.
     */
    protected WebControl getGroupWebControl()
    {
        return this.groupWebControl_;
    }


    /**
     * Sets the value of <code>groupWebControl</code>.
     *
     * @param groupWebControl
     *            The value of <code>groupWebControl</code> to set.
     */
    protected void setGroupWebControl(final WebControl groupWebControl)
    {
        this.groupWebControl_ = groupWebControl;
    }


    /**
     * Retrieves the value of <code>msisdnWebControl</code>.
     *
     * @return The value of <code>msisdnWebControl</code>.
     */
    protected WebControl getMsisdnWebControl()
    {
        return this.msisdnWebControl_;
    }


    /**
     * Sets the value of <code>msisdnWebControl</code>.
     *
     * @param msisdnWebControl
     *            The value of <code>msisdnWebControl</code> to set.
     */
    protected void setMsisdnWebControl(final WebControl msisdnWebControl)
    {
        this.msisdnWebControl_ = msisdnWebControl;
    }


    /**
     * Retrieves the value of <code>freeformMsisdnWebControl</code>.
     *
     * @return The value of <code>freeformMsisdnWebControl</code>.
     */
    protected WebControl getFreeformMsisdnWebControl()
    {
        return this.freeformMsisdnWebControl_;
    }


    /**
     * Sets the value of <code>freeformMsisdnWebControl</code>.
     *
     * @param freeformMsisdnWebControl
     *            The value of <code>freeformMsisdnWebControl</code> to set.
     */
    protected void setFreeformMsisdnWebControl(final WebControl freeformMsisdnWebControl)
    {
        this.freeformMsisdnWebControl_ = freeformMsisdnWebControl;
    }


    /**
     * Retrieves the value of <code>isOptional</code>.
     *
     * @return The value of <code>isOptional</code>.
     */
    protected boolean isOptional()
    {
        return this.optional_;
    }

    /**
     * Web control for MSISDN group.
     */
    private WebControl groupWebControl_ = new MsisdnGroupKeyWebControl(true);

    /**
     * Web control for MSISDN.
     */
    private WebControl msisdnWebControl_;

    /**
     * Web control for custom MSISDN.
     */
    private WebControl freeformMsisdnWebControl_ = new TextFieldWebControl(FREEFORM_MSISDN_FIELD_WIDTH);

    /**
     * Whether this MSISDN is an optional field or not.
     */
    private boolean optional_;

}
