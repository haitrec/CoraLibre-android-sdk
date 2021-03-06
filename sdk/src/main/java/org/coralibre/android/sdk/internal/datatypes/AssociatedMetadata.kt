package org.coralibre.android.sdk.internal.datatypes

import org.coralibre.android.sdk.internal.EnFrameworkConstants
import java.security.InvalidParameterException

class AssociatedMetadata {
    private val mutData = ByteArray(EnFrameworkConstants.AEM_LENGTH)
    val data: ByteArray
        get() {
            val retVal = ByteArray(EnFrameworkConstants.AEM_LENGTH)
            System.arraycopy(mutData, 0, retVal, 0, EnFrameworkConstants.AEM_LENGTH)
            return retVal
        }

    constructor(majorVersion: Int, minorVersion: Int, powerLevel: Int) {
        if (majorVersion < 0 || majorVersion >= 4) throw InvalidParameterException("Major version out of bound")
        if (minorVersion < 0 || minorVersion >= 4) throw InvalidParameterException("Minor version out of bound")
        if (powerLevel < -127 || powerLevel > 127) throw InvalidParameterException("Power level out of bound")
        mutData[VERSIONING_BYTE] = (
            ((majorVersion and 3) shl MAJOR_BIT_POS) or ((minorVersion and 3) shl MINOR_BIT_POS)
            ).toByte()
        mutData[POWERLEVEL_BYTE] = powerLevel.toByte()
    }

    constructor(rawAM: ByteArray) {
        if (rawAM.size != EnFrameworkConstants.AEM_LENGTH) throw InvalidParameterException("rawAEM not the right length")
        System.arraycopy(rawAM, 0, mutData, 0, EnFrameworkConstants.AEM_LENGTH)
    }

    private fun Byte.unsignedToInt(): Int {
        return (toInt() and 0xFF)
    }

    private infix fun Byte.shr(bitCount: Int): Int {
        return (unsignedToInt() shr bitCount)
    }

    val majorVersion: Int
        get() = (mutData[VERSIONING_BYTE] shr MAJOR_BIT_POS) and 3
    val minorVersion: Int
        get() = (mutData[VERSIONING_BYTE] shr MINOR_BIT_POS) and 3
    val transmitPowerLevel: Int
        get() = mutData[POWERLEVEL_BYTE].toInt()

    companion object {
        private const val VERSIONING_BYTE = 0
        private const val POWERLEVEL_BYTE = 1
        private const val MAJOR_BIT_POS = 6
        private const val MINOR_BIT_POS = 4
    }
}
