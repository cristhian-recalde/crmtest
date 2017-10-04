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
 * Copyright &copy; Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home;

import java.security.Principal;

import com.trilogy.app.crm.bean.BirthdayPlan;
import com.trilogy.app.crm.bean.BlacklistWhitelistTemplate;
import com.trilogy.app.crm.bean.BlacklistWhitelistTypeEnum;
import com.trilogy.app.crm.bean.CallingGroupServiceTypeEnum;
import com.trilogy.app.crm.bean.ClosedUserGroup;
import com.trilogy.app.crm.bean.ClosedUserGroupTemplate;
import com.trilogy.app.crm.bean.DiscountTypeEnum;
import com.trilogy.app.crm.bean.PersonalListPlan;
import com.trilogy.app.crm.log.CoreERLogger;
import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.ERLogMsg;

/**
 * @author Margarita Alp
 *         Created on Aug 12, 2004
 */
public class CallingGroupERLogMsg
{    

    /**
     * ********************************************************
     *
     * ER methods for Personal List Plan.
     *
     * *********************************************************
     */
    
    public static void generatePLPCreationER(
            final PersonalListPlan plan,     
            final int resultCode,
            final Context ctx)
    {
        String[] erData = generatePLPCreationERData(ctx, plan, resultCode);

        new ERLogMsg(
                PLP_CUGT_BP_CREATION_ER,
                ER_CLASS,
                PLP_CUGT_BP_CREATION_EVENT,
                plan.getSpid(),
                erData).log(ctx);
    }
    
    public static void generatePLPDeletionER(final PersonalListPlan plan, final int resultCode, final Context ctx)
    {        
        String[] erData = generatePLPDeletionERData(ctx, plan, resultCode);
        new ERLogMsg(PLP_CUGT_BP_DELETION_ER, ER_CLASS, PLP_CUGT_BP_DELETION_EVENT, plan.getSpid(), erData).log(ctx);
    }

    public static void generatePLPModificationER(
            final PersonalListPlan oldPlan,
            final PersonalListPlan newPlan,
            final int resultCode,
            final Context ctx)
    {
        String[] erData = generatePLPModificationERData(ctx, oldPlan, newPlan, resultCode);
        new ERLogMsg(
                PLP_CUGT_BP_MODIFICATION_ER,
                ER_CLASS,
                PLP_CUGT_BP_MODIFICATION_EVENT,
                oldPlan.getSpid(),
                erData).log(ctx);
    }

    private static String[] generatePLPCreationERData(Context ctx, final PersonalListPlan plan, final int resultCode)
    {
        final String[] erData = new String[]{
            plan.getName(),
            PLP_ER_TYPE,
            String.valueOf(plan.getID()),
            mapDiscountType(plan.getVoiceDiscountType()),
            String.valueOf(plan.getVoiceIncomingValue()),
            String.valueOf(plan.getVoiceOutgoingValue()),
            String.valueOf(plan.getMonthlyCharge()),
            String.valueOf(plan.getMaxSubscriberCount()),
            CoreERLogger.formatERDateDayOnly(plan.getStartDate()),
            CoreERLogger.formatERDateDayOnly(plan.getEndDate()),
            mapCallingGroupServiceType(plan.getPlpServiceType()),
            mapDiscountType(plan.getSmsDiscountType()),
            String.valueOf(plan.getSmsIncomingValue()),
            String.valueOf(plan.getSmsOutgoingValue()),
            String.valueOf(resultCode),
            getUserName(ctx)
        };

        return erData;
    }
    
