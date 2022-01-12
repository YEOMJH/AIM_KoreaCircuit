package kr.co.aim.greenframe.template.workflow;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import kr.co.aim.greenframe.fos.greenflow.BpelExRepository;
import kr.co.aim.greenframe.util.bundle.BundleUtil;
import kr.co.aim.greenframe.util.file.FileListenerEvent;
import kr.co.aim.greenframe.util.file.FileUtil;
import kr.co.aim.greenframe.util.file.FileWatcher;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.helpers.OptionConverter;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.w3c.dom.NamedNodeMap;

public class CustomBpelExRepository extends BpelExRepository {

	private static Log					log					= LogFactory.getLog(CustomBpelExRepository.class);
	/**
	 * @uml.property  name="useBpelsInInnerDirs"
	 */
	private boolean						useBpelsInInnerDirs	= false;
	/**
	 * @uml.property  name="bpelMapList"
	 * @uml.associationEnd  qualifier="path:java.lang.String java.lang.Long"
	 */
	private Map<String, Long>			bpelMapList			= new HashMap<String, Long>();
	/**
	 * @uml.property  name="bpelRootPath"
	 */
	private String						bpelRootPath		= "/META-INF/bpels";
	/**
	 * @uml.property  name="bpelRootPathList"
	 */
	private List						bpelRootPathList	= null;
	/**
	 * @uml.property  name="rootBpelName"
	 */
	private String						rootBpelName		= "dispatching.bpel";
	/**
	 * @uml.property  name="bundleSymbolicNameList"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
	 */
	private List						bundleSymbolicNameList;
	/**
	 * @uml.property  name="bundleContext"
	 * @uml.associationEnd  multiplicity="(0 -1)" ordering="true" elementType="java.lang.Object" qualifier="constant:java.lang.String java.lang.Object"
	 */
	private BundleContext				bundleContext;
	/**
	 * @uml.property  name="fileWatcherList"
	 * @uml.associationEnd  qualifier="watchDir:java.lang.String kr.co.aim.greenframe.util.file.FileWatcher"
	 */
	private Map<String, FileWatcher>	FileWatcherList		= new HashMap<String, FileWatcher>();

	/**
	 * @param arg0
	 * @uml.property  name="bundleContext"
	 */
	public void setBundleContext(BundleContext arg0)
	{
		// TODO Auto-generated method stub
		bundleContext = arg0;
	}

	public CustomBpelExRepository()
	{
	}

	/**
	 * @return
	 * @uml.property  name="bundleSymbolicNameList"
	 */
	public List getBundleSymbolicNameList()
	{
		return bundleSymbolicNameList;
	}

	/**
	 * @param bundleSymbolicNameList
	 * @uml.property  name="bundleSymbolicNameList"
	 */
	public void setBundleSymbolicNameList(List bundleSymbolicNameList)
	{
		this.bundleSymbolicNameList = bundleSymbolicNameList;
	}

	/**
	 * @return
	 * @uml.property  name="bpelRootPath"
	 */
	public String getBpelRootPath()
	{
		return bpelRootPath;
	}

	/**
	 * @param bpelRootPath
	 * @uml.property  name="bpelRootPath"
	 */
	public void setBpelRootPath(String bpelRootPath)
	{
		this.bpelRootPath = OptionConverter.substVars(bpelRootPath, null);
	}

	/**
	 * @return
	 * @uml.property  name="bpelRootPathList"
	 */
	public List getBpelRootPathList()
	{
		return bpelRootPathList;
	}

	/**
	 * @param bpelRootPathList
	 * @uml.property  name="bpelRootPathList"
	 */
	public void setBpelRootPathList(List bpelRootPathList)
	{
		String[] bpelDir = new String[bpelRootPathList.size()];
		for (int i = 0; i < bpelRootPathList.size(); i++)
		{
			bpelDir[i] = OptionConverter.substVars(bpelRootPathList.get(i).toString(), null);
		}
		bpelRootPathList.clear();

		for (int i = 0; i < bpelDir.length; i++)
		{
			bpelRootPathList.add(bpelDir[i]);
		}
		this.bpelRootPathList = bpelRootPathList;
	}

	public void setRepositoryName(String name)
	{
		super.setRepositoryName(System.getProperty("Process"));
	}

