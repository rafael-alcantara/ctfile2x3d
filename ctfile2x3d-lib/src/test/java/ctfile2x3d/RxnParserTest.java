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

package ctfile2x3d;

import java.io.InputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.web3d.x3d.X3D;

/**
 *
 * @author rafa
 */
public class RxnParserTest {
    
    private RxnParser instance;
    
    public RxnParserTest() {
    }
    
    @Before
    public void setUp() {
        instance = new RxnParser(new CTFile2X3DConfig());
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testParse() throws Exception {
        System.out.println("parse");
        InputStream is = this.getClass().getClassLoader()
                .getResourceAsStream("21881_ordered.rxn");
        X3D x3d = instance.parse(is, Display.MIXED);
        X3DMarshaller.marshallToSystemOut(x3d);
    }
    
}
