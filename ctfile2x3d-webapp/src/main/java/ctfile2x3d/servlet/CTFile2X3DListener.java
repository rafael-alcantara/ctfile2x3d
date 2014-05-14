/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ctfile2x3d.servlet;

import ctfile2x3d.CTFile2X3DConfig;
import java.lang.management.ManagementFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Web application life cycle listener.
 *
 * @author rafa
 */
public class CTFile2X3DListener implements ServletContextListener {
    
    static String getConfigMBeanName() {
        return CTFile2X3DConfig.class.getPackage().getName()
            + ":type=" + CTFile2X3DConfig.class.getSimpleName();
    }

    /**
     * Creates the configuration for the context, reading any init parameters,
     * and then registers it as a context attribute <code>conf</code> and as
     * an MBean.
     * @param sce 
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        CTFile2X3DConfig conf = new CTFile2X3DConfig();
        conf.setMolUrlPattern(sce.getServletContext()
                .getInitParameter(CTFile2X3DConfig.MOL_URL_PATTERN));
        conf.setRxnUrlPattern(sce.getServletContext()
                .getInitParameter(CTFile2X3DConfig.RXN_URL_PATTERN));
        try { // Register MBean in Platform MBeanServer
            ManagementFactory.getPlatformMBeanServer()
                    .registerMBean(conf, new ObjectName(getConfigMBeanName()));
            sce.getServletContext().setAttribute(getConfigMBeanName(), conf);
        } catch (JMException ex) {
            Logger.getLogger(CTFile2X3DListener.class.getName()).log(
                    Level.SEVERE,
                    "Unable to register MBean CTFile2X3D configuration",
                    ex);
        }
    }

    /**
     * Unregisters the MBean for the configuration.
     * @param sce 
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            ManagementFactory.getPlatformMBeanServer()
                    .unregisterMBean(new ObjectName(getConfigMBeanName()));
        } catch (InstanceNotFoundException | MBeanRegistrationException ex) {
            Logger.getLogger(CTFile2X3DServlet.class.getName()).log(
                    Level.SEVERE,
                    "Unable to unregister MBean CTFile2X3D configuration",
                    ex);
        } catch (MalformedObjectNameException ex) {
            Logger.getLogger(CTFile2X3DListener.class.getName()).log(
                    Level.SEVERE,
                    "Wrong MBean name: " + getConfigMBeanName(),
                    ex);
        }
    }

}
