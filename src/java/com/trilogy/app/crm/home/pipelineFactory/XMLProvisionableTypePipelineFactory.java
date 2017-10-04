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

import com.trilogy.app.crm.bean.ServiceHome;
import com.trilogy.app.crm.bean.ServiceXInfo;
import com.trilogy.app.crm.bean.service.xml.XMLProvisionableType;
import com.trilogy.app.crm.bean.service.xml.XMLProvisionableTypeHome;
import com.trilogy.app.crm.bean.service.xml.XMLProvisionableTypeXInfo;
import com.trilogy.app.crm.bean.service.xml.XMLProvisioningServiceType;
import com.trilogy.app.crm.bean.service.xml.XMLProvisioningServiceTypeXInfo;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.support.HomeSupportHelper;
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
public class XMLProvisionableTypePipelineFactory implements PipelineFactory
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
        Home home = CoreSupport.bindHome(ctx, XMLProvisionableType.class);
        home = new HomeProxy(ctx, home)
        {

            /**
             * Ensures that a XML-Provisioning-Type entities having dependencies cannot be removed
             */
            private static final long serialVersionUID = 1L;
            @Override
            public void remove(Context ctx, Object obj) throws HomeException
            {
                final XMLProvisionableType xmlProvType = (XMLProvisionableType) obj;
                if (HomeSupportHelper.get(ctx).getBeanCount(ctx, XMLProvisioningServiceType.class, new EQ(
                        XMLProvisioningServiceTypeXInfo.SERVICE_TYPE, xmlProvType.getType())) > 0)
                {
                    throw new HomeException("One or more XML-Provisioning-Service-Types are dependent on this XML-Porivsioning-Type with  Name ["
                            + xmlProvType.getName() + "] and type [" + xmlProvType.getType() + "]");
                }
                super.remove(ctx, obj);
            }
        };
        home = new AuditJournalHome(ctx,home);
        home = new RMIClusteredHome(ctx, XMLProvisionableTypeHome.class.getName(), home);
        ctx.put(XMLProvisionableTypeHome.class, home);
        return home;
    }
}
