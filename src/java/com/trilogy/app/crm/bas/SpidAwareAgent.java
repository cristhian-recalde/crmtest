package com.trilogy.app.crm.bas;

import java.util.Collection;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MajorLogMsg;

public abstract class SpidAwareAgent 
implements ContextAgent
{

 
	   
	public void execute(final Context ctx) throws AgentException 
	{
        
	    if (!isLicensed(ctx))
	    {
	        return; 
	    }
	    
		final Home home = (Home) ctx.get(CRMSpidHome.class);
		try 
		{
			Collection<CRMSpid> c = home.selectAll(ctx); 
		
			for (CRMSpid spid : c)
			{	
				processSpid(ctx, spid); 
			}
		} catch (HomeException e)
		{
			new MajorLogMsg(this, taskName + "Cron task was interrupted", e ).log(ctx);
			throw new AgentException( taskName + " Cron task was interrupted");
		}

		
	}
	
	
	public String getTaskName() {
		return taskName;
	}
	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}


	private String taskName;
	abstract public void processSpid(Context ctx, final CRMSpid spid);
	abstract public boolean isLicensed(Context ctx); 
}
