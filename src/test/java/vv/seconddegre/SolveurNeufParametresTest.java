package vv.seconddegre;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import static org.junit.jupiter.api.Assertions.*;

import vv.seconddegre.exceptions.CoefficientNulException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * =====================================================================
 *  CLASSE DE TEST PRINCIPALE  —  SolveurNeufParametresTest
 * =====================================================================
 *
 * <h2>Objectif</h2>
 * Valider le solveur {@link SolveurSecondDegre} avec deux techniques
 * complémentaires de V&amp;V :
 * <ol>
 *   <li><b>Tests Pairwise (All-Pairs)</b> : réduction de l'espace
 *       combinatoire 9³ = 729 vers ~200 cas tout en couvrant
 *       <b>toutes les paires</b> de valeurs possibles.</li>
 *   <li><b>Tests métamorphiques (MR)</b> : au lieu de comparer à un
 *       oracle exact, on vérifie des <b>propriétés invariantes</b>
 *       du résultat (MR1 = primitive nulle, MR2 = relations de Viète).</li>
 * </ol>
 *
 * <h2>Structure</h2>
 * <pre>
 *   ├── NEUF_PARAMETRES_INT   9 valeurs « entières » (0, ±1, ±2, ±42, INT_MAX…)
 *   ├── NEUF_PARAMETRES_DBL   9 valeurs « flottantes » (±0, ±1e-5, ±1e8, 1e14…)
 *   ├── genererMatricePairwise()   algorithme All-Pairs
 *   ├── sourcePairwiseEntiers()    source JUnit pour le 1er @ParameterizedTest
 *   ├── sourcePairwiseDoubles()    source JUnit pour le 2e @ParameterizedTest
 *   └── executerProtocoleMetamorphique()   vérifications MR1 / MR2
 * </pre>
 *
 * <h2>Pourquoi 9 paramètres ?</h2>
 * Le sujet impose 9 valeurs par paramètre (3 coefficients × 9 = 729
 * combinaisons naïves). Le pairwise les réduit à ~200 cas <b>sans
 * perdre la couverture des paires</b>.
 */
class SolveurNeufParametresTest {

    /** Instance du SUT (System Under Test) partagée par tous les cas. */
    private final SolveurSecondDegre solveur = new SolveurSecondDegre();

    /**
     * Base de tolérance pour les comparaisons.
     * Les tolérances réelles sont <b>relatives</b> : EPSILON × échelle
     * des termes (voir {@link #executerProtocoleMetamorphique}).
     */
    private static final double EPSILON = 1e-9;

    // ────────────────────────────────────────────────────────────────
    //  JEUX DE DONNÉES (domaines d'entrée des 3 paramètres a, b, c)
    // ────────────────────────────────────────────────────────────────

    /**
     * 9 valeurs de type « entier » :
     * zéro, ±1, ±2, ±42 (valeurs métier), bornes de int (±2³¹).
     * Sert à tester les cas nominaux + le cas d'exception a = 0.
     */
    private static final double[] NEUF_PARAMETRES_INT = {
        0, 1, -1, 2, -2, 42, -42, 2147483647, -2147483648
    };

    /**
     * 9 valeurs de type « flottant » :
     * ±0.0 (négatif zéro !), ±1, petites (1e-5), grandes (1e8, 1e14).
     * Sert à stresser la précision numérique et les débordements.
     */
    private static final double[] NEUF_PARAMETRES_DBL = {
        0.0, -0.0, 1.0, -1.0, 1e-5, -1e-5, 1e8, -1e8, 1e14
    };

    // ────────────────────────────────────────────────────────────────
    //  SOURCES JUnit (fournissent les cas aux tests paramétrés)
    // ────────────────────────────────────────────────────────────────

    /**
     * Source de cas Pairwise sur le domaine « entiers ».
     * JUnit appelle cette méthode pour injecter chaque {@link QuadTestCase}.
     *
     * @return flux de cas de test générés par All-Pairs
     */
    static Stream<QuadTestCase> sourcePairwiseEntiers() {
        return genererMatricePairwise(
            NEUF_PARAMETRES_INT, NEUF_PARAMETRES_INT, NEUF_PARAMETRES_INT
        ).stream();
    }

    /**
     * Source de cas Pairwise sur le domaine « doubles ».
     *
     * @return flux de cas de test générés par All-Pairs
     */
    static Stream<QuadTestCase> sourcePairwiseDoubles() {
        return genererMatricePairwise(
            NEUF_PARAMETRES_DBL, NEUF_PARAMETRES_DBL, NEUF_PARAMETRES_DBL
        ).stream();
    }

