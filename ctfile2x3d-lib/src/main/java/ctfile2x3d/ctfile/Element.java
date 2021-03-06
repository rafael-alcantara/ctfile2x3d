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

/**
 * Enumeration of chemical elements with their
 * <a href="http://en.wikipedia.org/wiki/Atomic_radius">atomic radii</a> and
 * colors (based on the
 * <a href="http://en.wikipedia.org/wiki/CPK_coloring">CPK</a> convention).
 * @author rafa
 */
public enum Element {
    
    C(0.70f, 0.67f, 1.7f, "0.2 0.2 0.2", "0 0 0"),
    H(0.25f, 0.53f, 1.2f, "0.6 0.6 0.6", "0 0 0"),
    O(0.60f, 0.48f, 1.52f, "1 0 0", "0.5 0 0"),
    N(0.65f, 0.56f, 1.55f, "0 0 1", "0 0 0.5"),
    F(0.5f, 0.42f, 1.47f, "0 0.7 0", "0 0.35 0"),
    Na(1.8f, 1.9f, 2.27f,"0.6 0 1", "0.3 0 0.5"),
    Mg(1.5f, 1.45f, 1.73f, "0 0.4 0", "0 0.2 0"),
    P(1.0f, 0.98f, 1.8f, "1 0.5 0", "0.5 0.25 0"),
    S(1.0f, 0.98f, 1.8f, "0.8 0.8 0", "0.4 0.4 0"),
    Cl(1.0f, 0.79f, 1.75f, "0 0.7 0", "0 0.35 0"),
    K(2.2f, 2.43f, 2.75f, "0.6 0 1", "0.3 0 0.5"),
    Ca(1.8f, 1.94f, 1f, "0 0.4 0", "0 0.2 0"), // FIXME VdW
    Mn(1.4f, 1.61f, 1f, "1 0.8 0.4", "0.5 0.4 0.2"), // FIXME VdW
    Fe(1.4f, 1.56f, 1f, "0.5 0.4 0", "0.25 0.2 0"), // FIXME VdW
    Cu(1.35f, 1.45f, 1.4f, "1 0.8 0.4", "0.5 0.4 0.2"),
    Zn(1.35f, 1.42f, 1.39f, "1 0.8 0.4", "0.5 0.4 0.2"),
    I(1.4f, 1.15f, 1.98f, "0.3 0 0.5", "0.15 0 0.25"),
    OTHER(0.75f, 0.75f, 1f, "1 0.6 0.6", "0.5 0.3 0.3"); // FIXME VdW
    
    private final float atomRadiusEmpirical;
    private final float atomRadiusCalculated;
    private final float atomRadiusVdw;
    private final String sphereColor;
    private final String labelColor;

    private Element(float atomRadiusEmpirical, float atomRadiusCalculated,
            float atomRadiusVdw, String sphereColor, String labelColor) {
        this.atomRadiusEmpirical = atomRadiusEmpirical;
        this.atomRadiusCalculated = atomRadiusCalculated;
        this.atomRadiusVdw = atomRadiusVdw;
        this.sphereColor = sphereColor;
        this.labelColor = labelColor;
    }

    /**
     * Gets the empirical atom radius as published by Slater in 1964. The
     * accuracy is about 5pm.
     * @return the atom radius in armstrongs.
     */
    public float getAtomRadiusEmpirical() {
        return atomRadiusEmpirical;
    }

    /**
     * The atom radius as calculated by Clementi et al. in 1967 from theoretical
     * models.
     * @return the atom radius in armstrongs.
     */
    public float getAtomRadiusCalculated() {
        return atomRadiusCalculated;
    }

    /**
     * Gets the CPK color for the element.
     * @return a color in RGB space, one-based.
     */
    public String getSphereColor() {
        return sphereColor;
    }

    public String getLabelColor() {
        return labelColor;
    }

}
