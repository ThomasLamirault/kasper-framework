// ----------------------------------------------------------------------------
//  This file is part of the Kasper framework.
//
//  The Kasper framework is free software: you can redistribute it and/or 
//  modify it under the terms of the GNU Lesser General Public License as 
//  published by the Free Software Foundation, either version 3 of the 
//  License, or (at your option) any later version.
//
//  Kasper framework is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public License
//  along with the framework Kasper.  
//  If not, see <http://www.gnu.org/licenses/>.
// --
//  Ce fichier fait partie du framework logiciel Kasper
//
//  Ce programme est un logiciel libre ; vous pouvez le redistribuer ou le 
//  modifier suivant les termes de la GNU Lesser General Public License telle 
//  que publiée par la Free Software Foundation ; soit la version 3 de la 
//  licence, soit (à votre gré) toute version ultérieure.
//
//  Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS 
//  AUCUNE GARANTIE ; sans même la garantie tacite de QUALITÉ MARCHANDE ou 
//  d'ADÉQUATION à UN BUT PARTICULIER. Consultez la GNU Lesser General Public 
//  License pour plus de détails.
//
//  Vous devez avoir reçu une copie de la GNU Lesser General Public License en 
//  même temps que ce programme ; si ce n'est pas le cas, consultez 
//  <http://www.gnu.org/licenses>
// ----------------------------------------------------------------------------
// ============================================================================
//                 KASPER - Kasper is the treasure keeper
//    www.viadeo.com - mobile.viadeo.com - api.viadeo.com - dev.viadeo.com
//
//           Viadeo Framework for effective CQRS/DDD architecture
// ============================================================================
package com.viadeo.kasper.api.component.command;

import com.google.common.base.Optional;
import com.viadeo.kasper.api.id.KasperID;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 *
 * Convenient base implementation for id and version management
 * 
 */
public abstract class UpdateCommand implements Command {
    private static final long serialVersionUID = -432287057423281452L;

    @NotNull
	private final KasperID id;

    private final Long version;

	// ------------------------------------------------------------------------

	protected UpdateCommand(final KasperID id) {
		this.id = checkNotNull(id);
        this.version = null;
	}

 	protected UpdateCommand(final KasperID id, final Long version) {
		this.id = checkNotNull(id);
        this.version = version; /* can be null */
	}

	// ------------------------------------------------------------------------

	public KasperID getId() {
		return this.id;
	}

    public Optional<Long> getVersion() {
        return Optional.fromNullable(this.version);
    }

    // ------------------------------------------------------------------------

    @Override
    public boolean equals(final Object obj) {
        if (null == obj) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UpdateCommand other = (UpdateCommand) obj;

        return com.google.common.base.Objects.equal(this.id, other.id)
                && com.google.common.base.Objects.equal(this.version, other.version);
    }

    @Override
    public int hashCode() {
        return com.google.common.base.Objects.hashCode(this.id, this.version);
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this)
                .addValue(this.id)
                .addValue(this.version)
                .toString();
    }

}
