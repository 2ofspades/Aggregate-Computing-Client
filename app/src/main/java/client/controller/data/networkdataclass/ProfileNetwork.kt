package client.controller.data.networkdataclass

import java.io.Serializable

class ProfileNetwork(
    val userID: Int,
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
        return userID.compareTo(other.userID)
    }

    override fun equals(other: Any?): Boolean {
        val otherProfile = other as? ProfileNetwork
        return if (otherProfile != null)
            this.userID == otherProfile.userID
        else
            super.equals(other)
    }

    override fun hashCode(): Int {
        return userID
    }
}