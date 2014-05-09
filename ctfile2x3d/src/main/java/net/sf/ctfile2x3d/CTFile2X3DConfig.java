/*
 * CTFile2X3DConfig.java
 *
 * Created on May 6, 2014, 6:50 PM
 */

package net.sf.ctfile2x3d;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Configuration for CTFile2X3D parsers and tools.
 *
 * @author rafa
 */
public class CTFile2X3DConfig implements CTFile2X3DConfigMBean {
    
    public static final String ATOM_SYMBOL_SIZE = "atom.symbol.size";
    public static final String ATOM_TRANSPARENCY = "atom.transparency";
    public static final String BOND_RADIUS = "bond.radius";
    public static final String BOND_DISTANCE = "bond.distance";
    public static final String BOND_COLOR = "bond.color";
    public static final String MOLECULE_SPACING = "molecule.spacing";
    public static final String MOL_URL_PATTERN = "url.pattern.mol";
    public static final String RXN_URL_PATTERN = "url.pattern.rxn";
    
    /**
     * Attribute : AtomSymbolSize
     */
    private float atomSymbolSize = 0.5f;
    /**
     * Attribute : AtomTransparency
     */
    private float atomTransparency = 0.3f;
    /**
     * Attribute : BondRadius
     */
    private float bondRadius = 0.1f;
    /**
     * Attribute : BondDistance
     */
    private float bondDistance = 0.1f;
    /**
     * Attribute : BondColor
     */
    private String bondColor = "0.75 0.75 0.75";
    /**
     * Attribute : MoleculeSpacing
     */
    private float moleculeSpacing;
    /**
     * Attribute : MolUrlPattern
     */
    private String molUrlPattern;
    /**
     * Attribute : RxnUrlPattern
     */
    private String rxnUrlPattern;
    
    /**
     * Default constructor. It tries to load settings from a file
     * <code>CTFile2X3DConfig.properties</code> in the classpath if available,
     * otherwise it uses the default values.
     * @see #load(java.util.Properties) 
     */
    public CTFile2X3DConfig() {
        InputStream is = CTFile2X3DConfig.class.getClassLoader()
                .getResourceAsStream("CTFile2X3DConfig.properties");
        if (is != null){
            Properties props = new Properties();
            try {
                props.load(is);
                load(props);
            } catch (IOException ex) {
                Logger.getLogger(CTFile2X3DConfig.class.getName()).log(
                        Level.SEVERE,
                        "Unable to load configuration from default file",
                        ex);
            }
        }
    }

    /**
     * Constructor with settings overriding the default values.
     * @param props the properties with their values.
     * @see #load(java.util.Properties) 
     */
    public CTFile2X3DConfig(Properties props) {
        load(props);
    }
    
    /**
     * Loads the configuration values from properties, overriding the existing
     * ones. The available keys are:
     * <ul>
     *  <li><code>atom.symbol.size</code></li>
     *  <li><code>atom.transparency</code></li>
     *  <li><code>bond.radius</code></li>
     *  <li><code>bond.distance</code></li>
     *  <li><code>bond.color</code></li>
     *  <li><code>molecule.spacing</code></li>
     *  <li><code>url.pattern.mol</code></li>
     *  <li><code>url.pattern.rxn</code></li>
     * </ul>
     * See the corresponding getter/setter methods for details.
     * @param props the configuration properties.
     */
    private void load(Properties props){
        if (props.containsKey(ATOM_SYMBOL_SIZE)){
            setAtomSymbolSize(
                    Float.parseFloat(props.getProperty(ATOM_SYMBOL_SIZE)));
        }
        if (props.containsKey(ATOM_TRANSPARENCY)){
            setAtomTransparency(
                    Float.parseFloat(props.getProperty(ATOM_TRANSPARENCY)));
        }
        if (props.containsKey(BOND_RADIUS)){
            setBondRadius(Float.parseFloat(props.getProperty(BOND_RADIUS)));
        }
        if (props.containsKey(BOND_DISTANCE)){
            setBondDistance(Float.parseFloat(props.getProperty(BOND_DISTANCE)));
        }
        if (props.containsKey(BOND_COLOR)){
            setBondColor(props.getProperty(BOND_COLOR));
        }
        if (props.containsKey(MOLECULE_SPACING)){
            setMoleculeSpacing(
                    Float.parseFloat(props.getProperty(MOLECULE_SPACING)));
        }
        if (props.containsKey(MOL_URL_PATTERN)){
            setMolUrlPattern(props.getProperty(MOL_URL_PATTERN));
        }
        if (props.containsKey(RXN_URL_PATTERN)){
            setRxnUrlPattern(props.getProperty(RXN_URL_PATTERN));
        }
    }

    @Override
    public String getMolUrlPattern() {
        return molUrlPattern;
    }

    @Override
    public void setMolUrlPattern(String value) {
        molUrlPattern = value;
    }

    @Override
    public String getRxnUrlPattern() {
        return rxnUrlPattern;
    }

    @Override
    public void setRxnUrlPattern(String value) {
        rxnUrlPattern = value;
    }

    @Override
    public float getAtomTransparency() {
        return atomTransparency;
    }

    @Override
    public void setAtomTransparency(float value) {
        atomTransparency = value;
    }

    @Override
    public float getBondRadius() {
        return bondRadius;
    }

    @Override
    public void setBondRadius(float value) {
        bondRadius = value;
    }

    @Override
    public float getBondDistance() {
        return bondDistance;
    }

    @Override
    public void setBondDistance(float value) {
        bondDistance = value;
    }

    @Override
    public String getBondColor() {
        return bondColor;
    }

    @Override
    public void setBondColor(String value) {
        bondColor = value;
    }

    @Override
    public float getMoleculeSpacing() {
        return moleculeSpacing;
    }

    @Override
    public void setMoleculeSpacing(float value) {
        moleculeSpacing = value;
    }

    @Override
    public float getAtomSymbolSize() {
        return atomSymbolSize;
    }

    @Override
    public void setAtomSymbolSize(float value) {
        atomSymbolSize = value;
    }
    
}
