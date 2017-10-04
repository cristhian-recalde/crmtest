package com.trilogy.app.crm.migration;

import java.io.PrintStream;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.CreditCategory;
import com.trilogy.app.crm.bean.Identification;
import com.trilogy.app.crm.bean.SubProfileNotificationMsg;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.beans.XCloneable;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.msp.SpidAware;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;




public class SpidAwareMigrationVisitor
implements Visitor
{
	protected Throwable migrationFailure_ = null;
	PrintStream out; 
	//String[] sqlString; 
	Object bean;
	
	public SpidAwareMigrationVisitor(){
	}
		
	SpidAwareMigrationVisitor(Object cc, PrintStream out){
		this.bean = cc; 
 		this.out = out; 
	} 

	public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException
	{
		Home home  = (Home) ctx.get(XBeans.getClass(ctx, bean.getClass(), Home.class));
		CRMSpid sp = (CRMSpid)obj; 
		int old_id = ((SpidAware) bean).getSpid(); 
		
		
		if ( old_id == sp.getId()) return; 
		
		try {
			Object tempBean = ((XCloneable) bean).clone(); 		
			((SpidAware) tempBean).setSpid( sp.getId() );
			
			if ( bean instanceof CreditCategory){
				
				CreditCategory cc_temp = (CreditCategory) tempBean; 
				cc_temp.setCode(-1); 
				CreditCategory cc = (CreditCategory)bean; 
				CreditCategory cc_new = (CreditCategory) home.create( cc_temp );	
				
 					out.println( "update account set creditcategory =  " + cc_new.getCode() + " where creditcategory = " + cc.getCode() + " and spid = " + sp.getSpid() + ";" ); 

				
			}

			if ( bean instanceof Identification){
				
				Identification cc_temp = (Identification) tempBean; 
				Identification cc = (Identification) bean; 
				cc_temp.setCode( sp.getSpid() * 100 + cc_temp.getCode() ); 
				Identification cc_new = (Identification) home.create( tempBean );	
				
 					out.println(" update account set idtype1 = " + cc_new.getCode() + " where idtype1 = " + cc.getCode() + " and spid = " + sp.getSpid() + ";"); 
					out.println(" update account set idtype2 = " + cc_new.getCode() + " where idtype2 = " + cc.getCode() + " and spid = " + sp.getSpid() + ";"); 
					//out.println(" update blacklist set idtype = " + cc_new.getCode() + " where ittype = " + cc.getCode() + " and spid = " + sp.getSpid() ); 
				
			}
			
		} catch (Exception e){
			throw new AgentException( e.getMessage());  
		}
	}
	

	
 
}