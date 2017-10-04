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

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.support.StringSeperator;
import com.trilogy.framework.xlog.er.ERSupport;

import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.support.ActivationReasonCodeSupport;


/**
 * {{{GENERATED_CODE}}}
 * Methods for reading and writing ERs.
 * This class was saved after being generated because the model is reused for ER generation and DB save.
 * For DB save model is specifying s-transient fields that are excluded from ER generation,
 * but we need them in the ER. Also Date fields need to be formatted differently.
 *
 * @author joel.hughes@redknee.com
 * @author victor.stratan@redknee.com
 */
public class CustomSubscriptionActivationERERSupport implements ERSupport
{
    protected static CustomSubscriptionActivationERERSupport instance__ = null;

    public static synchronized CustomSubscriptionActivationERERSupport instance()
    {
        if (instance__ == null)
        {
            instance__ = new CustomSubscriptionActivationERERSupport();
        }

        return instance__;
    }

    public String[] getFields(final Context ctx, final Object obj)
    {
        final SubscriptionActivationER bean = (SubscriptionActivationER) obj;

        final String[] fields = new String[60];
        int index = 0;

        fields[index++] = bean.getUserID();
        fields[index++] = bean.getGroupMobileNumber();
        fields[index++] = bean.getVoiceMobileNumber();
        fields[index++] = bean.getFaxMobileNumber();
        fields[index++] = bean.getDataMobileNumber();
        fields[index++] = bean.getSubscriptionID();
        fields[index++] = bean.getPackageID();
        fields[index++] = bean.getIMSI();
        fields[index++] = bean.getBAN();
        fields[index++] = String.valueOf(bean.getPricePlan());
        fields[index++] = SubscriberStateEnum.get((short)bean.getState()).getDescription();
        fields[index++] = String.valueOf(bean.getDeposit());
        fields[index++] = String.valueOf(bean.getInitialCreditLimit());
        fields[index++] = bean.getCurrency();
        fields[index++] = bean.getLastName();
        fields[index++] = bean.getFirstName();
        fields[index++] = ERLogger.formatERDateDayOnly(bean.getSubscriptionStartDate());
        fields[index++] = ERLogger.formatERDateDayOnly(bean.getSubscriptionEndDate());
        fields[index++] = String.valueOf(bean.getBillCycleDay());
        fields[index++] = bean.getServices();
        fields[index++] = bean.getDealerCode();
        fields[index++] = String.valueOf(bean.getCreditCategory());
        fields[index++] = String.valueOf(bean.getTransactionResultCode());
        fields[index++] = String.valueOf(bean.getUpsResultCode());
        fields[index++] = String.valueOf(bean.getEcareResultCode());
        fields[index++] = String.valueOf(bean.getEcpResultCode());
        fields[index++] = String.valueOf(bean.getSmsbResultCode());
        fields[index++] = String.valueOf(bean.getHlrResultCode());
        fields[index++] = String.valueOf(bean.getFirstChargeAmount());
        fields[index++] = String.valueOf(bean.getFirstChargeResultCode());
        fields[index++] = String.valueOf(bean.getPricePlanVersion());
        fields[index++] = bean.getPostpaidSupportMSISDN();
        fields[index++] = bean.getAddress1();
        fields[index++] = bean.getAddress2();
        fields[index++] = bean.getAddress3();
        fields[index++] = bean.getCity();
        fields[index++] = bean.getProvince();
        fields[index++] = bean.getCountry();
        fields[index++] = bean.getSubscriptionDealerCode();
        fields[index++] = String.valueOf(bean.getDiscountClass());
        fields[index++] = ERLogger.formatERDateDayOnly(bean.getDepositDate());
        fields[index++] = String.valueOf(bean.getBillingType());
        fields[index++] = bean.getBillingLanguage();
        fields[index++] = String.valueOf(bean.getBillingOption());
        fields[index++] = ActivationReasonCodeSupport.getActivationReasonCodeMessage(ctx, bean.getActivationReasonCode());
        fields[index++] = ERLogger.formatERDateDayOnly(bean.getExpiryDate());
        fields[index++] = String.valueOf(bean.getInitialBalance());
        fields[index++] = String.valueOf(bean.getInitialExpiryDateExtention());
        fields[index++] = String.valueOf(bean.getReactivationFee());
        fields[index++] = String.valueOf(bean.getMaxBalanceAmount());
        fields[index++] = String.valueOf(bean.getMaxRechargeAmount());
        fields[index++] = ERLogger.formatERDateDayOnly(bean.getDateOfBirth());
        fields[index++] = String.valueOf(bean.getSubscriptionCategory());
        fields[index++] = String.valueOf(bean.getMarketingCampain());
        fields[index++] = ERLogger.formatERDateDayOnly(bean.getMarketingCampainStartDate());
        fields[index++] = ERLogger.formatERDateDayOnly(bean.getMarketingCampainEndDate());
        fields[index++] = String.valueOf(bean.getSubscriptionType());
        fields[index++] = String.valueOf(bean.getSubscriptionClass());
        fields[index++] = String.valueOf(bean.isPricePlanRestrictionOverridden());
        fields[index++] = String.valueOf(bean.getOverdraftBalanceLimit());

        return fields;
    }


