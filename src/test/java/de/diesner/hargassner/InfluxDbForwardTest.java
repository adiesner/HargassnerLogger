package de.diesner.hargassner;

import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class InfluxDbForwardTest {

    @Test
    public void regexTest() {
        Pattern numberOnly = Pattern.compile("^[0-9.]+$");

        assertThat("10", numberOnly.matcher("10").matches(), equalTo(true));
        assertThat("10.0", numberOnly.matcher("10.0").matches(), equalTo(true));
        assertThat("0.0", numberOnly.matcher("0.0").matches(), equalTo(true));
        assertThat("10F", numberOnly.matcher("10F").matches(), equalTo(false));

    }

}
