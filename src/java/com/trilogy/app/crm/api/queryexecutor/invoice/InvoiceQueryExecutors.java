/*
� * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.api.queryexecutor.invoice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import com.trilogy.app.crm.api.queryexecutor.AbstractQueryExecutor;
import com.trilogy.app.crm.api.rmi.BillCycleToApiAdapter;
import com.trilogy.app.crm.api.rmi.GenericParameterParser;
import com.trilogy.app.crm.api.rmi.InvoiceDeliveryToApiAdapter;
import com.trilogy.app.crm.api.rmi.InvoiceToApiAdapter;
import com.trilogy.app.crm.api.rmi.InvoiceToSubAccountInvoiceApiAdapter;
import com.trilogy.app.crm.api.rmi.PaymentPlanToApiAdapter;
import com.trilogy.app.crm.api.rmi.impl.AccountsImpl;
import com.trilogy.app.crm.api.rmi.support.RmiApiErrorHandlingSupport;
import com.trilogy.app.crm.api.rmi.support.RmiApiSupport;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AlternateInvoice;
import com.trilogy.app.crm.bean.AlternateInvoiceHome;
import com.trilogy.app.crm.bean.AlternateInvoiceID;
import com.trilogy.app.crm.bean.BillCycleXInfo;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.InvoicePDFStateEnum;
import com.trilogy.app.crm.bean.InvoiceXInfo;
import com.trilogy.app.crm.bean.payment.PaymentPlanXInfo;
import com.trilogy.app.crm.invoice.service.InvoiceServerService;
import com.trilogy.app.crm.invoice.service.InvoiceServerServiceException;
import com.trilogy.app.crm.support.CollectionSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GTE;
import com.trilogy.framework.xhome.elang.LTE;
import com.trilogy.framework.xhome.elang.True;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.util.crmapi.wsdl.v2_0.exception.ValidationException;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v2_0.types.SuccessCode;
import com.trilogy.util.crmapi.wsdl.v2_0.types.SuccessCodeEnum;
import com.trilogy.util.crmapi.wsdl.v2_2.api.types.invoice.ListSubAccountInvoicesResponse;
import com.trilogy.util.crmapi.wsdl.v2_2.types.invoice.BillCycle;
import com.trilogy.util.crmapi.wsdl.v2_2.types.invoice.BillCycleReference;
import com.trilogy.util.crmapi.wsdl.v2_2.types.invoice.InvoiceDeliveryOption;
import com.trilogy.util.crmapi.wsdl.v2_2.types.invoice.InvoiceDetailReference;
import com.trilogy.util.crmapi.wsdl.v2_2.types.invoice.InvoiceReference;
import com.trilogy.util.crmapi.wsdl.v2_2.types.invoice.PaymentPlan;
import com.trilogy.util.crmapi.wsdl.v2_2.types.invoice.PaymentPlanReference;
import com.trilogy.util.crmapi.wsdl.v2_2.types.invoice.SubAccountInvoiceReference;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.types.CRMRequestHeader;
import com.trilogy.util.crmapi.wsdl.v3_0.types.invoice.ChargeableInvoiceDetails;
import com.trilogy.util.crmapi.wsdl.v3_0.types.invoice.Invoice;
import com.trilogy.util.crmapi.wsdl.v3_0.types.invoice.InvoicePDFState;

/**
 * 
 * @author Marcio Marques
 * @since 9.3
 *
 */
public class InvoiceQueryExecutors 
{
    
    private static final InvoiceToApiAdapter invoiceToApiAdapter_ = new InvoiceToApiAdapter();
    
    /**
     * CRM Invoice to Sub Account Api Adapter
     */
    private static final InvoiceToSubAccountInvoiceApiAdapter invoiceToSubAccountApiAdapter_ = new InvoiceToSubAccountInvoiceApiAdapter();
    
    /**
     * CRM bill cycle to API bill cycle adapter.
     */
    private static final BillCycleToApiAdapter billCycleToApiAdapter_ = new BillCycleToApiAdapter();
    
    /**
     * CRM payment to API payment adapter
     */
    private static final PaymentPlanToApiAdapter paymentToApiAdapter_ = new PaymentPlanToApiAdapter();
    
    private static final InvoiceDeliveryToApiAdapter invoiceDeliveryToApiAdatper_ = new InvoiceDeliveryToApiAdapter();

    /**
	 * 
	 * @author Marcio Marques
	 * @since 9.3.0
	 *
	 */
	public static class BillCycleQueryExecutor extends AbstractQueryExecutor<BillCycle>
	{
		public BillCycleQueryExecutor()
		{
			
		}

	    public BillCycle execute(Context ctx, Object... parameters) throws CRMExceptionFault
	    {
	        long billCycleID = getParameter(ctx, PARAM_BILL_CYCLE_ID, PARAM_BILL_CYCLE_ID_NAME, long.class, parameters);

            final com.redknee.app.crm.bean.BillCycle billCycle = RmiApiSupport.getCrmBillCycle(ctx, billCycleID, this);
            if (billCycle == null)
            {
                final String identifier = "Bill Cycle " + billCycleID;
                RmiApiErrorHandlingSupport.identificationException(ctx, identifier, this);
            }
            BillCycle result = null;
            try
            {
                result = BillCycleToApiAdapter.adaptBillCycleToApi(ctx, billCycle);
            }
            catch (Exception e)
            {
                final String msg = "Unable to retrieve Bill Cycle " + billCycleID;
                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
            }
            return result;
        }
	    
	    @Override
	    public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
	    {
	        Object[] result = null;
	        if (isGenericExecution(ctx, parameters))
	        {
	            result = new Object[3];
	            result[0] = parameters[0];
	            result[1] = getParameter(ctx, PARAM_BILL_CYCLE_ID, PARAM_BILL_CYCLE_ID_NAME, long.class, parameters);
                result[2] = getParameter(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, GenericParameter[].class, parameters);
	        }
	        else
	        {
	            result = parameters;
	        }
	        
	        return result;
	    }

	    @Override
	    public boolean validateParameterTypes(Class<?>[] parameterTypes)
	    {
	        boolean result = true;
	        result = result && (parameterTypes.length>=3);
	        result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
            result = result && long.class.isAssignableFrom(parameterTypes[PARAM_BILL_CYCLE_ID]);
            result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
	        return result;
	    }
	    
        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return BillCycle.class.isAssignableFrom(resultType);
        }


	    public static final int PARAM_HEADER = 0;
        public static final int PARAM_BILL_CYCLE_ID = 1;
        public static final int PARAM_GENERIC_PARAMETERS = 2;
	    
