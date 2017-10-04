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
package com.trilogy.app.crm.bulkloader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.trilogy.app.crm.agent.BeanInstall;
import com.trilogy.app.crm.audi.AUDILoadLogicProcess;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountCategory;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.BillCycle;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CustomerTypeEnum;
import com.trilogy.app.crm.bean.DiscountClass;
import com.trilogy.app.crm.bean.GroupTypeEnum;
import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.SpidIdentificationGroups;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.account.SecurityQuestionAnswer;
import com.trilogy.app.crm.bean.account.SubscriptionTypeEnum;
import com.trilogy.app.crm.bean.audi.AudiLoadSubscriber;
import com.trilogy.app.crm.bean.audi.AudiLoadSubscriberXInfo;
import com.trilogy.app.crm.contract.SubscriptionContract;
import com.trilogy.app.crm.factory.AccountFactory;
import com.trilogy.app.crm.support.AccountIdentificationSupport;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.AccountTypeSupportHelper;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.PackageSupportHelper;
import com.trilogy.app.crm.support.PricePlanSupport;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;

/**
 * @author amedina
 *
 * Implementation for the AUDI Logic process
 */
public class AUDIProcess extends AbstractAUDIProcess implements AUDILoadLogicProcess 
{

	public AUDIProcess(Context ctx)
	{
		setSubscriberTemplate((Subscriber)ctx.get(BeanInstall.BULK_SUBSCRIBER_TEMPLATE));
		try 
		{
			setSubscriberWriter(getSubscriberWriter(ctx));
		}
		catch (IOException e) 
		{
			LogSupport.major(ctx,this,"AUDIProcess is not installed properly. IO Exception encountered : Cannot set the log file", e);
		}
	}


	/* AUDI Subscriber Bulk Load
	 * param cvsObject AUDI subscriber
	 */
	@Override
    public void add(Context ctx, Object csvObject) 
	{
        Subscriber subscription = null;
        Account subscriber = null;
		final PMLogMsg createSubscriberPM = new PMLogMsg(PM_MODULE, "Create Subscriber");

		AudiLoadSubscriber bs = (AudiLoadSubscriber) csvObject;
        
		String subscriberBan = "";

		try
		{
			numberOfProcessedSubscribers++;
			
            assertSubscriptionMandatoryProperties(ctx, bs);
			
			boolean individualSubscriber = isIndividualSubscriber(ctx, bs.getAccountType());
			if (individualSubscriber)
			{
    	        subscription = retrieveSubscription(ctx, bs);
                //Validate Account existence
    			subscriber = AccountSupport.getAccount(ctx, bs.getBAN());
    
    			//Create account using Account Template if account doesn't exist
    			//Logic was adapted mostly from the AcctSubBulkLoadRequestServicer and AccountCreateVisitor classes 
				if (subscriber == null || !subscriber.isIndividual(ctx))
    			{
    	            assertSubscriberMandatoryProperties(bs);
    			    subscriber = createSubscriber(ctx, bs, subscriber);
    			    subscriberBan = subscriber.getBAN();
    			}

 				subscription.setBAN(subscriber.getBAN());
    			
    			createSubscription(ctx, subscription, bs);

    			//Assume this is a success
    			numberOfSuccessfullyProcessedSubscribers++;
			}
			else
			{
			    printMessageToLog(ctx, "Could not create subscription for MSISDN='" + bs.getMSISDN() + 
			            "'. Account type " +bs.getAccountType() + " is not an individual account type.");
			}
			
		}
		catch (Throwable t)
		{

            // An error occured trying to add this subscriber or subscription.
			try
			{
				//Delete subscribers we have created.
				if (subscriberBan.length() > 0)
				{
					Account uselessSubscriber = AccountSupport.getAccount(ctx, subscriberBan);
					getAccountHome(ctx).remove(ctx, uselessSubscriber);
				}
				
				t.printStackTrace(getSubscriberWriter());
			}
			catch (Throwable tt)
			{
				//Can't write to log file
				new InfoLogMsg(this, "Unable to write stack trace to subscriber log file: " + tt.getMessage(),
						tt).log(ctx);
			}
		}
		finally
		{
			createSubscriberPM.log(ctx);
			getSubscriberWriter().flush();
		}

	}
	
