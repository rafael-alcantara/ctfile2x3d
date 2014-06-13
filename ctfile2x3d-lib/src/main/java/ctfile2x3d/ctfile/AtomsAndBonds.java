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

package ctfile2x3d.ctfile;

import ctfile2x3d.geom.Point;
import ctfile2x3d.geom.Vector;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author rafa
 */
public class AtomsAndBonds {
    
    private final Map<Integer, Atom> atoms = new LinkedHashMap<>();
    
    private final Map<String, Bond> bonds = new LinkedHashMap<>();
    
    private String name;
    
    private double minX = Double.POSITIVE_INFINITY,
            maxX = Double.NEGATIVE_INFINITY,
            minY = Double.POSITIVE_INFINITY,
            maxY = Double.NEGATIVE_INFINITY,
            minZ = Double.POSITIVE_INFINITY,
            maxZ = Double.NEGATIVE_INFINITY;
    
    public void addAtom(Atom atom){
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
    
    public void addBond(Bond bond){
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Calculates the middle point of this object.
     * @return the middle point of this object.
     */
    public Point getMiddle(){
        return new Point((maxX + minX)/2, (maxY + minY)/2, (maxZ + minZ)/2);
    }

    public double getWidth() {
        return maxX - minX;
    }

    /**
     * Moves every atom and bond.
     * @param displacement the displacement to apply.
     */
    public void move(Vector displacement) {
        for (Atom atom : atoms.values()) {
            atom.getCoordinates().move(displacement);
        }
        /* Bonds do not need to apply the displacement,
           as they only refer to atom numbers. */
        // Recalculate limits:
        minX += displacement.getX();
        maxX += displacement.getX();
        minY += displacement.getY();
        maxY += displacement.getY();
        minZ += displacement.getZ();
        maxZ += displacement.getZ();
    }

    /**
     * Adds every atom and bond from another object of this class.
     * @param aab the other AtomsAndBonds object.
     */
    public void addAll(AtomsAndBonds aab) {
        for (Atom atom : aab.atoms.values()) {
            addAtom(atom);
        }
        for (Bond bond : aab.bonds.values()) {
            addBond(bond);
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
