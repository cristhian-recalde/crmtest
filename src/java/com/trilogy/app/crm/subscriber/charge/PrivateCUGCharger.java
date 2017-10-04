package com.trilogy.app.crm.subscriber.charge;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.trilogy.app.crm.bean.core.AuxiliaryService;
import com.trilogy.app.crm.bean.ClosedUserGroup;
import com.trilogy.app.crm.bean.PrivateCug;
import com.trilogy.app.crm.bean.PrivateCugHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.subscriber.charge.customize.PrivateCUGMemberTransactionCustomize;
import com.trilogy.app.crm.subscriber.charge.customize.PrivateCUGOwnerTransactionCustomize;
import com.trilogy.app.crm.subscriber.charge.customize.TransactionCustomize;
import com.trilogy.app.crm.subscriber.charge.handler.ChargeRefundResultHandler;
import com.trilogy.app.crm.subscriber.charge.handler.GenericHandler;
import com.trilogy.app.crm.subscriber.charge.handler.IgnoreUnapplicableTransactionHandler;
import com.trilogy.app.crm.subscriber.charge.handler.PCUGOwnerHandler;
import com.trilogy.app.crm.subscriber.charge.support.AuxServiceChargingSupport;
import com.trilogy.app.crm.subscriber.charge.support.ServiceChargingSupport;
import com.trilogy.app.crm.subscriber.charge.validator.DuplicationValidator;
import com.trilogy.app.crm.subscriber.charge.validator.PCUGExternalMsisdnTransactionValidator;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.MsisdnSupport;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.support.SystemSupport;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.xenum.Enum;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * The charger will handle charge and refund in case PCUG is created, modified and removed. 
 * it won't handle charge and refund that cug event triggered by subscriber state, which 
 * is handled by PrivateCUGSubscriberStateChargeReRefundHandler. 
 * 
 * @author lxia
 *
 */
public class PrivateCUGCharger extends CUGCharger 
{

    public PrivateCUGCharger(Context ctx, ClosedUserGroup cug, ClosedUserGroup refundCug) throws HomeException
    {
       super(cug, refundCug);
       getOwnerAuxiliaryService(ctx);
    }

    
    @Override
    public int getChargerType() 
    {
         return CHARGER_TYPE_PRIVATE_CUG;
    }

    
    @Override
    public int refund(Context ctx, ChargeRefundResultHandler handler) 
    {
        
        Subscriber owner;
        AuxiliaryService service;
        
        try 
        {
            owner = getOwner(ctx);
            service = refundCug.getAuxiliaryService(ctx); 
        } 
        catch (Exception e)
        {
            //unlikely to happen since the charger is used in cug pipe line. 
            return ChargingConstants.OPERATION_ERROR; 
        }

        Collection <String>  msisdns = refundCug.getRemoveddMsisdns(); 
        
        
        if (hasOwner(msisdns))
        {
            ChargableItemResult ret = initializeReturn(ctx, ChargingConstants.ACTION_PROVISIONING_REFUND , owner.getMSISDN(), true); 

         	TransactionCustomize  customizer = new PrivateCUGOwnerTransactionCustomize(ctx, service, msisdns, refundCug); 
			AuxServiceChargingSupport.handleSingleAuxServiceTransactionWithoutRedirecting(ctx,ret, new PCUGOwnerHandler(refundCug), null, customizer, null);
			
			ret.setChargedSubscriber(owner);


            return ret.getChargeResult(); 
            
        } else 
        {
            for (String msisdn : msisdns)
            {
                refundForMember(ctx,msisdn);
            }   
        }  
  
        return OPERATION_SUCCESS; 

    }

    
    @Override
    public int charge(Context ctx, ChargeRefundResultHandler handler) 
    {
        Subscriber owner;
        AuxiliaryService service;
        try 
        {
            owner = getOwner(ctx);
            service = cug.getAuxiliaryService(ctx); 
        } catch (Exception e)
        {
            //unlikely to happen since the charger is used in cug pipe line. 
            return ChargingConstants.OPERATION_ERROR; 
        }
         
     	Collection <String>  msisdns = cug.getNewMsisdns(); 
     	int result=0; 
     	
       	if (hasOwner(msisdns))
    	{
     		
        	ChargableItemResult ret = initializeReturn(ctx, ChargingConstants.ACTION_PROVISIONING_CHARGE , owner.getMSISDN(), true); 
        	TransactionCustomize customizer =  new PrivateCUGOwnerTransactionCustomize(ctx, service, msisdns, cug);
		   	
			AuxServiceChargingSupport.handleSingleAuxServiceTransactionWithoutRedirecting(ctx,ret, new PCUGOwnerHandler(cug), null, customizer, null);  	  					
        	
			updateNextRecurringChargeDate(ctx, msisdns);
			
        	return ret.getChargeResult(); 
    	} 
       	else
    	{	
     	
    		for (String msisdn : msisdns)
    		{
    			result =  chargeForMember(ctx, msisdn); 
    		}	
    			
    	}
        
        return result; 
    }

    
    
