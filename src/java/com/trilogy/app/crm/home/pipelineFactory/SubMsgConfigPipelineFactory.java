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

import com.trilogy.app.crm.bean.SpidLangMsgConfig;
import com.trilogy.app.crm.home.MsgConfigFactory;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.framework.xhome.beans.ID;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;


/*
 * author: simar.singh@redknee.com
 */
public class SubMsgConfigPipelineFactory<MESSAGE extends SpidLangMsgConfig, MESSAGEID extends ID> implements PipelineFactory
{


    /*
     * author: simar.singh@redknee.com
     *
     */
    public SubMsgConfigPipelineFactory(Class<MESSAGE> beanClass, Class<MESSAGEID> idBeanClass, String tableName, MESSAGEID ID)
    {
        super();
        beanClass_ = beanClass;
        idBeanClass_ = idBeanClass;
        messageID_ = ID;
        tableName_ = tableName;
    }


    public Home createPipeline(final Context ctx, final Context serverCtx) throws RemoteException, HomeException,
            IOException, AgentException
    {
        MsgConfigFactory<MESSAGE, MESSAGEID> FACTORY = new MsgConfigFactory<MESSAGE, MESSAGEID>(false);
        Class homeClass = XBeans.getClass(ctx, beanClass_, Home.class);
        Home msgHome = FACTORY.createStore(ctx, beanClass_, tableName_);
        
        ctx.put(beanClass_, msgHome);
        
        ctx.put(getMessageConfigurationKey(beanClass_), FACTORY.createSupport(ctx, homeClass, beanClass_, idBeanClass_, messageID_));
        return msgHome;
    }
    
    
    public static String getMessageConfigurationKey(Class beanClass)
    {
        return beanClass.getName() + "-SUPPORT";
    }

    private Class<MESSAGE> beanClass_ ;
    private Class<MESSAGEID> idBeanClass_;
    private MESSAGEID messageID_;
    private String tableName_;
}
