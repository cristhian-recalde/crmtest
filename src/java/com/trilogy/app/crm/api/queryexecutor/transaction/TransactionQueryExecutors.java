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
package com.trilogy.app.crm.api.queryexecutor.transaction;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.api.ApiSupport;
import com.trilogy.app.crm.api.Constants;
import com.trilogy.app.crm.api.queryexecutor.AbstractQueryExecutor;
import com.trilogy.app.crm.api.rmi.AdjustmentTypeToApiAdapter;
import com.trilogy.app.crm.api.rmi.GLCodeMappingToApiAdapter;
import com.trilogy.app.crm.api.rmi.GenericParameterParser;
import com.trilogy.app.crm.api.rmi.PaymentAgentToApiAdapter;
import com.trilogy.app.crm.api.rmi.SubAccountTransactionToApiAdapter;
import com.trilogy.app.crm.api.rmi.TransactionMethodToApiAdapter;
import com.trilogy.app.crm.api.rmi.TransactionToApiAdapter;
import com.trilogy.app.crm.api.rmi.impl.AccountsImpl;
import com.trilogy.app.crm.api.rmi.support.APIGenericParameterSupport;
import com.trilogy.app.crm.api.rmi.support.RmiApiErrorHandlingSupport;
import com.trilogy.app.crm.api.rmi.support.RmiApiSupport;
import com.trilogy.app.crm.api.rmi.support.SubscribersApiSupport;
import com.trilogy.app.crm.api.rmi.support.TransactionApiRerouteDcrmSupport;
import com.trilogy.app.crm.api.rmi.support.TransactionsApiSupport;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.GLCodeMapping;
import com.trilogy.app.crm.bean.GLCodeMappingXInfo;
import com.trilogy.app.crm.bean.GroupTypeEnum;
import com.trilogy.app.crm.bean.PayeeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.TransactionHome;
import com.trilogy.app.crm.bean.TransactionMethod;
import com.trilogy.app.crm.bean.TransactionXInfo;
import com.trilogy.app.crm.bean.payment.PaymentAgent;
import com.trilogy.app.crm.bean.payment.PaymentAgentXInfo;
import com.trilogy.app.crm.elang.PagingXStatement;
import com.trilogy.app.crm.home.UserDailyAdjustmentLimitTransactionIncreaseHome;
import com.trilogy.app.crm.home.ValidatingOverPaymentTransactionHome;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.CollectionSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SubscriptionNotificationSupport;
import com.trilogy.app.crm.transaction.OverPaymentTransactionValidator;
import com.trilogy.app.crm.transaction.TransactionAdjustmentTypeLimitValidator;
import com.trilogy.app.crm.validator.UserDailyAdjustmentLimitTransactionValidator;
import com.trilogy.framework.xhome.beans.CompoundValidator;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.elang.Not;
import com.trilogy.framework.xhome.elang.True;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.ValidatingHome;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v2_1.types.transaction.AdjustmentType;
import com.trilogy.util.crmapi.wsdl.v2_1.types.transaction.AdjustmentTypeReference;
import com.trilogy.util.crmapi.wsdl.v2_1.types.transaction.GLCodeQueryResult;
import com.trilogy.util.crmapi.wsdl.v2_1.types.transaction.GLCodeReference;
import com.trilogy.util.crmapi.wsdl.v2_1.types.transaction.PaymentAgentReference;
import com.trilogy.util.crmapi.wsdl.v2_1.types.transaction.ProfileType;
import com.trilogy.util.crmapi.wsdl.v2_1.types.transaction.ProfileTypeEnum;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.types.CRMRequestHeader;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.transaction.DetailedTransactionQueryResult;
import com.trilogy.util.crmapi.wsdl.v3_0.types.transaction.SubAccountReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.transaction.SubAccountTransactionQueryResult;
import com.trilogy.util.crmapi.wsdl.v3_0.types.transaction.SubAccountTransactionReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.transaction.Transaction;
import com.trilogy.util.crmapi.wsdl.v3_0.types.transaction.TransactionMethodReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.transaction.TransactionQueryResult;
import com.trilogy.util.crmapi.wsdl.v3_0.types.transaction.TransactionReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.transaction.TransactionRequest;

/**
 * 
 * @author Marcio Marques
 * @since 9.3
 *
 */
public class TransactionQueryExecutors 
{
   /** 
    * CRM GL code mapping to API GL code adapter.
    */
   private static final GLCodeMappingToApiAdapter glCodeAdapter_ = new GLCodeMappingToApiAdapter();

   /**
    * CRM payment agent to API payment agent adapter.
    */
   private static final PaymentAgentToApiAdapter paymentAgentAdapter_ = new PaymentAgentToApiAdapter();

   /**
    * CRM adjustment type to API adjustment type adapter.
    */
   private static final AdjustmentTypeToApiAdapter adjustmentTypeAdapter_ = new AdjustmentTypeToApiAdapter();

   /**
    * CRM transaction method to API transaction method adapter.
    */
   private static final TransactionMethodToApiAdapter transactionMethodAdapter_ = new TransactionMethodToApiAdapter();

   /**
    * CRM transaction to API transaction adapter.
    */
   private static final TransactionToApiAdapter transactionAdapter_ = new TransactionToApiAdapter();
   /**
    * CRM Sub Account transaction to API transaction adapter.
    */
   private static final SubAccountTransactionToApiAdapter subAccountTransactionAdapter_ = new SubAccountTransactionToApiAdapter();
    
   /**
    * 
    * @author Marcio Marques
    * @since 9.3.0
    *
    */
   public static class GLCodesListQueryExecutor extends AbstractQueryExecutor<GLCodeQueryResult>
   {
       public GLCodesListQueryExecutor()
       {
           
       }

       public GLCodeQueryResult execute(Context ctx, Object... parameters) throws CRMExceptionFault
       {

           int spid = getParameter(ctx, PARAM_SPID, PARAM_SPID_NAME, int.class, parameters);
           String pageKey = getParameter(ctx, PARAM_PAGE_KEY, PARAM_PAGE_KEY_NAME, String.class, parameters);
           int limit= getParameter(ctx, PARAM_LIMIT, PARAM_LIMIT_NAME, int.class, parameters);
           Boolean isAscending = getParameter(ctx, PARAM_IS_ASCENDING, PARAM_IS_ASCENDING_NAME, Boolean.class, parameters);
           
           final CRMSpid crmSpid = RmiApiSupport.getCrmServiceProvider(ctx, spid, this);

           RmiApiErrorHandlingSupport.validateLimitInput(ctx, limit, crmSpid.getMaxGetLimit());

           GLCodeReference[] numberReferences = new GLCodeReference[] {};
           try
           {
               final boolean ascending = RmiApiSupport.isSortAscending(isAscending);

               final And condition = new And();
               condition.add(new EQ(GLCodeMappingXInfo.SPID, spid));
               if (pageKey != null && pageKey.length() > 0)
               {
                   condition.add(new PagingXStatement(GLCodeMappingXInfo.GL_CODE, pageKey, ascending));
               }

               Collection<GLCodeMapping> collection = HomeSupportHelper.get(ctx).getBeans(
                       ctx,
                       GLCodeMapping.class,
                       condition,
                       limit,
                       ascending,
                       GLCodeMappingXInfo.GL_CODE);

               numberReferences = CollectionSupportHelper.get(ctx).adaptCollection(
                       ctx, 
                       collection, 
                       glCodeAdapter_, 
                       numberReferences);
           }
           catch (final Exception e)
           {
               final String msg = "Unable to retrieve GL Codes for Service Provider " + spid + " pageKey " + pageKey
                   + " limit " + limit;
               RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
           }

           final GLCodeQueryResult result = new GLCodeQueryResult();
           result.setReferences(numberReferences);
           if (numberReferences != null && numberReferences.length > 0)
           {
               result.setPageKey(numberReferences[numberReferences.length - 1].getGlCode());
           }

           return result;

       }
       
       @Override
      public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
      {
           Object[] result = null;
          if (isGenericExecution(ctx, parameters))
          {
              result = new Object[6];
              result[0] = parameters[0];
              result[1] = getParameter(ctx, PARAM_SPID, PARAM_SPID_NAME, int.class, parameters);
              result[2] = getParameter(ctx, PARAM_PAGE_KEY, PARAM_PAGE_KEY_NAME, String.class, parameters);
              result[3] = getParameter(ctx, PARAM_LIMIT, PARAM_LIMIT_NAME, int.class, parameters);
              result[4] = getParameter(ctx, PARAM_IS_ASCENDING, PARAM_IS_ASCENDING_NAME, Boolean.class, parameters);
              result[5] = getParameter(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, GenericParameter[].class, parameters);
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
          result = result && (parameterTypes.length>=6);
          result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
          result = result && int.class.isAssignableFrom(parameterTypes[PARAM_SPID]);
          result = result && String.class.isAssignableFrom(parameterTypes[PARAM_PAGE_KEY]);
          result = result && int.class.isAssignableFrom(parameterTypes[PARAM_LIMIT]);
          result = result && Boolean.class.isAssignableFrom(parameterTypes[PARAM_IS_ASCENDING]);
          result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
          return result;
      }

      @Override
      public boolean validateReturnType(Class<?> resultType)
      {
          return GLCodeQueryResult.class.isAssignableFrom(resultType);
      }

      public static final int PARAM_HEADER = 0;
      public static final int PARAM_SPID = 1;
      public static final int PARAM_PAGE_KEY = 2;
      public static final int PARAM_LIMIT = 3;
      public static final int PARAM_IS_ASCENDING = 4;
      public static final int PARAM_GENERIC_PARAMETERS = 5;
      
      public static final String PARAM_SPID_NAME = "spid";
      public static final String PARAM_PAGE_KEY_NAME = "pageKey";
      public static final String PARAM_LIMIT_NAME = "limit";
      public static final String PARAM_IS_ASCENDING_NAME = "isAscending";
      public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
   }
   
   /**
    * 
    * @author Marcio Marques
    * @since 9.3.0
    *
    */
   public static class PaymentAgentsListQueryExecutor extends AbstractQueryExecutor<PaymentAgentReference[]>
   {
       public PaymentAgentsListQueryExecutor()
       {
           
       }

