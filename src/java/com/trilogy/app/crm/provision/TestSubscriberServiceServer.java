/*
 * Created on Dec 10, 2003
 * 
 * Copyright (c) 1999-2003 REDKNEE.com. All Rights Reserved.
 * 
 * This software is the confidential and proprietary information of REDKNEE.com. ("Confidential Information"). You
 * shall not disclose such Confidential Information and shall use it only in accordance with the terms of the license
 * agreement you entered into with REDKNEE.com.
 * 
 * REDKNEE.COM MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE SOFTWARE, EITHCDR EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MCDRCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE,
 * OR NON-INFRINGEMENT. REDKNEE.COM SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFCDRED BY LICENSEE AS A RESULT OF USING,
 * MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */
package com.trilogy.app.crm.provision;

import junit.framework.Test;
import junit.framework.TestCase;

import com.trilogy.app.crm.*;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.provision.xgen.*;
import com.trilogy.app.crm.provision.xgen.RMISubscriberServiceClient;
import com.trilogy.app.crm.provision.xgen.SubscriberService;
import com.trilogy.app.crm.provision.xgen.SubscriberServiceInternalException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xhome.context.ContextSupport;
import com.trilogy.framework.xhome.rmi.RMIProperty;

/**
 * @author psperneac
 */
public class TestSubscriberServiceServer extends TestCase 
   implements ContextAware,SupportingTestConstants
{
   private Context context;

   /**
    * @param name
    */
   public TestSubscriberServiceServer(String name)
   {
      super(name);
   }
   
   /**
    * @param ctx
    * @param name
    */
   public TestSubscriberServiceServer(Context ctx,String name)
   {
      super(name);
      
      setContext(ctx);
   }
   
   /**
    * @see com.redknee.framework.xhome.context.ContextAware#getContext()
    */
   public Context getContext()
   {
      return context;
   }

   /**
    * @see com.redknee.framework.xhome.context.ContextAware#setContext(com.redknee.framework.xhome.context.Context)
    */
   public void setContext(Context context)
   {
      this.context=context;
   }
   /**
    * @see junit.framework.TestCase#setUp()
    */
   protected void setUp() throws Exception
   {
      super.setUp();
      
      Context ctx=new ContextSupport();
      setContext(ctx);

      Context subCtx=ctx.createSubContext();
      
      RMIProperty rmi=new RMIProperty();
      rmi.setHost(HOST);
      rmi.setPort(PORT);
      rmi.setService("SubscriberService");
      
      subCtx.put(RMIProperty.class, rmi);
      ctx.put(SubscriberService.class,new RMISubscriberServiceClient(subCtx));
      
      Context acctCtx=ctx.createSubContext();
      
      RMIProperty acctRMI=new RMIProperty();
      acctRMI.setHost(HOST);
      acctRMI.setPort(PORT);
      acctRMI.setService("AccountService");
      
      acctCtx.put(RMIProperty.class, acctRMI);
      ctx.put(AccountService.class,new RMIAccountServiceClient(acctCtx));
   }

   /**
    * Runs tests agains the Subscriber interface 
    */
   public void testSub()
   {
      SubscriberService service=(SubscriberService) getContext().get(SubscriberService.class);
      assertNotNull(service);
      
      //create an account for use with the subscriber
      Account acct=createNewAccount();
      assertNotNull(acct);
      
      // adds one
      Subscriber sub=new Subscriber();
      // TODO 2008-08-22 name no longer part of Subscriber
      //sub.setFirstName("John");
      //sub.setLastName("Doe");
      sub.setMSISDN("9056252908");
      sub.setBAN(acct.getBAN());
      
      // gets it back
      try
      {
         sub=service.activateSub(sub);
      }
      catch (SubscriberServiceInternalException e)
      {
         fail(e.getMessage());
      }
      catch (SubscriberServiceException e)
      {
         fail(e.getMessage());
      }
      
      assertNotNull(sub);
      
      String msisdn=sub.getMSISDN();
      assertNotNull(sub.getMSISDN());
      assertTrue(sub.getMSISDN().length()>0);
      
      sub=null;

      try
      {
         sub=service.getSub(msisdn);
      }
      catch (SubscriberServiceInternalException e1)
      {
         fail(e1.getMessage());
      }
      catch (SubscriberServiceException e1)
      {
         fail(e1.getMessage());
      }
      
      // this is what's interesting... finding the same one
      assertNotNull(sub);
      
      // let's change something and save it
      sub.setPackageId("Redknee");
      
      try
      {
         service.editSub(sub);
      }
      catch (SubscriberServiceInternalException e2)
      {
         fail(e2.getMessage());
      }
      catch (SubscriberServiceException e2)
      {
         fail(e2.getMessage());
      }
      
      checkState(service,msisdn,SubscriberStateEnum.ACTIVE);
      checkState(service,msisdn,SubscriberStateEnum.INACTIVE);
      checkState(service,msisdn,SubscriberStateEnum.PENDING);
      checkState(service,msisdn,SubscriberStateEnum.SUSPENDED);
      
      // find it back, see if it modified it
      try
      {
         sub=service.getSub(msisdn);
      }
      catch (SubscriberServiceInternalException e1)
      {
         fail(e1.getMessage());
      }
      catch (SubscriberServiceException e1)
      {
         fail(e1.getMessage());
      }
      
      assertEquals(sub.getPackageId(),"Redknee");
   }

   /**
    * @return
    */
   private Account createNewAccount()
   {
      AccountService service=(AccountService) getContext().get(AccountService.class);
      assertNotNull(service);

      try
      {
         // adds one
         Account acct=new Account();
         acct.setFirstName("John");
         acct.setLastName("Doe");
         
         return service.activateAcct(acct);
      }
      catch (AccountServiceInternalException e)
      {
         fail(e.getMessage());
      }
      catch (AccountServiceException e)
      {
         fail(e.getMessage());
      }
      
      return null;
   }

   /**
    * Checks if setting a state inside the account using the remote service works. 
    * @param service the service object
    * @param ban the account number
    * @param state the new state
    */
   private void checkState(SubscriberService service, String ban, SubscriberStateEnum state)
   {
      assertNotNull(service);
      assertNotNull(ban);
      assertNotNull(state);
      
      try
      {
         service.changeState(ban, state);
      }
      catch (SubscriberServiceInternalException e)
      {
         fail(e.getMessage());
      }
      catch (SubscriberServiceException e)
      {
         fail(e.getMessage());
      }
      
      Subscriber acct=null;
      
      try
      {
         acct=service.getSub(ban);
      }
      catch (SubscriberServiceInternalException e1)
      {
         fail(e1.getMessage());
      }
      catch (SubscriberServiceException e1)
      {
         fail(e1.getMessage());
      }
      
      assertNotNull(acct);
      assertNotNull(acct.getState());
      assertTrue(acct.getState().getIndex()==state.getIndex());
   }
}
