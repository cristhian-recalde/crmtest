package com.trilogy.app.crm.extension.auxiliaryservice.core.custom;

import com.trilogy.app.crm.amsisdn.AdditionalMsisdnAuxiliaryServiceSupport;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.core.Msisdn;
import com.trilogy.app.crm.bean.externalapp.ExternalAppEnum;
import com.trilogy.app.crm.client.AppEcpClient;
import com.trilogy.app.crm.extension.ExtendedAssociableExtension;
import com.trilogy.app.crm.extension.ExtensionAssociationException;
import com.trilogy.app.crm.home.sub.Claim;
import com.trilogy.app.crm.home.sub.StateChangeAuxiliaryServiceSupport;
import com.trilogy.app.crm.numbermgn.AppendNumberMgmtHistoryHome;
import com.trilogy.app.crm.numbermgn.HistoryEvent;
import com.trilogy.app.crm.numbermgn.HistoryEventSupport;
import com.trilogy.app.crm.numbermgn.MsisdnAlreadyAcquiredException;
import com.trilogy.app.crm.numbermgn.MsisdnManagement;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistoryHome;
import com.trilogy.app.crm.support.ExternalAppSupport;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.MsisdnSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.osa.ecp.provision.ErrorCode;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.NullHome;
import com.trilogy.framework.xlog.log.LogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.PMLogMsg;

