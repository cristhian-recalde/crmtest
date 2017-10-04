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
package com.trilogy.app.crm.home.pipelineFactory;

import com.trilogy.app.crm.api.queryexecutor.ApiMethodQueryExecutor;
import com.trilogy.app.crm.api.queryexecutor.ApiMethodQueryExecutorTransientHome;
import com.trilogy.app.crm.api.queryexecutor.ApiMethodQueryExecutorXMLHome;
import com.trilogy.app.crm.api.queryexecutor.QueryExecutor;
import com.trilogy.app.crm.api.queryexecutor.QueryExecutorProxy;
import com.trilogy.app.crm.home.ApiMethodQueryExecutorHomeChangeListener;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.validator.ApiMethodQueryExecutorScriptValidator;
import com.trilogy.app.crm.validator.ApiMethodQueryExecutorStubValidator;
import com.trilogy.app.crm.xhome.home.TotalCachingHome;
import com.trilogy.app.crm.xhome.visitor.HomeMigrationVisitor;
import com.trilogy.framework.core.platform.CoreSupport;
import com.trilogy.framework.xhome.beans.CompoundValidator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.True;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.AuditJournalHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.NotifyingHome;
import com.trilogy.framework.xhome.home.NotifyingHomeCmd;
import com.trilogy.framework.xhome.home.ValidatingHome;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xlog.log.MajorLogMsg;

/**
 * Creates the service home decorators and put is in the context.
 * @author Marcio Marques
 * @since 9.1.1
 *
 */
public class ApiMethodQueryExecutorHomePipelineFactory implements PipelineFactory
{

    /**
     * {@inheritDoc}
     */
    public Home createPipeline(final Context ctx, final Context serverCtx) throws HomeException
    {
        Home journalHome = CoreSupport.bindHome(ctx, ApiMethodQueryExecutor.class);
        ctx.put(API_METHOD_QUERY_EXECUTOR_JOURNAL_HOME, journalHome);
        
        Home home = new ApiMethodQueryExecutorXMLHome(ctx, CoreSupport.getFile(ctx, "ApiMethodQueryExecutor.xml"));
        home = new TotalCachingHome( ctx, new ApiMethodQueryExecutorTransientHome(ctx), home); 
        home = new AuditJournalHome(ctx, home);
        home = new NotifyingHome(home);
        home.cmd(new NotifyingHomeCmd(new ApiMethodQueryExecutorHomeChangeListener(ctx)));
        
        CompoundValidator validators = new CompoundValidator();
        validators.add(new ApiMethodQueryExecutorScriptValidator());
        validators.add(new ApiMethodQueryExecutorStubValidator());
        
        home = new ValidatingHome(home, validators);

        try
        {
            Home backupHome = new ApiMethodQueryExecutorTransientHome(ctx);
            journalHome.forEach(ctx, 
                    new HomeMigrationVisitor(
                            journalHome, 
                            home, 
                            backupHome,
                            new Predicate(){

                                @Override
                                public boolean f(Context ctx, Object obj) throws AbortVisitException
                                {
                                    boolean result = false;
                                    ApiMethodQueryExecutor executor = (ApiMethodQueryExecutor) obj;
                                    QueryExecutor queryExecutor = executor.getQueryExecutor();
                                    
                                    while (queryExecutor!=null && queryExecutor instanceof QueryExecutorProxy)
                                    {
                                        queryExecutor = ((QueryExecutorProxy) queryExecutor).getDelegate();
                                    }
                                    
                                    if (queryExecutor == null)
                                    {
                                        result = true;
                                    }
                                    
                                    return result;
                                }}, 
                            false));
            
            if (!HomeSupportHelper.get(ctx).hasBeans(ctx, backupHome, True.instance()))
            {
                backupHome.drop(ctx);
            }
        }
        catch (Exception e)
        {
            new MajorLogMsg(this, "Error(s) occurred migrating default Api Method Query Executors from journal to XML.", e).log(ctx);
        }

        return home;
    }
    
    public static final String API_METHOD_QUERY_EXECUTOR_JOURNAL_HOME = "ApiMethodQueryExecutor.JournalHome";

}
