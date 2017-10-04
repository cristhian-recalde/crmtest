/**
 * 
 */
package com.trilogy.app.crm.support;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;

import com.trilogy.app.crm.bean.ClosedUserGroup;
import com.trilogy.app.crm.bean.ClosedUserGroupHome;
import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.PrivateCug;
import com.trilogy.app.crm.bean.PrivateCugHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.core.AuxiliaryService;
import com.trilogy.app.crm.extension.auxiliaryservice.core.CallingGroupAuxSvcExtension;
import com.trilogy.app.crm.ff.FFClosedUserGroupSupport;
import com.trilogy.app.crm.ff.FFEcareException;
import com.trilogy.app.crm.poller.event.FnFSelfCareProcessor;
import com.trilogy.app.crm.subscriber.charge.CUGCharger;
import com.trilogy.app.crm.subscriber.charge.CrmCharger;
import com.trilogy.app.crm.subscriber.charge.PrivateCUGCharger;
import com.trilogy.app.ff.ecare.rmi.FFECareRmiService;
import com.trilogy.app.ff.ecare.rmi.TrPeerMsisdn;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.model.app.ff.param.Parameter;
import com.trilogy.model.app.ff.param.ParameterSetHolderImpl;

/**
 * @author abaid
 *
 */
public class ClosedUserGroupSupport73 
{
	
	public static PrivateCug findPrivateCug(Context ctx, long id)
	throws HomeException
	{
		Home home = (Home) ctx.get(PrivateCugHome.class); 
		return (PrivateCug) home.find(new Long(id)); 
		
	}
	
	public static CrmCharger getCUGCharger(Context ctx, ClosedUserGroup cug, ClosedUserGroup refundCug)
	throws HomeException
	{
	    	if (cug.getAuxiliaryService(ctx).isPrivateCUG(ctx))
	    	{
	    		return new PrivateCUGCharger(ctx, cug, refundCug);
	    	}
	    	
	    	return new CUGCharger(cug, refundCug); 
	}
	
	
	
	public static void switchCUGMsisdn(Context ctx, final SubscriberAuxiliaryService association, 
			final String oldMsisdn, final String newMsisdn, int spid)
	throws HomeException
	{        
        final long cugId = association.getSecondaryIdentifier();
        final ClosedUserGroup cug = ClosedUserGroupSupport.getCUG(ctx, cugId, spid);
        
        if (cug != null)
        {
            try 
            {
                removeFromCug(ctx, cug, oldMsisdn);
            	addToCug(ctx, cug,  newMsisdn);
            	
            } catch (RemoteException e)
            {
            	HomeException he = new HomeException(e.getMessage());
            	he.setStackTrace(e.getStackTrace());
            	throw he; 
            }
        }else 
        {
        	throw new HomeException("Can not find CUG " + cugId); 
        }
	}

	static public void updateCUGNotifyMsisdn(Context ctx, final SubscriberAuxiliaryService association,String oldMsisdn, String newMsisdn, int spid)throws HomeException
	{
		 final Home home = ((Home) ctx.get(ClosedUserGroupHome.class));
	     final long cugId = association.getSecondaryIdentifier();
	     final ClosedUserGroup cug = ClosedUserGroupSupport.getCUG(ctx, cugId, spid);
		
	     if (cug != null)
	     {
	    	 int result =0;
	    	 try
	    	 {
	    		if(cug.getSmsNotifyUser().equals(oldMsisdn)){
	    			cug.setSmsNotifyUser(newMsisdn);
	    			home.store(cug);
	    		}
	    		result =  getRmiService(ctx).updateCUGNotifyMsisdnWithSpid(cug.getSpid(), cug.getID(), cug.getSmsNotifyUser());

	    	 }
	    	 catch (FFEcareException e)
	    	 {
	    		 result = e.getResultCode();
	    		 LogSupport.minor(ctx, ClosedUserGroupSupport73.class, "Error updating Cug notification " + cugId + ". Result = " + result + ","+e.getMessage());

	    	 }catch (RemoteException e)
	    	 {
	    		 HomeException he = new HomeException(e.getMessage());
	    		 he.setStackTrace(e.getStackTrace());
	    		 throw he; 
	    	 }
	     }else{
	        	throw new HomeException("Can not find CUG " + cugId); 
	     }
	}

	
	static public int addToCug(Context ctx, ClosedUserGroup cug, String newMsisdn)
	throws HomeException, RemoteException
	{
        TrPeerMsisdn[] newSubs = new TrPeerMsisdn[1]; 
        newSubs[0] = new TrPeerMsisdn(newMsisdn);
        try
        {
            return getRmiService(ctx).addSubsToCUG(cug.getSpid(), cug.getID(), newSubs, new Parameter[]{}, new ParameterSetHolderImpl());
        }
        catch (FFEcareException e)
        {
            return e.getResultCode();
        }
 	}
	
	
	static public int removeFromCug(Context ctx, ClosedUserGroup cug, String oldMsisdn)
	throws HomeException, RemoteException
	{
           TrPeerMsisdn[] oldSubs = new TrPeerMsisdn[1];
           oldSubs[0] = new TrPeerMsisdn(oldMsisdn);
           try
           {
               return getRmiService(ctx).removeSubsFromCUG(cug.getSpid(), cug.getID(), oldSubs, new Parameter[]{}, new ParameterSetHolderImpl());
           }
           catch (FFEcareException e)
           {
               return e.getResultCode();
           }
	}

