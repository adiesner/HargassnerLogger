package de.diesner.hargassner;

import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class DataBufferTest {

    @Test
    public void testGet() throws UnsupportedEncodingException {
        DataBuffer buffer = new DataBuffer(1000);
        buffer.add("This is some test data".getBytes("UTF-8"));
        assertThat("Bytes available", buffer.getBytesAvailable(), equalTo(22));
        byte[] bytes = buffer.read(2, 7);
        assertThat("Buffer content", new String(bytes), equalTo("is is"));
        assertThat("Bytes available", buffer.getBytesAvailable(), equalTo(15));
        bytes = buffer.read(1, 5);
        assertThat("Buffer content", new String(bytes), equalTo("some"));
        assertThat("Bytes available", buffer.getBytesAvailable(), equalTo(10));
    }

    @Test
    public void testIndexOf() throws UnsupportedEncodingException {
        DataBuffer buffer = new DataBuffer(1000);
        buffer.add("This is some test data".getBytes("UTF-8"));
        assertThat("Index of i", buffer.indexOf('i', 0), equalTo(2));
        assertThat("Index of i", buffer.indexOf('i', 3), equalTo(5));
        int firstSpace = buffer.indexOf(' ', 0);
        int secondSpace = buffer.indexOf(' ', firstSpace + 1);
        assertThat("Index of Space", firstSpace, equalTo(4));
        assertThat("Index of Space", secondSpace, equalTo(7));
        byte[] bytes = buffer.read(firstSpace + 1, secondSpace);
        assertThat("Buffer content", new String(bytes), equalTo("is"));
        buffer.add("01234567890".getBytes("UTF-8"));
    }

    @Test
    public void testIndexOfBoundaries() throws UnsupportedEncodingException {
        DataBuffer buffer = new DataBuffer(10);
        buffer.add("0123456789".getBytes("UTF-8"));
        assertThat("Index of 0", buffer.indexOf('0', 0), equalTo(0));
        assertThat("Index of 0 after 2", buffer.indexOf('0', 2), equalTo(-1));
        assertThat("Index of 5", buffer.indexOf('5', 0), equalTo(5));
        assertThat("Index of 9", buffer.indexOf('9', 0), equalTo(9));
        assertThat("Index of 9", buffer.indexOf('9', 5), equalTo(9));
        buffer.read(0, 5);
        assertThat("Index of 5", buffer.indexOf('5', 0), equalTo(0));
        assertThat("Index of 5 after 1", buffer.indexOf('5', 1), equalTo(-1));
        buffer.add("ABC".getBytes("UTF-8"));
        assertThat("Index of A", buffer.indexOf('A', 0), equalTo(5));
    }

    @Test
    public void testIndexOfBorderCharacters() throws UnsupportedEncodingException {
        DataBuffer buffer = new DataBuffer(10);
        buffer.add("0123456789".getBytes("UTF-8"));
        assertThat("Index of 0", buffer.indexOf('0', 0), equalTo(0));
        assertThat("Index of 9", buffer.indexOf('9', 0), equalTo(9));
    }

    @Test
    public void testExactlyMaximumBytesAdded() {
        DataBuffer buffer = new DataBuffer(25);
        for (int i = 0; i < 5; i++) {
            byte[] bytes = new byte[5];
            Arrays.fill(bytes, (byte) (0x30 + i));
            buffer.add(bytes);
        }
        assertThat("Bytes available", buffer.getBytesAvailable(), equalTo(25));
        byte[] bytes = buffer.read(0, 25);
        assertThat("Content", new String(bytes), equalTo("0000011111222223333344444"));
    }

    @Test
    public void testBufferOverflow() {
        DataBuffer buffer = new DataBuffer(25);
        for (int i = 0; i < 6; i++) {
            byte[] bytes = new byte[5];
            Arrays.fill(bytes, (byte) (0x30 + i));
            buffer.add(bytes);
        }
        byte[] bytes = buffer.read(0, 25);
        assertThat("Content", new String(bytes), equalTo("1111122222333334444455555"));
    }

    @Test
    public void testAddTooLargeBuffer() throws UnsupportedEncodingException {
        DataBuffer buffer = new DataBuffer(25);
        buffer.add("This is some test data1".getBytes("UTF-8"));
        buffer.add("This is some test data2".getBytes("UTF-8"));
        assertThat("Bytes available", buffer.getBytesAvailable(), equalTo(25));
        String content = new String(buffer.read(0, 25));
        assertThat("Content", content, equalTo("a1This is some test data2"));
        assertThat("Bytes available", buffer.getBytesAvailable(), equalTo(0));
    }

    @Test
    public void testAddDataInTooSmallBuffer() throws UnsupportedEncodingException {
        DataBuffer buffer = new DataBuffer(10);
        buffer.add("This is some test data".getBytes("UTF-8"));
        String content = new String(buffer.read(0, 10));
        assertThat("Content", content, equalTo(" test data"));
    }

    @Test
    public void testInternalBufferMoveWithPartialRead() throws UnsupportedEncodingException {
        DataBuffer buffer = new DataBuffer(25);
        buffer.add("1111111111".getBytes("UTF-8"));
        buffer.add("2222222222".getBytes("UTF-8"));
        String content = new String(buffer.read(0, 10));
        assertThat("Content", content, equalTo("1111111111"));
        buffer.add("3333333333".getBytes("UTF-8"));
        assertThat("Bytes available", buffer.getBytesAvailable(), equalTo(20));
        assertThat("Index of 2", buffer.indexOf('2', 0), equalTo(0));
        assertThat("Index of 3", buffer.indexOf('3', 0), equalTo(10));
        content = new String(buffer.read(0, 20));
        assertThat("Content", content, equalTo("22222222223333333333"));
        assertThat("Bytes available", buffer.getBytesAvailable(), equalTo(0));
    }


}