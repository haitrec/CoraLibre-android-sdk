package org.coralibre.android.sdk.internal.crypto;

import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.coralibre.android.sdk.internal.crypto.ppcp.ENNumber;
import org.coralibre.android.sdk.internal.crypto.ppcp.ExposeChecker;
import org.coralibre.android.sdk.internal.crypto.ppcp.RollingProximityIdentifier;
import org.coralibre.android.sdk.internal.crypto.ppcp.TemporaryExposureKey;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.coralibre.android.sdk.internal.crypto.ppcp.RollingProximityIdentifier.RPI_SIZE;
import static org.coralibre.android.sdk.internal.crypto.ppcp.TemporaryExposureKey.TEK_LENGTH;
import static org.coralibre.android.sdk.internal.crypto.ppcp.TemporaryExposureKey.TEK_ROLLING_PERIOD;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class ExposeCheckerTests {
    private static byte[] hex2byte(String hex) {
        return new BigInteger(hex,16).toByteArray();
    }

    private static TemporaryExposureKey tek(long whichRollingPeriod, String hex) {
        assertEquals(2*TEK_LENGTH, hex.length());
        return new TemporaryExposureKey(new ENNumber(whichRollingPeriod * TEK_ROLLING_PERIOD),
                hex2byte(hex));
    }

    private static RollingProximityIdentifier rollingProximityIdentifier(long rawENNumber, String hex) {
        assertEquals(2*RPI_SIZE, hex.length());
        return new RollingProximityIdentifier(hex2byte(hex), new ENNumber(rawENNumber));
    }

    private static final List<TemporaryExposureKey> TEK_LIST =
            new ArrayList<>(Arrays.asList(
                    tek(10, "11111111111111111111111111111111"),
                    tek(11, "22222222222222222222222222222222"),
                    tek(12, "33333333333333333333333333333333"),
                    tek(13, "44444444444444444444444444444444"),
                    tek(14, "55555555555555555555555555555555"),
                    tek(15, "66666666666666666666666666666666"),
                    tek(16, "77777777777777777777777777777777"),
                    tek(17, "88888888888888888888888888888888"),
                    tek(18, "99999999999999999999999999999999"),
                    tek(19, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")));

    private static final ENNumber MIDDLE_OF_DAY_4 =
            new ENNumber((long)(13.5 * TEK_ROLLING_PERIOD));
    private static final ENNumber ONE_HOUR_INTO_DAY_5 =
            new ENNumber((long)(14 * TEK_ROLLING_PERIOD + 6));

    private static final long SLOTSTART_MIDDLE_OF_DAY4 = (long)(13.5 * TEK_ROLLING_PERIOD - 12);
    private static final long SLOTEND_MIDDLE_OF_DAY4 = (long)(13.5 * TEK_ROLLING_PERIOD + 12);

    private static final long SLOTSTART_ONE_HOUR_INTO_DAY5 = 14 * TEK_ROLLING_PERIOD;
    private static final long SLOTEND_ONE_HOUR_INTO_DAY5 = 14 * TEK_ROLLING_PERIOD + 6 + 12;
    private static final long SLOTSTART_END_DAY4 = 14 * TEK_ROLLING_PERIOD + 6 - 12;
    private static final long SLOTEND_END_DAY4 = SLOTSTART_ONE_HOUR_INTO_DAY5;

    @Test
    public void testGetAllRelatedTEKsDuringDay() {
        List<TemporaryExposureKey> tekSubSet =
                ExposeChecker.getAllRelatedTEKs(TEK_LIST, MIDDLE_OF_DAY_4);
        assertEquals(1, tekSubSet.size());
        assertArrayEquals(TEK_LIST.get(3).getKey(), tekSubSet.get(0).getKey());
    }

    @Test
    public void testGetAllRelatedTEKsAtBeginningOfDay() {
        List<TemporaryExposureKey> tekSubSet =
                ExposeChecker.getAllRelatedTEKs(TEK_LIST, ONE_HOUR_INTO_DAY_5);
        assertEquals(2, tekSubSet.size());
        assertArrayEquals(TEK_LIST.get(3).getKey(), tekSubSet.get(0).getKey());
        assertArrayEquals(TEK_LIST.get(4).getKey(), tekSubSet.get(1).getKey());
    }

    @Test
    public void testGenerateRPIsForSlotDuringDay() {
        List<RollingProximityIdentifier> genrpis =
                ExposeChecker.generateRPIsForSlot(TEK_LIST.get(3), MIDDLE_OF_DAY_4);
        assertEquals(25, genrpis.size());

        int i = 0;
        for(long interv = SLOTSTART_MIDDLE_OF_DAY4;
            interv <= SLOTEND_MIDDLE_OF_DAY4;
            interv++) {
            assertEquals("For RPI: " + i + " the interval does not fit.",
                    interv,
                    genrpis.get(i).getInterval().get());
            i++;
        }
    }

    @Test
    public void testGenerateRPIsForSlotAtBeginningOfDay() {
        List<RollingProximityIdentifier> genrpis =
                ExposeChecker.generateRPIsForSlot(TEK_LIST.get(4), ONE_HOUR_INTO_DAY_5);
        assertEquals(19, genrpis.size());

        int i = 0;
        for(long interv = SLOTSTART_ONE_HOUR_INTO_DAY5;
            interv <= SLOTEND_ONE_HOUR_INTO_DAY5;
            interv++) {
            assertEquals("For RPI: " + i + " the interval does not fit.",
                    interv,
                    genrpis.get(i).getInterval().get());
            i++;
        }
    }

    @Test
    public void testGenerateRPIsForSlotAtEndingOfDay() {
        List<RollingProximityIdentifier> genrpis =
                ExposeChecker.generateRPIsForSlot(TEK_LIST.get(3), ONE_HOUR_INTO_DAY_5);
        assertEquals(7, genrpis.size());

        int i = 0;
        for(long interv = SLOTSTART_END_DAY4;
            interv <= SLOTEND_END_DAY4;
            interv++) {
            assertEquals("For RPI: " + i + " the interval does not fit.",
                    interv,
                    genrpis.get(i).getInterval().get());
            i++;
        }
    }
}
