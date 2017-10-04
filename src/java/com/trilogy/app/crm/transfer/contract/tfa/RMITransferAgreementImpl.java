/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.transfer.contract.tfa;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.trilogy.app.crm.transfer.AdjustmentRange;
import com.trilogy.app.crm.transfer.DifferenceTypeEnum;
import com.trilogy.app.crm.transfer.FlatValueAdjustmentRange;
import com.trilogy.app.crm.transfer.PercentValueAdjustmentRange;
import com.trilogy.app.crm.transfer.TransferAgreement;
import com.trilogy.app.crm.transfer.ValueTypeEnum;
import com.trilogy.app.crm.transfer.contract.TransferAgreementFacade;
import com.trilogy.app.crm.transfer.contract.TransferAgreementNotFoundException;
import com.trilogy.app.crm.transfer.contract.TransferContractException;
import com.trilogy.app.transferfund.rmi.api.agreement.AgreementService;
import com.trilogy.app.transferfund.rmi.data.Adjustment;
import com.trilogy.app.transferfund.rmi.data.AdjustmentRate;
import com.trilogy.app.transferfund.rmi.data.Agreement;
import com.trilogy.app.transferfund.rmi.data.AuthCredentials;
import com.trilogy.app.transferfund.rmi.data.Distribution;
import com.trilogy.app.transferfund.rmi.exception.ContractProvisioningException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * RMI implementation of the RMI client for transfer agreement on TFA.
 * @author arturo.medina@redknee.com
 *
 */
public class RMITransferAgreementImpl implements TransferAgreementFacade
{

    /**
     * {@inheritDoc}
     */
    public TransferAgreement createTransferAgreement(Context ctx,
            TransferAgreement agreement) throws TransferContractException
    {
        final AuthCredentials credentials = TransferContractSupport.getCredentials(ctx);
        Agreement tfaAgreement = adaptTransferAgreement(ctx, agreement);
        final AgreementService service = getService(ctx);
        TransferAgreement crmAgreement = agreement;
        try
        {
            tfaAgreement = service.createAgreement(credentials, tfaAgreement, TransferContractSupport.getErReference(ctx));
            crmAgreement = adaptAgreement(ctx, tfaAgreement);
        }
        catch (ContractProvisioningException e)
        {
            LogSupport.major(ctx, this, "ContractProvisioningException when trying to create the agreement "
                    + crmAgreement.getIdentifier()
                    + " error code : "
                    + e.getResponseCode());
            
            throw new TransferContractException(
                    TransferContractSupport.mapErrorCodeWithMessage(e.getResponseCode()),e);
        }
        return crmAgreement;
    }


