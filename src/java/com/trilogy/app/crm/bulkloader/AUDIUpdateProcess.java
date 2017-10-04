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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.trilogy.app.crm.audi.AUDIUpdateLogicProcess;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountCategory;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.BillCycle;
import com.trilogy.app.crm.bean.CampaignConfig;
import com.trilogy.app.crm.bean.CreditCategory;
import com.trilogy.app.crm.bean.CustomerTypeEnum;
import com.trilogy.app.crm.bean.DealerCode;
import com.trilogy.app.crm.bean.DiscountClass;
import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.SpidIdentificationGroups;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberCategoryHome;
import com.trilogy.app.crm.bean.SubscriberCategoryXInfo;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.account.AccountIdentification;
import com.trilogy.app.crm.bean.audi.AudiUpdateSubscriber;
import com.trilogy.app.crm.contract.SubscriptionContract;
import com.trilogy.app.crm.support.AccountIdentificationSupport;
import com.trilogy.app.crm.support.AccountTypeSupportHelper;
import com.trilogy.app.crm.support.PricePlanSupport;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.PMLogMsg;

/**
 * @author amedina
 *         Process any subscriber update on any delta changes
 */
public class AUDIUpdateProcess extends AbstractAUDIProcess implements
    AUDIUpdateLogicProcess
{

	public AUDIUpdateProcess(Context ctx)
	{
		try
		{
			setSubscriberWriter(getSubscriberWriter(ctx));
		}
		catch (IOException e)
		{
			LogSupport.major(ctx, this, "AUDIUpdateProcess is not installed properly. IO Exception encountered : Cannot set the log file", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.redknee.app.crm.audi.AUDIDeleteLogicProcess#delete(com.redknee.framework
	 * .xhome.context.Context, java.lang.Object)
	 */
	@Override
	public void update(Context ctx, Object csvObject)
	{
		final PMLogMsg createSubscriberPM =
		    new PMLogMsg(PM_MODULE, "Update Subscriber");
		Subscriber subscriber = null;

		try
		{
			if (!(csvObject instanceof AudiUpdateSubscriber))
			{
				LogSupport
				    .minor(ctx, this,
				        "Expecting an AudiUpdateSubscriber object; skipping update.");
				return;
			}

			AudiUpdateSubscriber update = (AudiUpdateSubscriber) csvObject;

			// look up subscriber
			Home subHome = (Home) ctx.get(SubscriberHome.class);
			if (subHome == null)
			{
				printMessage(ctx, update.getMSISDN(),
				    "No Subscriber Home in context");
				return;
			}

			try
			{
				subscriber =
				    SubscriberSupport.lookupSubscriberForMSISDN(ctx,
				        update.getMSISDN());
			}
			catch (HomeException e)
			{
				printMessage(ctx, update.getMSISDN(),
				    "Exception caught while looking up subscriber");
				LogSupport.minor(
				    ctx,
				    this,
				    "Exception caught while looking up subscriber "
				        + update.getMSISDN(), e);
			}
			if (subscriber == null)
			{
				printMessage(ctx, update.getMSISDN(),
				    "Subscriber doesn't exist");
				return;
			}

			// look up account
			Home accountHome = (Home) ctx.get(AccountHome.class);
			if (accountHome == null)
			{
				printMessage(ctx, update.getMSISDN(),
				    "No Account Home in context");
				return;
			}

			Account account = null;
			try
			{
				account = subscriber.getAccount(ctx);
			}
			catch (HomeException e)
			{
				printMessage(ctx, update.getMSISDN(),
				    "Exception caught while looking up account");
				LogSupport.minor(
				    ctx,
				    this,
				    "Exception caught while looking up account "
				        + subscriber.getBAN(), e);
			}

			if (account == null)
			{
				printMessage(ctx, update.getMSISDN(),
				    "Cannot find account for subscriber");
				return;
			}

			// look up account type
			AccountCategory accountType =
			    AccountTypeSupportHelper.get(ctx).getTypedAccountType(ctx,
			        account.getType());
			if (accountType == null)
			{
				printMessage(ctx, update.getMSISDN(),
				    "Cannot find accountType for account");
				return;
			}

			boolean updateSubscriber = false;
			try
			{
				/*
				 * Update the subscriber. Save bean if there are any changes.
				 */
				updateSubscriber = updateSubscriber(ctx, update, subscriber);
				LogSupport.debug(ctx, this,
				    "Update subscriber " + subscriber.getId() + " = "
				        + updateSubscriber);

			}
			catch (Throwable t)
			{
				StringBuilder sb = new StringBuilder();
				sb.append(subscriber.getId());
				sb.append("-Failure Updating Subscriber-");
				sb.append(t.getMessage());
				try
				{
					printMessage(ctx, update.getMSISDN(), sb.toString());
					t.printStackTrace(getSubscriberWriter());
				}
				catch (Throwable tt)
				{
					LogSupport.info(ctx, this, "Unable to write to log file: "
					    + sb.toString(), tt);
				}
				return;
			}

			/*
			 * [Cindy Wong] TT10103152005: The account bean was previously not
			 * updated if none of the subscriber fields were modified. In
			 * addition, previously if the subscriber was updated, the account
			 * was always updated. Fixing both.
			 */
			boolean updateAccount = false;
			try
			{
				/*
				 * Update account. Save bean if any field has changed.
				 */
				if (CustomerTypeEnum.PERSONAL.equals(accountType
				    .getCustomerType()))
				{
					updateAccount = updateAccount(ctx, update, account);
				}
				LogSupport.debug(ctx, this,
				    "Update account " + account.getBAN() + " = "
				        + updateAccount);
			}
			catch (Throwable t)
			{
				// An error occurred trying to update this account
				StringBuilder msg = new StringBuilder();
				msg.append(subscriber.getBAN());
				msg.append("-Failure Updating Account-");
				msg.append(t.getMessage());
				try
				{
					printMessage(ctx, subscriber.getMSISDN(), msg.toString());
					t.printStackTrace(getSubscriberWriter());
				}
				catch (Throwable tt)
				{
					// Can't write to log file
					LogSupport.info(ctx, this,
					    "Unable to write to Account log file: " + msg, tt);
				}
				return;
			}

			if (updateSubscriber)
			{
				try
				{
					subHome.store(ctx, subscriber);
				}
				catch (Throwable t)
				{
					StringBuilder sb = new StringBuilder();
					sb.append(subscriber.getId());
					sb.append("-Failure Updating Subscriber-");
					sb.append(t.getMessage());
					try
					{
						printMessage(ctx, update.getMSISDN(), sb.toString());
						t.printStackTrace(getSubscriberWriter());
					}
					catch (Throwable tt)
					{
						LogSupport
						    .info(ctx, this, "Unable to write to log file: "
						        + sb.toString(), tt);
					}
					return;
				}
			}

			if (updateAccount)
			{
				try
				{
					accountHome.store(ctx, account);
				}
				catch (Throwable t)
				{
					// An error occurred trying to update this account
					StringBuilder msg = new StringBuilder();
					msg.append(subscriber.getBAN());
					msg.append("-Failure Updating Account-");
					msg.append(t.getMessage());
					try
					{
						printMessage(ctx, subscriber.getMSISDN(),
						    msg.toString());
						t.printStackTrace(getSubscriberWriter());
					}
					catch (Throwable tt)
					{
						// Can't write to log file
						LogSupport.info(ctx, this,
						    "Unable to write to Account log file: " + msg, tt);
					}
				}
			}
		}
		finally
		{
			createSubscriberPM.log(ctx);
			getSubscriberWriter().flush();
		}
	}

	/**
	 * @param update
	 * @param subscriber
	 * @return
	 * @throws HomeException
	 */
	private boolean updateSubscriber(Context ctx, AudiUpdateSubscriber update,
	    Subscriber subscriber) throws HomeException
	{
		/*
		 * [Cindy Wong] TT10103152005: The "change" flag was previously set
		 * incorrectly.
		 */
		boolean change = false;

		if (update.getState() != null)
		{
			change =
			    !SafetyUtil
			        .safeEquals(subscriber.getState(), update.getState());
			subscriber.setState(update.getState());
		}

		boolean ppchange = false;
		PricePlan pp = PricePlanSupport.getPlan(ctx, update.getPricePlan());
		if (pp != null)
		{
			ppchange = (update.getPricePlan() != subscriber.getPricePlan());
			change |= ppchange;
			subscriber.setPricePlan(update.getPricePlan());
		}

		// When changing price plan it is necessary to reprovision for new
		// mandatory services
		if (ppchange
		    || (update.getServices() != null && update.getServices().size() > 0))
		{
			subscriber.setServices(update.getServices());

			subscriber.populateMandatoryServicesForProvisioning(ctx);
			change = true;
		}

		if (update.getSubscriberCategory() >= 0)
		{
			Home scHome = (Home) ctx.get(SubscriberCategoryHome.class);
			Object subCategory =
			    scHome.find(new EQ(SubscriberCategoryXInfo.CATEGORY_ID, Long
			        .valueOf(update.getSubscriberCategory())));
			if (subCategory != null)
			{
				boolean fieldChange =
				    (subscriber.getSubscriberCategory() != update
				        .getSubscriberCategory());
				subscriber
				    .setSubscriberCategory(update.getSubscriberCategory());
				change |= fieldChange;
			}
		}

		DiscountClass discount =
		    getDiscountClass(ctx, subscriber.getSpid(),
		        update.getDiscountClass());

		if (discount != null)
		{
			boolean fieldChange =
			    (subscriber.getDiscountClass() != update.getDiscountClass());
			subscriber.setDiscountClass(update.getDiscountClass());
			change |= fieldChange;
		}

		DealerCode dealer =
		    getDealerCode(ctx, subscriber.getSpid(), update.getDealerCode());
		if (dealer != null)
		{
			boolean fieldChange =
			    !SafetyUtil.safeEquals(dealer.getCode(),
			        subscriber.getDealerCode());
			subscriber.setDealerCode(update.getDealerCode());
			change |= fieldChange;
		}

		CampaignConfig campaign =
		    getMarketingCampaign(ctx, subscriber.getSpid(),
		        update.getMarketingCampaign());
		if (campaign != null)
		{
			boolean fieldChange =
			    subscriber.getMarketingCampaignBean().getMarketingId() != update
			        .getMarketingCampaign();
			subscriber.getMarketingCampaignBean().setMarketingId(
			    update.getMarketingCampaign());
			change |= fieldChange;
		}

		if (update.getStardDate() != null)
		{
			boolean fieldChange =
			    !SafetyUtil.safeEquals(update.getStardDate(), subscriber
			        .getMarketingCampaignBean().getStartDate());
			subscriber.getMarketingCampaignBean().setStartDate(
			    update.getStardDate());
			change |= fieldChange;
		}

		if (update.getEndDate() != null)
		{
			boolean fieldChange =
			    !SafetyUtil.safeEquals(update.getEndDate(), subscriber
			        .getMarketingCampaignBean().getEndDate());
			subscriber.getMarketingCampaignBean().setEndDate(
			    update.getEndDate());
			change |= fieldChange;
		}

		SubscriptionContract contract =
		    getSubscriptionContract(ctx, update.getSubscriptionContractId());
		if (contract != null)
		{
			boolean fieldChange =
			    (subscriber.getSubscriptionContract(ctx) != update
			        .getSubscriptionContractId());
			subscriber.setSubscriptionContract(update
			    .getSubscriptionContractId());
			change |= fieldChange;
		}
		
        if (update.getOverdraftBalanceLimit()>=0)
        {
            subscriber.setOverdraftBalanceLimit(ctx, update.getOverdraftBalanceLimit());
            change = true;
        }
		
		return change;
	}

	/**
	 * Propagate the updates to the subscriber to the account level
	 * 
	 * @param update
	 * @param account
	 *            * @return whether the account is updated
	 * @throws HomeException
	 */
	private boolean updateAccount(Context ctx, AudiUpdateSubscriber update,
	    Account account) throws HomeException
	{
		boolean change = false;
		SpidIdentificationGroups spidIdGroups = null;
		try
		{
			spidIdGroups =
			    SpidSupport.getSpidIdentificationGroups(ctx, account.getSpid());
		}
		catch (Exception e)
		{
			LogSupport.info(ctx, this,
			    "Exception caught trying to find Spid Identification Groups info for SPID ["
			        + account.getSpid() + "]", e);
		}

        if (null == spidIdGroups)
		{
			LogSupport.info(ctx, this,
			    "No SPID Identification Groups configuration defined for SPID ["
			        + account.getSpid() + "]");
		}

		if (update.getFirstName() != null && update.getFirstName().length() > 0)
		{
			change |=
			    !SafetyUtil.safeEquals(account.getFirstName(),
			        update.getFirstName());
			account.setFirstName(update.getFirstName());
		}

		if (update.getLastName() != null && update.getLastName().length() > 0)
		{
			change |=
			    !SafetyUtil.safeEquals(account.getLastName(),
			        update.getLastName());
			account.setLastName(update.getLastName());
		}

		if (update.getAddress1() != null && update.getAddress1().length() > 0)
		{
			change |=
			    !SafetyUtil.safeEquals(account.getBillingAddress1(),
			        update.getAddress1());
			account.setBillingAddress1(update.getAddress1());
		}

		if (update.getAddress2() != null && update.getAddress2().length() > 0)
		{
			change |=
			    !SafetyUtil.safeEquals(account.getBillingAddress2(),
			        update.getAddress2());
			account.setBillingAddress2(update.getAddress2());
		}

		if (update.getAddress3() != null && update.getAddress3().length() > 0)
		{
			change |=
			    !SafetyUtil.safeEquals(account.getBillingAddress3(),
			        update.getAddress3());
			account.setBillingAddress3(update.getAddress3());
		}

		if (update.getCity() != null && update.getCity().length() > 0)
		{
			change |=
			    !SafetyUtil.safeEquals(account.getBillingCity(),
			        update.getCity());
			account.setBillingCity(update.getCity());
		}

		if (update.getProvince() != null && update.getProvince().length() > 0)
		{
			change |=
			    !SafetyUtil.safeEquals(account.getBillingProvince(),
			        update.getProvince());
			account.setBillingProvince(update.getProvince());
		}

		if (update.getPostalCode() != null && update.getPostalCode().length() > 0)
		{
			change |=
			    !SafetyUtil.safeEquals(account.getBillingPostalCode(),
			        update.getPostalCode());
			account.setBillingPostalCode(update.getPostalCode());
		}

		if (update.getCountry() != null && update.getCountry().length() > 0)
		{
			change |=
			    !SafetyUtil.safeEquals(account.getBillingCountry(),
			        update.getCountry());
			account.setBillingCountry(update.getCountry());
		}

		List accountIdList = account.getIdentificationGroupList();
		if (null == accountIdList)
		{
			accountIdList = new ArrayList();
			account.setIdentificationGroupList(accountIdList);
		}

		/*
		 * [Cindy Wong] Support the expandable AccountIdenfication bulkload/AUDI
		 * format added in 8.8/9.0.
		 */
		try
		{
			List<BulkLoadIdentification> ids =
			    AccountIdentificationSupport.parseIdentifications(ctx,
			        update.getIdType1(), update.getIdNumber1(),
			        update.getIdType2(), update.getIdNumber2());
			Set<Integer> usedIds =
			    AccountIdentificationSupport.updateAccountIdentifications(ctx,
			        account, ids);
			change |= !usedIds.isEmpty();
		}
		catch (AgentException e)
		{
			LogSupport.info(ctx, this,
			    "Exception caught while parsing the account identifications.",
			    e);
		}

		if (!SubscriberTypeEnum.PREPAID.equals(account.getSystemType())
		    && spidIdGroups != null)
		{
			Set<Integer> usedIds = new HashSet<Integer>();
			Iterator<AccountIdentification> listIt =
			    account.getIdentificationList().iterator();
			while (listIt.hasNext())
			{
				AccountIdentification ai = listIt.next();
				usedIds.add(Integer.valueOf(ai.getIdType()));
			}
			AccountIdentificationSupport.fillInIdentificationSpots(ctx,
			    account, spidIdGroups, usedIds);
		}

		DiscountClass dc =
		    getDiscountClass(ctx, account.getSpid(), update.getDiscountClass());

		if (dc != null)
		{
			change |= account.getDiscountClass() != update.getDiscountClass();
			account.setDiscountClass(update.getDiscountClass());
		}

		DealerCode dealer =
		    getDealerCode(ctx, account.getSpid(), update.getDealerCode());
		if (dealer != null)
		{
			change |=
			    !SafetyUtil.safeEquals(account.getDealerCode(),
			        update.getDealerCode());
			account.setDealerCode(update.getDealerCode());
		}

		CreditCategory cc =
		    getCreditCategory(ctx, account.getSpid(),
		        update.getCreditCategory());
		if (cc != null)
		{
			change |= account.getCreditCategory() != update.getCreditCategory();
			account.setCreditCategory(update.getCreditCategory());
		}

		BillCycle bc =
		    getBillCycle(ctx, account.getSpid(), update.getBillCycleId());
		if (bc != null)
		{
			change |= account.getBillCycleID() != update.getBillCycleId();
			account.setBillCycleID(bc.getBillCycleID());
		}

		// TODO postal code & PO box

		return change;
	}



	public static final String PM_MODULE = AUDIUpdateProcess.class.getName();

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
