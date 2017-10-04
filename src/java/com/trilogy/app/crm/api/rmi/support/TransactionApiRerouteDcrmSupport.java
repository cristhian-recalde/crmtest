package com.trilogy.app.crm.api.rmi.support;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Date;

import org.apache.axis2.databinding.utils.ConverterUtil;

import com.trilogy.app.crm.api.rmi.TransactionToApiAdapter;
import com.trilogy.app.crm.bean.AdjustmentTypeActionEnum;
import com.trilogy.app.crm.bean.CurrencyPrecision;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.bean.core.AdjustmentType;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.SystemSupport;
import com.trilogy.dynamics.crm.DCRMSoapClient;
import com.trilogy.dynamics.crm.crmservice._2006.query.ArrayOfAnyType;
import com.trilogy.dynamics.crm.crmservice._2006.query.ArrayOfConditionExpression;
import com.trilogy.dynamics.crm.crmservice._2006.query.ArrayOfString;
import com.trilogy.dynamics.crm.crmservice._2006.query.ColumnSet;
import com.trilogy.dynamics.crm.crmservice._2006.query.ConditionExpression;
import com.trilogy.dynamics.crm.crmservice._2006.query.ConditionOperator;
import com.trilogy.dynamics.crm.crmservice._2006.query.FilterExpression;
import com.trilogy.dynamics.crm.crmservice._2006.query.LogicalOperator;
import com.trilogy.dynamics.crm.crmservice._2006.query.QueryExpression;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.BusinessEntity;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.BusinessEntityCollection;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.CrmBoolean;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.CrmDateTime;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.CrmMoney;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.Lookup;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.Owner;
import com.trilogy.dynamics.crm.crmservice._2006.webservices.Picklist;
import com.trilogy.dynamics.crm.crmservice._2007.webservices.Rkn_adjustmentcategory;
import com.trilogy.dynamics.crm.crmservice._2007.webservices.Rkn_adjustmentrequest;
import com.trilogy.dynamics.crm.crmservice._2007.webservices.Rkn_adjustmenttype;
import com.trilogy.dynamics.crm.crmservice._2007.webservices.Rkn_paymentagency;
import com.trilogy.dynamics.crm.crmservice._2007.webservices.Rkn_paymentmethodtype;
import com.trilogy.dynamics.crm.crmservice._2007.webservices.Rkn_reasoncode;
import com.trilogy.dynamics.crm.crmservice._2007.webservices.Rkn_serviceprovider;
import com.trilogy.dynamics.crm.crmservice._2007.webservices.Systemuser;
import com.trilogy.dynamics.crm.crmservice._2007.webservices.Team;
import com.trilogy.dynamics.crm.crmservice.types.Guid;
import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v2_1.exception.ExceptionCode;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.types.transaction.TransactionReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.transaction.TransactionRequest;


public class TransactionApiRerouteDcrmSupport
{

    public final static String RKN_IDENTIFIER_ATTRIBUTE = "rkn_identifier";
    public final static String DCRM_ENTITY_SPID = "rkn_serviceprovider";
    public final static String DCRM_ENTITY_ADJUSTMENT_CATEGORY = "rkn_adjustmentcategory";
    public final static String DCRM_ENTITY_PAYMENT_AGENT = "rkn_paymentagent";
    public final static String DCRM_ENTITY_PAYMENT_METHOD_TYPE = "rkn_paymentmethodtype";
    public final static String DCRM_ENTITY_REASON_CODE = "rkn_adjustmentreason";
    public final static String DCRM_ENTITY_ADJUSTMENT_TYPE = "rkn_adjustmenttype";
    public final static String DCRM_ENTITY_SYSTEM_USER = "systemuser";
    public final static String DCRM_ENTITY_TEAM = "team";
    public final static String DCRM_ENTITY_ACCOUNT = "account";
    public final static String DCRM_ENTITY_SUBSCRIPTION = "rkn_subscription";
    
    public final static String RKN_ADJUSTMENT_TYPE_ID = "rkn_adjustmenttypeid";
    public final static String RKN_TEAM_ID  = "teamid";
    public final static String RKN_SYSTEMUSER_ID  = "systemuserid";
    public final static String RKN_PAYMENT_AGENCY_ID  = "rkn_paymentagencyid";
    public final static String RKN_PAYMENT_METHOD_TYPE_ID  = "rkn_paymentmethodtypeid";
    public final static String RKN_REASON_CODE_ID  = "rkn_reasoncodeid";

    
    public final static String DCRM_ADJUSTMENT_NAME= "System-Generated Pre-Payment Request";
    