        public static final String PARAM_BILL_CYCLE_ID_NAME = "billCycleID";
        public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
	}
	
    /**
     * 
     * @author Marcio Marques
     * @since 9.3.0
     *
     */
    public static class BillCyclesListQueryExecutor extends AbstractQueryExecutor<BillCycleReference[]>
    {
        public BillCyclesListQueryExecutor()
        {
            
        }

        public BillCycleReference[] execute(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            int spid = getParameter(ctx, PARAM_SPID, PARAM_SPID_NAME, int.class, parameters);
            Boolean isAscending = getParameter(ctx, PARAM_IS_ASCENDING, PARAM_IS_ASCENDING_NAME, Boolean.class, parameters);

            RmiApiSupport.getCrmServiceProvider(ctx, spid, this);
            BillCycleReference[] billCycleReferences = new BillCycleReference[]
                {};
            try
            {
                final Object condition = new EQ(BillCycleXInfo.SPID, spid);
                Collection<com.redknee.app.crm.bean.BillCycle> collection = HomeSupportHelper.get(ctx).getBeans(ctx,
                        com.redknee.app.crm.bean.BillCycle.class, condition, RmiApiSupport.isSortAscending(isAscending));
                billCycleReferences = CollectionSupportHelper.get(ctx).adaptCollection(ctx, collection,
                        billCycleToApiAdapter_, billCycleReferences);
            }
            catch (final Exception e)
            {
                final String msg = "Unable to retrieve Bill Cycles";
                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
            }
            return billCycleReferences;
            
        }
        
        @Override
       public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
       {
           Object[] result = null;
           if (isGenericExecution(ctx, parameters))
           {
               result = new Object[4];
               result[0] = parameters[0];
               result[1] = getParameter(ctx, PARAM_SPID, PARAM_SPID_NAME, int.class, parameters);
               result[2] = getParameter(ctx, PARAM_IS_ASCENDING, PARAM_IS_ASCENDING_NAME, Boolean.class, parameters);
               result[3] = getParameter(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, GenericParameter[].class, parameters);
           }
           else
           {
               result = parameters;
           }
           
           return result;
       }

       @Override
       public boolean validateParameterTypes(Class<?>[] parameterTypes)
       {
           boolean result = true;
           result = result && (parameterTypes.length>=4);
           result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
           result = result && int.class.isAssignableFrom(parameterTypes[PARAM_SPID]);
           result = result && Boolean.class.isAssignableFrom(parameterTypes[PARAM_IS_ASCENDING]);
           result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
           return result;
       }
       
       @Override
       public boolean validateReturnType(Class<?> resultType)
       {
           return BillCycleReference[].class.isAssignableFrom(resultType);
       }


       public static final int PARAM_HEADER = 0;
       public static final int PARAM_SPID = 1;
       public static final int PARAM_IS_ASCENDING = 2;
       public static final int PARAM_GENERIC_PARAMETERS = 3;
       
       public static final String PARAM_SPID_NAME = "spid";
       public static final String PARAM_IS_ASCENDING_NAME = "isAscending";
       public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
    }
    
    /**
     * 
     * @author Marcio Marques
     * @since 9.3.0
     *
     */
    public static class InvoiceQueryExecutor extends AbstractQueryExecutor<Invoice>
    {
        public InvoiceQueryExecutor()
        {
            
        }

        public Invoice execute(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            CRMRequestHeader header = getParameter(ctx, PARAM_HEADER, "header", CRMRequestHeader.class, parameters);
            String accountID = getParameter(ctx, PARAM_ACCOUNT_ID, PARAM_ACCOUNT_ID_NAME, String.class, parameters);
            String invoiceID = getParameter(ctx, PARAM_INVOICE_ID, PARAM_INVOICE_ID_NAME, String.class, parameters);
            
            RmiApiErrorHandlingSupport.validateMandatoryObject(accountID, PARAM_ACCOUNT_ID_NAME);
            RmiApiErrorHandlingSupport.validateMandatoryObject(invoiceID, PARAM_INVOICE_ID_NAME);

            AccountsImpl.getCrmAccount(ctx, accountID, this);
            com.redknee.app.crm.bean.Invoice invoice = null;
            try
            {
                final And condition = new And();
                condition.add(new EQ(InvoiceXInfo.BAN, accountID));
                condition.add(new EQ(InvoiceXInfo.INVOICE_ID, invoiceID));
                invoice = HomeSupportHelper.get(ctx).findBean(ctx, com.redknee.app.crm.bean.Invoice.class, condition);
            }
            catch (final Exception e)
            {
                final String msg = "Unable to retrieve Invoice " + invoiceID + " for BAN " + accountID;
                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
            }
            if (invoice == null)
            {
                final String identifier = "Invoice " + invoiceID + " for BAN " + accountID;
                RmiApiErrorHandlingSupport.identificationException(ctx, identifier, this);
            }
            Invoice result = null;
            result = InvoiceToApiAdapter.adaptInvoiceToApi(invoice);        
            ChargeableInvoiceDetails chargeableInvoice = InvoiceQueryExecutors.getChargeableInvoiceDetails(ctx, header, accountID, invoiceID, result.getInvoiceDate().getTime(), this);
            if (chargeableInvoice!=null)
            {
                result.setAlternateDetails(chargeableInvoice);
            }
            return result;

        }
        
        @Override
        public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            Object[] result = null;
            if (isGenericExecution(ctx, parameters))
            {
                result = new Object[4];
                result[0] = parameters[0];
                result[1] = getParameter(ctx, PARAM_ACCOUNT_ID, PARAM_ACCOUNT_ID_NAME, String.class, parameters);
                result[2] = getParameter(ctx, PARAM_INVOICE_ID, PARAM_INVOICE_ID_NAME, String.class, parameters);
                result[3] = getParameter(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, GenericParameter[].class, parameters);
            }
            else
            {
                result = parameters;
            }
            
            return result;
        }

        @Override
        public boolean validateParameterTypes(Class<?>[] parameterTypes)
        {
            boolean result = true;
            result = result && (parameterTypes.length>=4);
            result = result && String.class.isAssignableFrom(parameterTypes[PARAM_ACCOUNT_ID]);
            result = result && String.class.isAssignableFrom(parameterTypes[PARAM_INVOICE_ID]);
            result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
            return result;
        }
        
        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return Invoice.class.isAssignableFrom(resultType);
        }


        public static final int PARAM_HEADER = 0;
        public static final int PARAM_ACCOUNT_ID = 1;
        public static final int PARAM_INVOICE_ID = 2;
        public static final int PARAM_GENERIC_PARAMETERS = 3;
        
        public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
        public static final String PARAM_ACCOUNT_ID_NAME = "accountID";
        public static final String PARAM_INVOICE_ID_NAME = "invoiceID";
    }
    
    /**
     * 
     * @author Marcio Marques
     * @since 9.3.0
     *
     */
    public static class InvoicesListQueryExecutor extends AbstractQueryExecutor<InvoiceReference[]>
    {
        public InvoicesListQueryExecutor()
        {
            
        }

        public InvoiceReference[] execute(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            CRMRequestHeader header = getParameter(ctx, PARAM_HEADER, "header", CRMRequestHeader.class, parameters);
            String accountID = getParameter(ctx, PARAM_ACCOUNT_ID, PARAM_ACCOUNT_ID_NAME, String.class, parameters);
            Boolean isAscending = getParameter(ctx, PARAM_IS_ASCENDING, PARAM_IS_ASCENDING_NAME, Boolean.class, parameters);
            GenericParameter[] genericParameters = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);

            RmiApiErrorHandlingSupport.validateMandatoryObject(accountID, "accountID");
            AccountsImpl.getCrmAccount(ctx, accountID, this);
            InvoiceReference[] invoiceReferences = new InvoiceReference[]
                {};

            InvoiceToApiAdapter invoiceToApiAdapter = invoiceToApiAdapter_;
            
            GenericParameterParser parser = new GenericParameterParser(genericParameters);
            Boolean returnInvoices = parser.getParameter(RETURN_INVOICES, Boolean.class);
            Integer numberOfInvoices = parser.getParameter(NUMBER_OF_INVOICES, Integer.class);
            
            
            if (returnInvoices!=null && returnInvoices)
            {
                invoiceToApiAdapter = new InvoiceToApiAdapter(){
                    private static final long serialVersionUID = 1L;
                    @Override
                    public Object adapt(final Context ctx, final Object obj) throws HomeException
                    {
                        return adaptInvoiceToApi(
                            (com.redknee.app.crm.bean.Invoice) obj);
                    }
                };
                invoiceReferences = new Invoice[]
                        {};
            }

            
            try
            {
                final Object condition = new EQ(InvoiceXInfo.BAN, accountID);
                // Sort by invoice date
                Collection<com.redknee.app.crm.bean.Invoice> collection = HomeSupportHelper.get(ctx).getBeans(ctx,
                        com.redknee.app.crm.bean.Invoice.class, condition, RmiApiSupport.isSortAscending(isAscending),
                        new PropertyInfo[]
                            {InvoiceXInfo.INVOICE_DATE});
                Collection<com.redknee.app.crm.bean.Invoice> invoicesList = new ArrayList<com.redknee.app.crm.bean.Invoice>();                
                if (collection != null && numberOfInvoices != null && collection.size() > numberOfInvoices)
                {
                    if(RmiApiSupport.isSortAscending(isAscending))
                    {
                        int i = 0;
                        int noOfInvoicesToFilter = collection.size() - numberOfInvoices;
                        for (com.redknee.app.crm.bean.Invoice invoice : collection)
                        {
                            i++;
                            if (i <= noOfInvoicesToFilter)
                            {
                                continue;
                            }
                            invoicesList.add(invoice);                            
                        }
                    }
                    else
                    {
                        int i = 0;
                        for (com.redknee.app.crm.bean.Invoice invoice : collection)
                        {
                            if (i == numberOfInvoices)
                            {
                                break;
                            }
                            invoicesList.add(invoice);
                            i++;
                        }
                    }
                }
                else
                {
                    invoicesList = collection;
                }
                invoiceReferences = CollectionSupportHelper.get(ctx).adaptCollection(ctx, invoicesList,
                        invoiceToApiAdapter, invoiceReferences);
            }
            catch (final Exception e)
            {
                final String msg = "Unable to retrieve Invoices";
                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
            }
            
            if (returnInvoices!=null && returnInvoices)
            {
                for (Invoice invoice : (Invoice[]) invoiceReferences)
                {
                    ChargeableInvoiceDetails chargeableInvoice = getChargeableInvoiceDetails(ctx, header, accountID, invoice.getInvoiceID(), invoice.getInvoiceDate().getTime(), this);
                    if (chargeableInvoice!=null)
                    {
                        invoice.setAlternateDetails(chargeableInvoice);
                    }
                }
            }
            
            return invoiceReferences;
        }
        
        @Override
       public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
       {
           Object[] result = null;
           if (isGenericExecution(ctx, parameters))
           {
               result = new Object[4];
               result[0] = parameters[0];
               result[1] = getParameter(ctx, PARAM_ACCOUNT_ID, PARAM_ACCOUNT_ID_NAME, String.class, parameters);
               result[2] = getParameter(ctx, PARAM_IS_ASCENDING, PARAM_IS_ASCENDING_NAME, Boolean.class, parameters);
               result[3] = getParameter(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, GenericParameter[].class, parameters);
           }
           else
           {
               result = parameters;
           }
           
           return result;
       }

       @Override
       public boolean validateParameterTypes(Class<?>[] parameterTypes)
       {
           boolean result = true;
           result = result && (parameterTypes.length>=4);
           result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
           result = result && String.class.isAssignableFrom(parameterTypes[PARAM_ACCOUNT_ID]);
           result = result && Boolean.class.isAssignableFrom(parameterTypes[PARAM_IS_ASCENDING]);
           result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
           return result;
       }
       
       @Override
       public boolean validateReturnType(Class<?> resultType)
       {
           return InvoiceReference[].class.isAssignableFrom(resultType);
       }


       public static final String RETURN_INVOICES = "ReturnInvoices";
       public static final String NUMBER_OF_INVOICES = "NumberOfInvoices";

       public static final int PARAM_HEADER = 0;
       public static final int PARAM_ACCOUNT_ID = 1;
       public static final int PARAM_IS_ASCENDING = 2;
       public static final int PARAM_GENERIC_PARAMETERS = 3;
       
       public static final String PARAM_ACCOUNT_ID_NAME = "accountID";
       public static final String PARAM_IS_ASCENDING_NAME = "isAscending";
       public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
    }
    
    /**
     * 
     * @author Nilay Oza
     * 
     *
     */
    public static class SubAccountInvoicesListQueryExecutor extends /*AbstractQueryExecutor<SubAccountInvoiceReference[]>*/
    AbstractQueryExecutor<ListSubAccountInvoicesResponse>
    {
        public SubAccountInvoicesListQueryExecutor()
        {
            
        }

        public ListSubAccountInvoicesResponse execute(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            CRMRequestHeader header = getParameter(ctx, SubAccountInvoicesEnum.SUB_ACCOUNT_PARAM_HEADER.ordinal(), SubAccountInvoicesEnum.SUB_ACCOUNT_PARAM_HEADER.getPName(), CRMRequestHeader.class, parameters);
            String accountID = getParameter(ctx, SubAccountInvoicesEnum.SUB_ACCOUNT_PARAM_ACCOUNT_ID.ordinal(), SubAccountInvoicesEnum.SUB_ACCOUNT_PARAM_ACCOUNT_ID.getPName(), String.class, parameters);
            Boolean recurse = getParameter(ctx, SubAccountInvoicesEnum.SUB_ACCOUNT_PARAM_RECURSE.ordinal() , SubAccountInvoicesEnum.SUB_ACCOUNT_PARAM_RECURSE.getPName(), Boolean.class, parameters);
            Calendar start = getParameter(ctx, SubAccountInvoicesEnum.SUB_ACCOUNT_PARAM_START_DATE.ordinal() , SubAccountInvoicesEnum.SUB_ACCOUNT_PARAM_START_DATE.getPName(), Calendar.class, parameters);
            Calendar end = getParameter(ctx, SubAccountInvoicesEnum.SUB_ACCOUNT_PARAM_END_DATE.ordinal() , SubAccountInvoicesEnum.SUB_ACCOUNT_PARAM_END_DATE.getPName(), Calendar.class, parameters);
            String pagekey = getParameter(ctx, SubAccountInvoicesEnum.SUB_ACCOUNT_PARAM_PAGEKEY.ordinal(), SubAccountInvoicesEnum.SUB_ACCOUNT_PARAM_PAGEKEY.getPName(), String.class, parameters);
            Integer invoices = getParameter(ctx, SubAccountInvoicesEnum.SUB_ACCOUNT_PARAM_INVOICES.ordinal() , SubAccountInvoicesEnum.SUB_ACCOUNT_PARAM_INVOICES.getPName(), Integer.class, parameters);
            Integer limit = getParameter(ctx, SubAccountInvoicesEnum.SUB_ACCOUNT_PARAM_LIMIT.ordinal() , SubAccountInvoicesEnum.SUB_ACCOUNT_PARAM_LIMIT.getPName(), Integer.class, parameters);
            Boolean isAscending = getParameter(ctx, SubAccountInvoicesEnum.SUB_ACCOUNT_PARAM_IS_ASCENDING.ordinal() , SubAccountInvoicesEnum.SUB_ACCOUNT_PARAM_IS_ASCENDING.getPName(), Boolean.class, parameters);
            GenericParameter[] genericParameters = getGenericParameters(ctx, SubAccountInvoicesEnum.SUB_ACCOUNT_PARAM_GENERIC_PARAMETERS.ordinal(), SubAccountInvoicesEnum.SUB_ACCOUNT_PARAM_GENERIC_PARAMETERS.getPName(), parameters);

//            Boolean recurse = true;
            if (recurse == null)
            {
            	recurse = true;
            }
            
            if (isAscending == null)
            {
            	isAscending = true;
            }
            
            
            RmiApiErrorHandlingSupport.validateMandatoryObject(accountID, SubAccountInvoicesEnum.SUB_ACCOUNT_PARAM_ACCOUNT_ID.getPName());
            
            if (accountID != null && accountID.trim().length() == 0)
            {
            	final String msg = "accountID field is empty, please provide valid value";
            	RmiApiErrorHandlingSupport.generalException(ctx, null, msg, this);
            }
            
//            RmiApiErrorHandlingSupport.validateMandatoryObject(invoces, SubAccountInvoicesEnum.SUB_ACCOUNT_PARAM_INVOICES.getPName());
            RmiApiErrorHandlingSupport.validateMandatoryObject(limit, SubAccountInvoicesEnum.SUB_ACCOUNT_PARAM_LIMIT.getPName());
//            AccountsImpl.getCrmAccount(ctx, accountID, this);
            
            
            RmiApiErrorHandlingSupport.validateLimitInput(ctx, limit, Integer.MAX_VALUE);
            RmiApiErrorHandlingSupport.validateLongPageKey(ctx, pagekey);
            boolean invoicesFault = false;
            boolean limitFault = false;
            
            if (limit != null && limit.intValue() <= 0)
            {
            	limitFault = true;
            	final String msg = "Given limit value is invalid, please give non-zero positive value for limit";
                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, null, msg, this);

            }
            if (invoices != null && invoices.intValue() <= 0)
            {
            	invoicesFault = true;
            	final String msg = "Value for invoices is invalid, please give non-zero positive value for invoices";
                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, null, msg, this);
            }
            
            
            SubAccountInvoiceReference[] subAccountInvoiceReference = new SubAccountInvoiceReference[]
                {};
            
            ListSubAccountInvoicesResponse result = new ListSubAccountInvoicesResponse();


            InvoiceToSubAccountInvoiceApiAdapter invoiceToApiAdapter = invoiceToSubAccountApiAdapter_;
            
            GenericParameterParser parser = new GenericParameterParser(genericParameters);
