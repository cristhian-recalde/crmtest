package com.trilogy.app.crm.clean;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.framework.core.platform.CoreSupport;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.LT;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.CloneingVisitor;
import com.trilogy.framework.xhome.visitor.HomeVisitor;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.OMLogMsg;

/**
 * ContextAgent to active subscriber in Pending state
 * @author lzou
 * @date   Dec 17, 2003
 * no longer in user, function already moved to SubscriberFutureActiveOrDeactiveAgent.
 */
public class ActivePendingSubAgent 
            implements ContextAgent
{
    /** Prefix for the activated subscribers file name */
    public static String SUCCEED_SUBS_FILE_NAME = "ActivatedSubscriberList_";
    /** Prefix for the failed to activate subscribers file name */
    public static String FAIL_SUBS_FILE_NAME   = "FailToActivatedSubscriberList_";

    protected boolean isSucceedFile = true;

    protected SubscriberStateEnum state=null;
    
    protected SubscriberTypeEnum type=null;
   
    final static String FAILED_SUBS_CTX_KEY = "FAILED_SUBS";
    final static String SUCCESS_SUBS_CTX_KEY = "SUCCESS_SUBS";
	/**
	 *   Constructor
	 */
	public ActivePendingSubAgent()
	{
		state=SubscriberStateEnum.ACTIVE;
		type=SubscriberTypeEnum.POSTPAID;
	}
      
	/**
	 * @see com.redknee.framework.xhome.context.ContextAgent#execute(com.redknee.framework.xhome.context.Context)
	 */
	public void execute(Context ctx) throws AgentException
	{
		Home subscriberHome = (Home)ctx.get(SubscriberHome.class);
        
        if ( subscriberHome == null )
        {
             throw new AgentException("System error: SubscriberHome not found in context");
        }
        
        try
        {
        	  Date today = new Date();
              final List succeeded = new ArrayList();
              final List failed   = new ArrayList();
              
              Context subCtx = ctx.createSubContext();
              subCtx.put(SUCCESS_SUBS_CTX_KEY,succeeded);
              subCtx.put(FAILED_SUBS_CTX_KEY,failed);
              
              And predicate = new And();
              
              predicate.add(new EQ(SubscriberXInfo.STATE,SubscriberStateEnum.PENDING));
              predicate.add(new EQ(SubscriberXInfo.SUBSCRIBER_TYPE,type));
              predicate.add(new LT(SubscriberXInfo.DATE_CREATED,today));
              subscriberHome.where(subCtx,predicate)
              .forEach(subCtx,new CloneingVisitor(new HomeVisitor(subscriberHome)
                      {
                  		  public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException
                   		  {
                  			  Subscriber subscriber = (Subscriber)obj;
                              subscriber.setState(state);
                                     
                              try
                              {                 
                                  getHome().store(ctx,subscriber);
                                  
                                  succeeded.add(subscriber.getMSISDN());
                              }
                              catch(HomeException e)
                              {
                                    new MinorLogMsg(this, "Fail to Activate Pending Subscriber [MSISDN=" + subscriber.getMSISDN() +" ]", e).log(ctx);
                                    failed.add(subscriber.getMSISDN());
                              }
                              catch(Exception e)
                              {
                                    new MinorLogMsg(this, "Fail to Activate Pending Subscriber [MSISDN=" + subscriber.getMSISDN() +" ]", e).log(ctx);
                                    failed.add(subscriber.getMSISDN());
                              }
                              
                              // TODO: increment Recovery_Pending OM
                              new OMLogMsg(Common.OM_MODULE, Common.OM_RECOVERY_PENDING_SUCCESS).log(ctx);
                  		  }
                      }
              
                   ));
              
              // print out all suceeded/failed entries to files
              printoutSubs(ctx, succeeded, true );
              printoutSubs(ctx, failed, false  );
              
        }
        catch(HomeException e)
        {
              new MinorLogMsg(this, "Encountered problem when seleting held entries from MSISDNHome.", e).log(ctx); 
        }      
        
	}
      
    private void printoutSubs(Context ctx, List subscriberList, boolean _isSucceedFile1)
    {
        FileWriter fileWriter  = null;
        PrintWriter printWriter = null;        
       
        StringBuilder buff = new StringBuilder();
        
        buff.append(CoreSupport.getProjectHome(ctx));
        File homeDir = new File(buff.toString());
        if(!homeDir.exists()){
        	new MinorLogMsg(this,"Project Home is not exist in Context!", null).log(ctx);
        	return;
        }
        buff.append(File.separator);
        buff.append("SubRecovery");
        
        File dir = new File(buff.toString());
        if(!dir.exists()){
        	dir.mkdirs();
        }
        
        buff.append(File.separator);
        if( _isSucceedFile1 )
        {
            buff.append(SUCCEED_SUBS_FILE_NAME);
        }
        else
        {
            buff.append(FAIL_SUBS_FILE_NAME);
        }
        
        Date file_date = new Date();        
        final SimpleDateFormat formatter =  new SimpleDateFormat("yyyyMMdd_HHmmss");
        buff.append(formatter.format(file_date));
        
        File dataFile = new File(buff.toString()) ;
        
        try
        {
            fileWriter = new FileWriter(dataFile);
            printWriter = new PrintWriter(fileWriter);
            
            for ( Iterator i = subscriberList.iterator(); i.hasNext();)
            {
                  printWriter.println(i.next());
            }
        }  
        catch(IOException e)
        {
              
        }
        finally
        {
              try
              {
                  printWriter.close();
                  fileWriter.close();
              }
              catch(Exception e)
              {
                    
              }
        }
    }
}


