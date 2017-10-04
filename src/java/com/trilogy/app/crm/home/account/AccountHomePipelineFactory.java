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

package com.trilogy.app.crm.home.account;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.account.ChildSubBillCycleUpdateHome;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.account.AccountToCreditCardInfoMapper;
import com.trilogy.app.crm.bean.paymentgatewayintegration.home.NonResponsibleAccountCCTokenValidator;
import com.trilogy.app.crm.client.alcatel.AlcatelFunctions;
import com.trilogy.app.crm.client.alcatel.AlcatelUpdateHome;
import com.trilogy.app.crm.extension.ExtensionForeignKeyAdapter;
import com.trilogy.app.crm.extension.ExtensionHandlingHome;
import com.trilogy.app.crm.extension.account.AccountExtension;
import com.trilogy.app.crm.extension.account.AccountExtensionXInfo;
import com.trilogy.app.crm.extension.account.AccountExtensionsValidator;
import com.trilogy.app.crm.extension.account.PoolExtension;
import com.trilogy.app.crm.extension.validator.ParentBeanExtensionsValidator;
import com.trilogy.app.crm.extension.validator.SingleInstanceExtensionsValidator;
import com.trilogy.app.crm.home.AccountFamilyPlanHome;
import com.trilogy.app.crm.home.AccountProvisioningHome;
import com.trilogy.app.crm.home.BusinessAccountProvisioningHome;
import com.trilogy.app.crm.home.LicenseHome;
import com.trilogy.app.crm.home.MovePipelineCreator;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.home.PooledGroupAccountOwnerMsisdnProvisioningHome;
import com.trilogy.app.crm.home.PooledGroupAccountProvisioningHome;
import com.trilogy.app.crm.home.PostpaidRecurringCreditCardScheduleRemovalHome;
import com.trilogy.app.crm.home.ReCalculateTotalNoOfSubForAccountHome;
import com.trilogy.app.crm.home.SupplementaryDataHandlingHome;
import com.trilogy.app.crm.home.account.extension.PooledAccountValidator;
import com.trilogy.app.crm.home.generic.AutoMappingHome;
import com.trilogy.app.crm.home.validator.AccountLoyaltyCardExtensionValidator;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.ConfigChangeRequestSupportHelper;
import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.app.crm.supportInterface.AccountInterfaceSupport;
import com.trilogy.app.crm.util.cipher.SpidAwareEncryptingAdapter;
import com.trilogy.app.crm.validator.CreditCardEntryValidator;
import com.trilogy.app.crm.validator.SecondaryEmailAddressesValidator;
import com.trilogy.app.crm.vpn.AccountMomPropertyUpdateHome;
import com.trilogy.app.crm.vpn.AccountMomValidator;
import com.trilogy.app.crm.vpn.VpnAccountHome;
import com.trilogy.app.crm.xhome.validator.EmailAddressValidator;
import com.trilogy.framework.xhome.beans.CompoundValidator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.AdapterHome;
import com.trilogy.framework.xhome.home.AuditJournalHome;
import com.trilogy.framework.xhome.home.ContextualizingHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.NoSelectAllHome;
import com.trilogy.framework.xhome.home.ValidatingHome;
import com.trilogy.framework.xhome.msp.SpidAwareHome;

/**
 * Provides a ContextFactory for creating AccountHome pipelines on demand.
 *
 * @author cindy.wong@redknee.com
 */
public class AccountHomePipelineFactory implements PipelineFactory, MovePipelineCreator
{

    /**
     * Create a new instance of <code>AccountHomePipelineFactory</code>.
     */
    protected AccountHomePipelineFactory()
    {
        // empty
    }


    /**
     * Returns an instance of <code>AccountHomePipelineFactory</code>.
     *
     * @return An instance of <code>AccountHomePipelineFactory</code>.
     */
    public static AccountHomePipelineFactory instance()
    {
        if (instance == null)
        {
            instance = new AccountHomePipelineFactory();
        }
        return instance;
    }

    /**
     * @{inheritDoc}
     * 
     * @see AccountHomePipelineFactory#decorateMoveHome(Home, Context, Context)
     * @deprecated
     */
    @Deprecated
    @Override
    public Home createMovePipeline(Context ctx, Context serverCtx)
    {
        return decorateMoveHome((Home) ctx.get(Common.ACCOUNT_CACHED_HOME), ctx, serverCtx);
    }


