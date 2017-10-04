/**
 * 
 */
package com.trilogy.app.crm.support;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.ClosedSub;
import com.trilogy.app.crm.bean.ClosedUserGroup;
import com.trilogy.app.crm.bean.ClosedUserGroupHome;
import com.trilogy.app.crm.bean.ClosedUserGroupTemplate;
import com.trilogy.app.crm.bean.ClosedUserGroupTemplateHome;
import com.trilogy.app.crm.bean.ClosedUserGroupTemplateXInfo;
import com.trilogy.app.crm.bean.ClosedUserGroupXInfo;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.SuspendedEntity;
import com.trilogy.app.crm.bean.SystemNoteSubTypeEnum;
import com.trilogy.app.crm.bean.SystemNoteTypeEnum;
import com.trilogy.app.crm.bean.core.AuxiliaryService;
import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.extension.ExtensionHolder;
import com.trilogy.app.crm.extension.account.AbstractFriendsAndFamilyExtension;
import com.trilogy.app.crm.extension.account.FriendsAndFamilyExtension;
import com.trilogy.app.crm.extension.account.FriendsAndFamilyExtensionHome;
import com.trilogy.app.crm.extension.account.FriendsAndFamilyExtensionXInfo;
import com.trilogy.app.crm.ff.FFClosedUserGroupSupport;
import com.trilogy.app.crm.ff.FFEcareException;
import com.trilogy.app.ff.ecare.rmi.FFECareRmiService;
import com.trilogy.app.ff.ecare.rmi.TrCug;
import com.trilogy.app.ff.ecare.rmi.TrCugHolder;
import com.trilogy.app.ff.ecare.rmi.TrCugHolderImpl;
import com.trilogy.app.ff.ecare.rmi.TrCugListHolder;
import com.trilogy.app.ff.ecare.rmi.TrCugListHolderImpl;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.elang.Not;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.elang.Value;
import com.trilogy.framework.xhome.elang.Wildcard;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.msp.MSP;
import com.trilogy.framework.xhome.msp.Spid;
import com.trilogy.framework.xhome.msp.SpidAwareHomePredicate;
import com.trilogy.framework.xhome.visitor.ListBuildingVisitor;
import com.trilogy.framework.xhome.visitor.Visitors;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * @author abaid
 *
 */
public class ClosedUserGroupSupport
{
    
	public static final String            SUBSCRIBER_AS_CUGMEMEBR = "subscriberAsCUGMember";
	
    public static ClosedUserGroupTemplate getCugTemplate(final Context ctx, final long cugTemplateId, int spid)
            throws HomeException
    {
        Home cugTemplateHome = (Home) ctx.get(ClosedUserGroupTemplateHome.class);
        And filter = new And();
        filter.add(new EQ(ClosedUserGroupTemplateXInfo.SPID, Integer.valueOf(spid)));
        filter.add(new EQ(ClosedUserGroupTemplateXInfo.ID, Long.valueOf(cugTemplateId)));
        ClosedUserGroupTemplate cugT = (ClosedUserGroupTemplate) cugTemplateHome.find(ctx, filter);
        return cugT;
    }
    

    public static ClosedUserGroup getCUG(Context ctx, long cugId, int spid) throws HomeException
    {
        Home cugHome = (Home) ctx.get(ClosedUserGroupHome.class);
        And filter = new And();
        filter.add(new EQ(ClosedUserGroupXInfo.SPID, Integer.valueOf(spid)));
        filter.add(new EQ(ClosedUserGroupXInfo.ID, Long.valueOf(cugId)));
        final ClosedUserGroup oldCug = (ClosedUserGroup) cugHome.find(ctx, filter);
        if (oldCug == null)
        {
            String msg = "Failed to find the existing CUG instance " + cugId;
            new MinorLogMsg(ClosedUserGroupSupport.class.getName(), msg, null).log(ctx);
            throw new HomeException(msg);
        }
        return oldCug;
    }
    
