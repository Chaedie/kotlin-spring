package com.group.libraryapp.service.user

import com.group.libraryapp.domain.user.User
import com.group.libraryapp.domain.user.UserRepository
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistory
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistoryRepository
import com.group.libraryapp.domain.user.loanhistory.UserLoanStatus
import com.group.libraryapp.dto.user.request.UserCreateRequest
import com.group.libraryapp.dto.user.request.UserUpdateRequest
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class UserServiceTest @Autowired constructor(
    private val userRepository: UserRepository,
    private val userService: UserService,
    private val userLoanHistoryRepository: UserLoanHistoryRepository,

    ) {

    // Spring Context 를 공유하므로
    // 생성 테스트와 조회 테스트를 같이 하면 실패한다.
    @AfterEach
    fun clean() {
        println("CLEAN 시작")
        userRepository.deleteAll()
    }

    @Test
    @DisplayName("유저 저장이 정상 동작한다")
    fun saveUserTest() {
        // given
        val request = UserCreateRequest("임채동", null)

        // when
        userService.saveUser(request)

        // then
        val results = userRepository.findAll()
        assertThat(results.size).isEqualTo(1)
        assertThat(results[0].name).isEqualTo(request.name)
        //        val age: Int? = results[0].age
        //        assertThat(age).isNull()
        assertThat(results[0].age).isNull()
    }

    @Test
    @DisplayName("유저 조회가 정상 동작한다")
    fun getUsersTest() {
        // given
        userRepository.saveAll(
            listOf(
                User("A", 20),
                User("B", null),
            )
        )

        // when
        val results = userService.getUsers()

        // then
        assertThat(results.size).isEqualTo(2) // [UserResponse(), UserResponse()]
        assertThat(results).extracting("name") // ["A", "B"]
            .containsExactlyInAnyOrder("A", "B")
        assertThat(results).extracting("age").containsExactlyInAnyOrder(20, null)
    }

    @Test
    @DisplayName("유저 수정이 정상 동작한다")
    fun updateUserNameTest() {
        // given
        val savedUser = userRepository.save(User("A", null))
        val userUpdateRequest = UserUpdateRequest(savedUser.id!!, "B")

        // when
        userService.updateUserName(userUpdateRequest)

        // then
        val user = userRepository.findAll()[0]
        assertThat(user.name).isEqualTo(userUpdateRequest.name)
    }

    @Test
    @DisplayName("유저 삭제가 정상 동작한다")
    fun deleteUserTest() {
        // given
        userRepository.save(User("A", 20))

        // when
        userService.deleteUser("A")

        // then
        //        val results = userRepository.findAll()
        //        assertThat(results.size).isEqualTo(0)
        assertThat(userRepository.findAll()).isEmpty()
    }

    @Test
    @DisplayName("대출 기록이 없는 유저도 응답에 포함된다")
    fun getUserHistoriesTest1() {
        // given
        userRepository.save(User("A", 20))

        // when
        val results = userService.getUserLoanHistories()

        // then
        assertThat(results.size).isEqualTo(1)
        assertThat(results[0].name).isEqualTo("A")
        assertThat(results[0].books).isEmpty()
    }


    @Test
    @DisplayName("대출 기록이 많은 유저의 응답이 정상 동작한다")
    fun getUserHistoriesTest2() {
        // given
        val savedUser = userRepository.save(User("A", 20))
        userLoanHistoryRepository.saveAll(
            listOf(
                UserLoanHistory.fixture(savedUser, "책1", UserLoanStatus.LOANED),
                UserLoanHistory.fixture(savedUser, "책2", UserLoanStatus.LOANED),
                UserLoanHistory.fixture(savedUser, "책3", UserLoanStatus.RETURNED)
            )
        )


        // when
        val results = userService.getUserLoanHistories()

        // then
        assertThat(results.size).isEqualTo(1)
        assertThat(results[0].name).isEqualTo("A")
        assertThat(results[0].books).hasSize(3)
        assertThat(results[0].books).extracting("name")
            .containsExactlyInAnyOrder("책1", "책2", "책3")
        assertThat(results[0].books).extracting("isReturn")
            .containsExactlyInAnyOrder(false, false, true)
    }

}
