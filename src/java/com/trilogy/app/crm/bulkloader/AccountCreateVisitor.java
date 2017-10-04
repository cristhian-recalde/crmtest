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
package com.trilogy.app.crm.bulkloader;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.trilogy.app.crm.bean.AbstractAccount;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountCategory;
import com.trilogy.app.crm.bean.AccountCreationTemplate;
import com.trilogy.app.crm.bean.AccountCreationTemplateHome;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.GroupTypeEnum;
import com.trilogy.app.crm.bean.SpidIdentificationGroups;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.account.AccountIdentificationGroup;
import com.trilogy.app.crm.bean.account.SecurityQuestionAnswer;
import com.trilogy.app.crm.bean.bank.Bank;
import com.trilogy.app.crm.bean.bank.BankHome;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.extension.ExtensionHolder;
import com.trilogy.app.crm.extension.account.AccountExtensionHolder;
import com.trilogy.app.crm.extension.account.PoolExtension;
import com.trilogy.app.crm.extension.account.SubscriptionPoolProperty;
import com.trilogy.app.crm.home.account.AccountBillCycleValidator;
import com.trilogy.app.crm.support.AccountIdentificationSupport;
import com.trilogy.app.crm.support.AccountTypeSupportHelper;
import com.trilogy.app.crm.support.MsisdnSupport;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.util.format.ThreadLocalSimpleDateFormat;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.HomeVisitor;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;

public class AccountCreateVisitor extends HomeVisitor
{
    public static final String PM_MODULE = AcctSubBulkLoadRequestServicer.class.getName();

	public static final String ID_DELIMITER = ";";
	public static final String ID_FIELD_DELIMITER = "|";
	public static final int ID_NUM_FIELDS = 3;
	public static final int ID_FIELD_TYPE = 0;
	public static final int ID_FIELD_NUMBER = 1;
	public static final int ID_FIELD_EXPIRY_DATE = 2;

	
    /**
     * the number of accounts processed by this visitor
     */
    private int numberOfProcessedAccounts_;

    private int numberOfSuccessfullyProcessedAccounts_;

    /**
     * the account template class used instead of the default account (new Account())
     */
    private Account accountTemplate_ = null;

    /**
     * a writer for log/error messages regarding the account creation
     */
    private PrintWriter accountPrintWriter_;

    private PrintWriter accountErrPrintWriter_;

    private final Long subscriberAccountRole_ = Long.valueOf(1);

    private final Date now_;
    private final Date futureEndDate_;
    private final ThreadLocalSimpleDateFormat dateFormatter_;

    public AccountCreateVisitor(final Context ctx, final Home home, final Account template, final PrintWriter writer,
            final PrintWriter errWriter)
    {
        super(home);

        numberOfProcessedAccounts_ = 0;

        setAccountTemplate(template);
        setAccountPrintWriter(writer);
        setAccountErrPrintWriter(errWriter);

        now_ = new Date();
        futureEndDate_ = SubscriberSupport.getFutureEndDate(now_);
        dateFormatter_ = BulkLoadAccount.getDateFormat(ctx);
    }

