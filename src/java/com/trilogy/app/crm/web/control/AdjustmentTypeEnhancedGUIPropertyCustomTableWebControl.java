/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee. No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used in
 * accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */

package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.script.ScriptException;

import com.trilogy.app.crm.adjustmenttype.AdjustmentTypeEnhancedGUILimitEnum;
import com.trilogy.app.crm.adjustmenttype.AdjustmentTypeEnhancedGUIPermissionEnum;
import com.trilogy.app.crm.adjustmenttype.AdjustmentTypeEnhancedGUIProperty;
import com.trilogy.app.crm.adjustmenttype.AdjustmentTypeEnhancedGUIPropertyIdentitySupport;
import com.trilogy.app.crm.adjustmenttype.AdjustmentTypeEnhancedGUIPropertyTableWebControl;
import com.trilogy.app.crm.adjustmenttype.AdjustmentTypeEnhancedGUIPropertyXInfo;
import com.trilogy.framework.core.scripting.JSchemeExecutor;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.web.action.ActionMgr;
import com.trilogy.framework.xhome.web.action.WebActionSupport;
import com.trilogy.framework.xhome.web.renderer.ButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultTableRenderer;
import com.trilogy.framework.xhome.web.renderer.TableRenderer;
import com.trilogy.framework.xhome.web.support.WebSupport;
import com.trilogy.framework.xhome.webcontrol.AbstractTableWebControl;
import com.trilogy.framework.xhome.webcontrol.ViewModeEnum;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * A copy of AdjustmentTypeEnhancedGUIPropertyTableWebControl but with arrows
 * and checkboxes disabled, and sorting/ordering by columns disabled.
 * 
 * @author cindy.wong@redknee.com
 * @since 8.3
 */