	private boolean isIndividualSubscriber(Context ctx, long accountTypeId)
	{
		AccountCategory accountType =
		    AccountTypeSupportHelper.get(ctx)
		        .getTypedAccountType(ctx, accountTypeId);
		return SafetyUtil.safeEquals(CustomerTypeEnum.PERSONAL,
		    accountType.getCustomerType());
	}
	
	private void createSubscription(Context ctx, Subscriber subscription, AudiLoadSubscriber bs) throws HomeException, AgentException, Throwable
	{
        SubscriberStateEnum currState = subscription.getState();

        if(currState==SubscriberStateEnum.SUSPENDED || currState==SubscriberStateEnum.PENDING)
        {
            String msg="Didn't load subscriber '" + subscription.getMSISDN() + "' because it is SUSPENDED or PENDING";
            new MinorLogMsg(this,msg,null).log(ctx);
            printMessage(ctx, bs.getMSISDN(), msg);
        }
        else
        {
            try
            {
                //Modify to pending state for create
                subscription.setState(SubscriberStateEnum.PENDING);
                
                subscription = (Subscriber) getSubscriberHome(ctx).create(ctx, subscription);
    
                String id=subscription.getId();
    
                // for some reason there are a lot of problems if you reuse the same subscriber
                // so we have to find it again
                subscription = (Subscriber) getSubscriberHome(ctx).find(ctx,id);
    
                if(subscription == null)
                {
                    String msg="Cannot find the subscriber we just added: "+id;
                    printMessage(ctx, bs.getMSISDN(), msg);
                    throw new AgentException(msg);
                }
    
                if (subscription.getState() != currState)
                {
                    subscription.setState(currState);
                    
                    // this will make the bean think it is another provisioning operation
                    subscription.resetTransientProvisionedServices();
                    
                    getSubscriberHome(ctx).store(ctx, subscription);
                }
                
                resetNonMandatoryProperties(ctx, subscription, bs);
            }
            catch (Throwable t)
            {
                StringBuilder msg = new StringBuilder();
                msg.append("Failure adding subscriber '");
                msg.append(subscription.getMSISDN());
                msg.append("': ");
                msg.append(t.getMessage());
                msg.append("\n");
                printMessage(ctx, bs.getMSISDN(), msg.toString());
                throw t;
            }
        }
	}
	
	private Account createSubscriber(Context ctx, AudiLoadSubscriber bs, Account parentSubscriber) throws Throwable
	{
        final PMLogMsg createAccountPM = new PMLogMsg(PM_MODULE, "Create Account");
        Account subscriber = null;
        try
        {
            boolean parentBan = false;
            
            if (parentSubscriber!=null)
            {
                parentBan = true;
            }

            subscriber = retrieveSubscriber(ctx, bs, parentBan);
            
            if ("0".equals(subscriber.getParentBAN()))
            {
                subscriber.setParentBAN("");
            }

            // this is the subscriber connected to the account for the single view. Because the subscriber
            // is created when a new account is created, this is !=null but doesn't have any msisdn, package
            // so account creation fails.
            subscriber.setSubscriber(null);
            
            //Try to create this account.  This create will generate
            //a unique BAN id for this Account
            subscriber = (Account) getAccountHome(ctx).create(ctx, subscriber);
            //Provided the store is successfull we have to add the
            //oldKey mapping and the state mapping if we forced the
            //state change
            
        }
        catch (Throwable t)
        {
            StringBuilder msg = new StringBuilder();
            msg.append("Failure adding account '");
            if (subscriber != null && subscriber.getBAN() != null)
            {
                msg.append(subscriber.getBAN());
            }
            else if (subscriber != null)
            {
                msg.append("UNDEFINED");
            }
            msg.append("': ");
            msg.append(t.getMessage());
            msg.append("\n");
            printMessage(ctx, bs.getMSISDN(), msg.toString());
            throw t;
        }
        finally
        {
            createAccountPM.log(ctx);
        }
        
        return subscriber;	
    }
	
	

	/**
	 * @return Subscriber Home
	 */
	private Home getSubscriberHome(Context ctx) 
	{
		return (Home) ctx.get(SubscriberHome.class);
	}
	
	/**
	 * @return Account Home
	 */
	private Home getAccountHome(Context ctx) 
	{
		return (Home) ctx.get(AccountHome.class);
	}

