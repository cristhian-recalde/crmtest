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
package com.trilogy.app.crm.api.queryexecutor.note;

import java.util.Calendar;
import java.util.Collection;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.api.ApiSupport;
import com.trilogy.app.crm.api.queryexecutor.AbstractQueryExecutor;
import com.trilogy.app.crm.api.rmi.NoteToApiAdapter;
import com.trilogy.app.crm.api.rmi.agent.ConfigShareTask;
import com.trilogy.app.crm.api.rmi.impl.AccountsImpl;
import com.trilogy.app.crm.api.rmi.support.CallDetailsApiSupport;
import com.trilogy.app.crm.api.rmi.support.RmiApiErrorHandlingSupport;
import com.trilogy.app.crm.api.rmi.support.RmiApiSupport;
import com.trilogy.app.crm.api.rmi.support.SubscribersApiSupport;
import com.trilogy.app.crm.bean.AccountNoteHome;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.Note;
import com.trilogy.app.crm.bean.NoteHome;
import com.trilogy.app.crm.bean.NoteOwnerTypeEnum;
import com.trilogy.app.crm.bean.NoteXInfo;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberNoteHome;
import com.trilogy.app.crm.home.NoteValidator;
import com.trilogy.app.crm.support.CollectionSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.NoteSupportHelper;
import com.trilogy.framework.xhome.beans.CompoundValidator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.ValidatingHome;
import com.trilogy.framework.xhome.pipe.ThreadPool;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v2_0.types.SuccessCode;
import com.trilogy.util.crmapi.wsdl.v2_0.types.SuccessCodeEnum;
import com.trilogy.util.crmapi.wsdl.v2_1.exception.ExceptionCode;
import com.trilogy.util.crmapi.wsdl.v2_1.types.note.NoteReference;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.types.CRMRequestHeader;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionReference;
import com.trilogy.util.crmapi.wsdl.v2_1.types.note.NoteQueryResults;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.visitor.NoteConfigSharingVisitor;
import com.trilogy.app.crm.elang.PagingXStatement;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GTE;
import com.trilogy.framework.xhome.elang.LTE;
import com.trilogy.util.crmapi.wsdl.v3_0.types.ExecuteResult;
import com.trilogy.util.crmapi.wsdl.v2_1.types.note.NoteReferenceV2;

/**
 * 
 * @author Marcio Marques
 * @since 9.3
 * @autoher Sajid Memon
 * @since 9.7.1
 * 
 */
public class NoteQueryExecutors 
{
    
    private static final NoteToApiAdapter noteToApiAdapter_ = new NoteToApiAdapter();
    
    /**
     * 
     * @author Marcio Marques
     * @since 9.3.0
     *
     */
    public static class SubscriptionNotesListQueryExecutor extends AbstractQueryExecutor<NoteReference[]>
    {
        public SubscriptionNotesListQueryExecutor()
        {
            
        }

        public NoteReference[] execute(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            SubscriptionReference subscriptionRef = getParameter(ctx, PARAM_SUBSCRIPTION_REFERENCE, PARAM_SUBSCRIPTION_REFERENCE_NAME, SubscriptionReference.class, parameters);
            Boolean isAscending = getParameter(ctx, PARAM_IS_ASCENDING, PARAM_IS_ASCENDING_NAME, Boolean.class, parameters);

            RmiApiErrorHandlingSupport.validateMandatoryObject(subscriptionRef, PARAM_SUBSCRIPTION_REFERENCE_NAME);

            // getting subscriber to validate it's existence
            SubscribersApiSupport.getCrmSubscriber(ctx, subscriptionRef, this);

            NoteReference[] noteReferences = new NoteReference[] {};
            try
            {
                final Object condition = new EQ(NoteXInfo.ID_IDENTIFIER, subscriptionRef.getIdentifier());

                final Collection<Note> collection = HomeSupportHelper.get(ctx).getBeans(
                        ctx, 
                        Note.class, 
                        condition, 
                        RmiApiSupport.isSortAscending(isAscending));

                noteReferences = CollectionSupportHelper.get(ctx).adaptCollection(
                        ctx, 
                        collection, 
                        noteToApiAdapter_, 
                        noteReferences);
            }
            catch (Exception e)
            {
                final String msg = "Unable to retrieve Notes for Subscription " + subscriptionRef;
                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
            }

            return noteReferences;

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
           result = result && SubscriptionReference.class.isAssignableFrom(parameterTypes[PARAM_SUBSCRIPTION_REFERENCE]);
           result = result && Boolean.class.isAssignableFrom(parameterTypes[PARAM_IS_ASCENDING]);
           result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
           return result;
       }
       
