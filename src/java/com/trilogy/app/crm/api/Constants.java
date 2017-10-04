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
package com.trilogy.app.crm.api;

import com.trilogy.app.crm.api.rmi.support.APIGenericParameterSupport;
import com.trilogy.app.crm.api.rmi.support.ApiErrorSource;
import com.trilogy.util.crmapi.wsdl.v2_1.api.AccountService;
import com.trilogy.util.crmapi.wsdl.v2_1.api.CallDetailService;
import com.trilogy.util.crmapi.wsdl.v2_1.api.CallingGroupService;
import com.trilogy.util.crmapi.wsdl.v2_1.api.CardPackageService;
import com.trilogy.util.crmapi.wsdl.v2_1.api.MobileNumberService;
import com.trilogy.util.crmapi.wsdl.v2_1.api.NoteService;
import com.trilogy.util.crmapi.wsdl.v2_1.api.ServicesAndBundlesService;
import com.trilogy.util.crmapi.wsdl.v2_1.api.SubscriptionService;
import com.trilogy.util.crmapi.wsdl.v2_1.api.TransactionService;
import com.trilogy.util.crmapi.wsdl.v2_2.api.GeneralProvisioningService;
import com.trilogy.util.crmapi.wsdl.v2_2.api.InvoiceService;

/**
 * Provides constants for the API.  Currently this is only the current per-methodpermissions.
 *
 *  gary.anderson.com
 */
public
class Constants
{
    public static final Object METHOD_NAME_CTX_KEY = "API.MethodName";
    
    /** Permission for the {@link AccountService#createAccount} method. */
    public static final String PERMISSION_ACCOUNTS_WRITE_CREATEACCOUNT = "app.crm.api.Accounts.write.createAccount";

    /** Permission for the {@link AccountService#deleteAccount} method. */
    public static final String PERMISSION_ACCOUNTS_WRITE_DELETEACCOUNT = "app.crm.api.Accounts.write.deleteAccount";

    /** Permission for the {@link AccountService#getAccountBalance} method. */
    public static final String PERMISSION_ACCOUNTS_READ_GETACCOUNTBALANCE = "app.crm.api.Accounts.read.getAccountBalance";

    /** Permission for the {@link AccountService#getAccountBalance} method. */
    public static final String PERMISSION_ACCOUNTS_READ_GETDETAILEDACCOUNTBALANCES = "app.crm.api.Accounts.read.getDetailedAccountBalances";

    
    /** Permission for the {@link AccountService#getAccountProfile} method. */
    public static final String PERMISSION_ACCOUNTS_READ_GETACCOUNTPROFILE = "app.crm.api.Accounts.read.getAccountProfile";

    /** Permission for the {@link AccountService#getAccountProfileV2} method. */
    public static final String PERMISSION_ACCOUNTS_READ_GETACCOUNTPROFILEV2 = "app.crm.api.Accounts.read.getAccountProfileV2";
    
    /** Permission for the {@link AccountService#convertIndividualToGroup} method. */
    public static final String PERMISSION_ACCOUNTS_WRITE_CONVERTINDIVIDUALTOGROUP = "app.crm.api.Accounts.write.convertIndividualToGroup";

    /** Permission for the {@link AccountService#listAccountTypes} method. */
    public static final String PERMISSION_ACCOUNTS_READ_LISTACCOUNTTYPES = "app.crm.api.Accounts.read.listAccountTypes";

    /** Permission for the {@link AccountService#listAccountCreationTemplates} method. */
    public static final String PERMISSION_ACCOUNTS_READ_LISTACCOUNTCREATIONTEMPLATES = "app.crm.api.Accounts.read.listAccountCreationTemplates";

    /** Permission for the {@link AccountService#listSubAccounts} method. */
    public static final String PERMISSION_ACCOUNTS_READ_LISTSUBACCOUNTS = "app.crm.api.Accounts.read.listSubAccounts";
    
    public static final String PERMISSION_ACCOUNTS_WRITE_CONVERTBILLINGTYPE = "app.crm.api.accounts.write.convertBillingType";

    /** Permission for the {@link SubscriptionService#listSubscriptions} method. */
    @Deprecated
    public static final String PERMISSION_ACCOUNTS_READ_LISTSUBSCRIBERS = "app.crm.api.Accounts.read.listSubscribers";
    public static final String PERMISSION_SUBSCRIBERS_READ_LISTSUBSCRIPTIONS = "app.crm.api.Subscribers.read.listSubscriptions";

    public static final String PERMISSION_SUBSCRIBERS_READ_LISTPPSMSUPPORTEES = "app.crm.api.Subscribers.read.listPPSMSupportees";

    public static final String PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIPTIONSPPSMSUPPORTER = "app.crm.api.Subscribers.write.updateSubscriptionsPPSMSupporter";

    public static final String PERMISSION_SUBSCRIBERS_READ_LISTSUBSCRIPTIONSCONTRACTS = "app.crm.api.Subscribers.read.listSubscriptionContracts";
    
    public static final String PERMISSION_SUBSCRIBERS_READ_GETSUBSCRIPTIONCONTRACTSTATUS = "app.crm.api.Subscribers.read.getSubscriptionContractStatus";
    
    public static final String PERMISSION_SUBSCRIBERS_READ_GETSUBSCRIPTIONSECONDARYBALANCE = "app.crm.api.Subscribers.read.getSubscriptionSecondaryBalance";

    public static final String PERMISSION_SUBSCRIBERS_READ_LISTDETAILSUBSCRIPTIONSCONTRACTS = "app.crm.api.Subscribers.read.listDetailedSubscriptionContracts";
    
    public static final String PERMISSION_SUBSCRIBERS_READ_GETSUBSCRIPTIONSUPDATEFEES = "app.crm.api.Subscribers.read.getSubscriptionUpdateFees";
    
    public static final String PERMISSION_SUBSCRIBERS_READ_GETSUBSCRIPTIONSCONTRACT = "app.crm.api.Subscribers.read.getSubscriptionContract";
    
    public static final String PERMISSION_SUBSCRIBERS_READ_UPDATESUBSCRIPTIONSCONTRACTDETAILS ="app.crm.api.Subscribers.write.updateSubscriptionContractDetails";
    
    public static final String PERMISSION_SUBSCRIBERS_WRITE_CREATEDETAILEDSUBSCRIPTIONBUCKETHISTORY ="app.crm.api.Subscribers.write.createDetailedSubscriptionBucketHistory";
    
    public static final String PERMISSION_SUBSCRIBERS_READ_LISTSUBSCRIPTIONBUCKETHISTORY ="app.crm.api.Subscribers.read.listSubscriptionBucketHistory";
    
    public static final String PERMISSION_SUBSCRIBERS_READ_LISTDETAILEDSUBSCRIPTIONBUCKETHISTORY ="app.crm.api.Subscribers.read.listDetailedSubscriptionBucketHistory";
    
    public static final String PERMISSION_SUBSCRIBERS_READ_GETBUCKETHISTORY ="app.crm.api.Subscribers.read.getBucketHistory";

    /** Permission for the {@link SubscriptionService#executeSubscriptionBalanceQuery} method. */
    public static final String PERMISSION_SUBSCRIBERS_READ_EXECUTESUBSCRIPTIONBALANCEQUERY = "app.crm.api.Subscribers.read.executeSubscriptionBalanceQuery";

    /** Permission for the {@link AccountService#listAccountRoles} method. */
    public static final String PERMISSION_ACCOUNTS_READ_LISTACCOUNTROLES = "app.crm.api.Accounts.read.listAccountRoles";
    
    /** Permission for the {@link AccountService#getAccountExtension} method. */
    public static final String PERMISSION_ACCOUNTS_READ_GETACCOUNTEXTENSION = "app.crm.api.Accounts.read.getAccountExtension";

    /** Permission for the {@link AccountService#executeAccountBalanceQuery} method. */
    public static final String PERMISSION_ACCOUNTS_READ_EXECUTEACCOUNTBALANCEQUERY = "app.crm.api.Accounts.read.executeAccountBalanceQuery";
    
    /** Permission for the {@link AccountService#listAccountExtensions} method. */
    public static final String PERMISSION_ACCOUNTS_READ_LISTACCOUNTEXTENSIONS = "app.crm.api.Accounts.read.listAccountExtensions";
   
    /** Permission for the {@link AccountService#listDetailedAccountExtensions} method. */
    public static final String PERMISSION_ACCOUNTS_READ_LISTDETAILEDACCOUNTEXTENSIONS = "app.crm.api.Accounts.read.listDetailedAccountExtensions";

	/** Permission for the {@link AccountService#getAccountAgedDebt} method. */
	public static final String PERMISSION_ACCOUNTS_READ_GETACCOUNTAGEDDEBT =
	    "app.crm.api.Accounts.read.getAccountAgedDebt";

    /** Permission for the {@link AccountService#updateAccountBillCycle} method. */
    public static final String PERMISSION_ACCOUNTS_WRITE_UPDATEACCOUNTBILLCYCLE = "app.crm.api.Accounts.write.updateAccountBillCycle";

    /** Permission for the {@link AccountService#updateAccountGroupMobileNumber} method. */
    public static final String PERMISSION_ACCOUNTS_WRITE_UPDATEACCOUNTGROUPMOBILENUMBER = "app.crm.api.Accounts.write.updateAccountGroupMobileNumber";

    /** Permission for the {@link AccountService#updateAccountParent} method. */
    public static final String PERMISSION_ACCOUNTS_WRITE_UPDATEACCOUNTPARENT = "app.crm.api.Accounts.write.updateAccountParent";

    /** Permission for the {@link AccountService#updateAccountProfile} method. */
    public static final String PERMISSION_ACCOUNTS_WRITE_UPDATEACCOUNTPROFILE = "app.crm.api.Accounts.write.updateAccountProfile";

    /** Permission for the {@link AccountService#updateAccountState} method. */
    public static final String PERMISSION_ACCOUNTS_WRITE_UPDATEACCOUNTSTATE = "app.crm.api.Accounts.write.updateAccountState";

    /** Permission for the {@link AccountService#updateAccountWithStateTransition} method. */
    public static final String PERMISSION_ACCOUNTS_WRITE_UPDATEACCOUNTWITHSTATETRANSITION = "app.crm.api.Accounts.write.updateAccountWithStateTransition";

    /** Permission for the {@link AccountService#updateAccountVPNMobileNumber} method. */
    public static final String PERMISSION_ACCOUNTS_WRITE_UPDATEACCOUNTVPNMOBILENUMBER = "app.crm.api.Accounts.write.updateAccountVPNMobileNumber";

