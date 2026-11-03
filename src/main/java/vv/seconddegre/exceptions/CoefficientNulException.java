package vv.seconddegre.exceptions;

/**
 * =====================================================================
 *  EXCEPTION : COEFFICIENT a NUL  —  CoefficientNulException
 * =====================================================================
 *
 * <h2>Cas d'usage</h2>
 * Levée par {@code SolveurSecondDegre.resoudre()} lorsque {@code a == 0.0}.
 * L'équation {@code 0·x² + bx + c = 0} n'est plus du second degré :
 * on sort du domaine de définition du SUT (cas frontière).
 *
 * <h2>Contexte transporté</h2>
 * <ul>
 *   <li>Champ typé {@link #getCoefficient()} : la valeur fautive (0.0)</li>
 *   <li>Via la base : {@code getContexte(Double.class)}</li>
 * </ul>
 *
 * <h2>Usage en test</h2>
 * <pre>
 *   assertThrows(CoefficientNulException.class,
 *                () -&gt; solveur.resoudre(0, 1, 2));
 * </pre>
 */
public class CoefficientNulException extends EquationException {

    /** Identifiant de sérialisation (standard Java). */
    private static final long serialVersionUID = 1L;

    /** Valeur du coefficient nul (toujours 0.0, gardée pour le contexte). */
    private final double coefficient;

    /**
     * @param a valeur du coefficient nul déclenchant l'erreur (0.0)
     */
    public CoefficientNulException(double a) {
        super(
            String.format(
                "Le coefficient 'a' (%s) ne peut pas être nul : l'équation n'est pas du second degré.",
                a
            ),
            a
        );
        this.coefficient = a;
    }

    /**
     * @return la valeur du coefficient fautif
     */
    public double getCoefficient() {
        return coefficient;
    }
}
