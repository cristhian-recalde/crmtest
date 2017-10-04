/**
 * 
 */
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;

import javax.servlet.ServletRequest;

import com.trilogy.app.crm.client.ringbacktone.ProvCommand;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.BooleanWebControl;
import com.trilogy.framework.xhome.webcontrol.TextAreaWebControl;
import com.trilogy.framework.xhome.webcontrol.TextFieldWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;


/**
 * @author jli
 * RBTParamWebControl is used to display default value of RBT Parameter on Create Mode.
 * It is an alternative solution to set default value of parameters used in AuxiliaryService bean.
 * 
 * Define default value of field in Model file doesn't work because it is a static value but we need to
 * retrieve default value from context during runtime.
 */
public class RBTParamWebControl implements WebControl
{
    public RBTParamWebControl(String name)
    {
        this.paramName_ = name;
        
        // DoCharging is a parameter with boolean type. 
        if (name!= null && name.equals("DoCharging"))
        {
            delegate_ = new BooleanWebControl("True","False");
        }
        else if (name!= null && name.equals("Comment"))
        {
            delegate_ = new TextAreaWebControl(20,5);
        }
    }

    /* (non-Javadoc)
     * @see com.redknee.framework.xhome.webcontrol.InputWebControl#fromWeb(com.redknee.framework.xhome.context.Context, javax.servlet.ServletRequest, java.lang.String)
     */
    public Object fromWeb(Context ctx, ServletRequest request, String name) throws NullPointerException
    {
        return delegate_.fromWeb(ctx, request, name);
    }


    /* (non-Javadoc)
     * @see com.redknee.framework.xhome.webcontrol.InputWebControl#fromWeb(com.redknee.framework.xhome.context.Context, java.lang.Object, javax.servlet.ServletRequest, java.lang.String)
     */
    public void fromWeb(Context ctx, Object obj, ServletRequest request, String name)
    {
        delegate_.fromWeb(ctx, obj, request, name);
    }


    /* (non-Javadoc)
     * @see com.redknee.framework.xhome.webcontrol.OutputWebControl#toWeb(com.redknee.framework.xhome.context.Context, java.io.PrintWriter, java.lang.String, java.lang.Object)
     */
    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
    {
        int mode = ctx.getInt("MODE", DISPLAY_MODE);
        if (mode == CREATE_MODE && paramName_ != null)
        {
            String value = ProvCommand.getDefaultValue(paramName_);
            if (paramName_.equals("DoCharging"))
            {
                obj = value!=null?value.equals("1"):Boolean.FALSE;
            }
            else
            {
                obj = value;
            }
        }
        
        delegate_.toWeb(ctx, out, name, obj);
    }
    
    private String     paramName_= null;
    private WebControl delegate_ = new TextFieldWebControl();
}