    /**
     * query the cug template by cug Id.
     * @param ctx
     * @param cugId
     * @return
     * @throws HomeException
     */
    public static ClosedUserGroupTemplate getCugTemplate(final Context ctx, final long cugTemplateId) throws HomeException
    {
        Home cugTemplateHome = (Home) ctx.get(ClosedUserGroupTemplateHome.class);
        ClosedUserGroupTemplate cugT = (ClosedUserGroupTemplate) cugTemplateHome.find(ctx,
                Long.valueOf(cugTemplateId));
        return cugT;
    }

    /**
     * Returns the CUG associated with an account.  This association exists in the form of a FnF Account Extension.
     *
     * @param context
     *            The operating context.
     * @param ban
     *            Account BAN
     * @return The CUG with the provided ID.
     * @throws HomeException
     *             Thrown by home.
     */
    public static ClosedUserGroup getCug(final Context context, final String ban) throws HomeException
    {
        ClosedUserGroup cug = null;
        
        Home fnfExtHome = (Home)context.get(FriendsAndFamilyExtensionHome.class);
        if( fnfExtHome != null )
        {
            FriendsAndFamilyExtension ext = (FriendsAndFamilyExtension)fnfExtHome.find(context, ban);
            if( ext == null 
                    || ext.getCugID() == AbstractFriendsAndFamilyExtension.DEFAULT_CUGID )
            {
                new InfoLogMsg(ClosedUserGroupSupport.class, "No CUG associated with account " + ban + ". Ensure that a Friends And Family extension is configured in the account.", null).log(context);
            }
            else
            {
                Home home = (Home) context.get(ClosedUserGroupHome.class);
                
                cug = (ClosedUserGroup) home.find(context, Long.valueOf(ext.getCugID()));  
            }
        }
        
        return cug;        
    }


    /**
     * Returns the CUG with the provided ID.
     *
     * @param context
     *            The operating context.
     * @param cugId
     *            CUG ID.
     * @return The CUG with the provided ID.
     * @throws HomeException
     *             Thrown by home.
     */
    public static ClosedUserGroup getCug(final Context context, final long cugId) throws HomeException
    {

        Home home = (Home) context.get(ClosedUserGroupHome.class);
        
        ClosedUserGroup cug = (ClosedUserGroup) home.find(context, Long.valueOf(cugId));
        return cug;
    }
    
    /**
     * Adds a subscriber to a CUG.
     *
     * @param context
     *            The operating context.
     * @param cugId
     *            CUG ID.
     * @param msisdn
     *            MSISDN of the subscriber to be added to the CUG.
     * @param setSmsNotificationMsisdn
     *            a flag indicates whether to set the smsNotifyMsisdn in the CUG
     * @throws HomeException
     *             Thrown by home.
     */
    public static void addSubscriberToCug(
            final Context context, 
            final long cugId, 
            final String msisdn,
            final boolean setSmsNotificationMsisdn)
        throws HomeException
    {
        Home home = (Home) context.get(ClosedUserGroupHome.class);
        
        ClosedUserGroup cug = (ClosedUserGroup) home.find(context, Long.valueOf(cugId));
        
        if (cug != null)
        {
            if (setSmsNotificationMsisdn)
            {
                if (cug.getSmsNotifyUser() == null || cug.getSmsNotifyUser().trim().length() == 0)
                {
                    cug.setSmsNotifyUser(msisdn);
                }
            }
            ClosedSub closedSub = new ClosedSub();
            closedSub.setPhoneID(msisdn);
            cug.getSubscribers().put(msisdn,closedSub);
            
            AuxiliaryService auxService = cug.getAuxiliaryService(context);
           
            if(auxService != null)
            {
            	if(auxService.getAggPPServiceChargesToCUGOwner())
                {
            	    BooleanHolder alreadyPresent = (BooleanHolder)context.get(SUBSCRIBER_AS_CUGMEMEBR);
            	    if(alreadyPresent !=null)
            	    {
            	        alreadyPresent.setBooleanValue(true);
            	    }
            	    else
            	    {
            	        context.put(SUBSCRIBER_AS_CUGMEMEBR, new BooleanHolder(true));
            	    }
                }
            }
            
            
            
            home.store(context, cug);
            
            // check whether the subscriber is actually provisioned
            ClosedUserGroup returnCug = (ClosedUserGroup) home.find(context, Long.valueOf(cugId));
            if (returnCug == null || returnCug.getSubscribers() == null || (!returnCug.getSubscribers().containsKey(msisdn)))
            {
                ArrayList msisdnFailureList = new ArrayList();
                msisdnFailureList.add(msisdn);
                throw new AutoCugProvisioningException(
                        "Fail to provision the subscriber " + msisdn + " to the CUG " + cug.getID(), 
                        returnCug, 
                        msisdnFailureList,
                        null);
            }
            
            updateSmsNotification(context, returnCug);

        }
        else
        {
            throw new HomeException("Fail to add the subscriber to the CUG instance because the CUG " + cugId + " does not exist");
        }
        
    }