       public PaymentAgentReference[] execute(Context ctx, Object... parameters) throws CRMExceptionFault
       {

           int spid = getParameter(ctx, PARAM_SPID, PARAM_SPID_NAME, int.class, parameters);
           Boolean isAscending = getParameter(ctx, PARAM_IS_ASCENDING, PARAM_IS_ASCENDING_NAME, Boolean.class, parameters);
           
           RmiApiSupport.getCrmServiceProvider(ctx, spid, this);

           PaymentAgentReference[] paymentAgentReferences = new PaymentAgentReference[] {};
           try
           {
               final Object condition = new EQ(PaymentAgentXInfo.SPID, spid);

               final Collection<PaymentAgent> collection = HomeSupportHelper.get(ctx).getBeans(
                       ctx,
                       PaymentAgent.class,
                       condition,
                       RmiApiSupport.isSortAscending(isAscending));

               paymentAgentReferences = CollectionSupportHelper.get(ctx).adaptCollection(
                       ctx, 
                       collection, 
                       paymentAgentAdapter_, 
                       paymentAgentReferences);
           }
           catch (final Exception e)
           {
               final String msg = "Unable to retrieve Dealer Codes for Service Provider=" + spid;
               RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
           }

           return paymentAgentReferences;
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
          return PaymentAgentReference[].class.isAssignableFrom(resultType);
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
   public static class TransactionMethodsListQueryExecutor extends AbstractQueryExecutor<TransactionMethodReference[]>
   {
       public TransactionMethodsListQueryExecutor()
       {
           
       }

       public TransactionMethodReference[] execute(Context ctx, Object... parameters) throws CRMExceptionFault
       {
           Boolean isAscending = getParameter(ctx, PARAM_IS_ASCENDING, PARAM_IS_ASCENDING_NAME, Boolean.class, parameters);

           TransactionMethodReference[] methodReferences = new TransactionMethodReference[] {};
           try
           {
               final Object condition = True.instance();
               final Collection<TransactionMethod> collection = HomeSupportHelper.get(ctx).getBeans(
                       ctx,
                       TransactionMethod.class,
                       condition,
                       RmiApiSupport.isSortAscending(isAscending));

               methodReferences = CollectionSupportHelper.get(ctx).adaptCollection(
                       ctx, 
                       collection, 
                       transactionMethodAdapter_, 
                       methodReferences);
           }
           catch (final Exception e)
           {
               final String msg = "Unable to retrieve Transaction Methods";
               RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
           }

           return methodReferences;           
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
          return TransactionMethodReference[].class.isAssignableFrom(resultType);
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
   public static class AccountTransactionsListQueryExecutor extends AbstractQueryExecutor<TransactionQueryResult>
   {
       public AccountTransactionsListQueryExecutor()
       {
           
       }

       public TransactionQueryResult execute(Context ctx, Object... parameters) throws CRMExceptionFault
       {
           CRMRequestHeader header = getParameter(ctx, PARAM_HEADER, "header", CRMRequestHeader.class, parameters);
           String accountID = getParameter(ctx, PARAM_ACCOUNT_ID, PARAM_ACCOUNT_ID_NAME, String.class, parameters);
           Calendar start = getParameter(ctx, PARAM_START, PARAM_START_NAME, Calendar.class, parameters);
           Calendar end = getParameter(ctx, PARAM_END, PARAM_END_NAME, Calendar.class, parameters);
           Long category = getParameter(ctx, PARAM_CATEGORY, PARAM_CATEGORY_NAME, Long.class, parameters);
           String pageKey = getParameter(ctx, PARAM_PAGE_KEY, PARAM_PAGE_KEY_NAME, String.class, parameters);
           int limit= getParameter(ctx, PARAM_LIMIT, PARAM_LIMIT_NAME, int.class, parameters);
           Boolean isAscending = getParameter(ctx, PARAM_IS_ASCENDING, PARAM_IS_ASCENDING_NAME, Boolean.class, parameters);
           ProfileType ownerType = getParameter(ctx, PARAM_OWNER_TYPE, PARAM_OWNER_TYPE_NAME, ProfileType.class, parameters);
           Long accountLevelID = getParameter(ctx, PARAM_ACCOUNT_LEVEL_ID, PARAM_ACCOUNT_LEVEL_ID_NAME, Long.class, parameters);
           GenericParameter[] genericParameters = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);

           RmiApiErrorHandlingSupport.validateMandatoryObject(accountID, PARAM_ACCOUNT_ID_NAME);

           final Long longPageKey = RmiApiErrorHandlingSupport.validateLongPageKey(ctx, pageKey);

           // getting account to validate it's existence
           Account acct = AccountsImpl.getCrmAccount(ctx, accountID, this);

           final CRMSpid spid = RmiApiSupport.getCrmServiceProvider(ctx, acct.getSpid(), this);
           RmiApiErrorHandlingSupport.validateLimitInput(ctx, limit, spid.getMaxGetLimit());
           
           final EQ condition;
           
           if (ownerType!=null && ProfileTypeEnum.ACCOUNT.getValue().getValue() == ownerType.getValue())
           {
               condition = new EQ(TransactionXInfo.RESPONSIBLE_BAN, accountID);
           }
           else
           {
               condition = new EQ(TransactionXInfo.BAN, accountID);
           }

           return listTransactions(ctx, header, condition, start, end, category, longPageKey, limit, isAscending, genericParameters, ownerType, accountLevelID, accountID, this);           
       }
       
       @Override
      public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
      {
           Object[] result = null;
          if (isGenericExecution(ctx, parameters))
          {
              result = new Object[11];
              result[0] = parameters[0];
              result[1] = getParameter(ctx, PARAM_ACCOUNT_ID, PARAM_ACCOUNT_ID_NAME, String.class, parameters);
              result[2] = getParameter(ctx, PARAM_START, PARAM_START_NAME, Calendar.class, parameters);
              result[3] = getParameter(ctx, PARAM_END, PARAM_END_NAME, Calendar.class, parameters);
              result[4] = getParameter(ctx, PARAM_CATEGORY, PARAM_CATEGORY_NAME, Long.class, parameters);
              result[5] = getParameter(ctx, PARAM_PAGE_KEY, PARAM_PAGE_KEY_NAME, String.class, parameters);
              result[6] = getParameter(ctx, PARAM_LIMIT, PARAM_LIMIT_NAME, int.class, parameters);
              result[7] = getParameter(ctx, PARAM_IS_ASCENDING, PARAM_IS_ASCENDING_NAME, Boolean.class, parameters);
              result[8] = getParameter(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, GenericParameter[].class, parameters);
              result[9] = getParameter(ctx, PARAM_OWNER_TYPE, PARAM_OWNER_TYPE_NAME, ProfileType.class, parameters);
              result[10] = getParameter(ctx, PARAM_ACCOUNT_LEVEL_ID, PARAM_ACCOUNT_LEVEL_ID_NAME, Long.class, parameters);
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
          result = result && (parameterTypes.length>=11);
          result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
          result = result && String.class.isAssignableFrom(parameterTypes[PARAM_ACCOUNT_ID]);
          result = result && Calendar.class.isAssignableFrom(parameterTypes[PARAM_START]);
          result = result && Calendar.class.isAssignableFrom(parameterTypes[PARAM_END]);
          result = result && Long.class.isAssignableFrom(parameterTypes[PARAM_CATEGORY]);
          result = result && String.class.isAssignableFrom(parameterTypes[PARAM_PAGE_KEY]);
          result = result && int.class.isAssignableFrom(parameterTypes[PARAM_LIMIT]);
          result = result && Boolean.class.isAssignableFrom(parameterTypes[PARAM_IS_ASCENDING]);
          result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
          result = result && ProfileType.class.isAssignableFrom(parameterTypes[PARAM_OWNER_TYPE]);
          result = result && Long.class.isAssignableFrom(parameterTypes[PARAM_ACCOUNT_LEVEL_ID]);
          return result;
      }
      
      @Override
      public boolean validateReturnType(Class<?> resultType)
      {
          return TransactionQueryResult.class.isAssignableFrom(resultType);
      }


      public static final int PARAM_HEADER = 0;
      public static final int PARAM_ACCOUNT_ID = 1;
      public static final int PARAM_START = 2;
      public static final int PARAM_END = 3;
      public static final int PARAM_CATEGORY = 4;
      public static final int PARAM_PAGE_KEY = 5;
      public static final int PARAM_LIMIT = 6;
      public static final int PARAM_IS_ASCENDING = 7;
      public static final int PARAM_GENERIC_PARAMETERS = 8;
      public static final int PARAM_OWNER_TYPE = 9;
      public static final int PARAM_ACCOUNT_LEVEL_ID =  10;
      
      public static final String PARAM_ACCOUNT_ID_NAME = "accountID";
      public static final String PARAM_START_NAME = "start";
      public static final String PARAM_END_NAME = "end";
      public static final String PARAM_CATEGORY_NAME = "category";
      public static final String PARAM_PAGE_KEY_NAME = "pageKey";
      public static final String PARAM_LIMIT_NAME = "limit";
      public static final String PARAM_IS_ASCENDING_NAME = "isAscending";
      public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
      public static final String PARAM_OWNER_TYPE_NAME = "ownerType";
      public static final String PARAM_ACCOUNT_LEVEL_ID_NAME = "accountLevelID";
   }
   
   /**
    * 
    * @author Marcio Marques
    * @since 9.3.0
    *
    */
   public static class SubAccountTransactionsListQueryExecutor extends AbstractQueryExecutor<SubAccountTransactionQueryResult>
   {
       public SubAccountTransactionsListQueryExecutor()
       {
           
       }

       public SubAccountTransactionQueryResult execute(Context ctx, Object... parameters) throws CRMExceptionFault
       {
           CRMRequestHeader header = getParameter(ctx, SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_HEADER.ordinal(), SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_HEADER.getPName(), CRMRequestHeader.class, parameters);
           
           String accountID = getParameter(ctx, SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_ACCOUNT_ID.ordinal(), SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_ACCOUNT_ID.getPName(), String.class, parameters);
           
           Calendar start = getParameter(ctx, SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_START_DATE.ordinal() , SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_START_DATE.getPName(), Calendar.class, parameters);
           Calendar end = getParameter(ctx, SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_END_DATE.ordinal() , SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_END_DATE.getPName(), Calendar.class, parameters);
           
           Long category = getParameter(ctx, SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_CATEGORY.ordinal(), SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_CATEGORY.getPName(), Long.class, parameters);
           
           String pagekey = getParameter(ctx, SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_PAGEKEY.ordinal(), SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_PAGEKEY.getPName(), String.class, parameters);
           
           Integer limit = getParameter(ctx, SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_LIMIT.ordinal() , SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_LIMIT.getPName(), Integer.class, parameters);
           Boolean isAscending = getParameter(ctx, SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_IS_ASCENDING.ordinal() , SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_IS_ASCENDING.getPName(), Boolean.class, parameters);
           
           Boolean recurse = getParameter(ctx, SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_RECURSE.ordinal() , SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_RECURSE.getPName(), Boolean.class, parameters);
           
           Integer subAccounts = getParameter(ctx, SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_SUBACCOUNTS.ordinal() , SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_SUBACCOUNTS.getPName(), Integer.class, parameters);
           
           GenericParameter[] genericParameters = getGenericParameters(ctx, SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_GENERIC_PARAMETERS.ordinal(), SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_GENERIC_PARAMETERS.getPName(), parameters);
           
           ProfileType ownerType = getParameter(ctx, SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_OWNER_TYPE.ordinal(), SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_OWNER_TYPE.getPName(), ProfileType.class, parameters);
           Long accountLevelID = getParameter(ctx, SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_ACCOUNTlEVELID.ordinal(), SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_ACCOUNTlEVELID.getPName(), Long.class, parameters);

           RmiApiErrorHandlingSupport.validateMandatoryObject(accountID, SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_ACCOUNT_ID.getPName());
           
           if (accountID != null && accountID.trim().length() == 0)
           {
           	final String msg = "accountID field is empty, please provide valid value";
           	RmiApiErrorHandlingSupport.generalException(ctx, null, msg, this);
           }

           RmiApiErrorHandlingSupport.validateMandatoryObject(limit, SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_LIMIT.getPName());

//           RmiApiErrorHandlingSupport.validateLongPageKey(ctx, pageKey);
           


           final Long longPageKey = RmiApiErrorHandlingSupport.validateLongPageKey(ctx, pagekey);

           // getting account to validate it's existence
           
           Account acc = AccountsImpl.getCrmAccount(ctx, accountID, this);
           Collection<SortedAccount> accCollection = new LinkedList<SortedAccount>();
           
           if (acc != null)
           {
        	   
               final CRMSpid spid = RmiApiSupport.getCrmServiceProvider(ctx, acc.getSpid(), this);
//               RmiApiErrorHandlingSupport.validateLimitInput(ctx, limit, spid.getMaxGetLimit());
               if (limit != null && limit.intValue() <= 0 )
               {
            	   RmiApiErrorHandlingSupport.simpleValidation("limit", "The input limit value is smaller than or equal to 0");
               }
               
               if (limit > spid.getMaxGetLimit())
               {
            	   limit = spid.getMaxGetLimit();
               }
               
        	   accCollection.add(new SortedAccount(acc));

        	   if (recurse)
        	   {
        		   
        		   if (subAccounts == null || (subAccounts != null && subAccounts.intValue() > spid.getMaxGetLimit()))
                   {
        			   subAccounts = spid.getMaxGetLimit();
                   }

        		   boolean isAccAscend = true;

        		   Collection<Account> subaccCollection = AccountsImpl.getCrmSubAccounts(ctx, acc, recurse, false, isAccAscend, genericParameters);

        		   for (Account subAcc : subaccCollection)
        		   {
        			   accCollection.add(new SortedAccount(subAcc));

        		   }

        	   }

           }
           else
           {
           	final String msg = "Account with given accountID does not exist, please provide correct accountId";
               RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, null, msg, this);
           }

           if (accCollection.size() == 0)
           {
        	   final String msg = "No Records found for given account";
        	   RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, null, msg, this);
           }
           else if (accCollection.size() > 1)
           {
        	   Collections.sort((List<SortedAccount>)accCollection);
           }
           
    	   Collection<SortedAccount> accCollectionlimit = new LinkedList<SortedAccount>();
           if (recurse && longPageKey != null)
           {
        	   for (SortedAccount sortedacc: accCollection)
        	   {
        		  if (longPageKey >= Long.parseLong(sortedacc.getAcc().getBAN()))
        			  continue;
        		  accCollectionlimit.add(sortedacc);
        	   }
        	   
           }
           else
           {
        	   accCollectionlimit = accCollection;
           }
           
          
               
//           final CRMSpid spid = RmiApiSupport.getCrmServiceProvider(ctx, acc.getSpid(), this);
//           RmiApiErrorHandlingSupport.validateLimitInput(ctx, limit, spid.getMaxGetLimit());

     	   if (accCollectionlimit.size() == 0)
     	   {
     		   final String msg = "No More records found";
     		   RmiApiErrorHandlingSupport.generalException(ctx, null, msg, this);
     	   }
           SubAccountTransactionQueryResult result = new SubAccountTransactionQueryResult();
           List<SubAccountReference> subAccountReferenceList = new ArrayList<SubAccountReference>();
           SubAccountReference[] subAccountReferenceArray = new SubAccountReference[] {};
           
           int acclimit = 0;
    	   int i = 0;        	   
           if (subAccounts != null && subAccounts.intValue() > 0)
           {
        	   acclimit = subAccounts.intValue();
           }
           else if (subAccounts != null && subAccounts.intValue() <= 0)
           {
        	   final String msg = "subAccounts field value is either negative or 0, please give valid value";
        	   RmiApiErrorHandlingSupport.generalException(ctx, null, msg, this);
           }
           
           for (SortedAccount account : accCollectionlimit)
           {
//             final And condition = new And();
    		   if (acclimit > 0 && i == acclimit)
    		   {
    			   break;
    		   }

               final EQ condition ;
               
               if (ownerType!=null && ProfileTypeEnum.ACCOUNT.getValue().getValue() == ownerType.getValue())
               {
                   condition = new EQ(TransactionXInfo.RESPONSIBLE_BAN, account.getAcc().getBAN());
               }
               else
               {
                   condition = new EQ(TransactionXInfo.BAN, account.getAcc().getBAN());
               }
               
               if (ownerType == null)
               {
            	   ownerType = ProfileTypeEnum.SUBSCRIPTION.getValue();
               }
               
               SubAccountReference subAccountReference = listSubAccountTransactions(ctx, header, condition, start, end, category, longPageKey, limit, isAscending, genericParameters, ownerType, accountLevelID, account.getAcc(), this);
               if (subAccountReference.getReferences().length > 0)
               {
            	   subAccountReferenceList.add(subAccountReference);
                   i++;
               }
               
           }
           
           subAccountReferenceArray = subAccountReferenceList.toArray(subAccountReferenceArray);
           if (subAccountReferenceArray != null && subAccountReferenceArray.length > 0)
           {
               result.setPageKey(String.valueOf(subAccountReferenceArray[subAccountReferenceArray.length - 1].getBAN()));
//           result.setPageKey(((LinkedList<SortedAccount>)accCollectionlimit).getLast().getAcc().getBAN());
           }
           else
           {
              	final String msg = "Account with given accountID does not contain any transactions";
                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, null, msg, this);
        	   
           }
       
          /* }*/

               result.setAccountTransactions(subAccountReferenceArray);

               return result;
               
       }
       
       @Override
      public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
      {
           Object[] result = null;
          if (isGenericExecution(ctx, parameters))
          {
              result = new Object[13];
              result[0] = parameters[0];
              result[1] = getParameter(ctx, SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_ACCOUNT_ID.ordinal(), SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_ACCOUNT_ID.getPName(), String.class, parameters);
              result[2] = getParameter(ctx, SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_START_DATE.ordinal() , SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_START_DATE.getPName(), Calendar.class, parameters);
              result[3] = getParameter(ctx, SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_END_DATE.ordinal() , SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_END_DATE.getPName(), Calendar.class, parameters);
              result[4] = getParameter(ctx, SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_CATEGORY.ordinal(), SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_CATEGORY.getPName(), Long.class, parameters);
              result[5] = getParameter(ctx, SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_PAGEKEY.ordinal(), SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_PAGEKEY.getPName(), String.class, parameters);
              result[6] = getParameter(ctx, SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_LIMIT.ordinal() , SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_LIMIT.getPName(), Integer.class, parameters);
              result[7] = getParameter(ctx, SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_IS_ASCENDING.ordinal() , SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_IS_ASCENDING.getPName(), Boolean.class, parameters);
              result[8] = getParameter(ctx, SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_RECURSE.ordinal() , SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_RECURSE.getPName(), Boolean.class, parameters);
              result[9] = getParameter(ctx, SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_SUBACCOUNTS.ordinal() , SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_SUBACCOUNTS.getPName(), Integer.class, parameters);
              result[10] = getParameter(ctx, SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_GENERIC_PARAMETERS.ordinal(), SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_GENERIC_PARAMETERS.getPName(), GenericParameter[].class, parameters);
              
              result[11] = getParameter(ctx, SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_OWNER_TYPE.ordinal(), SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_OWNER_TYPE.getPName(), ProfileType.class, parameters);
              result[12] = getParameter(ctx, SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_ACCOUNTlEVELID.ordinal(), SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_ACCOUNTlEVELID.getPName(), Long.class, parameters);
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
          result = result && (parameterTypes.length>=13);
          result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_HEADER.ordinal()]);
          result = result && String.class.isAssignableFrom(parameterTypes[SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_ACCOUNT_ID.ordinal()]);
          result = result && Calendar.class.isAssignableFrom(parameterTypes[SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_START_DATE.ordinal()]);
          result = result && Calendar.class.isAssignableFrom(parameterTypes[SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_END_DATE.ordinal()]);
          result = result && Long.class.isAssignableFrom(parameterTypes[SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_CATEGORY.ordinal()]);
          result = result && String.class.isAssignableFrom(parameterTypes[SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_PAGEKEY.ordinal()]);
          result = result && int.class.isAssignableFrom(parameterTypes[SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_LIMIT.ordinal()]);
          result = result && Boolean.class.isAssignableFrom(parameterTypes[SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_IS_ASCENDING.ordinal()]);
          result = result && Boolean.class.isAssignableFrom(parameterTypes[SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_RECURSE.ordinal()]);
          result = result && Integer.class.isAssignableFrom(parameterTypes[SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_SUBACCOUNTS.ordinal()]);
          result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_GENERIC_PARAMETERS.ordinal()]);
          result = result && ProfileType.class.isAssignableFrom(parameterTypes[SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_OWNER_TYPE.ordinal()]);
          result = result && Long.class.isAssignableFrom(parameterTypes[SubAccountTransactionsEnum.SUB_ACCOUNT_PARAM_ACCOUNTlEVELID.ordinal()]);
          return result;
      }
      
