package com.instaclustr.esop.impl.restore;

import com.instaclustr.esop.impl.AbstractOperationRequest;
import com.instaclustr.esop.impl.ProxySettings;
import com.instaclustr.esop.impl.StorageLocation;
import com.instaclustr.esop.impl.retry.RetrySpec;
import picocli.CommandLine.Option;

public class BaseRestoreOperationRequest extends AbstractOperationRequest {

    @Option(names = {"--cc", "--concurrent-connections"},
        description = "Number of files (or file parts) to download concurrently. Higher values will increase throughput. Default is 10.",
        defaultValue = "10"
    )
    public Integer concurrentConnections = 10;

    public BaseRestoreOperationRequest() {
        // for picocli
    }

    public BaseRestoreOperationRequest(final StorageLocation storageLocation,
                                       final Integer concurrentConnections,
                                       final String k8sNamespace,
                                       final String k8sSecretName,
                                       final boolean insecure,
                                       final boolean skipBucketVerification,
                                       final ProxySettings proxySettings,
                                       final RetrySpec retry) {
        super(storageLocation, k8sNamespace, k8sSecretName, insecure, skipBucketVerification, proxySettings, retry);
        this.storageLocation = storageLocation;
        this.concurrentConnections = concurrentConnections == null ? 10 : concurrentConnections;
        this.k8sNamespace = k8sNamespace;
        this.k8sSecretName = k8sSecretName;
    }
}
