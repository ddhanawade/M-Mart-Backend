package com.mahabaleshwermart.paymentservice.repository;

import com.mahabaleshwermart.paymentservice.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, String> {

    List<PaymentTransaction> findByPaymentId(String paymentId);

    Optional<PaymentTransaction> findByGatewayTransactionId(String gatewayTransactionId);

    List<PaymentTransaction> findByTransactionType(PaymentTransaction.TransactionType transactionType);

    List<PaymentTransaction> findByStatus(PaymentTransaction.TransactionStatus status);

    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.payment.id = :paymentId AND pt.transactionType = :transactionType")
    List<PaymentTransaction> findByPaymentIdAndTransactionType(@Param("paymentId") String paymentId, 
                                                              @Param("transactionType") PaymentTransaction.TransactionType transactionType);

    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.payment.id = :paymentId AND pt.status = 'SUCCESS' AND pt.transactionType IN ('REFUND', 'PARTIAL_REFUND')")
    List<PaymentTransaction> findSuccessfulRefundsByPaymentId(@Param("paymentId") String paymentId);

    @Query("SELECT SUM(pt.amount) FROM PaymentTransaction pt WHERE pt.payment.id = :paymentId AND pt.status = 'SUCCESS' AND pt.transactionType IN ('REFUND', 'PARTIAL_REFUND')")
    Double getTotalRefundedAmount(@Param("paymentId") String paymentId);

    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.status = :status AND pt.createdAt < :cutoffDate")
    List<PaymentTransaction> findStaleTransactions(@Param("status") PaymentTransaction.TransactionStatus status, 
                                                  @Param("cutoffDate") LocalDateTime cutoffDate);
}
