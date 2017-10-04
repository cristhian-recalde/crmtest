package com.trilogy.app.crm.clean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.Invoice;
import com.trilogy.app.crm.bean.InvoiceHome;
import com.trilogy.app.crm.bean.InvoiceXInfo;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
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
import com.trilogy.framework.xhome.xdb.SimpleXStatement;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * ContextAgent to remove invoice entries after it stays in database 
 * remaining untouched for a configurable time period.
 * @author lzou
 * @date   Nov 14, 2003
 */
public class InvoiceCleanUpAgent implements ContextAgent
{
    final static long DAY_IN_MILLS = 24*60*60*1000L;
    
    /**
     * rattapattu
     * 
     * @see com.redknee.framework.xhome.context.ContextAgent#execute(com.redknee.framework.xhome.context.Context)
     * 
     * The Logic in this method is as follows. We walk through all the spids
     * using a visitor and in each spid we querry the invoice table with the
     * spid specific criteria for the clean up and then walk through all the
     * selected invoice entries using a visitor tand delete them.     
     *  
     */

    public void execute(Context ctx) throws AgentException
    {
        Collection allEntry = new ArrayList();
        Iterator   i;
        
        final Home invoiceHome = ( Home ) ctx.get(InvoiceHome.class);
        if ( invoiceHome == null )
        {
             throw new AgentException("System error: InvoiceHome not found in context");
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
                
                long expiryDate = new Date().getTime() - crmSp.getInvoiceHistoryCleanupDays()*DAY_IN_MILLS;
                
                try
                {
                    //invoiceHome.where(ctx, new EQ(InvoiceXInfo.SPID, Integer.valueOf(crmSp.getSpid())))
                    //.where(ctx, new LT(InvoiceXInfo.GENERATED_DATE, Long.valueOf(expiryDate)))
                    
                    // need to do a join as spid is a transient field in Invoice.
                    
                    String sql = "BAN in (select BAN from ACCOUNT where SPID=" + crmSp.getId() + ")" + 
                                 " and GENERATEDDATE<" + expiryDate;
                    
                    invoiceHome.where(ctx, new SimpleXStatement(sql))
                    .forEach(ctx, new CloneingVisitor(new HomeVisitor(invoiceHome)
                            {

                                public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException
                                {
                                    Invoice inv = (Invoice)obj;
                                    try
                                    {
                                        getHome().remove(ctx,inv);
                                    }
                                    catch (Exception e)
                                    {
                                        LogSupport.minor(ctx,this,"Couldn't remove invoice " + inv.getInvoiceId(),e);
                                    }
                                }
                        
                            }
                            
                    
                    ));
                }
                catch (Exception e)
                {
                    LogSupport.minor(ctx,this,"Error selecting invoices who are expired",e);
                }
            }
            
        }
        catch(HomeException e)
        {
              new MinorLogMsg(this, "Error getting data from spid table", e).log(ctx); 
        }
    }
}