    /** Permission for the {@link AccountService#updateAccountConvertSystemTypeHybridToPrepaid} method. */
    public static final String PERMISSION_ACCOUNTS_WRITE_UPDATEACCOUNTCONVERTSYSTEMTYPEHYBRIDTOPREPAID = "app.crm.api.Accounts.write.updateAccountConvertSystemTypeHybridToPrepaid";

    /** Permission for the {@link AccountService#updateAccountConvertSystemTypeHybridToPostpaid} method. */
    public static final String PERMISSION_ACCOUNTS_WRITE_UPDATEACCOUNTCONVERTSYSTEMTYPEHYBRIDTOPOSTPAID = "app.crm.api.Accounts.write.updateAccountConvertSystemTypeHybridToPostpaid";

    /** Permission for the {@link AccountService#updateAccountConvertSystemTypeToHybrid} method. */
    public static final String PERMISSION_ACCOUNTS_WRITE_UPDATEACCOUNTCONVERTSYSTEMTYPETOHYBRID = "app.crm.api.Accounts.write.updateAccountConvertSystemTypeToHybrid";

    
    /** Permission for the {@link AccountService#acquireMobileNumber} method. */
    public static final String PERMISSION_ACCOUNTS_WRITE_ACQUIREMOBILENUMBER = "app.crm.api.Accounts.write.acquireMobileNumber";

    /** Permission for the {@link AccountService#releaseMobileNumber} method. */
    public static final String PERMISSION_ACCOUNTS_WRITE_RELEASEMOBILENUMBER = "app.crm.api.Accounts.write.releaseMobileNumber";
    
    /** Permission for the {@link AccountService#updateAccountAddExtension} method. */
    public static final String PERMISSION_ACCOUNTS_WRITE_UPDATEACCOUNTADDEXTENSION = "app.crm.api.Accounts.write.updateAccountAddExtension";
    
    /** Permission for the {@link AccountService#updateAccountRemoveExtension} method. */
    public static final String PERMISSION_ACCOUNTS_WRITE_UPDATEACCOUNTREMOVEEXTENSION = "app.crm.api.Accounts.write.updateAccountRemoveExtension";
    
    /** Permission for the {@link AccountService#updateAccountExtension} method. */
    public static final String PERMISSION_ACCOUNTS_WRITE_UPDATEACCOUNTEXTENSION = "app.crm.api.Accounts.write.updateAccountExtension";
    
    /** Permission for the {@link AccountService#updateAccountParentV2} method. */
    public static final String PERMISSION_ACCOUNTS_WRITE_UPDATEACCOUNTPARENTV2 = "app.crm.api.Accounts.write.updateAccountParentV2";

    /** Permission for the {@link com.redknee.util.crmapi.wsdl.v2_1.api.BundleQueryService#requestAuxiliaryBundles(com.redknee.util.crmapi.wsdl.v2_0.types.CRMRequestHeader, com.redknee.util.crmapi.wsdl.v2_1.api.types.bundlequery.Spid_type1, com.redknee.util.crmapi.wsdl.v2_0.types.PaidType, int)} method. */
    public static final String PERMISSION_BUNDLEQUERY_READ_REQUESTAUXILIARYBUNDLES = "app.crm.api.BundleQuery.read.requestAuxiliaryBundles";
    
    /** Permission for the {@link CallDetailService#listDetailedCallDetails} method. */
    @Deprecated
    public static final String PERMISSION_CALLDETAILS_READ_GETALLCALLDETAILS = "app.crm.api.CallDetails.read.getAllCallDetails";
    public static final String PERMISSION_CALLDETAILS_READ_LISTDETAILEDCALLDETAILS = "app.crm.api.CallDetails.read.listDetailedCallDetails";

    /** Permission for the {@link CallDetailService#getCallDetail} method. */
    public static final String PERMISSION_CALLDETAILS_READ_GETCALLDETAIL = "app.crm.api.CallDetails.read.getCallDetail";

    /** Permission for the {@link CallDetailService#getCallType} method. */
    public static final String PERMISSION_CALLDETAILS_READ_GETCALLTYPE = "app.crm.api.CallDetails.read.getCallType";

    /** Permission for the {@link CallDetailService#getUsageType} method. */
    public static final String PERMISSION_CALLDETAILS_READ_GETUSAGETYPE = "app.crm.api.CallDetails.read.getUsageType";

    /** Permission for the {@link CallDetailService#listCallDetails} method. */
    public static final String PERMISSION_CALLDETAILS_READ_LISTCALLDETAILS = "app.crm.api.CallDetails.read.listCallDetails";

    /** Permission for the {@link CallDetailService#listCallTypes} method. */
    public static final String PERMISSION_CALLDETAILS_READ_LISTCALLTYPES = "app.crm.api.CallDetails.read.listCallTypes";

    /** Permission for the {@link CallDetailService#listUsageTypes} method. */
    public static final String PERMISSION_CALLDETAILS_READ_LISTUSAGETYPES = "app.crm.api.CallDetails.read.listUsageTypes";

    /** Permission for the {@link CallingGroupService#addClosedUserGroupEntries} method. */
    public static final String PERMISSION_CALLINGGROUPS_WRITE_ADDCLOSEDUSERGROUPENTRIES = "app.crm.api.CallingGroups.write.addClosedUserGroupEntries";

    /** Permission for the {@link CallingGroupService#addSubscriptionPersonalListPlanEntries} method. */
    @Deprecated
    public static final String PERMISSION_CALLINGGROUPS_WRITE_ADDSUBSCRIBERPERSONALLISTPLANENTRIES = "app.crm.api.CallingGroups.write.addSubscriberPersonalListPlanEntries";
    public static final String PERMISSION_CALLINGGROUPS_WRITE_ADDSUBSCRIPTIONPERSONALLISTPLANENTRIES = "app.crm.api.CallingGroups.write.addSubscriptionPersonalListPlanEntries";

    /** Permission for the {@link CallingGroupService#listClosedUserGroupEntries} method. */
    public static final String PERMISSION_CALLINGGROUPS_READ_LISTCLOSEDUSERGROUPENTRIES = "app.crm.api.CallingGroups.read.listClosedUserGroupEntries";

    /** Permission for the {@link CallingGroupService#listClosedUserGroups} method. */
    public static final String PERMISSION_CALLINGGROUPS_READ_LISTCLOSEDUSERGROUPS = "app.crm.api.CallingGroups.read.listClosedUserGroups";

    /** Permission for the {@link CallingGroupService#listSubscriptionPersonalListPlanEntries} method. */
    @Deprecated
    public static final String PERMISSION_CALLINGGROUPS_READ_LISTSUBSCRIBERPERSONALLISTPLANENTRIES = "app.crm.api.CallingGroups.read.listSubscriberPersonalListPlanEntries";
    public static final String PERMISSION_CALLINGGROUPS_READ_LISTSUBSCRIPTIONPERSONALLISTPLANENTRIES = "app.crm.api.CallingGroups.read.listSubscriptionPersonalListPlanEntries";

    /** Permission for the {@link CallingGroupService#getClosedUserGroup} method. */
    public static final String PERMISSION_CALLINGGROUPS_READ_GETCLOSEDUSERGROUP = "app.crm.api.CallingGroups.read.getClosedUserGroup";

    /** Permission for the {@link CallingGroupService#getClosedUserGroupTemplate} method. */
    public static final String PERMISSION_CALLINGGROUPS_READ_GETCLOSEDUSERGROUPTEMPLATE = "app.crm.api.CallingGroups.read.getClosedUserGroupTemplate";

    /** Permission for the {@link CallingGroupService#getPersonalListPlan} method. */
    public static final String PERMISSION_CALLINGGROUPS_READ_GETPERSONALLISTPLAN = "app.crm.api.CallingGroups.read.getPersonalListPlan";

    /** Permission for the {@link CallingGroupService#listClosedUserGroupTemplates} method. */
    public static final String PERMISSION_CALLINGGROUPS_READ_LISTCLOSEDUSERGROUPTEMPLATES = "app.crm.api.CallingGroups.read.listClosedUserGroupTemplates";

    /** Permission for the {@link CallingGroupService#listDetailedClosedUserGroupTemplates} method. */
    public static final String PERMISSION_CALLINGGROUPS_READ_LISTDETAILEDCLOSEDUSERGROUPTEMPLATES = "app.crm.api.CallingGroups.read.listDetailedClosedUserGroupTemplates";

    /** Permission for the {@link CallingGroupService#listDetailedClosedUserGroups} method. */
    public static final String PERMISSION_CALLINGGROUPS_READ_LISTDETAILEDCLOSEDUSERGROUPS = "app.crm.api.CallingGroups.read.listDetailedClosedUserGroups";

    /** Permission for the {@link CallingGroupService#listDetailedPersonalListPlans} method. */
    public static final String PERMISSION_CALLINGGROUPS_READ_LISTDETAILEDPERSONALLISTPLANS = "app.crm.api.CallingGroups.read.listDetailedPersonalListPlans";

    /** Permission for the {@link CallingGroupService#listPersonalListPlans} method. */
    public static final String PERMISSION_CALLINGGROUPS_READ_LISTPERSONALLISTPLANS = "app.crm.api.CallingGroups.read.listPersonalListPlans";

    /** Permission for the {@link CallingGroupService#removeClosedUserGroupEntries} method. */
    public static final String PERMISSION_CALLINGGROUPS_WRITE_REMOVECLOSEDUSERGROUPENTRIES = "app.crm.api.CallingGroups.write.removeClosedUserGroupEntries";

    /** Permission for the {@link CallingGroupService#removeSubscriptionPersonalListPlanEntries} method. */
    @Deprecated
    public static final String PERMISSION_CALLINGGROUPS_WRITE_REMOVESUBSCRIBERPERSONALLISTPLANENTRIES = "app.crm.api.CallingGroups.write.removeSubscriberPersonalListPlanEntries";
    public static final String PERMISSION_CALLINGGROUPS_WRITE_REMOVESUBSCRIPTIONPERSONALLISTPLANENTRIES = "app.crm.api.CallingGroups.write.removeSubscriptionPersonalListPlanEntries";

    /** Permission for the {@link CallingGroupService#addClosedUserGroupShortCodeEntries} method. */
    public static final String PERMISSION_CALLINGGROUPS_WRITE_ADDCLOSEDUSERGROUPSHORTCODEENTRIES = "app.crm.api.CallingGroups.write.addClosedUserGroupShortCodeEntries";

    /** Permission for the {@link CallingGroupService#listClosedUserGroupShortCodeEntries} method. */
    public static final String PERMISSION_CALLINGGROUPS_READ_LISTCLOSEDUSERGROUPSHORTCODEENTRIES = "app.crm.api.CallingGroups.read.listClosedUserGroupShortCodeEntries";