    private void updateNextRecurringChargeDate(Context ctx,
			Collection<String> members) 
    {
    	 try {
    		 SubscriberAuxiliaryService ownerSAS =  null;
    		 auxService_ = null; // to force getOwnerAuxiliaryService() to look into DB because next-recurring-charge-date has been updated as a part of transaction creation.
         
    		 ownerSAS = getOwnerAuxiliaryService(ctx);
    		 
    		 for( String member : members)
    		 {
    			 if(member.equals(owner.getMSISDN()))
    			 {
    				 continue;
    			 }
    			 Subscriber sub = SubscriberSupport.lookupSubscriberForMSISDN(ctx, member);
    			 if(sub != null)
    			 {
    				 SubscriberAuxiliaryService memberSAS = SubscriberAuxiliaryServiceSupport.getSubscriberAuxiliaryService(ctx, sub.getId(), ownerSAS.getAuxiliaryServiceIdentifier(), cug.getID());
    				 if(memberSAS != null)
    				 {
    					 memberSAS.setNextRecurringChargeDate(ownerSAS.getNextRecurringChargeDate());
    					 SubscriberAuxiliaryServiceSupport.updateSubscriberAuxiliaryService(ctx, memberSAS);
    				 }
    			 }
    			 
    		 }
    		 
		} catch (HomeException e) {
			LogSupport.major(ctx, this, "Exception occurred while updating next-recurring-charge-date of private-cug members!!",e);
		} 
    	 
	}


	private Subscriber getOwner(Context ctx)
    throws HomeException
    {
        if (owner == null )
        {
        
            Home home = (Home)ctx.get(PrivateCugHome.class); 
            PrivateCug pcug = (PrivateCug) home.find(new Long(this.cug.getID()));
        
            if (pcug != null)
            {   
                owner = SubscriberSupport.lookupSubscriberForMSISDN(ctx,String.valueOf(pcug.getOwnerMSISDN()));
            } else if (cug != null)
            {
                owner = SubscriberSupport.lookupSubscriberForMSISDN(ctx,String.valueOf(cug.getOwnerMSISDN()));
            }
            else
            {   
                throw new HomeException("can not find private cug owner for cug " + this.cug.getID());
            }   
        }
        
        return owner; 
    }
    
    public boolean hasOwner(Collection <String> msisdns)
    {
        for(String msisdn : msisdns)
        {
            if (msisdn.equals(owner.getMSISDN()))
            {
                return true; 
            }
        }
        return false; 
    }
    