    public static boolean redirectRequestToDCRM(final Context ctx, final TransactionRequest request,
            final GenericParameter[] parameters, final TransactionReference reference) throws CRMExceptionFault
    {
        boolean sendToDCrm = false;
        if (parameters != null)
        {
            String ownerGuid = null;
            String accountGuid = null;
            String subscriptionGuid = null;
            String spidGuid = null;
            String adjustmentCategoryGuid = null;
            String approvedBy = null;
            for (int i = 0; i < parameters.length; i++)
            {
                GenericParameter parameter = parameters[i];
                if (parameter.getName().equals(APIGenericParameterSupport.CRM_ADJUSTMENT_REQUEST))
                {
                    sendToDCrm = true;
                }
                else if (parameter.getName().equals(APIGenericParameterSupport.CRM_ADJUSTMENT_SPID_REQUEST))
                {
                    spidGuid = (String) parameter.getValue();
                }
                else if (parameter.getName().equals(APIGenericParameterSupport.CRM_ADJUSTMENT_SUBSCRIPTION_REQUEST))
                {
                    subscriptionGuid = (String) parameter.getValue();
                }
                else if (parameter.getName().equals(APIGenericParameterSupport.CRM_ADJUSTMENT_ACCOUNT_REQUEST))
                {
                    accountGuid = (String) parameter.getValue();
                }
                else if (parameter.getName().equals(
                        APIGenericParameterSupport.CRM_ADJUSTMENT_ADJUSTMENT_CATEGORY_REQUEST))
                {
                    adjustmentCategoryGuid = (String) parameter.getValue();
                }
                else if (parameter.getName().equals(APIGenericParameterSupport.CRM_ADJUSTMENT_OWNER_REQUEST))
                {
                    ownerGuid = (String) parameter.getValue();
                }
                else if (parameter.getName().equals(APIGenericParameterSupport.CRM_ADJUSTMENT_APPROVED_BY_REQUEST))
                {
                    approvedBy = (String) parameter.getValue();
                }
                
            }
            if ((sendToDCrm)
                    && ((ownerGuid == null) || (accountGuid == null) || (spidGuid == null) || (adjustmentCategoryGuid == null) || (approvedBy == null)))
            {
                RmiApiErrorHandlingSupport
                        .handleCreateExceptions(
                                ctx,
                                null,
                                " Missing at leaset one of required GUIDs passed through Generic Parameters ( ownerGuid, accountGuid, subscriptionGuid, spidGuid, and adjustmentCategoryGuid ",
                                false, Transaction.class, null, TransactionsApiSupport.class);
            }
            if (sendToDCrm)
            {
                final User principal = (User) ctx.get(java.security.Principal.class, new User());
                String user = (principal.getId().trim().equals("") ? SystemSupport.SYSTEM_AGENT : principal.getId());
                Rkn_adjustmentrequest adjustmentRequest = convertDCrmTransaction(ctx, request, ownerGuid, accountGuid,
                        subscriptionGuid, spidGuid, user, adjustmentCategoryGuid, approvedBy);
                try
                {
                    Guid guid = DCRMSoapClient.instance().create(adjustmentRequest,
                            DCRMSoapClient.getAuthenticationToken(ctx), null, null);
                    TransactionToApiAdapter.adaptTransactionToReference(ctx, -1, request, reference, accountGuid,
                            request.getAdjustmentType(), new Date(), user);
                }
                catch (RemoteException remoteEx)
                {
                    new MinorLogMsg(TransactionApiRerouteDcrmSupport.class, "Unable create adjustment request for "
                            + adjustmentRequest, remoteEx).log(ctx);
                    RmiApiErrorHandlingSupport.handleCreateExceptions(ctx, remoteEx,
                            "Unable create adjustment request.  RemoteException was thrown ", false,
                            Transaction.class, null, TransactionApiRerouteDcrmSupport.class);
                }
                catch (Exception ex)
                {
                    new MinorLogMsg(TransactionApiRerouteDcrmSupport.class, "Unable create adjustment request for "
                            + adjustmentRequest, ex).log(ctx);
                    RmiApiErrorHandlingSupport.handleCreateExceptions(ctx, ex, "Unable create adjustment request ",
                            false, Transaction.class, null, TransactionApiRerouteDcrmSupport.class);
                }
            }
        }
        return sendToDCrm;
    }


