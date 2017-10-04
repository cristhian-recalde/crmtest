package com.trilogy.app.crm.home.grr;

import java.util.Collection;
import java.util.Iterator;

import com.trilogy.app.crm.grr.XMLTemplateConfig;
import com.trilogy.app.crm.grr.XMLTemplateConfigXInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

public class XMLTemplateConfigSettingHome  extends HomeProxy
{
	public static final long MIN_SYSTEM_FILE_ID = 1;
	public static final long MAX_SYSTEM_FILE_ID = 10000;
	/**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;
    

    public XMLTemplateConfigSettingHome(final Context ctx, final Home delegate)
    {
        super(ctx, delegate);

    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(final Context ctx, final Object obj) throws HomeException
    {
    	XMLTemplateConfig xmlTemplateConfig = (XMLTemplateConfig) obj;
        
    	String templateIdentifier = xmlTemplateConfig.getVendor() + "_" + xmlTemplateConfig.getSystem() + "_"
    			+ xmlTemplateConfig.getTargetMethod() + "_" + xmlTemplateConfig.getTemplateVersion();
    	
    	xmlTemplateConfig.setTemplateID(templateIdentifier);
    	xmlTemplateConfig.setFileName(templateIdentifier + ".xml");
    	
    	if(xmlTemplateConfig.DEFAULT_DEPLOYABLEFILEID == xmlTemplateConfig.getDeployableFileId())
    	{
    	if (xmlTemplateConfig.isSystemProvided())
    	{
    		xmlTemplateConfig.setPath("etc/GRR/Template/System");
    		long max = MIN_SYSTEM_FILE_ID -1;
    		try
    	      {
    			EQ filter = new EQ(XMLTemplateConfigXInfo.SYSTEM_PROVIDED, true);
    			Collection<XMLTemplateConfig> collection = getDelegate().select(ctx,filter);
    	         for ( Iterator i = collection.iterator() ; i.hasNext() ; )
    	         {
    	        	 XMLTemplateConfig tempBean = (XMLTemplateConfig) i.next();
    	            long value = tempBean.getDeployableFileId();

    	            if ( value > max )
    	            {
    	               max  = value ;
    	            }
    	         }
    	         if(max == MAX_SYSTEM_FILE_ID)
    	         {
    	        	 throw new IllegalArgumentException("Can't generate any further templates. Please delete existing templates.");
    	         }
    	         xmlTemplateConfig.setDeployableFileId(max + 1);
    	      }
    	      catch (HomeException e)
    	      {
    	         throw new IllegalArgumentException(e.getMessage());
    	      }
    	}
    	
    	else
    	{
    		xmlTemplateConfig.setPath("etc/GRR/Template/Custom");
    		long max = MAX_SYSTEM_FILE_ID;
    		try
  	      {
    			EQ filter = new EQ(XMLTemplateConfigXInfo.SYSTEM_PROVIDED, false);
    			Collection<XMLTemplateConfig> collection = getDelegate().select(ctx,filter);
    			
  	         for ( Iterator i = getDelegate().selectAll(ctx).iterator() ; i.hasNext() ; )
  	         {
  	        	 XMLTemplateConfig tempBean = (XMLTemplateConfig) i.next();
  	            long value = tempBean.getDeployableFileId();

  	            if ( value > max )
  	            {
  	               max  = value ;
  	            }
  	         }
  	         if(max == Long.MAX_VALUE)
  	         {
  	        	 throw new IllegalArgumentException("Can't generate any further templates. Please delete existing templates.");
  	         }
  	         xmlTemplateConfig.setDeployableFileId(max + 1);
  	      }
  	      catch (HomeException e)
  	      {
  	         throw new IllegalArgumentException(e.getMessage());
  	      }
    	}
    	}
    	
    	return super.create(ctx, xmlTemplateConfig); 
    }
    
}