    /** Permission for the {@link CallingGroupService#updateClosedUserGroupShortCodeEntries} method. */
    public static final String PERMISSION_CALLINGGROUPS_WRITE_UPDATECLOSEDUSERGROUPSHORTCODEENTRIES = "app.crm.api.CallingGroups.write.updateClosedUserGroupShortCodeEntries";

    /** Permission for the {@link CallingGroupService#createClosedUserGroup} method. */
    public static final String PERMISSION_CALLINGGROUPS_WRITE_CREATECLOSEDUSERGROUP = "app.crm.api.CallingGroups.write.createClosedUserGroup";

    /** Permission for the {@link CallingGroupService#createClosedUserGroupTemplate} method. */
    public static final String PERMISSION_CALLINGGROUPS_WRITE_CREATECLOSEDUSERGROUPTEMPLATE = "app.crm.api.CallingGroups.write.createClosedUserGroupTemplate";

    /** Permission for the {@link CallingGroupService#createPersonalListPlan} method. */
    public static final String PERMISSION_CALLINGGROUPS_WRITE_CREATEPERSONALLISTPLAN = "app.crm.api.CallingGroups.write.createPersonalListPlan";

    /** Permission for the {@link CallingGroupService#updateClosedUserGroup} method. */
    public static final String PERMISSION_CALLINGGROUPS_WRITE_UPDATECLOSEDUSERGROUP = "app.crm.api.CallingGroups.write.updateClosedUserGroup";

    /** Permission for the {@link CallingGroupService#updateClosedUserGroupTemplate} method. */
    public static final String PERMISSION_CALLINGGROUPS_WRITE_UPDATECLOSEDUSERGROUPTEMPLATE = "app.crm.api.CallingGroups.write.updateClosedUserGroupTemplate";

    /** Permission for the {@link CallingGroupService#updatePersonalListPlan} method. */
    public static final String PERMISSION_CALLINGGROUPS_WRITE_UPDATEPERSONALLISTPLAN = "app.crm.api.CallingGroups.write.updatePersonalListPlan";

    /** Permission for the {@link CardPackageService#createCardPackage} method. */
    public static final String PERMISSION_CARDPACKAGES_WRITE_CREATECARDPACKAGE = "app.crm.api.CardPackages.write.createCardPackage";

    /** Permission for the {@link CardPackageService#getCardPackage} method. */
    public static final String PERMISSION_CARDPACKAGES_READ_GETCARDPACKAGE = "app.crm.api.CardPackages.read.getCardPackage";

    /** Permission for the {@link CardPackageService#listCardPackageGroups} method. */
    public static final String PERMISSION_CARDPACKAGES_READ_LISTCARDPACKAGEGROUPS = "app.crm.api.CardPackages.read.listCardPackageGroups";

    /** Permission for the {@link CardPackageService#listCardPackages} method. */
    public static final String PERMISSION_CARDPACKAGES_READ_LISTCARDPACKAGES = "app.crm.api.CardPackages.read.listCardPackages";

    /** Permission for the {@link GeneralProvisioningService#getCreditCategory} method. */
    @Deprecated
    public static final String PERMISSION_ACCOUNTS_READ_GETCREDITCATEGORY = "app.crm.api.Accounts.read.getCreditCategory";
    public static final String PERMISSION_GENERAL_PROVISIONING_READ_GETCREDITCATEGORY = "app.crm.api.GeneralProvisioning.read.getCreditCategory";

    /** Permission for the {@link GeneralProvisioningService#listOccupations} method. */
    @Deprecated
    public static final String PERMISSION_ACCOUNTS_READ_LISTOCCUPATIONS = "app.crm.api.Accounts.read.listOccupations";
    public static final String PERMISSION_GENERAL_PROVISIONING_READ_LISTOCCUPATIONS = "app.crm.api.GeneralProvisioning.read.listOccupations";

    /** Permission for the {@link GeneralProvisioningService#listBanks} method. */
    @Deprecated
    public static final String PERMISSION_ACCOUNTS_READ_LISTBANKS = "app.crm.api.Accounts.read.listBanks";
    public static final String PERMISSION_GENERAL_PROVISIONING_READ_LISTBANKS = "app.crm.api.GeneralProvisioning.read.listBanks";

    /** Permission for the {@link GeneralProvisioningService#listContracts} method. */
    @Deprecated
    public static final String PERMISSION_ACCOUNTS_READ_LISTCONTRACTS = "app.crm.api.Accounts.read.listContracts";
    public static final String PERMISSION_GENERAL_PROVISIONING_READ_LISTCONTRACTS = "app.crm.api.GeneralProvisioning.read.listContracts";

    /** Permission for the {@link GeneralProvisioningService#listCreditCategory} method. */
    @Deprecated
    public static final String PERMISSION_ACCOUNTS_READ_LISTCREDITCATEGORY = "app.crm.api.Accounts.read.listCreditCategory";
    public static final String PERMISSION_GENERAL_PROVISIONING_READ_LISTCREDITCATEGORY = "app.crm.api.GeneralProvisioning.read.listCreditCategory";

    /** Permission for the {@link GeneralProvisioningService#getContract} method. */
    @Deprecated
    public static final String PERMISSION_ACCOUNTS_READ_GETCONTRACT = "app.crm.api.Accounts.read.getContract";
    public static final String PERMISSION_GENERAL_PROVISIONING_READ_GETCONTRACT = "app.crm.api.GeneralProvisioning.read.getContract";

    /** Permission for the {@link GeneralProvisioningService#listBlockingTemplates} method. */
    public static final String PERMISSION_GENERAL_PROVISIONING_READ_LISTBLOCKINGTEMPLATES = "app.crm.api.GeneralProvisioning.read.listBlockingTemplates";

    /** Permission for the {@link GeneralProvisioningService#listChargingTemplates} method. */
    public static final String PERMISSION_GENERAL_PROVISIONING_READ_LISTCHARGINGTEMPLATES = "app.crm.api.GeneralProvisioning.read.listChargingTemplates";

    /** Permission for the {@link GeneralProvisioningService#listChargingTemplates} method. */
    public static final String PERMISSION_GENERAL_PROVISIONING_READ_LISTCOLLECTIONAGENCIES = "app.crm.api.GeneralProvisioning.read.listCollectionAgencies";

    /** Permission for the {@link GeneralProvisioningService#listScreeningTemplates} method. */
    public static final String PERMISSION_GENERAL_PROVISIONING_READ_LISTSCREENINGTEMPLATES = "app.crm.api.GeneralProvisioning.read.listScreeningTemplates";

    /** Permission for the {@link GeneralProvisioningService#getIdentificationGroup} method. */
    public static final String PERMISSION_GENERAL_PROVISIONING_READ_GETIDENTIFICATIONGROUP = "app.crm.api.GeneralProvisioning.read.getIdentificationGroup";

    /** Permission for the {@link GeneralProvisioningService#listIdentifications} method. */
    public static final String PERMISSION_GENERAL_PROVISIONING_READ_LISTIDENTIFICATIONS = "app.crm.api.GeneralProvisioning.read.listIdentifications";

    /** Permission for the {@link GeneralProvisioningService#listDetailedIdentificationGroups} method. */
    public static final String PERMISSION_GENERAL_PROVISIONING_READ_LISTDETAILEDIDENTIFICATIONGROUPS = "app.crm.api.GeneralProvisioning.read.listDetailedIdentificationGroups";

    /** Permission for the {@link GeneralProvisioningService#listDetailedIdentifications} method. */
    public static final String PERMISSION_GENERAL_PROVISIONING_READ_LISTDETAILEDIDENTIFICATIONS = "app.crm.api.GeneralProvisioning.read.listDetailedIdentifications";

    /** Permission for the {@link GeneralProvisioningService#getIdentification} method. */
    public static final String PERMISSION_GENERAL_PROVISIONING_READ_GETIDENTIFICATION = "app.crm.api.GeneralProvisioning.read.getIdentification";

    /** Permission for the {@link GeneralProvisioningService#listIdentificationGroups} method. */
    public static final String PERMISSION_GENERAL_PROVISIONING_READ_LISTIDENTIFICATIONGROUPS = "app.crm.api.GeneralProvisioning.read.listIdentificationGroups";

    /** Permission for the {@link GeneralProvisioningService#listDiscountClasses} method. */
    public static final String PERMISSION_GENERAL_PROVISIONING_READ_LISTDISCOUNTCLASSES = "app.crm.api.GeneralProvisioning.read.listDiscountClasses";

    /** Permission for the {@link GeneralProvisioningService#getDiscountClass} method. */
    public static final String PERMISSION_GENERAL_PROVISIONING_READ_GETDISCOUNTCLASS = "app.crm.api.GeneralProvisioning.read.getDiscountClass";

    /** Permission for the {@link GeneralProvisioningService#listDetailedDiscountClasses} method. */
    public static final String PERMISSION_GENERAL_PROVISIONING_READ_LISTDETAILEDDISCOUNTCLASSES = "app.crm.api.GeneralProvisioning.read.listDetailedDiscountClasses";

    /** Permission for the {@link GeneralProvisioningService#listAuthorizedDiscountClasses} method. */
    public static final String PERMISSION_GENERAL_PROVISIONING_READ_LISTAUTHORIZEDDISCOUNTCLASSES = "app.crm.api.GeneralProvisioning.read.listAuthorizedDiscountClasses";
    
    /** Permission for the {@link GeneralProvisioningService#createDiscountClass} method. */
    public static final String PERMISSION_GENERAL_PROVISIONING_WRITE_CREATEDISCOUNTCLASS = "app.crm.api.GeneralProvisioning.write.createDiscountClass";
    
    /** Permission for the {@link GeneralProvisioningService#createDiscountClass} method. */
    public static final String PERMISSION_GENERAL_PROVISIONING_WRITE_UPDATEDISCOUNTCLASS = "app.crm.api.GeneralProvisioning.write.updateDiscountClass";
    
    /** Permission for the {@link GeneralProvisioningService#addOrUpdateSupplementaryData} method. */
    public static final String PERMISSION_GENERAL_PROVISIONING_WRITE_ADDORUPDATESUPPLEMENTARYDATA = "app.crm.api.GeneralProvisioning.write.addOrUpdateSupplementaryData";
    
    /** Permission for the {@link GeneralProvisioningService#removeSupplementaryDataByName} method. */
    public static final String PERMISSION_GENERAL_PROVISIONING_WRITE_REMOVESUPPLEMENTARYDATABYNAME = "app.crm.api.GeneralProvisioning.write.removeSupplementaryDataByName";
    
    /** Permission for the {@link GeneralProvisioningService#removeSupplementaryData} method. */
    public static final String PERMISSION_GENERAL_PROVISIONING_WRITE_REMOVESUPPLEMENTARYDATA = "app.crm.api.GeneralProvisioning.write.removeSupplementaryData";
    
