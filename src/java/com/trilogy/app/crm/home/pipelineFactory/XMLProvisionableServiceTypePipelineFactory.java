/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home.pipelineFactory;

import java.io.IOException;
import java.rmi.RemoteException;

import org.apache.jasper.tagplugins.jstl.core.Remove;

import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.bean.ServiceHome;
import com.trilogy.app.crm.bean.ServiceXInfo;
import com.trilogy.app.crm.bean.service.xml.XMLProvisioningServiceType;
import com.trilogy.app.crm.bean.service.xml.XMLProvisioningServiceTypeHome;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.sequenceId.IdentifierSettingHome;
import com.trilogy.app.crm.support.AutoIncrementSupport;
import com.trilogy.app.crm.support.DeploymentTypeSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.IdentifierSequenceSupportHelper;

import com.trilogy.framework.core.platform.CoreSupport;
import com.trilogy.framework.xhome.cluster.RMIClusteredHome;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.AuditJournalHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;


/**
 * @author abaid
 * 
 */
public class XMLProvisionableServiceTypePipelineFactory implements PipelineFactory
{

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.redknee.app.crm.home.PipelineFactory#createPipeline(com.redknee.framework.xhome
     * .context.Context, com.redknee.framework.xhome.context.Context)
     */
    public Home createPipeline(final Context ctx, final Context serverCtx) throws RemoteException, HomeException,
            IOException, AgentException
    {
        Home home = CoreSupport.bindHome(ctx, XMLProvisioningServiceType.class);
        home = new HomeProxy(ctx, home)
        {
            /**
             * Ensures that a XML-Provisioning-Service-Type entities having dependencies cannot be removed
             */
            private static final long serialVersionUID = 1L;
            @Override
            public void remove(Context ctx, Object obj) throws HomeException
            {
                final XMLProvisioningServiceType xmlProvServType = (XMLProvisioningServiceType) obj;
                if (HomeSupportHelper.get(ctx).getBeanCount(ctx, ServiceHome.class, new EQ(ServiceXInfo.XML_PROV_SVC_TYPE,
                        xmlProvServType.getId())) > 0)
                {
                    throw new HomeException("One or more Services are dependent on this XML-Porivsioning-Service-Type with  Name ["
                            + xmlProvServType.getName() + "] and Internal-ID [" + xmlProvServType.getId() + "]");
                }
                super.remove(ctx,obj);
            }
        };
        home = new AuditJournalHome(ctx, home);
        home = new RMIClusteredHome(ctx, XMLProvisioningServiceTypeHome.class.getName(), home);
        
        ctx.put(XMLProvisioningServiceTypeHome.class, home);
        if (DeploymentTypeSupportHelper.get(ctx).isBas(ctx) || DeploymentTypeSupportHelper.get(ctx).isEcare(ctx))
        {
            ctx.put(XMLProvisioningServiceTypeHome.class, new IdentifierSettingHome(ctx, home,
                    IdentifierEnum.XML_SERVICE_TYPE_ID, null));
            IdentifierSequenceSupportHelper.get(ctx).ensureNextIdIsLargeEnough(ctx, IdentifierEnum.XML_SERVICE_TYPE_ID, home);
        }
        else
        {
            AutoIncrementSupport.enableAutoIncrement(ctx, (Home) ctx.get(XMLProvisioningServiceTypeHome.class));
        }
        return home;
    }
}