//            Boolean returnInvoices = parser.getParameter(RETURN_INVOICES, Boolean.class);
//            Integer numberOfInvoices = parser.getParameter(NUMBER_OF_INVOICES, Integer.class);
            

            Account acc = AccountsImpl.getCrmAccount(ctx, accountID, this);
            Collection<SortedAccount> accCollection = new LinkedList<SortedAccount>();
            
            if (acc != null)
            {
            	if (acc.isResponsible())
            		accCollection.add(new SortedAccount(acc));

            	if (recurse)
            	{
            		final CRMSpid spid = RmiApiSupport.getCrmServiceProvider(ctx, acc.getSpid(), this);
            		if (limit > spid.getMaxGetLimit())
            		{
            			limit = spid.getMaxGetLimit();
            		}
            		
            		boolean isAccAscend = true;

            		Collection<Account> subaccCollection = AccountsImpl.getCrmSubAccounts(ctx, acc, recurse, false, isAccAscend, genericParameters);

            		for (Account subAcc : subaccCollection)
            		{
            			
//            			subAcc.ID();
            			if (subAcc.isResponsible())
            			{
            				accCollection.add(new SortedAccount(subAcc));
            			}

            		}

            	}
            	
            }
            else
            {
            	final String msg = "Account with given accountID does not exist, please provide correct accountId";
                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, null, msg, this);
            }
            
            try
            {
            	Collection<SubAccountInvoiceReference> subAccountInvoiceList = new ArrayList<SubAccountInvoiceReference>();
            	
            	if (accCollection.size() == 0)
            	{
            		final String msg = "Given accountID is not responsible and it doesn't have any responsible sub account";
            		RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, null, msg, this);
            	}
            	else
            	{
            		Collections.sort((List<SortedAccount>)accCollection);
            	}
            	
            	
            	
        		int accountCounter = 0;

            	for (SortedAccount account : accCollection)
            	{
            		
                    InvoiceDetailReference[] invoiceDetailReference = new InvoiceDetailReference[] {};
                    
        			if (pagekey != null && Long.parseLong(account.getAcc().getBAN()) <= Long.parseLong(pagekey))
        				continue;
        			
        			  if (accountCounter >= limit)
        			  {
          				break;
        			  }

            		
            		final Object condition1 = new EQ(InvoiceXInfo.BAN, account.getAcc().getBAN());
            		And condition = new And();
            		condition.add(condition1);
            		
            		if (end != null)
            		{
            			java.util.Date endDate = new Date (end.getTimeInMillis());
            			condition.add(new LTE(InvoiceXInfo.INVOICE_DATE, endDate));
            		}
            		
            		if (start != null)
            		{
            			java.util.Date startDate = new Date (start.getTimeInMillis());
            			condition.add(new GTE(InvoiceXInfo.INVOICE_DATE, startDate));
            		}
            		
            		// Sort by invoice date
            		Collection<com.redknee.app.crm.bean.Invoice> collection = HomeSupportHelper.get(ctx).getBeans(ctx,
            				com.redknee.app.crm.bean.Invoice.class, condition, RmiApiSupport.isSortAscending(isAscending),
            				new PropertyInfo[]
            						{InvoiceXInfo.INVOICE_DATE});
            		Collection<com.redknee.app.crm.bean.Invoice> invoicesList = new ArrayList<com.redknee.app.crm.bean.Invoice>();

            		if (collection != null && invoices != null && collection.size() > invoices)
            		{
            			if(RmiApiSupport.isSortAscending(isAscending))
            			{
            				int i = 0;
            				int noOfInvoicesToFilter = collection.size() - invoices;
            				for (com.redknee.app.crm.bean.Invoice invoice : collection)
            				{
            					i++;
            					if (i <= noOfInvoicesToFilter)
            					{
            						continue;
            					}
            					invoicesList.add(invoice);                            
            				}
            			}
            			else
            			{
            				int i = 0;
            				for (com.redknee.app.crm.bean.Invoice invoice : collection)
            				{
            					if (i == invoices)
            					{
            						break;
            					}
            					invoicesList.add(invoice);
            					i++;
            				}
            			}
            		}
            		else
            		{
            			invoicesList = collection;
            		}
            		
/*            		if (accountCounter > 0 && invoicesList.size() < 0)
            		{
            			final String msg = "No invoices found for given accountId or it's responsible sub-accounts";
            			RmiApiErrorHandlingSupport.generalException(ctx, null, msg, this);
            		}
*/            		
            		if (invoicesList != null && invoicesList.size() > 0)
            		{
            			accountCounter++;
            			invoiceDetailReference = CollectionSupportHelper.get(ctx).adaptCollection(ctx, invoicesList,
            					invoiceToApiAdapter, invoiceDetailReference);

            			SubAccountInvoiceReference subAccountInvoiceRef = new SubAccountInvoiceReference();
            			subAccountInvoiceRef.setBAN(account.getAcc().getBAN());
            			subAccountInvoiceRef.setACCOUNTNAME(account.getAcc().getAccountName() == null ? "" : account.getAcc().getAccountName());
            			subAccountInvoiceRef.setFIRSTNAME(account.getAcc().getFirstName() == null ? "" : account.getAcc().getFirstName());
            			subAccountInvoiceRef.setLASTNAME(account.getAcc().getLastName());
            			subAccountInvoiceRef.setInvoice(invoiceDetailReference);

            			subAccountInvoiceList.add(subAccountInvoiceRef);
            		}

            	}// End of for loop
            	
            	subAccountInvoiceReference = subAccountInvoiceList.toArray(subAccountInvoiceReference) ;
            	if (subAccountInvoiceReference != null && subAccountInvoiceReference.length > 0)
            	{
            		String lastBANasPageKey = subAccountInvoiceReference[subAccountInvoiceReference.length -1].getBAN();
            		result.setPageKey(lastBANasPageKey);
            	}
            	else
            	{
        			final String msg = "No invoices found for given accountId or it's responsible sub-accounts";
        			RmiApiErrorHandlingSupport.generalException(ctx, null, msg, this);
            		
            	}
            	result.setAccountInvoices(subAccountInvoiceReference);
            	
