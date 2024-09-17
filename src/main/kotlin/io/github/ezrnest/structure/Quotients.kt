package io.github.ezrnest.structure

import io.github.ezrnest.model.Fraction

/**
 * Describes the rational numbers, namely quotients, ℚ.
 */
interface Quotients<T> : OrderedField<T> {

    /**
     * The characteristic of quotient number field is zero.
     */
    override val characteristic: Long
        get() = 0

    /**
     * Returns the number value corresponding to the integer.
     */
    val Int.asQ : T
        get() = of(this.toLong())

    /**
     * Returns the number value corresponding to the integer.
     */
    val Long.asQ : T
        get() = of(this)


    /**
     * Returns the number value corresponding to the integer `n`.
     */
    override fun of(n: Long): T {
        return super.of(n)
    }

}