       @Override
       public boolean validateReturnType(Class<?> resultType)
       {
           return NoteReference[].class.isAssignableFrom(resultType);
       }


       public static final int PARAM_HEADER = 0;
       public static final int PARAM_SUBSCRIPTION_REFERENCE = 1;
       public static final int PARAM_IS_ASCENDING = 2;
       public static final int PARAM_GENERIC_PARAMETERS = 3;
       
       public static final String PARAM_SUBSCRIPTION_REFERENCE_NAME = "subscriptionRef";
       public static final String PARAM_IS_ASCENDING_NAME = "isAscending";
       public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
    }
    
    /**
     * 
     * @author Marcio Marques
     * @since 9.3.0
     *
     */
    public static class AccountNotesListQueryExecutor extends AbstractQueryExecutor<NoteReference[]>
    {
        public AccountNotesListQueryExecutor()
        {
            
        }

        public NoteReference[] execute(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            String accountID = getParameter(ctx, PARAM_ACCOUNT_ID, PARAM_ACCOUNT_ID_NAME, String.class, parameters);
            Boolean isAscending = getParameter(ctx, PARAM_IS_ASCENDING, PARAM_IS_ASCENDING_NAME, Boolean.class, parameters);
            
            RmiApiErrorHandlingSupport.validateMandatoryObject(accountID, PARAM_ACCOUNT_ID_NAME);

            // getting account to validate it's existence
            AccountsImpl.getCrmAccount(ctx, accountID, this);

            NoteReference[] noteReferences = new NoteReference[] {};
            try
            {
                final Object condition = new EQ(NoteXInfo.ID_IDENTIFIER, accountID);

                final Collection<Note> collection;
                {
                    // Put the account note home in the sub-context before calling HomeSupport
                    // Otherwise, we'll be querying the subscription note home for account notes.
                    Context sCtx = ctx.createSubContext();
                    sCtx.put(NoteHome.class, ctx.get(Common.ACCOUNT_NOTE_HOME));
                    collection = HomeSupportHelper.get(ctx).getBeans(
                            sCtx, 
                            Note.class, 
                            condition, 
                            RmiApiSupport.isSortAscending(isAscending));
                }

                noteReferences = CollectionSupportHelper.get(ctx).adaptCollection(
                        ctx, 
                        collection, 
                        noteToApiAdapter_, 
                        noteReferences);
            }
            catch (Exception e)
            {
                final String msg = "Unable to retrieve Notes for Account " + accountID;
                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
            }

            return noteReferences;


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
           return NoteReference[].class.isAssignableFrom(resultType);
       }


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
     * @author Marcio Marques
     * @since 9.3.0
     *
     */
    public static class SubscriptionNoteCreationQueryExecutor extends AbstractQueryExecutor<SuccessCode>
    {
        public SubscriptionNoteCreationQueryExecutor()
        {
            
        }

        public SuccessCode execute(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            SubscriptionReference subscriptionRef = getParameter(ctx, PARAM_SUBSCRIPTION_REFERENCE, PARAM_SUBSCRIPTION_REFERENCE_NAME, SubscriptionReference.class, parameters);
            String note = getParameter(ctx, PARAM_NOTE, PARAM_NOTE_NAME, String.class, parameters);
            String mainType = getParameter(ctx, PARAM_MAIN_TYPE, PARAM_MAIN_TYPE_NAME, String.class, parameters);
            String subType = getParameter(ctx, PARAM_SUB_TYPE, PARAM_SUB_TYPE_NAME, String.class, parameters);

            RmiApiErrorHandlingSupport.validateMandatoryObject(subscriptionRef, PARAM_SUBSCRIPTION_REFERENCE_NAME);
            RmiApiErrorHandlingSupport.validateMandatoryObject(note, PARAM_NOTE_NAME);

            final Home home = getSubscriberNoteHome(ctx);

            // getting subscriber to validate it's existence
            SubscribersApiSupport.getCrmSubscriber(ctx, subscriptionRef, this);

            try
            {
            ctx.put(SubscriberNoteHome.class,
                    ApiSupport.injectAPIUpdateEntityHomeIntoPipeline(ctx, (Home) ctx.get(SubscriberNoteHome.class)));
            NoteSupportHelper.get(ctx).addNote(ctx, home, subscriptionRef.getIdentifier(), note, mainType, subType);
           }
            catch (Exception e)
            {
                final String msg = "Unable to create Note \"" + note + "\" for Subscription " + subscriptionRef;
                RmiApiErrorHandlingSupport.handleCreateExceptions(ctx, e, msg, false, Note.class, null, this);
            }

            return SuccessCodeEnum.SUCCESS.getValue();

        }
        
