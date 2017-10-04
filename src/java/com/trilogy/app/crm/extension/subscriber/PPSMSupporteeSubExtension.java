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
package com.trilogy.app.crm.extension.subscriber;

import com.trilogy.app.crm.bean.ChargingTemplateAdjTypeHome;
import com.trilogy.app.crm.bean.ChargingTemplateAdjTypeXInfo;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SystemNoteSubTypeEnum;
import com.trilogy.app.crm.bean.SystemNoteTypeEnum;
import com.trilogy.app.crm.client.bm.BalanceManagementSupport;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionClient;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionException;
import com.trilogy.app.crm.extension.CategoryExtension;
import com.trilogy.app.crm.extension.ExtensionInstallationException;
import com.trilogy.app.crm.support.NoteSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.product.bundle.manager.provision.profile.error.ErrorCode;

/**
 * 
 * @author Marcio Marques
 * @since 8.5
 *
 */
public class PPSMSupporteeSubExtension extends AbstractPPSMSupporteeSubExtension implements ContextAware, BypassURCSAware
{
    private boolean bypassURCS_ = false;

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    public PPSMSupporteeSubExtension()
    {
        super();
    }


    public PPSMSupporteeSubExtension(Context ctx)
    {
        super();
        setContext(ctx);
    }
    
    /**
     * {@inheritDoc}
     */
    public Context getContext()
    {
        return ctx_;
    }

    public Class<? extends CategoryExtension> getExtensionCategoryClass()
    {
        return PPSMSubscriberExtension.class;
    }
    
    @Override
    public void setMSISDN(String MSISDN)
    {
        bypassURCS_ = false;
        super.setMSISDN(MSISDN);
    }

    public void bypassURCS()
    {
        bypassURCS_ = true;
    }
    
    public void unBypassURCS()
    {
        bypassURCS_ = false;
    }

    public boolean isBypassURCS()
    {
        return bypassURCS_;
    }