    public Object setFields(final Context ctx, final Object obj, final String[] fields)
    {
        SubscriptionActivationER bean = (SubscriptionActivationER) obj;

        String field = null;
        int index = 0;

        field = fields[index++];
        bean.setUserID((field == null) ? "" : field);
        field = fields[index++];
        bean.setGroupMobileNumber((field == null) ? "" : field);
        field = fields[index++];
        bean.setVoiceMobileNumber((field == null) ? "" : field);
        field = fields[index++];
        bean.setFaxMobileNumber((field == null) ? "" : field);
        field = fields[index++];
        bean.setDataMobileNumber((field == null) ? "" : field);
        field = fields[index++];
        bean.setSubscriptionID((field == null) ? "" : field);
        field = fields[index++];
        bean.setPackageID((field == null) ? "" : field);
        field = fields[index++];
        bean.setIMSI((field == null) ? "" : field);
        field = fields[index++];
        bean.setBAN((field == null) ? "" : field);
        field = fields[index++];
        bean.setPricePlan((field == null || field.length() == 0) ? 0L : Long.parseLong(field));
        field = fields[index++];
        bean.setState((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = fields[index++];
        bean.setDeposit((field == null || field.length() == 0) ? 0L : Long.parseLong(field));
        field = fields[index++];
        bean.setInitialCreditLimit((field == null || field.length() == 0) ? 0L : Long.parseLong(field));
        field = fields[index++];
        bean.setCurrency((field == null) ? "" : field);
        field = fields[index++];
        bean.setLastName((field == null) ? "" : field);
        field = fields[index++];
        bean.setFirstName((field == null) ? "" : field);
        field = fields[index++];
        bean.setSubscriptionStartDate(ERLogger.parseERDateDayOnlyNoException(ctx, field));
        field = fields[index++];
        bean.setSubscriptionEndDate(ERLogger.parseERDateDayOnlyNoException(ctx, field));
        field = fields[index++];
        bean.setBillCycleDay((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = fields[index++];
        bean.setServices((field == null) ? "" : field);
        field = fields[index++];
        bean.setDealerCode((field == null) ? "" : field);
        field = fields[index++];
        bean.setCreditCategory((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = fields[index++];
        bean.setTransactionResultCode((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = fields[index++];
        bean.setUpsResultCode((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = fields[index++];
        bean.setEcareResultCode((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = fields[index++];
        bean.setEcpResultCode((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = fields[index++];
        bean.setSmsbResultCode((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = fields[index++];
        bean.setHlrResultCode((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = fields[index++];
        bean.setFirstChargeAmount((field == null || field.length() == 0) ? 0L : Long.parseLong(field));
        field = fields[index++];
        bean.setFirstChargeResultCode((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = fields[index++];
        bean.setPricePlanVersion((field == null || field.length() == 0) ? 0L : Long.parseLong(field));
        field = fields[index++];
        bean.setPostpaidSupportMSISDN((field == null) ? "" : field);
        field = fields[index++];
        bean.setAddress1((field == null) ? "" : field);
        field = fields[index++];
        bean.setAddress2((field == null) ? "" : field);
        field = fields[index++];
        bean.setAddress3((field == null) ? "" : field);
        field = fields[index++];
        bean.setCity((field == null) ? "" : field);
        field = fields[index++];
        bean.setProvince((field == null) ? "" : field);
        field = fields[index++];
        bean.setCountry((field == null) ? "" : field);
        field = fields[index++];
        bean.setSubscriptionDealerCode((field == null) ? "" : field);
        field = fields[index++];
        bean.setDiscountClass((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = fields[index++];
        bean.setDepositDate(ERLogger.parseERDateDayOnlyNoException(ctx, field));
        field = fields[index++];
        bean.setBillingType((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = fields[index++];
        bean.setBillingLanguage((field == null) ? "" : field);
        field = fields[index++];
        bean.setBillingOption((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = fields[index++];
        bean.setActivationReasonCode((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = fields[index++];
        bean.setExpiryDate(ERLogger.parseERDateDayOnlyNoException(ctx, field));
        field = fields[index++];
        bean.setInitialBalance((field == null || field.length() == 0) ? 0L : Long.parseLong(field));
        field = fields[index++];
        bean.setInitialExpiryDateExtention((field == null || field.length() == 0) ? 0L : Long.parseLong(field));
        field = fields[index++];
        bean.setReactivationFee((field == null || field.length() == 0) ? 0L : Long.parseLong(field));
        field = fields[index++];
        bean.setMaxBalanceAmount((field == null || field.length() == 0) ? 0L : Long.parseLong(field));
        field = fields[index++];
        bean.setMaxRechargeAmount((field == null || field.length() == 0) ? 0L : Long.parseLong(field));
        field = fields[index++];
        bean.setDateOfBirth(ERLogger.parseERDateDayOnlyNoException(ctx, field));
        field = fields[index++];
        bean.setSubscriptionCategory((field == null || field.length() == 0) ? 0L : Long.parseLong(field));
        field = fields[index++];
        bean.setMarketingCampain((field == null || field.length() == 0) ? 0L : Long.parseLong(field));
        field = fields[index++];
        bean.setMarketingCampainStartDate(ERLogger.parseERDateDayOnlyNoException(ctx, field));
        field = fields[index++];
        bean.setMarketingCampainEndDate(ERLogger.parseERDateDayOnlyNoException(ctx, field));
        field = fields[index++];
        bean.setSubscriptionType((field == null || field.length() == 0) ? 0L : Long.parseLong(field));
        field = fields[index++];
        bean.setSubscriptionClass((field == null || field.length() == 0) ? 0L : Long.parseLong(field));
        field = fields[index++];
        bean.setPricePlanRestrictionOverridden((field == null || field.length() == 0) ? false : Boolean.parseBoolean(field));
        field = fields[index++];
        bean.setOverdraftBalanceLimit((field == null || field.length() == 0) ? -1L : Long.parseLong(field));

        return bean;
    }


    public Object setFields(Context ctx, Object obj, StringSeperator seperator)
    {
        SubscriptionActivationER bean = (SubscriptionActivationER) obj;
        String field = null;

        field = seperator.next();
        bean.setUserID((field == null) ? "" : field);
        field = seperator.next();
        bean.setGroupMobileNumber((field == null) ? "" : field);
        field = seperator.next();
        bean.setVoiceMobileNumber((field == null) ? "" : field);
        field = seperator.next();
        bean.setFaxMobileNumber((field == null) ? "" : field);
        field = seperator.next();
        bean.setDataMobileNumber((field == null) ? "" : field);
        field = seperator.next();
        bean.setSubscriptionID((field == null) ? "" : field);
        field = seperator.next();
        bean.setPackageID((field == null) ? "" : field);
        field = seperator.next();
        bean.setIMSI((field == null) ? "" : field);
        field = seperator.next();
        bean.setBAN((field == null) ? "" : field);
        field = seperator.next();
        bean.setPricePlan((field == null || field.length() == 0) ? 0L : Long.parseLong(field));
        field = seperator.next();
        bean.setState((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = seperator.next();
        bean.setDeposit((field == null || field.length() == 0) ? 0L : Long.parseLong(field));
        field = seperator.next();
        bean.setInitialCreditLimit((field == null || field.length() == 0) ? 0L : Long.parseLong(field));
        field = seperator.next();
        bean.setCurrency((field == null) ? "" : field);
        field = seperator.next();
        bean.setLastName((field == null) ? "" : field);
        field = seperator.next();
        bean.setFirstName((field == null) ? "" : field);
        field = seperator.next();
        bean.setSubscriptionStartDate(ERLogger.parseERDateDayOnlyNoException(ctx, field));
        field = seperator.next();
        bean.setSubscriptionEndDate(ERLogger.parseERDateDayOnlyNoException(ctx, field));
        field = seperator.next();
        bean.setBillCycleDay((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = seperator.next();
        bean.setServices((field == null) ? "" : field);
        field = seperator.next();
        bean.setDealerCode((field == null) ? "" : field);
        field = seperator.next();
        bean.setCreditCategory((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = seperator.next();
        bean.setTransactionResultCode((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = seperator.next();
        bean.setUpsResultCode((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = seperator.next();
        bean.setEcareResultCode((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = seperator.next();
        bean.setEcpResultCode((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = seperator.next();
        bean.setSmsbResultCode((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = seperator.next();
        bean.setHlrResultCode((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = seperator.next();
        bean.setFirstChargeAmount((field == null || field.length() == 0) ? 0L : Long.parseLong(field));
        field = seperator.next();
        bean.setFirstChargeResultCode((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = seperator.next();
        bean.setPricePlanVersion((field == null || field.length() == 0) ? 0L : Long.parseLong(field));
        field = seperator.next();
        bean.setPostpaidSupportMSISDN((field == null) ? "" : field);
        field = seperator.next();
        bean.setAddress1((field == null) ? "" : field);
        field = seperator.next();
        bean.setAddress2((field == null) ? "" : field);
        field = seperator.next();
        bean.setAddress3((field == null) ? "" : field);
        field = seperator.next();
        bean.setCity((field == null) ? "" : field);
        field = seperator.next();
        bean.setProvince((field == null) ? "" : field);
        field = seperator.next();
        bean.setCountry((field == null) ? "" : field);
        field = seperator.next();
        bean.setSubscriptionDealerCode((field == null) ? "" : field);
        field = seperator.next();
        bean.setDiscountClass((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = seperator.next();
        bean.setDepositDate(ERLogger.parseERDateDayOnlyNoException(ctx, field));
        field = seperator.next();
        bean.setBillingType((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = seperator.next();
        bean.setBillingLanguage((field == null) ? "" : field);
        field = seperator.next();
        bean.setBillingOption((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = seperator.next();
        bean.setActivationReasonCode((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = seperator.next();
        bean.setExpiryDate(ERLogger.parseERDateDayOnlyNoException(ctx, field));
        field = seperator.next();
        bean.setInitialBalance((field == null || field.length() == 0) ? 0L : Long.parseLong(field));
        field = seperator.next();
        bean.setInitialExpiryDateExtention((field == null || field.length() == 0) ? 0L : Long.parseLong(field));
        field = seperator.next();
        bean.setReactivationFee((field == null || field.length() == 0) ? 0L : Long.parseLong(field));
        field = seperator.next();
        bean.setMaxBalanceAmount((field == null || field.length() == 0) ? 0L : Long.parseLong(field));
        field = seperator.next();
        bean.setMaxRechargeAmount((field == null || field.length() == 0) ? 0L : Long.parseLong(field));
        field = seperator.next();
        bean.setDateOfBirth(ERLogger.parseERDateDayOnlyNoException(ctx, field));
        field = seperator.next();
        bean.setSubscriptionCategory((field == null || field.length() == 0) ? 0L : Long.parseLong(field));
        field = seperator.next();
        bean.setMarketingCampain((field == null || field.length() == 0) ? 0L : Long.parseLong(field));
        field = seperator.next();
        bean.setMarketingCampainStartDate(ERLogger.parseERDateDayOnlyNoException(ctx, field));
        field = seperator.next();
        bean.setMarketingCampainEndDate(ERLogger.parseERDateDayOnlyNoException(ctx, field));
        field = seperator.next();
        bean.setSubscriptionType((field == null || field.length() == 0) ? 0L : Long.parseLong(field));
        field = seperator.next();
        bean.setSubscriptionClass((field == null || field.length() == 0) ? 0L : Long.parseLong(field));
        field = seperator.next();
        bean.setPricePlanRestrictionOverridden((field == null || field.length() == 0) ? false : Boolean.parseBoolean(field));
        field = seperator.next();
        bean.setOverdraftBalanceLimit((field == null || field.length() == 0) ? -1L : Long.parseLong(field));

        return bean;
    }

}