	public static void switchPrivateCUGMsisdn(Context ctx, SubscriberAuxiliaryService association, 
			 final String oldMsisdn, final String newMsisdn)
	throws HomeException
	{
        final Home home = ((Home) ctx.get(PrivateCugHome.class));
        final long cugId = association.getSecondaryIdentifier();
        final PrivateCug cug = (PrivateCug) home.find(ctx, new Long(cugId));
        
        if (cug != null && cug.getOwnerMSISDN().equals(oldMsisdn))
        {
        	cug.setOwnerMSISDN(newMsisdn);
        	home.store(cug);
        }
	}
	
    public static void updatePrivateCUGState(Context ctx, SubscriberAuxiliaryService association, 
            final Subscriber newSubscriber,
            final Subscriber oldSubscriber, int spid)
   throws HomeException
   {
       final long cugId = association.getSecondaryIdentifier();
       try
       {
           int result = updatePrivateCUGState(ctx, cugId, newSubscriber, oldSubscriber, spid);
           if (result!=0)
           {
               String resultMessage;
               switch (result)
               {
                   case 2:
                       resultMessage = "2 -> Invalid state parameter";
                       break;
                   case 9:
                       resultMessage = "9 -> Internal failure. Check FF application logs.";
                       break;
                   case 11:
                       resultMessage = "11 -> Invalid CUG identifier";
                       break;
                   default:
                       resultMessage = result + " -> Unknown result";
               }
               LogSupport.minor(ctx, ClosedUserGroupSupport73.class, "Error updating state for PCUG " + cugId + ". Result = " + resultMessage);
           }
       }
       catch (RemoteException e)
       {
           LogSupport.minor(ctx, ClosedUserGroupSupport73.class, "Exception updating state for PCUG " + cugId + ": " + e.getMessage(), e);
       }
   }
    
    public static int updatePrivateCUGState(Context ctx, long cugId, 
            final Subscriber newSubscriber,
            final Subscriber oldSubscriber, int spid)
   throws HomeException, RemoteException
   {
       ClosedUserGroup cug = ClosedUserGroupSupport.getCUG(ctx, cugId, spid);
       if (cug!=null && newSubscriber.getMSISDN().equals(cug.getOwnerMSISDN()))
       {
           if (requiresPCUGSuspension(ctx, oldSubscriber, newSubscriber))
           {
               if (LogSupport.isDebugEnabled(ctx))
               {
                   LogSupport.debug(ctx, ClosedUserGroupSupport73.class, "Setting Private CUG state DISABLED for PCUG " + cugId);
               }
               try
               {
                   return getRmiService(ctx).updateCUGState(cugId, CUG_STATE_DISABLED);
               }
               catch (FFEcareException e)
               {
                   return e.getResultCode();
               }
           }
           else if (requiresPCUGUnsuspension(ctx, oldSubscriber, newSubscriber))
           {
               if (LogSupport.isDebugEnabled(ctx))
               {
                   LogSupport.debug(ctx, ClosedUserGroupSupport73.class, "Setting Private CUG state ENABLED for PCUG " + cugId);
               }
               try
               {
                   return getRmiService(ctx).updateCUGState(cugId, CUG_STATE_ENABLED);
               }
               catch (FFEcareException e)
               {
                   return e.getResultCode();
               }
           }
       }
       return 0;
   }

