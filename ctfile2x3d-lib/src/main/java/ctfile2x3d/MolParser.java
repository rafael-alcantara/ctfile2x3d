package ctfile2x3d;

import ctfile2x3d.ctfile.Atom;
import ctfile2x3d.ctfile.AtomsAndBonds;
import ctfile2x3d.ctfile.Bond;
import ctfile2x3d.ctfile.Element;
import ctfile2x3d.x3d.X3DGenerator;
import ctfile2x3d.geom.Point;
import ctfile2x3d.geom.Vector;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.web3d.x3d.Group;
import org.web3d.x3d.Shape;
import org.web3d.x3d.Transform;
import org.web3d.x3d.X3D;

/**
 *
 * @author rafa
 */
public class MolParser implements CTFileParser {
    
    private final X3DGenerator x3dGen;

    public MolParser(CTFile2X3DConfig config) {
        x3dGen = new X3DGenerator(config);
    }
    
    @Override
    public X3D parse(InputStream is, Display display) throws IOException{
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        AtomsAndBonds aab = parseMol(br);
        return x3dGen.toX3D(aab, display);
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

}
