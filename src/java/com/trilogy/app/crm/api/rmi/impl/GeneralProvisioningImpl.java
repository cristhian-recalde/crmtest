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
package com.trilogy.app.crm.api.rmi.impl;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.trilogy.app.crm.ContextHelper;
import com.trilogy.app.crm.api.ApiSupport;
import com.trilogy.app.crm.api.Constants;
import com.trilogy.app.crm.api.queryexecutor.QueryExecutorFactory;
import com.trilogy.app.crm.api.rmi.BankToApiAdapter;
import com.trilogy.app.crm.api.rmi.ChargingTemplateToApiAdapter;
import com.trilogy.app.crm.api.rmi.CollectionAgencyReferenceToApiAdapter;
import com.trilogy.app.crm.api.rmi.ContractToApiAdapter;
import com.trilogy.app.crm.api.rmi.CreditCategoryToApiAdapter;
import com.trilogy.app.crm.api.rmi.DiscountClassToApiAdapter;
import com.trilogy.app.crm.api.rmi.DiscountClassToApiReferenceAdapter;
import com.trilogy.app.crm.api.rmi.IdentificationGroupToApiAdapter;
import com.trilogy.app.crm.api.rmi.IdentificationGroupToApiReferenceAdapter;
import com.trilogy.app.crm.api.rmi.IdentificationToApiAdapter;
import com.trilogy.app.crm.api.rmi.IdentificationToApiReferenceAdapter;
import com.trilogy.app.crm.api.rmi.OccupationToApiAdapter;
import com.trilogy.app.crm.api.rmi.ScreeningTemplateToApiAdapter;
import com.trilogy.app.crm.api.rmi.SupplementaryDataToApiAdapter;
import com.trilogy.app.crm.api.rmi.support.RmiApiErrorHandlingSupport;
import com.trilogy.app.crm.api.rmi.support.RmiApiSupport;
import com.trilogy.app.crm.api.rmi.support.SubscribersApiSupport;
import com.trilogy.app.crm.bean.BlackList;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidXInfo;
import com.trilogy.app.crm.bean.ChargingTemplate;
import com.trilogy.app.crm.bean.ChargingTemplateXInfo;
import com.trilogy.app.crm.bean.CreditCardType;
import com.trilogy.app.crm.bean.CreditCategoryXInfo;
import com.trilogy.app.crm.bean.DiscountClassXInfo;
import com.trilogy.app.crm.bean.GLCodeMapping;
import com.trilogy.app.crm.bean.GLCodeMappingXInfo;
import com.trilogy.app.crm.bean.IdentificationGroupXInfo;
import com.trilogy.app.crm.bean.IdentificationXInfo;
import com.trilogy.app.crm.bean.Occupation;
import com.trilogy.app.crm.bean.OnDemandSequence;
import com.trilogy.app.crm.bean.ScreeningTemplate;
import com.trilogy.app.crm.bean.ScreeningTemplateXInfo;
import com.trilogy.app.crm.bean.SpidIdentificationGroups;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SupplementaryDataEntityEnum;
import com.trilogy.app.crm.bean.TaxAuthority;
import com.trilogy.app.crm.bean.TaxAuthorityXInfo;
import com.trilogy.app.crm.bean.bank.Bank;
import com.trilogy.app.crm.bean.bank.BankXInfo;
import com.trilogy.app.crm.bean.core.CreditCategory;
import com.trilogy.app.crm.bean.payment.Contract;
import com.trilogy.app.crm.bean.payment.ContractXInfo;
import com.trilogy.app.crm.blacklist.BlackListSupport;
import com.trilogy.app.crm.sequenceId.OnDemandSequenceManager;
import com.trilogy.app.crm.support.AuthSupport;
import com.trilogy.app.crm.support.CollectionSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SupplementaryDataSupportHelper;
import com.trilogy.framework.xhome.auth.SimplePermission;
import com.trilogy.framework.xhome.beans.ComparableComparator;
import com.trilogy.framework.xhome.beans.ReverseComparator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.elang.True;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbstractValueVisitor;
import com.trilogy.framework.xhome.visitor.FindVisitor;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.visitor.Visitors;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v2_0.types.PaidType;
import com.trilogy.util.crmapi.wsdl.v2_0.types.SystemType;
import com.trilogy.util.crmapi.wsdl.v2_1.exception.ExceptionCode;
import com.trilogy.util.crmapi.wsdl.v2_2.types.generalprovisioning.BlockingTemplateReference;
import com.trilogy.util.crmapi.wsdl.v2_2.types.generalprovisioning.BlockingTemplateStateType;
import com.trilogy.util.crmapi.wsdl.v2_2.types.generalprovisioning.ChargingTemplateReference;
import com.trilogy.util.crmapi.wsdl.v2_2.types.generalprovisioning.ChargingTemplateStateType;
import com.trilogy.util.crmapi.wsdl.v2_2.types.generalprovisioning.ChargingTemplateStateTypeEnum;
import com.trilogy.util.crmapi.wsdl.v2_2.types.generalprovisioning.ContractReference;
import com.trilogy.util.crmapi.wsdl.v2_2.types.generalprovisioning.CreditCategoryReference;
import com.trilogy.util.crmapi.wsdl.v2_2.types.generalprovisioning.DiscountClassReference;
import com.trilogy.util.crmapi.wsdl.v2_2.types.generalprovisioning.IdentificationGroup;
import com.trilogy.util.crmapi.wsdl.v2_2.types.generalprovisioning.IdentificationGroupReference;
import com.trilogy.util.crmapi.wsdl.v2_2.types.generalprovisioning.OccupationReference;
import com.trilogy.util.crmapi.wsdl.v2_2.types.generalprovisioning.ScreeningTemplateReference;
import com.trilogy.util.crmapi.wsdl.v2_2.types.generalprovisioning.ScreeningTemplateStateType;
import com.trilogy.util.crmapi.wsdl.v2_2.types.generalprovisioning.ScreeningTemplateStateTypeEnum;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.api.GeneralProvisioningServiceSkeletonInterface;
import com.trilogy.util.crmapi.wsdl.v3_0.api.types.generalprovisioning.ListChargingTemplatesCriteria;
import com.trilogy.util.crmapi.wsdl.v3_0.api.types.generalprovisioning.ListChargingTemplatesCriteriaSequence_type0;
import com.trilogy.util.crmapi.wsdl.v3_0.api.types.generalprovisioning.ListChargingTemplatesCriteriaSequence_type1;
import com.trilogy.util.crmapi.wsdl.v3_0.api.types.generalprovisioning.ListScreeningTemplatesCriteria;
import com.trilogy.util.crmapi.wsdl.v3_0.api.types.generalprovisioning.ListScreeningTemplatesCriteriaSequence_type0;
import com.trilogy.util.crmapi.wsdl.v3_0.api.types.generalprovisioning.ListScreeningTemplatesCriteriaSequence_type1;
import com.trilogy.util.crmapi.wsdl.v3_0.types.CRMRequestHeader;
import com.trilogy.util.crmapi.wsdl.v3_0.types.ExecuteResult;
import com.trilogy.util.crmapi.wsdl.v3_0.types.generalprovisioning.AccountSupplementaryDataTarget;
import com.trilogy.util.crmapi.wsdl.v3_0.types.generalprovisioning.BankReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.generalprovisioning.BlacklistStatus;
import com.trilogy.util.crmapi.wsdl.v3_0.types.generalprovisioning.BlacklistType;
import com.trilogy.util.crmapi.wsdl.v3_0.types.generalprovisioning.CollectionAgencyReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.generalprovisioning.CreditCardTypeReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.generalprovisioning.DiscountClass;
import com.trilogy.util.crmapi.wsdl.v3_0.types.generalprovisioning.DiscountClassModificationResult;
import com.trilogy.util.crmapi.wsdl.v3_0.types.generalprovisioning.Identification;
import com.trilogy.util.crmapi.wsdl.v3_0.types.generalprovisioning.IdentificationEntry;
import com.trilogy.util.crmapi.wsdl.v3_0.types.generalprovisioning.IdentificationReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.generalprovisioning.ReceiptBlock;
import com.trilogy.util.crmapi.wsdl.v3_0.types.generalprovisioning.SequenceBlock;
import com.trilogy.util.crmapi.wsdl.v3_0.types.generalprovisioning.ServiceProviderReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.generalprovisioning.SubscriptionSupplementaryDataTarget;
import com.trilogy.util.crmapi.wsdl.v3_0.types.generalprovisioning.SupplementaryData;
import com.trilogy.util.crmapi.wsdl.v3_0.types.generalprovisioning.SupplementaryDataResponse;
import com.trilogy.util.crmapi.wsdl.v3_0.types.generalprovisioning.SupplementaryDataTarget;



