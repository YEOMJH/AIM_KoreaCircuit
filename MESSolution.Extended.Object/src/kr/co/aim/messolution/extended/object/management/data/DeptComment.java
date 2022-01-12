package kr.co.aim.messolution.extended.object.management.data;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class DeptComment extends UdfAccessor {
	@CTORMTemplate(seq = "1", name = "factoryName", type = "Key", dataType = "String", initial = "", history = "")
	private String factoryName;

	@CTORMTemplate(seq = "2", name = "department", type = "Key", dataType = "String", initial = "", history = "")
	private String department;

	@CTORMTemplate(seq = "3", name = "seq", type = "key", dataType = "Number", initial = "", history = "")
	private long seq;

	@CTORMTemplate(seq = "4", name = "deptComment", type = "Column", dataType = "String", initial = "", history = "")
	private String deptComment;

	public String getFactoryName()
	{
		return factoryName;
	}

	public void setFactoryName(String factoryName)
	{
		this.factoryName = factoryName;
	}

	public String getDepartment()
	{
		return department;
	}

	public void setDepartment(String department)
	{
		this.department = department;
	}

	public long getSeq()
	{
		return seq;
	}

	public void setSeq(long seq)
	{
		this.seq = seq;
	}

	public String getDeptComment()
	{
		return deptComment;
	}

	public void setDeptComment(String deptComment)
	{
		this.deptComment = deptComment;
	}
}
