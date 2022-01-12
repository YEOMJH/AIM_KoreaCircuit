package kr.co.aim.messolution.extended.object.management;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.exception.ErrorSignal;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;
import kr.co.aim.greenframe.util.bundle.BundleUtil;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
import kr.co.aim.greentrack.processflow.management.iter.NodeStackUtil;

public class ExtendedProcessFlowUtil {
	private static Log log = LogFactory.getLog(ExtendedProcessFlowUtil.class);

	private String[] InquireVariables = new String[4];

	public <T extends UdfAccessor> Node getNextNode(T dataInfo) throws CustomException
	{
		if (dataInfo == null)
		{
			//SYSTEM-0010:{0}: The incoming variable value can not be empty!!
			throw new CustomException("SYSTEM-0010",Thread.currentThread().getStackTrace()[1].getMethodName());
		}

		checkInquiredVariable(dataInfo);

		Node nextNode = null;
		boolean isCurrent = true;
		String[] nodeStackArray = StringUtil.split(getFieldValue(dataInfo, "getNodeStack"), ".");

		for (int idx = nodeStackArray.length; idx > 0; idx--)
		{
			if (isCurrent)
			{
				try
				{
					isCurrent = false;

					ProcessFlow processFlowData = getProcessFlowData(dataInfo);
					Object originalData = getOldData(dataInfo, false);

					kr.co.aim.greentrack.processflow.management.iter.NodeStack nodeStack = NodeStackUtil.stringToNodeStack(nodeStackArray[idx - 1]);
					ExtendedProcessFlowIterator pfi = new ExtendedProcessFlowIterator(processFlowData, nodeStack, null);
					ExtendedPFIValueSetter valueSetter = new ExtendedPFIValueSetter(pfi, (UdfAccessor) originalData, dataInfo, null, true);
					pfi.moveNext("N", valueSetter);
					nextNode = pfi.getCurrentNodeData();

					if (StringUtil.isEmpty(nextNode.getNodeAttribute1()) || StringUtil.isEmpty(nextNode.getProcessFlowName()))
						throw new Exception("LastNode");

					break;
				}
				catch (Exception ex)
				{
					if (ex instanceof CustomException)
						throw (CustomException) ex;
					if (ex.getMessage().equals("LastNode"))
						log.debug("It is last node");
					else
						throw new CustomException("SYS-0010", ex.getMessage());
				}
			}
			else
			{
				nextNode = ProcessFlowServiceProxy.getNodeService().getNode(nodeStackArray[idx - 1]);
				break;
			}
		}

		if (nextNode != null)
			return nextNode;
		else
			throw new CustomException("", "");
	}

	private ProcessFlow getProcessFlowData(Object dataInfo) throws CustomException
	{
		if (InquireVariables == null || InquireVariables.length == 0)
			checkInquiredVariable(dataInfo);

		String factoryName = getFieldValue(dataInfo, InquireVariables[0]);
		String processFlowName = getFieldValue(dataInfo, InquireVariables[2]);
		String processsFlowVersion = getFieldValue(dataInfo, InquireVariables[3]);

		ProcessFlow processFlowData = null;

		try
		{
			ProcessFlowKey processFlowKey = new ProcessFlowKey();
			processFlowKey.setFactoryName(factoryName);
			processFlowKey.setProcessFlowName(processFlowName);
			processFlowKey.setProcessFlowVersion(processsFlowVersion);

			processFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey);
		}
		catch (greenFrameDBErrorSignal dbEx)
		{
			if (dbEx.getErrorCode().equals(ErrorSignal.NotFoundSignal))
				throw new CustomException("COMM-1000", "ProcessFlow", String.format("FactoryName = %s,ProcessFlowName =%s ,Version = %s", factoryName, processFlowName, processsFlowVersion));
			else
				throw new CustomException(dbEx.getCause());
		}
		catch (Exception ex)
		{
			throw new CustomException(ex.getCause());
		}

		return processFlowData;

	}

	public Object getOldData(Object dataInfo, boolean isLock) throws greenFrameDBErrorSignal, CustomException
	{
		Object[] keyValue = CTORMUtil.makeKeyParam(dataInfo).toArray();

		try
		{
			Method method = CTORMService.class.getDeclaredMethod("selectByKey", Class.class, boolean.class, Object[].class);
			return method.invoke(CTORMService.class.newInstance(), new Object[] { dataInfo.getClass(), false, keyValue });

//			Object ctService = CTORMUtil.loadServiceProxy(dataInfo.getClass());
//			Method method = ctService.getClass().getDeclaredMethod("selectByKey", boolean.class, Object[].class);
//
//			method.setAccessible(true);
//			return method.invoke(ctService, new Object[] { false, keyValue });
		}
		catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
		{
			throw new CustomException("SYS-0010", e.getMessage());
		}
		catch (NoSuchMethodException | SecurityException e)
		{
			throw new CustomException("SYS-0010", e.getMessage());
		}
		catch (Exception ex)
		{
			if (ex instanceof greenFrameDBErrorSignal && ErrorSignal.NotFoundSignal.equals(((greenFrameDBErrorSignal) ex).getErrorCode()))
			{
				//CUSTOM-0010:{0} Data Information is not exists. Please confirm.
				throw new CustomException("CUSTOM-0010", dataInfo.getClass().getSimpleName());
			}
			else
			{
				throw new CustomException("SYS-0010", ex.getMessage());
			}
		}
	}

	private String getFieldValue(Object dataInfo, String fieldMethodName) throws CustomException
	{
		try
		{
			Method method = dataInfo.getClass().getDeclaredMethod(fieldMethodName, null);
			Object fieldValue = method.invoke(dataInfo, null);

			if (!(fieldValue instanceof String))
			{
				//SYSTEM-0013: The return type of method [{0}] is not string
				throw new CustomException("SYSTEM-0013", fieldMethodName);
			}

			return (String) fieldValue;
		}
		catch (NoSuchMethodException | SecurityException e)
		{
			throw new CustomException("SYS-0010", e.getMessage());
		}
		catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
		{
			throw new CustomException("SYS-0010", e.getMessage());
		}
	}

	private void checkInquiredVariable(Object dataInfo) throws CustomException
	{
		String[] methods = new String[4];

		if ("MaskLot".equals(dataInfo.getClass().getSimpleName()))
		{
			methods[0] = "getFactoryName";
			methods[1] = "getNodeStack";
			methods[2] = "getMaskProcessFlowName";
			methods[3] = "getMaskProcessFlowVersion";
		}
		else
		{
			methods[0] = "getFactoryName";
			methods[1] = "getNodeStack";
			methods[2] = "getProcessFlowName";
			methods[3] = "getProcessFlowVersion";
		}

		checkInquiredVariable(dataInfo, InquireVariables = methods);
	}

	private void checkInquiredVariable(Object dataInfo, String... variableGetMthods) throws CustomException
	{
		try
		{
			if (variableGetMthods != null && variableGetMthods.length > 0)
			{
				for (String variable : variableGetMthods)
				{
					dataInfo.getClass().getDeclaredMethod(variable, null);
				}
			}
			else
			{
				dataInfo.getClass().getDeclaredMethod("getFactoryName", null);
				dataInfo.getClass().getDeclaredMethod("getNodeStack", null);
				dataInfo.getClass().getDeclaredMethod("getProcessFlowName", null);
				dataInfo.getClass().getDeclaredMethod("getProcessFlowVersion", null);
			}
		}
		catch (NoSuchMethodException | SecurityException e)
		{
			throw new CustomException("SYS-0010", e.getMessage());
		}
	}

}