//            	result.setPageKey(pagekey);
            }
            catch (final Exception e)
            {
                final String msg = "Unable to retrieve Invoices";
                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
            }
//            return subAccountInvoiceReference;
            return result;
        }
        
        @Override
       public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
       {
           Object[] result = null;
           if (isGenericExecution(ctx, parameters))
           {
               result = new Object[SubAccountInvoicesEnum.values().length];
               result[0] = parameters[0];
               result[1] = getParameter(ctx, SubAccountInvoicesEnum.SUB_ACCOUNT_PARAM_ACCOUNT_ID.ordinal(), SubAccountInvoicesEnum.SUB_ACCOUNT_PARAM_ACCOUNT_ID.getPName(), String.class, parameters);
               result[2] = getParameter(ctx, SubAccountInvoicesEnum.SUB_ACCOUNT_PARAM_RECURSE.ordinal() , SubAccountInvoicesEnum.SUB_ACCOUNT_PARAM_RECURSE.getPName(), Boolean.class, parameters);
               result[3] = getParameter(ctx, SubAccountInvoicesEnum.SUB_ACCOUNT_PARAM_START_DATE.ordinal() , SubAccountInvoicesEnum.SUB_ACCOUNT_PARAM_START_DATE.getPName(), Calendar.class, parameters);
               result[4] = getParameter(ctx, SubAccountInvoicesEnum.SUB_ACCOUNT_PARAM_END_DATE.ordinal() , SubAccountInvoicesEnum.SUB_ACCOUNT_PARAM_END_DATE.getPName(), Calendar.class, parameters);
               result[5] = getParameter(ctx, SubAccountInvoicesEnum.SUB_ACCOUNT_PARAM_PAGEKEY.ordinal(), SubAccountInvoicesEnum.SUB_ACCOUNT_PARAM_PAGEKEY.getPName(), String.class, parameters);
               result[6] = getParameter(ctx, SubAccountInvoicesEnum.SUB_ACCOUNT_PARAM_INVOICES.ordinal() , SubAccountInvoicesEnum.SUB_ACCOUNT_PARAM_INVOICES.getPName(), Integer.class, parameters);
               result[7] = getParameter(ctx, SubAccountInvoicesEnum.SUB_ACCOUNT_PARAM_LIMIT.ordinal() , SubAccountInvoicesEnum.SUB_ACCOUNT_PARAM_LIMIT.getPName(), Integer.class, parameters);
               result[8] = getParameter(ctx, SubAccountInvoicesEnum.SUB_ACCOUNT_PARAM_IS_ASCENDING.ordinal() , SubAccountInvoicesEnum.SUB_ACCOUNT_PARAM_IS_ASCENDING.getPName(), Boolean.class, parameters);
               result[9] = getGenericParameters(ctx, SubAccountInvoicesEnum.SUB_ACCOUNT_PARAM_GENERIC_PARAMETERS.ordinal(), SubAccountInvoicesEnum.SUB_ACCOUNT_PARAM_GENERIC_PARAMETERS.getPName(), parameters);

           }
           else
           {
               result = parameters;
           }
           
           return result;
       }

       @Override
       public boolean validateParameterTypes(Class<?>[] parameterTypes)
       {
           boolean result = true;
           result = result && (parameterTypes.length>=SubAccountInvoicesEnum.values().length);
           result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[SubAccountInvoicesEnum.SUB_ACCOUNT_PARAM_HEADER.ordinal()]);
           result = result && String.class.isAssignableFrom(parameterTypes[SubAccountInvoicesEnum.SUB_ACCOUNT_PARAM_ACCOUNT_ID.ordinal()]);
           result = result && Boolean.class.isAssignableFrom(parameterTypes[SubAccountInvoicesEnum.SUB_ACCOUNT_PARAM_RECURSE.ordinal()]);
           result = result && Calendar.class.isAssignableFrom(parameterTypes[SubAccountInvoicesEnum.SUB_ACCOUNT_PARAM_START_DATE.ordinal()]);
           result = result && Calendar.class.isAssignableFrom(parameterTypes[SubAccountInvoicesEnum.SUB_ACCOUNT_PARAM_END_DATE.ordinal()]);
           result = result && String.class.isAssignableFrom(parameterTypes[SubAccountInvoicesEnum.SUB_ACCOUNT_PARAM_PAGEKEY.ordinal()]);
           result = result && Integer.class.isAssignableFrom(parameterTypes[SubAccountInvoicesEnum.SUB_ACCOUNT_PARAM_INVOICES.ordinal()]);
           result = result && Integer.class.isAssignableFrom(parameterTypes[SubAccountInvoicesEnum.SUB_ACCOUNT_PARAM_LIMIT.ordinal()]);
           result = result && Boolean.class.isAssignableFrom(parameterTypes[SubAccountInvoicesEnum.SUB_ACCOUNT_PARAM_IS_ASCENDING.ordinal()]);
           result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[SubAccountInvoicesEnum.SUB_ACCOUNT_PARAM_GENERIC_PARAMETERS.ordinal()]);
           return result;
       }
       
       @Override
       public boolean validateReturnType(Class<?> resultType)
       {
           return InvoiceReference[].class.isAssignableFrom(resultType);
       }

       enum SubAccountInvoicesEnum {
    	   
    	   SUB_ACCOUNT_PARAM_HEADER("header"),
    	   SUB_ACCOUNT_PARAM_ACCOUNT_ID("accountID"),
    	   SUB_ACCOUNT_PARAM_RECURSE("recurse"),
    	   SUB_ACCOUNT_PARAM_START_DATE("start"),
    	   SUB_ACCOUNT_PARAM_END_DATE("end"),
    	   SUB_ACCOUNT_PARAM_PAGEKEY("pagekey"),
    	   SUB_ACCOUNT_PARAM_INVOICES("invoices"),
    	   SUB_ACCOUNT_PARAM_LIMIT("limit"),
    	   SUB_ACCOUNT_PARAM_IS_ASCENDING("isAscending"),
    	   SUB_ACCOUNT_PARAM_GENERIC_PARAMETERS("parameters");
    	   
    	   private final String pName;
    	   
    	   private SubAccountInvoicesEnum(String paramName)
    	   {
    		   pName = paramName;
    	   }
    	   
    	   public String getPName(){
    		   return pName;
    	   }
       }

       public static final String RETURN_INVOICES = "ReturnInvoices";
       public static final String NUMBER_OF_INVOICES = "NumberOfInvoices";