    /**
     * There are too many things involved in creating the AccountHome pipeline. Use
     * {@link AccountHomePipelineFactory#decorateHome} to decorate the cached AccountHome
     * instead.
     *
     * @param context
     *            The operating context.
     * @param serverContext
     *            The server context.
     * @return Decorate home.
     * @see AccountHomePipelineFactory#decorateHome(Home, Context, Context)
     * @deprecated
     */
    @Override
    @Deprecated
    public Home createPipeline(final Context context, final Context serverContext)
    {
        return decorateHome(StorageSupportHelper.get(context).createHome(context, Account.class, "ACCOUNT"), context, serverContext);
    }


    /**
     * Decorates the home to be used within move logic.
     *
     * @param originalHome
     *            Home being decorated.
     * @param context
     *            The operating context.
     * @param serverContext
     *            The server context.
     * @return Decorated home.
     */
    public Home decoratePoolConversionMoveHome(final Home originalHome, final Context context, final Context serverContext)
    {
        return decorateHome(originalHome, context, serverContext, true, true);
    }

    /**
     * Decorates the home to be used within move logic.
     *
     * @param originalHome
     *            Home being decorated.
     * @param context
     *            The operating context.
     * @param serverContext
     *            The server context.
     * @return Decorated home.
     */
    public Home decorateMoveHome(final Home originalHome, final Context context, final Context serverContext)
    {
        return decorateHome(originalHome, context, serverContext, true, false);
    }


    /**
     * Decorates the home.
     *
     * @param originalHome
     *            Home being decorated.
     * @param context
     *            The operating context.
     * @param serverContext
     *            The server context.
     * @return Decorated home.
     */
    public Home decorateHome(final Home originalHome, final Context context, final Context serverContext)
    {
        return decorateHome(originalHome, context, serverContext, false, false);
    }

