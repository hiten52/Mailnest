package com.mailnest.subscriptions;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriberRepository extends JpaRepository<Subscriber, UUID> {}
