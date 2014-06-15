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

package ctfile2x3d.servlet;

import ctfile2x3d.CTFile2X3DConfig;
import ctfile2x3d.CTFileParser;
import ctfile2x3d.Display;
import ctfile2x3d.MolParser;
import ctfile2x3d.RxnParser;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.web3d.x3d.X3D;

/**
 * Servlet to provide MOL and RXN files in X3D format. The dimensions (2d/3D) of
 * the returned X3D only depend on the source CTFile.
 * @author rafa
 */
public class CTFile2X3DServlet extends HttpServlet {

    private JAXBContext jc;
    private MolParser molParser;
    private RxnParser rxnParser;
    
    /**
     * The CTFile formats supported by this servlet.
     */
    private static enum Format { MOL, RXN }
    
    @Override
    public void init() throws ServletException {
        super.init();
        try {
            jc = JAXBContext.newInstance("org.web3d.x3d");
        } catch (JAXBException ex) {
            Logger.getLogger(CTFile2X3DServlet.class.getName())
                    .log(Level.SEVERE, "Unable to get JAXB context", ex);
        }
    }

    private CTFile2X3DConfig getConf() {
        return (CTFile2X3DConfig) getServletContext()
                .getAttribute(CTFile2X3DListener.getConfigMBeanName());
    }
    
    private MolParser getMolParser(){
        if (molParser == null){
            molParser = new MolParser(getConf());
        }
        return molParser;
    }
    
    private RxnParser getRxnParser(){
        if (rxnParser == null){
            rxnParser = new RxnParser(getConf());
        }
        return rxnParser;
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
        Display display = Display.MIXED;
        try {
            display = Display.valueOf(req.getParameter("display"));
        } catch (Exception e){}
        CTFileParser parser = null;
        resp.setContentType("model/x3d+xml");
        URL url = null;
        try {
            switch (Format.valueOf(format.toUpperCase())){
                case MOL:
                    parser = getMolParser();
                    url = new URL(MessageFormat.format(
                            getConf().getMolUrlPattern(), id));
                    break;
                case RXN:
                    parser = getRxnParser();
                    url = new URL(MessageFormat.format(
                            getConf().getRxnUrlPattern(), id));
                    break;
            }
            req.setAttribute("ctfileURL", url);
            try (InputStream is = url.openStream()) {
                X3D x3d = parser.parse(is, display);
                Marshaller m = jc.createMarshaller();
                m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);
                m.marshal(x3d, resp.getWriter());
                resp.flushBuffer();
            }
        } catch (IOException ex) {
            Logger.getLogger(CTFile2X3DServlet.class.getName())
                    .log(Level.SEVERE, "Unable to marshall X3D: " + id, ex);
            req.setAttribute("error", ex.getMessage());
            throw ex;
        } catch (JAXBException ex) {
            Logger.getLogger(CTFile2X3DServlet.class.getName())
                    .log(Level.SEVERE, "Unable to marshall X3D: " + id, ex);
            req.setAttribute("error", ex.getMessage());
            throw new ServletException(ex);
        }
    }
    
}
