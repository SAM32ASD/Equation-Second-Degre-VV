package vv.seconddegre;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import vv.seconddegre.exceptions.CoefficientNulException;
import vv.seconddegre.exceptions.DeltaInvalideException;
import vv.seconddegre.exceptions.EquationException;
import vv.seconddegre.exceptions.ResultatNonFiniException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * =====================================================================
 *  TESTS COMPLÉMENTAIRES  —  ExceptionEquationTest
 * =====================================================================
 *
 * <h2>Objectif couverture 100% (JaCoCo)</h2>
 * Les tests Pairwise + métamorphiques couvrent le nominal, mais
 * laissent non couvertes :
 * <ul>
 *   <li>les <b>constructeurs et accesseurs</b> des exceptions</li>
 *   <li>la branche <b>delta = NaN/Inf</b> du solveur</li>
 *   <li>la branche <b>racine non finie</b> du solveur</li>
 *   <li>les surcharges {@link EquationException} (message seul, cause)</li>
 * </ul>
 * Cette classe comble ces trous pour atteindre 100% des branches.
 */
class ExceptionEquationTest {

    private final SolveurSecondDegre solveur = new SolveurSecondDegre();

    // ────────────────────────────────────────────────────────────────
    //  NIVEAU 1 : EquationException (base)
    // ────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("EquationException — base générique")
    class EquationExceptionTests {

        @Test
        @DisplayName("Constructeur message seul : contexte null")
        void constructeurMessageSeul() {
            EquationException ex = new EquationException("erreur simple");
            assertEquals("erreur simple", ex.getMessage());
            assertNull(ex.getContexteBrut());
            assertNull(ex.getContexte(Double.class));
        }

        @Test
        @DisplayName("Constructeur message + contexte typé")
        void constructeurMessageContexte() {
            EquationException ex = new EquationException("ctx", 42.0);
            assertEquals(42.0, ex.getContexte(Double.class));
            assertEquals(42.0, ex.getContexteBrut());
        }

        @Test
        @DisplayName("Constructeur message + contexte + cause enchaînée")
        void constructeurAvecCause() {
            Throwable cause = new IllegalStateException("origine");
            EquationException ex = new EquationException("avec cause", "payload", cause);
            assertSame(cause, ex.getCause());
            assertEquals("payload", ex.getContexte(String.class));
        }

        @Test
        @DisplayName("getContexte avec mauvais type lève ClassCastException")
        void getContexteMauvaisType() {
            EquationException ex = new EquationException("m", 1.0);
            assertThrows(ClassCastException.class, () -> ex.getContexte(String.class));
        }
    }

    // ────────────────────────────────────────────────────────────────
    //  NIVEAU 2 : CoefficientNulException
    // ────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("CoefficientNulException — a = 0")
    class CoefficientNulExceptionTests {

        @Test
        @DisplayName("Levée par le solveur quand a = 0")
        void leveeParSolveur() {
            CoefficientNulException ex = assertThrows(
                CoefficientNulException.class,
                () -> solveur.resoudre(0.0, 1.0, 2.0)
            );
            assertEquals(0.0, ex.getCoefficient());
            assertEquals(0.0, ex.getContexte(Double.class));
            assertTrue(ex.getMessage().contains("nul"));
        }

        @Test
        @DisplayName("Constructeur direct + getCoefficient()")
        void constructeurDirect() {
            CoefficientNulException ex = new CoefficientNulException(0.0);
            assertEquals(0.0, ex.getCoefficient());
        }
    }

    // ────────────────────────────────────────────────────────────────
    //  NIVEAU 3 : DeltaInvalideException
    // ────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("DeltaInvalideException — delta = NaN / Inf")
    class DeltaInvalideExceptionTests {

        @Test
        @DisplayName("Delta = NaN (Inf − Inf) : b² et 4ac débordent")
        void deltaNaN() {
            // b² = Inf, 4ac = Inf  →  Inf − Inf = NaN
            DeltaInvalideException ex = assertThrows(
                DeltaInvalideException.class,
                () -> solveur.resoudre(1e200, 1e200, 1e200)
            );
            assertTrue(Double.isNaN(ex.getDelta()));
            assertTrue(Double.isNaN((Double) ex.getContexte(Double.class)));
            assertTrue(ex.getMessage().contains("finie"));
        }

        @Test
        @DisplayName("Delta = +Inf : b² déborde seul")
        void deltaInfiniPositif() {
            // b² = Inf, 4ac fini petit → delta = +Inf
            DeltaInvalideException ex = assertThrows(
                DeltaInvalideException.class,
                () -> solveur.resoudre(1.0, 1e300, 1.0)
            );
            assertTrue(Double.isInfinite(ex.getDelta()));
            assertTrue(ex.getDelta() > 0);
        }

        @Test
        @DisplayName("Constructeur direct + getDelta()")
        void constructeurDirect() {
            DeltaInvalideException ex = new DeltaInvalideException(Double.NaN);
            assertTrue(Double.isNaN(ex.getDelta()));
        }
    }

    // ────────────────────────────────────────────────────────────────
    //  NIVEAU 4 : ResultatNonFiniException
    // ────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("ResultatNonFiniException — racine NaN / Inf")
    class ResultatNonFiniExceptionTests {

        @Test
        @DisplayName("Racine = Inf : a très petit, |x1| = |q/a| déborde")
        void racineInfinie() {
            // delta = 1 fini, mais q/a = −1 / 1e-320 = −Inf
            ResultatNonFiniException ex = assertThrows(
                ResultatNonFiniException.class,
                () -> solveur.resoudre(1e-320, 1.0, 0.0)
            );
            boolean contientInf = false;
            for (double x : ex.getRacines()) {
                if (Double.isInfinite(x) || Double.isNaN(x)) {
                    contientInf = true;
                }
            }
            assertTrue(contientInf, "au moins une racine doit être non finie");
            assertTrue(ex.getMessage().contains("finie"));
        }

        @Test
        @DisplayName("Constructeur avec tableau null : contexte = tableau vide")
        void constructeurNull() {
            ResultatNonFiniException ex = new ResultatNonFiniException(null);
            assertEquals(0, ex.getRacines().length);
            assertEquals(0, ((double[]) ex.getContexte(double[].class)).length);
        }

        @Test
        @DisplayName("Constructeur avec tableau : copie défensive")
        void constructeurAvecTableau() {
            double[] source = {1.0, Double.NaN};
            ResultatNonFiniException ex = new ResultatNonFiniException(source);
            double[] copie = ex.getRacines();
            assertArrayEquals(source, copie);
            // Muter la copie ne touche pas l'exception
            copie[0] = 99.0;
            assertEquals(1.0, ex.getRacines()[0]);
        }

        @Test
        @DisplayName("getContexte(double[].class) renvoie le tableau stocké")
        void contexteTypé() {
            double[] source = {Double.POSITIVE_INFINITY};
            ResultatNonFiniException ex = new ResultatNonFiniException(source);
            assertArrayEquals(source, ex.getContexte(double[].class));
        }
    }
}
