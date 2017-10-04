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

import javax.servlet.ServletRequest;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.support.IdentitySupport;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xhome.webcontrol.TextFieldWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.BirthdayPlan;
import com.trilogy.app.crm.bean.BirthdayPlanHome;
import com.trilogy.app.crm.bean.CallingGroupTypeEnum;
import com.trilogy.app.crm.bean.ClosedUserGroupTemplate;
import com.trilogy.app.crm.bean.ClosedUserGroupTemplateHome;
import com.trilogy.app.crm.bean.PersonalListPlan;
import com.trilogy.app.crm.bean.PersonalListPlanHome;
import com.trilogy.app.crm.support.AbstractIdentitySupport;


/**
 * Provides a custom WebControl for the calling group identifier property of the
 * AuxiliaryServices model.
 *
 * @author gary.anderson@redknee.com
 */
public
class CallingGroupLinkedWebControl
    implements WebControl
{
    /**
     * Creates a new CallingGroupLinkedWebControl.
     */
    protected CallingGroupLinkedWebControl()
    {
        plpWebControl_ = createPersonalListPlan();
        cugWebControl_ = createClosedUserGroup();
        bpWebControl_ = createBirthdayPlanWebControl();
    }


    /**
     * Creates a web control for the PersonalListPlan.
     *
     * @return A web control for the PersonalListPlan.
     */
    public static WebControl createPersonalListPlan()
    {
        final IdentitySupport support =
            new AbstractIdentitySupport()
            {
                @Override
                public String toStringID(Object bean)
                {
                    final PersonalListPlan plan = (PersonalListPlan)bean;
                    return plan.getName();
                }
            };

        final IdentitySupportWebControl supportWebControl =
            new IdentitySupportWebControl(
                new TextFieldWebControl(),
                support);

        final PrimaryKeyWebControl keyWebControl =
            new PrimaryKeyWebControl(
                supportWebControl,
                PersonalListPlanHome.class);

        // TODO - 2004-08-10 - Need to set the name of the PLP page when
        // available.
        final LinkedWebControl plpWebControl =
            new LinkedWebControl(
                keyWebControl,
                PLP_SCREEN);

        return plpWebControl;
    }


    /**
     * Creates a web control for the ClosedUserGroup.
     *
     * @return A web control for the ClosedUserGroup.
     */
    public static WebControl createClosedUserGroup()
    {
        final IdentitySupport support =
            new AbstractIdentitySupport()
            {
                @Override
                public String toStringID(Object bean)
                {
                    final ClosedUserGroupTemplate group = (ClosedUserGroupTemplate)bean;
                    return group.getName();
                }
            };

        final IdentitySupportWebControl supportWebControl =
            new IdentitySupportWebControl(
                new TextFieldWebControl(),
                support);

        final PrimaryKeyWebControl keyWebControl =
            new PrimaryKeyWebControl(
                supportWebControl,
                ClosedUserGroupTemplateHome.class);

        // TODO - 2004-08-10 - Need to set the name of the CUG page when
        // available.
        final LinkedWebControl cugWebControl =
            new LinkedWebControl(
                keyWebControl,
                CUG_SCREEN);

        return cugWebControl;
    }


    /**
     * Creates a web control for the ClosedUserGroup.
     *
     * @return A web control for the ClosedUserGroup.
     */
    public static WebControl createBirthdayPlanWebControl()
    {
        final IdentitySupport support =
            new AbstractIdentitySupport()
            {
                @Override
                public String toStringID(Object bean)
                {
                    final BirthdayPlan birthdayPlan = (BirthdayPlan) bean;
                    return birthdayPlan.getName();
                }
            };

        final IdentitySupportWebControl supportWebControl =
            new IdentitySupportWebControl(
                new TextFieldWebControl(),
                support);

        final PrimaryKeyWebControl keyWebControl =
            new PrimaryKeyWebControl(
                supportWebControl,
                BirthdayPlanHome.class);

        final LinkedWebControl bpWebControl =
            new LinkedWebControl(
                keyWebControl,
                BP_SCREEN);

        return bpWebControl;
    }


    // INHERIT
    public Object fromWeb(
        final Context context,
        final ServletRequest request,
        final String name)
    {
        return getWebControl(context).fromWeb(context, request, name);
    }


    // INHERIT
    public void fromWeb(
        final Context context,
        final Object obj,
        final ServletRequest request,
        final String name)
    {
        getWebControl(context).fromWeb(context, obj, request, name);
    }


    /**
     * Get the lone direct instance of this class.
     *
     * @return The lone direct instance of this class.
     */
    public static CallingGroupLinkedWebControl instance()
    {
        return instance_;
    }


    // INHERIT
    public void toWeb(
        final Context context,
        final PrintWriter out,
        final String name,
        final Object object)
    {
        try
        {
            getWebControl(context).toWeb(context, out, name, object);
        }
        catch (final Throwable t)
        {
            final HTMLExceptionListener listener = (HTMLExceptionListener) context.get(HTMLExceptionListener.class);

            if (listener != null)
            {
                listener.thrown(t);
            }
            
            new MinorLogMsg(
                this,
                "Failed to look-up calling group for link.",
                t).log(context);
        }
    }


    /**
     * Gets the appropriate WebControl for the bean in the context, that is the
     * PLP WebControl if the sevice (bean) refers to a PLP, and a CUG WebControl
     * otherwise.
     *
     * @param context The operating context.
     * @return The appropriate WebControl for the bean in the context.
     */
    private WebControl getWebControl(final Context context)
    {
        final Object bean = context.get(AbstractWebControl.BEAN);

        CallingGroupTypeEnum callingGroupType = ((com.redknee.app.crm.extension.auxiliaryservice.CallingGroupAuxSvcExtension)bean).getCallingGroupType();

        final WebControl control;
        if (callingGroupType != null)
        {
            if (callingGroupType == CallingGroupTypeEnum.PLP)
            {
                control = plpWebControl_;
            }
            else if (callingGroupType == CallingGroupTypeEnum.CUG || callingGroupType == CallingGroupTypeEnum.PCUG)
            {
                control = cugWebControl_;
            }
            else
            {
                control = bpWebControl_;
            }
        }
        else
        {
            control = bpWebControl_;
        }

        return control;
    }

    /**
     * The WebControl used to display and link to the PersonalListPlan.
     */
    private final WebControl plpWebControl_;

    /**
     * The WebControl used to display and link to the ClosedUserGroup.
     */
    private final WebControl cugWebControl_;

    /**
     * The WebControl used to display and link to the BithdayPlan.
     */
    private final WebControl bpWebControl_;

    /**
     * The lone direct instance of this class.
     */
    private final static CallingGroupLinkedWebControl instance_ = new CallingGroupLinkedWebControl();

    /**
     * The xmenu key for the PLP screen.
     */
    private static final String PLP_SCREEN = "appCRMConfigPersonalListPlans";

    /**
     * The xmenu key for the CUG screen.
     */
    private static final String CUG_SCREEN = "appCRMConfigCUGTemplate";

    /**
     * The xmenu key for the BP screen.
     */
    private static final String BP_SCREEN = "appCRMBirthdayPlan";

} // class
