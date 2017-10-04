package com.trilogy.app.crm.clean;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.account.AccountAttachment;
import com.trilogy.app.crm.util.FileUtil;
import com.trilogy.app.crm.visitor.RowCountVisitor;
import com.trilogy.app.crm.web.control.AccountAttachmentSupport;
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
import com.trilogy.framework.xhome.xdb.XDB;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * ContextAgent to remove out-dated, deactivated accounts from database.
 * @author lzou
 * @date   Nov 14, 2003
 */

public class AccountCleanUpAgent implements ContextAgent
{
    final static long DAY_IN_MILLS = 24*60*60*1000L;
    
    public AccountCleanUpAgent()
    {
        super();
    }
   
    /**
     * rattapattu
     * 
     * @see com.redknee.framework.xhome.context.ContextAgent#execute(com.redknee.framework.xhome.context.Context)
     * 
     * The Logic in this method is as follows. We walk through all the spids
     * using a visitor and in each spid we querry the Account table with the
     * spid specific criteria for the clean up and then walk through all the
     * selected Accounts using a visitor and remove them..     
     *  
     */
   
    public void execute(Context ctx) throws AgentException
    {
        
        Collection allEntry = new ArrayList();
        Iterator   i        = null;
        
        final Home accountHome = ( Home )ctx.get(AccountHome.class);       
        if ( accountHome == null )
        {
            throw new AgentException("System error: AccountHome not found in context");
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
                
                long expiryDate = new Date().getTime() - crmSp.getAccountCleanupDay()*DAY_IN_MILLS;
                
                ctx = ctx.createSubContext();
                ctx.put(CronConstants.BALANCE_THRESHOLD_CTX_KEY,Long.valueOf(crmSp.getAccountCleanUpThresholdAmtOwing()));
                
                try
                {
                    accountHome.where(ctx, new EQ(AccountXInfo.STATE,AccountStateEnum.INACTIVE))
                    .where(ctx,new EQ(AccountXInfo.SPID, Integer.valueOf(crmSp.getSpid())))
                    .where(ctx,new LT(AccountXInfo.LAST_MODIFIED,new Date(expiryDate)))
                    //.where(ctx,new LT(AccountXInfo.ACCUMULATED_BALANCE, Long.valueOf(crmSp.getAccountCleanUpThresholdAmtOwing()))) transient field.
                    .forEach(ctx, new CloneingVisitor(new HomeVisitor(accountHome)
                            {

                                public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException
                                {
                                    Account acct = (Account)obj;
                                    try                                                
                                    {
                                        
                                        
                                        XDB xdb = (XDB)ctx.get(XDB.class);                                                      
                                        RowCountVisitor visitor = new RowCountVisitor("SUB_COUNT");                                                 
                                        xdb.find(ctx, visitor,new SimpleXStatement("Select count(*) as SUB_COUNT from Subscriber where ban='" + acct.getBAN() + "'"));
                                        
                                        if(visitor.getCount()==0)
                                        {
                                            long balanceThreshold = ((Long)ctx.get(CronConstants.BALANCE_THRESHOLD_CTX_KEY)).longValue();
                                            if(acct.getAccumulatedBalance()<=balanceThreshold)
                                            {
                                                getHome().remove(ctx,acct);
                                                try
                                                {
                                                    // clean all attachments to the account
                                                    Home home = AccountAttachmentSupport.getBanAttachmentsHome(ctx, acct.getBAN());
                                                    home.forEach(ctx, new Visitor() {

                                                        @Override
                                                        public void visit(Context ctx, Object obj)
                                                                throws AgentException, AbortVisitException
                                                        {
                                                            AccountAttachment attachment = (AccountAttachment) obj;
                                                            if(FileUtil.deleteDir(new File(attachment.getFilePath()))==false)
                                                            {
                                                                new MinorLogMsg(this,"Could not remove all files for attachments of BAN [" +attachment.getBAN() +"].  At location " + attachment.getFilePath(),null).log(ctx);
                                                            }
                                                  
                                                        }});
                                                    home.removeAll(ctx);
                                                    
                                                } catch (Throwable t)
                                                {
                                                    new MajorLogMsg(this,"All attachments and files of BAN [" +acct.getBAN() +"].",t).log(ctx);
                                                }
                                            }   
                                                
                                        }
                                    }
                                    catch (Exception e)
                                    {
                                        LogSupport.minor(ctx,this,"Couldn't remove Account " + acct.getBAN(),e);
                                    }
                                }
                        
                            }
                            
                    
                    ));
                }
                catch (Exception e)
                {
                    LogSupport.minor(ctx,this,"Error selecting Accounts who are expired",e);
                }
            }  
        }
        catch(HomeException e)
        {
              new MinorLogMsg(this, "Error getting data from spid table", e).log(ctx); 
        }
    }
    
}
