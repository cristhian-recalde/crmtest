package com.trilogy.app.crm.clean;

import java.util.Calendar;

import com.trilogy.framework.lifecycle.LifecycleException;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.LT;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.BalanceBundleUsageSummaryHome;
import com.trilogy.app.crm.bean.BalanceBundleUsageSummaryXInfo;
import com.trilogy.app.crm.lifecycle.LifecycleAgentScheduledTask;

/**
 * @author chandrachud.ingale
 * @since 9.9
 * 
 * ContextAgent to remove out-dated BalanceBundleUsageSummary records from database CleanUpAgent takes two parameters
 * SPID (parameter1) and NumberOfDays (parameter2)
 * NoOfDays is parameter wherein entries older than those NoOfDays from today would be cleaned up.
 */
public class BalanceBundleUsageSummaryCleanUpAgent extends LifecycleAgentScheduledTask
{
    private static final long serialVersionUID = 1L;
    final static long         DAY_IN_MILLS     = 24 * 60 * 60 * 1000L;


    public BalanceBundleUsageSummaryCleanUpAgent(Context ctx, final String agentId) throws AgentException
    {
        super(ctx, agentId);
    }


    /**
     * {@inheritDoc}
     */
    protected void start(Context ctx) throws LifecycleException, HomeException
    {
        final Home balanceBundleUsageSummaryHome = (Home) ctx.get(BalanceBundleUsageSummaryHome.class);
        if (balanceBundleUsageSummaryHome == null)
        {
            throw new LifecycleException("System error: BalanceBundleUsageSummaryHome not found in context");
        }

        int spid = getParameter1(ctx, Integer.class);

        Calendar cal = Calendar.getInstance();
        cal.clear(Calendar.HOUR_OF_DAY);
        cal.clear(Calendar.AM_PM);
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);

        long expiryDate = cal.getTimeInMillis() - (getParameter2(ctx, Integer.class) * DAY_IN_MILLS);

        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "Starting BalanceBundleUsageSummaryCleanUpAgent for SPID : " + spid
                    + " and ExpiryDate : " + expiryDate).log(ctx);
        }

        try
        {
            And whereClause = new And().add(new EQ(BalanceBundleUsageSummaryXInfo.SPID, spid)).add(
                    new LT(BalanceBundleUsageSummaryXInfo.TRANS_DATE, expiryDate));
            balanceBundleUsageSummaryHome.removeAll(ctx, whereClause);
        }
        catch (Exception e)
        {
            LogSupport.major(ctx, this, "Error deleting BalanceBundleUsageSummary entries.", e);
        }

        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "Finised executing BalanceBundleUsageSummaryCleanUpAgent for SPID : " + spid
                    + " and ExpiryDate : " + expiryDate).log(ctx);
        }
    }

}