    /**
     * Converts the bean to an instance of a DCRM entity.
     * 
     * @param context
     *            The operating context.
     * @param Rkn_adjustmentrequest
     *            The bean to convert.
     * @return An instance of a DCRM entity.
     */
    private static Rkn_adjustmentrequest convertDCrmTransaction(final Context context,
            final TransactionRequest request, final String ownerGuid, final String accountGuid,
            final String subscriptionId, final String spidGuid, final String user, final String adjustmentCategoryGuid,
            final String approvedBy)
            throws CRMExceptionFault
    {
        final Rkn_adjustmentrequest dcrmAdjustment = new Rkn_adjustmentrequest();
        {
            try
            {
                AdjustmentType adjustmentType = AdjustmentTypeSupportHelper.get(context).getAdjustmentType(context,
                        (int) request.getAdjustmentType());
                if (adjustmentType == null)
                {
                    RmiApiErrorHandlingSupport.handleCreateExceptions(context, null,
                            "AdjustmentType " + request.getAdjustmentType() + " could not be found ", false,
                            Transaction.class, null, TransactionApiRerouteDcrmSupport.class);
                }
                if (adjustmentType.getAction() != AdjustmentTypeActionEnum.CREDIT)
                {
                    RmiApiErrorHandlingSupport.handleCreateExceptions(
                            context,
                            null,
                            "AdjustmentType " + adjustmentType.getName() + " has incorrect action "
                                    + adjustmentType.getAction(), false, Transaction.class, null, TransactionApiRerouteDcrmSupport.class);
                }
                CrmBoolean crmBoolean = new CrmBoolean();
                crmBoolean.set_boolean(false);
                dcrmAdjustment.setRkn_action(crmBoolean);
                Lookup lookup = getLookUp(context, RKN_ADJUSTMENT_TYPE_ID, Integer.toString(adjustmentType.getCode()),
                        DCRM_ENTITY_ADJUSTMENT_TYPE);
                dcrmAdjustment.setRkn_adjustmenttypeid(lookup);
                Lookup lookup2 = new Lookup();
                lookup2.setGuid(adjustmentCategoryGuid);
                lookup2.setName(DCRM_ENTITY_ADJUSTMENT_CATEGORY);
                dcrmAdjustment.setRkn_adjustmentcategoryid(lookup2);
                dcrmAdjustment.setRkn_name(DCRM_ADJUSTMENT_NAME);
            }
            catch (HomeException homeEx)
            {
                RmiApiErrorHandlingSupport.handleCreateExceptions(context, homeEx, "Unable to find", false,
                        Transaction.class, null, TransactionsApiSupport.class);
            }
        }
        {
            Lookup lookup = new Lookup();
            lookup.setGuid(accountGuid);
            lookup.setName(DCRM_ENTITY_ACCOUNT);
            dcrmAdjustment.setRkn_accountid(lookup);
        }
        {
            CrmMoney param = new CrmMoney();
            
            CurrencyPrecision precision = (CurrencyPrecision) context.get(CurrencyPrecision.class);
            if (precision == null)
            {
                String msg = "Cannot find currency precision object in the context.";
                LogSupport.minor(context, TransactionApiRerouteDcrmSupport.class,
                        msg);
                RmiApiErrorHandlingSupport.generalException(context, null, msg, ExceptionCode.GENERAL_EXCEPTION, null);
            }
            param.setDecimal(BigDecimal.valueOf(request.getAmount(), precision.getStoragePrecision()).setScale(precision.getDisplayPrecision(), RoundingMode.HALF_EVEN));
            
            dcrmAdjustment.setRkn_amount(param);
        }
        {
            Picklist list = new Picklist();
            list.set_int(1);
            dcrmAdjustment.setRkn_approved(list);
        }
        {
            Lookup lookup = new Lookup();
            lookup.setGuid(approvedBy);
            lookup.setName(DCRM_ENTITY_SYSTEM_USER);
            dcrmAdjustment.setRkn_approvedbyid(lookup);
        }
        {
            dcrmAdjustment.setRkn_approveddate(adaptToCrmDateTime(context, new Date()));
        }
        {
            Owner owner = new Owner();
            owner.setGuid(ownerGuid);
            if (validateGuid(context,RKN_TEAM_ID,ownerGuid,DCRM_ENTITY_TEAM))
            {
                owner.setType(DCRM_ENTITY_TEAM);                
            }
            else if (validateGuid(context, RKN_SYSTEMUSER_ID, ownerGuid, DCRM_ENTITY_SYSTEM_USER))
            {
                owner.setType(DCRM_ENTITY_SYSTEM_USER);
            }
            dcrmAdjustment.setOwnerid(owner);
        }
        if (request.getCsrInput() != null && !request.getCsrInput().isEmpty())
        {
            dcrmAdjustment.setRkn_csrinput(request.getCsrInput());
        }
        if (request.getExternalTransactionNumber() != null && !request.getExternalTransactionNumber().isEmpty())
        {
            dcrmAdjustment.setRkn_externaltransactionnumber(request.getExternalTransactionNumber());
        }
        if (request.getLocationCode() != null && !request.getLocationCode().isEmpty())
        {
            dcrmAdjustment.setRkn_locationcode(request.getLocationCode());
        }
        if (request.getPaymentDetails() != null && !request.getPaymentDetails().isEmpty())
        {
            dcrmAdjustment.setRkn_paymentdetails(request.getPaymentDetails());
        }
        // Key key = new Key();
        // key.setGuid("System approved prepayment");
        // dcrmAdjustment.setRkn_adjustmentrequestid(key);
        if (request.getPaymentAgency() != null && !request.getPaymentAgency().isEmpty())
        {
            Lookup lookup = getLookUp(context, RKN_PAYMENT_AGENCY_ID, request.getPaymentAgency(), DCRM_ENTITY_PAYMENT_AGENT);
            dcrmAdjustment.setRkn_paymentagencyid(lookup);
        }
        if (request.getTransactionMethodID() != null)
        {
            Lookup lookup = getLookUp(context,RKN_PAYMENT_METHOD_TYPE_ID, request.getTransactionMethodID().toString(),
                    DCRM_ENTITY_PAYMENT_METHOD_TYPE);
            
            dcrmAdjustment.setRkn_paymentmethodtypeid(lookup);
        }
        if (request.getReasonCode() != null)
        {
            Lookup lookup = getLookUp(context, RKN_REASON_CODE_ID, request.getReasonCode().toString(), DCRM_ENTITY_REASON_CODE);
            dcrmAdjustment.setRkn_reasoncodeid(lookup);
        }
        {
            Lookup lookup = new Lookup();
            lookup.setGuid(spidGuid);
            lookup.setName(DCRM_ENTITY_SPID);
            dcrmAdjustment.setRkn_serviceproviderid(lookup);
        }
        if (subscriptionId != null)
        {
            Lookup lookup = new Lookup();
            lookup.setGuid(subscriptionId);
            lookup.setName(DCRM_ENTITY_SUBSCRIPTION);
            dcrmAdjustment.setRkn_subscriptionid(lookup);
        }
        {
            dcrmAdjustment.setRkn_transactiondate(adaptToCrmDateTime(context, new Date()));
        }
        return dcrmAdjustment;
    }


