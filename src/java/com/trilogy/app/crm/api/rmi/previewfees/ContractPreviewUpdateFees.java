package com.trilogy.app.crm.api.rmi.previewfees;

import java.util.HashMap;
import java.util.Map;

import com.trilogy.app.crm.api.rmi.GenericParameterParser;
import com.trilogy.app.crm.api.rmi.impl.SubscribersImpl;
import com.trilogy.app.crm.api.rmi.support.RmiApiErrorHandlingSupport;
import com.trilogy.app.crm.api.rmi.support.RmiApiSupport;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.PricePlan;
import com.trilogy.app.crm.bean.core.PricePlanVersion;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.contract.SubscriptionContractTerm;
import com.trilogy.app.crm.contract.SubscriptionContractTermXInfo;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.PricePlanSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v2_1.types.serviceandbundle.PricePlanOptionTypeEnum;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlanOption;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.ContractSubscriptionUpdateCriteria;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.PricePlanSubscriptionUpdateCriteria;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionPricePlan;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionUpdateCriteria;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionUpdateFees;
import com.trilogy.app.crm.bean.*;


public class ContractPreviewUpdateFees  implements PreviewUpdateFees
{

    @Override
    public SubscriptionUpdateFees getUpdateFees(final Context ctx, final SubscriptionUpdateCriteria[] criteria,
            final GenericParameter[] parameters) throws CRMExceptionFault
    {
        for (int i = 0; i < criteria.length; i++)
        {
            if (criteria[i] instanceof ContractSubscriptionUpdateCriteria)
            {
                ContractSubscriptionUpdateCriteria contractCriteria = (ContractSubscriptionUpdateCriteria) criteria[i];
                contractId_ = contractCriteria.getContractId();
            }
            else if (criteria[i] instanceof PricePlanSubscriptionUpdateCriteria)
            {
                PricePlanSubscriptionUpdateCriteria ppCriteria = (PricePlanSubscriptionUpdateCriteria) criteria[i];
                SubscriptionPricePlan subPP = ppCriteria.getOptions();
                initializePricePlanOptions(ctx, subPP);
            }
        }
        SubscriptionContractTerm term = null;
        if (contractId_ != Long.MIN_VALUE)
        {
            term = getContractTerm(ctx, contractId_);
        }
        validateInput(ctx, term);
        if (pricePlanId_ == Long.MIN_VALUE)
        {
            pricePlanId_ = term.getContractPricePlan();
        }
        
        GenericParameterParser parser = new GenericParameterParser(parameters);
        boolean calculateMonthlyValue = parser.getParameter(SubscribersImpl.PRICE_PLAN_MONTHLY_VALUE, Boolean.class, false);
        
        com.redknee.app.crm.bean.core.ServicePackageVersion serviceVersion = getServicePackageVersion(ctx, pricePlanId_);
        Map<Long, BundleFee> bundlesMap = serviceVersion.getBundleFees(ctx);
        Map<ServiceFee2ID, ServiceFee2> serviceMap = serviceVersion.getServiceFees(ctx);
        SubscriptionUpdateFeesAdaptor adapter = new SubscriptionUpdateFeesAdaptor(ctx, bundlesMap, serviceMap, pp_,
                ppv_, term, serviceOptions_, auxiliaryServiceOptions_, auxiliaryBundleOptions_, bundleOptions_,
                criteria, calculateMonthlyValue);
        return adapter.adapt(ctx);
    }