	/**
	 * Creates a subscriber out of the bulk load subscriber and the subscriber template.
	 * @param ctx registry
	 * @param bs bulk load subscriber
	 * @return new subscriber
	 */
	protected Subscriber retrieveSubscription(Context ctx, AudiLoadSubscriber bs) throws HomeException
	{
		Subscriber subscription = null;
		
		if(getSubscriberTemplate()!=null)
		{
			try
			{
			    subscription = (Subscriber) getSubscriberTemplate().deepClone();
			}
			catch (CloneNotSupportedException e)
			{
				if(LogSupport.isDebugEnabled(ctx))
				{
					new DebugLogMsg(this,e.getMessage(),e).log(ctx);
				}
			}
		}
		else
		{
			if(LogSupport.isDebugEnabled(ctx))
			{
				new DebugLogMsg(this,"Cannot clone subscriber template.", null).log(ctx);
			}
			
			subscription = new Subscriber();
		}
		
		copySubscriptionBulkLoadValues(ctx, subscription, bs);
		
		return subscription;
	}

	
	/**
	 * Copies the values from the audi bulk load subscriber to the subscriber
	 * @param s
	 * @param bs
	 */
	protected void copySubscriptionBulkLoadValues(Context ctx,Subscriber s, AudiLoadSubscriber bs) throws HomeException
	{
        final Date now = new Date();

		s.setBAN(bs.getBAN());
        s.setSpid(bs.getSpid());
        s.setSubscriptionType(bs.getSubscriptionType());
        s.setSubscriptionClass(bs.getSubscriptionClass());
        s.setSubscriberType(bs.getSubscriberType());
        s.setTechnology(bs.getTechnology());
        s.setMSISDN(bs.getMSISDN());
        s.setFaxMSISDN(bs.getFaxMSISDN());
        s.setDataMSISDN(bs.getDataMSISDN());
        s.setState(bs.getState());
        s.setStartDate(bs.getStartDate());
        s.setEndDate(bs.getEndDate());
        if(s.getSubscriberType()==SubscriberTypeEnum.POSTPAID)
        {
            s.setDeposit(bs.getDeposit());
        }
        else
        {
        	s.setDeposit(0);
        }
        s.setDepositDate(bs.getLastDepositDate());
        s.setCreditLimit(bs.getCreditLimit());
        s.setMaxBalance(bs.getMaxBalance());
        s.setMaxRecharge(bs.getMaxRecharge());
        s.setReactivationFee(bs.getReactivationFee());
        //s.setSupportMSISDN(bs.getSupportMSISDN());
        //s.setChargePpsm(bs.getChargePpsm());
        if (bs.getExpiryDate() == null)
        {
        	Date endDate = SubscriberSupport.getFutureEndDate(now);
        	s.setExpiryDate(CalendarSupportHelper.get(ctx).convertDateWithNoTimeOfDayToTimeZone(endDate, s.getTimeZone(ctx)));
        }
        else
        {
        	s.setExpiryDate(CalendarSupportHelper.get(ctx).convertDateWithNoTimeOfDayToTimeZone(bs.getExpiryDate(), s.getTimeZone(ctx)));
        }
        
		PricePlan pp = PricePlanSupport.getPlan(ctx,bs.getPricePlan());
		if (pp != null)
		{
			s.setPricePlan(bs.getPricePlan());
		}
        
        s.setServices(bs.getServices());
        
        s.populateMandatoryServicesForProvisioning(ctx);
        
        if (bs.getPackageId().length() == 0)
        {
            s.setPackageId(
                PackageSupportHelper.get(ctx).lookupPackageForIMSIOrMIN(
                    ctx,
                    bs.getTechnology(),
                    bs.getIMSI(),
                    bs.getSpid()).getPackId());
        }
        else
        {
        	s.setPackageId(bs.getPackageId());
        }

        s.setDealerCode(bs.getDealerCode());

		DiscountClass dc =
		    getDiscountClass(ctx, bs.getSpid(), bs.getDiscountClass());
		if (dc != null)
		{
			s.setDiscountClass(bs.getDiscountClass());
		}
        s.setInitialBalance(bs.getInitialbalance());
        if (bs.getIMSI().length() == 0)
        {
			SubscriberSupport.setIMSI(ctx, s, bs.getPackageId());
        }
        else
        {
        	s.setIMSI(bs.getIMSI());
        }

		SubscriptionContract contract =
		    getSubscriptionContract(ctx, bs.getSubscriptionContractId());
		if (contract != null)
		{
			s.setSubscriptionContract(contract.getContractId());
		}
		
        if (bs.getOverdraftBalanceLimit()>=0)
        {
            s.setOverdraftBalanceLimit(ctx, bs.getOverdraftBalanceLimit());
        }

        // this removes any provisioned services that might have been set using the template
        s.resetTransientProvisionedServices();

	}

