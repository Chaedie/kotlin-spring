package com.group.libraryapp.service.book

import com.group.libraryapp.domain.book.Book
import com.group.libraryapp.domain.book.BookRepository
import com.group.libraryapp.domain.user.User
import com.group.libraryapp.domain.user.UserRepository
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistory
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistoryRepository
import com.group.libraryapp.dto.book.request.BookLoanRequest
import com.group.libraryapp.dto.book.request.BookRequest
import com.group.libraryapp.dto.book.request.BookReturnRequest
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
open class BookServiceTest @Autowired constructor(
    private val bookService: BookService,
    private val bookRepository: BookRepository,
    private val userRepository: UserRepository,
    private val userLoanHistoryRepository: UserLoanHistoryRepository,
) {

    @AfterEach
    fun cleanup() {
        bookRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    @DisplayName("책 등록이 정상 동작한다")
    fun saveBookTest() {
        // given
        val request = BookRequest("이상한 나라의 엘리스")

        // when
        bookService.saveBook(request)

        // then
        val books = bookRepository.findAll()
        assertThat(books).hasSize(1)
        assertThat(books[0].name).isEqualTo(request.name)
    }

    @Test
    @DisplayName("책 대출이 정상 동작한다")
    @Transactional
    open fun loanBookTest() {
        // given
        val book = Book("이상한 나라의 엘리스")
        val user = User("임채동", 30)
        val request = BookLoanRequest("임채동", book.name)

        bookRepository.save(book)
        val savedUser = userRepository.save(user)

        // when
        bookService.loanBook(request)

        // then
        val histories = userLoanHistoryRepository.findByUserName(user.name)
        assertThat(histories).hasSize(1)
        val history = histories[0]
        assertThat(history.bookName).isEqualTo(book.name)
        assertThat(history.user.id).isEqualTo(savedUser.id)
        assertThat(history.user.name).isEqualTo(savedUser.name)
        assertThat(history.user.age).isEqualTo(savedUser.age)
        assertThat(history.isReturn).isFalse()
    }

    @Test
    @DisplayName("책이 진작 대출되어 있다면, 신규 대출이 실패한다")
    fun loanBookFailTest() {
        // given
        val book = Book("이상한 나라의 엘리스")
        val user = User("임채동", 30)
        val request = BookLoanRequest("임채동", book.name)

        bookRepository.save(book)
        val savedUser = userRepository.save(user)
        userLoanHistoryRepository.save(UserLoanHistory(savedUser, book.name, false))

        // when & then
        val message = assertThrows<IllegalArgumentException> {
            bookService.loanBook(request)
        }.message
        assertThat(message).isEqualTo("진작 대출되어 있는 책입니다")
    }

    @Test
    @DisplayName("책 반납이 정상 동작한다")
    open fun returnBookTest() {
        // given
        val book = Book("이상한 나라의 엘리스")
        val user = User("임채동", 30)

        val savedBook = bookRepository.save(book)
        val savedUser = userRepository.save(user)
        userLoanHistoryRepository.save(UserLoanHistory(savedUser, savedBook.name, false))

        val bookReturnRequest = BookReturnRequest(savedUser.name, savedBook.name)
        println("bookReturnRequest = ${bookReturnRequest}")
        // when
        bookService.returnBook(bookReturnRequest)

        // then
        val results = userLoanHistoryRepository.findByUserName(user.name)
        assertThat(results).hasSize(1)
        assertThat(results[0].bookName).isEqualTo(bookReturnRequest.bookName)
        assertThat(results[0].user.name).isEqualTo(bookReturnRequest.userName)
        assertThat(results[0].isReturn).isTrue()
    }
}
