package com.group.libraryapp.calculator

import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows


class JUnitCalculatorTest {


    @Test
    fun addTest() {
        // given
        val calculator = Calculator(5)

        // when
        calculator.add(3)

        // then
        assertThat(calculator.number).isEqualTo(8)
    }

    @Test
    fun minusTest() {
        // given
        val calculator = Calculator(5)

        // when
        calculator.minus(3)

        // then
        assertThat(calculator.number).isEqualTo(2)
    }

    @Test
    fun multiplyTest() {
        // given
        val calculator = Calculator(5)

        // when
        calculator.multiply(3)

        // then
        assertThat(calculator.number).isEqualTo(15)
    }


    @Test
    fun divideTest() {
        // given
        val calculator = Calculator(15)

        // when
        calculator.divide(5)

        // then
        assertThat(calculator.number).isEqualTo(3)
    }

    @Test
    fun divideExceptionTest() {
        // given
        val calculator = Calculator(15)

        // when & then
        val message = assertThrows<IllegalArgumentException> { calculator.divide(0) }.message
        assertThat(message).isEqualTo("0으로 나눌 수 없습니다.")

    }
}


//class CalculatorTest {
//
//
//    fun addTest() {
//        // given
//        val calculator = Calculator(5)
//
//        // when
//        calculator.add(3)
//
//        // data class 로 테스트 하는 방법
//        //        val expectedCalculator = Calculator(8)
//        //        if (calculator != expectedCalculator) {
//        //            throw IllegalStateException()
//        //        }
//
//        // then
//        if (calculator.number != 8) {
//            throw IllegalStateException()
//        }
//    }
//
//    fun minusTest() {
//        // given
//        val calculator = Calculator(5)
//
//        // when
//        calculator.minus(3)
//
//        // then
//        if (calculator.number != 2) {
//            throw IllegalStateException()
//        }
//    }
//
//    fun multiplyTest() {
//        // given
//        val calculator = Calculator(5)
//
//        // when
//        calculator.multiply(3)
//
//        // then
//        if (calculator.number != 15) {
//            throw IllegalStateException()
//        }
//    }
//
//
//    fun divideTest() {
//        // given
//        val calculator = Calculator(15)
//
//        // when
//        calculator.divide(5)
//
//        // then
//        if (calculator.number != 3) {
//            throw IllegalStateException()
//        }
//    }
//
//    fun divideExceptionTest() {
//        // given
//        val calculator = Calculator(15)
//
//        // when
//        try{
//            calculator.divide(0)
//        } catch (e: IllegalArgumentException){
//
//            if (e.message != "0으로 나눌 수 없습니다.") {
//                throw IllegalStateException("메세지가 다릅니다.")
//            }
//            // 테스트 성공
//            return
//        } catch (e: Exception) {
//            throw IllegalStateException()
//        }
//
//        throw IllegalStateException("기대하는 예외가 발생하지 않았습니다.")
//    }
//}