    /**
     * {@inheritDoc}
     */
    public void validate(Context ctx) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();
        if (getSupportMSISDN()==null || getSupportMSISDN().trim().equals(""))
        {
            cise.thrown(new IllegalPropertyArgumentException(PPSMSupporteeSubExtensionXInfo.SUPPORT_MSISDN, PPSMSupporteeSubExtensionXInfo.SUPPORT_MSISDN.getLabel() + " cannot be empty."));
        }
        else
        {
            try
            {
                Subscriber supporterSubscriber = SubscriberSupport.lookupSubscriberForMSISDN(ctx, getSupportMSISDN());
                if (supporterSubscriber == null)
                {
                    cise.thrown(new IllegalPropertyArgumentException(PPSMSupporteeSubExtensionXInfo.SUPPORT_MSISDN, PPSMSupporteeSubExtensionXInfo.SUPPORT_MSISDN.getLabel() + " '" + getSupportMSISDN() + "' is not a valid subscription."));
                }
                else if (supporterSubscriber.getSpid()!=this.getSubscriber(ctx).getSpid())
                {
                    cise.thrown(new IllegalPropertyArgumentException(PPSMSupporteeSubExtensionXInfo.SUPPORT_MSISDN, PPSMSupporteeSubExtensionXInfo.SUPPORT_MSISDN.getLabel() + " '" + getSupportMSISDN() + "' is in a different SPID and therefore cannot be the supporter for this subscription."));
                }
                else
                {
                   PPSMSupporterSubExtension supporterExtension = PPSMSupporterSubExtension.getPPSMSupporterSubscriberExtension(ctx, supporterSubscriber.getId()); 
                   if (supporterExtension == null)
                   {
                       cise.thrown(new IllegalPropertyArgumentException(PPSMSupporteeSubExtensionXInfo.SUPPORT_MSISDN, PPSMSupporteeSubExtensionXInfo.SUPPORT_MSISDN.getLabel() + " '" + getSupportMSISDN() + "' is not a PPSM supporter."));
                   }
                }
            }
            catch (HomeException e)
            {
            }
        }
        cise.throwAll();
    }


    /**
     * {@inheritDoc}
     */
    public void install(Context ctx) throws ExtensionInstallationException
    {
        Subscriber sub = getSubscriber(ctx);
        if (sub!=null)
        {
            super.setMSISDN(sub.getMSISDN());
        }
        
        if (!isBypassURCS())
        {
            try
            {
                Subscriber supporterSub = SubscriberSupport.lookupSubscriberForMSISDN(ctx, this.getSupportMSISDN());
                if (supporterSub!=null)
                {
                    final SubscriberProfileProvisionClient client = BalanceManagementSupport
                    .getSubscriberProfileProvisionClient(ctx);
                    client.updatSubscriptionPPSMSupporter(ctx, sub, supporterSub.getBAN(), this.getScreeningTemplate());
                }
                else
                {
                    StringBuilder errorMessage = new StringBuilder();
                    errorMessage.append("Unable to install PPSM Supportee Extension for subscription ");
                    errorMessage.append(this.getSubId());
                    errorMessage.append(". The provided supporter MSISDN is invalid.");

                    LogSupport.minor(ctx, this, errorMessage.toString());
                    throw new ExtensionInstallationException(errorMessage.toString(), false, true);
                }
                
                createCreationNotes(ctx, supporterSub);
                

            }
            catch (HomeException e)
            {
                StringBuilder errorMessage = new StringBuilder();
                errorMessage.append("Unable to install PPSM Supportee Extension for subscription ");
                errorMessage.append(this.getSubId());
                errorMessage.append(" due to an error while retrieving the supporter subscription.");

                LogSupport.minor(ctx, this, errorMessage.toString() + " Exception: " + e.getMessage(), e);
                throw new ExtensionInstallationException(errorMessage.toString(), e, false, true);
            }
            catch (SubscriberProfileProvisionException e)
            {
                StringBuilder errorMessage = new StringBuilder();
                errorMessage.append("Unable to install PPSM Supportee Extension for subscription ");
                errorMessage.append(this.getSubId());
                errorMessage.append(" due to an error on URCS.");

                LogSupport.minor(ctx, this, errorMessage.toString() + " Exception: " + e.getMessage(), e);
                throw new ExtensionInstallationException(errorMessage.toString(), e, false, true);
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    public void uninstall(Context ctx) throws ExtensionInstallationException
    {
        Subscriber sub = getSubscriber(ctx);
        if (!isBypassURCS())
        {
            try
            {
                final SubscriberProfileProvisionClient client = BalanceManagementSupport
                .getSubscriberProfileProvisionClient(ctx);
                client.updatSubscriptionPPSMSupporter(ctx, sub, "", -1);
                
                createRemovalNotes(ctx);

            }
            catch (SubscriberProfileProvisionException e)
            {
                StringBuilder errorMessage = new StringBuilder();
                errorMessage.append("Unable to completely remove PPSM Supportee Extension for subscription ");
                errorMessage.append(this.getSubId());
                errorMessage.append(" due to an error on URCS. ");

                LogSupport.minor(ctx, this, errorMessage.toString() + " Exception: " + e.getMessage(), e);
                
                if (e.getErrorCode() != ErrorCode.RECORD_NOT_FOUND)
                {
                    throw new ExtensionInstallationException(errorMessage.toString(), e, false, true);
                }
            }
        }
    }


    /**
     * this functions {@inheritDoc}
     */
    public void update(Context ctx) throws ExtensionInstallationException
    {
        Subscriber sub = getSubscriber(ctx);
        if (sub!=null)
        {
            super.setMSISDN(sub.getMSISDN());
        }    

        if (!isBypassURCS())
        {
            try
            {
                Subscriber supporterSub = SubscriberSupport.lookupSubscriberForMSISDN(ctx, this.getSupportMSISDN());
                if (supporterSub!=null)
                {
                    final SubscriberProfileProvisionClient client = BalanceManagementSupport
                    .getSubscriberProfileProvisionClient(ctx);
                    client.updatSubscriptionPPSMSupporter(ctx, sub, supporterSub.getBAN(), this.getScreeningTemplate());
                }
                else
                {
                    StringBuilder errorMessage = new StringBuilder();
                    errorMessage.append("Unable to update PPSM Supportee Extension for subscription ");
                    errorMessage.append(this.getSubId());
                    errorMessage.append(". The provided supporter MSISDN is invalid.");

                    LogSupport.minor(ctx, this, errorMessage.toString());
                    throw new ExtensionInstallationException(errorMessage.toString(), false, true);
                }
                
                createUpdateNotes(ctx);

            }
            catch (HomeException e)
            {
                StringBuilder errorMessage = new StringBuilder();
                errorMessage.append("Unable to update PPSM Supportee Extension for subscription ");
                errorMessage.append(this.getSubId());
                errorMessage.append(" due to an error while retrieving the supporter subscription.");

                LogSupport.minor(ctx, this, errorMessage.toString() + " Exception: " + e.getMessage(), e);
                throw new ExtensionInstallationException(errorMessage.toString(), e, false, true);
            }
            catch (SubscriberProfileProvisionException e)
            {
                StringBuilder errorMessage = new StringBuilder();
                errorMessage.append("Unable to update PPSM Supportee Extension for subscription ");
                errorMessage.append(this.getSubId());
                errorMessage.append(" due to an error on URCS.");

                LogSupport.minor(ctx, this, errorMessage.toString() + " Exception: " + e.getMessage(), e);
                throw new ExtensionInstallationException(errorMessage.toString(), e, false, true);
            }
        }
    }
    
    public void deactivate(Context ctx) throws ExtensionInstallationException
    {
        Home home = (Home) ctx.get(PPSMSupporteeSubExtensionHome.class);
        try
        {
            home.remove(ctx, this);
        }
        catch (HomeException e)
        {
            StringBuilder errorMessage = new StringBuilder();
            errorMessage.append("Unable to deactivate PPSM Supportee Extension for subscription ");
            errorMessage.append(this.getSubId());
            errorMessage.append(" due to an error on URCS.");

            LogSupport.minor(ctx, this, errorMessage.toString() + " Exception: " + e.getMessage(), e);
            throw new ExtensionInstallationException(errorMessage.toString(), e, true, false);
        }
    }
        


    /**
     * {@inheritDoc}
     */
    public void move(Context ctx, Object newContainer) throws ExtensionInstallationException
    {
        try
        {
            PPSMSupporteeSubExtension newExtension = PPSMSupporteeSubExtension.getPPSMSupporteeSubscriberExtension(ctx, ((Subscriber) newContainer).getId());
            newExtension.install(ctx);
        }
        catch (HomeException e)
        {
            // If an exception occurs, reinstall this exception, since it's done based on MSISDN.
            this.install(ctx);
        }
        deactivate(ctx);
    }
    
    /**
     * {@inheritDoc}
     */
    public void setContext(Context ctx)
    {
        ctx_ = ctx;
    }
    
    public boolean supportsAdjustmentType(Context ctx, int adjustmentType) throws HomeException
    {
        boolean result = false;
        if (this.getChargingTemplate()>=0)
        {
            Home home = (Home) ctx.get(ChargingTemplateAdjTypeHome.class);
            And filter = new And();
            filter.add(new EQ(ChargingTemplateAdjTypeXInfo.IDENTIFIER, Long.valueOf(this.getChargingTemplate())));
            filter.add(new EQ(ChargingTemplateAdjTypeXInfo.ADJUSTMENT_TYPE_ID, Integer.valueOf(adjustmentType)));
            result = home.select(ctx, filter).size()>0;
        }
        return result;
    }
    
    public static PPSMSupporteeSubExtension getPPSMSupporteeSubscriberExtension(Context ctx, String subscriberId) throws HomeException
    {
        Home home = (Home) ctx.get(PPSMSupporteeSubExtensionHome.class);
        return (PPSMSupporteeSubExtension) home.find(ctx, new EQ(PPSMSupporteeSubExtensionXInfo.SUB_ID, subscriberId));
    }
    
    private void createCreationNotes(final Context ctx, final Subscriber supporterSub)
    {
        try
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Subscription became a PPSM supportee: ");
            sb.append("Supporter MSISDN = ");
            sb.append(this.getSupportMSISDN());
            sb.append(", Charging template: ");
            sb.append(this.getChargingTemplate());
            sb.append(", Screening template: ");
            sb.append(this.getScreeningTemplate());
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, sb.toString());
            }
            NoteSupportHelper.get(ctx).addSubscriberNote(ctx, getSubscriber(ctx).getId(), sb.toString(), SystemNoteTypeEnum.EVENTS , SystemNoteSubTypeEnum.PPSM);
        }
        catch (HomeException e)
        {
            LogSupport.minor(ctx, this, "Unable to add subscriber note regarding creation of PPSM Supporter Sub Extension: " + e.getMessage(), e);
        }

        try
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Suportee subscription added: ");
            sb.append("Supportee ID = ");
            sb.append(this.getSubscriber(ctx).getId());
            sb.append("Supportee MSISDN = ");
            sb.append(this.getSubscriber(ctx).getMSISDN());
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, sb.toString());
            }
            NoteSupportHelper.get(ctx).addSubscriberNote(ctx, supporterSub.getId(), sb.toString(), SystemNoteTypeEnum.EVENTS , SystemNoteSubTypeEnum.PPSM);
        }
        catch (HomeException e)
        {
            LogSupport.minor(ctx, this, "Unable to add subscriber note regarding creation of PPSM Supporter Sub Extension: " + e.getMessage(), e);
        }
    }

    private void createUpdateNotes(final Context ctx)
    {
        boolean chargingTemplateChanged = false;
        boolean screeningTemplateChanged = false;
        boolean supporterChanged = false;
        
        try
        {
            PPSMSupporteeSubExtension oldExtension = getPPSMSupporteeSubscriberExtension(ctx, getSubscriber(ctx).getId());
        
            if (oldExtension!=null)
            {
                chargingTemplateChanged = oldExtension.getChargingTemplate()!=this.getChargingTemplate();
                screeningTemplateChanged = oldExtension.getScreeningTemplate()!=this.getScreeningTemplate();
                supporterChanged = !oldExtension.getSupportMSISDN().equals(this.getSupportMSISDN());
            }
            
            if (chargingTemplateChanged || screeningTemplateChanged || supporterChanged)
            try
            {
                StringBuilder sb = new StringBuilder();
                sb.append("PPSM supportee updated: ");
                if (supporterChanged)
                {
                    sb.append("Supporter MSISDN - ");
                    sb.append(oldExtension.getSupportMSISDN());
                    sb.append(" -> ");
                    sb.append(this.getSupportMSISDN());
                    sb.append(". ");
                }
                if (chargingTemplateChanged)
                {
                    sb.append("Charging Template - ");
                    sb.append(oldExtension.getChargingTemplate());
                    sb.append(" -> ");
                    sb.append(this.getChargingTemplate());
                    sb.append(". ");
                }
                if (screeningTemplateChanged)
                {
                    sb.append("Screening Template - ");
                    sb.append(oldExtension.getScreeningTemplate());
                    sb.append(" -> ");
                    sb.append(this.getScreeningTemplate());
                    sb.append(". ");
                }
                
                if (LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, this, sb.toString());
                }
                NoteSupportHelper.get(ctx).addSubscriberNote(ctx, getSubscriber(ctx).getId(), sb.toString(), SystemNoteTypeEnum.EVENTS , SystemNoteSubTypeEnum.PPSM);
            }
            catch (HomeException e)
            {
                LogSupport.minor(ctx, this, "Unable to add subscriber note regarding creation of PPSM Supporter Sub Extension: " + e.getMessage(), e);
            }
    
            if (supporterChanged)
            {
                try
                {
                    Subscriber supporterSub = SubscriberSupport.lookupSubscriberForMSISDN(ctx, oldExtension.getSupportMSISDN());
                    if (supporterSub!=null)
                    {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Suportee subscription removed: ");
                        sb.append("Supportee ID = ");
                        sb.append(this.getSubscriber(ctx).getId());
                        sb.append("Supportee MSISDN = ");
                        sb.append(this.getSubscriber(ctx).getMSISDN());
                        if (LogSupport.isDebugEnabled(ctx))
                        {
                            LogSupport.debug(ctx, this, sb.toString());
                        }
                        NoteSupportHelper.get(ctx).addSubscriberNote(ctx, supporterSub.getId(), sb.toString(), SystemNoteTypeEnum.EVENTS , SystemNoteSubTypeEnum.PPSM);
                    }
                }
                catch (HomeException e)
                {
                    LogSupport.minor(ctx, this, "Unable to add subscriber note regarding remotion of PPSM Supporter Sub Extension: " + e.getMessage(), e);
                }
    
                    
                try
                {
                    Subscriber supporterSub = SubscriberSupport.lookupSubscriberForMSISDN(ctx, this.getSupportMSISDN());
                    if (supporterSub!=null)
                    {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Suportee subscription added: ");
                        sb.append("Supportee ID = ");
                        sb.append(this.getSubscriber(ctx).getId());
                        sb.append("Supportee MSISDN = ");
                        sb.append(this.getSubscriber(ctx).getMSISDN());
                        if (LogSupport.isDebugEnabled(ctx))
                        {
                            LogSupport.debug(ctx, this, sb.toString());
                        }
                        NoteSupportHelper.get(ctx).addSubscriberNote(ctx, supporterSub.getId(), sb.toString(), SystemNoteTypeEnum.EVENTS , SystemNoteSubTypeEnum.PPSM);
                    }
                }
                catch (HomeException e)
                {
                    LogSupport.minor(ctx, this, "Unable to add subscriber note regarding creation of PPSM Supporter Sub Extension: " + e.getMessage(), e);
                }
            }
        }
        catch (HomeException e)
        {
            LogSupport.minor(ctx, this, "Unable to retrieve old PPSM Supportee Sub Extension: " + e.getMessage(), e);
        }
    }

    private void createRemovalNotes(final Context ctx)
    {
        try
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Subscription stopped being a PPSM supportee.");
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, sb.toString());
            }
            NoteSupportHelper.get(ctx).addSubscriberNote(ctx, getSubscriber(ctx).getId(), sb.toString(), SystemNoteTypeEnum.EVENTS , SystemNoteSubTypeEnum.PPSM);
        }
        catch (HomeException e)
        {
            LogSupport.minor(ctx, this, "Unable to add subscriber note regarding removal of PPSM Supporter Sub Extension: " + e.getMessage(), e);
        }

        try
        {
            Subscriber supporterSub = SubscriberSupport.lookupSubscriberForMSISDN(ctx, this.getSupportMSISDN());
            if (supporterSub!=null)
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Suportee subscription removed: ");
                sb.append("Supportee ID = ");
                sb.append(this.getSubscriber(ctx).getId());
                sb.append("Supportee MSISDN = ");
                sb.append(this.getSubscriber(ctx).getMSISDN());
                if (LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, this, sb.toString());
                }
                NoteSupportHelper.get(ctx).addSubscriberNote(ctx, supporterSub.getId(), sb.toString(), SystemNoteTypeEnum.EVENTS , SystemNoteSubTypeEnum.PPSM);
            }
        }
        catch (HomeException e)
        {
            LogSupport.minor(ctx, this, "Unable to add subscriber note regarding remotion of PPSM Supporter Sub Extension: " + e.getMessage(), e);
        }
    }

    public boolean transientEquals(Object o)
    {
        boolean result = super.transientEquals(o);
        PPSMSupporteeSubExtension extension = (PPSMSupporteeSubExtension) o;
        
        if (extension!=null)
        {
            result = result && bypassURCS_ == extension.isBypassURCS();
        }
        
        return result;
    }
    
    protected transient Context ctx_ = null;

}
