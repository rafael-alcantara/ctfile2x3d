/*
 * Copyright (C) 2014 rafa
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.sf.ctfile2x3d.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.JMException;
import javax.management.ObjectName;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import net.sf.ctfile2x3d.CTFile2X3DConfig;
import net.sf.ctfile2x3d.CTFileParser;
import net.sf.ctfile2x3d.MolParser;
import net.sf.ctfile2x3d.RxnParser;
import org.web3d.x3d.X3D;

/**
 * Servlet to provide MOL and RXN files in X3D format. The dimensions (2d/3D) of
 * the returned X3D only depend on the source CTFile.
 * @author rafa
 */
public class CTFile2X3DServlet extends HttpServlet {

    private JAXBContext jc;
    private CTFile2X3DConfig conf;
    private MolParser molParser;
    private RxnParser rxnParser;
    
    /**
     * The CTFile formats supported by this servlet.
     */
    private static enum Format { MOL, RXN }
    
    @Override
    public void init() throws ServletException {
        super.init();
        conf = new CTFile2X3DConfig();
        conf.setMolUrlPattern(
                getInitParameter(CTFile2X3DConfig.MOL_URL_PATTERN));
        conf.setRxnUrlPattern(
                getInitParameter(CTFile2X3DConfig.RXN_URL_PATTERN));
        try {
            jc = JAXBContext.newInstance("org.web3d.x3d");
        } catch (JAXBException ex) {
            Logger.getLogger(CTFile2X3DServlet.class.getName())
                    .log(Level.SEVERE, "Unable to get JAXB context", ex);
        }
        try { // Register MBean in Platform MBeanServer
            ManagementFactory.getPlatformMBeanServer().registerMBean(conf,
                    new ObjectName("net.sf.ctfile2x3d:type=CTFile2X3DConfig"));
        } catch (JMException ex) {
            Logger.getLogger(CTFile2X3DServlet.class.getName()).log(
                    Level.SEVERE,
                    "Unable to register MBean CTFile2X3D configuration",
                    ex);
        }
        molParser = new MolParser(conf);
        rxnParser = new RxnParser(conf);
    }

    /**
     * This servlet takes two request parameters:
     * <ul>
     *  <li><code>id</code>: the identifier of the CTFile.</li>
     *  <li><code>format</code>: the {@link Format format} of the
     *      CTFile.</li>
     * </ul>
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException 
     */
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) 
    throws ServletException, IOException {
        String id = req.getParameter("id");
        String format = req.getParameter("format");
        CTFileParser parser = null;
        URL url = null;
        try {
            switch (Format.valueOf(format.toUpperCase())){
                case MOL:
                    parser = molParser;
                    url = new URL(
                            MessageFormat.format(conf.getMolUrlPattern(), id));
                    resp.setContentType("chemical-/x-mdl-molfile");
                    break;
                case RXN:
                    parser = rxnParser;
                    url = new URL(
                            MessageFormat.format(conf.getRxnUrlPattern(), id));
                    resp.setContentType("chemical-/x-mdl-rxnfile");
                    break;
            }
            try (InputStream is = url.openStream()) {
                X3D x3d = parser.parse(is);
                Marshaller m = jc.createMarshaller();
                m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                m.marshal(x3d, resp.getOutputStream());
                resp.flushBuffer();
            }
        } catch (JAXBException ex) {
            Logger.getLogger(CTFile2X3DServlet.class.getName())
                    .log(Level.SEVERE, "Unable to marshall X3D: " + id, ex);
            throw new ServletException(ex);
        }
    }
    
}
