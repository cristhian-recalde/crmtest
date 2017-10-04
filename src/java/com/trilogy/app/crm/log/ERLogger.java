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
package com.trilogy.app.crm.log;

import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.er.ERSupport;
import com.trilogy.framework.xlog.log.ERLogMsg;
import com.trilogy.framework.xlog.log.EntryLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.app.crm.bas.recharge.RechargeVisitorCountable;
import com.trilogy.app.crm.bas.recharge.RecurRechargeRequest;
import com.trilogy.app.crm.bas.tps.TPSRecord;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.AdjustmentInfo;
import com.trilogy.app.crm.bean.AutoDepositReleaseCriteria;
import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CreditCategory;
import com.trilogy.app.crm.bean.DealerCode;
import com.trilogy.app.crm.bean.ReleaseScheduleConfigurationEnum;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.ui.TechnicalServiceTemplate;
import com.trilogy.app.crm.bean.TopUpSchedule;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.bean.calldetail.CallDetail;
import com.trilogy.app.crm.bean.core.AdjustmentType;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bean.core.Msisdn;
import com.trilogy.app.crm.bean.core.PricePlan;
import com.trilogy.app.crm.bean.core.PricePlanVersion;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bean.payment.PaymentException;
import com.trilogy.app.crm.bean.paymentgatewayintegration.SubscriptionSupport;
import com.trilogy.app.crm.bean.service.ServiceStateEnum;
import com.trilogy.app.crm.bean.ui.PriceTemplate;
import com.trilogy.app.crm.bundle.service.CRMBundleProfile;
import com.trilogy.app.crm.client.bm.BalanceManagementSupport;
import com.trilogy.app.crm.extension.subscriber.MultiSimRecordHolder;
import com.trilogy.app.crm.extension.subscriber.MultiSimSubExtension;
import com.trilogy.app.crm.extension.subscriber.MultiSimSubExtensionXInfo;
import com.trilogy.app.crm.home.PaymentFailureERHome;
import com.trilogy.app.crm.move.MoveConstants;
import com.trilogy.app.crm.notification.NotificationTypeEnum;
import com.trilogy.app.crm.subscriber.provision.SubscriberProvisionResultCode;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.AuxiliaryServiceSupport;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.MsisdnSupport;
import com.trilogy.app.crm.support.RecurringRechargeSupport;
import com.trilogy.app.crm.support.ServiceSupport;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.app.crm.support.SubscriberServicesSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.support.SystemSupport;
import com.trilogy.app.crm.tps.pipe.TPSPipeConstant;
import com.trilogy.app.crm.transfer.TransferDispute;
import com.trilogy.app.crm.transfer.TransferDisputeStatusEnum;
import com.trilogy.util.collection.trie.Entry;
import com.trilogy.util.crmapi.wsdl.v3_0.api.core.io.common.v1.ExecutionStatus;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.offering.v1.OfferingIO;
import com.trilogy.util.snippet.log.Logger;


/**
 * This class contains methods for sending out ERs. It also contains definition for the
 * general ER fields (ER IDs, descriptions).
 * 
 * @author lanny.tse@redknee.com
 * @author larry.xia@redknee.com
 * @author paul.sperneac@redknee.com
 * @author angie.li@redknee.com
 * @author prasanna.kulkarni@redknee.com
 * @author cindy.wong@redknee.com
 */
public class ERLogger extends CoreERLogger
{
    /*
     * If any of the following COMPILE_CHECK variables don't compile, then it's because ERSupport.xsl is missing from project.xml file
     * (ERSupport gets removed in older versions of XBuild when POM is updated in GUI)
     */
    private static final ERSupport COMPILE_CHECK_DUNNING_ER = DunningActionERERSupport.instance();
    private static final ERSupport COMPILE_CHECK_PRICE_PLAN_CREATION_ER = PricePlanCreationERERSupport.instance();
    private static final ERSupport COMPILE_CHECK_PRICE_PLAN_MODIFICATION_ER = PricePlanModificationERERSupport.instance();
    private static final ERSupport COMPILE_CHECK_SUBSCRIPTION_ACTIVATION_ER = SubscriptionActivationERERSupport.instance();
    private static final ERSupport COMPILE_CHECK_SUBSCRIPTION_MODIFICATION_ER = SubscriptionModificationERERSupport.instance();
    private static final ERSupport COMPILE_CHECK_MULTI_SIM_PROVISIONING_ER = MultiSimProvisioningERERSupport.instance();
    // END COMPILE_CHECK variables

    /** ID of account adjustment ER. */
    protected static final int ACCOUNT_ADJUSTMENT_ERID = 771;
    /** ID of subscriber balance reset ER. */
    protected static final int SUBSCRIBER_BALANCE_RESET_ERID = 772;
    /** ID of subscriber adjustment ER. */
    protected static final int SUB_ADJUSTMENT_ERID = 773;
    /** ID of dropped call reimbursement ER. */
    protected static final int DROPPED_CALL_REIMBURSEMENT_ERID = 780;
    /** ID of TPS invalid file ER. */
    protected static final int TPS_INVALID_FILE_ERID = 787;
    /** ID of TPS invalid entry ER. */
    protected static final int TPS_INVALID_ENTRY_ERID = 788;
    /** ID of TPS invalid account ER. */
    protected static final int TPS_INVALID_ACCOUNT_ERID = 789;
    /** ID of Transfer Dispute ER. */
    protected static final int TRANSFER_DISPUTE_ERID = 792;
    /** ID of TPS multiple subscribers ER. */
    protected static final int TPS_MULTIPLE_SUBSCRIBERS_ERID = 1124;
    /** ID of TPS standard payment account ER. */
    protected static final int TPS_STD_PAYMENT_ACCOUNT_ERID = 1125;
    /** ID of TPS payment account report ER. */
    protected static final int TPS_PAYMENT_ACCOUNT_REPORT_ERID = 1126;
    /** ID of subscriber credit limit reset ER. */
    protected static final int SUBSCRIBER_CREDIT_LIMIT_RESET_ERID = 790;
    /** ER ID for Account Invoice. */
    protected static final int DEALER_CODE_ERID = 1106;
    /** ER ID for Loyalty Birthday Promotion ER. */
    protected static final int LOYALTY_PROMOTION_ERID = 1108;
    /** ER ID for PTP reset ER. */
    protected static final int PTPRESET_ERID = 1107;

    /** ER ID for Payment Plan Activation/Deactivation */
    protected static final int PAYMENT_PLAN_ACTIVATION_ERID = 1109;
    /** ER ID for Payment Plan Payment */
    protected static final int PAYMENT_PLAN_PAYMENT_ERID = 1110;
    /** ER ID for TPS reconciliation ER. */
    protected static final int TPS_RECONCILIATION_ERID = 1111;

    /**
     * ER ID 1151
     */
    public static final int SIMCARD_HLR_LOADER_ERID=1151;
    
    /**
     * ER 1149 ID 
     */
     public static final int HLR_COMMAND_INTERACTION_ERID=1149; 
     
     /**
      * ER ID 1162 for Individual to group account conversion
      */
     public static final int CONVERT_ACCOUNT_TO_GROUP_ERID=1162;
    
    /**
     * ER ID for Auto Deposit Release Criteria creation.
     */
    protected static final int AUTO_DEPOSIT_RELEASE_CRITERIA_CREATE_ERID = 1113;

    /**
     * ER ID for Auto Deposit Release Criteria modification.
     */
    protected static final int AUTO_DEPOSIT_RELEASE_CRITERIA_UPDATE_ERID = 1114;

    /**
     * ER ID for Auto Deposit Release Criteria deletion.
     */
    protected static final int AUTO_DEPOSIT_RELEASE_CRITERIA_DELETE_ERID = 1115;

    /**
     * ER ID for recurring charge summary.
     */
    protected static final int RECHARGE_COUNT_ERID = 1127;
    /** ID of subscriber move ER. */
    protected static final int SUBSCRIBER_CHANGE_ACCOUNT_ERID = 768;
    /** ID of recurring recharge ER. */
    protected static final int RECURRING_RECHARGE_ERID = 770;

    /** ID of TECHNICAL SERVICE TEMPLATE Publish ER. */
    protected static final int TECHNICAL_SERVICE_TEMPLATE_ERID = 1116;
    
    /** ID of prepareOffer OR create create product in BSS  ER. */
    protected static final int PREPARE_OFFER_ERID = 1117;
    
    protected static final String PREPARE_OFFER = "Prepare Offer";
    
    /** ID of Voucher Notification ER*/
    protected static final int NOTIFICATION_ERID = 3800;
    
    /** ID of PRICE TEMPLATE Publish ER. */
    protected static final int PRICE_TEMPLATE_ERID = 1118;
    
    protected static final int ATU_REGISTRATION_DEREGISTRATION_ERID = 1191;
    
    /** ID of GenerateAutomaticRefund. */    
    protected static final int GENERATE_AUTOMATIC_REFUND=1172;
    
    /** Description of subscriber move ER. */
    protected static final String SUBSCRIBER_CHANGE_ACCOUNT_NAME = "Subscriber Change Account";
    /** ER description of recurring recharge ER. */
    protected static final String RECURRING_RECHARGE_SERVICE_NAME = "Recurring Charge Event";
    /** ER description of TPS invalid file ER. */
    protected static final String TPS_INVALID_FILE_SERVICE_NAME = "Invalid TPS file Event";
    /** ER description of TPS invalid entry ER. */
    protected static final String TPS_INVALID_ENTRY_SERVICE_NAME = "Invalid TPS Entry Event";
    /** ER description of TPS invalid subscriber service ER. */
    protected static final String TPS_INVALID_SUBSCRIBER_SERVICE_NAME = "Invalid Subscriber/Account number in TPS Event";
    /** ER description of TPS multiple subscribers ER. */
    protected static final String TPS_MULTIPLE_SUBSCRIBERS_SERVICE_NAME = "Standard Payment Exception";
    /** ER description of TPS standard payment ER. */
    protected static final String TPS_STD_PAYMENT_ACCOUNT_SERVICE_NAME = "Standard Payment Account Summary";
    /** ER description of TPS file payment ER. */
    protected static final String TPS_FILE_PAYMENT_ACCOUNT_SERVICE_NAME = "TPS file payment report";
    /** ER description of account adjustment ER. */
    protected static final String ACCOUNT_ADJUSTMENT_SERVICE_NAME = "Account Adjustment Event";
    /** ER description of subscriber balance reset ER. */
    protected static final String SUBSCRIBER_BALANCE_RESET_SERVICE_NAME = "Subscriber Balance Reset Event";
    /** ER description of subscriber credit limit reset ER. */
    protected static final String SUBSCRIBER_CREDIT_LIMIT_RESET_SERVICE_NAME = "Subscriber Credit Limit Reset Event";
    /** ER description of dropped call reimbursement ER. */
    protected static final String DROPPED_CALL_REIMBURSEMENT_SERVICE_NAME = "Dropped Call Reimbursement Event";
    /** ER description of subscriber adjustment ER. */
    protected static final String SUB_ADJUST_SERVICE_NAME = "Subscriber Adjustment Event";
    /** ER description of price plan creation ER. */
    protected static final String PRICE_PLAN_CREATION_NAME = "Price Plan Creation Event";
    /** ER description of dealer code ER. */
    protected static final String DEALER_CODE_SERVICE_NAME = "Dealer Code Action";
    /** ER description of birthday promotion ER. */
    protected static final String BIRTHDAY_PROMOTION_SERVICE_NAME = "Birthday Promotion Eligibility";
    /** ER description of payment plan activation ER. */
    protected static final String PAYMENT_PLAN_ACTIVATION_SERVICE_NAME = "Payment Plan Activation/Deactivation";
    /** ER description of payment plan payment ER. */
    protected static final String PAYMENT_PLAN_PAYMENT_SERVICE_NAME = "Payment Plan Payment (Loan Payment)";
    /** ER description of TPS reconciliation ER. */
    protected static final String TPS_RECONCILIATION_NAME = "TPS Reconciliation Event";
    /** ER description of Account PTP reset. */
    protected static final String PTP_RESET_NAME = "Account Maximum Number of PTP";
    /** ER description of recurring charge summary ER. */
    protected static final String RECHARGE_COUNT_NAME = "Recurring Charge Summary";
    /** ER description of transfer dispute ER */
    protected static final String TRANSFER_DISPUTE_NAME = "Transfer Dispute Event";
    
    /**
     * ER ID for Auto Dunning Policy Assignment.
     */
    protected static final int DUNNING_POLICY_ASSIGNEMENT_ERID = 1170;
    
    /**
     * ER 1150
     */
    public static final String SIMCARD_HLR_LOADER_NAME = "File-based Bulk HLR Provisioning Sumary:1:0";
    /**
     * ER 1149
     */
    public static final String HLR_COMMAND_INTERACTION_Name = "HLR Command Request:1:0"; 

    /**
     * Description of ER 1162
     */
    
    public static final String CONVERT_ACCOUNT_TO_GROUP_ER_NAME = "Individual Account To Group Conversion Event";
    /**
     * Description of ER 1113.
     */
    protected static final String AUTO_DEPOSIT_RELEASE_CRITERIA_CREATE_NAME = "Auto-deposit Release Criteria (Creation)";

    /**
     * Description of ER 1114.
     */
    protected static final String AUTO_DEPOSIT_RELEASE_CRITERIA_UPDATE_NAME = "Auto-deposit Release Criteria (Modification)";

    /**
     * Description of ER 1115.
     */
    protected static final String AUTO_DEPOSIT_RELEASE_CRITERIA_DELETE_NAME = "Auto-deposit Release Criteria (Deletion)";

    /**
     * Description of ER 1116.
     */
    protected static final String TECHNICAL_SERVICE_TEMPLATE = "Technical service template publish";

    /**
     * Description of ER 3800.
     */
    protected static final String NOTIFICATION_NAME = "Notification";
    
