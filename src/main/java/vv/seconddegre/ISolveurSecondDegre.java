package vv.seconddegre;

/**
 * =====================================================================
 *  CONTRAT DU SOLVEUR  —  ISolveurSecondDegre
 * =====================================================================
 *
 * <h2>Rôle</h2>
 * Interface <b>portant le service</b> « résoudre une équation du
 * second degré ». Elle découple :
 * <ul>
 *   <li>le <b>client</b> (tests, décorateurs, futur UI) qui dépend
 *       du contrat, non de l'implémentation ;</li>
 *   <li>l'<b>implémentation</b> {@link SolveurSecondDegre} (SUT) qui
 *       fournit la formule stable de Kahan.</li>
 * </ul>
 *
 * <h2>Pourquoi une interface ?</h2>
 * <ol>
 *   <li><b>Polymorphisme</b> : on peut substituer un décorateur
 *       ({@code SolveurJournalise}) ou un mock de test sans toucher
 *       aux clients.</li>
 *   <li><b>Découplage / DIP</b> : le client dépend de l'abstraction
 *       (Pattern Dependency Inversion).</li>
 *   <li><b>Évolutivité</b> : une 2ᵉ implémentation (ex. solveur
 *       symbolique, solveur approché) s'ajoute sans modifier le
 *       client.</li>
 * </ol>
 *
 * <h2>Hiérarchie</h2>
 * <pre>
 *   ISolveurSecondDegre (interface)
 *   ├── SolveurSecondDegre   — implémentation « réelle » (SUT)
 *   └── SolveurJournalise    — décorateur (journal + compteurs)
 * </pre>
 *
 * @author Projet V&amp;V
 * @version 1.0
 */
public interface ISolveurSecondDegre {

    /**
     * Résout l'équation du second degré {@code a·x² + b·x + c = 0}.
     *
     * <h3>Contrat de pré-condition</h3>
     * {@code a} ne doit pas valoir {@code 0.0} (sinon l'équation
     * dégénère en 1ʳᵉ degré — hors domaine).
     *
     * <h3>Contrat de post-condition</h3>
     * Le tableau renvoyé est <b>trié croissant</b> et sa longueur
     * reflète le discriminant :
     * <ul>
     *   <li>{@code 0} si {@code delta < 0} (aucune racine réelle) ;</li>
     *   <li>{@code 1} si {@code delta = 0} (racine double) ;</li>
     *   <li>{@code 2} si {@code delta > 0} (deux racines distinctes).</li>
     * </ul>
     *
     * @param a coefficient de x² — ne doit pas être nul
     * @param b coefficient de x
     * @param c terme constant
     * @return tableau des racines réelles triées croissant
     * @throws vv.seconddegre.exceptions.CoefficientNulException  si a vaut 0.0
     * @throws vv.seconddegre.exceptions.DeltaInvalideException   si delta est NaN/Inf
     * @throws vv.seconddegre.exceptions.ResultatNonFiniException si une racine est Inf
     */
    double[] resoudre(double a, double b, double c);
}
