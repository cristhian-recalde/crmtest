package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletRequest;

import com.trilogy.app.crm.CommonFramework;
import com.trilogy.app.crm.bean.TechnicalServiceParameter;
import com.trilogy.app.crm.bean.TechnicalServiceParameterDisplay;
import com.trilogy.app.crm.bean.TechnicalServiceParameterDisplayTableWebControl;
import com.trilogy.app.crm.bean.TSParameterTemplate;
import com.trilogy.app.crm.bean.TSParameterTemplateHome;
import com.trilogy.app.crm.bean.TSParameterTemplateID;
import com.trilogy.app.crm.bean.ui.TechnicalServiceTemplate;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.web.action.ActionMgr;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.ViewModeEnum;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

public class TechnicalSerivceTemplateWebControl extends TechnicalServiceParameterDisplayTableWebControl{


    /**
     * {@inheritDoc}
     */
    @Override
    public void toWeb(final Context ctx, final PrintWriter out, final String name, final Object obj)
    {
        final TechnicalServiceTemplate technicalServiceTemplate = (TechnicalServiceTemplate) ctx.get(AbstractWebControl.BEAN);
        final Context subCtx = ctx.createSubContext();
    	List<TechnicalServiceParameterDisplay> beanList = new ArrayList<TechnicalServiceParameterDisplay>();
    	List<TechnicalServiceParameter> beans = new ArrayList<TechnicalServiceParameter>();
        Home serviceTemplateHome = (Home) ctx.get(TSParameterTemplateHome.class);
        TSParameterTemplate template = null;
        List<TechnicalServiceParameter> parametersList = new ArrayList<TechnicalServiceParameter>();
        try
        {
        	parametersList = technicalServiceTemplate.getServiceParameters();
        	
            template = (TSParameterTemplate) serviceTemplateHome.find(ctx, new TSParameterTemplateID(technicalServiceTemplate.getType(),
            		technicalServiceTemplate.getServiceSubType()));
        }
        catch (HomeException e)
        {
        	LogSupport.minor(ctx, this, "HomeException occurred while retrieveing Service Parameter Template for Techical Service Template Type" 
					+ technicalServiceTemplate.getType() + " and Service Sub Type " + technicalServiceTemplate.getServiceSubType(), e);
        }
        if (template != null)
        {
            //service.setServiceParameters(template.getServiceParameters());
        	beans = template.getServiceParameters();
        	TechnicalServiceParameterDisplay bean = null;
            
            for (TechnicalServiceParameter servparam : beans)
            {     
				try 
				{
					bean = adapt(subCtx,parametersList, servparam);
				}
				catch (final Exception e)
				{
					
					new DebugLogMsg(this, "Unable to adapt ", e).log(subCtx);
				}
                if (bean != null)
                {
                    beanList.add(bean);
                }
            }
        }
        subCtx.put(NUM_OF_BLANKS, -1);
        disableActions(subCtx);
        setPropertyReadOnly(subCtx, "TechnicalServiceParameterDisplay.isMandatory");
        
        /*
         * This was copied from DefaultServiceEditorWebControl
         *
         *  // MAALP: 05/03/04 - TT 402262116 // create a subcontext which
         * overrides DISPLAY_MODE // in order to make ServiceFee2tableWebControl //
         * call outputCheckBox method // and saves the real mode in order to
         * properly display check box
         */
        final int mode = subCtx.getInt(MODE, DISPLAY_MODE);
        subCtx.put(CommonFramework.REAL_MODE, mode);
        if (mode == DISPLAY_MODE)
        {
            subCtx.put(MODE, EDIT_MODE);
        }
        
        super.toWeb(subCtx, out, name, beanList);
        
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object fromWeb(final Context ctx, final ServletRequest req, final String name)
    {
        
        final List list = new ArrayList();
        final TechnicalServiceTemplate technicalServiceTemplate = (TechnicalServiceTemplate) ctx.get(AbstractWebControl.BEAN);
        List serviceParamList = new ArrayList();

        super.fromWeb(ctx, list, req, name);

        for (final Iterator i = list.iterator(); i.hasNext();)
        {
            final TechnicalServiceParameterDisplay serviceParameterDisplay = (TechnicalServiceParameterDisplay) i.next();
            
            final TechnicalServiceParameter bean = new TechnicalServiceParameter();
            
            bean.setIsMandatory(serviceParameterDisplay.getIsMandatory());
            bean.setName(serviceParameterDisplay.getName());
            bean.setDataType(serviceParameterDisplay.getDataType());
            bean.setParameterValue(serviceParameterDisplay.getParameterValue());
            bean.setDescription(serviceParameterDisplay.getDescription());
            bean.setSendTOpc(serviceParameterDisplay.getSendTOpc());
            bean.setNmflag(serviceParameterDisplay.getNmflag());
            
            serviceParamList.add(bean);
        }
        // this function is very important.
        syncServiceParameterField(technicalServiceTemplate,serviceParamList);

        return serviceParamList;
    }

    /**
     * @param service to which serviceparameters are added
     * @param List of service parameters
     */
    private void syncServiceParameterField(final TechnicalServiceTemplate technicalServiceTemplate, final List serviceParameterList)
    {
        
    	technicalServiceTemplate.setServiceParameters(serviceParameterList);
    }

    
    protected TechnicalServiceParameterDisplay adapt(final Context ctx, List<TechnicalServiceParameter> parametersList,TechnicalServiceParameter servparam)
        throws HomeException
    {
        final TechnicalServiceParameterDisplay bean = new TechnicalServiceParameterDisplay();
        bean.setIsMandatory(servparam.getIsMandatory());
        bean.setName(servparam.getName());
                
        
        if (parametersList != null && !parametersList.isEmpty() )
        {
        	for (TechnicalServiceParameter serviceparam : parametersList)
            {
        		if(serviceparam.getName().equals(servparam.getName()))
        		{
        			bean.setChecked(true);
        			bean.setDataType(serviceparam.getDataType());
        	        bean.setParameterValue(serviceparam.getParameterValue());
        	        bean.setDescription(serviceparam.getDescription());
        	        bean.setSendTOpc(serviceparam.getSendTOpc());
                    bean.setNmflag(serviceparam.getNmflag());
                    
        	        break;
        		}
            	else
		        {
		            bean.setChecked(false);
		            bean.setDataType(servparam.getDataType());
		            bean.setParameterValue(servparam.getParameterValue());
		            bean.setDescription(servparam.getDescription());
		            bean.setSendTOpc(servparam.getSendTOpc());
                    bean.setNmflag(servparam.getNmflag());
		        }
            }
        }
        else
        {
        	bean.setDataType(servparam.getDataType());
            bean.setParameterValue(servparam.getParameterValue());
            bean.setDescription(servparam.getDescription());
            bean.setSendTOpc(servparam.getSendTOpc());
            bean.setNmflag(servparam.getNmflag());
            
	        if (servparam.getIsMandatory())
	        {
	            bean.setChecked(true);
	        }
	        else
	        {
	            bean.setChecked(false);
	        }
        }
        return bean;
    }
    
    /**
     * Disables the actions from the services
     * @param ctx the operating context
     */
    private void disableActions(final Context ctx)
    {
        ActionMgr.disableActions(ctx);
    }


    void setPropertyReadOnly(final Context ctx, final String property)
    {
        final ViewModeEnum mode = getMode(ctx, property);
        if (mode != ViewModeEnum.NONE)
        {
            setMode(ctx, property, ViewModeEnum.READ_ONLY);
        }
    }
    
    // copied from DefaultserivceEditorWebControl
    /**
     * {@inheritDoc}
     */
    @Override
    public void outputCheckBox(final Context ctx, final PrintWriter out, final String name,
            final Object bean, final boolean isChecked)
    {
        final TechnicalServiceParameterDisplay servparameter = (TechnicalServiceParameterDisplay) bean;

        out.print("<input type=\"hidden\" name=\"");
        out.print(name);
        out.print(SEPERATOR);
        out.print("name\" value=\"");
        out.print(servparameter.getName());
        out.println("\" />");

        // MAALP: 05/03/04 - TT 402262116
        // Restore the real mode and display check box based on it
        // When view mode is selected show "x" by the checked items,
        // otherwise display a regular check box
        final int mode = ctx.getInt(CommonFramework.REAL_MODE, DISPLAY_MODE);
        if (servparameter.isIsMandatory() && mode != DISPLAY_MODE)
        {
            // Modified the name so it will not interfere with Framework fields.
            out.print("<td><input type=\"checkbox\" name=\"X");
            out.print(name);
            out.print("X\" disabled value=\"X\" ");
            if (servparameter.isChecked())
            {
                out.print("checked=\"checked\"");
            }
            out.print(" /> <input type=\"hidden\" name=\"");
            out.print(name);
            out.print(SEPERATOR);
            out.print("_enabled\" value=\"X\" /> </td>");
        }
        else if (servparameter.isIsMandatory() || mode == DISPLAY_MODE)
        {
            // Modified the name so it will not interfere with Framework fields.
            out.print("<td><input type=\"checkbox\" name=\"X");
            out.print(name);
            out.print("X\" disabled value=\"X\" ");
            if (servparameter.isChecked())
            {
                out.print("checked=\"checked\"");
            }
            out.print(" /> <input type=\"hidden\" name=\"");
            out.print(name);
            out.print(SEPERATOR);
            out.print("_enabled\" value=\"X\" /> </td>");
        }
        else
        {
            super.outputCheckBox(ctx, out, name, bean, servparameter.isChecked());
        }
    }

    
    /**
     * Context MODE key.
     */
    protected static final Object MODE = "MODE";

}
