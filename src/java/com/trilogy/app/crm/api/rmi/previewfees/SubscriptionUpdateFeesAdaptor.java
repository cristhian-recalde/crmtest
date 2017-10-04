package com.trilogy.app.crm.api.rmi.previewfees;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.trilogy.app.crm.api.rmi.support.APIGenericParameterSupport;
import com.trilogy.app.crm.api.rmi.support.RmiApiErrorHandlingSupport;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.ServicePreferenceEnum;
import com.trilogy.app.crm.bean.core.AuxiliaryService;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bean.core.PricePlan;
import com.trilogy.app.crm.bean.core.PricePlanVersion;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.contract.SubscriptionContractTerm;
import com.trilogy.app.crm.support.AuxiliaryServiceSupport;
import com.trilogy.app.crm.support.BundleSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v2_0.types.ProvisioningStateTypeEnum;
import com.trilogy.util.crmapi.wsdl.v2_1.types.serviceandbundle.PricePlanOptionTypeEnum;
import com.trilogy.util.crmapi.wsdl.v2_2.types.serviceandbundle.PricePlanOptionUpdateResult;
import com.trilogy.util.crmapi.wsdl.v2_2.types.serviceandbundle.PricePlanOptionUpdateTypeEnum;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlanOption;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.ContractSubscriptionUpdateCriteria;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.ContractSubscriptionUpdateCriteriaResults;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.PricePlanSubscriptionUpdateCriteria;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.PricePlanSubscriptionUpdateCriteriaResults;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionUpdateCriteria;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionUpdateCriteriaResults;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionUpdateFeeBreakdown;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionUpdateFeeBreakdownType;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionUpdateFees;
import com.trilogy.app.crm.bean.*;


public class SubscriptionUpdateFeesAdaptor
{

    public SubscriptionUpdateFeesAdaptor(final Context ctx, final Map<Long, BundleFee> bundleFees,
            final Map<ServiceFee2ID, ServiceFee2> serviceFees, final PricePlan pp, final PricePlanVersion ppv,
            final SubscriptionContractTerm term, final HashMap<Long, PricePlanOption> serviceOptions,
            final HashMap<Long, PricePlanOption> auxiliaryServiceOptions,
            final HashMap<Long, PricePlanOption> auxiliaryBundleOptions,
            final HashMap<Long, PricePlanOption> bundleOptions, SubscriptionUpdateCriteria[] criteria, boolean calculateMonthlyValue)
    {
        serviceOptions_ = serviceOptions;
        auxiliaryServiceOptions_ = auxiliaryServiceOptions;
        auxiliaryBundleOptions_ = auxiliaryBundleOptions;
        bundleOptions_ = bundleOptions;
        criterias_ = criteria;
        term_ = term;
        bundleFees_ = bundleFees;
        serviceFees_ = serviceFees;
        ppv_ = ppv;
        calculateMonthlyValue_ = calculateMonthlyValue;
    }


    public SubscriptionUpdateFees adapt(final Context ctx) throws CRMExceptionFault
    {
        SubscriptionUpdateFees updateFees = new SubscriptionUpdateFees();
        addBundleFees(ctx);
        addServiceFees(ctx);
        if (!calculateMonthlyValue_)
        {
            addAuxiliaryServices(ctx);
            addAuxiliaryBundle(ctx);
            addDepositBreakdown();
            addContractBreakdown();
            addNonContractFeeBreakdownTotal();
        }
        SubscriptionUpdateFeeBreakdown[] feeBreakdownArray = new SubscriptionUpdateFeeBreakdown[]
            {};
        updateFees.setFeeBreakdown(feeBreakDown_.toArray(feeBreakdownArray));
        updateFees.setTotalFee(Long.valueOf(totalCharges_));
        updateFees.setCriteriaResults(getCriteriaResults());
        return updateFees;
    }