    @Override
    public void visit(final Context ctx, final Object obj) throws AgentException, AbortVisitException
    {

        // Next Account
        final BulkLoadAccount ba = (BulkLoadAccount) obj;

        final PMLogMsg createAccountPM = new PMLogMsg(PM_MODULE, "Create Account", ba.getBAN());
        final PMLogMsg createAccountPMSuccess = new PMLogMsg(PM_MODULE, "Create Account (SUCCESS)", ba.getBAN());
        final PMLogMsg createAccountPMError = new PMLogMsg(PM_MODULE, "Create Account (ERROR)", ba.getBAN());
        try
        {
            incrementNumberOfProcessedAccounts();

            final Account a = getAccount(ctx, ba);

            // validates that the Bill Cycle exists and has the same spid as the account
            AccountBillCycleValidator.instance().validate(ctx, a);

            if ("0".equals(a.getParentBAN()))
            {
                a.setParentBAN("");
            }

            if (a.getRole() == AbstractAccount.DEFAULT_ROLE)
            {
                a.setRole(getSubscriberAccountRole(ctx));
            }

            // this is the subscriber connected to the account for the single view. Because the subscriber
            // is created when a new account is created, this is !=null but doesn't have any msisdn, package
            // so account creation fails.
            a.setSubscriber(null);

            //Try to create this account.  This create will generate
            //a unique BAN id for this Account
            getHome().create(ctx, a);
            incrementNumberOfSuccessfullyProcessedAccounts();

            //Provided the store is successfull we have to add the
            //oldKey mapping and the state mapping if we forced the
            //state change

            //If we reached here with no errors we assume success
            createAccountPMSuccess.log(ctx);
        }
        catch (Throwable t)
        {
            //An error occured trying to add this account
            String msg = "";
            try
            {
                //Write to log and error files: the error, the line that erred
                msg = ba.getBAN() + "-Failure Adding Account-" + t.getMessage() + "\n";
                logErrorToFiles(ctx, msg, ba);
                t.printStackTrace(getAccountPrintWriter());
            }
            catch (Throwable tt)
            {
                //Can't write to log file
                new InfoLogMsg(this, "Unable to write to Account log file: " + msg, tt).log(ctx);
            }
            createAccountPMError.log(ctx);
        }
        finally
        {
            createAccountPM.log(ctx);
        }
    }

    protected long getSubscriberAccountRole(final Context ctx)
    {
        return subscriberAccountRole_.longValue();
    }

