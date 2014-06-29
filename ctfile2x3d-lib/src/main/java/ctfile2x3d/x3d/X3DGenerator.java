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
import org.web3d.x3d.Billboard;
import org.web3d.x3d.Group;
import org.web3d.x3d.Material;
import org.web3d.x3d.ObjectFactory;
import org.web3d.x3d.OrientationInterpolator;
import org.web3d.x3d.PositionInterpolator;
import org.web3d.x3d.ROUTE;
import org.web3d.x3d.ScalarInterpolator;
import org.web3d.x3d.Shape;
import org.web3d.x3d.TimeSensor;
import org.web3d.x3d.Transform;
import org.web3d.x3d.X3D;
import org.web3d.x3d.X3DInterpolatorNode;
import org.web3d.x3d.X3DNode;

/**
 * Generator of X3D objects from parsed items.
 * @author rafa
 */
public class X3DGenerator {

    private static final String AAM = "AAM";
    private static final String APP_BOND = "APP_BOND_";
    private static final String MAT_BOND = "MAT_BOND_";
    private static final String INTERP = "INTERP_";
    private static final String FADE_OUT = "FADE_OUT";
    private static final String FADE_IN = "FADE_IN";
    
    private static final String FRACTION_CHANGED = "fraction_changed";
    private static final String SET_FRACTION = "set_fraction";
    private static final String VALUE_CHANGED = "value_changed";
    private static final String TRANSLATION = "translation";
    private static final String ROTATION = "rotation";
    private static final String TRANSPARENCY = "transparency";

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
    private Serializable getAtomLabel(Element elem, Atom atom, Display display) {
        float transparency = 1.0F;
        switch (display) {
            case WIREFRAME:
            case MIXED:
                transparency = 0.0F;
                break;
        }
        Billboard bb = x3dOf.createBillboard().withAxisOfRotation("0 0 0")
            .withBackgroundOrColorInterpolatorOrCoordinateInterpolator(
                x3dOf.createTransform()
                    .withClazz(CssClass.AtomLabelTransform.name())
                    .withTranslation("0 -0.45 0")
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
                                .withSolid(true)
                                .withFontStyle(
                                    x3dOf.createFontStyle()
                                        .withClazz(CssClass.AtomLabelFontStyle.name())
                                        .withFamily("SANS")
                                        .withJustify("MIDDLE MIDDLE")
                                        .withSize(conf.getAtomSymbolSize())
                                )
                        )
                    )
            );
        return bb;
    }

    /**
     * Builds a Transform around a bond.
     * @param bond the bond to render.
     * @param defs a table of DEFs already defined. If the DEF key is not
     *      already there, it will be added.
     * @param display the type of display for chemical structures.
     * @param aab the object containing the atoms linked by this bond.
     * @return a Transform representing a bond.
     */
    Transform getBondTransform(Bond bond, Map<String, X3DNode> defs,
            Display display, AtomsAndBonds aab) {
        // one end of the bond:
        Point fromP = aab.getAtoms().get(bond.getFromAtom()).getCoordinates();
        // the other end of the bond:
        Point toP = aab.getAtoms().get(bond.getToAtom()).getCoordinates();
        // central point of the bond:
        Point middle = Point.getMiddle(fromP, toP);
        Vector bondVector = new Vector(
                toP.getX() - fromP.getX(),
                toP.getY() - fromP.getY(),
                toP.getZ() - fromP.getZ());
        double bondLength = bondVector.getMagnitude();
        // Default rendering of Cylinder in X3D is vertical:
        Vector vertVector = new Vector(0, 1, 0);
        final Serializable x3dBond = getGroup(bond, defs, bondLength, display);
        Transform tr = x3dOf.createTransform()
            .withDEF(bond.getFullLabel())
            .withTranslation(middle.toString())
            .withBackgroundOrColorInterpolatorOrCoordinateInterpolator(x3dBond);
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
    private NodesAndDefs getNodesAndDefs(AtomsAndBonds aab, Display display) {
        List<Serializable> ser = new ArrayList<>();
        // Table of existing DEFs:
        Map<String, X3DNode> defs = new HashMap<>();
        int atomNum = 0;
        for (Map.Entry<Integer, Atom> entry : aab.getAtoms().entrySet()) {
            final Atom atom = entry.getValue();
            Transform tr = getAtomTransform(atom, defs, display, ++atomNum);
            String def = AAM + (atom.getAam() > 0 ? atom.getAam() : atomNum);
            tr.setDEF(def);
            defs.put(def, tr);
            ser.add(tr);
        }
        for (Map.Entry<String, Bond> entry : aab.getBonds().entrySet()) {
            final Bond bond = entry.getValue();
            Transform tr = getBondTransform(bond, defs, display, aab);
            final String bondDef = bond.getFullLabel();
            tr.setDEF(bondDef);
            defs.put(bondDef, tr);
            ser.add(tr);
        }
        ser.add(x3dOf.createViewpoint()
                .withPosition(aab.getMiddle().getX() + " "
                        + aab.getMiddle().getY() + " 10") // FIXME
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
     * @param defs a table of DEFs already defined. If the DEF key is not
     *      already there, it will be added.
     * @param bondLength the length of the bond.
     * @param display the type of display for chemical structures.
     * @return a Group with cylinders.
     */
    private Group getGroup(Bond bond, Map<String, X3DNode> defs,
            double bondLength, Display display) {
        Group group = x3dOf.createGroup();
        switch (bond.getType()) {
            case 1:
                group.withBackgroundOrColorInterpolatorOrCoordinateInterpolator(
                        getBondCylinderTransform("0 0 0", defs, bondLength,
                                bond, display));
                break;
            case 2:
                group.withBackgroundOrColorInterpolatorOrCoordinateInterpolator(
                        getBondCylinderTransform("-" + conf.getBondDistance()
                                + " 0 0", defs, bondLength, bond, display),
                        getBondCylinderTransform(conf.getBondDistance()
                                + " 0 0", defs, bondLength, bond, display));
                break;
            case 3:
                group.withBackgroundOrColorInterpolatorOrCoordinateInterpolator(
                        getBondCylinderTransform("-" + conf.getBondDistance()
                                + " 0 0", defs, bondLength, bond, display),
                        getBondCylinderTransform("0 0 0", defs, bondLength,
                                bond, display),
                        getBondCylinderTransform(conf.getBondDistance()
                                + " 0 0", defs, bondLength, bond, display));
                break;
            case 4:
                // aromatic
                group.withBackgroundOrColorInterpolatorOrCoordinateInterpolator(
                        getBondCylinderTransform("-" + conf.getBondDistance()
                                + " 0 0", defs, bondLength, bond, display),
                        getBondCylinderTransform(conf.getBondDistance()
                                + " 0 0", defs, bondLength, bond, display));
                break;
        }
        return group;
    }

    /*
    private Group getGroup2(Bond bond, double bondLength, Display display) {
        Group group = x3dOf.createGroup();
        float radius = 0.5f;
        float transparency = 0.5f;
        switch (bond.getType()) {
            case 2:
            case 3:
                radius *= bond.getType();
                break;
            case 4: // aromatic
                // Add extra cylinder:
                group.withBackgroundOrColorInterpolatorOrCoordinateInterpolator(
                    x3dOf.createTransform()
                        .withClazz(CssClass.BondCylinderTransform.name())
                        .withTranslation(TRANSLATION)
                        .withScale(scale + " " + scale + " " + scale)
                        .withBackgroundOrColorInterpolatorOrCoordinateInterpolator(
                            getBondCylinder(bondLength, display, bondType));)
                        
                break;
            default:
                break;
        }
        return group;
    }
    */
    
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
        Serializable label = getAtomLabel(elem, atom, display);
        group.withBackgroundOrColorInterpolatorOrCoordinateInterpolator(ball,
                label);
        return group;
    }

    /**
     * Builds a Transform around an atom group (ball + label).
     * @param atom the atom to render.
     * @param defs a table of DEFs already defined. If the DEF key is not
     *      already there, it will be added.
     * @param display the type of display for chemical structures.
     * @param atomNum the atom number. Only used if the atom does not contain
     *      information about its mapping.
     * @return a Transform representing an atom.
     */
    private Transform getAtomTransform(Atom atom,
            Map<String, X3DNode> defs, Display display, int atomNum) {
        final X3DNode x3dAtom;
        if (defs.containsKey(atom.getSymbol())) {
            x3dAtom = x3dOf.createGroup().withUSE(defs.get(atom.getSymbol()));
        } else {
            x3dAtom = getGroup(atom, display);
            defs.put(atom.getSymbol(), x3dAtom);
        }
        Transform tr = x3dOf.createTransform()
                .withTranslation(atom.getCoordinates().toString())
                .withBackgroundOrColorInterpolatorOrCoordinateInterpolator(
                        x3dAtom);
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
                                .withTransparency(transparency)),
                    x3dOf.createSphere()
                        .withRadius(elem.getAtomRadiusEmpirical())));
        return tr;
    }

    /**
     * Builds just one Cylinder to render a bond.
     * @param defs a table of DEFs already defined. If the DEF key is not
     *      already there, it will be added.
     * @param bondLength the length of the bond.
     * @param bond the bond to render
     * @param display the type of display for chemical structures.
     * @return a Shape with a Cylinder for the bond.
     */
    private Shape getBondCylinder(Map<String, X3DNode> defs,
            double bondLength, Bond bond, Display display) {
        float radius;
        switch (display) {
            case WIREFRAME:
                radius = 0.02F;
                break;
            default:
                radius = 0.05F;
        }
        String bondColor = conf.getBondColor(bond.getType());
        final String appDef = APP_BOND + bond.getFullLabel();
        final String matDef = MAT_BOND + bond.getFullLabel();
        X3DNode appearance;
        if (defs.containsKey(appDef)){
            appearance = x3dOf.createAppearance().withUSE(defs.get(appDef));
        } else {
            Material material = x3dOf.createMaterial()
                    .withDEF(matDef)
                    .withClazz(CssClass.BondMaterial.name(),
                            CssClass.BondType.name() + bond.getType())
                    .withDiffuseColor(bondColor);
            appearance = x3dOf.createAppearance()
                    .withDEF(appDef)
                    .withAppearanceChildContentModel(material);
            defs.put(matDef, material);
            defs.put(appDef, appearance);
        }
        return x3dOf.createShape().withRest(
                appearance,
                x3dOf.createCylinder()
                        .withClazz(CssClass.BondCylinder.name())
                        .withRadius(radius)
                        .withHeight((float) (bondLength)));
    }

    /**
     * Builds the Transform for one Cylinder of a bond.
     * @param translation the position of the centre of the bond.
     * @param defs a table of DEFs already defined. If the DEF key is not
     *      already there, it will be added.
     * @param bondLength the length of the bond.
     * @param bond the bond to render.
     * @param display the type of display for chemical structures.
     * @return a Transform including the bond Cylinder.
     */
    private Transform getBondCylinderTransform(String translation,
            Map<String, X3DNode> defs, double bondLength, Bond bond,
            Display display) {
        float scale = 1.0F;
        switch (display) {
            case WIREFRAME:
            case MIXED:
                scale = 0.5F;
                break;
        }
        return x3dOf.createTransform()
                .withClazz(CssClass.BondCylinderTransform.name())
                .withTranslation(translation)
                .withScale(scale + " " + scale + " " + scale)
                .withBackgroundOrColorInterpolatorOrCoordinateInterpolator(
                        getBondCylinder(defs, bondLength, bond, display));
    }

    /**
     * Converts AtomsAndBonds objects representing a reaction into X3D objects
     * which can be added to an X3D Scene.
     * @param aab the objects encapsulating atoms and bonds ([0] for reactants,
     *      [1] for products).
     * @param display the type of display for chemical structures.
     * @return an X3D representation of the RXN file.
     */
    public List<Serializable> toX3D(AtomsAndBonds[] aab, Display display) {
        // Calculate the animation fractions:
        float start = (1 - conf.getRxnAnimationFraction()) / 2;
        float end = start + conf.getRxnAnimationFraction();
        final String key = "0 " + start + " " + end + " 1";
        // Render reactants:
        logger.log(Level.FINE, "getting X3D for reactants");
        NodesAndDefs rNad = getNodesAndDefs(aab[0], display);
        NodesAndDefs pNad = getNodesAndDefs(aab[1], display);
        logger.log(Level.FINE, "getting TS");
        final TimeSensor ts = x3dOf.createTimeSensor()
                .withDEF(CssClass.TimeSensor.name())
                .withClazz(CssClass.TimeSensor.name())
                .withEnabled(true).withLoop(true).withCycleInterval("5"); // FIXME
        logger.log(Level.FINE, "adding TS");
        rNad.nodes.add(ts);
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
            if (v.getMagnitude() > 0.01){
                final X3DNode target = rNad.defs.get(AAM + aam.toString());
                rNad.nodes.addAll(getAnimation(ts, key,
                        target, TRANSLATION,
                        rAtom.getCoordinates().toString(),
                        pAtom.getCoordinates().toString(),
                        rNad.defs,
                        INTERP + TRANSLATION + "_" + target.getDEF()
                ));
            }
        }
        logger.log(Level.FINE, "loop finished");
        // - bonds:
        for (String bl : aab[0].getBonds().keySet()) {
            Bond rBond = aab[0].getBonds().get(bl);
            Bond pBond = aab[1].getBonds().get(bl);
            final String bondMatDef = MAT_BOND + rBond.getFullLabel();
            if (pBond == null){
                final X3DNode target = rNad.defs.get(bondMatDef);
                // - fade out for broken bonds
                rNad.nodes.addAll(getAnimation(ts, key, target,
                        TRANSPARENCY, "0", "1", rNad.defs,
                        INTERP + FADE_OUT));
            } else {
                // Kept bonds (same atoms):
                final Transform rTransform = (Transform)
                        rNad.defs.get(rBond.getFullLabel());
                final Transform pTransform = (Transform)
                        pNad.defs.get(pBond.getFullLabel());
                // - translation
                String fromTr = rTransform.getTranslation();
                String toTr = pTransform.getTranslation();
                // - rotation
                String fromRo = rTransform.getRotation();
                String toRo = pTransform.getRotation();
                moveAndRotate(rNad, ts, key, rTransform,
                        fromTr, toTr, fromRo, toRo);
                // - fade out/fade in for bonds changing type:
                if (rBond.getType() != pBond.getType()){
                    // Fade out reactant bond:
                    rNad.nodes.addAll(getAnimation(ts, key,
                            rNad.defs.get(bondMatDef),
                            TRANSPARENCY, "0", "1", rNad.defs,
                            INTERP + FADE_OUT));
                    // Create a fading-in product bond:
                    Transform fib = addFadeInBond(aab, rBond.getLabel(),
                            rNad, display, ts, key);
                    // Animate product bond:
                    moveAndRotate(rNad, ts, key, fib,
                            fromTr, toTr, fromRo, toRo);
                }
            }
        }
        // - fade in for formed bonds
        for (String bl : aab[1].getBonds().keySet()) {
            if (aab[0].getBonds().get(bl) == null){
                addFadeInBond(aab, bl, rNad, display, ts, key);
            }
        }
        // - movement of the camera
        // TODO
        return rNad.nodes;
    }

    /**
     * Translates and rotates an X3D node.
     * @param rNad object to add the animations to.
     * @param ts Timesensor controlling the animations.
     * @param key the key applied to the interpolators.
     * @param target the X3D node to be translated and rotated.
     * @param fromTr initial position.
     * @param toTr final position.
     * @param fromRo initial rotation.
     * @param toRo final rotation.
     */
    private void moveAndRotate(NodesAndDefs rNad, final TimeSensor ts,
            final String key, final X3DNode target, String fromTr, String toTr,
            String fromRo, String toRo) {
        rNad.nodes.addAll(getAnimation(ts, key, target,
                TRANSLATION, fromTr, toTr, rNad.defs,
                INTERP + TRANSLATION + "_" + target.getDEF()));
        rNad.nodes.addAll(getAnimation(ts, key, target,
                ROTATION, fromRo, toRo, rNad.defs,
                INTERP + ROTATION + "_" + target.getDEF()));
    }

    /**
     * Creates X3D nodes for a new bond which fades in.
     * @param aab the object containing the bond and the bound atoms.
     * @param bl the label for the bond.
     * @param rNad the object to add the new bond and its animation.
     * @param display the type of display for the bond.
     * @param ts the Timesensor controlling the animation.
     * @param key the key applied to the interpolator.
     * @return the created bond as a Transform node.
     */
    private Transform addFadeInBond(AtomsAndBonds[] aab, String bl,
            NodesAndDefs rNad, Display display, final TimeSensor ts,
            final String key) {
        Bond pBond = aab[1].getBonds().get(bl);
        Transform tr =
                getBondTransform(pBond, rNad.defs, display, aab[1]);
        final String trDef = pBond.getFullLabel();
        tr.setDEF(trDef);
        rNad.nodes.add(tr);
        rNad.defs.put(trDef, tr);
        // , then the animation:
        rNad.nodes.addAll(getAnimation(ts, key,
                rNad.defs.get(MAT_BOND + trDef),
                TRANSPARENCY, "1.0", "0.0", rNad.defs,
                INTERP + FADE_IN));
        return tr;
    }
    
    /**
     * Builds one Interpolator and two ROUTEs to animate an object.
     * @param ts the TimeSensor triggering the animation.
     * @param key the four fractions of time defining the animation.
     * @param target the object being animated.
     * @param field the field which changes during the animation.
     * @param fromValue the initial value of the <code>field</code> at the
     *      beginning of the animation.
     * @param toValue the final value of the <code>field</code> at the end of
     *      the animation.
     * @param defs map of DEFs already created, to reuse any existing
     *      Interpolator.
     * @param interpDef DEF for the interpolator to use.
     * @return 
     */
    private Collection<Serializable> getAnimation(TimeSensor ts, String key,
            X3DNode target, String field, String fromValue, String toValue,
            Map<String, X3DNode> defs, String interpDef){
        Collection<Serializable> anim = new ArrayList<>();
        X3DInterpolatorNode interp = null;
        if (defs.containsKey(interpDef)){
            // do not create interpolator nor route 1
            // crete only route 2
            interp = (X3DInterpolatorNode) defs.get(interpDef);
        } else {
            switch (field) {
                case TRANSLATION:
                    interp = x3dOf.createPositionInterpolator()
                            .withKeyValue(fromValue + " " + fromValue + " "
                                    + toValue + " " + toValue);
                    break;
                case ROTATION:
                    interp = x3dOf.createOrientationInterpolator()
                            .withKeyValue(fromValue + " " + fromValue + " "
                                    + toValue + " " + toValue);
                    break;
                case TRANSPARENCY:
                    interp = x3dOf.createScalarInterpolator()
                            .withKeyValue(fromValue + " " + fromValue + " "
                                    + toValue + " " + toValue);
                    break;
            }
            interp.setDEF(interpDef);
            interp.setKey(key);
            // Add the interpolator to defs for reuse:
            defs.put(interpDef, interp);
            ROUTE r1 = x3dOf.createROUTE()
                    .withFromNode(ts)
                    .withFromField(FRACTION_CHANGED)
                    .withToNode(interp)
                    .withToField(SET_FRACTION);
            anim.add(interp);
            anim.add(r1);
        }
        ROUTE r2 = x3dOf.createROUTE()
                .withFromNode(interp)
                .withFromField(VALUE_CHANGED)
                .withToNode(target)
                .withToField(field);
        anim.add(r2);
        return anim;
    }

    /**
     * Inner class to encapsulate both the X3D nodes to be added to a scene and
     * the DEFs among them.
     * <br>
     * The DEFs table contains these types of keys/values:
     * <table>
     *  <tr><th>key</th><th>value</th></tr>
     *  <tr>
     *      <td>Element symbol</td>
     *      <td><code>Group</code> containing both atom sphere and label.</td>
     *  </tr>
     *  <tr>
     *      <td>Atom number (atom-atom mapping), with the <code>AAM</code>
     *          prefix (ex. <code>AAM3, AAM21</code>).</td>
     *      <td><code>Transform</code> for one concrete atom.</td>
     *  </tr>
     *  <tr>
     *      <td>Bond label indicating the two bound atoms (ex.
     *          <code>2-6</code>).</td>
     *      <td><code>Transform</code> for one concrete bond.</td>
     *  </tr>
     *  <tr>
     *      <td><code>APP_BOND4 | APP_BOND</code></td>
     *      <td><code>Appearance</code> for bonds (aromatic | any other type).
     *          </td>
     *  </tr>
     *  <tr>
     *      <td><code>MAT_BOND4 | MAT_BOND</code></td>
     *      <td><code>Material</code> for bonds (aromatic | any other type).
     *          </td>
     *  </tr>
     * </table>
     */
    private class NodesAndDefs {
        
        private final List<Serializable> nodes;
        private final Map<String, X3DNode> defs;

        NodesAndDefs(List<Serializable> nodes, Map<String, X3DNode> defs) {
            this.nodes = nodes;
            this.defs = defs;
        }

        List<Serializable> getNodes() {
            return nodes;
        }

        Map<String, X3DNode> getDefs() {
            return defs;
        }

    }
    
}