    private SubscriptionUpdateCriteriaResults[] getCriteriaResults()
    {
        if (criterias_ != null)
        {
            ArrayList<SubscriptionUpdateCriteriaResults> listCriterias = new ArrayList<SubscriptionUpdateCriteriaResults>();
            PricePlanSubscriptionUpdateCriteriaResults ppResult = null;
            for (int i = 0; i < criterias_.length; i++)
            {
                SubscriptionUpdateCriteriaResults cResult = null;
                if (criterias_[i] instanceof ContractSubscriptionUpdateCriteria)
                {
                    cResult = new ContractSubscriptionUpdateCriteriaResults();
                    cResult.setReference(criterias_[i].getReference());
                    listCriterias.add(cResult);
                    if (ppResult == null)
                    {
                        ppResult = new PricePlanSubscriptionUpdateCriteriaResults();
                        PricePlanOptionUpdateResult[] optionResults = new PricePlanOptionUpdateResult[]
                            {};
                        optionResults = pricePlanOptionResults.toArray(optionResults);
                        ((PricePlanSubscriptionUpdateCriteriaResults) ppResult).setOptionResults(optionResults);
                    }
                }
                else if (criterias_[i] instanceof PricePlanSubscriptionUpdateCriteria)
                {
                    if (ppResult == null)
                    {
                        ppResult = new PricePlanSubscriptionUpdateCriteriaResults();
                        ppResult.setReference(criterias_[i].getReference());
                        PricePlanOptionUpdateResult[] optionResults = new PricePlanOptionUpdateResult[]
                            {};
                        optionResults = pricePlanOptionResults.toArray(optionResults);
                        ((PricePlanSubscriptionUpdateCriteriaResults) ppResult).setOptionResults(optionResults);
                    }
                }
                // results[i] = cResult;
            }
            if (ppResult != null)
            {
                listCriterias.add(ppResult);
            }
            SubscriptionUpdateCriteriaResults[] results = new SubscriptionUpdateCriteriaResults[]
                {};
            return listCriterias.toArray(results);
        }
        return null;
    }


    private void addDepositBreakdown()
    {
        if (ppv_.getDeposit() > 0)
        {
            SubscriptionUpdateFeeBreakdown depositBreakDown = new SubscriptionUpdateFeeBreakdown();
            depositBreakDown.setFee(Long.valueOf(ppv_.getDeposit()));
            depositBreakDown.setType(SubscriptionUpdateFeeBreakdownType.value3);
            feeBreakDown_.add(depositBreakDown);
            totalCharges_ += ppv_.getDeposit();
        }
    }


