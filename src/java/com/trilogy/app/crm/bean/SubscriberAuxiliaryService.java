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

import com.trilogy.app.crm.amsisdn.AdditionalMsisdnAuxiliaryServiceSupport;
import com.trilogy.app.crm.bean.core.custom.AuxiliaryService;
import com.trilogy.app.crm.bean.service.ServiceStateEnum;
import com.trilogy.app.crm.extension.auxiliaryservice.HomeZoneAuxSvcExtensionXInfo;
import com.trilogy.app.crm.extension.auxiliaryservice.core.AddMsisdnAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.DiscountAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.HomeZoneAuxSvcExtension;
import com.trilogy.app.crm.support.AuxiliaryServiceSupport;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * Concrete implementation of <code>SubscriberAuxliaryService</code> bean.
 *
 * @author cindy.wong@redknee.com
 * @since 17-Mar-08
 */
public class SubscriberAuxiliaryService extends AbstractSubscriberAuxiliaryService implements ContextAware
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 2L;


    /**
     * {@inheritDoc}
     */
    public Context getContext()
    {
        return this.context_;
    }


    /**
     * {@inheritDoc}
     */
    public void setContext(final Context context)
    {
        this.context_ = context;
    }


    /**
     * Returns the homezone information associated with this bean.
     *
     * @param context
     *            The operating context.
     * @return The homezone information associated with this bean.
     */
    public HomeZoneAuxSvcExtension getHomezone(final Context context)
    {
        if (this.homezone_ == null)
        {
            if (context != null && SafetyUtil.safeEquals(getType(context), AuxiliaryServiceTypeEnum.HomeZone))
            {
                try
                {
                    this.homezone_ = HomeSupportHelper.get(context).findBean(context, HomeZoneAuxSvcExtension.class, new EQ(HomeZoneAuxSvcExtensionXInfo.AUXILIARY_SERVICE_ID,
                            Long.valueOf(getIdentifier())));
                }
                catch (final HomeException exception)
                {
                    if (LogSupport.isDebugEnabled(context))
                    {
                        final StringBuilder sb = new StringBuilder();
                        sb.append(exception.getClass().getSimpleName());
                        sb.append(" caught in ");
                        sb.append("SubscriberAuxiliaryService.getHomezone(): ");
                        if (exception.getMessage() != null)
                        {
                            sb.append(exception.getMessage());
                        }
                        LogSupport.debug(context, this, sb.toString(), exception);
                    }
                }

                if (this.homezone_ == null)
                {
                    LogSupport.minor(context, this, "Homezone info for SubscriberAuxiliaryService "
                        + getIdentifier() + " cannot be found");
                }
            }
        }
        return this.homezone_;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int getHzPriority()
    {
        return getHzPriority(getContext());
    }


    /**
     * Returns the priority level of the homezone.
     *
     * @param context
     *            The operating context.
     * @return The priority level of the homezone.
     */
    public int getHzPriority(final Context context)
    {
        int result = super.getHzPriority();
        return result;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getHzCellID()
    {
        return getHzCellID(getContext());
    }


    /**
     * Returns the homezone cell identifier.
     *
     * @param context
     *            The operating context.
     * @return The homezone cell identifier.
     */
    public String getHzCellID(final Context context)
    {
        String result = super.getHzCellID();
        if (result == DEFAULT_HZCELLID)
        {
            final HomeZoneAuxSvcExtension homezone = getHomezone(context);
            if (homezone != null)
            {
                result = homezone.getHzCellID();
                this.hzCellID_ = result;
            }
        }
        return result;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public double getHzX()
    {
        return getHzX(getContext());
    }


    /**
     * Returns the latitude of the homezone location associated with this bean.
     *
     * @param context
     *            The operating context.
     * @return the latitude of the homezone location associated with this bean.
     */
    public double getHzX(final Context context)
    {
        double result = super.getHzX();
        if (result == DEFAULT_HZX)
        {
            final HomeZoneAuxSvcExtension homezone = getHomezone(context);
            if (homezone != null)
            {
                result = homezone.getHzX();
                this.hzX_ = result;
            }
        }
        return result;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public double getHzY()
    {
        return getHzY(getContext());
    }


    /**
     * Returns the longitude of the homezone location associated with this bean.
     *
     * @param context
     *            The operating context.
     * @return the longitude of the homezone location associated with this bean.
     */
    public double getHzY(final Context context)
    {
        double result = super.getHzY();
        if (result == DEFAULT_HZY)
        {
            final HomeZoneAuxSvcExtension homezone = getHomezone(context);
            if (homezone != null)
            {
                result = homezone.getHzY();
                this.hzY_ = result;
            }
        }
        return result;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int getHzRadius()
    {
        return getHzRadius(getContext());
    }


    /**
     * Returns the radius of the homezone location associated with this bean.
     *
     * @param context
     *            The operating context.
     * @return the radius of the homezone location associated with this bean.
     */
    public int getHzRadius(final Context context)
    {
        int result = super.getHzRadius();
        if (result == DEFAULT_HZRADIUS)
        {
            final HomeZoneAuxSvcExtension homezone = getHomezone(context);
            if (homezone != null)
            {
                result = homezone.getRadius();
                this.hzRadius_ = result;
            }
        }
        return result;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int getHzDiscount()
    {
        return getHzDiscount(getContext());
    }


    /**
     * Returns the homezone discount associated with this bean.
     *
     * @param context
     *            The operating context.
     * @return the homezone discount associated with this bean.
     */
    public int getHzDiscount(final Context context)
    {
        int result = super.getHzDiscount();
        if (result == DEFAULT_HZDISCOUNT)
        {
            final HomeZoneAuxSvcExtension homezone = getHomezone(context);
            if (homezone != null)
            {
                result = homezone.getDiscount();
                this.hzDiscount_ = result;
            }
        }
        return result;
    }


    /**
     * Returns the VPN auxiliary subscriber associated with this bean.
     *
     * @param context
     *            The operating context.
     * @return The VPN auxiliary subscriber associated with this bean.
     */
    public VpnAuxiliarySubscriber getVpnAuxiliarySubscriber(final Context context)
    {
        if (this.vpnAuxiliarySubscriber_ == null && context != null
            && SafetyUtil.safeEquals(getType(context), AuxiliaryServiceTypeEnum.Vpn))
        {
            final Home home = (Home) context.get(VpnAuxiliarySubscriberHome.class);
            if (home == null)
            {
                LogSupport.minor(context, this, "VPN auxiliary subscriber home is not found in context");
            }
            else
            {
                try
                {
                    final EQ condition = new EQ(VpnAuxiliarySubscriberXInfo.SUBCRIBER_AUXILIARY_ID,
                            Long.valueOf(getIdentifier()));
                    this.vpnAuxiliarySubscriber_ = (VpnAuxiliarySubscriber) home.find(context, condition);
                }
                catch (final HomeException exception)
                {
                    if (LogSupport.isDebugEnabled(context))
                    {
                        final StringBuilder sb = new StringBuilder();
                        sb.append(exception.getClass().getSimpleName());
                        sb.append(" caught in ");
                        sb.append("SubscriberAuxiliaryService.getVpnAuxiliarySubscriber(): ");
                        if (exception.getMessage() != null)
                        {
                            sb.append(exception.getMessage());
                        }
                        LogSupport.debug(context, this, sb.toString(), exception);
                    }

                }
                if (this.vpnAuxiliarySubscriber_ == null)
                {
                    LogSupport.minor(context, this, "VPN auxiliary subscriber for SubscriberAuxiliaryService "
                        + getIdentifier() + " cannot be found");
                }
            }
        }
        return this.vpnAuxiliarySubscriber_;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getVpnMsisdn()
    {
        return getVpnMsisdn(getContext());
    }


    /**
     * Returns the VPN MSISDN associated with this bean.
     *
     * @param context
     *            The operating context.
     * @return The VPN MSISDN associated with this bean.
     */
    public String getVpnMsisdn(final Context context)
    {
        if ((this.vpnMsisdn_ == null || this.vpnMsisdn_ == DEFAULT_VPNMSISDN) && context != null
            && SafetyUtil.safeEquals(getType(context), AuxiliaryServiceTypeEnum.Vpn))
        {
            Subscriber subscriber = null;
            try
            {
                subscriber = SubscriberSupport.getSubscriber(context, getSubscriberIdentifier());
            }
            catch (final HomeException exception)
            {
                if (LogSupport.isDebugEnabled(context))
                {
                    final StringBuilder sb = new StringBuilder();
                    sb.append(exception.getClass().getSimpleName());
                    sb.append(" caught in ");
                    sb.append("SubscriberAuxiliaryService.getVpnMsisdn(): ");
                    if (exception.getMessage() != null)
                    {
                        sb.append(exception.getMessage());
                    }
                    LogSupport.debug(context, this, sb.toString(), exception);
                }

            }
            if (subscriber == null)
            {
                LogSupport.minor(context, this, "Cannot find subscriber " + getSubscriberIdentifier()
                    + " for SubscriberAuxiliaryService " + getIdentifier());
            }
            else
            {
                Account account = null;
                try
                {
                    account = subscriber.getAccount(context);
                    if (account != null)
                    {
                        account = account.getRootAccount(context);
                    }
                }
                catch (final HomeException exception)
                {
                    if (LogSupport.isDebugEnabled(context))
                    {
                        final StringBuilder sb = new StringBuilder();
                        sb.append(exception.getClass().getSimpleName());
                        sb.append(" caught in ");
                        sb.append("SubscriberAuxiliaryService.getVpnMsisdn(): ");
                        if (exception.getMessage() != null)
                        {
                            sb.append(exception.getMessage());
                        }
                        LogSupport.debug(context, this, sb.toString(), exception);
                    }

                }
                if (account == null)
                {
                    LogSupport.minor(context, this, "Cannot find account " + subscriber.getBAN() + " for Subscriber "
                        + subscriber.getId());
                }
                else
                {
                    if (account.getVpn())
                    {
                        this.vpnMsisdn_ = account.getVpnMSISDN();
                    }
                    else
                    {
                        LogSupport.minor(context, this, "Account " + account.getBAN() + " is not a VPN account");
                    }
                }
            }
        }
        return this.vpnMsisdn_;
    }


    /**
     * Returns the auxiliary service associated with this bean.
     *
     * @param context
     *            The operating context.
     * @return The auxiliary service associated with this bean.
     */
    public AuxiliaryService getAuxiliaryService(final Context context) throws HomeException
    {
        if (this.service_ == null && context != null)
        {
            setAuxiliaryService(AuxiliaryServiceSupport.getAuxiliaryService(context, this.getAuxiliaryServiceIdentifier()));
            if (this.service_ == null)
            {
                LogSupport.minor(context, this, "Auxiliary service " + getAuxiliaryServiceIdentifier()
                        + " does not exist.");
            }
        }
        return this.service_;
    }


    public void setAuxiliaryService(final AuxiliaryService service)
    {
        if (service == null || this.getAuxiliaryServiceIdentifier() == service.getIdentifier())
        {
            this.service_ = service;
        }
    }

    public void setSubscriber(final Subscriber subscriber)
    {
        this.subscriber_ = subscriber;
    }

    /**
     * {@inheritDoc}
     * @deprecated use {@link #getType(Context) getType(Context)}
     */
    @Deprecated
    @Override
    public AuxiliaryServiceTypeEnum getType()
    {
        return getType(getContext());
    }


    /**
     * Returns the type of auxiliary service.
     *
     * @param context
     *            The operating context.
     * @return The type of auxiliary service.
     */
    public AuxiliaryServiceTypeEnum getType(final Context context)
    {
        AuxiliaryServiceTypeEnum result = super.getType();
        if (result == null)
        {
            AuxiliaryService service = null;
            try
            {
                service = getAuxiliaryService(context);
            }
            catch (HomeException e)
            {
                new MinorLogMsg(this, "Error occurred retrieving Auxiliary Service.  Unable to get service type for auxiliary service with ID=" + this.getAuxiliaryServiceIdentifier(), e).log(context);
            }
            if (service != null)
            {
                result = service.getType();
                this.type_ = result;
            }
        }
        return result;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getBearerType()
    {
        return getBearerType(getContext());
    }


    /**
     * Returns the bearer type of the additional MSISDN associated with this bean.
     *
     * @param context
     *            The operating context.
     * @return The bearer type of the additional MSISDN associated with this bean.
     */
    public String getBearerType(final Context context)
    {
        if ((this.bearerType_ == null || this.bearerType_ == DEFAULT_BEARERTYPE) && context != null
            && SafetyUtil.safeEquals(getType(context), AuxiliaryServiceTypeEnum.AdditionalMsisdn))
        {
            AuxiliaryService service = null;
            try
            {
                service = getAuxiliaryService(context);
            }
            catch (HomeException e)
            {
                new MinorLogMsg(this, "Error retrieving Auxiliary Service.  Unable to get bearer type for auxiliary service with ID=" + this.getAuxiliaryServiceIdentifier(), e).log(context);
            }
            if (service != null)
            {
                String bearerType = null;
                AddMsisdnAuxSvcExtension extension = ExtensionSupportHelper.get(context).getExtension(context , service, AddMsisdnAuxSvcExtension.class);
                if (extension!=null)
                {
                    bearerType = extension.getBearerType();
                }
                else
                {
                    LogSupport.minor(context, this,
                            "Unable to find required extension of type '" + AddMsisdnAuxSvcExtension.class.getSimpleName()
                                    + "' for auxiliary service " + service.getIdentifier());
                }
                this.bearerType_ = bearerType;
            }
            if (this.bearerType_ == null)
            {
                LogSupport.minor(context, this, "Bearer Type of auxiliary service " + getAuxiliaryServiceIdentifier()
                    + " cannot be found");
            }
        }
        return this.bearerType_;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getAMsisdn()
    {
        return getAMsisdn(getContext());
    }


    /**
     * Returns the additional MSISDN associated with this bean.
     *
     * @param context
     *            The operating context.
     * @return The additional MSISDN associated with this bean.
     */
    public String getAMsisdn(final Context context)
    {
        if ((this.aMsisdn_ == null || this.aMsisdn_ == DEFAULT_AMSISDN) && context != null 
            && SafetyUtil.safeEquals(getType(context), AuxiliaryServiceTypeEnum.AdditionalMsisdn))
        {
            final Msisdn aMsisdn = getAMsisdnBean(context);

            if (aMsisdn != null)
            {
                this.aMsisdn_ = aMsisdn.getMsisdn();
            }
        }
        return this.aMsisdn_;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int getAMsisdnGroup()
    {
        return getAMsisdnGroup(getContext());
    }


    /**
     * Returns the MSISDN group identifier of the additional MSISDN associated with this
     * bean.
     *
     * @param context
     *            The operating context.
     * @return The MSISDN group identifier of the additional MSISDN associated with this
     *         bean.
     */
    public int getAMsisdnGroup(final Context context)
    {
        if (this.aMsisdnGroup_ == DEFAULT_AMSISDNGROUP && context != null
            && SafetyUtil.safeEquals(getType(context), AuxiliaryServiceTypeEnum.AdditionalMsisdn))
        {
            final Msisdn aMsisdn = getAMsisdnBean(context);

            if (aMsisdn != null)
            {
                this.aMsisdnGroup_ = aMsisdn.getGroup();
            }
        }
        return this.aMsisdnGroup_;
    }


    /**
     * Returns the additional MSISDN bean associated with this bean.
     *
     * @return The additional MSISDN bean associated with this bean.
     */
    public Msisdn getAMsisdnBean()
    {
        return getAMsisdnBean(getContext());
    }


    /**
     * Returns the additional MSISDN bean associated with this bean.
     *
     * @param context
     *            The operating context.
     * @return The additional MSISDN bean associated with this bean.
     */
    public Msisdn getAMsisdnBean(final Context context)
    {
        if (this.aMsisdnBean_ == null && context != null
                && this.getSubscriberIdentifier()!=DEFAULT_SUBSCRIBERIDENTIFIER && !this.getSubscriberIdentifier().equals("-1")
            && SafetyUtil.safeEquals(getType(context), AuxiliaryServiceTypeEnum.AdditionalMsisdn))
        {
            try
            {
                this.aMsisdnBean_ = AdditionalMsisdnAuxiliaryServiceSupport.getAMsisdn(context, this);
            }
            catch (final HomeException exception)
            {
                if (LogSupport.isDebugEnabled(context))
                {
                    final StringBuilder sb = new StringBuilder();
                    sb.append(exception.getClass().getSimpleName());
                    sb.append(" caught in ");
                    sb.append("SubscriberAuxiliaryService.getAMsisdnBean(): ");
                    if (exception.getMessage() != null)
                    {
                        sb.append(exception.getMessage());
                    }
                    LogSupport.debug(context, this, sb.toString(), exception);
                }
            }
            if (this.aMsisdnBean_ == null)
            {
                LogSupport.minor(context, this, "Additional MSISDN of SubscriberAuxiliaryService " + getIdentifier()
                    + " cannot be found");
            }
        }
        return this.aMsisdnBean_;
    }

    
    
    
    
    @Override
    /**
     * the subscriber auxiliary service state is calculated based on provision action and 
     * result, t
     */
    public ServiceStateEnum getProvisionedState()
    {
        
        
        return SubscriberAuxiliaryServiceSupport.getState(this.getProvisionAction(), this.getProvisionActionState()); 
        
    }


    @Override
    public void setProvisionedState(ServiceStateEnum provisionedState) throws IllegalArgumentException
    {
      
    }


    public boolean isPersonalizedFeeSet()
    {
        return getIsfeePersonalizationApplied();
    }



    /**
     * The operating context.
     */
    private transient Context context_;

    /**
     * Subscriber for which auxiliary service is provisioned.
     */
    private transient Subscriber subscriber_;

    /**
     * Auxiliary service.
     */
    private transient AuxiliaryService service_;

    /**
     * Homezone information.
     */
    private transient HomeZoneAuxSvcExtension homezone_;

    /**
     * VPN auxiliary subscriber.
     */
    private transient VpnAuxiliarySubscriber vpnAuxiliarySubscriber_;

    /**
     * Additional MSISDN.
     */
    private transient Msisdn aMsisdnBean_;
}