	/**
	 * @return
	 * @uml.property  name="rootBpelName"
	 */
	public String getRootBpelName()
	{
		return rootBpelName;
	}

	/**
	 * @param rootBpelName
	 * @uml.property  name="rootBpelName"
	 */
	public void setRootBpelName(String rootBpelName)
	{
		this.rootBpelName = rootBpelName;
	}

	public int getWFThreadSize()
	{
		return this.getBpelProcessManager().getWFThreadSize();
	}

	/**
	 * @return
	 * @uml.property  name="useBpelsInInnerDirs"
	 */
	public boolean isUseBpelsInInnerDirs()
	{
		return useBpelsInInnerDirs;
	}

	/**
	 * @param useBpelsInInnerDirs
	 * @uml.property  name="useBpelsInInnerDirs"
	 */
	public void setUseBpelsInInnerDirs(boolean useBpelsInInnerDirs)
	{
		this.useBpelsInInnerDirs = useBpelsInInnerDirs;
	}

	public void init() throws Exception
	{
		try
		{
			read();
		} catch (Throwable e)
		{
			log.error(e, e);
		}
	}

	private void readBpels(String aBpelRootPath, List bpelAllList)
	{

		for (int i = 0; i < this.bundleSymbolicNameList.size(); i++)
		{
			try
			{
				List bpelList = FileUtil.getFileList(new File(aBpelRootPath), ".BPEL", this.useBpelsInInnerDirs);
				log.info("Read BPEL(" + bpelList.size() + ") Files from [" + aBpelRootPath + "]");
				bpelAllList = ListUtils.sum(bpelList, bpelAllList);
				continue;
			} catch (Exception e)
			{}

			Bundle bundle = null;
			try
			{
				bundle = BundleUtil.getBundle(bundleContext.getBundles(), (String) bundleSymbolicNameList.get(i));
				File bpelFile = BundleUtil.getBundleResource(bundle, aBpelRootPath);
				if (bpelFile != null)
				{
					List bpelList = FileUtil.getFileList(bpelFile, ".BPEL", this.useBpelsInInnerDirs);
					log.info("Target Bundle-Name ["
						+ (String) bundleSymbolicNameList.get(i)
						+ "] for greenFrame workflow service");
					bpelAllList = ListUtils.sum(bpelList, bpelAllList);
				}
				else
				{
					if (bundle == null)
						bundle = bundleContext.getBundle();
					List<org.w3c.dom.Document> docList = BundleUtil.getBundleBpels(bundle, aBpelRootPath);
					if (docList.size() > 0)
					{
						for (int j = 0; j < docList.size(); j++)
						{
							this.addBpelProcess(docList.get(j));
						}
						return;
					}
					else
					{
						log.warn("Could not find application service files [" + aBpelRootPath + "]");
					}

				}
			} catch (Exception e)
			{
				if (bundle == null)
					bundle = bundleContext.getBundle();
				List<org.w3c.dom.Document> docList = BundleUtil.getBundleBpels(bundle, aBpelRootPath);
				if (docList.size() > 0)
				{
					for (int j = 0; j < docList.size(); j++)
					{
						this.addBpelProcess(docList.get(j));
					}
					return;
				}
				else
				{
					log.warn("Could not find application service files [" + aBpelRootPath + "]");
				}
			}
		}

		if (bpelAllList == null || bpelAllList.size() == 0)
		{
			log.warn("Could not find application service files [" + aBpelRootPath + "]");
		}

		try
		{
			for (int i = 0; i < bpelAllList.size(); i++)
			{
				String path = ((File) bpelAllList.get(i)).getAbsolutePath();
				this.addBpelProcess(path);
			}
		} catch (Exception e)
		{
			log.error(e, e);
		}
		bpelAllList.clear();

	}