      @Override
      public boolean validateReturnType(Class<?> resultType)
      {
          return TransactionQueryResult.class.isAssignableFrom(resultType);
      }


//      public static final int PARAM_HEADER = 0;
//      public static final int PARAM_ACCOUNT_ID = 1;
//      public static final int PARAM_START = 2;
//      public static final int PARAM_END = 3;
//      public static final int PARAM_CATEGORY = 4;
//      public static final int PARAM_PAGE_KEY = 5;
//      public static final int PARAM_LIMIT = 6;
//      public static final int PARAM_IS_ASCENDING = 7;
//      public static final int PARAM_GENERIC_PARAMETERS = 8;
//      public static final int PARAM_OWNER_TYPE = 9;
//      public static final int PARAM_ACCOUNT_LEVEL_ID =  10;
//      
//      public static final String PARAM_ACCOUNT_ID_NAME = "accountID";
//      public static final String PARAM_START_NAME = "start";
//      public static final String PARAM_END_NAME = "end";
//      public static final String PARAM_CATEGORY_NAME = "category";
//      public static final String PARAM_PAGE_KEY_NAME = "pageKey";
//      public static final String PARAM_LIMIT_NAME = "limit";
//      public static final String PARAM_IS_ASCENDING_NAME = "isAscending";
//      public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
//      public static final String PARAM_OWNER_TYPE_NAME = "ownerType";
//      public static final String PARAM_ACCOUNT_LEVEL_ID_NAME = "accountLevelID";
      
      
      enum SubAccountTransactionsEnum {
   	   
   	   SUB_ACCOUNT_PARAM_HEADER("header"),
   	   SUB_ACCOUNT_PARAM_ACCOUNT_ID("accountID"),
   	   SUB_ACCOUNT_PARAM_START_DATE("start"),
   	   SUB_ACCOUNT_PARAM_END_DATE("end"),
   	   SUB_ACCOUNT_PARAM_CATEGORY("category"),
   	   SUB_ACCOUNT_PARAM_PAGEKEY("pageKey"),
   	   SUB_ACCOUNT_PARAM_LIMIT("limit"),
   	   SUB_ACCOUNT_PARAM_IS_ASCENDING("isAscending"),
   	   SUB_ACCOUNT_PARAM_RECURSE("recurse"),
   	   SUB_ACCOUNT_PARAM_SUBACCOUNTS("subAccounts"),
   	   SUB_ACCOUNT_PARAM_GENERIC_PARAMETERS("parameters"),
   	   SUB_ACCOUNT_PARAM_OWNER_TYPE("ownerType"),
   	   SUB_ACCOUNT_PARAM_ACCOUNTlEVELID("accountLevelId");
   	   
   	   private final String pName;
   	   
   	   private SubAccountTransactionsEnum(String paramName)
   	   {
   		   pName = paramName;
   	   }
   	   
   	   public String getPName(){
   		   return pName;
   	   }
      }

      
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
   public static class DetailedAccountTransactionsListQueryExecutor extends AbstractQueryExecutor<DetailedTransactionQueryResult>
   {
       public DetailedAccountTransactionsListQueryExecutor()
       {
           
       }

       public DetailedTransactionQueryResult execute(Context ctx, Object... parameters) throws CRMExceptionFault
       {
           String accountID = getParameter(ctx, PARAM_ACCOUNT_ID, PARAM_ACCOUNT_ID_NAME, String.class, parameters);
           Calendar start = getParameter(ctx, PARAM_START, PARAM_START_NAME, Calendar.class, parameters);
           Calendar end = getParameter(ctx, PARAM_END, PARAM_END_NAME, Calendar.class, parameters);
           Long category = getParameter(ctx, PARAM_CATEGORY, PARAM_CATEGORY_NAME, Long.class, parameters);
           String pageKey = getParameter(ctx, PARAM_PAGE_KEY, PARAM_PAGE_KEY_NAME, String.class, parameters);
           int limit= getParameter(ctx, PARAM_LIMIT, PARAM_LIMIT_NAME, int.class, parameters);
           Boolean isAscending = getParameter(ctx, PARAM_IS_ASCENDING, PARAM_IS_ASCENDING_NAME, Boolean.class, parameters);
           ProfileType ownerType = getParameter(ctx, PARAM_OWNER_TYPE, PARAM_OWNER_TYPE_NAME, ProfileType.class, parameters);
           Long accountLevelID = getParameter(ctx, PARAM_ACCOUNT_LEVEL_ID, PARAM_ACCOUNT_LEVEL_ID_NAME, Long.class, parameters);

           RmiApiErrorHandlingSupport.validateMandatoryObject(accountID, PARAM_ACCOUNT_ID_NAME);

           if (ownerType==null)
           {
               ownerType = ProfileTypeEnum.SUBSCRIPTION.getValue();
           }

           final Long longPageKey = RmiApiErrorHandlingSupport.validateLongPageKey(ctx, pageKey);

           // getting account to validate it's existence
           Account acct = AccountsImpl.getCrmAccount(ctx, accountID, this);

           final CRMSpid spid = RmiApiSupport.getCrmServiceProvider(ctx, acct.getSpid(), this);
           RmiApiErrorHandlingSupport.validateLimitInput(ctx, limit, spid.getMaxGetLimit());

           
           Transaction[] transactions = new Transaction[] {};
           try
           {
               Collection<com.redknee.app.crm.bean.Transaction> accountCollection = new ArrayList<com.redknee.app.crm.bean.Transaction>();
               Collection<com.redknee.app.crm.bean.Transaction> subscriptionCollection = new ArrayList<com.redknee.app.crm.bean.Transaction>();
               
               final And condition = new And();
               
               if(acct.getGroupType().getIndex() ==  GroupTypeEnum.GROUP_INDEX) 
               {
            	   condition.add(new EQ(TransactionXInfo.RESPONSIBLE_BAN, accountID));
               }
               else if(acct.getGroupType().getIndex() ==  GroupTypeEnum.SUBSCRIBER_INDEX)
               {
            	   condition.add(new EQ(TransactionXInfo.BAN, accountID));
               }
               
               if (ProfileTypeEnum.ACCOUNT.getValue().getValue() == ownerType.getValue())
               {
                   accountCollection.addAll(  
                           TransactionsApiSupport.getTransactionsUsingGivenParameters(
                                   ctx,
                                   new EQ(TransactionXInfo.BAN, accountID), 
                                   start, 
                                   end, 
                                   category, 
                                   longPageKey, 
                                   limit, 
                                   isAscending,
                                   ProfileTypeEnum.ACCOUNT.getValue(),
                                   accountLevelID,
                                   this));
                   
                   Set<Long> accountReceiptNums = new HashSet<Long>();
                   
                   for (com.redknee.app.crm.bean.Transaction transaction : accountCollection)
                   {
                       accountReceiptNums.add(transaction.getReceiptNum());
                   }
                   
                   condition.add(new Not(new In(TransactionXInfo.ACCOUNT_RECEIPT_NUM, accountReceiptNums)));
                   
                   
               }
               
               subscriptionCollection.addAll(
                   TransactionsApiSupport.getTransactionsUsingGivenParameters(
                           ctx,
                           condition, 
                           start, 
                           end, 
                           category, 
                           longPageKey, 
                           limit, 
                           isAscending,
                           ProfileTypeEnum.SUBSCRIPTION.getValue(),
                           accountLevelID,
                           this));
               
               
               
               transactions = new Transaction[accountCollection.size() + subscriptionCollection.size()];
               int i = 0;
               ProfileType profileType = ProfileTypeEnum.ACCOUNT.getValue();
               for (com.redknee.app.crm.bean.Transaction transaction : accountCollection)
               {
                   transactions[i++] = TransactionToApiAdapter.adaptTransactionToApi(ctx, transaction, profileType);
               }

               profileType = ProfileTypeEnum.SUBSCRIPTION.getValue();
               for (com.redknee.app.crm.bean.Transaction transaction : subscriptionCollection)
               {
                   transactions[i++] = TransactionToApiAdapter.adaptTransactionToApi(ctx, transaction, profileType);
               }
           }
           catch (final Exception e)
           {
               final String msg = "Unable to retrieve Transactions for start date " + start + " end date " + end
                   + " category " + category + " pageKey " + pageKey + " limit " + limit;
               RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
           }

           final DetailedTransactionQueryResult result = new DetailedTransactionQueryResult();
           result.setResults(transactions);
           if (transactions != null && transactions.length > 0)
           {
               result.setPageKey(String.valueOf(transactions[transactions.length - 1].getIdentifier()));
           }

           return result;   
       }
       
