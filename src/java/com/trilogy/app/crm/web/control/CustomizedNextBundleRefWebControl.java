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
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.ServletRequest;

import com.trilogy.app.crm.CoreCrmConstants;
import com.trilogy.app.crm.bean.ui.BundleProfile;
import com.trilogy.app.crm.bean.ui.BundleProfileHome;
import com.trilogy.app.crm.bean.ui.BundleProfileIdentitySupport;
import com.trilogy.app.crm.bean.ui.BundleProfileKeyWebControl;
import com.trilogy.app.crm.bean.ui.BundleProfileXInfo;
import com.trilogy.app.crm.bundle.FlexTypeEnum;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.support.IdentitySupport;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.SelectWebControl;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * @author bpandey
 * 
 *         Customized Web Control for Flex bundle next bundle reference.
 */
public class CustomizedNextBundleRefWebControl extends SelectWebControl
{

    private static final String SELF = "SELF";
    private final static String NONE = "NONE";
    private static BundleProfileKeyWebControl keyWebControl_ = new BundleProfileKeyWebControl();


    public CustomizedNextBundleRefWebControl()
    {
        super(new ArrayList<BundleProfile>());
    }


    public String getKey(Context ctx, Object obj)
    {
        if (obj instanceof BundleProfile)
        {
            IdentitySupport id = BundleProfileIdentitySupport.instance();
            return id.toStringID(id.ID(obj));
        }
        return obj.toString();
    }


    public String getDesc(Context ctx, Object obj)
    {
        if (obj instanceof BundleProfile)
        {
            return keyWebControl_.getDesc(ctx, obj);
        }
        if (obj instanceof Long)
        {
            Long nbrBundleID = (Long) obj;
            BundleProfile beanBundle = (BundleProfile) ctx.get(AbstractWebControl.BEAN);
            if (nbrBundleID == CoreCrmConstants.NONE_BUNDLE_ID)
            {
                return NONE;
            }
            if (nbrBundleID == beanBundle.getBundleId())
            {
                return SELF;
            }
            Home home = (Home) ctx.get(BundleProfileHome.class);
            try
            {
                BundleProfile nbr = (BundleProfile) home.find(ctx, nbrBundleID);
                if (nbr != null)
                {
                    return keyWebControl_.getDesc(ctx, nbr);
                }
            }
            catch (Exception e)
            {
                LogSupport.debug(ctx, this, "Failed to get bundle id : " + nbrBundleID, e);
            }
        }
        return obj.toString();
    }


    public Collection<BundleProfile> getBundleIdList(Context ctx)
    {
        List<BundleProfile> ret = new ArrayList<BundleProfile>();
        Home home = (Home) ctx.get(BundleProfileHome.class);
        try
        {
            for (Object o : home.where(ctx, getFilter(ctx)).selectAll())
            {
                BundleProfile b = (BundleProfile) o;
                ret.add(b);
            }
        }
        catch (Exception e)
        {
            LogSupport.minor(ctx, this, "Failed to get bundle id list.", e);
        }
        return ret;
    }


    public Object fromWeb(Context ctx, ServletRequest req, String name) throws NullPointerException
    {
        String val = (String) super.fromWeb(ctx, req, name);
        IdentitySupport identitySupport = BundleProfileIdentitySupport.instance();
        return identitySupport.fromStringID(val);
    }


    /**
     * @param ctx
     * @param out
     * @param name
     * @param obj
     */
    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
    {
        int mode = ctx.getInt("MODE", DISPLAY_MODE);
        MessageMgr mmgr = new MessageMgr(ctx, this);
        StringBuffer optionsBuf = new StringBuffer(512);
        setCollection(getBundleIdList(ctx));
        switch (mode)
        {
        case EDIT_MODE:
        case CREATE_MODE:
            buildOptions(ctx, optionsBuf, obj);
            out.print("<select id=\"" + name + "\" name=\"" + name + "\" size=\"" + listSize_ + "\"");
            if (autoPreview_)
            {
                out.print(" onChange=\"autoPreview('" + WebAgents.getDomain(ctx) + "', event)\"");
            }
            out.print(">");
            out.print(optionsBuf.toString());
            out.println("</select>");
            break;
        case DISPLAY_MODE:
        default:
            // change logic to fetch bundle and then display its details
            String desc = getDesc(ctx, obj);
            optionsBuf.append(mmgr.get(desc, desc));
            out.print(optionsBuf.toString());
        }
    }


    private int buildOptions(Context ctx, StringBuffer optionsBuf, Object obj)
    {
        BundleProfile beanBundle = (BundleProfile) ctx.get(AbstractWebControl.BEAN);
        int size = 1;
        // add NONE
        addOption(ctx, optionsBuf, String.valueOf(CoreCrmConstants.NONE_BUNDLE_ID), NONE,
                beanBundle.getNextBundleRef() == CoreCrmConstants.NONE_BUNDLE_ID);
        boolean displaySelf = beanBundle.getFlexType() == FlexTypeEnum.SECONDARY;
        boolean selfFound = false;
        // builds a buffer with all the options
        for (Object element : getCollection(ctx))
        {
            BundleProfile nbrBundle = (BundleProfile) element;
            String key = getKey(ctx, element);
            size++;
            String desc = null;
            if (displaySelf && beanBundle.getBundleId() == nbrBundle.getBundleId())
            {
                desc = SELF;
                selfFound = true;
            }
            else
            {
                desc = getDesc(ctx, element);
            }
            addOption(ctx, optionsBuf, key, desc, obj != null && nbrBundle.getBundleId() == (Long) obj);
        }
        if (displaySelf && !selfFound)
        {
            addOption(ctx, optionsBuf, String.valueOf(CoreCrmConstants.SELF_BUNDLE_ID), SELF,
                    beanBundle.getNextBundleRef() == CoreCrmConstants.SELF_BUNDLE_ID);
            size++;
        }
        return size;
    }


    private void addOption(Context ctx, StringBuffer optionsBuf, String key, String desc, boolean isSelected)
    {
        MessageMgr mmgr = new MessageMgr(ctx, this);
        optionsBuf.append("<option value=\"");
        optionsBuf.append(key);
        optionsBuf.append('\"');
        if (isSelected)
        {
            optionsBuf.append(" selected=\"selected\" ");
        }
        optionsBuf.append('>');
        optionsBuf.append(mmgr.get(desc, desc));
        optionsBuf.append("</option>");
    }


    private Predicate getFilter(final Context ctx)
    {
        BundleProfile bundle = (BundleProfile) ctx.get(AbstractWebControl.BEAN);
        // display secondary bundles of same segment and spid.
        And filter = new And().add(new EQ(BundleProfileXInfo.FLEX, true))
                .add(new EQ(BundleProfileXInfo.FLEX_TYPE, FlexTypeEnum.SECONDARY))
                .add(new EQ(BundleProfileXInfo.SPID, bundle.getSpid()))
                .add(new EQ(BundleProfileXInfo.SEGMENT, bundle.getSegment()));
        return filter;
    }
}
