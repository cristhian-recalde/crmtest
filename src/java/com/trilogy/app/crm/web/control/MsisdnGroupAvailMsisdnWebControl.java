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

import com.trilogy.framework.xhome.beans.Identifiable;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.elang.Limit;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.elang.True;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.Homes;
import com.trilogy.framework.xhome.home.SortingHome;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.PrimitiveWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.MsisdnGroup;
import com.trilogy.app.crm.bean.MsisdnGroupHome;
import com.trilogy.app.crm.bean.MsisdnGroupKeyWebControl;
import com.trilogy.app.crm.bean.MsisdnGroupTransientHome;
import com.trilogy.app.crm.bean.MsisdnGroupXInfo;
import com.trilogy.app.crm.bean.MsisdnHome;
import com.trilogy.app.crm.bean.MsisdnKeyWebControl;
import com.trilogy.app.crm.bean.MsisdnStateEnum;
import com.trilogy.app.crm.bean.MsisdnTransientHome;
import com.trilogy.app.crm.bean.MsisdnXInfo;
import com.trilogy.app.crm.bean.PortingTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.account.SubscriptionType;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.MsisdnSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.technology.TechnologyEnum;


/**
 * WebControl for selecting MSISDN's and MSISDN Groups.
 *
 * @author paul.sperneac@redknee.com
 * @author cindy.wong@redknee.com
 * @author rpatel
 */
public class MsisdnGroupAvailMsisdnWebControl extends PrimitiveWebControl
{

    /**
     * Field name suffix of hidden start index field.
     */
    protected static final String START_INDEX_FIELD_NAME = ".start";

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

    private PropertyInfo msisdnGroupProperty_;

    private PropertyInfo banProperty_;

    private PropertyInfo spidProperty_;

    private PropertyInfo technologyProperty_;

    private PropertyInfo subscriberTypeProperty_;

    private PropertyInfo subscriptionTypeProperty_;


    /**
     * 
     * @param banProperty
     * @param spidProperty
     * @param subscriberTypeProperty
     * @param technologyProperty
     * @param subscriptionTypeProperty 
     * @param isOptional
     */
    public MsisdnGroupAvailMsisdnWebControl(
            final PropertyInfo msisdnGroupProperty,
            final PropertyInfo banProperty,
            final PropertyInfo spidProperty,
            final PropertyInfo subscriberTypeProperty,
            final PropertyInfo technologyProperty,
            final PropertyInfo subscriptionTypeProperty, 
            final boolean isOptional)
    {
        optional_ = isOptional;
        msisdnGroupProperty_ = msisdnGroupProperty;
        banProperty_ = banProperty;
        spidProperty_ = spidProperty;
        subscriberTypeProperty_ = subscriberTypeProperty;
        technologyProperty_ = technologyProperty;
        subscriptionTypeProperty_ = subscriptionTypeProperty;
        
        if (isOptional)
        {
            this.msisdnWebControl_ = new MsisdnKeyWebControl(1, false, "");
        }
        else
        {
            this.msisdnWebControl_ = new MsisdnKeyWebControl(1, false, false);
        }
    }

    /**
     * Looks up the technology to use.
     *
     * @param context
     *            The operating context.
     * @return The technology of the current subscriber, or return
     *         {@link TechnologyEnum#ANY} if none exists.
     */
    protected TechnologyEnum getTechnology(final Context context)
    {
        Object obj = context.get(AbstractWebControl.BEAN);

        Class beanClass = technologyProperty_.getBeanClass();
        if (beanClass.isInstance(obj))
        {
            return (TechnologyEnum) technologyProperty_.get(obj);
        }
        else
        {
            
            return TechnologyEnum.ANY;
        }
    }


    protected int getSpid(Context context)
    {
        Object obj = context.get(AbstractWebControl.BEAN);
        
        Class beanClass = spidProperty_.getBeanClass();
        if (beanClass.isInstance(obj))
        {
            return (Integer)spidProperty_.get(obj);
        }
        else
        {
            return Subscriber.DEFAULT_SPID;
        }
    }
    