       @Override
      public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
      {
           Object[] result = null;
          if (isGenericExecution(ctx, parameters))
          {
              result = new Object[11];
              result[0] = parameters[0];
              result[1] = getParameter(ctx, PARAM_ACCOUNT_ID, PARAM_ACCOUNT_ID_NAME, String.class, parameters);
              result[2] = getParameter(ctx, PARAM_START, PARAM_START_NAME, Calendar.class, parameters);
              result[3] = getParameter(ctx, PARAM_END, PARAM_END_NAME, Calendar.class, parameters);
              result[4] = getParameter(ctx, PARAM_CATEGORY, PARAM_CATEGORY_NAME, Long.class, parameters);
              result[5] = getParameter(ctx, PARAM_PAGE_KEY, PARAM_PAGE_KEY_NAME, String.class, parameters);
              result[6] = getParameter(ctx, PARAM_LIMIT, PARAM_LIMIT_NAME, int.class, parameters);
              result[7] = getParameter(ctx, PARAM_IS_ASCENDING, PARAM_IS_ASCENDING_NAME, Boolean.class, parameters);
              result[8] = getParameter(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, GenericParameter[].class, parameters);
              result[9] = getParameter(ctx, PARAM_OWNER_TYPE, PARAM_OWNER_TYPE_NAME, ProfileType.class, parameters);
              result[10] = getParameter(ctx, PARAM_ACCOUNT_LEVEL_ID, PARAM_ACCOUNT_LEVEL_ID_NAME, Long.class, parameters);
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
          result = result && (parameterTypes.length>=11);
          result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
          result = result && String.class.isAssignableFrom(parameterTypes[PARAM_ACCOUNT_ID]);
          result = result && Calendar.class.isAssignableFrom(parameterTypes[PARAM_START]);
          result = result && Calendar.class.isAssignableFrom(parameterTypes[PARAM_END]);
          result = result && Long.class.isAssignableFrom(parameterTypes[PARAM_CATEGORY]);
          result = result && String.class.isAssignableFrom(parameterTypes[PARAM_PAGE_KEY]);
          result = result && int.class.isAssignableFrom(parameterTypes[PARAM_LIMIT]);
          result = result && Boolean.class.isAssignableFrom(parameterTypes[PARAM_IS_ASCENDING]);
          result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
          result = result && ProfileType.class.isAssignableFrom(parameterTypes[PARAM_OWNER_TYPE]);
          result = result && Long.class.isAssignableFrom(parameterTypes[PARAM_ACCOUNT_LEVEL_ID]);
          return result;
      }
      
      @Override
      public boolean validateReturnType(Class<?> resultType)
      {
          return DetailedTransactionQueryResult.class.isAssignableFrom(resultType);
      }


      public static final int PARAM_HEADER = 0;
      public static final int PARAM_ACCOUNT_ID = 1;
      public static final int PARAM_START = 2;
      public static final int PARAM_END = 3;
      public static final int PARAM_CATEGORY = 4;
      public static final int PARAM_PAGE_KEY = 5;
      public static final int PARAM_LIMIT = 6;
      public static final int PARAM_IS_ASCENDING = 7;
      public static final int PARAM_GENERIC_PARAMETERS = 8;
      public static final int PARAM_OWNER_TYPE = 9;
      public static final int PARAM_ACCOUNT_LEVEL_ID =  10;
      
      public static final String PARAM_ACCOUNT_ID_NAME = "accountID";
      public static final String PARAM_START_NAME = "start";
      public static final String PARAM_END_NAME = "end";
      public static final String PARAM_CATEGORY_NAME = "category";
      public static final String PARAM_PAGE_KEY_NAME = "pageKey";
      public static final String PARAM_LIMIT_NAME = "limit";
      public static final String PARAM_IS_ASCENDING_NAME = "isAscending";
      public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
      public static final String PARAM_OWNER_TYPE_NAME = "ownerType";
      public static final String PARAM_ACCOUNT_LEVEL_ID_NAME = "accountLevelID";
   }
   
   /**
    * 
    * @author Marcio Marques
    * @since 9.3.0
    *
    */
   public static class SubscriptionTransactionsListQueryExecutor extends AbstractQueryExecutor<TransactionQueryResult>
   {
       public SubscriptionTransactionsListQueryExecutor()
       {
           
       }

       public TransactionQueryResult execute(Context ctx, Object... parameters) throws CRMExceptionFault
       {
           CRMRequestHeader header = getParameter(ctx, PARAM_HEADER, "header", CRMRequestHeader.class, parameters);
           SubscriptionReference subscriptionRef = getParameter(ctx, PARAM_SUBSCRIPTION_REFERENCE, PARAM_SUBSCRIPTION_REFERENCE_NAME, SubscriptionReference.class, parameters);
           Calendar start = getParameter(ctx, PARAM_START, PARAM_START_NAME, Calendar.class, parameters);
           Calendar end = getParameter(ctx, PARAM_END, PARAM_END_NAME, Calendar.class, parameters);
           Long category = getParameter(ctx, PARAM_CATEGORY, PARAM_CATEGORY_NAME, Long.class, parameters);
           String pageKey = getParameter(ctx, PARAM_PAGE_KEY, PARAM_PAGE_KEY_NAME, String.class, parameters);
           int limit= getParameter(ctx, PARAM_LIMIT, PARAM_LIMIT_NAME, int.class, parameters);
           Boolean isAscending = getParameter(ctx, PARAM_IS_ASCENDING, PARAM_IS_ASCENDING_NAME, Boolean.class, parameters);
           GenericParameter[] genericParameters = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);

           RmiApiErrorHandlingSupport.validateMandatoryObject(subscriptionRef, PARAM_SUBSCRIPTION_REFERENCE_NAME);

           final Long longPageKey = RmiApiErrorHandlingSupport.validateLongPageKey(ctx, pageKey);

           // getting subscriber to validate it's existence
           SubscribersApiSupport.updateReferenceId(ctx, subscriptionRef, this);

           final CRMSpid spid = RmiApiSupport.getCrmServiceProvider(ctx, subscriptionRef.getSpid().intValue(), this);
           RmiApiErrorHandlingSupport.validateLimitInput(ctx, limit, spid.getMaxGetLimit());
           
           final EQ condition = new EQ(TransactionXInfo.SUBSCRIBER_ID, subscriptionRef.getIdentifier());

           return listTransactions(ctx, header, condition, start, end, category, longPageKey, limit, isAscending, genericParameters, ProfileTypeEnum.SUBSCRIPTION.getValue(), null, null, this);

       }
       
       @Override
      public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
      {
           Object[] result = null;
          if (isGenericExecution(ctx, parameters))
          {
              result = new Object[9];
              result[0] = parameters[0];
              result[1] = getParameter(ctx, PARAM_SUBSCRIPTION_REFERENCE, PARAM_SUBSCRIPTION_REFERENCE_NAME, SubscriptionReference.class, parameters);
              result[2] = getParameter(ctx, PARAM_START, PARAM_START_NAME, Calendar.class, parameters);
              result[3] = getParameter(ctx, PARAM_END, PARAM_END_NAME, Calendar.class, parameters);
              result[4] = getParameter(ctx, PARAM_CATEGORY, PARAM_CATEGORY_NAME, Long.class, parameters);
              result[5] = getParameter(ctx, PARAM_PAGE_KEY, PARAM_PAGE_KEY_NAME, String.class, parameters);
              result[6] = getParameter(ctx, PARAM_LIMIT, PARAM_LIMIT_NAME, int.class, parameters);
              result[7] = getParameter(ctx, PARAM_IS_ASCENDING, PARAM_IS_ASCENDING_NAME, Boolean.class, parameters);
              result[8] = getParameter(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, GenericParameter[].class, parameters);
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
          result = result && (parameterTypes.length>=9);
          result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
          result = result && SubscriptionReference.class.isAssignableFrom(parameterTypes[PARAM_SUBSCRIPTION_REFERENCE]);
          result = result && Calendar.class.isAssignableFrom(parameterTypes[PARAM_START]);
          result = result && Calendar.class.isAssignableFrom(parameterTypes[PARAM_END]);
          result = result && Long.class.isAssignableFrom(parameterTypes[PARAM_CATEGORY]);
          result = result && String.class.isAssignableFrom(parameterTypes[PARAM_PAGE_KEY]);
          result = result && int.class.isAssignableFrom(parameterTypes[PARAM_LIMIT]);
          result = result && Boolean.class.isAssignableFrom(parameterTypes[PARAM_IS_ASCENDING]);
          result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
          return result;
      }
      
      @Override
      public boolean validateReturnType(Class<?> resultType)
      {
          return TransactionQueryResult.class.isAssignableFrom(resultType);
      }


      public static final int PARAM_HEADER = 0;
      public static final int PARAM_SUBSCRIPTION_REFERENCE = 1;
      public static final int PARAM_START = 2;
      public static final int PARAM_END = 3;
      public static final int PARAM_CATEGORY = 4;
      public static final int PARAM_PAGE_KEY = 5;
      public static final int PARAM_LIMIT = 6;
      public static final int PARAM_IS_ASCENDING = 7;
      public static final int PARAM_GENERIC_PARAMETERS = 8;
      
      public static final String PARAM_SUBSCRIPTION_REFERENCE_NAME = "subscriptionRef";
      public static final String PARAM_START_NAME = "start";
      public static final String PARAM_END_NAME = "end";
      public static final String PARAM_CATEGORY_NAME = "category";
      public static final String PARAM_PAGE_KEY_NAME = "pageKey";
      public static final String PARAM_LIMIT_NAME = "limit";
      public static final String PARAM_IS_ASCENDING_NAME = "isAscending";
      public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
   }
   
