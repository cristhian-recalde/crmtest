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
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;

import com.trilogy.app.crm.CommonFramework;
import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.AuxiliaryServiceSelection;
import com.trilogy.app.crm.bean.AuxiliaryServiceSelectionXInfo;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.framework.auth.AuthMgr;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.OutputWebControl;
import com.trilogy.framework.xhome.webcontrol.ViewModeEnum;

/**
 * Creates the specialized TableWebControl that modifies the way the default
 * TableWebControl handles the accept/reject checkboxes that appear in the first
 * column of the table.
 *
 * @author cindy.wong@redknee.com
 */
public class SctAuxiliaryServiceSelectionTableWebControl extends EnhancedAuxiliaryServiceSelectionTableWebControl
{
    /**
     * {@inheritDoc}
     */
    @Override
    public void toWeb(final Context ctx, final PrintWriter out, final String name, final Object obj)
    {
        /*
         * If the mode is DISPLAY, then the TableWebController will not add the column of
         * accept/reject checkboxes.
         */
        final int mode = ctx.getInt(CommonFramework.MODE, OutputWebControl.DISPLAY_MODE);
        final Context subContext = ctx.createSubContext();
        if (mode == OutputWebControl.DISPLAY_MODE)
        {
            subContext.put(CommonFramework.REAL_MODE, mode);
            subContext.put(CommonFramework.MODE, OutputWebControl.EDIT_MODE);
        }
        /*
         * NUM_OF_BLANKS controls the number of blank entries that appear at the bottom of
         * the list (meant for adding to the list).
         */
        subContext.put(NUM_OF_BLANKS, -1);

        setMode(subContext, AuxiliaryServiceSelectionXInfo.SELECTION_IDENTIFIER, ViewModeEnum.READ_ONLY);
        setMode(subContext, AuxiliaryServiceSelectionXInfo.SECONDARY_ID, ViewModeEnum.READ_ONLY);

        super.toWeb(subContext, out, name, obj);
    }

    
    private boolean hasAccessRights(Context ctx, AuxiliaryServiceSelection selection)
    {
       return hasPermission(ctx, selection.getAuxiliarService(ctx).getPermission());
    }

    static private boolean hasRightForCug(Context ctx, AuxiliaryServiceSelection selection)
    {
        if (selection.getType().equals(AuxiliaryServiceTypeEnum.CallingGroup) && selection.isChecked())
        {
            return hasPermission(ctx,"app.ff.root.Ff");
        }
       	if (selection.getAuxiliarService(ctx).isPrivateCUG(ctx))
        {
       		return false; 
        }
        return true;
    }

    
    static private boolean isPcug(Context ctx, AuxiliaryServiceSelection selection)
    {

       	return (selection.getAuxiliarService(ctx).isPrivateCUG(ctx));
    }


    static private boolean hasPermission(Context ctx, String permssion)
    {
        AuthMgr authMgr = new AuthMgr(ctx);
        return authMgr.check(permssion);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void outputCheckBox(final Context context, final PrintWriter out, final String name, final Object bean,
        final boolean isChecked)
    {
        /*
         * We're overriding the normal usage of the accept/reject checkboxes to more
         * explicitly associate acception/rejection with the beans. The motivation for
         * this behavior is that many table entries need to appear that are not selected.
         * Normal behavior of the table is to remove those entries that are not selected.
         */
        final AuxiliaryServiceSelection selection = (AuxiliaryServiceSelection) bean;
        AuxiliaryService auxService = selection.getAuxiliarService(context);
        if (auxService != null)
        {
        /*
         * We always need the hidden value of the identifier of the bean.
         */
        //AHLOT TT#12101905016
        //Have to include the hidden field into the TD
        	
        out.print("<td>");
        //AHLOT TT#12101905016
        out.print("<input type=\"hidden\" name=\"");
        out.print(name);
        out.print(OutputWebControl.SEPERATOR);
        out.print("selectionIdentifier\" value=\"");
        out.print(selection.getSelectionIdentifier());
        out.println("\" />");
        out.print("<input type=\"hidden\" name=\"");
        out.print(name);
        out.print(SEPERATOR);
        out.print("secondaryId\" value=\"");
        out.print(selection.getSecondaryId());
        out.println("\" />");
        final int mode = context.getInt(CommonFramework.REAL_MODE, OutputWebControl.EDIT_MODE);
        if (!hasAccessRights(context, selection))
        {
            drawReadOnlyCheckBox(out, name, selection.isChecked());
        }
        else if (mode == OutputWebControl.DISPLAY_MODE || isPcug(context, selection))
        {
        	/*
             * In the case that the actual mode is DISPLAY, we don't want editable
             * checkboxes. Instead, draw a disabled checkbox. Modified the name so it
             * will not interfere with Framework fields.
             */
            if (isSelected(context, selection) || (!hasRightForCug(context, selection)))
            {
                drawReadOnlyCheckBox(out, name, true);
            }
            else
            {
                drawReadOnlyCheckBox(out, name, false);
            }
        }
        else
        {
        	//12101905016
        	outputCheckBox_(context,out,name,bean, selection.isChecked());
//            super.outputCheckBox(context, out, name, bean, selection.isChecked());
        }
        out.println("</td>");
    }
}
  //AHLOT TT#12101905016
    private void outputCheckBox_(Context ctx, PrintWriter out, String name, Object bean, boolean checked)
    {
        out.print(" <input");
        if (ctx.getBoolean(HIDE_CHECKBOX))
        {
            out.print(" type=\"hidden\"");
        }
        else
        {
            out.print(" type=\"checkbox\"");
        }
        out.print(" name=\"");
        out.print(name);
        out.print(SEPERATOR);
        out.print("_enabled\" value=\"X\"");
        if (checked)
        {
            out.print(" checked=\"checked\"");
        }
        out.print(" />");
    }
	//AHLOT TT#12101905016
    
    private void drawReadOnlyCheckBox(final PrintWriter out, final String name, final boolean isChecked)
    {
    	//AHLOT TT#12101905016
        out.print("<input type=\"checkbox\" name=\"X");
//      out.print("<td><input type=\"checkbox\" name=\"X");
        out.print(name);
        out.print("X\" disabled value=\"X\" ");
        if(isChecked)
        {
            out.print("checked=\"checked\" /> ");
            out.print("<input type=\"hidden\" name=\"");
            out.print(name);
            out.print(OutputWebControl.SEPERATOR);
          //AHLOT TT#12101905016
            out.print("_enabled\" value=\"X\" />");
//            out.print("_enabled\" value=\"X\" /> </td>");
        } else
        {
            out.print(" /> ");
//          out.print(" /> </td>");
        }
        
    }
    /**
     * This method is needed to differentiate between SCT needs and normal Subscription Auxiliary Services needs,
     * namely vpnChecked() call.
     *
     * @param context the operating context.
     * @param selection Auxiliary Service Selection for which to determine isSelected value.
     * @return true if auxiliary service is selected
     */
    protected boolean isSelected(final Context context, final AuxiliaryServiceSelection selection)
    {
        return selection.isChecked();
    }
}