    public int refundForMember(Context ctx, String msisdn)
    {
        
        
        ChargableItemResult ret = initializeReturn(ctx, ChargingConstants.ACTION_PROVISIONING_REFUND , msisdn, false); 
        
        ret.setChargedSubscriber(owner);
        
        if (ret.getChargeResult() == TRANSACTION_SUCCESS)
        {   

            PrivateCUGMemberTransactionCustomize customizer = null; 
            AuxiliaryService service = (AuxiliaryService)ret.getChargableObject(); 
            
            
            
            DuplicationValidator validator = null; 
            Subscriber memberSub = ret.getSubscriber();
            
            try
            {
                if (memberSub != null)
                {
                    customizer = new PrivateCUGMemberTransactionCustomize(ctx, memberSub, service, owner, getOwnerRefundAuxiliaryService(ctx), ret.getAction(), ret.getChargingCycleType(), refundCug); 
                    AuxServiceChargingSupport.handleSingleAuxServiceTransactionWithoutRedirecting(ctx,ret, null, null, customizer, null);
                }   
                else 
                {
                    customizer = new PrivateCUGMemberTransactionCustomize(ctx, msisdn, service, owner, getOwnerRefundAuxiliaryService(ctx), ret.getAction(), ret.getChargingCycleType(),refundCug);
                    validator = new PCUGExternalMsisdnTransactionValidator(msisdn); 
                    ret.setSubscriber(owner); 
                    AuxServiceChargingSupport.handleSingleAuxServiceTransactionWithoutRedirecting(ctx, ret, ingoreHandler, null, customizer, validator);                        
                }
            } 
            catch (HomeException e)
            {
                ret.setChargeResult(TRANSACTION_FAIL_UNKNOWN); 
                ret.thrownObject = e; 
            }
        }
        
        return ret.getChargeResult(); 
    }
    
    public int chargeForMember(Context ctx, String msisdn)
    {
        
        ChargableItemResult ret = initializeReturn(ctx, ChargingConstants.ACTION_PROVISIONING_CHARGE , msisdn, false); 
        

        if (ret.getChargeResult() == TRANSACTION_SUCCESS)
        {   
   
            AuxiliaryService service = (AuxiliaryService)ret.getChargableObject(); 
            PrivateCUGMemberTransactionCustomize customizer = null; 
            Subscriber memberSub = ret.getSubscriber();
            
            if (skipCalculation(ctx, memberSub))
            {
                return CALCULATION_SKIPPED;
            }
            
            Date nextRecurringChargeDate = null;
            auxService_ = null; // to force getOwnerAuxiliaryService() to look into DB because next-recurring-charge-date has been updated as a part of transaction creation.
            try
            {
            	nextRecurringChargeDate = getOwnerAuxiliaryService(ctx).getNextRecurringChargeDate();
            	
            	if (memberSub != null )
                {
                    customizer = new PrivateCUGMemberTransactionCustomize(ctx, memberSub, service, owner, getOwnerAuxiliaryService(ctx), ret.getAction(), ret.getChargingCycleType(), cug); 
                    AuxServiceChargingSupport.handleSingleAuxServiceTransactionWithoutRedirecting(ctx,ret, null, null, customizer, null);                       
                }   
                else 
                {
                    ret.setSubscriber(owner); 
                    customizer = new PrivateCUGMemberTransactionCustomize(ctx, msisdn, service, owner, getOwnerAuxiliaryService(ctx), ret.getAction(), ret.getChargingCycleType(), cug);
     
                    DuplicationValidator validator = new PCUGExternalMsisdnTransactionValidator(msisdn); 
                    AuxServiceChargingSupport.handleSingleAuxServiceTransactionWithoutRedirecting(ctx, ret,  ingoreHandler, null, customizer, validator);
                }
            } 
            catch (HomeException e)
            {
                ret.setChargeResult(TRANSACTION_FAIL_UNKNOWN); 
                ret.thrownObject = e; 
            }finally
            {
            	if(nextRecurringChargeDate != null && ret.getChargeResult() == TRANSACTION_SUCCESS )
            	{
            		/*
            		 * chargeForMember() would run whenever members are added to private cug.
            		 */
            		updateMemberAssociation(ctx,nextRecurringChargeDate,memberSub);
            	}
            }

        }
        
        return ret.chargeResult; 
    }
    
    
    private void updateMemberAssociation(Context ctx,
			Date nextRecurringChargeDate, Subscriber memberSub) 
    {
		
    	try {
			/*
			 * This is to restore owner's next-recurring-charge-date to older next-recurring-charge-date which is correct one
			 * This is done because each time owner is charged for member , its next-recurring-charge-date is updated to future date which is incorrect. 
			 */
    		long auxServiceId = getOwnerAuxiliaryService(ctx).getAuxiliaryServiceIdentifier();
    		SubscriberAuxiliaryService ownerSAS = SubscriberAuxiliaryServiceSupport.getSubscriberAuxiliaryService(ctx,getOwner(ctx).getId(),auxServiceId,cug.getID());
    		ownerSAS.setNextRecurringChargeDate(nextRecurringChargeDate);
			SubscriberAuxiliaryServiceSupport.updateSubscriberAuxiliaryService(ctx, ownerSAS);
			auxService_ = ownerSAS;
			
			/*
			 * Below code is to update member association's next-recurring-charge-date to owner's next-recurring-charge-date
			 */
			if(memberSub != null)
			{
				
				SubscriberAuxiliaryService memSAS  = SubscriberAuxiliaryServiceSupport.getSubscriberAuxiliaryService(ctx, memberSub.getId(), auxServiceId, cug.getID());
				memSAS.setNextRecurringChargeDate(nextRecurringChargeDate);
				SubscriberAuxiliaryServiceSupport.updateSubscriberAuxiliaryService(ctx, memSAS);
				
			}
			
			
		} catch (HomeException e) {
			LogSupport.major(ctx, this, "Could not update next-recurring-charge-date of private-cug owner and/or members!!",e);
		}
	}


