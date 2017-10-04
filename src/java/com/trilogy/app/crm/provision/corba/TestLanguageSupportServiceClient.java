/*
 * Created on Jan 27, 2005
 *
 * Copyright (c) 1999-2005 REDKNEE.com. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * REDKNEE.com. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with REDKNEE.com.
 *
 * REDKNEE.COM MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE
 * SUITABILITY OF THE SOFTWARE, EITHCDR EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE IMPLIED WARRANTIES OF MCDRCHANTABILITY, FITNESS FOR
 * A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. REDKNEE.COM SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFCDRED BY LICENSEE AS A RESULT OF USING,
 * MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.  

 * @author lzou 
 */

package com.trilogy.app.crm.provision.corba;

import java.util.Date;

import com.trilogy.app.crm.provision.SupportingTestConstants;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.ViewModeEnum;

import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.service.corba.CorbaClientProperty;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.omg.CORBA.StringHolder;

import com.trilogy.app.crm.provision.corba.LanguageSupportServicePackage.*;

import com.trilogy.app.crm.unit_test.ContextAwareTestCase;

/**
 * @author lzou
 * @date   Jan.18, 2005
 */
public class TestLanguageSupportServiceClient extends ContextAwareTestCase
{
   /**
    * @param ctx
    * @param name
    */
   public TestLanguageSupportServiceClient(final String name)
   {
      super(name);
   }

   /**
     * Creates a new suite of Tests for execution.  This method is intended to
     * be invoked by standard JUnit tools (i.e., those that do not provide a
     * context).
     *
     * @return A new suite of Tests for execution.
     */
   public static Test suite()
   {
      return suite(com.redknee.app.crm.TestPackage.createDefaultContext());
   }


   /**
     * Creates a new suite of Tests for execution.  This method is intended to
     * be invoked by the Redknee Xtest code, which provides the application's
     * operating context.
     *
     * @param context The operating context.
     * @return A new suite of Tests for execution.
     */
    public static Test suite(final Context context)
    {
        setParentContext(context);

        final TestSuite suite = new TestSuite(TestLanguageSupportServiceClient.class);

        return suite;
    }

     // INHERIT
    public void setUp()
    {
        super.setUp();
        /*
        CorbaClientProperty prop=new CorbaClientProperty();
        prop.setNameServiceHost(CORBA_NAMESERVICE_HOST);
        prop.setNameServicePort(CORBA_NAMESERVICE_PORT);
        prop.setNameServiceName("name");
        prop.setNameServiceContextName("Redknee/App/Crm/languageSupportServiceFactory");
        prop.setUsername("rkadm");
        prop.setPassword("rkadm");

        Context ctx=getContext().createSubContext();
        ctx.put(CorbaClientProperty.class,prop);
        */
    }

    // INHERIT
    public void tearDown()
    {
        super.tearDown();
    }

   public void testLanguageSupportConnection()
   {
       CorbaClientProperty prop=new CorbaClientProperty();
       prop.setNameServiceHost(CORBA_NAMESERVICE_HOST);
       prop.setNameServicePort(CORBA_NAMESERVICE_PORT);
       prop.setNameServiceName("name");
       prop.setNameServiceContextName("Redknee/App/Crm/languageSupportServiceFactory");
       prop.setUsername("rkadm");
       prop.setPassword("rkadm");

      getContext().put(CorbaClientProperty.class,prop);
       
      LanguageSupportServiceClient client=new LanguageSupportServiceClient(getContext());
      
      assertTrue(client.isConnected());
      System.out.println(client.isConnected());
      LanguageSupportService service=client.getService();

      try
      {
         StringHolder langId = new StringHolder();
         int resultCode = service.getLanguagePrompt("3977222295", langId);
         System.out.println("result: " + resultCode + "   holder  " + langId.value );
         assertTrue(resultCode == 0 );
         assertTrue( langId.value.equals("en") );

         // setLanguagePrompt method testing
         resultCode = service.setLanguagePrompt("3977222295", "id");
         assertTrue(resultCode == 0 );

         // getLanguagePrompt method testing
         resultCode = service.getLanguagePrompt("3977222295", langId);
         assertTrue(langId.value.equals( "id"));
         
         // no MSISDN found error code testing
         resultCode = service.getLanguagePrompt("999", langId);
         System.out.println("resultCode: "+ resultCode);
         assertTrue(resultCode == 1);
         resultCode = service.setLanguagePrompt("999", "id");
         assertTrue(resultCode == 1);
         
         // no LangId found error code testing
         resultCode = service.setLanguagePrompt("3977222295", "fr");
         assertTrue(resultCode == 2);
      }
      catch(Throwable th)
      {
          th.printStackTrace();
         new MinorLogMsg(this, th.getMessage(), th).log(getContext());
         
         assertTrue(false);
      }
      
   }

   public static final String CORBA_NAMESERVICE_HOST="localhost";
   public static final int CORBA_NAMESERVICE_PORT=20000;
}

