package com.group.libraryapp.service.book

import com.group.libraryapp.domain.book.Book
import com.group.libraryapp.domain.book.BookRepository
import com.group.libraryapp.domain.book.BookType
import com.group.libraryapp.domain.user.User
import com.group.libraryapp.domain.user.UserRepository
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistory
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistoryRepository
import com.group.libraryapp.domain.user.loanhistory.UserLoanStatus
import com.group.libraryapp.dto.book.request.BookLoanRequest
import com.group.libraryapp.dto.book.request.BookRequest
import com.group.libraryapp.dto.book.request.BookReturnRequest
import com.group.libraryapp.dto.book.response.BookStatResponse
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.CompositeIterator

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
        val request = BookRequest("이상한 나라의 엘리스", BookType.COMPUTER)

        // when
        bookService.saveBook(request)

        // then
        val books = bookRepository.findAll()
        assertThat(books).hasSize(1)
        assertThat(books[0].name).isEqualTo(request.name)
        assertThat(books[0].type).isEqualTo(request.type)

    }

    @Test
    @DisplayName("책 대출이 정상 동작한다")
    @Transactional
    open fun loanBookTest() {
        // given
        val book = Book.fixture("이상한 나라의 엘리스")
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
        assertThat(history.status).isEqualTo(UserLoanStatus.LOANED)
    }

    @Test
    @DisplayName("책이 진작 대출되어 있다면, 신규 대출이 실패한다")
    fun loanBookFailTest() {
        // given
        val book = Book.fixture("이상한 나라의 엘리스")
        val user = User("임채동", 30)
        val request = BookLoanRequest("임채동", book.name)

        bookRepository.save(book)
        val savedUser = userRepository.save(user)
        userLoanHistoryRepository.save(UserLoanHistory.fixture(savedUser, book.name))

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
        val book = Book.fixture("이상한 나라의 엘리스")
        val user = User("임채동", 30)

        val savedBook = bookRepository.save(book)
        val savedUser = userRepository.save(user)
        userLoanHistoryRepository.save(UserLoanHistory.fixture(savedUser, savedBook.name))

        val bookReturnRequest = BookReturnRequest(savedUser.name, savedBook.name)
        // when
        bookService.returnBook(bookReturnRequest)

        // then
        val results = userLoanHistoryRepository.findByUserName(user.name)
        assertThat(results).hasSize(1)
        assertThat(results[0].bookName).isEqualTo(bookReturnRequest.bookName)
        assertThat(results[0].user.name).isEqualTo(bookReturnRequest.userName)
        assertThat(results[0].status).isEqualTo(UserLoanStatus.RETURNED)
    }

    @Test
    @DisplayName("책 대여 권수를 정상 확인한다")
    fun countLoanBookTest() {
        // given
        //        val savedBook1 = bookRepository.save(Book.fixture("a"))
        //        val savedBook2 = bookRepository.save(Book.fixture("b"))
        val savedUser = userRepository.save(User("Chaedie", 30))
        userLoanHistoryRepository.saveAll(
            listOf(
                UserLoanHistory.fixture(savedUser, "A"),
                UserLoanHistory.fixture(savedUser, "B", UserLoanStatus.RETURNED),
                UserLoanHistory.fixture(savedUser, "C", UserLoanStatus.RETURNED)
            )
        )

        // when
        val result = bookService.countLoanedBook()

        // then
        assertThat(result).isEqualTo(1)
    }

    @Test
    @DisplayName("분야별 책 권수를 정상 확인한다")
    fun getBookStatisticsTest() {
        // given
        bookRepository.saveAll(
            listOf(
                Book.fixture("A", BookType.COMPUTER),
                Book.fixture("B", BookType.COMPUTER),
                Book.fixture("C", BookType.SCIENCE),
            )
        )

        // when
        val results = bookService.getBookStatistics()

        // then
        assertThat(results).hasSize(2)
        assertCount(results, BookType.COMPUTER, 2)
        assertCount(results, BookType.SCIENCE, 1)
    }

    private fun assertCount(results: List<BookStatResponse>, type: BookType, count: Int) {
        assertThat(results.first { result -> result.type == type }.count).isEqualTo(count)
    }
}
