package com.trilogy.app.crm.clean;

import java.util.Date;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.elang.LT;
import com.trilogy.framework.xhome.elang.NEQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.CloneingVisitor;
import com.trilogy.framework.xhome.visitor.HomeVisitor;
import com.trilogy.framework.xhome.xdb.SimpleXStatement;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * ContextAgent to go through database to set 
 * out-dated subscriber's state to be deactivated
 * @author lzou
 * @date   Nov 10, 2003
 */
public class SubEndDateCheckAgent implements ContextAgent
{
    final static long DAY_IN_MILLS = 24*60*60*1000L;
    
	public SubEndDateCheckAgent()
	{
		super();		
	}
   
    /**
     * rattapattu
     * 
     * @see com.redknee.framework.xhome.context.ContextAgent#execute(com.redknee.framework.xhome.context.Context)
     * 
     * The Logic in this method is as follows. We querry the subscriber table with the
     * specific criteria for the clean up and then walk through all the
     * selected subs using a visitor and then put them to inactive them.     
     *  
     */
   
   public void execute(Context ctx) throws AgentException
   {
      final Home subHome = (Home) ctx.get(SubscriberHome.class);
      if (subHome == null)
      {
         throw new AgentException("System error: SubscriberHome not found in context");
      }

      try
      {
           //subHome.where(ctx, new NEQ(SubscriberXInfo.STATE,SubscriberStateEnum.INACTIVE))
           //.where(ctx,new LT(SubscriberXInfo.END_DATE ,new Date()))
          Home h = subHome.where(ctx, new SimpleXStatement(" state<>" + SubscriberStateEnum.INACTIVE_INDEX + " and enddate<" + new Date().getTime() + " and enddate>0"));
          h.forEach(ctx, new CloneingVisitor(new HomeVisitor(subHome)
                  {

                      public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException
                      {
                          Subscriber sub = (Subscriber)obj;
                          try
                          {
                              sub.setState(SubscriberStateEnum.INACTIVE);
                              getHome().store(ctx,sub);
                          }
                          catch (Exception e)
                          {
                              LogSupport.minor(ctx,this,"Couldn't update sub " + sub.getId() + " to INACTIVE",e);
                          }
                      }
              
                  }
                  
          
          ));
      }
      catch (Exception e)
      {
          LogSupport.minor(ctx,this,"Error selecting subs who are expired",e);
      }
   }
}
