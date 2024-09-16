package linear

import TestUtils.assertEquals
import TestUtils.assertValueEquals
import cn.mathsymk.linear.Matrix
import cn.mathsymk.linear.MatrixImpl
import cn.mathsymk.linear.MatrixUtils
import cn.mathsymk.linear.MatrixUtils.charPoly
import cn.mathsymk.linear.toMutable
import cn.mathsymk.model.Multinomial
import cn.mathsymk.model.NumberModels
import cn.mathsymk.model.Polynomial
import cn.mathsymk.numberTheory.NTFunctions
import cn.mathsymk.util.IterUtils
import cn.mathsymk.util.MathUtils
import cn.mathsymk.util.pow
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MatrixTest {
    val Z = NumberModels.intAsIntegers()
    val Zmod7 = NumberModels.intModP(7)

    @Test
    fun testDetGB() {
        val mults = Multinomial.over(Z)
        (1..3).forEach { n ->
            val A = Matrix(n, mults) { i, j -> mults.monomial("($i$j)") } // 3x3 matrix with variables (ij)
            val detGB = MatrixImpl.detGaussBareiss(A.toMutable(), mults)
            mults.assertEquals(MatrixImpl.detDefinition(A, mults), detGB)
            mults.assertEquals(MatrixImpl.detSmall(A, mults), detGB)
        }
        (4..5).forEach { n ->
            val A = Matrix(n, mults) { i, j -> mults.monomial("($i$j)") } // 4x4 matrix with variables (ij)
            mults.assertEquals(MatrixImpl.detDefinition(A, mults), MatrixImpl.detGaussBareiss(A.toMutable(), mults))
        }
    }

    @Test
    fun scalarDivisionOfMatrix() {
        val A = Matrix(2, 2, Zmod7) { i, j -> (i + j) * 2 }
        val scalar = 2
        val expected = Matrix(2, 2, Zmod7) { i, j -> i + j }
        assertValueEquals(expected, A / scalar)
    }

    @Test
    fun additionOfMatricesWithDifferentDimensions() {
        val A = Matrix(2, 2, Z) { i, j -> i + j }
        val B = Matrix(3, 3, Z) { i, j -> (i + 1) * (j + 1) }
        assertThrows<IllegalArgumentException> { A + B }
    }

    @Test
    fun subtractionOfMatricesWithDifferentDimensions() {
        val A = Matrix(2, 2, Z) { i, j -> i + j }
        val B = Matrix(3, 3, Z) { i, j -> (i + 1) * (j + 1) }
        assertThrows<IllegalArgumentException> { A - B }
    }

    @Test
    fun multiplicationOfMatricesWithIncompatibleDimensions() {
        val A = Matrix(2, 3, Z) { i, j -> i + 1 }
        val B = Matrix(2, 2, Z) { i, j -> j + 1 }
        assertThrows<IllegalArgumentException> { A * B }
    }

    @Test
    fun inverseOfIdentityMatrix() {
        val identity = Matrix.identity(3, Zmod7)
        val expected = Matrix.identity(3, Zmod7)
        assertValueEquals(expected, identity.inv())
    }

    @Test
    fun inverseOfNonSingularMatrix() {
        val A = Matrix(2, Zmod7) { i, j -> if (i == j) 1 else 2 }
        assertValueEquals(Matrix.identity(2, Zmod7), A * A.inv())
        assertValueEquals(Matrix.identity(2, Zmod7), A.inv() * A)
    }

    @Test
    fun inverseOfSingularMatrixThrowsException() {
        val singular = Matrix(2, 2, Zmod7) { _, _ -> 1 }
        assertThrows<ArithmeticException> { singular.inv() }
    }

    @Test
    fun inverseOfNonSquareMatrixThrowsException() {
        val nonSquare = Matrix(2, 3, Zmod7) { i, j -> i + j }
        assertThrows<IllegalArgumentException> { nonSquare.inv() }
    }

    @Test
    fun testMatrixCharPoly() {
        val ℤ = NumberModels.intAsIntegers()
        val n = 4
        val A = Matrix(n, ℤ) { i, j -> i + 2 * j }
        val p = A.charPoly() // the characteristic polynomial of A, p(λ) = det(λI - A)
        ℤ.assertEquals(A.trace(), -p[n - 1])
        ℤ.assertEquals(A.det(), (-1).pow(n) * p[0])

        // another way to compute the characteristic polynomial
        // sum of all principal minors of A
        run {
            val coef = (0..n).map { k ->
                if (k == n) return@map ℤ.one
                var res = ℤ.zero
                for (rows in IterUtils.comb(A.row, n - k, false)) {
                    val major = A.slice(rows, rows).det()
                    res += major
                }
                res * MathUtils.powOfMinusOne(k)
            }
            val p2 = Polynomial.fromList(ℤ, coef)
            assertValueEquals(p, p2)
        }

        val matOverZ = Matrix.over(n, ℤ)
        assertTrue(p.substitute(A, matOverZ).isZero) // p(A) = 0, a matrix of zeros
    }

    @Test
    fun testDet() {
        val mult = Multinomial.over(Z)
        val A = Matrix(3, 3, mult) { i, j ->
            mult.monomial("($i$j)")
        }
        val det3 = MatrixImpl.detSmall(A, mult)
        val detGB = MatrixImpl.detGaussBareiss(A, mult)
        val detDef = MatrixImpl.detDefinition(A, mult)
        mult.assertEquals(det3, detGB)
        mult.assertEquals(det3, detDef)
    }

    @Test
    fun testInvariantFactorsOverIntegers() {
        // 3x3 matrix over integers
        val n = 3
        val A = Matrix(n, Z) { i, j -> (i + 1) * (j + 2) } // example integer matrix
        val factors = MatrixImpl.invariantFactors(A, Z)
        // the accumulated product of the invariant factors
        val accProd = factors.scan(1) { acc, factor -> acc * factor }.drop(1)
        // compute the gcd of the minors of the matrix
//        for(k in 1..n) {
//            val minors = IterUtils.comb(A.row, k, false).map { A.slice(it, it).det().toLong() }.toList()
//            val gcd = NTFunctions.gcd(*minors.toLongArray())
//            assertEquals(gcd, factors[k - 1])
//        }
        val gcds = mutableListOf<Int>()
        for (k in 1..n) {
            val minors = IterUtils.comb(A.row, k, false).map { A.slice(it, it).det().toLong() }.toList()
            val gcd = NTFunctions.gcd(*minors.toLongArray()).toInt()
            if(gcd == 0){
                break
            }
            gcds.add(gcd)
        }
        assertEquals(gcds,  accProd)
//        val minors1 = IterUtils.comb(A.row, 1, false).map { A.slice(it, it).det().toLong() }.toList()
//        val minors2 = IterUtils.comb(A.row, 2, false).map { A.slice(it, it).det().toLong() }.toList()
//        println(NTFunctions.gcd(*minors1.toLongArray()))
//        println(NTFunctions.gcd(*minors2.toLongArray()))
//        println(A.det()) // determinant of the matrix
//        val actualFactors = MatrixImpl.invariantFactors(A,Z)
//        println(actualFactors)
        // Check if the computed invariant factors are as expected
//        assertEquals(expectedFactors, actualFactors)
    }
}