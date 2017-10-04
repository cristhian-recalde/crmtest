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
import java.util.Date;

import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.ERLogMsg;

import com.trilogy.app.crm.bean.core.PricePlan;
import com.trilogy.app.crm.bean.core.PricePlanVersion;


/**
 * Represents the Price Plan Modification event record (766) described in the E-Care FS.
 *
 * @author gary.anderson@redknee.com
 */
public final class PricePlanModificationEventRecord implements EventRecord
{

    /**
     * Creates a new Event Record for the given price plan information. This constructor
     * is for cases where the modification is the creation of the first version, so there
     * is no previous version information to include.
     *
     * @param plan
     *            The owning price plan.
     * @param newVersion
     *            The new version of the price plan.
     */
    public PricePlanModificationEventRecord(final PricePlan oldPlan, final PricePlan plan, final PricePlanVersion newVersion)
    {
        this.plan_ = plan;
        this.oldPlan_ = oldPlan;
        this.previousVersion_ = null;
        this.newVersion_ = newVersion;
    }


    public PricePlanModificationEventRecord(final PricePlan oldPlan, final PricePlan plan)
    {
        this.plan_ = plan;
        this.oldPlan_ = oldPlan;
        this.previousVersion_ = null;
        this.newVersion_ = null;
    }

    /**
     * Creates a new Event Record for the given price plan information.
     *
     * @param plan
     *            The owning price plan.
     * @param previousVersion
     *            The previous version of the price plan.
     * @param newVersion
     *            The new version of the price plan.
     */
    public PricePlanModificationEventRecord(final PricePlan oldPlan, final PricePlan plan, final PricePlanVersion previousVersion,
        final PricePlanVersion newVersion)
    {
        this.plan_ = plan;
        this.oldPlan_ = oldPlan;
        this.previousVersion_ = previousVersion;
        this.newVersion_ = newVersion;
    }


    /**
     * {@inheritDoc}
     */
    public void generate(final Context context)
    {
        final User principal = (User) context.get(Principal.class);

        final String pricipleIdentifier;
        if (principal != null)
        {
            pricipleIdentifier = principal.getName();
        }
        else
        {
            pricipleIdentifier = "";
        }

        if (this.newVersion_ != null)
        {
            final String newServicesList = ERLogger.addDoubleQuotes(this.newVersion_.getServices(context).toString());
    
            if (this.previousVersion_ != null)
            {
                final String oldServicesList = ERLogger.addDoubleQuotes(this.previousVersion_.getServices(context)
                    .toString());
    
                generate(context, pricipleIdentifier, oldServicesList, newServicesList, true, true);
            }
            else
            {
                generateWithoutPreviousVersion(context, pricipleIdentifier, newServicesList);
            }
        }
        else
        {
            generateWithoutVersion(context, pricipleIdentifier);
        }
    }


    /**
     * Generates the ER with information about the previous version.
     *
     * @param context
     *            The operating context.
     * @param pricipleIdentifier
     *            The user causing this ER.
     * @param newServicesList
     *            New service list.
     * @param template
     *            FCT template.
     */
    private void generateWithoutPreviousVersion(final Context context, final String pricipleIdentifier,
        final String newServicesList)
    {
        generate(context, pricipleIdentifier, "", newServicesList, true, false);
    }


    /**
     * Generates the ER with information about the previous version.
     *
     * @param context
     *            The operating context.
     * @param pricipleIdentifier
     *            The user causing this ER.
     * @param newServicesList
     *            New service list.
     * @param template
     *            FCT template.
     */
    private void generateWithoutVersion(final Context context, final String pricipleIdentifier)
    {
        generate(context, pricipleIdentifier, "", "", false, false);
    }


    /**
     * Generates the ER with information about the previous version.
     *
     * @param context
     *            The operating context.
     * @param pricipleIdentifier
     *            The user causing this ER.
     * @param oldServicesList
     *            List of old services.
     * @param newServicesList
     *            List of new services.
     * @param oldTemplate
     *            Old FCT.
     * @param newTemplate
     *            New FCT.
     */
    private void generate(final Context context, final String pricipleIdentifier,
        final String oldServicesList, final String newServicesList, boolean includeVersion, boolean includeOldVersion)
    {
        PricePlanModificationER er = new PricePlanModificationER();
        
        er.setUserID(pricipleIdentifier);
        er.setNewMonthlyFee(0);
        er.setOldServices(oldServicesList);
        er.setNewServices(newServicesList);
        
        if (plan_ != null)
        {
            er.setSpid(this.plan_.getSpid());
            er.setNewPricePlanName(this.plan_.getName());
            er.setNewVoiceRatePlan(this.plan_.getVoiceRatePlan());
            er.setNewSMSRatePlan(this.plan_.getSMSRatePlan());
            er.setNewIPCGRatePlan(this.plan_.getDataRatePlan());
            if (this.plan_.isApplyContractDurationCriteria())
            {
                er.setNewMinimumContractDuration(this.plan_.getMinContractDuration());
                er.setNewMaximumContractDuration(this.plan_.getMaxContractDuration());
                er.setNewContractDurationUnits(this.plan_.getContractDurationUnits());
            }
            er.setNewState(plan_.getState().getDescription());	
            er.setGrandfatherPPId(plan_.getGrandfatherPPId());
        }
        if (oldPlan_ != null)
        {
            er.setOldPricePlanName(this.oldPlan_.getName());
            er.setOldVoiceRatePlan(this.oldPlan_.getVoiceRatePlan());
            er.setOldSMSRatePlan(this.oldPlan_.getSMSRatePlan());
            er.setOldIPCGRatePlan(this.oldPlan_.getDataRatePlan());
            if (this.oldPlan_.isApplyContractDurationCriteria())
            {
                er.setOldMinimumContractDuration(this.oldPlan_.getMinContractDuration());
                er.setOldMaximumContractDuration(this.oldPlan_.getMaxContractDuration());
                er.setOldContractDurationUnits(this.oldPlan_.getContractDurationUnits());
            }
            er.setOldState(oldPlan_.getState().getDescription());
        }
        if (includeVersion && includeOldVersion && previousVersion_ != null)
        {
            er.setOldDefaultDeposit(this.previousVersion_.getDeposit());
            er.setOldCreditLimit(this.previousVersion_.getCreditLimit());
            er.setOldPerMinuteDefaultAirRate(this.previousVersion_.getDefaultPerMinuteAirRate());
            er.setOldMonthlyFee(0);
        }
        if (includeVersion && newVersion_ != null)
        {
            er.setPricePlanID(this.newVersion_.getId());
            er.setNewDefaultDeposit(this.newVersion_.getDeposit());
            er.setNewCreditLimit(this.newVersion_.getCreditLimit());
            er.setNewPerMinuteDefaultAirRate(this.newVersion_.getDefaultPerMinuteAirRate());
            er.setActivationDate(getActivationDate(this.newVersion_));
            
            if (previousVersion_ == null || previousVersion_.getVersion() != newVersion_.getVersion())
            {
                er.setVersionID(this.newVersion_.getVersion());
            }
            
        }
        
        new ERLogMsg(context, er).log(context);
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
     * The owning price plan.
     */
    private final PricePlan plan_;

    /**
     * The owning price plan.
     */
    private final PricePlan oldPlan_;

    /**
     * The new version of the price plan.
     */
    private final PricePlanVersion newVersion_;

    /**
     * The previous version of the price plan.
     */
    private final PricePlanVersion previousVersion_;

} // class
