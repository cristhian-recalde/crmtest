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
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.util.regex.Pattern;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.support.WebSupport;
import com.trilogy.framework.xhome.webcontrol.TextFieldWebControl;


public abstract class AbstractTextFieldRegexCheckingWebControl extends TextFieldWebControl
{

    @Override
    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
    {
        ctx = ctx.createSubContext();
        outputCustomJScripts(ctx, out, name, obj);
        super.toWeb(ctx, out, name, obj);
        out.print("<div id=\"");
        out.print(WebSupport.fieldToId(ctx, name) + "_msg");
        out.print("\" >");
        out.println("</div>");
    }


    /**
     * Returns the pattern the target field must match
     * @param ctx
     * @param name
     * @param obj
     * @return
     */
    public abstract String getPatternRegExToMatch(Context ctx, String name, Object obj);
    
    /**
     * Returns if the the target field should be unique in table (Only used when the field is in a table (Map, List etc)
     * @param ctx
     * @param name
     * @param obj
     * @return
     */
    public abstract boolean isUniqueInTable(Context ctx, String name , Object obj);
    
    public String getMismatchHTML(Context ctx, String name, Object obj)
    {
        
        return "<font size=\"1\" color=\"red\"><i>Should match Pattern " + getPatternRegExToMatch(ctx, name, obj) + ".</i></font>";
    }
    
    public String getNotUniqueHTML(Context ctx, String name, Object obj)
    {
        
        return "<font size=\"1\" color=\"red\"><i>Should be unique.</i></font>";
    }


    @Override
    protected void outputCustomAttributes(Context ctx, PrintWriter out)
    {
        out.println("\" onChange=\"" + getOnChageJScriptFunctionName(ctx) + "(this)\" ");
        out.println("\" onkeypress=\"" + getOnChageJScriptFunctionName(ctx) + "(this)\" ");
        out.println("\" onkeyup=\"" + getOnChageJScriptFunctionName(ctx) + "(this)\" ");
    }

    public final static String DEFAULT_PATTERN = "\\w*";


    private String getOnChageJScriptFunctionName(Context ctx)
    {
        return "onChange" + hashCode();
    }


    private void outputCustomJScripts(Context ctx, PrintWriter out, String name, Object obj)
    {
        out.println(" <script type=\"text/javascript\" >");
        printJscriptQualifyPattern(ctx, out, name, obj);
        printJscriptOnChange(ctx, out, name, obj);
        printJscriptOnChangeRow(ctx, out, name, obj);
        out.println("</script>");
    }


    private void printJscriptQualifyPattern(Context ctx, PrintWriter out, String name, Object obj)
    {
        out.println(" function isQualifyPattern(value,pattern)");
        out.println("{");
        out.println("if ((null == pattern) || (pattern.length < 1) || (null == value) || (value.length < 1))");
        out.println("     { return true; } ");
        out.println("var matches = value.match(pattern);");
        out.println(" if( (null != matches) && (matches.length == 1 ) && ( matches[0] == value ))");
        out.println("     { return true; } ");
        out.println("else");
        out.println("     { return false; } ");
        out.println("}");
    }


    private void printJscriptOnChange(Context ctx, PrintWriter out, String name, Object obj)
    {
        out.println(" function " + getOnChageJScriptFunctionName(ctx) + "(textField)");
        out.println("{");
        out.println(" var errorMessage = '' ;");
        out.println(" var isMatchPattern = true ;");
        out.println(" if(!isQualifyPattern(textField.value,'"+ getPatternRegExToMatch(ctx, name, obj) + "'))");
        out.println(" {");
        out.println("\t textField.setAttribute('style', 'background-color: red;');");
        out.println("\t document.getElementById(textField.id + '_msg').innerHTML='" + getMismatchHTML(ctx, name, obj) + "';");
        out.println("\t isMatchPattern = false ;");
        out.println(" }");
        out.println(" else ");
        out.println(" {");
        out.println("\t textField.setAttribute('style', 'background-color: white;');");
        out.println("\t document.getElementById(textField.id + '_msg').innerHTML='';");
        out.println("\t isMatchPattern = true ;");
        out.println(" }");
        if(isUniqueInTable(ctx, name, obj))
        {
            final String[] tableTokens = WebSupport.fieldToId(ctx, name).split("_+\\d+_+");
            if (tableTokens.length > 1)
            {
                out.println(" if(true == isMatchPattern)");
                out.println(" {");
                out.println("\t if(isThisFieldNotUniqueAmongRows(textField,'" + tableTokens[0] + "','"
                        + tableTokens[1] + "'))");
                out.println("\t {");
                out.println("\t\t textField.setAttribute('style', 'background-color: red;');");
                out.println("\t\t document.getElementById(textField.id + '_msg').innerHTML='"
                        + getNotUniqueHTML(ctx, name, obj) + "';");
                out.println(" }");
                out.println("\t else ");
                out.println("\t {");
                out.println("\t\t textField.setAttribute('style', 'background-color: white;');");
                out.println("\t\t document.getElementById(textField.id + '_msg').innerHTML='';");
                out.println("\t }");
                out.println(" }");
            }
        }
        out.println("}");
    }
    
    
    private void printJscriptOnChangeRow(Context ctx, PrintWriter out, String name, Object obj)
    {
        out.println("function isThisFieldNotUniqueAmongRows (field,targetPath, targetFieldName)");
        out.println("{");
        out.println(" if( (null == field.value) || (field.value.length < 1) )");
        out.println(" { return false; } ");
        out.println("    try");
        out.println("    {");
        out.println("        var inputFields = document.getElementsByTagName('input');"); 
        out.println("        var pattern = new RegExp(targetPath + '_+[0-9]+_+' + targetFieldName );");
        out.println("        j=0;"); 
        out.println("        for(i = 0; i < inputFields.length; i++)"); 
        out.println("                {"); 
        out.println("            att = inputFields[i].getAttribute('id');"); 
        out.println("            if(att!=null && att.match(pattern))"); 
        out.println("            {"); 
        out.println("                 if( (inputFields[i].value == field.value) && (field.id != inputFields[i].id) )");
        out.println("                 {");
        out.println("                    return true;");
        out.println("                 }");
        out.println("            }"); 
        out.println("        }");
        out.println("   }");
        out.println("   catch(everything) {    }");
        out.println("   return false;");
        out.println(" }");
    }
//    private final String PATTERN_KEY = AbstractTextFieldRegexCheckingWebControl.class.getName() + ".PATTERN";
//    private final String MESSAGE_ON_MISMATCH_KEY = AbstractTextFieldRegexCheckingWebControl.class.getName() + ".MESSAGE_ON_MISMATCH";
//    private final String MESSAGE_ON_NOT_UNIQUE_KEY = AbstractTextFieldRegexCheckingWebControl.class.getName() + ".MESSAGE_ON_NOT_UNIQUE";
    
}
