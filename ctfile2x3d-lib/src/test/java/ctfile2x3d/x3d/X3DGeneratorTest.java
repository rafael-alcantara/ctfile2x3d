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

package ctfile2x3d.x3d;

import ctfile2x3d.CTFile2X3DConfig;
import ctfile2x3d.Display;
import ctfile2x3d.X3DMarshaller;
import ctfile2x3d.ctfile.Atom;
import ctfile2x3d.ctfile.AtomsAndBonds;
import javax.xml.bind.JAXBException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.web3d.x3d.X3D;

/**
 *
 * @author rafa
 */
public class X3DGeneratorTest {
    
    private X3DGenerator instance;
    
    @Before
    public void setUp() {
        instance = new X3DGenerator(new CTFile2X3DConfig());
    }

    @Test
    @Ignore("only visual check")
    public void testToX3D_AtomsAndBonds_Display() throws JAXBException {
        System.out.println("toX3D");
        System.out.println("toX3D - only visual check!");
        AtomsAndBonds aab = new AtomsAndBonds();
        aab.addAtom(new Atom(0.0, 0.0, 0.0, "C", 0));
        aab.addAtom(new Atom(0.0, 1.0, 0.0, "C", 0));
        aab.addAtom(new Atom(1.0, 0.0, 0.0, "C", 0));
        aab.addAtom(new Atom(1.0, 1.0, 0.0, "C", 0));
        X3D x3d = instance.toX3D(aab, Display.MIXED);
        X3DMarshaller.marshallToSystemOut(x3d);
    }
    
}
