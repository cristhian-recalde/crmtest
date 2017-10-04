package com.trilogy.app.crm.extension.auxiliaryservice.core.custom;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.BirthdayPlan;
import com.trilogy.app.crm.bean.BirthdayPlanHome;
import com.trilogy.app.crm.bean.CallingGroupTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.externalapp.ExternalAppEnum;
import com.trilogy.app.crm.extension.ExtendedAssociableExtension;
import com.trilogy.app.crm.extension.ExtensionAssociationException;
import com.trilogy.app.crm.ff.FFClosedUserGroupSupport;
import com.trilogy.app.crm.ff.FFEcareException;
import com.trilogy.app.crm.ff.PersonalListPlanSupport;
import com.trilogy.app.crm.poller.event.FnFSelfCareProcessor;
import com.trilogy.app.crm.support.CallingGroupSupport;
import com.trilogy.app.crm.support.ClosedUserGroupSupport73;
import com.trilogy.app.crm.support.ExternalAppSupport;
import com.trilogy.app.crm.support.LicensingSupportHelper;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.ff.ecare.rmi.FFECareRmiConstants;
import com.trilogy.app.ff.ecare.rmi.FFECareRmiService;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.MajorLogMsg;

public class CallingGroupAuxSvcExtension extends
com.redknee.app.crm.extension.auxiliaryservice.core.CallingGroupAuxSvcExtension implements
ExtendedAssociableExtension<SubscriberAuxiliaryService>
{

    private static final long serialVersionUID = 1L;

    @Override
    public void associate(Context ctx, SubscriberAuxiliaryService associatedBean) throws ExtensionAssociationException
    {
       
        if (CallingGroupTypeEnum.BP.equals(this.getCallingGroupType()))
        {
            associateBP(ctx, associatedBean);
        }
        else if (CallingGroupTypeEnum.PLP.equals(this.getCallingGroupType()))
        {
            associatePLP(ctx, associatedBean);
        }
    }

    @Override
    public void updateAssociation(Context ctx, SubscriberAuxiliaryService associatedBean)
            throws ExtensionAssociationException
    {
        if (CallingGroupTypeEnum.BP.equals(this.getCallingGroupType()))
        {
            updateAssociationBP(ctx, associatedBean);
        }
        else if (CallingGroupTypeEnum.PLP.equals(this.getCallingGroupType()))
        {
            updateAssociationPLP(ctx, associatedBean);
        }
    }

    @Override
    public void dissociate(Context ctx, SubscriberAuxiliaryService associatedBean) throws ExtensionAssociationException
    {
        if (CallingGroupTypeEnum.BP.equals(this.getCallingGroupType()))
        {
            dissociateBP(ctx, associatedBean);
        }
        else if (CallingGroupTypeEnum.PLP.equals(this.getCallingGroupType()))
        {
            dissociatePLP(ctx, associatedBean);
        }
    }

    @Override
    public void postExternalBeanCreation(Context ctx, SubscriberAuxiliaryService associatedBean, boolean success)
            throws ExtensionAssociationException
    {
        
    }

    @Override
    public void postExternalBeanUpdate(Context ctx, SubscriberAuxiliaryService associatedBean, boolean success)
            throws ExtensionAssociationException
    {
        
    }

    @Override
    public void postExternalBeanRemoval(Context ctx, SubscriberAuxiliaryService associatedBean, boolean success)
            throws ExtensionAssociationException
    {
        try
        {
            ClosedUserGroupSupport73.removeSubscriberFromClosedUserGroup(ctx, associatedBean);
        }
        catch (HomeException e)
        {
            if (e.getCause()!=null && e.getCause() instanceof FFEcareException)
            {
                FFEcareException exception = (FFEcareException) e.getCause();
                throw new ExtensionAssociationException(ExternalAppEnum.FF, exception.getMessage(), exception.getResultCode(), e);
            }
            else
            {
                throw new ExtensionAssociationException(ExternalAppEnum.BSS,
                        "Unable to clean calling group extension: " + e.getMessage(), e.getMessage(),
                        ExternalAppSupport.UNKNOWN, e, false);
            }
        }        
        
    }

    public void associateBP(Context ctx, SubscriberAuxiliaryService association) throws ExtensionAssociationException
    {
        if (LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.FNF_BIRTHDAYPLAN_LICENSE))
        {
            if (association.isProvisioned())
            {
                Subscriber subscriber = getSubscriber(ctx, association);
                updateBirthdayPlan(ctx, subscriber, getAccount(ctx, subscriber).getDateOfBirth(), association);
            }
        }
    }
    
    public void updateAssociationBP(Context ctx, SubscriberAuxiliaryService association)
            throws ExtensionAssociationException
    {
        if (LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.FNF_BIRTHDAYPLAN_LICENSE))
        {
            if (association.isProvisioned())
            {
                Subscriber subscriber = getSubscriber(ctx, association);
                updateBirthdayPlan(ctx, subscriber, getAccount(ctx, subscriber).getDateOfBirth(), association);
            }
        }
    }


    public void dissociateBP(Context ctx, SubscriberAuxiliaryService association) throws ExtensionAssociationException
    {
        if (LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.FNF_BIRTHDAYPLAN_LICENSE))
        {
            if (association.isProvisioned())
            {
                Subscriber subscriber = getSubscriber(ctx, association);
                removeBirthdayPlan(ctx, subscriber, association);
            }
        }
    }

    public void associatePLP(Context ctx, SubscriberAuxiliaryService association) throws ExtensionAssociationException
    {
        if (association.isProvisioned())
        {
            try 
            {
                PersonalListPlanSupport.addPLP(ctx, association, this.getCallingGroup());
                association.setProvisionActionState(true);

            }
            catch (FFEcareException e)
            {
                association.setProvisionActionState(false);
                throw new ExtensionAssociationException(ExternalAppEnum.FF, e.getMessage(), e.getResultCode(), e);
            }
            catch (Exception e )
            {
                association.setProvisionActionState(false);
                throw new ExtensionAssociationException(ExternalAppEnum.BSS, e.getMessage(), ExternalAppSupport.BSS_DATABASE_FAILURE_SUBSCRIPTION_RETRIEVAL, e);  
            }
        }
    }
    
    public void updateAssociationPLP(Context ctx, SubscriberAuxiliaryService association)
            throws ExtensionAssociationException
    {
        SubscriberAuxiliaryService oldSubscriberAuxiliaryService = 
                SubscriberAuxiliaryServiceSupport.getSubscriberAuxiliaryServicesBySubIdAndSvcId(ctx,
                        association.getSubscriberIdentifier(), association.getAuxiliaryServiceIdentifier()); 
            
            if (association.isProvisioned() && !oldSubscriberAuxiliaryService.isProvisioned())
            {
                try
                {
                    PersonalListPlanSupport.addPLP(ctx, association, this.getCallingGroup());
                    association.setProvisionActionState(true);

                }
                catch (FFEcareException e)
                {
                    association.setProvisionActionState(false);
                    throw new ExtensionAssociationException(ExternalAppEnum.FF, e.getMessage(), e.getResultCode(), e);
                }
                catch (Exception e )
                {
                    association.setProvisionActionState(false);
                    throw new ExtensionAssociationException(ExternalAppEnum.BSS, e.getMessage(), ExternalAppSupport.BSS_DATABASE_FAILURE_SUBSCRIPTION_RETRIEVAL, e);  
                }
            }
    }
    
    public void dissociatePLP(Context ctx, SubscriberAuxiliaryService association) throws ExtensionAssociationException
    {
        if (association.isProvisioned())
        {
            try {
                PersonalListPlanSupport.deletePLP(ctx, association, this.getCallingGroup());
                association.setProvisionActionState(true);

            }  
            catch (FFEcareException e)
            {
                association.setProvisionActionState(false);
                throw new ExtensionAssociationException(ExternalAppEnum.FF, e.getMessage(), e.getResultCode(), e);
            }
            catch (Exception e )
            {
                association.setProvisionActionState(false);
                throw new ExtensionAssociationException(ExternalAppEnum.BSS, e.getMessage(), ExternalAppSupport.BSS_DATABASE_FAILURE_SUBSCRIPTION_RETRIEVAL, e);  
            }
        }
    }

    /**
     * Removes the Birthday Plan associated with the subscriber within the Friends and Family application.
     *
     * @param ctx
     *            The operating context.
     * @param subscriber
     *            The subscriber for which to remove a PLP.
     * @exception HomeException
     *                Thrown if there are problems communicating with the services stored in the context.
     */
    private void removeBirthdayPlan(final Context ctx, final Subscriber subscriber, SubscriberAuxiliaryService association) throws ExtensionAssociationException
    {
        final FFECareRmiService service = getFFECareRmiService(ctx);

        //final BirthdayPlan plan = subscriber.getBirthdayPlan(ctx);
        //final long planID = plan.getID();

        try
        {
            if (ctx.has(FnFSelfCareProcessor.FNF_FROM_SELFCARE_ER_POLLER))
            {
                // In case of FnFSelfCare Poller, the subscriber's FnF profile would already be removed from
                // CUG/PLP,hence no need to make a call to ECP. [amit]
                new DebugLogMsg(this, "The context under use has the key 'FnFPoller',call to ECP is bypassed", null)
                    .log(ctx);
                return;
            }

            final int result = service.deleteBirthdayPlanForSub(subscriber.getSpid(), subscriber.getMSISDN());

            if (result != FFECareRmiConstants.FF_ECARE_SUCCESS)
            {
                association.setProvisionActionState(false);
                throw new ExtensionAssociationException(ExternalAppEnum.FF, "Friends and Family service returned "
                    + CallingGroupSupport.getDisplayMessageForReturnCode(ctx, result)
                    + " while attempting to delete Birthday Plan.", result, false);
            } else 
            {
                association.setProvisionActionState(true);
            }
            
        }
        catch (final RemoteException exception)
        {
            association.setProvisionActionState(false);
            throw new ExtensionAssociationException(ExternalAppEnum.FF, "Failed to remove Birthday Plan"
                + " from subscriber " + subscriber.getId(), ExternalAppSupport.REMOTE_EXCEPTION, exception, false);
        }
    }

    private FFECareRmiService getFFECareRmiService(Context ctx) throws ExtensionAssociationException
    {
        try
        {
            return FFClosedUserGroupSupport.getFFRmiService(ctx, this.getClass());
        }
        catch (FFEcareException e)
        {
            throw new ExtensionAssociationException(ExternalAppEnum.FF, "Unable to retrieve FFECareRmiService: " + e.getMessage(), e.getResultCode(), e, false);
        }
    }

    /**
     * Updates the date of birth and the plan ID in the subscriber's BithdayPlan in the Friends and Family application.
     *
     * @param ctx The operating context.
     * @param subscriber The subscriber to update.
     * @param birthDay The date of birth for the subscriber.
     * @exception HomeException Thrown if there are problems communicating with the services stored in the context.
     */
    public void updateBirthdayPlan(final Context ctx, final Subscriber subscriber, final Date birthDay, SubscriberAuxiliaryService association)
        throws ExtensionAssociationException
    {
        final FFECareRmiService service = getFFECareRmiService(ctx);

        final Calendar cal = Calendar.getInstance();
        cal.setTime(birthDay);

        final int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        // Java month count starts at 0 for Jan. Should send 1 for Jan.
        final int month = cal.get(Calendar.MONTH) + 1;
        final String timeZone = TimeZone.getDefault().getID();

        final BirthdayPlan plan = retrieveBirthdayPlan(ctx, subscriber);

        try
        {
            final int result = service.updateBirthdayPlanForSub(subscriber.getMSISDN(), plan.getID(),
                    dayOfMonth, month, timeZone);

            if (result != FFECareRmiConstants.FF_ECARE_SUCCESS)
            {
                association.setProvisionActionState(false);
                throw new ExtensionAssociationException(ExternalAppEnum.FF, "Friends and Family service returned "
                    + CallingGroupSupport.getDisplayMessageForReturnCode(ctx, result)
                    + " while attempting to update personal list plan.", result, false);
            } else 
            {
                association.setProvisionActionState(true);
            }
        }
        catch (final RemoteException throwable)
        {
            association.setProvisionActionState(false);
            throw new ExtensionAssociationException(ExternalAppEnum.FF,
                    "Failed to update subscriber's personal list plan.", ExternalAppSupport.REMOTE_EXCEPTION,
                    throwable, false);
        }
    }

    private BirthdayPlan retrieveBirthdayPlan(Context ctx, Subscriber subscriber)
    {
        BirthdayPlan plan = null;
        try
        {
            Home bpHome = (Home) ctx.get(BirthdayPlanHome.class);
            long callingGroupIdentifier = this.getCallingGroupIdentifier();
            plan = (BirthdayPlan) bpHome.find(ctx, Long.valueOf(callingGroupIdentifier));
        }
        catch (final HomeException exception)
        {
            new MajorLogMsg(this,
                    "Failed to look-up birthday plan for  " + subscriber.getId(), exception).log(ctx);
        }
        return plan;
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

    private Account getAccount(Context ctx, Subscriber subscriber) throws ExtensionAssociationException
    {
        try
        {
            return subscriber.getAccount(ctx);
        }
        catch (HomeException e)
        {
            throw new ExtensionAssociationException(ExternalAppEnum.BSS, "Unable to retrieve subscription "
                    + subscriber.getId() + ": " + e.getMessage(), ExternalAppSupport.BSS_DATABASE_FAILURE_ACCOUNT_RETRIEVAL, e, false);
        }
    }

}
