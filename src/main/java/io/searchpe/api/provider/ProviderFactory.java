package io.searchpe.api.provider;

public interface ProviderFactory<T extends Provider> {

    T create();

    String getId();

    default int order() {
        return 0;
    }

}