        @Override
       public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
       {
           Object[] result = null;
           if (isGenericExecution(ctx, parameters))
           {
               result = new Object[6];
               result[0] = parameters[0];
               result[1] = getParameter(ctx, PARAM_SUBSCRIPTION_REFERENCE, PARAM_SUBSCRIPTION_REFERENCE_NAME, SubscriptionReference.class, parameters);
               result[2] = getParameter(ctx, PARAM_NOTE, PARAM_NOTE_NAME, String.class, parameters);
               result[3] = getParameter(ctx, PARAM_MAIN_TYPE, PARAM_MAIN_TYPE_NAME, String.class, parameters);
               result[4] = getParameter(ctx, PARAM_SUB_TYPE, PARAM_SUB_TYPE_NAME, String.class, parameters);
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
           result = result && SubscriptionReference.class.isAssignableFrom(parameterTypes[PARAM_SUBSCRIPTION_REFERENCE]);
           result = result && String.class.isAssignableFrom(parameterTypes[PARAM_NOTE]);
           result = result && String.class.isAssignableFrom(parameterTypes[PARAM_MAIN_TYPE]);
           result = result && String.class.isAssignableFrom(parameterTypes[PARAM_SUB_TYPE]);
           result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
           return result;
       }
       
       @Override
       public boolean validateReturnType(Class<?> resultType)
       {
           return SuccessCode.class.isAssignableFrom(resultType);
       }


       public static final int PARAM_HEADER = 0;
       public static final int PARAM_SUBSCRIPTION_REFERENCE = 1;
       public static final int PARAM_NOTE = 2;
       public static final int PARAM_MAIN_TYPE = 3;
       public static final int PARAM_SUB_TYPE = 4;
       public static final int PARAM_GENERIC_PARAMETERS = 5;
       
       public static final String PARAM_SUBSCRIPTION_REFERENCE_NAME = "subscriptionRef";
       public static final String PARAM_NOTE_NAME = "note";
       public static final String PARAM_MAIN_TYPE_NAME = "mainType";
       public static final String PARAM_SUB_TYPE_NAME = "subType";
       public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
    }

    /**
     * 
     * @author Marcio Marques
     * @since 9.3.0
     *
     */
    public static class AccountNoteCreationQueryExecutor extends AbstractQueryExecutor<SuccessCode>
    {
        public AccountNoteCreationQueryExecutor()
        {
            
        }

        public SuccessCode execute(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            String accountID = getParameter(ctx, PARAM_ACCOUNT_ID, PARAM_ACCOUNT_ID_NAME, String.class, parameters);
            String note = getParameter(ctx, PARAM_NOTE, PARAM_NOTE_NAME, String.class, parameters);
            String mainType = getParameter(ctx, PARAM_MAIN_TYPE, PARAM_MAIN_TYPE_NAME, String.class, parameters);
            String subType = getParameter(ctx, PARAM_SUB_TYPE, PARAM_SUB_TYPE_NAME, String.class, parameters);

            RmiApiErrorHandlingSupport.validateMandatoryObject(accountID, PARAM_ACCOUNT_ID_NAME);
            RmiApiErrorHandlingSupport.validateMandatoryObject(note, PARAM_NOTE_NAME);

            // getting account to validate it's existence
            AccountsImpl.getCrmAccount(ctx, accountID, this);

            final Home home = getAccountNoteHome(ctx);

            try
            {
                ctx.put(AccountNoteHome.class, ApiSupport.injectAPIUpdateEntityHomeIntoPipeline(ctx, (Home)ctx.get(AccountNoteHome.class)));
                NoteSupportHelper.get(ctx).addNote(ctx, home, accountID, note, mainType, subType);
            }
            catch (Exception e)
            {
                final String msg = "Unable to create Note \"" + note + "\" for Account " + accountID;
                RmiApiErrorHandlingSupport.handleCreateExceptions(ctx, e, msg, false, Note.class, null, this);
            }

            return SuccessCodeEnum.SUCCESS.getValue();

        }
        
