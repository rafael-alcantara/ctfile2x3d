/*
 * CTFile2X3DConfigMBean.java
 *
 * Created on May 6, 2014, 6:50 PM
 */

package ctfile2x3d;

/**
 * Interface CTFile2X3DConfigMBean
 *
 * @author rafa
 */
public interface CTFile2X3DConfigMBean {

    /**
     * Get pattern for the URL serving MOL files.
     * @return 
     */
    public String getMolUrlPattern();

    /**
     * Set pattern for the URL serving MOL files.
     * @param value
     */
    public void setMolUrlPattern(String value);

    /**
     * Get pattern for the URL serving RXN files.
     * @return 
     */
    public String getRxnUrlPattern();

    /**
     * Set pattern for the URL serving RXN files.
     * @param value
     */
    public void setRxnUrlPattern(String value);

    /**
     * Get transparency of spheres representing atoms.
     * @return 
     */
    public float getAtomTransparency();

    /**
     * Set transparency of spheres representing atoms.
     * @param value
     */
    public void setAtomTransparency(float value);

    /**
     * Get distance between cylinders representing double/triple bonds.
     * @return 
     */
    public float getBondDistance();

    /**
     * Set distance between cylinders representing double/triple bonds.
     * @param value
     */
    public void setBondDistance(float value);

    /**
     * Get color of cylinders representing bonds.
     * @return 
     */
    public String getBondColor();

    /**
     * Set color of cylinders representing bonds.
     * @param value
     */
    public void setBondColor(String value);

    /**
     * Get font size for the atom symbols.
     * @return 
     */
    public float getAtomSymbolSize();

    /**
     * Set font size for the atom symbols.
     * @param value
     */
    public void setAtomSymbolSize(float value);

    /**
     * Get horizontal spacing between molecules in an X3D representation of a
     * reaction (RXN).
     * @return 
     */
    public float getMoleculeSpacing();

    /**
     * Set horizontal spacing between molecules in an X3D representation of a
     * reaction (RXN).
     * @param value
     */
    public void setMoleculeSpacing(float value);

    /**
     * Gets the fraction of the time loop taken by the animation from
     * reactants to products in RXN files.
     * This fraction (a value from 0 to 1) is used to animate atoms and bonds
     * from reactants to products, the rest being equally distributed between
     * static reactants at the beginning and static products at the end of the
     * loop.
     * @return the fraction of the time loop taken by the animation from
     *      reactants to products in RXN files.
     */
    public float getRxnAnimationFraction();

    /**
     * Sets the fraction of the time loop taken by the animation from
     * reactants to products in RXN files.
     * @param value a fraction between 0 and 1.
     */
    public void setRxnAnimationFraction(float value);

    /**
     * Gets the time in seconds for the time loop in RXN animations.
     * @return the duration of the loop in seconds.
     */
    public float getRxnCycleInterval();
    
    /**
     * Sets the time in seconds for the time loop in RXN animations.
     * @param value the duration of the loop in seconds.
     */
    public void setRxnCycleInterval(float value);


}
