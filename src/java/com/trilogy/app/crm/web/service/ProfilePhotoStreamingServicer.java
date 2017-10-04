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

import com.trilogy.app.crm.bean.account.AccountAttachmentManagementConfig;
import com.trilogy.app.crm.web.control.AccountAttachmentSupport;
import com.trilogy.framework.xhome.context.Context;


/*
 * author: simar.singh@redknee.com This class extends preview image streaming
 * implementation to scale to profile image size
 */
public class ProfilePhotoStreamingServicer extends PreviewAttachmentStreamingServicer
{

    /*
     * @Override(non-Javadoc)
     * 
     * @see
     * com.redknee.app.crm.web.service.PreviewAttachmentStreamingServicer#getImageProp
     * (com.redknee.framework.xhome.context.Context)
     */
    protected ImageProp getImageProp(Context ctx)
    {
        final AccountAttachmentManagementConfig config = AccountAttachmentSupport.getAccountMangement(ctx);
        return new ImageProp(config.getMaxWidthProfileImage(), config.getMaxHeightProfileImage(), config.getMimeType());
    }
}
