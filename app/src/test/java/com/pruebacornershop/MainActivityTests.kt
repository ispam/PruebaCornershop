package com.pruebacornershop

import com.pruebacornershop.Data.Local.Entities.Counter
import com.pruebacornershop.Data.Local.Entities.Total
import com.pruebacornershop.Data.Local.ViewModels.TotalViewModel
import com.pruebacornershop.Data.Remote.APIService
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.random.Random

class MainActivityTests {

    @Nested
    inner class DomainLogicTests {

        val apiService = mockk<APIService>()
        val totalVM = mockk<TotalViewModel>()

        @BeforeEach
        fun clear() {
            clearMocks(apiService, totalVM)
        }

        @Nested
        inner class TotalViewModelTests {
            @Test
            fun `getTotalFromLocal() method is not empty`() {

                val randomNum = Random.nextInt(10).toLong()
                every { totalVM.getTotal() } answers { Flowable.just(randomNum) }

                totalVM.getTotal()
                    .test()
                    .assertSubscribed()
                    .assertComplete()
                    .assertNoErrors()
                    .assertResult(randomNum)
                    .dispose()

            }

            @Test
            fun `insertTotal() method inserts successfully in local database`() {

                every { totalVM.insertTotal(Total(getTotalSumOfList())) } returns Completable.complete()

                totalVM.insertTotal(Total(getTotalSumOfList()))
                    .test()
                    .assertSubscribed()
                    .assertComplete()
                    .assertNoErrors()
                    .dispose()

            }

            @Test
            fun `updateTotal() method updates correctly in local database`() {

                every { totalVM.updateTotal(getTotalSumOfList(), 1) } returns Completable.complete()

                totalVM.updateTotal(getTotalSumOfList(), 1)
                    .test()
                    .assertSubscribed()
                    .assertComplete()
                    .assertNoErrors()
                    .assertValueCount(0)
                    .dispose()
            }

        }

        @Nested
        inner class ApiServiceTests {

            @Test
            fun `checkCounterListFromServer() method is empty`() {

                val emptyList = emptyList<Counter>()
                every { apiService.getCountersList() } returns Observable.just(emptyList)

                apiService.getCountersList()
                    .test()
                    .assertSubscribed()
                    .assertComplete()
                    .assertNoErrors()
                    .assertResult(emptyList)
                    .dispose()
            }

            @Test
            fun `checkCounterListFromServer() method is not empty`() {


                every { apiService.getCountersList() } returns Observable.just(testList())

                apiService.getCountersList()
                    .test()
                    .assertSubscribed()
                    .assertComplete()
                    .assertNoErrors()
                    .assertResult(testList())
                    .dispose()
            }

            @Test
            fun `createCounter() method successfully creates counter`() {

                val counter = Counter("test6", "zxcfgh", 0)
                val observable = Observable.just(testList())

                every { apiService.createCounter(Counter(counter.id!!)) } answers { Single.fromObservable(observable) }

                apiService.createCounter(Counter(counter.id!!))
                    .test()
                    .assertSubscribed()
                    .assertComplete()
                    .assertNoErrors()
                    .assertResult(testList())
                    .dispose()
            }
        }

    }

    private fun getTotalSumOfList(): Long = testList().sumBy { it.count!! }.toLong()

    private fun testList(): List<Counter> {
        val counter1 = Counter("test1", "zxcasd", 0)
        val counter2 = Counter("test2", "qweasd", 1)
        val counter3 = Counter("test3", "trbrwe", 2)
        val counter4 = Counter("test4", "ytuiyy", 3)
        val counter5 = Counter("test5", "bnmhgm", 4)

        return listOf(counter1, counter2, counter3, counter4, counter5)
    }
}