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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.trilogy.app.crm.bean.core.PricePlan;
import com.trilogy.app.crm.bean.core.PricePlanVersion;
import com.trilogy.app.crm.support.PricePlanSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeOperationEnum;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.web.util.Link;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Price Plan Version modification request
 * @author Marcio Marques
 * @since 9.2
 *
 */
public class PPVModificationRequest extends AbstractPPVModificationRequest
{
    public PPVModificationRequest()
    {

    }


    public PPVModificationRequest(Context ctx, long id, int version)
    {
        this.setPricePlanIdentifier(id);
        this.setPricePlanVersion(version);
        this.getServicePackageVersion().setId(id);
        this.getServicePackageVersion().setVersion(version);
        try
        {
            PricePlanVersion ppversion = PricePlanSupport.getVersion(ctx, getPricePlanIdentifier(), getPricePlanVersion());
            if (ppversion != null)
            {
                this.getServicePackageVersion().setServiceFees(
                        ppversion.getServicePackageVersion(ctx).getServiceFees(ctx));
                this.getServicePackageVersion().setBundleFees(
                        ppversion.getServicePackageVersion(ctx).getBundleFees(ctx));
                this.setCreditLimit(ppversion.getCreditLimit());
                this.setDeposit(ppversion.getDeposit());
                this.setDefaultPerMinuteAirRate(ppversion.getDefaultPerMinuteAirRate());
                this.setOverusageVoiceRate(ppversion.getOverusageVoiceRate());
                this.setOverusageSmsRate(ppversion.getOverusageSmsRate());
                this.setOverusageDataRate(ppversion.getOverusageDataRate());
                this.setChargeCycle(ppversion.getChargeCycle().getIndex());
            }
        }
        catch (HomeException e)
        {
            LogSupport.minor(ctx, this, "Unable to retrieve price plan " + getPricePlanIdentifier() + " version " + getPricePlanVersion() + ": "
                    + e.getMessage(), e);
        }
    }


    public PricePlan getPricePlan(Context ctx)
    {
        if (pricePlan_ == null)
        {
            try
            {
                pricePlan_ = PricePlanSupport.getPlan(ctx, this.getPricePlanIdentifier());
            }
            catch (HomeException e)
            {
                LogSupport.minor(ctx, this, "Unable to retrieve price plan " + getPricePlanIdentifier() + ": " + e.getMessage(), e);
            }
        }

        return pricePlan_;
    }


    public void reportError(Context ctx, Throwable error)
    {
        assertBeanNotFrozen();

        errors_.add(error);

        LogSupport.minor(ctx, this, error.getMessage(), error);

    }


    public boolean hasErrors(Context ctx)
    {
        return errors_ != null && errors_.size() > 0;
    }


    /**
     * @{inheritDoc
     */
    public Set<Throwable> getErrors(Context ctx)
    {
        return Collections.unmodifiableSet(errors_);
    }

    public void reportWarning(Context ctx, Throwable error)
    {
        assertBeanNotFrozen();

        warnings_.add(error);

        LogSupport.minor(ctx, this, error.getMessage(), error);

    }


    public boolean hasWarnings(Context ctx)
    {
        return warnings_ != null && warnings_.size() > 0;
    }


    /**
     * @{inheritDoc
     */
    public Set<Throwable> getWarnings(Context ctx)
    {
        return Collections.unmodifiableSet(warnings_);
    }

    public String getPartialSuccessMessage(Context ctx, HomeOperationEnum operation)
    {
        return getMessage(ctx, "Price plan <a href=\"{0}\">{1}</a> version {2} modification request was successfully {3}, but could not be activated immediately.", operation);
                
    }

    public String getSuccessMessage(Context ctx, HomeOperationEnum operation)
    {
        return getMessage(ctx, "Price plan <a href=\"{0}\">{1}</a> version {2} modification request successfully {3}", operation);
    }

    /**
     * @{inheritDoc
     */
    private String getMessage(Context ctx, String message, HomeOperationEnum operation)
    {
        String msg = null;
        String operationMsg = null;

        MessageMgr mmgr = new MessageMgr(ctx, this);

        final Link link = new Link(ctx);
        link.remove("cmd");
        link.add("cmd", "appCRMPricePlanMenu");
        link.add("mode", "display");
        link.remove("key");
        link.add("key", String.valueOf(this.getPricePlanIdentifier()));
        link.remove(".versionskey");
        link.remove(".versionsmode");

        switch (operation.getIndex())
        {
        case HomeOperationEnum.CREATE_INDEX:
            operationMsg = "created";
            break;
        case HomeOperationEnum.STORE_INDEX:
            operationMsg = "updated";
            break;
        case HomeOperationEnum.REMOVE_INDEX:
            operationMsg = "canceled";
            break;
        default:
            throw new UnsupportedOperationException("Unsupported home operation for PPV Modification request");
        }

        msg = mmgr.get(PPVModificationRequest.class.getSimpleName() + ".success",
                "Price plan <a href=\"{0}\">{1}</a> version {2} modification request successfully {3}", new String[] {
                        link.write(), String.valueOf(this.getPricePlanIdentifier()), String.valueOf(this.getPricePlanVersion()), operationMsg });

        return msg;
    }

    protected Set<Throwable> errors_ = new HashSet<Throwable>();

    protected Set<Throwable> warnings_ = new HashSet<Throwable>();

    private PricePlan        pricePlan_;

}