    private static String[] generatePLPModificationERData(Context ctx,
            final PersonalListPlan oldPlan,
            final PersonalListPlan newPlan,
            final int resultCode)
    {
        final String[] erData = new String[]{
            oldPlan.getName(),
            newPlan.getName(),
            PLP_ER_TYPE,
            String.valueOf(oldPlan.getID()),
            String.valueOf(newPlan.getID()),
            mapDiscountType(oldPlan.getVoiceDiscountType()),
            mapDiscountType(newPlan.getVoiceDiscountType()),
            String.valueOf(oldPlan.getVoiceIncomingValue()),
            String.valueOf(newPlan.getVoiceIncomingValue()),
            String.valueOf(oldPlan.getVoiceOutgoingValue()),
            String.valueOf(newPlan.getVoiceOutgoingValue()),
            String.valueOf(oldPlan.getMonthlyCharge()),
            String.valueOf(newPlan.getMonthlyCharge()),
            String.valueOf(oldPlan.getMaxSubscriberCount()),
            CoreERLogger.formatERDateDayOnly(oldPlan.getStartDate()),
            CoreERLogger.formatERDateDayOnly(newPlan.getStartDate()),
            CoreERLogger.formatERDateDayOnly(oldPlan.getEndDate()),
            CoreERLogger.formatERDateDayOnly(newPlan.getEndDate()),
            mapCallingGroupServiceType(oldPlan.getPlpServiceType()),
            mapCallingGroupServiceType(newPlan.getPlpServiceType()),
            mapDiscountType(oldPlan.getSmsDiscountType()),
            mapDiscountType(newPlan.getSmsDiscountType()),
            String.valueOf(oldPlan.getSmsIncomingValue()),
            String.valueOf(newPlan.getSmsIncomingValue()),
            String.valueOf(oldPlan.getSmsOutgoingValue()),
            String.valueOf(newPlan.getSmsOutgoingValue()),
            String.valueOf(resultCode),
            "",
            "",
            getUserName(ctx)
        };

        return erData;
    }

    private static String[] generatePLPDeletionERData(Context ctx, final PersonalListPlan plan, final int resultCode)
    {
        final String[] erData = new String[]{
            plan.getName(),
            String.valueOf(plan.getID()),
            String.valueOf(resultCode),
            getUserName(ctx)
        };

        return erData;
    }

    /**
     * ********************************************************
     *
     * ER methods for Closed User Groups.
     *
     * *********************************************************
     */

    public static void generateCUGCreationER(
            final ClosedUserGroup cug,
            final int resultCode,
            final Context ctx)
    {
        String[] erData = generateCUGCreationERData(ctx, cug, resultCode);

        new ERLogMsg(
          CUG_CREATION_ER,
          ER_CLASS,
          CUG_INSTANCE_CREATION_EVENT,
          cug.getSpid(),
          erData).log(ctx);

    }
    
    public static void generateCUGModificationER(
            final ClosedUserGroup oldCug,
            final ClosedUserGroup newCug,
            final int resultCode,
            final Context ctx)
    {
        String[] erData = generateCUGModificationERData(ctx, oldCug, newCug, resultCode);

        new ERLogMsg(
          CUG_MODIFICATION_ER,
          ER_CLASS,
          CUG_INSTANCE_MODIFICATION_EVENT,
          newCug.getSpid(),
          erData).log(ctx);

    }
    
    public static void generateCUGDeletionER(
            final ClosedUserGroup cug,
            final int resultCode,
            final Context ctx)
    {
        String[] erData = generateCUGDeletionERData(ctx, cug, resultCode);

        new ERLogMsg(
          CUG_DELETION_ER,
          ER_CLASS,
          CUG_INSTANCE_DELETION_EVENT,
          cug.getSpid(),
          erData).log(ctx);

    }
    
    private static String[] generateCUGCreationERData(Context ctx,
            ClosedUserGroup cug,
            int resultCode)
    {   
        String[] erData = new String[]
        {
            cug.getName(), 
            String.valueOf(cug.getID()),
            String.valueOf(cug.getCugTemplateID()),
            String.valueOf(resultCode),
            getUserName(ctx)
        };
        
        return erData;
    }
    

