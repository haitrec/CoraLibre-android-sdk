package org.coralibre.android.sdk.internal.crypto.ppcp;

import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

import static org.coralibre.android.sdk.internal.crypto.ppcp.CryptoModule.FUZZY_COMPARE_TIME_DEVIATION;
import static org.coralibre.android.sdk.internal.crypto.ppcp.CryptoModule.generateRPI;
import static org.coralibre.android.sdk.internal.crypto.ppcp.CryptoModule.generateRPIK;
import static org.coralibre.android.sdk.internal.crypto.ppcp.TemporaryExposureKey.TEK_ROLLING_PERIOD;
import static org.coralibre.android.sdk.internal.crypto.ppcp.TemporaryExposureKey.getMidnight;

public class ExposeChecker {
    public static List<RollingProximityIdentifier> generateAllRPIForADay(TemporaryExposureKey tek) {
        final long enInterval = tek.getInterval().get();
        List<RollingProximityIdentifier> rpiList = new ArrayList<>(TemporaryExposureKey.TEK_ROLLING_PERIOD);
        RollingProximityIdentifierKey rpik = generateRPIK(tek);
        for(long i = 0; i < TemporaryExposureKey.TEK_ROLLING_PERIOD; i++) {
            rpiList.add(generateRPI(rpik, new ENNumber(enInterval + i)));
        }
        return rpiList;
    }


    private static List<TemporaryExposureKey> getMatchingTEKs(List<TemporaryExposureKey> allTEKs,
                                                              ENNumber Interval) {
        List<TemporaryExposureKey> relatedTEKs = new ArrayList<>();
        for(TemporaryExposureKey key : allTEKs) {
            if(key.getInterval().equals(Interval)) {
                relatedTEKs.add(key);
            }
        }
        return relatedTEKs;
    }

    public static List<TemporaryExposureKey> getAllRelatedTEKs(List<TemporaryExposureKey> allTEKs,
                                                                ENNumber Interval) {
        ENNumber slotBeginning = getMidnight(
                new ENNumber(Interval.get() - FUZZY_COMPARE_TIME_DEVIATION));
        ENNumber slotEnding = getMidnight(
                new ENNumber(Interval.get() + FUZZY_COMPARE_TIME_DEVIATION));
        List<TemporaryExposureKey> relatedTeKs = getMatchingTEKs(allTEKs, slotBeginning);
        if(!slotBeginning.equals(slotEnding)) {
            relatedTeKs.addAll(getMatchingTEKs(allTEKs, slotEnding));
        }
        return relatedTeKs;
    }

    private static List<RollingProximityIdentifier> generateRPIs(TemporaryExposureKey tek, ENNumber Interval) {
        long slotBeginning = Interval.get() - FUZZY_COMPARE_TIME_DEVIATION;
        if(slotBeginning < TemporaryExposureKey.getMidnight(slotBeginning)) {
            slotBeginning = TemporaryExposureKey.getMidnight(slotBeginning);
        }
        long slotEnding = Interval.get() + FUZZY_COMPARE_TIME_DEVIATION;
        if(slotEnding > TemporaryExposureKey.getMidnight(slotEnding) + TEK_ROLLING_PERIOD) {
            slotEnding = TemporaryExposureKey.getMidnight(slotBeginning) + TEK_ROLLING_PERIOD;
        }
        RollingProximityIdentifierKey rpik = generateRPIK(tek);
        List<RollingProximityIdentifier> generatedRPIs =
                new ArrayList<>(2 * FUZZY_COMPARE_TIME_DEVIATION + 1);

        for(long i = slotBeginning; i <= slotEnding; i++) {
            generatedRPIs.add(generateRPI(rpik, new ENNumber(i)));
        }
        return generatedRPIs;
    }

    private static List<Pair<TemporaryExposureKey, RollingProximityIdentifier>>
        findMatches(List<TemporaryExposureKey> teks, List<RollingProximityIdentifier> collectedRPIs) {
        //TODO: Do dynamic programing foo and use a cache
        List<Pair<TemporaryExposureKey, RollingProximityIdentifier>> matchingKeys = new
                ArrayList<>();
        for(RollingProximityIdentifier crpi : collectedRPIs) {
            List<TemporaryExposureKey> relatedTeks = getAllRelatedTEKs(teks, crpi.getInterval());
            for(TemporaryExposureKey tek : relatedTeks) {
                List<RollingProximityIdentifier> generatedRPIs = generateRPIs(tek, crpi.getInterval());
                for(RollingProximityIdentifier grpi :  generatedRPIs) {
                    if(grpi.equals(crpi)) {
                        matchingKeys.add(new Pair<>(tek, crpi));
                    }
                }
            }
        }
        return matchingKeys;
    }
}
