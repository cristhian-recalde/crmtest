package com.trilogy.app.crm.dunning.visitor;

import java.util.Date;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.dunning.DunningProcessException;
import com.trilogy.app.crm.dunning.DunningProcessHelper;
import com.trilogy.app.crm.dunning.processor.DunningProcessor;
import com.trilogy.app.crm.dunning.processor.IgnoreSubscriberAgedDebtDunningProcessor;
import com.trilogy.app.crm.dunning.processor.PolicyBasedDunningProcessor;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.LogSupport;

public abstract class AccountVisitor implements Visitor
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	public AccountVisitor(Date runningDate)
	{
		runningDate_ = runningDate;
	}
	
	@Override
	public void visit(Context ctx, Object obj) throws AgentException,
			AbortVisitException {

        try
        {
            processAccount(ctx, obj);
        }
        catch (DunningProcessException e)
        {
            throw new AgentException(e);
        }
    
		
	}
	
	protected void processAccount(Context context,Object obj) throws DunningProcessException
	{
		final Account account = (Account)obj;
		DunningProcessor dunningProcessor = null; 
        //String processName = getProcessName();
        //boolean notificationProcess = processName.equals("Pre-Dunning Notification Processing");
        //Do we really require to check or find responsible account?No?
        final Account responsibleAccount = getResponsibleAccout(context,account);
        if(DunningProcessHelper.ignoreSubscriberAgedDebt(context,account))
        	dunningProcessor = new IgnoreSubscriberAgedDebtDunningProcessor(getRunningDate());
        else
        	dunningProcessor = new PolicyBasedDunningProcessor(getRunningDate());
        
        if (LogSupport.isDebugEnabled(context))
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Executing ");
            //sb.append(processName);
            sb.append(" for account '");
            sb.append(responsibleAccount.getBAN());
            sb.append("'");
            if (!responsibleAccount.getBAN().equals(account.getBAN()))
            {
                sb.append(", which is the responsible account for account '");
                sb.append(account.getBAN());
                sb.append("'");
            }
            LogSupport.debug(context, this, sb.toString());
        }
        
        dunningProcessor.processAccount(context, responsibleAccount);
       
    
	}
	
    
    protected Account getResponsibleAccout(Context context,Account account)
    {
    	return DunningProcessHelper.getResponsibleAccout(context, account);
    }


   
    
    protected Date getRunningDate()
    {
    	return runningDate_;
    }

    
    private Date runningDate_;
}