    private static String[] generateCUGModificationERData(Context ctx, ClosedUserGroup oldCug, ClosedUserGroup newCug,
            int resultCode)
    {
        String[] erData = new String[]
            {newCug.getName(), String.valueOf(newCug.getID()),
                    oldCug != null ? String.valueOf(oldCug.getCugTemplateID()) : "",
                    String.valueOf(newCug.getCugTemplateID()),
                    oldCug != null ? String.valueOf(oldCug.getOwnerMSISDN()) : "",
                    String.valueOf(newCug.getOwnerMSISDN()),
                    oldCug != null ? String.valueOf(oldCug.getSmsNotifyUser()) : "",
                    String.valueOf(newCug.getSmsNotifyUser()), String.valueOf(resultCode), getUserName(ctx)};
        return erData;
    }
    
    
    private static String[] generateCUGDeletionERData(Context ctx,
            ClosedUserGroup cug,
            int resultCode)
    {   
        String[] erData = new String[]
        {
            cug.getName(), 
            String.valueOf(cug.getID()),
            String.valueOf(cug.getCugTemplateID()),
            String.valueOf(resultCode),
            getUserName(ctx)
        };
        
        return erData;
    }
    
    
    /**
     * ********************************************************
     *
     * ER methods for Closed User Group Template.
     *
     * *********************************************************
     */

    static public void generateCUGTemplateCreationER(
            final ClosedUserGroupTemplate cugTemplate,
            final int resultCode,
            final Context ctx)
    {       
        String[] erData = generateCUGTemplateCreationERData(ctx, cugTemplate, resultCode);

        new ERLogMsg(
                PLP_CUGT_BP_CREATION_ER,
          ER_CLASS,
          PLP_CUGT_BP_CREATION_EVENT,
          cugTemplate.getSpid(),
          erData).log(ctx);
    }
    
    
    static public void generateCUGTemplateDeletionER(
            final ClosedUserGroupTemplate cugTemplate,
            final int resultCode,
            final Context ctx)
    {
        String[] erData = generateCUGTemplateDeletionERData(ctx, cugTemplate, resultCode);

        new ERLogMsg(
                PLP_CUGT_BP_DELETION_ER,
          ER_CLASS,
          PLP_CUGT_BP_DELETION_EVENT,
          cugTemplate.getSpid(),
          erData).log(ctx);

    }
    
    public static void generateCUGTemplateModificationER(
            final ClosedUserGroupTemplate oldCUGTemplate,
            final ClosedUserGroupTemplate newCUGTemplate,
            final int resultCode,
            final Context ctx)
    {
        String[] erData = generateCUGTemplateModificationERData(ctx,oldCUGTemplate, newCUGTemplate, resultCode);
                
        new ERLogMsg(
                PLP_CUGT_BP_MODIFICATION_ER,
                ER_CLASS,
                PLP_CUGT_BP_MODIFICATION_EVENT,
                oldCUGTemplate.getSpid(),
                erData).log(ctx);
    }

    private static String[] generateCUGTemplateCreationERData(Context ctx,
            final ClosedUserGroupTemplate cug,
            final int resultCode)
    {
        final String[] erData = new String[]{
            cug.getName(),
            CUG_ER_TYPE,
            String.valueOf(cug.getID()),
            mapDiscountType(cug.getVoiceDiscountType()),
            String.valueOf(cug.getVoiceIncomingValue()),
            String.valueOf(cug.getVoiceOutgoingValue()),
            String.valueOf(cug.getServiceCharge()),
            "",  // Max Subscribers - only applicable for PLPs
            CoreERLogger.formatERDateDayOnly(cug.getStartDate()),
            CoreERLogger.formatERDateDayOnly(cug.getEndDate()),
            mapCallingGroupServiceType(cug.getCugServiceType()),
            mapDiscountType(cug.getSmsDiscountType()),
            String.valueOf(cug.getSmsIncomingValue()),
            String.valueOf(cug.getSmsOutgoingValue()),
            String.valueOf(resultCode),
            getUserName(ctx)
        };

        return erData;
    }