public class AddMsisdnAuxSvcExtension extends
com.redknee.app.crm.extension.auxiliaryservice.core.AddMsisdnAuxSvcExtension implements
ExtendedAssociableExtension<SubscriberAuxiliaryService>
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Override
    public void associate(Context context, SubscriberAuxiliaryService association) throws ExtensionAssociationException
    {
        final LogMsg pm = new PMLogMsg(this.getClass().getSimpleName(), "AddMsisdnAuxSvcExtension_Associate");
        Context subContext = context;

        try
        {
            if (AdditionalMsisdnAuxiliaryServiceSupport.isAdditionalMsisdnEnabled(subContext))
            {
                subContext = context.createSubContext();
                subContext.setName("Additional MSISDN create");

                final Msisdn msisdn = claimMsisdn(subContext, association);
                makeMsisdnAMsisdn(subContext, association, msisdn);
                subContext.put(Lookup.NEW_AMSISDN, msisdn);
                
                if (this.isProvisionToECP())
                {
                    final Subscriber subscriber = getSubscriber(subContext,
                            association);
                    final short result = provisionMsisdnIfActive(subContext, association);
                    if (result != ErrorCode.SUCCESS)
                    {
                        // release MSISDN if fails
                        releaseMsisdn(subContext, association, subscriber.getSubscriptionType(), msisdn, null);
                        String msg = "Unable to add Additional MSISDN information to URCS Voice profile";
                        association.setProvisionActionState(false);
                        throw new ExtensionAssociationException(ExternalAppEnum.VOICE, msg, msg, result);
                    } else
                    {
                        association.setProvisionActionState(true);
                    }
                }
            }
        }
        finally
        {
            pm.log(context);
        }
    }

    @Override
    public void updateAssociation(Context context, SubscriberAuxiliaryService association)
            throws ExtensionAssociationException
    {
        final LogMsg pm = new PMLogMsg(this.getClass().getSimpleName(), "AddMsisdnAuxSvcExtension_UpdateAssociation");
        
        try
        {
            if (AdditionalMsisdnAuxiliaryServiceSupport.isAdditionalMsisdnEnabled(context))
            {
                SubscriberAuxiliaryService oldAssociation = (SubscriberAuxiliaryService) context
                        .get(Lookup.OLD_SUBSCRIBER_AUXILIARY_SERVICE);
                updateAdditionalMsisdnAssociation(context, oldAssociation, association);
            }
        }
        finally
        {
            pm.log(context);
        }

    }

    @Override
    public void dissociate(Context context, SubscriberAuxiliaryService associatedBean) throws ExtensionAssociationException
    {
        final LogMsg pm = new PMLogMsg(this.getClass().getSimpleName(), "AddMsisdnAuxSvcExtension_Dissociate");
        boolean removed = false;
        SubscriberAuxiliaryService association = (SubscriberAuxiliaryService) context.get(Lookup.OLD_SUBSCRIBER_AUXILIARY_SERVICE);
        
        try
        {
            if (AdditionalMsisdnAuxiliaryServiceSupport.isAdditionalMsisdnEnabled(context))
            {
                final Subscriber subscriber = getSubscriber(context, association);
                final Msisdn msisdn = getAssociatedAMsisdn(context, association);
                if (msisdn == null)
                {
                    throw new ExtensionAssociationException(ExternalAppEnum.BSS, "Cannot unprovision Additional MSISDN: MSISDN not found", ExternalAppSupport.BSS_DATABASE_FAILURE_MSISDN_RETRIEVAL);
                }
                
                context.put(Lookup.OLD_AMSISDN, msisdn);

                if (isProvisionToECP())
                {
                    final short result = unprovisionMsisdn(context, association, msisdn);
                    if (result == ErrorCode.SUBSCRIBER_NOT_FOUND)
                    {
                        LogSupport.info(context, this, "Additional MSISDN " + association.getAMsisdn(context)
                                + " not found in ECP (association = " + association + "); attempting to continue.");
                    }
                    else if (result != ErrorCode.SUCCESS)
                    {
                        String msg = "Unable to remove Additional MSISDN information from URCS Voice profile";
                        association.setProvisionActionState(false);;
                        throw new ExtensionAssociationException(ExternalAppEnum.VOICE, msg, msg, result);
                    } else
                    {
                        association.setProvisionActionState(true);;
                    }
                }
            }

        }
        finally
        {
            pm.log(context);
        }

    }


    @Override
    public void postExternalBeanCreation(Context ctx, SubscriberAuxiliaryService association, boolean success)
            throws ExtensionAssociationException
    {
        if (AdditionalMsisdnAuxiliaryServiceSupport.isAdditionalMsisdnEnabled(ctx))
        {
            if (!success)
            {
                final StringBuilder sb = new StringBuilder();
                sb.append("Exception caught when creating Additional MSISDN SubscriberAuxiliaryService. Dissociate MSISDN ");
                sb.append(association.getAMsisdn(ctx));
                sb.append(" from account and remove it from ECP to return to correct system state.");
                LogSupport.minor(ctx, this, sb.toString());
                throw new ExtensionAssociationException(ExternalAppEnum.BSS,
                        "Additional MSISDN SubscriberAuxiliaryService creation failed.",
                        ExternalAppSupport.BSS_DATABASE_FAILURE_AUXILIARY_SERVICE_CREATION);
            }
        }
    }

    @Override
    public void postExternalBeanUpdate(Context context, SubscriberAuxiliaryService newAssociation, boolean success)
            throws ExtensionAssociationException
    {
        if (AdditionalMsisdnAuxiliaryServiceSupport.isAdditionalMsisdnEnabled(context))
        {
            SubscriberAuxiliaryService oldAssociation = (SubscriberAuxiliaryService) context
                    .get(Lookup.OLD_SUBSCRIBER_AUXILIARY_SERVICE);
            final Msisdn oldMsisdn = getAssociatedAMsisdn(context, oldAssociation);

            final Subscriber subscriber = getSubscriber(context, newAssociation);
            
            if (success)
            {
                final Msisdn newMsisdn = getMsisdn(context, newAssociation);
                if (!SafetyUtil.safeEquals(oldMsisdn.getMsisdn(), newAssociation.getAMsisdn(context)))
                {
                    makeMsisdnAMsisdn(context, newAssociation, getMsisdn(context, newAssociation));
                    releaseMsisdn(context, oldAssociation, subscriber.getSubscriptionType(), oldMsisdn, newMsisdn.getMsisdn());
                }
                else
                {
                    /*
                     * TT 7082700028: Update MSISDN if subscriber ID has changed.
                     */
                    updateMsisdnSubscriberId(context, oldAssociation, newAssociation, subscriber, newMsisdn);
                }
                if (subscriber.getState() == SubscriberStateEnum.INACTIVE)
                {
                    releaseMsisdn(context, oldAssociation, subscriber.getSubscriptionType(), oldMsisdn, null);
                }
            }
            else
            {
                // TODO attempt to unprovision?
                final StringBuilder sb = new StringBuilder();
                sb.append("Exception caught when updating Additional MSISDN SubscriberAuxiliaryService: ");
                if (!SafetyUtil.safeEquals(oldMsisdn.getMsisdn(), newAssociation.getAMsisdn(context)))
                {
                    final Msisdn newMsisdn = getMsisdn(context, newAssociation);
                    // TODO attempt to unprovision?
                    releaseMsisdn(context, newAssociation, subscriber.getSubscriptionType(), newMsisdn, null);
                    sb.append("Release the new AMSISDN ");
                    sb.append(newMsisdn.getMsisdn());
                    sb.append(" from CRM");
                    if (StateChangeAuxiliaryServiceSupport.isActive(context, newAssociation))
                    {
                        sb.append(", and remove it from ECP");
                    }
                    if (oldAssociation.isProvisioned())
                    {
                        sb.append(", and add the old AMSISDN ");
                        sb.append(oldMsisdn.getMsisdn());
                        sb.append(" back to subscriber ");
                        sb.append(subscriber.getMSISDN());
                        sb.append(" in ECP");
                    }
                    sb.append(" and");
                }
                else
                {
                    final boolean wasProvisioned = oldAssociation.isProvisioned();
                    final boolean isActive = StateChangeAuxiliaryServiceSupport.isActive(context, newAssociation);

                    if (wasProvisioned != isActive)
                    {
                        if (this.isProvisionToECP())
                        {
                            if (wasProvisioned)
                            {
                                sb.append("Add the old AMSISDN ");
                            }
                            else
                            {
                                sb.append("Remove the old AMSISDN ");
                            }
                            sb.append(oldMsisdn.getMsisdn());
                            sb.append(" back to subscriber ");
                            sb.append(subscriber.getMSISDN());
                            sb.append(" in ECP and ");
                        }
                    }
                }
                sb.append(" verify with HLR to return to correct system state.");
                LogSupport.minor(context, this, sb.toString());
                throw new ExtensionAssociationException(ExternalAppEnum.BSS, "Additional MSISDN SubscriberAuxiliaryService update failed.", ExternalAppSupport.BSS_DATABASE_FAILURE_AUXILIARY_SERVICE_UPDATE);
                
            }

        }
    }

    @Override
    public void postExternalBeanRemoval(Context context, SubscriberAuxiliaryService associatedBean, boolean success)
            throws ExtensionAssociationException
    {
        if (AdditionalMsisdnAuxiliaryServiceSupport.isAdditionalMsisdnEnabled(context))
        {
            SubscriberAuxiliaryService association = (SubscriberAuxiliaryService) context.get(Lookup.OLD_SUBSCRIBER_AUXILIARY_SERVICE);
            final Subscriber subscriber = getSubscriber(context, association);
            final Msisdn msisdn = getAssociatedAMsisdn(context, association);
    
            if (success)
            {
                releaseMsisdn(context, association, subscriber.getSubscriptionType(), msisdn, null);
            }
            else
            {
                // TODO attempt to reprovision?
                final StringBuilder sb = new StringBuilder();
                sb.append("Excepion caught when removing Additional MSISDN SubscriberAuxiliaryService: ");
                if (association.isProvisioned())
                {
                    if (this.isProvisionToECP())
                    {
                        sb.append("Add the old AMSISDN ");
                        sb.append(msisdn.getMsisdn());
                        sb.append(" back to subscriber ");
                        sb.append(subscriber.getMSISDN());
                        sb.append(" in ECP and ");
                    }
                }
                sb.append("verify with HLR");
                sb.append(" to return to correct system state.");
                LogSupport.minor(context, this, sb.toString());
                throw new ExtensionAssociationException(ExternalAppEnum.BSS, "Additional MSISDN SubscriberAuxiliaryService remove failed.", ExternalAppSupport.BSS_DATABASE_FAILURE_AUXILIARY_SERVICE_REMOVAL);
            }
        }
    }
    

    /**
     * Updates the subscriber ID of the AMSISDN during a subscriber move.
     *
     * @param context
     *            The operating context.
     * @param oldAssociation
     *            The old association.
     * @param newAssociation
     *            The new association.
     * @param subscriber
     *            The new subscriber.
     * @param msisdn
     *            New MSISDN.
     * @throws HomeException
     *             Thrown if there are problems updating the subscriber.
     */
    protected void updateMsisdnSubscriberId(final Context context, final SubscriberAuxiliaryService oldAssociation,
        final SubscriberAuxiliaryService newAssociation, final Subscriber subscriber, final Msisdn msisdn)
        throws ExtensionAssociationException
    {
        final boolean isSubscriberChanged = !SafetyUtil.safeEquals(oldAssociation.getSubscriberIdentifier(),
            newAssociation.getSubscriberIdentifier());

        if (isSubscriberChanged)
        {
            try
            {
                MsisdnManagement.deassociateMsisdnWithSubscription(context, msisdn.getMsisdn(),
                    oldAssociation.getSubscriberIdentifier(), "aMsisdn");
            }
            catch (HomeException e)
            {
                throw new ExtensionAssociationException(ExternalAppEnum.BSS, "Unable to deassociate MSISDN "
                        + msisdn.getMsisdn() + " with subscription: " + e.getMessage(), "Unable to deassociate old additional MSISDN from subscription", ExternalAppSupport.BSS_DATABASE_FAILURE_MSISDN_DEASSOCIATION, e);
            }
            
            try
            {
                MsisdnManagement.associateMsisdnWithSubscription(context, msisdn.getMsisdn(),
                    newAssociation.getSubscriberIdentifier(), "aMsisdn");
            }
            catch (HomeException e)
            {
                throw new ExtensionAssociationException(ExternalAppEnum.BSS, "Unable to associate MSISDN "
                        + msisdn.getMsisdn() + " with subscription: " + e.getMessage(), "Unable to associate new additional MSISDN with subscription", ExternalAppSupport.BSS_DATABASE_FAILURE_MSISDN_ASSOCIATION, e);
            }
        }
    }
    
    /**
     * Updates an additional MSISDN association.
     *
     * @param context
     *            The operating context.
     * @param oldAssociation
     *            The existing association.
     * @param newAssociation
     *            The new association.
     * @return The object returned from <code>super.store()</code> if called, or
     *         <code>null</code> if not.
     * @throws HomeException
     *             Thrown if there are problems updating the association.
     */
    protected void updateAdditionalMsisdnAssociation(final Context context,
        final SubscriberAuxiliaryService oldAssociation, final SubscriberAuxiliaryService newAssociation)
        throws ExtensionAssociationException
    {
        if (newAssociation.getAMsisdn(context) == null)
        {
            throw new ExtensionAssociationException(ExternalAppEnum.BSS, "No additional MSISDN specified for association " + newAssociation.getIdentifier(),  "No additional MSISDN specified", ExternalAppSupport.BSS_DATABASE_FAILURE_MSISDN_RETRIEVAL);
        }

        // fetch the old aMSISDN
        final Msisdn oldMsisdn = getAssociatedAMsisdn(context, oldAssociation);
        if (oldMsisdn == null)
        {
            throw new ExtensionAssociationException(ExternalAppEnum.BSS, "Cannot find existing additional MSISDN for association "
                + newAssociation.getIdentifier(), ExternalAppSupport.BSS_DATABASE_FAILURE_MSISDN_RETRIEVAL);
        }
        context.put(Lookup.OLD_AMSISDN, oldMsisdn);
        final Subscriber subscriber = getSubscriber(context, newAssociation);

        // unprov if MSISDN has changed
        if (!SafetyUtil.safeEquals(oldMsisdn.getMsisdn(), newAssociation.getAMsisdn(context)))
        {
            updateAssociationMsisdnChanged(context, oldAssociation, newAssociation, oldMsisdn, subscriber);
        }
        else
        {
            updateAssociationMsisdnNotChanged(context, oldAssociation, newAssociation, subscriber);
        }
    }
    
    
    /**
     * Updates an additional MSISDN association when the MSISDN has not changed.
     *
     * @param context
     *            The operating context.
     * @param oldAssociation
     *            The old association.
     * @param newAssociation
     *            The new association.
     * @param oldMsisdn
     *            The MSISDN of the association being updated.
     * @param subscriber
     *            The subscriber being updated.
     * @return The object returned by <code>super.store()</code> if called, otherwise
     *         return <code>null</code>.
     * @throws HomeException
     *             Thrown if there are problems updating the association.
     */
    protected void updateAssociationMsisdnNotChanged(final Context context,
            final SubscriberAuxiliaryService oldAssociation, final SubscriberAuxiliaryService newAssociation,
            final Subscriber subscriber) throws ExtensionAssociationException
    {

        if (this.isProvisionToECP())
        {
            final short resultCode = updateState(context, oldAssociation, newAssociation);
            if (resultCode != ErrorCode.SUCCESS)
            {
                String msg = "Unable to update Additional MSISDN information on URCS Voice profile";
                newAssociation.setProvisionActionState(false);
                throw new ExtensionAssociationException(ExternalAppEnum.VOICE, msg, msg, resultCode);
            } else
            {
                newAssociation.setProvisionActionState(true);                
            }
        }
    }
    
    public void makeMsisdnAMsisdn(final Context context, final SubscriberAuxiliaryService association, final Msisdn msisdn) throws ExtensionAssociationException
    {
        try
        {
            msisdn.setAMsisdn(true);
            msisdn.setSubAuxSvcId(association.getIdentifier());
            
            HomeSupportHelper.get(context).storeBean(context, msisdn);
        }
        catch (Exception e)
        {
            throw new ExtensionAssociationException(ExternalAppEnum.BSS, "Unable to mark MSISDN as AMSISDN for this auxiliary service: "
                    + e.getMessage(), "Unable to mark MSISDN as AMSISDN", ExternalAppSupport.BSS_DATABASE_FAILURE_MSISDN_UPDATE);
        }
    }

    
    /**
     * Claims the additional MSISDN for the subscriber.
     *
     * @param context
     *            The operating context.
     * @param association
     *            Subscriber-auxiliary service association.
     * @param oldMsisdn
     *            The old MSISDN for this association. Set to <code>null</code> if none.
     * @return MSISDN claimed.
     * @throws HomeException
     *             Thrown by home.
     */
    protected Msisdn claimMsisdn(final Context context, final SubscriberAuxiliaryService association) throws ExtensionAssociationException
    {
        final Msisdn msisdn = getMsisdn(context, association);
        final Subscriber subscriber = getSubscriber(context, association);
        String newMsisdn = association.getAMsisdn(context);

        try
        {
            MsisdnManagement.claimMsisdn(context, newMsisdn, subscriber.getBAN(), false, "aMsisdn");
        }
        catch(MsisdnAlreadyAcquiredException e)
        {
            // ignore
        }
        catch (HomeException e)
        {
            throw new ExtensionAssociationException(ExternalAppEnum.BSS, "Unable to claim MSISDN " + newMsisdn + ": "
                    + e.getMessage(), "Unable to claim additional MSISDN to subscriber's account",
                    ExternalAppSupport.BSS_DATABASE_FAILURE_MSISDN_CLAIM, e);
        }

        try
        {
            MsisdnManagement.associateMsisdnWithSubscription(context, newMsisdn, subscriber.getId(), "aMsisdn");
        }
        catch (HomeException e)
        {
            throw new ExtensionAssociationException(ExternalAppEnum.BSS, "Unable to associate MSISDN " + newMsisdn
                    + " with subscriber: " + e.getMessage(), "Unable to associate additional MSISDN with subscription",
                    ExternalAppSupport.BSS_DATABASE_FAILURE_MSISDN_ASSOCIATION, e);
        }
        
        try
        {
            return MsisdnSupport.getMsisdn(context, newMsisdn);
        }
        catch (HomeException e)
        {
            throw new ExtensionAssociationException(ExternalAppEnum.BSS, "Unable to retrieve MSISDN "
                    + msisdn.getMsisdn() + ": " + e.getMessage(), "Unable to retrieve additional MSISDN",
                    ExternalAppSupport.BSS_DATABASE_FAILURE_MSISDN_RETRIEVAL, e);
        }
    }


    /**
     * Updates an additional MSISDN association where the AMSISDN is changed.
     *
     * @param context
     *            The operating context.
     * @param oldAssociation
     *            The old association.
     * @param newAssociation
     *            The new association.
     * @param oldMsisdn
     *            The old MSISDN.
     * @param subscriber
     *            The subscriber being updated.
     * @return The result of <code>super.store()</code> if called, otherwise return
     *         <code>null</code>.
     * @throws HomeException
     *             Thrown if there are problems updating the association.
     */
    protected void updateAssociationMsisdnChanged(final Context context,
        final SubscriberAuxiliaryService oldAssociation, final SubscriberAuxiliaryService newAssociation,
        final Msisdn oldMsisdn, final Subscriber subscriber) throws ExtensionAssociationException
    {
        newAssociation.setProvisionAction(com.redknee.app.crm.bean.service.ServiceProvisionActionEnum.UPDATE_ATTRIBUTES);
        // first claim new MSISDN
        final Msisdn newMsisdn = claimMsisdn(context, newAssociation);
        if (newMsisdn == null)
        {
            throw new ExtensionAssociationException(ExternalAppEnum.BSS, "Cannot claim new MSISDN " + newAssociation.getAMsisdn(context), "Unable to claim new additional MSISDN", ExternalAppSupport.BSS_DATABASE_FAILURE_MSISDN_CLAIM);
        }
        context.put(Lookup.NEW_AMSISDN, newMsisdn);

        if (this.isProvisionToECP())
        {
            // then update ECP
            final short resultCode = updateMsisdn(context, oldAssociation, newAssociation);
            if (resultCode != ErrorCode.SUCCESS)
            {
                // release the new MSISDN
                releaseMsisdn(context, newAssociation, subscriber.getSubscriptionType(), newMsisdn, null);
                String msg = "Unable to update additional MSISDN information on URCS Voice profile";
                newAssociation.setProvisionActionState(false);
                throw new ExtensionAssociationException(ExternalAppEnum.BSS, msg, msg, resultCode);
            } else 
            {
                newAssociation.setProvisionActionState(true);
            }
        }
    }
    
    /**
     * Releases the additional MSISDN for the subscriber.
     *
     * @param context
     *            The operating context.
     * @param association
     *            Auxiliary service association being released.
     * @param msisdn
     *            MSISDN being released.
     * @param newMsisdn
     *            The new MSISDN being migrated to, or <code>null</code> if none.
     * @throws HomeException
     *             Thrown by home.
     */
    protected void releaseMsisdn(final Context context, final SubscriberAuxiliaryService association,
            final long subscriptionTypeId, final Msisdn msisdn, final String newMsisdn) throws ExtensionAssociationException
    {
        try
        {
            Claim.releaseMsisdn(context, msisdn);
        }
        catch (HomeException e)
        {
            throw new ExtensionAssociationException(ExternalAppEnum.BSS, "Unable to release MSISDN " + msisdn + ": " + e.getMessage(), "Unable to release additional MSISDN from subscriber's account", ExternalAppSupport.BSS_DATABASE_FAILURE_MSISDN_CLAIM, e
                    );
        }


        // claim MSISDN in history
        final HistoryEventSupport historySupport = (HistoryEventSupport) context.get(HistoryEventSupport.class);
        final StringBuilder sb = new StringBuilder();
        HistoryEvent event = null;
        if (newMsisdn != null)
        {
            sb.append("Migrate additional mobile number of auxiliary service ");
            sb.append(association.getAuxiliaryServiceIdentifier());
            sb.append(" to [");
            sb.append(newMsisdn);
            sb.append("]");
            try
            {
                event = historySupport.getCustomerSwapEvent(context);
            }
            catch (HomeException e)
            {
                throw new ExtensionAssociationException(ExternalAppEnum.BSS, "Unable to retrieve customer swap event: "
                        + e.getMessage(), ExternalAppSupport.BSS_DATABASE_FAILURE_CUSTOMER_SWAP_RETRIEVAL, e);
            }
        }
        else
        {
            sb.append("Remove additional mobile number of auxiliary service ");
            sb.append(association.getAuxiliaryServiceIdentifier());
            try
            {
                event = historySupport.getFeatureModificationEvent(context);
            }
            catch (HomeException e)
            {
                throw new ExtensionAssociationException(ExternalAppEnum.BSS,
                        "Unable to retrieve feature modification event: " + e.getMessage(),
                        ExternalAppSupport.BSS_DATABASE_FAILURE_FEATURE_MODIFICATION_RETRIEVAL, e);
            }
        }
        
        try
        {
            getAppendNumberMgmtHistory(context).appendSubscriptionMsisdnHistory(context, msisdn.getMsisdn(), association.getSubscriberIdentifier(),
                subscriptionTypeId, event, sb.toString());
        }
        catch (HomeException e)
        {
            throw new ExtensionAssociationException(ExternalAppEnum.BSS,
                    "Unable to update subscription MSISDN history for msisdn '" + msisdn.getMsisdn() + "': "
                            + e.getMessage(), "Unable to update additional MSISDN history",
                    ExternalAppSupport.BSS_DATABASE_FAILURE_MSISDN_HISTORY_UPDATE, e);
        }
    }


    /**
     * Provisions the additional MSISDN to ECP.
     *
     * @param context
     *            The operating context.
     * @param association
     *            Subscriber-auxiliary service association.
     * @return ECP result code.
     * @throws HomeException
     *             Thrown if there are problems retrieving anything from context.
     */
    protected short ecpProvisionMsisdn(final Context context, final SubscriberAuxiliaryService association)
        throws ExtensionAssociationException
    {
        final AppEcpClient client = getClient(context);
        final Subscriber subscriber = getSubscriber(context, association);
        return client.addAMsisdn(subscriber.getMSISDN(), association.getAMsisdn(context));
    }


    /**
     * Retrieves the ECP client from context.
     *
     * @param context
     *            The operating context.
     * @return The ECP provisioning client from context.
     * @throws HomeException
     *             Thrown if the client is not found.
     */
    protected AppEcpClient getClient(final Context context) throws ExtensionAssociationException
    {
        final AppEcpClient client = (AppEcpClient) context.get(AppEcpClient.class);
        if (client == null)
        {
            throw new ExtensionAssociationException(ExternalAppEnum.VOICE, "ECP provisioning client does not exist in context", ExternalAppSupport.NO_CONNECTION);
        }
        return client;
    }


    /**
     * Unprovisions the additional MSISDN from ECP.
     *
     * @param context
     *            The operating context.
     * @param msisdn
     *            MSISDN being unprovisioned
     * @return ECP result code.
     * @throws HomeException
     *             Thrown by home.
     */
    protected short ecpUnprovisionMsisdn(final Context context, final Msisdn msisdn) throws ExtensionAssociationException
    {
        final AppEcpClient client = getClient(context);
        return (short) client.deleteSubscriber(msisdn.getMsisdn());
    }


    /**
     * Provisions the MSISDN if it is active.
     *
     * @param context
     *            The operating context.
     * @param association
     *            The subscriber-auxiliary service association.
     * @return ECP result code.
     * @throws HomeException
     *             Thrown by home.
     */
    protected short provisionMsisdnIfActive(final Context context, final SubscriberAuxiliaryService association)
        throws ExtensionAssociationException
    {
        short resultCode = ErrorCode.SUCCESS;

        // ECP provisioning happens only if the auxiliary service is active.
        if (StateChangeAuxiliaryServiceSupport.isActive(context, association))
        {
            try
            {
                resultCode = ecpProvisionMsisdn(context, association);
            }
            catch (final ExtensionAssociationException exception)
            {
                // attempt to release the aMSISDN
                LogSupport.debug(context, this, "ECP provisioning of aMSISDN " + association.getAMsisdn(context)
                    + " failed, attempting to release MSISDN", exception);
                throw exception;
            }
        }
        return resultCode;
    }


    /**
     * Unprovisions the MSISDN if this association is provisioned.
     *
     * @param context
     *            The operating context.
     * @param association
     *            The subscriber-auxiliary service being unprovisioned.
     * @param msisdn
     *            The MSISDN being unprovisioned.
     * @return ECP result code.
     * @throws HomeException
     *             Thrown by home.
     */
    protected short unprovisionMsisdn(final Context context, final SubscriberAuxiliaryService association,
        final Msisdn msisdn) throws ExtensionAssociationException
    {
        short resultCode = ErrorCode.SUCCESS;
        if (association != null && association.isProvisioned())
        {
            try
            {
                resultCode = ecpUnprovisionMsisdn(context, msisdn);
            }
            catch (final ExtensionAssociationException exception)
            {
                // attempt to release the aMSISDN
                LogSupport.debug(context, this, "ECP unprovisioning of aMSISDN " + msisdn.getMsisdn() + " failed",
                    exception);
                throw exception;
            }
        }
        return resultCode;
    }


    /**
     * Updates an additional MSISDN in ECP.
     *
     * @param context
     *            The operating context.
     * @param subscriber
     *            The subscriber being updated.
     * @param oldMsisdn
     *            The old additional MSISDN.
     * @param newMsisdn
     *            Thew new additional MSISDN.
     * @return ECP result code.
     * @throws HomeException
     *             Thrown if there are problems provisioning to ECP.
     */
    protected short ecpUpdateMsisdn(final Context context, final Subscriber subscriber, final Msisdn oldMsisdn,
        final Msisdn newMsisdn) throws ExtensionAssociationException
    {
        final short resultCode;
        final AppEcpClient client = getClient(context);
        try
        {
            resultCode = client.changeAMsisdn(subscriber.getMSISDN(), oldMsisdn.getMsisdn(), newMsisdn.getMsisdn());
        }
        catch (final IllegalStateException exception)
        {
            LogSupport.debug(context, this, "Cannot change AMSISDN: " + exception.getMessage(), exception);
            throw new ExtensionAssociationException(ExternalAppEnum.VOICE, "Failed to update AMSISDN in ECP",
                    "Unable to update Additional MSISDN on URCS Voice", ExternalAppSupport.REMOTE_EXCEPTION, exception);
        }
        return resultCode;
    }


    /**
     * Updates the MSISDN to ECP.
     *
     * @param context
     *            The operating context.
     * @param oldAssociation
     *            The existing association to be updated.
     * @param newAssociation
     *            The updated version of the association.
     * @return ECP result code.
     * @throws HomeException
     *             Thrown if update failed.
     */
    protected short updateMsisdn(final Context context, final SubscriberAuxiliaryService oldAssociation,
        final SubscriberAuxiliaryService newAssociation) throws ExtensionAssociationException
    {
        short resultCode = ErrorCode.SUCCESS;
        final boolean wasProvisioned = oldAssociation.isProvisioned();
        final boolean isActive = StateChangeAuxiliaryServiceSupport.isActive(context, newAssociation);
        final Msisdn oldMsisdn = (Msisdn) context.get(Lookup.OLD_AMSISDN);
        if (oldMsisdn == null)
        {
            throw new ExtensionAssociationException(ExternalAppEnum.BSS, "Cannot find old MSISDN of SubscriberAuxiliaryService "
                + newAssociation.getIdentifier(), "Unable to retrieve old additional MSISDN", ExternalAppSupport.BSS_DATABASE_FAILURE_MSISDN_RETRIEVAL);
        }
        final Msisdn newMsisdn = (Msisdn) context.get(Lookup.NEW_AMSISDN);
        if (newMsisdn == null)
        {
            throw new ExtensionAssociationException(ExternalAppEnum.BSS, "Cannot find new MSISDN of SubscriberAuxiliaryService "
                + newAssociation.getIdentifier(), "Unable to retrieve new additional MSISDN", ExternalAppSupport.BSS_DATABASE_FAILURE_MSISDN_RETRIEVAL);
        }
        final Subscriber subscriber = getSubscriber(context, newAssociation);

        if (wasProvisioned && isActive)
        {
            resultCode = ecpUpdateMsisdn(context, subscriber, oldMsisdn, newMsisdn);
        }
        else if (wasProvisioned)
        {
            resultCode = ecpUnprovisionMsisdn(context, oldMsisdn);
        }
        else if (isActive)
        {
            resultCode = ecpProvisionMsisdn(context, newAssociation);
        }
        return resultCode;
    }


    /**
     * Updates ECP according to state change.
     *
     * @param context
     *            The operating context.
     * @param oldAssociation
     *            The existing association to be updated.
     * @param newAssociation
     *            The updated version of the association.
     * @return ECP result code.
     * @throws HomeException
     *             Thrown if update failed.
     */
    protected short updateState(final Context context, final SubscriberAuxiliaryService oldAssociation,
        final SubscriberAuxiliaryService newAssociation) throws ExtensionAssociationException
    {
        short resultCode = ErrorCode.SUCCESS;
        final boolean wasProvisioned = oldAssociation.isProvisioned();
        final boolean isActive = StateChangeAuxiliaryServiceSupport.isActive(context, newAssociation);
        final Msisdn oldMsisdn = (Msisdn) context.get(Lookup.OLD_AMSISDN);
        if (oldMsisdn == null)
        {
            throw new ExtensionAssociationException(ExternalAppEnum.BSS, "Cannot find old MSISDN of SubscriberAuxiliaryService "
                + newAssociation.getIdentifier(), "Unable to retrieve additional MSISDN", ExternalAppSupport.BSS_DATABASE_FAILURE_MSISDN_RETRIEVAL);
        }
        if (wasProvisioned && !isActive)
        {
            resultCode = ecpUnprovisionMsisdn(context, oldMsisdn);
        }
        else if (!wasProvisioned && isActive)
        {
            resultCode = ecpProvisionMsisdn(context, newAssociation);
        }
        return resultCode;
    }
    
    private Subscriber getSubscriber(Context ctx, SubscriberAuxiliaryService subAuxSvc) throws ExtensionAssociationException
    {
        try
        {
            return SubscriberSupport.getSubscriberForAuxiliaryService(ctx, subAuxSvc);
        }
        catch (HomeException e)
        {
            throw new ExtensionAssociationException(ExternalAppEnum.BSS, "Unable to retrieve subscription "
                    + subAuxSvc.getSubscriberIdentifier() + ": " + e.getMessage(), ExternalAppSupport.BSS_DATABASE_FAILURE_SUBSCRIPTION_RETRIEVAL, e);
        }
    }

    private Msisdn getAssociatedAMsisdn(Context ctx, SubscriberAuxiliaryService subAuxSvc) throws ExtensionAssociationException
    {
        try
        {
            return AdditionalMsisdnAuxiliaryServiceSupport.getAMsisdn(ctx, subAuxSvc);
        }
        catch (HomeException e)
        {
            throw new ExtensionAssociationException(ExternalAppEnum.BSS, "Unable to retrieve MSISDN "
                    + subAuxSvc.getAMsisdn(ctx) + ": " + e.getMessage(), "Unable to retrieve additional MSISDN", ExternalAppSupport.BSS_DATABASE_FAILURE_MSISDN_RETRIEVAL, e);
        }
    }

    private Msisdn getMsisdn(Context ctx, SubscriberAuxiliaryService subAuxSvc) throws ExtensionAssociationException
    {
        try
        {
            return MsisdnSupport.getMsisdn(ctx, subAuxSvc.getAMsisdn(ctx));
        }
        catch (HomeException e)
        {
            throw new ExtensionAssociationException(ExternalAppEnum.BSS, "Unable to retrieve MSISDN "
                    + subAuxSvc.getAMsisdn(ctx) + ": " + e.getMessage(), "Unable to retrieve additional MSISDN", ExternalAppSupport.BSS_DATABASE_FAILURE_MSISDN_RETRIEVAL, e);
        }
    }

    private AppendNumberMgmtHistoryHome getAppendNumberMgmtHistory(Context ctx)
    {
        if (appendNumberMgmtHistory_==null)
        {
            appendNumberMgmtHistory_ = new AppendNumberMgmtHistoryHome(ctx, NullHome.instance(), MsisdnMgmtHistoryHome.class){}; 
        }
        return appendNumberMgmtHistory_;
    }
    
    private AppendNumberMgmtHistoryHome appendNumberMgmtHistory_ = null;
}
