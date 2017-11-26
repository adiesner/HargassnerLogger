package de.diesner.hargassner;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataBuffer {
    private byte[] buffer;
    private int startIndex;
    private int endIndex;

    public DataBuffer(int size) {
        buffer = new byte[size];
    }

    /**
     * Returns first found index of searchByte in buffer after startPos. If not found returns -1;
     *
     * @param searchByte
     * @param startPos
     * @return
     */
    public int indexOf(final byte searchByte, final int startPos) {
        if ((startPos < 0) || (startPos + startIndex > endIndex)) {
            return -1;
        }
        for (int i = startIndex + startPos; i < endIndex; i++) {
            if (buffer[i] == searchByte) {
                return i - startIndex;
            }
        }
        return -1;
    }

    public int indexOf(final char searchChar, final int startPos) {
        return indexOf((byte) searchChar, startPos);
    }

    /**
     * Add byte buffer to current buffer
     *
     * @param data
     */
    public void add(final byte[] data) {
        add(data, data.length);
    }

    /**
     * Add byte buffer to current buffer
     *
     * @param data
     */
    public void add(final byte[] data, int length) {
        int startPos = 0;
        int bytesToCopy = length;

        if (length <= 0) {
            return;
        }

        if (length > buffer.length) {
            log.warn("Losing {} bytes from added buffer!", length - buffer.length);
            System.arraycopy(data, length - buffer.length, buffer, 0, buffer.length);
            startIndex = 0;
            endIndex = length;
            return;
        }

        int freeAtEnd = buffer.length - endIndex;
        if (bytesToCopy > freeAtEnd) {
            // need to move
            int lostBytes = 0;
            if (startIndex + freeAtEnd < bytesToCopy) {
                lostBytes = bytesToCopy - (startIndex + freeAtEnd);
                log.warn("Losing {} bytes from input buffer!", lostBytes);
            }

            System.arraycopy(buffer, startIndex + lostBytes, buffer, 0, endIndex - startIndex - lostBytes);
            endIndex = endIndex - startIndex - lostBytes;
            startIndex = 0;
        }

        if (endIndex + bytesToCopy > buffer.length) {
            startPos = buffer.length - (endIndex + bytesToCopy);
            log.warn("Losing {} bytes from input buffer - added buffer is too large!", startPos);
        }
        System.arraycopy(data, startPos, buffer, endIndex, bytesToCopy);
        endIndex += bytesToCopy;
    }


    public byte[] read(int startPos, int endPos) {
        assert (startPos < endPos);
        assert (startPos >= 0);
        assert (endPos >= 0);
        byte[] data = new byte[endPos - startPos];
        System.arraycopy(buffer, startIndex + startPos, data, 0, data.length);
        if (startPos > 2) {
            log.debug("Deleting unread data: {}", startPos);
        }
        startIndex += endPos;
        return data;
    }

    public int getBytesAvailable() {
        return endIndex - startIndex;
    }
}