        @Override
       public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
       {
           Object[] result = null;
           if (isGenericExecution(ctx, parameters))
           {
               result = new Object[6];
               result[0] = parameters[0];
               result[1] = getParameter(ctx, PARAM_ACCOUNT_ID, PARAM_ACCOUNT_ID_NAME, String.class, parameters);
               result[2] = getParameter(ctx, PARAM_NOTE, PARAM_NOTE_NAME, String.class, parameters);
               result[3] = getParameter(ctx, PARAM_MAIN_TYPE, PARAM_MAIN_TYPE_NAME, String.class, parameters);
               result[4] = getParameter(ctx, PARAM_SUB_TYPE, PARAM_SUB_TYPE_NAME, String.class, parameters);
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
           result = result && String.class.isAssignableFrom(parameterTypes[PARAM_ACCOUNT_ID]);
           result = result && String.class.isAssignableFrom(parameterTypes[PARAM_NOTE]);
           result = result && String.class.isAssignableFrom(parameterTypes[PARAM_MAIN_TYPE]);
           result = result && String.class.isAssignableFrom(parameterTypes[PARAM_SUB_TYPE]);
           result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
           return result;
       }
       
       @Override
       public boolean validateReturnType(Class<?> resultType)
       {
           return SuccessCode.class.isAssignableFrom(resultType);
       }


       public static final int PARAM_HEADER = 0;
       public static final int PARAM_ACCOUNT_ID = 1;
       public static final int PARAM_NOTE = 2;
       public static final int PARAM_MAIN_TYPE = 3;
       public static final int PARAM_SUB_TYPE = 4;
       public static final int PARAM_GENERIC_PARAMETERS = 5;
       
       public static final String PARAM_ACCOUNT_ID_NAME = "accountID";
       public static final String PARAM_NOTE_NAME = "note";
       public static final String PARAM_MAIN_TYPE_NAME = "mainType";
       public static final String PARAM_SUB_TYPE_NAME = "subType";
       public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
    }

    private static Home getSubscriberNoteHome(final Context ctx) throws com.redknee.util.crmapi.wsdl.v3_0.api.CRMExceptionFault
    {
        // Add validation decorator for use with API.  This validator will perform the type of
        // validation normally performed by GUI logic such as web controls.
        CompoundValidator validator = new CompoundValidator();
        validator.add(NoteValidator.instance());
        return new ValidatingHome(
                    validator,
                    RmiApiSupport.getCrmHome(ctx, NoteHome.class, AccountsImpl.class));
    }

    private static Home getAccountNoteHome(final Context ctx) throws com.redknee.util.crmapi.wsdl.v3_0.api.CRMExceptionFault
    {
        // Add validation decorator for use with API.  This validator will perform the type of
        // validation normally performed by GUI logic such as web controls.
        CompoundValidator validator = new CompoundValidator();
        validator.add(NoteValidator.instance());
        return new ValidatingHome(
                    validator,
                    RmiApiSupport.getCrmHome(ctx, Common.ACCOUNT_NOTE_HOME, AccountsImpl.class));
    }
    
	/**
	 * 
	 * @author Sajid Memon
	 * @since 9.7.1
	 *
	 */
	public static class ListAccountNotesV2QueryExecutor extends AbstractQueryExecutor<NoteQueryResults>
	{

		public ListAccountNotesV2QueryExecutor()
	    {
			
	    }
		
