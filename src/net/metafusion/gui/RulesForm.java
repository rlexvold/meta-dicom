package net.metafusion.gui;

import net.metafusion.admin.ForwardRuleBean;
import net.metafusion.admin.PollRuleBean;
import net.metafusion.admin.ServerBean;
import net.metafusion.admin.StorageRuleBean;
import acme.util.NestedException;
import acme.util.StringUtil;

public class RulesForm extends MFForm
{
	// private ForwardRuleBean frb[] = new ForwardRuleBean[0];
	private PollRuleBean prb[] = new PollRuleBean[0];
	private ServerBean serverBean = new ServerBean();
	private StorageRuleBean srb[] = new StorageRuleBean[0];

	public static void main(String[] args)
	{
		SwingUtil.setLookAndFeel();
		Form f = new RulesForm();
		f.testForm();
	}

	// width='50' align='right' grow='true'
	public RulesForm()
	{
		appendSeparator("Storage Rules");
		appendLine(new String[] { "[check id='storeCheck0' text='Delete studies older than' gap='5']", "[text id='storeArg0' len='4' gap='5']", "[label text='days.']", });
		appendLine(new String[] { "[check id='storeCheck1' text='Disk Usage:   High Water' gap='3']", "[text id='storeArg1' len='4' gap='2']",
				"[label text='% used.   Low Water' gap='3']", "[text id='storeArg2' len='4' gap='2']", "[label text='% used.']", });
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 24; i++)
		{
			sb.append(StringUtil.int2(i) + ":00;");
			sb.append(StringUtil.int2(i) + ":30;");
		}
		sb.append("24:00");
		appendSeparator("Polling Rules");
		for (int i = 0; i < PollRuleBean.MAX_POLL; i++)
		{
			appendLine(new String[] { "[check id='poll" + i + "' text='Poll for studies from AE' gap='5']", "[text id='pollae" + i + "' len='15' gap='5']",
					"[label text='every' gap='5']", "[text id='pollmin" + i + "' len='4' gap='5']", "[label text='minutes.  Poll during: ' gap='5']",
					"[combo text='" + sb.toString() + "' value='24:00' id='pollstart" + i + "' width='60' gap='5']",
					"[combo text='" + "24:00;" + sb.toString() + "' value='24:00' id='pollstop" + i + "' width='60' gap='5']", });
		}
		// appendSeparator("Forwarding Rules");
		//
		//
		// for (int i=0;i<ForwardRuleBean.MAX_RULE;i++) {
		// appendLine(new String[] {
		// "[check id='forward"+i+"' text='Forward studies' gap='5']",
		// "[combo text='Of Modality;To Radiologist;From Source;From Physician'
		// id='type"+i+"' width='100' gap='5']",
		// "[text id='arg"+i+"' len='15' gap='5']",
		// "[label text='to AE' gap='5']",
		// "[text id='toArg"+i+"' len='15' gap='5']",
		// "[combo text='"+sb.toString()+"' value='24:00' id='start"+i+"'
		// width='60' gap='5']",
		// "[combo text='"+"24:00;"+sb.toString()+"' value='24:00'
		// id='stop"+i+"' width='60' gap='5']",
		// });
		// }
		buildButtonBar(new String[] { "[GLUE]", "Save", "Cancel" });
	}

	public void onCancel()
	{
		reset(serverBean);
	}

	public void onSave()
	{
		try
		{
			srb = new StorageRuleBean[2];
			for (int i = 0; i < 2; i++)
				srb[i] = getStorageRule(i);
			serverBean.setStorageRule(srb);
			// frb = ForwardRuleBean.getEmptyArray();
			// for (int i=0; i<ForwardRuleBean.MAX_RULE; i++)
			// frb[i] = getForwardRule(i);
			// serverBean.setForwardRule(frb);
			prb = PollRuleBean.getEmptyArray();
			for (int i = 0; i < PollRuleBean.MAX_POLL; i++)
				prb[i] = getPollRule(i);
			serverBean.setPollRule(prb);
			serverBean.save();
			admin.commit();
		}
		catch (Exception e)
		{
			reset(serverBean);
			throw new NestedException(e);
		}
	}

	public void reset(ServerBean serverBean)
	{
		this.serverBean = serverBean;
		srb = serverBean.getStorageRule();
		// frb = serverBean.getForwardRule();
		prb = serverBean.getPollRule();
		for (int i = 0; i < 2; i++)
			setStorageRule(i < srb.length ? srb[i] : null);
		// for (int i=0; i<ForwardRuleBean.MAX_RULE; i++)
		// setForwardRule(i, i<frb.length ? frb[i] : null);
		for (int i = 0; i < PollRuleBean.MAX_POLL; i++)
			setPollRule(i, i < prb.length ? prb[i] : null);
	}

	ForwardRuleBean getForwardRule(int i)
	{
		ForwardRuleBean b = new ForwardRuleBean(getSelection("type" + i), isSelected("forward" + i), getText("arg" + i), getText("toArg" + i), getText("start" + i), getText("stop"
				+ i));
		return b;
	}

	PollRuleBean getPollRule(int i)
	{
		PollRuleBean b = new PollRuleBean(isSelected("poll" + i), getText("pollmin" + i), getText("pollae" + i), getText("pollstart" + i), getText("pollstop" + i));
		return b;
	}

	StorageRuleBean getStorageRule(int i)
	{
		StorageRuleBean b;
		if (i == 0)
			b = new StorageRuleBean(i, isSelected("storeCheck0"), getText("storeArg0"), "");
		else b = new StorageRuleBean(i, isSelected("storeCheck1"), getText("storeArg1"), getText("storeArg2"));
		return b;
	}

	void setForwardRule(int i, ForwardRuleBean b)
	{
		setSelected("forward" + i, b != null ? b.isEnabled() : false);
		setSelection("type" + i, b != null ? b.getType() : 0);
		setText("arg" + i, b != null ? b.getArg() : "");
		setText("toArg" + i, b != null ? b.getDestAE() : "");
		setSelection("start" + i, b != null ? b.getStart() : "");
		setSelection("stop" + i, b != null ? b.getEnd() : "");
	}

	void setPollRule(int i, PollRuleBean b)
	{
		setSelected("poll" + i, b != null ? b.isEnabled() : false);
		setText("pollmin" + i, b != null ? b.getMin() : "");
		setText("pollae" + i, b != null ? b.getDestAE() : "");
		setSelection("pollstart" + i, b != null ? b.getStart() : "");
		setSelection("pollstop" + i, b != null ? b.getEnd() : "");
	}

	void setStorageRule(StorageRuleBean b)
	{
		if (b.getType() == StorageRuleBean.AGE_TYPE)
		{
			setSelected("storeCheck0", b != null ? b.isEnabled() : false);
			setText("storeArg0", b != null ? b.getArg1() : "");
		} else if (b.getType() == StorageRuleBean.FREESPACE_TYPE)
		{
			setSelected("storeCheck1", b != null ? b.isEnabled() : false);
			setText("storeArg1", b != null ? b.getArg1() : "");
			setText("storeArg2", b != null ? b.getArg2() : "");
		}
	}
}