    /**
     * Adds a subscriber to a CUG.
     *
     * @param context
     *            The operating context.
     * @param cugId
     *            CUG ID.
     * @param subscriber
     *            Subscriber to be added to the CUG.
     * @param setSmsNotificationMsisdn
     *            a flag indicates whether to set the smsNotifyMsisdn in the CUG
     * @throws HomeException
     *             Thrown by home.
     */
    public static void addSubscriberToCug(final Context context, final long cugId, 
            final Subscriber subscriber, final boolean setSmsNotificationMsisdn)
        throws HomeException
    {
        final String msisdn = subscriber.getMSISDN();
        
        Context subCtx = context.createSubContext();
        subCtx.put(Subscriber.class, subscriber);
        if (!subscriber.isPooledGroupLeader(context))
        {
            addSubscriberToCug(subCtx, cugId, msisdn, setSmsNotificationMsisdn);
        }
    }

    /** 
     * updateSmsNotification
     * @param ctx
     * @param cug
     */
    public static void updateSmsNotification(Context ctx, ClosedUserGroup cug)
    {
        Account acct = (Account) ctx.get(Account.class);
        
        if (acct == null || acct.getAccountExtensions() == null || cug == null)
        {
            return;
        }
        for (Iterator ite = acct.getAccountExtensions().iterator(); ite.hasNext();)
        {
            ExtensionHolder extensionHolder = (ExtensionHolder)ite.next();
            Extension extension = extensionHolder.getExtension();
            if (extension != null)
            {
                if (FriendsAndFamilyExtension.class.isAssignableFrom(extension.getClass()))
                {
                    FriendsAndFamilyExtension fnfExt = (FriendsAndFamilyExtension) extension;
                    fnfExt.setSmsNotificationMSISDN(cug.getSmsNotifyUser());
                    fnfExt.setCugOwnerMsisdn(cug.getOwnerMSISDN());
                }
            }
        }
    }
    
    /**
     * Removes a subscriber from a CUG.
     *
     * @param context
     *            The operating context.
     * @param cugId
     *            CUG ID.
     * @param msisdn
     *            MSISDN of the subscriber to be removed from the CUG.
     * @throws HomeException
     *             Thrown by home.
     */
    public static void removeSubscriberFromCug(final Context context, final long cugId, final String msisdn)
        throws HomeException
    {
        Home home = (Home) context.get(ClosedUserGroupHome.class);
        
        ClosedUserGroup cug = (ClosedUserGroup) home.find(context, Long.valueOf(cugId));
        if (cug != null)
        {
            cug.getSubscribers().remove(msisdn);
            
            if( SafetyUtil.safeEquals(msisdn, cug.getSmsNotifyUser()) )
            {
                if( cug.getSubscribers().size() > 0 )
                {
                    cug.setSmsNotifyUser((String)cug.getSubscribers().keySet().iterator().next());
                }
                else
                {
                    cug.setSmsNotifyUser("");
                }
                new InfoLogMsg(ClosedUserGroupSupport.class, 
                        "SMS Notification User " + msisdn
                        + " removed from CUG " + cugId+ ".  "
                        + "Setting new SMS Notification User = '" + cug.getSmsNotifyUser() + "'", null).log(context);
            }
            
            AuxiliaryService auxService = cug.getAuxiliaryService(context);
            
            if(auxService != null)
            {
                if(auxService.getAggPPServiceChargesToCUGOwner())
                {
                    BooleanHolder alreadyPresent = (BooleanHolder)context.get(SUBSCRIBER_AS_CUGMEMEBR);
                    if(alreadyPresent !=null)
                    {
                        alreadyPresent.setBooleanValue(true);
                    }
                    else
                    {
                        context.put(SUBSCRIBER_AS_CUGMEMEBR, new BooleanHolder(true));
                    }
                }
            }
            
            home.store(context, cug);
            
            // check whether the subscriber is actually provisioned
            ClosedUserGroup returnCug = (ClosedUserGroup) home.find(context, Long.valueOf(cugId));
            if (returnCug != null && returnCug.getSubscribers() != null &&
                    returnCug.getSubscribers().containsKey(msisdn))
            {
                ArrayList msisdnFailureList = new ArrayList();
                msisdnFailureList.add(msisdn);
                throw new AutoCugProvisioningException(
                        "Fail to remove the subscriber " + msisdn + " from the CUG " + cug.getID(), 
                        returnCug, 
                        msisdnFailureList,
                        null);
            }
            updateSmsNotification(context, returnCug);


        }
        else
        {
            throw new HomeException("Fail to remove subscriber from the CUG instance because the CUG " 
                    + cugId + " does not exist");
        }
    }


