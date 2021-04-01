package com.network_storage.server.handlers;

import com.network_storage.client_server.Stages;
import com.network_storage.client_server.Commands;
import com.network_storage.server.database.DatabaseConnector;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ClientHandler extends ChannelInboundHandlerAdapter {

    private final DatabaseConnector db_connector;
    private final FileHandler fileHandler = new FileHandler();
    private Path path;
    private long fileReqLength = 0;
    private long loadedLength = 0;
    private Commands uType = Commands.EMPTY;
    private Stages sType = Stages.START_TYPE;
    private String fileName;
    private String userInfo;
    private int reqNameLength = 0;
    private BufferedOutputStream bos;

    public ClientHandler(DatabaseConnector db_connector){
        this.db_connector = db_connector;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        fileHandler.setCtx(ctx);
        ByteBuf byteBuf = (ByteBuf) msg;

        if (sType == Stages.START_TYPE){
            uType = Commands.getTypeFromByte(byteBuf.readByte());
            switch (uType) {
                case AUTH, REG -> sType = Stages.GET_USER_LENGTH;
            }
        }

        if (sType == Stages.GET_USER_LENGTH){
            if (byteBuf.readableBytes() < 4) return;
            reqNameLength = byteBuf.readInt();
            sType = Stages.GET_USER;
        }

        if (sType == Stages.GET_USER){
            if (byteBuf.readableBytes() < reqNameLength) return;
            byte[] authNameArr = new byte[reqNameLength];
            byteBuf.readBytes(authNameArr);
            userInfo = new String(authNameArr);
            switch (uType) {
                case REG -> sType = Stages.GET_REG;
                case AUTH -> sType = Stages.GET_AUTH;
            }
        }

        if (sType == Stages.GET_AUTH){
            if (db_connector.getAuth(userInfo)) {
                String folder = db_connector.getFolder(userInfo);
                fileHandler.setServerPath(folder);
                fileHandler.sendAuthOk();
                sType = Stages.SEND_LIST;
            }
            else {
                fileHandler.sendWarning("Incorrect login or password!");
                sType = Stages.START_TYPE;
            }
        }

        if (sType == Stages.GET_REG){
            if (db_connector.getReg(userInfo)){
                Files.createDirectory(Paths.get(db_connector.getFolder(userInfo)));
                sType = Stages.GET_AUTH;
            } else {
                fileHandler.sendWarning("Failed to register user!");
                sType = Stages.START_TYPE;
            }
        }

        if (sType == Stages.GET_COMMAND){
            uType = Commands.getTypeFromByte(byteBuf.readByte());
            switch (uType) {
                case DOWNLOAD, UPLOAD, CREATE, FORWARD, DELETE -> sType = Stages.GET_FILE_NAME_LENGTH;
                case BACK -> sType = Stages.MOVE_BACK;
                case LIST -> sType = Stages.SEND_LIST;
                case EXIT -> sType = Stages.EXIT;
            }
        }

        if (sType == Stages.GET_FILE_NAME_LENGTH){
            if (byteBuf.readableBytes() < 4) return;
            reqNameLength = byteBuf.readInt();
            sType = Stages.GET_FILE_NAME;
        }

        if (sType == Stages.GET_FILE_NAME){
            if (byteBuf.readableBytes() < reqNameLength) return;
            byte[] fileNameArr = new byte[reqNameLength];
            byteBuf.readBytes(fileNameArr);
            fileName = new String(fileNameArr);
            path = Paths.get(fileHandler.getServerPath() + fileName);
            switch (uType) {
                case DOWNLOAD -> sType = Stages.SEND_FILE;
                case UPLOAD -> sType = Stages.GET_FILE_LENGTH;
                case DELETE -> sType = Stages.DELETE_FILE;
                case CREATE -> sType = Stages.CREATE_DIR;
                case FORWARD -> sType = Stages.MOVE_FORWARD;
                default -> sType = Stages.GET_COMMAND;
            }
        }

        if (sType == Stages.EXIT){
            db_connector.removeUser(userInfo);
            sType = Stages.START_TYPE;
        }

        if (sType == Stages.CREATE_DIR){
            fileHandler.createDir(path);
            sType = Stages.SEND_LIST;
        }

        if (sType == Stages.MOVE_FORWARD){
            fileHandler.moveForward(fileName);
            sType = Stages.SEND_LIST;
        }

        if (sType == Stages.DELETE_FILE){
            fileHandler.deleteFile(path);
            sType = Stages.SEND_LIST;
        }

        if (sType == Stages.SEND_FILE){
            fileHandler.writeFile(fileName, path);
            byteBuf.release();
            sType = Stages.SEND_LIST;
        }

        if (sType == Stages.MOVE_BACK){
            fileHandler.moveBack();
            sType = Stages.GET_COMMAND;
        }

        if (sType == Stages.SEND_LIST){
            fileHandler.sendFilesList();
            sType = Stages.GET_COMMAND;
        }

        if (sType == Stages.GET_FILE_LENGTH){
            if (byteBuf.readableBytes() < 8) return;
            fileReqLength = byteBuf.readLong();
            loadedLength = 0;
            fileHandler.createFile(path);
            bos = new BufferedOutputStream(new FileOutputStream(path.toString(), true));
            sType = Stages.GET_FILE;
        }

        if (sType == Stages.GET_FILE){
            while (byteBuf.readableBytes() > 0 && loadedLength < fileReqLength){
                bos.write(byteBuf.readByte());
                loadedLength++;
            }
            if (loadedLength < fileReqLength) return;
            bos.flush();
            byteBuf.release();
            bos.close();
            sType = Stages.SEND_LIST;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        db_connector.removeUser(userInfo);
        cause.printStackTrace();
        ctx.close();
    }
}
