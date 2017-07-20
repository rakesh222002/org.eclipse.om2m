package org.eclipse.om2m.sdt.home.monitoring;

import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.om2m.core.service.CseService;
import org.eclipse.om2m.sdt.home.monitoring.servlet.CredentialsServlet;
import org.eclipse.om2m.sdt.home.monitoring.servlet.HomeServlet;
import org.eclipse.om2m.sdt.home.monitoring.servlet.InCseContextServlet;
import org.eclipse.om2m.sdt.home.monitoring.servlet.LoginServlet;
import org.eclipse.om2m.sdt.home.monitoring.servlet.LogoutServlet;
import org.eclipse.om2m.sdt.home.monitoring.servlet.MonitorHomeServlet;
import org.eclipse.om2m.sdt.home.monitoring.util.AeRegistration;
import org.eclipse.om2m.sdt.home.monitoring.util.Constants;
import org.eclipse.om2m.sdt.home.monitoring.util.ResourceDiscovery;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;


public class Activator implements BundleActivator , ManagedService {
	/** logger */
	private static Log LOGGER = LogFactory.getLog(Activator.class);
	public static String globalContext = System.getProperty("org.eclipse.om2m.globalContext","");
	public static String uiContext = /*System.getProperty("org.eclipse.om2m.webInterfaceContext","/")*/"";
	public static String sep ="/";
	public static String CAMERAURL ="";
	/** HTTP service tracker */
	private ServiceTracker httpServiceTracker;
	private ServiceTracker sclServiceTracker;

	@Override
	public void start(BundleContext context) throws Exception {
		initCseServiceTracker(context) ;

		if (uiContext.equals("/")) {
			sep="";
		}

		httpServiceTracker = new ServiceTracker(context, HttpService.class.getName(), null) {
			
			public void removedService(ServiceReference reference, Object service) {
				LOGGER.info("HttpService removed");
				try {
					LOGGER.info("Unregister " + uiContext + sep + " http context");
					((HttpService) service).unregister(uiContext + sep + Constants.APPNAME);
				} catch (IllegalArgumentException e) {
					LOGGER.error("Error unregistring webapp servlet",e);
				}
			}

			public Object addingService(ServiceReference reference) {
				LOGGER.info("HttpService discovered");
				HttpService httpService = (HttpService) context.getService(reference);
				try {
					LOGGER.info("Register test " + uiContext + sep + "Home_Monitoring_Application http context");
					httpService.registerServlet(uiContext + sep + Constants.APPNAME, 
							new HomeServlet(context), null, null);
					httpService.registerServlet(uiContext + sep + Constants.APPNAME + "/monitor/home", 
							new MonitorHomeServlet(context), null, null);
//					httpService.registerServlet(uiContext + sep + Constants.APPNAME + "/monitor/control/device", 
//							new DeviceControlServlet(context), null, null);
//					httpService.registerServlet(uiContext + sep + Constants.APPNAME + "/monitor/devices", 
//							new DevicesServlet(context), null, null);
					httpService.registerServlet(uiContext + sep + Constants.APPNAME + "/in-cse/context", 
							new InCseContextServlet(), null, null);
					httpService.registerServlet(uiContext + sep + Constants.APPNAME + "/security/login", 
							new LoginServlet(context), null, null);
					httpService.registerServlet(uiContext + sep + Constants.APPNAME + "/security/cred", 
							new CredentialsServlet(context), null, null);
					httpService.registerServlet(uiContext + sep + Constants.APPNAME + "/security/logout", 
							new LogoutServlet(context), null, null);
					httpService.registerResources(uiContext + sep + Constants.APPNAME + "/webapps", 
							uiContext + sep + "webapps", null);
				} catch (Exception e) {
					LOGGER.error("Error registring webapp servlet",e);
				}
				return httpService;
			}
		};
		httpServiceTracker.open();
		Dictionary<String,String> dic= new Hashtable();
		dic.put("service.pid", "home.monitoring.application");
		context.registerService(ManagedService.class.getName(), new Activator(), dic);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		sclServiceTracker.close();
		httpServiceTracker.close();
	}

	private void initCseServiceTracker(BundleContext bundleContext) {
		sclServiceTracker = new ServiceTracker(bundleContext, CseService.class.getName(), null) {
			public void removedService(ServiceReference reference, Object service) {
            	AeRegistration.getInstance().deleteAe();
				AeRegistration.getInstance().setCseService(null);
				LOGGER.info("CSEService removed");
			}
			public Object addingService(ServiceReference reference) {
				LOGGER.info("CSEService Tracker found");
				CseService cseService = (CseService) this.context.getService(reference); 
				AeRegistration.getInstance().setCseService(cseService);
            	AeRegistration.getInstance().createAe();
				ResourceDiscovery.initCseService(cseService);
				return cseService;
			}
		};
		sclServiceTracker.open();
	}

	@Override
	public void updated(Dictionary dictionary) throws ConfigurationException {
		if (dictionary != null) {
			CAMERAURL=dictionary.get("ip.camera.url").toString();
		}
	}

}
