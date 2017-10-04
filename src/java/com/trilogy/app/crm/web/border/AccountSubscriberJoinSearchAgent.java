/*
 * Created on Oct 14, 2004
 *
 * Copyright (c) 1999-2003 REDKNEE.com. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * REDKNEE.com. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with REDKNEE.com.
 *
 * REDKNEE.COM MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE
 * SUITABILITY OF THE SOFTWARE, EITHCDR EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE IMPLIED WARRANTIES OF MCDRCHANTABILITY, FITNESS FOR
 * A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. REDKNEE.COM SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFCDRED BY LICENSEE AS A RESULT OF USING,
 * MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DCDRIVATIVES.
 */
package com.trilogy.app.crm.web.border;

import java.util.Vector;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgentProxy;
import com.trilogy.framework.xhome.web.search.SearchBorder;
import com.trilogy.framework.xhome.xdb.SimpleXStatement;
import com.trilogy.framework.xhome.xdb.XQL;
import com.trilogy.framework.xhome.xdb.XStatement;


/**
 * 
 * A search agent for those fields in both Account and Subscriber table 
 * like first name and last name
 * it also hardcode for ignore case criteria
 * @author jchen, candy
 *
 */
public abstract class  AccountSubscriberJoinSearchAgent extends ContextAgentProxy
{
    protected String fieldName_;
    protected boolean checkAccountTable_ = false;
    
   
   public AccountSubscriberJoinSearchAgent(String fieldName, boolean checkAccountTable)
   {
       fieldName_ = "UPPER(" + fieldName + ")"; 
       checkAccountTable_ = checkAccountTable;
       
   }
   
   public void execute(Context ctx)
      throws AgentException
   {
	  boolean isWildCardEnabled = false; 
      boolean isEscapeEnabled = false; 

	  Object criteria = SearchBorder.getCriteria(ctx);

      Vector returnVect = getCleanCriteriaValue(ctx, criteria);
      
      String value = (String)returnVect.get(0);
      Boolean wildCardObj = (Boolean)returnVect.get(1);      
      if(wildCardObj != null)
    	  isWildCardEnabled = wildCardObj.booleanValue();      
      Boolean escapeObj = (Boolean)returnVect.get(2);      
      if(escapeObj != null)
          isEscapeEnabled = escapeObj.booleanValue();      
      
      if ( ! "".equals(value) )
          SearchBorder.doSelect(ctx, getSqlJoinClause(ctx, checkAccountTable_, value, isWildCardEnabled,isEscapeEnabled)); 

      delegate(ctx);
   }
   
