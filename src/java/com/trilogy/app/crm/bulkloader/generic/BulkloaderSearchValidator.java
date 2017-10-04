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
package com.trilogy.app.crm.bulkloader.generic;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;

/**
 * 
 *
 * @author victor.stratan@redknee.com
 */
public class BulkloaderSearchValidator implements Validator
{
    /**
     *
     * {@inheritDoc}
     */
    public void validate(final Context ctx, final Object obj) throws IllegalStateException
    {
        final GenericBeanBulkloader bulkloader = (GenericBeanBulkloader) obj;
        
        if (bulkloader.getSearchClassName() != null && bulkloader.getSearchClassName().length() > 0)
        {
            if (bulkloader.getSearchType().equals(SearchTypeEnum.NONE))
            {
                final CompoundIllegalStateException el = new CompoundIllegalStateException();
                el.thrown(new IllegalPropertyArgumentException(GenericBeanBulkloaderXInfo.SEARCH_TYPE,
                        "Please provide the Search Type if you provide a Search Class Name."));
                el.throwAll();
            }
        }
        else
        {
            if (!bulkloader.getSearchType().equals(SearchTypeEnum.NONE))
            {
                final CompoundIllegalStateException el = new CompoundIllegalStateException();
                el.thrown(new IllegalPropertyArgumentException(GenericBeanBulkloaderXInfo.SEARCH_CLASS_NAME,
                        "Please provide the Search Class Name if you set a Search Type."));
                el.throwAll();
            }
        }
    }

}