    protected SubscriberTypeEnum getSubscriberType(Context context)
    {
        Object obj = context.get(AbstractWebControl.BEAN);
        
        Class beanClass = subscriberTypeProperty_.getBeanClass();
        if (beanClass.isInstance(obj))
        {
            return (SubscriberTypeEnum) subscriberTypeProperty_.get(obj);
        }
        else
        {
            return Subscriber.DEFAULT_SUBSCRIBERTYPE;
        }
    }
    
    protected String getBAN(Context context)
    {
        Object obj = context.get(AbstractWebControl.BEAN);
        if( isBANAvailable() )
        {
            Class beanClass = banProperty_.getBeanClass();
            if (beanClass.isInstance(obj))
            {
                return (String) banProperty_.get(obj);
            }
            else
            {
                return Subscriber.DEFAULT_BAN;
            }
        }
        else
        {
            return "";
        }
    }

    /**
     * Looks up the subscription type
     *
     * @param context
     *            The operating context.
     * @return The subscriptionTypeId of subscriber if defined, otherwise return 
     * @throws HomeException 
     */
    protected long getSubscriptionType(final Context context) throws HomeException
    {
        Object obj = context.get(AbstractWebControl.BEAN);

        if (subscriptionTypeProperty_ != null && subscriptionTypeProperty_.getBeanClass().isInstance(obj))
        {
            Long type = (Long)subscriptionTypeProperty_.get(obj);
            if (type != null)
            {
                return type;                
            }
        }
        return Subscriber.DEFAULT_SUBSCRIPTIONTYPE;
    }

    private boolean isBANAvailable()
    {
        return banProperty_ != null;
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
        final TechnologyEnum techEnum = getTechnology(context);
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
                        .where(context, new In(MsisdnGroupXInfo.TECHNOLOGY, selectableTechnologies));
            }
            Homes.copy(msisdnGroupHome, home);

            subContext.put(MsisdnGroupHome.class, home);
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
        final And and = new And();
        and.add(new EQ(MsisdnXInfo.GROUP, Integer.valueOf(msisdnGroupId)));
        and.add(new EQ(MsisdnXInfo.SPID, Integer.valueOf(getSpid(context))));
        and.add(new EQ(MsisdnXInfo.SUBSCRIBER_TYPE, getSubscriberType(context)));

        final Or or = new Or();
        or.add(new EQ(MsisdnXInfo.STATE, MsisdnStateEnum.AVAILABLE));

        /*
         * [Cindy] 2007-10-05: Allow MSISDNs in HELD state if it is held by this
         * subscriber.
         */
        if (isBANAvailable())
        {
            final And and2 = new And();
            and2.add(new EQ(MsisdnXInfo.STATE, MsisdnStateEnum.HELD));
            and2.add(new EQ(MsisdnXInfo.BAN, getBAN(context)));
            and2.add(new EQ(MsisdnXInfo.PORTING_TYPE, PortingTypeEnum.NONE));
            or.add(and2);
        }
        and.add(or);

        /*
         * [Cindy] 2007-09-10: Work around framework silently dropping offset in Limit
         * when composing the WHERE clause. When if finally supports Limit properly, the
         * below chunk can be replaced by Homes.copy().
         * 
         * [Ravi] 2008-09-06: DeployFramework branch 5_7_184 still doesn't resolve this issue.
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

            addOriginalMsisdn(context, msisdnGroupId, transientHome, msisdnObj);
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
     * @param originalMsisdn
     *            Original MSISDN.
     * @throws HomeException
     *             Thrown if there are home-related problems.
     */
    private void addOriginalMsisdn(final Context context, final int msisdnGroupId, final Home transientHome,
        final Msisdn originalMsisdn) throws HomeException
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
            addMsisdn = SafetyUtil.safeEquals(originalMsisdn.getTechnology(), getTechnology(context))
                && SafetyUtil.safeEquals(originalMsisdn.getSubscriberType(), getSubscriberType(context))
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
        this.msisdnWebControl_.toWeb(subContext, out, name, displayedObject);

