/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home.calldetail;

import java.util.regex.Pattern;

import com.trilogy.app.crm.bean.calldetail.CallDetail;
import com.trilogy.app.crm.bean.calldetail.CallTypeEnum;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

/**
 * Validate msisdn input is in digit only
 * TODO currently this home doesn't do anything
 * 
 * @author kwong
 *
 */
public class CallDetailValidatingHome extends HomeProxy
{
       /**
        * Constructor. 
        * @param delegate the next home in the chain
        */
       public CallDetailValidatingHome(Home delegate)
       {
          super(delegate);

       }
      
       
       public  Object create(Context ctx, Object obj)
        throws HomeException
       {
            //assertOrigMsisdn((CallDetail)obj);
            //assertDestMsisdn((CallDetail)obj);
            return getDelegate().create(ctx,obj);
       }
       
       
       public  Object store(Context ctx,Object obj)
        throws HomeException
       {
           //assertOrigMsisdn((CallDetail)obj);
           //assertDestMsisdn((CallDetail)obj);
           return super.store(ctx,obj);
       }
       
       
      /* private void assertDestMsisdn(CallDetail detail)
       {
           if(!detail.getCallType().equals(CallTypeEnum.SMS))          //Support for AlphaNumeric SMS. 
            assertMsisdn(detail.getDestMSISDN());
       }
       
       
       private void assertOrigMsisdn(CallDetail detail)
       {
           if ( !detail.getCallType().equals(CallTypeEnum.SMS) && (detail.getOrigMSISDN() == null || detail.getOrigMSISDN().length() > 0) )
           {
               assertMsisdn(detail.getOrigMSISDN());
           }
       }*/
       
       public void assertMsisdn(String msisdn)
           throws IllegalArgumentException
       {
            if ( ! MSISDN_PATTERN.matcher(String.valueOf(msisdn)).matches() )
            throw new IllegalPropertyArgumentException("MSISDN", "Doesn't match pattern: Cannot set MSISDN to value which does not match pattern '" + MSISDN_PATTERN.pattern() + "' .");
    
       }
       
      public final static Pattern MSISDN_PATTERN        = Pattern.compile("^\\d*$");
      
}
