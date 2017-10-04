/*
 *  SubscriberSearchBorder
 *
 *  Author : Kevin Greer
 *  Date   : Sept 23, 2003
 *  
 *  Copyright (c) 2003, Redknee
 *  All rights reserved.
 */
 
package com.trilogy.app.crm.web.border;

import com.trilogy.app.crm.bean.*;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.*;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.filter.*;
import com.trilogy.framework.xhome.home.*;
import com.trilogy.framework.xhome.session.Session;
import com.trilogy.framework.xhome.support.IdentitySupport;
import com.trilogy.framework.xhome.web.*;
import com.trilogy.framework.xhome.web.agent.*;
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.web.renderer.*;
import com.trilogy.framework.xhome.web.search.*;
import com.trilogy.framework.xhome.webcontrol.*;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.*;


/**
 * An Custom SearchBorder for Subscribers.
 *
 * This will be generated from an XGen template in the future but for now
 * I'm still experimenting with the design.  Also, some common helper classes
 * will be created for each Search type.
 *
 * Add this Border before the WebController, not as one of either its
 * Summary or Detail borders.
 *
 * @author     kgreer
 **/
public class SubscriberSearchBorder
   extends SearchBorder
{
   
   public SubscriberSearchBorder(final Context context)
   {
      super(context, Subscriber.class, new SubscriberSearchWebControl());
      
      
      
      // TODO 2008-08-22 name no longer part of subscriber
      // lastName
/*      addAgent(new SubscriberFirstLastNameSearchAgent(SubscriberXInfo.LAST_NAME.getSQLName(), true)
      {
         public String getCriteria(Object criteria)
         {
            return ((SubscriberSearch) criteria).getLastName();
         }
         
         public String getField(Object bean)
         {
            return ((Subscriber) bean).getLastName();
         }
      });

      // firstName
      addAgent(new SubscriberFirstLastNameSearchAgent(SubscriberXInfo.FIRST_NAME.getSQLName(), true)
      {
         public String getCriteria(Object criteria)
         {
            return ((SubscriberSearch) criteria).getFirstName();
         }
         
         public String getField(Object bean)
         {
            return ((Subscriber) bean).getFirstName();
         }
      }); */
      
      // MSISDN
      addAgent(new ContextAgentProxy() {
         public void execute(Context ctx)
            throws AgentException
         {
            SubscriberSearch criteria  = (SubscriberSearch) SearchBorder.getCriteria(ctx);
            String        msisdn      = criteria.getMSISDN();
            
            if ( ! "".equals(msisdn) )
            try
            {
               Account       acc = (Account)ctx.get(Account.class);
               SearchBorder.doSelect(ctx, new EQ(SubscriberXInfo.MSISDN, criteria.getMSISDN()));
            }

            catch (NullPointerException e)
            {
            }
       
            delegate(ctx);
         }
      });
      
      
      
      /*
      // MSISDN
      addAgent(new FindSearchAgent(SubscriberXInfo.MSISDN)
      {
         public String getCriteria(Object criteria)
         {
            return ((SubscriberSearch) criteria).getMSISDN();
         }
         
         public String getField(Object bean)
         {
            return ((Subscriber) bean).getMSISDN();
         }
      });
      */
      // Subscriber ID
      addAgent(new FindSearchAgent(SubscriberXInfo.ID, SubscriberSearchXInfo.SUB_ID));

      // IMSI
      addAgent(new FindSearchAgent(SubscriberXInfo.IMSI, SubscriberSearchXInfo.IMSI));

      // Address1 - nolonger part of the Subscriber
//      addAgent(new WildcardSelectSearchAgent(SubscriberXInfo.ADDRESS1, true)
//      {
//         public String getCriteria(Object criteria)
//         {
//            return ((SubscriberSearch) criteria).getAddress1();
//         }
//         
//         public String getField(Object bean)
//         {
//            return ((Subscriber) bean).getAddress1();
//         }
//      });

      // Package ID
      addAgent(new FindSearchAgent(SubscriberXInfo.PACKAGE_ID,SubscriberSearchXInfo.PACKAGE_ID));
      
      // Limit
      addAgent(new LimitSearchAgent(SubscriberSearchXInfo.LIMIT));

   }

}

