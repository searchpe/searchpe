package io.searchpe.api.provider;

import java.util.Map;

public interface ServerInfoAwareProviderFactory {

    /**
     * Return actual info about the provider. This info contains informations about providers configuration and operational conditions (eg. errors in connection to remote systems etc) which is
     * shown on "Server Info" page then.
     *
     * @return Map with keys describing value and relevant values itself
     */
    Map<String, String> getOperationalInfo();
}
