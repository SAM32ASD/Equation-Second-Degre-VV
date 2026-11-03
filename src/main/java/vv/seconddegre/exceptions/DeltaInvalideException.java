package vv.seconddegre.exceptions;

/**
 * =====================================================================
 *  EXCEPTION : DELTA NON FINI  —  DeltaInvalideException
 * =====================================================================
 *
 * <h2>Cas d'usage</h2>
 * Levée lorsque {@code delta = b² − 4ac} vaut {@code NaN} ou
 * {@code ±Infinité}. Cela se produit en cas de <b>débordement de
 * plage</b> (overflow) :
 * <pre>
 *   resoudre(1e200, 1e200, 1e200)  →  b² = Inf  →  delta = NaN
 * </pre>
 * Sans cette exception, le solveur renverrait silencieusement un
 * résultat faux (ou lèverait plus tard une exception obscure).
 *
 * <h2>Contexte transporté</h2>
 * <ul>
 *   <li>Champ typé {@link #getDelta()} : la valeur invalide</li>
 *   <li>Via la base : {@code getContexte(Double.class)}</li>
 * </ul>
 */
public class DeltaInvalideException extends EquationException {

    /** Identifiant de sérialisation (standard Java). */
    private static final long serialVersionUID = 1L;

    /** Valeur du discriminant non finie. */
    private final double delta;

    /**
     * @param delta valeur non finie (NaN ou ±Inf) du discriminant
     */
    public DeltaInvalideException(double delta) {
        super(
            String.format(
                "Le discriminant (%s) n'est pas une valeur finie : calcul impossible.",
                delta
            ),
            delta
        );
        this.delta = delta;
    }

    /**
     * @return la valeur du discriminant invalide
     */
    public double getDelta() {
        return delta;
    }
}
