package org.coralibre.android.sdk.internal.crypto.ppcp;

import java.util.ArrayList;
import java.util.List;

import static org.coralibre.android.sdk.internal.crypto.ppcp.CryptoModule.FUZZY_COMPARE_TIME_DEVIATION;
import static org.coralibre.android.sdk.internal.crypto.ppcp.CryptoModule.generateRPI;
import static org.coralibre.android.sdk.internal.crypto.ppcp.CryptoModule.generateRPIK;
import static org.coralibre.android.sdk.internal.crypto.ppcp.TemporaryExposureKey.TEK_ROLLING_PERIOD;
import static org.coralibre.android.sdk.internal.crypto.ppcp.TemporaryExposureKey.getMidnight;

public class ExposeChecker {
    public static List<RollingProximityIdentifier> generateAllRPIForADay(TemporaryExposureKey tek) {
        final long enTimestamp = tek.getTimestamp().get();
        List<RollingProximityIdentifier> rpiList = new ArrayList<>(TemporaryExposureKey.TEK_ROLLING_PERIOD);
        RollingProximityIdentifierKey rpik = generateRPIK(tek);
        for(long i = 0; i < TemporaryExposureKey.TEK_ROLLING_PERIOD; i++) {
            rpiList.add(generateRPI(rpik, new ENNumber(enTimestamp + i)));
        }
        return rpiList;
    }


    private static List<TemporaryExposureKey> getMatchingTEKs(List<TemporaryExposureKey> allTEKs,
                                                              ENNumber timestamp) {
        List<TemporaryExposureKey> relatedTEKs = new ArrayList<>();
        for(TemporaryExposureKey key : allTEKs) {
            if(key.getTimestamp().equals(timestamp)) {
                relatedTEKs.add(key);
            }
        }
        return relatedTEKs;
    }

    private static List<TemporaryExposureKey> getAllRelatedTEKs(List<TemporaryExposureKey> allTEKs,
                                                                ENNumber timestamp) {
        ENNumber slotBeginning = getMidnight(
                new ENNumber(timestamp.get() - FUZZY_COMPARE_TIME_DEVIATION));
        ENNumber slotEnding = getMidnight(
                new ENNumber(timestamp.get() + FUZZY_COMPARE_TIME_DEVIATION));
        List<TemporaryExposureKey> relatedTeKs = getMatchingTEKs(allTEKs, slotBeginning);
        if(!slotBeginning.equals(slotEnding)) {
            relatedTeKs.addAll(getMatchingTEKs(allTEKs, slotEnding));
        }
        return relatedTeKs;
    }

    private static List<RollingProximityIdentifier> generateRPIs(TemporaryExposureKey tek, ENNumber timestamp) {
        long slotBeginning = timestamp.get() - FUZZY_COMPARE_TIME_DEVIATION;
        if(slotBeginning < TemporaryExposureKey.getMidnight(slotBeginning)) {
            slotBeginning = TemporaryExposureKey.getMidnight(slotBeginning);
        }
        long slotEnding = timestamp.get() + FUZZY_COMPARE_TIME_DEVIATION;
        if(slotEnding > TemporaryExposureKey.getMidnight(slotEnding) + TEK_ROLLING_PERIOD) {
            slotEnding = TemporaryExposureKey.getMidnight(slotBeginning) + TEK_ROLLING_PERIOD;
        }
        RollingProximityIdentifierKey rpik = generateRPIK(tek);
        List<RollingProximityIdentifier> generatedRPIs =
                new ArrayList<>(2 * FUZZY_COMPARE_TIME_DEVIATION);

        for(long i = slotBeginning; i < slotEnding; i++) {
            generatedRPIs.add(generateRPI(rpik, new ENNumber(i)));
        }
        return generatedRPIs;
    }
}
