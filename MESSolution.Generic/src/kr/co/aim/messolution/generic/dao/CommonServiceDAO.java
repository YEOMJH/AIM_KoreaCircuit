/**
 * 
 */
package kr.co.aim.messolution.generic.dao;

import kr.co.aim.greenframe.orm.info.DataInfo;
import kr.co.aim.greenframe.orm.info.KeyInfo;
import kr.co.aim.greenframe.orm.service.CommonService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CommonServiceDAO<KEY extends KeyInfo, DATA extends DataInfo>
		extends kr.co.aim.greenframe.orm.OrmStandardEngine<KEY, DATA>  implements CommonService<KEY, DATA> {

	private static Log 								log; 
	
	public CommonServiceDAO() {
		log = LogFactory.getLog(this.getClass());
	}
}
