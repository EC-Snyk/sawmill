package io.logz.sawmill.processors;

import com.google.common.io.Resources;
import com.maxmind.db.CHMCache;
import com.maxmind.geoip2.DatabaseReader;
import io.logz.sawmill.exceptions.SawmillException;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

class GeoIpDbReaderFactory {

    private static final String TAR_GZ_SUFFIX = ".tar.gz";
    private static final String MMDB_GZ_SUFFIX = ".mmdb.gz";

    static DatabaseReader createDatabaseReader(String location) {
        try (InputStream mmdbStream = Resources.getResource(location).openStream()) {

            if (location.endsWith(TAR_GZ_SUFFIX)) {
                try (TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(new GZIPInputStream(mmdbStream))) {
                    return initReader(seekToDbFile(tarArchiveInputStream));
                }
            } else if (location.endsWith(MMDB_GZ_SUFFIX)) {
                return initReader(new GZIPInputStream(mmdbStream));
            } else {
                return initReader(mmdbStream);
            }
        } catch (Exception e) {
            throw new SawmillException(String.format("Failed to load geoip database from '%s'", location), e);
        }
    }

    private static DatabaseReader initReader(InputStream inputStream) throws IOException {
        return new DatabaseReader.Builder(inputStream)
                .withCache(new CHMCache())
                .build();
    }

    private static TarArchiveInputStream seekToDbFile(TarArchiveInputStream tarArchiveInputStream) throws IOException {
        while (tarArchiveInputStream.getNextEntry() != null) {
            boolean dbFile = tarArchiveInputStream.getCurrentEntry().getName().endsWith(".mmdb");

            if (dbFile) {
                return tarArchiveInputStream;
            }
        }

        throw new FileNotFoundException("Could not find a valid .mmdb file within archive");
    }
}
