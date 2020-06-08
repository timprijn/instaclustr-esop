package com.instaclustr.cassandra.backup.embedded.manifest;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.instaclustr.cassandra.backup.impl.KeyspaceTable;
import com.instaclustr.cassandra.backup.impl.Manifest;
import com.instaclustr.cassandra.backup.impl.ManifestEntry;
import com.instaclustr.cassandra.backup.impl.ManifestEntry.Type;
import com.instaclustr.cassandra.backup.impl.Snapshots.Snapshot;
import com.instaclustr.cassandra.backup.impl.Snapshots.Snapshot.Keyspace;
import com.instaclustr.cassandra.backup.impl.Snapshots.Snapshot.Keyspace.Table;
import com.instaclustr.jackson.JacksonModule;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ManifestComponentsTest {

    private static final Path TESTING_MANIFEST_PATH = Paths.get("src/test/resources/testing-manifest.json");

    @Inject
    private ObjectMapper objectMapper;

    @BeforeMethod
    public void setup() throws Exception {

        final List<Module> modules = new ArrayList<Module>() {{
            add(new JacksonModule());
        }};

        final Injector injector = Guice.createInjector(modules);
        injector.injectMembers(this);
    }

    @Test
    public void testSameTokens() throws Exception {
        Manifest manifest = parseManifest();
        Assert.assertTrue(manifest.hasSameTokens(manifest.getTokens()));
    }

    @Test
    public void testSchemaParsing() throws Exception {
        Manifest manifest = parseManifest();

        Table t1 = manifest.getSnapshot().getKeyspace("ks1").get().getTable("ks1t1").get();
        Table t2 = manifest.getSnapshot().getKeyspace("ks1").get().getTable("ks1t1").get();

        assertTrue(t1.schemaEqualsTo(t2.getSchemaContent()));
        assertTrue(t1.schemaEqualsTo(t2));
    }

    @Test
    public void testManifestEntry() throws CloneNotSupportedException {
        ManifestEntry original = new ManifestEntry(Paths.get("object/key"),
                                                   Paths.get("local/file"),
                                                   Type.FILE,
                                                   new KeyspaceTable("keyspace", "table"));

        ManifestEntry cloned = original.clone();

        assertNotEquals(original, cloned);

        original.localFile = Paths.get("other/file");
        original.objectKey = Paths.get("other/object");
        original.type = Type.CQL_SCHEMA;
        original.keyspaceTable = new KeyspaceTable("k2", "t2");

        assertNotEquals(original, cloned);
    }

    @Test
    public void testTableEquality() throws Exception {
        Manifest manifest = parseManifest();

        Snapshot snapshot = manifest.getSnapshot();
        Keyspace keyspace = snapshot.getKeyspace("ks1").orElseThrow(IllegalStateException::new);
        Table table = keyspace.getTable("ks1t1").orElseThrow(IllegalStateException::new);

        Table cloned = table.clone();

        assertNotSame(table, cloned);
        assertEquals(table, cloned);

        cloned.add(new ManifestEntry(Paths.get("abc/def"),
                                     null,
                                     Type.FILE,
                                     50,
                                     null));

        assertNotEquals(table, cloned);
    }

    @Test
    public void testKeyspaceEquality() throws Exception {
        Manifest manifest = parseManifest();

        Snapshot snapshot = manifest.getSnapshot();
        Keyspace keyspace = snapshot.getKeyspace("ks1").orElseThrow(IllegalStateException::new);

        Keyspace cloned = keyspace.clone();

        assertNotSame(keyspace, cloned);
        assertEquals(keyspace, cloned);

        cloned.add("newtable", keyspace.getTable("ks1t1").orElseThrow(IllegalStateException::new));

        assertNotEquals(keyspace, cloned);
    }

    @Test
    public void testSnapshotEquality() throws Exception {
        Manifest manifest = parseManifest();

        Snapshot snapshot = manifest.getSnapshot();
        Snapshot cloned = snapshot.clone();

        assertNotSame(cloned, snapshot);
        assertEquals(cloned, snapshot);

        Keyspace ks = new Keyspace(new HashMap<>());
        Table tb = snapshot.getTable("ks1", "ks1t2").orElseThrow(IllegalStateException::new);
        ks.add("t100", tb);

        cloned.add("k100", ks);

        assertNotEquals(snapshot, cloned);
    }

    @Test
    public void testManifestEquality() throws Exception {

        Manifest manifest = parseManifest();
        Manifest manifest2 = parseManifest();

        assertNotNull(manifest);
        assertNotNull(manifest2);
        assertEquals(manifest, manifest2);

        Manifest cloned = manifest.clone();

        assertNotSame(cloned, manifest);
        assertEquals(cloned, manifest);
    }

    private Manifest parseManifest() throws Exception {
        return Manifest.read(TESTING_MANIFEST_PATH, objectMapper);
    }
}
