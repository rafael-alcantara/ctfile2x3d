package net.sf.ctfile2x3d;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.ctfile2x3d.geom.Point;
import net.sf.ctfile2x3d.geom.Vector;
import org.web3d.x3d.Group;
import org.web3d.x3d.ObjectFactory;
import org.web3d.x3d.Shape;
import org.web3d.x3d.Transform;
import org.web3d.x3d.X3D;

/**
 *
 * @author rafa
 */
public class MolParser implements CTFileParser {
    
    private static final String ELEMENT = "element";
    private static final String AAM = "aam";
    private static final String M_END = "M END";


    private final CTFile2X3DConfig conf;
    
    private final ObjectFactory x3dOf = new ObjectFactory();

    public MolParser(CTFile2X3DConfig config) {
        this.conf = config;
    }
    
    @Override
    public X3D parse(InputStream is) throws IOException{
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        AtomsAndBonds aab = parseMol(br);
        X3D x3d = x3dOf.createX3D().withScene(x3dOf.createScene()
                .withMetadataBooleanOrMetadataDoubleOrMetadataFloat(toX3D(aab)));
        return x3d;
    }
    
    /**
     * Parses a whole MOL file (header and ctab). 
     * @param reader A reader ready at the start of the MOL file.
     * @return an object with atoms and bonds.
     * @throws IOException in case of problem reading the data.
     */
    AtomsAndBonds parseMol(BufferedReader reader) throws IOException{
        parseHeader(reader); // TODO
        AtomsAndBonds aab = parseCtab(reader);
        return aab;
    }

    /**
     * Parses the header of a MOL file.
     * @param reader A reader ready at the start of the MOL header.
     * @return an array of Strings:
     *      <ol>
     *          <li>molecule name</li>
     *          <li>metadata</li>
     *          <li>comments</li>
     *      </ol>
     * @throws IOException in case of problem reading the data.
     */
    protected String[] parseHeader(BufferedReader reader) throws IOException {
        return new String[]{
            reader.readLine(), // molecule name
            reader.readLine(), // metadata
            reader.readLine() // comments
        };
    }

    /**
     * Parses a chemical table (counts line, atom block and bond block).
     * @param reader A reader ready at the start of the chemical table.
     * @return an object encapsulating atoms and bonds.
     * @throws IOException in case of problem reading the data.
     */
    AtomsAndBonds parseCtab(BufferedReader reader) throws IOException {
        AtomsAndBonds aab = new AtomsAndBonds();
        int[] counts = parseCountsLine(reader.readLine());
        int atomCount = counts[0];
        int bondsCount = counts[1];
        for (int i = 0; i < atomCount; i++) {
            String atomLine = reader.readLine();
            aab.addAtom(parseAtomLine(atomLine));
        }
        for (int i = 0; i < bondsCount; i++) {
            String bondLine = reader.readLine();
            aab.addBond(parseBondLine(bondLine));
        }
        // ignore properties block:
        String line = reader.readLine();
        while (!line.startsWith(M_END)){}
        return aab;
    }

    /**
     * Parses one counts line.
     * @param countsLine the counts line.
     * @return an array of integers corresponding to the CTFile specification:
     *      <ol>
     *          <li>number of atoms</li>
     *          <li>number of bonds</li>
     *          <li>number of atom lists</li>
     *          <li>(obsolete)</li>
     *          <li>chiral flag: 0=not chiral, 1=chiral</li>
     *          <li>number of stext entries</li>
     *          <li>(obsolete)</li>
     *          <li>(obsolete)</li>
     *          <li>(obsolete)</li>
     *          <li>(obsolete)</li>
     *          <li>number of lines of additional properties, including the
     *              <code>M  END</code> line..</li>
     *      </ol>
     */
    protected int[] parseCountsLine(String countsLine) {
        int[] counts = new int[11];
        for (int i = 0; i < 11; i++) {
            final String txt = countsLine.substring(3*i, 3*i+3).trim();
            if (txt.length() > 0){
                counts[i] = Integer.parseInt(txt);
            }
        }
        String version = countsLine.substring(33);
        return counts;
    }
    