	/** 
	 * Creates an account out of the bulk load account and the account
	 * template
	 * 
	 * @param ctx registry
	 * @param bs bulk load subscriber
	 * @return the account object
	 */
	public Account retrieveSubscriber(Context ctx, AudiLoadSubscriber bs, boolean parentBan) throws HomeException
	{
		Account subscriber = null;
		
		if(getAccountTemplate(ctx)!=null)
		{
			try
			{
			    subscriber = (Account) getAccountTemplate(ctx).deepClone();
			}
			catch (CloneNotSupportedException e)
			{
				if(LogSupport.isDebugEnabled(ctx))
				{
					new DebugLogMsg(this,e.getMessage(),e).log(ctx);
				}
			}
		}
		else
		{
			if(LogSupport.isDebugEnabled(ctx))
			{
				new DebugLogMsg(this,"Cannot clone account template",null).log(ctx);
			}
			
			try
			{
			    subscriber = (Account) XBeans.instantiate(Account.class, ctx);
			}
			catch (Throwable e)
			{
			    subscriber = new Account();
			}
		}
		
		copySubscriberBulkLoadValues(ctx, subscriber, bs, parentBan);
		
		return subscriber;
	}
	
	/**
	 * @return Returns the accountTemplate.
	 */
	public Account getAccountTemplate(Context ctx)
	{
		return (Account) ctx.get(BeanInstall.BULK_ACCOUNT_TEMPLATE);
	}
	
	/**
	 * Copies some values from the AudiLoadSubscriber to the Account
	 * @param a
	 * @param bs
	 */
	public void copySubscriberBulkLoadValues(Context ctx, Account a, AudiLoadSubscriber bs, boolean parentBan) throws HomeException
	{
        SpidIdentificationGroups spidIdGroups = null;
        try
        {
            spidIdGroups = SpidSupport.getSpidIdentificationGroups(ctx, bs.getSpid());
        }
        catch (Exception e)
        {
            LogSupport.info(ctx, this, "Exception caught trying to find Spid Identification Groups info for SPID [" + a.getSpid() + "]", e);
        }

        if (null == spidIdGroups)
        {
            LogSupport.info(ctx, this, "No SPID Identification Groups configuration defined for SPID [" + a.getSpid() + "]");
        }

        a.setContext(ctx);
		if (bs.getBAN()!=null && bs.getBAN().length() > 0)
		{
		    if (parentBan)
		    {
		        a.setParentBAN(bs.getBAN());
		    }
		    else
		    {
		        a.setBAN(bs.getBAN());
		    }
		}
		a.setType(bs.getAccountType());
	    a.setSpid(bs.getSpid());
	    
	    // Setting default values for account.
	    AccountFactory.setDetailsFromSpid(ctx, a);
	    
		a.setFirstName(bs.getFirstName());
		a.setLastName(bs.getLastName());
		a.setCreditCategory(bs.getCreditCategory());
        a.setRole(bs.getAccountRole());
		//The Account Type in the Bulk Account Template is Prepaid to begin with.  
		if(bs.getSubscriberType()==SubscriberTypeEnum.POSTPAID)
        {
			a.setSystemType(SubscriberTypeEnum.POSTPAID);
			//ali: Postpaid has the following mandatory fields
			a.setContactName(bs.getFirstName()+" "+bs.getLastName());
			a.setContactTel(bs.getMSISDN());
//			a.setQuestion("------");
//			a.setAnswer("------");
        }
		else
		{
			a.setSystemType(SubscriberTypeEnum.PREPAID);
		}
		a.setDateOfBirth(bs.getDateOfBirth());
		a.setBillingAddress1(bs.getAddress1());
		a.setBillingAddress2(bs.getAddress2());
		a.setBillingAddress3(bs.getAddress3());
		a.setBillingCity(bs.getCity());
		a.setBillingProvince(bs.getProvince());
		a.setBillingPostalCode(bs.getPostalCode());
		a.setBillingCountry(bs.getCountry());
		a.setDealerCode(bs.getDealerCode());

		// TODO billing PO Box

		// TODO DCRM parameters

		List accountIdList = a.getIdentificationGroupList();
		if(null == accountIdList)
		{
		    accountIdList = new ArrayList();
		    a.setIdentificationGroupList(accountIdList);
		}
		accountIdList.clear();

		/*
		 * [Cindy Wong] Support the expandable AccountIdenfication bulkload/AUDI
		 * format added in 8.8/9.0.
		 */
		try
		{
			List<BulkLoadIdentification> ids =
			    AccountIdentificationSupport.parseIdentifications(ctx,
			        bs.getIdType1(), bs.getIdNumber1(), bs.getIdType2(),
			        bs.getIdNumber2());
			Set<Integer> usedIds =
			    AccountIdentificationSupport.addAccountIdentifications(ctx, a,
			        ids);
			if (!SubscriberTypeEnum.PREPAID.equals(a.getSystemType()))
			{
				AccountIdentificationSupport.fillInIdentificationSpots(ctx, a,
				    spidIdGroups, usedIds);
				fillInSecurityQuestions(ctx, a);
			}
		}
		catch (AgentException e)
		{
			LogSupport.info(ctx, this,
			    "Exception caught while parsing the account identifications.",
			    e);
		}

		DiscountClass dc =
		    getDiscountClass(ctx, bs.getSpid(), bs.getDiscountClass());
		if (dc != null)
		{
			a.setDiscountClass(bs.getDiscountClass());
		}
        
		BillCycle bc = getBillCycle(ctx, bs.getSpid(), bs.getBillCycleId());
		if (bc != null)
		{
			a.setBillCycleID(bs.getBillCycleId());
		}
		else
		{
			throw new HomeException("Invalid bill cycle ID "
			    + bs.getBillCycleId());
		}

		GroupTypeEnum groupType = GroupTypeEnum.get((short) bs.getGroupType());
		if (groupType != null)
		{
			a.setGroupType(groupType);
		}
		else
		{
			throw new HomeException("Invalid group type " + bs.getGroupType());
		}

		a.setSubscriber(null);
	}
	
