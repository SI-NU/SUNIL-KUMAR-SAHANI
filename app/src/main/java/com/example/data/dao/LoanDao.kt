package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.LoanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LoanDao {
    @Query("SELECT * FROM loans ORDER BY appliedDate DESC")
    fun getAllLoans(): Flow<List<LoanEntity>>

    @Query("SELECT * FROM loans ORDER BY appliedDate DESC")
    suspend fun getAllLoansList(): List<LoanEntity>

    @Query("SELECT * FROM loans WHERE memberId = :memberId ORDER BY appliedDate DESC")
    fun getLoansForMember(memberId: String): Flow<List<LoanEntity>>

    @Query("SELECT * FROM loans WHERE status = 'ACTIVE' ORDER BY nextInterestDueDate ASC")
    fun getActiveLoans(): Flow<List<LoanEntity>>

    @Query("SELECT * FROM loans WHERE status = 'PENDING' ORDER BY appliedDate DESC")
    fun getPendingLoans(): Flow<List<LoanEntity>>

    @Query("SELECT * FROM loans WHERE id = :loanId LIMIT 1")
    suspend fun getLoanById(loanId: String): LoanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoan(loan: LoanEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoans(loans: List<LoanEntity>)

    @Update
    suspend fun updateLoan(loan: LoanEntity)

    @Delete
    suspend fun deleteLoan(loan: LoanEntity)

    @Query("DELETE FROM loans WHERE id = :loanId")
    suspend fun deleteLoanById(loanId: String)

    @Query("SELECT COUNT(*) FROM loans")
    suspend fun getLoanCount(): Int
}