   /**
    * 
    * @author Marcio Marques
    * @since 9.3.0
    *
    */
   public static class DetailedSubscriptionTransactionsListQueryExecutor extends AbstractQueryExecutor<DetailedTransactionQueryResult>
   {
       public DetailedSubscriptionTransactionsListQueryExecutor()
       {
           
       }

       public DetailedTransactionQueryResult execute(Context ctx, Object... parameters) throws CRMExceptionFault
       {
           SubscriptionReference subscriptionRef = getParameter(ctx, PARAM_SUBSCRIPTION_REFERENCE, PARAM_SUBSCRIPTION_REFERENCE_NAME, SubscriptionReference.class, parameters);
           Calendar start = getParameter(ctx, PARAM_START, PARAM_START_NAME, Calendar.class, parameters);
           Calendar end = getParameter(ctx, PARAM_END, PARAM_END_NAME, Calendar.class, parameters);
           Long category = getParameter(ctx, PARAM_CATEGORY, PARAM_CATEGORY_NAME, Long.class, parameters);
           String pageKey = getParameter(ctx, PARAM_PAGE_KEY, PARAM_PAGE_KEY_NAME, String.class, parameters);
           int limit= getParameter(ctx, PARAM_LIMIT, PARAM_LIMIT_NAME, int.class, parameters);
           Boolean isAscending = getParameter(ctx, PARAM_IS_ASCENDING, PARAM_IS_ASCENDING_NAME, Boolean.class, parameters);

           RmiApiErrorHandlingSupport.validateMandatoryObject(subscriptionRef, PARAM_SUBSCRIPTION_REFERENCE_NAME);

           final Long longPageKey = RmiApiErrorHandlingSupport.validateLongPageKey(ctx, pageKey);

           // getting subscriber to validate it's existence
           final Subscriber sub = SubscribersApiSupport.getCrmSubscriber(ctx, subscriptionRef, this);

           final CRMSpid spid = RmiApiSupport.getCrmServiceProvider(ctx, sub.getSpid(), this);
           RmiApiErrorHandlingSupport.validateLimitInput(ctx, limit, spid.getMaxGetLimit());
           
           final EQ condition = new EQ(TransactionXInfo.SUBSCRIBER_ID, subscriptionRef.getIdentifier());
           
           Transaction[] transactions = new Transaction[] {};
           try
           {
               Collection<com.redknee.app.crm.bean.Transaction> collection = 
                   TransactionsApiSupport.getTransactionsUsingGivenParameters(
                       ctx, 
                       condition, 
                       start, 
                       end, 
                       category, 
                       longPageKey, 
                       limit, 
                       isAscending,
                       ProfileTypeEnum.SUBSCRIPTION.getValue(),
                       null,
                       this);

               transactions = new Transaction[collection.size()];
               int i = 0;
               ProfileType ownerType = ProfileTypeEnum.SUBSCRIPTION.getValue();
               for (com.redknee.app.crm.bean.Transaction transaction : collection)
               {
                   transactions[i++] = TransactionToApiAdapter.adaptTransactionToApi(ctx, transaction, ownerType);
               }
           }
           catch (final Exception e)
           {
               final String msg = "Unable to retrieve Transactions for start date " + start + " end date " + end
                   + " category " + category + " pageKey " + pageKey + " limit " + limit;
               RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
           }

           final DetailedTransactionQueryResult result = new DetailedTransactionQueryResult();
           result.setResults(transactions);
           if (transactions != null && transactions.length > 0)
           {
               result.setPageKey(String.valueOf(transactions[transactions.length - 1].getIdentifier()));
           }

           return result;
       }
       
       @Override
      public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
      {
           Object[] result = null;
          if (isGenericExecution(ctx, parameters))
          {
              result = new Object[9];
              result[0] = parameters[0];
              result[1] = getParameter(ctx, PARAM_SUBSCRIPTION_REFERENCE, PARAM_SUBSCRIPTION_REFERENCE_NAME, SubscriptionReference.class, parameters);
              result[2] = getParameter(ctx, PARAM_START, PARAM_START_NAME, Calendar.class, parameters);
              result[3] = getParameter(ctx, PARAM_END, PARAM_END_NAME, Calendar.class, parameters);
              result[4] = getParameter(ctx, PARAM_CATEGORY, PARAM_CATEGORY_NAME, Long.class, parameters);
              result[5] = getParameter(ctx, PARAM_PAGE_KEY, PARAM_PAGE_KEY_NAME, String.class, parameters);
              result[6] = getParameter(ctx, PARAM_LIMIT, PARAM_LIMIT_NAME, int.class, parameters);
              result[7] = getParameter(ctx, PARAM_IS_ASCENDING, PARAM_IS_ASCENDING_NAME, Boolean.class, parameters);
              result[8] = getParameter(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, GenericParameter[].class, parameters);
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
          result = result && (parameterTypes.length>=9);
          result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
          result = result && SubscriptionReference.class.isAssignableFrom(parameterTypes[PARAM_SUBSCRIPTION_REFERENCE]);
          result = result && Calendar.class.isAssignableFrom(parameterTypes[PARAM_START]);
          result = result && Calendar.class.isAssignableFrom(parameterTypes[PARAM_END]);
          result = result && Long.class.isAssignableFrom(parameterTypes[PARAM_CATEGORY]);
          result = result && String.class.isAssignableFrom(parameterTypes[PARAM_PAGE_KEY]);
          result = result && int.class.isAssignableFrom(parameterTypes[PARAM_LIMIT]);
          result = result && Boolean.class.isAssignableFrom(parameterTypes[PARAM_IS_ASCENDING]);
          result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
          return result;
      }

      @Override
      public boolean validateReturnType(Class<?> resultType)
      {
          return DetailedTransactionQueryResult.class.isAssignableFrom(resultType);
      }

      public static final int PARAM_HEADER = 0;
      public static final int PARAM_SUBSCRIPTION_REFERENCE = 1;
      public static final int PARAM_START = 2;
      public static final int PARAM_END = 3;
      public static final int PARAM_CATEGORY = 4;
      public static final int PARAM_PAGE_KEY = 5;
      public static final int PARAM_LIMIT = 6;
      public static final int PARAM_IS_ASCENDING = 7;
      public static final int PARAM_GENERIC_PARAMETERS = 8;
      
      public static final String PARAM_SUBSCRIPTION_REFERENCE_NAME = "subscriptionRef";
      public static final String PARAM_START_NAME = "start";
      public static final String PARAM_END_NAME = "end";
      public static final String PARAM_CATEGORY_NAME = "category";
      public static final String PARAM_PAGE_KEY_NAME = "pageKey";
      public static final String PARAM_LIMIT_NAME = "limit";
      public static final String PARAM_IS_ASCENDING_NAME = "isAscending";
      public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
   }
   
   /**
    * 
    * @author Marcio Marques
    * @since 9.3.0
    *
    */
   public static class TransactionsByExternalTransactionNumberListQueryExecutor extends AbstractQueryExecutor<TransactionQueryResult>
   {
       public TransactionsByExternalTransactionNumberListQueryExecutor()
       {
           
       }

       public TransactionQueryResult execute(Context ctx, Object... parameters) throws CRMExceptionFault
       {
           CRMRequestHeader header = getParameter(ctx, PARAM_HEADER, "header", CRMRequestHeader.class, parameters);
           String externalTransactionNumber = getParameter(ctx, PARAM_EXTERNAL_TRANSACTION_NUMBER, PARAM_EXTERNAL_TRANSACTION_NUMBER_NAME, String.class, parameters);
           String accountID = getParameter(ctx, PARAM_ACCOUNT_ID, PARAM_ACCOUNT_ID_NAME, String.class, parameters);
           SubscriptionReference subscriptionRef = getParameter(ctx, PARAM_SUBSCRIPTION_REFERENCE, PARAM_SUBSCRIPTION_REFERENCE_NAME, SubscriptionReference.class, parameters);
           Calendar start = getParameter(ctx, PARAM_START, PARAM_START_NAME, Calendar.class, parameters);
           Calendar end = getParameter(ctx, PARAM_END, PARAM_END_NAME, Calendar.class, parameters);
           Long category = getParameter(ctx, PARAM_CATEGORY, PARAM_CATEGORY_NAME, Long.class, parameters);
           String pageKey = getParameter(ctx, PARAM_PAGE_KEY, PARAM_PAGE_KEY_NAME, String.class, parameters);
           int limit= getParameter(ctx, PARAM_LIMIT, PARAM_LIMIT_NAME, int.class, parameters);
           Boolean isAscending = getParameter(ctx, PARAM_IS_ASCENDING, PARAM_IS_ASCENDING_NAME, Boolean.class, parameters);
           GenericParameter[] genericParameters = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);

           RmiApiErrorHandlingSupport.validateMandatoryObject(externalTransactionNumber, PARAM_ACCOUNT_ID_NAME);

           RmiApiErrorHandlingSupport.validateMandatoryObject(externalTransactionNumber, "ExternalTransactionNumber");

           final Long longPageKey = RmiApiErrorHandlingSupport.validateLongPageKey(ctx, pageKey);
           final And condition = new And();
           condition.add(new EQ(TransactionXInfo.EXT_TRANSACTION_ID, externalTransactionNumber));
           
           if (subscriptionRef!=null)
           {
               SubscribersApiSupport.updateReferenceId(ctx, subscriptionRef, this);

               final CRMSpid spid = RmiApiSupport.getCrmServiceProvider(ctx, subscriptionRef.getSpid().intValue(), this);
               RmiApiErrorHandlingSupport.validateLimitInput(ctx, limit, spid.getMaxGetLimit());
               condition.add(new EQ(TransactionXInfo.SUBSCRIBER_ID, subscriptionRef.getIdentifier()));
           }
           
           if (accountID!=null && !accountID.trim().isEmpty())
           {
               condition.add(new EQ(TransactionXInfo.BAN, accountID));
           }


           return listTransactions(ctx, header, condition, start, end, category, longPageKey, limit, isAscending, genericParameters, ProfileTypeEnum.SUBSCRIPTION.getValue(), null, null, this);
       }
       
       @Override
      public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
      {
           Object[] result = null;
          if (isGenericExecution(ctx, parameters))
          {
              result = new Object[11];
              result[0] = parameters[0];
              result[1] = getParameter(ctx, PARAM_EXTERNAL_TRANSACTION_NUMBER, PARAM_EXTERNAL_TRANSACTION_NUMBER_NAME, String.class, parameters);
              result[2] = getParameter(ctx, PARAM_ACCOUNT_ID, PARAM_ACCOUNT_ID_NAME, String.class, parameters);
              result[3] = getParameter(ctx, PARAM_SUBSCRIPTION_REFERENCE, PARAM_SUBSCRIPTION_REFERENCE_NAME, SubscriptionReference.class, parameters);
              result[4] = getParameter(ctx, PARAM_START, PARAM_START_NAME, Calendar.class, parameters);
              result[5] = getParameter(ctx, PARAM_END, PARAM_END_NAME, Calendar.class, parameters);
              result[6] = getParameter(ctx, PARAM_CATEGORY, PARAM_CATEGORY_NAME, Long.class, parameters);
              result[7] = getParameter(ctx, PARAM_PAGE_KEY, PARAM_PAGE_KEY_NAME, String.class, parameters);
              result[8] = getParameter(ctx, PARAM_LIMIT, PARAM_LIMIT_NAME, int.class, parameters);
              result[9] = getParameter(ctx, PARAM_IS_ASCENDING, PARAM_IS_ASCENDING_NAME, Boolean.class, parameters);
              result[10] = getParameter(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, GenericParameter[].class, parameters);
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
          result = result && (parameterTypes.length>=11);
          result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
          result = result && String.class.isAssignableFrom(parameterTypes[PARAM_EXTERNAL_TRANSACTION_NUMBER]);
          result = result && String.class.isAssignableFrom(parameterTypes[PARAM_ACCOUNT_ID]);
          result = result && SubscriptionReference.class.isAssignableFrom(parameterTypes[PARAM_SUBSCRIPTION_REFERENCE]);
          result = result && Calendar.class.isAssignableFrom(parameterTypes[PARAM_START]);
          result = result && Calendar.class.isAssignableFrom(parameterTypes[PARAM_END]);
          result = result && Long.class.isAssignableFrom(parameterTypes[PARAM_CATEGORY]);
          result = result && String.class.isAssignableFrom(parameterTypes[PARAM_PAGE_KEY]);
          result = result && int.class.isAssignableFrom(parameterTypes[PARAM_LIMIT]);
          result = result && Boolean.class.isAssignableFrom(parameterTypes[PARAM_IS_ASCENDING]);
          result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
          return result;
      }

      @Override
      public boolean validateReturnType(Class<?> resultType)
      {
          return TransactionQueryResult.class.isAssignableFrom(resultType);
      }

      public static final int PARAM_HEADER = 0;
      public static final int PARAM_EXTERNAL_TRANSACTION_NUMBER = 1;
      public static final int PARAM_ACCOUNT_ID = 2;
      public static final int PARAM_SUBSCRIPTION_REFERENCE = 3;
      public static final int PARAM_START = 4;
      public static final int PARAM_END = 5;
      public static final int PARAM_CATEGORY = 6;
      public static final int PARAM_PAGE_KEY = 7;
      public static final int PARAM_LIMIT = 8;
      public static final int PARAM_IS_ASCENDING = 9;
      public static final int PARAM_GENERIC_PARAMETERS = 10;
      
      public static final String PARAM_EXTERNAL_TRANSACTION_NUMBER_NAME = "externalTransactionNumber";
      public static final String PARAM_ACCOUNT_ID_NAME = "accountID";
      public static final String PARAM_SUBSCRIPTION_REFERENCE_NAME = "subscriptionRef";
      public static final String PARAM_START_NAME = "start";
      public static final String PARAM_END_NAME = "end";
      public static final String PARAM_CATEGORY_NAME = "category";
      public static final String PARAM_PAGE_KEY_NAME = "pageKey";
      public static final String PARAM_LIMIT_NAME = "limit";
      public static final String PARAM_IS_ASCENDING_NAME = "isAscending";
      public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
   }
   
