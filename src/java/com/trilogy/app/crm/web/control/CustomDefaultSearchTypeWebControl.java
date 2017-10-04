package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.security.Principal;

import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.EnumWebControl;
import com.trilogy.framework.xhome.xenum.Enum;
import com.trilogy.framework.xhome.xenum.EnumCollection;
import com.trilogy.framework.xlog.log.MajorLogMsg;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.SearchTypeEnum;
import com.trilogy.app.crm.xhome.CustomEnumCollection;


public class CustomDefaultSearchTypeWebControl extends EnumWebControl

{

    private final static EnumCollection ACCOUNT_FIRST = new CustomEnumCollection(
        SearchTypeEnum.Account, SearchTypeEnum.Both);

    private final static EnumCollection SUBSCRIBER_FIRST = new CustomEnumCollection(
        SearchTypeEnum.Account, SearchTypeEnum.Both);

    private final static EnumCollection BOTH_FIRST = new CustomEnumCollection(
        SearchTypeEnum.Both, SearchTypeEnum.Account);


    public CustomDefaultSearchTypeWebControl(final EnumCollection enumeration, final boolean autoPreview)
    {
        super(enumeration, autoPreview);
    }


    /**
     *
     * {@inheritDoc}
     */
    @Override
    public void toWeb(final Context ctx, final PrintWriter out, final String name, Object obj)
    {
        final Context subCtx = ctx.createSubContext();
        // System.out.println(" to web called ......");

        final EnumCollection newCol;

        final Principal principal = (Principal) ctx.get(Principal.class);
        final User user = (User) principal;
        final Home spidHome = (Home) subCtx.get(CRMSpidHome.class);

        CRMSpid spid = null;
        try
        {
            spid = (CRMSpid) spidHome.find(subCtx, Integer.valueOf(user.getSpid()));

        }
        catch (final HomeException exception)
        {
            new MajorLogMsg(this, " HomeException thrown durring CMRSpid look-up from User.", exception).log(subCtx);

            return;
        }

        if (spid == null)
        {
            new MajorLogMsg(this, "Could not set Default search type -- no CRMSpid \"" + user.getSpid()
                + "\"found in home.", null).log(subCtx);

            return;
        }
        obj = spid.getDefaultSearchType();

        // create collection
        if (spid.getDefaultSearchType().equals(SearchTypeEnum.Both))
        {
            newCol = BOTH_FIRST;
        }
        else
        {
            newCol = ACCOUNT_FIRST;
        }

        new EnumWebControl(newCol, true).toWeb(ctx, out, name, obj);

    }

}
