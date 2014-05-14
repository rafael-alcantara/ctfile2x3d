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

import java.util.Objects;
import ctfile2x3d.geom.Point;

/**
 *
 * @author rafa
 */
class Atom {
    
    private final Point coordinates;
    private final String symbol;
    private final int aam;

    protected Atom(double x, double y, double z, String symbol, int aam) {
        this.coordinates = new Point(x, y, z);
        this.symbol = symbol;
        this.aam = aam;
    }

    public Point getCoordinates() {
        return coordinates;
    }

    protected String getSymbol() {
        return symbol;
    }

    protected int getAam() {
        return aam;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 61 * hash + Objects.hashCode(this.coordinates);
        hash = 61 * hash + Objects.hashCode(this.symbol);
        hash = 61 * hash + this.aam;
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
        final Atom other = (Atom) obj;
        if (!Objects.equals(this.coordinates, other.coordinates)) {
            return false;
        }
        if (!Objects.equals(this.symbol, other.symbol)) {
            return false;
        }
        if (this.aam != other.aam) {
            return false;
        }
        return true;
    }
    
}
