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
package com.trilogy.app.crm.web.service;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.app.crm.bean.CRMSpidKeyWebControl;
import com.trilogy.app.crm.bean.DealerCode;
import com.trilogy.app.crm.bean.DealerCodeKeyWebControl;
import com.trilogy.app.crm.bean.PackageBulkTask;
import com.trilogy.app.crm.bean.PackageGroupKeyWebControl;
import com.trilogy.app.crm.bean.PackageStateEnum;
import com.trilogy.app.crm.bean.PackageTypeEnum;
import com.trilogy.app.crm.home.pipelineFactory.PackageBulkLoaderPipelineFactory;
import com.trilogy.app.crm.support.AbstractIdentitySupport;
import com.trilogy.app.crm.technology.SetTechnologyProxyWebControl;
import com.trilogy.app.crm.web.control.IdentitySupportWebControl;
import com.trilogy.app.crm.web.control.LinkedWebControl;
import com.trilogy.app.crm.web.control.PackageBatchIdDynamicTextWebCotrol;
import com.trilogy.app.crm.web.control.PackageBatchPinDynamicTextWebCotrol;
import com.trilogy.app.crm.web.control.PrimaryKeyWebControl;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.EnumWebControl;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.framework.xhome.webcontrol.TextFieldWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;


public abstract class AbstractPackageBulkLoaderServicer implements RequestServicer
{

    protected abstract PackageBulkTask processRequest(Context ctx, HttpServletRequest req, HttpServletResponse res,
            PrintWriter out);

    protected final String PACKAGE_LOAD_XMENU_KEY = "appCrmPackageBulkLoad";
    protected final WebControl task_wc = new LinkedWebControl(new PrimaryKeyWebControl(new IdentitySupportWebControl(
            new TextFieldWebControl(), new AbstractIdentitySupport()
            {

                private static final long serialVersionUID = 1L;


                public String toStringID(Object bean)
                {
                    PackageBulkTask task = ((PackageBulkTask) bean);
                    return String.valueOf("Click to open Task for Batch [" + task.getBatchId() + "], File ["
                            + task.getFileLocation() + "] ");
                }
            }), PackageBulkLoaderPipelineFactory.PAKCAGEBULKTASK_DATA_HOME), PACKAGE_LOAD_XMENU_KEY);
    protected final String PACKAGE_BATCH_ID_WEB_ID = "packageBatchId";
    protected final String PACKAGE_BATCH_PIN_WEB_ID = "packageBatchPin";
    protected final String PACKAGE_TASK_LINK_WEB_ID = "packageBatchTask";
    protected final String PACKAGE_TECH_WEB_ID = "technology";
    protected final String PACKAGE_STATE_WEB_ID = "packageState";
    protected final String PACKAGE_DEALER_WEB_ID = "dealerCode";
    protected final String PACKAGE_SPID_WEB_ID = "spid";
    protected final String PACKAGE_GROUP_WEB_ID = "packageGroup";
    protected final String PACKAGE_FILE_LOCATION_WEB_ID = "fileLocation";
    protected final WebControl packageBatchId_wc = new PackageBatchIdDynamicTextWebCotrol();
    protected final WebControl packageBatchPin_wc = new PackageBatchPinDynamicTextWebCotrol();
    protected final WebControl spid_wc = new CRMSpidKeyWebControl(true);
    protected final WebControl packageGroup_wc = new SetTechnologyProxyWebControl(new PackageGroupKeyWebControl(
            false));

    protected final WebControl dealerCode_wc = new com.redknee.app.crm.web.control.GenericSpidAwareKeyAdaptingWebControlProxy(DealerCode.class, new DealerCodeKeyWebControl(), "code");
    protected final WebControl packageTypeEnum_wc = new EnumWebControl(PackageTypeEnum.COLLECTION);
    protected final WebControl packageStateEnum_wc = new EnumWebControl(PackageStateEnum.COLLECTION);

    protected final String MMGR_NO_FILE_MSG_KEY = getClass().getName() + ".Exception.NoFileExists";
    protected final String MMGR_FILE_CHECK_MSG_KEY = getClass().getName() + ".Exception.CheckFile";
    protected final String MMGR_TASK_BATCH_EXISTS_MSG_KEY = getClass().getName() + ".Message.ExistingTask.batch";
    protected final String MMGR_TASK_FILE_EXISTS_MSG_KEY = getClass().getName() + ".Message.ExistingTask.file";
    protected final String MMGR_TASK_FILE_LOAD_MSG_KEY = getClass().getName() + ".Message.LoadingFile";
    protected final String ERROR_FILE_EXTENSION = "err";
    
    
    
        
}