    /** Permission for the {@link GeneralProvisioningService#setSupplementaryData} method. */
    public static final String PERMISSION_GENERAL_PROVISIONING_WRITE_SETSUPPLEMENTARYDATA = "app.crm.api.GeneralProvisioning.write.setSupplementaryData";
//    /** Permission for the {@link GeneralProvisioning#getServiceProvider} method. */
//    public static final String PERMISSION_GENERALPROVISIONING_READ_GETSERVICEPROVIDER = "app.crm.api.GeneralProvisioning.read.getServiceProvider";
//
//    /** Permission for the {@link GeneralProvisioning#getTaxAuthority} method. */
//    public static final String PERMISSION_GENERALPROVISIONING_READ_GETTAXAUTHORITY = "app.crm.api.GeneralProvisioning.read.getTaxAuthority";
//
//    /** Permission for the {@link GeneralProvisioning#listDealerCode} method. */
//    public static final String PERMISSION_GENERALPROVISIONING_READ_LISTDEALERCODE = "app.crm.api.GeneralProvisioning.read.listDealerCode";
//
//    /** Permission for the {@link GeneralProvisioning#listDiscountClasses} method. */
//    public static final String PERMISSION_GENERALPROVISIONING_READ_LISTDISCOUNTCLASSES = "app.crm.api.GeneralProvisioning.read.listDiscountClasses";
//
//    /** Permission for the {@link GeneralProvisioning#listHLRIdentifiers} method. */
//    public static final String PERMISSION_GENERALPROVISIONING_READ_LISTHLRIDENTIFIERS = "app.crm.api.GeneralProvisioning.read.listHLRIdentifiers";
//
//    /** Permission for the {@link GeneralProvisioning#listProvinces} method. */
//    public static final String PERMISSION_GENERALPROVISIONING_READ_LISTPROVINCES = "app.crm.api.GeneralProvisioning.read.listProvinces";
//
    /** Permission for the {@link GeneralProvisioning#listServiceProviders} method. */
    public static final String PERMISSION_GENERALPROVISIONING_READ_LISTSERVICEPROVIDERS = "app.crm.api.GeneralProvisioning.read.listServiceProviders";
    
    /** Permission for the {@link GeneralProvisioning#getBlacklistStatus} method. */
    public static final String PERMISSION_GENERALPROVISIONING_READ_GETBLACKLISTSTATUS = "app.crm.api.GeneralProvisioning.read.getBlacklistStatus";

    /** Permission for the {@link GeneralProvisioning#getSupplementaryData} method. */
    public static final String PERMISSION_GENERAL_PROVISIONING_READ_GETSUPPLEMENTARYDATA = "app.crm.api.GeneralProvisioning.read.getSupplementaryData";

    /** Permission for the {@link GeneralProvisioning#acquireSequenceBlock} method. */
    public static final String PERMISSION_GENERAL_PROVISIONING_READ_ACQUIRESEQUENCEBLOCK = "app.crm.api.GeneralProvisioning.read.acquireSequenceBlock";

    /** Permission for the {@link GeneralProvisioning#acquireReceiptBlock} method. */
    public static final String PERMISSION_GENERAL_PROVISIONING_READ_ACQUIRERECEIPTBLOCK = "app.crm.api.GeneralProvisioning.read.acquireReceiptBlock";

    /**
	 * Permission for the {@link GeneralProvisioning#listServiceProviders}
	 * method.
	 */
	public static final String PERMISSION_GENERALPROVISIONING_READ_LISTCREDITCARDTYPES =
	    "app.crm.api.GeneralProvisioning.read.listCreditCardTypes";
//
//    /** Permission for the {@link GeneralProvisioning#listSubscriptionCategories} method. */
//    @Deprecated
//    public static final String PERMISSION_GENERALPROVISIONING_READ_LISTSUBSCRIBERCATEGORIES = "app.crm.api.GeneralProvisioning.read.listSubscriberCategories";
//    public static final String PERMISSION_GENERALPROVISIONING_READ_LISTSUBSCRIPTIONCATEGORIES = "app.crm.api.GeneralProvisioning.read.listSubscriptionCategories";
//
//    /** Permission for the {@link GeneralProvisioning#listTaxAuthority} method. */
//    public static final String PERMISSION_GENERALPROVISIONING_READ_LISTTAXAUTHORITY = "app.crm.api.GeneralProvisioning.read.listTaxAuthority";

    /** Permission for the {@link InvoiceService#getBillCycle} method. */
    @Deprecated
    public static final String PERMISSION_ACCOUNTS_READ_GETBILLCYCLE = "app.crm.api.Accounts.read.getBillCycle";
    public static final String PERMISSION_INVOICES_READ_GETBILLCYCLE = "app.crm.api.Invoices.read.getBillCycle";

    /** Permission for the {@link InvoiceService#getInvoice} method. */
    @Deprecated
    public static final String PERMISSION_ACCOUNTS_READ_GETINVOICE = "app.crm.api.Accounts.read.getInvoice";
    public static final String PERMISSION_INVOICES_READ_GETINVOICE = "app.crm.api.Invoices.read.getInvoice";

    /** Permission for the {@link InoviceService#getPaymentPlan} method. */
    public static final String PERMISSION_INVOICES_READ_GETPAYMENTPLAN = "app.crm.api.Invoices.read.getPaymentPlan";

    /** Permission for the {@link InvoiceService#listBillCycles} method. */
    @Deprecated
    public static final String PERMISSION_ACCOUNTS_READ_LISTBILLCYCLES = "app.crm.api.Accounts.read.listBillCycles";
    public static final String PERMISSION_INVOICES_READ_LISTBILLCYCLES = "app.crm.api.Invoices.read.listBillCycles";

    /** Permission for the {@link InvoiceService#listInvoices} method. */
    public static final String PERMISSION_INVOICES_READ_LISTINVOICEDELIVERYOPTIONS = "app.crm.api.Invoices.read.listInvoiceDeliveryOptions";

    /** Permission for the {@link InvoiceService#listInvoices} method. */
    @Deprecated
    public static final String PERMISSION_ACCOUNTS_READ_LISTINVOICES = "app.crm.api.Accounts.read.listInvoices";
    public static final String PERMISSION_INVOICES_READ_LISTINVOICES = "app.crm.api.Invoices.read.listInvoices";
    
    /** Permission for the {@link InvoiceService#listPaymentPlans} method. */
    public static final String PERMISSION_INVOICES_READ_LISTPAYMENTPLANS = "app.crm.api.Invoices.read.listPaymentPlans";

    /** Permission for the {@link InvoiceService#generateAlternateInvoice} method. */
    public static final String PERMISSION_INVOICES_WRITE_GENERATEALTERNATEINVOICE = "app.crm.api.Invoices.write.generateAlternateInvoice";

    /** Permission for the {@link InvoiceService#applyChargesForAlternateInvoice} method. */
    public static final String PERMISSION_INVOICES_WRITE_APPLYCHARGESFORALTERNATEINVOICE = "app.crm.api.Invoices.write.applyChargesForAlternateInvoice";

    /** Permission for the {@link MobileNumberService#getGroup} method. */
    public static final String PERMISSION_MOBILENUMBERS_READ_GETGROUP = "app.crm.api.MobileNumbers.read.getGroup";

    /** Permission for the {@link MobileNumberService#getMobileNumber} method. */
    public static final String PERMISSION_MOBILENUMBERS_READ_GETMOBILENUMBER = "app.crm.api.MobileNumbers.read.getMobileNumber";

    /** Permission for the {@link MobileNumberService#updateMobileNumberState} method. */
    public static final String PERMISSION_MOBILENUMBERS_WRITE_UPDATEMOBILENUMBERSTATE = "app.crm.api.MobileNumbers.write.updateMobileNumberState";
    
    /** Permission for the {@link MobileNumberService#listGroups} method. */
    public static final String PERMISSION_MOBILENUMBERS_READ_LISTGROUPS = "app.crm.api.MobileNumbers.read.listGroups";

    /** Permission for the {@link MobileNumberService#listMobileNumbers} method. */
    public static final String PERMISSION_MOBILENUMBERS_READ_LISTMOBILENUMBERS = "app.crm.api.MobileNumbers.read.listMobileNumbers";

    /** Permission for the {@link NoteService#listAccountNotes} method. */
    public static final String PERMISSION_NOTES_READ_LISTACCOUNTNOTES = "app.crm.api.Notes.read.listAccountNotes";

    /** Permission for the {@link NoteService#listSubscriptionNotes} method. */
    @Deprecated
    public static final String PERMISSION_NOTES_READ_LISTSUBSCRIBERNOTES = "app.crm.api.Notes.read.listSubscriberNotes";
    public static final String PERMISSION_NOTES_READ_LISTSUBSCRIPTIONNOTES = "app.crm.api.Notes.read.listSubscriptionNotes";

    /** Permission for the {@link NoteService#updateAccountAddNote} method. */
    public static final String PERMISSION_NOTES_WRITE_UPDATEACCOUNTADDNOTE = "app.crm.api.Notes.write.updateAccountAddNote";

    /** Permission for the {@link NoteService#updateSubscriptionAddNote} method. */
    @Deprecated
    public static final String PERMISSION_NOTES_WRITE_UPDATESUBSCRIBERADDNOTE = "app.crm.api.Notes.write.updateSubscriberAddNote";
    public static final String PERMISSION_NOTES_WRITE_UPDATESUBSCRIPTIONADDNOTE = "app.crm.api.Notes.write.updateSubscriptionAddNote";
    
    /** Permission for the {@link NoteService#listAccountNotesV2} method. */
    public static final String PERMISSION_NOTES_READ_LISTACCOUNTNOTESV2 = "app.crm.api.Notes.read.listAccountNotesV2";

    /** Permission for the {@link NoteService#listSubscriptionNotesV2} method. */
    public static final String PERMISSION_NOTES_READ_LISTSUBSCRIPTIONNOTESV2 = "app.crm.api.Notes.read.listSubscriptionNotesV2";
    
    /** Permission for the {@link ServicesAndBundlesService#listDetailedPricePlans} method. */
    @Deprecated
    public static final String PERMISSION_SERVICESANDBUNDLES_READ_GETALLPRICEPLANS = "app.crm.api.ServicesAndBundles.read.getAllPricePlans";
    public static final String PERMISSION_SERVICESANDBUNDLES_READ_LISTDETAILEDPRICEPLANS = "app.crm.api.ServicesAndBundles.read.listDetailedPricePlans";

    /** Permission for the {@link ServicesAndBundlesService#getAuxiliaryService} method. */
    public static final String PERMISSION_SERVICESANDBUNDLES_READ_GETAUXILIARYSERVICE = "app.crm.api.ServicesAndBundles.read.getAuxiliaryService";

