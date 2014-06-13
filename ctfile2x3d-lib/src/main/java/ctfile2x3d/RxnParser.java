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

import ctfile2x3d.ctfile.AtomsAndBonds;
import ctfile2x3d.geom.Vector;
import ctfile2x3d.x3d.X3DGenerator;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.web3d.x3d.ObjectFactory;
import org.web3d.x3d.ProfileNames;
import org.web3d.x3d.X3D;

/**
 *
 * @author rafa
 */
public class RxnParser implements CTFileParser {

    private static final String M_END = "M  END";

    private static final Logger logger =
            Logger.getLogger(RxnParser.class.getName());

    private final ObjectFactory x3dOf = new ObjectFactory();
    
    private final X3DGenerator x3dGen;
    private final CTFile2X3DConfig conf;
    private final MolParser molParser;

    public RxnParser(CTFile2X3DConfig conf) {
        this.conf = conf;
        this.molParser = new MolParser(conf);
        this.x3dGen = new X3DGenerator(conf);
        logger.setLevel(Level.FINE); // FIXME
    }
    
    @Override
    public X3D parse(InputStream is, Display display) throws IOException{
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        X3D x3d = x3dOf.createX3D().withScene(x3dOf.createScene()
                .withMetadataBooleanOrMetadataDoubleOrMetadataFloat(
                        x3dGen.toX3D(parseRxn(br), display)))
                .withProfile(ProfileNames.FULL);
        return x3d;
    }

    /**
     * Parses a whole RXN file (header, counts line and participants).
     * @param brA reader ready at the start of the RXN file.
     * @return two objects representing atoms and bonds:
     *      <ol>
     *          <li>reactants</li>
     *          <li>products</li>
     *      </ol>
     * @throws IOException in case of problem reading the data.
     */
    private AtomsAndBonds[] parseRxn(BufferedReader br) throws IOException {
        String[] headerLines = parserHeader(br); // TODO
        int[] participants = parseCountsLine(br);
        AtomsAndBonds[] aab = {
            parseParticipants(br, participants[0]),
            parseParticipants(br, participants[1])
        };
        return aab;
    }

    /**
     * Parses the header of the RXN file.
     * @param br A reader ready at the start of the header.
     * @return an array of strings:
     *      <ol>
     *          <li><code>$RXN</code></li>
     *          <li>reaction name</li>
     *          <li>author, program, version, date, regno</li>
     *          <li>comment</li>
     *      </ol>
     * @throws IOException in case of problem reading the data.
     */
    private String[] parserHeader(BufferedReader br) throws IOException {
        String[] headerLines = new String[4];
        headerLines[0] = br.readLine(); // $RXN
        headerLines[1] = br.readLine(); // reaction name
        headerLines[2] = br.readLine(); // author, program, version, date, regno
        headerLines[3] = br.readLine(); // comment
        return headerLines;
    }

    /**
     * Parses the counts line.
     * @param br A reader ready at the start of the counts line.
     * @return an array of reaction participants numbers ([0] for reactants,
     *      [1] for products).
     * @throws IOException in case of problem reading the data.
     */
    private int[] parseCountsLine(BufferedReader br) throws IOException {
        String countsLine = br.readLine();
        int[] counts = {
            Integer.parseInt(countsLine.substring(0, 3).trim()),
            Integer.parseInt(countsLine.substring(3, 6).trim())
        };
        return counts;
    }

    /**
     * Parses the participants of the reaction.
     * @param br A reader ready at the start of the participants block (either
     *      reactants or products).
     * @param num the number of participants to parse.
     * @return an object with atoms and bonds representing the participants.
     * @throws IOException in case of problem reading the data.
     */
    private AtomsAndBonds parseParticipants(BufferedReader br, int num)
    throws IOException {
        AtomsAndBonds participants = null;
        for (int i = 0; i < num; i++) {
            logger.log(Level.INFO,
                    "Parsing participant {0}... ", i);
            String line = br.readLine(); // skip $MOL line
            AtomsAndBonds aab = molParser.parseMol(br);
            // ignore the properties block:
            line = br.readLine();
            while (!line.startsWith(M_END)){
                line = br.readLine();
            }
            if (participants == null){
                participants = aab;
            } else {
                aab.move(new Vector(
                        participants.getMaxX() + conf.getMoleculeSpacing() + aab.getWidth()/2,
                        0, 0));
                participants.addAll(aab);
            }
            logger.log(Level.FINE, "Participant at {0}", aab.getMiddle());
        }
        return participants;
    }
    
}
