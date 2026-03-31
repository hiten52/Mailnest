package com.mailnest.subscriptions;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SubscriberRepository extends JpaRepository<Subscriber, UUID> {
  @Query("SELECT s.email FROM Subscriber s WHERE s.status = 'confirmed'")
  List<String> findConfirmedSubscriberEmails();
}