	private void fillInSecurityQuestions(Context ctx, Account a) throws HomeException
	{
        final CRMSpid spidInfo = SpidSupport.getCRMSpid(ctx, a.getSpid());
        int minNum = 0;
        if (null != spidInfo)
        {
            minNum = spidInfo.getMinNumSecurityQuestions();
        }

        List list = a.getSecurityQuestionsAndAnswers();
        if (null == list)
        {
            list = new ArrayList();
            a.setSecurityQuestionsAndAnswers(list);
        }
        list.clear();

        // in the case of POSTPAID or HYBRID accounts, we need to make sure there is a minimum number of security questions
        // to ensure successful account creation
        if (!SubscriberTypeEnum.PREPAID.equals(a.getSystemType()))
        {
            for (int i = 0; i < minNum; i++)
            {
                final SecurityQuestionAnswer s = new SecurityQuestionAnswer();
                s.setQuestion("---------------");
                s.setAnswer("---------------");
                list.add(s);
            }
        }	
    }
	
	/**
	 * Calculates the number of days that the expiryDate is ahead of the date
	 * @param expiryDate
	 * @param date
	 * @return
	 */
	private int getNumberOfDays(Date expiryDate, Date date)
	{
		long tExp=expiryDate.getTime();
		long tDate=date.getTime();
		
		if(tExp<tDate)
		{
			return 0;
		}
		
		return (int) ((tExp-tDate)/(24*60*60*1000)+1);
	}


	/**
	 * @return Returns the numberOfProcessedSubscribers.
	 */
	public int getNumberOfProcessedSubscribers()
	{
		return numberOfProcessedSubscribers;
	}

	/**
	 * @param numberOfProcessedSubscribers The numberOfProcessedSubscribers to set.
	 */
	public void setNumberOfProcessedSubscribers(int numberOfProcessedSubscribers)
	{
		this.numberOfProcessedSubscribers = numberOfProcessedSubscribers;
	}

	/**
	 * @return Returns the numberOfSuccessfullyProcessedSubscribers.
	 */
	public int getNumberOfSuccessfullyProcessedSubscribers()
	{
		return numberOfSuccessfullyProcessedSubscribers;
	}

	/**
	 * @param numberOfSuccessfullyProcessedSubscribers The numberOfSuccessfullyProcessedSubscribers to set.
	 */
	public void setNumberOfSuccessfullyProcessedSubscribers(int numberOfSuccessfullyProcessedSubscribers)
	{
		this.numberOfSuccessfullyProcessedSubscribers = numberOfSuccessfullyProcessedSubscribers;
	}

