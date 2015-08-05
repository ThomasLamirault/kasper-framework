// ============================================================================
//                 KASPER - Kasper is the treasure keeper
//    www.viadeo.com - mobile.viadeo.com - api.viadeo.com - dev.viadeo.com
//
//           Viadeo Framework for effective CQRS/DDD architecture
// ============================================================================
package com.viadeo.kasper.domain.sample.hello.common.db;

/**
 * A naive business index in-memory implementation
 */
public class HelloMessagesByBuddyBusinessIndexStore extends KeyValueStore {

   public static final HelloMessagesByBuddyBusinessIndexStore db = new HelloMessagesByBuddyBusinessIndexStore();

}
