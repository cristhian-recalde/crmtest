package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;

import javax.servlet.ServletRequest;

import com.trilogy.app.crm.dunning.DunningReportRecord;
import com.trilogy.app.crm.dunning.DunningReportRecordMatureStateEnum;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.EnumIndexWebControl;
import com.trilogy.framework.xhome.xenum.EnumCollection;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.util.snippet.context.ContextUtils;

public class DunningReportRecordMaturityStatusWebControl extends EnumIndexWebControl
{
    private EnumIndexWebControl pendingApprove;
    private EnumIndexWebControl accepted;


    public DunningReportRecordMaturityStatusWebControl()
    {
        super(null);
        pendingApprove = new EnumIndexWebControl(new EnumCollection(new com.redknee.framework.xhome.xenum.Enum[] {
                DunningReportRecordMatureStateEnum.PENDING, DunningReportRecordMatureStateEnum.APPROVED }));
        accepted = new EnumIndexWebControl(DunningReportRecordMatureStateEnum.COLLECTION);
    }


    @Override
    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
    {
        if(LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, this, "Received toWeb for : " + name + " OBJECT : "  + obj);
        }
        DunningReportRecord dunningReportRecord = ContextUtils.getBeanInContextByType(ctx, AbstractWebControl.BEAN,
                DunningReportRecord.class);
        if (dunningReportRecord != null)
        {
            switch (dunningReportRecord.getRecordMaturity())
            {
                case DunningReportRecordMatureStateEnum.PENDING_INDEX:
                    pendingApprove.toWeb(ctx, out, name, obj);
                    break;
                case DunningReportRecordMatureStateEnum.APPROVED_INDEX:
                    pendingApprove.toWeb(ctx, out, name, obj);
                    break;

                case DunningReportRecordMatureStateEnum.ACCEPTED_INDEX:
                    accepted.toWeb(ctx, out, name, obj);
                    break;
                    
                case DunningReportRecordMatureStateEnum.DISCARDED_INDEX:
                    accepted.toWeb(ctx, out, name, obj);
                    break;
            }
        }
    }


    @Override
    public void fromWeb(Context ctx, Object obj, ServletRequest req, String name)
    {
        if(LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, this, "Received fromWeb for : " + name + " OBJECT : "  + obj);
        }
        DunningReportRecord dunningReportRecord = ContextUtils.getBeanInContextByType(ctx, AbstractWebControl.BEAN,
                DunningReportRecord.class);
        if (dunningReportRecord != null)
        {
            switch (dunningReportRecord.getRecordMaturity())
            {
                case DunningReportRecordMatureStateEnum.PENDING_INDEX:
                    pendingApprove.fromWeb(ctx, obj, req, name);
                    break;
                case DunningReportRecordMatureStateEnum.APPROVED_INDEX:
                    pendingApprove.fromWeb(ctx, obj, req, name);
                    break;

                default:
                    accepted.fromWeb(ctx, obj, req, name);
                    break;
            }
        }
    }


    @Override
    public Object fromWeb(Context ctx, ServletRequest req, String name) throws NullPointerException
    {
        if(LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, this, "Received fromWeb for : " + name );
        }
        DunningReportRecord dunningReportRecord = ContextUtils.getBeanInContextByType(ctx, AbstractWebControl.BEAN,
                DunningReportRecord.class);
        if (dunningReportRecord != null)
        {
            switch (dunningReportRecord.getRecordMaturity())
            {
                case DunningReportRecordMatureStateEnum.PENDING_INDEX:
                    return pendingApprove.fromWeb(ctx, req, name);

                case DunningReportRecordMatureStateEnum.APPROVED_INDEX:
                    return pendingApprove.fromWeb(ctx, req, name);

                default:
                    return accepted.fromWeb(ctx, req, name);
            }
        }
        return super.fromWeb(ctx, req, name);
    }

}