	public ChargableItemResult initializeReturn(Context ctx, int action, String msisdn, boolean owner)
    {
        ChargableItemResult ret  = new ChargableItemResult(); 
        ret.setAction(action);
        ret.setChargableItemType(CHARGABLE_ITEM_AUX_SERVICE);
        ret.setAction(action); 
        ret.isActivation= false;
        ClosedUserGroup cugToUse;
        SubscriberAuxiliaryService ownerAuxService;

        try 
        {

            final SubscriberAuxiliaryService subService;

            if (ChargingConstants.ACTION_PROVISIONING_REFUND == action)
            {
                cugToUse = refundCug;
                ownerAuxService = getOwnerRefundAuxiliaryService(ctx);
            }
            else
            {
                cugToUse = cug;
                ownerAuxService = getOwnerAuxiliaryService(ctx);
            }
            if (owner)
            {
                if (cugToUse.getSubAuxServices().get(msisdn)==null)
                {
                    cugToUse.getSubAuxServices().put(msisdn, ownerAuxService);
                }
                subService = cugToUse.getSubAuxServices().get(msisdn); 
            }
            else
            {
                subService = ownerAuxService;
            }
            
            if (subService == null)
            {
                
            }

            // can not use auxiliaryservice from cug which could be changed when cug template changed. 
            ret.setChargableObject(cugToUse.getAuxiliaryService(ctx)); 
            ret.setChargingCycleType(ServiceChargingSupport.getChargingCycleType(cugToUse.getAuxiliaryService(ctx).getChargingModeType()));

            ret.setChargableObjectRef(subService); 
            ret.setChargingCycleType(ServiceChargingSupport.getChargingCycleType(subService.getAuxiliaryService(ctx).getChargingModeType()));
            final Subscriber sub = SubscriberSupport.lookupSubscriberForMSISDN(ctx, msisdn);
            ret.setSubscriber(sub); 
            
            AuxiliaryService auxService = cugToUse.getAuxiliaryService(ctx);
            
     		if (ChargingConstants.ACTION_PROVISIONING_REFUND == action)
     		{
         		if (sub == null || (!SubscriberStateEnum.SUSPENDED.equals(sub.getState()) && !EnumStateSupportHelper.get(ctx).isOneOfStates(sub.getState(), AbstractSubscriberProvisioningCharger.getNonChargeableStates())))
         		{
         			if (!auxService.isRefundable())
         			{
         				if (LogSupport.isDebugEnabled(ctx))
                        {
                           LogSupport.debug(ctx, this, "Refund to be Waived off for Auxiliary Service ID : " + auxService.getIdentifier() + " for Subscriber ID : " + (sub == null? msisdn :sub.getId()) + ", as Is Refundable flag : " + auxService.isRefundable());
                        }
         				ret.setChargeResult(TRANSACTION_SKIPPED_NO_REFUND);
         				return ret;
         			}
         		}
     		}
            
            if (isChargeAndRefundEnabled(ctx, cugToUse.getAuxiliaryService(ctx), cugToUse.getOwner(ctx)))
            {
                ret.setChargeResult( TRANSACTION_SUCCESS); 
            }
            else
            {
               ret.setChargeResult(TRANSACTION_SKIPPED_SUSPEND);
            }
            
        } catch (HomeException e)
        {
            ret.setChargeResult(TRANSACTION_FAIL_UNKNOWN); 
            ret.thrownObject = e; 
        }
        
        return ret; 
    }
    