    /** Permission for the {@link ServicesAndBundlesService#getPackage} method. */
    public static final String PERMISSION_SERVICESANDBUNDLES_READ_GETPACKAGE = "app.crm.api.ServicesAndBundles.read.getPackage";

    /** Permission for the {@link ServicesAndBundlesService#getPricePlan} method. */
    public static final String PERMISSION_SERVICESANDBUNDLES_READ_GETPRICEPLAN = "app.crm.api.ServicesAndBundles.read.getPricePlan";

    /** Permission for the {@link ServicesAndBundlesService#listAuxiliaryBundles} method. */
    public static final String PERMISSION_SERVICESANDBUNDLES_READ_LISTAUXILIARYBUNDLES = "app.crm.api.ServicesAndBundles.read.listAuxiliaryBundles";

    /** Permission for the {@link ServicesAndBundlesService#listAuxiliaryServices} method. */
    public static final String PERMISSION_SERVICESANDBUNDLES_READ_LISTAUXILIARYSERVICES = "app.crm.api.ServicesAndBundles.read.listAuxiliaryServices";

    /** Permission for the {@link ServicesAndBundlesService#listBundleFees} method. */
    public static final String PERMISSION_SERVICESANDBUNDLES_READ_LISTBUNDLEFEES = "app.crm.api.ServicesAndBundles.read.listBundleFees";

    /** Permission for the {@link ServicesAndBundlesService#listPackages} method. */
    public static final String PERMISSION_SERVICESANDBUNDLES_READ_LISTPACKAGES = "app.crm.api.ServicesAndBundles.read.listPackages";

    /** Permission for the {@link ServicesAndBundlesService#listPricePlans} method. */
    public static final String PERMISSION_SERVICESANDBUNDLES_READ_LISTPRICEPLANS = "app.crm.api.ServicesAndBundles.read.listPricePlans";

    /** Permission for the {@link ServicesAndBundlesService#listServiceFees} method. */
    public static final String PERMISSION_SERVICESANDBUNDLES_READ_LISTSERVICEFEES = "app.crm.api.ServicesAndBundles.read.listServiceFees";

    /** Permission for the {@link ServicesAndBundlesService#getBundle} method. */
    public static final String PERMISSION_SERVICESANDBUNDLES_READ_GETBUNDLE = "app.crm.api.ServicesAndBundles.read.getBundle";

    /** Permission for the {@link ServicesAndBundlesService#getBundleCategory} method. */
    public static final String PERMISSION_SERVICESANDBUNDLES_READ_GETBUNDLECATEGORY = "app.crm.api.ServicesAndBundles.read.getBundleCategory";

    /** Permission for the {@link ServicesAndBundlesService#getBundleCategoryRatePlanAssociation} method. */
    public static final String PERMISSION_SERVICESANDBUNDLES_READ_GETBUNDLECATEGORYRATEPLANASSOCIATION = "app.crm.api.ServicesAndBundles.read.getBundleCategoryRatePlanAssociation";

    /** Permission for the {@link ServicesAndBundlesService#getPricePlanDependencyGroups} method. */
    public static final String PERMISSION_SERVICESANDBUNDLES_READ_GETPRICEPLANDEPENDENCYGROUPS = "app.crm.api.ServicesAndBundles.read.getPricePlanDependencyGroups";

    /** Permission for the {@link ServicesAndBundlesService#getPricePlanPrerequisiteGroups} method. */
    public static final String PERMISSION_SERVICESANDBUNDLES_READ_GETPRICEPLANPREREQUISITEGROUPS = "app.crm.api.ServicesAndBundles.read.getPricePlanPrerequisiteGroups";

    /** Permission for the {@link ServicesAndBundlesService#getPricePlanValidationGroups} method. */
    public static final String PERMISSION_SERVICESANDBUNDLES_READ_GETPRICEPLANVALIDATIONGROUPS = "app.crm.api.ServicesAndBundles.read.getPricePlanValidationGroups";

    /** Permission for the {@link ServicesAndBundlesService#getService} method. */
    public static final String PERMISSION_SERVICESANDBUNDLES_READ_GETSERVICE = "app.crm.api.ServicesAndBundles.read.getService";

    /** Permission for the {@link ServicesAndBundlesService#listBundleCategories} method. */
    public static final String PERMISSION_SERVICESANDBUNDLES_READ_LISTBUNDLECATEGORIES = "app.crm.api.ServicesAndBundles.read.listBundleCategories";

    /** Permission for the {@link ServicesAndBundlesService#listBundleCategoryRatePlanAssociations} method. */
    public static final String PERMISSION_SERVICESANDBUNDLES_READ_LISTBUNDLECATEGORYRATEPLANASSOCIATIONS = "app.crm.api.ServicesAndBundles.read.listBundleCategoryRatePlanAssociations";

    /** Permission for the {@link ServicesAndBundlesService#listBundles} method. */
    public static final String PERMISSION_SERVICESANDBUNDLES_READ_LISTBUNDLES = "app.crm.api.ServicesAndBundles.read.listBundles";

    /** Permission for the {@link ServicesAndBundlesService#listDetailedAuxiliaryServices} method. */
    public static final String PERMISSION_SERVICESANDBUNDLES_READ_LISTDETAILEDAUXILIARYSERVICES = "app.crm.api.ServicesAndBundles.read.listDetailedAuxiliaryServices";

    /** Permission for the {@link ServicesAndBundlesService#listDetailedBundleCategories} method. */
    public static final String PERMISSION_SERVICESANDBUNDLES_READ_LISTDETAILEDBUNDLECATEGORIES = "app.crm.api.ServicesAndBundles.read.listDetailedBundleCategories";

    /** Permission for the {@link ServicesAndBundlesService#listDetailedBundleCategoryRatePlanAssociations} method. */
    public static final String PERMISSION_SERVICESANDBUNDLES_READ_LISTDETAILEDBUNDLECATEGORYRATEPLANASSOCIATIONS = "app.crm.api.ServicesAndBundles.read.listDetailedBundleCategoryRatePlanAssociations";

    /** Permission for the {@link ServicesAndBundlesService#listDetailedBundles} method. */
    public static final String PERMISSION_SERVICESANDBUNDLES_READ_LISTDETAILEDBUNDLES = "app.crm.api.ServicesAndBundles.read.listDetailedBundles";

    /** Permission for the {@link ServicesAndBundlesService#listDetailedServices} method. */
    public static final String PERMISSION_SERVICESANDBUNDLES_READ_LISTDETAILEDSERVICES = "app.crm.api.ServicesAndBundles.read.listDetailedServices";

    /** Permission for the {@link ServicesAndBundlesService#listPricePlanDependencyGroups} method. */
    public static final String PERMISSION_SERVICESANDBUNDLES_READ_LISTPRICEPLANDEPENDENCYGROUPS = "app.crm.api.ServicesAndBundles.read.listPricePlanDependencyGroups";

    /** Permission for the {@link ServicesAndBundlesService#listPricePlanPrerequisiteGroups} method. */
    public static final String PERMISSION_SERVICESANDBUNDLES_READ_LISTPRICEPLANPREREQUISITEGROUPS = "app.crm.api.ServicesAndBundles.read.listPricePlanPrerequisiteGroups";

    /** Permission for the {@link ServicesAndBundlesService#listPricePlanValidationGroups} method. */
    public static final String PERMISSION_SERVICESANDBUNDLES_READ_LISTPRICEPLANVALIDATIONGROUPS = "app.crm.api.ServicesAndBundles.read.listPricePlanValidationGroups";

    /** Permission for the {@link ServicesAndBundlesService#listServices} method. */
    public static final String PERMISSION_SERVICESANDBUNDLES_READ_LISTSERVICES = "app.crm.api.ServicesAndBundles.read.listServices";

    /** Permission for the {@link ServicesAndBundlesService#createAuxiliaryService} method. */
    public static final String PERMISSION_SERVICESANDBUNDLES_WRITE_CREATEAUXILIARYSERVICE = "app.crm.api.ServicesAndBundles.write.createAuxiliaryService";

    /** Permission for the {@link ServicesAndBundlesService#createBundle} method. */
    public static final String PERMISSION_SERVICESANDBUNDLES_WRITE_CREATEBUNDLE = "app.crm.api.ServicesAndBundles.write.createBundle";

    /** Permission for the {@link ServicesAndBundlesService#createBundleCategory} method. */
    public static final String PERMISSION_SERVICESANDBUNDLES_WRITE_CREATEBUNDLECATEGORY = "app.crm.api.ServicesAndBundles.write.createBundleCategory";

    /** Permission for the {@link ServicesAndBundlesService#createPricePlan} method. */
    public static final String PERMISSION_SERVICESANDBUNDLES_WRITE_CREATEPRICEPLAN = "app.crm.api.ServicesAndBundles.write.createPricePlan";

    /** Permission for the {@link ServicesAndBundlesService#createPricePlanDependencyGroup} method. */
    public static final String PERMISSION_SERVICESANDBUNDLES_WRITE_CREATEPRICEPLANDEPENDENCYGROUP = "app.crm.api.ServicesAndBundles.write.createPricePlanDependencyGroup";

    /** Permission for the {@link ServicesAndBundlesService#createPricePlanPrerequisiteGroup} method. */
    public static final String PERMISSION_SERVICESANDBUNDLES_WRITE_CREATEPRICEPLANPREREQUISITEGROUP = "app.crm.api.ServicesAndBundles.write.createPricePlanPrerequisiteGroup";

    /** Permission for the {@link ServicesAndBundlesService#createPricePlanValidationGroup} method. */
    public static final String PERMISSION_SERVICESANDBUNDLES_WRITE_CREATEPRICEPLANVALIDATIONGROUP = "app.crm.api.ServicesAndBundles.write.createPricePlanValidationGroup";

    /** Permission for the {@link ServicesAndBundlesService#createPricePlanVersion} method. */
    public static final String PERMISSION_SERVICESANDBUNDLES_WRITE_CREATEPRICEPLANVERSION = "app.crm.api.ServicesAndBundles.write.createPricePlanVersion";

    /** Permission for the {@link ServicesAndBundlesService#createService} method. */
    public static final String PERMISSION_SERVICESANDBUNDLES_WRITE_CREATESERVICE = "app.crm.api.ServicesAndBundles.write.createService";

    /** Permission for the {@link ServicesAndBundlesService#updateAuxiliaryService} method. */
    public static final String PERMISSION_SERVICESANDBUNDLES_WRITE_UPDATEAUXILIARYSERVICE = "app.crm.api.ServicesAndBundles.write.updateAuxiliaryService";

    /** Permission for the {@link ServicesAndBundlesService#updateBundle} method. */
    public static final String PERMISSION_SERVICESANDBUNDLES_WRITE_UPDATEBUNDLE = "app.crm.api.ServicesAndBundles.write.updateBundle";