	public void read() throws RuntimeException
	{
		if (this.bundleSymbolicNameList.size() == 0)
		{
			this.bundleSymbolicNameList.add(bundleContext.getBundle().getHeaders().get("Bundle-SymbolicName"));
		}

		int emptysize = 0;
		for (int i = 0; i < this.bundleSymbolicNameList.size(); i++)
		{
			if (((String) this.bundleSymbolicNameList.get(i)).length() == 0)
				emptysize++;
		}

		if (this.bundleSymbolicNameList.size() == emptysize)
		{
			this.bundleSymbolicNameList.clear();
			//this.bundleSymbolicNameList.add(FrameBundleActivator.getBundleContext().getBundle().getHeaders().get("Bundle-SymbolicName"));
			this.bundleSymbolicNameList.add(bundleContext.getBundle().getHeaders().get("Bundle-SymbolicName"));
		}

		List bpelAllList = new ArrayList();

		if (this.bpelRootPathList == null)
		{
			readBpels(this.bpelRootPath, bpelAllList);
			startWatcher(this.bpelRootPath);
		}
		else
		{
			for (int i = 0; i < bpelRootPathList.size(); i++)
			{
				readBpels(this.bpelRootPathList.get(i).toString(), bpelAllList);
				startWatcher(this.bpelRootPathList.get(i).toString());
			}
		}
	}

	private synchronized void removeBpelProcess(String bpelfile) throws Exception
	{
		File file = new File(bpelfile);
		String path = file.getAbsolutePath();
		bpelMapList.remove(path);
		QName qname = new QName("http://bpel.aim.co.kr/bpelj/", file.getName());
		this.removeBpelProcess(qname);
	}

	private synchronized void addBpelProcess(String bpelfile) throws Exception
	{
		File file = new File(bpelfile);
		String path = file.getAbsolutePath();

		if (bpelMapList.containsKey(path))
		{
			if (file.lastModified() > bpelMapList.get(path).longValue())
			{
				bpelMapList.remove(path);
			}
			else
				return;
		}

		org.w3c.dom.Document doc = xutil4j.xml.DomUtils.load(path, true);

		this.addBpelProcess(doc);
		bpelMapList.put(path, file.lastModified());
	}

	private QName getQName(org.w3c.dom.Document doc) throws Exception
	{
		NamedNodeMap map = doc.getFirstChild().getAttributes();
		QName qname =
				new QName(map.getNamedItem("targetNamespace").getNodeValue(), map.getNamedItem("name").getNodeValue());
		return qname;
	}

	public void reload() throws RuntimeException
	{
		removeAll();
		read();
	}

	public void removeAll()
	{
		this.getRegisteredBpelProcesses().clear();
		super.removeAll();
	}

	private void startWatcher(String watchDir)
	{
		try
		{
			FileWatcher fileWatcher = FileWatcherList.get(watchDir);
			if (fileWatcher != null)
			{
				fileWatcher.stopWatching();
				fileWatcher.removeFileListener(this);
				FileWatcherList.remove(watchDir);
			}

			fileWatcher = new FileWatcher(watchDir, ".BPEL", this.useBpelsInInnerDirs);
			fileWatcher.setInterval(10); // 10 seconds
			fileWatcher.addFileListener(this);
			FileWatcherList.put(watchDir, fileWatcher);
		} catch (Exception e)
		{
			log.warn("Could not create BpelWatcher");
		}
	}

	public void onFileChanged(FileListenerEvent e)
	{
		try
		{
			if (e.getEventType().equalsIgnoreCase(FileListenerEvent.FILE_EVENT_TYPE_ADD)
				|| e.getEventType().equalsIgnoreCase(FileListenerEvent.FILE_EVENT_TYPE_MODIFY))
				addBpelProcess(e.getFile());
			else if (e.getEventType().equalsIgnoreCase(FileListenerEvent.FILE_EVENT_TYPE_DELETE))
				removeBpelProcess(e.getFile());
		} catch (Exception ex)
		{
			log.error(ex);
		}
	}
	
	public void StopFileWatcherList(String serverName){
		String selfServerName = System.getProperty("Seq");
		log.info("***************************************************************************************");
		log.info("Receive ServerName = " + serverName);
		log.info("My ServerName = " + selfServerName);
		
		if(serverName.equals(selfServerName)){
			for(FileWatcher fileWatcher : FileWatcherList.values()){
				fileWatcher.stopWatching();
			}
			
			log.info("***************************************************************************************");
			log.info("********************              BPEL Read Stop             **************************");
			log.info("***************************************************************************************");
		}else{
			log.warn("Receive ServerName is not same my Name");
		}
	}
}
