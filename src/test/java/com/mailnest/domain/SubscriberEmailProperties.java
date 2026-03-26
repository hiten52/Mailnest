package com.mailnest.domain;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

class SubscriberEmailProperties {

  @Property
  boolean validEmailsAreParsedSuccessfully(@ForAll("validEmails") String email) {
    return SubscriberEmail.parse(email).isPresent();
  }

  @Provide
  Arbitrary<String> validEmails() {
    Arbitrary<String> local =
        Arbitraries.strings().withCharRange('a', 'z').ofMinLength(1).ofMaxLength(20);

    Arbitrary<String> domain =
        Arbitraries.strings().withCharRange('a', 'z').ofMinLength(1).ofMaxLength(15);

    Arbitrary<String> tld = Arbitraries.of("com", "org", "net", "io", "dev");

    return Combinators.combine(local, domain, tld).as((l, d, t) -> l + "@" + d + "." + t);
  }
}
