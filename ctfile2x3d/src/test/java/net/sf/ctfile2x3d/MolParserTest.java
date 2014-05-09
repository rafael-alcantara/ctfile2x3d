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

package net.sf.ctfile2x3d;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.StringReader;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.web3d.x3d.X3D;

/**
 *
 * @author rafa
 */
public class MolParserTest {
    
    private MolParser instance;
        
    @Before
    public void setUp() {
        instance = new MolParser(new CTFile2X3DConfig());
    }

    @Test
    public void testParseHeader() throws Exception {
        // TODO
    }

    @Test
    public void testParseCountsLine() {
        System.out.println("parseCountsLine");
        String countsLine = "000111222333444555666777888999123 V2000";
        int[] expResult = { 0, 111, 222, 333, 444, 555, 666, 777, 888, 999, 123 };
        int[] result = instance.parseCountsLine(countsLine);
        assertArrayEquals(expResult, result);
    }

    @Test
    public void testParseAtomLine() {
        System.out.println("parseAtomLine");
        String atomLine;
        Atom expResult, result;
        
        atomLine = "   -0.2169    0.6674    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0";
        expResult = new Atom(-0.2169, 0.6674, 0.0, "C", 0);
        result = instance.parseAtomLine(atomLine);
        assertEquals(expResult, result);
        
        atomLine = "   -1.526899999.0001    0.0000 C   0  0  2  0  0  0  0  0  0666  0  0";
        expResult = new Atom(-1.5268, 99999.0001, 0.0, "C", 666);
        result = instance.parseAtomLine(atomLine);
        assertEquals(expResult, result);
    }

    @Test
    public void testParseBondLine() {
        System.out.println("parseBondLine");
        String bondLine;
        Bond expResult, result;
        
        bondLine = "  1 14  1  0  0  0  0";
        expResult = new Bond(1, 14, 1);
        result = instance.parseBondLine(bondLine);
        assertEquals(expResult, result);

        bondLine = "  1  2  2  0  0  0  0";
        expResult = new Bond(1, 2, 2);
        result = instance.parseBondLine(bondLine);
        assertEquals(expResult, result);
    }

    @Test
    public void testParseCtab() throws Exception {
        System.out.println("parseCtab");
        BufferedReader reader = new BufferedReader(new StringReader(
                "  2  1  0  0  1  0            999 V2000\n"
                + "    0.0       0.0       0.0    C   0  0  0  0  0  0  0  0  0  0  0  0\n"
                + "    1.0       0.0       0.0    O   0  0  0  0  0  0  0  0  0  0  0  0\n"
                + "  1  2  2  0  0  0  0"));
        AtomsAndBonds expResult = new AtomsAndBonds();
        expResult.addAtom(new Atom(0.0, 0.0, 0.0, "C", 0));
        expResult.addAtom(new Atom(1.0, 0.0, 0.0, "O", 0));
        expResult.addBond(new Bond(1, 2, 2));
        AtomsAndBonds result = instance.parseCtab(reader);
        assertEquals(expResult, result);
    }

    @Test
    @Ignore("not implemented")
    public void testParseMol() throws Exception {
        System.out.println("parseMol");
        try (
            InputStreamReader isr = new InputStreamReader(
                this.getClass().getClassLoader()
                    .getResourceAsStream("ChEBI_28413.mol"));
            BufferedReader reader = new BufferedReader(isr);
        ){
            AtomsAndBonds expResult = null;
            AtomsAndBonds result = instance.parseMol(reader);
            assertEquals(expResult, result);
        }
    }

    @Test
    public void testToX3D() throws JAXBException {
        System.out.println("toX3D - only visual check!");
        AtomsAndBonds aab = new AtomsAndBonds();
        aab.addAtom(new Atom(0.0, 0.0, 0.0, "C", 0));
        aab.addAtom(new Atom(0.0, 1.0, 0.0, "C", 0));
        aab.addAtom(new Atom(1.0, 0.0, 0.0, "C", 0));
        aab.addAtom(new Atom(1.0, 1.0, 0.0, "C", 0));
        List<Serializable> result = instance.toX3D(aab);
        for (Serializable ser : result) {
            marshallToSystemOut(ser);
        }
    }

    @Test
    public void testParse() throws Exception {
        System.out.println("parse - only visual check!");
        InputStream is = getClass().getClassLoader()
                .getResourceAsStream("ChEBI_28413.mol");
        X3D result = instance.parse(is);
        marshallToSystemOut(result);
    }

    private void marshallToSystemOut(Object jaxbObj)
    throws JAXBException, PropertyException {
        JAXBContext jc = JAXBContext.newInstance("org.web3d.x3d");
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.marshal(jaxbObj, System.out);
    }
    
}