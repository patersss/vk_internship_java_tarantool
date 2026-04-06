package org.leochel.services;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.leochel.*;

public class KeyValueAccessService extends KeyValueAccessServiceGrpc.KeyValueAccessServiceImplBase {
    @Override
    public void put(KeyValuePair request, StreamObserver<PutResponse> responseObserver) {
        super.put(request, responseObserver);
    }

    @Override
    public void count(Empty request, StreamObserver<CountResponse> responseObserver) {
        super.count(request, responseObserver);
    }

    @Override
    public void delete(Key request, StreamObserver<DeleteResponse> responseObserver) {
        super.delete(request, responseObserver);
    }

    @Override
    public void get(Key request, StreamObserver<KeyValuePair> responseObserver) {
        super.get(request, responseObserver);
    }

    @Override
    public void range(KeyRange request, StreamObserver<KeyValuePair> responseObserver) {
        super.range(request, responseObserver);
    }
}
