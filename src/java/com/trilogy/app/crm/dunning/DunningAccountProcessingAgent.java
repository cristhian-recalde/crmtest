package com.trilogy.app.crm.dunning;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.dunning.visitor.AbstractDunningXStatementVisitor;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.xdb.XResultSet;
import com.trilogy.framework.xlog.log.MinorLogMsg;

public class DunningAccountProcessingAgent implements DunningAgent
{

    
    public DunningAccountProcessingAgent(AbstractDunningXStatementVisitor dunningPredicateVisitor)
    {
        dunningPredicateVisitor_ = dunningPredicateVisitor;
    }

    
    public void execute(Context context)
    {
    	Object objectToProcess =  context.get(DunningConstants.DUNNINGAGENT_OBJECT_TOPROCESS);
    	Account account =null;
        try
        {
        	if(!(objectToProcess instanceof Account)){
	        	String accountID = ((XResultSet)objectToProcess).getString(1);
	        	account = AccountSupport.getAccount(context, accountID);
        	}else{ //Flow of dunning report
        		account = (Account)objectToProcess;
        	}
        }catch (SQLException sqlException){
			new MinorLogMsg(this, "Unable to retrieve account during task: "+ sqlException.getMessage(), sqlException).log(context);
		}catch (HomeException e) {
			new MinorLogMsg(this, "Unable to retrieve account during task: "+ e.getMessage(), e).log(context);
		}    	
        
        if (account!=null)
        {
            try
            {
                dunningPredicateVisitor_.getAccountVisitor().visit(context, account);
                dunningPredicateVisitor_.onItemSuccess();
            }
            catch (Exception e)
            {
                new MinorLogMsg(this, "Dunning process failed for account '" + account.getBAN() + "': " +  e.getMessage(), e).log(context);
                addFailedBAN(account.getBAN());
                dunningPredicateVisitor_.onItemFailure();
            }
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
