package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;

import javax.servlet.ServletRequest;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.EnumWebControl;
import com.trilogy.framework.xhome.webcontrol.PrimitiveWebControl;
import com.trilogy.framework.xhome.xenum.Enum;
import com.trilogy.framework.xhome.xenum.EnumCollection;

import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.xhome.CustomEnumCollection;

/*
 * @author lzou
 * @date   Dec 18, 2003
 *
 * Designed to disallow any users to see or set subscriber's state to MOVED via
 * GUI. "MOVED" state describers that a subscriber is not used any more and a
 * new subscriber with all same contents except for BAN is created in database.
 * This happens during "Switch Subscriber between Accounts" process.
 *
 * A subscriber with "MOVED" state is treated the same as "DEACTIVATE" in database
 * therefore will be removed when SubscriberCleanUp cron agent get executed.
 */

public class SubscriberStateChangeLimitWebControl
            extends PrimitiveWebControl

{
    /**
     *  The extent of Enum values to choose from.
     **/
    protected  EnumCollection enum_;

    /**
     *  Flag indicating if webpage gets submitted as "Preview" mode
     *  whenever user changes select's option value
     **/
    protected final boolean autoPreview_;

   /**
	 * @param stateEnum
	 * @param autoPreview
	 */
	public  SubscriberStateChangeLimitWebControl( final EnumCollection stateEnum, final boolean autoPreview )
	{
        enum_ = stateEnum;
        autoPreview_ = autoPreview ;
	}

    public void toWeb(final Context ctx, final PrintWriter out, final String name, final Object obj)
    {
        final Enum enumeration   = (Enum) obj;
        EnumCollection newEnum = null;

        newEnum = new CustomEnumCollection(
                  SubscriberStateEnum.ACTIVE,
                  SubscriberStateEnum.PENDING,
                  SubscriberStateEnum.INACTIVE,
                  SubscriberStateEnum.SUSPENDED);

        new EnumWebControl(newEnum, true).toWeb(ctx, out, name, enumeration);
    }

    public Object fromWeb(final Context ctx, final ServletRequest req, final String name)
          throws NullPointerException
    {
        return new EnumWebControl(enum_, true).fromWeb(ctx, req, name);
    }
}