        final int mode = subContext.getInt("MODE", DISPLAY_MODE);
        if (mode != DISPLAY_MODE)
        {
            // add refresh button to get the next batch of MSISDNs
            out.println("<input type=\"button\" onclick=\"setFieldValue('" + WebAgents.rewriteName(context, startName)
                + "','" + (start + WINDOW_SIZE) + "');autoPreview('" + WebAgents.getDomain(context) + "', event)\" name=\""
                + WebAgents.rewriteName(context, (name + ".more")) + "\" value=\"more\"/>");
        }
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

        // Filter the Msisdn Groups down to the acceptible list
        final Context subContext = wrapMsisdnGroupContext(context);

        
        int msisdnGroup = currentMsisdnGroup;
        if (msisdnGroup == Msisdn.DEFAULT_GROUP)
        {
            // If the msisdnGroup was not hinted to us (eg. fromWeb()), the try and retrieve the msisdn group for the currentMsisdn 
            msisdnGroup = getMsisdnGroupId(subContext, currentMsisdn);
        }

        
        
        // Don't need to see the group if in a read-only mode
        final int mode = subContext.getInt("MODE", DISPLAY_MODE);
        if (mode != DISPLAY_MODE)
        {
	        // Since MSISDN Group is a non-persisted value we don't want the toWeb() to attempt to set the field. We leave that for the fromWeb().
	        // TODO We might want to actually support persisting the MSISDN Group as you could then put some business logic around which group is 
	        // selected in subsequent fields of the bean.  To do so we would simply support a constructore where the PropertyInfo can be passed and
	        // replace null with that PropertyInfo.
	        Context grpWcCtx = subContext.createSubContext();
            grpWcCtx.setName(groupWebControl_.getClass().getName());
            grpWcCtx.put(AbstractWebControl.PROPERTY, null);
            
	        this.groupWebControl_.toWeb(grpWcCtx, out, getWebControlName(msisdnGroupProperty_, name), Integer.valueOf(msisdnGroup));
        }
        