    /**
     * Removes a subscriber from a CUG.
     *
     * @param context
     *            The operating context.
     * @param cugId
     *            CUG ID.
     * @param subscriber
     *            Subscriber to be removed from the CUG.
     * @throws HomeException
     *             Thrown by home.
     */
    public static void removeSubscriberFromCug(final Context context, final long cugId, final Subscriber subscriber)
        throws HomeException
    {
        final String msisdn = subscriber.getMSISDN();
        Context subCtx = context.createSubContext();
        subCtx.put(Subscriber.class, subscriber);

        removeSubscriberFromCug(subCtx, cugId, msisdn);
    }
    
    
    /**
     * This method create cug instance for account and
     * add all the subscriber in this account to the CUG.
     * @param ctx
     * @param account
     * @param cugTemplateID
     * @return ClosedUserGroup
     * @throws AutoCugProvisioningException
     */
    public static ClosedUserGroup createCugForAccount(Context ctx, 
            Account account, 
            long cugTemplateID, String cugOwnerMsisdn) throws AutoCugProvisioningException
    {
        Home home = (Home) ctx.get(ClosedUserGroupHome.class);
        ClosedUserGroup returnCug = null;
        try
        {
            ctx = ctx.createSubContext();
            MSP.setBeanSpid(ctx, account.getSpid());

            Collection subs = getEligibleSubsFromAccount(ctx, account, true);
            
            // create the CUG
            ClosedUserGroup cug = new ClosedUserGroup();
            cug.setSpid(account.getSpid());
            cug.setCugTemplateID(cugTemplateID);
            if (cug.getCugTemplateID()>0)
            {
                ClosedUserGroupTemplate cugTemplate = ClosedUserGroupSupport.getCugTemplate(ctx, cug.getCugTemplateID());
                cug.setSpid(cugTemplate.getSpid());
            }
            cug.setSmsNotifyUser(findSmsNotifyUser(ctx, account, subs));
            // Find out if this should be account.ownerMsisdn or ownerMsisdn is to be passed from GUI.
            cug.setOwnerMSISDN(cugOwnerMsisdn);
            
            for (Iterator ite = subs.iterator(); ite.hasNext();)
            {
                Subscriber sub = (Subscriber) ite.next();
                if (!sub.isPooledGroupLeader(ctx))
                {
                    ClosedSub closedSub = new ClosedSub();
                    closedSub.setPhoneID(sub.getMSISDN());
                    cug.getSubscribers().put(sub.getMSISDN(),closedSub);
                }
            }
            
            returnCug =  (ClosedUserGroup)home.create(ctx, cug);
            returnCug = (ClosedUserGroup) home.find(ctx, Long.valueOf(returnCug.getID()));
            
            // check if all the subscribers are provisioned to CUG properly
            if (returnCug.getSubscribers().size() != cug.getSubscribers().size())
            {
                HashSet<String> msisdnFailureList = new HashSet<String>(cug.getSubscribers().keySet());
                msisdnFailureList.removeAll(returnCug.getSubscribers().keySet());
                throw new AutoCugProvisioningException(
                        "Fail to provision one or more subscribers of the account to the CUG ", 
                        returnCug, 
                        msisdnFailureList,
                        null);
            }
            
            return returnCug;

        }
        catch (AutoCugProvisioningException ace)
        {
            new MinorLogMsg(ClosedUserGroupSupport.class, 
                    "Error in createCugForAccount: " + account.getBAN(), ace).log(ctx);
            throw ace;
            
        }
        catch (Exception e)
        {
            new MinorLogMsg(ClosedUserGroupSupport.class, 
                    "Error in createCugForAccount: " + account.getBAN(), e).log(ctx);
            throw new AutoCugProvisioningException(
                    e.getMessage(), 
                    returnCug, 
                    null,
                    e);
         }
    }
    