	/**
	 * @return Returns the subscriberTemplate.
	 */
	public Subscriber getSubscriberTemplate()
	{
		return subscriberTemplate;
	}

	/**
	 * @param subscriberTemplate The subscriberTemplate to set.
	 */
	public void setSubscriberTemplate(Subscriber subscriberTemplate)
	{
		this.subscriberTemplate = subscriberTemplate;
	}

	public void assertPostpaidSubscriptionProperties(AudiLoadSubscriber audiLoadSubscriber, CompoundIllegalStateException exceptions)
    throws IllegalArgumentException
	{
        if ( audiLoadSubscriber.getDeposit() == -1 )
        {
            exceptions.thrown(new IllegalPropertyArgumentException(AudiLoadSubscriberXInfo.DEPOSIT, "Value is required for Postpaid subscriber."));
        }
        if ( audiLoadSubscriber.getLastDepositDate() == null )
        {
            exceptions.thrown(new IllegalPropertyArgumentException(AudiLoadSubscriberXInfo.LAST_DEPOSIT_DATE, "Value is required for Postpaid subscriber."));
        }
        if ( audiLoadSubscriber.getCreditLimit() == -1 )
        {
            exceptions.thrown(new IllegalPropertyArgumentException(AudiLoadSubscriberXInfo.CREDIT_LIMIT, "Value is required for Postpaid subscriber."));
        }
        if ( audiLoadSubscriber.getDiscountClass() == -1 )
        {
            exceptions.thrown(new IllegalPropertyArgumentException(AudiLoadSubscriberXInfo.DISCOUNT_CLASS, "Value is required for Postpaid subscriber."));
        }
	}
	
	/* 
	 * Checking that mandatory fields to Postpaid are met.
	 */
	public void assertPostpaidProperties(AudiLoadSubscriber audiLoadSubscriber, CompoundIllegalStateException exceptions)
	    throws IllegalArgumentException
	{
		if ( audiLoadSubscriber.getFirstName().length() == 0)
        {
            exceptions.thrown(new IllegalPropertyArgumentException(AudiLoadSubscriberXInfo.FIRST_NAME, "Value is required for Postpaid subscriber."));
        }
		if ( audiLoadSubscriber.getLastName().length() == 0)
        {
            exceptions.thrown(new IllegalPropertyArgumentException(AudiLoadSubscriberXInfo.LAST_NAME, "Value is required for Postpaid subscriber."));
        }
	    if ( (new Date()).equals(audiLoadSubscriber.getDateOfBirth()) )
        {
            exceptions.thrown(new IllegalPropertyArgumentException(AudiLoadSubscriberXInfo.DATE_OF_BIRTH, "Value is required for Postpaid subscriber."));
        }
	    if ( audiLoadSubscriber.getAddress1().length() == 0)
        {
            exceptions.thrown(new IllegalPropertyArgumentException(AudiLoadSubscriberXInfo.ADDRESS1, "Value is required for Postpaid subscriber."));
        }
	    if ( audiLoadSubscriber.getCity().length() == 0)
        {
            exceptions.thrown(new IllegalPropertyArgumentException(AudiLoadSubscriberXInfo.CITY, "Value is required for Postpaid subscriber."));
        }
	    if ( audiLoadSubscriber.getProvince().length() == 0)
        {
            exceptions.thrown(new IllegalPropertyArgumentException(AudiLoadSubscriberXInfo.PROVINCE, "Value is required for Postpaid subscriber."));
        }
	    if ( audiLoadSubscriber.getCountry().length() == 0)
        {
            exceptions.thrown(new IllegalPropertyArgumentException(AudiLoadSubscriberXInfo.COUNTRY, "Value is required for Postpaid subscriber."));
        }
	    if ( audiLoadSubscriber.getIdNumber1().length() == 0 )
        {
            exceptions.thrown(new IllegalPropertyArgumentException(AudiLoadSubscriberXInfo.ID_NUMBER1, "Value is required for Postpaid subscriber."));
        }
	}
	
	/* 
	 * Checking that mandatory fields to Prepaid are met.
	 */
	public void assertPrepaidProperties(AudiLoadSubscriber audiLoadSubscriber, CompoundIllegalStateException exceptions)
    throws IllegalArgumentException
	{
		if ( audiLoadSubscriber.getExpiryDate() == null)
        {
            exceptions.thrown(new IllegalPropertyArgumentException(AudiLoadSubscriberXInfo.EXPIRY_DATE, "Value is required for Prepaid subscriber."));
        }
	}
	
