package vv.seconddegre;

import java.util.Objects;

/**
 * =====================================================================
 *  CONTENEUR DE CAS DE TEST  —  QuadTestCase
 * =====================================================================
 *
 * <h2>Rôle</h2>
 * Objet <b>immutable</b> qui encapsule un cas de test généré par
 * l'algorithme Pairwise : les trois coefficients (a, b, c) plus
 * l'exception attendue le cas échéant.
 *
 * <h2>Pourquoi un conteneur dédié ?</h2>
 * Les tests paramétrés JUnit 5 (@ParameterizedTest) n'acceptent
 * qu'<b>un seul paramètre</b> par cas. Plutôt de passer 3 doubles
 * (et perdre l'info « exception attendue »), on passe un objet
 * unique qui porte toutes les données.
 *
 * <h2>Double rôle selon le cas</h2>
 * <ul>
 *   <li><b>Cas nominal</b> : {@code expectedException == null}
 *       → on vérifie les racines (MR1 + MR2)</li>
 *   <li><b>Cas d'exception</b> : {@code expectedException != null}
 *       → on vérifie que la bonne exception est levée
 *       (ex. {@code CoefficientNulException} si a = 0)</li>
 * </ul>
 *
 * <h2>toString()</h2>
 * Surchargé pour s'afficher proprement dans les noms de tests JUnit :
 * <pre>
 *   Equation: [42.0x² + 1.0x + -2.0 = 0] -> Nominal
 *   Equation: [0.0x² + 1.0x + 2.0 = 0] -> Attend: CoefficientNulException
 * </pre>
 */
public class QuadTestCase {

    /** Coefficient de x². */
    private final double a;

    /** Coefficient de x. */
    private final double b;

    /** Terme constant. */
    private final double c;

    /**
     * Classe d'exception attendue, ou {@code null} si le cas est nominal.
     * Type {@code Class<? extends Throwable>} pour accepter n'importe
     * quelle exception (sous-classe de Throwable).
     */
    private final Class<? extends Throwable> expectedException;

    /**
     * Constructeur d'un cas <b>nominal</b> (pas d'exception attendue).
     *
     * @param a coefficient de x²
     * @param b coefficient de x
     * @param c terme constant
     */
    public QuadTestCase(double a, double b, double c) {
        this(a, b, c, null);
    }

    /**
     * Constructeur d'un cas avec <b>exception attendue</b>.
     *
     * @param a                 coefficient de x²
     * @param b                 coefficient de x
     * @param c                 terme constant
     * @param expectedException classe d'exception à exiger (null = nominal)
     */
    public QuadTestCase(double a, double b, double c, Class<? extends Throwable> expectedException) {
        // Objects.requireNonNull : garde-fou si null accidentel
        this.a = a;
        this.b = b;
        this.c = c;
        this.expectedException = expectedException;
    }

    /** @return coefficient a */
    public double getA() { return a; }

    /** @return coefficient b */
    public double getB() { return b; }

    /** @return terme constant c */
    public double getC() { return c; }

    /**
     * @return classe d'attendue, ou {@code null} pour un cas nominal
     */
    public Class<? extends Throwable> getExpectedException() {
        return expectedException;
    }

    /**
     * Indique si ce cas attend la levée d'une exception.
     *
     * @return {@code true} si une exception est attendue
     */
    public boolean expectsException() {
        return expectedException != null;
    }

    /**
     * Affichage lisible utilisé par JUnit dans le nom du test
     * (annotation {@code @ParameterizedTest(name = "... {0}")}).
     *
     * @return description texte du cas
     */
    @Override
    public String toString() {
        if (expectsException()) {
            return String.format(
                "Equation: [%sx² + %sx + %s = 0] -> Attend: %s",
                a, b, c, expectedException.getSimpleName()
            );
        }
        return String.format(
            "Equation: [%sx² + %sx + %s = 0] -> Nominal",
            a, b, c
        );
    }
}
