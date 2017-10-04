/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright &copy; Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.home;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.CUGTypeEnum;
import com.trilogy.app.crm.bean.CallingGroupTypeEnum;
import com.trilogy.app.crm.bean.ClosedUserGroup;
import com.trilogy.app.crm.bean.ClosedUserGroupHome;
import com.trilogy.app.crm.bean.ClosedUserGroupXInfo;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SuspendedEntity;
import com.trilogy.app.crm.bean.core.Msisdn;
import com.trilogy.app.crm.extension.account.FriendsAndFamilyExtension;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.CallingGroupSupport;
import com.trilogy.app.crm.support.ClosedUserGroupSupport;
import com.trilogy.app.crm.support.FrameworkSupportHelper;
import com.trilogy.app.crm.support.FriendsAndFamilyExtensionSupport;
import com.trilogy.app.crm.support.LicensingSupportHelper;
import com.trilogy.app.crm.support.SubscriberServicesSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.support.SuspendedEntitySupport;


/**
 * This class validates some user input for Closed User Groups.
 *
 * @author jimmy.ng@redknee.com
 */
public class ClosedUserGroupValidator
    implements Validator
{
    /**
     * Creates a new ClosedUserGroupValidator.
     *
     * @param ctx The operating context.
     */
    public ClosedUserGroupValidator()
    {
    }

    /**
     * @see com.redknee.framework.xhome.beans.Validator#validate(com.redknee.framework.xhome.context.Context, java.lang.Object)
     */
    public void validate(Context ctx,Object obj)
        throws IllegalStateException
    {
        
        final ClosedUserGroup cug = (ClosedUserGroup) obj;
    	
		final CompoundIllegalStateException exception = 
			new CompoundIllegalStateException();

		Map subMap = new HashMap();
            
        if (LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.CUG_MSISDN_VALIDATION_LICENSE)){
            
            // Validate SMS Notify User (if not empty).  
            final String user_msisdn = cug.getSmsNotifyUser().trim();
            if (user_msisdn.length() != 0)
            {
                Subscriber sub = null;
                try
                {
                    sub = SubscriberSupport.lookupSubscriberForMSISDN(ctx, user_msisdn);
                }
                catch (HomeException e)
                {
                    exception.thrown(
                        new IllegalPropertyArgumentException(
                            "SMS Notify User",
                            "Failed to get Subscription with MSISDN \"" + user_msisdn + "\""));      
                }
                
                if (sub != null)
                {
                    if (sub.getSpid() != cug.getSpid())
                    {
                        exception.thrown(
                            new IllegalPropertyArgumentException(
                                    ClosedUserGroupXInfo.SMS_NOTIFY_USER,
                                "Subscription with MSISDN \"" + user_msisdn + "\" must be in the same SPID as this CUG."));      
                    }
                    if (!SubscriberStateEnum.ACTIVE.equals(sub.getState()) && !SubscriberStateEnum.PROMISE_TO_PAY.equals(sub.getState()))
                    {
                        exception.thrown(
                            new IllegalPropertyArgumentException(
                                    ClosedUserGroupXInfo.SMS_NOTIFY_USER,
                                "Subscription with MSISDN \"" + user_msisdn + "\" must be in ACTIVE or PTP state."));      
                    }
                }
            }
    		
            // Validate the MSISDN list.
            validateSubscribersList(ctx, cug, subMap, exception);
        }
        else
        {
            warnSubscribersInDifferentSpid(ctx, cug, subMap, exception);
        }
        
        validateCugInSuspendedState(ctx, cug, subMap, exception);
        
        validateCugTemplateChange(ctx, cug, exception);
        
        validateAccountCugMembers(ctx, cug, subMap, exception);

        exception.throwAll();
    }
    
    private void validateSubscribersList(Context ctx, ClosedUserGroup cug, Map subMap, CompoundIllegalStateException exception)
    {
        final Iterator subs_itr = cug.getSubscribers().keySet().iterator();
        while (subs_itr.hasNext())
        {
            final String msisdn = (String) subs_itr.next();
    
            try
            {
                Subscriber sub = SubscriberSupport.lookupSubscriberForMSISDN(ctx, msisdn); 
                if (sub != null)
                {   
                    if (sub.getSpid() != cug.getSpid())
                    {
                        exception.thrown(
                                new IllegalPropertyArgumentException(
                                    ClosedUserGroupXInfo.SUBSCRIBERS,
                                    "Subscription with MSISDN \"" + msisdn + "\" is not in the same SPID as this CUG and therefore cannot be added to it."));      
                    }
                    else if (SubscriberStateEnum.INACTIVE.equals(sub.getState()))
                    {
                        exception.thrown(
                                new IllegalPropertyArgumentException(
                                    ClosedUserGroupXInfo.SUBSCRIBERS,
                                    "Subscription with MSISDN \"" + msisdn + "\" is DEACTIVATED and therefore cannot be added to this CUG."));      
                    
                    }else if (SubscriberStateEnum.IN_ARREARS.equals(sub.getState())){
                    	exception.thrown(
                                new IllegalPropertyArgumentException(
                                    ClosedUserGroupXInfo.SUBSCRIBERS,
                                    "Subscription with MSISDN \"" + msisdn + "\" is in In-Arrears State and therefore cannot be added to this CUG."));      
                    }
                    subMap.put(msisdn, sub);
                }   
                else
                {
                    exception.thrown(
                            new IllegalPropertyArgumentException(
                                ClosedUserGroupXInfo.SUBSCRIBERS,
                                "Subscription with MSISDN \"" + msisdn + "\" does not exist in the system and therefore cannot be added to this CUG."));      
                }
            }
            catch (Exception e)
            {
                exception.thrown(
                    new IllegalPropertyArgumentException(
                        "Subscribers",
                        "Failed to add MSISDN \"" + msisdn + "\": " + e.getMessage()));      
            }
        }
    }
    
    private void warnSubscribersInDifferentSpid(Context ctx, ClosedUserGroup cug, Map subMap, CompoundIllegalStateException exception)
    {
        final Iterator subs_itr = cug.getSubscribers().keySet().iterator();
        while (subs_itr.hasNext())
        {
            final String msisdn = (String) subs_itr.next();
    
            try
            {
                Subscriber sub = SubscriberSupport.lookupSubscriberForMSISDN(ctx, msisdn); 
                if (sub != null)
                {   
                    if (sub.getSpid() != cug.getSpid())
                    {
                        if (CUGTypeEnum.PrivateCUG.equals(cug.getTemplate(ctx).getCugType()))
                        {
                            FrameworkSupportHelper.get(ctx).notifyExceptionListener(ctx, new IllegalPropertyArgumentException(
                                    ClosedUserGroupXInfo.SUBSCRIBERS,
                                    "Subscription with MSISDN \"" + msisdn + "\" is not in the same SPID as this CUG and will be charged as an external MSISDN."));
                        }
                        else
                        {
                            FrameworkSupportHelper.get(ctx).notifyExceptionListener(ctx, new IllegalPropertyArgumentException(
                                    ClosedUserGroupXInfo.SUBSCRIBERS,
                                    "Subscription with MSISDN \"" + msisdn + "\" is not in the same SPID as this CUG."));
                        }
                    }
                    subMap.put(msisdn, sub);
                }   
            }
            catch (HomeException e)
            {
                exception.thrown(
                    new IllegalPropertyArgumentException(
                        "Subscribers",
                        "Failed to add MSISDN \"" + msisdn + "\": " + e.getMessage()));      
            }
        }
    }
    
    private void validateCugTemplateChange(Context ctx, 
            ClosedUserGroup cug,
            CompoundIllegalStateException exception)
    {
        try
        {
            final Long cugId = cug.getID();
            final Home home = (Home) ctx.get(ClosedUserGroupHome.class);
            if (cugId>0)
            {
                final ClosedUserGroup oldCug = (ClosedUserGroup) home.find(ctx, cugId);
                if (oldCug!=null)
                {
                    if (cug.getCugTemplateID() != oldCug.getCugTemplateID())
                    {
                        CUGTypeEnum newCugType = cug.getCugType(ctx);
                        CUGTypeEnum oldCugType = oldCug.getCugType(ctx);
                        if (!newCugType.equals(oldCugType))
                        {
                            StringBuilder sb = new StringBuilder();
                            sb.append("Cannot convert ");
                            if (CUGTypeEnum.PrivateCUG.equals(oldCugType))
                            {
                                exception.thrown(
                                        new IllegalPropertyArgumentException(
                                            ClosedUserGroupXInfo.CUG_TEMPLATE_ID,
                                            "Cannot convert PRIVATE CUG to PUBLIC CUG. Please choose another CUG Template."));
                            }
                            else
                            {
                                exception.thrown(
                                        new IllegalPropertyArgumentException(
                                            ClosedUserGroupXInfo.CUG_TEMPLATE_ID,
                                            "Cannot convert PUBLIC CUG to PRIVATE CUG. Please choose another CUG Template."));
                            }
                        }
                    }
                }
            }
        }
        catch (Throwable e)
        {
            LogSupport.minor(ctx, this, "Error while retrieving CUG template to validate update for CUG " + cug.getID() + ": " + e.getMessage(), e);
        }
    }

    public void validateCugInSuspendedState(
            Context ctx, 
            ClosedUserGroup cug,
            Map subMap,
            CompoundIllegalStateException exception) 
    {
        // Validate the MSISDN list.
        final Iterator subs_itr = cug.getSubscribers().keySet().iterator();

        try
        {  
            final AuxiliaryService auxSvc = 
                CallingGroupSupport.getAuxiliaryServiceForCUGTemplate(
                        ctx, 
                        cug.getCugTemplateID());

            while (subs_itr.hasNext())
            {
                final String msisdn = (String) subs_itr.next();
                
                // try to retrieve it from the cache
                Subscriber subInCache = (Subscriber)subMap.get(msisdn);
                
                // retrieve the subscriber ID 
                String subId = null;
                if (subInCache == null)
                {
                    final Msisdn msisdnObject = SubscriberSupport.lookupMsisdnObjectForMSISDN(ctx, msisdn);
                    if (msisdnObject == null || msisdnObject.getSubscriberID() == null)
                    {
                        // no need to check suspended state if the subscriber does not exist
                        continue;
                    }
                    subId = msisdnObject.getSubscriberID();
                }
                else
                {
                    subId = subInCache.getId();
                }
                
                
                SuspendedEntity entity = SuspendedEntitySupport.findSuspendedEntity(ctx, 
                        subId, 
                        auxSvc.getIdentifier() , 
                        cug.getID(), 
                        AuxiliaryService.class);
                
                if (entity != null)
                {
                    exception.thrown(
                            new IllegalPropertyArgumentException(
                                "Subscribers",
                                "Failed to add MSISDN \"" + msisdn 
                                + "\" because the CUG service is currently suspended due to the insufficient balance. "));      
                }
               
            }
        }
        catch (Exception e)
        {
            exception.thrown(
                    new IllegalPropertyArgumentException(
                        "Subscribers",
                        "Failed to validate CUG in suspended state " + e.getMessage()));
            new MinorLogMsg(this, "Error in validateCugInSuspendedState", e).log(ctx);
        }
    
    }
    
    
    
    static public boolean isMsisdnValid(final Context ctx, final int spid, final String msisdn)
     {
        Subscriber sub = null;
        try
        {
            sub = SubscriberSupport.lookupSubscriberForMSISDN(ctx, msisdn);
        }
         catch (HomeException e)
         {
            return false;  
          }
                                       
          if (sub != null)
          {
              if (sub.getSpid() != spid)
              {
                  return false; 
              }
              return true; 
            }
          
          return false; 
     }     
    
    
    public void validateAccountCugMembers(Context ctx, ClosedUserGroup cug, Map subMap,
            CompoundIllegalStateException exception)
    {
    	if (ctx.has(SKIP_CUG_ACCOUNT_VALIDATION))
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport
                        .debug(ctx, this,
                                "CUG service has been provisioned/unprovisioned to the account. No Account Cug valdation required.");
            }
            return;
        }
        Iterator subs_itr = null; 
        String cugAccountBan = null;
        boolean allowMemberOutsideGroup = false;
        try
        {
            cugAccountBan = ClosedUserGroupSupport.getCugAccount(ctx, cug.getID());
            if(cugAccountBan != null) 
            {
            	 ClosedUserGroup finalCug = ClosedUserGroupSupport.getCug(ctx, cugAccountBan);
            	 allowMemberOutsideGroup = finalCug.getAuxiliaryService(ctx).isMembersOutsideGroupHierarchy();
            	 if(allowMemberOutsideGroup)
            	 {
            		 cugAccountBan = null;
            	 }
            	 else
            	 {
            		 if(finalCug.getSubscribers().keySet().size() > cug.getSubscribers().keySet().size())
                     {
                     	subs_itr = finalCug.getSubscribers().keySet().iterator();
                     }
                     else
                     {
                     	subs_itr = cug.getSubscribers().keySet().iterator();
                     }
            	 }
            }
        }
        catch (HomeException e1)
        {
            exception.thrown(new IllegalPropertyArgumentException(ClosedUserGroupXInfo.BAN,
            		"Failed to retrieve Account associated with CUG : " + cug.getID()));
        }
        if (cugAccountBan != null)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, "Cug is associated with BAN : " + cugAccountBan
                        + ". Cug Account validation required");
            }
            while (subs_itr.hasNext())
            {
                final String msisdn = (String) subs_itr.next();
                // try to retrieve it from the cache
                Subscriber sub = (Subscriber) subMap.get(msisdn);
                try
                {
                    if (sub == null)
                    {
                        sub = SubscriberSupport.lookupSubscriberForMSISDN(ctx, msisdn);
                        if (sub != null)
                        {
                            subMap.put(msisdn, sub);
	                        if (sub.getRootAccount(ctx).getBAN().equals(cugAccountBan) && !(cug.getOwner(ctx).getBAN().equals(sub.getRootAccount(ctx).getBAN())))
	                        {
	                            exception.thrown(new IllegalPropertyArgumentException(ClosedUserGroupXInfo.SUBSCRIBERS,
	                                    "Subscription with MSISDN \"" + msisdn
	                                            + "\" is a child account of " + cugAccountBan + ". Hence this is not allowed."));
	                        }
                        }
                        else if(!(cug.getAuxiliaryService(ctx).getMembersOutsideGroupHierarchy()))
                        {
                        	
                        	exception.thrown(
                                    new IllegalPropertyArgumentException(
                                        ClosedUserGroupXInfo.SUBSCRIBERS,
                                        "Subscription with MSISDN \"" + msisdn + "\" does not exist in the system and Auxiliary service for this CUG does not allow members outside group hierarchy."));
                        }
                    }
                    if (sub != null)
                    {                        
                        Account account = sub.getAccount(ctx);
                        Account rootAccount = account.getRootAccount(ctx);
                        if(!(cug.getAuxiliaryService(ctx).getMembersOutsideGroupHierarchy()))
                        {

                             if (!cugAccountBan.equals(rootAccount.getBAN()))
                             {
                                 exception.thrown(new IllegalPropertyArgumentException(ClosedUserGroupXInfo.SUBSCRIBERS,
                                         "Subscription with MSISDN \"" + msisdn + "\" does not belong to Account \""
                                                 + cugAccountBan + "\""));
                             }
                             if (!sub.isPooledGroupLeader(ctx) && !subscriberHasCugService(ctx, sub.getId()))
                             {
                                 exception.thrown(new IllegalPropertyArgumentException(ClosedUserGroupXInfo.SUBSCRIBERS,
                                         "Subscription with MSISDN \"" + msisdn
                                                 + "\" does not have any Calling Group Service."));
                             }

                        }
                       
                    }
                    // subscriber should never be null
                }
                catch (HomeException e)
                {
                    exception.thrown(new IllegalPropertyArgumentException("Subscribers", "Failed to add MSISDN \""
                            + msisdn + "\": " + e.getMessage()));
                }
            }
        }
    }

    static private boolean subscriberHasCugService(final Context ctx, final String subscriberId)
    {
    	Collection col = SubscriberServicesSupport.getProvisionedSubscriberServices(ctx, subscriberId);
        for (Object obj : col)
        {
            SubscriberServices subServ = (SubscriberServices) obj;
            Service service = subServ.getService();
            if (service.getType() == ServiceTypeEnum.CALLING_GROUP
                    && service.getCallingGroupType() == CallingGroupTypeEnum.CUG_INDEX)
            {
                return true;
            }
        }
        return false;
    }
    
    /**
     * For all the cug operations which will be initiated through CUG Service Provisioning this key would be present in the context.
     * All such operations do not require CUG Account validation as these are triggered only when such services are added.
     * An other request which comes through API or GUI need to be validated for Account CUG and this key will not be present in Context for such requests.
     */
    public static String SKIP_CUG_ACCOUNT_VALIDATION = "SKIP_CUG_ACCOUNT_VALIDATION";
}
