package com.trilogy.app.crm.web.service;

import com.trilogy.app.crm.bean.account.AccountAttachmentManagementConfig;
import com.trilogy.app.crm.web.control.AccountAttachmentSupport;
import com.trilogy.framework.xhome.context.Context;

/*
 * author: simar.singh@redknee.com
 * This class extends preview image streaming implementation to scale to thumbnail image size
 */
public class ThumbnailAttachmentStreamingServicer extends PreviewAttachmentStreamingServicer
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
        return new ImageProp(config.getMaxWidthThumbnailImage(), config.getMaxHeightThumbnailImage(),
                config.getMimeType());
    }
}
