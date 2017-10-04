package com.trilogy.app.crm.home;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.api.rmi.GroupScreeningTemplateToApiAdapter;
import com.trilogy.app.crm.api.rmi.support.RmiApiErrorHandlingSupport;
import com.trilogy.app.crm.bean.GroupScreeningTemplate;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.client.RemoteServiceException;
import com.trilogy.app.crm.client.urcs.ScreeningTemplateProvisionClient;
import com.trilogy.app.crm.client.urcs.UrcsClientInstall;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.product.bundle.manager.provision.profile.error.ErrorCode;
import com.trilogy.product.bundle.manager.provision.v5_0.screeningTemplate.ScreeningTemplate;
import com.trilogy.product.bundle.manager.provision.v5_0.screeningTemplate.ScreeningTemplateReturnParam;
import com.trilogy.product.bundle.manager.provision.v5_0.screeningTemplate.ServiceLevelUsage;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;

public class GroupScreeningTemplateModifyHome extends HomeProxy
{

    private static final long serialVersionUID = 1L;


    public GroupScreeningTemplateModifyHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(final Context ctx, Object obj) throws HomeException
    {
        GroupScreeningTemplate groupScreeningTemplate = (GroupScreeningTemplate) obj;

        ScreeningTemplateProvisionClient client = UrcsClientInstall.getClient(ctx,
                ScreeningTemplateProvisionClient.class);
        try
        {
            ScreeningTemplate screeningTemplate = GroupScreeningTemplateToApiAdapter
                    .adaptScreeningTemplateRequestToScreeningTemplate(ctx, groupScreeningTemplate);
            ServiceLevelUsage[] serviceLevelUsageArray = GroupScreeningTemplateToApiAdapter
                    .adaptScreeningTemplateToServiceLevelUsage(ctx, groupScreeningTemplate);

            // call URCS client
            ScreeningTemplateReturnParam result = client.updateScreeningTemplate(ctx, 0, screeningTemplate,
                    serviceLevelUsageArray);
            if (ErrorCode.SUCCESS != result.resultCode)
            {
                return handleCreateOnUrcsFailure(ctx, groupScreeningTemplate, null);
            }
        }
        catch (final RemoteServiceException e)
        {
            return handleCreateOnUrcsFailure(ctx, groupScreeningTemplate, e);
        }

        try
        {
            return super.create(ctx, groupScreeningTemplate);
        }
        catch (HomeException e)
        {
            // revert CreatedGroupScreeningTemplate on URCS
            rollbackGroupScreeningTemplate(ctx, groupScreeningTemplate, e);
            throw e;
        }
    }


    /**
     * @param ctx
     * @param exception
     * @return
     */
    private Object handleCreateOnUrcsFailure(Context ctx, GroupScreeningTemplate groupScreeningTemplate,
            Exception exception) throws HomeException
    {
        final String msg = "Unable to create GroupScreeningTemplate in URCS for id :  "
                + groupScreeningTemplate.getIdentifier();

        if (exception != null)
        {
            try
            {
                RmiApiErrorHandlingSupport.handleCreateExceptions(ctx, exception, msg, false,
                        GroupScreeningTemplate.class, null, this);
            }
            catch (CRMExceptionFault crmExceptionFault)
            {
                LogSupport.major(ctx, this, msg, crmExceptionFault);
            }
        }

        throw new HomeException(msg);
    }


    private void rollbackGroupScreeningTemplate(final Context ctx, GroupScreeningTemplate groupScreeningTemplate,
            final Exception e)
    {
        if (groupScreeningTemplate != null)
        {
            // Rollback
            try
            {
                LogSupport.info(ctx, this, "Attempting to rollback locally", e);
                remove(ctx, groupScreeningTemplate);
                super.remove(ctx, groupScreeningTemplate);
            }
            catch (Exception e2)
            {
                LogSupport.major(ctx, this, "Failed to rollback locally", e2);
            }
        }
    }


    @Override
    public Object store(Context ctx, final Object obj) throws HomeException
    {
        GroupScreeningTemplate groupScreeningTemplate = (GroupScreeningTemplate) obj;

        ScreeningTemplateProvisionClient client = UrcsClientInstall.getClient(ctx,
                ScreeningTemplateProvisionClient.class);
        try
        {
            ScreeningTemplate screeningTemplate = GroupScreeningTemplateToApiAdapter
                    .adaptScreeningTemplateRequestToScreeningTemplate(ctx, groupScreeningTemplate);
            ServiceLevelUsage[] serviceLevelUsageArray = GroupScreeningTemplateToApiAdapter
                    .adaptScreeningTemplateToServiceLevelUsage(ctx, groupScreeningTemplate);

            // call URCS client
            client.updateScreeningTemplate(ctx, 1, screeningTemplate, serviceLevelUsageArray);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to update GroupScreeningTemplate in URCS for id :  "
                    + groupScreeningTemplate.getIdentifier() + ".";
            try
            {
                RmiApiErrorHandlingSupport.handleCreateExceptions(ctx, e, msg, false, GroupScreeningTemplate.class,
                        null, this);
            }
            catch (CRMExceptionFault e1)
            {
                throw new HomeException(msg);
            }

        }
        return super.store(ctx, groupScreeningTemplate);
    }


    @Override
    public void remove(final Context ctx, Object obj) throws HomeException
    {
        GroupScreeningTemplate groupScreeningTemplate = (GroupScreeningTemplate) obj;

        long sub = HomeSupportHelper.get(ctx).getBeanCount(ctx, Subscriber.class,
                new EQ(SubscriberXInfo.GROUP_SCREENING_TEMPLATE_ID, groupScreeningTemplate.getIdentifier()));
        if (sub > 0)
        {
            throw new HomeException("Cannot delete IN_USE screening template. It is already attached to a subscriber.");
        }

        ScreeningTemplateProvisionClient client = UrcsClientInstall.getClient(ctx, ScreeningTemplateProvisionClient.class);
        try
        {
            ScreeningTemplate screeningTemplate = GroupScreeningTemplateToApiAdapter.adaptScreeningTemplateRequestToScreeningTemplate(ctx, groupScreeningTemplate);
            ServiceLevelUsage[] serviceLevelUsageArray = GroupScreeningTemplateToApiAdapter.adaptScreeningTemplateToServiceLevelUsage(ctx, groupScreeningTemplate);
            // call URCS client
            client.updateScreeningTemplate(ctx, 2, screeningTemplate, serviceLevelUsageArray);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to delete GroupScreeningTemplate in URCS for id : " + groupScreeningTemplate.getIdentifier() + ".";

            try
            {
                RmiApiErrorHandlingSupport.handleCreateExceptions(ctx, e, msg, false, GroupScreeningTemplate.class,
                        null, this);
            }
            catch (CRMExceptionFault e1)
            {
                throw new HomeException(msg);
            }
        }
        super.remove(ctx, groupScreeningTemplate);
    }

}
