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
package com.trilogy.app.crm.bean;

import java.beans.PropertyChangeListener;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.api.rmi.support.AccountsApiSupport;
import com.trilogy.app.crm.bas.directDebit.DirectDebitSupport;
import com.trilogy.app.crm.bean.account.AccountIdentification;
import com.trilogy.app.crm.bean.account.AccountIdentificationGroup;
import com.trilogy.app.crm.bean.account.AccountIdentificationXInfo;
import com.trilogy.app.crm.bean.account.Contact;
import com.trilogy.app.crm.bean.account.ContactHome;
import com.trilogy.app.crm.bean.account.ContactXInfo;
import com.trilogy.app.crm.bean.account.SecurityQuestionAnswerHome;
import com.trilogy.app.crm.bean.account.SecurityQuestionAnswerXInfo;
import com.trilogy.app.crm.bean.account.SubscriptionTypeEnum;
import com.trilogy.app.crm.bean.core.AccountCategory;
import com.trilogy.app.crm.bean.core.BillCycle;
import com.trilogy.app.crm.bean.core.CreditCategory;
import com.trilogy.app.crm.bean.core.SubscriptionType;
import com.trilogy.app.crm.bean.payment.PaymentPlanHistory;
import com.trilogy.app.crm.calculation.service.CalculationService;
import com.trilogy.app.crm.calculation.service.CalculationServiceException;
import com.trilogy.app.crm.calculation.support.CalculationServiceSupport;
import com.trilogy.app.crm.client.bm.Parameters;
import com.trilogy.app.crm.config.AccountRequiredField;
import com.trilogy.app.crm.config.AccountRequiredFieldConfig;
import com.trilogy.app.crm.config.AccountRequiredFieldConfigID;
import com.trilogy.app.crm.dunning.DunningConstants;
import com.trilogy.app.crm.dunning.DunningPolicy;
import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.extension.ExtensionHolder;
import com.trilogy.app.crm.extension.ExtensionLoadingAdapter;
import com.trilogy.app.crm.extension.ExtensionSpidAdapter;
import com.trilogy.app.crm.extension.account.AccountExtension;
import com.trilogy.app.crm.extension.account.AccountExtensionXInfo;
import com.trilogy.app.crm.extension.account.GroupPricePlanExtension;
import com.trilogy.app.crm.extension.account.LoyaltyCardExtension;
import com.trilogy.app.crm.extension.account.PoolExtension;
import com.trilogy.app.crm.extension.account.PoolExtensionXInfo;
import com.trilogy.app.crm.filter.OrAccountRequiredFieldPredicate;
import com.trilogy.app.crm.home.account.AccountPropertyListeners;
import com.trilogy.app.crm.invoice.delivery.InvoiceDeliveryOptionSupport;
import com.trilogy.app.crm.move.MoveConstants;
import com.trilogy.app.crm.paymentprocessing.LateFeeEarlyRewardAccountProcessor;
import com.trilogy.app.crm.support.AccountIdentificationSupport;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.AccountTypeSupportHelper;
import com.trilogy.app.crm.support.BillingMessageSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.LicensingSupportHelper;
import com.trilogy.app.crm.support.PaymentPlanSupportHelper;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.support.SupplementaryDataSupportHelper;
import com.trilogy.app.crm.support.TaxAuthoritySupportHelper;
import com.trilogy.app.crm.util.cipher.CrmCipher;
import com.trilogy.app.crm.util.cipher.CrmEncryptingException;
import com.trilogy.app.crm.util.cipher.Encrypted;
import com.trilogy.app.crm.writeoff.WriteOffSupport;
import com.trilogy.app.crm.xhome.auth.PrincipalAware;
import com.trilogy.framework.xhome.beans.CloneFunction;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.XCloneable;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GT;
import com.trilogy.framework.xhome.elang.GTE;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.WebController;
import com.trilogy.framework.xhome.xenum.AbstractEnum;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;


/**
 * Concrete implementation of Account class.
 * 
 * @author paul.sperneac@redknee.com.
 */