    private static String[] generateCUGTemplateDeletionERData
    (Context ctx, ClosedUserGroupTemplate cugTemplate, int resultCode)
    {   
     String[] erData = new String[]
     {
         cugTemplate.getName(), 
         String.valueOf(cugTemplate.getID()),
         String.valueOf(resultCode),
         getUserName(ctx)
     };
     
     return erData;
    }
    private static String[] generateCUGTemplateModificationERData(Context ctx,
            final ClosedUserGroupTemplate oldCUGTemplate,
            final ClosedUserGroupTemplate newCUGTemplate,
            final int resultCode)
    {
        final String[] erData = new String[]{
        		oldCUGTemplate.getName(),
            newCUGTemplate.getName(),
            CUG_ER_TYPE,
            String.valueOf(oldCUGTemplate == null? newCUGTemplate.getID() : oldCUGTemplate.getID()),
            String.valueOf(newCUGTemplate.getID()),
            oldCUGTemplate == null? "" : mapDiscountType(oldCUGTemplate.getVoiceDiscountType()),
            mapDiscountType(newCUGTemplate.getVoiceDiscountType()),
            String.valueOf(oldCUGTemplate == null? "" : oldCUGTemplate.getVoiceIncomingValue()),
            String.valueOf(newCUGTemplate.getVoiceIncomingValue()),
            String.valueOf(oldCUGTemplate == null? "" : oldCUGTemplate.getVoiceOutgoingValue()),
            String.valueOf(newCUGTemplate.getVoiceOutgoingValue()),
            String.valueOf(oldCUGTemplate == null? "" : oldCUGTemplate.getServiceCharge()),
            String.valueOf(newCUGTemplate.getServiceCharge()),
            "",  // Max Subscribers - only applicable for PLPs
            oldCUGTemplate == null? "" : CoreERLogger.formatERDateDayOnly(oldCUGTemplate.getStartDate()),
            CoreERLogger.formatERDateDayOnly(newCUGTemplate.getStartDate()),
            oldCUGTemplate == null? "" : CoreERLogger.formatERDateDayOnly(oldCUGTemplate.getEndDate()),
            CoreERLogger.formatERDateDayOnly(newCUGTemplate.getEndDate()),
            oldCUGTemplate == null? "" : mapCallingGroupServiceType(oldCUGTemplate.getCugServiceType()),
            mapCallingGroupServiceType(newCUGTemplate.getCugServiceType()),
            oldCUGTemplate == null? "" : mapDiscountType(oldCUGTemplate.getSmsDiscountType()),
            mapDiscountType(newCUGTemplate.getSmsDiscountType()),
            String.valueOf(oldCUGTemplate == null? "" : oldCUGTemplate.getSmsIncomingValue()),
            String.valueOf(newCUGTemplate.getSmsIncomingValue()),
            String.valueOf(oldCUGTemplate == null? "" : oldCUGTemplate.getSmsOutgoingValue()),
            String.valueOf(newCUGTemplate.getSmsOutgoingValue()),
            String.valueOf(resultCode),
            oldCUGTemplate == null ? "" : oldCUGTemplate.isDeprecated() ? "N" : "Y",
            newCUGTemplate.isDeprecated() ? "N" : "Y",
            getUserName(ctx)
        };

        return erData;
    }

    
    /**
     * ********************************************************
     *
     * ER methods for Birthday Plans.
     *
     * *********************************************************
     */

    public static void generateBPCreationER(
            final BirthdayPlan plan,
            final int resultCode,
            final Context ctx)
    {
        String[] erData  = generateBPCreationERData(ctx,plan, resultCode);

        new ERLogMsg(
                PLP_CUGT_BP_CREATION_ER,
                ER_CLASS,
                PLP_CUGT_BP_CREATION_EVENT,
                plan.getSpid(),
                erData).log(ctx);
    }
    
    
    public static void generateBPDeletionER(
            final BirthdayPlan plan,
            final int resultCode,
            final Context ctx)
    {
        String[] erData = generateBPDeletionERData(ctx,plan, resultCode);

        new ERLogMsg(
                PLP_CUGT_BP_DELETION_ER,
                ER_CLASS,
                PLP_CUGT_BP_DELETION_EVENT,
                plan.getSpid(),
                erData).log(ctx);
    }

