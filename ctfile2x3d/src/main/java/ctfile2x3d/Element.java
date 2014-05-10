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
enum Element {
    
    C(0.70f, "0.2 0.2 0.2", "0 0 0"),
    H(0.25f, "0.6 0.6 0.6", "0 0 0"),
    O(0.60f, "1 0 0", "0.5 0 0"),
    N(0.65f, "0 0 1", "0 0 0.5"),
    OTHER(0.75f, "1 1 0", "0 0 0");
    
    // http://en.wikipedia.org/wiki/Atomic_radius
    private float spacefillRadius;
    private String sphereColor;
    private String labelColor;

    private Element(float sphereRadius, String sphereColor, String labelColor) {
        this.spacefillRadius = sphereRadius;
        this.sphereColor = sphereColor;
        this.labelColor = labelColor;
    }

    float getSpacefillRadius() {
        return spacefillRadius;
    }

    String getSphereColor() {
        return sphereColor;
    }

    String getLabelColor() {
        return labelColor;
    }

}
