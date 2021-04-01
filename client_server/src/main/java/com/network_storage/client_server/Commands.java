package com.network_storage.client_server;

public enum Commands {
    EMPTY((byte) -1),
    FILE((byte) 15),
    DOWNLOAD((byte) 16),
    UPLOAD((byte) 17),
    LIST((byte) 18),
    DELETE((byte) 19),
    END((byte) 20),
    AUTH((byte) 21),
    REG((byte) 22),
    CREATE((byte) 23),
    FORWARD((byte) 24),
    BACK((byte) 25),
    EXIT((byte) 26),
    WARNING((byte) 27);

    byte firstByteMessage;

    Commands(byte firstByteMessage) {
        this.firstByteMessage = firstByteMessage;
    }

    public static Commands getTypeFromByte(byte b) {
        if (b == FILE.firstByteMessage) return FILE;
        if (b == DOWNLOAD.firstByteMessage) return DOWNLOAD;
        if (b == UPLOAD.firstByteMessage) return UPLOAD;
        if (b == LIST.firstByteMessage) return LIST;
        if (b == DELETE.firstByteMessage) return DELETE;
        if (b == END.firstByteMessage) return END;
        if (b == AUTH.firstByteMessage) return AUTH;
        if (b == REG.firstByteMessage) return REG;
        if (b == CREATE.firstByteMessage) return CREATE;
        if (b == FORWARD.firstByteMessage) return FORWARD;
        if (b == BACK.firstByteMessage) return BACK;
        if (b == EXIT.firstByteMessage) return EXIT;
        if (b == WARNING.firstByteMessage) return WARNING;
        else return EMPTY;
    }

    public static byte getByteFromType(Commands type) {
        return switch (type) {
            case FILE -> (byte)15;
            case DOWNLOAD -> (byte)16;
            case UPLOAD -> (byte)17;
            case LIST -> (byte)18;
            case DELETE -> (byte)19;
            case END -> (byte)20;
            case AUTH -> (byte)21;
            case REG -> (byte)22;
            case CREATE -> (byte)23;
            case FORWARD -> (byte)24;
            case BACK -> (byte)25;
            case EXIT -> (byte)26;
            case WARNING -> (byte)27;
            default -> (byte)-1;
        };
    }
}

