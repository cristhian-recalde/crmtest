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
package com.trilogy.app.crm.web.control;

import javax.servlet.ServletRequest;

import com.trilogy.app.crm.bean.FileLoadTypeEnum;
import com.trilogy.app.crm.bean.FileLoadingTypeAware;
import com.trilogy.framework.xhome.beans.AbstractBean;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;


/**
 * @author simar.singh@redknee.com [A web-control that either loads a local file (via
 *         browser) to system and populates the location of the uploaded file in the
 *         FileAware bean and/or sets the fileName for download Class object of the
 *         Generic type is included in constructors to improve compile-time adherence. If
 *         the bean is not FileAware, the use of this WebControl will result in
 *         ClassCastException]
 */
public class FileLoadingTypeAwareUploadDownloadWebControl<BEAN extends AbstractBean & FileLoadingTypeAware>
        extends
            FileAwareUploadDownloadWebControl<BEAN>
{

    public FileLoadingTypeAwareUploadDownloadWebControl(Class<? extends BEAN> classObject)
    {
        super(classObject);
    }


    /**
     * Create a new File upload webcontrol.
     * 
     * @param cmd
     *            command
     */
    public FileLoadingTypeAwareUploadDownloadWebControl(String cmd, Class<? extends BEAN> classObject)
    {
        super(cmd, classObject);
    }


    /**
     * Create a new File upload webcontrol.
     * 
     * @param maxWidth
     *            max width of the field
     */
    public FileLoadingTypeAwareUploadDownloadWebControl(int maxWidth, Class<? extends BEAN> classObject)
    {
        super(maxWidth, classObject);
    }


    /**
     * Create a new File upload webcontrol.
     * 
     * @param displayWidth
     *            display width of the field
     * @param maxWidth
     *            max width of the field
     */
    public FileLoadingTypeAwareUploadDownloadWebControl(int displayWidth, int maxWidth,
            Class<? extends BEAN> classObject)
    {
        super(displayWidth, maxWidth, classObject);
    }


    /**
     * Create a new File upload webcontrol.
     * 
     * @param displayWidth
     *            display width of the field
     * @param maxWidth
     *            max width of the field
     * @param cmd
     *            command to process
     */
    public FileLoadingTypeAwareUploadDownloadWebControl(int displayWidth, int maxWidth, String cmd,
            Class<? extends BEAN> classObject)
    {
        super(displayWidth, maxWidth, cmd, classObject);
    }


    @Override
    public Object fromWeb(Context ctx, ServletRequest req, String name) throws IllegalArgumentException
    {
        FileLoadingTypeAware bean = (FileLoadingTypeAware) ctx.get(AbstractWebControl.BEAN);
        if (FileLoadTypeEnum.LOCAL == bean.getFileLoadType())
        {
            return super.fromWeb(ctx, req, name);
        }
        else
        {
            return bean.getFileLocation();
        }
    }
}
