// ============================================================================
//                 KASPER - Kasper is the treasure keeper
//    www.viadeo.com - mobile.viadeo.com - api.viadeo.com - dev.viadeo.com
//
//           Viadeo Framework for effective CQRS/DDD architecture
// ============================================================================
package com.viadeo.kasper.api.component.event;

import com.viadeo.kasper.api.component.Domain;

/**
 *
 * A Kasper event related to an entity update
 *
 */
public abstract class EntityUpdatedEvent<D extends Domain> implements EntityEvent<D> {

}

