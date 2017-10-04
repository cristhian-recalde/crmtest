package com.trilogy.app.crm.client;

import java.util.HashMap;
import java.util.Map;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.config.TFACorbaClientConfig;
import com.trilogy.app.crm.config.TFACorbaClientConfigHome;
import com.trilogy.app.crm.home.pipelineFactory.CRMSpidHomePipelineFactory;
import com.trilogy.app.crm.support.SystemStatusSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.LogSupport;


public class TFAAuxiliaryServiceClientFactory
{

    private static TFAAuxiliaryServiceClientFactory tfaClientFactory_ = null;


    private TFAAuxiliaryServiceClientFactory(Context ctx)
    {
        try
        {
            if (ctx == null)
            {
                ctx = ContextLocator.locate();
            }
            
            Home crmSpidHome = (Home) ctx.get(CRMSpidHome.class);
            if(crmSpidHome == null)
            {
                Context serverCtx = ctx.createSubContext("RMI Server Context");
                ctx.put(CRMSpidHome.class, CRMSpidHomePipelineFactory.instance().createPipeline(ctx, serverCtx));
                crmSpidHome = (Home) ctx.get(CRMSpidHome.class);
            }
            crmSpidHome.forEach(ctx, new Visitor()
            {

                @Override
                public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException
                {
                    CRMSpid crmSpidBean = (CRMSpid) obj;
                    try
                    {
                        getTfaClient(ctx, crmSpidBean.getSpid());
                    }
                    catch (TFAAuxiliarServiceClientException e)
                    {
                        // no op
                    }
                }
            });
        }
        catch (Exception e)
        {
            LogSupport.info(null,this, e.getMessage());
        }
    }


    public static TFAAuxiliaryServiceClientFactory getInstance(Context ctx)
    {
        synchronized (TFAAuxiliaryServiceClientFactory.class)
        {
            if (tfaClientFactory_ == null)
            {
                tfaClientFactory_ = new TFAAuxiliaryServiceClientFactory(ctx);
            }
        }
        return tfaClientFactory_;
    }


    public ITFAAuxiliaryServiceClient getTfaClient(Context context, int spid) throws TFAAuxiliarServiceClientException
    {
        ITFAAuxiliaryServiceClient client = null;
        synchronized (clientMap_)
        {
            client = clientMap_.get(spid);
            if (client != null)
            {
                return client;
            }
            else
            {
                TFACorbaClientConfig tfaCorbaClientConfig = getTfaCorbaClientConfig(context, spid);
                if (tfaCorbaClientConfig != null)
                {
                    client = new TFAAuxiliaryServiceClient(context, tfaCorbaClientConfig.getUsername(),
                            tfaCorbaClientConfig.getPassword());
                    clientMap_.put(spid, client);
                    SystemStatusSupportHelper.get(context).registerExternalService(context, client);
                }
                else
                {
                    throw new TFAAuxiliarServiceClientException("TFA cobra client is not configured for SPID : " + spid);
                }
            }
        }
        return client;
    }


    private TFACorbaClientConfig getTfaCorbaClientConfig(Context context, int spid)
    {
        try
        {
            Home tfaCorbaClientHome = (Home) context.get(TFACorbaClientConfigHome.class);
            return (TFACorbaClientConfig) tfaCorbaClientHome.find(context, spid);
        }
        catch (Exception e)
        {
            LogSupport.major(context, this, "Exception occurred while looking up TFA Corba Client config for spid : "
                    + spid, e);
        }
        return null;
    }

    Map<Integer, ITFAAuxiliaryServiceClient> clientMap_ = new HashMap<Integer, ITFAAuxiliaryServiceClient>();
}
