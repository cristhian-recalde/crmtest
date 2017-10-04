/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee, no unauthorised use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the licence agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */

package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;

import javax.servlet.ServletRequest;

import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.EnumIndexWebControl;
import com.trilogy.framework.xhome.webcontrol.PrimitiveWebControl;
import com.trilogy.framework.xhome.xenum.Enum;
import com.trilogy.framework.xhome.xenum.EnumCollection;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.AuxiliaryServiceHome;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.HomeZoneFieldsBean;
import com.trilogy.app.crm.homezone.HomeZonePriorityEnum;
import com.trilogy.app.crm.xhome.CustomEnumCollection;

/**
 * @author pkulkarni
 *
 * This webcontrol is used to show the homezone priorities dynamically in the
 * subscriber rating tab the webcontrol shows the drop down for passed/derived
 * number(Max home zones) as a drop down list of 1-Passed Number values Window -
 * Uses EnumIndexWebControl's toWeb and fromWeb
 */
public class HomeZonePriorityWebControl extends PrimitiveWebControl
{

    /**
     * The extent of Enum values to choose from.
     */
    protected EnumCollection enum_;

    /**
     * Flag indicating if webpage gets submitted as "Preview" mode whenever user
     * changes select's option value
     */
    protected final boolean autoPreview_;

    public HomeZonePriorityWebControl()
    {
        autoPreview_ = false;
    }

    public HomeZonePriorityWebControl(final boolean autoPreview)
    {
        autoPreview_ = autoPreview;
    }

    public void toWeb(final Context ctx, final PrintWriter out, final String name, final Object obj)
    {
        AuxiliaryService actualService = null;
        int maxZonesAllowed = 0;
        final HomeZoneFieldsBean hzBean = (HomeZoneFieldsBean) ctx
                .get(AbstractWebControl.BEAN);

        try
        {
            actualService = (AuxiliaryService) ((Home) ctx
                    .get(AuxiliaryServiceHome.class)).find(ctx, XBeans
                    .toObject(hzBean.getSelectionIdentifier()));

            final int spID = actualService.getSpid();

            final Home home = (Home) ctx.get(CRMSpidHome.class);
            final CRMSpid provider = (CRMSpid) home.find(ctx, Integer.valueOf(spID));
            maxZonesAllowed = provider.getMaxZones();
        }
        catch (final Exception e)
        {
            new MinorLogMsg(this, "No AuxiliaryService [Identifier="
                    + hzBean.getSelectionIdentifier() + "] found in Database",
                    e).log(ctx);
        }

        final Enum[] priorities = new Enum[maxZonesAllowed];
        for (int i = 0; i < maxZonesAllowed; i++)
        {
            //if the index will be 0 then description will be 1
            priorities[i] = new HomeZonePriorityEnum((short) i, String
                    .valueOf(i + 1));
        }
        final EnumCollection newCollection = new CustomEnumCollection(priorities);
        enum_ = newCollection;
        new EnumIndexWebControl(enum_, autoPreview_).toWeb(ctx, out, name, obj);
    }

    public Object fromWeb(final Context ctx, final ServletRequest req, final String name)
            throws NullPointerException
    {
        return new EnumIndexWebControl(enum_, autoPreview_).fromWeb(ctx, req,
                name);
    }
}