public class AdjustmentTypeEnhancedGUIPropertyCustomTableWebControl extends
        AdjustmentTypeEnhancedGUIPropertyTableWebControl
{

    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
    {

        Context context = wrapContext(ctx);
        Context subCtx = context.createSubContext();
        Context secureCtx = subCtx;
        MessageMgr mmgr = new MessageMgr(context, this);
        Map<Integer, String> nameMap = new HashMap<Integer, String>();
        Map<Integer, Set<Integer>> descendants = new HashMap<Integer, Set<Integer>>();

        // In table mode so set the TABLE_MODE to true. Used by individual web
        // controls
        subCtx.put("TABLE_MODE", true);

        int mode = context.getInt("MODE", DISPLAY_MODE);

        if (mode != DISPLAY_MODE)
        {
            secureCtx = subCtx.createSubContext();
            secureCtx.put("MODE", DISPLAY_MODE);
        }

        HttpServletRequest req = (HttpServletRequest) context
                .get(HttpServletRequest.class);
        int blanks = context.getInt(NUM_OF_BLANKS, DEFAULT_BLANKS);
        Collection beans = (Collection) obj;
        TableRenderer renderer = tableRenderer(context);
        // get the list of common actions
        List actions = ActionMgr.getActions(context);

        // The check for ACTIONS is for legacy support and should be removed at
        // some point
        boolean show_actions = context.getBoolean("ACTIONS", true)
                && ActionMgr.isEnabled(actions);

        // don't propogate ACTIONS to sub-controls
        if (show_actions)
        {
            ActionMgr.disableActions(subCtx);
        }

        if (mode == EDIT_MODE || mode == CREATE_MODE)
        {
            // The Math.max() bit is so that if blanks is set to 0 that you can
            // still add a row
            out.print("<input type=\"hidden\" name=\"" + name + SEPERATOR
                    + "_count\" value=\""
                    + (beans.size() + Math.max(1, blanks)) + "\" />");
            if (context
                    .getBoolean(com.redknee.framework.xhome.web.Constants.TABLEWEBCONTROL_REORDER_KEY))
                out.print("<input type=\"hidden\" name=\"" + name + SEPERATOR
                        + "_REORDER_KEY\" value=\"1\" />");
            else
                out.print("<input type=\"hidden\" name=\"" + name + SEPERATOR
                        + "_REORDER_KEY\" value=\"0\" />");
        }

        // WIDHT=722

        renderer.Table(ctx, out, mmgr.get("AdjustmentTypeEnhancedGUIProperty.Label",
                AdjustmentTypeEnhancedGUIPropertyXInfo.Label));

        out.println("<tr>");

        if (mode == EDIT_MODE || mode == CREATE_MODE)
        {

            if (context
                    .getBoolean(com.redknee.framework.xhome.web.Constants.TABLEWEBCONTROL_REORDER_KEY))
            {
                // this is for the up/down arrows
                out.print("<th>&nbsp;</th>");
            }
        }

        // default
        String img_src = "";

        ViewModeEnum code_mode = getMode(subCtx,
                "AdjustmentTypeEnhancedGUIProperty.code");
        ViewModeEnum name_mode = getMode(subCtx,
                "AdjustmentTypeEnhancedGUIProperty.name");
        ViewModeEnum permission_mode = getMode(subCtx,
                "AdjustmentTypeEnhancedGUIProperty.permission");
        ViewModeEnum limitSet_mode = getMode(subCtx,
                "AdjustmentTypeEnhancedGUIProperty.limitSet");
        ViewModeEnum limit_mode = getMode(subCtx,
                "AdjustmentTypeEnhancedGUIProperty.limit");
        if (code_mode != ViewModeEnum.NONE)
        {
            out
                    .println("<th TITLE=\""
                            + mmgr
                                    .get(
                                            "AdjustmentTypeEnhancedGUIProperty.code.Label",
                                            AdjustmentTypeEnhancedGUIPropertyXInfo.CODE.getLabel(ctx))
                            + "\" >");
            out
                    .println(mmgr
                            .get(
                                    "AdjustmentTypeEnhancedGUIProperty.code.ColumnLabel",
                                    AdjustmentTypeEnhancedGUIPropertyXInfo.CODE.getColumnLabel(ctx)));

            out.println("</th>");

        }

        if (name_mode != ViewModeEnum.NONE)
        {
            out
                    .println("<th TITLE=\""
                            + mmgr
                                    .get(
                                            "AdjustmentTypeEnhancedGUIProperty.name.Label",
                                            AdjustmentTypeEnhancedGUIPropertyXInfo.NAME.getLabel(ctx))
                            + "\" >");
            out
                    .println(mmgr
                            .get(
                                    "AdjustmentTypeEnhancedGUIProperty.name.ColumnLabel",
                                    AdjustmentTypeEnhancedGUIPropertyXInfo.NAME.getColumnLabel(ctx)));

            out.println("</th>");

        }

        if (permission_mode != ViewModeEnum.NONE)
        {
            out
                    .println("<th TITLE=\""
                            + mmgr
                                    .get(
                                            "AdjustmentTypeEnhancedGUIProperty.permission.Label",
                                            AdjustmentTypeEnhancedGUIPropertyXInfo.PERMISSION.getLabel(ctx))
                            + "\" >");
            out
                    .println(mmgr
                            .get(
                                    "AdjustmentTypeEnhancedGUIProperty.permission.ColumnLabel",
                                    AdjustmentTypeEnhancedGUIPropertyXInfo.PERMISSION.getColumnLabel(ctx)));

            out.println("</th>");

        }

        if (limitSet_mode != ViewModeEnum.NONE)
        {
            out
                    .println("<th TITLE=\""
                            + mmgr
                                    .get(
                                            "AdjustmentTypeEnhancedGUIProperty.limitSet.Label",
                                            AdjustmentTypeEnhancedGUIPropertyXInfo.LIMIT_SET.getLabel(ctx))
                            + "\" >");
            out
                    .println(mmgr
                            .get(
                                    "AdjustmentTypeEnhancedGUIProperty.limitSet.ColumnLabel",
                                    AdjustmentTypeEnhancedGUIPropertyXInfo.LIMIT_SET.getColumnLabel(ctx)));

            out.println("</th>");

        }

        if (limit_mode != ViewModeEnum.NONE)
        {
            out
                    .println("<th TITLE=\""
                            + mmgr
                                    .get(
                                            "AdjustmentTypeEnhancedGUIProperty.limit.Label",
                                            AdjustmentTypeEnhancedGUIPropertyXInfo.LIMIT.getLabel(ctx))
                            + "\" >");
            out
                    .println(mmgr
                            .get(
                                    "AdjustmentTypeEnhancedGUIProperty.limit.ColumnLabel",
                                    AdjustmentTypeEnhancedGUIPropertyXInfo.LIMIT.getColumnLabel(ctx)));

            out.println("</th>");

        }

        if (show_actions)
        {
            // out.println("<th>Actions</th>");
            out.println("<th>");
            out.println(mmgr.get("SummaryTable.Actions.Label", "Actions"));
            out.println("</th>");
        }

        out.println("</tr>");

        Iterator i = beans.iterator();
        final int count = beans.size();
        int rowStart = 0;

        if ((mode == EDIT_MODE || mode == CREATE_MODE)
                && !context.has(DISABLE_NEW)
                && context.getBoolean(ENABLE_ADDROW_BUTTON))
        {
            out.print("<tr style=\"display:none\">");

            // For the down only arrow-set
            renderer.TD(context, out);
            out
                    .println("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td>");
            out.println("<img src=\"/images/list/up-dark.gif\"></img>");
            out.println("</td></tr><tr><td>");
            out.println("<img onclick=\"swapTableLines(this,-2,-1,'" + name
                    + "','" + WebSupport.fieldToId(context, name)
                    + "');\" src=\"/images/list/down.gif\"></img>");
            out.println("</td></tr></table>");
            renderer.TDEnd(context, out);

            // For the up arrow-set
            renderer.TD(context, out);
            out
                    .println("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td>");
            out.println("<img onclick=\"swapTableLines(this,-2,-3,'" + name
                    + "','" + WebSupport.fieldToId(context, name)
                    + "');\" src=\"/images/list/up.gif\"></img>");
            out.println("</td></tr><tr><td>");
            out.println("<img src=\"/images/list/down-dark.gif\"></img>");
            out.println("</td></tr></table>");
            renderer.TDEnd(context, out);

            // For the bi-directional only arrow-set
            renderer.TD(context, out);
            out
                    .println("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td>");
            out.println("<img onclick=\"swapTableLines(this,-2,-3,'" + name
                    + "','" + WebSupport.fieldToId(context, name)
                    + "');\" src=\"/images/list/up.gif\"></img>");
            out.println("</td></tr><tr><td>");
            out.println("<img onclick=\"swapTableLines(this,-2,-1,'" + name
                    + "','" + WebSupport.fieldToId(context, name)
                    + "');\" src=\"/images/list/down.gif\"></img>");
            out.println("</td></tr></table>");
            renderer.TDEnd(context, out);

            // For both black arrows
            renderer.TD(context, out);
            out
                    .println("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td>");
            out.println("<img src=\"/images/list/up-dark.gif\"></img>");
            out.println("</td></tr><tr><td>");
            out.println("<img src=\"/images/list/down-dark.gif\"></img>");
            out.println("</td></tr></table>");
            renderer.TDEnd(context, out);

            out.print("</tr>");
            rowStart = -1;
        }

        final int start = rowStart;

        for (int j = start; j < count; j++)
        {
            AdjustmentTypeEnhancedGUIProperty bean;

            bean = (AdjustmentTypeEnhancedGUIProperty) i.next();

            /*
             * [Cindy Wong] 2010-02-17: add to map.
             */
            nameMap.put(bean.getCode(), name + SEPERATOR + j);
            Integer parentKey = new Integer(bean.getParentCode());
            Integer thisKey = new Integer(bean.getCode());
            if (bean.getParentCode() >= 0)
            {
                Set<Integer> siblings = descendants.get(parentKey);
                if (siblings == null)
                {
                    siblings = new TreeSet<Integer>();
                }
                siblings.add(thisKey);
                descendants.put(parentKey, siblings);
            }

            /*
             * if (j < 0) { out.print("<tr style=\"display:none\">"); } else {
             */
            renderer.TR(context, out, bean, j);
            // }

            // icons for up/down
            if (mode == EDIT_MODE || mode == CREATE_MODE)
            {
                if (context
                        .getBoolean(com.redknee.framework.xhome.web.Constants.TABLEWEBCONTROL_REORDER_KEY))
                {
                    renderer.TD(context, out);
                    out
                            .println("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td>");
                    if (context.getBoolean(ENABLE_ADDROW_BUTTON)
                            && beans.size() == 1 && j != -1)
                    {
                        // For both black arrows when 1)Normal: NOWAY...
                        // 2)Dynamic -- displaying the only one row in table
                        // (not the hidden row)
                        out
                                .println("<img src=\"/images/list/up-dark.gif\"></img>");
                        out.println("</td></tr><tr><td>");
                        out
                                .println("<img src=\"/images/list/down-dark.gif\"></img>");
                    }
                    else if (j == 0)
                    {
                        out
                                .println("<img src=\"/images/list/up-dark.gif\"></img>");
                        out.println("</td></tr><tr><td>");
                        // out.println("<img onclick=\"swapTableLines('"+name +
                        // "','" +
                        // SEPERATOR+"',"+j+","+(j+1)+",new Array('_enabled','code','name','permission','limitSet','limit'));\" src=\"./images/list/down.gif\"></img>");
                        out
                                .println("<img onclick=\"swapTableLines(this,"
                                        + j
                                        + ","
                                        + (j + 1)
                                        + ",'"
                                        + name
                                        + "','"
                                        + WebSupport.fieldToId(context, name)
                                        + "');\" src=\"/images/list/down.gif\"></img>");
                    }
                    else if ((j == count - 1) && (j != -1))
                    {
                        out.println("<img onclick=\"swapTableLines(this," + j
                                + "," + (j - 1) + ",'" + name + "','"
                                + WebSupport.fieldToId(context, name)
                                + "');\" src=\"/images/list/up.gif\"></img>");
                        out.println("</td></tr><tr><td>");
                        out
                                .println("<img src=\"/images/list/down-dark.gif\"></img>");
                    }
                    else
                    {
                        out.println("<img onclick=\"swapTableLines(this," + j
                                + "," + (j - 1) + ",'" + name + "','"
                                + WebSupport.fieldToId(context, name)
                                + "');\" src=\"/images/list/up.gif\"></img>");
                        // out.println("<img onclick=\"swapTableLines('"+name +
                        // "','" +
                        // SEPERATOR+"',"+j+","+(j-1)+",new Array('_enabled','code','name','permission','limitSet','limit'));\" src=\"./images/list/up.gif\"></img>");
                        out.println("</td></tr><tr><td>");
                        out
                                .println("<img onclick=\"swapTableLines(this,"
                                        + j
                                        + ","
                                        + (j + 1)
                                        + ",'"
                                        + name
                                        + "','"
                                        + WebSupport.fieldToId(context, name)
                                        + "');\" src=\"/images/list/down.gif\"></img>");
                        // out.println("<img onclick=\"swapTableLines('"+name +
                        // "','" +
                        // SEPERATOR+"',"+j+","+(j+1)+",new Array('_enabled','code','name','permission','limitSet','limit'));\" src=\"./images/list/down.gif\"></img>");
                    }
                    out.println("</td></tr></table>");
                    renderer.TDEnd(context,out);
                }
            }

            // checkbox: shwon only in edit/create mode and the "AddRow" button
            // is disable
            if ((mode == EDIT_MODE || mode == CREATE_MODE)
                    && !context.getBoolean(ENABLE_ADDROW_BUTTON))
            {
                // outputCheckBox(context, out, name + SEPERATOR + j, bean, b);
            }

            subCtx.put(BEAN, bean);

            if (code_mode != ViewModeEnum.NONE)
            {

                renderer.TD(subCtx,out);

                // Some nested WebControls may want to know about the particular
                // bean property they are dealing with.
                subCtx.put(PROPERTY,
                        AdjustmentTypeEnhancedGUIPropertyXInfo.CODE);
                getCodeWebControl().toWeb(
                        (code_mode == ViewModeEnum.READ_ONLY) ? secureCtx
                                : subCtx, out,
                        name + SEPERATOR + j + SEPERATOR + "code",
                        Integer.valueOf(bean.getCode()));
                renderer.TDEnd(subCtx,out);

            }

            if (name_mode != ViewModeEnum.NONE)
            {

                renderer.TD(subCtx, out);

                // Some nested WebControls may want to know about the particular
                // bean property they are dealing with.
                subCtx.put(PROPERTY,
                        AdjustmentTypeEnhancedGUIPropertyXInfo.NAME);
                getNameWebControl().toWeb(
                        (name_mode == ViewModeEnum.READ_ONLY) ? secureCtx
                                : subCtx, out,
                        name + SEPERATOR + j + SEPERATOR + "name",
                        bean.getName());
                renderer.TDEnd(subCtx,out);

            }

            if (permission_mode != ViewModeEnum.NONE)
            {

                renderer.TD(subCtx, out);

                // Some nested WebControls may want to know about the particular
                // bean property they are dealing with.
                subCtx.put(PROPERTY,
                        AdjustmentTypeEnhancedGUIPropertyXInfo.PERMISSION);
                getPermissionWebControl().toWeb(
                        (permission_mode == ViewModeEnum.READ_ONLY) ? secureCtx
                                : subCtx, out,
                        name + SEPERATOR + j + SEPERATOR + "permission",
                        Short.valueOf(bean.getPermission()));
                renderer.TDEnd(subCtx, out);

            }

            if (limitSet_mode != ViewModeEnum.NONE)
            {

                renderer.TD(subCtx,out);

                // Some nested WebControls may want to know about the particular
                // bean property they are dealing with.
                subCtx.put(PROPERTY,
                        AdjustmentTypeEnhancedGUIPropertyXInfo.LIMIT_SET);
                getLimitSetWebControl().toWeb(
                        (limitSet_mode == ViewModeEnum.READ_ONLY) ? secureCtx
                                : subCtx, out,
                        name + SEPERATOR + j + SEPERATOR + "limitSet",
                        Short.valueOf(bean.getLimitSet()));
                renderer.TDEnd(subCtx,out);

            }

            if (limit_mode != ViewModeEnum.NONE)
            {

                renderer.TD(subCtx,out);

                // Some nested WebControls may want to know about the particular
                // bean property they are dealing with.
                subCtx.put(PROPERTY,
                        AdjustmentTypeEnhancedGUIPropertyXInfo.LIMIT);
                getLimitWebControl().toWeb(
                        (limit_mode == ViewModeEnum.READ_ONLY) ? secureCtx
                                : subCtx, out,
                        name + SEPERATOR + j + SEPERATOR + "limit",
                        Long.valueOf(bean.getLimit()));
                renderer.TDEnd(subCtx,out);

            }

            if (show_actions)
            {
                List beanActions = ActionMgr.getActions(context, bean);
                if (beanActions == null)
                {
                    // use the backup actions from the home level
                    beanActions = actions;
                }
                ((WebActionSupport) context.get(WebActionSupport.class))
                        .writeLinks(subCtx, beanActions, out, bean,
                                AdjustmentTypeEnhancedGUIPropertyIdentitySupport
                                        .instance());
            }

            if ((mode == EDIT_MODE || mode == CREATE_MODE)
                    && !context.getBoolean(DISABLE_NEW)
                    && context.getBoolean(ENABLE_ADDROW_BUTTON))
            {
                out.println("<TD>");
                out
                        .println("<img onclick=\"removeRow(this,'"
                                + name
                                + "');\" src=\"ButtonRenderServlet?.src=abc &.label=Delete\"/>");
                out.println("</TD>");
            }
            renderer.TREnd(subCtx,out);
        }
        if ((mode == EDIT_MODE || mode == CREATE_MODE)
                && context.getBoolean(ENABLE_ADDROW_BUTTON)
                && !context.getBoolean(DISABLE_NEW))
        {
            ButtonRenderer br = (ButtonRenderer) context.get(
                    ButtonRenderer.class, DefaultButtonRenderer.instance());
            out.println("<tr><td colspan=4>");

            out
                    .println("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td>");

            out.println("<input type=\"text\" name=\"" + name
                    + ".addRowCount\" size=\"3\" value=\"1\"/></td><td>");
            out
                    .println("<img onclick=\"addRow(this,'"
                            + name
                            + "','"
                            + WebSupport.fieldToId(context, name)
                            + "');\" src=\"ButtonRenderServlet?.src=abc &.label=Add\"/>");
            out.println("</td></tr></table>");
            out.println("</td></tr>");
        }

        renderer.TableEnd(subCtx,out, (String) context.get(FOOTER));

        out.println("<script type=\"text/javascript\">");
        out.println("var descendants = new Array();");
        for (Integer parent : descendants.keySet())
        {
            String aName = "a" + parent.toString();
            out.print("var ");
            out.print(aName);
            out.println(" = new Array();");

            Set<Integer> children = descendants.get(parent);
            int j = 0;
            for (Integer child : children)
            {
                out.print(aName);
                out.print("[");
                out.print(j);
                out.print("] = ");
                out.print(child);
                out.println(";");
                j++;
            }

            out.print("descendants[");
            out.print(parent);
            out.print("] = ");
            out.print(aName);
            out.println(";");
        }

        out.println("");

        out.println("var nameMap = new Array();");
        for (Integer key : nameMap.keySet())
        {
            out.print("nameMap[");
            out.print(key);
            out.print("] = \"");
            out.print(nameMap.get(key));
            out.println("\";");
        }

        out.println("");

        out.println("function findElement(currentForm, adjType, elemType) {");
        out.println("  var name = nameMap[adjType] + \".\" + elemType;");
        out.println("  var elems = document.getElementsByName(name);");
        out.println("  if (elems != null && elems.length > 0) { return elems[0]; }");
        out.println("  return null;");
        out.println("}");
        out.println("");

        out
                .println("function enableDisableLimitSet(currentForm, adjType, enable) {");
        out
                .println("  var limitSetElem = findElement(currentForm, adjType, \"limitSet\");");
        out.println("  if (enable) {");
        out.println("    limitSetElem.disabled = false;");
        out.print("    var customLimit = (limitSetElem.value == \"");
        out.print(AdjustmentTypeEnhancedGUILimitEnum.CUSTOM_INDEX);
        out.println("\");");
        out
                .println("    enableDisableLimit(currentForm, adjType, customLimit);");
        out.println("  } else {");
        out.println("    limitSetElem.disabled = true;");
        out.println("  }");
        out.println("}");

        out
                .println("function enableDisableLimit(currentForm, adjType, enable) {");
        out
                .println("  var limitElem = findElement(currentForm, adjType, \"limit\");");
        out.println("  if (enable) {");
        out.println("      limitElem.disabled = false;");
        out.println("  } else {");
        out.println("    limitElem.disabled = true;");
        out.println("  }");
        out.println("}");
        out.println("");

        out
                .println("function changeChildrenPermission(currentForm, adjType, allow) {");
        out.println("  enableDisableLimitSet(currentForm, adjType, allow);");
        out.println("  var children = descendants[adjType];");
        out.println("  if (children != null) {");
        out.println("    for (var i = 0; i < children.length; i++) {");
        out
                .println("      var elem = findElement(currentForm, children[i], \"permission\");");
        out.println("      if (allow) {");
        out.println("        elem.disabled = true;");
        out.print("        elem.value = \"");
        out.print(AdjustmentTypeEnhancedGUIPermissionEnum.ALLOWED_INDEX);
        out.println("\";");
        out
                .println("        changeChildrenPermission(currentForm, children[i], allow);");
        out.println("      } else {");
        out.println("        elem.disabled = false;");
        out.print("        var childrenAllowed = elem.value == \"");
        out.print(AdjustmentTypeEnhancedGUIPermissionEnum.ALLOWED_INDEX);
        out.println("\";");
        out
                .println("        changeChildrenPermission(currentForm, children[i], childrenAllowed);");
        out.println("      }");
        out.println("    }");
        out.println("  }");
        out.println("}");
        out.println("");

        out.println("function permissionUpdate(event) {");
        out.println("  if (!event) { var event = window.event; }");
        out
                .println("  if (!event.target) { event.target = event.srcElement; }");
        out
                .println("  if (!event.currentTarget) { event.currentTarget = event.srcElement; }");

        // find the current element
        out.println("  var loc = document.location.toString();");
        out.println("  var targetName = event.target.name;");
        out.println("  var source = event.currentTarget;");
        out.println("  var soureFormNum = 0");
        out.println("  var sourceElem = null");
        out.println("  for (var i = 0; i < document.forms.length; i++) { ");
        out.println("    var form = document.forms[i];");
        out.println("    for (var j = 0; j < form.elements.length; j++) { ");
        out.println("      var thisElem = form.elements[j];");
        out.println("      if (thisElem.name == source.name) {");
        out.println("        sourceFormNum = i;");
        out.println("        sourceElem = thisElem;");
        out.println("        break;");
        out.println("      }");
        out.println("    }");
        out.println("  }");

        // find the current adj type
        out.println("  var adjType = 0;");
        out.println("  for (var i in nameMap) {");
        out.println("    var name = nameMap[i] + \".permission\";");
        out.println("    if (sourceElem.name == name) {");
        out.println("      adjType = i");
        out.println("      break;");
        out.println("    }");
        out.println("  }");

        out.print("  var allowed = sourceElem.value == \"");
        out.print(AdjustmentTypeEnhancedGUIPermissionEnum.ALLOWED_INDEX);
        out.println("\";");
        out
                .println("  changeChildrenPermission(document.forms[sourceFormNum], adjType, allowed);");
        out.println("}");
        out.println("");

        out.println("function limitSetUpdate(event) {");
        out.println("  if (!event) { var event = window.event; }");
        out
                .println("  if (!event.target) { event.target = event.srcElement; }");
        out
                .println("  if (!event.currentTarget) { event.currentTarget = event.srcElement; }");

        // find the current element
        out.println("  var loc = document.location.toString();");
        out.println("  var targetName = event.target.name;");
        out.println("  var source = event.currentTarget;");
        out.println("  var soureFormNum = 0");
        out.println("  var sourceElem = null");
        out.println("  for (var i = 0; i < document.forms.length; i++) { ");
        out.println("    var form = document.forms[i];");
        out.println("    for (var j = 0; j < form.elements.length; j++) { ");
        out.println("      var thisElem = form.elements[j];");
        out.println("      if (thisElem.name == source.name) {");
        out.println("        sourceFormNum = i;");
        out.println("        sourceElem = thisElem;");
        out.println("        break;");
        out.println("      }");
        out.println("    }");
        out.println("  }");

        // find the current adj type
        out.println("  var adjType = 0;");
        out.println("  for (var i in nameMap) {");
        out.println("    var name = nameMap[i] + \".limitSet\";");
        out.println("    if (sourceElem.name == name) {");
        out.println("      adjType = i");
        out.println("      break;");
        out.println("    }");
        out.println("  }");

        out.print("  var customLimit = sourceElem.value == \"");
        out.print(AdjustmentTypeEnhancedGUILimitEnum.CUSTOM_INDEX);
        out.println("\";");
        out
                .println("  enableDisableLimit(document.forms[sourceFormNum], adjType, customLimit);");
        out.println("}");
        out.println("");
        out.println("</script>");
    }

    /**
     * Mostly copied fromWeb from
     * AdjustmentTypeEnhancedGUIPropertyTableWebControl, except the bean web
     * control is changed.
     * 
     * @see com.redknee.app.crm.adjustmenttype.AdjustmentTypeEnhancedGUIPropertyTableWebControl#fromWeb(com.redknee.framework.xhome.context.Context,
     *      java.lang.Object, javax.servlet.ServletRequest, java.lang.String)
     */
    public void fromWeb(Context ctx, Object obj, ServletRequest req, String name)
    {

        try
        {
            Collection beans = (Collection) obj;
            int count = Integer.parseInt(req.getParameter(name + SEPERATOR
                    + "_count"));

            for (int i = 0; i < count; i++)
            {

                String hidden = req.getParameter(name + SEPERATOR + i
                        + SEPERATOR
                        + "AdjustmentTypeEnhancedGUIProperty.hidden");
                if (hidden != null)
                {
                    try
                    {
                        beans.add(JSchemeExecutor.instance().retrieveObject(
                                ctx, hidden, ""));
                    }
                    catch (ScriptException e)
                    {
                        // TODO: report
                        new com.redknee.framework.xlog.log.MajorLogMsg(this, e
                                .getMessage(), e).log(ctx);
                    }
                }
                else
                {
                    try
                    {
                        beans.add(getBeanWebControl().fromWeb(ctx, req,
                                name + SEPERATOR + i));
                    }
                    catch (Exception t)
                    {
                        // Don't let one bad apple spoil the whole lot, KGR
                        LogSupport.ignore(ctx, this, t.getMessage(), t);
                    }
                }
            }
            
            Map<Integer, Boolean> allowed = new HashMap<Integer, Boolean>();
            for (Object o : beans)
            {
                AdjustmentTypeEnhancedGUIProperty p = (AdjustmentTypeEnhancedGUIProperty)o;
                Integer key = new Integer(p.getCode());
                Integer parentKey = new Integer(p.getParentCode());
                if (allowed.containsKey(parentKey))
                {
                    boolean parentAllowed = allowed.get(parentKey).booleanValue();
                    p.setPermissionEditable(!parentAllowed);
                    if (parentAllowed)
                    {
                        p.setPermission(AdjustmentTypeEnhancedGUIPermissionEnum.ALLOWED_INDEX);
                    }
                }
                allowed.put(key, p.getPermission() == AdjustmentTypeEnhancedGUIPermissionEnum.ALLOWED_INDEX);
            }
        }
        catch (NumberFormatException e)
        {
            throw new NullPointerException("no data");
            // System.err.println("Unexpected missing counter for " + name);
        }
    }

    public Context wrapContext(Context ctx)
    {
        Context subContext = ctx.createSubContext();
        subContext
                .put(
                        com.redknee.framework.xhome.web.Constants.TABLEWEBCONTROL_REORDER_KEY,
                        false);
        subContext.put(AbstractTableWebControl.ENABLE_ADDROW_BUTTON, false);
        subContext.put(AbstractTableWebControl.HIDE_CHECKBOX, true);
        subContext.put(AbstractTableWebControl.DISABLE_NEW, true);
        return subContext;
    }

    public TableRenderer tableRenderer(Context ctx)
    {
        return tableRenderer_;
    }

    private TableRenderer tableRenderer_ = new DefaultTableRenderer()
    {
        public void TD(PrintWriter out)
        {
            out.print("  <td nowrap=\"nowrap\">");
        }
    };

    public WebControl getBeanWebControl()
    {
        return beanWebControl;
    }

    public WebControl getCodeWebControl()
    {
        return codeWebControl_;
    }

    protected WebControl codeWebControl_ = new AdjustmentTypeEnhancedGUIPropertyCodeWebControl();
    protected static final WebControl beanWebControl = new AdjustmentTypeEnhancedGUIPropertyCustomWebControl();
}