   /**
    * 
    * @author Marcio Marques
    * @since 9.3.0
    *
    */
   public static class AdjustmentTypesListQueryExecutor extends AbstractQueryExecutor<AdjustmentTypeReference[]>
   {
       public AdjustmentTypesListQueryExecutor()
       {
           
       }

       public AdjustmentTypeReference[] execute(Context ctx, Object... parameters) throws CRMExceptionFault
       {
           long category = getParameter(ctx, PARAM_CATEGORY, PARAM_CATEGORY_NAME, long.class, parameters);
           Boolean recurse= getParameter(ctx, PARAM_RECURSE, PARAM_RECURSE_NAME, Boolean.class, parameters);

           boolean recVal = false;
           if (recurse != null)
           {
               recVal = recurse.booleanValue();
           }
           
           final com.redknee.app.crm.bean.core.AdjustmentType adjustmentType = 
                TransactionsApiSupport.getCrmAdjustmentType(ctx, category, this);

           AdjustmentTypeReference[] adjustmentTypeReferences = new AdjustmentTypeReference[] {};
           try
           {
               final Collection<com.redknee.app.crm.bean.core.AdjustmentType> collection = adjustmentType.getDescendants(ctx, recVal, true);
               collection.add(adjustmentType);
               
               adjustmentTypeReferences = CollectionSupportHelper.get(ctx).adaptCollection(
                       ctx, 
                       collection, 
                       adjustmentTypeAdapter_, 
                       adjustmentTypeReferences);
           }
           catch (final Exception e)
           {
               final String msg = "Unable to retrieve Dealer Codes for category=" + category + " recurse=" + recurse;
               RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
           }

           return adjustmentTypeReferences;       }
       
       @Override
      public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
      {
           Object[] result = null;
          if (isGenericExecution(ctx, parameters))
          {
              result = new Object[5];
              result[0] = parameters[0];
              result[1] = getParameter(ctx, PARAM_CATEGORY, PARAM_CATEGORY_NAME, long.class, parameters);
              result[2] = getParameter(ctx, PARAM_RECURSE, PARAM_RECURSE_NAME, Boolean.class, parameters);
              result[3] = getParameter(ctx, PARAM_IS_ASCENDING, PARAM_IS_ASCENDING_NAME, Boolean.class, parameters);
              result[4] = getParameter(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, GenericParameter[].class, parameters);
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
          result = result && (parameterTypes.length>=5);
          result = result && CRMRequestHeader.class.isAssignableFrom(parameterTypes[PARAM_HEADER]);
          result = result && long.class.isAssignableFrom(parameterTypes[PARAM_CATEGORY]);
          result = result && Boolean.class.isAssignableFrom(parameterTypes[PARAM_RECURSE]);
          result = result && Boolean.class.isAssignableFrom(parameterTypes[PARAM_IS_ASCENDING]);
          result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
          return result;
      }
      
      @Override
      public boolean validateReturnType(Class<?> resultType)
      {
          return AdjustmentTypeReference[].class.isAssignableFrom(resultType);
      }


      public static final int PARAM_HEADER = 0;
      public static final int PARAM_CATEGORY = 1;
      public static final int PARAM_RECURSE = 2;
      public static final int PARAM_IS_ASCENDING = 3;
      public static final int PARAM_GENERIC_PARAMETERS = 4;
      
      public static final String PARAM_CATEGORY_NAME = "category";
      public static final String PARAM_RECURSE_NAME = "recurse";
      public static final String PARAM_IS_ASCENDING_NAME = "isAscending";
      public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
   }
   
   /**
     * 
     * @author Marcio Marques
     * @since 9.3.0
     *
     */
    public static class AdjustmentTypeQueryExecutor extends AbstractQueryExecutor<AdjustmentType>
    {
        public AdjustmentTypeQueryExecutor()
        {
            
        }

        public AdjustmentType execute(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            long identifier = getParameter(ctx, PARAM_IDENTIFIER, PARAM_IDENTIFIER_NAME, long.class, parameters);
            int spid = getParameter(ctx, PARAM_SPID, PARAM_SPID_NAME, int.class, parameters);

            // getting Service Provider to validate it's existence
            RmiApiSupport.getCrmServiceProvider(ctx, spid, this);

            final com.redknee.app.crm.bean.AdjustmentType adjustmentType = 
                TransactionsApiSupport.getCrmAdjustmentType(ctx, identifier, this);

            final AdjustmentType type = AdjustmentTypeToApiAdapter.adaptAdjustmentTypeToApi(ctx, adjustmentType, spid);

            return type;

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
               result[2] = getParameter(ctx, PARAM_IDENTIFIER, PARAM_IDENTIFIER_NAME, long.class, parameters);
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
           result = result && long.class.isAssignableFrom(parameterTypes[PARAM_IDENTIFIER]);
           result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
           return result;
       }
       
       @Override
       public boolean validateReturnType(Class<?> resultType)
       {
           return AdjustmentType.class.isAssignableFrom(resultType);
       }


       public static final int PARAM_HEADER = 0;
       public static final int PARAM_SPID = 1;
       public static final int PARAM_IDENTIFIER = 2;
       public static final int PARAM_GENERIC_PARAMETERS = 3;
       
       public static final String PARAM_SPID_NAME = "spid";
       public static final String PARAM_IDENTIFIER_NAME = "identifier";
       public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
    }
    

    /**
     * 
     * @author Marcio Marques
     * @since 9.3.0
     *
     */
    public static class TransactionQueryExecutor extends AbstractQueryExecutor<Transaction>
    {
        public TransactionQueryExecutor()
        {
            
        }

        public Transaction execute(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            long transactionID = getParameter(ctx, PARAM_TRANSACTION_ID, PARAM_TRANSACTION_ID_NAME, long.class, parameters);
            ProfileType ownerType = getParameter(ctx, PARAM_OWNER_TYPE, PARAM_OWNER_TYPE_NAME, ProfileType.class, parameters);

            com.redknee.app.crm.bean.Transaction transaction = null;

            Context subCtx = ctx;
            if (ownerType!=null && ProfileTypeEnum.ACCOUNT.getValue().getValue() == ownerType.getValue())
            {
                subCtx = ctx.createSubContext();
                subCtx.put(com.redknee.app.crm.bean.Transaction.class, ctx.get(Common.ACCOUNT_TRANSACTION_HOME));
            }
            
            try
            {
                final Object condition = new EQ(TransactionXInfo.RECEIPT_NUM, transactionID);
                transaction = HomeSupportHelper.get(subCtx).findBean(subCtx, com.redknee.app.crm.bean.Transaction.class, condition);
            }
            catch (final Exception e)
            {
                final String msg = "Unable to retrieve Transaction " + transactionID;
                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
            }

            if (transaction == null)
            {
                final String msg = "Transaction " + transactionID;
                RmiApiErrorHandlingSupport.identificationException(ctx, msg, this);
            }

            final Transaction result = TransactionToApiAdapter.adaptTransactionToApi(ctx, transaction, ownerType!=null?ownerType:ProfileTypeEnum.SUBSCRIPTION.getValue());

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
               result[1] = getParameter(ctx, PARAM_TRANSACTION_ID, PARAM_TRANSACTION_ID_NAME, long.class, parameters);
               result[2] = getParameter(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, GenericParameter[].class, parameters);
               result[3] = getParameter(ctx, PARAM_OWNER_TYPE, PARAM_OWNER_TYPE_NAME, ProfileType.class, parameters);
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
           result = result && long.class.isAssignableFrom(parameterTypes[PARAM_TRANSACTION_ID]);
           result = result && ProfileType.class.isAssignableFrom(parameterTypes[PARAM_OWNER_TYPE]);
           result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
           return result;
       }
       
       @Override
       public boolean validateReturnType(Class<?> resultType)
       {
           return Transaction.class.isAssignableFrom(resultType);
       }


       public static final int PARAM_HEADER = 0;
       public static final int PARAM_TRANSACTION_ID = 1;
       public static final int PARAM_GENERIC_PARAMETERS = 2;
       public static final int PARAM_OWNER_TYPE = 3;
       
       public static final String PARAM_TRANSACTION_ID_NAME = "transactionID";
       public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
       public static final String PARAM_OWNER_TYPE_NAME = "ownerType";
    }
    

    /**
     * 
     * @author Marcio Marques
     * @since 9.3.0
     *
     */
    public static class AccountTransactionCreationQueryExecutor extends AbstractQueryExecutor<TransactionReference>
    {
        public AccountTransactionCreationQueryExecutor()
        {
            
        }

        public TransactionReference execute(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            String accountID = getParameter(ctx, PARAM_ACCOUNT_ID, PARAM_ACCOUNT_ID_NAME, String.class, parameters);
            TransactionRequest request = getParameter(ctx, PARAM_TRANSACTION_REQUEST, PARAM_TRANSACTION_REQUEST_NAME, TransactionRequest.class, parameters);
            GenericParameter[] genericParameters = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);

            RmiApiErrorHandlingSupport.validateMandatoryObject(accountID, PARAM_ACCOUNT_ID_NAME);
            RmiApiErrorHandlingSupport.validateMandatoryObject(request, PARAM_TRANSACTION_REQUEST_NAME);
            TransactionReference result = new TransactionReference();
            if (!TransactionApiRerouteDcrmSupport.redirectRequestToDCRM(ctx, request, genericParameters, result))
            {
                Home home = ApiSupport.injectAPIUpdateEntityHomeIntoPipeline(ctx,
                        (Home) ctx.get(com.redknee.app.crm.bean.TransactionHome.class));
                result = null;
                // getting account to validate it's existence
                Account account = AccountsImpl.getCrmAccount(ctx, accountID, this);
                
                com.redknee.app.crm.bean.Transaction transaction = null;
                try
                {
                    if (request.getSubscriptionType() == null)
                    {
                        request.setSubscriptionType(RmiApiSupport.getDefaultSubscriptionType(ctx, account.getSpid(), this));
                    }
                    
                    transaction = TransactionToApiAdapter.adaptTransactionRequestToTransaction(ctx, request);
                    transaction.setBAN(accountID);
                    transaction.setPayee(PayeeEnum.Account);
                    /*
                     * [Cindy Wong] 2010-02-21: Make sure the tranction's limit validated.
                     */
                    Context subCtx = ctx.createSubContext();
                    home = new ValidatingOverPaymentTransactionHome(home);
                    subCtx.put(TransactionHome.class, home);

                    CompoundValidator createValidator = new CompoundValidator(); 
                    createValidator.add(new UserDailyAdjustmentLimitTransactionValidator()); 

                    CompoundValidator storeValidator = new CompoundValidator(); 

                    final Home enforcementHome = new ValidatingHome(createValidator, storeValidator,
                            new UserDailyAdjustmentLimitTransactionIncreaseHome(subCtx,
                                    (Home) subCtx.get(TransactionHome.class)));
                    
                    subCtx.put(TransactionHome.class, enforcementHome);
                    
                    subCtx.put(TransactionAdjustmentTypeLimitValidator.VALIDATE_KEY, true);
                    final com.redknee.app.crm.bean.Transaction resultTransaction = (com.redknee.app.crm.bean.Transaction) home.create(subCtx, transaction);
                    if (resultTransaction != null)
                    {
                        result = (TransactionReference) transactionAdapter_.adapt(ctx, resultTransaction);
                    }
                }
                catch (final Exception e)
                {
                    final String msg = "Unable to create Transaction for Account=" + accountID;
                    Object id = null;
                    if (transaction != null)
                    {
                        id = transaction.getReceiptNum();
                    }
                    RmiApiErrorHandlingSupport.handleCreateExceptions(ctx, e, msg, false, com.redknee.app.crm.bean.Transaction.class, id, this);
                }
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
               result[2] = getParameter(ctx, PARAM_TRANSACTION_REQUEST, PARAM_TRANSACTION_REQUEST_NAME, TransactionRequest.class, parameters);
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
           result = result && TransactionRequest.class.isAssignableFrom(parameterTypes[PARAM_TRANSACTION_REQUEST]);
           result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
           return result;
       }
       
