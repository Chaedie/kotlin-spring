package com.group.libraryapp.domain.user.loanhistory

import org.springframework.data.jpa.repository.JpaRepository

interface UserLoanHistoryRepository : JpaRepository<UserLoanHistory, Long> {

    fun findByBookNameAndStatus(bookName: String, status: UserLoanStatus): UserLoanHistory?

    fun findByUserName(userName: String): List<UserLoanHistory>

    fun findAllByStatus(status: UserLoanStatus): List<UserLoanHistory>

}
