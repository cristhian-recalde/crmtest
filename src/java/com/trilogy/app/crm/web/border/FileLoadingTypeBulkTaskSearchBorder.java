/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.web.border;

import java.util.Date;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgentProxy;
import com.trilogy.framework.xhome.elang.GTE;
import com.trilogy.framework.xhome.elang.LTE;
import com.trilogy.framework.xhome.web.search.FindSearchAgent;
import com.trilogy.framework.xhome.web.search.LimitSearchAgent;
import com.trilogy.framework.xhome.web.search.SearchBorder;
import com.trilogy.framework.xhome.web.search.WildcardSelectSearchAgent;
import com.trilogy.framework.xhome.webcontrol.WebControl;

import com.trilogy.app.crm.bean.FileBulkLoadTask;
import com.trilogy.app.crm.bean.FileBulkLoadTaskSearch;
import com.trilogy.app.crm.bean.FileBulkLoadTaskSearchWebControl;
import com.trilogy.app.crm.bean.FileBulkLoadTaskSearchXInfo;
import com.trilogy.app.crm.bean.FileBulkLoadTaskXInfo;
import com.trilogy.app.crm.support.CalendarSupportHelper;


/**
 * A Generic SearchBorder for all File Bulk Loading Task Homes.
 * 
 * Add this Border before the WebController, not as one of either its Summary or Detail
 * borders.
 * 
 * @author simar.singh@redknee.com
 **/
public class FileLoadingTypeBulkTaskSearchBorder<BEAN extends FileBulkLoadTask> extends SearchBorder
{

    public FileLoadingTypeBulkTaskSearchBorder(final Context context, Class<BEAN> beanClass)
    {
        this(context, beanClass, new FileBulkLoadTaskSearchWebControl());
    }
    
    
    public FileLoadingTypeBulkTaskSearchBorder(final Context context, Class<BEAN> beanClass, WebControl fileBulkLoadTaskSearchWebControl)
    {
        super(context, beanClass, fileBulkLoadTaskSearchWebControl);
        // FileBulkLoadTaskSearch
        // file location
        addAgent(new FindSearchAgent(FileBulkLoadTaskXInfo.FILE_LOCATION, FileBulkLoadTaskSearchXInfo.FILE_LOCATION, false));
        // filename
        addAgent(new WildcardSelectSearchAgent(FileBulkLoadTaskXInfo.FILE_NAME, FileBulkLoadTaskSearchXInfo.FILE_NAME, true));
        // user
        addAgent(new WildcardSelectSearchAgent(FileBulkLoadTaskXInfo.LAST_MODIFIED_BY,FileBulkLoadTaskSearchXInfo.LAST_MODIFIED_BY,true));
        // startDate
        addAgent(new ContextAgentProxy()
        {

            private static final long serialVersionUID = 1L;

            @Override
            public void execute(Context ctx) throws AgentException
            {
                FileBulkLoadTaskSearch criteria = (FileBulkLoadTaskSearch) getCriteria(ctx);
                Date startDate = criteria.getStartDate();
                if (startDate != null)
                {
                    startDate = CalendarSupportHelper.get(ctx).getDateWithLastSecondofDay(startDate);
                    doSelect(ctx, new LTE(FileBulkLoadTaskXInfo.LAST_MODIFIED, startDate));
                }
                delegate(ctx);
            }
        });
        // endDate
        addAgent(new ContextAgentProxy()
        {

            private static final long serialVersionUID = 1L;


            @Override
            public void execute(Context ctx) throws AgentException
            {
                FileBulkLoadTaskSearch criteria = (FileBulkLoadTaskSearch) getCriteria(ctx);
                Date endDate = criteria.getEndDate();
                if (endDate != null)
                {
                    endDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(endDate);
                    doSelect(ctx, new GTE(FileBulkLoadTaskXInfo.LAST_MODIFIED, endDate));
                }
                delegate(ctx);
            }
        });
        // limit the number of entries in search result
        addAgent(new LimitSearchAgent(FileBulkLoadTaskSearchXInfo.LIMIT));
    }
}
