package de.finnos.southparkdownloader.services;

import de.finnos.southparkdownloader.Registration;

import java.util.List;

public interface HasServices {
    Registration registerService(final BaseService<?, ?> service);

    List<BaseService<?, ?>> getServices();
}