    protected static final String ATU_REGISTRATION_DEREGISTRATION_NAME = "ATU Registration Deregistration";
    
    protected static final String GENERATE_AUTOMATIC_REFUND_NAME = "Generate Automatic Refund Name";
    
    /**
     * Description of ER 1169
     */
    
    public static final String DUNNING_POLICY_ASSIGNMENT_ER_NAME = "Dunning Policy Assignment Event";
    
    
    /**
     * Description of ER 3803.
     */
    
    public static final String SERVICE_STRING ="Service";
    
    public static final String AUX_SERVICE_STRING ="Auxilary Service";
    
    public static final String SERVICE_TYPE_KEY = "Service Type";
    
    public static final String SERVICE_ID_KEY = "Service Id";
    
    public static final String SERVICE_NAME_KEY = "Service Name";
    
    public static final String NEW_STATE_KEY = "New State";
    
    public static final String OLD_STATE_KEY = "Old State";

    public static final String FEE_KEY = "Fee";
    
    public static final String END_DATE_KEY = "End Date";
    
    public static final String VOUCHER_NUMBER_KEY = "Voucher Number";
    
    public static final String VOUCHER_VALUE_KEY = "Voucher Value";
    
    public static final String TRANSFER_AMOUNT = "Amount";
    
    public static final String  ADJUSTMENT_TYPE_KEY = "Adjustment Type";
    
    protected static final String PRICE_TEMPLATE = "Price Template Publish";

    public static final int ATU_REGISTRATION_ACTION = 0;
    public static final int ATU_DEREGISTRATION_ACTION = 1;
    
    /**
     * Creates a new <code>ERLogger</code> instance. This method is made protected to
     * prevent instantiation of utility class.
     */
    protected ERLogger()
    {
        // empty
    }


    /**
     * Sends a recur recharge summary ER.
     * 
     * @param ctx
     *            The operating context.
     * @param delegate
     *            The recurring recharge summary.
     */
    public static void generateRechargeCountEr(final Context ctx, final RechargeVisitorCountable delegate)

    {
        final String[] fields = new String[18];

        fields[0] = String.valueOf(delegate.getAccountCount());
        fields[1] = String.valueOf(delegate.getAccountSuccessCount());
        fields[2] = String.valueOf(delegate.getAccountFailCount());
        fields[3] = String.valueOf(delegate.getSubscriberCount());
        fields[4] = String.valueOf(delegate.getSubscriberSuccessCount());
        fields[5] = String.valueOf(delegate.getSubscriberFailCount());
        fields[6] = String.valueOf(delegate.getServicesCount());
        fields[7] = String.valueOf(delegate.getServicesCountSuccess());
        fields[8] = String.valueOf(delegate.getServicesCountFailed());
        fields[9] = String.valueOf(delegate.getChargeAmount());
        fields[10] = String.valueOf(delegate.getChargeAmountSuccess());
        fields[11] = String.valueOf(delegate.getChargeAmountFailed());
        fields[12] = String.valueOf(delegate.getPackagesCount());
        fields[13] = String.valueOf(delegate.gePackagesSuccessCount());
        fields[14] = String.valueOf(delegate.getPackagesFailedCount());
        fields[15] = String.valueOf(delegate.getBundleCount());
        fields[16] = String.valueOf(delegate.getBundleCountSuccess());
        fields[17] = String.valueOf(delegate.getBundleCountFailed());

        // spid is irrelevant for this ER
        new ERLogMsg(RECHARGE_COUNT_ERID, RECORD_CLASS, RECURRING_RECHARGE_SERVICE_NAME, -1, fields).log(ctx);
    }


    /**
     * Sends a Auto-deposit Release Criteria (Creation) ER.
     * 
     * @param context
     *            The operating context
     * @param criteria
     *            The criteria being created
     */
    public static void generateAutoDepositReleaseCriteriaCreateER(final Context context,
            final AutoDepositReleaseCriteria criteria)
    {
        final String[] fields = new String[8];
        // field 1: criteria ID
        fields[0] = Long.toString(criteria.getIdentifier());
        // field 2: description
        fields[1] = criteria.getDescription();
        // field 3: service duration
        fields[2] = Integer.toString(criteria.getServiceDuration());
        // field 4: deposit release adjustment type
        fields[3] = Integer.toString(criteria.getDepositReleaseAdjustmentType());
        // field 5: deposit release percent
        fields[4] = Double.toString(criteria.getDepositReleasePercent());
        // field 6: release schedule
        fields[5] = Integer.toString(criteria.getReleaseSchedule());
        // field 7: apply to bill cycle
        fields[6] = Boolean.toString(criteria.getReleaseScheduleConfiguration().equals(
                ReleaseScheduleConfigurationEnum.DAY_OF_MONTH));
        // field 8: minimum deposit amount
        fields[7] = Long.toString(criteria.getMinimumDepositReleaseAmount());
        new ERLogMsg(AUTO_DEPOSIT_RELEASE_CRITERIA_CREATE_ERID, RECORD_CLASS,
                AUTO_DEPOSIT_RELEASE_CRITERIA_CREATE_NAME, -1, fields).log(context);
    }


    /**
     * Sends a Auto-deposit Release Criteria (Modification) ER.
     * 
     * @param context
     *            The operating context
     * @param oldCriteria
     *            The criteria before update
     * @param newCriteria
     *            The criteria after update
     */
    public static void generateAutoDepositReleaseCriteriaUpdateER(final Context context,
            final AutoDepositReleaseCriteria oldCriteria, final AutoDepositReleaseCriteria newCriteria)
    {
        final String[] fields = new String[15];
        // field 1: criteria ID
        fields[0] = Long.toString(oldCriteria.getIdentifier());
        // field 2: old description
        fields[1] = oldCriteria.getDescription();
        // field 3: new description
        fields[2] = newCriteria.getDescription();
        // field 4: old service duration
        fields[3] = Integer.toString(oldCriteria.getServiceDuration());
        // field 5: new service duration
        fields[4] = Integer.toString(newCriteria.getServiceDuration());
        // field 6: old adjustment type
        fields[5] = Integer.toString(oldCriteria.getDepositReleaseAdjustmentType());
        // field 7: new adjustment type
        fields[6] = Integer.toString(newCriteria.getDepositReleaseAdjustmentType());
        // field 8: old deposit release percent
        fields[7] = Double.toString(oldCriteria.getDepositReleasePercent());
        // field 9: new deposit release percent
        fields[8] = Double.toString(newCriteria.getDepositReleasePercent());
        // field 10: old release schedule
        fields[9] = Integer.toString(oldCriteria.getReleaseSchedule());
        // field 11: new release schedule
        fields[10] = Integer.toString(newCriteria.getReleaseSchedule());
        // field 12: old apply to bill cycle
        fields[11] = Boolean.toString(oldCriteria.getReleaseScheduleConfiguration().equals(
                ReleaseScheduleConfigurationEnum.DAY_OF_MONTH));
        // field 13: new apply to bill cycle
        fields[12] = Boolean.toString(newCriteria.getReleaseScheduleConfiguration().equals(
                ReleaseScheduleConfigurationEnum.DAY_OF_MONTH));
        // field 14: old minimum deposit amount
        fields[13] = Long.toString(oldCriteria.getMinimumDepositReleaseAmount());
        // field 15: new minimum deposit amount
        fields[14] = Long.toString(newCriteria.getMinimumDepositReleaseAmount());
        new ERLogMsg(AUTO_DEPOSIT_RELEASE_CRITERIA_UPDATE_ERID, RECORD_CLASS,
                AUTO_DEPOSIT_RELEASE_CRITERIA_UPDATE_NAME, -1, fields).log(context);
    }


    /**
     * Sends a Auto-deposit Release Criteria (Deletion) ER.
     * 
     * @param context
     *            The operating context
     * @param criteria
     *            The criteria being created
     */
    public static void generateAutoDepositReleaseCriteriaDeleteER(final Context context,
            final AutoDepositReleaseCriteria criteria)
    {
        final String[] fields = new String[8];
        // field 1: ID
        fields[0] = Long.toString(criteria.getIdentifier());
        // field 2: description
        fields[1] = criteria.getDescription();
        // field 3: service duration
        fields[2] = Integer.toString(criteria.getServiceDuration());
        // field 4: adjustment type
        fields[3] = Integer.toString(criteria.getDepositReleaseAdjustmentType());
        // field 5: deposit release percent
        fields[4] = Double.toString(criteria.getDepositReleasePercent());
        // field 6: release schedule
        fields[5] = Integer.toString(criteria.getReleaseSchedule());
        // field 7: apply to bill cycle
        fields[6] = Boolean.toString(criteria.getReleaseScheduleConfiguration().equals(
                ReleaseScheduleConfigurationEnum.DAY_OF_MONTH));
        // field 8: minimum deposit amount
        fields[7] = Long.toString(criteria.getMinimumDepositReleaseAmount());
        new ERLogMsg(AUTO_DEPOSIT_RELEASE_CRITERIA_DELETE_ERID, RECORD_CLASS,
                AUTO_DEPOSIT_RELEASE_CRITERIA_DELETE_NAME, -1, fields).log(context);
    }


    /**
     * Sends a recur recharge ER.
     * 
     * @param ctx
     *            The operating context.
     * @param request
     *            The recurring recharge request.
     */
    public static void createRecurRechargeEr(final Context ctx, final RecurRechargeRequest request)
    {
        final String[] fields = new String[9];

        fields[0] = request.getSub().getMSISDN();
        fields[1] = request.getSub().getBAN();
        fields[2] = String.valueOf(request.getSub().getPricePlan());
        fields[3] = String.valueOf(request.getServiceFee().getServiceId());
        fields[4] = String.valueOf(request.getServiceFee().getFee());
        if (request.getService() == null)
        {
            fields[5] = "";
        }
        else
        {
            fields[5] = request.getService().getAdjustmentTypeName();
        }

        if (request.getResultAdjustment() != null)
        {
            fields[6] = request.getResultAdjustment().getGLCode();
        }
        else
        {
            fields[6] = "";

        }

        //DZ new OCG field 16/02/04
        fields[7] = "" + request.getOcgResult();

        fields[8] = "" + request.getResult();

        new ERLogMsg(RECURRING_RECHARGE_ERID, RECORD_CLASS, RECURRING_RECHARGE_SERVICE_NAME,
                request.getSub().getSpid(), fields).log(ctx);
    }


    /**
     * Sends a recur recharge ER.
     * 
     * @param ctx
     *            The operating context.
     * @param trans
     *            The recurring charge transaction.
     * @param priceplan
     *            Price plan containing the recurring charge service.
     * @param serviceId
     *            Identifier for the service generating this recurring charge.
     * @param ocgResult
     *            OCG result code.
     * @param result
     *            Result code.
     */
    public static void createRecurRechargeEr(final Context ctx, final Transaction trans, final long priceplan,
            final String serviceId, final int ocgResult, final int result)
    {
        final String[] fields = new String[9]; // DZ: add a OCG result field

        fields[0] = trans.getMSISDN();
        fields[1] = trans.getBAN();
        fields[2] = String.valueOf(priceplan);
        fields[3] = String.valueOf(serviceId);
        fields[4] = String.valueOf(trans.getAmount());
        fields[5] = String.valueOf(trans.getAdjustmentType());
        fields[6] = String.valueOf(trans.getGLCode());
        fields[7] = String.valueOf(ocgResult);
        fields[8] = String.valueOf(result);

        new ERLogMsg(RECURRING_RECHARGE_ERID, RECORD_CLASS, RECURRING_RECHARGE_SERVICE_NAME, trans.getSpid(), fields)
                .log(ctx);
    }


    /**
     * Sends an invalid entry ER.
     * 
     * @param ctx
     *            The operating context.
     */
    public static void genInvalidEntryER(final Context ctx)
    {
        final TPSRecord tps = (TPSRecord) ctx.get(TPSRecord.class);

        final String[] fields = new String[4];

        fields[0] = tps.getTelephoneNum();
        fields[1] = tps.getAccountNum();
        fields[2] = tps.getRawline();
        fields[3] = "" + TPSPipeConstant.RESULT_CODE_GENERAL;

        new ERLogMsg(TPS_INVALID_ENTRY_ERID, RECORD_CLASS, TPS_INVALID_ENTRY_SERVICE_NAME, -1, fields).log(ctx);
    }


    /**
     * Sends an invalid File ER.
     * 
     * @param ctx
     *            THe operating context.
     * @param filename
     *            The name of the invalid file.
     */
    public static void genInvalidFileER(final Context ctx, final String filename)
    {
        final String[] fields = new String[2];

        fields[0] = filename;
        fields[1] = "" + TPSPipeConstant.RESULT_CODE_GENERAL;
        new ERLogMsg(TPS_INVALID_FILE_ERID, RECORD_CLASS, TPS_INVALID_FILE_SERVICE_NAME, -1, fields).log(ctx);

    }


    /**
     * Sends an account adjustment ER for a TPS record.
     * 
     * @param ctx
     *            The operating context.
     * @param tps
     *            The TPS record.
     * @param result
     *            Result code of the adjustment.
     */
    public static void genAccountAdjustmentER(final Context ctx, final TPSRecord tps, final int result)
    {
        final Account acct = (Account) ctx.get(Account.class);

        // If account is null then we set the spid = 0 
        int spid = 0;
        if ( acct != null )
               spid = acct.getSpid();
        
        /*
         * this method should be called only before the TPS record is converted to
         * transaction record
         */
        final int taxPaid = 0;

        final String[] fields = new String[12];

        fields[0] = tps.getAccountNum();

        if (tps.getLocationCode() == null)
        {
            fields[1] = "";
        }
        else
        {
            fields[1] = tps.getLocationCode();
        }
        fields[2] = tps.getTransactionNum();
        fields[3] = tps.getPaymentType();

        if (tps.getPaymentMethod() == null)
        {
            fields[5] = "";
        }
        else
        {
            fields[5] = tps.getPaymentMethod().getDescription();
        }

        if (tps.getPaymentDetail() == null)
        {
            fields[6] = "";
        }
        else
        {
            fields[6] = tps.getPaymentDetail();
        }

        fields[7] = String.valueOf(tps.getAmount());
        fields[8] = String.valueOf(taxPaid);
        fields[9] = "\"\"";
        fields[10] = tps.getTpsFileName();
        fields[11] = String.valueOf(result);

        new ERLogMsg(ACCOUNT_ADJUSTMENT_ERID, RECORD_CLASS, ACCOUNT_ADJUSTMENT_SERVICE_NAME, spid, fields)
                .log(ctx);

    }
    
