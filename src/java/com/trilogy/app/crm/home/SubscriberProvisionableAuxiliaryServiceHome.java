package com.trilogy.app.crm.home;

import com.trilogy.app.crm.CoreCrmLicenseConstants;
import com.trilogy.app.crm.amsisdn.AdditionalMsisdnAuxiliaryServiceSupport;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.ProvisionCommand;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.core.custom.AuxiliaryService;
import com.trilogy.app.crm.client.EcpVpnClientException;
import com.trilogy.app.crm.client.VpnClientException;
import com.trilogy.app.crm.hlr.HlrSupport;
import com.trilogy.app.crm.home.sub.StateChangeAuxiliaryServiceSupport;
import com.trilogy.app.crm.provision.ProvisionAgentException;
import com.trilogy.app.crm.support.LicensingSupportHelper;
import com.trilogy.app.crm.vpn.SubscriberAuxiliaryVpnServiceSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * 
 * @author odeshpande
 *
 */
public abstract class SubscriberProvisionableAuxiliaryServiceHome extends HomeProxy
{
	/**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new SubscriberProvisionableAuxiliaryServiceUpdateHome proxy.
     *
     * @param ctx
     *            The operating context.
     * @param delegate
     *            The Home to which we delegate.
     */
    public SubscriberProvisionableAuxiliaryServiceHome(final Context ctx, final Home delegate)
    {
        super(ctx, delegate);
    }
    
    /**
     * Provisions the subscriber-auxiliary service association.
     *
     * @param ctx
     *            The operating context.
     * @param association
     *            The association to be provisioned.
     * @param auxService
     *            The auxiliary service to be provisioned.
     * @param subscriber
     *            The subscriber to be provisioned.
     * @throws HomeException
     *             Thrown if there are problems looking up any related beans.
     */
    protected void provisionSubscriberAuxiliaryService(final Context ctx, final SubscriberAuxiliaryService association,
        final AuxiliaryService auxService, final Subscriber subscriber) throws HomeException
    {
        try
        {
            provisionHLR(ctx, association, auxService, subscriber);
        }
        catch (final Exception e)
        {
            /*
             * something went wrong when provisioning this aux.svc on HLR. Don't want to
             * create entry in SubscriberAuxiliaryService home because that will cause
             * inconsistency between HLR and ECare. Throw HomeException out to stop process
             */
            new InfoLogMsg(this, "Failed to provision HLR, subId=" + subscriber.getBAN() + " [" + e + "]", e).log(ctx);
            throw new HomeException(e.getMessage(), e);
        }

        if (auxService.getType().equals(AuxiliaryServiceTypeEnum.Vpn))
        {
            try
            {
                provisionVpnClient(ctx, subscriber, association);
            }
            catch (final EcpVpnClientException e)
            {
                throw new HomeException(e.toString(), e.getCause());
            }
            catch (final VpnClientException e)
            {
                throw new HomeException(e.toString(), e.getCause());
            }
        }
        
        association.setProvisioned(true);
    }
    
    /**
     * Provisions to VPN client.
     *
     * @param ctx
     *            The operating context.
     * @param subscriber
     *            Subscriber being provisioned.
     * @param auxService
     *            Auxiliary service being provisioned.
     * @throws HomeException
     *             Thrown by home.
     * @throws EcpVpnClientException
     *             Thrown by VPN client.
     * @throws VpnClientException
     *             Thrown by VPN client.
     */
    protected void provisionVpnClient(final Context ctx, final Subscriber subscriber,
        final SubscriberAuxiliaryService auxService) throws HomeException, EcpVpnClientException, VpnClientException
    {
        SubscriberAuxiliaryVpnServiceSupport.provisionVpn(ctx, subscriber, auxService);
    }


    /**
     * Updates the HLR by sending the given command.
     *
     * @param ctx
     *            The operating context.
     * @param association
     *            The subscriber-auxiliary service association.
     * @param auxService
     *            The auxiliary service being provisioned.
     * @param subscriber
     *            The subscriber being provisioned.
     * @throws HomeException
     *             Thrown if provisioning fails.
     */
    protected void provisionHLR(final Context ctx, final SubscriberAuxiliaryService association,
        final AuxiliaryService auxService, final Subscriber subscriber) throws HomeException
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "Attempting to send HLR provisioning command for auxiliary service [ID=" + association.getAuxiliaryServiceIdentifier()
                    + ",SecondaryID=" + association.getSecondaryIdentifier()
                    + "] for subscription [" + association.getSubscriberIdentifier() + "]...", null).log(ctx);
        }
        
        StateChangeAuxiliaryServiceSupport.provisionHlr(ctx, association, auxService, subscriber, this);
    }


    /**
     * Whether this association should be provisioned.
     *
     * @param ctx
     *            The operating context.
     * @param association
     *            The association to be provisioned.
     * @param subscriber
     *            The subscriber to be provisioned.
     * @param auxService
     *            The auxiliary service to be provisioned.
     * @return Whether this association should be provisioned to HLR or not.
     */
    protected boolean shouldProvision(final Context ctx, final SubscriberAuxiliaryService association,
        final Subscriber subscriber, final AuxiliaryService auxService)
    {
        boolean provision = true;
        if (auxService == null || subscriber == null)
        {
            provision = false;
        }
        else if (!auxService.isHLRProvisionable())
        {
            provision = false;
        }
        else if (!StateChangeAuxiliaryServiceSupport.isActive(ctx, association))
        {
            provision = false;
        }
        else if (StateChangeAuxiliaryServiceSupport.isProvisionOnSuspendDisable(ctx, subscriber, auxService))
        {
            provision = false;
        }
        else if (AuxiliaryServiceTypeEnum.MultiSIM.equals(auxService.getType())
                    && association.getSecondaryIdentifier() == SubscriberAuxiliaryService.DEFAULT_SECONDARYIDENTIFIER)
        {
            // Provisioning commands only get sent for per-SIM services
            provision = false;
        }
        return provision;
    }
        
    /**
     * Whether this auxiliary service is an additional MSISDN type and additional MSISDN
     * feature is enabled.
     *
     * @param context
     *            The operating context.
     * @param auxService
     *            The auxiliary service being determined.
     * @return Returns <code>true</code> if additional MSISDN feature is enabled and the
     *         auxiliary service is additional MSISDN type.
     */
    protected boolean isEnabledAdditionalMsisdn(final Context context, final AuxiliaryService auxService)
    {
        return AdditionalMsisdnAuxiliaryServiceSupport.isAdditionalMsisdnEnabled(context) && auxService != null
            && auxService.getType() == AuxiliaryServiceTypeEnum.AdditionalMsisdn;
    }


    /**
     * Whether this auxiliary service is an Multi-SIM type and Multi-SIM
     * feature is enabled.
     *
     * @param ctx
     *            The operating context.
     * @param auxService
     *            The auxiliary service being determined.
     * @return Returns <code>true</code> if Multi-SIM feature is enabled and the
     *         auxiliary service is Multi-SIM type.
     */
    protected boolean isEnabledMultiSIM(final Context ctx, final AuxiliaryService auxService)
    {
        return auxService != null 
                && auxService.getType() == AuxiliaryServiceTypeEnum.MultiSIM
                && LicensingSupportHelper.get(ctx).isLicensed(ctx, CoreCrmLicenseConstants.MULTI_SIM_LICENSE);
    }
    
    


}