/**
 * Implementation of Accounts API interface.
 *
 * @author victor.stratan@redknee.com
 */
public class GeneralProvisioningImpl implements GeneralProvisioningServiceSkeletonInterface, ContextAware
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;


    /**
     * Create a new instance of <code>AccountsImpl</code>.
     *
     * @param ctx
     *            The operating context.
     * @throws RemoteException
     *             Thrown by RMI.
     */
    public GeneralProvisioningImpl(final Context ctx)
    {
        this.context_ = ctx;
        this.creditCategoryToApiAdapter_ = new CreditCategoryToApiAdapter();
        this.occupationToApiAdapter_ = new OccupationToApiAdapter();
        this.bankToApiAdapter_ = new BankToApiAdapter();
        this.contractToApiAdapter_ = new ContractToApiAdapter();
        this.chargingTemplateToApiAdapter_ = new ChargingTemplateToApiAdapter();
        this.screeningTemplateToApiAdapter_ = new ScreeningTemplateToApiAdapter();
        this.idGroupToApiReferenceAdapter_ = new IdentificationGroupToApiReferenceAdapter();
        this.idToApiReferenceAdapter_ = new IdentificationToApiReferenceAdapter();
        this.idGroupToApiAdapter_ = new IdentificationGroupToApiAdapter();
        this.idToApiAdapter_ = new IdentificationToApiAdapter();
        this.discountClassToApiReferenceAdapter_ = new DiscountClassToApiReferenceAdapter();
        this.discountClassToApiAdapter_ = new DiscountClassToApiAdapter();
        this.collectionAgencyToApiReferenceAdapter_ = new CollectionAgencyReferenceToApiAdapter();
        this.supplementaryDataToApiReferenceAdapter_ = new SupplementaryDataToApiAdapter();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public CreditCategoryReference[] listCreditCategory(final CRMRequestHeader header, final int spid,
            Boolean isAscending, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "listCreditCategory",
                Constants.PERMISSION_GENERAL_PROVISIONING_READ_LISTCREDITCATEGORY,
                Constants.PERMISSION_ACCOUNTS_READ_LISTCREDITCATEGORY);
        RmiApiSupport.getCrmServiceProvider(ctx, spid, this);
        CreditCategoryReference[] creditCategoryReferences = new CreditCategoryReference[]
            {};
        try
        {
            final Object condition = new EQ(CreditCategoryXInfo.SPID, spid);
            Collection<CreditCategory> collection = HomeSupportHelper.get(ctx).getBeans(ctx, CreditCategory.class,
                    condition, RmiApiSupport.isSortAscending(isAscending));
            creditCategoryReferences = CollectionSupportHelper.get(ctx).adaptCollection(ctx, collection,
                    this.creditCategoryToApiAdapter_, creditCategoryReferences);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Credit Categories";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return creditCategoryReferences;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public com.redknee.util.crmapi.wsdl.v2_2.types.generalprovisioning.CreditCategory getCreditCategory(final CRMRequestHeader header,
        final long categoryID, GenericParameter[] parameters) throws CRMExceptionFault
    {

            final Context ctx = getContext().createSubContext();
            // handle login. Failed logins throw exceptions
            RmiApiSupport.authenticateUser(ctx, header, "getCreditCategory",
                    Constants.PERMISSION_GENERAL_PROVISIONING_READ_GETCREDITCATEGORY,
                    Constants.PERMISSION_ACCOUNTS_READ_GETCREDITCATEGORY);

            CreditCategory creditCategory = null;
            try
            {
                creditCategory = HomeSupportHelper.get(ctx).findBean(ctx, CreditCategory.class, Integer.valueOf((int) categoryID));
            }
            catch (final Exception e)
            {
                final String msg = "Unable to retrieve Credit Category " + categoryID;
                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
            }

            if (creditCategory == null)
            {
                final String identifier = "Credit Category " + categoryID;
                RmiApiErrorHandlingSupport.identificationException(ctx, identifier, this);
            }

            final com.redknee.util.crmapi.wsdl.v2_2.types.generalprovisioning.CreditCategory result;
            result = CreditCategoryToApiAdapter.adaptCreditCategoryToApi(creditCategory);

            return result;

    }


    /**
     * {@inheritDoc}
     */
    @Override
    public OccupationReference[] listOccupations(final CRMRequestHeader header, Boolean isAscending,
            GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "listOccupations",
                Constants.PERMISSION_GENERAL_PROVISIONING_READ_LISTOCCUPATIONS,
                Constants.PERMISSION_ACCOUNTS_READ_LISTOCCUPATIONS);
        OccupationReference[] occupationReferences = new OccupationReference[]
            {};
        try
        {
            final Object condition = True.instance();
            Collection<Occupation> collection = HomeSupportHelper.get(ctx).getBeans(ctx, Occupation.class, condition,
                    RmiApiSupport.isSortAscending(isAscending));
            occupationReferences = CollectionSupportHelper.get(ctx).adaptCollection(ctx, collection,
                    this.occupationToApiAdapter_, occupationReferences);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Occupations";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return occupationReferences;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public BankReference[] listBanks(final CRMRequestHeader header, int spid, Boolean isAscending,
            GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "listBanks",
                Constants.PERMISSION_GENERAL_PROVISIONING_READ_LISTBANKS, Constants.PERMISSION_ACCOUNTS_READ_LISTBANKS);
        BankReference[] bankReferences = new BankReference[]
            {};
        try
        {
            final Object condition = new EQ(BankXInfo.SPID, Integer.valueOf(spid));
            Collection<Bank> collection = HomeSupportHelper.get(ctx).getBeans(ctx, Bank.class, condition,
                    RmiApiSupport.isSortAscending(isAscending));
            bankReferences = CollectionSupportHelper.get(ctx).adaptCollection(ctx, collection, this.bankToApiAdapter_,
                    bankReferences);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Banks";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return bankReferences;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ContractReference[] listContracts(final CRMRequestHeader header, final int spid, Boolean isAscending,
            GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "listContracts",
                Constants.PERMISSION_GENERAL_PROVISIONING_READ_LISTCONTRACTS,
                Constants.PERMISSION_ACCOUNTS_READ_LISTCONTRACTS);
        RmiApiSupport.getCrmServiceProvider(ctx, spid, this);
        ContractReference[] contactReferences = new ContractReference[]
            {};
        try
        {
            final Object condition = new EQ(ContractXInfo.SPID, spid);
            Collection<Contract> collection = HomeSupportHelper.get(ctx).getBeans(ctx, Contract.class, condition,
                    RmiApiSupport.isSortAscending(isAscending));
            contactReferences = CollectionSupportHelper.get(ctx).adaptCollection(ctx, collection,
                    this.contractToApiAdapter_, contactReferences);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Contracts";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return contactReferences;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public com.redknee.util.crmapi.wsdl.v2_2.types.generalprovisioning.Contract getContract(final CRMRequestHeader header,
        final long contractID, GenericParameter[] parameters) throws CRMExceptionFault
    {

            final Context ctx = getContext().createSubContext();
            // handle login. Failed logins throw exceptions
            RmiApiSupport.authenticateUser(ctx, header, "getContract", 
                    Constants.PERMISSION_GENERAL_PROVISIONING_READ_GETCONTRACT, 
                    Constants.PERMISSION_ACCOUNTS_READ_GETCONTRACT);

            Contract contract = null;
            try
            {
                contract = HomeSupportHelper.get(ctx).findBean(ctx, Contract.class, contractID);
            }
            catch (final Exception e)
            {
                final String msg = "Unable to retrieve Contract " + contractID;
                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
            }

            if (contract == null)
            {
                final String identifier = "Contract " + contractID;
                RmiApiErrorHandlingSupport.identificationException(ctx, identifier, this);
            }

            final com.redknee.util.crmapi.wsdl.v2_2.types.generalprovisioning.Contract result;
            result = ContractToApiAdapter.adaptContractToApi(contract);

            return result;

    }


    /**
     * {@inheritDoc}
     */
    @Override
    public BlockingTemplateReference[] listBlockingTemplates(CRMRequestHeader header, int spid,
            BlockingTemplateStateType[] states, Boolean isAscending, GenericParameter[] parameters) throws CRMExceptionFault
    {

            final Context ctx = getContext().createSubContext();
            // handle login. Failed logins throw exceptions
            RmiApiSupport.authenticateUser(ctx, header, "listBlockingTemplates", Constants.PERMISSION_GENERAL_PROVISIONING_READ_LISTBLOCKINGTEMPLATES);
            RmiApiSupport.getCrmServiceProvider(ctx, spid, this);
            // TODO: Implement this method!
            return new BlockingTemplateReference[] {};

    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ChargingTemplateReference[] listChargingTemplates(CRMRequestHeader header,
            ListChargingTemplatesCriteria criteria, Boolean isAscending, GenericParameter[] parameters) throws CRMExceptionFault
    {

            final Context ctx = getContext().createSubContext();
            // handle login. Failed logins throw exceptions
            RmiApiSupport.authenticateUser(ctx, header, "listChargingTemplates", Constants.PERMISSION_GENERAL_PROVISIONING_READ_LISTCHARGINGTEMPLATES);
            And filter = new And();

            RmiApiErrorHandlingSupport.validateMandatoryObject(criteria, "criteria");

            ListChargingTemplatesCriteriaSequence_type0 spidChoice = criteria.getListChargingTemplatesCriteriaSequence_type0();
            ListChargingTemplatesCriteriaSequence_type1 idChoice = criteria.getListChargingTemplatesCriteriaSequence_type1();

            // All choices may be non-null, so must branch code based on values within the choices
            if (spidChoice != null
                    && (spidChoice.getSpid() != null
                            || (spidChoice.getStates() != null)
                            && spidChoice.getStates().length > 0))
            {
                RmiApiErrorHandlingSupport.validateMandatoryObject(spidChoice.getSpid(), "spid");
                filter.add(new EQ(ChargingTemplateXInfo.SPID, Integer.valueOf(spidChoice.getSpid())));

                if (spidChoice.getStates() != null)
                {
                    Or or = new Or();
                    for (int i=0; i<spidChoice.getStates().length; i++)
                    {
                        ChargingTemplateStateType stateType = spidChoice.getStates()[i];
                        if (stateType == null) {
                            continue;
                        }

                        //equals method on AXIS2 generated code (the enum) is useless
                        if (ChargingTemplateStateTypeEnum.ACTIVE.getValue().getValue() == stateType.getValue())
                        {
                            or.add(new EQ(ChargingTemplateXInfo.ENABLED, Boolean.TRUE));
                        }
                        else
                        {
                            or.add(new EQ(ChargingTemplateXInfo.ENABLED, Boolean.FALSE));
                        }

                    }
                    if (or.getList().size() > 0)
                    {
                        filter.add(or);
                    }
                }
            }
            else if (idChoice != null
                    && idChoice.getIdentifiers() != null
                    && idChoice.getIdentifiers().length > 0)
            {
                Set<Long> identifiers = new HashSet<Long>();
                for (int i=0; i<idChoice.getIdentifiers().length; i++)
                {
                    long id = idChoice.getIdentifiers()[i];
                    identifiers.add(Long.valueOf(id));

                }
                if (identifiers.size()>0)
                {
                    In in = new In(ChargingTemplateXInfo.IDENTIFIER, identifiers);
                    filter.add(in);
                }
            }
            else 
            {
                RmiApiErrorHandlingSupport.simpleValidation("criteria", "valid choice criteria must be specified.");
            }

            ChargingTemplateReference[] chargingTemplateReferences = new ChargingTemplateReference[] {};
            try
            {
                Collection<ChargingTemplate> collection = HomeSupportHelper.get(ctx).getBeans(
                        ctx, 
                        ChargingTemplate.class, 
                        filter, 
                        RmiApiSupport.isSortAscending(isAscending));

                chargingTemplateReferences = CollectionSupportHelper.get(ctx).adaptCollection(
                        ctx, 
                        collection, 
                        this.chargingTemplateToApiAdapter_, 
                        chargingTemplateReferences);

            }
            catch (final Exception e)
            {
                final String msg = "Unable to retrieve Charging Templates";
                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
            }
            return chargingTemplateReferences;

    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ScreeningTemplateReference[] listScreeningTemplates(CRMRequestHeader header,
            ListScreeningTemplatesCriteria criteria, Boolean isAscending, GenericParameter[] parameters) throws CRMExceptionFault
    {

            final Context ctx = getContext().createSubContext();
            // handle login. Failed logins throw exceptions
            RmiApiSupport.authenticateUser(ctx, header, "listScreeningTemplates", Constants.PERMISSION_GENERAL_PROVISIONING_READ_LISTSCREENINGTEMPLATES);

            RmiApiErrorHandlingSupport.validateMandatoryObject(criteria, "criteria");

            And filter = new And();

            ListScreeningTemplatesCriteriaSequence_type0 spidChoice = criteria.getListScreeningTemplatesCriteriaSequence_type0();
            ListScreeningTemplatesCriteriaSequence_type1 idChoice = criteria.getListScreeningTemplatesCriteriaSequence_type1();

            if (spidChoice != null
                    && (spidChoice.getSpid() != null
                            || (spidChoice.getStates() != null)
                            && spidChoice.getStates().length > 0))
            {
                RmiApiErrorHandlingSupport.validateMandatoryObject(spidChoice.getSpid(), "spid");
                filter.add(new EQ(ScreeningTemplateXInfo.SPID, Integer.valueOf(spidChoice.getSpid())));

                if (spidChoice.getStates() != null)
                {
                    Or or = new Or();
                    for (int i=0; i<spidChoice.getStates().length; i++)
                    {
                        ScreeningTemplateStateType stateType = spidChoice.getStates()[i];

                        if (stateType == null) {
                            continue;
                        }

                        //equals method on AXIS2 generated code (the enum) is useless
                        if (ScreeningTemplateStateTypeEnum.ACTIVE.getValue().getValue() == stateType.getValue())
                        {
                            or.add(new EQ(ScreeningTemplateXInfo.ENABLED, Boolean.TRUE));
                        }
                        else
                        {
                            or.add(new EQ(ScreeningTemplateXInfo.ENABLED, Boolean.FALSE));
                        }
                    }
                    if (or.getList().size() > 0)
                    {
                        filter.add(or);
                    }
                }
            }
            else if (idChoice != null
                    && idChoice.getIdentifiers() != null
                    && idChoice.getIdentifiers().length > 0)
            {
                Set<Long> identifiers = new HashSet<Long>();
                for (int i=0; i<idChoice.getIdentifiers().length; i++)
                {
                    long id = idChoice.getIdentifiers()[i];
                    identifiers.add(Long.valueOf(id));

                }
                if (identifiers.size()>0)
                {
                    In in = new In(ScreeningTemplateXInfo.IDENTIFIER, identifiers);
                    filter.add(in);
                }
            }
            else
            {
                RmiApiErrorHandlingSupport.simpleValidation("criteria", "valid choice criteria must be specified.");
            }

            ScreeningTemplateReference[] screeningTemplateReferences = new ScreeningTemplateReference[] {};
            try
            {
                Collection<ScreeningTemplate> collection = HomeSupportHelper.get(ctx).getBeans(
                        ctx, 
                        ScreeningTemplate.class, 
                        filter, 
                        RmiApiSupport.isSortAscending(isAscending));

                screeningTemplateReferences = CollectionSupportHelper.get(ctx).adaptCollection(
                        ctx, 
                        collection, 
                        this.screeningTemplateToApiAdapter_, 
                        screeningTemplateReferences);

            }
            catch (final Exception e)
            {
                final String msg = "Unable to retrieve Screening Templates";
                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
            }
            return screeningTemplateReferences;

     }


    /**
     * {@inheritDoc}
     */
    @Override
    public Identification getIdentification(CRMRequestHeader header, long identifier, GenericParameter[] parameters)
            throws CRMExceptionFault
    {

            final Context ctx = getContext().createSubContext();
            // handle login. Failed logins throw exceptions
            RmiApiSupport.authenticateUser(ctx, header, "getIdentification", Constants.PERMISSION_GENERAL_PROVISIONING_READ_GETIDENTIFICATION);

            Identification id = null;
            try
            {
                com.redknee.app.crm.bean.Identification crmId = HomeSupportHelper.get(ctx).findBean(
                        ctx, 
                        com.redknee.app.crm.bean.Identification.class, 
                        (int) identifier);

                id = (Identification) this.idToApiAdapter_.adapt(ctx, crmId);
            }
            catch (final Exception e)
            {
                final String msg = "Unable to retrieve Identification";
                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
            }
            return id;

    }


    /**
     * {@inheritDoc}
     */
    @Override
    public IdentificationReference[] listIdentifications(CRMRequestHeader header, int spid, Boolean isAscending, GenericParameter[] parameters)
            throws CRMExceptionFault
    {

            final Context ctx = getContext().createSubContext();
            // handle login. Failed logins throw exceptions
            RmiApiSupport.authenticateUser(ctx, header, "listIdentifications", Constants.PERMISSION_GENERAL_PROVISIONING_READ_LISTIDENTIFICATIONS);

            IdentificationReference[] ids = new IdentificationReference[] {};
            try
            {
                EQ filter = new EQ(IdentificationXInfo.SPID, spid);
                // ID Groups are not paid-type aware yet.  Ignore paidType paremter.

                Collection<com.redknee.app.crm.bean.Identification> collection = HomeSupportHelper.get(ctx).getBeans(
                        ctx, 
                        com.redknee.app.crm.bean.Identification.class, 
                        filter, 
                        RmiApiSupport.isSortAscending(isAscending));

                ids = CollectionSupportHelper.get(ctx).adaptCollection(
                        ctx, 
                        collection, 
                        this.idToApiReferenceAdapter_, 
                        ids);
            }
            catch (final Exception e)
            {
                final String msg = "Unable to retrieve Identifications";
                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
            }
            return ids;

    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Identification[] listDetailedIdentifications(CRMRequestHeader header, int spid, Boolean isAscending, GenericParameter[] parameters)
            throws CRMExceptionFault
    {

            final Context ctx = getContext().createSubContext();
            // handle login. Failed logins throw exceptions
            RmiApiSupport.authenticateUser(ctx, header, "listDetailedIdentifications", Constants.PERMISSION_GENERAL_PROVISIONING_READ_LISTDETAILEDIDENTIFICATIONS);

            Identification[] ids = new Identification[] {};
            try
            {
                EQ filter = new EQ(IdentificationXInfo.SPID, spid);
                // ID Groups are not paid-type aware yet.  Ignore paidType paremter.

                Collection<com.redknee.app.crm.bean.Identification> collection = HomeSupportHelper.get(ctx).getBeans(
                        ctx, 
                        com.redknee.app.crm.bean.Identification.class, 
                        filter, 
                        RmiApiSupport.isSortAscending(isAscending));

                ids = CollectionSupportHelper.get(ctx).adaptCollection(
                        ctx, 
                        collection, 
                        this.idToApiAdapter_, 
                        ids);
            }
            catch (final Exception e)
            {
                final String msg = "Unable to retrieve Identifications";
                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
            }
            return ids;

    }


    /**
     * {@inheritDoc}
     */
    @Override
    public IdentificationGroup getIdentificationGroup(CRMRequestHeader header, int spid, long identifier, GenericParameter[] parameters) throws CRMExceptionFault
    {
            final Context ctx = getContext().createSubContext();
            // handle login. Failed logins throw exceptions
            RmiApiSupport.authenticateUser(ctx, header, "getIdentificationGroup", Constants.PERMISSION_GENERAL_PROVISIONING_READ_GETIDENTIFICATIONGROUP);

            IdentificationGroup idGroup = null;
            try
            {
                SpidIdentificationGroups crmIdGroup = HomeSupportHelper.get(ctx).findBean(
                        ctx, 
                        SpidIdentificationGroups.class, 
                        spid);

                List<com.redknee.app.crm.bean.IdentificationGroup> groups = crmIdGroup.getGroups();
                Visitor visitor = new FindVisitor(new EQ(IdentificationGroupXInfo.ID_GROUP, Long.valueOf(identifier).intValue()));
                visitor = Visitors.forEach(ctx, groups, visitor);
                visitor = Visitors.find(visitor, AbstractValueVisitor.class);
                if (visitor instanceof AbstractValueVisitor)
                {
                    com.redknee.app.crm.bean.IdentificationGroup value = (com.redknee.app.crm.bean.IdentificationGroup) ((AbstractValueVisitor) visitor).getValue();
                    idGroup = (IdentificationGroup) this.idGroupToApiAdapter_.adapt(ctx, value);
                }
            }
            catch (final Exception e)
            {
                final String msg = "Unable to retrieve Identification Group";
                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
            }
            return idGroup;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public IdentificationGroupReference[] listIdentificationGroups(CRMRequestHeader header, int spid,
            SystemType paidType, Boolean isAscending, GenericParameter[] parameters) throws CRMExceptionFault
    {
            final Context ctx = getContext().createSubContext();
            // handle login. Failed logins throw exceptions
            RmiApiSupport.authenticateUser(ctx, header, "listIdentificationGroups", Constants.PERMISSION_GENERAL_PROVISIONING_READ_LISTIDENTIFICATIONGROUPS);

            IdentificationGroupReference[] idGroupReferences = new IdentificationGroupReference[] {};
            try
            {
                // ID Groups are not paid-type aware yet.  Ignore paidType parameter.   
                SpidIdentificationGroups crmGroup = HomeSupportHelper.get(ctx).findBean(
                        ctx, 
                        SpidIdentificationGroups.class, 
                        spid);

                boolean sortAscending = RmiApiSupport.isSortAscending(isAscending);
                Comparator sorter = ComparableComparator.instance();
                if (!sortAscending)
                {
                    sorter = new ReverseComparator(sorter);
                }

                List groups = new ArrayList(crmGroup.getGroups());
                Collections.sort(groups, sorter);

                idGroupReferences = CollectionSupportHelper.get(ctx).adaptCollection(
                        ctx, 
                        groups, 
                        this.idGroupToApiReferenceAdapter_, 
                        idGroupReferences);

                for (IdentificationGroupReference idGroup : idGroupReferences)
                {
                    idGroup.setSpid(spid);
                }
            }
            catch (final Exception e)
            {
                final String msg = "Unable to retrieve Identification Groups";
                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
            }
            return idGroupReferences;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public IdentificationGroup[] listDetailedIdentificationGroups(CRMRequestHeader header, int spid,
            SystemType paidType, Boolean isAscending, GenericParameter[] parameters) throws CRMExceptionFault
    {
            final Context ctx = getContext().createSubContext();
            // handle login. Failed logins throw exceptions
            RmiApiSupport.authenticateUser(ctx, header, "listDetailedIdentificationGroups", Constants.PERMISSION_GENERAL_PROVISIONING_READ_LISTDETAILEDIDENTIFICATIONGROUPS);

            IdentificationGroup[] idGroups = new IdentificationGroup[] {};
            try
            {
                // ID Groups are not paid-type aware yet.  Ignore paidType parameter.
                SpidIdentificationGroups crmGroup = HomeSupportHelper.get(ctx).findBean(
                        ctx, 
                        SpidIdentificationGroups.class, 
                        spid);

                boolean sortAscending = RmiApiSupport.isSortAscending(isAscending);
                Comparator sorter = ComparableComparator.instance();
                if (!sortAscending)
                {
                    sorter = new ReverseComparator(sorter);
                }

                List groups = new ArrayList(crmGroup.getGroups());
                Collections.sort(groups, sorter);

                idGroups = CollectionSupportHelper.get(ctx).adaptCollection(
                        ctx, 
                        groups, 
                        this.idGroupToApiAdapter_, 
                        idGroups);

                for (IdentificationGroup idGroup : idGroups)
                {
                    idGroup.setSpid(spid);
                }
            }
            catch (final Exception e)
            {
                final String msg = "Unable to retrieve Identification Groups";
                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
            }
            return idGroups;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public DiscountClassReference[] listDiscountClasses(CRMRequestHeader header, int spid, Boolean isAscending,
            GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "listDiscountClasses",
                Constants.PERMISSION_GENERAL_PROVISIONING_READ_LISTDISCOUNTCLASSES);
        RmiApiSupport.getCrmServiceProvider(ctx, spid, this);
        DiscountClassReference[] discountClasses = new DiscountClassReference[]
            {};
        try
        {
            Collection<com.redknee.app.crm.bean.DiscountClass> crmDiscountClasses = HomeSupportHelper.get(ctx)
                    .getBeans(ctx, com.redknee.app.crm.bean.DiscountClass.class, new EQ(DiscountClassXInfo.SPID, spid),
                            RmiApiSupport.isSortAscending(isAscending));
            discountClasses = CollectionSupportHelper.get(ctx).adaptCollection(ctx, crmDiscountClasses,
                    this.discountClassToApiReferenceAdapter_, discountClasses);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Discount Classes";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return discountClasses;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public DiscountClass getDiscountClass(CRMRequestHeader header, long discountClassID, GenericParameter[] parameters) throws CRMExceptionFault
    {
            final Context ctx = getContext().createSubContext();
            // handle login. Failed logins throw exceptions
            RmiApiSupport.authenticateUser(ctx, header, "getDiscountClass", Constants.PERMISSION_GENERAL_PROVISIONING_READ_GETDISCOUNTCLASS);        
            DiscountClass discountClass = null;
            if(discountClassID < 0)
            {
                RmiApiErrorHandlingSupport.simpleValidation("discountClassID", "Invalid discount class ID : " + discountClassID);
            }
            try
            {
                com.redknee.app.crm.bean.DiscountClass crmDiscountClass = HomeSupportHelper.get(ctx).findBean(
                        ctx, 
                        com.redknee.app.crm.bean.DiscountClass.class,
                        (int) discountClassID);
               if(crmDiscountClass == null)
               {
                   RmiApiErrorHandlingSupport.identificationException(ctx, "Discount Class not found : " + discountClassID, this);
               }
                discountClass = (DiscountClass) this.discountClassToApiAdapter_.adapt(ctx, crmDiscountClass);
            }
            catch (final Exception e)
            {
                final String msg = "Unable to retrieve Discount Classes";
                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
            }
            return discountClass;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public DiscountClass[] listDetailedDiscountClasses(CRMRequestHeader header, int spid, Boolean isAscending, GenericParameter[] parameters)
            throws CRMExceptionFault
    {
            final Context ctx = getContext().createSubContext();
            // handle login. Failed logins throw exceptions
            RmiApiSupport.authenticateUser(ctx, header, "listDetailedDiscountClasses", Constants.PERMISSION_GENERAL_PROVISIONING_READ_LISTDETAILEDDISCOUNTCLASSES);
            RmiApiSupport.getCrmServiceProvider(ctx, spid, this);        
            DiscountClass[] discountClasses = new DiscountClass[] {};
            try
            {
                Collection<com.redknee.app.crm.bean.DiscountClass> crmDiscountClasses = HomeSupportHelper.get(ctx).getBeans(
                        ctx, 
                        com.redknee.app.crm.bean.DiscountClass.class, 
                        new EQ(DiscountClassXInfo.SPID, spid), 
                        RmiApiSupport.isSortAscending(isAscending));
                
                discountClasses = CollectionSupportHelper.get(ctx).adaptCollection(
                        ctx, 
                        crmDiscountClasses, 
                        this.discountClassToApiAdapter_, 
                        discountClasses);
            }
            catch (final Exception e)
            {
                final String msg = "Unable to retrieve Discount Classes";
                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
            }
            return discountClasses;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public DiscountClassReference[] listAuthorizedDiscountClasses(CRMRequestHeader header, int spid, Boolean isAscending, GenericParameter[] parameters) throws CRMExceptionFault
    {
            final Context ctx = getContext().createSubContext();
            // handle login. Failed logins throw exceptions
            RmiApiSupport.authenticateUser(ctx, header, "listAuthorizedDiscountClasses", Constants.PERMISSION_GENERAL_PROVISIONING_READ_LISTAUTHORIZEDDISCOUNTCLASSES);
            RmiApiSupport.getCrmServiceProvider(ctx, spid, this);        
            DiscountClassReference[] discountClasses = new DiscountClassReference[] {};
            try
            {
                Collection<com.redknee.app.crm.bean.DiscountClass> crmDiscountClasses = HomeSupportHelper.get(ctx).getBeans(
                        ctx, 
                        com.redknee.app.crm.bean.DiscountClass.class, 
                        new EQ(DiscountClassXInfo.SPID, spid), 
                        RmiApiSupport.isSortAscending(isAscending));
                
                if (crmDiscountClasses != null)
                {
                    Iterator<com.redknee.app.crm.bean.DiscountClass> iter = crmDiscountClasses.iterator();
                    while (iter.hasNext())
                    {
                        com.redknee.app.crm.bean.DiscountClass current = iter.next();
                        if (current == null 
                                || !AuthSupport.hasPermission(ctx, new SimplePermission(current.getPermission())))
                        {
                            iter.remove();
                        }
                    }
                }
                
                discountClasses = CollectionSupportHelper.get(ctx).adaptCollection(
                        ctx, 
                        crmDiscountClasses, 
                        this.discountClassToApiReferenceAdapter_, 
                        discountClasses);
            }
            catch (final Exception e)
            {
                final String msg = "Unable to retrieve Discount Classes";
                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
            }
            return discountClasses;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public CollectionAgencyReference[] listCollectionAgencies(CRMRequestHeader header, int spid,
            Boolean isAscending, GenericParameter[] parameters) throws CRMExceptionFault
    {

        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "listCollectionAgencies", Constants.PERMISSION_GENERAL_PROVISIONING_READ_LISTCOLLECTIONAGENCIES);
        RmiApiSupport.getCrmServiceProvider(ctx, spid, this);
        And filter = new And();

        CollectionAgencyReference[] collectionAgencies = new CollectionAgencyReference[] {};
        try
        {
            Collection<com.redknee.app.crm.bean.DebtCollectionAgency> crmCollectionAgencies = HomeSupportHelper.get(ctx).getBeans(
                    ctx, 
                    com.redknee.app.crm.bean.DebtCollectionAgency.class, 
                    new EQ(DiscountClassXInfo.SPID, spid), 
                    RmiApiSupport.isSortAscending(isAscending));
            
            collectionAgencies = CollectionSupportHelper.get(ctx).adaptCollection(
                    ctx, 
                    crmCollectionAgencies, 
                    this.collectionAgencyToApiReferenceAdapter_, 
                    collectionAgencies);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Collection Agencies";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return collectionAgencies;
     }


    /**
     * {@inheritDoc}
     */
    @Override
    public DiscountClassModificationResult createDiscountClass(CRMRequestHeader header, DiscountClass discountClass,
            GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "createDiscountClass",
                Constants.PERMISSION_GENERAL_PROVISIONING_WRITE_CREATEDISCOUNTCLASS);
        RmiApiErrorHandlingSupport.validateMandatoryObject(discountClass, "discountClass");
        RmiApiErrorHandlingSupport.validateMandatoryObject(discountClass.getSpid(), "discountClass.spid");
        RmiApiErrorHandlingSupport.validateMandatoryObject(discountClass.getName(), "discountClass.name");
        RmiApiErrorHandlingSupport.validateMandatoryObject(discountClass.getParameters(), "discountClass.parameters");
        RmiApiErrorHandlingSupport.validateMandatoryObject(discountClass.getDiscountPercentage(), "discountClass.discountPercentage");
        RmiApiSupport.validateExistanceOfBeanForKey(ctx, CRMSpid.class, new EQ(CRMSpidXInfo.ID, discountClass.getSpid()));
        DiscountClassModificationResult discountClassResult = new DiscountClassModificationResult();
        try
        {
            Home home = RmiApiSupport.getCrmHome(ctx, com.redknee.app.crm.bean.DiscountClassHome.class,
                    GeneralProvisioningImpl.class);
            home = ApiSupport.injectAPIUpdateEntityHomeIntoPipeline(ctx, home);
            com.redknee.app.crm.bean.DiscountClass crmDiscountClass = DiscountClassToApiAdapter
                    .adaptApiToDiscountClass(ctx, discountClass);
            DiscountClassToApiAdapter.adaptGenericParametersToCreateDiscountClass(discountClass.getParameters(), crmDiscountClass);
            RmiApiSupport.validateExistanceOfBeanForKey(
                    ctx,
                    GLCodeMapping.class,
                    new And().add(new EQ(GLCodeMappingXInfo.SPID, crmDiscountClass.getSpid())).add(
                            new EQ(GLCodeMappingXInfo.GL_CODE, crmDiscountClass.getGLCode())));
            RmiApiSupport.validateExistanceOfBeanForKey(
                    ctx,
                    TaxAuthority.class,
                    new And().add(new EQ(TaxAuthorityXInfo.SPID, crmDiscountClass.getSpid())).add(
                            new EQ(TaxAuthorityXInfo.TAX_ID, crmDiscountClass.getTaxAuthority())));
            Object obj = home.create(crmDiscountClass);
            discountClassResult.setDiscountClass((DiscountClass) discountClassToApiAdapter_.adapt(ctx, obj));
            discountClassResult.setParameters(parameters);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to create Discount Classes";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return discountClassResult;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public DiscountClassModificationResult updateDiscountClass(CRMRequestHeader header, DiscountClass discountClass,
            GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "updateDiscountClass",
                Constants.PERMISSION_GENERAL_PROVISIONING_WRITE_CREATEDISCOUNTCLASS);
        RmiApiErrorHandlingSupport.validateMandatoryObject(discountClass, "discountClass");
        RmiApiErrorHandlingSupport.validateMandatoryObject(discountClass.getIdentifier(), "discountClass.identifier");
        RmiApiSupport.validateExistanceOfBeanForKey(ctx, CRMSpid.class, new EQ(CRMSpidXInfo.ID, discountClass.getSpid()));
        DiscountClassModificationResult discountClassResult = new DiscountClassModificationResult();
        try
        {
            Home home = RmiApiSupport.getCrmHome(ctx, com.redknee.app.crm.bean.DiscountClassHome.class,
                    GeneralProvisioningImpl.class);
            home = ApiSupport.injectAPIUpdateEntityHomeIntoPipeline(ctx, home);
            
            com.redknee.app.crm.bean.DiscountClass findBean = HomeSupportHelper.get(ctx).findBean(ctx,
                    com.redknee.app.crm.bean.DiscountClass.class, discountClass.getIdentifier().intValue());
            if (findBean == null)
            {
                RmiApiErrorHandlingSupport.simpleValidation("discountClass", "Discount Class does not exist "
                        + discountClass.getIdentifier() + " .");
            }
            if (discountClass.getSpid() != null && findBean.getSpid() != discountClass.getSpid().intValue())
            {
                RmiApiErrorHandlingSupport.simpleValidation("discountClass",
                        "Spid Update is not allowed for Discount " + discountClass.getIdentifier() + " to Spid " + discountClass.getSpid());
            }
            com.redknee.app.crm.bean.DiscountClass crmDiscountClass = DiscountClassToApiAdapter
                    .adaptApiToDiscountClass(ctx,discountClass, findBean);            
            Object obj = home.store(crmDiscountClass);
            discountClassResult.setDiscountClass((DiscountClass) discountClassToApiAdapter_.adapt(ctx, obj));
            discountClassResult.setParameters(parameters);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to update Discount Classes ";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return discountClassResult;
    }

    @Override
    public CreditCardTypeReference[] listCreditCardTypes(CRMRequestHeader header, Boolean isAscending, GenericParameter[] parameters)
            throws CRMExceptionFault
    {
        CreditCardTypeReference[] ret = new CreditCardTypeReference[] {}; 
        try 
        {
           final Context ctx = ContextHelper.getContext().createSubContext();
            
            // handle login. Failed logins throw exceptions
			RmiApiSupport
			    .authenticateUser(
			        ctx,
			        header,
			        "listCreditCardTypes",
			        Constants.PERMISSION_GENERALPROVISIONING_READ_LISTCREDITCARDTYPES);

            try
            {
                Collection<CreditCardType> c = HomeSupportHelper.get(ctx).getBeans(ctx, CreditCardType.class); 
                ret = new CreditCardTypeReference[c.size()]; 
                int i = 0;
                
                for (CreditCardType cardType : c)
                {
                    ret[i] = new CreditCardTypeReference(); 
                    ret[i].setIdentifier(cardType.getId());
                    ret[i].setName(cardType.getCardType()); 
                    ret[i].setNumberRegex(cardType.getNumberregex());
                    ret[i].setUsesLuhnValidation(cardType.getLuhnValidation()); 
                    ++i; 
                }
                
                
            } catch (final Exception e)
            {
                final String msg = "Unable to retrieve creditCardType" ; 
                new MinorLogMsg(this, msg, e).log(ctx); 
                RmiApiErrorHandlingSupport.generalException(ctx, e, msg, this);
            }
        
    
        }catch (com.redknee.util.crmapi.wsdl.v3_0.api.CRMExceptionFault v30Exception)
        {
            throw v30Exception;
        }
        
           
        return ret; 
    }


    @Override
    public ServiceProviderReference[] listServiceProviders(CRMRequestHeader header, Boolean isAscending, GenericParameter[] parameters)
            throws CRMExceptionFault
    {
		ServiceProviderReference[] ret = new ServiceProviderReference[] {};
		try
		{
			final Context ctx = ContextHelper.getContext().createSubContext();

			// handle login. Failed logins throw exceptions
			RmiApiSupport
			    .authenticateUser(
			        ctx,
			        header,
			        "listServiceProviders",
			        Constants.PERMISSION_GENERALPROVISIONING_READ_LISTSERVICEPROVIDERS);

			try
			{
				Collection<CRMSpid> c =
				    HomeSupportHelper.get(ctx).getBeans(ctx, CRMSpid.class);
				ret = new ServiceProviderReference[c.size()];
				int i = 0;

				for (CRMSpid spid : c)
				{
					ret[i] = new ServiceProviderReference();
					ret[i].setIdentifier(spid.getSpid());
					ret[i].setName(spid.getName());
					++i;
				}

			}
			catch (final Exception e)
			{
				final String msg = "Unable to retrieve serviceProviders";
				new MinorLogMsg(this, msg, e).log(ctx);
				RmiApiErrorHandlingSupport.generalException(ctx, e, msg, this);
			}

		}
		catch (com.redknee.util.crmapi.wsdl.v3_0.api.CRMExceptionFault v30Exception)
		{
			throw v30Exception;
		}

		return ret;
	}



    @Override
	public BlacklistStatus getBlacklistStatus(CRMRequestHeader header,
	    IdentificationEntry identification, GenericParameter[] parameters)
            throws CRMExceptionFault
    {
		BlacklistStatus ret = new BlacklistStatus();
		try
		{
			final Context ctx = ContextHelper.getContext().createSubContext();

			// handle login. Failed logins throw exceptions
			RmiApiSupport
			    .authenticateUser(
			        ctx,
			        header,
			        "listServiceProviders",
			        Constants.PERMISSION_GENERALPROVISIONING_READ_GETBLACKLISTSTATUS);
			RmiApiErrorHandlingSupport.validateMandatoryObject(identification,
			    "identification");

			try
			{
				ret.setIdentificationNumber(identification.getValue());
				ret.setIdentificationTypeID(identification.getType());

				BlackList list =
				    BlackListSupport.getIdList(ctx, identification.getType()
				        .intValue(), identification.getValue());
				BlacklistType blacklistType =
				    BlacklistType.Factory.fromValue(-1);
				if (list != null)
				{
					ret.setIdentifier(list.getBlackListID());
					if (list.getBlackType() != null)
					{
						blacklistType =
						    BlacklistType.Factory.fromValue(list.getBlackType()
						        .getIndex());
					}
					ret.setNote(list.getNote());
				}
				ret.setListType(blacklistType);
			}
			catch (final Exception e)
			{
				final String msg = "Unable to retrieve blacklist status";
				new MinorLogMsg(this, msg, e).log(ctx);
				RmiApiErrorHandlingSupport.generalException(ctx, e, msg, this);
			}

		}
		catch (com.redknee.util.crmapi.wsdl.v3_0.api.CRMExceptionFault v30Exception)
		{
			throw v30Exception;
		}

		return ret;
    }


    @Override
    public SequenceBlock acquireSequenceBlock(CRMRequestHeader header, String sequenceID, int blockSize,
            GenericParameter[] parameters) throws CRMExceptionFault
    {
        try
        {
            final Context ctx = getContext().createSubContext();
            RmiApiSupport.authenticateUser(ctx, header, "acquireSequenceBlock",
                    Constants.PERMISSION_GENERAL_PROVISIONING_READ_ACQUIRESEQUENCEBLOCK);
            OnDemandSequence sequence = OnDemandSequenceManager.acquireNextOnDemandSequnceBlock(ctx,
                    sequenceID, blockSize);
            if (sequence != null)
            {
                SequenceBlock block = new SequenceBlock();
                block.setEndingValue(sequence.getEndNum());
                block.setStartingValue(sequence.getNextNum());
                return block;
            }
        }
        catch (Exception ex)
        {
            throw new CRMExceptionFault("Unable to acquire receipt block ", ex);
        }
        return null;
    }


    @Override
    public ReceiptBlock acquireReceiptBlock(CRMRequestHeader header, int blockSize, GenericParameter[] parameters)
            throws CRMExceptionFault
    {        
        try
        {
            final Context ctx = getContext().createSubContext();
            RmiApiSupport.authenticateUser(ctx, header, "acquireReceiptBlock",
                    Constants.PERMISSION_GENERAL_PROVISIONING_READ_ACQUIRERECEIPTBLOCK);
            OnDemandSequence sequence = OnDemandSequenceManager.acquireNextOnDemandSequnceBlock(ctx,
                    OnDemandSequenceManager.RECEIPT_SEQUENCE_KEY, blockSize);
            if (sequence != null)
            {
                ReceiptBlock block = new ReceiptBlock();
                block.setEndingValue(sequence.getEndNum());
                block.setStartingValue(sequence.getNextNum());
                return block;
            }
        }
        catch (Exception ex)
        {
            throw new CRMExceptionFault("Unable to acquire receipt block ", ex);
        }
        return null;
    }
   
    
    
    private static SupplementaryDataEntityEnum extractSupplementaryDataEntity(SupplementaryDataTarget target)
    {
        SupplementaryDataEntityEnum entity = null;

        if (target instanceof AccountSupplementaryDataTarget)
        {
            entity = SupplementaryDataEntityEnum.ACCOUNT;
        }
        else if (target instanceof SubscriptionSupplementaryDataTarget)
        {
            entity = SupplementaryDataEntityEnum.SUBSCRIPTION;
        }
        return entity;
    }
    
    private void validateSupplementaryDataParameterNames(SupplementaryData[] supplementaryData, String xmlField) throws CRMExceptionFault
    {
        if (supplementaryData!=null)
        {
            for (SupplementaryData data: supplementaryData)
            {
                validateSupplementaryDataFieldName(data.getName(), xmlField);
            }
        }
    }
    
    private void validateSupplementaryDataParameterNames(String[] supplementaryData, String xmlField) throws CRMExceptionFault
    {
        if (supplementaryData!=null)
        {
            for (String data: supplementaryData)
            {
                validateSupplementaryDataFieldName(data, xmlField);
            }
        }
    }

    private void validateSupplementaryDataFieldName(String fieldName, String xmlField) throws CRMExceptionFault
    {
        if (fieldName == null || fieldName.isEmpty())
        {
            RmiApiErrorHandlingSupport.simpleValidation(xmlField,
                    "Supplementary Data field name cannot be empty");
        }
    }

    private String extractSupplementaryDataEntityIdentifier(Context ctx, SupplementaryDataTarget target) throws CRMExceptionFault
    {
        String entityIdentifier = null;

        if (target instanceof AccountSupplementaryDataTarget)
        {
            AccountSupplementaryDataTarget accountTarget = (AccountSupplementaryDataTarget) target;
            RmiApiErrorHandlingSupport.validateMandatoryObject(accountTarget.getIdentifier(), "accountID");
            AccountsImpl.getCrmAccount(ctx, accountTarget.getIdentifier(), this);
            entityIdentifier = accountTarget.getIdentifier();
        }
        else if (target instanceof SubscriptionSupplementaryDataTarget)
        {
            SubscriptionSupplementaryDataTarget subscriptionTarget = (SubscriptionSupplementaryDataTarget) target;
            final Subscriber subscriber = SubscribersApiSupport.getCrmSubscriber(ctx, subscriptionTarget.getSubscriptionRef(), this);
            entityIdentifier = subscriber.getId();
        }
        else
        {
            RmiApiErrorHandlingSupport.simpleValidation("target",
                    "Object cannot be of base type. Please use either AccountSupplementaryDataTarget or SubscriptionSupplementaryDataTarget");
        }
        
        return entityIdentifier;
    }

    @Override
    public SupplementaryDataResponse removeSupplementaryData(CRMRequestHeader header, SupplementaryDataTarget target,
            SupplementaryData[] data, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        RmiApiSupport.authenticateUser(ctx, header, "removeSupplementaryData",
                Constants.PERMISSION_GENERAL_PROVISIONING_WRITE_REMOVESUPPLEMENTARYDATA);

        validateSupplementaryDataParameterNames(data, "data");
        String identifier = extractSupplementaryDataEntityIdentifier(ctx, target);
        SupplementaryDataEntityEnum entity = extractSupplementaryDataEntity(target);
        boolean failure = false;
        
        for (SupplementaryData singleData : data)
        {
            try
            {
                SupplementaryDataSupportHelper.get(ctx).removeSupplementaryData(ctx, entity, identifier, singleData.getName());
            }
            catch (HomeException e)
            {
                failure = true;
                LogSupport.minor(ctx, this, "Unable to remove supplementary data: entity = " + entity.getDescription()
                        + ", identifier = " + identifier + ", key = " + singleData.getName() + " -> " + e.getMessage(),
                        e);
            }
        }
        
        if (failure)
        {
            RmiApiErrorHandlingSupport.generalException(ctx, null, "Unable to remove all the supplementary data", ExceptionCode.DATA_DELETION_EXCEPTION, "removeSupplementaryData");
        }
        
        return getSupplementaryData(ctx, entity, identifier);
    }


    @Override
    public SupplementaryDataResponse removeSupplementaryDataByName(CRMRequestHeader header,
            SupplementaryDataTarget target, String[] dataNames, GenericParameter[] parameters)
            throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        RmiApiSupport.authenticateUser(ctx, header, "removeSupplementaryDataByName",
                Constants.PERMISSION_GENERAL_PROVISIONING_WRITE_REMOVESUPPLEMENTARYDATABYNAME);

        validateSupplementaryDataParameterNames(dataNames, "dataNames");
        String identifier = extractSupplementaryDataEntityIdentifier(ctx, target);
        SupplementaryDataEntityEnum entity = extractSupplementaryDataEntity(target);
        
        boolean failure = false;
        
        for (String key : dataNames)
        {
            try
            {
                SupplementaryDataSupportHelper.get(ctx).removeSupplementaryData(ctx, entity, identifier, key);
            }
            catch (HomeException e)
            {
                failure = true;
                LogSupport.minor(ctx, this, "Unable to remove supplementary data: entity = " + entity.getDescription()
                        + ", identifier = " + identifier + ", key = " + key + " -> " + e.getMessage(),
                        e);
            }
        }

        if (failure)
        {
            RmiApiErrorHandlingSupport.generalException(ctx, null, "Unable to remove all the supplementary data", ExceptionCode.DATA_DELETION_EXCEPTION, "removeSupplementaryData");
        }

        return getSupplementaryData(ctx, entity, identifier);
    }


    @Override
    public SupplementaryDataResponse addOrUpdateSupplementaryData(CRMRequestHeader header,
            SupplementaryDataTarget target, SupplementaryData[] data, GenericParameter[] parameters)
            throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        RmiApiSupport.authenticateUser(ctx, header, "addOrUpdateSupplementaryData",
                Constants.PERMISSION_GENERAL_PROVISIONING_WRITE_ADDORUPDATESUPPLEMENTARYDATA);
        
        validateSupplementaryDataParameterNames(data, "data");
        String identifier = extractSupplementaryDataEntityIdentifier(ctx, target);
        SupplementaryDataEntityEnum entity = extractSupplementaryDataEntity(target);
        
        boolean failure = false;

        for (SupplementaryData singleData : data)
        {
            try
            {
                SupplementaryDataSupportHelper.get(ctx).addOrUpdateSupplementaryData(ctx, entity, identifier, singleData.getName(), (String) singleData.getValue());
            }
            catch (HomeException e)
            {
                failure = true;
                LogSupport.minor(ctx, this, "Unable to add or update supplementary data: entity = " + entity.getDescription()
                        + ", identifier = " + identifier + ", key = " + singleData.getName() + " value = " + singleData.getValue() + " -> " + e.getMessage(),
                        e);
            }
        }
        
        if (failure)
        {
            RmiApiErrorHandlingSupport.generalException(ctx, null, "Unable to add or update all the supplementary data", ExceptionCode.DATA_CREATION_EXCEPTION, "addOrUpdateSupplementaryData");
        }

        return getSupplementaryData(ctx, entity, identifier);
    }

    
    @Override
    public SupplementaryDataResponse setSupplementaryData(CRMRequestHeader header, SupplementaryDataTarget target,
            SupplementaryData[] data, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        RmiApiSupport.authenticateUser(ctx, header, "setSupplementaryData",
                Constants.PERMISSION_GENERAL_PROVISIONING_WRITE_SETSUPPLEMENTARYDATA);
        
        validateSupplementaryDataParameterNames(data, "data");
        String identifier = extractSupplementaryDataEntityIdentifier(ctx, target);
        SupplementaryDataEntityEnum entity = extractSupplementaryDataEntity(target);
        
        boolean failureDelete = false;
        boolean failureAdd = false;

        try
        {
            SupplementaryDataSupportHelper.get(ctx).removeAllSupplementaryData(ctx, entity, identifier);
        }
        catch (HomeException e)
        {
            failureDelete = true;
            LogSupport.minor(ctx, this, "Unable to remove old supplementary data: entity = " + entity.getDescription()
                    + ", identifier = " + identifier + " -> " + e.getMessage(),
                    e);
        }

        for (SupplementaryData singleData : data)
        {
            try
            {
                SupplementaryDataSupportHelper.get(ctx).addOrUpdateSupplementaryData(ctx, entity, identifier, singleData.getName(), (String) singleData.getValue());
            }
            catch (HomeException e)
            {
                failureAdd = true;
                LogSupport.minor(ctx, this, "Unable to add or update supplementary data: entity = " + entity.getDescription()
                        + ", identifier = " + identifier + ", key = " + singleData.getName() + " value = " + singleData.getValue() + " -> " + e.getMessage(),
                        e);
            }
        }
        
        if (failureDelete && failureAdd)
        {
            RmiApiErrorHandlingSupport.generalException(ctx, null, "Unable to remove the old supplementary data and to add all the the supplementary data", ExceptionCode.DATASTORE_EXCEPTION, "setSupplementaryData");
        }
        else if (failureDelete)
        {
            RmiApiErrorHandlingSupport.generalException(ctx, null, "Unable to remove all the old supplementary data", ExceptionCode.DATA_DELETION_EXCEPTION, "setSupplementaryData");
        }
        else if (failureAdd)
        {
            RmiApiErrorHandlingSupport.generalException(ctx, null, "Unable to add all the new supplementary data", ExceptionCode.DATA_CREATION_EXCEPTION, "setSupplementaryData");
        }
        

        return getSupplementaryData(ctx, entity, identifier);
    }
    
    
    @Override
    public SupplementaryDataResponse getSupplementaryData(CRMRequestHeader header, SupplementaryDataTarget target,
            GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        RmiApiSupport.authenticateUser(ctx, header, "getSupplementaryData",
                Constants.PERMISSION_GENERAL_PROVISIONING_READ_GETSUPPLEMENTARYDATA);
        
        String identifier = extractSupplementaryDataEntityIdentifier(ctx, target);
        SupplementaryDataEntityEnum entity = extractSupplementaryDataEntity(target);

        return getSupplementaryData(ctx, entity, identifier);
    }
    
    @Override
    public ExecuteResult executeVoucherInfoQuery(CRMRequestHeader header, String voucher, int spid, PaidType paidType, 
            GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();

        QueryExecutorFactory executor = QueryExecutorFactory.getInstance();

        return executor.execute(ctx, GeneralProvisioningServiceSkeletonInterface.class.getSimpleName(), "executeVoucherInfoQuery", ExecuteResult.class, 
                header, voucher, spid, paidType, parameters);
    }
    
    @Override
    public ExecuteResult executeVouchersInfoQuery(CRMRequestHeader header, String[] vouchers, int spid, PaidType paidType, 
            GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();

        QueryExecutorFactory executor = QueryExecutorFactory.getInstance();

        return executor.execute(ctx, GeneralProvisioningServiceSkeletonInterface.class.getSimpleName(), "executeVouchersInfoQuery", ExecuteResult.class, 
                header, vouchers, spid, paidType, parameters);
    }

    private SupplementaryDataResponse getSupplementaryData(Context ctx, SupplementaryDataEntityEnum entity, String identifier) throws CRMExceptionFault
    {
        SupplementaryDataResponse result = null;

        SupplementaryData[] resultData = new SupplementaryData[] {};

        try
        {
            Collection<com.redknee.app.crm.bean.SupplementaryData> crmSupplementaryData = SupplementaryDataSupportHelper.get(ctx).getSupplementaryData(ctx, entity, identifier);
            
            resultData = CollectionSupportHelper.get(ctx).adaptCollection(
                    ctx, 
                    crmSupplementaryData, 
                    this.supplementaryDataToApiReferenceAdapter_, 
                    resultData);
            
            result = new SupplementaryDataResponse();
            result.setData(resultData);
            result.setParameters(null);
        }
        catch (HomeException e)
        {
            final String msg = "Unable to retrieve supplementary data";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        
        return result;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Context getContext()
    {
        return this.context_;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setContext(final Context ctx)
    {
        this.context_ = ctx;
    }

    /**
     * The operating context.
     */
    private Context context_;

    /**
     * CRM credit category to API credit category adapter.
     */
    private final CreditCategoryToApiAdapter creditCategoryToApiAdapter_;


    /**
     * CRM occupation to API occupation adapter.
     */
    private final OccupationToApiAdapter occupationToApiAdapter_;

    /**
     * CRM bank to API bank adapter.
     */
    private final BankToApiAdapter bankToApiAdapter_;

    /**
     * CRM contract to API contract adapter.
     */
    private final ContractToApiAdapter contractToApiAdapter_;

    /**
     * CRM charging template to API charging template adapter.
     */
    private final ChargingTemplateToApiAdapter chargingTemplateToApiAdapter_;

    /**
     * CRM screening template to API screening template adapter.
     */
    private final ScreeningTemplateToApiAdapter screeningTemplateToApiAdapter_;

    /**
     * CRM id group to API id group adapter.
     */
    private final IdentificationGroupToApiReferenceAdapter idGroupToApiReferenceAdapter_;

    /**
     * CRM id to API id adapter.
     */
    private final IdentificationToApiReferenceAdapter idToApiReferenceAdapter_;

    /**
     * CRM id group to API id group adapter.
     */
    private final IdentificationGroupToApiAdapter idGroupToApiAdapter_;

    /**
     * CRM id to API id adapter.
     */
    private final IdentificationToApiAdapter idToApiAdapter_;

    /**
     * CRM discount class to API discount class adapter.
     */
    private final DiscountClassToApiReferenceAdapter discountClassToApiReferenceAdapter_;

    /**
     * CRM discount class to API discount class adapter.
     */
    private final DiscountClassToApiAdapter discountClassToApiAdapter_;

    /**
     * CRM discount class to API discount class adapter.
     */
    private final CollectionAgencyReferenceToApiAdapter collectionAgencyToApiReferenceAdapter_;

    /**
     * CRM supplementary data to API supplementary data adapter.
     */
    private final SupplementaryDataToApiAdapter supplementaryDataToApiReferenceAdapter_;
}
