package kr.co.aim.messolution.transportjob.event;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;

import org.jdom.Document;

public class MCSStateChanged extends AsyncHandler {

	/**
	 * MessageSpec [MCS -> TEX]
	 * 
	 * <Body>
	 *    <MCSNAME />
	 *    <MCSMODELNAME />
	 *    <MCSSTATE />
	 * </Body>
	 */
	
	@Override
	public void doWorks(Document doc) throws CustomException
	{
	}
}