	    public NoteQueryResults execute(Context ctx, Object... parameters) throws CRMExceptionFault
	    {
	    	String accountID = getParameter(ctx, PARAM_ACCOUNT_ID, PARAM_ACCOUNT_ID_NAME, String.class, parameters);
            Calendar start = getParameter(ctx, PARAM_START, PARAM_START_NAME, Calendar.class, parameters);
            Calendar end = getParameter(ctx, PARAM_END, PARAM_END_NAME, Calendar.class, parameters);
            String pageKey = getParameter(ctx, PARAM_PAGE_KEY, PARAM_PAGE_KEY_NAME, String.class, parameters);
            
            Boolean isAscending = getParameter(ctx, PARAM_IS_ASCENDING, PARAM_IS_ASCENDING_NAME, Boolean.class, parameters);
            Boolean isAsync = getParameter(ctx, PARAM_IS_ASYNC, PARAM_IS_ASYNC_NAME, Boolean.class, parameters);
            
            RmiApiErrorHandlingSupport.validateMandatoryObject(accountID, PARAM_ACCOUNT_ID_NAME);
            
            NoteReferenceV2[] noteReferences = new NoteReferenceV2[] {};
            final NoteQueryResults noteQueryResults = new NoteQueryResults();
	        final ExecuteResult executeResult = new ExecuteResult();
	        
            final Account account = AccountsImpl.getCrmAccount(ctx, accountID, this);
            
            if (isAsync)
            {
            	if(RmiApiErrorHandlingSupport.validateAysnNotesPushSupported(ctx,account.getSpid()))
            	{
            		try
            		{
            			// Put the account note home in the sub-context before calling HomeSupport
            			// Otherwise, we'll be querying the subscription note home for account notes.
            			Home accountNoteHome = (Home) ctx.get(Common.ACCOUNT_NOTE_HOME); 

            			final And condition = new And();

            			condition.add(new EQ(NoteXInfo.ID_IDENTIFIER, accountID));

            			if (start != null)
            			{
            				condition.add(new GTE(NoteXInfo.CREATED, CalendarSupportHelper.get(ctx).calendarToDate(start)));
            			}
            			if (end != null)
            			{
            				condition.add(new LTE(NoteXInfo.CREATED, CalendarSupportHelper.get(ctx).calendarToDate(end)));
            			}

            			ConfigShareTask task = new ConfigShareTask();
            			task.setHome(accountNoteHome);
            			task.setVisitor(new NoteConfigSharingVisitor(NoteOwnerTypeEnum.ACCOUNT));
            			task.setCondition(condition);

            			ThreadPool pool = (ThreadPool) ctx.get(com.redknee.app.crm.agent.ServiceInstall.ASYNC_CONFIGSHARE_REQUEST_PUSH);
            			
            			if(pool == null)
            			{
            				RmiApiErrorHandlingSupport.generalException(ctx, null, "Threadpool not installed in Context for this operation.",ExceptionCode.UNKNOWN_EXCEPTION , this);
            			}

            			Context subCtx = ctx.createSubContext();
            			subCtx.put(ConfigShareTask.class, task);
            			pool.execute(subCtx);

            		}
            		catch (Exception e)
            		{

            			final String msg = "Unable to retrieve Notes for Account [" + accountID + "], for StartDate [" + start + "], EndDate [" + end
            					+ "]";
            			RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
            		}

            		executeResult.setResultCode(RESULT_CODE_SUCCESS);
            		noteQueryResults.setResult(executeResult);
            	}
            }
            else
            {
            	//If Async is false, API will return Notes through API
            	int limit= getParameter(ctx, PARAM_LIMIT, PARAM_LIMIT_NAME, int.class, parameters);
            	
                final CRMSpid spid = RmiApiSupport.getCrmServiceProvider(ctx, account.getSpid(), this);
	
	            RmiApiErrorHandlingSupport.validateLimitInput(ctx, limit, spid.getMaxGetLimit());
	            final Long longPageKey = RmiApiErrorHandlingSupport.validateLongPageKey(ctx, pageKey);
		        
	            boolean ascending = RmiApiSupport.isSortAscending(isAscending);
	            
	            
	            // Put the account note home in the sub-context before calling HomeSupport
                // Otherwise, we'll be querying the subscription note home for account notes.
	        	Context sCtx = ctx.createSubContext();
                sCtx.put(NoteHome.class, ctx.get(Common.ACCOUNT_NOTE_HOME));
		        
                try
	            {
	                
	                
	                final And condition = new And();
	                
	                condition.add(new EQ(NoteXInfo.ID_IDENTIFIER, accountID));
	                
	                if (start != null)
	                {
	                    condition.add(new GTE(NoteXInfo.CREATED, CalendarSupportHelper.get(sCtx).calendarToDate(start)));
	                }
	                if (end != null)
	                {
	                    condition.add(new LTE(NoteXInfo.CREATED, CalendarSupportHelper.get(sCtx).calendarToDate(end)));
	                }
	                if (pageKey != null)
	                {
	                    condition.add(new PagingXStatement(NoteXInfo.ID, pageKey, ascending));
	                }
	                
	                Collection<Note> collection = HomeSupportHelper.get(sCtx).getBeans(
	                        sCtx,
	                        Note.class,
	                        condition,
	                        limit,
	                        ascending);
	                
	                
                
	                noteReferences = new NoteReferenceV2[collection.size()];

	                int i = 0;
	                for (Note note : collection)
	                {
	                	noteReferences[i++] = (NoteReferenceV2) NoteToApiAdapter.adaptNoteToReference(ctx, note, new NoteReferenceV2());
	                }

	                noteQueryResults.setResult(executeResult);
			        
			        if (noteReferences != null)
			        {
			    		if (noteReferences.length > 0)
			        	{
			    			noteQueryResults.setNoteReference(noteReferences);
			    			noteQueryResults.setPageKey(String.valueOf(noteReferences[noteReferences.length - 1].getIdentifier()));
			        	}
			        }
	                
	            }
	            catch (Exception e)
	            {
	                
	                final String msg = "Unable to retrieve Notes for Account [" + accountID + "], for StartDate [" + start + "], EndDate [" + end
	                		+ "], pageKey [" + pageKey + "] and limit [" + limit + "]";
	                RmiApiErrorHandlingSupport.handleQueryExceptions(sCtx, e, msg, this);
	            }
            }
            return noteQueryResults;
	    }
	    