	/* 
	 * Checking that mandatory fields to both Prepaid and Postpaid are met.
	 * Having these properties set as "REQUIRED" in the AudiLoadSubscriber model is not an option,
	 * since some properties are required for Postpaid only (and vice versa).  
	 * For absolutely mandatory fields, we particularly have to check INTs and LONGs because they 
	 * are parsed differently than STRINGs by AudiLoadSubscriberCSVSupport.parse().
	 * Either the packageID or the IMSI have to be set.  Their relation has to be checked here.
	 */
	public void assertSubscriberMandatoryProperties(AudiLoadSubscriber audiLoadSubscriber)
    	throws IllegalArgumentException
	{
	    CompoundIllegalStateException exceptions = new CompoundIllegalStateException();
	    
        //Subscriber mandatory fields
        if (audiLoadSubscriber.getAccountType() == -1)
        {
            exceptions.thrown(new IllegalPropertyArgumentException(AudiLoadSubscriberXInfo.ACCOUNT_TYPE, "Value is required."));
        }

        if (audiLoadSubscriber.getCreditCategory() == -1)
        {
            exceptions.thrown( new IllegalPropertyArgumentException(AudiLoadSubscriberXInfo.CREDIT_CATEGORY, "Value is required."));
        }
        
        if (audiLoadSubscriber.getAccountRole() == -1)
        {
            exceptions.thrown( new IllegalPropertyArgumentException(AudiLoadSubscriberXInfo.ACCOUNT_ROLE, "Value is required."));
        }
        
        if (audiLoadSubscriber.getSubscriberType().equals(SubscriberTypeEnum.POSTPAID))
        {
            assertPostpaidProperties(audiLoadSubscriber, exceptions);
        }
        
        exceptions.throwAll();
	}        
    
	public void assertSubscriptionMandatoryProperties(Context ctx, AudiLoadSubscriber audiLoadSubscriber) throws IllegalArgumentException, HomeException
    {
        CompoundIllegalStateException exceptions = new CompoundIllegalStateException();

        // Subscription mandatory fields
        if (audiLoadSubscriber.getSubscriberType() == null)
        {
            exceptions.thrown(new IllegalPropertyArgumentException(AudiLoadSubscriberXInfo.SUBSCRIBER_TYPE, "Value is required."));
        }
        

        if (audiLoadSubscriber.getTechnology() == null)
        {
            exceptions.thrown( new IllegalPropertyArgumentException(AudiLoadSubscriberXInfo.TECHNOLOGY, "Value is required."));
        }
        
        if (audiLoadSubscriber.getSubscriptionType() == -1)
        {
            exceptions.thrown( new IllegalPropertyArgumentException(AudiLoadSubscriberXInfo.SUBSCRIPTION_TYPE, "Value is required."));
        }
        
        if (audiLoadSubscriber.getSpid() == -1)
        {
            exceptions.thrown(new IllegalPropertyArgumentException(AudiLoadSubscriberXInfo.SPID, "Value is required."));
        }

        if (audiLoadSubscriber.getSubscriptionClass() == -1)
        {
            exceptions.thrown( new IllegalPropertyArgumentException(AudiLoadSubscriberXInfo.SUBSCRIPTION_CLASS, "Value is required."));
        }
        
        if (audiLoadSubscriber.getState() == null)
        {
            exceptions.thrown( new IllegalPropertyArgumentException(AudiLoadSubscriberXInfo.STATE, "Value is required."));
        }
        
        if (audiLoadSubscriber.getSubscriptionType(ctx).isOfType(SubscriptionTypeEnum.AIRTIME))
        {
            if (audiLoadSubscriber.getPackageId().length() == 0 && audiLoadSubscriber.getIMSI().length() == 0)
            {
                exceptions.thrown( new IllegalPropertyArgumentException(AudiLoadSubscriberXInfo.PACKAGE_ID,
                        "Both PackageID and IMSI values are missing.  At least one must be set."));
            }
        }
        if (audiLoadSubscriber.getPricePlan() == -1)
        {
            exceptions.thrown( new IllegalPropertyArgumentException(AudiLoadSubscriberXInfo.PRICE_PLAN, "Value is required."));
        }
        
        if (audiLoadSubscriber.getSubscriberType().equals(SubscriberTypeEnum.POSTPAID))
        {
            assertPostpaidSubscriptionProperties(audiLoadSubscriber, exceptions);
        }
        else 
        {
            assertPrepaidProperties(audiLoadSubscriber, exceptions);
        }

        exceptions.throwAll();
	}
	