    public static void genPTPResetER(final Context ctx, final Account act, final CreditCategory cc,
    		final AccountStateEnum state, int maxNumberPTP, int maxPTPInterval)
    {
    	final String[] fields = new String[13];
        fields[0] = act.getBAN();
        fields[1] = act.getFirstName();
        fields[2] = act.getLastName();
        final User principal = (User) ctx.get(Principal.class);

        if (principal != null)
        {
            fields[3] = principal.getFirstName();
        }
        else
        {
            fields[3] = "ecare";
        }

        fields[4] = String.valueOf(act.getCurrentNumPTPTransitions());
        fields[5] = String.valueOf(maxNumberPTP);
        fields[6] = String.valueOf(maxPTPInterval);
        final Calendar cal = Calendar.getInstance();
        if (act.getPromiseToPayStartDate() != null)
        {
            cal.setTime(act.getPromiseToPayStartDate());
        }

        fields[7] = formatERDateDayOnly(cal.getTime());

        cal.add(Calendar.DAY_OF_YEAR, maxPTPInterval);

        fields[8] = formatERDateDayOnly(cal.getTime());

        fields[9] = String.valueOf(cc.getCode());
        fields[10] = cc.getDesc();

        // previous state
        fields[11] = state.getDescription();
        fields[12] = String.valueOf(act.getAccumulatedBalance(ctx, null));

        new ERLogMsg(PTPRESET_ERID, RECORD_CLASS, PTP_RESET_NAME, act.getSpid(), fields).log(ctx);
    }


    /**
     * Sends a PTP reset ER.
     * 
     * @param ctx
     *            The operating context.
     * @param act
     *            The account whose PTP is being reset.
     * @param cc
     *            The credit category of the account.
     * @param state
     *            The account state.
     */
    
    public static void genPTPResetER(final Context ctx, final Account act, final CreditCategory cc,
            final AccountStateEnum state)
    {

        final String[] fields = new String[13];
        fields[0] = act.getBAN();
        fields[1] = act.getFirstName();
        fields[2] = act.getLastName();
        final User principal = (User) ctx.get(Principal.class);

        if (principal != null)
        {
            fields[3] = principal.getFirstName();
        }
        else
        {
            fields[3] = "ecare";
        }

        fields[4] = String.valueOf(act.getCurrentNumPTPTransitions());
        fields[5] = String.valueOf(cc.getMaxNumberPTP());
        fields[6] = String.valueOf(cc.getMaxPTPInterval());
        final Calendar cal = Calendar.getInstance();
        if (act.getPromiseToPayStartDate() != null)
        {
            cal.setTime(act.getPromiseToPayStartDate());
        }

        fields[7] = formatERDateDayOnly(cal.getTime());

        cal.add(Calendar.DAY_OF_YEAR, cc.getMaxPTPInterval());

        fields[8] = formatERDateDayOnly(cal.getTime());

        fields[9] = String.valueOf(cc.getCode());
        fields[10] = cc.getDesc();

        // previous state
        fields[11] = state.getDescription();
        fields[12] = String.valueOf(act.getAccumulatedBalance(ctx, null));

        new ERLogMsg(PTPRESET_ERID, RECORD_CLASS, PTP_RESET_NAME, act.getSpid(), fields).log(ctx);
    }


    /**
     * Sends an account adjustment ER for a transaction.
     * 
     * @param ctx
     *            The operating context.
     * @param trans
     *            The transaction being created.
     * @param upsResult
     *            Result code from UPS.
     * @param result
     *            Result code.
     */
    public static void genAccountAdjustmentER(final Context ctx, final Transaction trans, final int upsResult,
            final int result)
    {
        final Account acct = (Account) ctx.get(Account.class);
        final TPSRecord tps = (TPSRecord) ctx.get(TPSRecord.class);

        // get tax paid
        long taxPaid = trans.getTaxPaid();
        if (taxPaid < 0)
        {
            taxPaid = taxPaid * -1;
        }

        /*
         * amount is supposed to be negative. convert it to positive for er purpose
         */
        long amount = trans.getAmount();
        if (amount < 0)
        {
            amount = amount * -1;
        }

        // get adjustment type
        String glCode = "";
        try
        {
            AdjustmentType type = AdjustmentTypeSupportHelper.get(ctx).getAdjustmentTypeForRead(ctx,
                    trans.getAdjustmentType());

            if (type != null)
            {
                glCode = type.getGLCodeForSPID(ctx, acct.getSpid());
            }
        }
        catch (final HomeException he)
        {
            // ignore
        }

        final String[] fields = new String[16];

        fields[0] = acct.getBAN();
        fields[1] = trans.getLocationCode();
        fields[2] = trans.getExtTransactionId();
        if (tps == null)
        {
            fields[3] = "";
        }
        else
        {
            fields[3] = tps.getPaymentType();
        }
        fields[4] = String.valueOf(trans.getAdjustmentType());

        if (tps == null)
        {
            fields[5] = "";
        }
        else
        {
            fields[5] = String.valueOf(tps.getPaymentMethod().getIndex());
        }
        fields[6] = trans.getPaymentDetails();

        fields[7] = formatERDateDayOnly(trans.getTransDate());
        if (tps == null)
        {
            fields[8] = "";
        }
        else if (tps.getVoidFlag())
        {
            fields[8] = "Y";
        }
        else
        {
            fields[8] = "N";
        }
        fields[9] = addDoubleQuotes(trans.getCSRInput().replace("\n", " - "));
        fields[10] = String.valueOf(amount);
        fields[11] = String.valueOf(taxPaid);
        fields[12] = glCode;
        fields[13] = String.valueOf(upsResult);
        fields[14] = String.valueOf(result);
        if (trans.getAgent() != null)
        {
            fields[15] = trans.getAgent();
        }
        else
        {
            // Larry: for TT: 403303537
            fields[15] = "";
        }

        new ERLogMsg(ACCOUNT_ADJUSTMENT_ERID, RECORD_CLASS, ACCOUNT_ADJUSTMENT_SERVICE_NAME, acct.getSpid(), fields)
                .log(ctx);

    }


    /**
     * Sends an invalid TPS subscriber ER.
     * 
     * @param ctx
     *            The operating context.
     * @param result
     *            Result code.
     */
    public static void genInvalidTPSSubscriberER(final Context ctx, final int result)
    {
        final TPSRecord tps = (TPSRecord) ctx.get(TPSRecord.class);

        int spid = -1;
        if (ctx.has(Subscriber.class))
        {

            final Subscriber subs = (Subscriber) ctx.get(Subscriber.class);
            spid = subs.getSpid();
        }

        final String[] fields = new String[5];

        fields[0] = tps.getTelephoneNum();
        fields[1] = tps.getAccountNum();
        fields[2] = String.valueOf(tps.getAmount());
        if (tps.getPaymentType() == null)
        {
            fields[3] = "";
        }
        else
        {
            fields[3] = tps.getPaymentType();
        }
        fields[4] = String.valueOf(result);

        new ERLogMsg(TPS_INVALID_ACCOUNT_ERID, RECORD_CLASS, TPS_INVALID_SUBSCRIBER_SERVICE_NAME, spid, fields)
                .log(ctx);
    }


    /**
     * Generates an ER 1124 for payments with multiple subscribers in the MSISDN history.
     * 
     * @param ctx
     *            The context to get information
     * @param subscribers
     *            the list of subscribers on the MSISDN history with outstanding amount
     *            owing
     * @param fileName
     *            the file name where it came from
     * @param txn
     *            the transaction to be reported in the ER
     * @param resultCode
     *            the result code
     */
    public static void writeMultipleSubscribersInHistoryER(final Context ctx, final String subscribers,
            final String fileName, final Transaction txn, final int resultCode)
    {
        int spid = -1;
        String ban = "";
        if (ctx.has(Subscriber.class))
        {
            final Subscriber subs = (Subscriber) ctx.get(Subscriber.class);
            spid = subs.getSpid();
            ban = subs.getBAN();
        }

        final String[] fields = new String[10];

        fields[0] = txn.getAgent();
        fields[1] = txn.getMSISDN();
        fields[2] = ban;
        fields[3] = String.valueOf(Math.abs(txn.getAmount()));
        fields[4] = String.valueOf(txn.getAdjustmentType());
        fields[5] = formatERDateDayOnly(txn.getTransDate());
        fields[6] = txn.getExtTransactionId();
        fields[7] = subscribers;
        fields[8] = fileName;
        fields[9] = String.valueOf(resultCode);

        new ERLogMsg(TPS_MULTIPLE_SUBSCRIBERS_ERID, RECORD_CLASS, TPS_MULTIPLE_SUBSCRIBERS_SERVICE_NAME, spid, fields)
                .log(ctx);
    }


    /**
     * Generates an ER 1125 for Payment report.
     * 
     * @param ctx
     *            The context to get information
     * @param fileName
     *            the file name where it came from
     * @param txn
     *            the transaction to be reported in the ER
     * @param succsfulAmt
     *            the total amount of successful transactions
     * @param failedAmt
     *            the total amount of failed transactions
     * @param successApplied
     *            Number of subscribers who have been successfully paid
     * @param failedSubscribers
     *            Number of subscribers who did not receive their intended payment
     * @param resultCode
     *            the result code
     */
    public static void writePaymentAtAccountLevelER(final Context ctx, final String fileName, final Transaction txn,
            final long succsfulAmt, final long failedAmt, final long successApplied, final long failedSubscribers,
            final int resultCode)
    {
        int spid = -1;
        String ban = "";
        if (ctx.has(Account.class))
        {
            final Account acct = (Account) ctx.get(Account.class);
            spid = acct.getSpid();
            ban = acct.getBAN();
        }

        final String[] fields = new String[12];

        fields[0] = txn.getAgent();
        fields[1] = ban;
        fields[2] = String.valueOf(Math.abs(txn.getAmount()));
        fields[3] = String.valueOf(succsfulAmt);
        fields[4] = String.valueOf(failedAmt);
        fields[5] = String.valueOf(successApplied);
        fields[6] = String.valueOf(failedSubscribers);
        fields[7] = String.valueOf(txn.getAdjustmentType());
        fields[8] = formatERDateDayOnly(txn.getTransDate());
        fields[9] = txn.getExtTransactionId();
        fields[10] = fileName;
        fields[11] = String.valueOf(resultCode);

        new ERLogMsg(TPS_STD_PAYMENT_ACCOUNT_ERID, RECORD_CLASS, TPS_STD_PAYMENT_ACCOUNT_SERVICE_NAME, spid, fields)
                .log(ctx);
    }


    /**
     * Generates an ER 1125 for Payment report.
     * 
     * @param ctx
     *            The context to get information
     * @param fileName
     *            the file name where it came from
     * @param succsfulAmt
     *            the total amount of successful transactions
     * @param failedAmt
     *            the total amount of failed transactions
     * @param succesRecords
     *            the number of record that successfully went through
     * @param failedRecords
     *            the number of record that failed to go into the system
     */
    public static void writePaymentAtAccountLevelFileER(final Context ctx, final String fileName,
            final long succsfulAmt, final long failedAmt, final long succesRecords, final long failedRecords)
    {

        final String[] fields = new String[6];

        fields[0] = fileName;
        fields[1] = String.valueOf(succsfulAmt + failedAmt);
        fields[2] = String.valueOf(succsfulAmt);
        fields[3] = String.valueOf(failedAmt);
        fields[4] = String.valueOf(succesRecords);
        fields[5] = String.valueOf(failedRecords);

        new ERLogMsg(TPS_PAYMENT_ACCOUNT_REPORT_ERID, RECORD_CLASS, TPS_FILE_PAYMENT_ACCOUNT_SERVICE_NAME, -1, fields)
                .log(ctx);
    }


    /**
     * Sends the balance reset ER.
     * 
     * @param ctx
     *            The operating context.
     * @param sub
     *            The subscriber whose balance is being reset.
     * @param balance
     *            The balance of the subscriber.
     * @param upsRet
     *            UPS result.
     * @param result
     *            Result code.
     * @param paymentAmount
     *            Payment amount.
     */
    public static void genBalanceResetER(final Context ctx, final Subscriber sub, final int balance, final int upsRet,
            final int result, final long paymentAmount)
    {

        final CRMSpid crmspid = (CRMSpid) ctx.get(CRMSpid.class);
        final AdjustmentType adjustType = (AdjustmentType) ctx.get(AdjustmentType.class);
        AdjustmentInfo adjustInfo = null;

        if (adjustType != null)
        {
            adjustInfo = (AdjustmentInfo) adjustType.getAdjustmentSpidInfo().get(Integer.valueOf(crmspid.getId()));
        }

        final String[] fields = new String[11];
        fields[0] = sub.getBAN();
        fields[1] = sub.getId();
        fields[2] = String.valueOf(balance);
        fields[3] = String.valueOf(sub.getCreditLimit(ctx));
        fields[4] = sub.getCurrency(ctx);

        if (adjustType == null)
        {
            fields[5] = "";
        }
        else
        {
            fields[5] = adjustType.getName();
        }

        if (adjustInfo == null)
        {
            fields[6] = "";
        }
        else
        {
            fields[6] = adjustInfo.getGLCode();
        }

        fields[7] = String.valueOf(upsRet);
        fields[8] = String.valueOf(result);

        // added payment amount for loyalty TT6010628908
        fields[9] = String.valueOf(paymentAmount);
        // added the subscriber msisdn for loyalty TT6010628908
        fields[10] = sub.getMSISDN();

        new ERLogMsg(SUBSCRIBER_BALANCE_RESET_ERID, RECORD_CLASS, SUBSCRIBER_BALANCE_RESET_SERVICE_NAME, sub.getSpid(),
                fields).log(ctx);
    }


