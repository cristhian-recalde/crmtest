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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.ERLogMsg;

import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.core.PricePlanVersion;
import com.trilogy.app.crm.bean.core.ServiceFee2;


/**
 * Represents the Price Plan Activation event record (888) described in the E-Care FS.
 *
 * @author gary.anderson@redknee.com
 */
public final class PricePlanVersionActivationEventRecord implements EventRecord
{

    /**
     * Creates a new Event Record for the given price plan information.
     *
     * @param plan
     *            The owning price plan.
     * @param version
     *            The version of the price plan.
     * @param result
     *            The result code of the activation process.
     */
    public PricePlanVersionActivationEventRecord(final PricePlan plan, final PricePlanVersion version, final int result)
    {
        // TODO - 2004-10-13 - Check the parameters.
        this.plan_ = plan;
        this.version_ = version;
        this.result_ = result;
    }


    /**
     * {@inheritDoc}
     */
    public void generate(final Context context)
    {
        String activationDate;

        activationDate = ERLogger.formatERDateWithTime(this.version_.getActivation());

        /*
         * Adds all service fees for the priceplan version. (including mandatory and
         * optional only)
         */
        new ERLogMsg(IDENTIFIER, CLASS, TITLE, this.plan_.getSpid(), new String[]
        {
            getPricipleIdentifier(context),
            String.valueOf(this.version_.getId()),
            String.valueOf(this.version_.getVersion()),
            this.plan_.getName(),
            activationDate,
            this.plan_.getVoiceRatePlan(),
            this.plan_.getSMSRatePlan(),
            String.valueOf(this.plan_.getDataRatePlan()),
            String.valueOf(this.version_.getDeposit()),
            String.valueOf(this.version_.getCreditLimit()),
            String.valueOf(getMonthlyFee(context)), "-1",
            "0",
            Integer.toString(this.result_),
        }).log(context);
    }


    /**
     * Gets the name of the user controlling this generation from the context.
     *
     * @param context
     *            The operating context.
     * @return The name of the user if there is one; otherwise a blank string.
     */
    private String getPricipleIdentifier(final Context context)
    {
        final User principal = (User) context.get(Principal.class);

        final String pricipleIdentifier;
        if (principal != null && principal.getName() != null)
        {
            pricipleIdentifier = principal.getName();
        }
        else
        {
            pricipleIdentifier = "";
        }

        return pricipleIdentifier;
    }


    /**
     * Returns the sum of all service fees for the priceplan version. (including mandatory
     * and optional only)
     *
     * @param ctx
     *            The operating context.
     * @return The sum of all service fees
     */
    private long getMonthlyFee(final Context ctx)
    {
        long monthlyFee = 0;
        Collection serviceFees = new HashSet();

        final Map fees = this.version_.getServiceFees(ctx);

        if (this.version_ != null && fees != null)
        {
            serviceFees = fees.values();
        }

        for (final Iterator i = serviceFees.iterator(); i.hasNext();)
        {
            final ServiceFee2 fee = (ServiceFee2) i.next();
            monthlyFee += fee.getFee();
        }

        return monthlyFee;
    }

    /**
     * The numeric identifier of the ER.
     */
    private static final int IDENTIFIER = 888;

    /**
     * The String identifier of the ER.
     */
    private static final String TITLE = "Price Plan Version Activation Event";

    /**
     * The owning price plan.
     */
    private final PricePlan plan_;

    /**
     * The version of the price plan.
     */
    private final PricePlanVersion version_;

    /**
     * The result code of the activation.
     */
    private final int result_;

} // class