    private static Lookup getLookUp(final Context context, final String attributeName, final String value,
            final String entityName) throws CRMExceptionFault
    {
        Guid guid = getDcrmGuid(context, attributeName, value, entityName);
        final Lookup lookup = new Lookup();
        lookup.setType(entityName);
        lookup.setGuid(guid.getGuid());
        return lookup;
    }


    /**
     * Checks if the GUID is valid for the entity
     * 
     * @param ctx
     * @param attributeName
     * @param value
     * @param entityName
     * @return
     * @throws CRMExceptionFault
     */
    private static boolean validateGuid(final Context ctx, final String attributeName, final String value,
            final String entityName) throws CRMExceptionFault
    {
        final ConditionExpression condition = new ConditionExpression();
        condition.setAttributeName(attributeName);
        condition.setOperator(ConditionOperator.Equal);
        condition.setValues(new ArrayOfAnyType());
                
        condition.getValues().addValue(value);
        final ConditionExpression[] conditions = new ConditionExpression[]
            {condition,};
        final BusinessEntity[] businessEntities = getBusinessEntities(ctx, attributeName, attributeName, conditions,
                entityName);
        if (businessEntities != null && businessEntities.length > 0)
        {
            return getGuid(businessEntities, entityName) != null ? true : false;
        }
        return false;
    }

    /**
     * Gets the DCRM GUID for the bean of the given ID.
     * 
     * @param context
     *            The operating context.
     * @param spidID
     *            The ID of the bean for which a GUID is needed.
     * @return The GUID if one exists; null otherwise.
     */
    private static Guid getDcrmGuid(final Context context, final String attributeName, final String value,
            final String entityName) throws CRMExceptionFault
    {
        final ConditionExpression condition = new ConditionExpression();
        condition.setAttributeName(RKN_IDENTIFIER_ATTRIBUTE);
        condition.setOperator(ConditionOperator.Equal);
        condition.setValues(new ArrayOfAnyType());
        condition.getValues().addValue(value);
        final ConditionExpression[] conditions = new ConditionExpression[]
            {condition,};
        final BusinessEntity[] businessEntities = getBusinessEntities(context, attributeName, RKN_IDENTIFIER_ATTRIBUTE,
                conditions, entityName);
        if (businessEntities != null && businessEntities.length > 0)
        {
            return getGuid(businessEntities, entityName);
        }
        else
        {
            return null;
        }
    }