    /**
     * Generates the credit limit reset ER.
     * 
     * @param ctx
     *            The operating context.
     * @param sub
     *            Subscriber whose credit limit is being reset.
     * @param deposit
     *            The deposit amount of the subscriber.
     * @param totalDeposit
     *            The total deposit of the subscriber.
     * @param creditLimit
     *            The credit limit of the subscriber.
     * @param upsRet
     *            UPS result.
     */
    public static void genCreditLimitResetER(final Context ctx, final Subscriber sub, final long deposit,
            final long totalDeposit, final long creditLimit, final int upsRet)
    {
        getCreditLimitResetER(ctx, sub, deposit, totalDeposit, creditLimit, upsRet).log(ctx);
    }


    /**
     * Gets the credit limit reset ER.
     * 
     * @param ctx
     *            The operating context.
     * @param sub
     *            Subscriber whose credit limit is being reset.
     * @param deposit
     *            The deposit amount of the subscriber.
     * @param totalDeposit
     *            The total deposit of the subscriber.
     * @param creditLimit
     *            The credit limit of the subscriber.
     * @param upsRet
     *            UPS result.
     * @return ER Log message.
     */
    public static ERLogMsg getCreditLimitResetER(final Context ctx, final Subscriber sub, final long deposit,
            final long totalDeposit, final long creditLimit, final int upsRet)
    {
        final CRMSpid crmspid = (CRMSpid) ctx.get(CRMSpid.class);
        final AdjustmentType adjustType = (AdjustmentType) ctx.get(AdjustmentType.class);
        AdjustmentInfo adjustInfo = null;

        if (adjustType != null && crmspid != null)
        {
            adjustInfo = (AdjustmentInfo) adjustType.getAdjustmentSpidInfo().get(Integer.valueOf(crmspid.getId()));
        }

        final String[] fields = new String[9];
        fields[0] = sub.getBAN();
        fields[1] = sub.getId();
        fields[2] = String.valueOf(deposit);
        fields[3] = String.valueOf(totalDeposit);
        fields[4] = String.valueOf(creditLimit);

        if (adjustType == null)
        {
            fields[5] = "";
        }
        else
        {
            fields[5] = adjustType.getName();
        }

        if (adjustInfo == null)
        {
            fields[6] = "";
        }
        else
        {
            fields[6] = adjustInfo.getGLCode();
        }

        fields[7] = String.valueOf(upsRet);

        if (upsRet == 0)
        {
            fields[8] = "0";
        }
        else
        {
            fields[8] = String.valueOf(TPSPipeConstant.RESULT_CODE_UPS_FAILS);
        }

        return new ERLogMsg(SUBSCRIBER_CREDIT_LIMIT_RESET_ERID, RECORD_CLASS,
                SUBSCRIBER_CREDIT_LIMIT_RESET_SERVICE_NAME, sub.getSpid(), fields);
    }


    /**
     * Updates the given credit-limit adjustment ER with the current date and result code.
     * 
     * @param message
     *            The ERLogMsg to update.
     * @param result
     *            The result code to include in the ER.
     */
    public static void updateCreditLimitResetER(final ERLogMsg message, final int result)
    {
        message.setTimestamp(System.currentTimeMillis());
        final String[] fields = message.getFields();

        /*
         * As above, the result code is set to RESULT_CODE_UPS_FAILS if not zero. The
         * result is always the last entry.
         */

        fields[fields.length - 2] = String.valueOf(result);
        if (result == 0)
        {
            fields[fields.length - 1] = "0";
        }
        else
        {
            fields[fields.length - 1] = String.valueOf(TPSPipeConstant.RESULT_CODE_UPS_FAILS);
        }
    }


    /**
     * Generates the drop call reimbursement ER.
     * 
     * @param ctx
     *            The operating context.
     * @param call
     *            The call being reimbursed.
     * @param resultCode
     *            Result code.
     */
    public static void generateDroppedCallReimbursementER(final Context ctx, final CallDetail call, final int resultCode)
    {
        final String[] fields = new String[6];

        fields[0] = call.getOrigMSISDN();
        fields[1] = call.getBAN();
        fields[2] = String.valueOf(call.getCharge());
        fields[3] = call.getCallType().getDescription();
        fields[4] = call.getGLCode();
        fields[5] = String.valueOf(resultCode);

        new ERLogMsg(DROPPED_CALL_REIMBURSEMENT_ERID, RECORD_CLASS, DROPPED_CALL_REIMBURSEMENT_SERVICE_NAME, call
                .getSpid(), fields).log(ctx);
    }


    /**
     * Generates the subscriber Adjustment ER.
     * 
     * @param ctx
     *            The operating context.
     * @param spid
     *            Service provider ID.
     * @param acctNum
     *            Account BAN.
     * @param subNum
     *            Subscriber ID.
     * @param amount
     *            Amount being adjusted.
     * @param csrInput
     *            CSR input string.
     * @param adjustmentType
     *            Adjustment type.
     * @param glCode
     *            GL code.
     * @param result
     *            Result code.
     * @param agent
     *            CSR agent.
     * @param locationCode
     *            Location code.
     */
    public static void createSubAdjustmentEr(final Context ctx, final int spid, final String acctNum,
            final String subNum, final long amount, final String csrInput, final String adjustmentType,
            final String glCode, final int result, final String agent, final String locationCode)
    {

        final String[] fields = new String[9];
        fields[0] = acctNum;
        fields[1] = subNum;
        fields[2] = String.valueOf(amount);
        fields[3] = addDoubleQuotes(csrInput.replace("\n", " - "));
        fields[4] = adjustmentType;
        fields[5] = glCode;
        fields[6] = String.valueOf(result);
        fields[7] = agent;
        fields[8] = locationCode;

        new ERLogMsg(SUB_ADJUSTMENT_ERID, RECORD_CLASS, SUB_ADJUST_SERVICE_NAME, spid, fields).log(ctx);
    }


    /**
     * Generates the multi-sim provisioning event ER.
     * 
     * @param ctx
     *            The operating context.
     * @param service
     *            The subscriber auxiliary service being provisioned
     */
    public static void createMultiSimProvisioningEr(final Context ctx, 
            MultiSimSubExtension newExtension,
            MultiSimProvisioningTypeEnum provisionType)
    {
        String subId = newExtension.getSubId();
        long auxSvcId = newExtension.getAuxSvcId();
        int spid = newExtension.getSpid();
        
        MultiSimProvisioningER er = null;
        try
        {
            er = (MultiSimProvisioningER) XBeans.instantiate(MultiSimProvisioningER.class, ctx);
        }
        catch (Exception e)
        {
            er = new MultiSimProvisioningER();
        }
        
        er.setAgent(SystemSupport.getAgent(ctx));

        er.setSubscriptionID(subId);
        er.setAuxSvcId(auxSvcId);
        er.setSpid(spid);

        Subscriber sub = (Subscriber) ctx.get(Subscriber.class);
        if (sub == null 
                || !SafetyUtil.safeEquals(sub.getId(), subId))
        {
            try
            {
                sub = SubscriberSupport.lookupSubscriberForSubId(ctx, subId);
            }
            catch (HomeException e)
            {
                new MinorLogMsg(ERLogger.class, "Error retrieving subscriber [ID=" + subId + "].  ER " + er.getId() + " will be logged without primary IMSI/MSISDN information.", e).log(ctx);
            }
        }
        if (sub != null)
        {
            er.setImsi(sub.getPackageId());
            er.setMsisdn(sub.getMSISDN());
        }

        er.setProvisioningType(provisionType.getIndex());

        And filter = new And();
        filter.add(new EQ(MultiSimSubExtensionXInfo.SUB_ID, subId));
        filter.add(new EQ(MultiSimSubExtensionXInfo.AUX_SVC_ID, auxSvcId));

        MultiSimSubExtension oldExtension = (MultiSimSubExtension) ctx.get(Lookup.OLD_MULTISIM_SUB_EXTENSION);
        if (oldExtension == null)
        {
            List<MultiSimSubExtension> existingExtensions = ExtensionSupportHelper.get(ctx).getExtensions(ctx, MultiSimSubExtension.class, filter);
            if (existingExtensions != null && existingExtensions.size() > 0)
            {
                oldExtension = existingExtensions.get(0);
            }
        }
        
        List<SimERDetail> oldSimDetails = getMultiSimERDetails(ctx, oldExtension);
        er.setOriginalSimDetails(oldSimDetails);

        List<SimERDetail> newSimDetails = getMultiSimERDetails(ctx, newExtension);
        er.setUpdatedSimDetails(newSimDetails);

        new ERLogMsg(ctx, er).log(ctx);
    }


    public static List<SimERDetail> getMultiSimERDetails(final Context ctx, MultiSimSubExtension extension)
    {
        List<SimERDetail> simDetails = new ArrayList<SimERDetail>();
        if (extension != null)
        {
            List<MultiSimRecordHolder> sims = extension.getSims();
            if (sims != null)
            {
                for (MultiSimRecordHolder sim : sims)
                {
                    SimERDetail detail = null;
                    try
                    {
                        detail = (SimERDetail) XBeans.instantiate(SimERDetail.class, ctx);
                    }
                    catch (Exception e)
                    {
                        detail = new SimERDetail();
                    }
                    if(sim.isSimSwapped())
                    {
                    	detail.setImsi(sim.getNewSimAfterSwap().getImsi());
                    }else
                    	detail.setImsi(sim.getImsi());
                    
                    detail.setMsisdn(sim.getMsisdn());                    
                    detail.setProvCode(sim.getProvCode());
                    detail.setChargeCode(sim.getChargeCode());
                    
                    simDetails.add(detail);
                }
            }
        }
        return simDetails;
    }


    /**
     * Generates the price plan creation ER.
     * 
     * @param ctx
     *            The operating context.
     * @param plan
     *            The price plan being created.
     * @param version
     *            Price plan version being created.
     * @param userID
     *            User creating the price plan version.
     */
    public static void createPricePlanEr(final Context ctx, final PricePlan plan, final PricePlanVersion version)
    {
        PricePlanCreationER er = new PricePlanCreationER();

        er.setSpid(plan.getSpid());
        er.setUserID(SystemSupport.getAgent(ctx));
        er.setPricePlanID(plan.getId());
        er.setPricePlanName(plan.getName());
        er.setVoiceRatePlan(plan.getVoiceRatePlan());
        er.setSmsRatePlan(plan.getSMSRatePlan());
        er.setDefaultDeposit(version.getDeposit());
        er.setCreditLimit(version.getCreditLimit());
        er.setMonthlyFee(0);
        er.setFreeMinutes(0);
        er.setPerMinuteDefaultAirRate(version.getDefaultPerMinuteAirRate());
        er.setServices(addDoubleQuotes(version.getServices(ctx).toString()));

        Date activation = version.getActivation();
        if (activation == null)
        {
            er.setActivationDate(version.getActivateDate());
        }
        er.setActivationDate(activation);
        
        er.setVersionID(version.getVersion());
        
        if (plan.isApplyContractDurationCriteria())
        {
            er.setMinimumContractDuration(plan.getMinContractDuration());
            er.setMaximumContractDuration(plan.getMaxContractDuration());
            er.setContractDurationUnits(plan.getContractDurationUnits());
        }
        
        er.setState(plan.getState().getDescription());
        er.setGrandfatherPPId(plan.getGrandfatherPPId());

        new ERLogMsg(ctx, er).log(ctx);
    }


    protected Date getActivationDate(PricePlanVersion version)
    {
        Date activation = version.getActivation();
        if (activation == null)
        {
            return version.getActivateDate();
        }
        return activation;
    }