       @Override
       public boolean validateReturnType(Class<?> resultType)
       {
           return TransactionReference.class.isAssignableFrom(resultType);
       }


       public static final int PARAM_HEADER = 0;
       public static final int PARAM_ACCOUNT_ID = 1;
       public static final int PARAM_TRANSACTION_REQUEST = 2;
       public static final int PARAM_GENERIC_PARAMETERS = 3;
       
       public static final String PARAM_ACCOUNT_ID_NAME = "accountID";
       public static final String PARAM_TRANSACTION_REQUEST_NAME = "request";
       public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
    }
    
    /**
     * 
     * @author Marcio Marques
     * @since 9.3.0
     *
     */
    public static class SubscriptionTransactionCreationQueryExecutor extends AbstractQueryExecutor<TransactionReference>
    {
        public SubscriptionTransactionCreationQueryExecutor()
        {
            
        }

        public TransactionReference execute(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            SubscriptionReference subscriptionRef = getParameter(ctx, PARAM_SUBSCRIPTION_REFERENCE, PARAM_SUBSCRIPTION_REFERENCE_NAME, SubscriptionReference.class, parameters);
            TransactionRequest request = getParameter(ctx, PARAM_TRANSACTION_REQUEST, PARAM_TRANSACTION_REQUEST_NAME, TransactionRequest.class, parameters);
            GenericParameter[] genericParameters = getGenericParameters(ctx, PARAM_GENERIC_PARAMETERS, PARAM_GENERIC_PARAMETERS_NAME, parameters);

            RmiApiErrorHandlingSupport.validateMandatoryObject(request, PARAM_TRANSACTION_REQUEST_NAME);

            TransactionReference result = new TransactionReference();

            if (!TransactionApiRerouteDcrmSupport.redirectRequestToDCRM(ctx, request, genericParameters, result))
            {
                result = null;
                final Subscriber subscriber = SubscribersApiSupport.getCrmSubscriber(ctx, subscriptionRef, this);
                final Map<String, Object> genericParameterMap = APIGenericParameterSupport.createGenericParameterMap(genericParameters);
               
                com.redknee.app.crm.bean.Transaction transaction = null;
                try
                {
                    transaction = TransactionToApiAdapter.adaptTransactionRequestToTransaction(ctx, request, subscriber,
                            genericParameterMap);
                    transaction.setSubscriberID(subscriber.getId());
                    transaction.setBAN(subscriber.getBAN());
                    transaction.setMSISDN(subscriber.getMSISDN());
                    transaction.setSubscriptionTypeId(subscriber.getSubscriptionType());

                    
                    Home home;
                    
                    boolean unappliedTransaction = APIGenericParameterSupport.isUnappliedTransaction(genericParameterMap); 
                    // Deciding which home to use based on whether or not this is an unapplied transaction.
                    if(unappliedTransaction) 
                    {
                        home = ((Home)ctx.get(Common.UNAPPLIED_TRANSACTION_HOME));
                    }
                    else
                    {
                        home = (Home) ctx.get(com.redknee.app.crm.bean.TransactionHome.class);
                    }

                    home = ApiSupport.injectAPIUpdateEntityHomeIntoPipeline(ctx, home);

                    /*
                     * [Cindy Wong] 2010-02-21: Make sure the tranction's limit validated.
                     */
                    Context subCtx = ctx.createSubContext();
                    home = new ValidatingOverPaymentTransactionHome(home);
                    subCtx.put(TransactionHome.class, home);
                    
                    subCtx.put(TransactionAdjustmentTypeLimitValidator.VALIDATE_KEY, true);
                    CompoundValidator createValidator = new CompoundValidator(); 
                    createValidator.add(new UserDailyAdjustmentLimitTransactionValidator()); 
                    
                    final Home enforcementHome =
                            new ValidatingHome(createValidator, 
                                    new Validator()
                                    {
                                        @Override
                                        public void validate(Context ctx, Object obj) throws IllegalStateException
                                        {
                                        }
                                        
                                    },
                                    new UserDailyAdjustmentLimitTransactionIncreaseHome(subCtx, home));
                    subCtx.put(TransactionHome.class, enforcementHome);
                    
                    /*
                     * [Cindy Wong] 2011-05-05: Cache the subscriber. This should
                     * allow transaction creation at subscriber creation/activation
                     * time, where the transDate < MSISDN assignment time.
                     */
                    subCtx.put(Subscriber.class, subscriber);

                    com.redknee.app.crm.bean.Transaction resultTransaction = null;
                    
                    if(!unappliedTransaction)
                    {
	                    resultTransaction = (com.redknee.app.crm.bean.Transaction) enforcementHome
	                            .create(subCtx, transaction);
                    }
                    else
                    {
                    	resultTransaction = (com.redknee.app.crm.bean.Transaction) home.
                    	create(subCtx, transaction);
                    }
                    
                    if (resultTransaction != null)
                    {
                        result = (TransactionReference) transactionAdapter_.adapt(ctx, resultTransaction);
                    }
                    if (TransactionsApiSupport.requiresVRAClawbackTransaction(ctx, genericParameterMap))
                    {
                        TransactionsApiSupport.createVRAClawbackTransaction(subCtx, subscriber, transaction, genericParameterMap);
                    }
                    /*
                     * Call Notification for TFA
                     */
                    if(transaction.getFromTFAPoller())
                    {
                        if(transaction.getBalance() != 0)
                        {
                            SubscriptionNotificationSupport.sendTFANotification(ctx, subscriber, transaction);
                        }
                    }
                    /*
                     * Send Notification for External Transaction
                     */
                    final String originatingApplication = (String)genericParameterMap.get(APIGenericParameterSupport.ORIGINATING_APPLICATION);
                    if (APIGenericParameterSupport.EXT.equals(originatingApplication))
                    {
                        SubscriptionNotificationSupport.sendExternalTransactionNotification(ctx, subscriber, transaction.getAmount());
                    }
                }
                catch (final com.redknee.util.crmapi.wsdl.v3_0.api.CRMExceptionFault e)
                {
                    throw e;
                }
                catch (final Exception e)
                {
                    final String msg = "Unable to create Transaction for Subscriber=" + subscriptionRef.getIdentifier();
                    Object id = null;
                    if (transaction != null)
                    {
                        id = transaction.getReceiptNum();
                    }
                    RmiApiErrorHandlingSupport.handleCreateExceptions(ctx, e, msg, false, com.redknee.app.crm.bean.Transaction.class, id, this);
                }
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
               result[1] = getParameter(ctx, PARAM_SUBSCRIPTION_REFERENCE, PARAM_SUBSCRIPTION_REFERENCE_NAME, SubscriptionReference.class, parameters);
               result[2] = getParameter(ctx, PARAM_TRANSACTION_REQUEST, PARAM_TRANSACTION_REQUEST_NAME, TransactionRequest.class, parameters);
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
           result = result && SubscriptionReference.class.isAssignableFrom(parameterTypes[PARAM_SUBSCRIPTION_REFERENCE]);
           result = result && TransactionRequest.class.isAssignableFrom(parameterTypes[PARAM_TRANSACTION_REQUEST]);
           result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
           return result;
       }
       
       @Override
       public boolean validateReturnType(Class<?> resultType)
       {
           return TransactionReference.class.isAssignableFrom(resultType);
       }


       public static final int PARAM_HEADER = 0;
       public static final int PARAM_SUBSCRIPTION_REFERENCE = 1;
       public static final int PARAM_TRANSACTION_REQUEST = 2;
       public static final int PARAM_GENERIC_PARAMETERS = 3;
       
       public static final String PARAM_SUBSCRIPTION_REFERENCE_NAME = "subscriptionRef";
       public static final String PARAM_TRANSACTION_REQUEST_NAME = "request";
       public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
    }
    
    /**
     * Lists transactions. Not part of the CRM API.
     *
     * @param ctx
     *            The operating context.
     * @param header
     *            CRM API request header.
     * @param parentCondition
     *            parentCondition
     * @param start
     *            Start date.
     * @param end
     *            End date.
     * @param category
     *            Adjustment category.
     * @param pageKey
     *            Page key.
     * @param limit
     *            Limit.
     * @return Result of the query.
     * @throws RemoteException
     *             Thrown if there are problems looking up the transactions.
     */
    private static TransactionQueryResult listTransactions(final Context ctx, final CRMRequestHeader header,
            final Object parentCondition, final Calendar start, final Calendar end, final Long category,
            final Long pageKey, final int limit, final Boolean isAscending, GenericParameter[] parameters, ProfileType profileType, Long accountLevelID, String accountID, Object caller) throws CRMExceptionFault
    {
     
        TransactionReference[] transactionReferences = new TransactionReference[] {};
        TransactionReference[] subscriptionTransactionReferences = new TransactionReference[] {};
        TransactionReference[] accountTransactionReferences = new TransactionReference[] {};
        
        TransactionToApiAdapter transactionAdapter = transactionAdapter_;
        try
        {
            Collection<com.redknee.app.crm.bean.Transaction> accountCollection = new ArrayList<com.redknee.app.crm.bean.Transaction>();
            Collection<com.redknee.app.crm.bean.Transaction> subscriptionCollection = new ArrayList<com.redknee.app.crm.bean.Transaction>();
            And condition = new And();
            condition.add(parentCondition);
            
            if (ProfileTypeEnum.ACCOUNT.getValue().getValue() == profileType.getValue())
            {
                accountCollection.addAll(  
                        TransactionsApiSupport.getTransactionsUsingGivenParameters(
                                ctx,
                                new EQ(TransactionXInfo.BAN, accountID), 
                                start, 
                                end, 
                                category, 
                                pageKey, 
                                limit, 
                                isAscending,
                                ProfileTypeEnum.ACCOUNT.getValue(),
                                accountLevelID,
                                caller));
                
                Set<Long> accountReceiptNums = new HashSet<Long>();
                
                for (com.redknee.app.crm.bean.Transaction transaction : accountCollection)
                {
                    accountReceiptNums.add(transaction.getReceiptNum());
                }
                
                condition.add(new Not(new In(TransactionXInfo.ACCOUNT_RECEIPT_NUM, accountReceiptNums)));
                
                
            }
            
            if (parameters!=null)
            {
                GenericParameterParser parser = new GenericParameterParser(parameters);
                
                Integer spid = parser.getParameter(SPID, Integer.class);
                Boolean returnTransactions = parser.getParameter(RETURN_TRANSACTIONS, Boolean.class);

                if (spid != null)
                {
                    condition.add(new EQ(TransactionXInfo.SPID, spid));
                }
                
                if (returnTransactions!=null && returnTransactions)
                {
                    transactionAdapter = new TransactionToApiAdapter(){
                        private static final long serialVersionUID = 1L;
                        @Override
                        public Object adapt(final Context ctx, final Object obj) throws HomeException
                        {
                            return adaptTransactionToApi(ctx,
                                (com.redknee.app.crm.bean.Transaction) obj);
                        }
                    };
                }
            }

            subscriptionCollection.addAll( 
                TransactionsApiSupport.getTransactionsUsingGivenParameters(
                        ctx,
                        condition, 
                        start, 
                        end, 
                        category, 
                        pageKey, 
                        limit, 
                        isAscending,
                        ProfileTypeEnum.SUBSCRIPTION.getValue(),
                        accountLevelID,
                        caller));
            
            
            accountTransactionReferences = CollectionSupportHelper.get(ctx).adaptCollection(
                    ctx, 
                    accountCollection, 
                    transactionAdapter, 
                    transactionReferences);
            
            for (TransactionReference transaction : accountTransactionReferences)
            {
                GenericParameter gp = new GenericParameter();
                gp.setName(OWNER_TYPE);
                gp.setValue(ProfileTypeEnum.ACCOUNT.getValue());
                transaction.addParameters(gp);
            }

            subscriptionTransactionReferences = CollectionSupportHelper.get(ctx).adaptCollection(
                    ctx, 
                    subscriptionCollection, 
                    transactionAdapter, 
                    transactionReferences);
            
            for (TransactionReference transaction : subscriptionTransactionReferences)
            {
                GenericParameter gp = new GenericParameter();
                gp.setName(OWNER_TYPE);
                gp.setValue(ProfileTypeEnum.SUBSCRIPTION.getValue());
                transaction.addParameters(gp);
            }
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Transactions for start date " + start + " end date " + end
                + " category " + category + " pageKey " + pageKey + " limit " + limit;
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, caller);
        }

