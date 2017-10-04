package com.trilogy.app.crm.api.rmi.previewfees;

import com.trilogy.app.crm.api.rmi.support.RmiApiErrorHandlingSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.ContractSubscriptionUpdateCriteria;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.PricePlanSubscriptionUpdateCriteria;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionUpdateCriteria;


public class SubscriptionUpdateFeesFactory
{

    public static PreviewUpdateFees getPreviewUpdatesFees(final Context ctx, final SubscriptionUpdateCriteria[] criteria,
            final GenericParameter[] parameters) throws CRMExceptionFault
    {
        PreviewUpdateFees type = null;
        if (criteria.length > 0)
        {
            // Right now we support only contract and priceplan
            for (int i = 0; i < criteria.length; i++)
            {
                if (criteria[i] instanceof PricePlanSubscriptionUpdateCriteria
                        || (criteria[i] instanceof ContractSubscriptionUpdateCriteria))
                {
                    type = new ContractPreviewUpdateFees();// Contractprview
                    break;
                }
            }

        }
        if (type == null)
        {
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, null,
                    "Please provide valid criterias for your update preview fees" , SubscriptionUpdateFeesFactory.class);
        }
        return type;
    }
}