    /**
     * Generates the subscriber successful transaction ER.
     * 
     * @param ctx
     *            The operating context.
     * @param txn
     *            The transaction.
     * @param amount
     *            Amount in the transaction.
     * @param result
     *            Result code.
     */
    public static void createTransactionEr(final Context ctx, final Transaction txn, final long amount,
            final Long balanceAmount, final int result)
    {
        int adjustmentType = txn.getAdjustmentType();
        String itemType = null;
        String serviceId = "";
        String svcName = "";
        String startDate = "";
        String endDate = "";
        String svcFee = "";
        Object item = ctx.get(RecurringRechargeSupport.RECURRING_RECHARGE_CHARGED_ITEM);
        try
        {

            Subscriber sub = (Subscriber) ctx.get(Subscriber.class);
            Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);

            if (sub == null)
            {
                throw new Exception("Development error: Subscriber doesn't exist in context.");
            }
            String subId = sub.getId();

            Service svc = null;
            ServiceFee2 svcFee2 = null;

            if (item != null)
            {
                if (item instanceof ServiceFee2)
                {
                    svcFee2 = (ServiceFee2) item;
                    svc = svcFee2.getService(ctx);
                }
            }
            else
            {
                svc = ServiceSupport.getServiceByAdjustment(ctx, adjustmentType);
            }

            if (svc != null)
            {
                itemType = "Service";
                long svcId = svc.getID();
                serviceId = String.valueOf(svcId);
                svcName = svc.getName();

                if (svcFee2 == null)
                {
                    final PricePlanVersion pricePlan = sub.getPricePlan(ctx);
                    if (pricePlan == null)
                    {
                        throw new Exception("Cannot find price plan version for subscriber " + subId);
                    }
                    final Map serviceFees = pricePlan.getServiceFees(ctx);
                    if (serviceFees == null)
                    {
                        throw new Exception("Cannot retrieve service fees for price plan " + pricePlan.getId()
                                + " version " + pricePlan.getVersion());
                    }
                    svcFee2 = (ServiceFee2) serviceFees.get(svcId);
                }

                // Trying to retrieve service fee from old price plan.
                if (svcFee2 == null && oldSub!=null && (oldSub.getPricePlan()!=sub.getPricePlan() || oldSub.getPricePlanVersion()!=sub.getPricePlanVersion()))
                {
                    final PricePlanVersion oldPricePlan = oldSub.getPricePlan(ctx);
                    if (oldPricePlan!=null)
                    {
                        final Map oldServiceFees = oldPricePlan.getServiceFees(ctx);
                        
                        if (oldServiceFees!=null)
                        {
                            svcFee2 = (ServiceFee2) oldServiceFees.get(svcId);
                        }
                    }
                }
                

                if (svcFee2 == null)
                {
                    throw new Exception("Cannot find corresponding ServiceFee2 for serive id:" + svcId);
                }
                svcFee = String.valueOf(svcFee2.getFee());

                SubscriberServices subSvc = SubscriberServicesSupport.getSubscriberServiceRecord(ctx, subId, svcId, svcFee2.getPath());
                if (subSvc != null)
                {
                    Date start = subSvc.getStartDate();
                    if (start != null)
                    {
                        startDate = formatERDateWithTime(start);
                    }
                    Date end = subSvc.getEndDate();
                    if (end != null)
                    {
                        endDate = formatERDateWithTime(end);
                    }
                }
            }
            else
            {
                AuxiliaryService auxSvc = null;
                SubscriberAuxiliaryService subAuxSvc = null;
                if (item != null)
                {
                    if (item instanceof SubscriberAuxiliaryService)
                    {
                        subAuxSvc = (SubscriberAuxiliaryService) item;
                        auxSvc = subAuxSvc.getAuxiliaryService(ctx);
                    }
                }
                else
                {
                    auxSvc = AuxiliaryServiceSupport.getAuxiliaryServiceByAdjustmentType(ctx, adjustmentType);
                }

                if (auxSvc != null)
                {
                    itemType = "Auxiliary Service";
                    long auxSvcId = auxSvc.getIdentifier();
                    serviceId = String.valueOf(auxSvcId);
                    svcName = auxSvc.getName();
                    svcFee = String.valueOf(auxSvc.getCharge());

                    if (subAuxSvc == null)
                    {
                        subAuxSvc = SubscriberAuxiliaryServiceSupport.
                                getSubscriberAuxiliaryServicesBySubIdAndSvcId(ctx, subId, auxSvcId);
                    }

                    if (subAuxSvc != null)
                    {
                        Date start = subAuxSvc.getStartDate();
                        if (start != null)
                        {
                            startDate = formatERDateWithTime(start);
                        }
                        Date end = subAuxSvc.getEndDate();
                        if (end != null)
                        {
                            endDate = formatERDateWithTime(end);
                        }
                    }
                }
                else
                {
                    BundleProfile bp = null;
                    BundleFee bundleFee = null;

                    if (item != null)
                    {
                        if (item instanceof BundleFee)
                        {
                            bundleFee = (BundleFee) item;
                            bp = bundleFee.getBundleProfile(ctx, sub.getSpid());
                        }
                    }
                    else
                    {
                        CRMBundleProfile bundleSvc = (CRMBundleProfile) ctx.get(CRMBundleProfile.class);
                        bp = bundleSvc.getBundleByAdjustmentType(ctx, adjustmentType);
                    }

                    if (bp != null)
                    {
                        long bundleId = bp.getBundleId();
                        serviceId = String.valueOf(bundleId);
                        svcName = bp.getName();
                        itemType = bp.isAuxiliary() ? "Auxiliary Bundle" : "Bundle";

                        if (bundleFee == null)
                        {
                            //WARNING: REVERT THIS CHANGE
                            //   Map subBundles = SubscriberBundleSupport.getSubscribedBundles(ctx, sub);
                            //   bundleFee = (BundleFee) subBundles.get(bundleId);
                        }

                        if (bundleFee != null)
                        {
                            svcFee = String.valueOf(bundleFee.getFee());
                            Date start = bundleFee.getStartDate();
                            if (start != null)
                            {
                                startDate = formatERDateWithTime(start);
                            }
                            Date end = bundleFee.getEndDate();
                            if (end != null)
                            {
                                endDate = formatERDateWithTime(end);
                            }
                        }
                    }
                }
            }
        }
        catch (Throwable t)
        {
            LogSupport.minor(ctx, ERLogger.class.getName(), "Exception detected while generating ER.", t);
        }

        final String[] fields = new String[16];
        fields[0] = txn.getBAN();
        fields[1] = txn.getMSISDN();
        fields[2] = String.valueOf(txn.getSubscriberType().getIndex());

        if (txn.getTransDate() != null)
        {
            fields[3] = formatERDateWithTime(txn.getTransDate());
        }
        else
        {
            fields[3] = "";
        }

        fields[4] = txn.getGLCode();
        fields[5] = String.valueOf(amount);
        fields[6] = String.valueOf(adjustmentType);
        fields[7] = String.valueOf(txn.getReceiptNum());
        fields[8] = String.valueOf(result);
        if (balanceAmount != null)
        {
            fields[9] = String.valueOf(balanceAmount.longValue());
        }
        else
        {
            fields[9] = "";
        }

        fields[10] = itemType != null ? itemType : "";
        fields[11] = serviceId;
        fields[12] = svcName;
        fields[13] = startDate;
        fields[14] = endDate;
        fields[15] = svcFee;