    private void addAuxiliaryBundle(final Context ctx) throws CRMExceptionFault
    {
        try
        {
            for (PricePlanOption option : auxiliaryBundleOptions_.values())
            {
                if (option.getIsSelected())
                {
                    if (!bundleFees_.containsKey(Long.valueOf(option.getIdentifier())))
                    {
                        if (option.getIsSelected())
                        {
                            BundleProfile bundleProfile = BundleSupportHelper.get(ctx).getBundleProfile(ctx,
                                    option.getIdentifier());
                            PricePlanOptionUpdateResult ppUR = new PricePlanOptionUpdateResult();
                            ppUR.setAppliedFee(bundleProfile.getAuxiliaryServiceCharge());
                            ppUR.setIsSelected(true);
                            ppUR.setOptionType(PricePlanOptionTypeEnum.AUXILIARY_BUNDLE.getValue());
                            ppUR.setEndDate(bundleProfile.getEndDate());
                            ppUR.setStartDate(bundleProfile.getStartDate());
                            ppUR.setProvisioningState(ProvisioningStateTypeEnum.NOT_PROVISIONED.getValue());
                            ppUR.setIdentifier(Long.valueOf(bundleProfile.getBundleId()));
                            ppUR.setUpdateType(PricePlanOptionUpdateTypeEnum.ADD.getValue());
                            ppUR.setParameters(getAllGenericParameter(ctx, false, 0, bundleProfile.getName()));
                            pricePlanOptionResults.add(ppUR);
                            notCoveredContract_ += bundleProfile.getAuxiliaryServiceCharge();
                        }
                    }
                }
            }
        }
        catch (Exception ex)
        {
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, ex, " Unable to load bundle profile ", this);
        }
    }


    private void addAuxiliaryServices(final Context ctx) throws CRMExceptionFault
    {
        try
        {
            for (PricePlanOption option : auxiliaryServiceOptions_.values())
            {
                if (option.getIsSelected())
                {
                    AuxiliaryService auxService = AuxiliaryServiceSupport.getAuxiliaryServicById(ctx,
                            option.getIdentifier());
                    PricePlanOptionUpdateResult ppUR = new PricePlanOptionUpdateResult();
                    ppUR.setAppliedFee(auxService.getCharge());
                    ppUR.setIsSelected(true);
                    ppUR.setOptionType(PricePlanOptionTypeEnum.AUXILIARY_SERVICE.getValue());
                    ppUR.setEndDate(auxService.getEndDate());
                    ppUR.setStartDate(auxService.getStartDate());
                    ppUR.setProvisioningState(ProvisioningStateTypeEnum.NOT_PROVISIONED.getValue());
                    ppUR.setUpdateType(PricePlanOptionUpdateTypeEnum.ADD.getValue());
                    ppUR.setIdentifier(Long.valueOf(auxService.getIdentifier()));
                    ppUR.setParameters(getAllGenericParameter(ctx, false, 0, auxService.getName()));
                    pricePlanOptionResults.add(ppUR);
                    notCoveredContract_ += auxService.getCharge();
                }
            }
        }
        catch (Exception ex)
        {
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, ex, " Unable to find auxiliaryservice ", this);
        }
    }


    private void addServiceFees(final Context ctx) throws CRMExceptionFault
    {
        Collection<ServiceFee2> serviceColl = serviceFees_.values();
        try
        {
            for (ServiceFee2 fee : serviceColl)
            {
                if (shouldAddService(fee))
                {
                    PricePlanOptionUpdateResult ppUR = new PricePlanOptionUpdateResult();
                    ppUR.setEndDate(new Date());
                    ppUR.setStartDate(new Date());
                    ppUR.setOptionType(PricePlanOptionTypeEnum.SERVICE.getValue());
                    ppUR.setUpdateType(PricePlanOptionUpdateTypeEnum.ADD.getValue());
                    ppUR.setIdentifier(Long.valueOf(fee.getServiceId()));
                    ppUR.setProvisioningState(ProvisioningStateTypeEnum.NOT_PROVISIONED.getValue());
                    if ( fee.getServicePreference() != ServicePreferenceEnum.OPTIONAL)
                    {
                        ppUR.setIsSelected(true);                        
                    }
                    else
                    {
                        ppUR.setIsSelected(false);                                                
                    }

                    if (calculateMonthlyValue_)
                    {
                        long monthlyFee = calculateMonthlyFee(fee.getFee(), fee.getServicePeriod(), fee.getRecurrenceInterval());
                        totalCharges_ += monthlyFee;
                        ppUR.setAppliedFee(monthlyFee);
                    }
                    else if (term_ != null && fee.isPaidByContract())
                    {
                        ppUR.setAppliedFee(Long.valueOf(0L));
                    }
                    else
                    {
                        ppUR.setAppliedFee(Long.valueOf(fee.getFee()));
                        notCoveredContract_ += fee.getFee();
                    }
                    ppUR.setParameters(getAllGenericParameter(ctx, fee.isPaidByContract(), fee.getFee(), fee
                            .getService(ctx).getName()));
                    pricePlanOptionResults.add(ppUR);
                }
            }
        }
        catch (Exception ex)
        {
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, ex, " Unable to find service ", this);
        }
    }

    private long calculateMonthlyFee(long fee, ServicePeriodEnum servicePeriod, long recurrenceInterval)
    {
        long result = fee;
        switch (servicePeriod.getIndex())
        {
            case ServicePeriodEnum.ANNUAL_INDEX:
                result = result/12;
                break;
            case ServicePeriodEnum.DAILY_INDEX:
                result = result*30;
                break;
            case ServicePeriodEnum.MULTIMONTHLY_INDEX:
                result = result/recurrenceInterval;
                break;
            case ServicePeriodEnum.MULTIDAY_INDEX:
                result = result*30/recurrenceInterval;
                break;
            case ServicePeriodEnum.WEEKLY_INDEX:
                result = result * 4;
                break;
            case ServicePeriodEnum.ONE_TIME_INDEX:
                result = 0;
                break;
            case ServicePeriodEnum.MONTHLY_INDEX:
                break;
        }
        return result;
    }

    private void addBundleFees(final Context ctx) throws CRMExceptionFault
    {
        if (bundleFees_ != null)
        {
            for (BundleFee fee : bundleFees_.values())
            {
                if (shouldAddBundle(fee))
                {
                    PricePlanOptionUpdateResult ppUR = new PricePlanOptionUpdateResult();
                    ppUR.setEndDate(fee.getEndDate());
                    ppUR.setStartDate(fee.getStartDate());
                    ppUR.setOptionType(PricePlanOptionTypeEnum.BUNDLE.getValue());
                    ppUR.setUpdateType(PricePlanOptionUpdateTypeEnum.ADD.getValue());
                    ppUR.setIdentifier(Long.valueOf(fee.getId()));
                    ppUR.setProvisioningState(ProvisioningStateTypeEnum.NOT_PROVISIONED.getValue());
                    if ( fee.getServicePreference() != ServicePreferenceEnum.OPTIONAL)
                    {
                        ppUR.setIsSelected(true);                        
                    }
                    else
                    {
                        ppUR.setIsSelected(false);                                                
                    }

                    if (calculateMonthlyValue_)
                    {
                        long monthlyFee = calculateMonthlyFee(fee.getFee(), fee.getServicePeriod(), 1);
                        totalCharges_ += monthlyFee;
                        ppUR.setAppliedFee(monthlyFee);
                    }
                    else if (term_ != null && fee.isPaidByContract())
                    {
                        ppUR.setAppliedFee(0L);
                    }
                    else
                    {
                        ppUR.setAppliedFee(fee.getFee());
                        notCoveredContract_ += fee.getFee();
                    }
                    try
                    {
                        BundleProfile bundleProfile = BundleSupportHelper.get(ctx).getBundleProfile(ctx, fee.getId());
                        ppUR.setParameters(getAllGenericParameter(ctx, fee.isPaidByContract(), fee.getFee(),
                                bundleProfile.getName()));
                    }
                    catch (Exception ex)
                    {
                        RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, ex,
                                " Unable to find bundle  " + fee.getId(), this);
                    }
                    pricePlanOptionResults.add(ppUR);
                }
            }
        }
    }


    private boolean shouldAddService(ServiceFee2 fee)
    {
        PricePlanOption option = serviceOptions_.get(Long.valueOf(fee.getServiceId()));
        boolean result = true;
        if (option != null)
        {
            if (calculateMonthlyValue_)
            {
                result = shouldAddToMonthlyCalculation(fee.getServicePreference(), fee.getServicePeriod());
            }
            else if (!option.getIsSelected() && (fee.getServicePreference() != ServicePreferenceEnum.MANDATORY))
            {
                result = false;
            }
        }
        return result;
    }
    
    private boolean shouldAddToMonthlyCalculation(ServicePreferenceEnum servicePreference, ServicePeriodEnum servicePeriod)
    {
        return (ServicePreferenceEnum.MANDATORY.equals(servicePreference) || ServicePreferenceEnum.DEFAULT.equals(servicePreference)) &&
        !ServicePeriodEnum.ONE_TIME.equals(servicePeriod);
    }


    private boolean shouldAddBundle(BundleFee fee)
    {
        PricePlanOption option = bundleOptions_.get(Long.valueOf(fee.getId()));
        boolean result = true;
        if (option != null)
        {
            if (calculateMonthlyValue_)
            {
                result = shouldAddToMonthlyCalculation(fee.getServicePreference(), fee.getServicePeriod());
            }
            else if (!option.getIsSelected() && (!fee.isMandatory()))
            {
                result = false;
            }
        }
        else
        {
            
            PricePlanOption auxBundleOption = auxiliaryBundleOptions_.get(Long.valueOf(fee.getId()));
            if (auxBundleOption != null)
            {
                if (calculateMonthlyValue_)
                {
                    result = shouldAddToMonthlyCalculation(fee.getServicePreference(), fee.getServicePeriod());
                }
                else if (!auxBundleOption.getIsSelected() && (!fee.isMandatory()))
                {
                    result = false;
                }
            }
        }
        return result;
    }


    private void addNonContractFeeBreakdownTotal()
    {
        SubscriptionUpdateFeeBreakdown nonContractBreakdown = new SubscriptionUpdateFeeBreakdown();
        nonContractBreakdown.setFee(Long.valueOf(notCoveredContract_));
        nonContractBreakdown.setType(SubscriptionUpdateFeeBreakdownType.value2);
        feeBreakDown_.add(nonContractBreakdown);
        totalCharges_ += notCoveredContract_;
    }


    private void addContractBreakdown()
    {
        if (term_ != null)
        {
            SubscriptionUpdateFeeBreakdown contractBreakdown = new SubscriptionUpdateFeeBreakdown();
            contractBreakdown.setFee(Long.valueOf(term_.getPrepaymentAmount()));
            contractBreakdown.setType(SubscriptionUpdateFeeBreakdownType.value1);
            feeBreakDown_.add(contractBreakdown);
            totalCharges_ += term_.getPrepaymentAmount();
        }
    }


    private GenericParameter[] getAllGenericParameter(final Context ctx, final boolean isCoveredByContract,
            final long fee, final String name)
    {
        GenericParameter[] parameters = new GenericParameter[]
            {};
        Collection<GenericParameter> parametersList = new ArrayList<GenericParameter>();
        if (isCoveredByContract)
        {
            parametersList.add(APIGenericParameterSupport.getCoveredByContract(ctx, fee));
        }
        parametersList.add(APIGenericParameterSupport.getPricePlanUpdateResultName(ctx, name));
        return parametersList.toArray(parameters);
    }

    private HashMap<Long, PricePlanOption> serviceOptions_ = new HashMap<Long, PricePlanOption>();
    private HashMap<Long, PricePlanOption> bundleOptions_ = new HashMap<Long, PricePlanOption>();
    private HashMap<Long, PricePlanOption> auxiliaryBundleOptions_ = new HashMap<Long, PricePlanOption>();
    private HashMap<Long, PricePlanOption> auxiliaryServiceOptions_ = new HashMap<Long, PricePlanOption>();
    private Map<ServiceFee2ID, ServiceFee2> serviceFees_;
    private Map<Long, BundleFee> bundleFees_;
    private SubscriptionContractTerm term_;
    private PricePlanVersion ppv_;
    private SubscriptionUpdateCriteria[] criterias_;
    private ArrayList<PricePlanOptionUpdateResult> pricePlanOptionResults = new ArrayList<PricePlanOptionUpdateResult>();
    private ArrayList<SubscriptionUpdateFeeBreakdown> feeBreakDown_ = new ArrayList<SubscriptionUpdateFeeBreakdown>();
    private long totalCharges_ = 0;
    private long notCoveredContract_ = 0;
    private boolean calculateMonthlyValue_ = false;
}
