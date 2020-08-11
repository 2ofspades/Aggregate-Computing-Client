package client.controller.protelis


import org.protelis.lang.datatype.DeviceUID
import java.util.*

class UserUID(val uuid: UUID = UUID.randomUUID()): DeviceUID {

    override fun equals(other: Any?): Boolean {
        return when(other){
            is UserUID -> uuid == other.uuid
            else -> super.equals(other)
        }
    }

    override fun hashCode(): Int {
        return uuid.hashCode()
    }
}