/*
 * Created on Jan 26, 2004
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

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.MessageFormat;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xhome.support.DateUtil;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubBillingLanguage;
import com.trilogy.app.crm.bean.SubBillingLanguageHome;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.provision.xgen.*;
import com.trilogy.app.crm.provision.corba.LanguageSupportServicePackage.ServiceError;

import org.omg.CORBA.StringHolder;

/**
 * @author  lzou
 * @date    Jan.15, 2005
 */

public class LanguageSupportServiceImpl extends LanguageSupportServicePOA implements ContextAware
{
   protected Context context;
   protected String uid;

   public LanguageSupportServiceImpl(Context context)
   {
       this(context, "internal");
      
   }
   
   /**
    * Constructor. Saves the context and the user id.
    * @param context
    * @param uid
    */
   public LanguageSupportServiceImpl(Context context, String uid)
   {
      super();
      
      setContext(context);
      setUid(uid);
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
    * @see com.redknee.app.crm.provision.corba.LanguageSupportServiceOperations#getLanguagePrompt(java.lang.String, org.omg.CORBA.StringHolder)
    */
   public int getLanguagePrompt (String msisdn, org.omg.CORBA.StringHolder langId) throws com.redknee.app.crm.provision.corba.LanguageSupportServicePackage.ServiceError
   {
       // langId.value must be initialzed or null will possibly be passed over to client side 
       // in case any exception occurs. BAD_PARAM exception will be thrown in client side in this 
       // case, which is bad. 
       langId.value = "";

       try
       {
            Subscriber subscriber = validateMsisdn(msisdn);
            
            if ( subscriber == null )
            {
                return LanguageSupportService.EC_MSISDN_NOT_FOUND;
            }
            
            String subLangId = subscriber.getBillingLanguage();

            SubBillingLanguage langObj = validateLangId(subLangId);
            
            if ( langObj == null )
            {
                return LanguageSupportService.EC_UNKNOWN_LANG_ID;
            }

            langId.value = langObj.getId();
            
            return LanguageSupportService.EC_SUCCESS;
       }
       catch(Exception ex)
       { 
           new MinorLogMsg(this,ex.getMessage(),ex).log(getContext());
           throw new ServiceError(ex.getMessage());
       }
   }

   /**
    * @see com.redknee.app.crm.provision.corba.LanguageSupportServiceOperations#setLanguagePrompt(java.lang.String, int)
    */
   public int setLanguagePrompt (String msisdn, String langId) throws com.redknee.app.crm.provision.corba.LanguageSupportServicePackage.ServiceError
   {
       try
       {
            Subscriber subscriber = validateMsisdn(msisdn);
            
            if ( subscriber == null )
            {
                return LanguageSupportService.EC_MSISDN_NOT_FOUND;
            }
            
            SubBillingLanguage langObj = validateLangId(langId);

            if ( langObj == null )
            {
                return LanguageSupportService.EC_UNKNOWN_LANG_ID;
            }

            subscriber.setBillingLanguage(langId);

            Home subHome = (Home)getContext().get(SubscriberHome.class);

            try
            {
                subHome.store(subscriber);
            }
            catch(Exception e)
            {
               throw e;
            }
            
            return LanguageSupportService.EC_SUCCESS;
       }
       catch(Exception ex)
       { 
           new MinorLogMsg(this,ex.getMessage(),ex).log(getContext());
           throw new ServiceError(ex.getMessage());
       }

   }

   public void setUid(String uid)
   {
       this.uid = uid;
   }

   private Subscriber validateMsisdn(String msisdn)
   {
        Subscriber subscriber = null;
        Exception exception   = null;

        try
        {
            subscriber = SubscriberSupport.lookupSubscriberForMSISDN(getContext(), msisdn);
        }
        catch(Exception e)
        {
            exception = e;    
            subscriber = null;               
        }

        if ( subscriber == null )
        {
            logNullSub(msisdn, exception);
        }

        return subscriber;
   }

   private void logNullSub(String msisdn, Exception exception)
   {
       final String formattedMsg = MessageFormat.format("Could not find subscriber with Msisdn \"{0}\".",
               new Object[]{msisdn});

       new MinorLogMsg(this, formattedMsg, exception).log(getContext());
   }

   private  SubBillingLanguage validateLangId(String subLangId )
   {
        SubBillingLanguage langObj    = null;
        Exception          exception  = null;
        
        try
        {
		    Home langHome = (Home)getContext().get(SubBillingLanguageHome.class);
            langObj = (SubBillingLanguage)langHome.find(getContext(),subLangId);
        }
        catch(Exception e)
        {
            langObj   = null;
            exception = e; 
        }

        if ( langObj == null )
        {
            logNullLangId(subLangId, exception);
        }

        return langObj;
    }

    private void logNullLangId(String subLangId, Exception e)
    {
        final String formattedMsg = MessageFormat.format(" \"{0}\" is not valid BillingLanguage Id.",
                new Object[]{subLangId});

        new MinorLogMsg(this, formattedMsg, e).log(getContext());
    }
}