    /** Permission for the {@link ServicesAndBundlesService#updateBundleCategory} method. */
    public static final String PERMISSION_SERVICESANDBUNDLES_WRITE_UPDATEBUNDLECATEGORY = "app.crm.api.ServicesAndBundles.write.updateBundleCategory";

    /** Permission for the {@link ServicesAndBundlesService#updateBundleCategoryRatePlanAssociation} method. */
    public static final String PERMISSION_SERVICESANDBUNDLES_WRITE_UPDATEBUNDLECATEGORYRATEPLANASSOCIATION = "app.crm.api.ServicesAndBundles.write.updateBundleCategoryRatePlanAssociation";

    /** Permission for the {@link ServicesAndBundlesService#updatePricePlan} method. */
    public static final String PERMISSION_SERVICESANDBUNDLES_WRITE_UPDATEPRICEPLAN = "app.crm.api.ServicesAndBundles.write.updatePricePlan";

    /** Permission for the {@link ServicesAndBundlesService#updatePricePlanDependencyGroup} method. */
    public static final String PERMISSION_SERVICESANDBUNDLES_WRITE_UPDATEPRICEPLANDEPENDENCYGROUP = "app.crm.api.ServicesAndBundles.write.updatePricePlanDependencyGroup";

    /** Permission for the {@link ServicesAndBundlesService#updatePricePlanPrerequisiteGroup} method. */
    public static final String PERMISSION_SERVICESANDBUNDLES_WRITE_UPDATEPRICEPLANPREREQUISITEGROUP = "app.crm.api.ServicesAndBundles.write.updatePricePlanPrerequisiteGroup";

    /** Permission for the {@link ServicesAndBundlesService#updatePricePlanValidationGroup} method. */
    public static final String PERMISSION_SERVICESANDBUNDLES_WRITE_UPDATEPRICEPLANVALIDATIONGROUP = "app.crm.api.ServicesAndBundles.write.updatePricePlanValidationGroup";

    /** Permission for the {@link ServicesAndBundlesService#updateService} method. */
    public static final String PERMISSION_SERVICESANDBUNDLES_WRITE_UPDATESERVICE = "app.crm.api.ServicesAndBundles.write.updateService";

    /** Permission for the {@link AccountService#createIndividualSubscriber} method. */
    @Deprecated
    public static final String PERMISSION_SUBSCRIBERS_WRITE_CREATEINDIVIDUALSUBSCRIBER = "app.crm.api.Subscribers.write.createIndividualSubscriber";
    public static final String PERMISSION_ACCOUNTS_WRITE_CREATEINDIVIDUALSUBSCRIBER = "app.crm.api.Accounts.write.createIndividualSubscriber";

    /** Permission for the {@link SubscriptionService#createSubscription} method. */
    @Deprecated
    public static final String PERMISSION_SUBSCRIBERS_WRITE_CREATESUBSCRIBER = "app.crm.api.Subscribers.write.createSubscriber";
    public static final String PERMISSION_SUBSCRIBERS_WRITE_CREATESUBSCRIPTION = "app.crm.api.Subscribers.write.createSubscription";

    /** Permission for the {@link SubscriptionService#deleteSubscriber} method. */
    @Deprecated
    public static final String PERMISSION_SUBSCRIBERS_WRITE_DELETESUBSCRIBER = "app.crm.api.Subscribers.write.deleteSubscriber";
    public static final String PERMISSION_SUBSCRIBERS_WRITE_DELETESUBSCRIPTION = "app.crm.api.Subscribers.write.deleteSubscription";

    /** Permission for the {@link SubscriptionService#getSubscriptionBalance} method. */
    @Deprecated
    public static final String PERMISSION_SUBSCRIBERS_READ_GETSUBSCRIBERBALANCE = "app.crm.api.Subscribers.read.getSubscriberBalance";
    public static final String PERMISSION_SUBSCRIBERS_READ_GETSUBSCRIPTIONBALANCE = "app.crm.api.Subscribers.read.getSubscriptionBalance";

    /** Permission for the {@link SubscriptionService#getSubscriptionBundleBalances} method. */
    @Deprecated
    public static final String PERMISSION_SUBSCRIBERS_READ_GETSUBSCRIBERBUNDLEBALANCES = "app.crm.api.Subscribers.read.getSubscriberBundleBalances";
    public static final String PERMISSION_SUBSCRIBERS_READ_GETSUBSCRIPTIONBUNDLEBALANCES = "app.crm.api.Subscribers.read.getSubscriptionBundleBalances";

    /** Permission for the {@link SubscriptionService#getSubscriptionBundleBalances} method. */
    public static final String PERMISSION_SUBSCRIBERS_READ_GETSUBSCRIPTIONPRICEPLANOPTIONS = "app.crm.api.Subscribers.read.getSubscriptionPricePlanOptions"; 

    public static final String PERMISSION_SUBSCRIBERS_READ_GETSUBSCRIPTIONPROMOTIONSTATUS = "app.crm.api.Subscribers.read.getSubscriptionPromotionStatus"; 
    
    /** Permission for the {@link SubscriptionService#getDeviceType} method. */
    public static final String PERMISSION_SUBSCRIBERS_READ_GETSUBSCRIPTIONDEVICETYPE = "app.crm.api.Subscribers.read.getSubscriptionDeviceType"; 

    /** Permission for the {@link SubscriptionService#getSubscriptionExtension} method. */
    public static final String PERMISSION_SUBSCRIBERS_READ_GETSUBSCRIPTIONEXTENSION = "app.crm.api.Subscribers.read.getSubscriptionExtension";

    /** Permission for the {@link SubscriptionService#listDetailedSubscriptionExtensions} method. */
    public static final String PERMISSION_SUBSCRIBERS_READ_LISTDETAILEDSUBSCRIPTIONEXTENSIONS = "app.crm.api.Subscribers.read.listDetailedSubscriptionExtensions";

    /** Permission for the {@link SubscriptionService#listSubscriptionExtensions} method. */
    public static final String PERMISSION_SUBSCRIBERS_READ_LISTSUBSCRIPTIONEXTENSIONS = "app.crm.api.Subscribers.read.listSubscriptionExtensions";
    
    /** Permission for the {@link SubscriptionService#getSubscriptionCreationTemplate} method. */
    @Deprecated
    public static final String PERMISSION_SUBSCRIBERS_READ_GETSUBSCRIBERCREATIONTEMPLATE = "app.crm.api.Subscribers.read.getSubscriberCreationTemplate";
    public static final String PERMISSION_SUBSCRIBERS_READ_GETSUBSCRIPTIONCREATIONTEMPLATE = "app.crm.api.Subscribers.read.getSubscriptionCreationTemplate";

    /** Permission for the {@link SubscriptionService#getSubscriptionProfile} method. */
    @Deprecated
    public static final String PERMISSION_SUBSCRIBERS_READ_GETSUBSCRIBERPROFILE = "app.crm.api.Subscribers.read.getSubscriberProfile";
    public static final String PERMISSION_SUBSCRIBERS_READ_GETSUBSCRIPTIONPROFILE = "app.crm.api.Subscribers.read.getSubscriptionProfile";

    /** Permission for the {@link SubscriptionService#listBillingLanguages} method. */
    public static final String PERMISSION_SUBSCRIBERS_READ_LISTBILLINGLANGUAGES = "app.crm.api.Subscribers.read.listBillingLanguages";

    /** Permission for the {@link SubscriptionService#listSubscriptionClasses} method. */
    public static final String PERMISSION_SUBSCRIBERS_READ_LISTSUBSCRIPTIONCLASSES = "app.crm.api.Subscribers.read.listSubscriptionClasses";

    /** Permission for the {@link SubscriptionService#listSubscriptionLevels} method. */
    public static final String PERMISSION_SUBSCRIBERS_READ_LISTSUBSCRIPTIONLEVELS = "app.crm.api.Subscribers.read.listSubscriptionLevels";

    /** Permission for the {@link SubscriptionService#listSubscriptionTypes} method. */
    public static final String PERMISSION_SUBSCRIBERS_READ_LISTSUBSCRIPTIONTYPES = "app.crm.api.Subscribers.read.listSubscriptionTypes";

    /** Permission for the {@link SubscriptionService#listReasonCodes} method. */
    public static final String PERMISSION_SUBSCRIBERS_READ_LISTREASONCODES = "app.crm.api.Subscribers.read.listReasonCodes";

    /** Permission for the {@link SubscriptionService#listSubscriptionCreationTemplates} method. */
    @Deprecated
    public static final String PERMISSION_SUBSCRIBERS_READ_LISTSUBSCRIBERCREATIONTEMPLATES = "app.crm.api.Subscribers.read.listSubscriberCreationTemplates";
    public static final String PERMISSION_SUBSCRIBERS_READ_LISTSUBSCRIPTIONCREATIONTEMPLATES = "app.crm.api.Subscribers.read.listSubscriptionCreationTemplates";

    /** Permission for the {@link SubscriptionService#reactivateSubscription} method. */
    @Deprecated
    public static final String PERMISSION_SUBSCRIBERS_WRITE_REACTIVATESUBSCRIBER = "app.crm.api.Subscribers.write.reactivateSubscriber";
    public static final String PERMISSION_SUBSCRIBERS_WRITE_REACTIVATESUBSCRIPTION = "app.crm.api.Subscribers.write.reactivateSubscription";

    /** Permission for the {@link SubscriptionService#resetVoicemailPassword} method. */
    public static final String PERMISSION_SUBSCRIBERS_WRITE_RESETVOICEMAILPASSWORD = "app.crm.api.Subscribers.write.resetVoicemailPassword";
    
    /** Permission for the {@link SubscriptionService#updateSubscriptionAccount} method. */
    @Deprecated
    public static final String PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIBERACCOUNT = "app.crm.api.Subscribers.write.updateSubscriberAccount";
    public static final String PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIPTIONACCOUNT = "app.crm.api.Subscribers.write.updateSubscriptionAccount";

    /** Permission for the {@link SubscriptionService#updateSubscriptionAdditionalMobileNumber} method. */
    @Deprecated
    public static final String PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIBERADDITIONALMOBILENUMBER = "app.crm.api.Subscribers.write.updateSubscriberAdditionalMobileNumber";
    public static final String PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIPTIONADDITIONALMOBILENUMBER = "app.crm.api.Subscribers.write.updateSubscriptionAdditionalMobileNumber";

    /** Permission for the {@link SubscriptionService#updateSubscriptionCardPackage} method. */
    @Deprecated
    public static final String PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIBERCARDPACKAGE = "app.crm.api.Subscribers.write.updateSubscriberCardPackage";
    public static final String PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIPTIONCARDPACKAGE = "app.crm.api.Subscribers.write.updateSubscriptionCardPackage";