    private static BusinessEntity[] getBusinessEntities(final Context context, final String guidAttributeName,
            final String identifierName, final ConditionExpression[] conditions, final String entityName)
            throws CRMExceptionFault
    {
        final ColumnSet attributesSet = new ColumnSet();
        attributesSet.setAttributes(new ArrayOfString());
        if (guidAttributeName.compareTo(identifierName) <= 0)
        {
            attributesSet.getAttributes().addAttribute(guidAttributeName);
            attributesSet.getAttributes().addAttribute(identifierName);
        }
        else
        {
            attributesSet.getAttributes().addAttribute(identifierName);
            attributesSet.getAttributes().addAttribute(guidAttributeName);
        }
        final FilterExpression filter = new FilterExpression();
        filter.setFilterOperator(LogicalOperator.And);
        filter.setConditions(new ArrayOfConditionExpression());
        for (final ConditionExpression condition : conditions)
        {
            filter.getConditions().addCondition(condition);
        }
        final QueryExpression query = new QueryExpression();
        query.setEntityName(entityName);
        query.setColumnSet(attributesSet);
        query.setCriteria(filter);
        try
        {
            final BusinessEntityCollection results = DCRMSoapClient.instance().retrieveMultiple(query,
                    DCRMSoapClient.getAuthenticationToken(context), null, null);
            final BusinessEntity[] businessEntities = results.getBusinessEntities().getBusinessEntity();
            return businessEntities;
        }
        catch (Exception ex)
        {
            RmiApiErrorHandlingSupport.handleCreateExceptions(context, ex, "Unable to load data from DCrm for entity "
                    + entityName, false, Transaction.class, null, TransactionApiRerouteDcrmSupport.class);
        }
        return null;
    }


    private static Guid getGuid(final BusinessEntity[] businessEntities, final String entityName)
    {
        final Guid[] guids = new Guid[businessEntities.length];
        for (int index = 0; index < businessEntities.length; ++index)
        {
            if (entityName.equals(DCRM_ENTITY_SPID))
            {
                guids[index] = ((Rkn_serviceprovider) businessEntities[index]).getRkn_serviceproviderid();
            }
            else if (entityName.equals(DCRM_ENTITY_ADJUSTMENT_CATEGORY))
            {
                guids[index] = ((Rkn_adjustmentcategory) businessEntities[index]).getRkn_adjustmentcategoryid();
            }
            else if (entityName.equals(DCRM_ENTITY_PAYMENT_AGENT))
            {
                guids[index] = ((Rkn_paymentagency) businessEntities[index]).getRkn_paymentagencyid();
            }
            else if (entityName.equals(DCRM_ENTITY_PAYMENT_METHOD_TYPE))
            {
                guids[index] = ((Rkn_paymentmethodtype) businessEntities[index]).getRkn_paymentmethodtypeid();
            }
            else if (entityName.equals(DCRM_ENTITY_REASON_CODE))
            {
                guids[index] = ((Rkn_reasoncode) businessEntities[index]).getRkn_reasoncodeid();
            }
            else if (entityName.equals(DCRM_ENTITY_ADJUSTMENT_TYPE))
            {
                guids[index] = ((Rkn_adjustmenttype) businessEntities[index]).getRkn_adjustmenttypeid();
            }
            else if (entityName.equals(DCRM_ENTITY_SYSTEM_USER))
            {
                guids[index] = ((Systemuser) businessEntities[index]).getSystemuserid();
            }
            else if ( entityName.equals(DCRM_ENTITY_TEAM))
            {
                guids[index] = ((Team) businessEntities[index]).getTeamid();                
            }
        }
        if (guids.length > 1)
        {
            final StringBuilder builder = new StringBuilder();
            for (final Guid guid : guids)
            {
                if (builder.length() != 0)
                {
                    builder.append(", ");
                }
                builder.append(guid.getGuid());
            }
            // new MajorLogMsg(TransactionsApiSupport.class,
            // "More than one entity with the same key!  GUIDs: " + builder,
            // null).log(ctx);
        }
        final Guid primaryGuid;
        if (guids.length > 0)
        {
            primaryGuid = guids[0];
        }
        else
        {
            primaryGuid = null;
        }
        return primaryGuid;
    }


    public static CrmDateTime adaptToCrmDateTime(final Context context, final Date date)
    {
        final CrmDateTime dateTime = new CrmDateTime();
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        dateTime.setString(ConverterUtil.convertToString(calendar));
        return dateTime;
    }



}