    // ────────────────────────────────────────────────────────────────
    //  TESTS PARAMÉTRÉS (point d'entrée JUnit)
    // ────────────────────────────────────────────────────────────────

    /**
     * Test paramétré sur le domaine entier.
     * Chaque cas est un {@link QuadTestCase} → vérification métamorphique.
     *
     * @param cas un cas généré par All-Pairs (a, b, c [+ exception])
     */
    @ParameterizedTest(name = "[ENTIERS] Cas n°{index} -> {0}")
    @MethodSource("sourcePairwiseEntiers")
    void testerJeuDonneesEntiers(QuadTestCase cas) {
        executerProtocoleMetamorphique(cas);
    }

    /**
     * Test paramétré sur le domaine flottant.
     *
     * @param cas un cas généré par All-Pairs (a, b, c [+ exception])
     */
    @ParameterizedTest(name = "[DOUBLES] Cas n°{index} -> {0}")
    @MethodSource("sourcePairwiseDoubles")
    void testerJeuDonneesDoubles(QuadTestCase cas) {
        executerProtocoleMetamorphique(cas);
    }

    // ────────────────────────────────────────────────────────────────
    //  PROTOCOLE MÉTAMORPHIQUE (cœur des vérifications)
    // ────────────────────────────────────────────────────────────────

    /**
     * Applique le protocole de tests métamorphiques à un cas.
     *
     * <h3>Si exception attendue</h3>
     * On vérifie simplement que la bonne exception est levée
     * ({@code assertThrows}).
     *
     * <h3>Si cas nominal : deux relations métamorphiques</h3>
     *
     * <b>MR1 — Réinjection (oracle approximatif)</b> :
     * si x est racine, alors {@code P(x) = a·x² + b·x + c ≈ 0}.
     * On n'a pas besoin d'un oracle exact : la primitive elle-même
     * sert de vérificateur.
     *
     * <b>MR2 — Relations de Viète (reconstruction)</b> :
     * <ul>
     *   <li>2 racines : {@code x1+x2 = −b/a} et {@code x1·x2 = c/a}</li>
     *   <li>1 racine double : {@code x = −b/2a}</li>
     * </ul>
     * On reconstruit b et c à partir des racines : si la
     * reconstruction échoue, le solveur s'est trompé.
     *
     * <h3>Tolérances relatives</h3>
     * En virgule flottante, l'erreur absolue croît avec la magnitude
     * des termes. On utilise donc :
     * <pre>
     *   tol = EPSILON × max(1, |ax²| + |bx| + |c|)
     * </pre>
     * Cela absorbe l'erreur machine (~1e-16 relative) même pour des
     * coefficients de l'ordre de 1e14.
     *
     * @param cas cas de test à exécuter
     */
    private void executerProtocoleMetamorphique(QuadTestCase cas) {

        // ── Branche 1 : le cas attend une exception ─────────────────
        if (cas.expectsException()) {
            assertThrows(cas.getExpectedException(), () -> {
                solveur.resoudre(cas.getA(), cas.getB(), cas.getC());
            });
            return;  // rien d'autre à vérifier
        }

        // ── Branche 2 : cas nominal → calcul + MR1 + MR2 ───────────
        double[] racines = solveur.resoudre(cas.getA(), cas.getB(), cas.getC());

        // ==== MR1 : Réinjection de la primitive P(x) ≈ 0 ====
        // Tolérance proportionnelle à la somme des termes
        // (l'erreur machine est relative, pas absolue).
        for (double x : racines) {
            double termeAx2 = cas.getA() * (x * x);
            double termeBx  = cas.getB() * x;
            double image    = termeAx2 + termeBx + cas.getC();

            double toleranceMR1 = EPSILON * Math.max(1.0,
                    Math.abs(termeAx2) + Math.abs(termeBx) + Math.abs(cas.getC()));

            assertEquals(0.0, image, toleranceMR1,
                "Échec MR1 (Primitive) : x n'annule pas l'équation.");
        }

        // ==== MR2 : Reconstruction par les relations de Viète ====
        if (racines.length == 2) {
            // Deux racines distinctes : x1+x2 = −b/a, x1·x2 = c/a
            double x1 = racines[0];
            double x2 = racines[1];

            // Reconstruction de b :  b = −a·(x1 + x2)
            double bReconstruit = -cas.getA() * (x1 + x2);
            double toleranceMR2B = EPSILON * Math.max(1.0,
                    Math.abs(cas.getA() * x1) + Math.abs(cas.getA() * x2)
                    + Math.abs(cas.getB()));
            assertEquals(cas.getB(), bReconstruit, toleranceMR2B,
                "Échec MR2 : Impossible de reconstruire 'b'.");

            // Reconstruction de c :  c = a·(x1 · x2)
            double cReconstruit = cas.getA() * (x1 * x2);
            double toleranceMR2C = EPSILON * Math.max(1.0,
                    Math.abs(cas.getA() * x1 * x2) + Math.abs(cas.getC()));
            assertEquals(cas.getC(), cReconstruit, toleranceMR2C,
                "Échec MR2 : Impossible de reconstruire 'c'.");

        } else if (racines.length == 1) {
            // Racine unique (delta = 0) : x = −b/2a
            double xUnique = racines[0];

            double bReconstruit = -cas.getA() * (2.0 * xUnique);
            double toleranceMR2B = EPSILON * Math.max(1.0,
                    2.0 * Math.abs(cas.getA() * xUnique) + Math.abs(cas.getB()));
            assertEquals(cas.getB(), bReconstruit, toleranceMR2B,
                "Échec MR2 : Reconstruction 'b' échouée.");

            double cReconstruit = cas.getA() * (xUnique * xUnique);
            double toleranceMR2C = EPSILON * Math.max(1.0,
                    Math.abs(cas.getA() * xUnique * xUnique) + Math.abs(cas.getC()));
            assertEquals(cas.getC(), cReconstruit, toleranceMR2C,
                "Échec MR2 : Reconstruction 'c' échouée.");
        }
        // racines.length == 0 (delta < 0) : rien à reconstruire,
        // MR1 n'a pas de racine à réinjecter → cas triviallement OK.
    }

