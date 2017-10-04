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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.trilogy.app.crm.bean.BillingOptionEnum;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.support.StringSeperator;
import com.trilogy.framework.xlog.er.ERSupport;


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
public class CustomSubscriptionModificationERERSupport implements ERSupport
{
    protected static CustomSubscriptionModificationERERSupport instance__ = null;

    public static synchronized CustomSubscriptionModificationERERSupport instance()
    {
        if (instance__ == null)
        {
            instance__ = new CustomSubscriptionModificationERERSupport();
        }

        return instance__;
    }


    @Override
    public String[] getFields(Context ctx, Object obj)
    {
        SubscriptionModificationER bean = (SubscriptionModificationER) obj;

        String[] fields = new String[96];
        int index = 0;

        fields[index++] = bean.getUserID();
        fields[index++] = bean.getOldGroupMobileNumber();
        fields[index++] = bean.getNewGroupMobileNumber();
        fields[index++] = bean.getOldVoiceMobileNumber();
        fields[index++] = bean.getNewVoiceMobileNumber();
        fields[index++] = bean.getOldFaxMobileNumber();
        fields[index++] = bean.getNewFaxMobileNumber();
        fields[index++] = bean.getOldDataMobileNumber();
        fields[index++] = bean.getNewDataMobileNumber();
        fields[index++] = bean.getOldIMSI();
        fields[index++] = bean.getNewIMSI();
        fields[index++] = String.valueOf(bean.getOldSPID());
        fields[index++] = String.valueOf(bean.getNewSPID());
        fields[index++] = bean.getOldBAN();
        fields[index++] = bean.getNewBAN();
        fields[index++] = String.valueOf(bean.getOldPricePlan());
        fields[index++] = String.valueOf(bean.getNewPricePlan());
        fields[index++] = String.valueOf(bean.getOldFreeMinutes());
        fields[index++] = String.valueOf(bean.getNewFreeMinutes());
        fields[index++] = String.valueOf(bean.getUsedFreeMinutes());
        fields[index++] = String.valueOf(bean.getAdjustmentMinutes());
        fields[index++] = String.valueOf(bean.getAdjustmentAmount());
        fields[index++] = String.valueOf(bean.getPricePlanChangeResultCode());
        fields[index++] = bean.getOldState()!=-1?SubscriberStateEnum.get((short) bean.getOldState()).getDescription():"-1";
        fields[index++] = bean.getNewState()!=-1?SubscriberStateEnum.get((short) bean.getNewState()).getDescription():"-1";
        fields[index++] = String.valueOf(bean.getStateChangeResultCode());
        fields[index++] = String.valueOf(bean.getOldDeposit());
        fields[index++] = String.valueOf(bean.getNewDeposit());
        fields[index++] = String.valueOf(bean.getOldCreditLimit());
        fields[index++] = String.valueOf(bean.getNewCreditLimit());
        fields[index++] = String.valueOf(bean.getCreditLimitResultCode());
        fields[index++] = bean.getOldCurrency();
        fields[index++] = bean.getNewCurrency();
        fields[index++] = bean.getOldServices();
        fields[index++] = bean.getNewServices();
        fields[index++] = String.valueOf(bean.getServicesChangeResultCode());
        fields[index++] = String.valueOf(bean.getOldBillingType());
        fields[index++] = String.valueOf(bean.getNewBillingType());
        fields[index++] = bean.getOldPostpaidSupportMSISDN();
        fields[index++] = bean.getNewPostpaidSupportMSISDN();
        fields[index++] = bean.getOldAddress1();
        fields[index++] = bean.getNewAddress1();
        fields[index++] = bean.getOldAddress2();
        fields[index++] = bean.getNewAddress2();
        fields[index++] = bean.getOldAddress3();
        fields[index++] = bean.getNewAddress3();
        fields[index++] = bean.getOldCity();
        fields[index++] = bean.getNewCity();
        fields[index++] = bean.getOldRegion();
        fields[index++] = bean.getNewRegion();
        fields[index++] = bean.getOldCountry();
        fields[index++] = bean.getNewCountry();
        fields[index++] = bean.getOldSubscriptionDealerCode();
        fields[index++] = bean.getNewSubscriptionDealerCode();
        fields[index++] = String.valueOf(bean.getOldDiscountClass());
        fields[index++] = String.valueOf(bean.getNewDiscountClass());
        fields[index++] = ERLogger.formatERDateDayOnly(bean.getOldDepositDate());
        fields[index++] = ERLogger.formatERDateDayOnly(bean.getNewDepositDate());
        fields[index++] = ERLogger.formatERDateDayOnly(bean.getOldSubscriptionStartDate());
        fields[index++] = ERLogger.formatERDateDayOnly(bean.getNewSubscriptionStartDate());
        fields[index++] = ERLogger.formatERDateDayOnly(bean.getOldSubscriptionEndDate());
        fields[index++] = ERLogger.formatERDateDayOnly(bean.getNewSubscriptionEndDate());
        fields[index++] = bean.getOldBillingLanguage();
        fields[index++] = bean.getNewBillingLanguage();
        fields[index++] = bean.getOldPackageID();
        fields[index++] = bean.getNewPackageID();
        fields[index++] = bean.getSubscriptionID();
        fields[index++] = ERLogger.formatERDateDayOnly(bean.getSubscriptionCreateDate());
        fields[index++] = bean.getFirstName();
        fields[index++] = bean.getLastName();
        fields[index++] = BillingOptionEnum.get((short) bean.getBillingOption()).getDescription();
        fields[index++] = ERLogger.formatERDateDayOnly(bean.getOldExpiryDate());
        fields[index++] = ERLogger.formatERDateDayOnly(bean.getNewExpiryDate());
        fields[index++] = String.valueOf(bean.getOldReactivationFee());
        fields[index++] = String.valueOf(bean.getNewReactivationFee());
        fields[index++] = ERLogger.formatERDateDayOnly(bean.getDateOfBirth());
        fields[index++] = bean.getOldAboveCreditLimit();
        fields[index++] = bean.getNewAboveCreditLimit();
        fields[index++] = String.valueOf(bean.getOldSubscriptionCategory());
        fields[index++] = String.valueOf(bean.getNewSubscriptionCategory());
        fields[index++] = String.valueOf(bean.getOldMarketingCampain());
        fields[index++] = String.valueOf(bean.getNewMarketingCampain());
        fields[index++] = ERLogger.formatERDateDayOnly(bean.getOldMarketingCampainStartDate());
        fields[index++] = ERLogger.formatERDateDayOnly(bean.getNewMarketingCampainStartDate());
        fields[index++] = ERLogger.formatERDateDayOnly(bean.getOldMarketingCampainEndDate());
        fields[index++] = ERLogger.formatERDateDayOnly(bean.getNewMarketingCampainEndDate());
        fields[index++] = String.valueOf(bean.getLeftoverBalance());
        fields[index++] = bean.getOldAuxiliaryServices();
        fields[index++] = bean.getNewAuxiliaryServices();
        fields[index++] = String.valueOf(bean.isPricePlanRestrictionOverridden());
		fields[index++] = String.valueOf(bean.getOldMonthlySpendLimit());
		fields[index++] = String.valueOf(bean.getNewMonthlySpendLimit());
        fields[index++] = String.valueOf(bean.getOldOverdraftBalanceLimit());
        fields[index++] = String.valueOf(bean.getNewOverdraftBalanceLimit());
        fields[index++] = String.valueOf(bean.getInterfaceId());
        fields[index++] = bean.getSuspensionReasonCode();

        return fields;
    }