public class Account extends AbstractAccount implements PrincipalAware,
    LazyLoadBean, Encrypted, AccountCrmISInterface, SupplementaryDataAware
{

	/**
	 * Serial version UID.
	 */
	public static final long serialVersionUID = 3410492883710286831L;

	/**
	 * Used to indicate that a calculated value is invalid.
	 */
	public static final int INVALID_VALUE = -9999;

	/**
	 * Registers property change listener to watch for contact data changes
	 */
	public Account()
	{
		// super();
		//
		// this.removePropertyChangeListener(new
		// ContactPropertyChangeListener());
	}
	
    public boolean isSupplementaryDataLoaded()
    {
        return this.getSupplementaryDataLoaded();
    }

    public boolean getSupplementaryDataLoaded()
	{
	    return super.getSupplementaryDataLoaded() && supplementaryDataSpid_==getSpid();
	}

    @Override
    public void setSupplementaryDataList(List supplementaryData)
    {
        synchronized (this)
        {
            if (!getSupplementaryDataLoaded())
            {
                lazyLoadSupplementaryDataInfo();
            }
        }
        super.setSupplementaryDataList(supplementaryData);
    }
    
   public List getSupplementaryDataList()
   {
       synchronized (this)
       {
           if (!getSupplementaryDataLoaded())
           {
               lazyLoadSupplementaryDataInfo();
           }
       }
       return super.getSupplementaryDataList();
   }
   
   protected synchronized void lazyLoadSupplementaryDataInfo()
   {
       final Context ctx = getContext();

       try
       {
           supplementaryDataSpid_ = getSpid();
           List<SupplementaryData> result = new ArrayList<SupplementaryData>(); 
           
           And filter = new And();
           filter.add(new EQ(SupplementaryDataReqFieldsXInfo.SPID, Integer.valueOf(this.getSpid())));
           filter.add(new EQ(SupplementaryDataReqFieldsXInfo.ENTITY, Integer.valueOf(SupplementaryDataEntityEnum.ACCOUNT_INDEX)));
           SupplementaryDataReqFields fields = HomeSupportHelper.get(ctx).findBean(ctx, SupplementaryDataReqFields.class, filter);
           if (fields!=null && fields.getFields()!=null && fields.getFields().size()>0)
           {
               Map<String, SupplementaryData> supplementaryDataMap = new HashMap<String, SupplementaryData>();
               List<SupplementaryData> supplementaryData = new ArrayList<SupplementaryData>();
               
               // Retrieving existing supplementary data
               if (!AbstractAccount.DEFAULT_BAN.equals(this.getBAN()))
               {
                   supplementaryData = new ArrayList<SupplementaryData>(getSupplementaryData(ctx));
               }

               // Populating map with existing supplementary data
               for (SupplementaryData data : supplementaryData)
               {
                   supplementaryDataMap.put(data.getKey(), data);   
               }
               
               // Adding required supplementary data to result
               for (SupplementaryDataField field : (List<SupplementaryDataField>) fields.getFields())
               {
                   SupplementaryData data = new SupplementaryData();
                   data.setEntity(SupplementaryDataEntityEnum.ACCOUNT_INDEX);
                   data.setIdentifier(this.getBAN());
                   data.setKey(field.getName());
                   
                   // Populating supplementary data value if it exists
                   if (supplementaryDataMap.get(field.getName())!=null)
                   {
                       SupplementaryData found = supplementaryDataMap.get(field.getName());
                       data.setValue(found.getValue());
                       supplementaryDataMap.remove(field.getName());
                   }
                   
                   result.add(data);
               }

               // Adding extra supplementary data to result
               for (SupplementaryData data : (Collection<SupplementaryData>) supplementaryDataMap.values())
               {
                   result.add(data);
               }
           }
           setSupplementaryDataLoaded(true);
           supplementaryDataList_ = result;
       }
       catch (HomeException e)
       {
           LogSupport.minor(ctx, this, "Unable to retrieve supplementary data", e);
       }
   }

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Context getContext()
	{
		if (context_ == null)
		{
			return ContextLocator.locate();
		}
		return context_;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setContext(final Context context)
	{
		if (getSubscriber() != null)
		{
			getSubscriber().setContext(context);
		}
		context_ = context;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean lazyLoad(Context ctx, PropertyInfo property)
	{
		if (property != null)
		{
			if (isFrozen())
			{
				new InfoLogMsg(this, "Unable to lazy-load "
				    + property.getBeanClass().getSimpleName() + "."
				    + property.getName() + " because account " + this.getBAN()
				    + " is frozen.", null).log(ctx);
			}
			else if (property.getBeanClass().isAssignableFrom(this.getClass()))
			{
				PMLogMsg pm =
				    new PMLogMsg(LazyLoadBean.class.getName(), this.getClass()
				        .getSimpleName()
				        + ".lazyLoad("
				        + property.getName()
				        + ")");
				try
				{
					property.get(this);
					return true;
				}
				catch (Throwable t)
				{
					ExceptionListener el =
					    (ExceptionListener) ctx.get(ExceptionListener.class);
					if (el != null)
					{
						el.thrown(new IllegalPropertyArgumentException(
						    property, t.getMessage()));
					}
					new MinorLogMsg(this, "Error occured lazy-loading "
					    + property.getBeanClass().getSimpleName() + "."
					    + property.getName() + ": " + t.getMessage(), t)
					    .log(ctx);
				}
				finally
				{
					pm.log(ctx);
				}
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean lazyLoadAllProperties(Context ctx)
	{
		PMLogMsg pm =
		    new PMLogMsg(LazyLoadBean.class.getName(),
		        Account.class.getSimpleName() + ".lazyLoadAllProperties()");

		Context sCtx = ctx.createSubContext();

		String sessionKey = CalculationServiceSupport.createNewSession(sCtx);
		try
		{
			for (PropertyInfo property : getLazyLoadedProperties(sCtx))
			{
				lazyLoad(sCtx, property);
			}

			return true;
		}
		catch (Throwable t)
		{
			ExceptionListener el =
			    (ExceptionListener) sCtx.get(ExceptionListener.class);
			if (el != null)
			{
				el.thrown(t);
			}
			new MinorLogMsg(this,
			    "Error occured lazy-loading properties for account "
			        + this.getBAN() + ": " + t.getMessage(), t).log(sCtx);
		}
		finally
		{
			CalculationServiceSupport.endSession(sCtx, sessionKey);
			pm.log(ctx);
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public static Collection<PropertyInfo> getLazyLoadedProperties(Context ctx)
	{
		if (lazyLoadedProperties_ == null)
		{
			lazyLoadedProperties_ =
			    AccountPropertyListeners.getLazyLoadedProperties();

			if (lazyLoadedProperties_ == null)
			{
				lazyLoadedProperties_ = new HashSet<PropertyInfo>();
			}
			lazyLoadedProperties_.addAll(Arrays.asList(
			    AccountXInfo.LAST_BILL_DATE, AccountXInfo.PAYMENT_DUE_DATE,
			    AccountXInfo.ACCOUNT_EXTENSIONS,
			    AccountXInfo.ACCUMULATED_BUNDLE_MINUTES,
			    AccountXInfo.ACCUMULATED_BUNDLE_MESSAGES,
			    AccountXInfo.ACCUMULATED_BALANCE,
			    AccountXInfo.ACCUMULATED_MDUSAGE, AccountXInfo.BLOCKED_BALANCE,
			    AccountXInfo.CURRENT_EARLY_REWARD,
                AccountXInfo.SUPPLEMENTARY_DATA_LIST,
			    AccountXInfo.CURRENT_LATE_FEE,
			    AccountXInfo.APPLIED_EARLY_REWARD));
			lazyLoadedProperties_ =
			    Collections.unmodifiableSet(lazyLoadedProperties_);
		}
		return lazyLoadedProperties_;
	}

	public RegistrationStatusEnum getRegistrationStatusEnum()
	{
		RegistrationStatusEnum statusEnum =
		    RegistrationStatusEnum.get((short) getRegistrationStatus());
		if (statusEnum == null)
		{
			statusEnum =
			    RegistrationStatusEnum.get((short) DEFAULT_REGISTRATIONSTATUS);
		}
		return statusEnum;
	}

	public void setRegistrationStatus(RegistrationStatusEnum status)
	    throws IllegalArgumentException
	{
		setRegistrationStatus(status.getIndex());
	}

	public boolean isRegistrationRequired(Context ctx)
	{
		Map<String, AccountRequiredField> registrationFields =
		    getRegistrationFields(ctx);
		return registrationFields != null && registrationFields.size() > 0;
	}

	public Map<String, AccountRequiredField> getRegistrationFields(Context ctx)
	{
		return getRequiredFields(ctx, true);
	}

	public Map<String, AccountRequiredField> getMandatoryFields(Context ctx)
	{
		return getRequiredFields(ctx, false);
	}

	protected Map<String, AccountRequiredField> getRequiredFields(Context ctx,
	    boolean registrationOnly)
	{
		Map<String, AccountRequiredField> result =
		    new HashMap<String, AccountRequiredField>();

		int[] spids = new int[]
		{
		    -1, /* Global Required Fields */
		    getSpid()
		};

		Collection<SubscriberTypeEnum> types =
		    new ArrayList<SubscriberTypeEnum>();
		types.add(getSystemType());
		if (isHybrid())
		{
			types.add(SubscriberTypeEnum.POSTPAID);
			types.add(SubscriberTypeEnum.PREPAID);
		}

		for (SubscriberTypeEnum type : types)
		{
			for (int spid : spids)
			{
				AccountRequiredFieldConfig requiredFieldConfig = null;
				AccountRequiredFieldConfigID id =
				    new AccountRequiredFieldConfigID(spid, type,
				        registrationOnly);
				try
				{
					// Get the master (all-spid) required field configuration
					requiredFieldConfig =
					    HomeSupportHelper.get(ctx).findBean(ctx,
					        AccountRequiredFieldConfig.class, id);
				}
				catch (Throwable t)
				{
					new MinorLogMsg(this,
					    "Error retrieving required field configuration for "
					        + id, t).log(ctx);
				}

				if (requiredFieldConfig != null
				    && requiredFieldConfig.getRequiredProperties() != null)
				{
					for (AccountRequiredField field : (List<AccountRequiredField>) requiredFieldConfig
					    .getRequiredProperties())
					{
						if (field != null && field.getPropertyName() != null)
						{
							AccountRequiredField oldField =
							    result.get(field.getPropertyName());
							if (oldField != null)
							{
								field =
								    (AccountRequiredField) CloneFunction
								        .instance().f(ctx, field);
								if (field.getPredicate() != null)
								{
									if (!(field.getPredicate() instanceof OrAccountRequiredFieldPredicate))
									{
										OrAccountRequiredFieldPredicate filter =
										    new OrAccountRequiredFieldPredicate();
										filter.add(field.getPredicate());
										field.setPredicate(filter);
									}
									((Or) field.getPredicate()).add(oldField
									    .getPredicate());
								}
								field.setDefaultValueValid(field
								    .isDefaultValueValid()
								    && oldField.isDefaultValueValid());
							}

							if (field.getPredicate() == null
							    || field.getPredicate().f(ctx, this))
							{
								result.put(field.getPropertyName(), field);
							}
							else
							{
								result.remove(field.getPropertyName());
							}
						}
					}
				}
			}
		}

		return result;
	}

	public boolean isBANSet()
	{
		boolean isDefaultValue =
		    (BAN_ == null || AbstractAccount.DEFAULT_BAN.equals(BAN_)
		        || "0".equals(BAN_) || "".equals(BAN_) || BAN_
		        .startsWith(MoveConstants.DEFAULT_MOVE_PREFIX));
		return !isDefaultValue;
	}

	/**
	 * Check if the HTTP request is a result of a spid selection preview
	 * page reload
	 * 
	 * @return true if screen refresh is due to a price plan related change in
	 *         GUI.
	 */
	public static boolean isFromWebNewOrPreviewOnSpid(final Context ctx)
	{
		if (ctx != null)
		{
			final HttpServletRequest req =
			    (HttpServletRequest) ctx.get(HttpServletRequest.class);

			// If a preview is occurring as a result of a price plan selection
			// set the deposit and credit limit to that of the price plan
			if (req != null
			    && (WebController.isCmd("New", req) || WebController.isCmd(
			        "Preview", req)
			        && req.getParameter("PreviewButtonSrc") != null
			        && (req.getParameter("PreviewButtonSrc").indexOf(".spid") != -1)))
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public SubscriberTypeEnum getSubscriberType()
	{
		return getSystemType();
	}

	@Override
	public void setSubscriberType(SubscriberTypeEnum subscriberType)
	{
		setSystemType(subscriberType);
	}

	/**
	 * A list of idetifications used to verify the account holder.
	 **/
	@Override
	public List getIdentificationGroupList()
	{
		synchronized (this)
		{
			if (!getAccountIdentificationLoaded())
			{
				lazyLoadAccountIdentifcationInfo();
			}
		}
		return super.getIdentificationGroupList();
	}

	@Override
	public void setIdentificationGroupList(List identificationGroups)
	{
		synchronized (this)
		{
			if (!getAccountIdentificationLoaded())
			{
				lazyLoadAccountIdentifcationInfo();
			}
		}
		super.setIdentificationGroupList(identificationGroups);
	}

	/**
	 * A list of security questions and answers that can be asked to identify a
	 * subscriber.
	 **/
	@Override
	public List getSecurityQuestionsAndAnswers()
	{
		synchronized (this)
		{
			if (!getSecurityQuestionAndAnswerLoaded())
			{
				lazyLoadSecurityQuestionAnswerInfo();
			}
		}
		return super.getSecurityQuestionsAndAnswers();
	}

	/**
	 * A list of security questions and answers that can be asked to identify a
	 * subscriber.
	 **/
	@Override
	public void setSecurityQuestionsAndAnswers(List securityQuestionAndAnswers)
	{
		synchronized (this)
		{
			if (!getSecurityQuestionAndAnswerLoaded())
			{
				lazyLoadSecurityQuestionAnswerInfo();
			}
		}
		super.setSecurityQuestionsAndAnswers(securityQuestionAndAnswers);
	}

	public List getIdentificationList(int idGroup)
	{
		List<AccountIdentification> list = null;
		Iterator<AccountIdentificationGroup> i =
		    getIdentificationGroupList().iterator();
		while (i.hasNext())
		{
			AccountIdentificationGroup aig = i.next();
			if (aig.getIdGroup() == idGroup)
			{
				list = aig.getIdentificationList();
				break;
			}
		}
		return list;
	}

	public List<AccountIdentification> getIdentificationList()
	{
		ArrayList<AccountIdentification> list =
		    new ArrayList<AccountIdentification>();
		Iterator<AccountIdentificationGroup> i =
		    getIdentificationGroupList().iterator();
		while (i.hasNext())
		{
			AccountIdentificationGroup aig = i.next();
			Iterator<AccountIdentification> j =
			    aig.getIdentificationList().iterator();
			while (j.hasNext())
			{
				AccountIdentification ai = j.next();
				if (ai.getIdType() != AccountIdentification.DEFAULT_IDTYPE)
				{
					list.add(ai);
				}
			}
		}
		return list;
	}

	/**
	 * Gets the AccountType of this Account.
	 * 
	 * @return The AccountType of this Account.
	 */
	public AccountCategory getAccountCategory()
	{
		return getAccountCategory(getContext());
	}

	/**
	 * Returns TRUE if the account is provisioned as a MOM account Returns FALSE
	 * otherwise.
	 * 
	 * @param ctx
	 *            The operating context.
	 * @return TRUE if the account is provisioned as a MOM account
	 */
	public boolean isMom(final Context ctx)
	{
		// MOM is no longer supported
		// final AccountType type = getAccountCategory(ctx);
		// if (type == null)
		// {
		// return false;
		// }
		// return type.isMom();
		return false;
	}

	/**
	 * Returns the account type of this account.
	 * 
	 * @param context
	 *            The operating context.
	 * @return The account type of this account.
	 */
	public com.redknee.app.crm.bean.core.AccountCategory getAccountCategory(final Context context)
	{
		return  (com.redknee.app.crm.bean.core.AccountCategory)AccountTypeSupportHelper.get(context).getTypedAccountType(
		    context, getType(), true);
	}

	/**
	 * Sets this account's currency to match that of the given service provider.
	 * 
	 * @param serviceProviderIdentifier
	 *            Identifier of the service provider.
	 */
	public void setCurrencyFromServiceProvider(
	    final int serviceProviderIdentifier)
	{
		if (getContext() == null)
		{
			return;
		}

		setCurrencyFromServiceProvider(getContext(), serviceProviderIdentifier);
	}

	/**
	 * Sets this account's currency to match that of the given service provider.
	 * 
	 * @param ctx
	 *            The operating context.
	 * @param serviceProviderIdentifier
	 *            Identifier of the service provider.
	 */
	public void setCurrencyFromServiceProvider(final Context ctx,
	    final int serviceProviderIdentifier)
	{
		CRMSpid spid = null;
		try
		{
			spid = SpidSupport.getCRMSpid(ctx, serviceProviderIdentifier);
		}
		catch (final HomeException exception)
		{
			new MajorLogMsg(
			    this,
			    "Could not set currency -- HomeException thrown durring CMRSpid look-up.",
			    exception).log(ctx);

			return;
		}

		if (spid == null)
		{
			new MajorLogMsg(this, "Could not set currency -- no CRMSpid \""
			    + serviceProviderIdentifier + "\"found in home.", null)
			    .log(ctx);

			return;
		}

		if (LogSupport.isDebugEnabled(ctx))
		{
			new DebugLogMsg(this, "Setting currency to default \""
			    + spid.getCurrency() + "\" from spid \""
			    + serviceProviderIdentifier + "\".", null).log(ctx);
		}

		setCurrency(spid.getCurrency());
	}

	/**
	 * Returns all the subscribers for the Account.
	 * 
	 * @param ctx
	 *            The operating context.
	 * @return The collection of all subscribers for the account number.
	 * @exception HomeException
	 *                Thrown if there is a problem with retrieving the
	 *                subscribers.
	 */
	public Collection<Subscriber> getSubscribers(final Context ctx)
	    throws HomeException
	{
		if (ctx == null)
		{
			throw new IllegalArgumentException(
			    "Could not find object.  Context parameter is null.");
		}

		return this.getImmediateChildrenSubscribers(ctx);
	}
	
	/**
	 * Get all immediate subscribers and all subscribers under its
     * sub accounts.
	 * 
	 * @param ctx
	 *            The operating context.
	 * @return All immediate children subscribers of this account..
	 * @throws HomeException
	 *             Thrown if there are problems retrieving the subscribers.
	 */
	public Collection<Subscriber> getAllSubscribers(
	    final Context ctx) throws HomeException
	{
		return AccountSupport.getAllSubscribers(ctx, this);
	}

	/**
	 * Returns all the subscribers for the Account.
	 * 
	 * @param ctx
	 *            The operating context.
	 * @return The collection of all subscribers for the account number.
	 * @exception HomeException
	 *                Thrown if there is a problem with retrieving the
	 *                subscribers.
	 */
	public Collection<Subscriber> getSubscriptions(final Context ctx,
	    SubscriptionTypeEnum type) throws HomeException
	{
		if (ctx == null)
		{
			throw new IllegalArgumentException(
			    "Could not find object.  Context parameter is null.");
		}

		Collection<Subscriber> result = new ArrayList<Subscriber>();

		SubscriptionType subType =
		    SubscriptionType.getSubscriptionType(ctx, type);
		if (subType != null)
		{
			And filter = new And();
			filter.add(new EQ(SubscriberXInfo.BAN, getBAN()));
			filter.add(new EQ(SubscriberXInfo.SUBSCRIPTION_TYPE, subType
			    .getId()));
			result =
			    HomeSupportHelper.get(ctx).getBeans(ctx, Subscriber.class,
			        filter);
		}

		return result;
	}

	/**
	 * Returns all ACTIVE subscribers for the Account.
	 * 
	 * @param ctx
	 *            The operating context.
	 * @return The collection of all ACTIVE subscribers for the account number.
	 * @exception HomeException
	 *                Thrown if there is a problem with retrieving the ACTIVE
	 *                subscribers.
	 */
	public Collection getActiveSubscribers(final Context ctx)
	    throws HomeException
	{
		if (ctx == null)
		{
			throw new IllegalArgumentException(
			    "Could not find object.  Context parameter is null.");
		}

		return AccountSupport.getActiveImmediateChildrenSubscribers(ctx,
		    getBAN());
	}

	/**
	 * Returns the start date of the most recent Payment Plan Loan Program
	 * active at
	 * the given date.
	 * If the Payment Plan History Feature is disabled or if the Account doesn't
	 * have
	 * a Payment Plan History record, the Payment Plan Start Date from
	 * the Account Profile is returned. Since the Date in the Account Profile
	 * becomes
	 * overwritten when the Account enrolls in a new Payment Plan, the date
	 * retrieved
	 * by this method will belong to the most recently enrolled Payment Plan
	 * Loan Program.
	 * 
	 * @param context
	 * @param accountId
	 *            the Account who's Payment Plan Start Date will be looked up.
	 * @param periodEndIntervalDate
	 *            the date as of which the Payment Plan is still active
	 * @return
	 * @throws HomeException
	 *             Besides other runtime errors, this method
	 *             throws exception if there is no previous Payment Plan History
	 *             for the given account.
	 */
	public Date getPaymentPlanStartDate(final Context context,
	    final Date periodEndIntervalDate)
	{
		if (PaymentPlanSupportHelper.get(context).isHistoryEnabled(context))
		{
			try
			{
				Collection<PaymentPlanHistory> list =
				    PaymentPlanSupportHelper.get(context).getLastEnrollments(
				        context, getBAN(), 1, periodEndIntervalDate);
				if (list.size() == 1)
				{
					PaymentPlanHistory record = list.iterator().next();
					return record.getRecordDate();
				}
				else
				{
					if (LogSupport.isDebugEnabled(context))
					{
						new DebugLogMsg(
						    this,
						    "The Account="
						        + getBAN()
						        + " does not have any Payment Plan History."
						        + " Returning the Payment Plan Start Date from the Account Profile:"
						        + getPaymentPlanStartDate(), null).log(context);
					}
				}
			}
			catch (HomeException e)
			{
				new MajorLogMsg(
				    this,
				    "Failed to retrieve the Payment Plan History records for Account="
				        + getBAN()
				        + ". Returning the Payment Plan Start Date from the Account Profile:"
				        + getPaymentPlanStartDate(), e).log(context);
			}
		}
		return getPaymentPlanStartDate();
	}

	/**
	 * Determine if the account is to be exempted from tax or not.
	 * 
	 * @param ctx
	 *            The operating context
	 * @param respAcc
	 *            The account whose tax exemption level is to be determined.
	 * @return
	 * @throws HomeException
	 */
	public boolean isTaxExempted(final Context ctx)
	{
		boolean isTaxExempted = isTaxExemption();
		try
		{
			Account respAcc = this;

			// fetch the responsible parent
			if (!isResponsible())
			{
				respAcc = getResponsibleParentAccount(ctx);
			}

			if (!TaxAuthoritySupportHelper.get(ctx).isTEICEnabled(ctx,
			    respAcc.getSpid()))
			{
				// tax exemption detection based on the legacy
				// 'Account.TaxExemption'
				// field.
				isTaxExempted = respAcc.isTaxExemption();
			}
			else
			{
				// tax exemption detection based on the enhanced taxation
				// control using
				// account's TEIC
				final long teicId = respAcc.getTEIC();
				final int spid = respAcc.getSpid();
				final TaxExemptionInclusion teic =
				    TaxAuthoritySupportHelper.get(ctx).getTEICById(ctx, teicId,
				        spid);
				if (teic != null)
				{
					final ExemptionInclusionTypeEnum exIncType = teic.getType();
					final int actionTa = teic.getActionTA();
					final ActionTypeEnum actionType = teic.getActionType();
					final TaxAuthority ta =
					    TaxAuthoritySupportHelper.get(ctx).getTaxAuthorityById(
					        ctx, actionTa, spid);
					if (ta != null)
					{
						double totalTaxRate =
						    TaxAuthoritySupportHelper.get(ctx)
						        .getTotalTaxPercentage(ctx, ta, spid);
						// consider the multiple tax percentages.
						// Legacy tax exemption is achieved only when selecting
						// �Exemption
						// /
						// Inclusion Type� as �All� for the main TEIC and
						// setting the TA
						// Override
						// to 0%
						if (totalTaxRate == 0
						    && exIncType.getIndex() == ExemptionInclusionTypeEnum.ALL_INDEX
						    && actionType.getIndex() == ActionTypeEnum.OVERRIDE_INDEX)
						{
							isTaxExempted = true;
						}
					}
					else
					{
						final String msg =
						    "Couldn't find the Action TA with id "
						        + actionTa
						        + " specified in the account's TEIC,using account's tax exemption field instead.";
						LogSupport.info(ctx, this, msg);
					}
				}
				else
				{
					final String msg =
					    "Couldn't find the TEIC with id " + teicId
					        + ",using account's tax exemption field instead.";
					LogSupport.info(ctx, this, msg);
				}
			}
		}
		catch (final HomeException he)
		{
			final String msg =
			    "Encountered an error while determining if the account "
			        + getBAN() + " should be exempted from tax or not.";
			LogSupport.minor(ctx, this, msg, he);
		}
		if (LogSupport.isDebugEnabled(ctx))
		{
			LogSupport.debug(ctx, this, "Is Account " + getBAN()
			    + " exempted from tax? " + isTaxExempted);
		}
		return isTaxExempted;
	}

	/**
	 * Since CRM 8.2: Return the "beginning of time": Date(0). the account
	 * hierarchy restructuring in CRM 8.0 (Mobile Money) has broken the basic
	 * assumption of the old lookup method (the top account in a hierarchy has
	 * the first activity). Rather than doing expensive lookups to the Call
	 * Detail and Transaction tables for the earliest activity date, we assume
	 * the earliest activity date was the "beginning of time". The use of this
	 * class is strictly to be used only after a verification that no previous
	 * invoice exists. Prior to CRM 8.2: Looks-up and returns the earliest known
	 * activity date for the given account. That is, the date of the earliest
	 * found adjustment or transaction for the account. The current date is
	 * returned if no activity is found.
	 * 
	 * @param ctx
	 *            The operating context.
	 * @param account
	 *            The account for which to get the earliest activity date.
	 * @return The earliest known activity date.
	 */
	public Date lookupEarliestActivityDate(final Context ctx)
	{
		Date earliestDate = new Date(0);
		if (LogSupport.isDebugEnabled(ctx))
		{
			new DebugLogMsg(this, "The earliest Activity Date for Account "
			    + getBAN() + " is date=" + earliestDate, null).log(ctx);
		}
		return earliestDate;
	}

	/**
	 * The pool mobile number.
	 */
	public String getPoolMSISDN()
	{
		String poolMSISDN = "";
		final PoolExtension extension =
		    (PoolExtension) this
		        .getFirstAccountExtensionOfType(PoolExtension.class);
		if (extension != null)
		{
			poolMSISDN = extension.getPoolMSISDN();
		}
		return poolMSISDN;
	}

	private String getPoolProperty(final Context ctx,
	    final long subscriptionType, final PropertyInfo property)
	{
		String poolID = "";
		if (this.isPooled(ctx))
		{
			final PoolExtension extension =
			    (PoolExtension) this
			        .getFirstAccountExtensionOfType(PoolExtension.class);
			if (extension != null
			    && extension.getSubscriptionPoolProperties().keySet()
			        .contains(Long.valueOf(subscriptionType)))
			{
				poolID = (String) property.get(extension);
			}
			else
			{
				LogSupport.major(ctx, this,
				    "Pooled Account missing PoolExtention " + this.getBAN());
			}
		}
		else if (this.isIndividual(ctx) && this.getParentBAN().length() > 0)
		{
			try
			{
				final Account parentAccount =
				    AccountSupport.getAccount(ctx, this.getParentBAN());
				if (parentAccount != null)
				{
					poolID =
					    parentAccount.getPoolProperty(ctx, subscriptionType,
					        property);
				}
				else
				{
					LogSupport.major(
					    ctx,
					    this,
					    "Account " + this.getParentBAN()
					        + " not found.  Unable to retrieve "
					        + property.getName(), null);
				}
			}
			catch (HomeException e)
			{
				LogSupport.major(ctx, this,
				    "Unable to retreive Account " + this.getParentBAN()
				        + ".  Unable to retrieve " + property.getName(), e);
			}
		}

		return poolID;
	}

	/**
	 * The pool id.
	 */
	public String getPoolID(final Context ctx, final long subscriptionType)
	{
		return getPoolProperty(ctx, subscriptionType, PoolExtensionXInfo.BAN);
	}

	/**
	 * The pool MSISDN.
	 */
	public String
	    getGroupMSISDN(final Context ctx, final long subscriptionType)
	{
		return getPoolProperty(ctx, subscriptionType,
		    PoolExtensionXInfo.POOL_MSISDN);
	}

	/**
     * Looks-up and returns the Group Subscriber (if one exists).
     *
     * @param ctx The operating context.
     *
     * @return The Group Subscriber if one exists; null otherwise.
     *
     * @exception HomeException Thrown if there is a problem with the
     * MsisdnHome or the SubscriberHome.
     */
    public Subscriber getGroupSubscriber(Context ctx) throws HomeException
    {
        if (ctx == null)
        {
            throw new IllegalArgumentException("Could not find object.  Context parameter is null.");
        }
        
        if (getOwnerMSISDN() == null || getOwnerMSISDN().equals(""))
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                    new DebugLogMsg(this, "Owner MSISDN not set in the account", null).log(ctx);
            }
            return null;
        }

        final Home msisdn_home = (Home) ctx.get(MsisdnHome.class);
        if (msisdn_home == null)
        {
            throw new HomeException("Could not find MsisdnHome in context.");
        }

        final com.redknee.app.crm.bean.core.Msisdn msisdn_obj = (com.redknee.app.crm.bean.core.Msisdn) msisdn_home.find(ctx, getOwnerMSISDN());
        if (msisdn_obj == null)
        {
            throw new HomeException("Could not find Msisdn for \"" + getOwnerMSISDN() + "\".");
        }

        final Home sub_home = (Home) ctx.get(SubscriberHome.class);
        if (sub_home == null)
        {
            throw new HomeException("Could not look-up subscriber.  No SubscriberHome in context.");
        }

        return (Subscriber) sub_home.find(ctx, msisdn_obj.getSubscriberID(ctx));
    }

	public AccountExtension getFirstAccountExtensionOfType(
	    final Class<? extends AccountExtension> extensionClass)
	{
		List<ExtensionHolder> accountExtensions = this.getAccountExtensions();
		for (ExtensionHolder extensionHolder : accountExtensions)
		{
			Extension extension = extensionHolder.getExtension();
			if (extensionClass.isInstance(extension))
			{
				return (AccountExtension) extension;
			}
		}

		return null;
	}

	public GroupPricePlanExtension getGroupPricePlanExtension()
	{
		return (GroupPricePlanExtension) this
		    .getFirstAccountExtensionOfType(GroupPricePlanExtension.class);
	}

	public PoolExtension getPoolExtension()
	{
		return (PoolExtension) this
		    .getFirstAccountExtensionOfType(PoolExtension.class);
	}

	/**
	 * Looks-up and returns the Owner Subscriber (if one exists).
	 * 
	 * @param ctx
	 *            The operating context.
	 * @return The Group Subscriber if one exists; null otherwise.
	 * @exception HomeException
	 *                Thrown if there is a problem with the MsisdnHome or the
	 *                SubscriberHome.
	 */
	public Subscriber getOwnerSubscriber(final Context ctx)
	    throws HomeException
	{
		if (isIndividual(ctx))
		{
            Collection<Subscriber> subs = HomeSupportHelper.get(ctx).getBeans(ctx, Subscriber.class,
                    new EQ(SubscriberXInfo.BAN, getBAN()), true, SubscriberXInfo.SUBSCRIPTION_TYPE);
            
            Subscriber sub = null;
            
            if (subs!=null && subs.size()>0)
            {
                for (Subscriber s : subs)
                {
                    s.setContext(ctx);
                    if (!s.isInFinalState())
                    {
                        if (s.getSubscriptionType(ctx).getType() == SubscriptionTypeEnum.AIRTIME_INDEX)
                        {
                            sub = s;
                            break;
                        }
                    }
                }
                
                if (LogSupport.isDebugEnabled(ctx))
                {
                    if (sub==null)
                    {
                        new DebugLogMsg(this, "getOwnerSubscriber() - Individual Account " + this.getBAN()
                                + " No non-deactivated airtime subscription found.", null).log(ctx);
                    }
                    else
                    {
                        new DebugLogMsg(this, "getOwnerSubscriber() - Individual Account " + this.getBAN()
                                + " Returning Subscriber : " + sub, null).log(ctx);
                    }
                }
            }
            else
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "getOwnerSubscriber() - Individual Account " + this.getBAN()
                            + " No subscription found.", null).log(ctx);
                }
            }
            
            return sub;
		}

		if (getOwnerMSISDN() == null || getOwnerMSISDN().length() == 0)
		{
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "getOwnerSubscriber() - Group Account " + this.getBAN()
                        + " Owner Msisdn Not confiured, Returning null.", null).log(ctx);
            }
			return null;
		}

		if (ctx == null)
		{
			throw new IllegalArgumentException(
			    "Could not find object.  Context parameter is null.");
		}

		return SubscriberSupport.lookupSubscriberForMSISDN(ctx,
		    getOwnerMSISDN());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Date getLastBillDate()
	{
		synchronized (this)
		{
			if (lastBillDate_ == null)
			{
				AccountSupport.updateInvoiceDates(this);
			}
		}

		return lastBillDate_;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Date getPaymentDueDate()
	{
		synchronized (this)
		{
			if (paymentDueDate_ == null)
			{

				AccountSupport.updateInvoiceDates(this);
			}
		}

		return paymentDueDate_;
	}

	/**
	 * @param ctx
	 *            - don't pass null context. Use the other variant
	 * @return - null if PaymentDueDate is not set as per last invoice
	 * @throws HomeException
	 */
	public Date getPaymentDueDate(Context ctx)
	{
		if (ctx == null)
		{
			throw new IllegalArgumentException(
			    "Could not find object.  Context parameter is null.");
		}
		return getPaymentDueDate();
	}

	/**
	 * @param ctx
	 *            - don't pass null context.
	 * @return - null if PaymentDueDate is not set or proper date to-be-dunned
	 * @throws HomeException
	 */
	public Date getToBeDunnedDate(Context ctx) throws HomeException
	{
		if (ctx == null)
		{
			throw new IllegalArgumentException(
			    "Could not find object.  Context parameter is null.");
		}
		final Date date = getPaymentDueDate();
		if (date != null)
		{
			final CreditCategory creditCategory =
			    (CreditCategory) ((Home) ctx.get(CreditCategoryHome.class))
			        .find(ctx, getCreditCategory());
			final int inDunnigGraceDays;
			if (null != creditCategory
			    && creditCategory.getDunningConfiguration() != DunningConfigurationEnum.SERVICE_PROVIDER)
			{
				inDunnigGraceDays = creditCategory.getGraceDaysDunning();
			}
			else
			{
				inDunnigGraceDays =
				    AccountSupport.getServiceProvider(ctx, this)
				        .getGraceDayDunning();
			}
			final Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			calendar.add(Calendar.DATE, inDunnigGraceDays);
			return calendar.getTime();
		}
		return date;
	}

	/**
	 * @param ctx
	 *            - don't pass null context.
	 * @return - null if PaymentDueDate is not set or proper date
	 *         to-be-in-arrears
	 * @throws HomeException
	 */
	public Date getToBeInArrearsDate(Context ctx) throws HomeException
	{
		if (ctx == null)
		{
			throw new IllegalArgumentException(
			    "Could not find object.  Context parameter is null.");
		}
		final Date date = getPaymentDueDate();
		if (date != null)
		{
			final CreditCategory creditCategory =
			    (CreditCategory) ((Home) ctx.get(CreditCategoryHome.class))
			        .find(ctx, getCreditCategory());
			final int inArrearGraceDays;
			if (null != creditCategory
			    && creditCategory.getDunningConfiguration() != DunningConfigurationEnum.SERVICE_PROVIDER)
			{
				inArrearGraceDays = creditCategory.getGraceDaysInArrears();
			}
			else
			{
				inArrearGraceDays =
				    AccountSupport.getServiceProvider(ctx, this)
				        .getGraceDayInArrears();
			}
			final Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			calendar.add(Calendar.DATE, inArrearGraceDays);
			return calendar.getTime();
		}
		return date;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void assertContactFax(final String contactFax)
	    throws IllegalArgumentException
	{
		super.assertContactFax(contactFax);

		final String trimmedFax = contactFax.trim();
		if (trimmedFax.length() > 0 && trimmedFax.length() < 7)
		{
			throw new IllegalArgumentException(
			    "Contact Fax Must have at least 7 digits if set.");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getAccumulatedBundleMinutes()
	{
		synchronized (this)
		{
			if (accumulatedBundleMinutes_ == AccountSupport.INVALID_VALUE)
			{
				// TODO uses FCT, upgrade to BM
				// AccountSupport.updateAccumulatedBundledMinutes(this);
				accumulatedBundleMinutes_ = 0;
			}
		}

		return accumulatedBundleMinutes_;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getAccumulatedBundleMessages()
	{
		synchronized (this)
		{
			if (accumulatedBundleMessages_ == AccountSupport.INVALID_VALUE)
			{
				// TODO uses FCT, upgrade to BM
				// AccountSupport.updateAccumulatedBundledMessages(this);
				accumulatedBundleMessages_ = 0;
			}
		}

		return accumulatedBundleMessages_;
	}

	/**
	 * @deprecated Use {@link #getAppliedEarlyReward(Context, String)}
	 */
	@Deprecated
	@Override
	public long getAppliedEarlyReward()
	{
		String mySessionKey = (String) getContext().get(TRANSIENT_EQUALS_SESSION_KEY);
		return getAppliedEarlyReward(getContext(), mySessionKey);
	}

	/**
	 * Returns the early reward applied since the most recent invoice.
	 * 
	 * @param ctx
	 *            The operating context.
	 * @param sessionKey
	 *            Calculation service session key. Use null if no session has
	 *            been established -- a temporary session will be established if
	 *            no session key is provided.
	 * @return The early reward applied since the most recent invoice.
	 */
	public long getAppliedEarlyReward(final Context ctx, String sessionKey)
	{
		synchronized (this)
		{
			if (appliedEarlyReward_ == AccountSupport.INVALID_VALUE)
			{
				Context myCtx = ctx;
				String mySessionKey = sessionKey;
				if (sessionKey == null)
				{
					myCtx = ctx.createSubContext();
					mySessionKey =
					    CalculationServiceSupport.createNewSession(myCtx);
				}
				try
				{
					CalculationService service =
					    (CalculationService) myCtx
					        .get(CalculationService.class);
					appliedEarlyReward_ =
					    service.getAppliedEarlyReward(myCtx, mySessionKey,
					        getBAN(), new Date());
				}
				catch (CalculationServiceException e)
				{
					LogSupport.minor(ctx, this,
					    "Failed to fetch applied early reward for account.", e);
				}
				finally
				{
					if (sessionKey == null)
					{
						CalculationServiceSupport.endSession(myCtx,
						    mySessionKey);
					}
				}
			}
			return appliedEarlyReward_;
		}
	}

	/**
	 * @deprecated Use {@link #getCurrentLateFee(Context, String)} instead.
	 */
	@Override
	@Deprecated
	public long getCurrentLateFee()
	{
		String mySessionKey = (String) getContext().get(TRANSIENT_EQUALS_SESSION_KEY);
		return getCurrentLateFee(getContext(), mySessionKey);
	}

	/**
	 * Returns the current applicable late fee.
	 * 
	 * @param ctx
	 *            The operating context.
	 * @param sessionKey
	 *            Calculation service session key. Use null if no session has
	 *            been established -- a temporary session will be established if
	 *            no session key is provided.
	 * @return The current late fee applicable to the most recent overdue
	 *         invoice.
	 */
	public long getCurrentLateFee(Context ctx, String sessionKey)
	{
		synchronized (this)
		{
			if (currentLateFee_ == AccountSupport.INVALID_VALUE)
			{
				Context myCtx = ctx;
				String mySessionKey = sessionKey;
				if (sessionKey == null)
				{
					myCtx = ctx.createSubContext();
					mySessionKey =
					    CalculationServiceSupport.createNewSession(myCtx);
				}
				try
				{
					Object result =
					    LateFeeEarlyRewardAccountProcessor
					        .getApplicableLateFeeInstance().processAccount(
					            myCtx, this, new Date());
					if (result != null)
					{
						currentLateFee_ = ((Number) result).longValue();
					}
				}
				finally
				{
					if (sessionKey == null)
					{
						CalculationServiceSupport.endSession(myCtx,
						    mySessionKey);
					}
				}
			}
			return currentLateFee_;
		}
	}

	/**
	 * @deprecated Use {@link #getCurrentEarlyReward(Context, String)} instead.
	 */
	@Override
	@Deprecated
	public long getCurrentEarlyReward()
	{
		String mySessionKey = (String) getContext().get(TRANSIENT_EQUALS_SESSION_KEY);
		return getCurrentEarlyReward(getContext(), mySessionKey);
	}

	/**
	 * Returns the current applicable early reward if the invoice is paid in
	 * full today.
	 * 
	 * @param ctx
	 *            The operating context.
	 * @param sessionKey
	 *            Calculation service session key. Use null if no session has
	 *            been established -- a temporary session will be established if
	 *            no session key is provided.
	 * @return The current early reward applicable to the most recent invoice if
	 *         it is paid in full today.
	 */
	public long getCurrentEarlyReward(Context ctx, String sessionKey)
	{
		synchronized (this)
		{
			if (currentEarlyReward_ == AccountSupport.INVALID_VALUE)
			{
				Context myCtx = ctx;
				String mySessionKey = sessionKey;
				if (sessionKey == null)
				{
					myCtx = ctx.createSubContext();
					mySessionKey =
					    CalculationServiceSupport.createNewSession(myCtx);
				}
				try
				{
					Object result =
					    LateFeeEarlyRewardAccountProcessor
					        .getApplicableEarlyRewardInstance().processAccount(
					            myCtx, this, new Date());
					if (result != null)
					{
						currentEarlyReward_ = ((Number) result).longValue();
					}
				}
				finally
				{
					if (sessionKey == null)
					{
						CalculationServiceSupport.endSession(myCtx,
						    mySessionKey);
					}
				}
			}
			return currentEarlyReward_;
		}
	}

	/**
	 * Returns the accumulated other adjustments of this account since the last
	 * invoice.
	 * 
	 * @param ctx
	 *            The operating context.
	 * @param sessionKey
	 *            Calculation service session key. Use null if no session has
	 *            been established -- a temporary session will be established if
	 *            no session key is provided.
	 * @return The accumulated other adjustments of this account since the last
	 *         invoice.
	 */
	public long getAccumulatedOtherAdjustments(final Context ctx,
	    String sessionKey)
	{
		synchronized (this)
		{
			if (accumulatedOtherAdjustments_ == AccountSupport.INVALID_VALUE)
			{
				Context myCtx = ctx;
				String mySessionKey = sessionKey;
				if (sessionKey == null)
				{
					myCtx = ctx.createSubContext();

					mySessionKey =
					    CalculationServiceSupport.createNewSession(myCtx);
				}
				try
				{
					/*
					 * [Cindy Wong] Ideally we would have a single
					 * CalculationService call, but since it's not available,
					 * we're
					 * making two calls instead.
					 */
					accumulatedOtherAdjustments_ =
					    getAccumulatedMDUsage(myCtx, mySessionKey)
					        - getAccumulatedPayment(myCtx, mySessionKey);
				}
				finally
				{
					if (sessionKey == null)
					{
						CalculationServiceSupport.endSession(myCtx,
						    mySessionKey);
					}
				}
			}
			return accumulatedOtherAdjustments_;
		}
	}

	/**
	 * Returns the accumulated other adjustments of this account since the last
	 * invoice.
	 * 
	 * @return The accumulated other adjustments of this account since the last
	 *         invoice.
	 * @see com.redknee.app.crm.bean.AbstractAccount#getAccumulatedOtherAdjustments()
	 * @deprecated Use {@link #getAccumulatedOtherAdjustments(Context, String)}
	 *             instead.
	 */
	@Override
	@Deprecated
	public long getAccumulatedOtherAdjustments()
	{
		String mySessionKey = (String) getContext().get(TRANSIENT_EQUALS_SESSION_KEY);
		return getAccumulatedOtherAdjustments(getContext(), mySessionKey);
	}

	/**
	 * Returns the accumulated payment of this account since the last invoice.
	 * This is the accumulation of all payments made to all non-responsible
	 * subscribers since the last invoice.
	 * 
	 * @param ctx
	 *            The operating context.
	 * @param sessionKey
	 *            Calculation service session key. Use null if no session has
	 *            been established -- a temporary session will be established if
	 *            no session key is provided.
	 * @return The accumulated payment of this account since the last invoice.
	 */
	public long getAccumulatedPayment(final Context ctx, String sessionKey)
	{
		synchronized (this)
		{
			if (accumulatedPayment_ == AccountSupport.INVALID_VALUE)
			{
				Context myCtx = ctx;
				String mySessionKey = sessionKey;
				if (sessionKey == null)
				{
					myCtx = ctx.createSubContext();
					mySessionKey =
					    CalculationServiceSupport.createNewSession(myCtx);
				}

				CalculationService service =
				    (CalculationService) ctx.get(CalculationService.class);
				try
				{
					accumulatedPayment_ =
					    service.getAccountPaymentsReceived(myCtx, mySessionKey,
					        this.getBAN(),
 CalendarSupportHelper.get(myCtx)
					            .getRunningDate(myCtx));
				}
				catch (CalculationServiceException e)
				{
					LogSupport.minor(myCtx, this,
					    "Failed to fetch due amount for account.", e);
				}
				finally
				{
					//Session should be invalidated only if it is created by the method. Not otherwise.
					if (sessionKey == null)
					{
						CalculationServiceSupport.endSession(myCtx, mySessionKey);
					}
				}
			}
			return accumulatedPayment_;
		}
	}

	/**
	 * Returns the accumulated payment of this account since the last invoice.
	 * 
	 * @return The accumulated payment.
	 * @see com.redknee.app.crm.bean.AbstractAccount#getAccumulatedPayment()
	 * @deprecated use {@link #getAccumulatedPayment(Context, String)} instead.
	 */
	@Override
	@Deprecated
	public long getAccumulatedPayment()
	{
		String mySessionKey = (String) getContext().get(TRANSIENT_EQUALS_SESSION_KEY);
		return getAccumulatedPayment(getContext(), mySessionKey);
	}

	public long getAmountDue(Context ctx, String sessionKey)
	{
		synchronized (this)
		{
			if (amountDue_ == AccountSupport.INVALID_VALUE)
			{
				String mySessionKey = sessionKey;
				Context myCtx = ctx;
				if (sessionKey == null)
				{
					myCtx = ctx.createSubContext();
					mySessionKey =
					    CalculationServiceSupport.createNewSession(myCtx);
				}
				CalculationService service =
				    (CalculationService) ctx.get(CalculationService.class);
				try
				{
					amountDue_ =
					    service.getDueAmountForAccount(myCtx, mySessionKey,
					        this.getBAN(), new Date());
				}
				catch (CalculationServiceException e)
				{
					LogSupport.minor(ctx, this,
					    "Failed to fetch due amount for account.", e);
				}
				finally
				{
					if (sessionKey == null)
					{
						CalculationServiceSupport.endSession(myCtx,
						    mySessionKey);
					}
				}
			}
		}

		return amountDue_;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Deprecated
	public long getAmountDue()
	{
		String mySessionKey = (String) getContext().get(TRANSIENT_EQUALS_SESSION_KEY);
		return getAmountDue(getContext(), mySessionKey);
	}

	public long getAccumulatedBalance(Context ctx, String sessionKey)
	{
		synchronized (this)
		{
			if (accumulatedBalance_ == AccountSupport.INVALID_VALUE)
			{
				String mySessionKey = sessionKey;
				Context myCtx = ctx;
				if (sessionKey == null)
				{
					myCtx = ctx.createSubContext();
					mySessionKey =
					    CalculationServiceSupport.createNewSession(myCtx);
				}

				CalculationService service =
				    (CalculationService) ctx.get(CalculationService.class);
				try
				{
					accumulatedBalance_ =
					    service.getAmountOwedByAccount(ctx, mySessionKey,
					        this.getBAN(), new Date());
				}
				catch (CalculationServiceException e)
				{
					LogSupport.minor(ctx, this,
					    "Failed to fetch accumulated balance for account.", e);
				}
				finally
				{
					if (sessionKey == null)
					{
						CalculationServiceSupport.endSession(myCtx,
						    mySessionKey);
					}
				}
			}
		}

		return accumulatedBalance_;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Deprecated
	public long getAccumulatedBalance()
	{
	    String mySessionKey = (String) getContext().get(TRANSIENT_EQUALS_SESSION_KEY);
		return getAccumulatedBalance(getContext(), mySessionKey);
	}

	/**
	 * Sets the account Account balance to the accumulated balance up to this
	 * date
	 * 
	 * @param ctx
	 *            The operating context.
	 * @param sessionKey
	 *            Calculation service session key. Use null if no session has
	 *            been established -- a temporary session will be established if
	 *            no session key is provided.
	 * @param date
	 *            account accumulated balance until this date.
	 * @return accumulated balance until this date.
	 */
	public long getAccumulatedBalance(final Context ctx, String sessionKey,
	    final Date date)
	{
		synchronized (this)
		{
			if (accumulatedBalance_ == AccountSupport.INVALID_VALUE)
			{
				String myKey = sessionKey;
				Context myCtx = ctx;
				if (sessionKey == null)
				{
					myCtx = ctx.createSubContext();
					myKey = CalculationServiceSupport.createNewSession(myCtx);

				}
				CalculationService service =
				    (CalculationService) ctx.get(CalculationService.class);
				try
				{
					accumulatedBalance_ =
					    service.getAmountOwedByAccount(myCtx, myKey,
					        this.getBAN(), date);
				}
				catch (CalculationServiceException e)
				{
					LogSupport.minor(ctx, this,
					    "Failed to fetch accumulated balance for account.", e);
				}
				finally
				{
					if (sessionKey == null)
					{
						CalculationServiceSupport.endSession(myCtx, myKey);
					}
				}
			}
		}

		return accumulatedBalance_;
	}

	/**
	 * Sets the account Account balance to the accumulated balance up to this
	 * date
	 * 
	 * @param date
	 *            account accumulated balance until this date.
	 * @return accumulated balance until this date.
	 */
	@Deprecated
	public long getAccumulatedBalance(final Date date)
	{
		return getAccumulatedBalance(getContext(), null, date);
	}

	public long getAccumulatedMDUsage(Context ctx, String sessionKey)
	{
		synchronized (this)
		{
			if (accumulatedMDUsage_ == AccountSupport.INVALID_VALUE)
			{
				String mySessionKey = sessionKey;
				Context myCtx = ctx;
				if (sessionKey == null)
				{
					myCtx = ctx.createSubContext();
					mySessionKey =
					    CalculationServiceSupport.createNewSession(myCtx);
				}
				CalculationService service =
				    (CalculationService) ctx.get(CalculationService.class);
				try
				{
					accumulatedMDUsage_ =
					    service.getAccountAccumulatedMDUsage(myCtx,
					        mySessionKey, this.getBAN());
				}
				catch (CalculationServiceException e)
				{
					LogSupport.minor(ctx, this,
					    "Failed to fetch accumulated Md Usage for account.", e);
				}
				finally
				{
					if (sessionKey == null)
					{
						CalculationServiceSupport.endSession(myCtx,
						    mySessionKey);
					}
				}
			}
		}

		return accumulatedMDUsage_;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Deprecated
	public long getAccumulatedMDUsage()
	{
		String mySessionKey = (String) getContext().get(TRANSIENT_EQUALS_SESSION_KEY);
		return getAccumulatedMDUsage(getContext(), mySessionKey);
	}

	public long getBlockedBalance(Context ctx)
	{
		synchronized (this)
		{
			if (blockedBalance_ == AccountSupport.INVALID_VALUE)
			{
				AccountSupport.updateBlockedBalance(ctx, this);
			}
		}
		return blockedBalance_;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Deprecated
	public long getBlockedBalance()
	{
		return getBlockedBalance(getContext());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setParent(final Object parent)
	{
		setParentBAN((String) parent);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getParent()
	{
		return getParentBAN();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AbstractEnum getAbstractState()
	{
		return getState();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setAbstractState(final AbstractEnum state)
	{
		setState((AccountStateEnum) state);
	}

	/**
	 * Get immediate subscribers.
	 * 
	 * @param ctx
	 *            The operating context.
	 * @return All immediate children subscribers of this account..
	 * @throws HomeException
	 *             Thrown if there are problems retrieving the subscribers.
	 */
	public Collection<Subscriber> getImmediateChildrenSubscribers(
	    final Context ctx) throws HomeException
	{
		return AccountSupport.getImmediateChildrenSubscribers(ctx, this);
	}

	/**
	 * Get immediate children accounts.
	 * 
	 * @param ctx
	 *            The operating context.
	 * @return All immediate children account of this account.
	 * @throws HomeException
	 *             Thrown if there are problems retrieving the accounts.
	 */
	public Collection getImmediateChildrenAccounts(final Context ctx)
	    throws HomeException
	{
		// TODO 2007-05-24 remove unused methods
		return AccountSupport.getImmediateChildrenAccounts(ctx, getBAN());
	}

	/**
	 * Get a home containing only the immediate children accounts.
	 * 
	 * @param ctx
	 *            The operating context.
	 * @return An account home containing only the immediate children accounts
	 *         of this
	 *         account.
	 * @throws HomeException
	 *             Thrown if there are problems retrieving the account home.
	 */
	public Home getImmediateChildrenAccountHome(final Context ctx)
	    throws HomeException
	{
		return AccountSupport.getImmediateChildrenAccountHome(ctx, getBAN());
	}

	/**
	 * Get non-responsible subscribers.
	 * 
	 * @param ctx
	 *            The operating context.
	 * @return All non-responsible subscribers of this account.
	 * @throws HomeException
	 *             Thrown if there are problems retrieving the subscribers.
	 */
	public Collection getNonResponsibleSubscribers(final Context ctx)
	    throws HomeException
	{
		return AccountSupport.getNonResponsibleSubscribers(ctx, this);
	}

	/**
	 * Get immediate non-responsible children accounts.
	 * 
	 * @param ctx
	 *            The operating context.
	 * @return All immediate non-responsible children accounts of this account.
	 * @throws HomeException
	 *             Thrown if there are problems retrieving the accounts.
	 */
	public Collection getImmediateNonResponsibleChildrenAccounts(
	    final Context ctx) throws HomeException
	{
		return AccountSupport.getImmediateNonResponsibleChildrenAccountHome(
		    ctx, this.getBAN()).selectAll();
	}

	/**
	 * Get immediate responsible children accounts.
	 * 
	 * @param ctx
	 *            The operating context.
	 * @return All immediate responsible children accounts fo this account.
	 * @throws HomeException
	 *             Thrown if there are problems retrieving the accounts.
	 */
	public Collection
	    getImmediateResponsibleChildrenAccounts(final Context ctx)
	        throws HomeException
	{
		return AccountSupport.getImmediateResponsibleChildrenAccountHome(ctx,
		    this.getBAN()).selectAll();
	}

	/**
	 * Determines whether this is a root account.
	 * 
	 * @return Returns <code>true</code> if this is a root account,
	 *         <code>false</code> otherwise.
	 */
	public boolean isRootAccount()
	{
		return getParentBAN() == null || getParentBAN().trim().length() == 0;
	}

	/**
	 * Returns the bill cycle of the account.
	 * 
	 * @param ctx
	 *            The operating context.
	 * @return The bill cycle of this account.
	 * @throws HomeException
	 *             Thrown if there are problems looking up the bill cycle.
	 */
	public BillCycle getBillCycle(final Context ctx) throws HomeException
	{
		if (billCycle_ == null
		    || billCycle_.getBillCycleID() != getBillCycleID())
		{
			billCycle_ =
			    HomeSupportHelper.get(ctx).findBean(ctx, BillCycle.class,
			        Integer.valueOf(this.getBillCycleID()));
		}
		return billCycle_;
	}

	/**
	 * Returns the parent account of this account (if any).
	 * 
	 * @param ctx
	 *            The operating context.
	 * @return The parent account of this account, or <code>null</code> if this
	 *         is a
	 *         root account.
	 * @throws HomeException
	 *             Thrown if there are problems looking up the parent account.
	 */
	public Account getParentAccount(final Context ctx) throws HomeException
	{
		Account pb = null;
		if (!isRootAccount())
		{
			pb = AccountSupport.getAccount(ctx, getParentBAN());
			if (pb == null)
			{
				throw new HomeException("Account " + getBAN()
				    + " references Parent Account which does not exist ["
				    + getParentBAN() + "]");
			}
		}
		return pb;
	}

	@Override
    public String getResponsibleBAN()
    {
        Context ctx = getContext();
        if (ctx == null)
        {
            ctx = ContextLocator.locate();
        }
        try
        {
            return getResponsibleBAN(ctx);
        }
        catch (Throwable t)
        {
            handleError(ctx, t);
        }
        return responsibleBAN_;

    }


    public String getResponsibleBAN(Context ctx) throws HomeException
    {
        if (!isPrepaid() && (responsibleBAN_ == null || responsibleBAN_.isEmpty()) && !isResponsible() && getParentBAN()!=null && !getParentBAN().isEmpty())
        {
            responsibleBAN_ = getParentAccount(ctx).getResponsibleBAN();

            if (responsibleBAN_ == null ||  responsibleBAN_.isEmpty())
            {
                throw new HomeException("Responsible BAN not set for parent account " + getParentBAN());
            }
        }
        

        return responsibleBAN_;
    }


	/**
	 * Gets the responsible parent account, if it is responsible already, will
	 * return
	 * itself.
	 * 
	 * @param ctx
	 *            The operating context.
	 * @return If this is a responsible account, returns itself. Otherwise,
	 *         returns the
	 *         responsible parent account.
	 * @throws HomeException
	 *             Thrown if there are problems determining the responsible
	 *             parent
	 *             account.
	 */
	public Account getResponsibleParentAccount(final Context ctx)
	    throws HomeException
    {
        Account parent = this;
        if (!isResponsible())
        {
            if (!SafetyUtil.safeEquals(getBAN(), getResponsibleBAN(ctx)))
            {
                parent = AccountSupport.getAccount(ctx, this.getResponsibleBAN());
                
                if (parent == null)
                {
                    throw new HomeException("INVALID DATA, you have non-responsible account [" + getBAN()
                            + "] whose responsbile BAN [" + String.valueOf(this.getResponsibleBAN()) + "] does not exist.");
                }
            }
            else
            {
                throw new HomeException("INVALID DATA, you have non-responsible account [" + getBAN()
                        + "] whose responsbile BAN [" + getResponsibleBAN(ctx) + "] is same");
            }
        }
        return parent;
	}
	
	
	public DunningPolicy getDunningPolicy(final Context ctx) throws HomeException
	{
		DunningPolicy policy = null;
		policy = HomeSupportHelper.get(ctx).findBean(ctx, DunningPolicy.class, this.getDunningPolicyId());
		if(policy == null || isIsDunningExempted())
		{
			policy = DunningConstants.DUMMY_DUNNING_POLICY;
		}
		return policy;
	}

	/**
	 * Return root account object, if it is root already, returns itself.
	 * 
	 * @param ctx
	 *            The operating context.
	 * @return The root account of this account. If this account is already
	 *         root, returns
	 *         itself.
	 * @throws HomeException
	 *             Thrown if there are problems looking up the root account.
	 */
	public Account getRootAccount(final Context ctx) throws HomeException
	{
		Account parent = this;
		if (!isRootAccount())
		{
			// for checking circular references
			final Set<String> allAccountIds = new HashSet<String>();
			allAccountIds.add(getBAN());

			do
			{
				parent = parent.getParentAccount(ctx);
				// for a non-root, parent will never be null
				if (allAccountIds.contains(parent.getBAN()))
				{
					throw new HomeException(
					    "Circular reference in account topology, root="
					        + parent.getBAN() + ", sub ban=" + this.getBAN());
				}
				allAccountIds.add(parent.getBAN());
			}
			while (!parent.isRootAccount());

		}
		return parent;
	}

	/**
	 * Returns whether this is an individual account.
	 * 
	 * @param ctx
	 *            The operating context.
	 * @return Whether this is an individual account.
	 */
	public boolean isIndividual(final Context ctx)
	{
		return GroupTypeEnum.SUBSCRIBER.equals(getGroupType());
	}

	/**
	 * Returns whether this is a business account.
	 * 
	 * @param ctx
	 *            The operating context.
	 * @return Whether this is a business account.
	 */
	public boolean isBusiness(final Context ctx)
	{
		boolean result = false;
		AccountCategory ac = getAccountCategory(ctx);
		if (ac != null)
		{
			CustomerTypeEnum customerType = ac.getCustomerType();
			result = SafetyUtil.safeEquals(customerType, CustomerTypeEnum.CORPORATE);
		}
		return result;
	}

	/**
	 * Returns whether this is a pooled account.
	 * 
	 * @param ctx
	 *            The operating context.
	 * @return Whether this is a pooled account.
	 */
	public boolean isPooled(final Context ctx)
	{
        return GroupTypeEnum.GROUP_POOLED.equals(getGroupType());
	}
	
	private void updateAccountQuotaUsage(Context ctx, AccountUsage usage)
	{
	    if (LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.ACCOUNT_POOLED_QUOTA_USAGE_BALANCE_SCREEN) && this.isPooled(ctx))
	    {
	        usage.setPooled(true);
    	    usage.setGroupUsageQuota(AccountSupport.INVALID_VALUE);
            usage.setGroupUsageQuotaAllocated(0);
    	    usage.setGroupUsage(0);
    	    PoolExtension extension = getPoolExtension();
    	    if (QuotaTypeEnum.LIMITED_QUOTA.equals(extension.getQuotaType()))
    	    {
    	        usage.setGroupUsageQuota(extension.getQuotaLimit());
    	    }
    	    try
    	    {
    	        Collection<Subscriber> subscribers = AccountSupport.getNonResponsibleSubscribers(ctx, this);
    	    
        	    if (subscribers!=null)
        	    {
        	        for (Subscriber subscriber : subscribers)
        	        {
        	            if (QuotaTypeEnum.LIMITED_QUOTA.equals(subscriber.getQuotaType()))
        	            {
        	                usage.setGroupUsageQuotaAllocated(usage.getGroupUsageQuotaAllocated() + subscriber.getQuotaLimit());
        	            }

        	            if (subscriber.isPooledMemberSubscriber(ctx))
        	            {
                            Parameters parameters = SubscriberSupport.updateSubscriberSummaryABMReturnParameterList(ctx, subscriber);
                    
                            if (parameters != null)
                            {
                                usage.setGroupUsage(usage.getGroupUsage() + parameters.getGroupUsage());
                            }
        	            }
        	        }
        	    }
    	    }
    	    catch (HomeException t)
    	    {
                LogSupport.minor(ctx, this,
                        "Unable to retrieve non responsible subscriptions for account '" + this.getBAN()
                                + "' while calculating group usage: " + t.getMessage(), t);
    	    }
	    }
	    else
	    {
	        usage.setPooled(false);
	    }
	}

	/**
	 * Returns account usage.
	 * 
	 * @return Account usage.
	 */
	public AccountUsage getAccountUsage(Context ctx)
	{

		final AccountUsage usage = new AccountUsage();
		
		setAccumulatedBundleMinutes(AccountSupport.INVALID_VALUE);
		setAccumulatedBundleMessages(AccountSupport.INVALID_VALUE);
		setAccumulatedBalance(AccountSupport.INVALID_VALUE);
		setAccumulatedMDUsage(AccountSupport.INVALID_VALUE);
		setAmountDue(AccountSupport.INVALID_VALUE);
		setAccumulatedPayment(AccountSupport.INVALID_VALUE);
		setAccumulatedOtherAdjustments(AccountSupport.INVALID_VALUE);
		setAppliedEarlyReward(AccountSupport.INVALID_VALUE);
		setCurrentLateFee(AccountSupport.INVALID_VALUE);
		setCurrentEarlyReward(AccountSupport.INVALID_VALUE);

		/*
		 * KGR: I have to do this because AccountSupport doesn't return the
		 * answers
		 * directly but instead only sets an Account's values. If those values
		 * aren't used
		 * anywhere else then we can fix AccountSupport and remove the fields
		 * from
		 * Account.
		 */

		// TODO uses FCT, upgrade to BM
		// usage.setBundleMinutes(getAccumulatedBundleMinutes());
		// usage.setBundleMessages(getAccumulatedBundleMessages());

		Context subCtx = ctx.createSubContext();
		String sessionKey = CalculationServiceSupport.createNewSession(subCtx);
		try
		{
		    
			usage.setBalance(getAccumulatedBalance(subCtx, sessionKey));
			usage.setMDUsage(getAccumulatedMDUsage(subCtx, sessionKey));
			usage.setBalanceBlocked(getBlockedBalance(subCtx));
			usage.setAmountDue(getAmountDue(subCtx, sessionKey));
			usage.setPayment(getAccumulatedPayment(subCtx, sessionKey));
			//Since the 2 values are already calculated above, making use of them insted of fresh call.
//			usage.setOtherAdjustments(getAccumulatedOtherAdjustments(subCtx, sessionKey));
			usage.setOtherAdjustments(usage.getMDUsage() - usage.getPayment());
			
			usage.setAppliedEarlyReward(getAppliedEarlyReward(subCtx,
			    sessionKey));
			usage.setCurrentEarlyReward(getCurrentEarlyReward(subCtx,
			    sessionKey));
			usage.setCurrentLateFee(getCurrentLateFee(subCtx, sessionKey));
			if (getWrittenOff())
            {
                usage.setIsWrittenOff(true);
                usage.setWriteOffAmount(WriteOffSupport.getTotalWriteOffAmountForAccount(subCtx, this));
            }
		}
		finally
		{
			CalculationServiceSupport.endSession(subCtx, sessionKey);
		}

        updateAccountQuotaUsage(ctx, usage);
        
		return usage;
	}

	/**
	 * Subscriber information, modified by configureSubscriber.
	 */
	@Override
	public Subscriber getSubscriber()
	{
		final Subscriber s = subscriber_;

		if (s == null)
		{
			return null;
		}

		configureSubscriber(s);

		return s;
	}
	

    /**
     * If the account is of individual type, then return the single subscriber underneath
     * it.
     * 
     * @param ctx
     * @return
     */
    public Subscriber getIndividualSubscriber(final Context ctx)
    {
        Subscriber s = null;
        if (this.isIndividual(ctx))
        {
            s = getSubscriber();
            if (s == null)
            {
                return s;
            }
            else
            {
                s = SubscriberSupport.getSubscriberIndividualAccount(ctx, this.getBAN());
                super.setSubscriber(s);
            }
        }
        return s;
    }

	public boolean isConverge(final Context ctx)
	{
		final SubscriberTypeEnum type = getSystemType();
		if (type == null)
		{
			return false;
		}

		return type == SubscriberTypeEnum.HYBRID;
	}

	public boolean isInCollection()
	{
		if (super.getState() == AccountStateEnum.IN_COLLECTION)
		{
			return true;
		}
		return false;
	}

	/**
	 * TODO 2008-08-08 this is no longer needed
	 * Configure a subscriber with information taken from the parent Account.
	 * Used for
	 * Individual account types but could/should also be used in Subscriber
	 * factory.
	 */
	public void configureSubscriber(final Subscriber s)
	{
		// try/catch blocks required because for some fields the default values
		// is not
		// actually valid

        try
        {
            s.setSpid(getSpid());
        }
        catch (final Throwable t)
        {
        }
        try
        {
            s.setBAN(getBAN());
        }
        catch (final Throwable t)
        {
        }
        try
        {
            s.setSubscriberType(getSystemType());
        }
        catch (final Throwable t)
        {
        }
	}

	/**
	 * Discount Class for Non-Responsible Account get's inheritted from its
	 * responsible parent
	 */
	@Override
	public int getDiscountClass()
	{
		if (isPrepaid())
		{
			return Account.DEFAULT_DISCOUNTCLASS;
		}

		if (isResponsible())
		{
			return discountClass_;
		}

		final Context ctx = getContext();
		if (ctx == null || ctx.getBoolean(Common.DURING_MIGRATION, false))
		{
			return discountClass_;
		}
		try
		{
			return getDiscountClass(ctx);
		}
		catch (Throwable t)
		{
			handleError(ctx, t);
		}
		return DEFAULT_DISCOUNTCLASS;

	}

	public int getDiscountClass(Context ctx) throws HomeException
	{
		if (isPrepaid())
		{
			return Account.DEFAULT_DISCOUNTCLASS;
		}

		if (!isResponsible())
		{
            setPropertiesFromResponsibleParentAccount(ctx);
		}

		return super.getDiscountClass();
	}

	
    private void setPropertiesFromResponsibleParentAccount(Context ctx)
    {
        synchronized (this)
        {
            if (!getRespParentLoaded())
            {
                lazyLoadResponsibleParentAccount(ctx);
            }
        }
    }


    protected synchronized void lazyLoadResponsibleParentAccount(final Context ctx)
    {
        try
        {
            Account parentRespAccount = getResponsibleParentAccount(ctx);
            CRMSpid spid = AccountSupport.getServiceProvider(ctx, parentRespAccount);
            
            if (parentRespAccount != null && spid!=null && !spid.getEnableCustomDiscounts())
            {
            	super.setDiscountClass(parentRespAccount.getDiscountClass());
                super.setTaxExemption(parentRespAccount.getTaxExemption());
            }
            super.setRespParentLoaded(true);
        }
        catch (HomeException homeEx)
        {
            new MinorLogMsg(Account.class, "Unable to load responsible parent account for ban " + this.getBAN(), homeEx).log(ctx);
        }
    }

    @Override
    public boolean getTaxExemption()
    {
        if (!isResponsible() && !isPrepaid())
        {
            setPropertiesFromResponsibleParentAccount(getContext());
        }
        return super.getTaxExemption();
    }
	
	public DiscountClass getDiscountClassBean(Context ctx) throws HomeException
	{
		if (isPrepaid())
		{
			return null;
		}
		return HomeSupportHelper.get(ctx).findBean(ctx, DiscountClass.class,
		    getDiscountClass());
	}

	@Override
	public String getAgent()
	{
		return agent_;
	}

	/**
	 * @deprecated, by introducing PrincipalAware interface
	 */
	@Override
	public void setAgent(final String usr)
	{
		agent_ = usr;
	}

	/*
	 * (non-Javadoc)
	 * @see com.redknee.app.crm.bean.PrincipalAware#getPrincipal()
	 */
	@Override
	public Principal getPrincipal()
	{
		return principal_;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.redknee.app.crm.bean.PrincipalAware#setPrincipal(java.security.Principal
	 * )
	 */
	@Override
	public void setPrincipal(final Principal usr)
	{
		principal_ = usr;
		if (usr != null)
		{
			final String userName = usr.getName();
			if (userName != null)
			{
				setAgent(userName);
			}
		}
	}

	@Override
	public String getDefaultBillingMessage()
	{
		// TODO 2010-01-07 what if there is no UI ???
		return BillingMessageSupport.getUIBillingMessage(getContext(), this);
	}

	protected synchronized void lazyLoadContactInfo()
	{
		final Context ctx = getContext();

		if (AbstractAccount.DEFAULT_BAN.equals(this.getBAN()))
		{
			if (LogSupport.isDebugEnabled(ctx))
			{
				LogSupport.debug(ctx, this,
				    "Contact info NOT loaded because BAN is not set.");
			}
			setContactsLoaded(true);
			return;
		}

		final Home home = (Home) ctx.get(ContactHome.class);
		if (home == null)
		{
			// Contact home is not yet available
			LogSupport.minor(ctx, this,
			    "Contact Home is not available yet. Contact info NOT loaded.");
			setContactsLoaded(true);
			return;
		}

		try
		{
			final Collection contats =
			    home.select(ctx, new EQ(ContactXInfo.ACCOUNT, this.getBAN()));

			for (final Iterator it = contats.iterator(); it.hasNext();)
			{
				final Contact contact = (Contact) it.next();
				switch (contact.getType())
				{
					case ContactTypeEnum.PERSON_INDEX:
						setPersonFields(contact);
						break;
					case ContactTypeEnum.COMPANY_INDEX:
						setCompanyFields(contact);
						break;
					case ContactTypeEnum.BANK_INDEX:
						setBankFields(contact);
						break;
					default:
						LogSupport.debug(ctx, this,
						    "Unsupported Contact Type: " + contact.getType());
				}
			}
			setContactsLoaded(true);
		}
		catch (HomeException e)
		{
			LogSupport.minor(ctx, this, "Unable to load Contact Info", e);
		}
	}

	protected synchronized void lazyLoadAccountIdentifcationInfo()
	{
		final Context ctx = getContext();
		try
		{
			int spid = this.getSpid();
			Home spidHome = (Home) ctx.get(SpidIdentificationGroupsHome.class);
			SpidIdentificationGroups identificationGroups =
			    (SpidIdentificationGroups) spidHome.find(new EQ(
			        SpidIdentificationGroupsXInfo.SPID, Integer.valueOf(spid)));

            if (AbstractAccount.DEFAULT_BAN.equals(this.getBAN()))
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport
                        .debug(ctx, this,
                            "AccountIdentification info NOT loaded because BAN is not set.");
                }
                setAccountIdentificationLoaded(true);
                List<AccountIdentificationGroup> idList =
                    AccountSupport.createEmptyAccountIdentificationGroupsList(
                        ctx, identificationGroups);
                this.identificationGroupList_ = idList;
                return;
            }
			else
			{
                List<AccountIdentificationGroup> idList =
                    AccountSupport.createEmptyAccountIdentificationGroupsList(
                        ctx, identificationGroups);
                
                // Map of identification lists based on the group id.
                Map<Integer, List<AccountIdentification>> map = new HashMap<Integer, List<AccountIdentification>>();
                
                // List of retrieved identifications for which the group id doesn't match any of the spid identification groups configured.
                List<AccountIdentification> unmatched = new ArrayList<AccountIdentification>();
                
                // List of retrieve identifications which are not valid in the group they are supposed to be.
                List<AccountIdentification> removed = new ArrayList<AccountIdentification>();
                
                // Adding the identification lists to the map.
                Iterator<AccountIdentificationGroup> iterLists = idList.iterator();
                while (iterLists.hasNext())
                {
                    AccountIdentificationGroup idGroup = iterLists.next();
                    map.put(Integer.valueOf(idGroup.getIdGroup()), idGroup.getIdentificationList());
                }
                
                
                Collection<AccountIdentification> allIdentifications =
                        HomeSupportHelper.get(ctx).getBeans(ctx, AccountIdentification.class, new EQ(AccountIdentificationXInfo.BAN,
                                this.getBAN()), true, AccountIdentificationXInfo.ID);


                Iterator i = allIdentifications.iterator();
                while(i.hasNext())
                {
                    AccountIdentification ai = (AccountIdentification)i.next();
                    List<AccountIdentification> list = map.get(Integer.valueOf(ai.getIdGroup()));
                    // If list is not in the map, means that identification group id does not match with any of the defined groups in the spid groups configuration.
                    if (list!=null)
                    {
                        if (isIdentificationInGroup(identificationGroups, ai.getIdGroup(), ai.getIdType()))
                        {
                            if (list.get(0).getIdType()==AccountIdentification.DEFAULT_IDTYPE)
                            {
                                list.remove(0);
                                list.add(ai);
                            }
                            else
                            {
                                // If the group if full, identification should be added as unmatched as well.
                                unmatched.add(ai);
                            }
                        }
                        else
                        {
                            removed.add(ai);
                        }
                    }
                    else
                    {
                        unmatched.add(ai);
                    }

                }

                // Adding identifications that aren't in a valid group to the next available list.
                Iterator unmatchedIt = unmatched.iterator();
                while(unmatchedIt.hasNext())
                {
                    AccountIdentification ai = (AccountIdentification) unmatchedIt.next();
                    addIdentificationToNextAvailableGroup(ctx, identificationGroups, map, idList, ai);
                }
                
                // For identifications removed from the group, they will be added to
                // the same group as the group is not full. Otherwise, they will be
                // added to the next available group.
                Iterator removedIt = removed.iterator();
                while(removedIt.hasNext())
                {
                    AccountIdentification ai = (AccountIdentification) removedIt.next();
                    List<AccountIdentification> list = map.get(Integer.valueOf(ai.getIdGroup()));
                    if (list.get(0).getIdType()==AccountIdentification.DEFAULT_IDTYPE)
                    {
                        list.remove(0);
                        list.add(ai);
                    }
                    else
                    {
                        addIdentificationToNextAvailableGroup(ctx, identificationGroups, map, idList, ai);
                    }
                }


                setAccountIdentificationLoaded(true);
                this.identificationGroupList_ = idList;
			}
		}
		catch (HomeException e)
		{
			LogSupport.minor(getContext(), this,
			    "Unable to load account identification Info", e);
		}
	}

    /**
     * Adds the retrieved identification to the next available group (that means, not full group which accepts the identification).
     * If no group is found, the identification is added to the default group.
     * @param ctx
     * @param idGroups
     * @param account
     * @param map
     * @param ai
     */
    private static void addIdentificationToNextAvailableGroup(Context ctx, SpidIdentificationGroups idGroups, Map<Integer, List<AccountIdentification>> map, List<AccountIdentificationGroup> identificationsList, AccountIdentification ai)
    {
        boolean added = false;
        
        if (idGroups != null)
        {
            Iterator<IdentificationGroup> iter = (Iterator<IdentificationGroup>) idGroups.getGroups().iterator();
           
            while (iter.hasNext())
            {
                IdentificationGroup group = iter.next();
                List<AccountIdentification> list = map.get(Integer.valueOf(group.getIdGroup()));
                if (group.getIdGroup()==ai.getIdGroup())
                {
                    continue;
                }
                else if (group.getIdentificationList().contains(String.valueOf(ai.getIdType()))
                        && list.get(0).getIdType()==AccountIdentification.DEFAULT_IDTYPE)
                {
                    list.remove(0);
                    ai.setIdGroup(group.getIdGroup());
                    list.add(ai);
                    added = true;
                    break;
                }
            }
        }
        if (!added)
        {
            addIdentificationToDefaultGroup(ctx, map, identificationsList, ai);
        }
    }

    /**
     * Adds identification to the default account identification group. This group will be created if it does not exist.
     * @param ctx
     * @param account
     * @param map
     * @param ai
     */
    private static void addIdentificationToDefaultGroup(Context ctx, Map<Integer, List<AccountIdentification>> map, List<AccountIdentificationGroup> identificationsList, AccountIdentification ai)
    {
        List<AccountIdentification> list = map.get(Integer.valueOf(AccountIdentification.DEFAULT_IDGROUP));
        if (list==null)
        {
            list = AccountIdentificationSupport.addDefaultAccountIdentificationGroupToList(ctx, identificationsList).getIdentificationList();
            map.put(Integer.valueOf(AccountIdentification.DEFAULT_IDGROUP), list);
        }
        ai.setIdGroup(-1);
        list.add(ai);
        
    }

    /**
     * Verifies whether or not identification is accepted in identification group.
     * @param idGroups
     * @param idGroup
     * @param idCode
     * @return
     */
    private static boolean isIdentificationInGroup(SpidIdentificationGroups idGroups, int idGroup, int idCode)
    {
        boolean result = false;
        if (idGroups!=null)
        {
            Iterator<IdentificationGroup> iter = (Iterator<IdentificationGroup>) idGroups.getGroups().iterator();
            while (iter.hasNext())
            {
                IdentificationGroup group = iter.next();
                if (group.getIdGroup()==idGroup)
                {
                    if (group.getIdentificationList().contains(String.valueOf(idCode)))
                    {
                        result = true;
                    }
                    break;
                }
            }
        }
        return result;
    }

    protected synchronized void lazyLoadSecurityQuestionAnswerInfo()
	{
		final Context ctx = getContext();
		if (AbstractAccount.DEFAULT_BAN.equals(this.getBAN()))
		{
			if (LogSupport.isDebugEnabled(ctx))
			{
				LogSupport
				    .debug(ctx, this,
				        "SecurityQuestionAndAnswer info NOT loaded because BAN is not set.");
			}
			setSecurityQuestionAndAnswerLoaded(true);
			return;
		}
		final Home home = (Home) ctx.get(SecurityQuestionAnswerHome.class);
		if (home == null)
		{
			// SecurityQuestionAndAnswer home is not yet available
			LogSupport
			    .minor(
			        ctx,
			        this,
			        "SecurityQuestionAndAnswer Home is not available yet. SecurityQuestionAndAnswer info NOT loaded.");
			setSecurityQuestionAndAnswerLoaded(true);
			return;
		}
		try
		{
			final Collection questions =
			    home.select(ctx,
			        new EQ(SecurityQuestionAnswerXInfo.BAN, this.getBAN()));
			this.securityQuestionsAndAnswers_ = (List) questions;
			setSecurityQuestionAndAnswerLoaded(true);
		}
		catch (HomeException e)
		{
			LogSupport.minor(ctx, this,
			    "Unable to load SecurityQuestionAndAnswer Info", e);
		}
	}

	public void setPersonFields(final Contact contact)
	{
		this.initials_ = contact.getInitials();
		// this field is still in Account table
		// this.billingAddress1_ = contact.getAddressLineOne();
		this.billingAddress2_ = contact.getAddressLineTwo();
		this.billingAddress3_ = contact.getAddressLineThree();
		this.billingCity_ = contact.getCity();
		Province province = null;
        
		if(getContext() != null)
		{
			try
			{
				String contactProvince = contact.getProvince();
				
				//Blank Display name is allowed for contact.
				if(contactProvince != null)
				{
					final And filter = new And();
					filter.add(new EQ(ProvinceXInfo.SPID, getSpid()));
					filter.add(new EQ(ProvinceXInfo.DISPLAY_NAME, contactProvince ));

					province = HomeSupportHelper.get(getContext()).findBean(getContext(), Province.class, filter);
				}
			}
			catch (final Exception exception)
			{
				if (LogSupport.isDebugEnabled(getContext()))
				{
					final StringBuilder sb = new StringBuilder();
					sb.append(exception.getClass().getSimpleName());
					sb.append(" caught in ");
					sb.append(getClass().getSimpleName());
					sb.append(".setPersonFields: ");
					if (exception.getMessage() != null)
					{
						sb.append(exception.getMessage());
					}
					LogSupport.debug(getContext(), this, sb.toString(), exception);
				}
			}

			if(province != null )
			{
				this.billingProvince_ = province.getName();
			}
		}
		this.billingPostalCode_ = contact.getPostalCode();
		this.billingCountry_ = contact.getCountry();
		this.contactTel_ = contact.getPhone();
		this.contactFax_ = contact.getFax();
		this.emailID_ = contact.getEmail();
		this.secondaryEmailAddresses_ = contact.getSecondaryEmailAddresses();
		this.employer_ = contact.getEmployer();
		this.employerAddress_ = contact.getEmployerAddress();
		this.dateOfBirth_ = contact.getDateOfBirth();
		this.occupation_ = contact.getOccupation();
		this.csa_ = contact.getCsa();
	}

	@Override
	public PropertyInfo getExtensionHolderProperty()
	{
		return AccountXInfo.ACCOUNT_EXTENSIONS;
	}

	@Override
	public Collection<Extension> getExtensions()
	{
		Collection<ExtensionHolder> holders =
		    (Collection<ExtensionHolder>) getExtensionHolderProperty()
		        .get(this);
		return ExtensionSupportHelper.get(getContext()).unwrapExtensions(
		    holders);
	}
	
    public Collection<Class> getExtensionTypes()
    {
        final Context ctx = ContextLocator.locate();
        return AccountSupport.getExtensionTypes(ctx, this);
    }


	/**
	 * Lazy loading extensions. {@inheritDoc}
	 */
	@Override
	public List getAccountExtensions()
	{
		synchronized (this)
		{
			if (super.getAccountExtensions() == null)
			{
				final Context ctx = getContext();
				try
				{
					// To avoid deadlock, use an account
					// "with extensions loaded" along with extension loading
					// adapter.
					Account accountCopy = (Account) this.clone();
					accountCopy.setAccountExtensions(new ArrayList());

					accountCopy =
					    (Account) new ExtensionLoadingAdapter<AccountExtension>(
					        AccountExtension.class, AccountExtensionXInfo.BAN)
					        .adapt(ctx, accountCopy);
					accountCopy =
					    (Account) new ExtensionSpidAdapter().adapt(ctx,
					        accountCopy);

					this.setAccountExtensions(accountCopy
					    .getAccountExtensions());
				}
				catch (Exception e)
				{
					LogSupport
					    .minor(ctx, this,
					        "Exception occurred loading extensions. Extensions NOT loaded.");
					LogSupport
					    .debug(
					        ctx,
					        this,
					        "Exception occurred loading extensions. Extensions NOT loaded.",
					        e);
				}
			}
		}

		return super.getAccountExtensions();
	}

	/**
	 * Lazy loading contact fields.
	 * 
	 * @return initials
	 */
	@Override
	public String getInitials()
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		return super.getInitials();
	}

	/**
	 * Lazy loading contact fields since modification can trigger a save, so we
	 * need to prevent stale writes.
	 * 
	 * @param initials
	 *            new value
	 * @throws IllegalArgumentException
	 */
	@Override
	public void setInitials(final String initials)
	    throws IllegalArgumentException
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		super.setInitials(initials);
	}

	/**
	 * Lazy loading contact fields.
	 * 
	 * @return billing Address line 1
	 */
	@Override
	public String getBillingAddress1()
	{
		// this field is not loaded because it is still in the Account tabel and
		// is used in the Table view
		// so for optimization purposes....
		return super.getBillingAddress1();
	}

	/**
	 * Lazy loading contact fields since modification can trigger a save, so we
	 * need to prevent stale writes.
	 * 
	 * @param billingAddress1
	 *            new value
	 * @throws IllegalArgumentException
	 */
	@Override
	public void setBillingAddress1(final String billingAddress1)
	    throws IllegalArgumentException
	{
		// this field is not loaded because it is still in the Account tabel and
		// is used in the Table view
		// so for optimization purposes....
		super.setBillingAddress1(billingAddress1);
	}

	/**
	 * Lazy loading contact fields.
	 * 
	 * @return billing Address line 2
	 */
	@Override
	public String getBillingAddress2()
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		return super.getBillingAddress2();
	}

	/**
	 * Lazy loading contact fields since modification can trigger a save, so we
	 * need to prevent stale writes.
	 * 
	 * @param billingAddress2
	 *            new value
	 * @throws IllegalArgumentException
	 */
	@Override
	public void setBillingAddress2(final String billingAddress2)
	    throws IllegalArgumentException
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		super.setBillingAddress2(billingAddress2);
	}

	/**
	 * Lazy loading contact fields.
	 * 
	 * @return billing Address line 3
	 */
	@Override
	public String getBillingAddress3()
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		return super.getBillingAddress3();
	}

	/**
	 * Lazy loading contact fields since modification can trigger a save, so we
	 * need to prevent stale writes.
	 * 
	 * @param billingAddress3
	 *            new value
	 * @throws IllegalArgumentException
	 */
	@Override
	public void setBillingAddress3(final String billingAddress3)
	    throws IllegalArgumentException
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		super.setBillingAddress3(billingAddress3);
	}

	/**
	 * Lazy loading contact fields.
	 * 
	 * @return billing City
	 */
	@Override
	public String getBillingCity()
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		return super.getBillingCity();
	}

	/**
	 * Lazy loading contact fields since modification can trigger a save, so we
	 * need to prevent stale writes.
	 * 
	 * @param billingCity
	 *            new value
	 * @throws IllegalArgumentException
	 */
	@Override
	public void setBillingCity(final String billingCity)
	    throws IllegalArgumentException
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		super.setBillingCity(billingCity);
	}

	/**
	 * Lazy loading contact fields.
	 * 
	 * @return billing Province
	 */
	@Override
	public String getBillingProvince()
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		return super.getBillingProvince();
	}

	/**
	 * Lazy loading contact fields since modification can trigger a save, so we
	 * need to prevent stale writes.
	 * 
	 * @param billingProvince
	 *            new value
	 * @throws IllegalArgumentException
	 */
	@Override
	public void setBillingProvince(final String billingProvince)
	    throws IllegalArgumentException
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}
		
		super.setBillingProvince(billingProvince);
	}

	/**
	 * Lazy loading contact fields.
	 * 
	 * @return billing Postal Code
	 */
	@Override
	public String getBillingPostalCode()
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		return super.getBillingPostalCode();
	}

	/**
	 * Lazy loading contact fields since modification can trigger a save, so we
	 * need to prevent stale writes.
	 * 
	 * @param billingPostalCode
	 *            new value
	 * @throws IllegalArgumentException
	 */
	@Override
	public void setBillingPostalCode(final String billingPostalCode)
	    throws IllegalArgumentException
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		super.setBillingPostalCode(billingPostalCode);
	}

	/**
	 * Lazy loading contact fields.
	 * 
	 * @return billing Country
	 */
	@Override
	public String getBillingCountry()
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		return super.getBillingCountry();
	}

	/**
	 * Lazy loading contact fields since modification can trigger a save, so we
	 * need to prevent stale writes.
	 * 
	 * @param billingCountry
	 *            new value
	 * @throws IllegalArgumentException
	 */
	@Override
	public void setBillingCountry(final String billingCountry)
	    throws IllegalArgumentException
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		super.setBillingCountry(billingCountry);
	}

	/**
	 * Lazy loading contact fields.
	 * 
	 * @return contact Tel
	 */
	@Override
	public String getContactTel()
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		return super.getContactTel();
	}

	/**
	 * Lazy loading contact fields since modification can trigger a save, so we
	 * need to prevent stale writes.
	 * 
	 * @param contactTel
	 *            new value
	 * @throws IllegalArgumentException
	 */
	@Override
	public void setContactTel(final String contactTel)
	    throws IllegalArgumentException
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		super.setContactTel(contactTel);
	}

	/**
	 * Lazy loading contact fields.
	 * 
	 * @return contact Fax
	 */
	@Override
	public String getContactFax()
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		return super.getContactFax();
	}

	/**
	 * Lazy loading contact fields since modification can trigger a save, so we
	 * need to prevent stale writes.
	 * 
	 * @param contactFax
	 *            new value
	 * @throws IllegalArgumentException
	 */
	@Override
	public void setContactFax(final String contactFax)
	    throws IllegalArgumentException
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		super.setContactFax(contactFax);
	}

	/**
	 * Lazy loading contact fields.
	 * 
	 * @return email
	 */
	@Override
	public String getEmailID()
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		return super.getEmailID();
	}

	/**
	 * Lazy loading contact fields since modification can trigger a save, so we
	 * need to prevent stale writes.
	 * 
	 * @param emailID
	 *            new value
	 * @throws IllegalArgumentException
	 */
	@Override
	public void setEmailID(final String emailID)
	    throws IllegalArgumentException
	{
		assertEmailId(emailID);
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}
		super.setEmailID(emailID);
	}
	
	/**
	 * Lazy loading contact fields.
	 * 
	 * @return secondaryEmailAddresses
	 */
	@Override
	public String getSecondaryEmailAddresses()
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		return super.getSecondaryEmailAddresses();
	}
	
	/**
	 * Lazy loading contact field CSA.
	 * 
	 * @return csa
	 */
	@Override
	public String getCsa()
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		return super.getCsa();
	}

	/**
	 * Lazy loading contact fields since modification can trigger a save, so we
	 * need to prevent stale writes.
	 * 
	 * @param secondaryemailAddresses
	 *            new value
	 * @throws IllegalArgumentException
	 */
	@Override
	public void setSecondaryEmailAddresses(final String secondaryEmailAddresses)
	    throws IllegalArgumentException
	{
		
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}
		super.setSecondaryEmailAddresses(secondaryEmailAddresses);
	}


	/**
	 * Lazy loading contact fields since modification can trigger a save, so we
	 * need to prevent stale writes.
	 * 
	 * @param csa
	 *            new value
	 * @throws IllegalArgumentException
	 */
	@Override
	public void setCsa(final String csa)
	    throws IllegalArgumentException
	{
		
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}
		super.setCsa(csa);
	}

	/**
	 * Lazy loading contact fields.
	 * 
	 * @return Employer
	 */
	@Override
	public String getEmployer()
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		return super.getEmployer();
	}

	/**
	 * Lazy loading contact fields since modification can trigger a save, so we
	 * need to prevent stale writes.
	 * 
	 * @param employer
	 *            new value
	 * @throws IllegalArgumentException
	 */
	@Override
	public void setEmployer(final String employer)
	    throws IllegalArgumentException
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		super.setEmployer(employer);
	}

	/**
	 * Lazy loading contact fields.
	 * 
	 * @return Employer Address
	 */
	@Override
	public String getEmployerAddress()
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		return super.getEmployerAddress();
	}

	/**
	 * Lazy loading contact fields since modification can trigger a save, so we
	 * need to prevent stale writes.
	 * 
	 * @param employerAddress
	 *            new value
	 * @throws IllegalArgumentException
	 */
	@Override
	public void setEmployerAddress(final String employerAddress)
	    throws IllegalArgumentException
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		super.setEmployerAddress(employerAddress);
	}

	/**
	 * Lazy loading contact fields.
	 * 
	 * @return Date Of Birth
	 */
	@Override
	public Date getDateOfBirth()
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		return super.getDateOfBirth();
	}

	/**
	 * Lazy loading contact fields since modification can trigger a save, so we
	 * need to prevent stale writes.
	 * 
	 * @param dateOfBirth
	 *            new value
	 * @throws IllegalArgumentException
	 */
	@Override
	public void setDateOfBirth(final Date dateOfBirth)
	    throws IllegalArgumentException
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		super.setDateOfBirth(dateOfBirth);
	}

	/**
	 * Lazy loading contact fields.
	 * 
	 * @return Occupation
	 */
	@Override
	public int getOccupation()
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		return super.getOccupation();
	}

	/**
	 * Lazy loading contact fields since modification can trigger a save, so we
	 * need to prevent stale writes.
	 * 
	 * @param occupation
	 *            new value
	 * @throws IllegalArgumentException
	 */
	@Override
	public void setOccupation(final int occupation)
	    throws IllegalArgumentException
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		super.setOccupation(occupation);
	}

	public void setCompanyFields(final Contact contact)
	{
		// this field is still in Account table
		// this.companyName_ = contact.getCompanyName();
		this.tradingName_ = contact.getTradingName();
		this.registrationNumber_ = contact.getRegistrationNumber();
		this.companyTel_ = contact.getPhone();
		this.companyFax_ = contact.getFax();
		this.companyAddress1_ = contact.getAddressLineOne();
		this.companyAddress2_ = contact.getAddressLineTwo();
		this.companyAddress3_ = contact.getAddressLineThree();
		this.companyCity_ = contact.getCity();
		      
		Province province = null;
        if(getContext() != null)
		{
			try
			{
				String contactProvince = contact.getProvince();
				
				//Blank Display name is allowed for contact.
				if(contactProvince != null)
				{
					final And filter = new And();
					filter.add(new EQ(ProvinceXInfo.SPID, getSpid()));
					filter.add(new EQ(ProvinceXInfo.DISPLAY_NAME, contactProvince ));

					province = HomeSupportHelper.get(getContext()).findBean(getContext(), Province.class, filter);
				}
			}
			catch (final Exception exception)
			{
				if (LogSupport.isDebugEnabled(getContext()))
				{
					final StringBuilder sb = new StringBuilder();
					sb.append(exception.getClass().getSimpleName());
					sb.append(" caught in ");
					sb.append(getClass().getSimpleName());
					sb.append(".setCompanyFields: ");
					if (exception.getMessage() != null)
					{
						sb.append(exception.getMessage());
					}
					LogSupport.debug(getContext(), this, sb.toString(), exception);
				}
			}

			if(province != null )
			{
				this.companyProvince_ = province.getName();
			}
		}
        
		
		this.companyPostalCode_ = contact.getPostalCode();
		this.companyCountry_ = contact.getCountry();
	}

	/**
	 * Lazy loading contact fields.
	 * 
	 * @return Company Name
	 */
	@Override
	public String getCompanyName()
	{
		// this field is not loaded because it is still in the Account becuse it
		// is used in an index
		return super.getCompanyName();
	}

	/**
	 * Lazy loading contact fields since modification can trigger a save, so we
	 * need to prevent stale writes.
	 * 
	 * @param companyName
	 *            new value
	 * @throws IllegalArgumentException
	 */
	@Override
	public void setCompanyName(final String companyName)
	    throws IllegalArgumentException
	{
		// this field is not loaded because it is still in the Account becuse it
		// is used in an index
		super.setCompanyName(companyName);
	}

	/**
	 * Lazy loading contact fields.
	 * 
	 * @return Trading Name
	 */
	@Override
	public String getTradingName()
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		return super.getTradingName();
	}

	/**
	 * Lazy loading contact fields since modification can trigger a save, so we
	 * need to prevent stale writes.
	 * 
	 * @param tradingName
	 *            new value
	 * @throws IllegalArgumentException
	 */
	@Override
	public void setTradingName(final String tradingName)
	    throws IllegalArgumentException
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		super.setTradingName(tradingName);
	}

	/**
	 * Lazy loading contact fields.
	 * 
	 * @return Registration Number
	 */
	@Override
	public String getRegistrationNumber()
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		return super.getRegistrationNumber();
	}

	/**
	 * Lazy loading contact fields since modification can trigger a save, so we
	 * need to prevent stale writes.
	 * 
	 * @param registrationNumber
	 *            new value
	 * @throws IllegalArgumentException
	 */
	@Override
	public void setRegistrationNumber(final String registrationNumber)
	    throws IllegalArgumentException
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		super.setRegistrationNumber(registrationNumber);
	}

	/**
	 * Lazy loading contact fields.
	 * 
	 * @return Company Tel
	 */
	@Override
	public String getCompanyTel()
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		return super.getCompanyTel();
	}

	/**
	 * Lazy loading contact fields since modification can trigger a save, so we
	 * need to prevent stale writes.
	 * 
	 * @param companyTel
	 *            new value
	 * @throws IllegalArgumentException
	 */
	@Override
	public void setCompanyTel(final String companyTel)
	    throws IllegalArgumentException
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		super.setCompanyTel(companyTel);
	}

	/**
	 * Lazy loading contact fields.
	 * 
	 * @return Company Fax
	 */
	@Override
	public String getCompanyFax()
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		return super.getCompanyFax();
	}

	/**
	 * Lazy loading contact fields since modification can trigger a save, so we
	 * need to prevent stale writes.
	 * 
	 * @param companyFax
	 *            new value
	 * @throws IllegalArgumentException
	 */
	@Override
	public void setCompanyFax(final String companyFax)
	    throws IllegalArgumentException
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		super.setCompanyFax(companyFax);
	}

	/**
	 * Lazy loading contact fields.
	 * 
	 * @return Company Address line one
	 */
	@Override
	public String getCompanyAddress1()
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		return super.getCompanyAddress1();
	}

	/**
	 * Lazy loading contact fields since modification can trigger a save, so we
	 * need to prevent stale writes.
	 * 
	 * @param companyAddress1
	 *            new value
	 * @throws IllegalArgumentException
	 */
	@Override
	public void setCompanyAddress1(final String companyAddress1)
	    throws IllegalArgumentException
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		super.setCompanyAddress1(companyAddress1);
	}

	/**
	 * Lazy loading contact fields.
	 * 
	 * @return Company Address line two
	 */
	@Override
	public String getCompanyAddress2()
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		return super.getCompanyAddress2();
	}

	/**
	 * Lazy loading contact fields since modification can trigger a save, so we
	 * need to prevent stale writes.
	 * 
	 * @param companyAddress2
	 *            new value
	 * @throws IllegalArgumentException
	 */
	@Override
	public void setCompanyAddress2(final String companyAddress2)
	    throws IllegalArgumentException
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		super.setCompanyAddress2(companyAddress2);
	}

	/**
	 * Lazy loading contact fields.
	 * 
	 * @return Company Address line three
	 */
	@Override
	public String getCompanyAddress3()
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		return super.getCompanyAddress3();
	}

	/**
	 * Lazy loading contact fields since modification can trigger a save, so we
	 * need to prevent stale writes.
	 * 
	 * @param companyAddress3
	 *            new value
	 * @throws IllegalArgumentException
	 */
	@Override
	public void setCompanyAddress3(final String companyAddress3)
	    throws IllegalArgumentException
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		super.setCompanyAddress3(companyAddress3);
	}

	/**
	 * Lazy loading contact fields.
	 * 
	 * @return Company City
	 */
	@Override
	public String getCompanyCity()
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		return super.getCompanyCity();
	}

	/**
	 * Lazy loading contact fields since modification can trigger a save, so we
	 * need to prevent stale writes.
	 * 
	 * @param companyCity
	 *            new value
	 * @throws IllegalArgumentException
	 */
	@Override
	public void setCompanyCity(final String companyCity)
	    throws IllegalArgumentException
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		super.setCompanyCity(companyCity);
	}

	/**
	 * Lazy loading contact fields.
	 * 
	 * @return Company Province
	 */
	@Override
	public String getCompanyProvince()
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		return super.getCompanyProvince();
	}

	/**
	 * Lazy loading contact fields since modification can trigger a save, so we
	 * need to prevent stale writes.
	 * 
	 * @param companyProvince
	 *            new value
	 * @throws IllegalArgumentException
	 */
	@Override
	public void setCompanyProvince(final String companyProvince)
	    throws IllegalArgumentException
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}
    	
		super.setCompanyProvince(companyProvince);
		
	}

	/**
	 * Lazy loading contact fields.
	 * 
	 * @return Company Postal Code
	 */
	@Override
	public String getCompanyPostalCode()
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		return super.getCompanyPostalCode();
	}

	/**
	 * Lazy loading contact fields since modification can trigger a save, so we
	 * need to prevent stale writes.
	 * 
	 * @param companyPostalCode
	 *            new value
	 * @throws IllegalArgumentException
	 */
	@Override
	public void setCompanyPostalCode(final String companyPostalCode)
	    throws IllegalArgumentException
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		super.setCompanyPostalCode(companyPostalCode);
	}

	/**
	 * Lazy loading contact fields.
	 * 
	 * @return Company Country
	 */
	@Override
	public String getCompanyCountry()
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		return super.getCompanyCountry();
	}

	/**
	 * Lazy loading contact fields since modification can trigger a save, so we
	 * need to prevent stale writes.
	 * 
	 * @param companyCountry
	 *            new value
	 * @throws IllegalArgumentException
	 */
	@Override
	public void setCompanyCountry(final String companyCountry)
	    throws IllegalArgumentException
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		super.setCompanyCountry(companyCountry);
	}

	public void setBankFields(final Contact contact)
	{
		this.bankName_ = contact.getBankName();
		this.bankPhone_ = contact.getPhone();
		this.bankAddress1_ = contact.getAddressLineOne();
		this.bankAddress2_ = contact.getAddressLineTwo();
		this.bankAccountNumber_ = contact.getBankAccountNumber();
		this.bankAccountName_ = contact.getBankAccountName();
	}

	/**
	 * Lazy loading contact fields.
	 * 
	 * @return Bank Name
	 */
	@Override
	public String getBankName()
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		return super.getBankName();
	}

	/**
	 * Lazy loading contact fields since modification can trigger a save, so we
	 * need to prevent stale writes.
	 * 
	 * @param bankName
	 *            new value
	 * @throws IllegalArgumentException
	 */
	@Override
	public void setBankName(final String bankName)
	    throws IllegalArgumentException
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		super.setBankName(bankName);
	}

	/**
	 * Lazy loading contact fields.
	 * 
	 * @return Bank Phone
	 */
	@Override
	public String getBankPhone()
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		return super.getBankPhone();
	}

	/**
	 * Lazy loading contact fields since modification can trigger a save, so we
	 * need to prevent stale writes.
	 * 
	 * @param bankPhone
	 *            new value
	 * @throws IllegalArgumentException
	 */
	@Override
	public void setBankPhone(final String bankPhone)
	    throws IllegalArgumentException
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		super.setBankPhone(bankPhone);
	}

	/**
	 * Lazy loading contact fields.
	 * 
	 * @return Bank Address line one
	 */
	@Override
	public String getBankAddress1()
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		return super.getBankAddress1();
	}

	/**
	 * Lazy loading contact fields since modification can trigger a save, so we
	 * need to prevent stale writes.
	 * 
	 * @param bankAddress1
	 *            new value
	 * @throws IllegalArgumentException
	 */
	@Override
	public void setBankAddress1(final String bankAddress1)
	    throws IllegalArgumentException
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		super.setBankAddress1(bankAddress1);
	}

	/**
	 * Lazy loading contact fields.
	 * 
	 * @return Bank Address line two
	 */
	@Override
	public String getBankAddress2()
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		return super.getBankAddress2();
	}

	/**
	 * Lazy loading contact fields since modification can trigger a save, so we
	 * need to prevent stale writes.
	 * 
	 * @param bankAddress2
	 *            new value
	 * @throws IllegalArgumentException
	 */
	@Override
	public void setBankAddress2(final String bankAddress2)
	    throws IllegalArgumentException
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		super.setBankAddress2(bankAddress2);
	}

	/**
	 * Lazy loading contact fields.
	 * 
	 * @return Bank Account Number
	 */
	@Override
	public String getBankAccountNumber()
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		return super.getBankAccountNumber();
	}

	/**
	 * Lazy loading contact fields since modification can trigger a save, so we
	 * need to prevent stale writes.
	 * 
	 * @param bankAccountNumber
	 *            new value
	 * @throws IllegalArgumentException
	 */
	@Override
	public void setBankAccountNumber(final String bankAccountNumber)
	    throws IllegalArgumentException
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		super.setBankAccountNumber(bankAccountNumber);
	}

	/**
	 * Lazy loading contact fields.
	 * 
	 * @return Bank Account Name
	 */
	@Override
	public String getBankAccountName()
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		return super.getBankAccountName();
	}

	/**
	 * Lazy loading contact fields since modification can trigger a save, so we
	 * need to prevent stale writes.
	 * 
	 * @param bankAccountName
	 *            new value
	 * @throws IllegalArgumentException
	 */
	@Override
	public void setBankAccountName(final String bankAccountName)
	    throws IllegalArgumentException
	{
		synchronized (this)
		{
			if (!getContactsLoaded())
			{
				lazyLoadContactInfo();
			}
		}

		super.setBankAccountName(bankAccountName);
	}

	private static PropertyInfo[][] contactProperties_ =
	{
	    {
	        // AccountXInfo.FIRST_NAME,
	        // AccountXInfo.LAST_NAME,
	        AccountXInfo.INITIALS,
	        // AccountXInfo.BILLING_ADDRESS1,
	        AccountXInfo.BILLING_ADDRESS2, AccountXInfo.BILLING_ADDRESS3,
	        AccountXInfo.BILLING_CITY, AccountXInfo.BILLING_PROVINCE,
	        AccountXInfo.BILLING_POSTAL_CODE,
	        AccountXInfo.BILLING_COUNTRY, AccountXInfo.CONTACT_TEL,
	        AccountXInfo.CONTACT_FAX, AccountXInfo.EMAIL_ID,AccountXInfo.SECONDARY_EMAIL_ADDRESSES,
	        AccountXInfo.EMPLOYER, AccountXInfo.EMPLOYER_ADDRESS,
	        AccountXInfo.DATE_OF_BIRTH, AccountXInfo.OCCUPATION,
	    },
	    {
	        // AccountXInfo.COMPANY_NAME,
	        AccountXInfo.TRADING_NAME, AccountXInfo.REGISTRATION_NUMBER,
	        AccountXInfo.COMPANY_TEL, AccountXInfo.COMPANY_FAX,
	        AccountXInfo.COMPANY_ADDRESS1, AccountXInfo.COMPANY_ADDRESS2,
	        AccountXInfo.COMPANY_ADDRESS3, AccountXInfo.COMPANY_CITY,
	        AccountXInfo.COMPANY_PROVINCE, AccountXInfo.COMPANY_POSTAL_CODE,
	        AccountXInfo.COMPANY_COUNTRY,
	    },
	    {
	        AccountXInfo.BANK_NAME, AccountXInfo.BANK_PHONE,
	        AccountXInfo.BANK_ADDRESS1, AccountXInfo.BANK_ADDRESS2,
	        AccountXInfo.BANK_ACCOUNT_NUMBER, AccountXInfo.BANK_ACCOUNT_NAME,
	    },
	};

	private static Set<PropertyInfo> lazyLoadedProperties_;

	private PropertyChangeListener accountPropertyWatch_ = null;

	/**
	 * Adds PropertyChangeListener so the property changes will be watch for
	 * further
	 * changes.
	 */
	public void watchLazyLoadedProperitesChange()
	{
		if (accountPropertyWatch_ == null)
		{
			accountPropertyWatch_ = new AccountPropertyListeners();
		}
		this.addPropertyChangeListener(accountPropertyWatch_);
	}

	/**
	 * Adds PropertyChangeListener so the property changes will be watch for
	 * further
	 * changes.
	 */
	public PropertyChangeListener getAccountLazyLoadedPropertyListener()
	{
		if (accountPropertyWatch_ == null)
		{
			accountPropertyWatch_ = new AccountPropertyListeners();
		}
		return accountPropertyWatch_;
	}

	/**
	 * Removes PropertyChangeListener so the property changes will not be
	 * watched for further changes.
	 */
	public void stopLazyLoadedProperitesChange()
	{
		this.removePropertyChangeListener(accountPropertyWatch_);
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	public Object deepClone() throws CloneNotSupportedException
	{
		final Account clone = (Account) super.deepClone();
		clone.setDecodedCreditCard( this.getDecodedCreditCard());
		return cloneLazyLoadMetaData(clone);
	}

	/**
	 * Adding cloning functionality to clone added fields.
	 * 
	 * @return the clone object
	 * @throws CloneNotSupportedException
	 *             should not be thrown
	 */
	@Override
	public Object clone() throws CloneNotSupportedException
	{
		Account clone = (Account) super.clone();

		clone = cloneAccountExtensionList(clone);
		clone = cloneAccountDiscountClass(clone);		
		clone = (Account) cloneLazyLoadMetaData(clone);
		clone.resetTransitFields();
		clone.setDecodedCreditCard( this.getDecodedCreditCard());
		clone.setContext(null);
		return clone;
	}

	public void resetTransitFields()
	{
		this.setCreditCardInfo(new CreditCardEntry());
		this.setCreditCardPayment(false);
		setCreditCardInfoLoaded(false);
		setSupplementaryDataLoaded(false);
	}

	private Account cloneAccountExtensionList(final Account clone)
	    throws CloneNotSupportedException
	{
		List accountExtensions = super.getAccountExtensions();
		if (accountExtensions != null)
		{
			final List extentionList = new ArrayList(accountExtensions.size());
			clone.setAccountExtensions(extentionList);
			for (final Iterator it = accountExtensions.iterator(); it.hasNext();)
			{
				extentionList.add(safeClone((XCloneable) it.next()));
			}
		}
		return clone;
	}
/**
 * 
 * @param clone
 * @return
 * @throws CloneNotSupportedException
 */
	private Account cloneAccountDiscountClass(final Account clone)
			throws CloneNotSupportedException {

		final int SERVICE_LEVEL_DISCOUNT = -2; 

		Context ctx = getContext();

		if (clone.getDiscountClass() == SERVICE_LEVEL_DISCOUNT) {

			if (null == clone.getDiscountsClassHolder()
					|| clone.getDiscountsClassHolder().isEmpty()) {

				And and = new And();
				and.add(new EQ(AccountsDiscountXInfo.BAN, clone.getBAN()));
				and.add(new EQ(AccountsDiscountXInfo.SPID, clone.getSpid()));

				try {
					Collection<AccountsDiscount> coll = HomeSupportHelper.get(
							ctx).getBeans(ctx, AccountsDiscount.class, and);
					Set<Integer> discountClassSet = new HashSet<Integer>();
					for (AccountsDiscount acc : coll) {
						discountClassSet.add((int) acc.getDiscountClass());
					}
					if (!discountClassSet.isEmpty()) {
						clone.setDiscountsClassHolder(discountClassSet);
					}
				} catch (HomeException e) {
					new MinorLogMsg(this,
							"Caught execption while retrieving AccountsDiscount on BAN:"
									+ clone.getBAN() + " " + e);
				}
			}
		}

		return clone;
	}

	/**
	 * @param clone
	 * @return
	 */
	private Object cloneLazyLoadMetaData(final Account clone)
	{
		clone.accountPropertyWatch_ = new AccountPropertyListeners();
		if (this.accountPropertyWatch_ != null)
		{
			((AccountPropertyListeners) this.accountPropertyWatch_)
			    .cloneLazyLoadMetaData((AccountPropertyListeners) clone.accountPropertyWatch_);
			clone.watchLazyLoadedProperitesChange();
		}
		return clone;
	}

	public boolean isPostpaid()
	{
		return SubscriberTypeEnum.POSTPAID.equals(getSystemType());
	}

	public boolean isPrepaid()
	{
		return SubscriberTypeEnum.PREPAID.equals(getSystemType());
	}

	public boolean isHybrid()
	{
		return SubscriberTypeEnum.HYBRID.equals(getSystemType());
	}

	private void assertEmailId(final String emailID)
	{
		try
		{
			if (InvoiceDeliveryOptionSupport.isEmail(getContext(),
			    getInvoiceDeliveryOption()) && emailID.trim().length() == 0)
			{
				throw new IllegalPropertyArgumentException("Account.emailId",
				    "Please specify an email Id as email delivery option is selected.");
			}
		}
		catch (HomeException he)
		{
			throw new IllegalPropertyArgumentException("Account.emailId",
			    he.getMessage());
		}
	}

	public int getBillCycleDay(final Context context) throws HomeException
	{
		return getBillCycle(context).getDayOfMonth();
	}

	public CreditCardInfo getCreditCardInfo(Context ctx)
	{
		try
		{
			Home home = (Home) ctx.get(CreditCardInfoHome.class);

			CreditCardInfo info =
			    (CreditCardInfo) home.find(ctx, this.getBAN());

			if (info != null)
			{	
				this.setCreditCardPayment(true);
				return info; 
			}
		}
		catch (Throwable t)
		{

		}

		return new CreditCardInfo();
	}

	@Override
	public CreditCardEntry getCreditCardInfo()
	{

		if (isCreditCardInfoLoaded())
		{
			return super.getCreditCardInfo();
		}

		CreditCardInfo cardEntry = null;

		synchronized (this.creditCardInfo_)
		{
			if (this.getBAN().equals(""))
			{
				// ignore the first call from constructor of generated xbean.
				// creditCardInfoFirstGet_ = false;
				return this.creditCardInfo_;
			}

			cardEntry = getCreditCardInfo(this.getContext());
			if (!this.isFrozen())
			{
				setCreditCardInfoLoaded(true);
				this.setCreditCardInfo(cardEntry);

			}
		}

		return cardEntry;

	}

	public String computeResponsibleBAN(final Context ctx) throws HomeException
	{
		String result = getBAN();

		if (!this.getResponsible())
		{
			// for checking circular references
			final Set<String> allAccountIds = new HashSet<String>();
			allAccountIds.add(this.getBAN());

			Account up = this.getParentAccount(ctx);
			while (up != null && !up.getResponsible())
			{
				// for a non-responsible, parent will never be null
				if (allAccountIds.contains(up.getBAN()))
				{
					throw new HomeException(
					    "Circular reference in account topology, root="
					        + up.getBAN() + ", sub ban=" + this.getBAN());
				}
				allAccountIds.add(up.getBAN());

				up = up.getParentAccount(ctx);
			}

			if (up != null)
			{
				result = up.getBAN();
			}
			else
			{
				throw new HomeException(
				    "Unable to compute Responsible BAN for Account="
				        + this.getBAN());
			}
		}

		return result;
	}

	public QuotaTypeEnum getQuotaType(Context ctx)
	{
	    if (this.isPooled(ctx))
	    {
	        PoolExtension ext = this.getPoolExtension();
	        if (ext != null)
	        {
	            return ext.getQuotaType();
	        }
	    }
	    // Checking if it's not responsible and avoiding loop if it's root account or parent BAN is set to the BAN itself.
	    else if (!this.isResponsible() && !isRootAccount() && !this.getParentBAN().equals(this.getBAN()))
	    {
	        try
	        {
    	        Account parentAccount = this.getParentAccount(ctx);
    	        if (parentAccount != null)
    	        {
    	            return parentAccount.getQuotaType(ctx);
    	        }
            }
            catch (HomeException e)
            {
                LogSupport.minor(ctx,  this, "Unable to retrieve parent account for account " + this.getBAN(), e); 
            }

	    }
		return PoolExtension.DEFAULT_QUOTATYPE;
	}

	public long getQuotaLimit(Context ctx)
	{
        if (this.isPooled(ctx))
        {
    		PoolExtension ext = this.getPoolExtension();
    		if (ext != null)
    		{
    			return ext.getQuotaLimit();
    		}
        }
        else if (!this.isResponsible() && !isRootAccount() && !this.getParentBAN().equals(this.getBAN()))
        {
            try
            {
                Account parentAccount = this.getParentAccount(ctx);
                if (parentAccount != null)
                {
                    return parentAccount.getQuotaLimit(ctx);
                }
            }
            catch (HomeException e)
            {
                LogSupport.minor(ctx,  this, "Unable to retrieve parent account for account " + this.getBAN(), e); 
            }
        }
		return 0;
	}

	public boolean isLoDGenerated()
	{
		Date stateChangeDate = getLastStateChangeDate();
		if (stateChangeDate == null)
		{
			stateChangeDate = getLastModified();
		}
		if (stateChangeDate == null)
		{
			stateChangeDate = new Date();
		}

		Date lodGenerationDate = getLastLoDGenerationDate();
		if (lodGenerationDate == null)
		{
			lodGenerationDate = new Date(0);
		}

		return stateChangeDate.getTime() <= lodGenerationDate.getTime();
	}

    public List<AgedDebt> getAgedDebt(Context ctx, List<AgedDebt> agedDebts)
    {
        List<AgedDebt> result = new ArrayList<AgedDebt>(agedDebts.size());
		// reset accumulated payment to not use the cached value.
		accumulatedPayment_ = AccountSupport.INVALID_VALUE;
        long newPayment = this.getAccumulatedPayment(ctx, null);
        if (newPayment != AccountSupport.INVALID_VALUE && agedDebts!=null)
        {
        	
        	if (agedDebts.iterator().hasNext()) 
        	{
	        	// this is a payment and there is accumulated debt
	            if (newPayment <= 0 && agedDebts.iterator().next().getAccumulatedDebt()>0)
	            {
	                Collections.reverse(agedDebts);
	                
	                boolean first = true;
	                long accumulatedPayment = 0;
	                
	
	                for (AgedDebt agedDebt : agedDebts)
	                {
	                    // Taking care of first aged debt with accumulated value.
	                    if (first)
	                    {
	                        first = false;
	                        if (agedDebt.getDebt() != agedDebt.getAccumulatedDebt())
	                        {
	                            long previousDebt = agedDebt.getAccumulatedDebt() - agedDebt.getDebt();
	                            long agedDebtPayment = Math.max(newPayment, -previousDebt);
	                            accumulatedPayment += agedDebtPayment;
	                            newPayment -= agedDebtPayment;
	                        }
	                    }
	                    
	                    long currentDebt = agedDebt.getDebt();
	                    long currentAccumulatedDebt = agedDebt.getAccumulatedDebt() + accumulatedPayment;
	                    long currentPayment = agedDebt.getInvoicedPayment();
	
	                    if (currentDebt > 0 && newPayment < 0)
	                    {
	                        long agedDebtPayment = Math.max(newPayment, -currentDebt);
	                        currentDebt += agedDebtPayment;
	                        currentPayment += agedDebtPayment;
	                        currentAccumulatedDebt += agedDebtPayment;
	                        accumulatedPayment += agedDebtPayment;
	                        newPayment -= agedDebtPayment;
	                    }
	                    
	                    agedDebt.setCurrentDebt(currentDebt);
	                    agedDebt.setCurrentPayment(currentPayment);
	                    agedDebt.setCurrentAccumulatedDebt(currentAccumulatedDebt);
	                    result.add(agedDebt);
	                }
	                
	                Collections.reverse(agedDebts);
	                Collections.reverse(result);
	            }            
	
	            // this is somehow a debt
	            else if (newPayment > 0)
	            {
	                long accumulatedDebt = newPayment;
	                
	                for (AgedDebt agedDebt : agedDebts)
	                {
	                    boolean first = true;
	                    
	                    if (first)
	                    {
	                        first = false;
	                        // If there is an overpayment
	                        if (agedDebt.getAccumulatedTotalAmount()<0)
	                        {
	                            newPayment = newPayment + agedDebt.getAccumulatedTotalAmount();
	                            newPayment = Math.max(newPayment, 0);
	                            accumulatedDebt = newPayment;
	                        }
	                    }
	
	                    long currentDebt = agedDebt.getDebt();
	                    long currentPayment = agedDebt.getInvoicedPayment();
	                    long currentAccumulatedDebt = agedDebt.getAccumulatedDebt() + accumulatedDebt;
	                    if (currentPayment < 0 && newPayment > 0)
	                    {
	                        long unpaying = Math.min(newPayment, -currentPayment);
	                        currentDebt += unpaying;
	                        currentPayment += unpaying;
	                        accumulatedDebt -= unpaying;
	                        newPayment -= unpaying;
	                    }
	                    
	                    agedDebt.setCurrentDebt(currentDebt);
	                    agedDebt.setCurrentPayment(currentPayment);
	                    agedDebt.setCurrentAccumulatedDebt(currentAccumulatedDebt);
	                    
	                    result.add(agedDebt);
	                }
	            }
        	}
            else
            {
                result.addAll(agedDebts);
            }
        }
        return result;
    }
    
    public List<AgedDebt> getAgedDebt(Context ctx)
	{
        List<AgedDebt> agedDebts = getInvoicedAgedDebt(ctx);
        return getAgedDebt(ctx, agedDebts);

	}

	public List<AgedDebt> getInvoicedAgedDebt(Context ctx)
	{
        List<AgedDebt> result = new ArrayList<AgedDebt>();
        try
        {
            result.addAll(HomeSupportHelper.get(ctx).getBeans(ctx, AgedDebt.class,
                    new EQ(
                            AgedDebtXInfo.BAN, getBAN()), false, AgedDebtXInfo.DEBT_DATE));
        }
        catch (HomeException e)
        {
            LogSupport
                .info(ctx, this,
                    "Cannot retrieve invoiced aged debt for account "
                        + getBAN(), e);
        }
        return result;

	}

    public List<AgedDebt> getInvoicedAgedDebt(Context ctx, Date maxDueDate, boolean stopOnNonAccumulatedDebt)
    {
        List<AgedDebt> result = new ArrayList<AgedDebt>();
        try
        {
            And predicate = new And();
            predicate.add(new EQ(AgedDebtXInfo.BAN, getBAN()));
            predicate.add(new GTE(AgedDebtXInfo.DUE_DATE, maxDueDate));
            if (stopOnNonAccumulatedDebt)
            {
                predicate.add(new GT(AgedDebtXInfo.ACCUMULATED_DEBT, Long.valueOf(0)));
            }

            result.addAll(HomeSupportHelper.get(ctx).getBeans(ctx, AgedDebt.class,
                    predicate, false, AgedDebtXInfo.DEBT_DATE));
        }
        catch (HomeException e)
        {
            LogSupport
                .info(ctx, this,
                    "Cannot retrieve invoiced aged debt for account "
                        + getBAN(), e);
        }
        return result;
    }

    public AgedDebt getLastInvoicedAgedDebt(Context ctx)
    {
        AgedDebt result = null;
        try
        {
            Collection<AgedDebt> agedDebts = HomeSupportHelper.get(ctx).getBeans(ctx, AgedDebt.class,
                    new EQ(AgedDebtXInfo.BAN, getBAN()), 1, false, AgedDebtXInfo.DEBT_DATE);
            if (agedDebts!=null && agedDebts.size()>0)
            {
                result = agedDebts.iterator().next();
            }
        }
        catch (HomeException e)
        {
            LogSupport
                .info(ctx, this,
                    "Cannot retrieve last invoiced aged debt for account "
                        + getBAN(), e);
        }
        return result;
    }

	public void unsetLazyLoadCollections()
	{
        this.supplementaryDataLoaded_ = false;
        this.supplementaryDataList_ = new ArrayList();
		this.accountIdentificationLoaded_ = false;
		this.identificationGroupList_ = new ArrayList();
		this.securityQuestionAndAnswerLoaded_ = false;
		this.securityQuestionsAndAnswers_ = new ArrayList();
	}

    @Deprecated
	private void handleError(Throwable t)
	{
		handleError(getContext(), t);
	}

	private void handleError(Context ctx, Throwable t)
	{
		new MinorLogMsg(this, t.getMessage(), t).log(ctx);
		final ExceptionListener exceptionListner =
		    (ExceptionListener) ctx.get(ExceptionListener.class);
		exceptionListner.thrown(t);
	}

	
	@Override
    synchronized public void encrypt(CrmCipher cipher) throws CrmEncryptingException
	{
		try 
		{
			if(this.getCreditCardNumber()!=null && !this.getCreditCardNumber().isEmpty() 
					&& !this.getCreditCardNumber().startsWith(Encrypted.ENCRYPTED_MASK_PREFIX) )
			{
				this.setEncodedCreditCardNumber(cipher.encode(this.getCreditCardNumber()));
				this.decodedCreditCard = this.getCreditCardNumber(); 
				String tempStr = this.decodedCreditCard.trim();
				if (tempStr.length() < 8)
				{
				    if (LogSupport.isDebugEnabled(getContext()))
				    {
	                    new DebugLogMsg(this, "Decoded credit card number for account " + this.getBAN() + " is too short to partially mask.  Masking entire number...", null).log(getContext());
				    }
	                this.setCreditCardNumber(Encrypted.ENCRYPTED_MASK_PREFIX);
				}
				else
				{
				    tempStr = tempStr.substring(tempStr.length()-4);
	                this.setCreditCardNumber(Encrypted.ENCRYPTED_MASK_PREFIX + tempStr);
				}
			}
		} catch (Exception e)
		{
			CrmEncryptingException ex =  new CrmEncryptingException();
			ex.setStackTrace(e.getStackTrace());
			throw ex; 
		}
			
	}
	
	@Override
    synchronized public void decrypt(CrmCipher cipher) throws CrmEncryptingException
	{
		try 
		{
			if(this.getEncodedCreditCardNumber()!=null && !this.getEncodedCreditCardNumber().isEmpty() )
			{
				this.decodedCreditCard = cipher.decode(this.getEncodedCreditCardNumber());
				this.setCreditCardNumber( Encrypted.ENCRYPTED_MASK_PREFIX + this.decodedCreditCard.trim().substring(
					this.decodedCreditCard.trim().length() -4));
			
			} 
		} catch (Exception e)
		{
			CrmEncryptingException ex =  new CrmEncryptingException();
			ex.setStackTrace(e.getStackTrace());
			throw ex; 
		}
	
	}
	
	

	public Date convertExpiryDate() throws IllegalArgumentException
	{
        final SimpleDateFormat formatter = AccountsApiSupport.getCreditCardExpiryDateFormater(this.getContext());
        try
        {
			return formatter.parse(this.getExpiryDate());
		}
        catch (ParseException e)
        {
        	new IllegalArgumentException("Expiry date invalid: " + this.getExpiryDate());
		}
        return null;
	}
	
	@Override
    public Date getPMethodExpiryDate()
	throws IllegalArgumentException
	{
		
		if (this.getExpiryDate() != null)
		{
			return convertExpiryDate(); 
		} 
		
		throw new IllegalArgumentException("Expiry date is not set"); 
	}

	
	public void redoDirectDebit()
	throws HomeException
	{
		DirectDebitSupport.redoDirectDebit(this.getContext(), this); 
	}
	
	
	public DirectDebitRecord  createNewRecord()
	throws HomeException 
	{
	    return DirectDebitSupport.createNewRecord(this.getContext(), this); 
	}

	
	public String getDecodedCreditCard() {
		return decodedCreditCard;
	}

	public void setDecodedCreditCard(String decodedCreditCard) {
		this.decodedCreditCard = decodedCreditCard;
	}

    public String getTimeZone()
    {
        if (getContext()!=null)
        {
            return getTimeZone(getContext());
        }
        else
        {
            return getTimeZone(ContextLocator.locate());
        }
    }

    public String getTimeZone(Context ctx)
    {
        try
        {
            CRMSpid spid = SpidSupport.getCRMSpid(ctx, this.getSpid());
            return spid.getTimezone();
        }
        catch (Throwable t)
        {
            LogSupport.minor(ctx, this, "Unable to retrieve SPID. Returning default timezone: " + t.getMessage(), t);
            return TimeZone.getDefault().getID();
        }
    }

    /**
     * The loyalty card #
     */
    public String getLoyaltyCardId()
    {
        String lcid = "";
        final LoyaltyCardExtension extension =
            (LoyaltyCardExtension) this
                .getFirstAccountExtensionOfType(LoyaltyCardExtension.class);
        if (extension != null && extension.getLoyaltyCard() != null)
        {
            lcid = extension.getLoyaltyCard().getLoyaltyCardID();
        }
        return lcid;
    }

    /**
     * Return all the supplementary data for this account
     * @param context
     * @return
     * @throws HomeException
     */
    public Collection<SupplementaryData> getSupplementaryData(Context context) throws HomeException
    {
        return SupplementaryDataSupportHelper.get(context).getSupplementaryData(context,
                SupplementaryDataEntityEnum.ACCOUNT, this.getBAN());
    }

    /**
     * Return a given supplementary data for this account
     * @param context
     * @param key
     * @return
     * @throws HomeException
     */
    public SupplementaryData getSupplementaryData(Context context, String key) throws HomeException
    {
        return SupplementaryDataSupportHelper.get(context).getSupplementaryData(context,
                SupplementaryDataEntityEnum.ACCOUNT, this.getBAN(), key);
    }
    
    /**
     * Remove all the supplementary data for this bean
     * @param context
     * @throws HomeException
     */
    public void removeAllSupplementaryData(Context context) throws HomeException
    {
        SupplementaryDataSupportHelper.get(context).removeAllSupplementaryData(context,
                SupplementaryDataEntityEnum.ACCOUNT, this.getBAN());
    }


    /**
     * Returns all NO DEACTIVE subscribers for the Account.
     * 
     * @param ctx
     *            The operating context.
     * @return The collection of all ACTIVE subscribers for the account number.
     * @exception HomeException
     *                Thrown if there is a problem with retrieving the ACTIVE
     *                subscribers.
     */
    public Collection getNonDeActiveSubscribers(final Context ctx)
        throws HomeException
    {
        if (ctx == null)
        {
            throw new IllegalArgumentException(
                "Could not find object.  Context parameter is null.");
        }

        return AccountSupport.getNonDeActiveChildrenSubscribers(ctx,
            this);
    }
    
    /**
     * Returns First Active subscriber for the Account.
     * 
     * @param ctx
     *            The operating context.
     * @return First ACTIVE subscriber for the account number.
     * @exception HomeException
     *                Thrown if there is a problem with retrieving the ACTIVE
     *                subscribers.
     */
    public Subscriber getFirstActiveSubscriber(final Context ctx)
        throws HomeException
    {
        if (ctx == null)
        {
            throw new IllegalArgumentException(
                "Could not find object.  Context parameter is null.");
        }

        return AccountSupport.getFirstActiveSubscriber(ctx,
            this);
    }
    
    /**
     * Returns contract associated with Account.
     * 
     * @param ctx
     *            The operating context.
     * @return contract associated with account number.
     */
   public long getContract(final Context ctx)
    {
        if (ctx == null)
        {
            throw new IllegalArgumentException(
                "Could not find object.  Context parameter is null.");
        }

        return AccountSupport.getAccountContract(ctx, this.BAN_);
    }
    

    @Override
    public boolean transientEquals(Object o)
    {
    
        String mySessionKey = null;
        boolean flag = false;
        mySessionKey = CalculationServiceSupport.createNewSession(getContext());
        getContext().put(TRANSIENT_EQUALS_SESSION_KEY, mySessionKey);
        flag = super.transientEquals(o);
        
        getContext().put(TRANSIENT_EQUALS_SESSION_KEY, null);
     
        return flag;
    }
    
	public Collection<Subscriber> getSubscriptionsBySubscriptionType(final Context ctx,
		    long subType) throws HomeException
		{
			if (ctx == null)
			{
				throw new IllegalArgumentException(
				    "Could not find object.  Context parameter is null.");
			}

			Collection<Subscriber> result = new ArrayList<Subscriber>();
			if (subType != 0)
			{
				And filter = new And();
				filter.add(new EQ(SubscriberXInfo.BAN, getBAN()));
				filter.add(new EQ(SubscriberXInfo.SUBSCRIPTION_TYPE, subType));
				result =
				    HomeSupportHelper.get(ctx).getBeans(ctx, Subscriber.class,
				        filter);
			}

			return result;
		}
    
	protected Principal principal_ = null;
	protected String agent_;

	private String decodedCreditCard = ""; 
	/**
	 * The operating context.
	 */
	protected transient Context context_;
	
	public static String TRANSIENT_EQUALS_SESSION_KEY = "singleCallsessionKey";
	private BillCycle billCycle_ = null;


	
}
