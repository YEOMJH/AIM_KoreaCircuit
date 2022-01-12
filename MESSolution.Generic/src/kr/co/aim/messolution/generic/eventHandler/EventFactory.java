package kr.co.aim.messolution.generic.eventHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

public interface EventFactory {
	
	public Log eventLog = LogFactory.getLog(EventFactory.class);
	
	public void execute(Document doc) throws Exception;
	
	public void handleSync(Object doc, String sendSubjectName);
	
	public void handleFault(Document doc, String sendSubjectName, Exception ex);
	
	//thread-safe handler
	public void init();
}