        final TransactionQueryResult result = new TransactionQueryResult();

        transactionReferences = new TransactionReference[subscriptionTransactionReferences.length + accountTransactionReferences.length];
        
        int i = 0;
        for (TransactionReference transaction : accountTransactionReferences)
        {
            transactionReferences[i++] = transaction;
        }

        for (TransactionReference transaction : subscriptionTransactionReferences)
        {
            transactionReferences[i++] = transaction;
        }

        result.setReferences(transactionReferences);
        if (transactionReferences != null && transactionReferences.length > 0)
        {
            result.setPageKey(String.valueOf(transactionReferences[transactionReferences.length - 1].getIdentifier()));
        }

        return result;

    }
    
    
    /**
     * Lists Sub Account transactions. Not part of the CRM API.
     *
     * @param ctx
     *            The operating context.
     * @param header
     *            CRM API request header.
     * @param parentCondition
     *            parentCondition
     * @param start
     *            Start date.
     * @param end
     *            End date.
     * @param category
     *            Adjustment category.
     * @param pageKey
     *            Page key.
     * @param limit
     *            Limit.
     * @return Result of the query.
     * @throws RemoteException
     *             Thrown if there are problems looking up the transactions.
     */
    private static SubAccountReference listSubAccountTransactions(final Context ctx, final CRMRequestHeader header,
            final Object parentCondition, final Calendar start, final Calendar end, final Long category,
            final Long pageKey, final int limit, final Boolean isAscending, GenericParameter[] parameters, ProfileType profileType, Long accountLevelID, Account accountID, Object caller) throws CRMExceptionFault
    {
     
        SubAccountTransactionReference[] transactionReferences = new SubAccountTransactionReference[] {};
        
        SubAccountTransactionReference[] subscriptionTransactionReferences = new SubAccountTransactionReference[] {};
        SubAccountTransactionReference[] accountTransactionReferences = new SubAccountTransactionReference[] {};
        
        SubAccountTransactionToApiAdapter transactionAdapter = subAccountTransactionAdapter_;
        
        
        try
        {
            Collection<com.redknee.app.crm.bean.Transaction> accountCollection = new ArrayList<com.redknee.app.crm.bean.Transaction>();
            Collection<com.redknee.app.crm.bean.Transaction> subscriptionCollection = new ArrayList<com.redknee.app.crm.bean.Transaction>();
            And condition = new And();
            condition.add(parentCondition);
            if (ProfileTypeEnum.ACCOUNT.getValue().getValue() == profileType.getValue())
            {
                accountCollection.addAll(
                        TransactionsApiSupport.getTransactionsUsingGivenParameters(
                                ctx,
                                new EQ(TransactionXInfo.BAN, accountID.getBAN()), 
                                start, 
                                end, 
                                category, 
                                null, 
                                limit, 
                                /*isAscending*/false, // In order to get most recent records, we always fetch it in descending order
                                ProfileTypeEnum.ACCOUNT.getValue(),
                                accountLevelID,
                                caller));
                
                Set<Long> accountReceiptNums = new HashSet<Long>();
                
                for (com.redknee.app.crm.bean.Transaction transaction : accountCollection)
                {
                    accountReceiptNums.add(transaction.getReceiptNum());
                }
                
                condition.add(new Not(new In(TransactionXInfo.ACCOUNT_RECEIPT_NUM, accountReceiptNums)));
                
                
            }
            
            if (parameters!=null)
            {

                GenericParameterParser parser = new GenericParameterParser(parameters);               
                Integer spid = parser.getParameter(SPID, Integer.class);
                Boolean returnTransactions = parser.getParameter(RETURN_TRANSACTIONS, Boolean.class);
                
                if (spid != null)
                {
                    condition.add(new EQ(TransactionXInfo.SPID, spid));
                }
                
                if (returnTransactions!=null && returnTransactions)
                {
                    transactionAdapter = new SubAccountTransactionToApiAdapter(){
                        private static final long serialVersionUID = 1L;
                        @Override
                        public Object adapt(final Context ctx, final Object obj) throws HomeException
                        {
                            return adaptTransactionToApi(ctx,
                                (com.redknee.app.crm.bean.Transaction) obj);
                        }
                    };
                }
                
            }

            subscriptionCollection.addAll( 
                TransactionsApiSupport.getTransactionsUsingGivenParameters(
                        ctx,
                        condition, 
                        start, 
                        end, 
                        category, 
                        /*pageKey*/null, 
                        limit, 
                        /*isAscending*/false, // In order to get most recent records, we always fetch it in descending order
                        ProfileTypeEnum.SUBSCRIPTION.getValue(),
                        accountLevelID,
                        caller));
            
            
            accountTransactionReferences = CollectionSupportHelper.get(ctx).adaptCollection(
                    ctx, 
                    accountCollection, 
                    transactionAdapter, 
                    transactionReferences);
            
            for (SubAccountTransactionReference transaction : accountTransactionReferences)
            {
                GenericParameter gp = new GenericParameter();
                gp.setName(OWNER_TYPE);
                gp.setValue(ProfileTypeEnum.ACCOUNT.getValue());
				transaction.addParameters(gp);
                
				com.redknee.app.crm.bean.core.Transaction trans=getAccountTransactionForGenericParamter(ctx,transaction.getIdentifier(),/*transaction.getAccountID()*/accountID.getBAN() );
                GenericParameter gp1 = new GenericParameter();
                gp1.setName(Constants.SERVICE_RECOGNIZED_DATE);
                gp1.setValue(trans.getServiceRevenueRecognizedDate());
                transaction.addParameters(gp1);
                
                GenericParameter gp2 = new GenericParameter();
                gp2.setName(Constants.TRANSACTION_CREATION_DATE);
                gp2.setValue(trans.getCreationTimestamp());
                transaction.addParameters(gp2);
                
            }
            
            Map <Long, com.redknee.app.crm.bean.Transaction> trxMap = new HashMap<Long,com.redknee.app.crm.bean.Transaction>();
            for (com.redknee.app.crm.bean.Transaction trx : subscriptionCollection)
            {
            	trxMap.put(trx.getReceiptNum(), trx);
            }
            
            subscriptionTransactionReferences = CollectionSupportHelper.get(ctx).adaptCollection(
                    ctx, 
                    subscriptionCollection, 
                    transactionAdapter, 
                    transactionReferences);
            
            for (SubAccountTransactionReference transaction : subscriptionTransactionReferences)
            {
                GenericParameter gp = new GenericParameter();
                gp.setName(OWNER_TYPE);
                gp.setValue(ProfileTypeEnum.SUBSCRIPTION.getValue());
                transaction.addParameters(gp);
                
               
                    GenericParameter gpsub = new GenericParameter();
                    gpsub.setName(SUBSCRIBER_ID);
                    gpsub.setValue(trxMap.get(transaction.getIdentifier()).getSubscriberID());
                    transaction.addParameters(gpsub);
                	
                
            }
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Transactions for start date " + start + " end date " + end
                + " category " + category + " pageKey " + pageKey + " limit " + limit;
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, caller);
        }

        SubAccountReference result = new SubAccountReference();

//        transactionReferences = new TransactionReference[subscriptionTransactionReferences.length + accountTransactionReferences.length];
        transactionReferences = new SubAccountTransactionReference[subscriptionTransactionReferences.length + accountTransactionReferences.length];
        if (ProfileTypeEnum.ACCOUNT.getValue().getValue() == profileType.getValue())
        {
        	int totalFetched = subscriptionTransactionReferences.length + accountTransactionReferences.length;
        	if (totalFetched > 0)
        	{
        		int actualRecNeeded = 0;
        		if (totalFetched >= limit)
        		{
        	        transactionReferences = new SubAccountTransactionReference[limit];
        			actualRecNeeded = limit;
        		}
        		else
        		{
        			actualRecNeeded = totalFetched;
        		}
        		int accountTrxlimit = accountTransactionReferences.length;
        		int subTrxlimit = subscriptionTransactionReferences.length;
        		int accountCounter = 0;
        		int subCounter = 0;
        		int i =0;

        		for(;i<actualRecNeeded;i++)
        		{
        			if (accountCounter < accountTrxlimit && subCounter < subTrxlimit)
        			{
        				if (
        					subscriptionTransactionReferences[subCounter].getTransactionDate().before(accountTransactionReferences[accountCounter].getTransactionDate()))
        				{
        					transactionReferences[isAscending ? (actualRecNeeded-1 - i) : (i)] = accountTransactionReferences[ accountCounter++];
        				}
        				else
        				{
        					transactionReferences[isAscending ? (actualRecNeeded-1 - i) : (i)] = subscriptionTransactionReferences[ subCounter++];
        				}
        			}
        			else if (accountCounter == accountTrxlimit)
        			{
        				transactionReferences[isAscending ? (actualRecNeeded-1 - i) : (i)] = subscriptionTransactionReferences[ subCounter++];
        			}
        			else
        			{
        				transactionReferences[isAscending ? (actualRecNeeded-1 - i) : (i)] = accountTransactionReferences[ accountCounter++];
        			}

        		}
        	}
        	
        }
        else
        {
        	if (!isAscending)
        	{
        		int i = 0;
        		/*for (SubAccountTransactionReference transaction : accountTransactionReferences)
        		{
        			transactionReferences[i++] = transaction;
        		}*/

        		for (SubAccountTransactionReference transaction : subscriptionTransactionReferences)
        		{
        			transactionReferences[i++] = transaction;
        		}
        	}
        	else
        	{
        		int i = subscriptionTransactionReferences.length;
        		for (SubAccountTransactionReference transaction : subscriptionTransactionReferences)
        		{
        			transactionReferences[--i] = transaction;
        		}
        	}
        }

        result.setReferences(transactionReferences);
        result.setBAN(accountID.getBAN());
        result.setACCOUNTNAME(accountID.getAccountName() == null ? "" : accountID.getAccountName());
        result.setFIRSTNAME(accountID.getFirstName() == null ? "" : accountID.getFirstName());
        result.setLASTNAME(accountID.getLastName());

        return result;

    }
    private static com.redknee.app.crm.bean.core.Transaction getAccountTransactionForGenericParamter(
			Context ctx, long receiptNumber, String ban) {
	
    	com.redknee.app.crm.bean.core.Transaction result=null;
    	And and= new And();
    	and.add(new EQ(TransactionXInfo.BAN,ban));
    	and.add(new EQ(TransactionXInfo.RECEIPT_NUM, receiptNumber));
		try {
			Home home =
    				HomeSupportHelper.get(ctx).getHome(ctx,
    						Common.ACCOUNT_TRANSACTION_HOME);
			result = (com.redknee.app.crm.bean.core.Transaction) home.find(ctx,and);
    		return result;
		} catch (HomeException e) {
			e.printStackTrace();
		}
		return result;
	}
    
    public static String OWNER_TYPE = "ownerType";
	public static String SUBSCRIBER_ID = "subscriberID";
    private static final String SPID = "Spid";
    public static final String SHOW_TAXAMOUNT = "showTaxAmount";
    private static final String RETURN_TRANSACTIONS = "ReturnTransactions";
}
