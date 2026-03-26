package com.mailnest.subscriptions;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionTokenRepository extends JpaRepository<SubscriptionToken, String> {

  Optional<SubscriptionToken> findBySubscriptionToken(String subscriptionToken);

  void deleteBySubscriberId(UUID subscriberId);
}
