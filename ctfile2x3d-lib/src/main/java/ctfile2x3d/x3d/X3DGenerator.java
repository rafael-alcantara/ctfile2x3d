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

package ctfile2x3d.x3d;

import ctfile2x3d.CTFile2X3DConfig;
import ctfile2x3d.Display;
import ctfile2x3d.RxnParser;
import ctfile2x3d.ctfile.Atom;
import ctfile2x3d.ctfile.AtomsAndBonds;
import ctfile2x3d.ctfile.Bond;
import ctfile2x3d.ctfile.Element;
import ctfile2x3d.geom.Point;
import ctfile2x3d.geom.Vector;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.web3d.x3d.Group;
import org.web3d.x3d.ObjectFactory;
import org.web3d.x3d.PositionInterpolator;
import org.web3d.x3d.ROUTE;
import org.web3d.x3d.Shape;
import org.web3d.x3d.TimeSensor;
import org.web3d.x3d.Transform;
import org.web3d.x3d.X3D;

/**
 * Generator of X3D objects from parsed items.
 * @author rafa
 */
public class X3DGenerator {

    private static final String AAM = "AAM";
    private static final String fraction_changed = "fraction_changed";
    private static final String set_fraction = "set_fraction";
    private static final String value_changed = "value_changed";
    private static final String translation = "translation";

    private static final Logger logger =
            Logger.getLogger(X3DGenerator.class.getName());
    
    public final CTFile2X3DConfig conf;
    public final ObjectFactory x3dOf = new ObjectFactory();

    public X3DGenerator(CTFile2X3DConfig conf) {
        this.conf = conf;
    }

    /**
     * Builds an X3D text with the element symbol.
     * @param elem The element to render as a label.
     * @param atom
     * @param display the type of display for chemical structures.
     * @return
     */
    private Transform getAtomLabel(Element elem, Atom atom, Display display) {
        float transparency = 1.0F;
        switch (display) {
            case WIREFRAME:
            case MIXED:
                transparency = 0.0F;
                break;
        }
        // TODO: Billboard?
        Transform tr = x3dOf.createTransform().withClazz(CssClass.AtomLabelTransform.name()).withTranslation("0 -0.45 0").withBackgroundOrColorInterpolatorOrCoordinateInterpolator(x3dOf.createShape().withRest(x3dOf.createAppearance().withAppearanceChildContentModel(x3dOf.createMaterial().withClazz(CssClass.AtomLabelMaterial.name()).withDiffuseColor(elem.getLabelColor()).withTransparency(transparency)), x3dOf.createText().withString(atom.getSymbol()).withFontStyle(x3dOf.createFontStyle().withClazz(CssClass.AtomLabelFontStyle.name()).withFamily("SANS").withJustify("MIDDLE MIDDLE").withSize(conf.getAtomSymbolSize()))));
        return tr;
    }

    /**
     * Builds a Transform around a bond.
     * @param bond the bond to render.
     * @param display the type of display for chemical structures.
     * @param aab the object containing the atoms linked by this bond.
     * @return a Transform representing a bond.
     */
    Transform getBondTransform(Bond bond, Display display, AtomsAndBonds aab) {
        // one end of the bond:
        Point fromP = aab.getAtoms().get(bond.getFromAtom()).getCoordinates();
        // the other end of the bond:
        Point toP = aab.getAtoms().get(bond.getToAtom()).getCoordinates();
        // central point of the bond:
        Point middle = Point.getMiddle(fromP, toP);
        Vector bondVector = new Vector(toP.getX() - fromP.getX(), toP.getY() - fromP.getY(), toP.getZ() - fromP.getZ());
        double bondLength = bondVector.getMagnitude();
        // Default rendering of Cylinder in X3D is vertical:
        Vector vertVector = new Vector(0, 1, 0);
        final Serializable x3dBond = getGroup(bond, bondLength, display);
        Transform tr = x3dOf.createTransform().withDEF(bond.getLabel()).withTranslation(middle.toString()).withBackgroundOrColorInterpolatorOrCoordinateInterpolator(x3dBond);
        double rotAngle = Vector.getAngle(vertVector, bondVector);
        if (rotAngle > 0.01) {
            // FIXME
            Vector rotVector = Vector.getNormal(vertVector, bondVector);
            tr.setRotation(rotVector.toString() + " " + rotAngle);
        }
        return tr;
    }

