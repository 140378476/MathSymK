package model

import TestUtils.assertValueEquals
import cn.mathsymk.model.*
import cn.mathsymk.model.TensorImpl
import org.junit.jupiter.api.Assertions.assertArrayEquals
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TensorTest {
    @Test
    fun testAdd() {
        val mc = NumberModels.IntAsIntegers
        val shape = intArrayOf(2, 8)
//    val v = Tensor.zeros(mc, *shape)
//    val w = Tensor.ones(mc, *shape)
        val u = Tensor.of(shape, mc) { it.sum() }
        assertTrue((u - u).isZero)
    }

    @Test
    fun testView() {
        val mc = NumberModels.IntAsIntegers
        val shape = intArrayOf(2, 8)
//    val v = Tensor.zeros(mc, *shape)
//    val w = Tensor.ones(mc, *shape)
        val u = Tensor.of(shape, mc) { it.sum() }
//        println(u.slice(null, 0 downTo -1 step 2).joinToString())
        assertEquals(2, u.slice(null, 0 downTo -1 step 2).size)
    }

    @Test
    fun testPermute(){
        val mc = NumberModels.IntAsIntegers
        val shape = intArrayOf(2, 3, 4)
        val u = Tensor.of(shape, mc) { idx -> idx.withIndex().sumOf { (1 + it.index) * it.value } }
        val v = u.permute(1, 2, 0)
        assertArrayEquals(intArrayOf(3, 4, 2), v.shape)
        assertEquals(u[0, 1, 2], v[1, 2, 0])
    }

    @Test
    fun testSet() {
        val mc = NumberModels.IntAsIntegers
        val shape = intArrayOf(2, 8)
//    val v = Tensor.zeros(mc, *shape)
//    val w = Tensor.ones(mc, *shape)
//        val s1 =
        val u = Tensor.of(shape, mc) { it.sum() }
        u[1, 1] = 3
        assertEquals(3, u[1, 1])
    }

    @Test
    fun testWedge() {
        val mc = NumberModels.IntAsIntegers
        val shape = intArrayOf(2, 3)
        val shape2 = intArrayOf(3, 2)
        val u = Tensor.of(shape, mc) { it.sum() }
        val w = Tensor.of(shape2, mc) { it[0] }
//        println(u)
//        println(w)
//        println()
        assertValueEquals(w, u.wedge(w).slice(0, 1))
    }

    @Test
    fun testSum() {
        val mc = NumberModels.IntAsIntegers
        val t = Tensor.of(intArrayOf(2, 3), mc, 0, 1, 2, 2, 3, 4)
        val re = Tensor.of(intArrayOf(3), mc, 2, 4, 6)
        assertValueEquals(re, t.sum(0))
    }

    @Test
    fun testEinsum() {
        val mc = NumberModels.IntAsIntegers
        val shape = intArrayOf(3, 3)
        val shape2 = intArrayOf(3, 3)
//    val v = Tensor.zeros(mc, *shape)
//    val w = Tensor.ones(mc, *shape)
        val u = Tensor.of(shape, mc) { it.sum() }
        val w = Tensor.of(shape2, mc) { it[0] }
        var r = TensorImpl.einsum(listOf(u, w),
                intArrayOf(3, 3),
                intArrayOf(1),
                listOf(intArrayOf(0, 0, 1, 1), intArrayOf(0, 0, 1, 1)),
                listOf(intArrayOf(), intArrayOf()),
                mc)//element-wise multiplication
        assertValueEquals(r, u * w)


        r = TensorImpl.einsum(listOf(u),
                intArrayOf(3),
                intArrayOf(1),
                listOf(intArrayOf(0, 0, 1, 0)),
                listOf(intArrayOf()),
                mc)//diagonal elements
        assertValueEquals(r, Tensor.of(intArrayOf(3), mc) { it[0] * 2 })

        r = TensorImpl.einsum(listOf(u),
                intArrayOf(1),
                intArrayOf(3),
                listOf(intArrayOf()),
                listOf(intArrayOf(0, 0, 1, 0)),
                mc) // trace
        assertEquals(6, r[0])

        r = TensorImpl.einsum(listOf(u, w),
                intArrayOf(3, 3, 3, 3),
                intArrayOf(1),
                listOf(intArrayOf(0, 0, 1, 1), intArrayOf(0, 2, 1, 3)),
                listOf(intArrayOf(), intArrayOf()),
                mc) // wedge(outer product)
        assertValueEquals(r, u.wedge(w))
    }

    @Test
    fun testEinsum2() {
        val mc = NumberModels.IntAsIntegers
        val shape = intArrayOf(3, 3)
        val shape2 = intArrayOf(3, 3)
//    val v = Tensor.zeros(mc, *shape)
//    val w = Tensor.ones(mc, *shape)
        val u = Tensor.of(shape, mc) { it[0] + 2 * it[1] }
        val w = Tensor.of(shape2, mc) { it[0] }

        assertEquals(9, Tensor.einsum("ii", u)[0]) // trace
        assertEquals(27, Tensor.einsum("ij->", u)[0]) // sum
        assertEquals(3, Tensor.einsum("ii->i", u)[1]) // diagonal
        assertEquals(4, Tensor.einsum("ij->ji", u)[2, 0]) // transpose

        assertEquals(12, Tensor.einsum("ij,ij->ij", u, w)[2, 2]) // element-wise multiplication

        assertEquals(13, Tensor.einsum("ij,jk->ik", u, w)[1, 1]) // matrix multiplication
    }

    @Test
    fun testEinsum3() {
        val mc = NumberModels.IntAsIntegers
        val shape = intArrayOf(3, 3)
        val u = Tensor.of(shape, mc) { it[0] + 2 * it[1] }
        assertValueEquals(TensorImpl.sumInOneAxis(u, 1), Tensor.einsum("ij->i", u))
        assertValueEquals(TensorImpl.sumInOneAxis(u, 0), Tensor.einsum("ij->j", u))
    }

    @Test
    fun testEinsum4() {
        val mc = NumberModels.IntAsIntegers
        val shape = intArrayOf(2, 3, 4)
        val u = Tensor.of(shape, mc) { idx -> idx.withIndex().sumOf { (1 + it.index) * it.value } }
        assertValueEquals(u.sum(-1), Tensor.einsum("ijk->ij", u))
        assertValueEquals(u.sum(0, 1), Tensor.einsum("ijk->k", u))
    }

    @Test
    fun testEinsum5() {
        val mc = NumberModels.IntAsIntegers
        val shape = intArrayOf(2, 2, 3)
        val shape2 = intArrayOf(2, 3, 4)
        val u = Tensor.of(shape, mc) { idx -> idx.withIndex().sumOf { (1 + it.index) * it.value } }
        val w = Tensor.of(shape2, mc) { it[0] + 1 }

        val r1 = u.matmul(w, r = 2)
        val r2 = Tensor.einsum("ijk,jkl->il", u, w)
        assertValueEquals(r1, r2)
    }

    @Test
    fun testConcat() {
        val mc = NumberModels.IntAsIntegers
        val shape = intArrayOf(3, 2)
        val shape2 = intArrayOf(3, 3)
        val u = Tensor.of(shape, mc) { idx -> idx.withIndex().sumOf({ (1 + it.index) * it.value }) }
        val w = Tensor.of(shape2, mc) { it[0] }
        val v = Tensor.concatM(u, w, axis = 1)
        assertArrayEquals(intArrayOf(3, 5), v.shape)
        val v1 = v.slice(0, null)
        v1.setAll(1)
        assertTrue(u.slice(0).all { it == 1 })
        assertTrue(w.slice(0).all { it == 1 })
    }

    @Test
    fun testStack() {
        val mc = NumberModels.IntAsIntegers
        val shape = intArrayOf(3, 3)
        val shape2 = intArrayOf(3, 3)
        val u = Tensor.of(shape, mc) { idx -> idx.withIndex().sumOf { (1 + it.index) * it.value } }
        val w = Tensor.of(shape2, mc) { it[0] }
        val v = Tensor.stackM(u, w, axis = 1)
        assertValueEquals(v.slice(null, 0, null), u)
        assertValueEquals(v.slice(null, 1, null), w)
    }

    @Test
    fun testCreate() {
        val mc = NumberModels.IntAsIntegers
        val t = Tensor.of(listOf(
                listOf(1, 2, 3),
                listOf(3, 4, 5)),
                mc)
        assertArrayEquals(intArrayOf(2, 3), t.shape)
        assertEquals(18, t.sumAll())
    }

    @Test
    fun testDiag() {
        val mc = NumberModels.IntAsIntegers
        val a = Tensor.of((0..3).toList(), mc).reshape(2, 2)
        assertValueEquals(Tensor.of(listOf(0, 3), mc), a.diagonal())
        assertValueEquals(Tensor.scalar(1, mc), a.diagonal(1))

        val b = Tensor.of((0..7).toList(), mc).reshape(2, 2, 2)
        assertValueEquals(Tensor.of(intArrayOf(2, 2), mc, 0, 6, 1, 7),
                b.diagonal(0, 0, 1))

    }

    @Test
    fun testTrace() {
        val mc = NumberModels.IntAsIntegers
        val a = Tensor.of((0..3).toList(), mc).reshape(2, 2)
        val t = Tensor.of(listOf(3), mc)
        val tr = a.trace()
        assertValueEquals(t, tr)

        val b = Tensor.of((0..7).toList(), mc).reshape(2, 2, 2)
        assertValueEquals(Tensor.of(listOf(5, 9), mc),
                b.trace(0, 0))
        assertValueEquals(b.diagonal(0, 0, -1).sum(-1),
                b.trace(0, 0, -1))
    }

//    @Test
//    fun testToMatrix() {
//        val mc = NumberModels.DoubleAsReals
//        val a = Tensor(intArrayOf(3, 3), mc) {
//            Random.nextDouble()
//        }
//        val m = a.toMatrix()
//        val a1 = Tensor.fromMatrix(m)
//        assertValueEquals(a, a1)
//        assert(mc.isEqual(a.sumAll(), m.sum()))
//        assert(mc.isEqual(a.trace().sumAll(), m.trace()))
//        assertValueEquals(a.transpose().toMatrix(), m.transpose())
//
//        val b = Tensor(a.shape, mc) {
//            Random.nextDouble()
//        }
//        val c = a matmul b
//        val m1 = m * b.toMatrix()
//        assertValueEquals(c.toMatrix(), m1)
//
//    }


}