    /**
     * Parses one atom line from the atom block in a MOL.
     * @param atomLine the atom line.
     * @return an Atom.
     */
    Atom parseAtomLine(String atomLine){
        double x = Double.parseDouble(atomLine.substring(0, 10).trim());
        double y = Double.parseDouble(atomLine.substring(10, 20).trim());
        double z = Double.parseDouble(atomLine.substring(20, 30).trim());
        // jump one space (30)
        String symbol = atomLine.substring(31, 34).trim();
        // 34-36: mass difference
        // 36-39: charge
        // 39-42: atom stereo parity
        // 42-45: hydrogen count +1
        // 45-48: stereo care box
        // 48-51: valence
        // 51-54: HO designator
        // 54-57: not used
        // 57-60: not used
        // 60-63: atom-atom mapping:
        final String aamStr = atomLine.substring(60, 63).trim();
        int aam = 0;
        if (!aamStr.isEmpty()){
            aam = Integer.parseInt(aamStr);
        }
        // 63-66: inversion/retention flag
        // 66-69: exact change flag
        return new Atom(x, y, z, symbol, aam);
    }
    
    /**
     * Parses one bond line from the bond block in a MOL.
     * @param bondLine the bond line.
     * @return 
     */
    Bond parseBondLine(String bondLine){
        int fromAtom = Integer.parseInt(bondLine.substring(0, 3).trim());
        int toAtom = Integer.parseInt(bondLine.substring(3, 6).trim());
        int type = Integer.parseInt(bondLine.substring(6, 9).trim());
        // 9-12: bond stereo
        // 12-15: not used
        // 15-18: bond topology
        // 18-21: reacting center status
        return new Bond(fromAtom, toAtom, type);
    }
    
    /**
     * Renders atoms and bonds into a list of X3D objects that can be added to a
     * X3D Scene.
     * @param aab the object encapsulating atoms and bonds.
     * @return a list of X3D objects.
     */
    List<Serializable> toX3D(AtomsAndBonds aab){
        List<Serializable> transforms = new ArrayList<>();
        // Table of existing DEFs:
        Map<String, Serializable> defs = new HashMap<>();
        
        int atomNum = 0;
        for (Map.Entry<Integer, Atom> entry : aab.getAtoms().entrySet()) {
            Integer aam = entry.getKey();
            Atom atom = entry.getValue();
            final Serializable x3dAtom;
            if (defs.containsKey(atom.getSymbol())){
                x3dAtom = x3dOf.createGroup()
                        .withUSE(defs.get(atom.getSymbol()));
            } else {
                x3dAtom = getGroup(atom);
                defs.put(atom.getSymbol(), x3dAtom);
            }
            String def = "" + (atom.getAam() > 0? atom.getAam() : ++atomNum);
            Transform tr = x3dOf.createTransform()
                    .withDEF(def)
                    .withTranslation(atom.getCoordinates().toString())
//                    .withMetadataString(
//                        x3dOf.createMetadataString()
//                                .withName(AAM)
//                                .withValue(aam.toString()))
                    .withBackgroundOrColorInterpolatorOrCoordinateInterpolator(
                        x3dAtom);
            transforms.add(tr);
        }
        for (Map.Entry<String, Bond> entry : aab.getBonds().entrySet()) {
            String label = entry.getKey();
            Bond bond = entry.getValue();
            // one end of the bond:
            Point fromP = aab.getAtoms().get(bond.getFromAtom())
                    .getCoordinates();
            // the other end of the bond:
            Point toP = aab.getAtoms().get(bond.getToAtom()).getCoordinates();
            // central point of the bond:
            Point middle = Point.getMiddle(fromP, toP);
            Vector bondVector = new Vector(toP.getX() - fromP.getX(),
                    toP.getY() - fromP.getY(),
                    toP.getZ() - fromP.getZ());
            double bondLength = bondVector.getMagnitude();
            // Default rendering of Cylinder in X3D is vertical:
            Vector vertVector = new Vector(0, 1, 0);
            final Serializable x3dBond;
            if (defs.containsKey(bond.getTypeLabel())){
                x3dBond = x3dOf.createGroup()
                        .withUSE(defs.get(bond.getTypeLabel()));
            } else {
                x3dBond = getGroup(bond, bondLength);
                defs.put(bond.getTypeLabel(), x3dBond);
            }
            Transform tr = x3dOf.createTransform()
                    .withTranslation(middle.toString())
                    .withBackgroundOrColorInterpolatorOrCoordinateInterpolator(
                            x3dBond);
            double rotAngle = Vector.getAngle(vertVector, bondVector);
            if (rotAngle > 0.01){ // FIXME
                Vector rotVector = Vector.getNormal(vertVector, bondVector);
                tr.setRotation(rotVector.toString() + " " + rotAngle);
            }
            transforms.add(tr);
        }
        return transforms;
    }

