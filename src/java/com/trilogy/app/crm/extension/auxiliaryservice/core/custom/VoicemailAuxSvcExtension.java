package com.trilogy.app.crm.extension.auxiliaryservice.core.custom;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.VoicemailServiceConfig;
import com.trilogy.app.crm.bean.externalapp.ExternalAppEnum;
import com.trilogy.app.crm.extension.AssociableExtension;
import com.trilogy.app.crm.extension.ExtensionAssociationException;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.ExternalAppSupport;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.voicemail.VoiceMailConstants;
import com.trilogy.app.crm.voicemail.VoiceMailServer;
import com.trilogy.app.crm.voicemail.client.ExternalProvisionResult;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;

public class VoicemailAuxSvcExtension extends
        com.redknee.app.crm.extension.auxiliaryservice.core.VoicemailAuxSvcExtension implements
        AssociableExtension<SubscriberAuxiliaryService>
{
    @Override
    public void associate(Context ctx, SubscriberAuxiliaryService subAuxSvc) throws ExtensionAssociationException
    {
        createVoiceMailSvc(ctx, subAuxSvc);
    }

    @Override
    public void updateAssociation(Context ctx, SubscriberAuxiliaryService subAuxSvc)
            throws ExtensionAssociationException
    {
        modifyVoiceMailSvc(ctx, subAuxSvc);
    }

    @Override
    public void dissociate(Context ctx, SubscriberAuxiliaryService subAuxSvc) throws ExtensionAssociationException
    {
        removeVoiceMailSvc(ctx, subAuxSvc);
    }

    private void createVoiceMailSvc(Context ctx, SubscriberAuxiliaryService subAuxSvc) throws ExtensionAssociationException
    {
        boolean updateVM = (((VoicemailServiceConfig) ctx.get(VoicemailServiceConfig.class)).getProvVM());
        if (updateVM)// If provisoning in voicemail enabled
        {
            // provision to voicemail only if the start date <= current date
            Date runningDate = CalendarSupportHelper.get(ctx).getRunningDate(ctx);
            if (CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(subAuxSvc.getStartDate())
                    .equals(CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(runningDate))
                    || CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(subAuxSvc.getStartDate())
                            .before(CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(runningDate)))
            {
                addUserInVoicemail(ctx, subAuxSvc);
            }
        }
    }


    private void modifyVoiceMailSvc(Context ctx, SubscriberAuxiliaryService subAuxSvc) throws ExtensionAssociationException
    {
        boolean updateVM = (((VoicemailServiceConfig) ctx.get(VoicemailServiceConfig.class)).getProvVM());
        if (updateVM)// If provisoning in voicemail enabled
        {
            // provision to voicemail only if the start date <= current date
            Date runningDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(CalendarSupportHelper.get(ctx).getRunningDate(ctx));
            Date startDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(subAuxSvc.getStartDate());
            if ((startDate.equals(runningDate) || startDate.before(runningDate))
                    && subAuxSvc.getProvisioned())
            {
                modifyUserInVoicemail(ctx, subAuxSvc);
            }
        }
    }


    private void removeVoiceMailSvc(Context ctx, SubscriberAuxiliaryService subAuxSvc) throws ExtensionAssociationException
    {
        boolean updateVM = (((VoicemailServiceConfig) ctx.get(VoicemailServiceConfig.class)).getProvVM());
        if (updateVM)// If provisoning in voicemail enabled
        {
            // provision to voicemail only if the start date <= current date
            Date runningDate = CalendarSupportHelper.get(ctx).getRunningDate(ctx);
            if ((CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(subAuxSvc.getStartDate())
                    .equals(CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(runningDate)) || CalendarSupportHelper
                    .get(ctx).getDateWithNoTimeOfDay(subAuxSvc.getStartDate())
                    .before(CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(runningDate)))
                    && subAuxSvc.getProvisioned())
            {
                removeUserInVoicemail(ctx, subAuxSvc);
            }
        }
    }


    private void addUserInVoicemail(Context ctx, SubscriberAuxiliaryService subAuxSvc) throws ExtensionAssociationException
    {
        final Subscriber sub = getSubscriber(ctx, subAuxSvc);
        final AuxiliaryService service = this.getAuxiliaryService(ctx);

        if (service == null)
        {
            subAuxSvc.setProvisionActionState(false);
            throw new ExtensionAssociationException(ExternalAppEnum.VOICEMAIL, "Fail to provision voice mail, the auxiliary service "
                    + subAuxSvc.getAuxiliaryServiceIdentifier() + " not found", ExternalAppSupport.NO_CONNECTION);
        }
        else
        {
            final VoiceMailServer vmServer = (VoiceMailServer) ctx.get(VoiceMailServer.class);
            final ExternalProvisionResult ret = vmServer.provision(ctx, sub, service);

            if (ret.getCrmVMResultCode() != VoiceMailConstants.RESULT_SUCCESS)
            {
                subAuxSvc.setProvisionActionState(false);
                throw new ExtensionAssociationException(ExternalAppEnum.VOICEMAIL, "Fail to provision voice mail, result = " + ret.getCrmVMResultCode(), "Unable to provision voicemail profile", ret.getCrmVMResultCode());

            } else
            {
                subAuxSvc.setProvisionActionState(true);;
            }

        }

    }


    private void modifyUserInVoicemail(Context ctx, SubscriberAuxiliaryService subAuxSvc) throws ExtensionAssociationException
    {
        final AuxiliaryService service = this.getAuxiliaryService(ctx);

        final Subscriber newSub = getSubscriber(ctx, subAuxSvc);
        final Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
        final VoiceMailServer vmServer = (VoiceMailServer) ctx.get(VoiceMailServer.class);
        ExternalProvisionResult ret = null;

        // subscriber msisdn change
        if (!SafetyUtil.safeEquals(oldSub.getMSISDN(), newSub.getMSISDN()))
        {
            ret = vmServer.changeMsisdn(ctx, oldSub, newSub.getMSISDN());

            if (ret.getCrmVMResultCode() != VoiceMailConstants.RESULT_SUCCESS)
            {
                subAuxSvc.setProvisionActionState(false);
                throw new ExtensionAssociationException(ExternalAppEnum.VOICEMAIL, "Fail to change MSISDN on  voice mail, result = " + ret.getCrmVMResultCode(), "Unable to change Mobile Number on voicemail profile", ret.getCrmVMResultCode());

            }
        }

        if (service == null)
        {
            subAuxSvc.setProvisionActionState(false);
            throw new ExtensionAssociationException(ExternalAppEnum.BSS, "Fail to modify user in voice mail, the auxiliary service "
                    + this.getAuxiliaryServiceId() + " was not found", "Unable to modify user in voicemail", ExternalAppSupport.BSS_DATABASE_FAILURE_AUXILIARY_SERVICE_RETRIEVAL);
        }
        else if (EnumStateSupportHelper.get(ctx).isTransition(oldSub, newSub, ACTIVATION_STATES, DEACTIVATION_STATES))
        {
            // deactivate
            ret = vmServer.deactivate(ctx, newSub, service);

            if (ret.getCrmVMResultCode() != VoiceMailConstants.RESULT_SUCCESS)
            {
                subAuxSvc.setProvisionActionState(false);
                throw new ExtensionAssociationException(ExternalAppEnum.VOICEMAIL, "Fail to deactivate voice mail profile, result = " + ret.getCrmVMResultCode(), "Unable to deactivate voicemail profile", ret.getCrmVMResultCode());
            } else 
            {
                subAuxSvc.setProvisionedState(com.redknee.app.crm.bean.service.ServiceStateEnum.PROVISIONED);
            }
        }
        else if (EnumStateSupportHelper.get(ctx).isTransition(oldSub, newSub, DEACTIVATION_STATES, ACTIVATION_STATES))
        {
            // activate
            ret = vmServer.activate(ctx, newSub, service);

            if (ret.getCrmVMResultCode() != VoiceMailConstants.RESULT_SUCCESS)
            {
                subAuxSvc.setProvisionActionState(false);
                throw new ExtensionAssociationException(ExternalAppEnum.VOICEMAIL, "Fail to activate voice mail profile, result = " + ret.getCrmVMResultCode(), "Unable to activate voicemail profile", ret.getCrmVMResultCode());
            } else 
            {
                subAuxSvc.setProvisionActionState(true);
            }
        }
    }


    private void removeUserInVoicemail(Context ctx, SubscriberAuxiliaryService subAuxSvc) throws ExtensionAssociationException
    {
        final Subscriber sub = getSubscriber(ctx, subAuxSvc);
        AuxiliaryService service = this.getAuxiliaryService(ctx);

        if (service == null)
        {
            subAuxSvc.setProvisionActionState(false);
            throw new ExtensionAssociationException(ExternalAppEnum.VOICEMAIL, "Fail to unprovision voice mail, the auxiliary service "
                    + this.getAuxiliaryServiceId() + " not found", ExternalAppSupport.NO_CONNECTION);
        }
        else
        {
            VoiceMailServer vmServer = (VoiceMailServer) ctx.get(VoiceMailServer.class);
            final ExternalProvisionResult ret = vmServer.unprovision(ctx, sub, service);

            if (ret.getCrmVMResultCode() != VoiceMailConstants.RESULT_SUCCESS)
            {
                subAuxSvc.setProvisionActionState(false);
                throw new ExtensionAssociationException(ExternalAppEnum.VOICEMAIL, "Fail to unprovision voice mail, result = " + ret.getCrmVMResultCode(), "Unable to unprovision voicemail profile", ret.getCrmVMResultCode());
            } else 
            {
                subAuxSvc.setProvisionActionState(true);
            }

        }

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
                    + subAuxSvc.getSubscriberIdentifier() + ": " + e.getMessage(), ExternalAppSupport.BSS_DATABASE_FAILURE_SUBSCRIPTION_RETRIEVAL, e, false);
        }
    }

    private static final Collection<SubscriberStateEnum> DEACTIVATION_STATES = Collections
                                                                                     .unmodifiableCollection(Arrays
                                                                                             .asList(SubscriberStateEnum.SUSPENDED,
                                                                                                     SubscriberStateEnum.LOCKED));

    private static final Collection<SubscriberStateEnum> ACTIVATION_STATES   = Collections
                                                                                     .unmodifiableCollection(Arrays
                                                                                             .asList(SubscriberStateEnum.ACTIVE,
                                                                                                     SubscriberStateEnum.PROMISE_TO_PAY));

    private static final long                            serialVersionUID    = 1L;
}
