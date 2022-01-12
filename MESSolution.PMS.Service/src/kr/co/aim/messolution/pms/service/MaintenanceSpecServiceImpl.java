package kr.co.aim.messolution.pms.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.pms.management.data.MaintenanceSpec;
import kr.co.aim.messolution.pms.management.data.MaintenanceSpecKey;
import kr.co.aim.greentrack.generic.common.CommonServiceDAO;

public class MaintenanceSpecServiceImpl extends CommonServiceDAO<MaintenanceSpecKey, MaintenanceSpec> {

	private Log logger = LogFactory.getLog(MaintenanceSpecServiceImpl.class);
}