	/* 
	 * Some of the non mandatory properties that are blank in an AudiLoadSubscriber are 
	 * being overwritten with default values from the Bulk Subscriber Creation Template.
	 * We want these fields to remain blank as they were specified by the CSV file.
	 */
	public void resetNonMandatoryProperties(Context ctx, Subscriber s, AudiLoadSubscriber bs)
	{
		if ( !s.getFaxMSISDN().equals(bs.getFaxMSISDN()) )
        {
            s.setFaxMSISDN(bs.getFaxMSISDN());
        }
		if ( !s.getDataMSISDN().equals(bs.getDataMSISDN()) )
        {
            s.setDataMSISDN(bs.getDataMSISDN());
        }
		if ( !s.getStartDate().equals(bs.getStartDate()) )
        {
            s.setStartDate(bs.getStartDate());
        }
		if ( bs.getEndDate() != null && !s.getEndDate().equals(bs.getEndDate()) )
        {
            s.setEndDate(bs.getEndDate());
        }
		if ( s.getMaxBalance() != bs.getMaxBalance())
        {
            s.setMaxBalance(bs.getMaxBalance());
        }
		if ( s.getMaxRecharge() != bs.getMaxRecharge())
        {
            s.setMaxRecharge(bs.getMaxRecharge());
        }
		if ( s.getReactivationFee() != bs.getReactivationFee())
        {
            s.setReactivationFee(bs.getReactivationFee());
        }
		//TODO: Readd support to PPSM to AUDI tool.
		/*if ( !s.getSupportMSISDN().equals(bs.getSupportMSISDN()))
        {
            // s.setSupportMSISDN(bs.getSupportMSISDN());
        }
		if ( s.getChargePpsm() != bs.getChargePpsm() )
        {
            // s.setChargePpsm(bs.getChargePpsm());
        }
        */
		if ( bs.getExpiryDate() != null && !s.getExpiryDate().equals(bs.getExpiryDate()) )
        {
            s.setExpiryDate(CalendarSupportHelper.get(ctx).convertDateWithNoTimeOfDayToTimeZone(bs.getExpiryDate(), s.getTimeZone(ctx)));
        }
        // TODO 2008-08-21 fields no longer part of Subscriber
//		if ( !s.getDateOfBirth().equals(bs.getDateOfBirth()) )
//			s.setDateOfBirth(bs.getDateOfBirth());
//		if ( !s.getAddress1().equals(bs.getAddress1()) )
//			s.setAddress1(bs.getAddress1());
//		if ( !s.getAddress2().equals(bs.getAddress2()) )
//			s.setAddress2(bs.getAddress2());
//		if ( !s.getAddress3().equals(bs.getAddress3()) )
//			s.setAddress3(bs.getAddress3());
//		if ( !s.getCity().equals(bs.getCity()) )
//			s.setCity(bs.getCity());
//		if ( !s.getProvince().equals(bs.getProvince()) )
//			s.setProvince(bs.getProvince());
//		if ( !s.getCountry().equals(bs.getCountry()) )
//			s.setCountry(bs.getCountry());
//		if ( s.getIdType1() != bs.getIdType1() )
//			s.setIdType1(bs.getIdType1());
//		if ( !s.getIdNumber1().equals(bs.getIdNumber1()) )
//			s.setIdNumber1(bs.getIdNumber1());
//		if ( s.getIdType2() != bs.getIdType2() )
//			s.setIdType2(bs.getIdType2());
//		if ( !s.getIdNumber2().equals(bs.getIdNumber2()) )
//			s.setIdNumber2(bs.getIdNumber2());
		if ( s.getInitialBalance() != bs.getInitialbalance() )
        {
            s.setInitialBalance(bs.getInitialbalance());
        }
	}
	
	
	public static final String	PM_MODULE	= AUDIProcess.class.getName();

	protected int numberOfProcessedSubscribers=0;
	
	protected int numberOfSuccessfullyProcessedSubscribers=0;
	
	protected Subscriber subscriberTemplate;

	/* 
	 * Used to access the Error Log from the socklet
	 * @param msg is a well formed string error message
	 */
	@Override
    public void printMessageToLog(Context ctx, String msg)
	{
		printMessage(ctx, msg);
		getSubscriberWriter().flush();
	}

}