    public static Subscriber getSub(Context ctx, String msisdn)
    {
        Subscriber sub = null; 
        try
        {
            sub = SubscriberSupport.lookupSubscriberForMSISDN(ctx, msisdn);
        } catch (HomeException e)
        {
            
        }
        
        return sub; 
        
    }
    
    
    public void logErrorMsg(Context ctx, String msg)
    {
           ExceptionListener el= (ExceptionListener)ctx.get(ExceptionListener.class); 
            new MinorLogMsg(this, msg, null).log(ctx); 
            if ( el != null )
            {
                el.thrown(new Exception(msg));
            }  
    }

    private SubscriberAuxiliaryService getOwnerRefundAuxiliaryService(Context ctx) throws HomeException
    {
        if (refundAuxService_==null)
        {
            Subscriber owner = getOwner(ctx);
            List<SubscriberAuxiliaryService> services = owner.getAuxiliaryServices(ctx);
            for (SubscriberAuxiliaryService service : services)
            {
                if (service.getAuxiliaryServiceIdentifier() == refundCug.getAuxiliaryService(ctx).getIdentifier())
                {
                    refundAuxService_ = service;
                    break;
                }
            }
        }
        return refundAuxService_;
    }
   
    private SubscriberAuxiliaryService getOwnerAuxiliaryService(Context ctx) throws HomeException
    {
        if (auxService_==null)
        {
            Subscriber owner = getOwner(ctx);
            List<SubscriberAuxiliaryService> services = owner.getAuxiliaryServices(ctx);
            for (SubscriberAuxiliaryService service : services)
            {
                if (service.getAuxiliaryServiceIdentifier() == cug.getAuxiliaryService(ctx).getIdentifier())
                {
                    auxService_ = service;
                    break;
                }
            }
        }
        return auxService_;
    }
    
    private boolean skipCalculation(Context ctx, Subscriber subscriber)
    {
        boolean result = false;
        
        if (subscriber != null && ((subscriber.isPrepaid() && EnumStateSupportHelper.get(ctx).isOneOfStates(subscriber.getState(), getNonChargeableStates()))))
        {
        	   result = true;
        	   return result;
        }
             
        return result;
    }
    
    public static Collection<? extends Enum> getNonChargeableStates()
    {
        return NON_CHARGEABLE_STATES;
    }
    
    public static Collection<? extends Enum> getNonChargeablePostpaidStates()
    {
        return NON_POSTPAID_CHARGEABLE_STATES;
    }
    
    private static final Collection<SubscriberStateEnum> NON_CHARGEABLE_STATES = 
        Arrays.asList(
            SubscriberStateEnum.AVAILABLE,
            SubscriberStateEnum.IN_ARREARS,
            SubscriberStateEnum.IN_COLLECTION
            );
    
    private static final Collection<SubscriberStateEnum> NON_POSTPAID_CHARGEABLE_STATES = 
        Arrays.asList(
            SubscriberStateEnum.IN_ARREARS,
            SubscriberStateEnum.IN_COLLECTION
            );
    
    Subscriber owner; 
    SubscriberAuxiliaryService auxService_;
    SubscriberAuxiliaryService refundAuxService_;
    final public static boolean ISCHARGE = true;
    public static GenericHandler ingoreHandler = new IgnoreUnapplicableTransactionHandler();
    
}