    /**
     * Creates an account out of the bulk load account and the account
     * template
     *
     * @param ctx registry
     * @param ba  bulk load account
     * @return the account object
     * @throws HomeException 
     * @throws HomeInternalException 
     * @throws NumberFormatException 
     */
    public Account getAccount(final Context ctx, final BulkLoadAccount ba) throws AgentException, NumberFormatException, HomeInternalException, HomeException
    {   
        /* TT# 14012846025 [TCB 9.7.2][Regression]: Unable to create familyplan acct with extension "Sub Limit and Group PP" through Bulk loader*/
        Account a = new Account();
        Home home = (Home) ctx.get(AccountCreationTemplateHome.class);
        String templateId = ba.getAccountCreationTemplate();
        AccountCreationTemplate act=null;

        if(templateId != null && !"".equals(templateId.trim()))
        {
            Long tmeplateIdLong = Long.parseLong(templateId);
            act = (AccountCreationTemplate) home.find(ctx,tmeplateIdLong);      
            
        }        
        if (act != null)
        {
                a.setSpid(act.getSpid());
                AccountSupport.applyAccountCreationTemplate(ctx, a, act, false);    
        }
        else
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Account Creation Template not present.", null).log(ctx);
            }
        }

        copyBulkLoadValues(ctx, a, ba);
        saveBankInformation(ctx, ba);

        return a;
    }

    /**
     * Will save a Bank object in the Bank home if the bank is not yet saved. the Bank home is used for the
     * key webcontrol in the Account screen.
     *
     * @param ctx context, used for logging and registry
     * @param ba  the bulkload information
     */
    protected void saveBankInformation(final Context ctx, final BulkLoadAccount ba)
    {
        final Home home = (Home) ctx.get(BankHome.class);
        if (home == null)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Cannot find BankHome in context", null).log(ctx);
            }

            return;
        }

        if (ba.getBankID().length() != 0 && !ba.getBankID().equals(""))
        {
			final Bank bank = new Bank();
			bank.setBankId(ba.getBankID());
			bank.setName(ba.getBankName());
			bank.setSpid(ba.getSpid());
            try
            {
                if (home.find(ctx, ba.getBankID()) == null)
                {
                    home.create(ctx, bank);
                }
            }
            catch (HomeException e)
            {
                // we don't worry about it
            }
        }
    }

    /**
     * Copies values from the BulkLoadAccount to the Account
     *
     * @param a
     * @param ba
     */
    public void copyBulkLoadValues(final Context ctx, final Account a, final BulkLoadAccount ba) throws AgentException
    {
        a.setContext(ctx);

        if(ba.getBAN()!=null && !ba.getBAN().isEmpty())
        {
        	a.setBAN(ba.getBAN());
        }
        a.setParentBAN(ba.getParentBAN());
        a.setResponsible(ba.getResponsible());
        a.setSpid(ba.getSpid());
        
        a.setType(getAccountType(ctx, ba.getType()));
        a.setRole(ba.getAccountRole());
        a.setSystemType(ba.getBillingType());
        
        a.setGroupType(GroupTypeEnum.get((short) ba.getGroupHierarchyType()));
        
        // ba.getAccountCreationTemplate]
        a.setOwnerMSISDN(ba.getOwnerMSISDN());
        a.setTaxAuthority(ba.getTaxAuthority());
        a.setTaxExemption(ba.getTaxExemption());
        a.setCreditCategory(ba.getCreditCategory());

        a.setLanguage(ba.getLanguage());
        a.setCurrency(ba.getCurrency());
        a.setBillCycleID(ba.getBillCycleId());
        a.setBillingAddress1(ba.getBillingAddress1());
        a.setBillingAddress2(ba.getBillingAddress2());
        a.setBillingAddress3(ba.getBillingAddress3());
        a.setBillingCity(ba.getBillingCity());
        a.setBillingProvince(ba.getBillingProvince());
        a.setBillingCountry(ba.getBillingCountry());
        
        
        a.setBillingPostalCode(ba.getBillingPostalCode());

        // The following are added in 8.8/9.0 ICD but not in code?
        // a.setBillingPOBox(ba.getBillingPostOfficeBox());
        
        a.setContactName(ba.getContactName());
        a.setContactTel(ba.getContactTel());
        a.setContactFax(ba.getContactFax());
        a.setEmployer(ba.getEmployer());
        a.setEmployerAddress(ba.getEmployerAddress());

        copySecurityQuestions(ctx, a, ba);
        copyAccountIdentification(ctx, a, ba);

        if (a.getSystemType() == SubscriberTypeEnum.PREPAID)
        {
            a.setDateOfBirth(BulkLoadSupport.getDate(ctx, ba.getDateOfBirth(), null, dateFormatter_,
                    AccountXInfo.DATE_OF_BIRTH));
        }
        else
        {
            a.setDateOfBirth(BulkLoadSupport.getMandatoryDate(ctx, ba.getDateOfBirth(), dateFormatter_,
                    AccountXInfo.DATE_OF_BIRTH));
        }
        a.setOccupation(ba.getOccupation());
        a.setCompanyName(ba.getCompanyName());
        a.setTradingName(ba.getTradingName());
        a.setRegistrationNumber(ba.getRegistrationNumber());
        a.setCompanyTel(ba.getCompanyTel());
        a.setCompanyFax(ba.getCompanyFax());
        a.setCompanyAddress1(ba.getCompanyAddress1());
        a.setCompanyAddress2(ba.getCompanyAddress2());
        a.setCompanyAddress3(ba.getCompanyAddress3());
        a.setCompanyCity(ba.getCompanyCity());
        a.setCompanyProvince(ba.getCompanyProvince());
        a.setCompanyCountry(ba.getCompanyCountry());
        a.setBankID(ba.getBankID());
        a.setBankName(ba.getBankName());
        a.setBankPhone(ba.getBankPhone());
        a.setBankAddress1(ba.getBankAddress1());
        a.setBankAddress2(ba.getBankAddress2());
        a.setBankAccountNumber(ba.getBankAccountNumber());
        a.setBankAccountName(ba.getBankAccountName());
        a.setDealerCode(ba.getDealerCode());
        a.setFirstName(ba.getFirstName());
        a.setLastName(ba.getLastName());
        a.setInitials(ba.getInitials());
        a.setAccountName(ba.getAccountName());
        a.setPaymentMethodType(ba.getPaymentMethod());
        a.setCreditCardNumber(ba.getCreditCardNumber());
        a.setExpiryDate(ba.getExpiryDate());
        a.setHolderName(ba.getHolderName());
        a.setDebitBankTransit(ba.getDebitBankTransit());
        a.setDebitAccountNumber(ba.getDebitAccountNumber());
        
        if (ba.getCollectionAgencyId() != null && !ba.getCollectionAgencyId().isEmpty())
        {
        try {
        	int collectionAgency = Integer.parseInt(ba.getCollectionAgencyId());
        	a.setDebtCollectionAgencyId(collectionAgency);
        } catch (NumberFormatException exception)
        {
        	throw new AgentException("Collection Agency ID is invalid");
        }
        }
        
		a.setPMethodBankID(ba.getPaymentBankId());

		if (ba.getPaymentCreditCardTypeId() != null
		    && !ba.getPaymentCreditCardTypeId().isEmpty())
		{
			try
			{
				int creditCardTypeId =
				    Integer.parseInt(ba.getPaymentCreditCardTypeId());
				a.setPMethodCardTypeId(creditCardTypeId);
			}
			catch (NumberFormatException exception)
			{
				throw new AgentException(
				    "Payment Credit Card Type ID is invalid");
			}
		}

		if (ba.getMaximumDirectDebitAmount() != null
		    && !ba.getMaximumDirectDebitAmount().isEmpty())
		{
			try
			{
				long maxDebitAmount =
				    Long.parseLong(ba.getMaximumDirectDebitAmount());
				a.setMaxDebitAmount(maxDebitAmount);
			}
			catch (NumberFormatException exception)
			{
				throw new AgentException(
				    "Maximum Direct Debit Amount is invalid");
			}
		}

		// TODO billing postal code & PO box

		// TODO handle the DCRM parameters

        if (a.isRootAccount())
        {
            a.setAccountMgr(ba.getAccountManager());
        }
        a.setGreeting(ba.getGreeting());
        a.setReason(ba.getReasonCode());
        a.setDiscountClass(ba.getDiscountClass());
        if (ba.getBillingMessageOption() != null)
        {
            a.setBillingMsgPreference(ba.getBillingMessageOption());
        }
        a.setBillingMessage(ba.getBillingMessage());
        a.setEmailID(ba.getEmail());
        a.setContract(ba.getContract());
        a.setContractStartDate(BulkLoadSupport.getDate(ctx, ba.getContractStartDate(), futureEndDate_, dateFormatter_,
                AccountXInfo.CONTRACT_START_DATE));
        a.setContractEndDate(BulkLoadSupport.getDate(ctx, ba.getContractEndDate(), futureEndDate_, dateFormatter_,
                AccountXInfo.CONTRACT_END_DATE));

        copyPoolProperties(ctx, a, ba);
 
        String secondaryEmailAddress = ba.getSecondaryEmailAddress();
       
        if(secondaryEmailAddress!=null ){
        	
        	secondaryEmailAddress = secondaryEmailAddress.replace(';', ',');
        	a.setSecondaryEmailAddresses(secondaryEmailAddress);
        }
        
    }


    /**
     * The Account Type can be given as a Name or an ID from the Bulk Load CSV file.
     * We have to validate the value that is given and check if such an Account Type
     * actually exists in the system.  If it doesn't we should fail the Bulk load attempt.
     *
     * @param ctx
     * @param accountType Account Type from Bulk Load CSV file
     */
    protected long getAccountType(final Context ctx, final String accountType)
    {
        if (accountType == null)
        {
            return -1;
        }

        long type = -1;
        try
        {
            type = Long.parseLong(accountType);

			final AccountCategory at =
			    AccountTypeSupportHelper.get(ctx).getTypedAccountType(ctx, type);
            if (at == null)
            {
                throw new IllegalPropertyArgumentException("BulkLoadAccount.type", "Account Type [" + type
                        + "] is not a valid Account Type.");
            }
        }
        catch (Throwable th)
        {
            throw new IllegalPropertyArgumentException("BulkLoadAccount.type", "Account Type [" + accountType
                            + "] is not a valid Account Type.");
        }

        return type;
    }

    private void copySecurityQuestions(final Context ctx, final Account a, final BulkLoadAccount ba)
    {
        final CRMSpid spidInfo = getSpidProperty(ctx, a.getSpid());
        final int minNum;
        if (null != spidInfo)
        {
            minNum = spidInfo.getMinNumSecurityQuestions();
        }
        else
        {
            minNum = 0;
        }
        int count = 0;
        List list = a.getSecurityQuestionsAndAnswers();
        if (null == list)
        {
            list = new ArrayList();
            a.setSecurityQuestionsAndAnswers(list);
        }
        list.clear();

        if (ba.getQuestion1().trim().length() > 0 && ba.getAnswer1() != null && ba.getAnswer1().trim().length() > 0)
        {
            final SecurityQuestionAnswer s = new SecurityQuestionAnswer();
            s.setQuestion(ba.getQuestion1());
            s.setAnswer(ba.getAnswer1());
            list.add(s);
            count++;
        }
        if (ba.getQuestion2().trim().length() > 0 && ba.getAnswer2() != null && ba.getAnswer2().trim().length() > 0)
        {
            final SecurityQuestionAnswer s = new SecurityQuestionAnswer();
            s.setQuestion(ba.getQuestion2());
            s.setAnswer(ba.getAnswer2());
            list.add(s);
            count++;
        }
        if (ba.getQuestion3().trim().length() > 0 && ba.getAnswer3() != null && ba.getAnswer3().trim().length() > 0)
        {
            final SecurityQuestionAnswer s = new SecurityQuestionAnswer();
            s.setQuestion(ba.getQuestion3());
            s.setAnswer(ba.getAnswer3());
            list.add(s);
            count++;
        }

        // in the case of POSTPAID or HYBRID accounts, we need to make sure there is a minimum number of security questions
        // to ensure successful account creation
        if (!SubscriberTypeEnum.PREPAID.equals(a.getSystemType()) && count < minNum)
        {
            getAccountPrintWriter().print("Account [");
            getAccountPrintWriter().print(ba.getBAN());
            getAccountPrintWriter().println("] needs to have it's security questions and answers updated.");
            for (int i = count; i < minNum; i++)
            {
                final SecurityQuestionAnswer s = new SecurityQuestionAnswer();
                s.setQuestion("---------------");
                s.setAnswer("---------------");
                list.add(s);
            }
        }
    }

	private void copyAccountIdentification(final Context ctx, final Account a, final BulkLoadAccount ba) throws AgentException
    {
        SpidIdentificationGroups spidIdGroups = null;

        try
        {
            spidIdGroups = SpidSupport.getSpidIdentificationGroups(ctx, a.getSpid());
        }
        catch (Exception e)
        {
            LogSupport.info(ctx, this, "Exception caught trying to find Spid Identification Groups info for SPID [" + a.getSpid() + "]", e);
        }

        if (null == spidIdGroups)
        {
            LogSupport.info(ctx, this, "No SPID Identification Groups configuration defined for SPID [" + a.getSpid() + "]");
        }

        int count = 0;
        List list = a.getIdentificationGroupList();
        if (null == list)
        {
            list = new ArrayList<AccountIdentificationGroup>();
            a.setIdentificationGroupList(list);
        }
        list.clear();

		/*
		 * [Cindy Wong] Support the expandable AccountIdenfication bulkload/AUDI
		 * format added in 8.8/9.0.
		 */
		ba.setIdentifications(AccountIdentificationSupport.parseIdentifications(ctx, ba.getIdType1(), ba.getIdNumber1(), ba.getIdType2(), ba.getIdNumber2()));
		Set<Integer> usedIds = AccountIdentificationSupport.addAccountIdentifications(ctx, a,
		        ba.getIdentifications());

        // in the case of POSTPAID or HYBRID accounts, we need to make sure there is a minimum number of account identifications
        // to ensure successful account creation
        if (!SubscriberTypeEnum.PREPAID.equals(a.getSystemType()) && spidIdGroups!=null)
        {
            getAccountPrintWriter().println("Account [" + ba.getBAN() + "] needs to have it's identification updated.");

            AccountIdentificationSupport.fillInIdentificationSpots(ctx, a, spidIdGroups, usedIds);
		}
    }
    

    private void copyPoolProperties(final Context ctx, final Account a, final BulkLoadAccount ba) throws AgentException
    {
        final int poolSubscriptionSize = ba.getPooledSubscriptionTypes().size();
        final int numberOfPoolBundles = ba.getPooledBundleId().size();
        
        if (poolSubscriptionSize > 0  || ba.getPoolAirtimeBundles())
            {
            final PoolExtension poolExtn = new PoolExtension();
            poolExtn.setBAN(a.getBAN());
            poolExtn.setSpid(a.getSpid());
            final int pooledMsisdnGroup = SpidSupport.getGroupPooledMsisdnGroup(ctx, a.getSpid());

            /*try
            {
                // TT#13012237083 - bss acc bulkloader not able to load pooled accounts
            	poolExtn.getPoolMSISDN(ctx);
            }
            catch (HomeException e)
            {
                LogSupport.minor(ctx, this, "Unable to aquire Pool MSISDN: " + e.getMessage(), e);
                throw new AgentException("Unable to aquire Pool MSISDN: " + e.getMessage(), e);
            }*/
            if ( poolSubscriptionSize > 0 )
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "Pooled Subscriptions exists. Processing values for Pool Extension", null)
                            .log(ctx);
                }
                if (poolSubscriptionSize != ba.getPooledSubscriptionBalances().size())
                {
                    final String msg = "Subscription and Intital Balance entries mismatch in numbers for BAN: "
                            + a.getBAN();
                    new MinorLogMsg(this, msg, null).log(ctx);
                    throw new AgentException(msg);
                }

                
            final Iterator<Long> subscriptions = ba.getPooledSubscriptionTypes().iterator();

            final Iterator<Long> balances = ba.getPooledSubscriptionBalances().iterator();
            final Map<Long, SubscriptionPoolProperty> subscriptionProps = poolExtn.getSubscriptionPoolProperties();
            for (int count = 0; count < poolSubscriptionSize; ++count)
            {
                final SubscriptionPoolProperty prop = new SubscriptionPoolProperty();
                prop.setSubscriptionType(subscriptions.next());
                prop.setInitialPoolBalance(balances.next());
                subscriptionProps.put(prop.getSubscriptionType(), prop);
            }
          
            }
        	// TT#12122023013 - BSS 9.5, Bulkload Limitation
        	if(ba.getPoolAirtimeBundles() && numberOfPoolBundles > 0)
        	{
        		new DebugLogMsg(this, "Processing Pooled Bundles for Pool Extension of BAN: "+a.getBAN(), null)
                .log(ctx);   		
        		
        		final Iterator<Long> bundlesItr = ba.getPooledBundleId().iterator();
        		final Map<Long, BundleFee> poolBundles;

        		 Long bundleIds[]= new Long[numberOfPoolBundles];
        		for (int i = 0; i < numberOfPoolBundles; i++)
                {
        			bundleIds[i] =  bundlesItr.next();
                }
        		final CompoundIllegalStateException excl; 
                {
                    excl = new CompoundIllegalStateException();
                    poolBundles = PoolExtension.transformBundles(ctx, excl, bundleIds);
                    if(excl.getSize() >0 )
                    {
                    	excl.throwAll();
                    }
                }
                poolExtn.setPoolBundles(poolBundles);
                
                new DebugLogMsg(this, "Number of Pooled airtime bundles added for BAN: "+a.getBAN()+" are ["+numberOfPoolBundles+"].", null)
                .log(ctx);
        	}
        	else
        	{
        		new DebugLogMsg(this, "No pooled airtime bundles will be added for BAN: "+a.getBAN(), null)
                .log(ctx);
            }   
        	
        	final ExtensionHolder acctExtnHolder = new AccountExtensionHolder();
            acctExtnHolder.setExtension(poolExtn);

            List<ExtensionHolder> accountExtensions = new ArrayList<ExtensionHolder>(a.getAccountExtensions());
            accountExtensions.add(acctExtnHolder);
            a.setAccountExtensions(accountExtensions);
        }        
    }


    /**
     * @param ctx  - The operating context.
     * @param spid - The ID of the service provider to look up.
     * @return - The properties of the service provider specified.
     * @throws IllegalStateException
     */
    private CRMSpid getSpidProperty(final Context ctx, final int spid)
    {
        final Home spidHome = (Home) ctx.get(CRMSpidHome.class);
        if (null == spidHome)
        {
            LogSupport.info(ctx, this, "System error: CRMSpidHome not found in context");
        }

        CRMSpid sp = null;
        try
        {
            sp = (CRMSpid) spidHome.find(ctx, Integer.valueOf(spid));
        }
        catch (Exception e)
        {
            LogSupport.info(ctx, this, "Exception caught trying to find SP info for SPID [" + spid + "]", e);
        }

        if (null == sp)
        {
            LogSupport.info(ctx, this, "No SPID configuration defined for SPID [" + spid + "]");
        }
        return sp;
    }

    /**
     * @return Returns the numberOfProcessedAccounts.
     */
    public int getNumberOfProcessedAccounts()
    {
        return numberOfProcessedAccounts_;
    }

    public synchronized void incrementNumberOfProcessedAccounts()
    {
        ++numberOfProcessedAccounts_;
    }
    
    /**
     * @param numberOfProcessedAccounts The numberOfProcessedAccounts to set.
     */
    public void setNumberOfProcessedAccounts(final int numberOfProcessedAccounts)
    {
        this.numberOfProcessedAccounts_ = numberOfProcessedAccounts;
    }

    /**
     * @return Returns the accountTemplate.
     */
    public Account getAccountTemplate()
    {
        return accountTemplate_;
    }

    /**
     * @param accountTemplate The accountTemplate to set.
     */
    public void setAccountTemplate(final Account accountTemplate)
    {
        this.accountTemplate_ = accountTemplate;
    }

    /**
     * @return Returns the accountPrintWriter.
     */
    public PrintWriter getAccountPrintWriter()
    {
        return accountPrintWriter_;
    }

    /**
     * @param accountPrintWriter The accountPrintWriter to set.
     */
    public void setAccountPrintWriter(final PrintWriter accountPrintWriter)
    {
        this.accountPrintWriter_ = accountPrintWriter;
    }

    /**
     * @return Returns the numberOfSuccessfullyProcessedAccounts.
     */
    public int getNumberOfSuccessfullyProcessedAccounts()
    {
        return numberOfSuccessfullyProcessedAccounts_;
    }

    private synchronized void incrementNumberOfSuccessfullyProcessedAccounts()
    {
        ++numberOfSuccessfullyProcessedAccounts_;
    }
    
    /**
     * @param numberOfSuccessfullyProcessedAccounts
     *         The numberOfSuccessfullyProcessedAccounts to set.
     */
    public void setNumberOfSuccessfullyProcessedAccounts(final int numberOfSuccessfullyProcessedAccounts)
    {
        this.numberOfSuccessfullyProcessedAccounts_ = numberOfSuccessfullyProcessedAccounts;
    }

    /**
     * @return Returns the accountErrPrintWriter.
     */
    public PrintWriter getAccountErrPrintWriter()
    {
        return accountErrPrintWriter_;
    }

    /**
     * @param accountErrPrintWriter The accountErrPrintWriter to set.
     */
    public void setAccountErrPrintWriter(final PrintWriter accountErrPrintWriter)
    {
        this.accountErrPrintWriter_ = accountErrPrintWriter;
    }

    public void logErrorToFiles(final Context ctx, final String msg, final BulkLoadAccount ba)
    {
        // Write to log file: the error, the line that erred
        getAccountPrintWriter().print(msg);

        // writes the error in the error file: the error and the line that erred
        getAccountErrPrintWriter().print(msg);
        getAccountErrPrintWriter().print("   ");
        getAccountErrPrintWriter().println(BulkLoadAccountCSVSupport.instance().toString(ctx, ba));
    }
}
