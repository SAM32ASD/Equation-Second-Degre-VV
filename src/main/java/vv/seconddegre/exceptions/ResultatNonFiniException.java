package vv.seconddegre.exceptions;

/**
 * =====================================================================
 *  EXCEPTION : RACINE NON FINIE  —  ResultatNonFiniException
 * =====================================================================
 *
 * <h2>Cas d'usage</h2>
 * Levée en <b>contrôle qualité final</b> du solveur : après le calcul,
 * chaque racine est vérifiée. Si l'une d'elles vaut {@code NaN} ou
 * {@code ±Infinité}, c'est qu'un bug s'est produit en interne
 * (division par zéro masquée, overflow…). On préfère échouer fort
 * plutôt que de renvoyer un résultat corrompu au client.
 *
 * <h2>Contexte transporté</h2>
 * <ul>
 *   <li>Champ typé {@link #getRacines()} : copie du tableau des racines</li>
 *   <li>Via la base : {@code getContexte(double[].class)}</li>
 * </ul>
 *
 * <h2>Immuabilité</h2>
 * Le tableau est <b>cloné</b> à la construction et à l'accès
 * ({@code defensive copying}) pour éviter qu'un appelant ne modifie
 * l'état de l'exception après coup.
 */
public class ResultatNonFiniException extends EquationException {

    /** Identifiant de sérialisation (standard Java). */
    private static final long serialVersionUID = 1L;

    /** Copie des racines calculées (toujours non null). */
    private final double[] racines;

    /**
     * @param racines tableau des racines contenant au moins une valeur
     *                NaN ou Infinité (peut être {@code null})
     */
    public ResultatNonFiniException(double[] racines) {
        super(
            "Le calcul a produit une racine non finie (NaN ou Infinité).",
            racines == null ? new double[0] : racines.clone()
        );
        // Copie défensive : le tableau interne n'est jamais partagé.
        this.racines = racines == null ? new double[0] : racines.clone();
    }

    /**
     * @return une copie fraîche des racines calculées
     */
    public double[] getRacines() {
        return racines.clone();
    }
}