//       public static final int PARAM_HEADER = 0;
//       public static final int PARAM_ACCOUNT_ID = 1;
//       public static final int PARAM_IS_ASCENDING = 2;
//       public static final int PARAM_GENERIC_PARAMETERS = 3;
//       
//       public static final String PARAM_ACCOUNT_ID_NAME = "accountID";
//       public static final String PARAM_IS_ASCENDING_NAME = "isAscending";
//       public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
       
       class SortedAccount implements Comparable<SortedAccount>
       {
    	   
    	   private Account acc;
    	   public Account getAcc() {
			return acc;
		}


		public Date getCreationDate() {
			return creationDate;
		}


		private Date creationDate;
    	   
    	   SortedAccount(Account accRef)
    	   {
    		   this.acc = accRef;
    		   this.creationDate = accRef.getCreationDate();
    	   }
    	   

    	   @Override
    	   public int compareTo(SortedAccount arg0) {
    		   Date anotherVal = arg0.getCreationDate();
    		   return (this.creationDate.before(anotherVal) ? -1 : (this.creationDate.equals(anotherVal) ? 0 : 1));
    		   
    	   }

       }
    }
    
    /**
     * 
     * @author Marcio Marques
     * @since 9.3.0
     *
     */
    public static class PaymentPlanQueryExecutor extends AbstractQueryExecutor<PaymentPlan>
    {
        public PaymentPlanQueryExecutor()
        {
            
        }

        public PaymentPlan execute(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            int paymentPlanID = getParameter(ctx, PARAM_PAYMENT_PLAN_ID, PARAM_PAYMENT_PLAN_ID_NAME, int.class, parameters);

            com.redknee.app.crm.bean.payment.PaymentPlan plan = null;
            try
            {
                plan = HomeSupportHelper.get(ctx).findBean(ctx, com.redknee.app.crm.bean.payment.PaymentPlan.class,
                        Long.valueOf(paymentPlanID));
            }
            catch (final Exception e)
            {
                final String msg = "Unable to retrieve PaymentPlan " + paymentPlanID;
                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
            }
            if (plan == null)
            {
                final String identifier = "PaymentPlan " + paymentPlanID;
                RmiApiErrorHandlingSupport.identificationException(ctx, identifier, this);
            }
            final PaymentPlan result;
            result = PaymentPlanToApiAdapter.adaptPaymentPlanToApi(plan);
            return result;        }
        
        @Override
        public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            Object[] result = null;
            if (isGenericExecution(ctx, parameters))
            {
                result = new Object[3];
                result[0] = parameters[0];
                result[1] = getParameter(ctx, PARAM_PAYMENT_PLAN_ID, PARAM_PAYMENT_PLAN_ID_NAME, int.class, parameters);
                result[2] = getParameter(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, GenericParameter[].class, parameters);
            }
            else
            {
                result = parameters;
            }
            
            return result;
        }

        @Override
        public boolean validateParameterTypes(Class<?>[] parameterTypes)
        {
            boolean result = true;
            result = result && (parameterTypes.length>=3);
            result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
            result = result && int.class.isAssignableFrom(parameterTypes[PARAM_PAYMENT_PLAN_ID]);
            result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
            return result;
        }
        
        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return PaymentPlan.class.isAssignableFrom(resultType);
        }


        public static final int PARAM_HEADER = 0;
        public static final int PARAM_PAYMENT_PLAN_ID = 1;
        public static final int PARAM_GENERIC_PARAMETERS = 2;
        
        public static final String PARAM_PAYMENT_PLAN_ID_NAME = "paymentPlanID";
        public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
    }
    
    /**
     * 
     * @author Marcio Marques
     * @since 9.3.0
     *
     */
    public static class PaymentPlansListQueryExecutor extends AbstractQueryExecutor<PaymentPlanReference[]>
    {
        public PaymentPlansListQueryExecutor()
        {
            
        }

        public PaymentPlanReference[] execute(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            int spid = getParameter(ctx, PARAM_SPID, PARAM_SPID_NAME, int.class, parameters);
            Boolean isAscending = getParameter(ctx, PARAM_IS_ASCENDING, PARAM_IS_ASCENDING_NAME, Boolean.class, parameters);
            
            PaymentPlanReference[] paymentPlanReferences = new PaymentPlanReference[]
                    {};
            
            try
            {
                final Object condition = new EQ(PaymentPlanXInfo.SPID, spid);
                Collection<com.redknee.app.crm.bean.payment.PaymentPlan> collection = HomeSupportHelper.get(ctx)
                        .getBeans(ctx, com.redknee.app.crm.bean.payment.PaymentPlan.class, condition,
                                RmiApiSupport.isSortAscending(isAscending));
                paymentPlanReferences = CollectionSupportHelper.get(ctx).adaptCollection(ctx, collection,
                        paymentToApiAdapter_, paymentPlanReferences);
            }
            catch (final Exception e)
            {
                final String msg = "Unable to retrieve PaymentPlans";
                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
            }
            
            return paymentPlanReferences;

        }
        
        @Override
        public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            Object[] result = null;
            if (isGenericExecution(ctx, parameters))
            {
                result = new Object[4];
                result[0] = parameters[0];
                result[1] = getParameter(ctx, PARAM_SPID, PARAM_SPID_NAME, int.class, parameters);
                result[2] = getParameter(ctx, PARAM_IS_ASCENDING, PARAM_IS_ASCENDING_NAME, Boolean.class, parameters);
                result[3] = getParameter(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, GenericParameter[].class, parameters);
            }
            else
            {
                result = parameters;
            }
            
            return result;
        }

        @Override
        public boolean validateParameterTypes(Class<?>[] parameterTypes)
        {
            boolean result = true;
            result = result && (parameterTypes.length>=4);
            result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
            result = result && int.class.isAssignableFrom(parameterTypes[PARAM_SPID]);
            result = result && Boolean.class.isAssignableFrom(parameterTypes[PARAM_IS_ASCENDING]);
            result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
            return result;
        }
        
        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return PaymentPlanReference[].class.isAssignableFrom(resultType);
        }


        public static final int PARAM_HEADER = 0;
        public static final int PARAM_SPID = 1;
        public static final int PARAM_IS_ASCENDING = 2;
        public static final int PARAM_GENERIC_PARAMETERS = 3;
        
        public static final String PARAM_SPID_NAME = "spid";
        public static final String PARAM_IS_ASCENDING_NAME = "isAscending";
        public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
    }
    
    /**
     * 
     * @author Marcio Marques
     * @since 9.3.0
     *
     */
    public static class InvoiceDeliveryOptionsListQueryExecutor extends AbstractQueryExecutor<InvoiceDeliveryOption[]>
    {
        public InvoiceDeliveryOptionsListQueryExecutor()
        {
            
        }

        public InvoiceDeliveryOption[] execute(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            Boolean isAscending = getParameter(ctx, PARAM_IS_ASCENDING, PARAM_IS_ASCENDING_NAME, Boolean.class, parameters);
            
            InvoiceDeliveryOption[] apiInvoiceDeliveryOptions = new InvoiceDeliveryOption[]
                    {};
            
            try
            {
                // Sort by invoice date
                Collection<com.redknee.app.crm.invoice.delivery.InvoiceDeliveryOption> collection = HomeSupportHelper
                        .get(ctx).getBeans(ctx, com.redknee.app.crm.invoice.delivery.InvoiceDeliveryOption.class,
                                True.instance(), RmiApiSupport.isSortAscending(isAscending));
                apiInvoiceDeliveryOptions = CollectionSupportHelper.get(ctx).adaptCollection(ctx, collection,
                        invoiceDeliveryToApiAdatper_, apiInvoiceDeliveryOptions);
            }
            catch (final Exception e)
            {
                final String msg = "Unable to retrieve Invoice delivery options";
                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
            }
            
            return apiInvoiceDeliveryOptions;
        }
        
        @Override
        public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            Object[] result = null;
            if (isGenericExecution(ctx, parameters))
            {
                result = new Object[3];
                result[0] = parameters[0];
                result[1] = getParameter(ctx, PARAM_IS_ASCENDING, PARAM_IS_ASCENDING_NAME, Boolean.class, parameters);
                result[2] = getParameter(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, GenericParameter[].class, parameters);
            }
            else
            {
                result = parameters;
            }
            
            return result;
        }

        @Override
        public boolean validateParameterTypes(Class<?>[] parameterTypes)
        {
            boolean result = true;
            result = result && (parameterTypes.length>=3);
            result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
            result = result && Boolean.class.isAssignableFrom(parameterTypes[PARAM_IS_ASCENDING]);
            result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
            return result;
        }
        
        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return InvoiceDeliveryOption[].class.isAssignableFrom(resultType);
        }


        public static final int PARAM_HEADER = 0;
        public static final int PARAM_IS_ASCENDING = 1;
        public static final int PARAM_GENERIC_PARAMETERS = 2;
        
        public static final String PARAM_IS_ASCENDING_NAME = "isAscending";
        public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
    }
    
    /**
     * 
     * @author Marcio Marques
     * @since 9.3.0
     *
     */
    public static class AlternateInvoiceGenerationQueryExecutor extends AbstractQueryExecutor<SuccessCode>
    {
        public AlternateInvoiceGenerationQueryExecutor()
        {
            
        }

        public SuccessCode execute(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            String accountID = getParameter(ctx, PARAM_ACCOUNT_ID, PARAM_ACCOUNT_ID_NAME, String.class, parameters);
            String invoiceID = getParameter(ctx, PARAM_INVOICE_ID, PARAM_INVOICE_ID_NAME, String.class, parameters);
            
            AccountsImpl.getCrmAccount(ctx, accountID, this);
            com.redknee.app.crm.bean.Invoice invoice = null;
            try
            {
                final And condition = new And();
                condition.add(new EQ(InvoiceXInfo.BAN, accountID));
                condition.add(new EQ(InvoiceXInfo.INVOICE_ID, invoiceID));
                invoice = HomeSupportHelper.get(ctx).findBean(ctx, com.redknee.app.crm.bean.Invoice.class, condition);
            }
            catch (final Exception e)
            {
                final String msg = "Unable to retrieve Invoice " + invoiceID + " for BAN " + accountID;
                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
            }
            if (invoice == null)
            {
                final String identifier = "Invoice " + invoiceID + " for BAN " + accountID;
                RmiApiErrorHandlingSupport.identificationException(ctx, identifier, this);
            }
            InvoiceServerService service = (InvoiceServerService) ctx.get(InvoiceServerService.class);
            if(service == null)
            {
                final String identifier = "InvoiceServerService";
                RmiApiErrorHandlingSupport.identificationException(ctx, identifier, AlternateInvoiceGenerationQueryExecutor.class);
            }
            try
            {
                service.generateAlternateInvoice(accountID, invoiceID);
            }        
            catch (InvoiceServerServiceException e)
            {
                final String msg = "Unable to generate Alternate Invoice for " + accountID + ", Invoice Id : " + invoiceID;
                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, AlternateInvoiceGenerationQueryExecutor.class);
            }        
            return SuccessCodeEnum.SUCCESS.getValue();

        }
        
        @Override
        public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            Object[] result = null;
            if (isGenericExecution(ctx, parameters))
            {
                result = new Object[3];
                result[0] = parameters[0];
                result[1] = getParameter(ctx, PARAM_ACCOUNT_ID, PARAM_ACCOUNT_ID_NAME, String.class, parameters);
                result[2] = getParameter(ctx, PARAM_INVOICE_ID, PARAM_INVOICE_ID_NAME, String.class, parameters);
            }
            else
            {
                result = parameters;
            }
            
            return result;
        }

        @Override
        public boolean validateParameterTypes(Class<?>[] parameterTypes)
        {
            boolean result = true;
            result = result && (parameterTypes.length>=3);
            result = result && String.class.isAssignableFrom(parameterTypes[PARAM_ACCOUNT_ID]);
            result = result && String.class.isAssignableFrom(parameterTypes[PARAM_INVOICE_ID]);
            return result;
        }
        
        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return SuccessCode.class.isAssignableFrom(resultType);
        }


        public static final int PARAM_HEADER = 0;
        public static final int PARAM_ACCOUNT_ID = 1;
        public static final int PARAM_INVOICE_ID = 2;
        
        public static final String PARAM_ACCOUNT_ID_NAME = "accountID";
        public static final String PARAM_INVOICE_ID_NAME = "invoiceID";
    }
    
    /**
     * 
     * @author Marcio Marques
     * @since 9.3.0
     *
     */
    public static class AlternateInvoiceChargesApplierQueryExecutor extends AbstractQueryExecutor<Invoice>
    {
        public AlternateInvoiceChargesApplierQueryExecutor()
        {
            
        }

        public Invoice execute(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            CRMRequestHeader header = getParameter(ctx, PARAM_HEADER, "header", CRMRequestHeader.class, parameters);
            String accountID = getParameter(ctx, PARAM_ACCOUNT_ID, PARAM_ACCOUNT_ID_NAME, String.class, parameters);
            String invoiceID = getParameter(ctx, PARAM_INVOICE_ID, PARAM_INVOICE_ID_NAME, String.class, parameters);
            
            AccountsImpl.getCrmAccount(ctx, accountID, this);
            com.redknee.app.crm.bean.Invoice invoice = null;
            try
            {
                final And condition = new And();
                condition.add(new EQ(InvoiceXInfo.BAN, accountID));
                condition.add(new EQ(InvoiceXInfo.INVOICE_ID, invoiceID));
                invoice = HomeSupportHelper.get(ctx).findBean(ctx, com.redknee.app.crm.bean.Invoice.class, condition);
            }
            catch (final Exception e)
            {
                final String msg = "Unable to retrieve Invoice " + invoiceID + " for BAN " + accountID;
                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
            }
            if (invoice == null)
            {
                final String identifier = "Invoice " + invoiceID + " for BAN " + accountID;
                RmiApiErrorHandlingSupport.identificationException(ctx, identifier, this);
            }
            InvoiceServerService service = (InvoiceServerService) ctx.get(InvoiceServerService.class);
            if(service == null)
            {
                final String identifier = "InvoiceServerService";
                RmiApiErrorHandlingSupport.identificationException(ctx, identifier, AlternateInvoiceChargesApplierQueryExecutor.class);
            }
            long charge = 0;
            try
            {           
                 charge = service.calculateChargesForAlternateInvoice(accountID, invoiceID, header.getUsername(), true);
            }
            catch (InvoiceServerServiceException e)
            {
                final String msg = "Unable to apply charges for Alternate Invoice for " + accountID + ", Invoice Id : "
                        + invoiceID;
                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, AlternateInvoiceChargesApplierQueryExecutor.class);
            }
            Invoice result = null;
            result = InvoiceToApiAdapter.adaptInvoiceToApi(invoice);        
            try
            {
                AlternateInvoice alternateInvoice = getAlternateInvoice(ctx, accountID, result.getInvoiceDate().getTime());
                ChargeableInvoiceDetails chargeableInvoice = adaptToChargeableInvoiceDetails(alternateInvoice);
                if (alternateInvoice != null)
                {
                    chargeableInvoice.setFeeAmount(Long.valueOf(charge));
                    result.setAlternateDetails(chargeableInvoice);
                }
            }
            catch (Exception e)
            {
                final String msg = "Unable to retrieve Alternate Invoice for " + accountID + ", Invoice Id : " + invoiceID;
                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, AlternateInvoiceChargesApplierQueryExecutor.class);
            }               
            return result;
        }
        
        @Override
        public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            Object[] result = null;
            if (isGenericExecution(ctx, parameters))
            {
                result = new Object[3];
                result[0] = parameters[0];
                result[1] = getParameter(ctx, PARAM_ACCOUNT_ID, PARAM_ACCOUNT_ID_NAME, String.class, parameters);
                result[2] = getParameter(ctx, PARAM_INVOICE_ID, PARAM_INVOICE_ID_NAME, String.class, parameters);
            }
            else
            {
                result = parameters;
            }
            
            return result;
        }

        @Override
        public boolean validateParameterTypes(Class<?>[] parameterTypes)
        {
            boolean result = true;
            result = result && (parameterTypes.length>=3);
            result = result && String.class.isAssignableFrom(parameterTypes[PARAM_ACCOUNT_ID]);
            result = result && String.class.isAssignableFrom(parameterTypes[PARAM_INVOICE_ID]);
            return result;
        }
        
        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return Invoice.class.isAssignableFrom(resultType);
        }


        public static final int PARAM_HEADER = 0;
        public static final int PARAM_ACCOUNT_ID = 1;
        public static final int PARAM_INVOICE_ID = 2;
        
        public static final String PARAM_ACCOUNT_ID_NAME = "accountID";
        public static final String PARAM_INVOICE_ID_NAME = "invoiceID";
    }
    
    private static ChargeableInvoiceDetails getChargeableInvoiceDetails(Context ctx, CRMRequestHeader header, String accountID, String invoiceID, Date invoiceDate, Object caller) throws CRMExceptionFault
    {
        long charge = 0;
        ChargeableInvoiceDetails chargeableInvoice = null;
        AlternateInvoice alternateInvoice = null;
        try
        {            
            alternateInvoice = getAlternateInvoice(ctx, accountID, invoiceDate);
            chargeableInvoice = adaptToChargeableInvoiceDetails(alternateInvoice);
        }
        catch (Exception e)
        {
            final String msg = "Unable to retrieve Alternate Invoice for " + accountID + ", Invoice Id : " + invoiceID;
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, caller);
        }

        // get charge only if detailed invoice is present and not available
        if (alternateInvoice != null && InvoicePDFStateEnum.NOT_AVAILABLE_INDEX != alternateInvoice.getInvoicePDFState().getIndex()
                && InvoicePDFStateEnum.AVAILABLE_INDEX != alternateInvoice.getInvoicePDFState().getIndex())
        {
            InvoiceServerService service = (InvoiceServerService) ctx.get(InvoiceServerService.class);
            if(service == null)
            {
                final String identifier = "InvoiceServerService";
                RmiApiErrorHandlingSupport.identificationException(ctx, identifier, caller);
            }
            try
            {                
                charge = service.calculateChargesForAlternateInvoice(accountID, invoiceID, header.getUsername(), false);
                chargeableInvoice.setFeeAmount(Long.valueOf(charge));
            }
            catch (InvoiceServerServiceException e)
            {
                final String msg = "Unable to retrieve charges for Alternate Invoice for " + accountID
                        + ", Invoice Id : " + invoiceID;
                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, caller);
            }
        }
        return chargeableInvoice;
    }
    
    private static AlternateInvoice getAlternateInvoice(Context ctx, String accountID, Date invoiceDate)
            throws Exception
    {
        Home alternateInvoiceHome = (Home) ctx.get(AlternateInvoiceHome.class);
        AlternateInvoiceID id = new AlternateInvoiceID(accountID, invoiceDate);
        return (AlternateInvoice) alternateInvoiceHome.find(ctx, id);
    }
    
    private static ChargeableInvoiceDetails adaptToChargeableInvoiceDetails(AlternateInvoice alternateInvoice)
            throws Exception
    {        
        ChargeableInvoiceDetails chargeableInvoice = new ChargeableInvoiceDetails();
        if (alternateInvoice != null)
        {
            chargeableInvoice.setNumberOfPages(Long.valueOf(alternateInvoice.getNumberOfPages()));
            switch (alternateInvoice.getInvoicePDFState().getIndex())
            {
            case InvoicePDFStateEnum.NOT_AVAILABLE_INDEX:
                chargeableInvoice.setPdfInvoiceState(InvoicePDFState.value1);
                break;
            case InvoicePDFStateEnum.PENDING_INDEX:
                chargeableInvoice.setPdfInvoiceState(InvoicePDFState.value2);
                break;
            case InvoicePDFStateEnum.NOT_APPROVED_INDEX:
                chargeableInvoice.setPdfInvoiceState(InvoicePDFState.value3);
                break;
            case InvoicePDFStateEnum.AVAILABLE_INDEX:
                chargeableInvoice.setPdfInvoiceState(InvoicePDFState.value4);
                break;
            }
        }
        else
        {
            // not available
            chargeableInvoice.setPdfInvoiceState(InvoicePDFState.value1);
        }
        return chargeableInvoice;
    }
}