   /**
    * TODO refactor to use XStatement
    */
   public XStatement getSqlJoinClause(Context ctx, boolean bCheckAccountTable, String value, boolean isWildCardSearch, boolean isEscapeNeeded)
   {
       StringBuffer sqlClause = new StringBuffer();
        
       if (bCheckAccountTable)
       {
           sqlClause.append(" ( ");
           sqlClause.append(getFieldName());
           if (isWildCardSearch)
           {
               sqlClause.append(" like '");
           }
           else
           {
               sqlClause.append(" = '");
           }
           sqlClause.append(((XQL) ctx.get(XQL.class)).escapeSQL(value.toUpperCase()));
    	   sqlClause.append("'");
           if (isEscapeNeeded)
           {
               sqlClause.append(" escape '!'");
           }
    	   sqlClause.append(" OR");
       }
       
       sqlClause.append(" BAN in (select BAN from subscriber where ");
       sqlClause.append(getFieldName());
       if (isWildCardSearch)
       {
           sqlClause.append(" like '");
       }
       else
       {
           sqlClause.append(" = '");
       }
       sqlClause.append(((XQL) ctx.get(XQL.class)).escapeSQL(value.toUpperCase()));
       sqlClause.append("'");
       if (isEscapeNeeded)
       {
           sqlClause.append(" escape '!'");
       }
       sqlClause.append(")");
       

       if (bCheckAccountTable)
       {
           sqlClause.append(" )");
       }
     
       
       //System.out.println(sqlClause);
       return new SimpleXStatement(sqlClause.toString());
       
   }
   
   
   /**
    * Following code is copied from WildcardSelectSearchAgent
    * @param criteria
    * @returns a vector which always holds 2 values
    * 		   1.	A value to be passed to in the sql clause
    * 		   2.	A boolean which indicates whether wild card is enabled or not
    */
   public Vector getCleanCriteriaValue(Context ctx, Object criteria)
   {
	   boolean isWildCardEnabled = false;
       boolean isEscapeNeeded = false;
       Vector returnVect = new Vector(3);

       String value = getCriteriaUpperCase(ctx, criteria);
       value = value.trim();
       
       StringBuilder valueStrBuff = new StringBuilder(value);
	   
       int i = 0;

       // Remove 's
       while ( ( i = valueStrBuff.indexOf("'") ) != -1 ) valueStrBuff = valueStrBuff.deleteCharAt(i);

       //If value is less than equal to 2 then wildcard search is not allowed and exact search is performed
       if( valueStrBuff.length() <= 2)
       {
           int questionMarkIndex = valueStrBuff.indexOf("?");
           while (questionMarkIndex != -1)
           {
               valueStrBuff = valueStrBuff.deleteCharAt(questionMarkIndex);
               questionMarkIndex = valueStrBuff.indexOf("?");
           }

           int starIndex = valueStrBuff.indexOf("*");
           if (starIndex == 0)
    	   {
    		   valueStrBuff = valueStrBuff.deleteCharAt(starIndex);
    	   }

           int lastStarIndex = valueStrBuff.lastIndexOf("*");
           if (lastStarIndex!=-1 && lastStarIndex == valueStrBuff.length()-1)
           {
               valueStrBuff = valueStrBuff.deleteCharAt(lastStarIndex);
           }
       }
       else
       {
           int questionMarkIndex = valueStrBuff.indexOf("?");
           int starIndex = valueStrBuff.indexOf("*");
           
           if (questionMarkIndex != -1 || starIndex ==0 || (starIndex!=-1 && starIndex == valueStrBuff.length()-1))
           {
               isWildCardEnabled = true;                   

               // Escaping %, ! and _ in the query if escaping is needed.
               if (valueStrBuff.indexOf("%") != -1 || valueStrBuff.indexOf("_") != -1)
               {
                   isEscapeNeeded = true;

                   i = 0;
                   while (( i = valueStrBuff.indexOf("!", i) ) != -1 ) 
                   {
                       valueStrBuff.insert(i, "!");
                       i = i+2;
                   }
    
                   i = 0;
                   while ( ( i = valueStrBuff.indexOf("%", i) ) != -1 ) 
                   {
                       valueStrBuff.insert(i, "!");
                       i = i+2;
                   }
                   
                   i = 0;
                   while ( ( i = valueStrBuff.indexOf("_", i) ) != -1 ) 
                   {
                       valueStrBuff.insert(i, "!");
                       i = i+2;
                   }
               }
           }
           
           questionMarkIndex = valueStrBuff.indexOf("?");
           while (questionMarkIndex != -1)
           {
               valueStrBuff = valueStrBuff.deleteCharAt(questionMarkIndex);
               valueStrBuff.insert(questionMarkIndex, "_");
               questionMarkIndex = valueStrBuff.indexOf("?");
           }

           starIndex = valueStrBuff.indexOf("*");
           if (starIndex == 0)
           {
               valueStrBuff = valueStrBuff.deleteCharAt(starIndex);
               valueStrBuff.insert(0, "%");
           }

           int lastStarIndex = valueStrBuff.lastIndexOf("*");
           if (lastStarIndex!=-1 && lastStarIndex == valueStrBuff.length()-1)
           {
               valueStrBuff = valueStrBuff.deleteCharAt(lastStarIndex);
               valueStrBuff.append("%");
           }

       }
       
       returnVect.add(0, valueStrBuff.toString());
       returnVect.add(1, Boolean.valueOf(isWildCardEnabled));
       returnVect.add(2, Boolean.valueOf(isEscapeNeeded));
       
       return returnVect;

   }

   public abstract String getCriteria(Context ctx, Object criteria);
   
// TODO, figure, do we need this function
   public abstract String getField(Object bean);
   
   
   public String getCriteriaUpperCase(Context ctx, Object criteria)
   {
      return getCriteria(ctx, criteria).toUpperCase();
   }
   
   
   public String getFieldUpperCase(Object bean)
   {
      return getField(bean).toUpperCase();
   }
   
   public String getFieldName()
   {
       return fieldName_;
   }
   
}