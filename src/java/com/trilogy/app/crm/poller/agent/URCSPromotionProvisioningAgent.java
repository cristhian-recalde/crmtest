/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.poller.agent;

import java.util.ArrayList;
import java.util.List;

import com.trilogy.app.crm.bean.urcs.ServiceOption;
import com.trilogy.app.crm.client.urcs.PromotionManagementClientV2;
import com.trilogy.app.crm.client.urcs.UrcsClientInstall;
import com.trilogy.app.crm.poller.FilterOutException;
import com.trilogy.app.crm.poller.ProcessorInfo;
import com.trilogy.app.crm.poller.URSPoller;
import com.trilogy.app.crm.poller.event.CRMProcessor;
import com.trilogy.app.crm.poller.event.CRMProcessorSupport;
import com.trilogy.app.crm.poller.event.URCSPromotionProvisioningProcessor;
import com.trilogy.app.urcs.promotion.v2_0.Promotion;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;


/**
 * 
 *
 * @author Aaron Gourley
 * @since 8.1
 */
public class URCSPromotionProvisioningAgent implements ContextAgent
{

    public URCSPromotionProvisioningAgent(URCSPromotionProvisioningProcessor processor)
    {
        processor_ = processor;
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void execute(Context ctx) throws AgentException
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, this, "starting to execute");
        }
        final ProcessorInfo info = (ProcessorInfo) ctx.get(ProcessorInfo.class);

        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "execute()");
        try
        {
            List<String> params = new ArrayList<String>();
            try
            {
                params = CRMProcessorSupport.makeArray(ctx, params, info.getRecord(), info.getStartIndex(), ',', info.getErid(), this);
            }
            catch (FilterOutException e)
            {
                return;
            }

            boolean wasSuccessful = Integer.valueOf(params.get(PROMOTION_PROV_ER_ARRAY_INDEX_RESULT)) == 0;
            if (wasSuccessful)
            {
                int updateType = Integer.valueOf(params.get(PROMOTION_PROV_ER_ARRAY_INDEX_UPDATE_TYPE));
                if (updateType == PROMOTION_PROV_ER_TYPE_UPDATE_STATE)
                {
                    long promotionId = Long.valueOf(params.get(PROMOTION_PROV_ER_ARRAY_INDEX_PROMOTIONID));

                    final PromotionManagementClientV2 client = UrcsClientInstall.getClient(ctx, UrcsClientInstall.PROMOTION_MANAGEMENT_CLIENT_V2_KEY);
                    
                    int spid = Integer.valueOf(params.get(PROMOTION_PROV_ER_ARRAY_INDEX_SPID));
                    
                    if (client!=null)
                    {
                        Promotion promotion = client.retrievePromotionWithSPID(ctx, spid, promotionId);
                        
                        if (promotion!=null)
                        {
                            final ServiceOption option = new ServiceOption();
                            option.setIdentifier(promotion.optionTag);
                            option.setName(promotion.name);
                            option.setSpid(promotion.spid);
                            
                            if (option != null)
                            {   
                                int eventType = Integer.valueOf(params.get(PROMOTION_PROV_ER_ARRAY_INDEX_STATE));
                                switch(eventType)
                                {
                                case PROMOTION_PROV_ER_STATE_ACTIVATED:
                                    option.unretire(ctx);
                                    break;
                                case PROMOTION_PROV_ER_STATE_RETIRED:
                                    option.retire(ctx);
                                    break;
                                case PROMOTION_PROV_ER_STATE_DEACTIVATED:
                                    option.deactivate(ctx);
                                    break;
                                default:
                                    break;
                                }
                            }
                        }
                        else
                        {
                            LogSupport.minor(ctx, this, "Failed to process ER " + URSPoller.URS_PROMOTION_PROVISIONING_ER_IDENTIFIER 
                                    + " because promotion '" + promotionId + "' could not be found.");
                            processor_.saveErrorRecord(ctx, info.getRecord());
                        }
                    }
                    else
                    {
                        LogSupport.minor(ctx, this, "Failed to process ER " + URSPoller.URS_PROMOTION_PROVISIONING_ER_IDENTIFIER 
                                + " because PromotionManagementClientV2 could not be found in the context.");
                        processor_.saveErrorRecord(ctx, info.getRecord());
                    }
                }
            }
        }
        catch (final Throwable t)
        {
            new MinorLogMsg(this, "Failed to process ER " + URSPoller.URS_PROMOTION_PROVISIONING_ER_IDENTIFIER 
                    + " because of Exception " + t.getMessage(), t).log(ctx);
            processor_.saveErrorRecord(ctx, info.getRecord());
        }
        finally
        {
            pmLogMsg.log(ctx);
        }
    }

    private CRMProcessor processor_ = null;

    private static final String PM_MODULE = URCSPromotionProvisioningAgent.class.getName();

    private static final int PROMOTION_PROV_ER_ARRAY_INDEX_SPID                 = 2;
    private static final int PROMOTION_PROV_ER_ARRAY_INDEX_UPDATE_TYPE          = 3;
    private static final int PROMOTION_PROV_ER_ARRAY_INDEX_RESULT               = 4;
    private static final int PROMOTION_PROV_ER_ARRAY_INDEX_PROMOTIONID          = 5;
    private static final int PROMOTION_PROV_ER_ARRAY_INDEX_PROMOTIONNAME        = 6;
    private static final int PROMOTION_PROV_ER_ARRAY_INDEX_PROMOTIONDESC        = 7;
    private static final int PROMOTION_PROV_ER_ARRAY_INDEX_TYPE                 = 8;
    private static final int PROMOTION_PROV_ER_ARRAY_INDEX_OPTION_TAG           = 9;
    private static final int PROMOTION_PROV_ER_ARRAY_INDEX_STATE                = 10;
    private static final int PROMOTION_PROV_ER_ARRAY_INDEX_ACTIVATION           = 11;
    private static final int PROMOTION_PROV_ER_ARRAY_INDEX_RETIREMENT           = 12;
    private static final int PROMOTION_PROV_ER_ARRAY_INDEX_EXPIRY               = 13;
    private static final int PROMOTION_PROV_ER_ARRAY_INDEX_ENABLE_STATUS        = 14;
    private static final int PROMOTION_PROV_ER_ARRAY_INDEX_COUNTER_INFO         = 15;
    private static final int PROMOTION_PROV_ER_ARRAY_INDEX_RATING_OBJECT        = 16;
    
    private static final int PROMOTION_PROV_ER_RESULT_SUCCESS                   = 0;
    private static final int PROMOTION_PROV_ER_RESULT_FAIL                      = 1;
    
    private static final int PROMOTION_PROV_ER_TYPE_CREATION                    = 0;
    private static final int PROMOTION_PROV_ER_TYPE_UPDATE                      = 1;
    private static final int PROMOTION_PROV_ER_TYPE_UPDATE_STATE                = 2;
    private static final int PROMOTION_PROV_ER_TYPE_DELETE                      = 3;

    private static final int PROMOTION_PROV_ER_STATE_PENDING                    = 0;
    private static final int PROMOTION_PROV_ER_STATE_ACTIVATED                  = 1;
    private static final int PROMOTION_PROV_ER_STATE_RETIRED                    = 2;
    private static final int PROMOTION_PROV_ER_STATE_DEACTIVATED                = 3;
}
