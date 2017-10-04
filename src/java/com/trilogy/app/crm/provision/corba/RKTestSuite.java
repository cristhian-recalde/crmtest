/*
 * Created on Jan 27, 2004
 *
 * Copyright (c) 1999-2003 REDKNEE.com. All Rights Reserved.
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
 * MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.  */
package com.trilogy.app.crm.provision.corba;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xhome.context.ContextAwareSupport;


/**
 * @author psperneac
 */
public class RKTestSuite extends TestSuite
{
   /**
    * @param name
    */
   public RKTestSuite()
   {
      super();

       }

   /**
    * @param ctx
    * @return
    */
   public static Test suite()
   {
      RKTestSuite suite=new RKTestSuite();

      /*
      suite.addTest(TestSupport.getSuite(suite.getContext(),TestECareServiceClient.class));
      suite.addTest(TestSupport.getSuite(suite.getContext(), TestLanguageSupportServiceClient.class));
      */
        
        //final Context context = contextSupport_.getContext().createSubContext();
        

      //suite.addTest(TestSupport.getSuite(context,TestECareServiceClient.class));
      //suite.addTest(TestSupport.getSuite(context, TestLanguageSupportServiceClient.class));

      return suite;
   }

   //private static final ContextAware contextSupport_ = new ContextAwareSupport() {} ;


}
