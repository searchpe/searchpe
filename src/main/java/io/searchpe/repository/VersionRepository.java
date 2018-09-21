package io.searchpe.repository;

import io.searchpe.model.Version;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface VersionRepository {

    Version createVersion(Version version);

    Optional<Version> getVersion(String id);

    Optional<Version> getLastVersion();

    Optional<Version> getLastCompletedVersion();

    List<Version> getCompleteVersionsBefore(Date date);

    List<Version> getCompleteVersionsDesc(int skip);

    List<Version> getVersionsByParameters(Map<String, Object> parameters);

    Version updateVersion(Version version);

    boolean deleteVersion(Version version);

}
