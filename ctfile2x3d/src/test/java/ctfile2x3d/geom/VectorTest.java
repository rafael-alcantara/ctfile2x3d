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

package ctfile2x3d.geom;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author rafa
 */
public class VectorTest {

    @Test
    public void testGetNormal_3args() {
        System.out.println("getNormal");
        Point p1, p2, p3;
        Vector expResult, result;

        p1 = new Point(0, 0, 0);
        p2 = new Point(0, 1, 0);
        p3 = new Point(0, 0, 1);

        result = Vector.getNormal(p1, p2, p3);
        expResult = new Vector(1, 0, 0);
        System.out.println(result.toString());
        assertEquals(expResult, result);

        result = Vector.getNormal(p1, p3, p2);
        expResult = new Vector(-1, 0, 0);
        System.out.println(result.toString());
        assertEquals(expResult, result);
        
        result = Vector.getNormal(p2, p1, p3);
        expResult = new Vector(-1, 0, 0);
        System.out.println(result.toString());
        assertEquals(expResult, result);

        result = Vector.getNormal(p2, p3, p1); // OK
        expResult = new Vector(1, 0, 0);
        System.out.println(result.toString());
        assertEquals(expResult, result);
        
        result = Vector.getNormal(p3, p1, p2);
        expResult = new Vector(1, 0, 0);
        System.out.println(result.toString());
        assertEquals(expResult, result);

        result = Vector.getNormal(p3, p2, p1);
        expResult = new Vector(-1, 0, 0);
        System.out.println(result.toString());
        assertEquals(expResult, result);
    }

    @Test
    public void testGetNormal_Vector_Vector() {
        System.out.println("getNormal");
        Vector v1, v2;
        Vector expResult, result;
        
        v1 = new Vector(0, 1, 0);
        v2 = new Vector(0, 0, 1);
        expResult = new Vector(1, 0, 0);
        result = Vector.getNormal(v1, v2);
        assertEquals(expResult, result);
        
        v1 = new Vector(0, 1, 0);
        v2 = new Vector(0, 0, -1);
        expResult = new Vector(-1, 0, 0);
        result = Vector.getNormal(v1, v2);
        assertEquals(expResult, result);
    }
    
}
