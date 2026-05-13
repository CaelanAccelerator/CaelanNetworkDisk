package com.disk.files.facade;

import com.disk.api.file.FileGrpcServiceGrpc;
import com.disk.api.file.FileReadData;
import com.disk.api.file.FileReadRequest;
import com.disk.api.file.FileReadResponse;
import com.disk.files.domain.entity.FileDO;
import com.disk.files.domain.entity.UserFileDO;
import com.disk.files.domain.service.impl.FileServiceImpl;
import com.disk.files.infrastructure.mapper.UserFileMapper;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
public class FileGrpcService extends FileGrpcServiceGrpc.FileGrpcServiceImplBase {

    private final UserFileMapper userFileMapper;
    private final FileServiceImpl fileService;

    @Override
    public void getFileReadInfo(FileReadRequest request,
                                StreamObserver<FileReadResponse> responseObserver) {
        UserFileDO userFile = userFileMapper.selectById(request.getUserFileId());

        if (userFile == null || request.getUserId() != userFile.getUserId()) {
            responseObserver.onNext(FileReadResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("File not found or access denied")
                    .build());
            responseObserver.onCompleted();
            return;
        }

        FileDO file = fileService.getById(userFile.getRealFileId());
        if (file == null) {
            responseObserver.onNext(FileReadResponse.newBuilder()
                    .setSuccess(false).setMessage("Physical file record missing").build());
            responseObserver.onCompleted();
            return;
        }

        FileReadData data = FileReadData.newBuilder()
                .setUserFileId(userFile.getId())
                .setRealFileId(file.getId())
                .setFilename(userFile.getFilename())
                .setRealPath(file.getRealPath())
                .setFileSuffix(file.getFileSuffix() != null ? file.getFileSuffix() : "")
                .setIdentifier(file.getIdentifier())
                .setFileSize(file.getFileSize() != null ? file.getFileSize() : 0L)
                .setFileType(userFile.getFileType() != null ? userFile.getFileType() : 0)
                .build();

        responseObserver.onNext(FileReadResponse.newBuilder()
                .setSuccess(true).setData(data).build());
        responseObserver.onCompleted();
    }
}
