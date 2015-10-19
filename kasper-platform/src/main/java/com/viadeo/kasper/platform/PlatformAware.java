// ============================================================================
//                 KASPER - Kasper is the treasure keeper
//    www.viadeo.com - mobile.viadeo.com - api.viadeo.com - dev.viadeo.com
//
//           Viadeo Framework for effective CQRS/DDD architecture
// ============================================================================
package com.viadeo.kasper.platform;

import com.viadeo.kasper.platform.bundle.descriptor.DomainDescriptor;
import com.viadeo.kasper.platform.plugin.Plugin;

public interface PlatformAware {

    /**
     * Invoked when a platform is started
     * @param platform a platform
     */
    void platformStarted(Platform platform);

    /**
     * Invoked when a platform is stopped
     * @param platform a platform
     */
    void platformStopped(Platform platform);

    /**
     * Invoked when a domain is registered
     * @param domainDescriptor the registered domain descriptor
     */
    void domainRegistered(DomainDescriptor domainDescriptor);

    /**
     * Invoked when a plugin is registered
     * @param plugin the registered plugin
     */
    void pluginRegistered(Plugin plugin);
}