    /**
     * This method update the cug instance with the new CUG template ID. 
     * Also, this method will add the subscribers to the CUG if they are
     * immediate children of the account and they have not been added to the CUG
     * @param ctx
     * @param account
     * @param cugId
     * @param cugTemplateID
     * @return ClosedUserGroup
     * @throws AutoCugProvisioningException
     */
    public static ClosedUserGroup updateCugForAccount(
            Context ctx, 
            Account account, 
            long cugId, 
            long cugTemplateID,
            String smsNotifyMsisdn) throws AutoCugProvisioningException
    {
        
        Home home = (Home) ctx.get(ClosedUserGroupHome.class);
        ClosedUserGroup cug = null;
        ClosedUserGroup returnCug = null;
        
        try
        {
            cug = (ClosedUserGroup) home.find(ctx, Long.valueOf(cugId));
            
            if (cug == null)
            {
                throw new AutoCugProvisioningException("The specified cug " + cugId + " does not exist. ",
                        null,
                        null,
                        null);
            }
            
            final AuxiliaryService auxSvc = 
                CallingGroupSupport.getAuxiliaryServiceForCUGTemplate(
                        ctx, 
                        cug.getCugTemplateID());
            
            if (auxSvc == null)
            {
                throw new AutoCugProvisioningException("The aux service does not exist for the cug template " 
                        + cug.getCugTemplateID(),
                        null,
                        null,
                        null);
                
            }
            
            Collection subs = getEligibleSubsFromAccount(ctx, account, true);
            
            // update the CUG
            cug.setCugTemplateID(cugTemplateID);
            
            // update sms notify user
            if (smsNotifyMsisdn != null && smsNotifyMsisdn.length() > 0 )
            {
                cug.setSmsNotifyUser(smsNotifyMsisdn);
            }
            else if (cug.getSmsNotifyUser() == null || cug.getSmsNotifyUser().length() ==0)
            {
                cug.setSmsNotifyUser(findSmsNotifyUser(ctx, account, subs));
            }
            
            // update the CUG with the subscribers in the account that haven't been added yet
            for (Iterator ite = subs.iterator(); ite.hasNext();)
            {
                Subscriber sub = (Subscriber) ite.next();
                
                if(subscriberHasCallingGroupAuxService(ctx, sub))
                {
                	SuspendedEntity suspendEntity = SuspendedEntitySupport.findSuspendedEntity(ctx, 
                            sub.getId(), 
                            auxSvc.getIdentifier(), 
                            cug.getID(), 
                            AuxiliaryService.class);
                    
                    if (suspendEntity == null && !sub.isPooledGroupLeader(ctx))
                    {    
                        ClosedSub closedSub = new ClosedSub();
                        closedSub.setPhoneID(sub.getMSISDN());
                        cug.getSubscribers().put(sub.getMSISDN(),closedSub);
                    }
                }
            }
            
            Context subCtx = ctx.createSubContext();
            //subCtx.put(Subscriber.class, account.getSubscriber());
            home.store(subCtx, cug);
            returnCug = (ClosedUserGroup) home.find(ctx, Long.valueOf(cugId));
            
            // check if all the subscribers are provisioned to CUG properly
            if (!returnCug.getSubscribers().equals(cug.getSubscribers()))
            {
                // find the list of new subs that cannot be added
                HashSet<String> msisdnFailureList = new HashSet<String>(cug.getSubscribers().keySet());
                msisdnFailureList.removeAll(returnCug.getSubscribers().keySet());
                
                throw new AutoCugProvisioningException(
                        "Fail to provision all the subscribers of the account to the CUG", 
                        returnCug, 
                        msisdnFailureList,
                        null);
            }
            
            return returnCug;

        }
        catch (AutoCugProvisioningException ace)
        {
            new MinorLogMsg(ClosedUserGroupSupport.class, 
                    "Error in updateCugForAccount: " + account.getBAN(), ace).log(ctx);
            throw ace;
            
        }
        catch (Exception e)
        {
            new MinorLogMsg(ClosedUserGroupSupport.class, 
                    "Error in updateCugForAccount: " + account.getBAN(), 
                    e).log(ctx);
            throw new AutoCugProvisioningException(
                    e.getMessage(), 
                    returnCug, 
                    null,
                    e);
         }
    
    }
    
