package com.trilogy.app.crm.web.border;

import com.trilogy.framework.xhome.auth.*;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.framework.xhome.language.*;
import com.trilogy.framework.xhome.webcontrol.ColourSettings;
import java.io.*;
import java.security.Principal;
import javax.servlet.ServletException;
import javax.servlet.http.*;


public class AccountJSBorder
   implements Border
{

   public final static String ACCOUNT_JSCRIPT             = "AccountBorder.JScriptContent";

   public AccountJSBorder()
   {
       super();
   }


   public void service(Context ctx, HttpServletRequest req, HttpServletResponse res, RequestServicer delegate)
     throws ServletException, IOException
   {
      
      PrintWriter out      = res.getWriter();

      outputJScript(out, ctx, req);

      delegate.service(ctx, req, res);
   }


   public void outputJScript(PrintWriter out, Context ctx, HttpServletRequest req )
   {
      MessageMgr            mMgr  = new MessageMgr(ctx, this);
      
      //Principal principal         = (Principal) ctx.get(Principal.class);
      //Lang      lang              = (Lang)ctx.get(Lang.class);
      //String    display_langage   = ( lang == null ) ? Lang.DEFAULT.getCode() :  lang.getCode();

      //ColourSettings colours = ColourSettings.getSettings(ctx);

      out.println(mMgr.get("Account.JScript", (String)ctx.get(ACCOUNT_JSCRIPT, 
             "      <script language=\"JavaScript1.2\" type=\"text/javascript\">\n" +
            "<!--\n" +
      "function changeAcctFolderImg(linkId)\n" +
      "{\n" +
            "var pos = -1;\n"+
            "pos = linkId.indexOf(\"-\");\n"+
            "var imgObj = document.getElementById(\"acctimg-\" + linkId.substr(pos + 1 ));\n"+
            "//alert(imgObj.src);\n"+
            "var parses = imgObj.src.split(\"/\");\n"+
            "var pos = parses.length;\n"+
            "if ( parses[pos-1] == \"closeFolder.gif\")\n"+
            "{\n"+
            "   parses[pos-1] = \"openFolder.gif\";\n"+
            "}\n"+
            "else\n"+
            "{\n"+
            "   parses[pos-1] = \"closeFolder.gif\";\n"+
            "}\n"+
            "\n"+
            "var srcStr = \"\";\n"+
            "for ( var i = 0; i < parses.length; i++)\n"+
            "{\n"+
            "   srcStr += parses[i];\n"+
            "   if ( i < parses.length-1 )\n"+
            "   {\n"+
            "       srcStr += \"/\";\n"+
            "   }\n"+
            "}\n"+
            "\n"+
            "   imgObj.src = srcStr;\n"+
       "}\n" +
    "\n" +
    "function changeTRColor(id)\n" +
    "{\n"+
    "    var mybody=document.getElementsByTagName(\"body\").item(0);\n"+
    "\n" +
    "    var mytables = mybody.getElementsByTagName(\"table\");\n"+
    "\n" +
    "    for ( var i = 0; i < mytables.length; i++)\n"+
    "    {\n"+
    "        var mytable = mytables.item(i);\n" +
    "        // suppose only one tboday tag\n"+
    "        var mytablebody=mytable.getElementsByTagName(\"tbody\").item(0);\n"+  
    "\n" +
    "        if ( mytablebody != null )\n"+
    "        {\n"+
    "          var myrows=mytablebody.getElementsByTagName(\"tr\");\n" +
    "\n" +
    "          for ( var j = 0; j < myrows.length; j++)\n"+
    "          {\n" +
    "              var myrow = myrows.item(j);\n"+
    "\n" +
    "              if ( myrow.style.backgroundColor == \"Thistle\")\n"+
    "              {\n"+
    "                  myrow.style.backgroundColor = \"\";\n"+
    "              } // if\n" +
    "          } // for\n" +
    "       }// if\n"+
    "   }  // for\n"+
    "\n" +
    "   id.style.backgroundColor=\"Thistle\";\n"+
    "\n" +
    "}\n"+
    "\n" +
    "\n" +
    "//-->\n" +
    "</script>\n")));
   }
}
