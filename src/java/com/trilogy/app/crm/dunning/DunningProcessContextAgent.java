package com.trilogy.app.crm.dunning;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.dunning.visitor.AbstractDunningXStatementVisitor;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.xdb.XResultSet;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;


public class DunningProcessContextAgent implements DunningAgent
{
    
    public DunningProcessContextAgent(AbstractDunningXStatementVisitor dunningPredicateVisitor)
    {
        dunningPredicateVisitor_ = dunningPredicateVisitor;
    }

    
    public void execute(Context context)
    {
    	Object objectToProcess =  context.get(DunningConstants.DUNNINGAGENT_OBJECT_TOPROCESS);
    	DunningReportRecord drr = null;
        
    	if(objectToProcess instanceof DunningReportRecord)
    	{
    		drr = (DunningReportRecord)objectToProcess;
    		try{
    			drr = (DunningReportRecord)drr.clone();
    		}catch(CloneNotSupportedException e)
    		{
    			
    		}
    	}
        
        
        if (drr!=null)
        {
            try
            {
                dunningPredicateVisitor_.getAccountVisitor().visit(context, drr);
                dunningPredicateVisitor_.onItemSuccess();
            }
            catch (Exception e)
            {
                new MinorLogMsg(this, "Dunning process failed for account '" + drr.getBAN() + "': " +  e.getMessage(), e).log(context);
                addFailedBAN(drr.getBAN());
                dunningPredicateVisitor_.onItemFailure();
            }
        }else
        {
        	new MinorLogMsg(this, "Wrong object to process , marking execution failed").log(context);
        }
        
    }
    
    
    
    private synchronized void addFailedBAN(String BAN)
    {
        this.failedBANs_.add(BAN);
    }
    

    public synchronized List<String> getFailedBANs()
    {
        return failedBANs_;
    }


    private List<String> failedBANs_  = new ArrayList<String>();

    private AbstractDunningXStatementVisitor dunningPredicateVisitor_;
    
    
    
}