    /**
     * This method remove CUG instance for the account
     * @param ctx
     * @param account
     * @param cugId
     * @throws AutoCugProvisioningException
     */
    public static void removeCugForAccount(
            Context ctx,
            Account account, 
            long cugId) throws AutoCugProvisioningException
    {
        Home home = (Home) ctx.get(ClosedUserGroupHome.class);
        ClosedUserGroup cug = null;
        ClosedUserGroup returnCug = null;
        
        try
        {
            cug = (ClosedUserGroup) home.find(ctx, Long.valueOf(cugId));
            
            if (cug == null)
            {
                throw new AutoCugProvisioningException("The specified cug " + cugId + " does not exist",
                        null,
                        null,
                        null);
            }
            
            Context subCtx = ctx.createSubContext();
            if (account != null)
            {
                subCtx.put(Subscriber.class, account.getSubscriber());
            }
            
            home.remove(subCtx, cug);
            

        }
        catch (AutoCugProvisioningException ace)
        {
            new MinorLogMsg(ClosedUserGroupSupport.class, 
                    "Error in removeCugForAccount: " + account.getBAN(), ace).log(ctx);
            throw ace;
            
        }
        catch (Exception e)
        {
            new MinorLogMsg(ClosedUserGroupSupport.class, 
                    "Fail to remove CUG for account " + account.getBAN(), e).log(ctx);
            throw new AutoCugProvisioningException(
                    e.getMessage(), 
                    null, 
                    null,
                    e);
         }
    }
    
    /**
     * find SmsNotifyUser for the auto-CUG feature
     * @param ctx
     * @param acct - the account object
     * @param subList - the list of subscribers for this account
     * @return smsNotifyUser. Return group leader MSISDN if it exists or
     * return first subscriber found in the list. Return empty string if there is 
     * no subscriber in the account.
     */
    public static String findSmsNotifyUser(final Context ctx, 
            final Account acct,
            Collection<Subscriber> subList)    
    {
        if (acct.getOwnerMSISDN() != null && acct.getOwnerMSISDN().length() > 0)
        {
            return acct.getOwnerMSISDN();
        }
        else
        {
            if (subList.size() > 0)
            {
                for (Subscriber sub : subList)
                {
                    if (!sub.isPooledGroupLeader(ctx))
                    {
                        return sub.getMSISDN();
                    }
                }
            }
        }

        return "";
       
    }
    
    /**
     * Get eligible subscribers from account for importing. 
     * @param ctx
     * @param ban
     * @param retporErrorImmediately boolean to indicate whether the exceptions should be thrown out
     * @return Collection that contains all the non-inactive subscribers for the account
     */
    public static Collection getEligibleSubsFromAccount(
            final Context ctx, 
            final Account acct,
            boolean reportErrorImmediately) throws Exception
    {
        
        Collection subsCollection = null;
        try
        {
            
            subsCollection = AccountSupport.getAllSubscribers(ctx, acct);
            subsCollection = getEligibleSubsFromCollection(ctx, subsCollection);
            
        }
        catch(Exception e)
        {
            new MinorLogMsg(ClosedUserGroupSupport.class, "Exception when looking up subscribers in account " + acct.getBAN(), e).log(ctx);
            
            if (reportErrorImmediately)
            {
                throw e;
            }
            else
            {
                final ExceptionListener exceptions = (ExceptionListener) ctx.get(ExceptionListener.class);
                if (exceptions != null)
                {
                    exceptions.thrown(new IllegalPropertyArgumentException("Account",
                    "Exception when looking up subscribers in the account " + acct.getBAN()));
                }
            }
        }
        
        return subsCollection;
    }
    
