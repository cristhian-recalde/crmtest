/*
 * Copyright (c) 2007, REDKNEE.com. All Rights Reserved.
 * 
 * This software is the confidential and proprietary information of REDKNEE.com.
 * ("Confidential Information"). You shall not disclose such Confidential Information and
 * shall use it only in accordance with the terms of the license agreement you entered
 * into with REDKNEE.com.
 * 
 * REDKNEE.COM MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT.
 * REDKNEE.COM SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */
package com.trilogy.app.crm.web.control;

import com.trilogy.app.crm.grr.XMLTemplateConfigKeyWebControl;
import com.trilogy.app.crm.grr.XMLTemplateConfigXInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;

import com.trilogy.app.crm.grr.XMLTemplateConfigHome;


/**
 * @author bpandey
 * 
 */
public class CustomXMLTemplateConfigKeyWebControl extends ProxyWebControl
{

    public CustomXMLTemplateConfigKeyWebControl(boolean autoPreview)
    {
        super(new XMLTemplateConfigKeyWebControl(autoPreview));
    }


    public Context wrapContext(Context ctx)
    {
        Context subCtx = ctx.createSubContext();
        Home home = (Home) ctx.get(XMLTemplateConfigHome.class);
        Object where = new EQ(XMLTemplateConfigXInfo.VISIBLE, true);
        subCtx.put(XMLTemplateConfigHome.class, home.where(subCtx, where));
        return subCtx;
    }
}