    private SubscriptionContractTerm getContractTerm(final Context ctx, long contractId) throws CRMExceptionFault
    {
        try
        {
            return HomeSupportHelper.get(ctx).findBean(ctx, SubscriptionContractTerm.class,
                    new EQ(SubscriptionContractTermXInfo.ID, contractId));
        }
        catch (Exception ex)
        {
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, ex,
                    "Unable to find subscription contract for contractId " + contractId, this);
        }
        return null;
    }


    private void initializePricePlanOptions(final Context ctx, final SubscriptionPricePlan subOptions)
    {
        com.redknee.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlan pp = subOptions.getPricePlanDetails();
        pricePlanId_ = pp.getIdentifier();
        PricePlanOption[] ppOptions = subOptions.getItems();
        if (ppOptions != null)
        {
            for (int i = 0; i < ppOptions.length; i++)
            {
                if (PricePlanOptionTypeEnum.SERVICE.getValue().getValue() == ppOptions[i].getOptionType().getValue())
                {
                    serviceOptions_.put(Long.valueOf(ppOptions[i].getIdentifier()), ppOptions[i]);
                }
                else if (PricePlanOptionTypeEnum.AUXILIARY_BUNDLE.getValue().getValue() == ppOptions[i].getOptionType()
                        .getValue())
                {
                    auxiliaryBundleOptions_.put(Long.valueOf(ppOptions[i].getIdentifier()), ppOptions[i]);
                }
                else if (PricePlanOptionTypeEnum.AUXILIARY_SERVICE.getValue().getValue() == ppOptions[i]
                        .getOptionType().getValue())
                {
                    auxiliaryServiceOptions_.put(Long.valueOf(ppOptions[i].getIdentifier()), ppOptions[i]);
                }
                else if (PricePlanOptionTypeEnum.BUNDLE.getValue().getValue() == ppOptions[i].getOptionType()
                        .getValue())
                {
                    bundleOptions_.put(Long.valueOf(ppOptions[i].getIdentifier()), ppOptions[i]);
                }
            }
        }
    }


    private void validateInput(final Context ctx, final SubscriptionContractTerm term) throws CRMExceptionFault
    {                
        if (pricePlanId_ == Long.MIN_VALUE && contractId_ == Long.MIN_VALUE)
        {
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, null,
                    " Need to provide either pricePlanId or contract(at minimum) ", this);
        }
        /**
         * Adding below check for checking spid level configuration for "Use contract Priceplan".
         * Below validation should not be called if the flag is false.
         * Because if the flag is false, contract is not tightly coupled with priceplan
         * and any contract can be associated to a subscription irrespective of the priceplan associated.
         */
        if (term !=null){ /**Adding NULL check to avoid NPE if contractId is not provided by DCRM**/
        	final CRMSpid sp = RmiApiSupport.getCrmServiceProvider(ctx, Integer.valueOf(term.getSpid()), this);
        	if (sp.getUseContractPricePlan())
        	{
        		if (pricePlanId_ != Long.MIN_VALUE && term != null && term.getContractPricePlan() != pricePlanId_)
        		{
        			RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, null,
        					" PricePlanId doesn't match contract's pricePlan", this);
        		}
        	}
        }
    }


    public com.redknee.app.crm.bean.core.ServicePackageVersion getServicePackageVersion(final Context ctx, long id)
            throws CRMExceptionFault
    {
        try
        {
            pp_ = PricePlanSupport.getPlan(ctx, id);
            ppv_ = PricePlanSupport.getCurrentVersion(ctx, pp_);
            com.redknee.app.crm.bean.core.ServicePackageVersion servicePackageVersion = ppv_
                    .getServicePackageVersion(ctx);
            return servicePackageVersion;
        }
        catch (Exception ex)
        {
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, null, "Unable to load priceplan " + id
                    + "or price plan version ", this);
        }
        return null;
    }

    private long pricePlanId_ = Long.MIN_VALUE;
    private long contractId_ = Long.MIN_VALUE;
    private PricePlan pp_;
    private PricePlanVersion ppv_;
    private HashMap<Long, PricePlanOption> serviceOptions_ = new HashMap<Long, PricePlanOption>();
    private HashMap<Long, PricePlanOption> bundleOptions_ = new HashMap<Long, PricePlanOption>();
    private HashMap<Long, PricePlanOption> auxiliaryBundleOptions_ = new HashMap<Long, PricePlanOption>();
    private HashMap<Long, PricePlanOption> auxiliaryServiceOptions_ = new HashMap<Long, PricePlanOption>();
}
