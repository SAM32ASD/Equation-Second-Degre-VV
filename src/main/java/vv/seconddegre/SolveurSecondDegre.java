package vv.seconddegre;

import vv.seconddegre.exceptions.CoefficientNulException;
import vv.seconddegre.exceptions.DeltaInvalideException;
import vv.seconddegre.exceptions.ResultatNonFiniException;

/**
 * =====================================================================
 *  SOLVEUR D'ÉQUATION DU SECOND DEGRÉ  —  ax² + bx + c = 0
 * =====================================================================
 *
 * <h2>1. Rôle de la classe</h2>
 * Cette classe est le <b>SUT</b> (System Under Test) du projet de V&V :
 * elle calcule les racines réelles d'une équation du second degré à
 * partir des trois coefficients a, b et c.
 *
 * <h2>2. Théorie</h2>
 * Le discriminant est {@code delta = b² − 4ac}. Selon sa valeur :
 * <ul>
 *   <li>{@code delta < 0}  → aucune racine réelle (tableau vide)</li>
 *   <li>{@code delta = 0}  → une racine double : {@code x = −b / 2a}</li>
 *   <li>{@code delta > 0}  → deux racines réelles distinctes</li>
 * </ul>
 *
 * <h2>3. Choix d'implémentation : formule numeriquement stable</h2>
 * La formule classique {@code x = (−b ± √delta) / 2a} souffre de
 * <b>l'élimination catastrophique</b> : lorsque {@code b² ≫ 4ac},
 * la soustraction {@code −b − √delta} annule les chiffres significatifs
 * et dégrade la racine petite (cf. Séance 2, p. 44).
 *
 * On utilise donc la <b>forme de citard / algorithme de Kahan</b> :
 * <pre>
 *   q = −½ (b + sign(b)·√delta)     // même signe que b → pas de cancellation
 *   x1 = q / a
 *   x2 = c / q
 * </pre>
 * Cela garantit que l'opération de soustraction porte toujours sur deux
 * nombres de même signe, donc pas d'annulation de chiffres.
 *
 * <h2>4. Cas d'exception (bornes du domaine)</h2>
 * <ul>
 *   <li>{@code a = 0}         → {@link CoefficientNulException}</li>
 *   <li>{@code delta = NaN/Inf} → {@link DeltaInvalideException}</li>
 *   <li>racine = NaN/Inf       → {@link ResultatNonFiniException}</li>
 * </ul>
 *
 * @author Projet V&amp;V
 * @version 2.0
 */
public class SolveurSecondDegre implements ISolveurSecondDegre {

    /**
     * Résout l'équation du second degré {@code a·x² + b·x + c = 0}.
     *
     * <h3>Étapes de l'algorithme</h3>
     * <ol>
     *   <li><b>Validation</b> : a ≠ 0 sinon exception</li>
     *   <li><b>Discriminant</b> : delta = b² − 4ac, contrôlé NaN/Inf</li>
     *   <li><b>Branchement</b> selon le signe de delta</li>
     *   <li><b>Formule stable</b> si delta > 0 (q, x1, x2)</li>
     *   <li><b>Contrôle final</b> : les racines doivent être finies</li>
     * </ol>
     *
     * @param a coefficient de x² — ne doit pas être nul
     * @param b coefficient de x
     * @param c terme constant
     * @return tableau des racines réelles, trié croissant ;
     *         longueur 0 (delta &lt; 0), 1 (delta = 0) ou 2 (delta &gt; 0)
     * @throws CoefficientNulException   si a vaut 0.0
     * @throws DeltaInvalideException    si delta est NaN ou Infinité
     * @throws ResultatNonFiniException  si une racine calculée est NaN ou Inf
     */
    @Override
    public double[] resoudre(double a, double b, double c) {

        // ── Étape 1 : validation du domaine ──────────────────────────
        // a = 0 dégénère en équation du 1er degré → hors domaine.
        if (a == 0.0) {
            throw new CoefficientNulException(a);
        }

        // ── Étape 2 : calcul du discriminant ─────────────────────────
        // On garde le calcul littéral (pas de Math.fma) pour rester
        // lisible et comparer avec les valeurs de référence du cours.
        double delta = (b * b) - (4.0 * a * c);

        // Protection : débordement de plage (ex. b = 1e200 → b² = Inf).
        if (Double.isNaN(delta) || Double.isInfinite(delta)) {
            throw new DeltaInvalideException(delta);
        }

        // ── Étape 3 : branchement selon le signe de delta ────────────
        double[] racines;

        if (delta < 0.0) {
            // Cas 1 : pas de racine réelle → tableau vide.
            racines = new double[0];

        } else if (delta == 0.0) {
            // Cas 2 : racine unique (double) : x = −b / 2a.
            racines = new double[]{ -b / (2.0 * a) };

        } else {
            // Cas 3 : deux racines distinctes — FORMULE STABLE.
            //
            // q = −½ (b + sign(b)·√delta)
            //   · si b ≥ 0 : q = −½ (b + √delta)  → q ≤ 0, même signe que b
            //   · si b < 0 : q = −½ (b − √delta)  → q ≥ 0, même signe que b
            // Dans les deux cas, (b + sign(b)·√delta) est une somme de
            // nombres de même signe : AUCUNE élimination catastrophique.
            double q;
            if (b >= 0.0) {
                q = -0.5 * (b + Math.sqrt(delta));
            } else {
                q = -0.5 * (b - Math.sqrt(delta));
            }

            // NOTE : q != 0 est garanti ici car delta > 0 et a != 0
            // (si b>=0 : b+sqrt(delta) > 0 ; si b<0 : b-sqrt(delta) < 0).
            // La branche de protection q==0 était du code mort → supprimée
            // pour atteindre 100% de branches couvertes (JaCoCo).

            // Viète : x1 · x2 = c/a  et  x1 + x2 = −b/a
            //   → x1 = q/a,  x2 = c/q  (produit exact = c/a)
            double x1 = q / a;
            double x2 = c / q;
            // Tri croissant pour un résultat déterministe
            // (indispensable aux tests métamorphiques).
            racines = new double[]{ Math.min(x1, x2), Math.max(x1, x2) };
        }

        // ── Étape 4 : contrôle de finitude du résultat ───────────────
        // Un résultat Inf signalerait un débordement silencieux.
        // NOTE : NaN est impossible ici car delta est déjà contrôlé fini
        // et a ≠ 0 → toutes les divisions portent sur des finis non nuls
        // au dénominateur (q ≠ 0 prouvé plus haut). On ne teste donc
        // que isInfinite() — couverture 100% des branches (JaCoCo).
        for (double x : racines) {
            if (Double.isInfinite(x)) {
                throw new ResultatNonFiniException(racines);
            }
        }

        return racines;
    }
}
