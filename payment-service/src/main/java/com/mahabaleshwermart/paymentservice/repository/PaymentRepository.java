package com.mahabaleshwermart.paymentservice.repository;

import com.mahabaleshwermart.paymentservice.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {

    Optional<Payment> findByOrderId(String orderId);

    Optional<Payment> findByGatewayPaymentId(String gatewayPaymentId);

    Optional<Payment> findByGatewayOrderId(String gatewayOrderId);

    List<Payment> findByUserId(String userId);

    Page<Payment> findByUserId(String userId, Pageable pageable);

    List<Payment> findByStatus(Payment.PaymentStatus status);

    List<Payment> findByGatewayProvider(Payment.GatewayProvider gatewayProvider);

    @Query("SELECT p FROM Payment p WHERE p.userId = :userId AND p.status = :status")
    List<Payment> findByUserIdAndStatus(@Param("userId") String userId, 
                                       @Param("status") Payment.PaymentStatus status);

    @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :startDate AND :endDate")
    List<Payment> findPaymentsByDateRange(@Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);

    @Query("SELECT p FROM Payment p WHERE p.status = :status AND p.createdAt < :cutoffDate")
    List<Payment> findStalePayments(@Param("status") Payment.PaymentStatus status, 
                                   @Param("cutoffDate") LocalDateTime cutoffDate);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = 'SUCCESS' AND p.paymentDate BETWEEN :startDate AND :endDate")
    Long countSuccessfulPaymentsByDateRange(@Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'SUCCESS' AND p.paymentDate BETWEEN :startDate AND :endDate")
    Double getTotalAmountByDateRange(@Param("startDate") LocalDateTime startDate, 
                                    @Param("endDate") LocalDateTime endDate);

    @Query("SELECT p.gatewayProvider, COUNT(p) FROM Payment p WHERE p.status = 'SUCCESS' GROUP BY p.gatewayProvider")
    List<Object[]> getPaymentCountByGateway();

    @Query("SELECT p.paymentMethod, COUNT(p) FROM Payment p WHERE p.status = 'SUCCESS' GROUP BY p.paymentMethod")
    List<Object[]> getPaymentCountByMethod();

    boolean existsByOrderId(String orderId);
}