	    @Override
        public Object[] getParameters(Context ctx, Object... parameters) throws CRMExceptionFault
        {
            Object[] result = null;
           if (isGenericExecution(ctx, parameters))
           {
               result = new Object[9];
               result[0] = parameters[0];
               result[1] = getParameter(ctx, PARAM_ACCOUNT_ID, PARAM_ACCOUNT_ID_NAME, SubscriptionReference.class, parameters);
               result[2] = getParameter(ctx, PARAM_START, PARAM_START_NAME, Calendar.class, parameters);
               result[3] = getParameter(ctx, PARAM_END, PARAM_END_NAME, Calendar.class, parameters);
               result[4] = getParameter(ctx, PARAM_PAGE_KEY, PARAM_PAGE_KEY_NAME, String.class, parameters);
               result[5] = getParameter(ctx, PARAM_LIMIT, PARAM_LIMIT_NAME, int.class, parameters);
               result[6] = getParameter(ctx, PARAM_IS_ASCENDING, PARAM_IS_ASCENDING_NAME, Boolean.class, parameters);
               result[7] = getParameter(ctx, PARAM_IS_ASYNC, PARAM_IS_ASYNC_NAME, Boolean.class, parameters);
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
            result = result && SubscriptionReference.class.isAssignableFrom(parameterTypes[PARAM_ACCOUNT_ID]);
            result = result && Calendar.class.isAssignableFrom(parameterTypes[PARAM_START]);
            result = result && Calendar.class.isAssignableFrom(parameterTypes[PARAM_END]);
            result = result && String.class.isAssignableFrom(parameterTypes[PARAM_PAGE_KEY]);
            result = result && int.class.isAssignableFrom(parameterTypes[PARAM_LIMIT]);
            result = result && Boolean.class.isAssignableFrom(parameterTypes[PARAM_IS_ASCENDING]);
            result = result && Boolean.class.isAssignableFrom(parameterTypes[PARAM_IS_ASYNC]);
            result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
            return result;
        }
       
       @Override
       public boolean validateReturnType(Class<?> resultType)
       {
           return NoteQueryResults.class.isAssignableFrom(resultType);
       }


       public static final int PARAM_HEADER = 0;
       public static final int PARAM_ACCOUNT_ID = 1;
       public static final int PARAM_START = 2;
       public static final int PARAM_END = 3;
       public static final int PARAM_PAGE_KEY = 4;
       public static final int PARAM_LIMIT = 5;
       public static final int PARAM_IS_ASCENDING = 6;
       public static final int PARAM_IS_ASYNC = 7;
       public static final int PARAM_GENERIC_PARAMETERS = 8;
       
       public static final String PARAM_ACCOUNT_ID_NAME = "accountID";
       public static final String PARAM_START_NAME = "startDate";
       public static final String PARAM_END_NAME = "endDate";
       public static final String PARAM_PAGE_KEY_NAME = "pageKey";
       public static final String PARAM_LIMIT_NAME = "limit";
       public static final String PARAM_IS_ASCENDING_NAME = "isAscending";
       public static final String PARAM_IS_ASYNC_NAME = "isAsync";
       public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
       
       public static final int RESULT_CODE_SUCCESS = 0;
       
    }
	
	/**
	 * 
	 * @author Sajid Memon
	 * @since 9.7.1
	 *
	 */
	public static class ListSubscriptionNotesV2QueryExecutor extends AbstractQueryExecutor<NoteQueryResults>
	{

