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
import junit.framework.TestSuite;

import com.trilogy.app.crm.*;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.provision.xgen.*;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xhome.context.ContextSupport;
import com.trilogy.framework.xhome.rmi.RMIProperty;

/**
 * @author psperneac
 */
public class TestAccountServiceServer extends TestCase 
   implements ContextAware,SupportingTestConstants
{
   //private static final int M2D_USAGE = 300;
   //private static final int BUNDLE_MINUTES = 2000;
   //private static final int BUNDLE_MESSAGES = 200;
   //private static final int BAL_OWING = 1000;
   
   
   
   private Context context;

   /**
    * @param name
    */
   public TestAccountServiceServer(String name)
   {
      super(name);
   }
   
   /**
    * @param ctx
    * @param name
    */
   public TestAccountServiceServer(Context ctx,String name)
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
      
      RMIProperty rmi=new RMIProperty();
      rmi.setHost(HOST);
      rmi.setPort(PORT);
      rmi.setService("AccountService");
      
      
      ctx.put(RMIProperty.class, rmi);
      ctx.put(AccountService.class,new RMIAccountServiceClient(ctx));
   }

   /**
    * Runs tests against the Account interface 
    */
   public void testAcct()
   {
      AccountService service=(AccountService) getContext().get(AccountService.class);
      assertNotNull(service);
      
      // adds one
      Account acct=new Account();
      acct.setFirstName("John");
      acct.setLastName("Doe");

      // gets it back
      try
      {
         acct=service.activateAcct(acct);
      }
      catch (AccountServiceInternalException e)
      {
         fail(e.getMessage());
      }
      catch (AccountServiceException e)
      {
         fail(e.getMessage());
      }
      
      assertNotNull(acct);
      
      String ban=acct.getBAN();
      assertNotNull(acct.getBAN());
      assertTrue(acct.getBAN().length()>0);
      
      acct=null;

      try
      {
         acct=service.getAcct(ban);
      }
      catch (AccountServiceInternalException e1)
      {
         fail(e1.getMessage());
      }
      catch (AccountServiceException e1)
      {
         fail(e1.getMessage());
      }
      
      // this is what's interesting... finding the same one
      assertNotNull(acct);
      
      // let's change something and save it
      acct.setEmployer("Redknee");
      
      // transient fields, can't test this way.
      //acct.setAccumulatedBalance(BAL_OWING);
      //acct.setAccumulatedBundleMessages(BUNDLE_MESSAGES);
      //acct.setAccumulatedBundleMinutes(BUNDLE_MINUTES);
      //acct.setAccumulatedMDUsage(M2D_USAGE);
      
      try
      {
         service.editAcct(acct);
      }
      catch (AccountServiceInternalException e2)
      {
         fail(e2.getMessage());
      }
      catch (AccountServiceException e2)
      {
         fail(e2.getMessage());
      }
      
      checkState(service,ban,AccountStateEnum.ACTIVE);
      checkState(service,ban,AccountStateEnum.INACTIVE);
      checkState(service,ban,AccountStateEnum.NON_PAYMENT_SUSPENDED);
      checkState(service,ban,AccountStateEnum.NON_PAYMENT_WARN);
      checkState(service,ban,AccountStateEnum.PROMISE_TO_PAY);
      checkState(service,ban,AccountStateEnum.SUSPENDED);
      
      // find it back, ee if it modified it
      try
      {
         acct=service.getAcct(ban);
      }
      catch (AccountServiceInternalException e1)
      {
         fail(e1.getMessage());
      }
      catch (AccountServiceException e1)
      {
         fail(e1.getMessage());
      }
      
      // this is what's interesting... finding the same one
      assertNotNull(acct);
      assertNotNull(acct.getEmployer());
      assertEquals(acct.getEmployer(),"Redknee");
      
      //assertTrue(acct.getAccumulatedBalance()==BAL_OWING);
      //assertTrue(acct.getAccumulatedBundleMessages()==BUNDLE_MESSAGES);
      //assertTrue(acct.getAccumulatedBundleMinutes()==BUNDLE_MINUTES);
      //assertTrue(acct.getAccumulatedMDUsage()==M2D_USAGE);
      
      //checkBalance(service, ban);
   }

   /** 
    * Checks if getting the balance returns the same thing as checking the account object.
    * @param service the service
    * @param ban the account number.
    */
   /*private void checkBalance(AccountService service, String ban)
   {
      AccountBalance bal=null;
      try
      {
         bal=service.balanceAcct(ban);
      }
      catch (AccountServiceInternalException e3)
      {
         e3.printStackTrace();
      }
      catch (AccountServiceException e3)
      {
         e3.printStackTrace();
      }
      
      assertNotNull(bal);
      assertTrue(bal.getAccumulatedBalanceOwing()==BAL_OWING);
      assertTrue(bal.getAccumulatedBundleMessages()==BUNDLE_MESSAGES);
      assertTrue(bal.getAccumulatedBundleMinutes()==BUNDLE_MINUTES);
      assertTrue(bal.getAccumulatedMonthToDateUsage()==M2D_USAGE);
   }*/

   /**
    * Checks if setting a state inside the account using the remote service works. 
    * @param service the service object
    * @param ban the account number
    * @param state the new state
    */
   private void checkState(AccountService service, String ban, AccountStateEnum state)
   {
      assertNotNull(service);
      assertNotNull(ban);
      assertNotNull(state);
      
      try
      {
         service.changeState(ban, state);
      }
      catch (AccountServiceInternalException e)
      {
         fail(e.getMessage());
      }
      catch (AccountServiceException e)
      {
         fail(e.getMessage());
      }
      
      Account acct=null;
      
      try
      {
         acct=service.getAcct(ban);
      }
      catch (AccountServiceInternalException e1)
      {
         fail(e1.getMessage());
      }
      catch (AccountServiceException e1)
      {
         fail(e1.getMessage());
      }
      
      assertNotNull(acct);
      assertNotNull(acct.getState());
      assertTrue(acct.getState().getIndex()==state.getIndex());
   }
}
