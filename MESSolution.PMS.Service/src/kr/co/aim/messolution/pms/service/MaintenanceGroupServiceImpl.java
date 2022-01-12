package kr.co.aim.messolution.pms.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.pms.management.data.MaintenanceGroup;
import kr.co.aim.messolution.pms.management.data.MaintenanceGroupKey;
import kr.co.aim.greentrack.generic.common.CommonServiceDAO;

public class MaintenanceGroupServiceImpl extends CommonServiceDAO<MaintenanceGroupKey, MaintenanceGroup> {

	private Log logger = LogFactory.getLog(MaintenanceGroupServiceImpl.class);
}