    /**
     * Renders atoms and bonds into a list of X3D objects that can be added to a
     * X3D Scene.
     * @param aab the object encapsulating atoms and bonds.
     * @param display the type of display for chemical structures.
     * @return a list of X3D objects along with the map of DEFs used.
     */
    public NodesAndDefs getNodesAndDefs(AtomsAndBonds aab, Display display) {
        List<Serializable> ser = new ArrayList<>();
        // Table of existing DEFs:
        Map<String, Serializable> defs = new HashMap<>();
        int atomNum = 0;
        for (Map.Entry<Integer, Atom> entry : aab.getAtoms().entrySet()) {
            Transform tr = getAtomTransform(entry.getValue(), defs, display, ++atomNum);
            ser.add(tr);
        }
        for (Map.Entry<String, Bond> entry : aab.getBonds().entrySet()) {
            Transform tr = getBondTransform(entry.getValue(), display, aab);
            ser.add(tr);
        }
        ser.add(x3dOf.createViewpoint()
                .withPosition(
                    aab.getMiddle().getX() + " " + aab.getMiddle().getY() + " 10")
                .withDescription(aab.getName()));
        // TODO: add SphereSensor?
        NodesAndDefs nodesAndDefs = new NodesAndDefs(ser, defs);
        return nodesAndDefs;
    }
    
    public X3D toX3D(AtomsAndBonds aab, Display display) {
        X3D x3d = x3dOf.createX3D().withScene(x3dOf.createScene()
                .withMetadataBooleanOrMetadataDoubleOrMetadataFloat(
                        getNodesAndDefs(aab, display).getNodes()));
        return x3d;
    }
    
    /**
     * Builds a Group with the Cylinders forming the bond.
     * @param bond the bond.
     * @param bondLength the length of the bond.
     * @param display the type of display for chemical structures.
     * @return a Group with cylinders.
     */
    private Group getGroup(Bond bond, double bondLength, Display display) {
        Group group = x3dOf.createGroup();
        switch (bond.getType()) {
            case 1:
                group.withBackgroundOrColorInterpolatorOrCoordinateInterpolator(getBondCylinderTransform("0 0 0", bondLength, display, null));
                break;
            case 2:
                group.withBackgroundOrColorInterpolatorOrCoordinateInterpolator(getBondCylinderTransform("-" + conf.getBondDistance() + " 0 0", bondLength, display, null), getBondCylinderTransform(conf.getBondDistance() + " 0 0", bondLength, display, null));
                break;
            case 3:
                group.withBackgroundOrColorInterpolatorOrCoordinateInterpolator(getBondCylinderTransform("-" + conf.getBondDistance() + " 0 0", bondLength, display, null), getBondCylinderTransform("0 0 0", bondLength, display, null), getBondCylinderTransform(conf.getBondDistance() + " 0 0", bondLength, display, null));
                break;
            case 4:
                // aromatic
                group.withBackgroundOrColorInterpolatorOrCoordinateInterpolator(getBondCylinderTransform("-" + conf.getBondDistance() + " 0 0", bondLength, display, null), getBondCylinderTransform(conf.getBondDistance() + " 0 0", bondLength, display, bond.getType()));
                break;
        }
        return group;
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
        } catch (Exception e) {
            elem = Element.OTHER;
        }
        Group group = x3dOf.createGroup().withDEF(atom.getSymbol());
        Transform ball = getAtomBall(elem, display);
        Transform label = getAtomLabel(elem, atom, display);
        group.withBackgroundOrColorInterpolatorOrCoordinateInterpolator(ball, label);
        return group;
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
    private Transform getAtomTransform(Atom atom, Map<String, Serializable> defs, Display display, int atomNum) {
        final Serializable x3dAtom;
        if (defs.containsKey(atom.getSymbol())) {
            x3dAtom = x3dOf.createGroup().withUSE(defs.get(atom.getSymbol()));
        } else {
            x3dAtom = getGroup(atom, display);
            defs.put(atom.getSymbol(), x3dAtom);
        }
        String def = AAM + (atom.getAam() > 0 ? atom.getAam() : atomNum);
        Transform tr = x3dOf.createTransform().withDEF(def).withTranslation(atom.getCoordinates().toString()).withBackgroundOrColorInterpolatorOrCoordinateInterpolator(x3dAtom);
        defs.put(def, tr);
        return tr;
    }