    /** Permission for the {@link SubscriptionService#updateSubscriptionCreditLimit} method. */
    @Deprecated
    public static final String PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIBERCREDITLIMIT = "app.crm.api.Subscribers.write.updateSubscriberCreditLimit";
    public static final String PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIPTIONCREDITLIMIT = "app.crm.api.Subscribers.write.updateSubscriptionCreditLimit";

    /** Permission for the {@link SubscriptionService#updateSubscriptionResetMonthlySpendAmount} method. */
    public static final String PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIPTIONRESETMONTHLYSPENDAMOUNT = "app.crm.api.Subscribers.write.updateSubscriptionResetMonthlySpendAmount";

    /** Permission for the {@link SubscriptionService#updateSubscriptionResetPoolQuotaUsage} method. */
    public static final String PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIPTIONRESETPOOLQUOTAUSAGE = "app.crm.api.Subscribers.write.updateSubscriptionResetPoolQuotaUsage";

    /** Permission for the {@link SubscriptionService#updateSubscriptionDisablePricePlanOption} method. */
    @Deprecated
    public static final String PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIBERDISABLEPRICEPLANOPTION = "app.crm.api.Subscribers.write.updateSubscriberDisablePricePlanOption";
    public static final String PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIPTIONDISABLEPRICEPLANOPTION = "app.crm.api.Subscribers.write.updateSubscriptionDisablePricePlanOption";

    /** Permission for the {@link SubscriptionService#updateSubscriptionEnablePricePlanOption} method. */
    @Deprecated
    public static final String PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIBERENABLEPRICEPLANOPTION = "app.crm.api.Subscribers.write.updateSubscriberEnablePricePlanOption";
    public static final String PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIPTIONENABLEPRICEPLANOPTION = "app.crm.api.Subscribers.write.updateSubscriptionEnablePricePlanOption";

    /** Permission for the {@link SubscriptionService#updateSubscriptionMobileNumber} method. */
    @Deprecated
    public static final String PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIBERMOBILENUMBER = "app.crm.api.Subscribers.write.updateSubscriberMobileNumber";
    public static final String PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIPTIONMOBILENUMBER = "app.crm.api.Subscribers.write.updateSubscriptionMobileNumber";

    /** Permission for the {@link SubscriptionService#updateSubscriptionPaidType} method. */
    @Deprecated
    public static final String PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIBERPAIDTYPE = "app.crm.api.Subscribers.write.updateSubscriberPaidType";
    public static final String PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIPTIONPAIDTYPE = "app.crm.api.Subscribers.write.updateSubscriptionPaidType";

    /** Permission for the {@link SubscriptionService#updateSubscriptionPrimaryPricePlan} method. */
    @Deprecated
    public static final String PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIBERPRIMARYPRICEPLAN = "app.crm.api.Subscribers.write.updateSubscriberPrimaryPricePlan";
    public static final String PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIPTIONPRIMARYPRICEPLAN = "app.crm.api.Subscribers.write.updateSubscriptionPrimaryPricePlan";

    /** Permission for the {@link SubscriptionService#updateSubscriptionProfile} method. */
    @Deprecated
    public static final String PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIBERPROFILE = "app.crm.api.Subscribers.write.updateSubscriberProfile";
    public static final String PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIPTIONPROFILE = "app.crm.api.Subscribers.write.updateSubscriptionProfile";

    public static final String PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIPTIONPRICEPLANOPTIONS = "app.crm.api.Subscribers.write.updateSubscriptionPricePlanOptions";

    /** Permission for the {@link SubscriptionService#updateSubscriptionRechargeVoucher} method. */
    @Deprecated
    public static final String PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIBERRECHARGEVOUCHER = "app.crm.api.Subscribers.write.updateSubscriberRechargeVoucher";
    public static final String PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIPTIONRECHARGEVOUCHER = "app.crm.api.Subscribers.write.updateSubscriptionRechargeVoucher";

    /** Permission for the {@link SubscriptionService#updateSubscriptionRechargeVoucherBatch} method. */
    @Deprecated
    public static final String PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIBERRECHARGEVOUCHERBATCH = "app.crm.api.Subscribers.write.updateSubscriberRechargeVoucherBatch";
    public static final String PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIPTIONRECHARGEVOUCHERBATCH = "app.crm.api.Subscribers.write.updateSubscriptionRechargeVoucherBatch";

    /** Permission for the {@link SubscriptionService#updateSubscriptionSecondaryPricePlan} method. */
    @Deprecated
    public static final String PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIBERSECONDARYPRICEPLAN = "app.crm.api.Subscribers.write.updateSubscriberSecondaryPricePlan";
    public static final String PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIPTIONSECONDARYPRICEPLAN = "app.crm.api.Subscribers.write.updateSubscriptionSecondaryPricePlan";

    /** Permission for the {@link SubscriptionService#updateSubscriptionState} method. */
    @Deprecated
    public static final String PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIBERSTATE = "app.crm.api.Subscribers.write.updateSubscriberState";
    public static final String PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIPTIONSTATE = "app.crm.api.Subscribers.write.updateSubscriptionState";

    /** Permission for the {@link SubscriptionService#updateSubscriptionWithStateTransition} method. */
    public static final String PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIPTIONWITHSTATETRANSITION = "app.crm.api.Subscribers.write.updateSubscriptionWithStateTransition";
    
    public static final String PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIPTIONCONVERTPAIDTYPETOPREPAID = "app.crm.api.Subscribers.write.updateSubscriptionConvertPaidTypeToPrepaid";

    public static final String PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIPTIONCONVERTPAIDTYPETOPOSTPAID = "app.crm.api.Subscribers.write.updateSubscriptionConvertPaidTypeToPostpaid";

    /** Permission for the {@link SubscriptionService#updateSubscriptionTechnologyType} method. */
    @Deprecated
    public static final String PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIBERTECHNOLOGYTYPE = "app.crm.api.Subscribers.write.updateSubscriberTechnologyType";
    public static final String PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIPTIONTECHNOLOGYTYPE = "app.crm.api.Subscribers.write.updateSubscriptionTechnologyType";

    /** Permission for the {@link SubscriptionService#updateSubscriptionAddExtension} method. */
    public static final String PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIPTIONADDEXTENSION = "app.crm.api.Subscribers.write.updateSubscriptionAddExtension";

    /** Permission for the {@link SubscriptionService#updateSubscriptionExtension} method. */
    public static final String PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIPTIONEXTENSION = "app.crm.api.Subscribers.write.updateSubscriptionExtension";

    /** Permission for the {@link SubscriptionService#updateSubscriptionRemoveExtension} method. */
    public static final String PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIPTIONREMOVEEXTENSION = "app.crm.api.Subscribers.write.updateSubscriptionRemoveExtension";
    
    /** Permission for the {@link SubscriptionService#createBundleAdjustments} method. */
    public static final String PERMISSION_SUBSCRIBERS_WRITE_CREATEBUNDLEADJUSTMENTS = "app.crm.api.Subscribers.write.createBundleAdjustments";

    /** Permission for the {@link TransactionService#createAccountTransaction} method. */
    public static final String PERMISSION_TRANSACTIONS_WRITE_CREATEACCOUNTTRANSACTION = "app.crm.api.Transactions.write.createAccountTransaction";

    /** Permission for the {@link TransactionService#createSubscriptionTransaction} method. */
    @Deprecated
    public static final String PERMISSION_TRANSACTIONS_WRITE_CREATESUBSCRIBERTRANSACTION = "app.crm.api.Transactions.write.createSubscriberTransaction";
    public static final String PERMISSION_TRANSACTIONS_WRITE_CREATESUBSCRIPTIONTRANSACTION = "app.crm.api.Transactions.write.createSubscriptionTransaction";

    /** Permission for the {@link TransactionService#getAdjustmentType} method. */
    public static final String PERMISSION_TRANSACTIONS_READ_GETADJUSTMENTTYPE = "app.crm.api.Transactions.read.getAdjustmentType";

    /** Permission for the {@link TransactionService#listDetailedAccountTransactions} method. */
    @Deprecated
    public static final String PERMISSION_TRANSACTIONS_READ_GETALLACCOUNTTRANSACTIONS = "app.crm.api.Transactions.read.getAllAccountTransactions";
    public static final String PERMISSION_TRANSACTIONS_READ_LISTDETAILEDACCOUNTTRANSACTIONS = "app.crm.api.Transactions.read.listDetailedAccountTransactions";

    /** Permission for the {@link TransactionService#listDetailedSubscriptionTransactions} method. */
    @Deprecated
    public static final String PERMISSION_TRANSACTIONS_READ_GETALLSUBSCRIBERTRANSACTIONS = "app.crm.api.Transactions.read.getAllSubscriberTransactions";
    public static final String PERMISSION_TRANSACTIONS_READ_LISTDETAILEDSUBSCRIPTIONTRANSACTIONS = "app.crm.api.Transactions.read.listDetailedSubscriptionTransactions";

    /** Permission for the {@link TransactionService#getTransaction} method. */
    public static final String PERMISSION_TRANSACTIONS_READ_GETTRANSACTION = "app.crm.api.Transactions.read.getTransaction";

    /** Permission for the {@link TransactionService#listAccountTransactions} method. */
    public static final String PERMISSION_TRANSACTIONS_READ_LISTACCOUNTTRANSACTIONS = "app.crm.api.Transactions.read.listAccountTransactions";

    /** Permission for the {@link TransactionService#listAdjustmentTypes} method. */
    public static final String PERMISSION_TRANSACTIONS_READ_LISTADJUSTMENTTYPES = "app.crm.api.Transactions.read.listAdjustmentTypes";

    /** Permission for the {@link TransactionService#listGLCodes} method. */
    public static final String PERMISSION_TRANSACTIONS_READ_LISTGLCODES = "app.crm.api.Transactions.read.listGLCodes";

    /** Permission for the {@link TransactionService#listPaymentAgents} method. */
    public static final String PERMISSION_TRANSACTIONS_READ_LISTPAYMENTAGENTS = "app.crm.api.Transactions.read.listPaymentAgents";

    /** Permission for the {@link TransactionService#listSubscriptionTransactions} method. */
    @Deprecated
    public static final String PERMISSION_TRANSACTIONS_READ_LISTSUBSCRIBERTRANSACTIONS = "app.crm.api.Transactions.read.listSubscriberTransactions";
    public static final String PERMISSION_TRANSACTIONS_READ_LISTSUBSCRIPTIONTRANSACTIONS = "app.crm.api.Transactions.read.listSubscriptionTransactions";
    