    /**
     * Filter invalid state subscribers from the collection passed in
     * @param ctx
     * @param subs
     * @return
     * @throws HomeException
     * @throws AgentException
     */
    public static Collection getEligibleSubsFromCollection(final Context ctx, Collection subs) throws HomeException, AgentException
    {
        Set subStates = new HashSet();
        //subStates.add(SubscriberStateEnum.PENDING);
        subStates.add(SubscriberStateEnum.INACTIVE);
        subStates.add(SubscriberStateEnum.MOVED);
        subStates.add(SubscriberStateEnum.AVAILABLE);
        
        ListBuildingVisitor subRet = new ListBuildingVisitor();
        Visitors.forEach(ctx, subs, subRet, new Not(new In (SubscriberXInfo.STATE, subStates)));            
        return subRet;
    }
    
    
	/**
	 * Return the remote Friends & Family RMI service.
	 * 
	 * @return FFECareRmiService The Friends & Family RMI service.
	 */
	static private FFECareRmiService getRmiService(Context ctx) throws FFEcareException
	{
		return FFClosedUserGroupSupport.getFFRmiService(ctx, ClosedUserGroupSupport.class);
	}

	
	/**
	 * Return the Closed User Group having the given CUG ID (with the
	 * corresponding Friends & Family Remote service).
	 * 
	 * @param id The given CUG ID.
	 * 
	 * @return ClosedUserGroup The result Closed User Group; or null if
	 * no matching Closed User Group could be found.
	 */
    static public ClosedUserGroup getCUGByID(Context ctx, final long id) throws HomeException, UnsupportedOperationException
    {
         ClosedUserGroup cug = null;
        try
        {
            final TrCugHolder holder = new TrCugHolderImpl();
            int result = getRmiService(ctx).getCUG(UserSupport.getSpid(ctx), id, holder);
            if (holder != null && holder.getValue() != null)
            {
                cug = FFClosedUserGroupSupport.convertFFCugToCrmCug(ctx, holder.getValue());
            }
        }
        catch (FFEcareException e)
        {
            throw new HomeException(e);
        }
        catch (Throwable e)
        {
            String msg = "Failed to get CUG with Id: " + id;
            new MinorLogMsg(ClosedUserGroupSupport.class, msg, e).log(ctx);
            throw new HomeException(msg);
        }
        return cug;
    }

    
    
