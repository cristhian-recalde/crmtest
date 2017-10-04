package com.trilogy.app.crm.visitor;


import com.trilogy.app.crm.bean.AccountNote;
import com.trilogy.app.crm.bean.Note;
import com.trilogy.app.crm.bean.NoteOwnerTypeEnum;
import com.trilogy.app.crm.bean.SubscriberNote;
import com.trilogy.app.crm.configshare.ConfigSharingVisitor;
import com.trilogy.app.crm.home.NotesAuxiliaryFieldSetHome;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * An extension to ConfigSharingVisitor with special adapter for AccountNote and SubscriberNote.
 *
 * @author suyash.gaidhani@redknee.com
 * @since 9.7.1
 */
public class NoteConfigSharingVisitor extends ConfigSharingVisitor
{

	public NoteConfigSharingVisitor(NoteOwnerTypeEnum owner)
	{
		super();
		ownerType_ = owner;
	}

	public void visit(Context ctx, Object bean) throws AgentException, AbortVisitException
	{
		if (ownerType_ == NoteOwnerTypeEnum.ACCOUNT)
		{
			try
			{
				AccountNote accountNote = new AccountNote();
				com.redknee.framework.xhome.beans.XBeans.copy(ctx, bean, accountNote);

				accountNote.setBan(((Note)bean).getIdIdentifier());

				NotesAuxiliaryFieldSetHome.updateAccountFields(ctx, accountNote);
				super.visit(ctx,accountNote);
			}catch(HomeException he)
			{
				LogSupport.major(ctx, this, "HomeException occured while trying to ConfigShare AccountNote Object with ID :" + ((Note)bean).getId() , he);
			}
		}
		else if (ownerType_ == NoteOwnerTypeEnum.SUBSCRIPTION)
		{
			try
			{
				SubscriberNote subscriberNote = new SubscriberNote();
				com.redknee.framework.xhome.beans.XBeans.copy(ctx, bean, subscriberNote);
				subscriberNote.setSubscriberId(((Note)bean).getIdIdentifier());
				NotesAuxiliaryFieldSetHome.updateSubscriberFields(ctx, subscriberNote);
				super.visit(ctx, subscriberNote);
			}catch(HomeException he)
			{
				LogSupport.major(ctx, this, "HomeException occured while trying to ConfigShare SubscriberNote Object with ID :" + ((Note)bean).getId(), he);
			}
		}
	}
	private NoteOwnerTypeEnum ownerType_;


}