package com.group.libraryapp.domain.user.loanhistory;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserLoanHistoryRepository extends JpaRepository<UserLoanHistory, Long> {

	UserLoanHistory findByBookNameAndIsReturn(String bookName, boolean isReturn);

	List<UserLoanHistory> findByUserName(String userName);
}
