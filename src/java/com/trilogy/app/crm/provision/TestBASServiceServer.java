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
import com.trilogy.app.crm.provision.xgen.BASService;
import com.trilogy.app.crm.provision.xgen.RMIBASServiceClient;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xhome.context.ContextSupport;
import com.trilogy.framework.xhome.rmi.RMIProperty;

/**
 * This is a unit test for the BAS Service Server
 * @author psperneac
 */
public class TestBASServiceServer extends TestCase 
   implements ContextAware,SupportingTestConstants
{
   private Context context;

   /**
    * @param name
    */
   public TestBASServiceServer(String name)
   {
      super(name);
   }
   
   /**
    * @param ctx
    * @param name
    */
   public TestBASServiceServer(Context ctx,String name)
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
      rmi.setService("BASService");
      
      
      ctx.put(RMIProperty.class, rmi);
      ctx.put(BASService.class,new RMIBASServiceClient(ctx));
   }

   /**
    * Tests the BAS interface
    */
   public void testBAS()
   {
   }
}
