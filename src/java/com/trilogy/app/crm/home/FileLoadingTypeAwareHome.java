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
package com.trilogy.app.crm.home;


import java.io.File;

import com.trilogy.app.crm.bean.FileLoadTypeEnum;
import com.trilogy.app.crm.bean.FileLoadingTypeAware;
import com.trilogy.framework.xhome.beans.AbstractBean;
import com.trilogy.framework.xhome.beans.Identifiable;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;


/**
 * Manages File Name and File Location for FileLadingTypeAware beans
 * 
 * @author simar.singh@redknee.com
 */
public class FileLoadingTypeAwareHome<BEAN extends AbstractBean & Identifiable & FileLoadingTypeAware> extends HomeProxy
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;


    /**
     * Creates a a new FileLadingTypeAware bean.
     * The Class<BEAN> class object has been added to encourage compile-time type-safety
     * @param delegate
     *            The home to which we delegate.
     */
    public FileLoadingTypeAwareHome(Class<BEAN> classObject, final Home delegate)
    {
        super(delegate);
    }


    /**
     * Sets the File-Name from File-location before forwarding craetion to HomePipeline
     * {@inheritDoc}
     */
    @Override
    public Object create(final Context ctx, final Object bean) throws HomeException
    {
        final BEAN fileLodingAwareBean = (BEAN) bean;
        if (FileLoadTypeEnum.SYSTEM == fileLodingAwareBean.getFileLoadType())
        {
            final String fileLocation = fileLodingAwareBean.getFileLocation();
            if (null != fileLocation && fileLocation.length() > 1)
            {
                try
                {
                    final File file = new File(fileLocation);
                    if (file.isFile())
                    {
                        fileLodingAwareBean.setFileName(file.getName());
                    }
                    else
                    {
                        throw new HomeException("File Location[" + fileLocation + "] might be invalid");
                    }
                }
                catch (Throwable t)
                {
                    if (t instanceof HomeException)
                    {
                        throw (HomeException) t;
                    }
                    else
                    {
                        new HomeException("Error in locating file[" + fileLocation + "] . Error [" + t.getMessage()
                                + "]", t);
                    }
                }
            }
            else
            {
                throw new HomeException("File location is blank.");
            }
        }
        return super.create(ctx, bean);
    }

}