    /**
     * Generates the ball and label for one atom.
     * @param atom the atom to render.
     * @return the rendered Group of ball and label.
     */
    private Group getGroup(Atom atom) {
        Element elem;
        try {
            elem = Element.valueOf(atom.getSymbol());
        } catch (Exception e){
            elem = Element.OTHER;
        }
        Group group = x3dOf.createGroup().withDEF(atom.getSymbol())
//                .withMetadataString(x3dOf.createMetadataString()
//                        .withName(ELEMENT)
//                        .withValue(atom.getSymbol()))
                ;
        Shape ball = x3dOf.createShape().withRest(
                x3dOf.createAppearance()
                        .withAppearanceChildContentModel(
                                x3dOf.createMaterial()
                                        .withDiffuseColor(elem.getSphereColor())
                                        .withTransparency(conf.getAtomTransparency())
                        ),
                x3dOf.createSphere().withRadius(elem.getSpacefillRadius()*0.5f) // FIXME
        );
        // TODO: Billboard
        Shape label = x3dOf.createShape().withRest(
                x3dOf.createAppearance()
                        .withAppearanceChildContentModel(
                                x3dOf.createMaterial()
                                        .withDiffuseColor(elem.getLabelColor())
                        ),
                x3dOf.createText()
                        .withString(atom.getSymbol())
                        .withFontStyle(x3dOf.createFontStyle()
                                .withFamily("SANS")
                                .withJustify("MIDDLE")
                                .withSize(conf.getAtomSymbolSize())
                        )
        );
        group.withBackgroundOrColorInterpolatorOrCoordinateInterpolator(
                ball, label);
        return group;
    }

    /**
     * Builds a Group with the Cylinders forming the bond.
     * @param bond the bond.
     * @param bondLength the length of the bond.
     * @return a Group with cylinders.
     */
    private Group getGroup(Bond bond, double bondLength){
        Group group = x3dOf.createGroup().withDEF(bond.getTypeLabel());
        switch (bond.getType()){
            case 1:
                group.withBackgroundOrColorInterpolatorOrCoordinateInterpolator(
                        getBondTransform("0 0 0", bondLength)
                );
                break;
            case 2:
                group.withBackgroundOrColorInterpolatorOrCoordinateInterpolator(
                        getBondTransform("-"+conf.getBondDistance()+" 0 0",
                                bondLength),
                        getBondTransform(conf.getBondDistance()+" 0 0",
                                bondLength)
                );
                break;
            case 3:
                group.withBackgroundOrColorInterpolatorOrCoordinateInterpolator(
                        getBondTransform("-"+conf.getBondDistance()+" 0 0",
                                bondLength),
                        getBondTransform("0 0 0", bondLength),
                        getBondTransform(conf.getBondDistance()+" 0 0",
                                bondLength)
                );
                break;
        }
        return group;
    }

    /**
     * Builds the Transform for one Cylinder of a bond.
     * @param translation the position of the centre of the bond.
     * @param bondLength the length of the bond.
     * @return a Transform including the bond Cylinder.
     */
    private Transform getBondTransform(String translation, double bondLength) {
        return x3dOf.createTransform()
                .withTranslation(translation)
                .withBackgroundOrColorInterpolatorOrCoordinateInterpolator(
                        getBondCylinder(bondLength)
                );
    }

    /**
     * Builds just one Cylinder to render a bond.
     * @param bondLength the length of the bond.
     * @return a Shape with a Cylinder for the bond.
     */
    private Shape getBondCylinder(double bondLength){
        return x3dOf.createShape().withRest(
            x3dOf.createAppearance().withAppearanceChildContentModel(
                    x3dOf.createMaterial().withDiffuseColor(conf.getBondColor())),
            x3dOf.createCylinder()
                    .withRadius(conf.getBondRadius())
                    .withHeight((float) bondLength));
    }
}
