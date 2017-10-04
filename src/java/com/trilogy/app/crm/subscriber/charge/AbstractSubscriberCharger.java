package com.trilogy.app.crm.subscriber.charge;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.trilogy.app.crm.bean.AdjustmentTypeActionEnum;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.PricePlanHome;
import com.trilogy.app.crm.bean.PricePlanSubTypeEnum;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.subscriber.charge.handler.ChargeRefundResultHandler;
import com.trilogy.app.crm.subscriber.charge.support.AuxServiceChargingSupport;
import com.trilogy.app.crm.subscriber.charge.support.BundleChargingSupport;
import com.trilogy.app.crm.subscriber.charge.support.ChargeRefundResultHandlerSupport;
import com.trilogy.app.crm.subscriber.charge.support.ServiceChargingSupport;
import com.trilogy.app.crm.subscriber.charge.support.PackageChargingSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;
import com.trilogy.app.crm.support.DefaultCalendarSupport;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.ServiceSupport;
import com.trilogy.app.crm.xhome.home.OcgTransactionException;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.client.bm.BalanceManagementSupport;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionClient;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionException;

public abstract class AbstractSubscriberCharger 
extends AbstractCrmCharger
implements
SubscriberChargingAccumulator
{
    /**
     * 
     * @param ctx
     * @param isActivation
     * @param action, identify it is a provision or unsuspend action, need support recurring charge in future
     * @param handler
     * @return
     * @throws HomeInternalException 
     * @throws HomeException 
     */
    protected int createChargeTransactions( 
    		final Context context,  
            final boolean isActivation, 
            final int action,
            ChargeRefundResultHandler handler) 
    {
        Context subCtx = context.createSubContext();
        subCtx.put(Subscriber.class, subscriber);

        if ( subscriber == null)
        {
            new MajorLogMsg(AbstractSubscriberCharger.class, 
                    "can not find subscriber in context", null).log(subCtx); 
            return RUNNING_ERROR_STOP; 
        }

        this.clearChargedSet();
        int continueState = RUNNING_SUCCESS;

        continueState = PackageChargingSupport.applyPackagesChargeByIds(subCtx, packagesToBeCharged,  
               subscriber, isActivation, handler, action,  this, continueState);
        
        //To check whether the subscriber has sufficient balance for services and bundles which falls under MRC group
        // If its not having a balance then suspend all the services       
        if(subscriber.getSubscriberType().equals(SubscriberTypeEnum.PREPAID) && totalMrcCharge > 0)
        {
        	long subBalance = subscriber.getBalanceRemaining(subCtx);
        	
        	if(LogSupport.isDebugEnabled(subCtx))
        	{
        		LogSupport.debug(subCtx, this, "Subscriber balance is "+subBalance 
        				+" and total PickNPay MRC group charge is "+totalMrcCharge);
        	}
        	
        	if(subBalance < totalMrcCharge)
	        {
	        	continueState = RUNNING_CONTINUE_SUSPEND;
	        	
	        	String msg = "Suspending all services and bundles because of Insufficient balance to charge MRC group";
	        	if (LogSupport.isDebugEnabled(subCtx))
				{
					LogSupport.debug(subCtx, this, msg);
				}
	        	
	        	ChargableItemResult ret = new ChargableItemResult();
	        	ret.setChargeResult(ChargingConstants.TRANSACTION_FAIL_UNKNOWN);
	        	        	
	        	ChargeRefundResultHandlerSupport.logErroMsg(subCtx,msg,ret);
	        	
	        	
	        	
	            com.redknee.app.crm.bean.core.Transaction transaction = null;
	        	transaction = (com.redknee.app.crm.bean.core.Transaction) XBeans.instantiate(Transaction.class, context);
	        	  Date txnDate = new Date();
	        	  
	        	  
	        	  Home pricePlanHome = (Home) context.get(PricePlanHome.class);
	        	  PricePlan pp = null;
	        	  
	              if (pricePlanHome != null)
	              {
	                  try {
	    				pp = (PricePlan) pricePlanHome.find(context, subscriber.getPricePlan());
	    			} catch (HomeInternalException e) 
	    			{
	    				 new MajorLogMsg(context, "Price Plan found null",e).log(context);
	    			} catch (HomeException e) 
	    			{
	    				new MajorLogMsg(context, "Price Plan found null",e).log(context);
	    			}
	              }   
	                
	              /*
	               * Below code is very bad but we don't have option to for pick & pay price plan to hit the subscriber pipeline 
	               * to update the subscriber state
	               * 
	               * TCBSUP-296: [PnP] BSS - Not Expired When Changing Plan With Insufficient Balance
	               */
	             if(PricePlanSubTypeEnum.PICKNPAY.equals(pp.getPricePlanSubType()))
	             {     
	        	 
	    		    	 int adjustmentTypeId = getSubServiceAdjustmentTypeId(context,subscriber);
	    		    	  
	    		    	 transaction.setMSISDN(subscriber.getMSISDN());
	    		    	 transaction.setSubscriberID(subscriber.getId());
	    		    	 transaction.setSpid(subscriber.getSpid());
	    		         transaction.setAdjustmentType(adjustmentTypeId);	  
	    		         transaction.setAmount(totalMrcCharge);
	    		         transaction.setReasonCode(Long.MAX_VALUE);
	    		         transaction.setTransDate(txnDate);
	    		         transaction.setAction(AdjustmentTypeActionEnum.DEBIT);
	    		         
	    		     	try {
	    						Transaction resultTrans = CoreTransactionSupportHelper.get(context).createTransaction(context, transaction, true);
	    					}catch (final OcgTransactionException ocge)
	    					{
	    						int result = ocge.getErrorCode();
	    						new MajorLogMsg(context, "Failed on OCG",ocge).log(context);
	    					}
	    		     		catch (HomeException e) {
	    		     			new MajorLogMsg(context, "Exception found in createChargeTransactions:",e).log(context);
	    					}
	             }	
	        }
        }
        
        continueState = ServiceChargingSupport.applyServicesChargeByIds(subCtx, servicesToBeCharged, 
                subscriber, getOldSubscriber(subCtx), isActivation, handler, action, this, continueState);
        
        continueState = BundleChargingSupport.applyBundlesTransactionsByIds(subCtx, bundlesToBeCharged, 
                subscriber,getOldSubscriber(subCtx), isActivation, handler, action, this, continueState);
        
        continueState = AuxServiceChargingSupport.applyAuxServicesChargeByIds(subCtx, auxServicesToBeCharged, 
                subscriber, getOldSubscriber(subCtx), isActivation, handler, action, this, continueState);
        
       
        return continueState;
    }
    
    	  
	private int getSubServiceAdjustmentTypeId(Context context,Subscriber subscriber) {
		int adjustmentTypeId = 0;

		Map services;

		try {
			services = ServiceChargingSupport.getProvisionedServices(context,
					subscriber.getPricePlan(context).getServiceFees(context)
							.values(), servicesToBeCharged);

			List<ServiceFee2> serviceFees = new ArrayList(services.keySet());

			Service service;
			for (Iterator i = serviceFees.iterator(); i.hasNext();) 
			{
				final ServiceFee2 fee = (ServiceFee2) i.next();
				if (fee.isPrimary()) 
				{
					service = ServiceSupport.getService(context,fee.getServiceId());
					adjustmentTypeId = service.getAdjustmentType();
					break;
				}

			}
		} catch (HomeException e1) {
			new MajorLogMsg(context, "fail to get ProvisionedServices for"
					+ subscriber.getId(), e1).log(context);
		}

		return adjustmentTypeId;
	}
    
    
    protected void clearToBeSet()
    {
        clearToBeChargedSet();
        clearToBeRefundSet();
    }
    
    protected void clearToBeChargedSet()
    {
       servicesToBeCharged = new  HashSet<SubscriberServices>();
//       {
//
//
//   		public boolean addAll(Collection<? extends SubscriberServices> c) 
//   		{
//   			int oldSize = super.size();
//   			Date today = DefaultCalendarSupport.instance().getDateWithNoTimeOfDay(Calendar.getInstance().getTime());
//   			Iterator<? extends SubscriberServices> iter = c.iterator();
//   			while (iter.hasNext())
//   	        {
//   	            SubscriberServices service = iter.next();
//   	            if (!service.getEndDate().before(today) )
//   	            {
//   	            	super.add(service);
//   	            }
//   	        }
//
//   			int newSize = super.size();
//   			return oldSize==newSize?false:true;
//   		}
//       	
//       }; 
       packagesToBeCharged = new  HashSet(); 
       auxServicesToBeCharged= new  HashSet(); 
       bundlesToBeCharged= new  HashSet();
    }
    
    protected void clearToBeRefundSet()
    {
       bundlesToBeRefund= new  HashSet();
       packagesToBeRefund= new  HashSet(); 
       servicesToBeRefund = new  HashSet(); 
       auxServicesToBeRefund= new  HashSet(); 

    }

    protected void clearDoneSet()
    {
        clearChargedSet();
        clearRefundSet();
    }
    
    protected void clearChargedSet()
    {
       super.clearChargedSet();
       servicesCharged = new  HashSet(); 
       packagesCharged = new  HashSet();
       auxServicesCharged= new  HashSet(); 
       bundlesCharged= new  HashSet();
    }
    
    protected void clearRefundSet()
    {
        super.clearRefundSet();
        bundlesRefund= new  HashSet();
        packagesRefund= new  HashSet(); 
        servicesRefund = new  HashSet(); 
        auxServicesRefund= new  HashSet(); 

    }
    
    
    
    
    
    public Collection getServicesToBeCharged() {
        return servicesToBeCharged;
    }
    public Collection getServicesToBeRefund() {
        return servicesToBeRefund;
    }
    public Collection getPackagesToBeCharged() {
        return packagesToBeCharged;
    }
    public Collection getPackagesToBeRefund() {
        return packagesToBeRefund;
    }
    public Collection getAuxServicesToBeCharged() {
        return auxServicesToBeCharged;
    }
    public Collection getAuxServicesToBeRefund() {
        return auxServicesToBeRefund;
    }
    public Collection getBundlesToBeCharged() {
        return bundlesToBeCharged;
    }
    public Collection getBundlesToBeRefund() {
        return bundlesToBeRefund;
    }     
    
    public Collection getServicesCharged() {
        return servicesCharged;
    }
    public Collection getServicesRefund() {
        return servicesRefund;
    }
    public Collection getPackagesCharged() {
        return packagesCharged;
    }
    public Collection getPackagesRefund() {
        return packagesRefund;
    }
    public Collection getAuxServicesCharged() {
        return auxServicesCharged;
    }
    public Collection getAuxServicesRefund() {
        return auxServicesRefund;
    }
    public Collection getBundlesCharged() {
        return bundlesCharged;
    }
    public Collection getBundlesRefund() {
        return bundlesRefund;
    }    
    
    
    public void addChargedService(long id)
    {
        this.servicesCharged.add(new Long(id)); 
    }
    public void addChargedPackage(long id)
    {
        this.packagesCharged.add(new Long(id)); 
    }
    public void addChargedBundle(long id)
    {
        this.packagesCharged.add(new Long(id));
    }
    public void addChargedAuxiliaryService(long id)
    {
        this.auxServicesCharged.add(new Long(id));
    }
     
    public void addRefundService(long id)
    {
        this.servicesRefund.add(new Long(id));
    }
    public void addRefundPackage(long id)
    {
        this.packagesRefund.add(new Long(id));
    }
    
    public void addRefundBundle(long id)
    {
        this.bundlesRefund.add(new Long(id));
    }
    public void addRefundAuxiliaryService(long id)
    {
        this.auxServicesRefund.add(new Long(id));
    }

    
    
    public Subscriber getNewSubscriber()
    {
        return subscriber; 
    }

    protected Subscriber getOldSubscriber(Context ctx)
    {
        if ( oldSub == null)
        {
            oldSub = (Subscriber)ctx.get(Lookup.OLD_FROZEN_SUBSCRIBER); 
        }
        return oldSub; 
    }

    protected Subscriber subscriber; 
    protected Subscriber oldSub; 

    protected Collection<SubscriberServices> servicesToBeCharged = new  HashSet<SubscriberServices>();
//    {
//
//		
//		public boolean addAll(Collection<? extends SubscriberServices> c) 
//		{
//			int oldSize = super.size();
//			Date today = DefaultCalendarSupport.instance().getDateWithNoTimeOfDay(Calendar.getInstance().getTime());
//			Iterator<? extends SubscriberServices> iter = c.iterator();
//			while (iter.hasNext())
//	        {
//	            SubscriberServices service = iter.next();
//	            if (!service.getEndDate().before(today) )
//	            {
//	            	super.add(service);
//	            }
//	        }
//
//			int newSize = super.size();
//			return oldSize==newSize?false:true;
//		}
//    	
//    }; 
    protected Collection<SubscriberServices> servicesToBeRefund = new  HashSet<SubscriberServices>(); 
    protected Collection<Long> packagesToBeCharged = new  HashSet<Long>(); 
    protected Collection<Long> packagesToBeRefund= new  HashSet<Long>(); 
    protected Collection<SubscriberAuxiliaryService> auxServicesToBeCharged= new  HashSet<SubscriberAuxiliaryService>(); 
    protected Collection<SubscriberAuxiliaryService> auxServicesToBeRefund= new  HashSet<SubscriberAuxiliaryService>(); 
    protected Collection<Long> bundlesToBeCharged= new  HashSet<Long>();
    protected Collection<Long> bundlesToBeRefund= new  HashSet<Long>();
    
    protected long totalMrcCharge = 0;
    protected boolean allowChargeOnStateChange = false;

    private Collection<Long> servicesCharged = new  HashSet<Long>(); 
    private Collection<Long> servicesRefund = new  HashSet<Long>(); 
    private Collection<Long> packagesCharged = new  HashSet<Long>(); 
    private Collection<Long> packagesRefund= new  HashSet<Long>(); 
    private Collection<Long> auxServicesCharged= new  HashSet<Long>(); 
    private Collection<Long> auxServicesRefund= new  HashSet<Long>(); 
    private Collection<Long> bundlesCharged= new  HashSet<Long>();
    private Collection<Long> bundlesRefund= new  HashSet<Long>();
}
