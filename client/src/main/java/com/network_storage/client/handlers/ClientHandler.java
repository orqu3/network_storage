package com.network_storage.client.handlers;

import com.network_storage.client.network.Network;
import com.network_storage.client_server.Commands;
import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.netty.buffer.Unpooled.buffer;

public class ClientHandler {


    private final String CLIENT_PATH = "client_storage/";
    private final Network network;

    public ClientHandler(Network network){
        this.network = network;
    }

    public String getCLIENT_PATH() {
        return CLIENT_PATH;
    }

    public void createFile(Path path){
        try{
            Files.deleteIfExists(path);
            Files.createFile(path);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void writeFile(String fileName){
        try {
            Path path = Paths.get(CLIENT_PATH + fileName) ;
            if (Files.exists(path)){
                InputStream inputStream = Files.newInputStream(path);
                byte[] data = new byte[1024];
                while (inputStream.available() >= 1024){
                    inputStream.read(data);
                    sendData(data);
                }
                byte[] dataEnd = new byte[inputStream.available()];
                while (inputStream.available() > 0){
                    inputStream.read(dataEnd);
                    sendData(dataEnd);
                }
                inputStream.close();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void sendData(byte[] data){
        ByteBuf buf = buffer(1024);
        buf.writeBytes(data);
        network.getCurrentChannel().writeAndFlush(buf);
    }

    public void sendCommand(Commands commandType, String commandInfo){
        ByteBuf buf = buffer(1024);
        try {
            buf.writeByte(Commands.getByteFromType(commandType));
            buf.writeInt(commandInfo.getBytes().length);
            buf.writeBytes(commandInfo.getBytes());
            if (commandType == Commands.UPLOAD) buf.writeLong(Files.size(Paths.get("client_storage/" + commandInfo)));
        } catch (IOException e){
            e.printStackTrace();
        }
        network.getCurrentChannel().writeAndFlush(buf);
    }

    public void sendShortCommand(Commands commandType){
        ByteBuf buf = buffer(1);
        buf.writeByte(Commands.getByteFromType(commandType));
        network.getCurrentChannel().writeAndFlush(buf);
    }
}
