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

/**
 *
 * @author rafa
 */
public class Vector {

    private final double x, y, z, magnitude;

    public Vector(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.magnitude = Math.sqrt(x*x + y*y + z*z);
    }

    public Vector(Point start, Point end) {
        x = end.getX() - start.getX();
        y = end.getY() - start.getY();
        z = end.getZ() - start.getZ();
        magnitude = Math.sqrt(x*x + y*y + z*z);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public double getMagnitude() {
        return magnitude;
    }
    
    /**
     * Calculates the normal to a plane defined by three points.
     * @param p1
     * @param p2
     * @param p3
     * @return the vector normal to the vectors <code>p1 -> p2</code> and
     *      <code>p1 -> p3</code>.
     * @see #getNormal(net.sf.ctfile2x3d.geom.Vector, net.sf.ctfile2x3d.geom.Vector) 
     * @see <a href="http://en.wikipedia.org/wiki/Plane_%28geometry%29">Plane</a>
     */
    public static Vector getNormal(Point p1, Point p2, Point p3){
        return getNormal(new Vector(p1, p2), new Vector(p1, p3));
    }
    
    /**
     * Calculates the vector normal to other two given vectors.
     * @param v1
     * @param v2
     * @return The normal vector, whose direction follows the right hand rule.
     * @see <a href="/en.wikipedia.org/wiki/Cross_product">Cross product</a>
     */
    public static Vector getNormal(Vector v1, Vector v2){
        return new Vector(
                v1.getY() * v2.getZ() - v1.getZ() * v2.getY(),
                v1.getZ() * v2.getX() - v1.getX() * v2.getZ(),
                v1.getX() * v2.getY() - v1.getY() * v2.getX());
    }
    
    public static double getDotProduct(Vector v1, Vector v2){
        return v1.getX()*v2.getX() + v1.getY()*v2.getY() + v1.getZ()*v2.getZ();
    }
    
    public static double getAngle(Vector v1, Vector v2){
        return Math.acos(Vector.getDotProduct(v1, v2)
                / (v1.getMagnitude() * v2.getMagnitude()));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.z) ^ (Double.doubleToLongBits(this.z) >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Vector other = (Vector) obj;
        if (Double.doubleToLongBits(this.x) != Double.doubleToLongBits(other.x)) {
            return false;
        }
        if (Double.doubleToLongBits(this.y) != Double.doubleToLongBits(other.y)) {
            return false;
        }
        if (Double.doubleToLongBits(this.z) != Double.doubleToLongBits(other.z)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return  x + " " + y + " " + z;
    }
    
}