        displayMsisdnSelectionWebControl(subContext, msisdnGroup, currentMsisdn, out, name, displayedObject);
    }


    /**
     * Returns the MSISDN group ID for the MSISDN provided.  If the Msisdn doesn't exist in our 
     * system, then we return the first MSISDN group. 
     *
     * @param context
     *            The operating context
     * @param currentMsisdnId
     *            The current MSISDN
     * @return The MSISDN group ID to be used
     */
    protected int getMsisdnGroupId(final Context context, final String currentMsisdnId)
    {
        final Home msisdnHome = (Home) context.get(MsisdnHome.class);
        Msisdn currentMsisdn = null;

        // Look up the MSISDN so that we can get the group
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

        int msisdnGroupId = Msisdn.DEFAULT_GROUP;
        if (currentMsisdn != null)
        {
            // we found a MSISDN so we can get a valid MSISDN Group ID
            return currentMsisdn.getGroup();
        }
        else
        {
            try
            {
                //try the spid default first
                CRMSpid spid = HomeSupportHelper.get(context).findBean(context,  CRMSpid.class, getSpid(context));
                Integer defaultMsisdnGroupId = spid.getDefaultSubTypeMsisdnGroupId(getSubscriptionType(context));
                if (defaultMsisdnGroupId != null)
                {
                    if (LogSupport.isDebugEnabled(context))
                    {
                        new DebugLogMsg(this, "Using default msisdn group = " + defaultMsisdnGroupId + ", taken from Spid DefaultSubTypeMsisdnGroups.", null).log(context);
                    }
                    return defaultMsisdnGroupId;
                }

                // didn't find a MSISDN so we should just return the first group                          
                MsisdnGroup group = HomeSupportHelper.get(context).findBean(context, MsisdnGroup.class,  True.instance());
                if (group != null)
                {
                    if (LogSupport.isDebugEnabled(context))
                    {
                        new DebugLogMsg(this, "No default msisdn group found in Spid, using the first group found = " + group, null).log(context);
                    }
                    return group.getId();                    
                }
                if (LogSupport.isDebugEnabled(context))
                {
                    new DebugLogMsg(this, "No MSISDN Groups are defined!", null).log(context);
                }
            }
            catch (final HomeException exception)
            {
                new MinorLogMsg(this, "Unable to calculate default MSISDN Group", exception).log(context);
    
            }
        }
        return msisdnGroupId;
    }
    
    
    protected String getWebControlName(PropertyInfo property, final String name)
    {
        StringBuilder newName = new StringBuilder(name);
        if (newName.lastIndexOf(".")+1 < newName.length())
        {
            newName.delete(newName.lastIndexOf(".")+1, newName.length());   
        }
        newName.append(property.getName());
        return newName.toString();
    }


    /**
     * {@inheritDoc}
     */
    public synchronized void toWeb(final Context ctx, final PrintWriter out, final String name, final Object obj)
    {
        final Object bean = ctx.get(AbstractWebControl.BEAN);
        String currentMsisdn = (String) ctx.get(MsisdnGroupAvailMsisdnWebControl.MSISDN_KEY, "");
        final int msidnGroup = ((Number) msisdnGroupProperty_.get(bean)).intValue();
        final int mode = ctx.getInt("MODE", DISPLAY_MODE);

        if (mode == CREATE_MODE)
        {
            // Don't use the existing MSISDN if in create mode.
            currentMsisdn = "";
            displayEditableWebControl(ctx, msidnGroup, currentMsisdn, out, name, obj);
        }
        //else if (mode == EDIT_MODE)
        else
        {
            if (currentMsisdn == null || SafetyUtil.safeEquals(currentMsisdn, ""))
            {
                currentMsisdn = (String) ctx.get(MsisdnGroupAvailMsisdnWebControl.ORIGINAL_MSISDN_KEY, "");
            }
            displayEditableWebControl(ctx, msidnGroup, currentMsisdn, out, name, obj);
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
        String selectedMsisdn = "";
        try
        {
            selectedMsisdn = (String) this.msisdnWebControl_.fromWeb(ctx, req, name);
        }
        catch (final NullPointerException exception)
        {
            // AbstractKeyWebControl throws a NPE when msisdn field is null (i.e. you
            // don't have any regular msisdns)
            // this is a valid scenario, no need to print the exception trace
            new InfoLogMsg(this, "We do not have any regular msisdns for  [" + name + "]", null).log(ctx);
            new DebugLogMsg(this, "AbstractKeyWebControl throws a NPE when msisdn field is null", null).log(ctx);
        }

        int selectedMsisdnGroup = -1;
        try
        {
            final Number selectedMsisdnGroupObj = (Number) this.groupWebControl_.fromWeb(ctx, req, getWebControlName(msisdnGroupProperty_, name));

            if (selectedMsisdnGroupObj != null)
            {
                selectedMsisdnGroup = selectedMsisdnGroupObj.intValue();
            }
        }
        catch (NullPointerException e)
        {
            // Catching NullPointerException in case field is hidden but MSISDN is not.
            if (selectedMsisdn==null || selectedMsisdn.isEmpty())
            {
                throw e;
            }
        }
        

        if (selectedMsisdnGroup == -1)
        {
            selectedMsisdnGroup = getMsisdnGroupId(ctx, selectedMsisdn);
        }

        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "selected group: " + selectedMsisdnGroup + " selected msisdn: " + selectedMsisdn,
                null).log(ctx);
        }
        
        final Object bean = ctx.get(AbstractWebControl.BEAN);
        msisdnGroupProperty_.set(bean, Integer.valueOf(selectedMsisdnGroup));
        // Hint the MSISDN to optimize subsquent attempts
        ctx.put(MsisdnGroupAvailMsisdnWebControl.MSISDN_KEY, selectedMsisdn);
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
     * Whether this MSISDN is an optional field or not.
     */
    private boolean optional_;

}
