package src;

import java.io.Serializable;
import java.util.Vector;

public class InformationPackage implements Serializable {
    private static final long serialVersionUID = 1L;
    private Vector<Character> topVisible;
    private Vector<Character> bottomVisible;
    private Vector<Character> rightVisible;
    private Vector<Character> leftVisible;
}