    /**
     * Builds an X3D Sphere.
     * @param elem The element to render as a sphere.
     * @param display the type of display for chemical structures.
     * @return a Transform containing a sphere.
     */
    private Transform getAtomBall(Element elem, Display display) {
        float scale = 1.0F;
        float transparency = 0.0F;
        switch (display) {
            case WIREFRAME:
            case STICKS:
                transparency = 1.0F;
                break;
            case BALLS_STICKS:
                scale = 0.5F;
                break;
            case MIXED:
                transparency = conf.getAtomTransparency();
                break;
        }
        Transform tr = x3dOf.createTransform().withClazz(CssClass.AtomSphereTransform.name()).withScale(scale + " " + scale + " " + scale).withBackgroundOrColorInterpolatorOrCoordinateInterpolator(x3dOf.createShape().withRest(x3dOf.createAppearance().withAppearanceChildContentModel(x3dOf.createMaterial().withClazz(CssClass.AtomSphereMaterial.name()).withDiffuseColor(elem.getSphereColor()).withTransparency(transparency)), x3dOf.createSphere().withRadius(elem.getAtomRadiusEmpirical())));
        return tr;
    }

    /**
     * Builds just one Cylinder to render a bond.
     * @param bondLength the length of the bond.
     * @param display the type of display for chemical structures.
     * @param bondType the bond type (<code>null)</code> means render default).
     * @return a Shape with a Cylinder for the bond.
     */
    private Shape getBondCylinder(double bondLength, Display display, Integer bondType) {
        float transparency = 0.5F;
        float radius = 0.05F;
        switch (display) {
            case WIREFRAME:
                transparency = 0.0F;
                radius = 0.02F;
                break;
            case SPACEFILL:
                transparency = 1.0F;
                break;
        }
        if (bondType != null) {
            transparency += (1 - transparency) / 1.5; // FIXME?
        }
        Collection<Serializable> appNodes = new ArrayList<>();
        appNodes.add(x3dOf.createMaterial().withClazz(CssClass.BondMaterial.name(), CssClass.BondType.name() + bondType).withDiffuseColor(conf.getBondColor()).withTransparency(transparency));
        /* Not supporte by x3dom yet:
        if (hatch != null){
        appNodes.add(x3dOf.createFillProperties()
        .withClazz("fillProperties")
        .withFilled(true)
        .withHatched(true)
        .withHatchColor("1 1 1") // FIXME
        .withHatchStyle(BigInteger.valueOf(hatch)));
        }
         */
        return x3dOf.createShape().withRest(x3dOf.createAppearance().withAppearanceChildContentModel(appNodes), x3dOf.createCylinder().withClazz(CssClass.BondCylinder.name()).withRadius(radius).withHeight((float) (bondLength)));
    }

    /**
     * Builds the Transform for one Cylinder of a bond.
     * @param translation the position of the centre of the bond.
     * @param bondLength the length of the bond.
     * @param display the type of display for chemical structures.
     * @param bondType the bond type (<code>null)</code> means render default).
     * @return a Transform including the bond Cylinder.
     */
    private Transform getBondCylinderTransform(String translation, double bondLength, Display display, Integer bondType) {
        float scale = 1.0F;
        switch (display) {
            case WIREFRAME:
            case MIXED:
                scale = 0.5F;
                break;
        }
        return x3dOf.createTransform().withClazz(CssClass.BondCylinderTransform.name()).withTranslation(translation).withScale(scale + " " + scale + " " + scale).withBackgroundOrColorInterpolatorOrCoordinateInterpolator(getBondCylinder(bondLength, display, bondType));
    }


    /**
     * Converts AtomsAndBonds objects into X3D objects which can be added to an
     * X3D Scene.
     * @param aab the objects encapsulating atoms and bonds ([0] for reactants,
     *      [1] for products).
     * @param display the type of display for chemical structures.
     * @return an X3D representation of the RXN file.
     */
    public List<Serializable> toX3D(AtomsAndBonds[] aab, Display display) {
        // Render reactants:
        logger.log(Level.FINE, "getting X3D for reactants");
        NodesAndDefs nodesAndDefs = getNodesAndDefs(aab[0], display);
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
                                .get(X3DGenerator.AAM + aam.toString()))
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