    public static void generateBPModificationER(
            final BirthdayPlan oldPlan,
            final BirthdayPlan newPlan,
            final int resultCode,
            final Context ctx)
    {
        String[] erData = generateBPModificationERData(ctx,oldPlan, newPlan, resultCode);
         
        new ERLogMsg(
                PLP_CUGT_BP_MODIFICATION_ER,
                ER_CLASS,
                PLP_CUGT_BP_MODIFICATION_EVENT,
                oldPlan.getSpid(),
                erData).log(ctx);
    }

    private static String[] generateBPCreationERData(Context ctx,final BirthdayPlan plan, final int resultCode)
    {
        final String[] erData = new String[]{
            plan.getName(),
            BP_ER_TYPE,
            String.valueOf(plan.getID()),
            mapDiscountType(plan.getVoiceDiscountType()),
            String.valueOf(plan.getVoiceIncomingValue()),
            String.valueOf(plan.getVoiceOutgoingValue()),
            String.valueOf(plan.getMonthlyCharge()),
            "",  // Max Subscribers - only applicable for PLPs
            CoreERLogger.formatERDateDayOnly(plan.getStartDate()),
            CoreERLogger.formatERDateDayOnly(plan.getEndDate()),
            mapCallingGroupServiceType(plan.getBpServiceType()),
            mapDiscountType(plan.getSmsDiscountType()),
            String.valueOf(plan.getSmsIncomingValue()),
            String.valueOf(plan.getSmsOutgoingValue()),
          String.valueOf(resultCode),
          getUserName(ctx)
        };

        return erData;
    }

    private static String[] generateBPModificationERData(Context ctx,
            final BirthdayPlan oldPlan,
            final BirthdayPlan newPlan,
            final int resultCode)
    {
        final String[] erData = new String[]{
            oldPlan.getName(),
            newPlan.getName(),
            BP_ER_TYPE,
            String.valueOf(oldPlan.getID()),
            String.valueOf(newPlan.getID()),
            mapDiscountType(oldPlan.getVoiceDiscountType()),
            mapDiscountType(newPlan.getVoiceDiscountType()),
            String.valueOf(oldPlan.getVoiceIncomingValue()),
            String.valueOf(newPlan.getVoiceIncomingValue()),
            String.valueOf(oldPlan.getVoiceOutgoingValue()),
            String.valueOf(newPlan.getVoiceOutgoingValue()),
            String.valueOf(oldPlan.getMonthlyCharge()),
            String.valueOf(newPlan.getMonthlyCharge()),
            "",  // Max Subscribers - only applicable for PLPs
            CoreERLogger.formatERDateDayOnly(oldPlan.getStartDate()),
            CoreERLogger.formatERDateDayOnly(newPlan.getStartDate()),
            CoreERLogger.formatERDateDayOnly(oldPlan.getEndDate()),
            CoreERLogger.formatERDateDayOnly(newPlan.getEndDate()),
            mapCallingGroupServiceType(oldPlan.getBpServiceType()),
            mapCallingGroupServiceType(newPlan.getBpServiceType()),
            mapDiscountType(oldPlan.getSmsDiscountType()),
            mapDiscountType(newPlan.getSmsDiscountType()),
            String.valueOf(oldPlan.getSmsIncomingValue()),
            String.valueOf(newPlan.getSmsIncomingValue()),
            String.valueOf(oldPlan.getSmsOutgoingValue()),
            String.valueOf(newPlan.getSmsOutgoingValue()),
            String.valueOf(resultCode),
            "",
            "",
            getUserName(ctx)
        };

        return erData;
    }

    private static String[] generateBPDeletionERData(Context ctx, final BirthdayPlan plan, final int resultCode)
    {
        final String[] erData = new String[]{
            plan.getName(),
            String.valueOf(plan.getID()),
            String.valueOf(resultCode),
            getUserName(ctx)
        };

        return erData;
    }

    
    /**
     * ********************************************************
     *
     * ER methods for Blacklist / Whitelist Template.
     *
     * *********************************************************
     */
    