    public static boolean requiresPCUGStateChange(final Context ctx, final Subscriber oldSub, final Subscriber newSub)
    {
        return requiresPCUGUnsuspension(ctx, oldSub, newSub) || requiresPCUGSuspension(ctx, oldSub, newSub);
       

    }

    private static boolean requiresPCUGUnsuspension(final Context ctx, final Subscriber oldSub, final Subscriber newSub)
    {
        return newSub.isPostpaid() &&
            (EnumStateSupportHelper.get(ctx).isTransition(oldSub, newSub, SubscriberStateEnum.SUSPENDED, SubscriberStateEnum.ACTIVE) ||
                    EnumStateSupportHelper.get(ctx).isTransition(oldSub, newSub, new int[] {SubscriberStateEnum.IN_ARREARS_INDEX, SubscriberStateEnum.IN_COLLECTION_INDEX, SubscriberStateEnum.NON_PAYMENT_SUSPENDED_INDEX}, new int[] {SubscriberStateEnum.PROMISE_TO_PAY_INDEX, SubscriberStateEnum.ACTIVE_INDEX}));
       

    }

    public static boolean requiresPCUGSuspension(final Context ctx, final Subscriber sub)
    {
        return sub.isPostpaid() && 
            (EnumStateSupportHelper.get(ctx).isOneOfStates(sub, SubscriberStateEnum.SUSPENDED, SubscriberStateEnum.IN_ARREARS, SubscriberStateEnum.IN_COLLECTION));
       

    }

    private static boolean requiresPCUGSuspension(final Context ctx, final Subscriber oldSub, final Subscriber newSub)
    {
        return newSub.isPostpaid() &&
            (EnumStateSupportHelper.get(ctx).isTransition(oldSub, newSub, SubscriberStateEnum.ACTIVE, SubscriberStateEnum.SUSPENDED) ||
                    EnumStateSupportHelper.get(ctx).isTransition(oldSub, newSub, new int[] {SubscriberStateEnum.ACTIVE_INDEX, SubscriberStateEnum.PROMISE_TO_PAY_INDEX, SubscriberStateEnum.NON_PAYMENT_WARN_INDEX, SubscriberStateEnum.NON_PAYMENT_SUSPENDED_INDEX}, 
                    		new int[] {SubscriberStateEnum.IN_ARREARS_INDEX, SubscriberStateEnum.IN_COLLECTION_INDEX, SubscriberStateEnum.NON_PAYMENT_SUSPENDED_INDEX}));
       

    }

    public static boolean isOwner(Context ctx, Subscriber sub, SubscriberAuxiliaryService service)
	throws HomeException
	{
		PrivateCug pcug = findPrivateCug(ctx, service.getSecondaryIdentifier()); 
		
		if (pcug != null)
		{	
			return (sub.getMSISDN().equals(pcug.getOwnerMSISDN())); 
		}
		
        long callingGroupIdentifier = CallingGroupAuxSvcExtension.DEFAULT_CALLINGGROUPIDENTIFIER;
        CallingGroupAuxSvcExtension callingGroupAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx, service.getAuxiliaryService(ctx), CallingGroupAuxSvcExtension.class);
        if (callingGroupAuxSvcExtension!=null)
        {
            callingGroupIdentifier = callingGroupAuxSvcExtension.getCallingGroupIdentifier();
        }
        else
        {
            LogSupport.minor(ctx, ClosedUserGroupSupport73.class,
                    "Unable to find required extension of type '" + CallingGroupAuxSvcExtension.class.getSimpleName()
                            + "' for auxiliary service " + service.getIdentifier());
        }

