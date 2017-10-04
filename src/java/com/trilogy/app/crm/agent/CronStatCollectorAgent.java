/*
 * Created on Jul 26, 2005
 */
package com.trilogy.app.crm.agent;

import java.util.Date;

import com.trilogy.framework.core.cron.TaskEntry;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.app.crm.bean.*;
/**
 * @author rattapattu
 */
public class CronStatCollectorAgent implements ContextAgent
{
    private ContextAgent delegate;
    public ContextAgent getDelegate()
    {
        return delegate;
    }

    private static long MIN_THRESHOLD = 1000*60*15;
    
    public CronStatCollectorAgent(ContextAgent delegate)
    {
        this.delegate = delegate;
    }

    public void execute(Context ctx) throws AgentException
    {
        Home home = (Home)ctx.get(CronStatHome.class);
        CronStat stat = getCronStat(ctx);
        stat.setStartTime(new Date());
        
        delegate.execute(ctx);
        
        stat.setEndTime(new Date());
        stat.setTimeTaken(stat.getEndTime().getTime() -stat.getStartTime().getTime());       
        
        // We record only events that are more than 15 mins to avoid capturing the 5 or 10 min OM stuff.
        // in case one of those tasks takes longer than that then we will be catching them.
        if(stat.getTimeTaken()>MIN_THRESHOLD)
        {    
	        try
	        {
	        	home.store(ctx,stat);
	        }
	        catch(Exception e)
	        {
	            try
	            {
	                home.create(stat);
	            }
	            catch (Exception e1)
	            {
	                //e1.printStackTrace();
	            }
	            //e.printStackTrace();
	        }
        }
    }
    
    private CronStat getCronStat(Context ctx)
    {
        TaskEntry taskEntry = (TaskEntry) ctx.get(TaskEntry.class);
        
        Home home = (Home)ctx.get(CronStatHome.class);
        try
        {
            CronStat stat = (CronStat) home.find(ctx,taskEntry.getName());
            if(stat == null)
            {
                stat = new CronStat();
                stat.setAgentName(taskEntry.getName());
                stat.setAgentDescription(taskEntry.getDescription());
            }
            return stat;
        }
        catch(Exception e)
        {
            CronStat stat = new CronStat();
            stat.setAgentName(taskEntry.getName());
            stat.setAgentDescription(taskEntry.getDescription());
            return stat;            
        }
    }

}
