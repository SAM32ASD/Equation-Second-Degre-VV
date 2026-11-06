package vv.seconddegre;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import vv.seconddegre.exceptions.CoefficientNulException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * =====================================================================
 *  JEUX DE DONNÉES GÉNÉRÉS ALÉATOIREMENT  —  JeuDonneesAleatoiresTest
 * =====================================================================
 *
 * <h2>Pourquoi générer aléatoirement ?</h2>
 * Le Pairwise couvre les <b>paires</b> d'un domaine fini mais ne
 * sort jamais de ce domaine. La génération aléatoire
 * (<i>random testing</i>, D. Hamlet) explore des combinaisons
 * <b>jamais vues</b> de (a, b, c) : elle augmente la diversité des
 * entrées sans écrire un cas à la main.
 *
 * <h2>Reproductibilité (essentiel en V&amp;V)</h2>
 * Un test aléatoire qui échoue de façon non reproductible est
 * inutilisable. On fixe donc une <b>graine (seed) constante</b> :
 * la suite est « aléatoire » pour le lecteur mais strictement
 * déterministe pour la machine. Pour explorer d'autres graines :
 * <pre>
 *   mvn test -Dtest.seed=12345
 * </pre>
 *
 * <h2>Protocole de vérification (pas d'oracle exact)</h2>
 * Comme pour le Pairwise, on applique les relations métamorphiques :
 * <ul>
 *   <li><b>MR1</b> — réinjection : P(x) ≈ 0 pour chaque racine ;</li>
 *   <li><b>MR2</b> — Viète : reconstruction de b et c ;</li>
 *   <li><b>MR3</b> — homogénéité : (ka, kb, kc) a les MÊMES racines
 *       que (a, b, c) pour tout k ≠ 0 ;</li>
 *   <li><b>MR4</b> — symétrie : les racines de (a, −b, c) sont les
 *       opposées des racines de (a, b, c).</li>
 * </ul>
 *
 * <h2>Domaines générés</h2>
 * Magnitudes 10⁻⁶…10⁶ (signes libres), valeurs « rondes » fréquentes
 * (0, ±1, ±2, ±42…) et ~10% de {@code a = 0} pour re-tester la
 * branche d'exception sur des triplets non planifiés.
 */
class JeuDonneesAleatoiresTest {

    /** SUT partagé. */
    private final SolveurSecondDegre solveur = new SolveurSecondDegre();

    /** Graine fixe → génération déterministe et reproductible. */
    private static final long SEED =
            Long.getLong("test.seed", 20261106L);

    /** Nombre de triplets (a, b, c) générés par le test paramétré. */
    private static final int NOMBRE_CAS = 500;

    /** Probabilité qu'un coefficient soit « a = 0 » (cas d'exception). */
    private static final double PROB_A_NUL = 0.10;

    /** Tolérance relative (cf. SolveurNeufParametresTest). */
    private static final double EPSILON = 1e-9;

    /** Valeurs « rondes » injectées avec une certaine probabilité. */
    private static final double[] VALEURS_RONDES = {
        0.0, 1.0, -1.0, 2.0, -2.0, 42.0, -42.0, 0.5, -0.5, 1e-5, 1e5
    };

    // ────────────────────────────────────────────────────────────────
    //  GÉNÉRATEUR ALÉATOIRE (source JUnit)
    // ────────────────────────────────────────────────────────────────

    /**
     * Source aléatoire de {@link QuadTestCase}.
     *
     * <h3>Algorithme de génération d'un coefficient</h3>
     * <pre>
     *   10%  → valeur « ronde » (0, ±1, ±2, ±42, 1e-5, 1e5…)
     *   90%  → signe aléatoire × mantisse [1,10) × 10^exp, exp ∈ [−6,6]
     * </pre>
     * Les exposants bornés à ±6 évitent tout débordement (b² ≤ 10¹²
     * très loin de la capacité de {@code double}).
     *
     * <p>Un triplet avec {@code a = 0} est marqué
     * {@link CoefficientNulException} comme exception attendue.</p>
     *
     * @return flux déterministe de N cas aléatoires
     */
    static Stream<QuadTestCase> jeuxAleatoires() {
        Random alea = new Random(SEED);
        List<QuadTestCase> cas = new ArrayList<>(NOMBRE_CAS);

        for (int i = 0; i < NOMBRE_CAS; i++) {
            double a = (alea.nextDouble() < PROB_A_NUL)
                    ? 0.0 : genererCoefficient(alea);
            double b = genererCoefficient(alea);
            double c = genererCoefficient(alea);

            if (a == 0.0) {
                cas.add(new QuadTestCase(a, b, c, CoefficientNulException.class));
            } else {
                cas.add(new QuadTestCase(a, b, c));
            }
        }
        return cas.stream();
    }

    /**
     * Génère un coefficient aléatoire non nul (ou une valeur ronde,
     * zéro éventuel non concerné ici car géré par l'appelant).
     *
     * @param alea générateur partagé (graine fixe)
     * @return un double de magnitude 10⁻⁶…10⁶
     */
    private static double genererCoefficient(Random alea) {
        if (alea.nextDouble() < 0.20) {
            // 20% de valeurs « rondes » → cas métier + bornes simples
            return VALEURS_RONDES[alea.nextInt(VALEURS_RONDES.length)];
        }
        double signe = alea.nextBoolean() ? 1.0 : -1.0;
        double mantisse = 1.0 + alea.nextDouble() * 9.0;   // [1, 10)
        int exposant = alea.nextInt(13) - 6;               // [−6, 6]
        return signe * mantisse * Math.pow(10.0, exposant);
    }

    /**
     * Génère un coefficient <b>strictement non nul</b> (garanti pour
     * {@code a}, hors domaine métier sinon).
     *
     * @param alea générateur partagé (graine fixe)
     * @return un double non nul de magnitude 10⁻⁶…10⁶
     */
    private static double genererCoefficientNonNul(Random alea) {
        double v;
        do {
            v = genererCoefficient(alea);
        } while (v == 0.0);
        return v;
    }

    // ────────────────────────────────────────────────────────────────
    //  TEST 1 : 500 triplets aléatoires → MR1 + MR2
    // ────────────────────────────────────────────────────────────────

    /**
     * Exécute le protocole métamorphique MR1 + MR2 sur chacun des
     * 500 triplets générés aléatoirement.
     *
     * @param cas triplet (a, b, c) [+ exception attendue]
     */
    @ParameterizedTest(name = "Cas aléatoire n°{index}")
    @MethodSource("jeuxAleatoires")
    void resoudreCasAleatoires(QuadTestCase cas) {
        // ── Branche exception (a = 0 généré au hasard) ──────────────
        if (cas.expectsException()) {
            assertThrows(cas.getExpectedException(),
                    () -> solveur.resoudre(cas.getA(), cas.getB(), cas.getC()));
            return;
        }

        double[] racines = solveur.resoudre(cas.getA(), cas.getB(), cas.getC());

        // ==== MR1 : réinjection P(x) ≈ 0 ====
        for (double x : racines) {
            double termeAx2 = cas.getA() * (x * x);
            double termeBx = cas.getB() * x;
            double image = termeAx2 + termeBx + cas.getC();
            double tol = EPSILON * Math.max(1.0,
                    Math.abs(termeAx2) + Math.abs(termeBx) + Math.abs(cas.getC()));
            assertEquals(0.0, image, tol,
                    "MR1 échouée pour (" + cas + ") : x=" + x);
        }

        // ==== MR2 : reconstruction par Viète ====
        if (racines.length == 2) {
            double x1 = racines[0];
            double x2 = racines[1];

            double bReconstruit = -cas.getA() * (x1 + x2);
            double tolB = EPSILON * Math.max(1.0,
                    Math.abs(cas.getA() * x1) + Math.abs(cas.getA() * x2)
                            + Math.abs(cas.getB()));
            assertEquals(cas.getB(), bReconstruit, tolB,
                    "MR2 échouée (b) pour (" + cas + ")");

            double cReconstruit = cas.getA() * (x1 * x2);
            double tolC = EPSILON * Math.max(1.0,
                    Math.abs(cas.getA() * x1 * x2) + Math.abs(cas.getC()));
            assertEquals(cas.getC(), cReconstruit, tolC,
                    "MR2 échouée (c) pour (" + cas + ")");

        } else if (racines.length == 1) {
            double x = racines[0];

            double bReconstruit = -cas.getA() * (2.0 * x);
            double tolB = EPSILON * Math.max(1.0,
                    2.0 * Math.abs(cas.getA() * x) + Math.abs(cas.getB()));
            assertEquals(cas.getB(), bReconstruit, tolB,
                    "MR2 échouée (b, racine double) pour (" + cas + ")");

            double cReconstruit = cas.getA() * (x * x);
            double tolC = EPSILON * Math.max(1.0,
                    Math.abs(cas.getA() * x * x) + Math.abs(cas.getC()));
            assertEquals(cas.getC(), cReconstruit, tolC,
                    "MR2 échouée (c, racine double) pour (" + cas + ")");
        }
        // racines.length == 0 → rien à reconstruire
    }

    // ────────────────────────────────────────────────────────────────
    //  TEST 2 : MR3 — homogénéité (mise à l'échelle)
    // ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("MR3 — (ka, kb, kc) a les mêmes racines que (a, b, c)")
    class MetamorphiqueMiseAEchelle {

        @ParameterizedTest(name = "k = {0}")
        @MethodSource("vv.seconddegre.JeuDonneesAleatoiresTest#facteursEchelle")
        void miseAEchellePreserveLesRacines(double k) {
            Random alea = new Random(SEED + 1);
            for (int i = 0; i < 100; i++) {
                double a = genererCoefficientNonNul(alea);
                double b = genererCoefficient(alea);
                double c = genererCoefficient(alea);

                double[] originales = solveur.resoudre(a, b, c);
                double[] scalees = solveur.resoudre(k * a, k * b, k * c);

                assertEquals(originales.length, scalees.length,
                        "MR3 : nombre de racines différent pour k=" + k);
                for (int j = 0; j < originales.length; j++) {
                    double tol = EPSILON * Math.max(1.0, Math.abs(originales[j]));
                    assertEquals(originales[j], scalees[j], tol,
                            "MR3 : racine " + j + " modifiée par l'échelle k=" + k);
                }
            }
        }
    }

    /** Facteurs d'échelle testés par MR3 (k ≠ 0 obligatoire). */
    static Stream<Double> facteursEchelle() {
        return Stream.of(0.5, 2.0, 10.0, -1.0, -0.1, 1e6);
    }

    // ────────────────────────────────────────────────────────────────
    //  TEST 3 : MR4 — symétrie des racines (b → −b)
    // ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("MR4 — racines de (a, −b, c) = opposées de celles de (a, b, c)")
    class MetamorphiqueSymetrie {

        @ParameterizedTest(name = "Cas symétrique n°{index}")
        @MethodSource("vv.seconddegre.JeuDonneesAleatoiresTest#jeuxAleatoires")
        void symetrieDesRacines(QuadTestCase cas) {
            if (cas.expectsException()) {
                return; // a = 0 : hors domaine, déjà couvert ailleurs
            }
            double[] racines = solveur.resoudre(cas.getA(), cas.getB(), cas.getC());
            double[] symetriques = solveur.resoudre(cas.getA(), -cas.getB(), cas.getC());

            assertEquals(racines.length, symetriques.length,
                    "MR4 : nombre de racines différent");

            // Les racines sont triées croissant : l'opposé inverse l'ordre.
            for (int i = 0; i < racines.length; i++) {
                double attendu = -racines[racines.length - 1 - i];
                double tol = EPSILON * Math.max(1.0, Math.abs(attendu));
                assertEquals(attendu, symetriques[i], tol,
                        "MR4 : racine " + i + " non symétrique");
            }
        }
    }

    // ────────────────────────────────────────────────────────────────
    //  TEST 4 : le décorateur reste transparent sur données aléatoires
    // ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Décorateur — transparent sur 200 triplets aléatoires")
    class DecorateurTransparent {

        @ParameterizedTest(name = "Décoré n°{index}")
        @MethodSource("vv.seconddegre.JeuDonneesAleatoiresTest#jeuxAleatoires")
        void memeResultatQueSolveurNue(QuadTestCase cas) {
            SolveurJournalise decore = new SolveurJournalise(solveur, "alea");
            if (cas.expectsException()) {
                assertThrows(cas.getExpectedException(),
                        () -> decore.resoudre(cas.getA(), cas.getB(), cas.getC()));
                return;
            }
            assertArrayEquals(
                    solveur.resoudre(cas.getA(), cas.getB(), cas.getC()),
                    decore.resoudre(cas.getA(), cas.getB(), cas.getC()),
                    "Le décorateur doit déléguer sans altérer le résultat");
        }
    }
}