    /**
     * Decorates the home properly depending on whether or not the home is required for a move operation or normal business logic.
     * 
     * @param originalHome
     *            Home being decorated.
     * @param context
     *            The operating context.
     * @param serverContext
     *            The server context.
     * @param moveHome
     * 
     * @return Appropriate home pipeline
     */
    protected Home decorateHome(final Home originalHome, final Context context, final Context serverContext, boolean moveHome, boolean poolExtensionInstallation)
    {
        Home home = originalHome;

        home = new ContextualizingHome(context, home);
        home = new AutoMappingHome( home , new AccountToCreditCardInfoMapper());
        
        // Decorate with adapter homes
        home = new AccountLazyLoadedPropertyUpdateHome(context, home);
        

        if (!moveHome)
        {
  
            home = new AccountDiscountUpdateHome(context, home);
            home = new AccountsGroupScreeningTemplateUpdateHome(context, home);
            home = new AccountModificationHistoryHome(context, home);
        	home = new AccountLastModifiedHome(home);
            home = new AuditJournalHome(context, home);
            home = new AccountCreditCategoryModificationHome(context, home);
            home = new VpnAccountHome(context, home);
            home = new AdapterHome(home, new SpidAwareEncryptingAdapter());
            
            home = new ChildSubBillCycleUpdateHome(context, home);

            home = new AccountHierachySyncHome(context, home);

            home = new BusinessAccountProvisioningHome(context, home);
            home = new PooledGroupAccountProvisioningHome(context, home);
        }

        home = new PendingBillCycleChangeCancellingHome(context, home);
        home = new AccountStateChangeReasonCodeUpdateHome(context, home);
        home = new BalanceManagementAccountProvisioningHome(context, home);
        
        home = new PostpaidRecurringCreditCardScheduleRemovalHome(context, home);
        
        home = new AccountRelationshipUpdateHome(context, home);
        
        home = new DiscountGradeInAccountUpdateHome(context, home);
        
        home = new AccountDiscountGradeChangeHome(context, home);
        
        if (!moveHome)
        {
            home = new ReleaseMsisdnsOnDeactivateHome(context, home);

            home = new ApplyAccountDiscountToSubscriberHome(context, home);
            
        }
        
        if (!moveHome || poolExtensionInstallation)
        {
            // Decorate with adapter homes that must be omitted from move pipelines unless it's installing the pool extension
            home = new ExtensionHandlingHome<AccountExtension>(
                    context, 
                    AccountExtension.class, 
                    AccountExtensionXInfo.BAN, 
                    home);
            home = new AdapterHome(home, 
                    new ExtensionForeignKeyAdapter(AccountExtensionXInfo.BAN));
        }
        
        // home that propagates update to Alcatel related properties
        home = new LicenseHome(context, LicenseConstants.ALCATEL_LICENSE, new AlcatelUpdateHome<Account>(context,
                new AlcatelFunctions.AlcatalAccountFunction<Account>(), home));
        
        // responsible BAN should be computed every time a new Account record is created, even during move
        home = new AccountSetResponsibleBANOnCreateHome(home);
        
        home = new AccountClearDebtCollectionAgencyHome(context, home);

        /*
         * TT#13011420022
         */
        home = new ReCalculateTotalNoOfSubForAccountHome(context, home);

        home = new AccountProvisioningHome(context, home);
        
        home = new AccountFamilyPlanHome(context, home);

        home = new SupplementaryDataHandlingHome(context, home);

        if (!moveHome)
        {
            // AccountPaymentPlanTransferBalanceHome needs to proceed
            // AccountProvisioningHome in the AccountHome pipeline. (Angie Li)
            home = new AccountPaymentPlanTransferBalanceHome(context, home);

            // DO NOT MOVE AccountPaymentPlanBalanceUpdateHome, it has to wrap
            // AccountPaymentPlanTransferBalanceHome. (Angie Li)
            home = new AccountPaymentPlanBalanceUpdateHome(context, home);

            home = new AccountMomPropertyUpdateHome(context, home);
            home = new AccountSetCurrencyOnCreateHome(home);

            // sets account contract end date
            home = new ContractEndDateSettingHome(context, home);
       
        }

        home = new SubscriberBirthdayPlanUpdateHome(home);
		home = new BlackListOverrideNoteCreationHome(home);
        
        if (!moveHome)
        {

            // validators
            final CompoundValidator validators = new CompoundValidator();
            validators.add(new NonResponsibleAccountCCTokenValidator());
            validators.add(new AccountRequiredFieldValidator());
            validators.add(new AccountSystemTypeValidator());
            validators.add(AccountTypeValidator.instance());
            validators.add(new AccountParentValidator());
            validators.add(new BusinessAccountValidator());
            validators.add(new AccountBlacklistValidator());
            validators.add(new AccountBillCycleValidator());
            validators.add(new AccountPromiseToPayValidator());
            validators.add(new AccountHierachyValidator());

            /*
             * TT#5112227140 Account SMS Number SHALL NOT be verified in any ways.
             */
            // validators.add(new AccountOwnerMsisdnValidator());
            validators.add(new AccountChildrenInactiveValidator());
            validators.add(new AccountMomValidator());
            validators.add(AccountPaymentPlanIntervalValidador.instance());
            validators.add(CreditCardEntryValidator.instance());
            validators.add(AccountDatesValidator.instance());
            validators.add(new AccountExtensionsValidator(AccountXInfo.ACCOUNT_EXTENSIONS));
            validators.add(AccountBillingLanguageValidator.instance());
            validators.add(AccountAcctMgrValidator.instance());
            validators.add(AccountDiscountClassValidator.instance());
            validators.add(AccountBirthdayPlanValidator.instance());

            /*
             * TT7080351570: Validate all SPID-aware fields referenced by the account belong
             * to the same SPID.
             */
            validators.add(AccountSpidAwareValidator.instance());
            validators.add(AccountBankValidator.instance());
            validators.add(new PooledAccountValidator());
            validators.add(new SingleInstanceExtensionsValidator());
            validators.add(new ParentBeanExtensionsValidator(PoolExtension.class));
            validators.add(new SecondaryEmailAddressesValidator(AccountXInfo.EMAIL_ID,AccountXInfo.SECONDARY_EMAIL_ADDRESSES));
            validators.add(new EmailAddressValidator(AccountXInfo.EMAIL_ID));
            validators.add(new AccountPromiseToPayExpiryDateValidator());
			validators.add(new AccountLoyaltyCardExtensionValidator());
			validators.add(new AccountsDiscountValidator());
			
            home = new ValidatingHome(validators, home);

            home = new PooledGroupAccountOwnerMsisdnProvisioningHome(context, home);
            home = new AccountBillCycleCreateHome(context,home);
            home = new SpidAwareHome(context, home);
        }
        home = new AccountPipeLineContextPrepareHome(home);

        home = new NoSelectAllHome(home);
		home =
		    ConfigChangeRequestSupportHelper.get(context)
		        .registerHomeForConfigSharing(context, home, Account.class);

		context.put(AccountInterfaceSupport.class, new AccountSupport());
        return home;
    }

    /**
     * Singleton instance.
     */
    private static AccountHomePipelineFactory instance;
}
