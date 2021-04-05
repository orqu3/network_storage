package com.network_storage.client.handlers;

import com.network_storage.client.controllers.Controller;
import com.network_storage.client_server.Commands;
import com.network_storage.client_server.Stages;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import javafx.application.Platform;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileHandler extends ChannelInboundHandlerAdapter {

    private ClientHandler clientHandler;
    private Path path = null;
    private long fileReqLength = 0;
    private long loadedLength = 0;
    private Commands uType = Commands.EMPTY;
    private Stages sType = Stages.GET_COMMAND;
    private String fileName;
    private int fileNameLength = 0;
    private BufferedOutputStream bos;
    private final Controller controller;

    public FileHandler(Controller controller) {
        this.controller = controller;
        clientHandler = controller.getClientFileMethods();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        ByteBuf byteBuf = (ByteBuf) msg;

        if (sType == Stages.GET_COMMAND) {
            uType = Commands.getTypeFromByte(byteBuf.readByte());
            switch (uType) {
                case WARNING, LIST, FILE -> sType = Stages.GET_FILE_NAME_LENGTH;
                case AUTH -> Platform.runLater(controller::closeAuth);
            }
        }

        if (sType == Stages.GET_FILE_NAME_LENGTH) {
            if (byteBuf.readableBytes() < 4) {
                return;
            }
            fileNameLength = byteBuf.readInt();
            sType = Stages.GET_FILE_NAME;
        }

        if (sType == Stages.GET_FILE_NAME) {
            if (byteBuf.readableBytes() < fileNameLength) {
                return;
            }
            byte[] fileNameArr = new byte[fileNameLength];
            byteBuf.readBytes(fileNameArr);
            fileName = new String(fileNameArr);
            switch (uType) {
                case LIST -> {
                    controller.refreshServerFilesList(fileName.split(" "));
                    sType = Stages.GET_COMMAND;
                }
                case FILE -> {
                    path = Paths.get("client_storage/" + fileName);
                    sType = Stages.GET_FILE_LENGTH;
                }
                case WARNING -> {
                    Platform.runLater(() -> controller.showWarning(fileName));
                    sType = Stages.GET_COMMAND;
                }
            }
        }

        if (sType == Stages.GET_FILE_LENGTH) {
            if (byteBuf.readableBytes() < 8) {
                return;
            }
            fileReqLength = byteBuf.readLong();
            loadedLength = 0;
            clientHandler.createFile(path);
            bos = new BufferedOutputStream(new FileOutputStream(path.toString(), true));
            sType = Stages.GET_FILE;
        }

        if (sType == Stages.GET_FILE) {
            while (byteBuf.readableBytes() > 0 && loadedLength < fileReqLength) {
                bos.write(byteBuf.readByte());
                loadedLength++;
            }
            if (loadedLength < fileReqLength) {
                return;
            }
            bos.flush();
            byteBuf.release();
            bos.close();
            controller.updateLocalFilesList();
            sType = Stages.GET_COMMAND;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
