package com.trilogy.app.crm.clean;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import com.trilogy.app.crm.bean.*;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.bean.TransactionHome;
import com.trilogy.app.crm.bean.TransactionXInfo;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.LT;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.CloneingVisitor;
import com.trilogy.framework.xhome.visitor.HomeVisitor;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * ContextAgent to remove out-dated Transaction History Records from database
 * @author lzou
 * @date   Nov 14, 2003
 */
public class TransactionCleanUpAgent implements ContextAgent
{
    final static long DAY_IN_MILLS = 24*60*60*1000L;
    
    public TransactionCleanUpAgent()
    {
        super();
    }
      
    /** 
     * @see com.redknee.framework.xhome.context.ContextAgent#execute(com.redknee.framework.xhome.context.Context)
     */
    public void execute(Context ctx) throws AgentException
    {
        final Home transHome = ( Home ) ctx.get(TransactionHome.class);
        if ( transHome == null )
        {
             throw new AgentException("System error: TransactionHome not found in context");
        }
        
        Home spidHome = ( Home ) ctx.get(CRMSpidHome.class);
        if ( spidHome == null )
        {
             throw new AgentException("System error: CRMSpidHome not found in context");
        }
        
        try
        {
            Collection spids = spidHome.selectAll();
            
            final Iterator iter = spids.iterator();
            
            //for each spid
            while (iter.hasNext())
            {
                CRMSpid crmSp = (CRMSpid)iter.next();
                
                GeneralConfig gen=(GeneralConfig) ctx.get(GeneralConfig.class);

                long expiryDate = new Date().getTime() - gen.getTransactionHistoryDays()*DAY_IN_MILLS;
                
                try
                {
                    transHome.where(ctx, new EQ(TransactionXInfo.SPID, Integer.valueOf(crmSp.getSpid())))
                    .where(ctx,new LT(TransactionXInfo.TRANS_DATE,new Date(expiryDate)))
                    .forEach(ctx, new CloneingVisitor(new HomeVisitor(transHome)
                            {

                                public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException
                                {
                                    Transaction transaction = (Transaction)obj;
                                    try
                                    {
                                        getHome().remove(ctx,transaction);
                                    }
                                    catch (Exception e)
                                    {
                                        LogSupport.minor(ctx,this,"Couldn't remove transaction " + transaction.getReceiptNum(),e);
                                    }
                                }
                        
                            }
                            
                    
                    ));
                }
                catch (Exception e)
                {
                    LogSupport.minor(ctx,this,"Error selecting transactions",e);
                }
            }   
         
        }
        catch(HomeException e)
        {
              new MinorLogMsg(this, "Encountered problem when selecting for each for Spid.", e).log(ctx);  
        }
        
        
    }
 
      
}
