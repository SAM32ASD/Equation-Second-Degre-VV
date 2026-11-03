package vv.seconddegre.exceptions;

/**
 * =====================================================================
 *  EXCEPTION DE BASE DU SOLVEUR  —  EquationException
 * =====================================================================
 *
 * <h2>Pourquoi une exception maison ?</h2>
 * Les exceptions natives ({@code IllegalArgumentException}, etc.) sont
 * trop génériques : elles ne transportent pas le <b>contexte métier</b>
 * (quel coefficient est fautif, valeur du delta, racines calculées…).
 * On crée donc une petite hiérarchie dédiée au domaine « équation du
 * second degré ».
 *
 * <h2>Design « générique sans générique »</h2>
 * En Java, <b>une classe générique ne peut pas étendre {@code Throwable}</b>
 * (erreur de compilation : <i>"a generic class may not extend
 * java.lang.Throwable"</i>). On contourne cette contrainte en stockant
 * le contexte comme {@code Object} et en exposant un accesseur générique :
 *
 * <pre>
 *   public &lt;T&gt; T getContexte(Class&lt;T&gt; type) { return type.cast(contexte); }
 * </pre>
 *
 * Ainsi chaque sous-classe précise le type réel du contexte, et l'appelant
 * récupère un objet typé sans cast explicite.
 *
 * <h2>Hiérarchie</h2>
 * <pre>
 *   EquationException (RuntimeException)
 *   ├── CoefficientNulException    contexte : Double  (valeur de a)
 *   ├── DeltaInvalideException     contexte : Double  (valeur de delta)
 *   └── ResultatNonFiniException   contexte : double[] (racines)
 * </pre>
 *
 * <h2>Choix RuntimeException (non checked)</h2>
 * Un bug logique (a = 0 dans le domaine métier) n'a pas à être déclaré
 * dans la signature : on hérite du comportement de
 * {@code IllegalArgumentException} qu'il remplace.
 */
public class EquationException extends RuntimeException {

    /** Identifiant de sérialisation (standard Java). */
    private static final long serialVersionUID = 1L;

    /** Contexte d'erreur porté par l'exception (type libre). */
    private final Object contexte;

    /**
     * Constructeur minimal : message seul, sans contexte.
     *
     * @param message description de l'erreur
     */
    public EquationException(String message) {
        this(message, null, null);
    }

    /**
     * Constructeur avec contexte métier.
     *
     * @param message  description de l'erreur
     * @param contexte donnée associée (coefficient, delta, tableau…)
     */
    public EquationException(String message, Object contexte) {
        this(message, contexte, null);
    }

    /**
     * Constructeur complet avec cause enchaînée.
     *
     * @param message  description de l'erreur
     * @param contexte donnée associée
     * @param cause    exception d'origine (peut être {@code null})
     */
    public EquationException(String message, Object contexte, Throwable cause) {
        super(message, cause);
        this.contexte = contexte;
    }

    /**
     * Récupère le contexte d'erreur sous le type attendu
     * (méthode générique — voir Javadoc de classe).
     *
     * @param type classe du contexte attendu (ex. {@code Double.class})
     * @param <T>  type du contexte
     * @return le contexte casté, ou {@code null}
     * @throws ClassCastException si le contexte n'est pas du type demandé
     */
    public <T> T getContexte(Class<T> type) {
        return type.cast(contexte);
    }

    /**
     * @return le contexte brut non typé (peut être {@code null})
     */
    public Object getContexteBrut() {
        return contexte;
    }
}