    @Override
    public Object setFields(Context ctx, Object obj, String[] fields)
    {
        SubscriptionModificationER bean = (SubscriptionModificationER) obj;

        String field = null;
        int index = 0;

        field = fields[index++];
        bean.setUserID((field == null) ? "" : field);
        field = fields[index++];
        bean.setOldGroupMobileNumber((field == null) ? "" : field);
        field = fields[index++];
        bean.setNewGroupMobileNumber((field == null) ? "" : field);
        field = fields[index++];
        bean.setOldVoiceMobileNumber((field == null) ? "" : field);
        field = fields[index++];
        bean.setNewVoiceMobileNumber((field == null) ? "" : field);
        field = fields[index++];
        bean.setOldFaxMobileNumber((field == null) ? "" : field);
        field = fields[index++];
        bean.setNewFaxMobileNumber((field == null) ? "" : field);
        field = fields[index++];
        bean.setOldDataMobileNumber((field == null) ? "" : field);
        field = fields[index++];
        bean.setNewDataMobileNumber((field == null) ? "" : field);
        field = fields[index++];
        bean.setOldIMSI((field == null) ? "" : field);
        field = fields[index++];
        bean.setNewIMSI((field == null) ? "" : field);
        field = fields[index++];
        bean.setOldSPID((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = fields[index++];
        bean.setNewSPID((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = fields[index++];
        bean.setOldBAN((field == null) ? "" : field);
        field = fields[index++];
        bean.setNewBAN((field == null) ? "" : field);
        field = fields[index++];
        bean.setOldPricePlan((field == null || field.length() == 0) ? 0L : Long.parseLong(field));
        field = fields[index++];
        bean.setNewPricePlan((field == null || field.length() == 0) ? 0L : Long.parseLong(field));
        field = fields[index++];
        bean.setOldFreeMinutes((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = fields[index++];
        bean.setNewFreeMinutes((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = fields[index++];
        bean.setUsedFreeMinutes((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = fields[index++];
        bean.setAdjustmentMinutes((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = fields[index++];
        bean.setAdjustmentAmount((field == null || field.length() == 0) ? 0L : Long.parseLong(field));
        field = fields[index++];
        bean.setPricePlanChangeResultCode((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = fields[index++];

        int oldState = -1;
        int newState = -1;
        Iterator iter = SubscriberStateEnum.COLLECTION.iterator();
        while (iter.hasNext() && 
                ((oldState==-1 && field!=null && field.length()>0) || 
                 (newState==-1 && fields[index+1]!=null && fields[index+1].length()>0)
                )
              )
        {
            SubscriberStateEnum state = (SubscriberStateEnum) iter.next();
            if (oldState==-1 && field!=null && field.length()>0 && state.getDescription().equals(field))
            {
                oldState = state.getIndex();
            }

            if (newState==-1 && fields[index+1]!=null && fields[index+1].length()>0 && state.getDescription().equals(fields[index+1]))
            {
                newState = state.getIndex();
            }
        }
        
        bean.setOldState((field == null || field.length() == 0) ? -1 : oldState);
        field = fields[index++];
        bean.setNewState((field == null || field.length() == 0) ? -1 : newState);
        field = fields[index++];
        
        bean.setStateChangeResultCode((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = fields[index++];
        bean.setOldDeposit((field == null || field.length() == 0) ? 0L : Long.parseLong(field));
        field = fields[index++];
        bean.setNewDeposit((field == null || field.length() == 0) ? 0L : Long.parseLong(field));
        field = fields[index++];
        bean.setOldCreditLimit((field == null || field.length() == 0) ? 0L : Long.parseLong(field));
        field = fields[index++];
        bean.setNewCreditLimit((field == null || field.length() == 0) ? 0L : Long.parseLong(field));
        field = fields[index++];
        bean.setCreditLimitResultCode((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = fields[index++];
        bean.setOldCurrency((field == null) ? "" : field);
        field = fields[index++];
        bean.setNewCurrency((field == null) ? "" : field);
        field = fields[index++];
        bean.setOldServices((field == null) ? "" : field);
        field = fields[index++];
        bean.setNewServices((field == null) ? "" : field);
        field = fields[index++];
        bean.setServicesChangeResultCode((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = fields[index++];
        bean.setOldBillingType((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = fields[index++];
        bean.setNewBillingType((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = fields[index++];
        bean.setOldPostpaidSupportMSISDN((field == null) ? "" : field);
        field = fields[index++];
        bean.setNewPostpaidSupportMSISDN((field == null) ? "" : field);
        field = fields[index++];
        bean.setOldAddress1((field == null) ? "" : field);
        field = fields[index++];
        bean.setNewAddress1((field == null) ? "" : field);
        field = fields[index++];
        bean.setOldAddress2((field == null) ? "" : field);
        field = fields[index++];
        bean.setNewAddress2((field == null) ? "" : field);
        field = fields[index++];
        bean.setOldAddress3((field == null) ? "" : field);
        field = fields[index++];
        bean.setNewAddress3((field == null) ? "" : field);
        field = fields[index++];
        bean.setOldCity((field == null) ? "" : field);
        field = fields[index++];
        bean.setNewCity((field == null) ? "" : field);
        field = fields[index++];
        bean.setOldRegion((field == null) ? "" : field);
        field = fields[index++];
        bean.setNewRegion((field == null) ? "" : field);
        field = fields[index++];
        bean.setOldCountry((field == null) ? "" : field);
        field = fields[index++];
        bean.setNewCountry((field == null) ? "" : field);
        field = fields[index++];
        bean.setOldSubscriptionDealerCode((field == null) ? "" : field);
        field = fields[index++];
        bean.setNewSubscriptionDealerCode((field == null) ? "" : field);
        field = fields[index++];
        bean.setOldDiscountClass((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = fields[index++];
        bean.setNewDiscountClass((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = fields[index++];
        bean.setOldDepositDate(com.redknee.framework.xhome.support.DateUtil.parse(field));
        field = fields[index++];
        bean.setNewDepositDate(com.redknee.framework.xhome.support.DateUtil.parse(field));
        field = fields[index++];
        bean.setOldSubscriptionStartDate(com.redknee.framework.xhome.support.DateUtil.parse(field));
        field = fields[index++];
        bean.setNewSubscriptionStartDate(com.redknee.framework.xhome.support.DateUtil.parse(field));
        field = fields[index++];
        bean.setOldSubscriptionEndDate(com.redknee.framework.xhome.support.DateUtil.parse(field));
        field = fields[index++];
        bean.setNewSubscriptionEndDate(com.redknee.framework.xhome.support.DateUtil.parse(field));
        field = fields[index++];
        bean.setOldBillingLanguage((field == null) ? "" : field);
        field = fields[index++];
        bean.setNewBillingLanguage((field == null) ? "" : field);
        field = fields[index++];
        bean.setOldPackageID((field == null) ? "" : field);
        field = fields[index++];
        bean.setNewPackageID((field == null) ? "" : field);
        field = fields[index++];
        bean.setSubscriptionID((field == null) ? "" : field);
        field = fields[index++];
        bean.setSubscriptionCreateDate(com.redknee.framework.xhome.support.DateUtil.parse(field));
        field = fields[index++];
        bean.setFirstName((field == null) ? "" : field);
        field = fields[index++];
        bean.setLastName((field == null) ? "" : field);
        field = fields[index++];
        bean.setBillingOption((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = fields[index++];
        bean.setOldExpiryDate(com.redknee.framework.xhome.support.DateUtil.parse(field));
        field = fields[index++];
        bean.setNewExpiryDate(com.redknee.framework.xhome.support.DateUtil.parse(field));
        field = fields[index++];
        bean.setOldReactivationFee((field == null || field.length() == 0) ? 0L : Long.parseLong(field));
        field = fields[index++];
        bean.setNewReactivationFee((field == null || field.length() == 0) ? 0L : Long.parseLong(field));
        field = fields[index++];
        bean.setDateOfBirth(com.redknee.framework.xhome.support.DateUtil.parse(field));
        field = fields[index++];
        bean.setOldAboveCreditLimit((field == null) ? "" : field);
        field = fields[index++];
        bean.setNewAboveCreditLimit((field == null) ? "" : field);
        field = fields[index++];
        bean.setOldSubscriptionCategory((field == null || field.length() == 0) ? 0L : Long.parseLong(field));
        field = fields[index++];
        bean.setNewSubscriptionCategory((field == null || field.length() == 0) ? 0L : Long.parseLong(field));
        field = fields[index++];
        bean.setOldMarketingCampain((field == null || field.length() == 0) ? 0L : Long.parseLong(field));
        field = fields[index++];
        bean.setNewMarketingCampain((field == null || field.length() == 0) ? 0L : Long.parseLong(field));
        field = fields[index++];
        bean.setOldMarketingCampainStartDate(com.redknee.framework.xhome.support.DateUtil.parse(field));
        field = fields[index++];
        bean.setNewMarketingCampainStartDate(com.redknee.framework.xhome.support.DateUtil.parse(field));
        field = fields[index++];
        bean.setOldMarketingCampainEndDate(com.redknee.framework.xhome.support.DateUtil.parse(field));
        field = fields[index++];
        bean.setNewMarketingCampainEndDate(com.redknee.framework.xhome.support.DateUtil.parse(field));
        field = fields[index++];
        bean.setLeftoverBalance((field == null || field.length() == 0) ? 0L : Long.parseLong(field));
        field = fields[index++];
        bean.setOldAuxiliaryServices((field == null) ? "" : field);
        field = fields[index++];
        bean.setNewAuxiliaryServices((field == null) ? "" : field);
        field = fields[index++];
        bean.setPricePlanRestrictionOverridden((field == null || field.length() == 0) ? false : Boolean.parseBoolean(field));
		field = fields[index++];
		bean.setOldMonthlySpendLimit((field == null || field.length() == 0) ? 0L
		    : Long.parseLong(field));
		field = fields[index++];
		bean.setNewMonthlySpendLimit((field == null || field.length() == 0) ? 0L
		    : Long.parseLong(field));
        field = fields[index++];
        bean.setOldOverdraftBalanceLimit((field == null || field.length() == 0) ? -1L
            : Long.parseLong(field));
        field = fields[index++];
        bean.setNewOverdraftBalanceLimit((field == null || field.length() == 0) ? -1L
            : Long.parseLong(field));
        field = fields[index++];
        bean.setInterfaceId((field == null || field.length() == 0) ? -1L
            : Long.parseLong(field));
        field = fields[index++];
        bean.setSuspensionReasonCode((field == null) ? "" : field);

        return bean;
    }


    @Override
    public Object setFields(Context ctx, Object obj, StringSeperator seperator)
    {
        SubscriptionModificationER bean = (SubscriptionModificationER) obj;
        String field = null;

        field = seperator.next();
        bean.setUserID((field == null) ? "" : field);
        field = seperator.next();
        bean.setOldGroupMobileNumber((field == null) ? "" : field);
        field = seperator.next();
        bean.setNewGroupMobileNumber((field == null) ? "" : field);
        field = seperator.next();
        bean.setOldVoiceMobileNumber((field == null) ? "" : field);
        field = seperator.next();
        bean.setNewVoiceMobileNumber((field == null) ? "" : field);
        field = seperator.next();
        bean.setOldFaxMobileNumber((field == null) ? "" : field);
        field = seperator.next();
        bean.setNewFaxMobileNumber((field == null) ? "" : field);
        field = seperator.next();
        bean.setOldDataMobileNumber((field == null) ? "" : field);
        field = seperator.next();
        bean.setNewDataMobileNumber((field == null) ? "" : field);
        field = seperator.next();
        bean.setOldIMSI((field == null) ? "" : field);
        field = seperator.next();
        bean.setNewIMSI((field == null) ? "" : field);
        field = seperator.next();
        bean.setOldSPID((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = seperator.next();
        bean.setNewSPID((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = seperator.next();
        bean.setOldBAN((field == null) ? "" : field);
        field = seperator.next();
        bean.setNewBAN((field == null) ? "" : field);
        field = seperator.next();
        bean.setOldPricePlan((field == null || field.length() == 0) ? 0L : Long.parseLong(field));
        field = seperator.next();
        bean.setNewPricePlan((field == null || field.length() == 0) ? 0L : Long.parseLong(field));
        field = seperator.next();
        bean.setOldFreeMinutes((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = seperator.next();
        bean.setNewFreeMinutes((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = seperator.next();
        bean.setUsedFreeMinutes((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = seperator.next();
        bean.setAdjustmentMinutes((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = seperator.next();
        bean.setAdjustmentAmount((field == null || field.length() == 0) ? 0L : Long.parseLong(field));
        field = seperator.next();
        bean.setPricePlanChangeResultCode((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));

        String oldStateDescription = seperator.next();
        String newStateDescription = seperator.next();
        
        int oldState = -1;
        int newState = -1;
        Iterator iter = SubscriberStateEnum.COLLECTION.iterator();
        while (iter.hasNext() && 
                ((oldState==-1 && oldStateDescription!=null && oldStateDescription.length()>0) || 
                 (newState==-1 && newStateDescription!=null && newStateDescription.length()>0)
                )
              )
        {
            SubscriberStateEnum state = (SubscriberStateEnum) iter.next();
            if (oldState==-1 && oldStateDescription!=null && oldStateDescription.length()>0 && state.getDescription().equals(oldStateDescription))
            {
                oldState = state.getIndex();
            }

            if (newState==-1 && newStateDescription!=null && newStateDescription.length()>0 && state.getDescription().equals(newStateDescription))
            {
                newState = state.getIndex();
            }
        }

        bean.setOldState((oldStateDescription == null || oldStateDescription.length() == 0) ? -1 : oldState);
        bean.setNewState((newStateDescription == null || newStateDescription.length() == 0) ? -1 : newState);

        field = seperator.next();
        bean.setStateChangeResultCode((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = seperator.next();
        bean.setOldDeposit((field == null || field.length() == 0) ? 0L : Long.parseLong(field));
        field = seperator.next();
        bean.setNewDeposit((field == null || field.length() == 0) ? 0L : Long.parseLong(field));
        field = seperator.next();
        bean.setOldCreditLimit((field == null || field.length() == 0) ? 0L : Long.parseLong(field));
        field = seperator.next();
        bean.setNewCreditLimit((field == null || field.length() == 0) ? 0L : Long.parseLong(field));
        field = seperator.next();
        bean.setCreditLimitResultCode((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = seperator.next();
        bean.setOldCurrency((field == null) ? "" : field);
        field = seperator.next();
        bean.setNewCurrency((field == null) ? "" : field);
        field = seperator.next();
        bean.setOldServices((field == null) ? "" : field);
        field = seperator.next();
        bean.setNewServices((field == null) ? "" : field);
        field = seperator.next();
        bean.setServicesChangeResultCode((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = seperator.next();
        bean.setOldBillingType((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = seperator.next();
        bean.setNewBillingType((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = seperator.next();
        bean.setOldPostpaidSupportMSISDN((field == null) ? "" : field);
        field = seperator.next();
        bean.setNewPostpaidSupportMSISDN((field == null) ? "" : field);
        field = seperator.next();
        bean.setOldAddress1((field == null) ? "" : field);
        field = seperator.next();
        bean.setNewAddress1((field == null) ? "" : field);
        field = seperator.next();
        bean.setOldAddress2((field == null) ? "" : field);
        field = seperator.next();
        bean.setNewAddress2((field == null) ? "" : field);
        field = seperator.next();
        bean.setOldAddress3((field == null) ? "" : field);
        field = seperator.next();
        bean.setNewAddress3((field == null) ? "" : field);
        field = seperator.next();
        bean.setOldCity((field == null) ? "" : field);
        field = seperator.next();
        bean.setNewCity((field == null) ? "" : field);
        field = seperator.next();
        bean.setOldRegion((field == null) ? "" : field);
        field = seperator.next();
        bean.setNewRegion((field == null) ? "" : field);
        field = seperator.next();
        bean.setOldCountry((field == null) ? "" : field);
        field = seperator.next();
        bean.setNewCountry((field == null) ? "" : field);
        field = seperator.next();
        bean.setOldSubscriptionDealerCode((field == null) ? "" : field);
        field = seperator.next();
        bean.setNewSubscriptionDealerCode((field == null) ? "" : field);
        field = seperator.next();
        bean.setOldDiscountClass((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = seperator.next();
        bean.setNewDiscountClass((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = seperator.next();
        bean.setOldDepositDate(com.redknee.framework.xhome.support.DateUtil.parse(field));
        field = seperator.next();
        bean.setNewDepositDate(com.redknee.framework.xhome.support.DateUtil.parse(field));
        field = seperator.next();
        bean.setOldSubscriptionStartDate(com.redknee.framework.xhome.support.DateUtil.parse(field));
        field = seperator.next();
        bean.setNewSubscriptionStartDate(com.redknee.framework.xhome.support.DateUtil.parse(field));
        field = seperator.next();
        bean.setOldSubscriptionEndDate(com.redknee.framework.xhome.support.DateUtil.parse(field));
        field = seperator.next();
        bean.setNewSubscriptionEndDate(com.redknee.framework.xhome.support.DateUtil.parse(field));
        field = seperator.next();
        bean.setOldBillingLanguage((field == null) ? "" : field);
        field = seperator.next();
        bean.setNewBillingLanguage((field == null) ? "" : field);
        field = seperator.next();
        bean.setOldPackageID((field == null) ? "" : field);
        field = seperator.next();
        bean.setNewPackageID((field == null) ? "" : field);
        field = seperator.next();
        bean.setSubscriptionID((field == null) ? "" : field);
        field = seperator.next();
        bean.setSubscriptionCreateDate(com.redknee.framework.xhome.support.DateUtil.parse(field));
        field = seperator.next();
        bean.setFirstName((field == null) ? "" : field);
        field = seperator.next();
        bean.setLastName((field == null) ? "" : field);
        field = seperator.next();
        bean.setBillingOption((field == null || field.length() == 0) ? 0 : Integer.parseInt(field));
        field = seperator.next();
        bean.setOldExpiryDate(com.redknee.framework.xhome.support.DateUtil.parse(field));
        field = seperator.next();
        bean.setNewExpiryDate(com.redknee.framework.xhome.support.DateUtil.parse(field));
        field = seperator.next();
        bean.setOldReactivationFee((field == null || field.length() == 0) ? 0L : Long.parseLong(field));
        field = seperator.next();
        bean.setNewReactivationFee((field == null || field.length() == 0) ? 0L : Long.parseLong(field));
        field = seperator.next();
        bean.setDateOfBirth(com.redknee.framework.xhome.support.DateUtil.parse(field));
        field = seperator.next();
        bean.setOldAboveCreditLimit((field == null) ? "" : field);
        field = seperator.next();
        bean.setNewAboveCreditLimit((field == null) ? "" : field);
        field = seperator.next();
        bean.setOldSubscriptionCategory((field == null || field.length() == 0) ? 0L : Long.parseLong(field));
        field = seperator.next();
        bean.setNewSubscriptionCategory((field == null || field.length() == 0) ? 0L : Long.parseLong(field));
        field = seperator.next();
        bean.setOldMarketingCampain((field == null || field.length() == 0) ? 0L : Long.parseLong(field));
        field = seperator.next();
        bean.setNewMarketingCampain((field == null || field.length() == 0) ? 0L : Long.parseLong(field));
        field = seperator.next();
        bean.setOldMarketingCampainStartDate(com.redknee.framework.xhome.support.DateUtil.parse(field));
        field = seperator.next();
        bean.setNewMarketingCampainStartDate(com.redknee.framework.xhome.support.DateUtil.parse(field));
        field = seperator.next();
        bean.setOldMarketingCampainEndDate(com.redknee.framework.xhome.support.DateUtil.parse(field));
        field = seperator.next();
        bean.setNewMarketingCampainEndDate(com.redknee.framework.xhome.support.DateUtil.parse(field));
        field = seperator.next();
        bean.setLeftoverBalance((field == null || field.length() == 0) ? 0L : Long.parseLong(field));
        field = seperator.next();
        bean.setOldAuxiliaryServices((field == null) ? "" : field);
        field = seperator.next();
        bean.setNewAuxiliaryServices((field == null) ? "" : field);
        field = seperator.next();
        bean.setPricePlanRestrictionOverridden((field == null || field.length() == 0) ? false : Boolean.parseBoolean(field));
		field = seperator.next();
		bean.setOldMonthlySpendLimit((field == null || field.length() == 0) ? 0L
		    : Long.parseLong(field));
		field = seperator.next();
		bean.setNewMonthlySpendLimit((field == null || field.length() == 0) ? 0L
		    : Long.parseLong(field));
        field = seperator.next();
        bean.setOldOverdraftBalanceLimit((field == null || field.length() == 0) ? -1L
            : Long.parseLong(field));
        field = seperator.next();
        bean.setNewOverdraftBalanceLimit((field == null || field.length() == 0) ? -1L
            : Long.parseLong(field));
        field = seperator.next();
        bean.setInterfaceId((field == null || field.length() == 0) ? -1L
            : Long.parseLong(field));
        bean.setSuspensionReasonCode((field == null) ? "" : field);
        field = seperator.next();
        
        return bean;
    }

}