    // ────────────────────────────────────────────────────────────────
    //  ALGORITHME PAIRWISE / ALL-PAIRS
    // ────────────────────────────────────────────────────────────────

    /**
     * Génère une matrice de cas par l'algorithme <b>All-Pairs</b>
     * (réduction Pairwise).
     *
     * <h2>Principe</h2>
     * Plutôt que de tester les 9×9×9 = 729 combinaisons, on ne garde
     * que les combinaisons qui couvrent au moins une <b>paire</b>
     * (a,b), (a,c) ou (b,c) pas encore vue.
     *
     * <h2>Algorithme (greedy)</h2>
     * <pre>
     *   pour chaque (a, b, c) dans l'espace complet :
     *       si la paire AB ou AC ou BC est nouvelle :
     *           marquer les 3 paires comme couvertes
     *           ajouter le cas à la liste
     * </pre>
     * Complexité : O(9³) parcours, mais ~200 cas retenus
     * (au lieu de 729) avec couverture paire complète.
     *
     * <h2>Cas d'exception intégrés</h2>
     * Si {@code a == 0}, le cas est marqué avec
     * {@link CoefficientNulException} comme exception attendue.
     *
     * @param valeursA valeurs possibles pour a
     * @param valeursB valeurs possibles pour b
     * @param valeursC valeurs possibles pour c
     * @return liste réduite des cas de test Pairwise
     */
    private static List<QuadTestCase> genererMatricePairwise(
            double[] valeursA, double[] valeursB, double[] valeursC) {

        // Cas retenus par l'algorithme greedy
        List<QuadTestCase> combinaisonsRetenues = new ArrayList<>();

        // Ensemble des paires déjà couvertes : "AB:v1_v2", "AC:…", "BC:…"
        Set<String> pairesCouvertes = new HashSet<>();

        // ── Parcours exhaustif de l'espace 9×9×9 ────────────────────
        for (double a : valeursA) {
            for (double b : valeursB) {
                for (double c : valeursC) {

                    // Clés identifiant les 3 paires du triplet courant
                    String paireAB = "AB:" + a + "_" + b;
                    String paireAC = "AC:" + a + "_" + c;
                    String paireBC = "BC:" + b + "_" + c;

                    // Au moins une paire est inédite → ce triplet apporte
                    // de la couverture → on le retient.
                    boolean estNouveau =
                            !pairesCouvertes.contains(paireAB) ||
                            !pairesCouvertes.contains(paireAC) ||
                            !pairesCouvertes.contains(paireBC);

                    // Toujours prendre le tout premier triplet (amorçage).
                    if (combinaisonsRetenues.isEmpty() || estNouveau) {

                        // Marquer les 3 paires comme couvertes
                        pairesCouvertes.add(paireAB);
                        pairesCouvertes.add(paireAC);
                        pairesCouvertes.add(paireBC);

                        // a = 0 → cas d'exception attendue
                        if (a == 0.0) {
                            combinaisonsRetenues.add(
                                new QuadTestCase(a, b, c, CoefficientNulException.class));
                        } else {
                            combinaisonsRetenues.add(
                                new QuadTestCase(a, b, c));
                        }
                    }
                }
            }
        }
        return combinaisonsRetenues;
    }
}
