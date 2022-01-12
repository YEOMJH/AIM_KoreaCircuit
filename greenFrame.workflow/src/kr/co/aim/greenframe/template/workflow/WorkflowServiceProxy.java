package kr.co.aim.greenframe.template.workflow;

import kr.co.aim.greenframe.util.bundle.BundleUtil;

public class WorkflowServiceProxy {
	
	public static BpelExecuter getBpelExecuter()
	{
		return (BpelExecuter) BundleUtil.getBundleServiceClass(BpelExecuter.class);
	}

}