		public ListSubscriptionNotesV2QueryExecutor()
	    {
			
	    }
		
	    public NoteQueryResults execute(Context ctx, Object... parameters) throws CRMExceptionFault
	    {
            SubscriptionReference subscriptionRef = getParameter(ctx, PARAM_SUBSCRIPTION_REFERENCE, PARAM_SUBSCRIPTION_REFERENCE_NAME, SubscriptionReference.class, parameters);
            Calendar start = getParameter(ctx, PARAM_START, PARAM_START_NAME, Calendar.class, parameters);
            Calendar end = getParameter(ctx, PARAM_END, PARAM_END_NAME, Calendar.class, parameters);
            String pageKey = getParameter(ctx, PARAM_PAGE_KEY, PARAM_PAGE_KEY_NAME, String.class, parameters);
            Boolean isAscending = getParameter(ctx, PARAM_IS_ASCENDING, PARAM_IS_ASCENDING_NAME, Boolean.class, parameters);
            Boolean isAsync = getParameter(ctx, PARAM_IS_ASYNC, PARAM_IS_ASYNC_NAME, Boolean.class, parameters);

            RmiApiErrorHandlingSupport.validateMandatoryObject(subscriptionRef,PARAM_SUBSCRIPTION_REFERENCE_NAME);
            
            NoteReferenceV2[] noteReferences = new NoteReferenceV2[] {};
            final NoteQueryResults noteQueryResults = new NoteQueryResults();
	        final ExecuteResult executeResult = new ExecuteResult();
	                    
            final Subscriber subscriber = SubscribersApiSupport.getCrmSubscriber(ctx, subscriptionRef, this);
            final String subscriptionID = subscriptionRef.getIdentifier();
            
            if (isAsync)
            {
            	if(RmiApiErrorHandlingSupport.validateAysnNotesPushSupported(ctx,subscriber.getSpid()))
            	{
            		
            		Home subscriberNoteHome = (Home) ctx.get(com.redknee.app.crm.CoreCrmConstants.SUBSCRIBER_NOTE_HOME);
            		try
            		{
            			final And condition = new And();

            			condition.add(new EQ(NoteXInfo.ID_IDENTIFIER, subscriptionID));

            			if (start != null)
            			{
            				condition.add(new GTE(NoteXInfo.CREATED, CalendarSupportHelper.get(ctx).calendarToDate(start)));
            			}
            			if (end != null)
            			{
            				condition.add(new LTE(NoteXInfo.CREATED, CalendarSupportHelper.get(ctx).calendarToDate(end)));
            			}

            			ConfigShareTask task = new ConfigShareTask();
            			task.setHome(subscriberNoteHome);
            			task.setVisitor(new NoteConfigSharingVisitor(NoteOwnerTypeEnum.SUBSCRIPTION));
            			task.setCondition(condition);

            			ThreadPool pool = (ThreadPool) ctx.get(com.redknee.app.crm.agent.ServiceInstall.ASYNC_CONFIGSHARE_REQUEST_PUSH);
            			
            			if(pool == null)
            			{
            				RmiApiErrorHandlingSupport.generalException(ctx, null, "Threadpool not installed in Context for this operation.",ExceptionCode.UNKNOWN_EXCEPTION , this);
            			}

            			Context subCtx = ctx.createSubContext();
            			subCtx.put(ConfigShareTask.class, task);
            			pool.execute(subCtx);

            		}
            		catch (Exception e)
            		{

            			final String msg = "Unable to retrieve Notes for Subscriber [" + subscriptionID + "], for StartDate [" + start + "], EndDate [" + end
            					+ "]";
            			RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
            		}

            		executeResult.setResultCode(RESULT_CODE_SUCCESS);
            		noteQueryResults.setResult(executeResult);
            	}
            }
            else
            {
            	//If Async is false, API will return Notes through API
	            final CRMSpid spid = RmiApiSupport.getCrmServiceProvider(ctx, subscriber.getSpid(), this);
	
	            int limit= getParameter(ctx, PARAM_LIMIT, PARAM_LIMIT_NAME, int.class, parameters);
	            RmiApiErrorHandlingSupport.validateLimitInput(ctx, limit, spid.getMaxGetLimit());
	            final Long longPageKey = RmiApiErrorHandlingSupport.validateLongPageKey(ctx, pageKey);
		        
	            boolean ascending = RmiApiSupport.isSortAscending(isAscending);
	            
	            try
	            {
	                
	                final And condition = new And();
	                
	                condition.add(new EQ(NoteXInfo.ID_IDENTIFIER, subscriptionID));
	                
	                if (start != null)
	                {
	                    condition.add(new GTE(NoteXInfo.CREATED, CalendarSupportHelper.get(ctx).calendarToDate(start)));
	                }
	                if (end != null)
	                {
	                    condition.add(new LTE(NoteXInfo.CREATED, CalendarSupportHelper.get(ctx).calendarToDate(end)));
	                }
	                if (pageKey != null)
	                {
	                    condition.add(new PagingXStatement(NoteXInfo.ID, pageKey, ascending));
	                }
	                
	                Collection<Note> collection = HomeSupportHelper.get(ctx).getBeans(
	                        ctx,
	                        Note.class,
	                        condition,
	                        limit,
	                        ascending);
	                
	                
	                noteReferences = new NoteReferenceV2[collection.size()];

	                int i = 0;
	                for (Note note : collection)
	                {
	                    noteReferences[i++] = (NoteReferenceV2) NoteToApiAdapter.adaptNoteToReference(ctx, note, new NoteReferenceV2());
	                }

	                noteQueryResults.setResult(executeResult);
			        
			        if (noteReferences != null)
			        {
			    		if (noteReferences.length > 0)
			        	{
			    			noteQueryResults.setNoteReference(noteReferences);
			    			noteQueryResults.setPageKey(String.valueOf(noteReferences[noteReferences.length - 1].getIdentifier()));
			        	}
			        }
	            }
	            catch (Exception e)
	            {
	                
	                final String msg = "Unable to retrieve Notes for Subscription [" + subscriptionID + "], for StartDate [" + start + "], EndDate [" + end
	                        + "], pageKey [" + pageKey + "] and limit [" + limit + "]";
	                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
	            }
            }   
            
            return noteQueryResults;
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
               result[4] = getParameter(ctx, PARAM_PAGE_KEY, PARAM_PAGE_KEY_NAME, String.class, parameters);
               result[5] = getParameter(ctx, PARAM_LIMIT, PARAM_LIMIT_NAME, int.class, parameters);
               result[6] = getParameter(ctx, PARAM_IS_ASCENDING, PARAM_IS_ASCENDING_NAME, Boolean.class, parameters);
               result[7] = getParameter(ctx, PARAM_IS_ASYNC, PARAM_IS_ASYNC_NAME, Boolean.class, parameters);
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
            result = result && String.class.isAssignableFrom(parameterTypes[PARAM_PAGE_KEY]);
            result = result && int.class.isAssignableFrom(parameterTypes[PARAM_LIMIT]);
            result = result && Boolean.class.isAssignableFrom(parameterTypes[PARAM_IS_ASCENDING]);
            result = result && Boolean.class.isAssignableFrom(parameterTypes[PARAM_IS_ASYNC]);
            result = result && GenericParameter[].class.isAssignableFrom(parameterTypes[PARAM_GENERIC_PARAMETERS]);
            return result;
        }
        
        @Override
        public boolean validateReturnType(Class<?> resultType)
        {
            return NoteQueryResults.class.isAssignableFrom(resultType);
        }


        public static final int PARAM_HEADER = 0;
        public static final int PARAM_SUBSCRIPTION_REFERENCE = 1;
        public static final int PARAM_START = 2;
        public static final int PARAM_END = 3;
        public static final int PARAM_PAGE_KEY = 4;
        public static final int PARAM_LIMIT = 5;
        public static final int PARAM_IS_ASCENDING = 6;
        public static final int PARAM_IS_ASYNC = 7;
        public static final int PARAM_GENERIC_PARAMETERS = 8;
        
        public static final String PARAM_SUBSCRIPTION_REFERENCE_NAME = "subscriptionRef";
        public static final String PARAM_START_NAME = "startDate";
        public static final String PARAM_END_NAME = "endDate";
        public static final String PARAM_PAGE_KEY_NAME = "pageKey";
        public static final String PARAM_LIMIT_NAME = "limit";
        public static final String PARAM_IS_ASCENDING_NAME = "isAscending";
        public static final String PARAM_IS_ASYNC_NAME = "isAsync";
        public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
        
        public static final int RESULT_CODE_SUCCESS = 0;
     }
}
