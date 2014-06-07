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

import ctfile2x3d.MolParser.NodesAndDefs;
import ctfile2x3d.geom.Vector;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.web3d.x3d.ObjectFactory;
import org.web3d.x3d.PositionInterpolator;
import org.web3d.x3d.ProfileNames;
import org.web3d.x3d.ROUTE;
import org.web3d.x3d.ScalarInterpolator;
import org.web3d.x3d.TimeSensor;
import org.web3d.x3d.X3D;

/**
 *
 * @author rafa
 */
public class RxnParser implements CTFileParser {

    private static final String M_END = "M  END";
    private static final String fraction_changed = "fraction_changed";
    private static final String set_fraction = "set_fraction";
    private static final String value_changed = "value_changed";
    private static final String translation = "translation";

    private final Logger logger = Logger.getLogger(RxnParser.class.getName());

    private final ObjectFactory x3dOf = new ObjectFactory();
    
    private CTFile2X3DConfig conf;
    private MolParser molParser;

    public RxnParser(CTFile2X3DConfig conf) {
        this.conf = conf;
        this.molParser = new MolParser(conf);
        logger.setLevel(Level.FINE); // FIXME
    }
    
    @Override
    public X3D parse(InputStream is, Display display) throws IOException{
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        X3D x3d = x3dOf.createX3D().withScene(x3dOf.createScene()
                .withMetadataBooleanOrMetadataDoubleOrMetadataFloat(
                        toX3D(parseRxn(br), display)))
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

    /**
     * Converts AtomsAndBonds objects into X3D objects which can be added to an
     * X3D Scene.
     * @param aab the objects encapsulating atoms and bonds ([0] for reactants,
     *      [1] for products).
     * @param display the type of display for chemical structures.
     * @return an X3D representation of the RXN file.
     */
    List<Serializable> toX3D(AtomsAndBonds[] aab, Display display) {
        // Render reactants:
        logger.log(Level.FINE, "getting X3D for reactants");
        NodesAndDefs nodesAndDefs = molParser.toX3D(aab[0], display);
        logger.log(Level.FINE, "getting TS");
        final TimeSensor ts = x3dOf.createTimeSensor()
                .withDEF(CssClass.TimeSensor.name())
                .withClazz(CssClass.TimeSensor.name())
                .withEnabled(true).withLoop(true).withCycleInterval("5"); // FIXME
        logger.log(Level.FINE, "adding TS");
        nodesAndDefs.getNodes().add(ts);
        // Process the products and compute the proper animation:
        // - translation for atoms:
        logger.log(Level.FINE,
                "starting loop for atom translation, length: {0}",
                aab[0].getAtoms().size());
        for (Integer aam : aab[0].getAtoms().keySet()){
        logger.log(Level.FINE, "one atom: {0}", aam);
            Atom rAtom = aab[0].getAtoms().get(aam);
            Atom pAtom = aab[1].getAtoms().get(aam);
            Vector v = new Vector(
                    pAtom.getCoordinates().getX()-rAtom.getCoordinates().getX(),
                    pAtom.getCoordinates().getY()-rAtom.getCoordinates().getY(),
                    pAtom.getCoordinates().getZ()-rAtom.getCoordinates().getZ()
            );
            logger.log(Level.FINE, "distance p-r: {0}", v.getMagnitude());
            if (v.getMagnitude() > 0.01){ // FIXME
                // Calculate the animation fractions:
                float start = (1 - conf.getRxnAnimationFraction()) / 2;
                float end = start + conf.getRxnAnimationFraction();
                PositionInterpolator pi = new PositionInterpolator()
                        .withDEF(CssClass.AtomPI.name() + aam)
                        .withClazz(CssClass.AtomPI.name())
                        .withKey("0 " + start + " " + end + " 1")
                        .withKeyValue(rAtom.getCoordinates().toString() + " "
                                + rAtom.getCoordinates().toString() + " "
                                + pAtom.getCoordinates().toString() + " "
                                + pAtom.getCoordinates().toString());
                ROUTE r1 = x3dOf.createROUTE()
                        .withFromNode(ts).withFromField(fraction_changed)
                        .withToNode(pi).withToField(set_fraction);
                ROUTE r2 = x3dOf.createROUTE()
                        .withFromNode(pi).withFromField(value_changed)
                        .withToNode(nodesAndDefs.getDefs()
                                .get(MolParser.AAM + aam.toString()))
                        .withToField(translation);
                logger.log(Level.FINE, "adding routes");
                nodesAndDefs.getNodes().add(pi);
                nodesAndDefs.getNodes().add(r1);
                nodesAndDefs.getNodes().add(r2);
            }
        }
        logger.log(Level.FINE, "loop finished");
        /*
        // - bonds:
        ScalarInterpolator fadeOutInterp = null, fadeInInterp = null;
        for (String bl : aab[0].getBonds().keySet()) {
            Bond rBond = aab[0].getBonds().get(bl);
            Bond pBond = aab[1].getBonds().get(bl);
            if (pBond == null){
                // - fade out for broken bonds
                if (fadeOutInterp == null){
                    fadeOutInterp = x3dOf.createScalarInterpolator()
                            .withKeyValue("0 1, 0 1");
                    ROUTE r1 = x3dOf.createROUTE()
                            .withFromNode(ts)
                            .withFromField("fractin_changed") // FIXME
                            .withToNode(fadeOutInterp)
                            .withToField("set_fracdtion"); // FIXME
                    nodesAndDefs.getNodes().add(fadeOutInterp);
                    nodesAndDefs.getNodes().add(r1);
                }
                ROUTE r2 = x3dOf.createROUTE()
                        .withFromNode(fadeOutInterp)
                        .withFromField("value_changed") // FIXME
                        .withToNode(XXX)
                        .withToField("set_transparency"); // FIXME
                nodesAndDefs.getNodes().add(r2);
            } else if (rBond.getType() == pBond.getType()){
                // - translation for unchanged bonds
                
            } else {
                // - fade out/fade in for changed bonds
                
            }
            // - movement of the camera
        }
        // - fade in for formed bonds
        for (String bl : aab[1].getBonds().keySet()) {
            if (aab[0].getBonds().get(bl) == null){
                nodesAndDefs.getNodes().add(molParser.getBondTransform(
                        aab[1].getBonds().get(bl), aab[1], XXX));
            }
        }
        */
        return nodesAndDefs.getNodes();
    }
    
}