        throw new HomeException("fail to find private cug for CallingGroupId = " + callingGroupIdentifier + " and SecondaryId = " + service.getSecondaryIdentifier());
	}
	
	
	
	public static void removeMsisdnFromCug( final Context ctx, 
			final long cugId, final String msisdn, int spid)
	throws HomeException 
	{
       ClosedUserGroup cug = ClosedUserGroupSupport.getCUG(ctx, cugId, spid);
       if (cug != null)
       {
    	   cug.getSubscribers().remove(msisdn);
        	   Home  home = (Home) ctx.get(ClosedUserGroupHome.class);
    	   home.store(cug); 
       }
	}
	
	
	
    /**
     * Updates the ClosedUserGroup for the given association, if applicable.
     *
     * @param association The association between Subscriber and
     * AuxiliaryService.
     *
     * @exception HomeException Thrown if there are any problems access Home
     * information in the context.
     */
    public static boolean removeSubscriberFromClosedUserGroup(
    	final Context ctx,
    	final SubscriberAuxiliaryService association)
        throws HomeException
    {
    	// I don't like such xhome style magic, however, it is impossible to identify the source 
    	// of remove event under current design. so leave it for now. 
    	// if the cug pipeline called, then skip this part, since cug pipe line already take care of 
    	// F&F side in ClosedUserGroupServiceHome. 
    	if (!ctx.getBoolean(CallingGroupAuxSvcExtension.ENABLED, true))
    	{
    		return true; 
    	}
    	
        final AuxiliaryService service = association.getAuxiliaryService(ctx); 
        final Subscriber subscriber = SubscriberSupport.lookupSubscriberForSubId(ctx,association.getSubscriberIdentifier());
        final String msisdn = subscriber.getMSISDN();

        
        if (service.isCUG(ctx))
        {
            if(ctx.has(FnFSelfCareProcessor.FNF_FROM_SELFCARE_ER_POLLER))
            {
            		//In case of FnFSelfCare Poller, the subscriber's FnF profilOe would already be removed from CUG/PLP,hence no need to make a call to ECP. [amit]        	
            	new DebugLogMsg(ClosedUserGroupSupport.class,
            					"The context under use has the key 'FnFPoller',call to ECP is bypassed",
            					null).log(ctx);
            } else 
            {
            
            	removeMsisdnFromCug(ctx, association.getSecondaryIdentifier(), msisdn, service.getSpid());
            }
        } else if (service.isPrivateCUG(ctx))
        {
        	ClosedUserGroup cug = ClosedUserGroupSupport.getCUG(ctx, association.getSecondaryIdentifier(), service.getSpid()); 
        	if (cug.getOwnerMSISDN()!= null && cug.getOwnerMSISDN().equals(msisdn))
        	{     		
           		removeCUG(ctx, association.getSecondaryIdentifier(), service.getSpid()); 
         	} else 
        	{
                if(ctx.has(FnFSelfCareProcessor.FNF_FROM_SELFCARE_ER_POLLER))
                {
                		//In case of FnFSelfCare Poller, the subscriber's FnF profilOe would already be removed from CUG/PLP,hence no need to make a call to ECP. [amit]        	
                	new DebugLogMsg(ClosedUserGroupSupport.class,
                					"The context under use has the key 'FnFPoller',call to ECP is bypassed",
                					null).log(ctx);
                } else 
                {

                	removeMsisdnFromCug(ctx, association.getSecondaryIdentifier(), msisdn, service.getSpid());
                }	
        	}
        } else 
        {
        	// need continue to remove subscriber auxiliary service for other type. 
        	return true; 
        }
       
        return false; 
    }	


	
	public static  void removeCUG(Context ctx, long cugId, int spid) 
	throws HomeException
	{
		ClosedUserGroup cug = ClosedUserGroupSupport.getCUG(ctx, cugId, spid);
		if (cug != null )
		{			
			Home home = (Home) ctx.get(ClosedUserGroupHome.class);
			home.remove(cug); 
		}
		
	}
	
 
	
	public static boolean isOwnerInActiveState(Context ctx, ClosedUserGroup cug)
	throws HomeException
	{
		Subscriber owner = cug.getOwner(ctx); 
		
		if(owner == null )
		{ 
			throw new HomeException("Can not find Private CUG owner" + cug.getOwnerMSISDN()); 
		}
		
		return (owner.getState().equals(SubscriberStateEnum.ACTIVE) || owner.getState().equals(SubscriberStateEnum.PROMISE_TO_PAY)); 
 
	}
	

	public static long getTotalChargeForPCUG(final Context ctx, 
			final AuxiliaryService service, 
			final Collection msisdns, final int spid)
	{
		
		int postpaids =0; 
		int prepaids =0; 
		int externals =0; 
		
		
		for(Iterator i = msisdns.iterator(); i.hasNext(); )
		{
			String number = (String)i.next(); 
			
			try
			{
				Msisdn msisdn = MsisdnSupport.getMsisdn(ctx, number); 
				if (msisdn == null || msisdn.getSpid()!=spid)
				{
					externals++; 
				} else if (msisdn.getSubscriberType().equals(SubscriberTypeEnum.POSTPAID))
				{
					postpaids++; 
				} else 
				{
					prepaids++; 
				}
			} catch (Exception e)
			{
				new MinorLogMsg(ClosedUserGroupSupport.class, "fail to find msisdn " + number, e).log(ctx); 
			}
			

		}
		
        long serviceChargeExternal = CallingGroupAuxSvcExtension.DEFAULT_SERVICECHARGEEXTERNAL;
        long serviceChargePostpaid = CallingGroupAuxSvcExtension.DEFAULT_SERVICECHARGEPOSTPAID;
        long serviceChargePrepaid = CallingGroupAuxSvcExtension.DEFAULT_SERVICECHARGEPREPAID;
        
        CallingGroupAuxSvcExtension callingGroupAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx,
                service, CallingGroupAuxSvcExtension.class);
        if (callingGroupAuxSvcExtension!=null)
        {
            serviceChargeExternal = callingGroupAuxSvcExtension.getServiceChargeExternal();
            serviceChargePostpaid = callingGroupAuxSvcExtension.getServiceChargePostpaid();
            serviceChargePrepaid = callingGroupAuxSvcExtension.getServiceChargePrepaid();
        }
        else
        {
            LogSupport.minor(ctx, ClosedUserGroupSupport73.class,
                    "Unable to find required extension of type '" + CallingGroupAuxSvcExtension.class.getSimpleName()
                            + "' for auxiliary service " + service.getIdentifier());
        }
        
		final long ret = serviceChargePostpaid * postpaids +
		 serviceChargePrepaid * prepaids +
		 serviceChargeExternal * externals; 

		return ret; 		
	}
	

	
	
	public static long getSingleChargeForPCUG(final Context ctx, 
			final AuxiliaryService service, 
			final String number)
	throws HomeException
	{
	    long serviceChargeExternal = CallingGroupAuxSvcExtension.DEFAULT_SERVICECHARGEEXTERNAL;
        long serviceChargePostpaid = CallingGroupAuxSvcExtension.DEFAULT_SERVICECHARGEPOSTPAID;
        long serviceChargePrepaid = CallingGroupAuxSvcExtension.DEFAULT_SERVICECHARGEPREPAID;
        
        CallingGroupAuxSvcExtension callingGroupAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx,
                service, CallingGroupAuxSvcExtension.class);
        if (callingGroupAuxSvcExtension!=null)
        {
            serviceChargeExternal = callingGroupAuxSvcExtension.getServiceChargeExternal();
            serviceChargePostpaid = callingGroupAuxSvcExtension.getServiceChargePostpaid();
            serviceChargePrepaid = callingGroupAuxSvcExtension.getServiceChargePrepaid();
        }
        else
        {
            LogSupport.minor(ctx, ClosedUserGroupSupport73.class,
                    "Unable to find required extension of type '" + CallingGroupAuxSvcExtension.class.getSimpleName()
                            + "' for auxiliary service " + service.getIdentifier());
        }
        
		Msisdn msisdn = MsisdnSupport.getMsisdn(ctx, number); 
		if (msisdn == null )
		{
			return serviceChargeExternal; 
		} else if (msisdn.getSubscriberType().equals(SubscriberTypeEnum.POSTPAID))
		{
			return serviceChargePostpaid; 
		} else 
		{
			return serviceChargePrepaid; 
		}
	}

	
	/**
	 * Return the remote Friends & Family RMI service.
	 * 
	 * @return FFECareRmiService The Friends & Family RMI service.
	 */
	static public FFECareRmiService getRmiService(Context ctx) throws FFEcareException
	{
		return FFClosedUserGroupSupport.getFFRmiService(ctx, ClosedUserGroupSupport73.class);
	}


	static public final int CUG_RATEPLAN = 3;
	static public final int CUG_DISCOUNT = 4;
	
	public static final int CUG_STATE_DISABLED = 1;
    public static final int CUG_STATE_ENABLED = 0;
}
