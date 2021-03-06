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

import java.io.IOException;
import java.io.InputStream;
import org.web3d.x3d.X3D;

/**
 *
 * @author rafa
 */
public interface CTFileParser {
    
    /**
     * Parses an input (CTFile) to generate X3D.
     * @param is an input stream for a CTFile.
     * @param display the type of display for chemical structures.
     * @return an X3D object.
     * @throws java.io.IOException in case of problem reading the input.
     */
    public X3D parse(InputStream is, Display display) throws IOException;
}