    /**
     * Return a list of Closed User Groups starting with the given CUG name (with the
     * corresponding Friends & Family Remote service).
     * 
     * @param name
     *            The given CUG name.
     * 
     * @return Collection The result list of Closed User Groups.
     */
    static public Collection<ClosedUserGroup> getCUGByName(Context ctx, final String name) throws HomeException,
            UnsupportedOperationException
    {
        TrCug[] trCugArray = null;
        try
        {
            final TrCugListHolder holder = new TrCugListHolderImpl();
            getRmiService(ctx).getCUGByName(UserSupport.getSpid(ctx), name, holder);
            if (holder != null && holder.getValue() != null)
            {
                trCugArray = holder.getValue().trCugList;
            }
        }
        catch (FFEcareException e)
        {
            throw new HomeException(e);
        }
        catch (Throwable e)
        {
            String msg = "Failed to get CUGs with name: " + name;
            new MinorLogMsg(ClosedUserGroupSupport.class, msg, e).log(ctx);
            throw new HomeException(msg);
        }
        final Collection<ClosedUserGroup> cugList = new ArrayList<ClosedUserGroup>();
        if (trCugArray != null)
        {
            for (int i = 0; i < trCugArray.length; i++)
            {
                cugList.add(FFClosedUserGroupSupport.convertFFCugToCrmCug(ctx, trCugArray[i]));
            }
        }
        return cugList;
    }
    
    
    public static void addAccountCugNote(Context ctx, Account account, String msg)
    {
        try
        {
            if (account != null)
            {
                NoteSupportHelper.get(ctx).addAccountNote(ctx, account.getBAN(), msg, SystemNoteTypeEnum.EVENTS,
                        SystemNoteSubTypeEnum.ACCUPDATE);
            }
        }
        catch (HomeException e)
        {
            LogSupport.minor(ctx, ClosedUserGroupSupport.class.getName(),
                    "Failed to add account notes for cug. account: " + account);
        }
    }
    
    
    public static String getCugAccount(final Context context, final long cugId) throws HomeException
    {
        Home fnfExtHome = (Home) context.get(FriendsAndFamilyExtensionHome.class);
        if (fnfExtHome != null)
        {
            FriendsAndFamilyExtension ext = (FriendsAndFamilyExtension) fnfExtHome.find(context, new EQ(
                    FriendsAndFamilyExtensionXInfo.CUG_ID, cugId));
            if (ext != null)
            {                
                return ext.getBAN();
            }
        }
        return null;
    }
    
    
    public static Collection getApplicableSpids(Context ctx, Integer spid) throws HomeException, HomeInternalException
    {
        if (spid != null)
        {
            Collection spids = new ArrayList();
            Spid s = new Spid();
            s.setSpid(spid.intValue());
            spids.add(s);
            return spids;
        }
        Principal p = (Principal) ctx.get(Principal.class);
        return HomeSupportHelper.get(ctx).getBeans(ctx, Spid.class, new SpidAwareHomePredicate(p));
    }
    
    public static Object getPropertyArgumentFromFilter(final Object x, final PropertyInfo propertyInfo, final Class<? extends Object> expectedClass)
    {
        if (x instanceof EQ)
        {
            final EQ eq = (EQ) x;
            final String name = ((PropertyInfo) eq.getArg1()).getName();
            if (name.equals(propertyInfo.getName()))
            {
                return eq.getArg2();
            }
            else if (expectedClass.isInstance(eq.getArg1()))
            {
                return eq.getArg1();
            }
        }
        if (x instanceof Context)
        {
            final Context xc = (Context) x;
            final Object result = getPropertyArgumentFromFilter(xc.get("__1"), propertyInfo, expectedClass);
            if (result != null)
            {
                return result;
            }
            return getPropertyArgumentFromFilter(xc.get("__2"), propertyInfo, expectedClass);
        }
        if (x instanceof Wildcard)
        {
            final Wildcard wildcard = (Wildcard) x;
            final String name = ((PropertyInfo) wildcard.getArg1()).getName();
            if (name.equals(propertyInfo.getName()))
            {
                return wildcard.getArg2();
            }
            else if (expectedClass.isInstance(wildcard.getArg1()))
            {
                return wildcard.getArg1();
            }
        }
        if (x instanceof Value)
        {
            final Value v = (Value) x;
            final Object result = getPropertyArgumentFromFilter(((Value) x).getArg1(), propertyInfo, expectedClass);
            return result;
        }
        if (x instanceof Or)
        {
            final Or xc = (Or) x;
            for (Object o : xc.getList())
            {
                final Object result = getPropertyArgumentFromFilter(o, propertyInfo, expectedClass);
                if (result != null)
                {
                    return result;
                }
            }
            return null;
        }
        if (x instanceof And)
        {
            final And xc = (And) x;
            for (Object o : xc.getList())
            {
                final Object result = getPropertyArgumentFromFilter(o, propertyInfo, expectedClass);
                if (result != null)
                {
                    return result;
                }
            }
            return null;
        }
        return null;
    }    
    
    public static boolean subscriberHasCallingGroupAuxService(Context ctx, Subscriber sub)
    {
    	List<SubscriberAuxiliaryService> auxServices = sub.getAuxiliaryServices(ctx);
    	
    	if(auxServices != null)
    	{
    		for (SubscriberAuxiliaryService auxservice : auxServices)
            { 
                if (auxservice != null)
                {
                    if (auxservice.getType(ctx).equals(AuxiliaryServiceTypeEnum.CallingGroup))
                    {
                    	return true;
                    }
                }
            }
    	}
        return false;
    }
}
