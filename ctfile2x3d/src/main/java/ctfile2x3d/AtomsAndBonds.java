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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import ctfile2x3d.geom.Vector;

/**
 *
 * @author rafa
 */
class AtomsAndBonds {
    
    private final Map<Integer, Atom> atoms = new LinkedHashMap<>();
    
    private final Map<String, Bond> bonds = new LinkedHashMap<>();
    
    private double minX = Double.MAX_VALUE,
            maxX = Double.MIN_VALUE,
            minY = Double.MAX_VALUE,
            maxY = Double.MAX_VALUE,
            minZ = Double.MAX_VALUE,
            maxZ = Double.MAX_VALUE;
    
    protected void addAtom(Atom atom){
        if (atoms.get(atom.getAam()) != null){
            throw new IllegalArgumentException("Existing AAM");
        }
        atoms.put(atom.getAam() > 0? atom.getAam() : atoms.size() + 1, atom);
        // recalculate the minimum and maximum values of x, y and z:
        minX = Math.min(minX, atom.getCoordinates().getX());
        maxX = Math.max(maxX, atom.getCoordinates().getX());
        minY = Math.min(minY, atom.getCoordinates().getY());
        maxY = Math.max(maxY, atom.getCoordinates().getY());
        minZ = Math.min(minZ, atom.getCoordinates().getZ());
        maxZ = Math.max(maxZ, atom.getCoordinates().getZ());
    }
    
    protected void addBond(Bond bond){
        String label = bond.getFromAtom() + "-" + bond.getToAtom();
        bonds.put(label, bond);
    }

    public Map<Integer, Atom> getAtoms() {
        return atoms;
    }

    public Map<String, Bond> getBonds() {
        return bonds;
    }

    public double getMinX() {
        return minX;
    }

    public double getMaxX() {
        return maxX;
    }

    public double getMinY() {
        return minY;
    }

    public double getMaxY() {
        return maxY;
    }

    public double getMinZ() {
        return minZ;
    }

    public double getMaxZ() {
        return maxZ;
    }

    /**
     * Moves every atom and bond.
     * @param displacement the displacement to apply.
     */
    protected void move(Vector displacement) {
        for (Atom atom : atoms.values()) {
            atom.getCoordinates().move(displacement);
        }
        // Bonds do not need to apply the displacement,
        // as they only refer to atomss.
    }

    /**
     * Adds every atom and bond from another object of this class.
     * @param aab the other AtomsAndBonds object.
     */
    protected void addAll(AtomsAndBonds aab) {
        for (Map.Entry<Integer, Atom> atom : aab.atoms.entrySet()) {
            this.atoms.put(atom.getKey(), atom.getValue());
        }
        for (Map.Entry<String, Bond> bond : aab.bonds.entrySet()) {
            this.bonds.put(bond.getKey(), bond.getValue());
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 19 * hash + Objects.hashCode(this.atoms);
        hash = 19 * hash + Objects.hashCode(this.bonds);
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
        final AtomsAndBonds other = (AtomsAndBonds) obj;
        if (!Objects.equals(this.atoms, other.atoms)) {
            return false;
        }
        if (!Objects.equals(this.bonds, other.bonds)) {
            return false;
        }
        return true;
    }
    
}
