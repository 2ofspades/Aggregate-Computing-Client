package client.controller.data.networkdataclass

import java.io.Serializable
import java.util.*

class ProfileNetwork(
    val userUUID: UUID,
    val username: String,
    val interest: String,
    val image: Serializable?
) : Serializable, Comparable<ProfileNetwork> {
    /**
     * Compares this object with the specified object for order. Returns zero if this object is equal
     * to the specified [other] object, a negative number if it's less than [other], or a positive number
     * if it's greater than [other].
     */
    override fun compareTo(other: ProfileNetwork): Int {
        return userUUID.compareTo(other.userUUID)
    }

    override fun equals(other: Any?): Boolean {
        return when (other){
            is ProfileNetwork -> userUUID == other.userUUID
            else -> super.equals(other)
        }
    }

    override fun hashCode(): Int {
        return userUUID.hashCode()
    }


}