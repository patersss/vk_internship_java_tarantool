package org.leochel.services;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.leochel.*;
import org.leochel.exceptions.TarantoolOperationException;
import org.leochel.repositories.Repository;

import java.util.logging.Logger;

public class KeyValueAccessService extends KeyValueAccessServiceGrpc.KeyValueAccessServiceImplBase {
    private static final Logger logger = Logger.getLogger(KeyValueAccessService.class.getName());
    private final Repository repository;

    public KeyValueAccessService(Repository repository) {
        this.repository = repository;
    }

    @Override
    public void put(KeyValuePair request, StreamObserver<Empty> responseObserver) {
        try {
            byte[] value = request.hasValue() ? request.getValue().toByteArray() : null;
            repository.put(request.getKey(), value);
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (TarantoolOperationException e) {
            logger.severe("Put failed for key: " + request.getKey() + " " + e.getMessage());
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Failed to store value")
                    .asRuntimeException());
        }
    }

    @Override
    public void get(Key request, StreamObserver<KeyValuePair> responseObserver) {
        try {
            KeyValuePair result = repository.get(request.getKey());
            if (result == null) {
                responseObserver.onError(Status.NOT_FOUND
                        .withDescription("Key not found: " + request.getKey())
                        .asRuntimeException());
                return;
            }
            responseObserver.onNext(result);
            responseObserver.onCompleted();
        } catch (TarantoolOperationException e) {
            logger.severe("Get failed for key: " + request.getKey() + " " + e.getMessage());
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Failed to retrieve value")
                    .asRuntimeException());
        }
    }

    @Override
    public void delete(Key request, StreamObserver<Empty> responseObserver) {
        try {
            repository.delete(request.getKey());
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (TarantoolOperationException e) {
            logger.severe("Delete failed for key: " + request.getKey() + " " + e.getMessage());
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Failed to delete value")
                    .asRuntimeException());
        }
    }

    @Override
    public void count(Empty request, StreamObserver<CountResponse> responseObserver) {
        try {
            long count = repository.count();
            responseObserver.onNext(CountResponse.newBuilder()
                    .setRowCount(count)
                    .build());
            responseObserver.onCompleted();
        } catch (TarantoolOperationException e) {
            logger.severe("Count failed: " + e.getMessage());
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Failed to count records")
                    .asRuntimeException());
        } catch (IllegalStateException e) {
            logger.severe("Count returned unexpected data: " + e.getMessage());
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Failed to count records")
                    .asRuntimeException());
        }
    }

    @Override
    public void range(KeyRange request, StreamObserver<KeyValuePair> responseObserver) {
        try {
            repository.range(
                    request.getKeySince(),
                    request.getKeyTo(),
                    responseObserver::onNext
            );
            responseObserver.onCompleted();
        } catch (TarantoolOperationException e) {
            logger.severe("Range failed [" + request.getKeySince()
                    + ", " + request.getKeyTo() + "): " + e.getMessage());
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Failed to retrieve range")
                    .asRuntimeException());
        } catch (IllegalStateException e) {
            logger.severe("Range mapping error [" + request.getKeySince()
                    + ", " + request.getKeyTo() + "): " + e.getMessage());
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Failed to retrieve range")
                    .asRuntimeException());
        }
    }
}