    /** Permission for the {@link TransactionService#listTransactionsByExternalTransactionNumber} method. */
    public static final String PERMISSION_TRANSACTIONS_READ_LISTTRANSACTIONSBYEXTERNALTRANSACTIONNUMBER = "app.crm.api.Transactions.read.listTransactionsByExternalTransactionNumber";

    /** Permission for the {@link TransactionService#listTransactionMethods} method. */
    public static final String PERMISSION_TRANSACTIONS_READ_LISTTRANSACTIONMETHODS = "app.crm.api.Transactions.read.listTransactionMethods";
    
    /** Permission for the {@link Loyalty#createLoyaltyProfileAssociation} method. */
    public static final String PERMISSION_LOYALTY_WRITE_CREATELOYALTYPROFILEASSOCIATION = "app.crm.api.Loyalty.write.createLoyaltyProfileAssociation";

    /** Permission for the {@link Loyalty#updateLoyaltyProfileAssociation} method. */
    public static final String PERMISSION_LOYALTY_WRITE_UPDATELOYALTYPROFILEASSOCIATION = "app.crm.api.Loyalty.write.updateLoyaltyProfileAssociation";

    /** Permission for the {@link Loyalty#updateLoyaltyProfileBalance} method. */
    public static final String PERMISSION_LOYALTY_WRITE_UPDATELOYALTYPROFILEBALANCE = "app.crm.api.Loyalty.write.updateLoyaltyProfileBalance";
    
    /** Permission for the {@link Loyalty#convertLoyaltyPoints} method. */
    public static final String PERMISSION_LOYALTY_WRITE_CONVERTLOYALTYPOINTS = "app.crm.api.Loyalty.write.convertLoyaltyPoints";

    /** Permission for the {@link Loyalty#getLoyaltyProfileAssociation} method. */
    public static final String PERMISSION_LOYALTY_READ_GETLOYALTYPROFILEASSOCIATION = "app.crm.api.Loyalty.read.getLoyaltyProfileAssociation";

    /** Permission for the {@link Loyalty#queryLoyaltyProfileBalance} method. */
    public static final String PERMISSION_LOYALTY_READ_QUERYLOYALTYPROFILEBALANCE = "app.crm.api.Loyalty.read.queryLoyaltyProfileBalance";
    
    public static final String GENERICPARAMETER_XMLPROVSVCTYPE = "XMLProvSvcType";
    
    public static final String GENERICPARAMETER_VMPLANID = "VMPlanID";
    
    public static final String GENERICPARAMETER_TAXAUTHORITY = "TaxAuthority";
    
    public static final String GENERICPARAMETER_SPGSERVICETYPE = "SPGSErviceType";

    public static final String GENERICPARAMETER_ALLOWCARRYOVER = "AllowCarryOver";

    public static final String GENERICPARAMETER_PREPAIDPROVISIONHLRCOMMAND = "PrepaidProvisionHLRCommand";

    public static final String GENERICPARAMETER_PREPAIDUNPROVISIONHLRCOMMAND = "PrepaidUnprovisionHLRCommand";

    public static final String GENERICPARAMETER_PREPAIDSUSPENDHLRCOMMAND = "PrepaidSuspendHLRCommand";

    public static final String GENERICPARAMETER_PREPAIDRESUMEHLRCOMMAND = "PrepaidResumeHLRCommand";

    public static final String GENERICPARAMETER_POSTPAIDPROVISIONHLRCOMMAND = "PostpaidProvisionHLRCommand";

    public static final String GENERICPARAMETER_POSTPAIDUNPROVISIONHLRCOMMAND = "PostpaidUnprovisionHLRCommand";

    public static final String GENERICPARAMETER_POSTPAIDSUSPENDHLRCOMMAND = "PostpaidSuspendHLRCommand";

    public static final String GENERICPARAMETER_POSTPAIDRESUMEHLRCOMMAND = "PostpaidResumeHLRCommand";

    public static final String GENERICPARAMETER_ADJUSTMENTTYPEDESCRIPTION = "AdjustmentTypeDescription";

    public static final String GENERICPARAMETER_GLCODE = "GLCode";

    public static final String GENERICPARAMETER_INVOICEDESCRIPTION = "InvoiceDescription";

    public static final String GENERICPARAMETER_PPSMADJUSTMENTTYPEDESCRIPTION = "PPSMAdjustmentTypeDescription";

    public static final String GENERICPARAMETER_PPSMGLCODE = "PPSMGLCode";

    public static final String GENERICPARAMETER_PPSMINVOICEDESCRIPTION = "PPSMInvoiceDescription";
    
    public static final String GENERICPARAMETER_RECURRINGSTARTVALIDITY = "RecurringStartValidity";

    public static final String GENERICPARAMETER_RECURRINGSTARTINTERVAL = "RecurringStartInterval";

    public static final String GENERICPARAMETER_RECURRINGSTARTHOUR = "RecurringStartHour";

    public static final String GENERICPARAMETER_RECURRINGSTARTMINUTES = "RecurringStartMinutes";

    public static final String GENERICPARAMETER_RELATIVESTARTVALIDITY = "RelativeStartValidity";

    public static final String GENERICPARAMETER_RELATIVESTARTINTERVAL = "RelativeStartInterval";

    public static final String GENERICPARAMETER_RELATIVESTARTHOUR = "RelativeStartHour";

    public static final String GENERICPARAMETER_RELATIVESTARTMINUTES = "RelativeStartMinutes";
    
    public static final String GENERICPARAMETER_BEARERTYPE = "BearerType";

    public static final String GENERICPARAMETER_PROVISION_TO_ECP = "ProvisionToECP";

    public static final String GENERICPARAMETER_VPNPRICEPLAN = "VPNPricePlan";

    public static final String GENERICPARAMETER_RBTID = "RBTId";
   
    public static final String GENERICPARAMETER_WARNINGONSUSPENDDISABLE = "WarningOnSuspendDisable";

    public static final String GENERICPARAMETER_PRIVATECUGPREPAIDSERVICECHARGE = "PrivateCUGPrepaidServiceCharge";

    public static final String GENERICPARAMETER_PRIVATECUGPOSTPAIDSERVICECHARGE = "PrivateCUGPostpaidServiceCharge";

    public static final String GENERICPARAMETER_PRIVATECUGEXTERNALSERVICECHARGE = "PrivateCUGExternalServiceCharge";

    public static final String GENERICPARAMETER_ENABLEDISCOUNTTHRESHOLD = "EnableDiscountThreshold";

    public static final String GENERICPARAMETER_MINIMUMDISCOUNTTOTALCHARGETHREASHOLD = "MinimumDiscountTotalChargeThreashold";

    public static final String GENERICPARAMETER_DISCOUNTPERCENTAGE = "DiscountPercentage";

    public static final String GENERICPARAMETER_VPNGROUPCHARGE = "VPNGroupCharge";

    public static final String GENERICPARAMETER_VPNGROUPADJUSTMENTTYPEID = "VPNGroupAdjustmentTypeID";

    public static final String GENERICPARAMETER_VPNADJUSTMENTTYPEDESCRIPTION = "VPNAdjustmentTypeDescription";

    public static final String GENERICPARAMETER_VPNGLCODE = "VPNGLCode";

    public static final String GENERICPARAMETER_VPNINVOICEDESCRIPION = "VPNInvoiceDescripion";

    public static final String GENERICPARAMETER_VPNTAXAUTHORITY = "VPNTaxAuthority";
    
    public static final String GENERICPARAMETER_STARTDATE = "StartDate";
    
    public static final String GENERICPARAMETER_ENDDATE = "EndDate";
    
    public static final String GENERICPARAMETER_ACTIVATIONFEETYPE = "ActivationFeeType";
    
    public static final String GENERICPARAMETER_SMARTSUSPENSIONENABLED = "SmartSuspensionEnabled";
    
    public static final String GENERICPARAMETER_SERVICECHARGE = "ServiceCharge";
    
    public static final String GENERICPARAMETER_RBTUSER = "RBTUser";
    
    public static final String GENERICPARAMETER_VOICEMAILPLANID = "VoicemailPlanID";
    
    public static final String GENERICPARAMETER_OLDBAN = "OldBAN";
    
    public static final String GENERICPARAMETER_NEWBAN = "NewBAN";
    
    public static final String GENERICPARAMETER_OLDPARENTBAN = "OldParentBAN";
    
    public static final String GENERICPARAMETER_NEWPARENTBAN = "NewParentBAN";
    
    public static final String GENERICPARAMETER_OLDRESPONSIBLEBAN = "OldResponsibleBAN";
    
    public static final String GENERICPARAMETER_NEWRESPONSIBLEBAN = "NewResponsibleBAN";
    
    public static final String GENERICPARAMETER_NUMOFACCOUNTSELIGIBLEFORMOVE = "NumOfAccountsEligibleForMove"; 

    /**
     * To instruct marking the bundle as 'Repurchasable'
     */
    public static final String GENERICPARAMETER_REPURCHASABLE = "Repurchaseable";
    
    /**
     * Expiry Extension to be set in case of a 'Repurchasable' bundle
     */
    public static final String GENERICPARAMETER_REPURCHASE_EXPIRY_EXTENSION = "RepurchaseExpiryExtension";
    
	 public static final String GENERICPARAMETER_BUNDLE_EXECUTION_ORDER = "ExecutionOrder";
    public static final String GENERICPARAMETER_BUNDLEID="bundleId";
    public static final String GENERICPARAMETER_BUNDLENAME="bundleName";
    public static final String GENERICPARAMETER_BUNDLETYPE="bundleType";
    public static final String GENERICPARAMETER_AUXILIARYBUNDLES="AuxiliaryBundles";
    

    /**
     * Non-zero means a failure. This is usually the transaction error code,
     * in case of transactions. For clients, just a zero or non-zero would
     * be significant.
     */
    public static final String API_ERROR_CODE = "ApiErrorCode";
    public static final String API_ERROR_MESSAGE = "ApiErrorMessage";
    /**
     * A value within the ENUM: {@link ApiErrorSource}, represented by String
     */
    public static final String API_ERROR_SOURCE = "ApiErrorSource";
    /**
     * Thrown by the {@link #API_ERROR_SOURCE}
     */
    public static final String API_INTERNAL_ERROR_CODE = "ApiInternalErrorCode";
    
    public static final String EXTERNAL_SERVICE_CODE = "ExternalServiceCode";

    public static final String NON_STANDARD_RENEWAL = "NonStandardRenewal";
    
    public static final String CONTRACT_ACTIVE = "ACTIVE";
    
    public static final String CONTRACT_INACTIVE = "INACTIVE";
    
    public static final String CONTRACT_EXPIRED = "EXPIRED";
    
    public static final String SERVICE_RECOGNIZED_DATE="ServiceRecognizedDate";
    
    public static final String TRANSACTION_CREATION_DATE="TransactionCreationDate";
    
    
}