        new ERLogMsg(SUBSCRIBER_TRANSACTION_ERID, RECORD_CLASS_1100, SUBSCRIBER_TRANSACTION_SERVICE_NAME,
                txn.getSpid(), fields).log(ctx);
    }


    /**
     * Generates the dealer code modification ER.
     * 
     * @param ctx
     *            The operating context.
     * @param dealer
     *            The dealer being updated.
     * @param action
     *            The modification.
     * @param result
     *            Result code.
     */
    public static void modifyDealerCodeEr(final Context ctx, final DealerCode dealer, final String action,
            final int result)
    {

        final String[] fields = new String[4];

        fields[0] = action;
        fields[1] = dealer.getCode();
        fields[2] = dealer.getDesc();
        fields[3] = String.valueOf(result);

        new ERLogMsg(DEALER_CODE_ERID, RECORD_CLASS_1100, DEALER_CODE_SERVICE_NAME, dealer.getSpid(), fields).log(ctx);
    }


    /**
     * Generate an ER for subscriber move.
     * 
     * @param ctx
     *            The operating context.
     * @param oldBan
     *            The BAN of the old account.
     * @param newBan
     *            The BAN of the new account.
     * @param sub
     *            The subscriber being moved.
     * @param result
     *            Result code.
     */
    public static void subscriberChangeAccountEr(final Context ctx, final String oldBan, final String newBan,
            final Subscriber sub, final int result)
    {
        final String[] fields = new String[36];
        fields[0] = oldBan;
        fields[1] = newBan;
        fields[2] = sub.getGroupMSISDN(ctx);
        fields[3] = sub.getMSISDN();
        fields[4] = sub.getFaxMSISDN();
        fields[5] = sub.getDataMSISDN();
        fields[6] = sub.getIMSI();
        fields[7] = String.valueOf(sub.getSpid());
        fields[8] = sub.getBAN();
        fields[9] = String.valueOf(sub.getPricePlan());
        fields[10] = sub.getState().toString();
        fields[11] = String.valueOf(sub.getDeposit(ctx));
        fields[12] = String.valueOf(sub.getCreditLimit(ctx));
        fields[13] = sub.getCurrency(ctx);
        fields[14] = addDoubleQuotes(sub.getServices(ctx).toString());
        if (sub.getSubscriberType() != null)
        {
            fields[15] = String.valueOf(sub.getSubscriberType().getIndex());
        }
        else
        {
            fields[15] = "";
        }
        fields[16] = sub.getSupportMSISDN(ctx);
        fields[17] = addDoubleQuotes("");
        fields[18] = addDoubleQuotes("");
        fields[19] = addDoubleQuotes("");
        fields[20] = addDoubleQuotes("");
        fields[21] = addDoubleQuotes("");
        fields[22] = addDoubleQuotes("");
        fields[23] = sub.getDealerCode();
        fields[24] = String.valueOf(sub.getDiscountClass());

        if (sub.getDepositDate() != null)
        {
            fields[25] = formatERDateDayOnly(sub.getDepositDate());
        }
        else
        {
            fields[25] = "";
        }

        if (sub.getStartDate() != null)
        {
            fields[26] = formatERDateDayOnly(sub.getStartDate());
        }
        else
        {
            fields[26] = "";
        }

        if (sub.getEndDate() != null)
        {
            fields[27] = formatERDateDayOnly(sub.getEndDate());
        }
        else
        {
            fields[27] = "";
        }

        fields[28] = sub.getBillingLanguage();
        fields[29] = sub.getPackageId();
        fields[30] = sub.getId();

        if (sub.getDateCreated() != null)
        {
            fields[31] = formatERDateDayOnly(sub.getDateCreated());
        }
        else
        {
            fields[31] = "";
        }

        fields[32] = "";
        fields[33] = "";
        fields[34] = sub.getBillingOption().toString();
        fields[35] = String.valueOf(result);

        new ERLogMsg(SUBSCRIBER_CHANGE_ACCOUNT_ERID, RECORD_CLASS, SUBSCRIBER_CHANGE_ACCOUNT_NAME, sub.getSpid(),
                fields).log(ctx);
    }


    /**
     * Generate an ER for loyalty for birthday promotions.
     * 
     * @param ctx
     *            The operating context.
     * @param sub
     *            The subscriber receiving the promotion.
     */
    public static void generateBirthdayPromotionER(final Context ctx, final Subscriber sub)
    {
        final String[] fields = new String[6];
        fields[0] = "CRM";
        fields[1] = sub.getBAN();
        fields[2] = sub.getMSISDN();
        // TODO 2008-08-22 date of birth no longer part of Subscriber
        fields[3] = ""; //formatERDateDayOnly(sub.getAccount(ctx).getDateOfBirth());
        fields[4] = sub.getState().toString();
        if (sub.getStartDate() != null)
        {
            fields[5] = formatERDateDayOnly(sub.getStartDate());
        }
        else
        {
            fields[5] = null;
        }

        new ERLogMsg(LOYALTY_PROMOTION_ERID, RECORD_CLASS, BIRTHDAY_PROMOTION_SERVICE_NAME, sub.getSpid(), fields)
                .log(ctx);
    }


    /**
     * Generate an ER for TPS reconciliation.
     * 
     * @param ctx
     *            The operating context.
     * @param tps
     *            The TPS record.
     * @param sub
     *            The sibscriber.
     */
    public static void generateTPSReconciliationER(final Context ctx, final TPSRecord tps, final Subscriber sub)
    {

        final String[] fields = new String[5];

        fields[0] = sub.getBAN();
        fields[1] = tps.getTransactionNum();

        fields[2] = sub.getId();
        fields[3] = sub.getMSISDN();
        fields[4] = String.valueOf(tps.getAmount());

        new ERLogMsg(TPS_RECONCILIATION_ERID, RECORD_CLASS, TPS_RECONCILIATION_NAME, sub.getSpid(), fields).log(ctx);
    }


    /**
     * Generates the payment plan activation/deactivation ER
     * 
     * @param ctx
     *            The operating context.
     * @param spid
     *            service provider
     * @param ban
     *            account number
     * @param paymentPlanID
     *            unique identifier of the Payment Plan selected
     * @param event
     *            1=Activation, 2=Deactivation
     * @param initialAmount
     *            amount that is originally transferred to the Payment Plan Loan bucket
     * @param numPayments
     *            number of payments as defined in the Payment Plan profile
     * @param remainingBalance
     *            The remaining balance
     */
    public static void generatePaymentPlanActivationEr(final Context ctx, final int spid, final String ban,
            final long paymentPlanID, final int event, final long initialAmount, final int numPayments,
            final long remainingBalance)
    {
        final String[] fields = new String[6];
        fields[0] = ban;
        fields[1] = String.valueOf(paymentPlanID);
        fields[2] = String.valueOf(event);
        fields[3] = String.valueOf(initialAmount);
        fields[4] = String.valueOf(numPayments);
        fields[5] = String.valueOf(remainingBalance);

        new ERLogMsg(PAYMENT_PLAN_ACTIVATION_ERID, RECORD_CLASS, PAYMENT_PLAN_ACTIVATION_SERVICE_NAME, spid, fields)
                .log(ctx);
    }


    /**
     * Generates the payment plan payment ER
     * 
     * @param ctx
     * @param spid
     *            service provider
     * @param ban
     *            account number
     * @param paymentPlanID
     *            unique identifier of the Payment Plan selected
     * @param amount
     *            amount that is originally transferred to the Payment Plan Loan bucket
     * @param receiptNum
     *            The receipt number.
     * @param balance
     *            The balance.
     */
    public static void generatePaymentPlanPaymentEr(final Context ctx, final int spid, final String ban,
            final long paymentPlanID, final long amount, final long receiptNum, final long balance)
    {

        final String[] fields = new String[5];
        fields[0] = ban;
        fields[1] = String.valueOf(paymentPlanID);
        fields[2] = String.valueOf(amount);
        fields[3] = String.valueOf(receiptNum);
        fields[4] = String.valueOf(balance);

        new ERLogMsg(PAYMENT_PLAN_PAYMENT_ERID, RECORD_CLASS, PAYMENT_PLAN_PAYMENT_SERVICE_NAME, spid, fields).log(ctx);
    }


    /**
     * Adds double-quotes (blindly) to the given string.
     * 
     * @param value
     *            The string value. Null is treated as a blank string.
     * @return The given String value with double-quotes added around it.
     */
    public static String addDoubleQuotes(final String value)
    {
        final String quoted;

        if (value == null || value.length() == 0)
        {
            quoted = "\"\"";
        }
        else
        {
            quoted = "\"" + value + "\"";
        }

        return quoted;
    }


    public static void generateTransferDisputEr(Context ctx, TransferDispute dispute, String event, int resultCode)
    {
        final String[] fields = new String[17];
        String contMsisdn = "";
        String recipMsisdn = "";
        Subscriber cont = null;
        Subscriber recp = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd,HH:mm:ss");
        boolean resolved = dispute.getState().equals(TransferDisputeStatusEnum.ACCEPTED)
                || dispute.getState().equals(TransferDisputeStatusEnum.REJECTED);

        try
        {
            cont = SubscriberSupport.getSubscriber(ctx, dispute.getContSubId());
            recp = SubscriberSupport.getSubscriber(ctx, dispute.getRecpSubId());
            if (null != cont)
            {
                contMsisdn = cont.getMSISDN();
            }
            if (null != recp)
            {
                recipMsisdn = recp.getMSISDN();
            }
        }
        catch (Exception e)
        {
            LogSupport.minor(ctx, ERLogger.class.getName(), "Error trying to retrieve Subscription.", e);
        }

        fields[0] = contMsisdn;
        fields[1] = dispute.getContSubId();
        fields[2] = recipMsisdn;
        fields[3] = dispute.getRecpSubId();
        fields[4] = dispute.getExtTransactionId();
        fields[5] = String.valueOf(dispute.getContAmount());
        fields[6] = String.valueOf(dispute.getRecpAmount());
        fields[7] = dispute.isBlockBalanceFailed() ? "0" : String.valueOf(dispute.getBlockedBalance());
        fields[8] = resolved ? String.valueOf(dispute.getBlockedBalance()) : "";
        fields[9] = resolved ? String.valueOf(dispute.getRefundAmount()) : "";
        fields[10] = String.valueOf(dispute.getDisputeId());
        fields[11] = dateFormat.format(dispute.getTransferDate());
        fields[12] = dateFormat.format(dispute.getDisputeInitiatedDate());
        fields[13] = resolved ? dateFormat.format(dispute.getLastUpdatedDate()) : "";
        fields[14] = dispute.getState().getDescription();
        fields[15] = (event == null) ? "" : event;
        fields[16] = String.valueOf(resultCode);
        new ERLogMsg(TRANSFER_DISPUTE_ERID, RECORD_CLASS, TRANSFER_DISPUTE_NAME, cont != null ? cont.getSpid() : 0,
                fields).log(ctx);
    }


    public static void writeMultipleSubscribersInHistoryER(final Context ctx,
            final String subscribers,
            final PaymentException paymentException)
    {
        int spid = -1;
        try
        {
            Msisdn msisdn = MsisdnSupport.getMsisdn(ctx, paymentException.getMsisdn());
            spid = msisdn.getSpid();
        }
        catch (HomeException e)
        {
            new MinorLogMsg(ERLogger.class, "fail to find MSISDN" + paymentException.getMsisdn(), e).log(ctx);
        }

        final String[] fields = new String[10];

        fields[0] = paymentException.getAgent();
        fields[1] = paymentException.getMsisdn();
        fields[2] = paymentException.getBan();
        fields[3] = String.valueOf(Math.abs(paymentException.getAmount()));
        fields[4] = String.valueOf(paymentException.getAdjustmentType());
        fields[5] = formatERDateDayOnly(paymentException.getTransDate());
        fields[6] = paymentException.getExtTransactionId();
        fields[7] = subscribers;
        fields[8] = paymentException.getAgent();
        fields[9] = String.valueOf(PaymentFailureERHome.DUPLICATE_MSISDN_EXCEPTION);

        new ERLogMsg(
                TPS_MULTIPLE_SUBSCRIBERS_ERID,
                RECORD_CLASS,
                TPS_MULTIPLE_SUBSCRIBERS_SERVICE_NAME,
                spid,
                fields).log(ctx);
    }


    public static void logActivationER(Context ctx,
            int spid,
            String groupMsisdn,
            String voiceMsisdn,
            String faxMsisdn,
            String dataMsisdn,
            String subscriberId, //DZ:  newly added 
            String packageId,
            String imsi,
            String ban,
            long pricePlan,
            SubscriberStateEnum state,
            long deposit,
            long creditLimit,
            String currency,
            String lastName,
            String firstName,
            Date startDate,
            Date endDate,
            int billCycleDate,
            Set services,
            String dealerCode, //DZ:  newly added
            int creditCategory, //DZ:  newly added
            int transactionResultCode,
            int upsResultCode,
            int ecareResultCode,
            int ecpResultCode,
            int smsbResultCode,
            int hlrResultCode,
            long firstChargeAmount,
            int firstChargeCode,
            int pricePlanVersion,
            String postsupportMSISDN,
            String address1, //ali: newly added
            String address2,
            String address3,
            String city,
            String province,
            String country,
            String subscriberDealerCode,
            int discountClass,
            Date depositDate,
            short subscriberType,
            String billingLanguage,
            short billingOption,
            int activationCode,
            Date expiryDate,
            long initialBalance,
            int initialExpiryDateExtension,
            long reactivationFee,
            long maxBalanceAmount,
            long maxRechargeAmount,
            Date birthDay,
            long categoryId,
            long marketingId,
            Date marketingStartDate,
            Date marketingStopDate,
            long subscriptionType,
            long subscriptionClass,
            boolean pricePlanRestrictionOverride,
            long overdraftBalanceLimit)
    {
        User principal = (User) ctx.get(java.security.Principal.class);
        String user = (principal == null) ? "" : principal.getId();

        final SubscriptionActivationER er = new SubscriptionActivationER();
        er.setTimestamp(System.currentTimeMillis());
        er.setSpid(spid);
        er.setUserID(user);
        er.setGroupMobileNumber(groupMsisdn);
        er.setVoiceMobileNumber(voiceMsisdn);
        er.setFaxMobileNumber(faxMsisdn);
        er.setDataMobileNumber(dataMsisdn);
        er.setSubscriptionID(subscriberId);
        er.setPackageID(packageId);
        er.setIMSI(imsi);
        er.setBAN(ban);
        er.setPricePlan(pricePlan);
        er.setState(state.getIndex());
        er.setDeposit(deposit);
        er.setInitialCreditLimit(creditLimit);
        er.setCurrency(currency);
        er.setLastName(addDoubleQuotes(lastName));
        er.setFirstName(addDoubleQuotes(firstName));
        er.setSubscriptionStartDate(startDate);
        er.setSubscriptionEndDate(endDate);
        er.setBillCycleDay(billCycleDate);
        er.setServices(services.toString());
        er.setDealerCode(dealerCode);
        er.setCreditCategory(creditCategory);
        er.setTransactionResultCode(transactionResultCode);
        er.setUpsResultCode(upsResultCode);
        er.setEcareResultCode(ecareResultCode);
        er.setEcpResultCode(ecpResultCode);
        er.setSmsbResultCode(smsbResultCode);
        er.setHlrResultCode(hlrResultCode);
        er.setFirstChargeAmount(firstChargeAmount);
        er.setFirstChargeResultCode(firstChargeCode);
        er.setPricePlanVersion(pricePlanVersion);
        er.setPostpaidSupportMSISDN(postsupportMSISDN);
        er.setAddress1(addDoubleQuotes(address1));
        er.setAddress2(addDoubleQuotes(address2));
        er.setAddress3(addDoubleQuotes(address3));
        er.setCity(addDoubleQuotes(city));
        er.setProvince(addDoubleQuotes(province));
        er.setCountry(addDoubleQuotes(country));
        er.setSubscriptionDealerCode(subscriberDealerCode);
        er.setDiscountClass(discountClass);
        er.setDepositDate(depositDate);
        er.setBillingType(subscriberType);
        er.setBillingLanguage(billingLanguage);
        er.setBillingOption(billingOption);
        er.setActivationReasonCode(activationCode);
        er.setExpiryDate(expiryDate);
        er.setInitialBalance(initialBalance);
        er.setInitialExpiryDateExtention(initialExpiryDateExtension);
        er.setReactivationFee(reactivationFee);
        er.setMaxBalanceAmount(maxBalanceAmount);
        er.setMaxRechargeAmount(maxRechargeAmount);
        er.setDateOfBirth(birthDay);
        er.setSubscriptionCategory(categoryId);
        er.setMarketingCampain(marketingId);
        er.setMarketingCampainStartDate(marketingStartDate);
        er.setMarketingCampainEndDate(marketingStopDate);
        er.setSubscriptionType(subscriptionType);
        er.setSubscriptionClass(subscriptionClass);
        er.setPricePlanRestrictionOverridden(pricePlanRestrictionOverride);
        er.setOverdraftBalanceLimit(overdraftBalanceLimit);
        Service externalPricePlanservice = new Service();
        externalPricePlanservice = SubscriberServicesSupport.getExternalPricePlanServiceCode(ctx, subscriberId, false);
        if(externalPricePlanservice != null) {    
            er.setExternalServiceID(externalPricePlanservice.getID());
            er.setExternalServiceCode(externalPricePlanservice.getExternalServiceCode());
        }
        new ERLogMsg(ctx, er).log(ctx);

        try
        {
        	//To keep it consistent on how oldServices and newServices fields will be wrapped with double quotes storage in SubscriptionModification table. 
        	//Hence wrapping services field of SubscriptionActivation table also with double quotes.
        	//Can't add double quotes in ER 761 as it may break backward compatibility of ER 761 and cause other clients / reporting applications to fail, but can to SubscriptionActivation table.
        	er.setServices(addDoubleQuotes(services.toString()));
            HomeSupportHelper.get(ctx).createBean(ctx, er);
        }
        catch (HomeException e)
        {
            Logger.minor(ctx, ERLogger.class, "Error while saving SubscriptionActivationER to the DB table: "
                    + e.getMessage(), e);
        }
    }


    public static void logModificationER(Context ctx,
            int spid,
            Subscriber oldSub,
            Subscriber newSub,
            int oldFreeMinutes,
            int newFreeMinutes,
            int numberUsedMinutes,
            int adjustmentMinutes,
            long adjustmentAmount,
            int pricePlanChangeResultCode,
            SubscriberStateEnum oldState,
            SubscriberStateEnum newState,
            int stateChangeResultCode,
            long oldDeposit,
            long newDeposit,
            long oldCreditLimit,
            long newCreditLimit,
            int creditLimitResultCode,
            String oldCurrency,
            String newCurrency,
            Set oldServices,
            Set newServices,
            int serviceChangeResultCode,
            String oldSupportMsisdn,
            String newSupportMsisdn,
            final long leftoverBalance)
    {
        User principal = (User) ctx.get(java.security.Principal.class);
        String user = (principal == null) ? "" : principal.getId();

        boolean pricePlanRestrictionOverride = false;
        if (oldSub.getPricePlan() != newSub.getPricePlan())
        {
            PricePlan pricePlan = null;
            
            try
            {
                PricePlanVersion ppv;
                ppv = newSub.getPricePlan(ctx);
                if (ppv != null)
                {
                    pricePlan = ppv.getPricePlan(ctx);
                }
            }
            catch (HomeException e)
            {
                new MinorLogMsg(ERLogger.class, "Error retrieving price plan " + newSub.getPricePlan() + " for subscription " + newSub.getId(), e).log(ctx);
            }
            
            if (pricePlan != null)
            {
                pricePlanRestrictionOverride = pricePlan.isRestrictionViolation(ctx, newSub);
            }
        }
        
        long interfaceId =ctx.getBoolean(Lookup.FUTURE_DATED_PRICEPLAN_CHANGE, false) ? 1 : 0;
        
        final SubscriptionModificationER er = new SubscriptionModificationER();
        
        er.setTimestamp(System.currentTimeMillis());
        er.setSpid(spid);
        er.setUserID(user);
        er.setOldGroupMobileNumber(oldSub.getGroupMSISDN(ctx));
        er.setNewGroupMobileNumber(newSub.getGroupMSISDN(ctx));
        er.setOldVoiceMobileNumber(oldSub.getMSISDN());
        er.setNewVoiceMobileNumber(newSub.getMSISDN());
        er.setOldFaxMobileNumber(oldSub.getFaxMSISDN());
        er.setNewFaxMobileNumber(newSub.getFaxMSISDN());
        er.setOldDataMobileNumber(oldSub.getDataMSISDN());
        er.setNewDataMobileNumber(newSub.getDataMSISDN());
        er.setOldIMSI(oldSub.getIMSI());
        er.setNewIMSI(newSub.getIMSI());
        er.setOldSPID(oldSub.getSpid());
        er.setNewSPID(newSub.getSpid());
        er.setOldBAN(oldSub.getBAN());
        er.setNewBAN(newSub.getBAN());
        er.setOldPricePlan(oldSub.getPricePlan());
        er.setNewPricePlan(newSub.getPricePlan());
        er.setOldFreeMinutes(oldFreeMinutes);
        er.setNewFreeMinutes(newFreeMinutes);
        er.setUsedFreeMinutes(numberUsedMinutes);
        er.setAdjustmentMinutes(adjustmentMinutes);
        er.setAdjustmentAmount(adjustmentAmount);
        er.setPricePlanChangeResultCode(pricePlanChangeResultCode);
        er.setOldState(oldState.getIndex());
        er.setNewState(newState.getIndex());
        er.setStateChangeResultCode(stateChangeResultCode);
        er.setOldDeposit(oldDeposit);
        er.setNewDeposit(newDeposit);
        er.setOldCreditLimit(oldCreditLimit);
        er.setNewCreditLimit(newCreditLimit);
        er.setCreditLimitResultCode(creditLimitResultCode);
        er.setOldCurrency(oldCurrency);
        er.setNewCurrency(newCurrency);
        er.setOldServices(addDoubleQuotes(oldServices.toString()));
        er.setNewServices(addDoubleQuotes(newServices.toString()));
        er.setServicesChangeResultCode(serviceChangeResultCode);
        er.setOldBillingType(oldSub.getSubscriberType().getIndex());
        er.setNewBillingType(newSub.getSubscriberType().getIndex());
        er.setOldPostpaidSupportMSISDN(oldSupportMsisdn);
        er.setNewPostpaidSupportMSISDN(newSupportMsisdn);
        er.setOldSubscriptionDealerCode(oldSub.getDealerCode());
        er.setNewSubscriptionDealerCode(newSub.getDealerCode());
        er.setOldDiscountClass(oldSub.getDiscountClass());
        er.setNewDiscountClass(newSub.getDiscountClass());
        er.setOldDepositDate(oldSub.getDepositDate());
        er.setNewDepositDate(newSub.getDepositDate());
        er.setOldSubscriptionStartDate(oldSub.getStartDate());
        er.setNewSubscriptionStartDate(newSub.getStartDate());
        er.setOldSubscriptionEndDate(oldSub.getEndDate());
        er.setNewSubscriptionEndDate(newSub.getEndDate());
        er.setOldBillingLanguage(oldSub.getBillingLanguage());
        er.setNewBillingLanguage(newSub.getBillingLanguage());
        er.setOldPackageID(oldSub.getPackageId());
        er.setNewPackageID(newSub.getPackageId());
        er.setSubscriptionID(newSub.getId());
        er.setSubscriptionCreateDate(newSub.getDateCreated());
        er.setBillingOption(newSub.getBillingOption().getIndex());
        er.setOldExpiryDate(oldSub.getExpiryDate());
        er.setNewExpiryDate(newSub.getExpiryDate());
        er.setOldReactivationFee(oldSub.getReactivationFee());
        er.setNewReactivationFee(newSub.getReactivationFee());
        er.setOldAboveCreditLimit(String.valueOf(oldSub.isAboveCreditLimit()));
        er.setNewAboveCreditLimit(String.valueOf(newSub.isAboveCreditLimit()));
        er.setOldSubscriptionCategory(oldSub.getSubscriberCategory());
        er.setNewSubscriptionCategory(newSub.getSubscriberCategory());
        er.setOldMarketingCampain(oldSub.getMarketingCampaignBean().getMarketingId());
        er.setNewMarketingCampain(newSub.getMarketingCampaignBean().getMarketingId());
        er.setOldMarketingCampainStartDate(oldSub.getMarketingCampaignBean().getStartDate());
        er.setNewMarketingCampainStartDate(newSub.getMarketingCampaignBean().getStartDate());
        er.setOldMarketingCampainEndDate(oldSub.getMarketingCampaignBean().getEndDate());
        er.setNewMarketingCampainEndDate(newSub.getMarketingCampaignBean().getEndDate());
        er.setLeftoverBalance(leftoverBalance);
        er.setOldAuxiliaryServices(addDoubleQuotes(oldSub.getAuxiliaryServiceIds(ctx).toString()));
        er.setNewAuxiliaryServices(addDoubleQuotes(newSub.getAuxiliaryServiceIds(ctx).toString()));
        er.setPricePlanRestrictionOverridden(pricePlanRestrictionOverride);
		er.setOldMonthlySpendLimit(oldSub.getMonthlySpendLimit());
		er.setNewMonthlySpendLimit(newSub.getMonthlySpendLimit());
        er.setOldOverdraftBalanceLimit(oldSub.getOverdraftBalanceLimit(ctx));
        er.setNewOverdraftBalanceLimit(newSub.getOverdraftBalanceLimit(ctx));
        Service oldService = new Service();
        Service newService = new Service();
        Map<Long, SubscriberServices> provisionedServices = oldSub.getProvisionedServicesBackup(ctx);
        if(provisionedServices != null)
        {
            Collection<SubscriberServices> services  = provisionedServices.values();
            if(services != null)
            {
                oldService = SubscriberServicesSupport.getExternalPricePlanServiceCode(ctx, services, false);
            }
        }
        if(oldService != null)
        {
            er.setOldExternalServiceID(oldService.getID());
            er.setOldExternalServiceCode(oldService.getExternalServiceCode());
        }
        newService = SubscriberServicesSupport.getExternalPricePlanServiceCode(ctx, newSub.getId(), false);
        if(newService != null) {
            er.setNewExternalServiceID(newService.getID());
            er.setNewExternalServiceCode(newService.getExternalServiceCode());
        }
        
        er.setInterfaceId(interfaceId);
        er.setSuspensionReasonCode(newSub.getSuspensionReason());
        
        new ERLogMsg(ctx, er).log(ctx);

        try
        {
            HomeSupportHelper.get(ctx).createBean(ctx, er);
        }
        catch (HomeException e)
        {
            Logger.minor(ctx, ERLogger.class, "Error while saving SubscriptionModificationER to the DB table: "
                    + e.getMessage(), e);
        }
    }


    public static void logActivationER(Context ctx, Subscriber subscriber) throws HomeException
    {
        PricePlanVersion pricePlanVersion = getSubscriberPricePlanVersion(ctx, subscriber);
        Account account = (Account) ctx.get(Lookup.ACCOUNT);
        int billCycleDate = SubscriberSupport.getBillCycleDay(ctx, subscriber);
        
        boolean pricePlanRestrictionOverride = false;
        PricePlan pricePlan = pricePlanVersion.getPricePlan(ctx);
        if (pricePlan != null)
        {
            pricePlanRestrictionOverride = pricePlan.isRestrictionViolation(ctx, subscriber);
        }
        
        //		 Generate Subscriber Activate ER with result code 3009
        logActivationER(ctx,
                subscriber.getSpid(),
                subscriber.getGroupMSISDN(ctx),
                subscriber.getMSISDN(),
                subscriber.getFaxMSISDN(),
                subscriber.getDataMSISDN(),
                subscriber.getId(),
                subscriber.getPackageId(),
                subscriber.getIMSI(),
                subscriber.getBAN(),
                subscriber.getPricePlan(),
                subscriber.getState(),
                subscriber.getDeposit(ctx),
                subscriber.getCreditLimit(ctx),
                account.getCurrency(),
                "",
                "",
                subscriber.getStartDate(),
                subscriber.getEndDate(),
                billCycleDate,
                pricePlanVersion.getServices(ctx),
                account.getDealerCode(),
                account.getCreditCategory(),
                SubscriberProvisionResultCode.getProvisionLastResultCode(ctx),
                // transaction
                SubscriberProvisionResultCode.getProvisionUpsErrorCode(ctx),
                // ups
                SubscriberProvisionResultCode.getProvisionCrmResultCode(ctx),
                // ecare
                SubscriberProvisionResultCode.getProvisionEcpErrorCode(ctx),
                // ecp
                SubscriberProvisionResultCode.getProvisionSMSBErrorCode(ctx),
                // smsb
                SubscriberProvisionResultCode.getProvisionHlrResultCode(ctx),
                // hlr
                // first charge amount
                SubscriberProvisionResultCode.getChargeAmount(ctx),
                0,
                subscriber.getPricePlanVersion(),

                subscriber.getSupportMSISDN(ctx),
                "",
                "",
                "",
                "",
                "",
                "",
                subscriber.getDealerCode(),
                subscriber.getDiscountClass(),
                subscriber.getDepositDate(),
                subscriber.getSubscriberType().getIndex(),
                subscriber.getBillingLanguage(),
                subscriber.getBillingOption().getIndex(),

                subscriber.getReasonCode(),

                subscriber.getExpiryDate(),
                subscriber.getInitialBalance(),
                0,
                subscriber.getReactivationFee(),
                subscriber.getMaxBalance(),
                subscriber.getMaxRecharge(),
                null,
                subscriber.getSubscriberCategory(),
                subscriber.getMarketingCampaignBean().getMarketingId(),
                subscriber.getMarketingCampaignBean().getStartDate(),
                subscriber.getMarketingCampaignBean().getEndDate(),
                subscriber.getSubscriptionType(),
                subscriber.getSubscriptionClass(),
                pricePlanRestrictionOverride,
                subscriber.getOverdraftBalanceLimit(ctx));

    }
    
    
    private static PricePlanVersion getSubscriberPricePlanVersion(Context ctx, Subscriber subscriber) throws HomeException
    {
        if(ctx.get(PricePlanVersion.class) != null)
        {
            return (PricePlanVersion) ctx.get(PricePlanVersion.class);
        }
        else
        {
            try
            {
                return subscriber.getPricePlan(ctx);
            }
            catch (HomeException e)
            {
                String message = "Error while fetching priceplan version" +
                        " for subscriber with id: "+subscriber.getId()+" . Error: "+e.getMessage();
                LogSupport.major(ctx,ERLogger.class.getName() , message);
                
                throw new HomeException(message);
            }
        }
    }


    public static void logPortInER(Context ctx, Subscriber subscriber) throws HomeException
    {
        new ERLogMsg(1157, 1100, "Number Portability Port-In", subscriber.getSpid(), 
                Arrays.asList(subscriber.getBAN(), subscriber.getId(), subscriber.getMsisdn(), subscriber.getIMSI()).toArray(new String [0])).log(ctx);

    }
    
    public static void logPortOutER(Context ctx, Subscriber subscriber) throws HomeException
    {
        new ERLogMsg(1158, 1100, "Number Portability Port-Out", subscriber.getSpid(), 
                Arrays.asList(subscriber.getBAN(), subscriber.getId(), subscriber.getMsisdn(), subscriber.getIMSI()).toArray(new String [0])).log(ctx);

    }
    
    
    public static void logSnapBackER(Context ctx, Msisdn msisdn) throws HomeException
    {
        new ERLogMsg(1159, 1100, "Number Portability Snap Back In", msisdn.getSpid(), 
                Arrays.asList(msisdn.getMsisdn()).toArray(new String [0])).log(ctx);

    }
    


    public static void logRemovalER(Context ctx, int spid, String voiceMsisdn, String ban, long subscriptionClass, int resultCode)
    {
        new ERLogMsg(
                763,
                700,
                "Subscriber Removal Event",
                spid,
                new String[]
                    {voiceMsisdn, ban, String.valueOf(subscriptionClass), String.valueOf(resultCode)}).log(ctx);
    }


    public static void logMarketingCampaignER(Context ctx, int spid, long id, short operation)
    {
        new ERLogMsg(1112, 700, "Marketing Campaign Add-Update-Delete", spid, new String[]
            {
                    String.valueOf(id),
                    String.valueOf(operation)}).log(ctx);
    }


    /**
     * @param ctx
     * @param newSub
     * @throws HomeException
     */
    public static void logModificationER(Context ctx, final Subscriber newSub) throws HomeException
    {
        Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLD_FROZEN_SUBSCRIBER);
        Account account = (Account) ctx.get(Lookup.ACCOUNT);

        long leftoverBalance = 0;
        if (oldSub.getState() != SubscriberStateEnum.INACTIVE
                && newSub.getState() == SubscriberStateEnum.INACTIVE)
        {
            // only populate the value if deactivation happened
            leftoverBalance = BalanceManagementSupport.getSubscriptionBalance(ctx,
                    ERLogger.class, oldSub);
        }

        int resultPricePlan = 0;
        int resultStateChange = 0;
        int resultCreditLimit = SubscriberProvisionResultCode.getProvisionCreditLimitResultCode(ctx);
        int resultServices = 0;
        int adjustMin = SubscriberProvisionResultCode.getProvisionAdjustMinutes(ctx);
        long adjustAmt = SubscriberProvisionResultCode.getChargeAmount(ctx);

        if (oldSub != null && newSub != null)
        {
            if (!EnumStateSupportHelper.get(ctx).stateEquals(oldSub, newSub))
            {
                resultStateChange = SubscriberProvisionResultCode.getProvisionLastResultCode(ctx);
            }
            if (!SubscriberSupport.isSamePricePlanVersion(ctx, oldSub, newSub))
            {
                resultPricePlan = SubscriberProvisionResultCode.getProvisionLastResultCode(ctx);
            }
        }

        String oldSupportMsisdn = "";
        String newSupportMsisdn = "";
        oldSupportMsisdn = oldSub.getSupportMSISDN(ctx);
        newSupportMsisdn = newSub.getSupportMSISDN(ctx);

        Subscriber tOldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
        final PricePlanVersion oldPricePlan = tOldSub.getPricePlan(ctx);
        final PricePlanVersion newPricePlan = newSub.getPricePlan(ctx);

        logModificationER(ctx,
                newSub.getSpid(),
                oldSub,
                newSub,
                0,
                0,
                0,
                adjustMin,
                adjustAmt,
                resultPricePlan,
                oldSub.getState(),
                newSub.getState(),
                resultStateChange,
                oldSub.getDeposit(ctx),
                newSub.getDeposit(ctx),
                oldSub.getCreditLimit(ctx),
                newSub.getCreditLimit(ctx),
                resultCreditLimit,
                account.getCurrency(),
                account.getCurrency(),
                oldPricePlan.getServices(ctx),
                newPricePlan.getServices(ctx),
                resultServices,
                oldSupportMsisdn,
                newSupportMsisdn,
                leftoverBalance // don't what this field for. 
        );
    }


    public static void logOutOfSync10339(Context ctx, Subscriber sub, Exception e, Object caller, int errCode)
    {
        new EntryLogMsg(
                10339,
                caller,
                "",
                sub.toString(),
                new String[]
                    {"" + errCode},
                e).log(ctx);
    }
    
    
    public static void logPRBTBulkProvisioningER(Context ctx, Date date, int spid, String msisdn, String imsi,
            String userId, String subId, long extNetEleCode,
            long extProSerCode, long tcbStatusCode)
    {
        new ERLogMsg(1148, 1100, "PRBT Service Provisioning:1:0", spid, new String[]
            {userId, msisdn, imsi, subId, String.valueOf(extNetEleCode), String.valueOf(extProSerCode),
                    String.valueOf(tcbStatusCode)}).log(ctx);
    }
    

    /**
     * 
     * @param ctx
     * @param spid
     * @param agentName
     * @param fileName
     * @param counter
     * @param success
     * @param templateID
     */
    public static void generateSimcardHLRLoaderEr(final Context ctx, final int spid, final String agentName,
            final String fileName, final long counter, final long success, final long templateID)
    {

        final String[] fields = new String[5];
        fields[0] = agentName;
        fields[1] = fileName;
        fields[2] = String.valueOf(counter);
        fields[3] = String.valueOf(success);
        fields[4] = String.valueOf(templateID);

        if (fields[1].length()> 256)
        {
        	fields[1] = fields[1].substring(0, 255); 
        }

        new ERLogMsg(SIMCARD_HLR_LOADER_ERID, RECORD_CLASS_1100, SIMCARD_HLR_LOADER_NAME, spid, fields).log(ctx);
    }
    
    
    /**
     * 
     * @param ctx
     * @param spid
     * @param agentName
     * @param msisdn
     * @param old_msisdn
     * @param imsi
     * @param profileCode
     * @param comment
     * @param crmHlrCode
     */
    public static void generateHlrCommandInteractionEr(final Context ctx, 
    		final int spid, final String agentName,
            final String msisdn, final String old_msisdn, final String imsi, final String old_imsi, final Long profileCode, 
            final String comment, final long crmHlrCode)
    {


    	final String[] fields = new String[8];
        fields[0] = agentName;
        fields[1] = (msisdn==null)?"":msisdn;
        fields[2] = (old_msisdn==null)?"":old_msisdn;
        fields[3]= (imsi==null)?"":imsi; 
        fields[4] = (old_imsi==null)?"":old_imsi;
        fields[5] = (profileCode==null)?"":profileCode.toString();
        fields[6] = (comment==null)?"":comment;
        fields[7] = String.valueOf(crmHlrCode); 
        
        if (fields[6].length()> 256)
        {
        	fields[6] = fields[6].substring(0, 255); 
        }
        
        new ERLogMsg(HLR_COMMAND_INTERACTION_ERID, RECORD_CLASS_1100, HLR_COMMAND_INTERACTION_Name, spid, fields).log(ctx);
    }
    
    
    /**
     * Log ER for Group Account conversion events . After conversion, One ER is generated for Online process and 
     * one for offline process. 
     * @param ctx
     * @param oldAccount
     * @param newAccount
     * @param message
     * @param cls
     */
    public static <T> void logIndividualToGroupAccountConvertER(Context ctx, Account oldAccount, Account newAccount,String processId, String statusCode,  String message, Class<T> cls) 
    {
        
        final String[] fields = new String[7];
        fields[0] = oldAccount.getBAN();
        fields[1] = Integer.toString(newAccount.getGroupType().getIndex());
        String newBan = "";
    	if(!newAccount.getBAN().contains(MoveConstants.DEFAULT_MOVE_PREFIX))
    	{
    		newBan = newAccount.getBAN();
    	}
        fields[2] = newBan;
        fields[3] = processId;
        fields[4] = statusCode;
        fields[5] = message; 
        fields[6] = SystemSupport.getAgent(ctx);
        
        new ERLogMsg(CONVERT_ACCOUNT_TO_GROUP_ERID, RECORD_CLASS_1100, CONVERT_ACCOUNT_TO_GROUP_ER_NAME, oldAccount.getSpid(), fields).log(ctx);

    }
    /**
     * Log ER for Notification sent on Voucher Top Up.
     * @param ctx
     * @param msisdn
     * @param amount
     * @param voucherNumber
     * @param sub
     * @param result
     */
    public static void createVoucherNotifiactionEr(final Context ctx, final String msisdn,final String amount,final String voucherNumber,  final Subscriber sub,
             final String notificationMethod, final String notificationEvent, final int result)
    {
    	Map<String,String> additionalInfo = new HashMap<String,String>();
    	additionalInfo.put(VOUCHER_NUMBER_KEY, voucherNumber);
    	additionalInfo.put(VOUCHER_VALUE_KEY, amount);
        createNotifiactionEr(ctx, sub ,notificationEvent,notificationMethod,result, additionalInfo);
    }
    /**
     * Log ER for Notification Of Service state change.
     * @param ctx
     * @param sub
     * @param service
     * @param auxService
     * @param result
     * @param state
     */
    public static void createServiceStateNotifiactionEr(final Context ctx, final Subscriber sub,final Service service, final AuxiliaryService auxService,
             final String notificationMethod,final int result, final ServiceStateEnum state)
    { 
    	Map<String,String> additionalInfo = new HashMap<String,String>();
        if(service!=null){
        	additionalInfo.put(SERVICE_TYPE_KEY, SERVICE_NAME_KEY);
        	additionalInfo.put(SERVICE_ID_KEY, String.valueOf(service.getID()));
        	additionalInfo.put(SERVICE_NAME_KEY, String.valueOf(service.getName()));
        }
        if(auxService != null){
        	additionalInfo.put(SERVICE_TYPE_KEY, "Auxilary Service");
        	additionalInfo.put(SERVICE_ID_KEY, String.valueOf(auxService.getID()));
        	additionalInfo.put(SERVICE_NAME_KEY, String.valueOf(auxService.getName()));
        }
        additionalInfo.put(NEW_STATE_KEY, state.getDescription());
        createNotifiactionEr(ctx, sub ,NotificationTypeEnum.SERVICE_STATE_CHANGE.getDescription(),notificationMethod,result, additionalInfo);
    }
    /**
     * Log ER for Service Pre-expiry and Pre-Recurrance Notification.
     * @param ctx
     * @param fee
     * @param endDate
     * @param sub
     * @param service
     * @param auxService
     * @param result
     */
    public static void createPreExpiryPreRecurrenceEr(final Context ctx, final String fee,final Date endDate,final Subscriber sub,final Service service, final AuxiliaryService auxService,
    		final String notificationEvent,final String notificationMethod, final int result)
    {
    	Map<String,String> additionalInfo = new HashMap<String,String>();
        if(service!=null){
        	additionalInfo.put(SERVICE_TYPE_KEY, "Service");
        	additionalInfo.put(SERVICE_ID_KEY, String.valueOf(service.getID()));
        	additionalInfo.put(SERVICE_NAME_KEY, String.valueOf(service.getName()));
        }
        if(auxService != null){
        	additionalInfo.put(SERVICE_TYPE_KEY, "Auxilary Service");
        	additionalInfo.put(SERVICE_ID_KEY, String.valueOf(auxService.getID()));
        	additionalInfo.put(SERVICE_NAME_KEY, String.valueOf(auxService.getName()));
        }
        additionalInfo.put(FEE_KEY, fee);
        additionalInfo.put(END_DATE_KEY, String.valueOf(endDate));
        createNotifiactionEr(ctx, sub ,NotificationTypeEnum.STATE_CHANGE.getDescription(),notificationMethod,result, additionalInfo);
    }
    /**
     * Log ER for Sybscriber State change Notification Event
     * @param ctx
     * @param oldSub
     * @param newSub
     * @param result
     */
    public static void createSubscriberStateChangeNotifiactionEr(final Context ctx, final Subscriber oldSub,final Subscriber newSub,final String notificationMethod,final int result)
   { 
    	Map<String,String> additionalInfo = new HashMap<String,String>();
    	SubscriberStateEnum oldSubState= null;
       if(oldSub!=null)
       {
    	   oldSubState	=	oldSub.getState();
    	   additionalInfo.put(OLD_STATE_KEY, String.valueOf(oldSub.getState().getDescription()));
       }
       else
       {
    	   additionalInfo.put(OLD_STATE_KEY,null);
       }
       if(newSub.getState().equals(oldSubState)){
    	   return;
       }
       additionalInfo.put(NEW_STATE_KEY,newSub.getState().getDescription());
       createNotifiactionEr(ctx, newSub,NotificationTypeEnum.STATE_CHANGE.getDescription(),notificationMethod,result, additionalInfo);
   }
    /**
     * create ER log for all type of notifications.
     * @param ctx
     * @param msisdn
     * @param amount
     * @param voucherNumber
     * @param sub
     * @param result
     */
    public static void createNotifiactionEr(final Context ctx, final Subscriber sub,final String notificationEvent,final String notificationMethod,
    		final int result, final Map additionalInfo)
  {
      final String[] fields = new String[4]; 
      fields[0] = String.valueOf(sub.getMsisdn());
      fields[1] = notificationEvent;
      fields[2] = notificationMethod;
      StringBuffer additionalInfoString =new StringBuffer();
      if(additionalInfo.size()>0)
      {
    	  additionalInfoString.append("|");
    	  for (Iterator iterator = additionalInfo.entrySet().iterator(); iterator.hasNext();)
    	  {
			Map.Entry entry = (Map.Entry) iterator.next();
			additionalInfoString .append(entry.getKey()+"="+entry.getValue()+"|");
    	  }
      }
      fields[3] = String.valueOf(additionalInfoString);
      new ERLogMsg(NOTIFICATION_ERID, RECORD_CLASS, NOTIFICATION_NAME, sub.getSpid(), fields)
              .log(ctx);
  }
    /**
     * create ER log for TFA notification.
     * @param ctx
     * @param sub
     * @param amount
     * @param adjustmentType
     * @param notificationMethod
     * @param notificationEvent
     * @param result
     */
    
    public static void createTFANotifiactionEr(final Context ctx,final Subscriber sub, final String amount,final String adjustmentType,  
            final String notificationMethod, final String notificationEvent, final int result)
   {
	   	Map<String,String> additionalInfo = new HashMap<String,String>();
	   	additionalInfo.put(TRANSFER_AMOUNT, amount);
	   	additionalInfo.put(ADJUSTMENT_TYPE_KEY, adjustmentType);
	    createNotifiactionEr(ctx, sub ,notificationEvent,notificationMethod,result, additionalInfo);
   }
    
   /**
    *  Log ER on Auto Top up registration or de-registration for a subscriber 
    *  @param ctx
    *  @param sub
    *  @param action
    *  @throws HomeException 
    */
    
    public static void createATUReistrationDeregistrationER(final Context ctx, final Subscriber sub, int action) throws HomeException
    {
    	final int spid = sub.getSpid();
    	
    	final String[] fields = new String[7];

    	fields[0] = sub.getMsisdn();
    	fields[1] = sub.getBAN();
    	fields[2] = String.valueOf(action);
    	
		ServiceFee2 primarySvcFee = SubscriptionSupport.getProvisionedPrimaryService(ctx, sub);
		fields[3] = (primarySvcFee!=null ? String.valueOf(primarySvcFee.getServiceId()) : "");
		fields[4] = (primarySvcFee!=null ? String.valueOf(primarySvcFee.getService(ctx).getAdjustmentType()) : "");

		fields[5] = formatERDateWithTime(new Date());
		fields[6] = SystemSupport.getAgent(ctx);
    	
		new ERLogMsg(ATU_REGISTRATION_DEREGISTRATION_ERID, RECORD_CLASS, ATU_REGISTRATION_DEREGISTRATION_NAME, spid, fields).log(ctx);
    }

	public static void genrateERPriceTemplate(Context ctx,String response, PriceTemplate priceTemplate) {
    	
		final String[] fields = new String[3];
		fields[0] = String.valueOf(priceTemplate.getID());
		fields[1] = priceTemplate.getName();
		fields[2] = response;
		
		new ERLogMsg(PRICE_TEMPLATE_ERID, RECORD_CLASS_1100, PRICE_TEMPLATE, priceTemplate.getSpid(), fields).log(ctx);
    }

	public static void genrateERTechnicalServiceTemplate(Context ctx,
			String response, TechnicalServiceTemplate technicalServiceTemplate) {
    	
		final String[] fields = new String[6];
		fields[0] = String.valueOf(technicalServiceTemplate.getID());
		fields[1] = technicalServiceTemplate.getName();
		fields[2] = String.valueOf(technicalServiceTemplate.getServiceSubType());
		fields[3] = String.valueOf(technicalServiceTemplate.getSubscriptionType());
		fields[4] = response;
		
		new ERLogMsg(TECHNICAL_SERVICE_TEMPLATE_ERID, RECORD_CLASS_1100, TECHNICAL_SERVICE_TEMPLATE, technicalServiceTemplate.getSpid(), fields).log(ctx);
    }

	public static void genrateERprepareOffering(Context ctx,
			ExecutionStatus status, String messageId, OfferingIO offering) {
		
		final String[] fields = new String[4];
		fields[0] = status.getValue();
		fields[1] = messageId;
		fields[2] = offering.getBusinessKey();
		fields[3] = String.valueOf(offering.getBusinessKey());
		
		new ERLogMsg(PREPARE_OFFER_ERID, RECORD_CLASS_1100, PREPARE_OFFER, Integer.parseInt(offering.getSpid()), fields).log(ctx);		
			
	}

    public static void logForAutomaticGenrateRefundTask(Context ctx,Account account,Transaction tran,int errorCode,String errorDescription)
    {
    	 final String[] fields = new String[10];
    	 fields[0] = account.getBAN();
    	 fields[1] = String.valueOf(account.getStateChangeReason());
    	 fields[2]= String.valueOf(account.getLastStateChangeDate());
    	 if(tran !=null)
    	 {
    		 fields[3] = String.valueOf(tran.getAmount());
        	 fields[4] = String.valueOf(tran.getReceiptNum());
    	 }
    	 
    	 fields[5] = String.valueOf(errorCode);
    	 fields[6] = errorDescription;
    	 
    	 new ERLogMsg(GENERATE_AUTOMATIC_REFUND, RECORD_CLASS_1100, GENERATE_AUTOMATIC_REFUND_NAME, account.getSpid(), fields).log(ctx);
    	 
    }
    
    public static void generateDunningPolicyAssignmentER(Context ctx,int count)
    {
    	final String[] fields = new String[2];
    	fields[0] = String.valueOf(count);
    	new ERLogMsg(DUNNING_POLICY_ASSIGNEMENT_ERID, RECORD_CLASS_1100, DUNNING_POLICY_ASSIGNMENT_ER_NAME, -1, fields).log(ctx);
    	
    }    
    
}
