package kr.co.aim.messolution.pms.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.pms.management.data.MaintenanceSpecItem;
import kr.co.aim.messolution.pms.management.data.MaintenanceSpecItemKey;
import kr.co.aim.greentrack.generic.common.CommonServiceDAO;

public class MaintenanceSpecItemServiceImpl extends CommonServiceDAO<MaintenanceSpecItemKey, MaintenanceSpecItem> {

	private Log logger = LogFactory.getLog(MaintenanceSpecItemServiceImpl.class);
}
