package ctfile2x3d;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ctfile2x3d.geom.Point;
import ctfile2x3d.geom.Vector;
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
    
    static final String AAM = "AAM";

    private final CTFile2X3DConfig conf;
    
    private final ObjectFactory x3dOf = new ObjectFactory();

    public MolParser(CTFile2X3DConfig config) {
        this.conf = config;
    }
    
    @Override
    public X3D parse(InputStream is, Display display) throws IOException{
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        AtomsAndBonds aab = parseMol(br);
        X3D x3d = x3dOf.createX3D().withScene(x3dOf.createScene()
                .withMetadataBooleanOrMetadataDoubleOrMetadataFloat(
                        toX3D(aab, display).getNodes())
        );
        return x3d;
    }
    
    /**
     * Parses a whole MOL file (header and ctab). 
     * @param reader A reader ready at the start of the MOL file.
     * @return an object with atoms and bonds, centered in the origin.
     * @throws IOException in case of problem reading the data.
     */
    AtomsAndBonds parseMol(BufferedReader reader) throws IOException{
        String[] header = parseHeader(reader);
        AtomsAndBonds aab = parseCtab(reader);
        aab.setName(header[0]);
        // Move all atoms and bonds to the origin:
        final Point m = aab.getMiddle();
        aab.move(new Vector(-m.getX(), -m.getY(), -m.getZ()));
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
        boolean isAam = false; // is there any atom-atom mapping?
        for (int i = 0; i < atomCount; i++) {
            String atomLine = reader.readLine();
            final Atom atom = parseAtomLine(atomLine);
            if (atom.getAam() > 0) isAam = true;
            aab.addAtom(atom);
        }
        List<Atom> atoms = isAam? new ArrayList(aab.getAtoms().values()) : null;
        for (int i = 0; i < bondsCount; i++) {
            String bondLine = reader.readLine();
            final Bond bond = parseBondLine(bondLine, atoms);
            aab.addBond(bond);
        }
        // ignore properties block
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
     * @param atoms the list of bound atoms. If not <code>null</code>, these
     *      atoms contain the atom-atom mapping information required to build
     *      the bond.
     * @return a bond between two atoms.
     */
    Bond parseBondLine(String bondLine, List<Atom> atoms){
        int fromAtom = Integer.parseInt(bondLine.substring(0, 3).trim());
        int toAtom = Integer.parseInt(bondLine.substring(3, 6).trim());
        int type = Integer.parseInt(bondLine.substring(6, 9).trim());
        // 9-12: bond stereo
        // 12-15: not used
        // 15-18: bond topology
        // 18-21: reacting center status
        return new Bond(
                atoms == null? fromAtom : atoms.get(fromAtom-1).getAam(),
                atoms == null? toAtom : atoms.get(toAtom-1).getAam(), type);
    }
    
    /**
     * Renders atoms and bonds into a list of X3D objects that can be added to a
     * X3D Scene.
     * @param aab the object encapsulating atoms and bonds.
     * @param display the type of display for chemical structures.
     * @return a list of X3D objects along with the map of DEFs used.
     */
    NodesAndDefs toX3D(AtomsAndBonds aab, Display display){
        List<Serializable> ser = new ArrayList<>();
        // Table of existing DEFs:
        Map<String, Serializable> defs = new HashMap<>();
        
        int atomNum = 0;
        for (Map.Entry<Integer, Atom> entry : aab.getAtoms().entrySet()) {
            Transform tr = getAtomTransform(entry.getValue(), defs, display,
                    ++atomNum);
            ser.add(tr);
        }
        for (Map.Entry<String, Bond> entry : aab.getBonds().entrySet()) {
            Transform tr = getBondTransform(entry.getValue(), defs, display,
                    aab);
            ser.add(tr);
        }
        ser.add(x3dOf.createViewpoint()
                .withPosition(
                    aab.getMiddle().getX()+" "+aab.getMiddle().getY()+" 10")
                .withDescription(aab.getName())
        );
        // TODO: add SphereSensor?
        NodesAndDefs nodesAndDefs = new NodesAndDefs(ser, defs);
        return nodesAndDefs;
    }

    /**
     * Builds a Transform around a bond.
     * @param bond the bond to render.
     * @param defs a table of DEFs already defined, mapping bond types to
     *      their X3D representation. If the <code>bond</code> type is not
     *      already there, it will be added.
     * @param display the type of display for chemical structures.
     * @param aab the object containing the atoms linked by this bond.
     * @return a Transform representing a bond.
     */
    Transform getBondTransform(Bond bond, Map<String, Serializable> defs,
            Display display, AtomsAndBonds aab) {
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
            x3dBond = getGroup(bond, bondLength, display);
            defs.put(bond.getTypeLabel(), x3dBond);
        }
        Transform tr = x3dOf.createTransform()
                .withDEF(bond.getLabel())
                .withTranslation(middle.toString())
                .withBackgroundOrColorInterpolatorOrCoordinateInterpolator(
                        x3dBond);
        double rotAngle = Vector.getAngle(vertVector, bondVector);
        if (rotAngle > 0.01){ // FIXME
            Vector rotVector = Vector.getNormal(vertVector, bondVector);
            tr.setRotation(rotVector.toString() + " " + rotAngle);
        }
        return tr;
    }

    /**
     * Builds a Transform around an atom group (ball + label).
     * @param atom the atom to render.
     * @param defs a table of DEFs already defined, mapping atom symbols to
     *      their X3D representation. If the <code>atom</code> symbol is not
     *      already there, it will be added.
     * @param display the type of display for chemical structures.
     * @param atomNum the atom number. Only used if the atom does not contain
     *      information about its mapping.
     * @return a Transform representing an atom.
     */
    private Transform getAtomTransform(Atom atom,
            Map<String, Serializable> defs, Display display, int atomNum) {
        final Serializable x3dAtom;
        if (defs.containsKey(atom.getSymbol())){
            x3dAtom = x3dOf.createGroup()
                    .withUSE(defs.get(atom.getSymbol()));
        } else {
            x3dAtom = getGroup(atom, display);
            defs.put(atom.getSymbol(), x3dAtom);
        }
        String def = AAM + (atom.getAam() > 0? atom.getAam() : atomNum);
        Transform tr = x3dOf.createTransform()
                .withDEF(def)
                .withTranslation(atom.getCoordinates().toString())
                .withBackgroundOrColorInterpolatorOrCoordinateInterpolator(
                        x3dAtom);
        defs.put(def, tr);
        return tr;
    }

    /**
     * Generates the ball and label for one atom.
     * @param atom the atom to render.
     * @param display the type of display for chemical structures.
     * @return the rendered Group of ball and label.
     */
    private Group getGroup(Atom atom, Display display) {
        Element elem;
        try {
            elem = Element.valueOf(atom.getSymbol());
        } catch (Exception e){
            elem = Element.OTHER;
        }
        Group group = x3dOf.createGroup().withDEF(atom.getSymbol());
        Transform ball = getAtomBall(elem, display);
        Transform label = getAtomLabel(elem, atom, display);
        group.withBackgroundOrColorInterpolatorOrCoordinateInterpolator(
                ball, label);
        return group;
    }

    /**
     * Builds an X3D text with the element symbol.
     * @param elem The element to render as a label.
     * @param atom 
     * @param display the type of display for chemical structures.
     * @return 
     */
    private Transform getAtomLabel(Element elem, Atom atom, Display display) {
        float transparency = 1f;
        switch (display){
            case WIREFRAME:
            case MIXED:
                transparency = 0f;
                break;
        }
        // TODO: Billboard?
        Transform tr = x3dOf.createTransform()
            .withClazz(CssClass.AtomLabelTransform.name())
            .withTranslation("0 -0.45 0") // FIXME
            .withBackgroundOrColorInterpolatorOrCoordinateInterpolator(
                x3dOf.createShape().withRest(
                x3dOf.createAppearance()
                    .withAppearanceChildContentModel(
                        x3dOf.createMaterial()
                                .withClazz(CssClass.AtomLabelMaterial.name())
                                .withDiffuseColor(elem.getLabelColor())
                                .withTransparency(transparency)
                    ),
                x3dOf.createText()
                    .withString(atom.getSymbol())
                    .withFontStyle(x3dOf.createFontStyle()
                            .withClazz(CssClass.AtomLabelFontStyle.name())
                            .withFamily("SANS")
                            .withJustify("MIDDLE MIDDLE")
                            .withSize(conf.getAtomSymbolSize())
                    )
        ));
        return tr;
    }

    /**
     * Builds an X3D Sphere.
     * @param elem The element to render as a sphere.
     * @param display the type of display for chemical structures.
     * @return a Transform containing a sphere.
     */
    private Transform getAtomBall(Element elem, Display display) {
        float scale = 1f, transparency = 0f;
        switch (display){
            case WIREFRAME:
            case STICKS:
                transparency = 1f;
                break;
            case BALLS_STICKS:
                scale = 0.5f;
            case SPACEFILL:
            case MIXED:
                transparency = conf.getAtomTransparency();
                break;
        }
        Transform tr = x3dOf.createTransform()
            .withClazz(CssClass.AtomSphereTransform.name())
            .withScale(scale + " " + scale + " " + scale)
            .withBackgroundOrColorInterpolatorOrCoordinateInterpolator(
                x3dOf.createShape().withRest(
                    x3dOf.createAppearance()
                        .withAppearanceChildContentModel(
                            x3dOf.createMaterial()
                                    .withClazz(CssClass.AtomSphereMaterial.name())
                                    .withDiffuseColor(elem.getSphereColor())
                                    .withTransparency(transparency)
                    ),
                    x3dOf.createSphere()
                            .withRadius(elem.getAtomRadiusEmpirical())
                )
            );
        return tr;
    }

    /**
     * Builds a Group with the Cylinders forming the bond.
     * @param bond the bond.
     * @param bondLength the length of the bond.
     * @param display the type of display for chemical structures.
     * @return a Group with cylinders.
     */
    private Group getGroup(Bond bond, double bondLength, Display display){
        Group group = x3dOf.createGroup().withDEF(bond.getTypeLabel());
        switch (bond.getType()){
            case 1:
                group.withBackgroundOrColorInterpolatorOrCoordinateInterpolator(
                        getBondTransform("0 0 0", bondLength, display)
                );
                break;
            case 2:
                group.withBackgroundOrColorInterpolatorOrCoordinateInterpolator(
                        getBondTransform("-"+conf.getBondDistance()+" 0 0",
                                bondLength, display),
                        getBondTransform(conf.getBondDistance()+" 0 0",
                                bondLength, display)
                );
                break;
            case 3:
                group.withBackgroundOrColorInterpolatorOrCoordinateInterpolator(
                        getBondTransform("-"+conf.getBondDistance()+" 0 0",
                                bondLength, display),
                        getBondTransform("0 0 0", bondLength, display),
                        getBondTransform(conf.getBondDistance()+" 0 0",
                                bondLength, display)
                );
                break;
        }
        return group;
    }

    /**
     * Builds the Transform for one Cylinder of a bond.
     * @param translation the position of the centre of the bond.
     * @param bondLength the length of the bond.
     * @param display the type of display for chemical structures.
     * @return a Transform including the bond Cylinder.
     */
    private Transform getBondTransform(String translation, double bondLength,
            Display display) {
        float scale = 1f;
        switch (display){
            case WIREFRAME:
            case MIXED:
                scale = 0.5f;
                break;
        }
        return x3dOf.createTransform()
                .withClazz(CssClass.BondCylinderTransform.name())
                .withTranslation(translation)
                .withScale(scale + " " + scale + " " + scale)
                .withBackgroundOrColorInterpolatorOrCoordinateInterpolator(
                        getBondCylinder(bondLength, display)
                );
    }

    /**
     * Builds just one Cylinder to render a bond.
     * @param bondLength the length of the bond.
     * @param display the type of display for chemical structures.
     * @return a Shape with a Cylinder for the bond.
     */
    private Shape getBondCylinder(double bondLength, Display display){
        float transparency = 0.5f;
        float radius = 0.05f;
        switch (display){
            case WIREFRAME:
                transparency = 0f;
                radius = 0.02f;
                break;
            case SPACEFILL:
                transparency = 1f;
                break;
        }
        return x3dOf.createShape().withRest(
            x3dOf.createAppearance()
                    .withAppearanceChildContentModel(
                        x3dOf.createMaterial()
                                .withClazz(CssClass.BondMaterial.name())
                                .withDiffuseColor(conf.getBondColor())
                                .withTransparency(transparency)),
            x3dOf.createCylinder()
                    .withClazz(CssClass.BondCylinder.name())
                    .withRadius(radius)
                    .withHeight((float) (bondLength)));
    }
    
    /**
     * Inner class to encapsulate both the X3D nodes to be added to a scene and
     * the DEFs among them.
     */
    class NodesAndDefs {
        
        private final List<Serializable> nodes;
        private final Map<String, Serializable> defs;

        NodesAndDefs(List<Serializable> nodes, Map<String, Serializable> defs) {
            this.nodes = nodes;
            this.defs = defs;
        }

        List<Serializable> getNodes() {
            return nodes;
        }

        Map<String, Serializable> getDefs() {
            return defs;
        }
        
    }
}