    public static void generateBlWlCreationER(
            final BlacklistWhitelistTemplate blwlTemplate,     
            final int resultCode,
            final Context ctx)
    {
        final String[] erData = new String[]{
                blwlTemplate.getName(),
                blwlTemplate.getType().getIndex() == BlacklistWhitelistTypeEnum.BLACKLIST_INDEX ? BLACKLIST_ER_TYPE : WHITELIST_ER_TYPE,
                String.valueOf(blwlTemplate.getIdentifier()),
                "",
                "",
                "",
                "",
                String.valueOf(blwlTemplate.getMaxSubscribersAllowed()),
                CoreERLogger.formatERDateDayOnly(blwlTemplate.getStartDate()),
                CoreERLogger.formatERDateDayOnly(blwlTemplate.getEndDate()),
                "",
                "",
                "",
                "",
                String.valueOf(resultCode),
                getUserName(ctx)
            };

        new ERLogMsg(
                PLP_CUGT_BP_CREATION_ER,
                ER_CLASS,
                PLP_CUGT_BP_CREATION_EVENT,
                blwlTemplate.getSpid(),
                erData).log(ctx);
    }
    
    private static String mapDiscountType(final DiscountTypeEnum planDiscountType)
    {
        String discount = "";
        if (planDiscountType == DiscountTypeEnum.DISCOUNT)
        {
            discount = ER_DISCOUNT;
        }
        else
        {
            discount = ER_RATEPLAN;
        }
        return discount;
    }

    private static String mapCallingGroupServiceType(final CallingGroupServiceTypeEnum serviceType)
    {
        String result = "";
        if (serviceType == CallingGroupServiceTypeEnum.VOICE)
        {
            result = ER_VOICE;
        }
        else if (serviceType == CallingGroupServiceTypeEnum.SMS)
        {
            result = ER_SMS;
        }
        else
        {
            result = ER_ALL;
        }
        return result;
    }
    
    private static String getUserName(Context ctx)
    {
        User user = (User) ctx.get(Principal.class);
        return user != null ? user.getName() : USER_NAME;
    }
    
    public static final int PLP_CUGT_BP_CREATION_ER = 885;
    public static final int PLP_CUGT_BP_MODIFICATION_ER = 886;
    public static final int PLP_CUGT_BP_DELETION_ER = 887;
    static public final int CUG_CREATION_ER = 894;
    static public final int CUG_MODIFICATION_ER = 895;
    static public final int CUG_DELETION_ER = 896;

    public static final int SUCCESS_RESULT_CODE = 0;
    public static final int ERROR_PROVISIONING_TO_FF_RESULT_CODE = 3015;
    public static final int ERROR_PROVISIONING_AUXILIARY_SERVICE = 3016;
    public static final int ERROR_PROVISIONING_SERVICE = 3017;
    
    private static final String PLP_CUGT_BP_CREATION_EVENT = "PLP/CUGT/BirthdayPlan Creation Event";
    private static final String PLP_CUGT_BP_DELETION_EVENT = "PLP/CUGT/BirthdayPlan Deletion Event";
    private static final String PLP_CUGT_BP_MODIFICATION_EVENT = "PLP/CUGT/BirthdayPlan Modification Event";
    private static final String CUG_INSTANCE_CREATION_EVENT = "CUG Creation Event";
    private static final String CUG_INSTANCE_MODIFICATION_EVENT = "CUG Modification Event";
    private static final String CUG_INSTANCE_DELETION_EVENT = "CUG Deletion Event";
    
    private static final int ER_CLASS = 700;
    private static final String PLP_ER_TYPE = "0";
    private static final String CUG_ER_TYPE = "1";
    private static final String BP_ER_TYPE = "2";
    private static final String PRIVATE_CUG_ER_TYPE = "3";
    private static final String BLACKLIST_ER_TYPE = "4";
    private static final String WHITELIST_ER_TYPE = "5";
    
    private static final String ER_RATEPLAN = "0";
    private static final String ER_DISCOUNT = "1";
    private static final String ER_VOICE = "0";
    private static final String ER_SMS = "1";
    private static final String ER_ALL = "2";
    public static String USER_NAME = "";
}
