package kr.co.aim.messolution.machine.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineKey;

public class MachineServiceUtil implements ApplicationContextAware {

	private ApplicationContext applicationContext;
	private static Log log = LogFactory.getLog(MachineServiceImpl.class);

	public void setApplicationContext(ApplicationContext arg0) throws BeansException
	{
		// TODO Auto-generated method stub
		applicationContext = arg0;
	}

	public Machine getMachineData(String machineName) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		try
		{
			if (log.isInfoEnabled())
			{
				log.info("machineName = " + machineName);
			}
			MachineKey machineKey = new MachineKey();
			machineKey.setMachineName(machineName);
			Machine machineData = MachineServiceProxy.getMachineService().selectByKey(machineKey);
			return machineData;
		}
		catch (Exception e)
		{
			throw new CustomException("MACHINE-9000", machineName);
		}
	}
}