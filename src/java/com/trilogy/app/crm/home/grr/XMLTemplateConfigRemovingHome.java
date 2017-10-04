package com.trilogy.app.crm.home.grr;

import java.io.IOException;

import com.trilogy.app.crm.grr.XMLTemplateConfig;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

public class XMLTemplateConfigRemovingHome extends HomeProxy
{
	 
	/**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;
    

    public XMLTemplateConfigRemovingHome(final Context ctx, final Home delegate)
    {
        super(ctx, delegate);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(final Context ctx, final Object obj) throws HomeException
    {
    	XMLTemplateConfig xmlTemplateConfig = (XMLTemplateConfig) obj;
    	try 
    	{
    		GenericRequestResponseSupport.removeFile(ctx, xmlTemplateConfig.getPath(), xmlTemplateConfig.getFileName());
    	}
    	catch (IOException io)
        {
    		throw new HomeException("Fail to Delete File : " + xmlTemplateConfig.getFileName() + " : Error : " + io.getLocalizedMessage());
        }
    	super.remove(ctx, obj);
    }
}
