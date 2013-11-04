// ============================================================================
//                 KASPER - Kasper is the treasure keeper
//    www.viadeo.com - mobile.viadeo.com - api.viadeo.com - dev.viadeo.com
//
//           Viadeo Framework for effective CQRS/DDD architecture
// ============================================================================
package com.viadeo.kasper.cqrs.query.impl;

import com.viadeo.kasper.KasperID;
import com.viadeo.kasper.cqrs.query.QueryEntityResult;
import org.joda.time.DateTime;

import static com.google.common.base.Preconditions.checkNotNull;

public class AbstractQueryEntityResult implements QueryEntityResult {

    private final KasperID id;
    private final String type;
    private final DateTime lastModificationTime;

    // ------------------------------------------------------------------------

    public AbstractQueryEntityResult(final KasperID id, final String type) {
        this(id, type, new DateTime(0, 0, 0, 0, 0));
    }

    public AbstractQueryEntityResult(final KasperID id, final String type, final DateTime time) {
        this.id = checkNotNull(id);
        this.type = checkNotNull(type);
        this.lastModificationTime = checkNotNull(time);
    }

    // ------------------------------------------------------------------------

    @Override
    public KasperID getId() {
        return this.id;
    }

    @Override
    public String getType() {
        return this.type;
    }

    @Override
    public DateTime getLastModificationTime() {
        return this.lastModificationTime;
    }

}