    /**
     * {@inheritDoc}
     */
    public void deleteAgreement(Context ctx, long agreementId)
            throws TransferContractException
    {
        final AuthCredentials credentials = TransferContractSupport.getCredentials(ctx);
        final AgreementService service = getService(ctx);
        try
        {
            service.deleteAgreement(credentials, agreementId, TransferContractSupport.getErReference(ctx));
        }
        catch (ContractProvisioningException e)
        {
            LogSupport.major(ctx, this, "ContractProvisioningException when trying to delete the agreementId "
                    + agreementId
                    + " error code : "
                    + e.getResponseCode());
            
            if (e.getResponseCode() != TransferContractSupport.ERR_AGREEMENTID_NOT_EXIST)
            {
                throw new TransferContractException(
                        TransferContractSupport.mapErrorCodeWithMessage(e.getResponseCode()),e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void deleteAgreementsByOwner(Context ctx, String owner)
            throws TransferContractException
    {
        final AuthCredentials credentials = TransferContractSupport.getCredentials(ctx);
        final AgreementService service = getService(ctx);
        try
        {
            service.deleteAgreementsByOwner(credentials, owner, TransferContractSupport.getErReference(ctx));
        }
        catch (ContractProvisioningException e)
        {
            LogSupport.major(ctx, this, "ContractProvisioningException when trying to delete the owner "
                    + owner
                    + " error code : "
                    + e.getResponseCode());
            
            if (e.getResponseCode() != TransferContractSupport.ERR_AGREEMENTID_NOT_EXIST)
            {
                throw new TransferContractException(
                        TransferContractSupport.mapErrorCodeWithMessage(e.getResponseCode()),e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
   public TransferAgreement retrieveAgreement(Context ctx, long agreementId)
            throws TransferContractException, TransferAgreementNotFoundException
    {
       final AuthCredentials credentials = TransferContractSupport.getCredentials(ctx);
       final AgreementService service = getService(ctx);
       TransferAgreement crmAgreement = null;
       Agreement agreement;
       try
       {
           agreement = service.retrieveAgreement(credentials, agreementId, TransferContractSupport.getErReference(ctx));
           crmAgreement = adaptAgreement(ctx, agreement);
       }
       catch (ContractProvisioningException e)
       {
           LogSupport.major(ctx, this, "ContractProvisioningException when trying to get the agreement "
                   + agreementId
                   + " error code : "
                   + e.getResponseCode());
           if (e.getResponseCode() == TransferContractSupport.ERR_AGREEMENTID_NOT_EXIST)
           {
               throw new TransferAgreementNotFoundException(
                       TransferContractSupport.mapErrorCodeWithMessage(e.getResponseCode()),e);
           }
           else
           {
               throw new TransferContractException(
                       TransferContractSupport.mapErrorCodeWithMessage(e.getResponseCode()),e);
           }
       }
       return crmAgreement;
    }

   /**
    * {@inheritDoc}
    */
    public List<TransferAgreement> retrieveOwnerAgreements(Context ctx,
            String agreementOwner) throws TransferContractException
    {
        final AuthCredentials credentials = TransferContractSupport.getCredentials(ctx);
        final AgreementService service = getService(ctx);
        final List<TransferAgreement> agreements = new ArrayList<TransferAgreement>();

        try
        {
            final Agreement[] tfaContracts = service.retrieveOwnerAgreements(credentials,
                    agreementOwner, TransferContractSupport.getErReference(ctx));
            for (int i = 0; i < tfaContracts.length; i++)
            {
                if (tfaContracts[i] != null)
                {
                    agreements.add(adaptAgreement(ctx, tfaContracts[i]));
                }
            }
        }
        catch (ContractProvisioningException e)
        {
            LogSupport.major(ctx, this, "ContractProvisioningException when trying to get the contract owner"
                    + agreementOwner
                    + " error code : "
                    + e.getResponseCode());
            
            if (e.getResponseCode() != TransferContractSupport.ERR_OWNER_NOT_EXISTS_AGREEMENT
                        && e.getResponseCode() != TransferContractSupport.ERR_AGREEMENTID_NOT_EXIST)
            {
                throw new TransferContractException(
                        TransferContractSupport.mapErrorCodeWithMessage(e.getResponseCode()),e);
            }

        }
        return agreements;
    }

    /**
     * {@inheritDoc}
     */
    public TransferAgreement updateTransferContract(Context ctx,
            TransferAgreement agreement) throws TransferContractException
    {
       final AuthCredentials credentials = TransferContractSupport.getCredentials(ctx);
       Agreement tfaAgreement = adaptTransferAgreement(ctx, agreement);
       final AgreementService service = getService(ctx);
       TransferAgreement crmAgreement = agreement;
       try
       {
           service.updateAgreement(credentials, tfaAgreement, TransferContractSupport.getErReference(ctx));
       }
       catch (ContractProvisioningException e)
       {
           LogSupport.major(ctx, this, "ContractProvisioningException when trying to update the agreement "
                   + crmAgreement.getIdentifier()
                   + " error code : "
                   + e.getResponseCode());
           
           throw new TransferContractException(
                   TransferContractSupport.mapErrorCodeWithMessage(e.getResponseCode()),e);
       }
       return crmAgreement;
    }

  
    /**
    * Creates a Transfer agreement and adapts all the fields from the TFA agreement
    * @param ctx
    * @param tfaAgreement
    * @return
    */
    private TransferAgreement adaptAgreement(Context ctx, Agreement tfaAgreement)
    {
       TransferAgreement agreement = new TransferAgreement();
       agreement.setOwnerID(tfaAgreement.getAgreementOwner());
       agreement.setIdentifier(tfaAgreement.getAgreementId());
       agreement.setDescription(tfaAgreement.getDescription());
       agreement.setSpid(tfaAgreement.getSpid());
       agreement.setMinimumTransferAmount(tfaAgreement.getMinTransferAmount());
       agreement.setMaximumTransferAmount(tfaAgreement.getMaxTransferAmount());
       agreement.setMinimumAdjustment(tfaAgreement.getAdjustment().getMinAdjustmentAmount());
       agreement.setMaximumAdjustment(tfaAgreement.getAdjustment().getMaxAdjustmentAmount());

       setTfaAdjstmentRateValues(agreement, tfaAgreement);
       setTfaAgreementDistributionValues(agreement, tfaAgreement);
       
       return agreement;
    }

    /**
     * Sets the Adjustment rate values to the CRM agreement 
     * @param agreement
     * @param tfaAgreement
     */
    private void setTfaAdjstmentRateValues(TransferAgreement agreement,
            Agreement tfaAgreement)
    {
        final Adjustment adjustment = tfaAgreement.getAdjustment();
        agreement.setAdjustmentValueType(ValueTypeEnum.get(adjustment.getAdjustmentRatingType()));
        agreement.setAdjustmentDifferenceType(DifferenceTypeEnum.get(adjustment.getAdjustmentAmountType()));
        
        final Map ranges;        
        if (agreement.getAdjustmentValueType() == ValueTypeEnum.FLAT)
        {
            ranges = agreement.getFlatValueRanges();
        }
        else
        {
            ranges = agreement.getPercentValueRanges();
        }
        
        for (final AdjustmentRate rate : adjustment.getAdjustmentRate())    
        {
            AdjustmentRange range = null;
            if (agreement.getAdjustmentValueType() == ValueTypeEnum.FLAT)
            {
                range = new FlatValueAdjustmentRange();
                ((FlatValueAdjustmentRange)range).setFlatValue(Long.valueOf(rate.getAdjustmentValue()));
            }
            else
            {
                range = new PercentValueAdjustmentRange();
                ((PercentValueAdjustmentRange)range).setPercentValue(rate.getAdjustmentValue());
            }
            range.setLowerBound(rate.getTransferAmount());
            ranges.put(rate.getTransferAmount(), range);
        }
    }


    /**
     * Sets the Distribution values to the CRM agreement 
     * @param agreement
     * @param tfaAgreement
     */
    private void setTfaAgreementDistributionValues(TransferAgreement agreement,
            Agreement tfaAgreement)
    {
        Distribution dist = tfaAgreement.getDistribution();

        
        agreement.setDistributionValueType(ValueTypeEnum.get(dist.getDistributionRatingType()));
        agreement.setDistributionDifferenceType(DifferenceTypeEnum.get(dist.getDistributionAmountType()));
        
        if (dist.getDistributionRatingType() == ValueTypeEnum.PERCENT_INDEX)
        {
            agreement.setPercentDistribution(dist.getDistributionValue());
        }
        else
        {
            if (dist.getDistributionRatingType() == ValueTypeEnum.FLAT_INDEX)
            {
                agreement.setFlatDistribution(Long.valueOf(dist.getDistributionValue()));
            }
        }
        
    }


    /**
     * Creates a TFA agreement and adapts all the fields from the CRM agreement
     * @param ctx
     * @param agreement
     * @return
     */
    private Agreement adaptTransferAgreement(Context ctx,
            TransferAgreement agreement)
    {
        Agreement tfaAgreement = new Agreement();

        tfaAgreement.setAgreementId(agreement.getIdentifier());
        tfaAgreement.setDescription(agreement.getDescription());
        tfaAgreement.setSpid(agreement.getSpid());
        tfaAgreement.setAgreementOwner(agreement.getOwnerID());
        tfaAgreement.setMinTransferAmount(agreement.getMinimumTransferAmount());
        tfaAgreement.setMaxTransferAmount(agreement.getMaximumTransferAmount());

        tfaAgreement.setAdjustment(createAdjusmtent(agreement));

        tfaAgreement.setDistribution(createDistribution(agreement));

        return tfaAgreement;
    }

    /**
     * Creates a TFA adjustment based on the CRM Transfer Agreement
     * @param agreement
     * @return
     */
    private Adjustment createAdjusmtent(TransferAgreement agreement)
    {
        Adjustment adj = new Adjustment();
        adj.setMaxAdjustmentAmount(agreement.getMaximumAdjustment());
        adj.setMinAdjustmentAmount(agreement.getMinimumAdjustment());
        adj.setAdjustmentAmountType(agreement.getAdjustmentDifferenceType().getIndex());
        adj.setAdjustmentRatingType(agreement.getAdjustmentValueType().getIndex());
        adj.setAdjustmentRate(adaptAdjusmtentRates(agreement));

        return adj;
    }


    /**
     * Creates a TFA distribution based on the CRM Transfer Agreement
     * @param agreement
     * @return
     */
    private Distribution createDistribution(TransferAgreement agreement)
    {
        Distribution dist = new Distribution();

        dist.setDistributionAmountType(agreement.getDistributionDifferenceType().getIndex());
        dist.setDistributionRatingType(agreement.getDistributionValueType().getIndex());

        String distValue = null;

        if (agreement.getDistributionValueType() == ValueTypeEnum.FLAT)
        {
            distValue = String.valueOf(agreement.getFlatDistribution());
        }
        else
        {
            if (agreement.getDistributionValueType() == ValueTypeEnum.PERCENT)
            {
                distValue = agreement.getPercentDistribution();
            }
        }
        dist.setDistributionValue(distValue);
        return dist;
    }


    /**
     * Creates a TFA Adjustment rates based on the CRM Transfer Agreement
     * @param agreement
     * @return
     */
    private AdjustmentRate[] adaptAdjusmtentRates(TransferAgreement agreement)
    {
        final Map ranges;

        if (agreement.getAdjustmentValueType() == ValueTypeEnum.FLAT)
        {
            ranges = agreement.getFlatValueRanges();
        }
        else
        {
            ranges = agreement.getPercentValueRanges();
        }

        final List<AdjustmentRate> adjRates = new ArrayList<AdjustmentRate>();

        // Note: the Map is "known" to be a SortedMap, and so the values are in
        // proper order.
        for (final Object rate : ranges.values())
        {
            adjRates.add(adaptAdjustmentRate(rate));
        }

        return adjRates.toArray(new AdjustmentRate[0]);
    }

    /**
     * Creates a TFA Adjustment rate based on the CRM Transfer Agreement
     * @param agreement
     * @return
     */
    private AdjustmentRate adaptAdjustmentRate(Object value)
    {
        AdjustmentRate rate = new AdjustmentRate();

        if (value instanceof FlatValueAdjustmentRange)
        {
            FlatValueAdjustmentRange flat = (FlatValueAdjustmentRange)value;
            rate.setTransferAmount(flat.getLowerBound());
            rate.setAdjustmentValue(String.valueOf(flat.getFlatValue()));
        }
        else if (value instanceof PercentValueAdjustmentRange)
        {
            PercentValueAdjustmentRange range = (PercentValueAdjustmentRange) value;
            rate.setTransferAmount(range.getLowerBound());
            rate.setAdjustmentValue(range.getPercentValue());
        }

        return rate;
    }


    /**
     * Gets the Transfer Agreement RMI service
     * @param ctx
     * @return
     */
    private AgreementService getService(Context ctx)
    {
        final AgreementService service =
            (AgreementService)ctx.get(AgreementService.class);
        return service;
    }

}
