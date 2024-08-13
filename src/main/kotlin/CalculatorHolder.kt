/**
 * 2018-03-05
 */
package cn.ancono.math

import cn.mathsymk.structure.EqualPredicate


/**
 * @author liyicheng
 * 2018-03-05 20:25
 */
interface CalculatorHolder<T, S : EqualPredicate<T>> {

    /**
     * Return the calculator used by this object.
     *
     * @return a calculator
     */
    val calculator: S

}
