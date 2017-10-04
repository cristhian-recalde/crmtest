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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.provision.corba.ECareServicePackage.ServiceError;
import com.trilogy.app.crm.provision.xgen.AccountService;
import com.trilogy.app.crm.provision.xgen.BASService;
import com.trilogy.app.crm.provision.xgen.BASServiceException;
import com.trilogy.app.crm.provision.xgen.SubscriberService;
import com.trilogy.app.crm.provision.xgen.SubscriberServiceException;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.provision.corba.SubscriberInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.support.DateUtil;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * @author psperneac
 */
public class ECareServiceImpl extends ECareServicePOA implements ContextAware
{
   protected Context context;
   protected String uid;
   protected SubscriberService sub;
   protected AccountService acct;
   protected BASService bas;

   /**
    * Constructor. Saves the context and the user id.
    * @param context
    * @param uid
    */
   public ECareServiceImpl(Context context, String uid)
   {
      super();
      
      setContext(context);
      setUid(uid);
      dateFormat =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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

   private BASService getBASService(Context context) 
   {
	   bas = (BASService)context.get(BASService.class);
	   return bas;
   }
   
   private SubscriberService getSubscriberService(Context context) 
   {
	   sub = (SubscriberService)context.get(SubscriberService.class);
	   return sub;
   }


   /**
    * @see com.redknee.app.crm.provision.corba.ECareServiceOperations#acctAdjust(java.lang.String, java.lang.String, int, long, java.lang.String, java.lang.String)
    */
   public int acctAdjust(
      String acctNum,
      String msisdn,
      int adjustType,
      long amount,
      String transDate,
      String csrInput)
      throws ServiceError
   {
	   bas = getBASService(getContext());
      if(bas==null)
      {
         new MinorLogMsg(this,"BASService is null",null).log(getContext());
         throw new ServiceError("BASService is null");
      }
      
      Date tDate=new Date();
      try
      {
         tDate=DateUtil.parse(transDate);
      }
      catch(Throwable th)
      {
         new MinorLogMsg(this,th.getMessage(),th).log(getContext());
         throw new ServiceError("Cannot parse Transaction Date");
      }
      
      try
      {
         bas.acctAdjust(acctNum,msisdn,adjustType,amount,tDate,csrInput);
         return 0;
      }
      catch(BASServiceException be)
      {
         new MinorLogMsg(this,be.getMessage(),be).log(getContext());
         throw new ServiceError("Cannot ajust account. Original message:"+be.getMessage());
      } catch(Exception e)
      {
        new MinorLogMsg(this,e.getMessage(),e).log(getContext());
        throw new ServiceError("Cannot ajust account. Original message:"+e.getMessage());
     }


   }


/**
    * @see com.redknee.app.crm.provision.corba.ECareServiceOperations#changeSubscriberCategory(java.lang.String, int)
    */
   public long changeSubscriberCategory(String msisdn,long subscriberCategory)throws ServiceError
   {
	   try {
		sub = getSubscriberService(getContext());
	    if(sub==null)
	    {
	    	new MinorLogMsg(this,"SubscriberService is null",null).log(getContext());
	    	throw new ServiceError("SubscriberService is null");
	    }
	    
	    Subscriber subscriber = sub.getSub(msisdn);
		subscriber.setSubscriberCategory(subscriberCategory);
		sub.editSub(subscriber);
		return subscriberCategory;
	} catch(SubscriberServiceException se)
    {
        new MinorLogMsg(this,se.getMessage(),se).log(getContext());
        throw new ServiceError("Cannot change plan. Original message:"+se.getMessage());
     }catch(Exception e)
     {
       new MinorLogMsg(this,e.getMessage(),e).log(getContext());
       throw new ServiceError("Cannot change Plan. Original message:"+e.getMessage());
    }
	   
   }


	/**
    * @see com.redknee.app.crm.provision.corba.ECareServiceOperations#changePlan(java.lang.String, int,java.lang.String,java.lang.String)
    */
   public int changePlanEx(String msisdn, int pricePlan, String startDate, String endDate, String campaignID) throws ServiceError
   {
	   sub = getSubscriberService(getContext());
      if(sub==null)
      {
         new MinorLogMsg(this,"SubscriberService is null",null).log(getContext());
         throw new ServiceError("SubscriberService is null");
      }
      
      try
      {
         int plan=getPlan(msisdn);
         sub.changePlan(msisdn,pricePlan);
         Subscriber subscriber = sub.getSub(msisdn);
         if("".equals(endDate)==false)
         {
        	 Date eDate = new Date();
        	 eDate = DateUtil.parse(endDate);
        	 subscriber.getMarketingCampaignBean().setEndDate(eDate);
         }
         if("".equals(startDate)==false)
         {
        	 Date sDate = new Date();
        	 sDate = DateUtil.parse(startDate);
        	 subscriber.getMarketingCampaignBean().setEndDate(sDate);
         }
         
         if("".equals(campaignID)==false)
         {
        	 subscriber.getMarketingCampaignBean().setMarketingId(Long.parseLong(campaignID));
         }
         
         sub.editSub(subscriber);
         
         return plan;
      }
      catch(SubscriberServiceException se)
      {
         new MinorLogMsg(this,se.getMessage(),se).log(getContext());
         throw new ServiceError("Cannot change plan. Original message:"+se.getMessage());
      }catch(Exception e)
      {
        new MinorLogMsg(this,e.getMessage(),e).log(getContext());
        throw new ServiceError("Cannot change Plan. Original message:"+e.getMessage());
     }
   }
   
    /**
    * @see com.redknee.app.crm.provision.corba.ECareServiceOperations#changePlan(java.lang.String, int)
    */

    public int changePlan(String msisdn, int pricePlan) throws ServiceError
   {
 	   sub = getSubscriberService(getContext());
      if(sub==null)
      {
         new MinorLogMsg(this,"SubscriberService is null",null).log(getContext());
         throw new ServiceError("SubscriberService is null");
      }
      
      try
      {
         int plan=getPlan(msisdn);
         sub.changePlan(msisdn,pricePlan);
         return plan;
      }
      catch(SubscriberServiceException se)
      {
         new MinorLogMsg(this,se.getMessage(),se).log(getContext());
         throw new ServiceError("Cannot change plan. Original message:"+se.getMessage());
      }catch(Exception e)
      {
        new MinorLogMsg(this,e.getMessage(),e).log(getContext());
        throw new ServiceError("Cannot change Plan. Original message:"+e.getMessage());
     }
   }


   /**
    * @see com.redknee.app.crm.provision.corba.ECareServiceOperations#getPlan(java.lang.String)
    */
   public int getPlan(String msisdn) throws ServiceError
   {
	   sub = getSubscriberService(getContext());
      if(sub==null)
      {
         new MinorLogMsg(this,"SubscriberService is null",null).log(getContext());
         throw new ServiceError("SubscriberService is null");
      }
      
      try
      {
         return sub.getPlan(msisdn);
      }
      catch(SubscriberServiceException se)
      {
         new MinorLogMsg(this,se.getMessage(),se).log(getContext());
         throw new ServiceError("Cannot get plan. Original message:"+se.getMessage());
      }catch(Exception e)
      {
        new MinorLogMsg(this,e.getMessage(),e).log(getContext());
        throw new ServiceError("Cannot get Plan. Original message:"+e.getMessage());
     }
   }

   public int acctAdjustWithSvcFee(String msisdn,
   		int amountAdjustType,
        String csrInputAmt,
		long amount,
		int svcAdjustType,
        String csrInputSvcFee,
		long svcFee, 
		String transDate
        ) throws ServiceError
   {
  	
  	 
	 return acctAdjustEx(msisdn,
	   		amountAdjustType,
	        csrInputAmt,
			amount,
			svcAdjustType,
	        csrInputSvcFee,
			svcFee, 
			transDate, 
			0,0,null); 
   	
   }
   
   public int acctAdjustEx(String msisdn,
   		int amountAdjustType,
        String csrInputAmt,
		long amount,
		int svcAdjustType,
        String csrInputSvcFee,
		long svcFee, 
		String transDate,
		int expiryDaysExt,
		long resrv1,
		String resrv2
        ) throws ServiceError
   {
   
   	try {
	   		Date date = new Date(); 
	   		synchronized (dateFormat){
	   			date = dateFormat.parse( transDate );
	   		}
	 	   bas = getBASService(getContext());
	       if(bas==null)
	       {
	          new MinorLogMsg(this,"BASService is null",null).log(getContext());
	          throw new ServiceError("BASService is null");
	       }
	  		bas.acctAdjustEx(msisdn, 
	  				amountAdjustType, 
					csrInputAmt, 
					amount, 
					svcAdjustType, 
					csrInputSvcFee, 
					svcFee, 
					date, 
					expiryDaysExt, 
					resrv1, 
					resrv2); 
	  		
  	} catch (ParseException pe) {
        new MinorLogMsg(this,pe.getMessage(),pe).log(getContext());
  		throw new ServiceError("Fail to apply adjustment to " + msisdn + " due to wrong transaction date format" + transDate); 	  		
		}catch(Exception e){
	        new MinorLogMsg(this,e.getMessage(),e).log(getContext());
  		throw new ServiceError("Fail to apply adjustment to " + msisdn + " " + e.getMessage()); 
  	}  
 
 return 0; 
   }
   
   public SubscriberInfo getSub(String msisdn ) throws ServiceError
   {
   		Subscriber subscriber = null; 
   		try {
   			sub = getSubscriberService(getContext());
   			if(sub==null)
   			{
   				new MinorLogMsg(this,"SubscriberService is null",null).log(getContext());
   				throw new ServiceError("SubscriberService is null");
   			}
   			subscriber = sub.getSub(msisdn ); 
   		} catch ( SubscriberServiceException se){
   	        new MinorLogMsg(this,se.getMessage(),se).log(getContext());
   			throw new ServiceError("fail to get subscirber " + msisdn + " " +  se.getMessage()); 
   		} catch(Exception e)
	      {
   	        new MinorLogMsg(this,e.getMessage(),e).log(getContext());
   	        throw new ServiceError("Cannot get subscriber. Original message:"+e.getMessage());
   	     }
   		
   		if ( sub != null ){
   			SubscriberInfo info = new SubscriberInfo(); 
   			//info.dateCreated = sub.getStartDate().toString(); 
            // TODO 2008-08-22 date of birthday no longer part of Subscriber
   			info.firstName = "";
   			info.lastName = "";
   			info.MSISDN = msisdn; 
   			info.spid = subscriber.getSpid();
   			info.startDate = subscriber.getStartDate()==null?"":subscriber.getStartDate().toString(); 
   			info.state = getSubState(subscriber.getState()); 
   			info.subscriberType = getSubType(subscriber.getSubscriberType()); 
   			return info; 
   		}
        new MinorLogMsg(this,"fail to find suscriber" + msisdn,null).log(getContext());
   		throw new ServiceError("fail to find subscriber " + msisdn);  
   }
   
   public  com.redknee.app.crm.provision.corba.AccountStateEnum getAccountStateByMsisdn(String msisdn) 
   throws ServiceError{
   		Account account = null; 
   		
   		try{
   			account = AccountSupport.getAccountByMsisdn(this.getContext(), msisdn); 
   		} catch (HomeException he){
   	        new MinorLogMsg(this,he.getMessage(),he).log(getContext());
   			throw new ServiceError("Fail to get Account state for "+ msisdn); 
   		}catch(Exception e)
	    {
   	        new MinorLogMsg(this,e.getMessage(),e).log(getContext());
   	        throw new ServiceError("Cannot get account. Original message:"+e.getMessage());
   	     }
   		
   		if ( account!= null )
   			return  getAccountState(account.getState());

   		new MinorLogMsg(this,"fail to find account state for " + msisdn,null).log(getContext());
   		throw new ServiceError("fail to find account state for " + msisdn);  
   }
   
   
   public com.redknee.app.crm.provision.corba.AccountStateEnum getAccountState(com.redknee.app.crm.bean.AccountStateEnum state){
	 	if ( state.equals( com.redknee.app.crm.bean.AccountStateEnum.ACTIVE)){
   	 		return com.redknee.app.crm.provision.corba.AccountStateEnum.ACCT_ACTIVE; 
   	 	} else if (state.equals( com.redknee.app.crm.bean.AccountStateEnum.SUSPENDED ) ){
   	 		return com.redknee.app.crm.provision.corba.AccountStateEnum.ACCT_SUSPENDED;    	 		
   	 	} else if (state.equals( com.redknee.app.crm.bean.AccountStateEnum.NON_PAYMENT_WARN ) ){
   	 		return com.redknee.app.crm.provision.corba.AccountStateEnum.ACCT_WARNED;    	 		
   	 	}else if (state.equals( com.redknee.app.crm.bean.AccountStateEnum.NON_PAYMENT_SUSPENDED ) ){
   	 		return com.redknee.app.crm.provision.corba.AccountStateEnum.ACCT_DUNNED;    	 		
   	 	}else if (state.equals( com.redknee.app.crm.bean.AccountStateEnum.PROMISE_TO_PAY ) ){
   	 		return com.redknee.app.crm.provision.corba.AccountStateEnum.ACCT_PROMISETOPAY;    	 		
   	 	} else if (state.equals( com.redknee.app.crm.bean.AccountStateEnum.IN_ARREARS  ) ){
   	 		return com.redknee.app.crm.provision.corba.AccountStateEnum.ACCT_INARREARS;    	 		
   	 	}else if (state.equals( com.redknee.app.crm.bean.AccountStateEnum.IN_COLLECTION ) ){
   	 		return com.redknee.app.crm.provision.corba.AccountStateEnum.ACCT_INCOLLECTION;    	 		
   	 	}else {
   	 		return com.redknee.app.crm.provision.corba.AccountStateEnum.ACCT_INACTIVE;    	 		
   	 	} 
   }
   
   public com.redknee.app.crm.provision.corba.SubscriberStateEnum  getSubState( com.redknee.app.crm.bean.SubscriberStateEnum state){
   	 	if ( state.equals( com.redknee.app.crm.bean.SubscriberStateEnum.ACTIVE)){
   	 		return com.redknee.app.crm.provision.corba.SubscriberStateEnum.SUB_ACTIVE; 
   	 	} else if (state.equals( com.redknee.app.crm.bean.SubscriberStateEnum.SUSPENDED ) ){
   	 		return com.redknee.app.crm.provision.corba.SubscriberStateEnum.SUB_SUSPENDED;    	 		
   	 	} else if (state.equals( com.redknee.app.crm.bean.SubscriberStateEnum.PENDING ) ){
   	 		return com.redknee.app.crm.provision.corba.SubscriberStateEnum.SUB_PENDING;    	 		
   	 	}else if (state.equals( com.redknee.app.crm.bean.SubscriberStateEnum.MOVED  ) ){
   	 		return com.redknee.app.crm.provision.corba.SubscriberStateEnum.SUB_MOVED;    	 		
   	 	}else if (state.equals( com.redknee.app.crm.bean.SubscriberStateEnum.LOCKED  ) ){
   	 		return com.redknee.app.crm.provision.corba.SubscriberStateEnum.SUB_LOCKED;    	 		
   	 	}else if (state.equals( com.redknee.app.crm.bean.SubscriberStateEnum.EXPIRED  ) ){
   	 		return com.redknee.app.crm.provision.corba.SubscriberStateEnum.SUB_EXPIRED;    	 		
   	 	}else if (state.equals( com.redknee.app.crm.bean.SubscriberStateEnum.AVAILABLE  ) ){
   	 		return com.redknee.app.crm.provision.corba.SubscriberStateEnum.SUB_AVAILABLE;    	 		
   	 	} else if (state.equals( com.redknee.app.crm.bean.SubscriberStateEnum.NON_PAYMENT_WARN ) ){
   	 		return com.redknee.app.crm.provision.corba.SubscriberStateEnum.SUB_WARNED;    	 		
   	 	}else if (state.equals( com.redknee.app.crm.bean.SubscriberStateEnum.NON_PAYMENT_SUSPENDED ) ){
   	 		return com.redknee.app.crm.provision.corba.SubscriberStateEnum.SUB_DUNNED;    	 		
   	 	}else if (state.equals( com.redknee.app.crm.bean.SubscriberStateEnum.PROMISE_TO_PAY ) ){
   	 		return com.redknee.app.crm.provision.corba.SubscriberStateEnum.SUB_PROMISETOPAY;    	 		
   	 	}else if (state.equals( com.redknee.app.crm.bean.SubscriberStateEnum.IN_ARREARS  ) ){
   	 		return com.redknee.app.crm.provision.corba.SubscriberStateEnum.SUB_INARREARS;    	 		
   	 	}else if (state.equals( com.redknee.app.crm.bean.SubscriberStateEnum.IN_COLLECTION ) ){
   	 		return com.redknee.app.crm.provision.corba.SubscriberStateEnum.SUB_INCOLLECTION;    	 		
   	 	}else {
   	 		return com.redknee.app.crm.provision.corba.SubscriberStateEnum.SUB_INACTIVE;    	 		
   	 	} 
   	
   }
 
   public com.redknee.app.crm.provision.corba.SubscriberTypeEnum  getSubType( com.redknee.app.crm.bean.SubscriberTypeEnum type)
   {
	 	if ( type.equals( com.redknee.app.crm.bean.SubscriberTypeEnum.POSTPAID)){
	 		return com.redknee.app.crm.provision.corba.SubscriberTypeEnum.POSTPAID; 
	 	} else {
	 		return com.redknee.app.crm.provision.corba.SubscriberTypeEnum.PREPAID;    	 		
	 	} 
	}
   
   
   /**
    * @return Returns the uid.
    */
   public String getUid()
   {
      return uid;
   }

   /**
    * @param uid The uid to set.
    */
   public void setUid(String uid)
   {
      this.uid = uid;
   }
   

   final SimpleDateFormat dateFormat; 

}
