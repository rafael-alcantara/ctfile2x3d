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

/**
 *
 * @author rafa
 */
class Bond {
    
    private final int fromAtom, toAtom;
    private final int type;

    protected Bond(int fromAtom, int toAtom, int type) {
        if (fromAtom == toAtom){
            throw new IllegalArgumentException("Bond to the same atom!");
        }
        this.fromAtom = fromAtom < toAtom? fromAtom : toAtom;
        this.toAtom = fromAtom < toAtom? toAtom : fromAtom;
        this.type = type;
    }

    protected int getFromAtom() {
        return fromAtom;
    }

    protected int getToAtom() {
        return toAtom;
    }

    protected int getType() {
        return type;
    }
    
    protected String getTypeLabel(){
        return "bondType" + type;
    }
    
    protected String getLabel(){
        return fromAtom + "-" + toAtom;
    }

    @Override
    public int hashCode() {
        int hash = 7;
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
        final Bond other = (Bond) obj;
        if (this.fromAtom != other.fromAtom) {
            return false;
        }
        if (this.toAtom != other.toAtom) {
            return false;
        }
        if (this.type != other.type) {
            return false;
        }
        return true;
    }

}
