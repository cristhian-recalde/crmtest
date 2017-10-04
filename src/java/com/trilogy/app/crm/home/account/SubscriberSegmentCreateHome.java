package com.trilogy.app.crm.home.account;

import java.util.Collection;

import com.trilogy.app.crm.CoreCrmConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.BillCycle;
import com.trilogy.app.crm.bean.BillCycleXInfo;
import com.trilogy.app.crm.support.CalendarSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.LT;
import com.trilogy.framework.xhome.elang.OrderBy;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.home.OrderByHome;
import com.trilogy.app.crm.bean.SubscriberSegment;

/***
 * The purpose of this class is to convert SegmentName in upper case.
 * 
 * @author chandrachud.ingale
 * @since
 */
public class SubscriberSegmentCreateHome extends HomeProxy
{

    private static final long serialVersionUID = 1L;

    public SubscriberSegmentCreateHome(final Context ctx, final Home delegate)
    {
        super(ctx, delegate);
    }


    @Override
    public Object create(final Context ctx, final Object obj) throws HomeException
    {
        SubscriberSegment subSeg = (SubscriberSegment) obj;
        subSeg.setSegmentName(subSeg.getSegmentName().toUpperCase());
        return super.create(ctx, subSeg);
    }
}
